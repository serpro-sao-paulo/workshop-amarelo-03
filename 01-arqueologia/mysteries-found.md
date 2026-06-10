<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

> Classificação (caminho de resolução): **blocks-stage-2** (resolver antes do Estágio 2), **needs-investigation** (resposta provável em outro arquivo), **needs-facilitator** (conhecimento de domínio/mentor), **parked** (fora de escopo). Ordenado por severidade.

| ID      | Descrição | Onde Encontrado | Impacto Potencial | Confiança |
| ------- | --------- | --------------- | ----------------- | --------- |
| MYS-001 | `FATOR-K` e a constante mágica `0.347215`: `FATOR-K = 1 + (FATOR-REAJ × 0.347215)`, sem origem documentada (inserido ago/2008 por Adilson, "atende SENARC"). | `CADPROG.NSN#~L60-L80`; `PROGRAMA-SOCIAL.ddm` (campo FATOR-K) | **blocks-stage-2** — afeta valor calculado de todo programa; sem a fórmula, a spec do cálculo fica incorreta. | ALTA |
| MYS-002 | `CALCCORR.NSN` (correção IPCA retroativa) não tem `CALLNAT` confirmado em nenhum programa — órfão. | `CALCCORR.NSN` (programa inteiro); `dependency-map.md` (órfãos) | **blocks-stage-2** — se for código vivo, falta um gatilho no escopo; se for morto, não migrar. | ALTA |
| MYS-003 | Região 99 ignora todas as checagens de elegibilidade ("bypass do Roberto", diplomático). Finalidade real não documentada. | `VALELEG.NSN#~L110-L130`; `REGRAS-NEGOCIO-2012.md` RN-005 | **needs-facilitator** — regra de segurança/autorização; decidir se preservar na modernizada. | ALTA |
| MYS-004 | Tolerância de ±0,01 na conciliação CNAB 240 — origem (spec CNAB? erro de arredondamento?) desconhecida. | `BATCHCON.NSN#~L70-L110` | **needs-investigation** — buscar `0.01` em todos os `.NSN`/docs; afeta aceitação/rejeição de pagamentos. | MÉDIA |
| MYS-005 | Divergência de arredondamento: `CALCBENF` trunca; `BATCHREL` arredonda a 0,005 — mesmo tipo de valor, métodos diferentes. | `CALCBENF.NSN#~L210-L230` vs `BATCHREL.NSN#~L40-L90`; corresponde a INC-004 | **needs-investigation** — confirmar impacto nos totalizadores/conciliação. | ALTA |
| MYS-006 | Prefixos especiais de CPF (000, 001, 002, 010, 011, 099, 100, 999) fazem a validação aceitar documentos inválidos. Quantos registros "teste" existem em produção? | `VALDOCS.NSN#~L170-L190`; `VALBENEF.NSN#~L240-L260` | **needs-investigation** — risco de beneficiários fantasmas; verificar escopo na base. | MÉDIA |
| MYS-007 | `RELAUDIT` oculta ações de exclusão (`EX`) na exibição ("não corrigir sem aprovação da auditoria"). Por que a auditoria vetaria? | `RELAUDIT.NSN#~L45-L60`; `AUDITORIA.ddm` (COD-ACAO) | **needs-facilitator** — implicação de compliance (IN-TCU 63/2010). | MÉDIA |

## Detalhamento dos Mistérios

### MYS-001: Constante mágica do FATOR-K

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#~L60-L80` (constante também no DDM `PROGRAMA-SOCIAL`)
- **O que esperávamos**: um fator de reajuste parametrizado e rastreável a uma norma.
- **O que o código faz**: aplica `FATOR-K = 1 + (FATOR-REAJ × 0.347215)`; a constante `0.347215` não tem origem documentada. Campo inserido em ago/2008, comentário "atende solicitação SENARC".
- **Hipótese do time**: índice de reajuste de algum decreto da época convertido em constante.
- **Risco se ignorarmos**: cálculo de benefício diverge do legado em todos os programas; passivo financeiro.
- **Classificação / Severidade**: blocks-stage-2 / Critical.

