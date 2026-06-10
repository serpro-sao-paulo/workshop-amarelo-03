<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# SPECIFICATION — SIFAP 2.0 (Sistema Moderno)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **SPECIFICATION**

**Time**: Equipe SIFAP
**Data**: 2026-06-10
**Gerado por**: `/write-ears-spec` (agente `@architect`)
**Inputs**: [`discovery-report.md`](../01-arqueologia/discovery-report.md), [`business-rules-catalog.md`](../01-arqueologia/business-rules-catalog.md), [`mysteries-found.md`](../01-arqueologia/mysteries-found.md), [`bounded-contexts.md`](bounded-contexts.md)

> [!IMPORTANT]
> **Rastreabilidade obrigatória.** Todo requisito traz uma linha `source_legacy:` apontando para
> `.NSN`/`.ddm` ou `[GREENFIELD] + justificativa`. O CI `legacy-traceability` rejeita PRs sem isso.

> [!WARNING]
> **Faixas de linha aproximadas.** Apenas `CALCDSCT.NSN#L142-L148` está com faixa de linha
> confirmada (BR-001). As demais referências estão em nível de arquivo `.NSN`; as 9 regras
> críticas precisam ter os números exatos fixados antes da implementação (§7 do discovery-report).
> Requisitos que dependem de mistérios abertos (FATOR-K, arredondamento) estão **bloqueados** e
> marcados como tal — a regra confirmada vira REQ, o valor não documentado fica em Open Questions.

---

## Bounded Context: Gestão de Beneficiários

### REQ-001: Validação de CPF por módulo 11

- **EARS Pattern**: Event-driven
- **Statement**: Quando um beneficiário é cadastrado ou alterado, o sistema deverá validar o CPF informado usando o algoritmo módulo 11 da Receita Federal antes de persistir o registro.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN`
- **Source Rule**: Validação cadastral (CPF/NIS/duplicidade), `VALBENEF` — discovery-report §2.1
- **Critérios de Aceite**:
  - [ ] Given um CPF com dígito verificador inválido, when o cadastro é submetido, then o sistema rejeita com erro 400 e não persiste o beneficiário.
  - [ ] Given um CPF válido por módulo 11, when o cadastro é submetido, then o sistema aceita e persiste o beneficiário.

### REQ-002: Bloqueio de transição direta de status inativo para ativo

- **EARS Pattern**: Unwanted
- **Statement**: Se uma transição de status levar um beneficiário diretamente de INACTIVE (I) para ACTIVE (A) sem passar por reavaliação, então o sistema deverá rejeitar a operação.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN`
- **Source Rule**: Ciclo de vida de status A/S/C/I/D — discovery-report §2.3
- **Critérios de Aceite**:
  - [ ] Given um beneficiário com status I, when se tenta alterar direto para A, then o sistema retorna erro 409 (conflito de transição).
  - [ ] Given um beneficiário com status I, when ele passa por reavaliação válida, then a transição para A é permitida.

### REQ-003: Validação de elegibilidade por programa

- **EARS Pattern**: Event-driven
- **Statement**: Quando um beneficiário é associado a um programa social, o sistema deverá validar a elegibilidade conforme as regras do programa antes de aprovar a associação.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN`
- **Source Rule**: Validação de elegibilidade por regras do programa, `VALELEG` — discovery-report §2.1
- **Critérios de Aceite**:
  - [ ] Given um beneficiário fora dos critérios de renda do programa, when a associação é submetida, then o sistema rejeita com motivo de inelegibilidade.
  - [ ] Given um beneficiário dentro dos critérios, when a associação é submetida, then o sistema aprova a associação.

### REQ-004: Validação de documentação comprobatória

- **EARS Pattern**: Event-driven
- **Statement**: Quando documentos comprobatórios são submetidos para um beneficiário, o sistema deverá validar sua completude e validade antes de marcar o cadastro como apto.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN`
- **Source Rule**: Validação de documentação comprobatória, `VALDOCS` — discovery-report §2.1
- **Critérios de Aceite**:
  - [ ] Given documentação obrigatória ausente, when a validação roda, then o cadastro permanece pendente e lista os documentos faltantes.
  - [ ] Given documentação completa e válida, when a validação roda, then o cadastro é marcado como apto.

