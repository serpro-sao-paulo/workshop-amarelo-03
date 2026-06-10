# Phase 1 — Data Model: Geração do Ciclo Mensal de Pagamento

**Feature**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10

Modelo de dados escopado à feature. Deriva de [02-spec-moderna/data-model.md](../../02-spec-moderna/data-model.md)
e do DDM [`PAGAMENTO`](../../01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm). Entidades de
outros contextos (`Beneficiary`, `SocialProgram`) são **somente leitura** aqui, acessadas via facade.

## Entidades desta feature

### PaymentCycle (novo — agregador da execução)

| Campo | Tipo | Regra | Origem |
| --- | --- | --- | --- |
| `id` | Long (PK) | surrogate | GREENFIELD |
| `referenceYearMonth` | int (AAAAMM) | único por ciclo; não futuro (FR-013) | `PAGAMENTO.ANO-MES-REF` |
| `cycleNumber` | int | sequencial | `PAGAMENTO.NUM-CICLO` |
| `status` | enum | `RUNNING` / `COMPLETED` / `FAILED` | GREENFIELD (lock/retomada) |
| `processedCount` | int | totalizador | `BATCHPGT` #QTD-PROCESSADOS |
| `generatedCount` | int | totalizador | `BATCHPGT` #QTD-GERADOS |
| `skippedCount` | int | totalizador | `BATCHPGT` #QTD-IGNORADOS |
| `errorCount` | int | totalizador | `BATCHPGT` #QTD-ERROS |
| `totalGross` | BigDecimal(15,2) | truncado 2 casas | `BATCHPGT` #VLR-TOTAL-BRUTO |
| `totalDeductions` | BigDecimal(15,2) | truncado 2 casas | `BATCHPGT` #VLR-TOTAL-DESC |
| `totalNet` | BigDecimal(15,2) | truncado 2 casas | `BATCHPGT` #VLR-TOTAL-LIQ |
| `startedAt` / `finishedAt` | Instant (UTC) | controle | GREENFIELD |

> **Constraint**: único `(referenceYearMonth)` em estado `RUNNING` — suporta lock por competência (R7).

### Payment

| Campo | Tipo | Regra | Origem (DDM PAGAMENTO) |
| --- | --- | --- | --- |
| `paymentNumber` | Long (PK) | sequencial único | `NUM-PAGAMENTO` (AA) |
| `cpf` | String(11) | FK lógico p/ beneficiário; mascarado em log (FR-010) | `NUM-CPF` (AB) |
| `programCode` | String(4) | referência ao programa | `COD-PROGRAMA` (AD) |
| `referenceYearMonth` | int (AAAAMM) | competência | `ANO-MES-REF` (AE) |
| `cycleId` | Long (FK) | → PaymentCycle | GREENFIELD |
| `grossValue` | BigDecimal(11,2) | truncado 2 casas (FR-014) | `VLR-BRUTO` (BA) |
| `totalDeduction` | BigDecimal(9,2) | truncado 2 casas | `VLR-DESCONTO-TOTAL` (BC) |
| `netValue` | BigDecimal(11,2) | `gross - deductions`, truncado | `VLR-LIQUIDO` (BB) |
| `status` | enum | inicial `G` (gerado) | `SIT-PAGAMENTO` (DA): P/G/E/C/D/X/R |
| `generationDate` | LocalDate | data de geração | `DT-GERACAO` (DB) |

> **Unique constraint**: `(cpf, referenceYearMonth)` — idempotência (FR-006, R3).
> **Índice**: `(referenceYearMonth, status)` — super-descriptor S2 do legado.

### PaymentDeduction (filha — grupo PE do legado)

| Campo | Tipo | Regra | Origem |
| --- | --- | --- | --- |
| `id` | Long (PK) | surrogate | — |
| `paymentNumber` | Long (FK) | → Payment | `GRP-DESCONTO` (PE, max 8) |
| `deductionType` | String(3) | IR/JD/CS/PA/EM/TX/OU/EX | `TIPO-DESCONTO` (CB) |
| `value` | BigDecimal(9,2) | truncado 2 casas | `VLR-DESCONTO` (CC) |
| `percentage` | BigDecimal(5,2) | percentual aplicado | `PCT-DESCONTO` (CD) |
| `judicialProcess` | String(20) | preenchido se tipo = JD | `NUM-PROCESSO` (CE) |

## Entidades lidas (somente leitura, via facade)

- **Beneficiary** (`BeneficiaryFacade.findActive()` / status): filtro central status = A (FR-001/002).
- **SocialProgram** (`ProgramFacade.getValueRange` / `getCalculationParameters`): faixas e FATOR-K
  para o cálculo (FR-004). ⚠️ FATOR-K bloqueado por MYS-001 (paridade numérica).

## Transições de estado

```text
PaymentCycle:  RUNNING → COMPLETED   (ciclo concluído)
               RUNNING → FAILED      (falha não recuperável; reexecução retoma)

Payment:       (novo) → G (gerado)   (esta feature cria em G)
               G → E → C / D / X / R (estágios posteriores: emissão, conciliação)
```

## Regras de validação (resumo)

1. `referenceYearMonth` válido (AAAAMM) e não futuro — FR-013.
2. Só beneficiários status = A geram Payment — FR-001/002.
3. `(cpf, referenceYearMonth)` único — FR-006.
4. Todos os valores truncados em 2 casas — FR-014.
5. `netValue = grossValue - totalDeduction` (após truncamento) — SC-004.
6. Falha de cálculo individual → `errorCount++`, ciclo continua — FR-011.

---

**Rastreabilidade**: REQ-011/012, FR-001..014, BR-016, DDM `PAGAMENTO`. Schema final (migrations)
no Estágio 3.