### MYS-002: CALCCORR órfão

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN` (programa inteiro)
- **O que esperávamos**: um chamador (`CALLNAT CALCCORR`) em BATCHPGT ou em rotina de reajuste anual.
- **O que o código faz**: calcula correção retroativa por IPCA (tabela hardcoded 2010–2012) e marca `IND-CORRIGIDO='S'`, mas nenhuma chamada foi confirmada.
- **Hipótese do time**: acionamento manual/sob demanda, ou código morto pós-2012.
- **Risco se ignorarmos**: ou se perde uma função de correção, ou se migra código morto.
- **Classificação / Severidade**: blocks-stage-2 / Critical. Caminho: buscar `CALCCORR` e `CALLNAT` em todos os `.NSN`.

### MYS-003: Região 99 (bypass de elegibilidade)

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#~L110-L130`
- **O que esperávamos**: toda região como código de UF (01–27).
- **O que o código faz**: região 99 ignora todas as checagens de elegibilidade (RN-005, "bypass do Roberto", uso diplomático/interno).
- **Risco se ignorarmos**: backdoor de autorização migrado sem controle.
- **Classificação / Severidade**: needs-facilitator / Medium-High.

### MYS-004: Tolerância 0,01 na conciliação

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#~L70-L110`
- **O que o código faz**: aceita divergência de ±0,01 entre valor SIFAP e retorno do banco. Origem do número não documentada.
- **Risco se ignorarmos**: regra de aceite/rejeição de pagamentos reproduzida errada.
- **Classificação / Severidade**: needs-investigation / Medium. Caminho: buscar `0.01` em `.NSN` e CNAB docs.

### MYS-005: Truncar vs. arredondar (INC-004)

- **Arquivo**: `CALCBENF.NSN#~L210-L230` (trunca) vs `BATCHREL.NSN#~L40-L90` (arredonda 0,005)
- **O que o código faz**: dois métodos de arredondamento para o mesmo tipo de valor monetário.
- **Risco se ignorarmos**: divergência de centavos em totalizadores e conciliação.
- **Classificação / Severidade**: needs-investigation / High.

### MYS-006: Prefixos especiais de CPF

- **Arquivo**: `VALDOCS.NSN#~L170-L190`; `VALBENEF.NSN#~L240-L260`
- **O que o código faz**: prefixos 000, 001, 002, 010, 011, 099, 100, 999 fazem a validação aceitar CPFs inválidos (governo/teste).
- **Risco se ignorarmos**: beneficiários "fantasma" migrados para produção.
- **Classificação / Severidade**: needs-investigation / Medium. Caminho: contar registros com esses prefixos na base.

### MYS-007: RELAUDIT oculta exclusões (EX)

- **Arquivo**: `RELAUDIT.NSN#~L45-L60`
- **O que o código faz**: filtra ações `EX` na exibição ("não corrigir sem aprovação da auditoria").
- **Risco se ignorarmos**: trilha de auditoria incompleta (IN-TCU 63/2010) ou, ao corrigir, expor algo sensível sem entender o motivo.
- **Classificação / Severidade**: needs-facilitator / Medium.

---

> Copie o bloco acima para cada mistério novo encontrado.

## Easter Eggs

> Dica: existem **3 easter eggs** escondidos no código legado. Registre aqui os que encontrar:

1. [ ] Easter Egg 1: \_\_\_
2. [ ] Easter Egg 2: \_\_\_
3. [ ] Easter Egg 3: \_\_\_

## Resumo

- Total de mistérios encontrados: 7
- Confiança alta: 4 (MYS-001, MYS-002, MYS-003, MYS-005)
- Confiança média: 3 (MYS-004, MYS-006, MYS-007)
- Confiança baixa: 0
- Bloqueadores do Estágio 2 (Critical): 2 (MYS-001, MYS-002)
- Precisam de investigação (High): 3 (MYS-004, MYS-005, MYS-006)
- Precisam de facilitador (Medium): 2 (MYS-003, MYS-007)
- Easter eggs encontrados: 0 / 3 (não investigados nesta passada)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