---

## Bounded Context: Gestão de Programas Sociais

### REQ-005: Faixas de valor por programa e exercício

- **EARS Pattern**: Ubiquitous
- **Statement**: O sistema deverá armazenar, para cada programa social, suas faixas de valores (mínimo/máximo) por exercício e os parâmetros de elegibilidade associados.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN`
- **Source Rule**: Cadastro de programas sociais — faixas e parâmetros, `CADPROG` / DDM `PROGRAMA-SOCIAL` — discovery-report §2.1
- **Critérios de Aceite**:
  - [ ] Given um programa com faixas definidas para 2026, when consultado por exercício, then o sistema retorna o mínimo e o máximo corretos da faixa.
  - [ ] Given um programa sem faixa para um exercício, when consultado, then o sistema retorna ausência de faixa (não um valor zero implícito).

### REQ-006: Estrutura da fórmula do FATOR-K

- **EARS Pattern**: Event-driven
- **Statement**: Quando o valor de um programa é reajustado, o sistema deverá aplicar o FATOR-K segundo a estrutura `FATOR-K = 1 + (FATOR-REAJ × CONSTANTE-K)` sobre o valor do programa.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN`
- **Source Rule**: BR-008 — FATOR-K aplicado ao valor do programa — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given `FATOR-REAJ` = 0 e qualquer `CONSTANTE-K`, when o FATOR-K é calculado, then o resultado é exatamente 1 (sem reajuste).
  - [ ] Given `FATOR-REAJ` > 0, when o FATOR-K é calculado, then o valor do programa é multiplicado por `1 + (FATOR-REAJ × CONSTANTE-K)`.
- **⚠️ Bloqueado por**: MYS-001 — o valor da `CONSTANTE-K` (`0.347215`) não tem origem documentada. A *estrutura* da fórmula é confirmada (vira REQ); o **valor exato** fica em Open Questions até validação.

---

## Bounded Context: Processamento de Pagamentos

### REQ-007: Cálculo do valor base do benefício por faixa

- **EARS Pattern**: Event-driven
- **Statement**: Quando o benefício de um beneficiário é calculado, o sistema deverá determinar o valor base a partir do programa e da faixa aplicável do beneficiário.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN`
- **Source Rule**: BR-003 — Fórmula do benefício = VALOR-BASE(programa, faixa) + acréscimo por dependente — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given um beneficiário na faixa X do programa P, when o benefício é calculado, then o valor base corresponde ao definido para (P, X).
  - [ ] Given um beneficiário sem faixa válida, when o benefício é calculado, then o sistema rejeita o cálculo em vez de assumir valor zero.

### REQ-008: Acréscimo por dependente

- **EARS Pattern**: Event-driven
- **Statement**: Quando o benefício é calculado para um beneficiário com dependentes, o sistema deverá adicionar o acréscimo por dependente ao valor base.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN`
- **Source Rule**: BR-003 — acréscimo por dependente — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given um beneficiário com 2 dependentes, when o benefício é calculado, then o valor final = valor base + 2 × acréscimo por dependente.
  - [ ] Given um beneficiário sem dependentes, when o benefício é calculado, then nenhum acréscimo é aplicado.

### REQ-009: Teto de 30% para descontos não judiciais

