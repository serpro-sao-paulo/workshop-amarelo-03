<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: Equipe SIFAP
**Data**: 2026-06-10
**Edição**:
**Participantes**: Arqueologia conduzida com `@archaeologist` (par 1 lidera; apoio de EA/SA, DBA, QA, TW)

---

## 1. Sumário Executivo

> Em 3 a 5 frases, resuma o que o time descobriu sobre o SIFAP legado.
> O que é este sistema? Qual sua criticidade? Qual o estado do código?

O SIFAP é um sistema Natural/Adabas de ~29 anos que calcula e processa pagamentos de benefícios sociais, composto por **15 programas `.NSN` e 4 DDMs** (BENEFICIARIO ~4,2M, PAGAMENTO ~180M, PROGRAMA-SOCIAL ~45, AUDITORIA ~25M registros). Catalogamos **20 regras de negócio** (100% rastreadas a `.NSN`/`.ddm`), das quais 9 são críticas e concentradas nos programas de cálculo (`CALCBENF`, `CALCDSCT`). O grafo de dependências é **acíclico**: os entry points batch/online chamam subprogramas de validação e cálculo que acessam os 4 DDMs; `CALCCORR` aparece como **órfão** (sem `CALLNAT` confirmado). O maior risco para o Estágio 2 é o **`FATOR-K` com a constante mágica `0.347215`** (MYS-001) sem origem documentada, que afeta o valor de todo benefício. **Confiança para modernização: Média** — a estrutura está clara, mas regras financeiras críticas vivem apenas no código e precisam de validação de linha exata.

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

Sistema de Fiscalização e Administração de Pagamentos: cadastra beneficiários e programas sociais, valida elegibilidade/documentos, calcula o benefício mensal (com fatores regional, familiar, idade, faixa de renda, 13º e abono natalino), aplica descontos, gera a folha mensal de pagamentos e a remessa bancária, concilia o retorno CNAB 240 e mantém trilha de auditoria (IN-TCU 63/2010). Fonte: `business-rules-catalog.md`, `legacy-docs/REGRAS-NEGOCIO-2012.md`.

### 2.2 Arquitetura Legada

15 programas Natural + 4 DDMs Adabas. Organização por prefixo: `CAD*` (cadastro online), `CONS*` (consulta), `VAL*` (validação), `CALC*` (cálculo), `REL*` (relatórios), `BATCH*` (jobs). Fluxo principal: `BATCHPGT` (1º dia útil) lê BENEFICIARIO/PROGRAMA-SOCIAL, chama `VALELEG` → `CALCBENF` → `CALCDSCT` e grava PAGAMENTO; `BATCHCON` concilia o retorno bancário e grava AUDITORIA. Detalhe completo em [dependency-map.md](dependency-map.md).

### 2.3 Usuários e Perfis

Operadores (CGPB/DEFIS) via terminal 3270; auditores (trilha AUDITORIA); jobs batch agendados (scheduler). Perfis de auditoria: ADM, OPR, CON, AUD, SUP (campo `COD-PERFIL` em `AUDITORIA.ddm`). Integrações externas: SIAFI (orçamento) e banco (CNAB 240).

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

> Liste as 5 regras de negócio mais importantes encontradas.

1. BR-001 — Teto de descontos não judiciais em 30% do bruto, judicial sem teto (`CALCDSCT.NSN#L142-L148`).
2. BR-003 — Fórmula do benefício = VALOR-BASE(programa, faixa) + acréscimo por dependente (`CALCBENF.NSN`).
3. BR-008 — `FATOR-K` = 1 + (FATOR-REAJ × 0.347215) aplicado ao valor do programa (`CADPROG.NSN`).
4. BR-016 — Ciclo mensal processa só status 'A', na ordenação do descritor, chamando CALCBENF/CALCDSCT (`BATCHPGT.NSN`).
5. BR-017 — Conciliação CNAB 240 com tolerância ±0,01 e mapeamento de códigos de retorno (`BATCHCON.NSN`).

> Catálogo completo (20 regras) em [business-rules-catalog.md](business-rules-catalog.md).

### 3.2 Dependências Complexas

> Quais programas estão mais acoplados? Onde há risco de efeito cascata?

`BATCHPGT` é o nó mais conectado: depende de VALELEG, CALCBENF, CALCDSCT e dos 3 DDMs principais — qualquer mudança no cálculo cascateia na folha mensal. A família com radical `BENEF` (`CADBENF`/`VALBENEF`/`CALCBENF`/`CONSBENF`) gira em torno do DDM BENEFICIARIO. Grafo acíclico, sem dependências circulares. Ver [dependency-map.md](dependency-map.md).

