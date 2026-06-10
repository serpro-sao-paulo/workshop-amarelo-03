<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Preencha esta tabela com todos os termos, abreviações e siglas encontrados no código Natural/Adabas.
> **Meta: no mínimo 30 termos.**

## Por que isso importa

Sistemas legados têm vocabulário próprio que ninguém documenta em lugar nenhum — só está no nome das variáveis. Se o time do Estágio 2 não souber o que `DSCT`, `BENF`, `PE` ou `CTC` significam, vai escrever uma spec sobre o que ele _acha_ que isso significa. Glossário é o que evita esse desencontro.

## Como preencher

- **Termo**: a abreviação ou sigla exatamente como aparece no código
- **Expansão**: o significado completo do termo
- **Programa**: em qual arquivo `.NSN` ou `.ddm` o termo foi encontrado
- **Contexto**: breve explicação de como/onde o termo é usado

## Dica de extração

Prompt útil no Copilot Chat (cole o conteúdo de 2–3 arquivos `.NSN` no chat antes):

> _"Liste todas as abreviações e siglas usadas neste código Natural. Para cada uma, sugira a expansão e marque com 'CONFIRMADO' ou 'HIPÓTESE'."_

## Termos encontrados

| #   | Termo | Expansão | Programa | Contexto |
| --- | ----- | -------- | -------- | -------- |
| 1   | `SIFAP` | Sistema de Fiscalização e Administração de Pagamentos | Todos os `.NSN` | Nome do sistema legado Natural/Adabas de pagamento de benefícios sociais. |
| 2   | `BENF` | Beneficiário | `CADBENF.NSN`, `CALCBENF.NSN`, `VALBENEF.NSN`, `CONSBENF.NSN` | Pessoa cadastrada para receber benefício de programa social (entidade `BENEFICIARIO`, arq. 150). |
| 3   | `DEPEND` / `PE DEPENDENTES` | Dependentes (Periodic Group) | `CADDEPEND.NSN`, `BENEFICIARIO.ddm` | Grupo periódico de até 10 ocorrências com CPF, nome, parentesco do dependente do titular. |
| 4   | `PROG` | Programa Social | `CADPROG.NSN`, `PROGRAMA-SOCIAL.ddm` | Parametrização de programa de benefício (entidade `PROGRAMA-SOCIAL`, arq. 151). |
| 5   | `PGTO` | Pagamento | `BATCHPGT.NSN`, `RELPGT.NSN`, `PAGAMENTO.ddm` | Registro de pagamento processado (entidade `PAGAMENTO`, arq. 152, ~180M registros). |
| 6   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Dedução sobre o valor bruto. Tipos: IR, JD, CS, PA, EM, TX, OU, EX. |
| 7   | `CALC` | Cálculo | `CALCBENF.NSN`, `CALCCORR.NSN`, `CALCDSCT.NSN` | Prefixo de programas de cálculo (benefício, correção, desconto). |
| 8   | `VAL` | Validação | `VALBENEF.NSN`, `VALDOCS.NSN`, `VALELEG.NSN` | Prefixo de rotinas de validação (cadastro, documentos, elegibilidade). |
| 9   | `CAD` | Cadastro | `CADBENF.NSN`, `CADDEPEND.NSN`, `CADPROG.NSN` | Prefixo de programas de cadastro/CRUD online. |
| 10  | `CONS` | Consulta | `CONSBENF.NSN` | Prefixo de programa de consulta online (tela 3270). |
| 11  | `REL` | Relatório | `RELPGT.NSN`, `RELAUDIT.NSN`, `BATCHREL.NSN` | Prefixo de programas de geração de relatório. |
| 12  | `BATCH` | Processamento batch | `BATCHPGT.NSN`, `BATCHCON.NSN`, `BATCHREL.NSN` | Prefixo de entry points de job batch. |
| 13  | `CORR` | Correção (monetária) | `CALCCORR.NSN` | Correção retroativa de pagamentos por variação IPCA. |
| 14  | `CON` (em `BATCHCON`) | Conciliação | `BATCHCON.NSN` | Conciliação bancária de retorno CNAB 240 contra pagamentos gerados. |
| 15  | `ELEG` | Elegibilidade | `VALELEG.NSN` | Validação de elegibilidade do beneficiário a um programa. |
| 16  | `DOCS` | Documentos | `VALDOCS.NSN` | Validação de documentos (CPF, RG) do beneficiário. |
| 17  | `AUDIT` | Auditoria | `RELAUDIT.NSN`, `AUDITORIA.ddm` | Trilha de auditoria imutável (IN-TCU 63/2010, arq. 153, ~25M registros). |
| 18  | `FATOR-K` | Fator K (multiplicador não documentado) | `CADPROG.NSN`, `PROGRAMA-SOCIAL.ddm` | Campo inserido em ago/2008; `FATOR-K = 1 + (FATOR-REAJ × 0.347215)`. Constante mágica — ver mistérios. |
| 19  | `SIT-BENEFICIARIO` | Situação do beneficiário | `CADBENF.NSN`, `VALBENEF.NSN`, `BENEFICIARIO.ddm` | Status: `A`=Ativo, `S`=Suspenso, `C`=Cancelado, `I`=Inativo, `D`=Desligado. |
| 20  | `SIT-PAGAMENTO` | Situação do pagamento | `BATCHPGT.NSN`, `BATCHCON.NSN`, `PAGAMENTO.ddm` | Status: `P`=Pago, `G`=Gerado, `E`=Emitido, `C`=Confirmado, `D`=Devolvido, `X`=Cancelado, `R`=Reprocessado. |
| 21  | `TIPO` (programa) | Tipo de programa social | `VALELEG.NSN`, `CALCBENF.NSN`, `PROGRAMA-SOCIAL.ddm` | `A`=Assistencial, `P`=Previdenciário, `T`=Trabalho. |
| 22  | `ANO-MES-REF` / Competência | Competência (AAAAMM) | `BATCHPGT.NSN`, `RELPGT.NSN`, `PAGAMENTO.ddm` | Mês de referência do pagamento, formato `AAAAMM` (6 dígitos). |
| 23  | `NUM-CICLO` | Número do ciclo | `BATCHPGT.NSN`, `PAGAMENTO.ddm` | Identifica a execução do ciclo mensal de pagamento. |
| 24  | `NUM-INSCRICAO` / `NIS` | Número de inscrição social | `CADBENF.NSN`, `CONSBENF.NSN`, `BENEFICIARIO.ddm` | Identificador do beneficiário (11 dígitos). |
| 25  | `Mod 11` | Módulo 11 (dígito verificador) | `VALBENEF.NSN`, `VALDOCS.NSN` | Algoritmo de validação de CPF; exceções para CPFs de governo/teste. |
| 26  | `VLR-BRUTO` / `VLR-LIQUIDO` | Valor bruto / líquido | `CALCBENF.NSN`, `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Valor do benefício antes/depois dos descontos. |
| 27  | `FATOR-REGIONAL` | Fator regional | `CALCBENF.NSN`, `BATCHPGT.NSN` | Multiplicador hardcoded por UF (faixa 1.0–1.4) aplicado no cálculo. |
| 28  | `Região 99` | Região especial/diplomática | `VALELEG.NSN` | Código de região que ignora todas as checagens de elegibilidade (diplomático/internacional). |
| 29  | `CNAB 240` | Centro Nacional de Automação Bancária (layout 240) | `BATCHCON.NSN` | Layout de arquivo de retorno bancário (BB) usado na conciliação. |
| 30  | `SIAFI` | Sistema Integrado de Administração Financeira | `PAGAMENTO.ddm` | Integração orçamentária (campos `NUM-OB-SIAFI`, `NUM-NE-SIAFI`), adicionada em 2002. |
| 31  | `PE` | Periodic Group (grupo periódico Adabas) | `BENEFICIARIO.ddm`, `PAGAMENTO.ddm`, `PROGRAMA-SOCIAL.ddm` | Estrutura Adabas de ocorrências repetidas (ex.: `DEPENDENTES`, `DESCONTOS`, `FAIXAS-CALCULO`). |
| 32  | `MU` | Multiple Value Field (campo multivalorado Adabas) | DDMs | Campo Adabas que admite múltiplos valores na mesma ocorrência. |
| 33  | `descriptor` | Descritor / superdescritor Adabas | `BENEFICIARIO.ddm`, `PAGAMENTO.ddm` | Campo indexado para busca (`FIND`); superdescritores combinam campos (ex.: UF+SITUACAO). |
| 34  | `DDM` | Data Definition Module | `*.ddm` | Definição de view Adabas usada pelos programas Natural (`DEFINE DATA ... VIEW`). |
| 35  | `CALLNAT` | Chamada de subprograma Natural | `BATCHPGT.NSN` | Comando Natural que invoca outro programa (ex.: cálculo de benefício/desconto). |
| 36  | `FIND` / `STORE` / `UPDATE` | Operações Adabas (ler / inserir / alterar) | Todos os `.NSN` | `FIND VIEW WITH chave = valor`; `STORE` insere; `UPDATE` modifica registro. |
| 37  | `13º` / Décimo | Décimo terceiro benefício | `CALCBENF.NSN`, `BATCHPGT.NSN` | Pagamento adicional em dezembro proporcional a meses ativos/12. |
| 38  | `Abono natalino` | Abono de fim de ano | `CALCBENF.NSN` | Acréscimo de 15% no benefício, apenas para programas tipo `A` (assistencial). |
| 39  | `Parentesco` | Grau de parentesco do dependente | `CADDEPEND.NSN`, `BENEFICIARIO.ddm` | `FI`=Filho, `CO`/`CJ`=Cônjuge, `IR`=Irmão, `NT`=Neto, `TU`=Tutelado, `OU`=Outro. |
| 40  | `COD-ACAO` | Código de ação de auditoria | `AUDITORIA.ddm`, `RELAUDIT.NSN` | `IN`=Inclusão, `AL`=Alteração, `EX`=Exclusão, `CO`=Consulta, `LG`/`LO`=Login/Logout, `BT`=Batch, `ER`=Erro. |

> Adicione mais linhas conforme necessário. Não se limite a 30!

## Exemplo de linha bem preenchida

| #   | Termo  | Expansão | Programa                        | Contexto                                                                                                         |
| --- | ------ | -------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Tipo de dedução aplicada sobre valor bruto do pagamento. Tipos: 'J' (judicial), 'I' (imposto), 'T' (trabalhista) |

## Observações

- **Padrões de nomenclatura identificados:**
  - Programas seguem `PREFIXO + RADICAL` de domínio: `CAD*` (cadastro), `CALC*` (cálculo), `VAL*` (validação), `CONS*` (consulta), `REL*` (relatório), `BATCH*` (batch).
  - Radicais de domínio reaproveitados: `BENF`/`BENEF` (beneficiário) aparece em `CADBENF`, `CALCBENF`, `VALBENEF`, `CONSBENF`, indicando forte acoplamento na entidade Beneficiário.
- **Convenções de prefixo/sufixo encontradas:**
  - Campos de DDM usam `NUM-` (numérico/identificador), `VLR-` (valor monetário), `DT-` (data), `SIT-` (situação/status), `COD-` (código), `IND-` (indicador booleano), `PCT-` (percentual), `USR-` (usuário).
  - Variáveis de trabalho Natural usam prefixo `#` (ex.: `#OPER`, `#ERRO`, `#FOUND`, `#VLR-TEMP`).
  - Códigos de status são single-char com domínio fixo (ver termos `SIT-BENEFICIARIO`, `SIT-PAGAMENTO`, `TIPO`).
- **Termos ambíguos que precisam de validação com especialista:**
  - `FATOR-K` e a constante `0.347215` em `CADPROG.NSN` — origem e fórmula não documentadas (ver `mysteries-found.md`).
  - Tolerância de `0.01` na conciliação de `BATCHCON.NSN` — especificação CNAB ou erro de arredondamento?
  - Divergência arredondar (`BATCHREL`) vs truncar (`CALCBENF`) — impacto financeiro a confirmar.
  - Prefixos especiais de CPF (000, 001, 002, 010, 011, 099, 100, 999) tratados como válidos — escopo de uso real a confirmar.

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
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

