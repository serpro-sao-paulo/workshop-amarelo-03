---
description: "Task list for Geração do Ciclo Mensal de Pagamento"
---

# Tasks: Geração do Ciclo Mensal de Pagamento

**Input**: Design documents from `/specs/002-geracao-ciclo-pagamento/`

**Prerequisites**: [plan.md](plan.md), [spec.md](spec.md), [research.md](research.md), [data-model.md](data-model.md), [contracts/](contracts/)

**Tests**: INCLUÍDOS — a spec e o plano exigem TDD (Testcontainers + unit; regras financeiras críticas).

**Organization**: Tarefas agrupadas por user story (P1, P2, P3) para implementação e teste independentes.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependências pendentes)
- **[Story]**: User story (US1, US2, US3)
- Caminhos relativos à raiz do repositório

## Path Conventions

Base do módulo `payment` (Estágio 3):
`03-implementacao/generated/src/main/java/br/gov/serpro/sifap/payment/` ·
testes em `.../src/test/java/br/gov/serpro/sifap/payment/` ·
migrations em `.../src/main/resources/db/migration/`.

> ⚠️ **Risco de base package** (plan.md): o terminal criou `br/gov/client/...`; o design usa
> `br.gov.serpro`. T001 alinha isso antes de qualquer código.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicialização do projeto e estrutura básica

