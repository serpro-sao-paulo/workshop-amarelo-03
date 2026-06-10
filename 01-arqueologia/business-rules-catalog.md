<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

> Faixas de linha marcadas com `~` são aproximadas (programa lido em alto nível); confirmar na leitura detalhada antes de usar como `source_legacy:` no Estágio 2. As demais foram confirmadas no código.

| ID     | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
| ------ | ---------------- | -------------- | ---------- | -------------- | ----- |
| BR-001 | O total de descontos **não judiciais** (tipo ≠ 'J') não pode exceder 30% do valor bruto; descontos judiciais não têm teto. | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-DESCONTO-TOTAL`, `PAGAMENTO.DESCONTOS.TIPO-DSCT` | CRÍTICO | Trecho confirmado (`IF #TIPO-DSCT NE 'J' ... 0.30`). |
| BR-002 | Contribuição social é progressiva por faixa de valor bruto (≈3% / 5% / 7,5% / 10%), aplicada de forma obrigatória. | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L56-L72` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.DESCONTOS` | CRÍTICO | Tabela de alíquotas hardcoded; valores tributários. |
| BR-003 | Benefício mensal = `VALOR-BASE(programa, faixa)` + (`ACRESCIMO-DEPEND` × `QT-DEPEND`). | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#~L100-L200` | `PROGRAMA-SOCIAL.VLR-BASE-INDIVIDUAL`, `PROGRAMA-SOCIAL.FAIXAS-CALCULO`, `BENEFICIARIO.DEPENDENTES` | CRÍTICO | Fórmula básica (RN-013). CALCBENF tem ~4.800 linhas e variações. |
| BR-004 | Valor do benefício é sempre **truncado** para baixo em centavos (não arredonda). Ex.: 125,567 → 125,56. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#~L210-L230` | `PAGAMENTO.VLR-LIQUIDO`, `PAGAMENTO.VLR-BRUTO` | CRÍTICO | Diverge de `BATCHREL` que arredonda a 0,005 — risco de divergência em totalizadores. |
| BR-005 | Em dezembro há 13º/abono natalino; programas tipo 'A' (assistencial) recebem acréscimo de 15%. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#~L380-L395` | `PROGRAMA-SOCIAL.TIPO`, `PAGAMENTO.VLR-BRUTO` | ALTO | Regra nunca documentada (pendência do Manual 2008). |
| BR-006 | Fator regional por UF (hardcoded, faixa ≈1,0–1,4) é aplicado ao valor do benefício. | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#~L150-L180` | `BENEFICIARIO.UF`, `PROGRAMA-SOCIAL` (parâmetros regionais) | ALTO | Constantes embutidas no código, sem parametrização externa. |
| BR-007 | A faixa de cálculo aplicável é a primeira cujo limite superior ≥ renda per capita declarada (ordem crescente). | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#~L120-L150` | `PROGRAMA-SOCIAL.FAIXAS-CALCULO`, `BENEFICIARIO.RENDA-FAMILIAR`, `BENEFICIARIO.QTD-MEMBROS-FAMILIA` | ALTO | RN-018. Até 5 faixas (PE) por programa. |
| BR-008 | `FATOR-K` = 1 + (`FATOR-REAJ` × 0.347215), aplicado no valor calculado do programa. | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#~L60-L80` | `PROGRAMA-SOCIAL.FATOR-K`, `PROGRAMA-SOCIAL.PCT-REAJUSTE-ANUAL` | CRÍTICO | Constante mágica 0.347215 sem origem documentada — ver mistério. |
| BR-009 | Correção monetária retroativa usa índices IPCA hardcoded (2010–2012); marca `IND-CORRIGIDO='S'`. | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#~L40-L90` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.IND-CORRIGIDO` | ALTO | Tabela hardcoded; bloco comentado do Plano Verão 1989–1991. |
| BR-010 | CPF é validado por dígito verificador módulo 11; CPFs de governo/teste (prefixo 000…) são aceitos como exceção. | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#~L240-L260` | `BENEFICIARIO.NUM-CPF` | CRÍTICO | CPFs com todos dígitos iguais são inválidos, salvo exceção governo. |
| BR-011 | Prefixos especiais de CPF (000, 001, 002, 010, 011, 099, 100, 999) ignoram a validação de documentos. | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#~L170-L190` | `BENEFICIARIO.NUM-CPF`, `BENEFICIARIO.RG` | ALTO | Bypass para registros de governo/teste; escopo real a confirmar. |
| BR-012 | Beneficiário com região 99 (especial/diplomático) é sempre elegível — ignora todas as checagens. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#~L110-L130` | `BENEFICIARIO.UF` (código região), `PROGRAMA-SOCIAL` | CRÍTICO | RN-005 ("bypass do Roberto"); origem não documentada. |
| BR-013 | Programa tipo 'A' (assistencial) com renda > 600 exige pelo menos 1 dependente para ser elegível. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#~L180-L195` | `PROGRAMA-SOCIAL.TIPO`, `BENEFICIARIO.RENDA-FAMILIAR`, `BENEFICIARIO.DEPENDENTES` | ALTO | Regra de elegibilidade por faixa de renda. |
| BR-014 | Idade do beneficiário deve respeitar o tipo do programa: 'P' ≥ 60 anos; 'T' entre 16 e 65 anos. | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#~L140-L175` | `PROGRAMA-SOCIAL.IDADE-MIN`, `PROGRAMA-SOCIAL.IDADE-MAX`, `BENEFICIARIO.DT-NASCIMENTO` | ALTO | Idade mín./máx. parametrizada por programa. |
| BR-015 | Inclusão de beneficiário exige idade ≥ 16 anos na data de inclusão (exceto como dependente). | `01-arqueologia/legado-sifap/natural-programs/CADBENF.NSN#~L90-L120` | `BENEFICIARIO.DT-NASCIMENTO` | MÉDIO | RN-006. |
| BR-016 | Processamento mensal (1º dia útil) recalcula todos os beneficiários com status 'A', na ordenação do descritor Adabas, invocando CALCBENF e CALCDSCT. | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#~L80-L160` | `BENEFICIARIO.SIT-BENEFICIARIO`, `PAGAMENTO.ANO-MES-REF`, `PAGAMENTO.NUM-CICLO` | CRÍTICO | Ordenação afeta totalizadores acumulados — não pode mudar. |
| BR-017 | Conciliação CNAB 240 compara valor SIFAP × retorno com tolerância ±0,01 e mapeia códigos 00→Pago, 01→Devolvido, 02→Erro. | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#~L70-L110` | `PAGAMENTO.VLR-LIQUIDO`, `PAGAMENTO.SIT-PAGAMENTO`, `PAGAMENTO.COD-RETORNO-BANCO` | CRÍTICO | Origem da tolerância 0,01 não documentada. |
| BR-018 | Relatório consolidado agrupa por região (faixas de UF 1-5→N, 6-10→NE, …) e arredonda valores a 0,005. | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#~L40-L90` | `PAGAMENTO.VLR-BRUTO`, `BENEFICIARIO.UF` | MÉDIO | Arredonda (vs. CALCBENF que trunca). |
| BR-019 | O relatório de auditoria oculta ações de exclusão (`EX`) na exibição. | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#~L45-L60` | `AUDITORIA.COD-ACAO` | ALTO | "Não corrigir sem aprovação da auditoria" — investigar. |
| BR-020 | Beneficiários com mais de 75 anos têm o status ajustado automaticamente no cadastro. | `01-arqueologia/legado-sifap/natural-programs/CADBENF.NSN#~L130-L160` | `BENEFICIARIO.DT-NASCIMENTO`, `BENEFICIARIO.SIT-BENEFICIARIO` | MÉDIO | Regra implícita de manutenção de status. |
| BR-011 |                  |                |            |                |       |
| BR-012 |                  |                |            |                |       |
| BR-013 |                  |                |            |                |       |
| BR-014 |                  |                |            |                |       |
| BR-015 |                  |                |            |                |       |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

