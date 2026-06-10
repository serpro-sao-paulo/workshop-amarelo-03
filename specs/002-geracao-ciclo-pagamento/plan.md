# Implementation Plan: Geração do Ciclo Mensal de Pagamento

**Branch**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-geracao-ciclo-pagamento/spec.md`

## Summary

Gerar a folha mensal de pagamentos: para uma competência (AAAAMM), percorrer os beneficiários
na ordenação determinística do descritor, considerar apenas os ACTIVE (A), recalcular o
benefício (delegando à capacidade de cálculo — REQ-007/008) e aplicar os descontos (REQ-009/010),
criando um registro de pagamento por beneficiário elegível. O ciclo é **retomável/idempotente
por beneficiário** (commit individual; sem duplicata por competência), aceita a competência como
parâmetro (default = vigente, recusa futuras) e **trunca** valores em 2 casas decimais. Cada
pagamento emite um evento de auditoria; logs mascaram CPF/valores.

Abordagem técnica: módulo `payment` do Modular Monolith (Spring Boot 3.3 + JPA), expondo
`PaymentFacade.generateMonthlyCycle(YearMonth)`, consumindo `BeneficiaryFacade`/`ProgramFacade`
(síncrono) e publicando eventos para `AuditFacade` (assíncrono). Endpoint `POST /api/v1/payment-cycles`.

## Technical Context

**Language/Version**: Java 21 (records, sealed types, pattern matching, virtual threads)

**Primary Dependencies**: Spring Boot 3.3, Spring Data JPA / Hibernate, Spring Modulith, Bean Validation

**Storage**: PostgreSQL 16 — tabelas `payment`, `payment_deduction`, `payment_cycle`; leitura de `beneficiary`, `social_program` via facades

**Testing**: JUnit 5 + Testcontainers (PostgreSQL real); ArchUnit (enforcement de fronteira de módulo)

**Target Platform**: Linux server (container Docker); single deployable (ADR-001)

**Project Type**: Web service (backend Modular Monolith) — frontend Next.js fora do escopo desta feature

**Performance Goals**: Paridade funcional com o legado; sem NFR de throughput específico na spec

**Constraints**: Valores em `BigDecimal`/`NUMERIC` truncados em 2 casas; `@Transactional` só na service; JPA/JPQL (sem SQL concatenado); CPF/valores mascarados em log

**Scale/Scope**: `PAGAMENTO` ~180M registros (~3,8M/mês); `BENEFICIARIO` ~4,2M. Esta feature foca a geração; particionamento de tabela é decisão de Estágio 3.

### Decisões de design assumidas (não eram clarifications formais)

- **Concorrência**: lock por competência — apenas um ciclo por competência de cada vez; tentativa concorrente é rejeitada. Protege a idempotência (FR-006). *(Recomendação não confirmada pelo usuário — risco aberto.)*

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

> ⚠️ **Constituição não preenchida.** `.specify/memory/constitution.md` ainda contém os
> placeholders do template (sem princípios ratificados). Não há gates formais a avaliar.
> Na ausência da constituição, aplico as **regras do projeto** de
> `.github/copilot-instructions.md` como gates substitutos:

| Regra do projeto (substituta) | Aderência do plano |
| --- | --- |
| Java 21; records p/ DTOs; `Optional` em retornos públicos | ✅ DTOs como records; `Optional<PaymentView>` |
| `@Transactional` só em service | ✅ `PaymentCycleService`; commit por beneficiário |
| Validação na controller com `@Valid` | ✅ `CycleRequest` validado |
| REST `/api/v1/{resource}`, status corretos, OpenAPI | ✅ `POST /api/v1/payment-cycles` (201) |
| JPA/JPQL, sem concatenação SQL | ✅ Spring Data repositories |
| Mascarar CPF/valores em log | ✅ FR-010 |
| Rastreabilidade `source_legacy:` | ✅ todos os FRs |
| Testes junto à implementação (TDD) | ✅ Testcontainers + unit |

**Resultado do gate**: PASS (substituto). Recomenda-se rodar `/speckit.constitution` antes do Estágio 3.

## Project Structure

### Documentation (this feature)

```text
specs/002-geracao-ciclo-pagamento/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── contracts/           # Phase 1 output
    └── payment-cycles.openapi.yaml
```

### Source Code (repository root)

Código da nova aplicação gerado no Estágio 3 sob `03-implementacao/`, package-by-feature do módulo `payment`:

```text
03-implementacao/generated/src/main/java/br/gov/serpro/sifap/payment/
├── api/                          # PaymentCycleController (POST /api/v1/payment-cycles)
├── domain/                       # Payment, PaymentDeduction, PaymentCycle, PaymentStatus
├── service/                      # PaymentCycleService (orquestra; commit por beneficiário)
├── repository/                   # PaymentRepository, PaymentCycleRepository
└── PaymentFacade.java            # generateMonthlyCycle(YearMonth)

03-implementacao/generated/src/main/resources/db/migration/
└── V00X__payment_cycle.sql       # payment, payment_deduction, payment_cycle

03-implementacao/generated/src/test/java/br/gov/serpro/sifap/payment/
├── PaymentCycleServiceTest.java          # unit (filtro status, idempotência, truncamento)
└── PaymentCycleIntegrationTest.java      # Testcontainers (ordenação, totalizadores, retry)
```

> Nota: o terminal já criou `.../br/gov/client/sifap/payment/...`. Há divergência de base package
> (`br.gov.client` vs. `br.gov.serpro` do design). **Item em aberto** — alinhar antes do Estágio 3.

**Structure Decision**: Web service, módulo único `payment` do Modular Monolith. Cross-context
somente via facades (`BeneficiaryFacade`, `ProgramFacade`, `AuditFacade`), enforced por Spring
Modulith + ArchUnit.

## Complexity Tracking

| Violação / Risco | Por que necessário | Alternativa mais simples rejeitada porque |
|-----------|------------|-------------------------------------|
| Lock por competência | Proteger idempotência (FR-006) contra corrida | Só unique constraint deixaria corridas virarem exceções não tratadas |
| Commit por beneficiário (não transação global) | Retomabilidade sobre base grande (Q1) | Transação única sobre milhões de linhas é inviável e não retomável |

## Riscos / Open Questions que afetam o plano

| ID | Risco | Impacto |
| --- | --- | --- |
| MYS-001 | Constante FATOR-K sem origem | Bloqueia **paridade numérica** (não a estrutura) |
| Concorrência | Lock por competência não confirmado | Definir antes do Estágio 3 |
| Base package | `br.gov.client` vs. `br.gov.serpro` | Alinhar antes de gerar código |
| Constituição | Template não preenchido | Gate formal ausente; rodar `/speckit.constitution` |