- **EARS Pattern**: Unwanted
- **Statement**: Se o total de descontos não judiciais (tipo ≠ 'J') exceder 30% do valor bruto do pagamento, então o sistema deverá truncar o total de descontos não judiciais em 30% do valor bruto.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148`
- **Source Rule**: BR-001 — Teto de descontos não judiciais em 30% do bruto — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given desconto não judicial de 35% do bruto, when o pagamento é calculado, then o desconto não judicial aplicado é truncado para 30%.
  - [ ] Given desconto não judicial de 20% do bruto, when o pagamento é calculado, then o desconto é aplicado integralmente (20%).

### REQ-010: Descontos judiciais sem teto

- **EARS Pattern**: Event-driven
- **Statement**: Quando um desconto judicial (tipo 'J') é aplicado, o sistema deverá adicioná-lo ao total de descontos sem submetê-lo ao teto de 30%.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148`
- **Source Rule**: BR-001 — descontos judiciais sem teto — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given desconto judicial de 50% do bruto, when o pagamento é calculado, then o desconto judicial é aceito integralmente.
  - [ ] Given mix de judicial (20%) + não judicial (25%), when o pagamento é calculado, then o total aplicado é 45% (judicial integral + não judicial dentro do teto).

### REQ-011: Ciclo mensal processa apenas beneficiários ativos

- **EARS Pattern**: Complex
- **Statement**: Enquanto um beneficiário estiver com status ACTIVE (A), quando o ciclo mensal de pagamento é gerado, o sistema deverá criar um registro de pagamento recalculando o benefício e aplicando os descontos.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **Source Rule**: BR-016 — Ciclo mensal processa só status 'A', chamando CALCBENF/CALCDSCT — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given 10 beneficiários ACTIVE e 2 SUSPENDED, when o ciclo mensal roda, then exatamente 10 registros de pagamento são criados.
  - [ ] Given um beneficiário ACTIVE, when o ciclo mensal roda, then o pagamento usa o benefício recalculado (CALCBENF) com descontos aplicados (CALCDSCT).

### REQ-012: Ordenação do processamento pelo descritor

- **EARS Pattern**: Ubiquitous
- **Statement**: O sistema deverá processar o ciclo mensal de pagamento na ordenação determinística do descritor do beneficiário, garantindo resultado reproduzível entre execuções.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`
- **Source Rule**: BR-016 — ordenação na sequência do descritor — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given o mesmo conjunto de beneficiários, when o ciclo roda duas vezes, then a ordem de processamento é idêntica nas duas execuções.
  - [ ] Given beneficiários adicionados entre execuções, when o ciclo roda, then a ordem segue o descritor, não a ordem de inserção.

---

## Bounded Context: Conciliação Bancária

### REQ-013: Conciliação CNAB 240 com tolerância de ±0,01

- **EARS Pattern**: Complex
- **Statement**: Enquanto um pagamento estiver aguardando conciliação, quando o retorno bancário CNAB 240 é processado, o sistema deverá considerar o pagamento conciliado se a diferença entre o valor enviado e o valor retornado for menor ou igual a R$ 0,01.
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN`
- **Source Rule**: BR-017 — Conciliação CNAB 240 com tolerância ±0,01 — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given diferença de R$ 0,01 entre enviado e retornado, when a conciliação roda, then o pagamento é marcado como conciliado.
  - [ ] Given diferença de R$ 0,02, when a conciliação roda, then o pagamento é marcado como divergente para revisão.

### REQ-014: Mapeamento de códigos de retorno bancário

- **EARS Pattern**: Event-driven
- **Statement**: Quando um registro de retorno CNAB 240 é lido, o sistema deverá mapear o código de retorno para o status correspondente do pagamento (pago, rejeitado, devolvido).
- **source_legacy**: `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN`
- **Source Rule**: BR-017 — mapeamento de códigos de retorno — discovery-report §3.1
- **Critérios de Aceite**:
  - [ ] Given um código de retorno de crédito efetivado, when o retorno é processado, then o pagamento recebe status PAID.
  - [ ] Given um código de retorno de devolução, when o retorno é processado, then o pagamento recebe status RETURNED com o motivo registrado.

---

## Bounded Context: Trilha de Auditoria

### REQ-015: Registro de auditoria em toda alteração de entidade