## Regras por Categoria

### Cálculos Financeiros

- BR-001 (teto de descontos), BR-002 (contribuição progressiva), BR-003 (fórmula do benefício), BR-004 (truncamento), BR-005 (13º/abono), BR-006 (fator regional), BR-007 (faixa por renda), BR-008 (FATOR-K), BR-009 (correção IPCA), BR-017 (conciliação ±0,01), BR-018 (arredondamento no relatório).

### Validações de Status

- BR-016 (processa apenas status 'A'), BR-020 (ajuste de status >75 anos). Domínio de status: `A`=Ativo, `S`=Suspenso, `C`=Cancelado, `I`=Inativo, `D`=Desligado.

### Regras de Autorização

- BR-012 (região 99 ignora elegibilidade), BR-010 / BR-011 (exceções de CPF de governo/teste), BR-019 (exclusões `EX` ocultadas no relatório de auditoria).

### Regras de Negócio Temporais

- BR-005 (13º só em dezembro), BR-016 (ciclo mensal no 1º dia útil), BR-009 (correção retroativa por competência).

## Resumo Estatístico

- Total de regras encontradas: 20
- Regras críticas: 9 (BR-001, BR-002, BR-003, BR-004, BR-008, BR-010, BR-012, BR-016, BR-017)
- Regras com duplicação: 0 confirmadas (investigar sobreposição BR-006 fator regional vs. parâmetros regionais em `PROGRAMA-SOCIAL`)
- Regras sem documentação (escondidas): 6 (BR-005 13º/abono, BR-006 fator regional, BR-008 FATOR-K, BR-012 região 99, BR-017 tolerância 0,01, BR-019 filtro EX)
- Cobertura: 12 de 15 programas tocados; 100% das linhas com `Programa Fonte` preenchido.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