- [ ] T001 Alinhar e criar a estrutura de packages do módulo `payment` em `03-implementacao/generated/src/main/java/br/gov/serpro/sifap/payment/{api,domain,service,repository}` (resolver divergência `br.gov.client` vs `br.gov.serpro` registrada em [plan.md](plan.md))
- [ ] T002 Garantir dependências do projeto (Spring Boot 3.3, Spring Data JPA, Spring Modulith, Bean Validation, Flyway) no `pom.xml` do backend gerado
- [ ] T003 [P] Configurar dependências de teste (JUnit 5, Testcontainers PostgreSQL, ArchUnit) no escopo `test` do `pom.xml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Infra que DEVE estar pronta antes de qualquer user story

**⚠️ CRITICAL**: Nenhuma user story começa antes desta fase

- [ ] T004 Criar migration `V00X__payment_cycle.sql` em `03-implementacao/generated/src/main/resources/db/migration/` com tabelas `payment`, `payment_deduction`, `payment_cycle`, unique `(cpf, reference_year_month)` e índice `(reference_year_month, status)` per [data-model.md](data-model.md)
- [ ] T005 [P] Criar enum `PaymentStatus` (P/G/E/C/D/X/R) em `payment/domain/PaymentStatus.java`
- [ ] T006 [P] Criar enum `PaymentCycleStatus` (RUNNING/COMPLETED/FAILED) em `payment/domain/PaymentCycleStatus.java`
- [ ] T007 [P] Criar utilitário de truncamento monetário (`BigDecimal` scale 2, `RoundingMode.DOWN`) em `shared/money/Money.java` (FR-014, research R4)
- [ ] T008 [P] Criar utilitário de mascaramento de CPF/valores em `shared/security/Masking.java` (FR-010, SC-005)
- [ ] T009 Definir as portas (facades consumidas) `BeneficiaryFacade`, `ProgramFacade`, `AuditFacade` como interfaces referenciadas pelo módulo `payment` (somente assinaturas usadas pelo ciclo)

**Checkpoint**: Fundação pronta — user stories podem começar

---

## Phase 3: User Story 1 - Ciclo só para beneficiários ACTIVE (Priority: P1) 🎯 MVP

**Goal**: Gerar um pagamento por beneficiário ACTIVE numa competência, recalculando benefício e aplicando descontos; ignorar não-ACTIVE.

**Independent Test**: 10 ACTIVE + 2 SUSPENDED → ciclo cria exatamente 10 pagamentos, 0 para SUSPENDED ([quickstart.md](quickstart.md) Cenário 1).

### Tests for User Story 1 ⚠️ (escrever primeiro, devem FALHAR)

- [ ] T010 [P] [US1] Teste de contrato de `POST /api/v1/payment-cycles` (201 + `PaymentCycleSummary`) em `payment/PaymentCycleContractTest.java` conforme [contracts/payment-cycles.openapi.yaml](contracts/payment-cycles.openapi.yaml)
- [ ] T011 [P] [US1] Teste de integração (Testcontainers) "só ACTIVE gera pagamento" em `payment/PaymentCycleIntegrationTest.java` (SC-001, FR-001/002)
- [ ] T012 [P] [US1] Teste unit do filtro de status + truncamento de valores em `payment/PaymentCycleServiceTest.java` (FR-002, FR-014)

### Implementation for User Story 1

- [ ] T013 [P] [US1] Criar entidade `Payment` em `payment/domain/Payment.java` (campos per [data-model.md](data-model.md))
- [ ] T014 [P] [US1] Criar entidade `PaymentDeduction` (`@OneToMany` de Payment) em `payment/domain/PaymentDeduction.java`
- [ ] T015 [P] [US1] Criar entidade `PaymentCycle` (totalizadores) em `payment/domain/PaymentCycle.java`
- [ ] T016 [US1] Criar `PaymentRepository` (incl. `existsByCpfAndReferenceYearMonth`) em `payment/repository/PaymentRepository.java`
- [ ] T017 [US1] Criar `PaymentCycleRepository` em `payment/repository/PaymentCycleRepository.java`
- [ ] T018 [US1] Implementar `PaymentCycleService.generateMonthlyCycle(...)` em `payment/service/PaymentCycleService.java`: itera beneficiários ACTIVE, chama cálculo (BeneficiaryFacade/ProgramFacade), cria Payment (`@Transactional` por beneficiário) — FR-001/004/005 (depende de T013–T017)
- [ ] T019 [US1] Implementar `PaymentFacade.generateMonthlyCycle(YearMonth)` em `payment/PaymentFacade.java` delegando ao service
- [ ] T020 [US1] Implementar `PaymentCycleController` (`POST /api/v1/payment-cycles`, `@Valid CycleRequest`, 201) em `payment/api/PaymentCycleController.java`
- [ ] T021 [US1] Implementar `GET /api/v1/payments/{id}` (`PaymentView` mascarado) em `payment/api/PaymentController.java` (FR-010)
- [ ] T022 [US1] Adicionar contabilização de erro de cálculo sem abortar o ciclo (FR-011) no `PaymentCycleService`

**Checkpoint**: User Story 1 funcional e testável de forma independente (MVP)

---

## Phase 4: User Story 2 - Ordenação determinística do descritor (Priority: P2)

**Goal**: Processar sempre na mesma ordem (CPF crescente / keyset), reprodutível entre execuções.

**Independent Test**: Rodar duas vezes sobre o mesmo conjunto → mesma ordem e mesmos totalizadores ([quickstart.md](quickstart.md) Cenário 2, SC-002).

### Tests for User Story 2 ⚠️

- [ ] T023 [P] [US2] Teste de integração "ordem idêntica em duas execuções" em `payment/PaymentCycleOrderingIntegrationTest.java` (SC-002, FR-003)
- [ ] T024 [P] [US2] Teste "ordem segue descritor, não inserção" no mesmo arquivo de ordenação

### Implementation for User Story 2

- [ ] T025 [US2] Implementar leitura ordenada por keyset pagination (`ORDER BY cpf ASC`, seek) na obtenção de beneficiários do `PaymentCycleService` (research R2, FR-003)
- [ ] T026 [US2] Garantir totalizadores acumulados de forma determinística (ordem-estável) no `PaymentCycle` (FR-008)

**Checkpoint**: US1 e US2 funcionam de forma independente

---

## Phase 5: User Story 3 - Idempotência por competência (Priority: P3)

**Goal**: Reexecução na mesma competência não cria pagamento duplicado.

**Independent Test**: Rodar, rodar de novo na mesma competência → contagem não aumenta ([quickstart.md](quickstart.md) Cenário 3, SC-003).

### Tests for User Story 3 ⚠️

- [ ] T027 [P] [US3] Teste de integração "reexecução não duplica" em `payment/PaymentCycleIdempotencyIntegrationTest.java` (SC-003, FR-006)
- [ ] T028 [P] [US3] Teste "retomada após falha parcial" no mesmo arquivo (FR-012, Cenário 4)

### Implementation for User Story 3

- [ ] T029 [US3] Implementar verificação `existsByCpfAndReferenceYearMonth` antes de inserir + tratamento do unique constraint no `PaymentCycleService` (FR-006, research R3)
- [ ] T030 [US3] Garantir commit por beneficiário e retomabilidade (sem transação global) no `PaymentCycleService` (FR-012, research R1)
- [ ] T031 [US3] Implementar skip de CPF duplicado consecutivo (FR-007)

**Checkpoint**: Todas as user stories independentemente funcionais

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Preocupações transversais e fechamento

- [ ] T032 [US1] Publicar evento de auditoria por pagamento via `AuditFacade` (FR-009, REQ-015)
- [ ] T033 Validar competência futura/malformada com retorno 400 no controller (FR-013, Cenário 6)
- [ ] T034 Implementar lock por competência (rejeitar 2º ciclo concorrente → 409) no `PaymentCycleService` (research R7) — **confirmar decisão de concorrência antes de implementar**
- [ ] T035 [P] Teste ArchUnit: `payment` acessa outros contextos só via facade em `payment/ModuleBoundaryTest.java`
- [ ] T036 [P] Teste unit de mascaramento: nenhum CPF/valor em claro nos logs (SC-005)
- [ ] T037 Executar a validação manual do [quickstart.md](quickstart.md) (8 cenários)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sem dependências — começa imediatamente
- **Foundational (Phase 2)**: depende do Setup — BLOQUEIA todas as user stories
- **User Stories (Phase 3–5)**: dependem da Foundational
  - P1 é o MVP; P2 e P3 estendem o `PaymentCycleService` (sequencial recomendado por compartilharem o service)
- **Polish (Phase 6)**: depende das user stories desejadas

### User Story Dependencies

- **US1 (P1)**: após Foundational — sem dependência de outras stories
- **US2 (P2)**: após Foundational — modifica a leitura do service criado em US1
- **US3 (P3)**: após Foundational — adiciona idempotência ao service de US1

### Within Each User Story

- Testes escritos e FALHANDO antes da implementação
- Models antes de repositories; repositories antes de service; service antes de controller

### Parallel Opportunities

- Setup: T003 [P]
- Foundational: T005, T006, T007, T008 [P]
- US1 testes: T010, T011, T012 [P]; US1 models: T013, T014, T015 [P]
- US2 testes: T023, T024 [P]; US3 testes: T027, T028 [P]
- Polish: T035, T036 [P]

---

## Parallel Example: User Story 1

```
# Testes primeiro (devem falhar):
T010 [P] contrato POST /payment-cycles
T011 [P] integração "só ACTIVE"
T012 [P] unit filtro+truncamento

# Depois os models em paralelo:
T013 [P] Payment   T014 [P] PaymentDeduction   T015 [P] PaymentCycle
```

---

## Implementation Strategy

- **MVP = User Story 1** (Phases 1–3 + T032/T033 da Phase 6): já entrega a folha mensal para
  beneficiários ACTIVE, com auditoria e mascaramento. Demonstrável sozinho.
- **Incremento 2 = US2** (ordenação determinística) — reprodutibilidade/auditoria.
- **Incremento 3 = US3** (idempotência/retomada) — robustez operacional.
- **T034 (concorrência)** depende de decisão de negócio ainda aberta (plan.md, research R7).

**Bloqueios de paridade numérica (não de estrutura)**: MYS-001 (FATOR-K) afeta o valor calculado
em US1 mas não impede implementar e testar a estrutura do ciclo.
