# Feature Specification: Geração do Ciclo Mensal de Pagamento

**Feature Branch**: `002-geracao-ciclo-pagamento`

**Created**: 2026-06-10

**Status**: Draft

**Input**: User description: "Geração do ciclo mensal de pagamento (REQ-011, REQ-012): processa beneficiários ACTIVE na ordenação do descritor, recalcula benefício (CALCBENF) e aplica descontos (CALCDSCT), criando um registro de pagamento por beneficiário elegível."

> [!IMPORTANT]
> **Rastreabilidade obrigatória (CI `legacy-traceability`).** Cada requisito funcional traz uma
> linha `source_legacy:` apontando para `.NSN`/`.ddm` ou `[GREENFIELD] + justificativa`.
> Fonte primária: [`SPECIFICATION.md`](../../02-spec-moderna/SPECIFICATION.md) (REQ-011, REQ-012),
> [`business-rules-catalog.md`](../../01-arqueologia/business-rules-catalog.md) (BR-016) e
> [`BATCHPGT.NSN`](../../01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN).

---

## Clarifications

### Session 2026-06-10

- Q: Comportamento em caso de falha no meio do ciclo? → A: Retomável/idempotente por beneficiário — cada pagamento é commitado individualmente; após falha, reexecutar retoma sem duplicar.
- Q: Como a competência (AAAAMM) é informada ao ciclo? → A: Parametrizável, com default na competência vigente; competências futuras são recusadas.
- Q: Política de arredondamento dos valores (MYS-005)? → A: Truncar em 2 casas decimais (RN-014 / CALCBENF), sem arredondar; decisão de spec revisável com o negócio.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Gerar o ciclo mensal apenas para beneficiários ativos (Priority: P1)

Um operador (CGPB/DEFIS) dispara o ciclo mensal de pagamento para uma competência (AAAAMM).
O sistema percorre os beneficiários, considera **somente** os que estão com status ACTIVE (A) e
cria um registro de pagamento para cada um, recalculando o benefício e aplicando os descontos.
Beneficiários em qualquer outro status (S/C/I/D) são ignorados sem gerar pagamento.

**Why this priority**: É o núcleo financeiro do SIFAP e a prioridade 1 de migração
([discovery-report §5.1](../../01-arqueologia/discovery-report.md)). Sem ele não há folha de
pagamento — entrega valor de negócio sozinha (MVP).

**Independent Test**: Carregar um conjunto de beneficiários com status misto, disparar o ciclo
para uma competência e verificar que existe exatamente um pagamento por beneficiário ACTIVE e
nenhum para os demais status.

**Acceptance Scenarios**:

1. **Given** 10 beneficiários ACTIVE e 2 SUSPENDED, **When** o ciclo mensal roda para a competência, **Then** exatamente 10 registros de pagamento são criados.
2. **Given** um beneficiário ACTIVE, **When** o ciclo roda, **Then** o pagamento usa o benefício recalculado (CALCBENF) com descontos aplicados (CALCDSCT).
3. **Given** um beneficiário INACTIVE, **When** o ciclo roda, **Then** nenhum pagamento é criado para ele e ele é contabilizado como ignorado.

---

### User Story 2 - Processar na ordenação determinística do descritor (Priority: P2)

O ciclo processa os beneficiários sempre na mesma ordem (a ordenação do descritor — no legado,
CPF crescente), garantindo que duas execuções sobre o mesmo conjunto produzam a mesma sequência
de processamento e os mesmos totalizadores.

**Why this priority**: A ordem afeta totalizadores acumulados e a reprodutibilidade exigida por
auditoria (BR-016 — "ordenação afeta totalizadores, não pode mudar"). Depende da User Story 1.

**Independent Test**: Rodar o ciclo duas vezes sobre o mesmo conjunto e comparar a ordem de
processamento registrada; devem ser idênticas.

**Acceptance Scenarios**:

1. **Given** o mesmo conjunto de beneficiários, **When** o ciclo roda duas vezes, **Then** a ordem de processamento é idêntica nas duas execuções.
2. **Given** beneficiários adicionados entre execuções, **When** o ciclo roda, **Then** a ordem segue o descritor, não a ordem de inserção.

---

### User Story 3 - Idempotência: não duplicar pagamento na mesma competência (Priority: P3)