- **EARS Pattern**: Event-driven
- **Statement**: Quando qualquer entidade de negócio é criada, alterada ou removida, o sistema deverá gravar um registro de auditoria contendo autor, timestamp UTC e estado anterior e posterior.
- **source_legacy**: `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`
- **Source Rule**: Trilha de auditoria (IN-TCU 63/2010), DDM `AUDITORIA` — discovery-report §2.1
- **Critérios de Aceite**:
  - [ ] Given uma alteração em um beneficiário, when a transação é confirmada, then existe um registro de auditoria com estado anterior e posterior em formato JSON.
  - [ ] Given uma criação de pagamento, when a transação é confirmada, then o registro de auditoria contém o autor e o timestamp UTC.

### REQ-016: Imutabilidade da trilha de auditoria

- **EARS Pattern**: Unwanted
- **Statement**: Se uma operação tentar atualizar ou excluir um registro existente da trilha de auditoria, então o sistema deverá rejeitar a operação.
- **source_legacy**: `[GREENFIELD]` Requisito de compliance moderno derivado da IN-TCU 63/2010 — o legado mantém AUDITORIA append-only, mas não há proteção técnica explícita; tornamos a imutabilidade uma garantia do sistema.
- **Source Rule**: Trilha de auditoria regulatória — discovery-report §2.1
- **Critérios de Aceite**:
  - [ ] Given um registro de auditoria existente, when um UPDATE é tentado, then o sistema retorna erro 403.
  - [ ] Given um registro de auditoria existente, when um DELETE é tentado, then o sistema retorna erro 403.

### REQ-017: Mascaramento de dados sensíveis em logs

- **EARS Pattern**: Unwanted
- **Statement**: Se um CPF ou valor de benefício for escrito em logs da aplicação, então o sistema deverá mascará-lo (ex.: CPF no formato `XXX.XXX.NNN-NN`).
- **source_legacy**: `[GREENFIELD]` Requisito de segurança (LGPD/OWASP) não presente no legado — o SIFAP original opera em terminal 3270 sem logging estruturado; introduzido na modernização.
- **Source Rule**: Proteção de dados sensíveis — instruções do projeto / discovery-report §2.3
- **Critérios de Aceite**:
  - [ ] Given um CPF logado durante o processamento, when o log é gravado, then o CPF aparece mascarado, nunca em claro.
  - [ ] Given um valor de benefício logado, when o log é gravado, then o valor é omitido ou mascarado.

---

## Open Questions (Não São Requisitos)

> Mistérios do Estágio 1 classificados como bloqueadores. Não viram requisitos até serem resolvidos com facilitador/negócio.

| MYS-ID | Descrição | Bloqueia | Informação necessária para resolver |
| --- | --- | --- | --- |
| MYS-001 | Constante mágica `0.347215` do FATOR-K, sem origem (`CADPROG.NSN`) | REQ-006 | Documento normativo que define a constante; confirmar valor exato e base legal. |
| MYS-002 | `CALCCORR` órfão (sem `CALLNAT` confirmado) | Requisito de correção/reajuste | Confirmar se é código morto ou função usada fora do grafo; decidir migrar ou descartar. |
| MYS-005 | Truncar (`CALCBENF`) vs. arredondar (`BATCHREL`) — perda sistemática de centavos | REQ-007, REQ-013 | Definir a regra oficial de arredondamento (RN-014 sugere truncamento); alinhar todos os pontos. |
| MYS-003 | Região 99 ignora verificações de elegibilidade | REQ-003 | Decidir se é backdoor a remover ou exceção legítima a documentar. |
| MYS-004 | Tolerância 0,01 na conciliação — origem da constante | REQ-013 | Confirmar base normativa da tolerância de aceite. |
| MYS-006 | Prefixos de CPF aceitos sem validação real | REQ-001 | Identificar prefixos e decidir se é bug ou feature de teste a remover. |
| MYS-007 | Ocultação de exclusões com marcador `EX` | REQ-015/REQ-016 | Confirmar impacto em compliance e se dados "fantasma" devem aparecer na auditoria. |