### 3.3 Dívida Técnica Identificada

> Que problemas no código legado vão complicar a migração?

- [x] `CALCBENF` com ~4.800 linhas e aninhamento condicional de até 7 níveis; lógica sem parametrização externa.
- [x] Constantes hardcoded (fator regional por UF, tabela IPCA 2010–2012, `0.347215`) sem documentação.
- [x] Divergência de arredondamento (truncar em `CALCBENF` vs. arredondar em `BATCHREL`).

### 3.4 Gaps de Documentação

> O que a documentação existente NÃO cobre?

Cálculo do 13º/abono natalino, fórmula do FATOR-K, regras de conciliação (BATCHCON) e finalidade da região 99 estão marcados como pendentes em `REGRAS-NEGOCIO-2012.md`/`MANUAL-TECNICO-SIFAP-2008.md`. O conhecimento dos módulos de cálculo reside exclusivamente no código (equipe original aposentada/transferida).

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

> Resuma os mistérios do arquivo `mysteries-found.md` que permanecem sem explicação.

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
| MYS-001 | Constante mágica `0.347215` do FATOR-K, sem origem (`CADPROG.NSN`) | **Bloqueia S2** — cálculo de benefício diverge se reproduzido errado |
| MYS-002 | `CALCCORR` órfão (sem CALLNAT confirmado) | **Bloqueia S2** — migrar código morto ou perder função de correção |
| MYS-005 | Truncar (`CALCBENF`) vs. arredondar (`BATCHREL`) | Alto — divergência de centavos em totalizadores |
| MYS-003 | Região 99 ignora elegibilidade | Médio — backdoor de autorização |
| MYS-004 / MYS-006 / MYS-007 | Tolerância 0,01; prefixos de teste de CPF; ocultação de exclusões `EX` | Médio — aceite de pagamento, dados fantasma, compliance |

> Catálogo completo (7 mistérios) em [mysteries-found.md](mysteries-found.md).

### 4.2 Riscos para o Estágio 2

> O que o time de especificação precisa saber antes de começar?

1. Resolver MYS-001 (FATOR-K) e MYS-002 (CALCCORR) antes de escrever EARS de cálculo/correção.
2. As faixas de linha marcadas com `~` no catálogo são aproximadas — fixar números exatos das 9 regras críticas antes de usá-las como `source_legacy:`.
3. Decidir explicitamente o tratamento de exceções de segurança (região 99, prefixos de CPF) e da divergência de arredondamento.

---

## 5. Recomendações

### 5.1 O que migrar primeiro

> Com base na priorização do Par 1 (Product Owner), quais funcionalidades devem ser migradas primeiro?

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1          | Cálculo e folha de pagamento mensal (`BATCHPGT`, `CALCBENF`, `CALCDSCT`) | Núcleo financeiro do sistema; concentra 9 regras críticas e o maior risco. |
| 2          | Cadastro/validação de beneficiários (`CADBENF`, `VALBENEF`, `VALDOCS`, `VALELEG`) | Entidade central; alimenta todo o cálculo e a elegibilidade. |
| 3          | Conciliação bancária CNAB 240 (`BATCHCON`) | Fecha o ciclo financeiro e a integração externa; regra de aceite crítica. |

### 5.2 O que descartar

> Funcionalidades que provavelmente não precisam ser migradas:

- [Funcionalidade]: [Motivo para descartar]

### 5.3 O que evoluir

> Funcionalidades que devem ser migradas E melhoradas:

- [Funcionalidade]: [Como melhorar]

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | 15 / 15  |
| DDMs mapeados                 | 4 / 4   |
| Regras de negócio encontradas | 20       |
| Regras escondidas encontradas | 6 / 10  |
| Easter eggs encontrados       | 0 / 3   |
| Termos no glossário           | 40       |
| Mistérios catalogados         | 7       |
| Tempo total gasto             | — (sessão assistida) |

---

## 7. Notas para o Próximo Estágio

> Deixe aqui mensagens para o time no Estágio 2 (Especificação Moderna):

Comecem pelas 9 regras críticas do cálculo e pagamento e fixem as faixas de linha exatas (marcadas com `~`) antes de escrever `source_legacy:`. Resolvam MYS-001 (FATOR-K) e MYS-002 (CALCCORR órfão) com facilitador antes de especificar cálculo/correção. Usem as 5 hipóteses de recorte da §5.4 como ponto de partida para os bounded contexts.

---

## Definição de Pronto deste relatório

- [x] Todas as seções acima preenchidas (sem placeholders).
- [x] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [x] Decisões de migrar/descartar/evoluir em §5 cobrem as 8+ funcionalidades principais.
- [x] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