Se o ciclo for reexecutado para uma competência que já gerou pagamento para um beneficiário, o
sistema não cria um segundo pagamento para ele.

**Why this priority**: O legado verifica "se já gerou nesta competência" e ignora duplicatas
(`BATCHPGT.NSN`); reexecução por falha parcial é um cenário operacional real. Depende da User Story 1.

**Independent Test**: Rodar o ciclo, rodar de novo na mesma competência e verificar que a
contagem de pagamentos não aumenta.

**Acceptance Scenarios**:

1. **Given** um beneficiário ACTIVE que já tem pagamento na competência AAAAMM, **When** o ciclo roda de novo para AAAAMM, **Then** nenhum pagamento adicional é criado para ele.
2. **Given** um beneficiário ACTIVE sem pagamento na competência, **When** o ciclo roda, **Then** um pagamento é criado.

---

### Edge Cases

- **Beneficiário ACTIVE sem faixa de cálculo válida**: o cálculo do benefício rejeita em vez de assumir valor zero (REQ-007); o beneficiário é contabilizado como erro e o ciclo continua com os demais.
- **CPF duplicado na base** (visto no legado): registros com o mesmo CPF do anterior são ignorados para evitar pagamento em dobro.
- **Competência inválida ou futura**: a competência é parametrizável (default = vigente); o sistema **recusa** competências futuras e valores AAAAMM malformados antes de iniciar o ciclo.
- **Falha no meio do ciclo**: o ciclo é **retomável/idempotente por beneficiário** — cada pagamento é commitado individualmente; reexecutar o ciclo na mesma competência retoma do ponto de parada sem duplicar (apoiado por FR-006). Espelha o comportamento do legado (verificação "já gerado").
- **Política de arredondamento**: todos os valores monetários são **truncados em 2 casas decimais** (RN-014 / CALCBENF), sem arredondar. Decisão de spec que resolve MYS-005 para esta feature e é revisável com o negócio/normativa.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001 (REQ-011)**: O sistema MUST criar, para cada beneficiário com status ACTIVE (A) na competência informada, um registro de pagamento recalculando o benefício e aplicando os descontos.
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **FR-002 (REQ-011)**: O sistema MUST ignorar beneficiários em status S/C/I/D, sem criar pagamento, contabilizando-os como ignorados.
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **FR-003 (REQ-012)**: O sistema MUST processar os beneficiários na ordenação determinística do descritor, produzindo resultado reproduzível entre execuções.
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **FR-004 (REQ-011)**: O sistema MUST calcular o valor do benefício a partir do programa e da faixa aplicável do beneficiário (delega à capacidade de cálculo — REQ-007/008).
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN`
- **FR-005 (REQ-011)**: O sistema MUST aplicar os descontos sobre o valor bruto, respeitando o teto de 30% para descontos não judiciais e sem teto para judiciais (delega à capacidade de descontos — REQ-009/010).
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN`
- **FR-006**: O sistema MUST garantir idempotência por competência — não criar um segundo pagamento para um beneficiário que já possui pagamento na mesma competência (AAAAMM).
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **FR-007**: O sistema MUST ignorar registros de beneficiário duplicados (mesmo CPF consecutivo) para impedir pagamento em duplicidade.
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **FR-008**: O sistema MUST registrar, ao final do ciclo, totalizadores: processados, gerados, ignorados e erros, além do valor total bruto, total de descontos e total líquido.
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **FR-009**: O sistema MUST emitir um evento de auditoria para cada pagamento criado (autor, timestamp UTC, estado), conforme a trilha de auditoria (REQ-015).
  - source_legacy: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
- **FR-010**: O sistema MUST mascarar CPF e valores de benefício em qualquer log emitido durante o ciclo (REQ-017).
  - source_legacy: `[GREENFIELD]` Requisito de segurança LGPD/OWASP; o legado opera em terminal 3270 sem logging estruturado.
