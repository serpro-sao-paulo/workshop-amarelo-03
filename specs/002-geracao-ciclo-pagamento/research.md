# Phase 0 — Research: Geração do Ciclo Mensal de Pagamento

**Feature**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10

Consolida as decisões que resolvem as incógnitas do Technical Context e dos `[NEEDS CLARIFICATION]`
da spec (já endereçados nas Clarifications). Cada item: Decisão · Justificativa · Alternativas.

---

## R1 — Estratégia de transação e retomabilidade

- **Decisão**: Commit por beneficiário (não transação global). Cada pagamento é uma unidade
  atômica; o ciclo é retomável e idempotente por competência.
- **Justificativa**: Clarification Q1 (A). A base é grande (~4,2M beneficiários); uma transação
  única é inviável e não retomável. O legado `BATCHPGT` é retomável via verificação "já gerado".
  A idempotência (FR-006) é garantida por unique constraint `(beneficiary, competência)`.
- **Alternativas**: transação global (rejeitada — locks longos, sem retomada); chunks de N
  (válida, mas adiciona complexidade de checkpoint sem ganho claro neste volume de teste).

## R2 — Ordenação determinística do descritor

- **Decisão**: Processar `ORDER BY cpf ASC` (descritor legado), via paginação por keyset
  (seek) sobre `beneficiary.cpf`.
- **Justificativa**: REQ-012 / BR-016 exigem ordem reproduzível; keyset pagination é estável e
  escalável (evita `OFFSET` em base grande). O legado lê `BENEFICIARIO BY CPF`.
- **Alternativas**: `OFFSET` pagination (rejeitada — O(n²) e instável sob inserções);
  ordenar em memória (rejeitada — não cabe ~4,2M registros).

## R3 — Idempotência por competência

- **Decisão**: Unique constraint `(cpf, reference_year_month)` em `payment` + verificação
  `existsByCpfAndReferenceYearMonth` antes de inserir.
- **Justificativa**: FR-006/FR-012. Garante 0 duplicatas mesmo em reexecução ou corrida; a
  constraint é a última linha de defesa no banco.
- **Alternativas**: só checagem em aplicação (rejeitada — corrida pode furar); upsert
  (rejeitada — mascararia erro de lógica e dificultaria auditoria).

## R4 — Política de arredondamento

- **Decisão**: `BigDecimal` com `setScale(2, RoundingMode.DOWN)` (truncamento) em todos os
  valores monetários.
- **Justificativa**: Clarification Q3 (A) — RN-014 / `CALCBENF` trunca. Centraliza a regra em
  `shared/money` para consistência. Resolve MYS-005 para esta feature.
- **Alternativas**: `HALF_UP` como `BATCHREL` (rejeitada — diverge do núcleo de cálculo);
  manter aberto (rejeitada — bloquearia testes de totalizadores).

## R5 — Modelo de entrada da competência

- **Decisão**: Parâmetro opcional `competencia` (AAAAMM) no request; default = competência
  vigente (`YearMonth.now()`); validação rejeita futura/malformada.
- **Justificativa**: Clarification Q2 (A). Permite reprocessamento e testes determinísticos,
  mantendo conveniência operacional.
- **Alternativas**: sempre derivar da data (rejeitada — impede reprocessar meses anteriores);
  aceitar qualquer AAAAMM (rejeitada — risco de gerar competência futura).

## R6 — Comunicação cross-context

- **Decisão**: Síncrona via `BeneficiaryFacade`/`ProgramFacade` (leitura de referência);
  assíncrona via domain event para `AuditFacade` (um evento por pagamento).
- **Justificativa**: [modular-monolith-design.md](../../02-spec-moderna/modular-monolith-design.md)
  (comunicação mista). Cálculo precisa de consistência forte; auditoria tolera fire-and-forget.
- **Alternativas**: só eventos (rejeitada — consistência eventual no cálculo financeiro choca
  com a paridade exigida).

## R7 — Concorrência (risco aberto)

- **Decisão (proposta)**: Lock por competência (advisory lock no PostgreSQL ou linha de controle
  em `payment_cycle` com status RUNNING) — apenas um ciclo por competência de cada vez.
- **Justificativa**: Protege a idempotência contra corrida em reexecução. **Não confirmado pelo
  usuário** (Q4 não respondida) — registrado como risco no plano.
- **Alternativas**: confiar só no unique constraint (corridas viram exceções tratadas);
  lock global (menos throughput). Decidir antes do Estágio 3.

## R8 — Testes (paridade e regressão)

- **Decisão**: Testcontainers (PostgreSQL real) para integração (ordenação, totalizadores,
  retry/idempotência) + unit para regras (filtro de status, truncamento, mascaramento). ArchUnit
  para garantir que `payment` só acessa outros contextos via facade.
- **Justificativa**: Regras financeiras críticas exigem banco real; fronteira de módulo precisa
  de enforcement automatizado.
- **Alternativas**: H2 em memória (rejeitada — diverge do PostgreSQL em tipos NUMERIC/keyset).

---

**Status**: Todas as `[NEEDS CLARIFICATION]` resolvidas (3 via Clarifications, demais via decisões
acima). Risco aberto: concorrência (R7) e MYS-001 (paridade numérica) — não bloqueiam o design.