> Cálculo do 13º/abono natalino (mencionado em `REGRAS-NEGOCIO-2012.md` e `CALCBENF`) está marcado como pendente no legado e **não** foi promovido a requisito por falta de fórmula confirmada. Candidato a `[GREENFIELD]`/investigação no próximo ciclo.

---

## Matriz de Rastreabilidade

| REQ-ID | EARS Pattern | source_legacy | Source Rule | Source File | Bounded Context |
| --- | --- | --- | --- | --- | --- |
| REQ-001 | Event-driven | `VALBENEF.NSN` | Validação cadastral CPF | VALBENEF.NSN | Gestão de Beneficiários |
| REQ-002 | Unwanted | `CADBENEF.NSN` | Ciclo de vida de status | CADBENEF.NSN | Gestão de Beneficiários |
| REQ-003 | Event-driven | `VALELEG.NSN` | Validação de elegibilidade | VALELEG.NSN | Gestão de Beneficiários |
| REQ-004 | Event-driven | `VALDOCS.NSN` | Validação de documentos | VALDOCS.NSN | Gestão de Beneficiários |
| REQ-005 | Ubiquitous | `CADPROG.NSN` | Faixas de valor por programa | CADPROG.NSN | Gestão de Programas Sociais |
| REQ-006 | Event-driven | `CADPROG.NSN` | BR-008 FATOR-K (estrutura) | CADPROG.NSN | Gestão de Programas Sociais |
| REQ-007 | Event-driven | `CALCBENF.NSN` | BR-003 valor base | CALCBENF.NSN | Processamento de Pagamentos |
| REQ-008 | Event-driven | `CALCBENF.NSN` | BR-003 acréscimo dependente | CALCBENF.NSN | Processamento de Pagamentos |
| REQ-009 | Unwanted | `CALCDSCT.NSN#L142-L148` | BR-001 teto 30% | CALCDSCT.NSN | Processamento de Pagamentos |
| REQ-010 | Event-driven | `CALCDSCT.NSN#L142-L148` | BR-001 judicial sem teto | CALCDSCT.NSN | Processamento de Pagamentos |
| REQ-011 | Complex | `BATCHPGT.NSN` | BR-016 ciclo mensal | BATCHPGT.NSN | Processamento de Pagamentos |
| REQ-012 | Ubiquitous | `BATCHPGT.NSN` | BR-016 ordenação descritor | BATCHPGT.NSN | Processamento de Pagamentos |
| REQ-013 | Complex | `BATCHCON.NSN` | BR-017 tolerância ±0,01 | BATCHCON.NSN | Conciliação Bancária |
| REQ-014 | Event-driven | `BATCHCON.NSN` | BR-017 códigos de retorno | BATCHCON.NSN | Conciliação Bancária |
| REQ-015 | Event-driven | `AUDITORIA.ddm` | Trilha de auditoria IN-TCU | AUDITORIA.ddm | Trilha de Auditoria |
| REQ-016 | Unwanted | `[GREENFIELD]` | Imutabilidade (compliance) | — | Trilha de Auditoria |
| REQ-017 | Unwanted | `[GREENFIELD]` | Mascaramento (LGPD/OWASP) | — | Trilha de Auditoria |

---

**Definição de Pronto:** 17 requisitos EARS com REQ-IDs únicos (≥10 ✅), `source_legacy:` em todos (15 legado + 2 GREENFIELD justificados ✅), ≥2 critérios de aceite por requisito ✅, agrupados por bounded context ✅, mistérios somente em Open Questions ✅, matriz de rastreabilidade ✅.

> Próximo passo: `/speckit.clarify` para resolver ambiguidades, depois `/stage-architect-design-modular-monolith` para o C4 + esqueleto OpenAPI.

— Equipe SIFAP