- **FR-011**: Quando o cálculo de um beneficiário falhar, o sistema MUST contabilizá-lo como erro e prosseguir com os demais, sem abortar o ciclo inteiro.
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **FR-012**: O sistema MUST commitar cada pagamento individualmente (não em uma única transação global), de modo que uma falha parcial seja recuperável reexecutando o ciclo na mesma competência sem gerar duplicatas.
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **FR-013**: O sistema MUST aceitar a competência (AAAAMM) como parâmetro de entrada, assumindo a competência vigente quando omitida, e MUST recusar competências futuras ou malformadas.
  - source_legacy: `[GREENFIELD]` O legado deriva a competência da data do 1º dia útil; a parametrização é introduzida para reprocessamento e testes determinísticos.
- **FR-014**: O sistema MUST truncar todos os valores monetários em 2 casas decimais (sem arredondar), de forma consistente em valor bruto, descontos, valor líquido e totalizadores.
  - source_legacy: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN` (RN-014 — truncação)

### Key Entities *(include if feature involves data)*

- **PaymentCycle (Ciclo de Pagamento)**: representa uma execução do ciclo para uma competência (AAAAMM) e número de ciclo; agrega os totalizadores (processados, gerados, ignorados, erros, valores). Ver [data-model.md](../../02-spec-moderna/data-model.md).
- **Payment (Pagamento)**: registro gerado por beneficiário elegível — valor bruto, descontos, valor líquido, status (P/G/E/C/D/X/R), competência. DDM `PAGAMENTO`.
- **Beneficiary (Beneficiário)**: fonte de quem é elegível; status A/S/C/I/D é o filtro central. DDM `BENEFICIARIO`.
- **SocialProgram (Programa Social)**: fornece faixas de valor e parâmetros de cálculo consumidos pelo cálculo do benefício. DDM `PROGRAMA-SOCIAL`.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Para um conjunto com N beneficiários ACTIVE e M não-ACTIVE, o ciclo cria exatamente N pagamentos e 0 para os não-ACTIVE.
- **SC-002**: Duas execuções do ciclo sobre o mesmo conjunto produzem a mesma ordem de processamento e os mesmos totalizadores (reprodutibilidade 100%).
- **SC-003**: Reexecutar o ciclo na mesma competência não aumenta a contagem de pagamentos (0 duplicatas).
- **SC-004**: A soma de (valor líquido + descontos) de todos os pagamentos gerados é igual ao total bruto reportado nos totalizadores (consistência de totais), com todos os valores truncados em 2 casas decimais.
- **SC-005**: Nenhum log do ciclo contém CPF ou valor de benefício em claro (0 vazamentos).

## Assumptions

- O cálculo do benefício (REQ-007/008) e dos descontos (REQ-009/010) são capacidades **separadas** já especificadas; esta feature as **invoca**, não as redefine.
- A "ordenação do descritor" do legado corresponde a CPF crescente; no sistema novo será uma ordenação determinística estável equivalente (a definir no plano técnico).
- A competência-alvo é uma entrada do caso de uso, parametrizável com default na competência vigente; competências futuras são recusadas (no legado a competência é derivada da data).
- O 13º/abono natalino **não** faz parte desta feature (sem fórmula confirmada no legado — pendente no discovery-report).
- A constante do FATOR-K (MYS-001) permanece aberta e pode afetar os valores calculados; não bloqueia a estrutura do ciclo, mas bloqueia a paridade numérica exata. A política de arredondamento foi resolvida como **truncação em 2 casas** (MYS-005 — ver Clarifications).

## Dependencies

- Capacidade de **cálculo de benefício** (REQ-007/008) — `CALCBENF.NSN`.
- Capacidade de **cálculo de descontos** (REQ-009/010) — `CALCDSCT.NSN`.
- **Gestão de Beneficiários** (status/elegibilidade) — `BeneficiaryFacade` ([modular-monolith-design.md](../../02-spec-moderna/modular-monolith-design.md)).
- **Gestão de Programas Sociais** (faixas/parâmetros) — `ProgramFacade`.
- **Trilha de Auditoria** (evento por pagamento) — `AuditFacade`.

## Open Questions (bloqueiam paridade, não a estrutura)

| ID | Questão | Afeta |
| --- | --- | --- |
| MYS-001 | Valor da constante do FATOR-K (`0.347215`) sem origem documentada | Valor do benefício (FR-004) |

---

> Próximo passo: `/speckit.clarify` para resolver os marcadores `[NEEDS CLARIFICATION]` e as
> Open Questions críticas, depois `/speckit.plan`.
