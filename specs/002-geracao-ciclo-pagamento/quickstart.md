# Quickstart — Validação do Ciclo Mensal de Pagamento

**Feature**: `002-geracao-ciclo-pagamento` | **Date**: 2026-06-10

Guia de validação end-to-end. Prova que a feature satisfaz REQ-011/012 e os critérios de sucesso
da [spec](spec.md). A implementação (código, migrations, testes) é produzida no Estágio 3 —
este guia descreve **como validar**, não a implementação.

## Pré-requisitos

- Backend `payment` em execução (Spring Boot 3.3, Java 21) — single deployable.
- PostgreSQL 16 com schema aplicado (`payment`, `payment_deduction`, `payment_cycle`).
- Massa de teste carregada: beneficiários com status misto (A/S/C/I/D) e ao menos 1 programa
  social com faixa de valor vigente.
- Contrato: [contracts/payment-cycles.openapi.yaml](contracts/payment-cycles.openapi.yaml).

## Cenário 1 — Só beneficiários ACTIVE geram pagamento (SC-001)

1. Carregar 10 beneficiários ACTIVE + 2 SUSPENDED para a mesma competência.
2. Disparar o ciclo:
   ```bash
   curl -X POST http://localhost:8080/api/v1/payment-cycles \
     -H 'Content-Type: application/json' \
     -d '{"competencia":"202606"}'
   ```
3. **Esperado**: HTTP 201; `generated = 10`, `skipped = 2`, `processed = 12`.
4. Verificar que existe 1 pagamento por beneficiário ACTIVE e 0 para os SUSPENDED.

## Cenário 2 — Ordenação determinística (SC-002)

1. Rodar o ciclo duas vezes sobre o mesmo conjunto (competência distinta ou base limpa).
2. **Esperado**: a ordem de processamento (por CPF crescente) e os totalizadores são idênticos
   nas duas execuções.

## Cenário 3 — Idempotência por competência (SC-003)

1. Disparar o ciclo para `202606` (gera N pagamentos).
2. Disparar **de novo** para `202606`.
3. **Esperado**: a contagem total de pagamentos **não aumenta**; nenhum pagamento duplicado
   (unique `(cpf, referenceYearMonth)`).

## Cenário 4 — Retomada após falha (FR-012)

1. Interromper o ciclo no meio (simular falha após alguns commits).
2. Reexecutar para a mesma competência.
3. **Esperado**: o ciclo retoma; beneficiários já pagos são pulados; total final correto, sem duplicatas.

## Cenário 5 — Consistência de totais e truncamento (SC-004 / FR-014)

1. Após um ciclo, somar `netValue + totalDeduction` de todos os pagamentos.
2. **Esperado**: igual ao `totalGross` do `PaymentCycleSummary`; todos os valores com exatamente
   2 casas decimais (truncadas, não arredondadas).

## Cenário 6 — Competência futura é recusada (FR-013)

1. Disparar o ciclo com `competencia` futura (ex.: `209901`).
2. **Esperado**: HTTP 400; nenhum pagamento criado.

## Cenário 7 — Mascaramento em logs (SC-005)

1. Executar o ciclo e inspecionar os logs da aplicação.
2. **Esperado**: nenhum CPF ou valor de benefício em claro; CPF aparece mascarado.

## Cenário 8 — Execução concorrente (R7, risco aberto)

1. Disparar dois ciclos simultâneos para a mesma competência.
2. **Esperado (proposto)**: o segundo recebe HTTP 409 (ciclo em execução). *Confirmar a decisão
   de concorrência antes do Estágio 3.*

---

**Rastreabilidade**: cenários ↔ SC-001..005 / FR-006,012,013,014. Detalhes em [spec.md](spec.md),
[data-model.md](data-model.md) e [research.md](research.md).
