<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Decisões de Escopo — SIFAP 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S2](https://img.shields.io/badge/PREENCHA-Durante%20S2-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Scope Decisions**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 2 (Spec Moderna).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento preenchido para sua feature
> 2. Rastreabilidade `source_legacy:` para cada REQ-ID
> 3. Sign-off do Product Owner antes da passagem H2
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Para cada funcionalidade encontrada no Estágio 1, decida: **Migrar**, **Descartar** ou **Evoluir**.
>
> - **Migrar**: trazer para o SIFAP 2.0 como está (mesma lógica, nova tecnologia)
> - **Descartar**: não trazer — funcionalidade obsoleta ou desnecessária
> - **Evoluir**: trazer E melhorar (nova UX, novo fluxo, nova capacidade)

**Time**: Equipe SIFAP
**Data**: 2026-06-10
**Edição**:
**Par 1 (Product Owner) responsável**: Luana

## Por que isso importa

O escopo é o que protege o time de chegar às 17h00 com 12 features pela metade. Se o Par 1 não cortar, o Estágio 3 não fecha. **Decisão difícil é tomada aqui, não no Estágio 3.**

## Como decidir

Pergunte de cada funcionalidade:

1. **Afeta o ciclo mensal de pagamento?** Sim → Migrar. Não → considere descartar.
2. **Tem uso documentado nos últimos 12 meses?** Não → descartar.
3. **Faz parte de um relatório regulatório obrigatório (TCU, CGU, BB)?** Sim → Migrar como está.
4. **Tem uma versão moderna mais barata de implementar?** Sim → Evoluir.

---

## Decisões por Funcionalidade

| #   | Funcionalidade            | Decisão   | Justificativa | Regra de Negócio (BR-XXX) | Prioridade |
| --- | ------------------------- | --------- | ------------- | ------------------------- | ---------- |
| 1   | Cadastro de Beneficiários | Migrar    | Entidade central que alimenta cálculo e elegibilidade (discovery §5.1, prioridade 2). | BR-002 (status A/S/C/I/D) | Alta |
| 2   | Consulta de Beneficiários | Migrar    | Necessária para operação online (perfis OPR/CON) e auditoria. | — | Média |
| 3   | Registro de Pagamentos    | Migrar    | Parte do núcleo financeiro; gera os registros do ciclo. | BR-016 | Alta |
| 4   | Processamento Batch (folha mensal) | Migrar | Núcleo do sistema; gera a folha mensal processando só status 'A' (discovery §5.1, prioridade 1). | BR-016 | Alta |
| 5   | Cálculo de Benefícios     | Migrar    | Concentra 9 regras críticas (valor base + dependente + FATOR-K). | BR-003, BR-008 | Alta |
| 6   | Cálculo de Descontos      | Migrar    | Teto de 30% para não judiciais; judicial sem teto. | BR-001 | Alta |
| 7   | Validação de CPF          | Migrar    | Validação módulo 11 obrigatória no cadastro. | BR-002 (validação cadastral) | Alta |
| 8   | Conciliação Bancária CNAB 240 | Migrar | Fecha o ciclo financeiro e a integração externa (discovery §5.1, prioridade 3). | BR-017 | Alta |
| 9   | Auditoria                 | Evoluir   | Migrar a trilha E adicionar imutabilidade técnica (IN-TCU). | — | Alta |
| 10  | Relatórios (RELPGT/RELAUDIT) | Evoluir | Viram read models por contexto, não módulo próprio. | — | Média |
| 11  | Cálculo de Correção/Reajuste (CALCCORR) | Descartar (provisório) | `CALCCORR` é órfão (MYS-002) — decidir migrar/descartar após resolver com facilitador. | — | Baixa |
| 12  | Gestão de Usuários        | Evoluir   | Substituída por autenticação federada gov.br + RBAC (ver ADR-003). | — | Média |
| 13  | Cálculo do 13º / abono natalino | Descartar (provisório) | Fórmula não confirmada no legado; sem fonte para EARS. Reavaliar em ciclo futuro. | — | Baixa |

> Decisões derivadas de `discovery-report.md` §5 e dos bounded contexts. Itens "Descartar (provisório)" dependem de resolução de mistério antes do Estágio 3.

---

## Funcionalidades Novas (não existem no legado)

> Liste funcionalidades que o SIFAP 2.0 deveria ter e que não existem no sistema legado. Cada uma vira REQ-ID com `source_legacy: [GREENFIELD] <justificativa>`.

| #   | Funcionalidade Nova | Justificativa | Prioridade | Complexidade |
| --- | ------------------- | ------------- | ---------- | ------------ |
| N1  | Imutabilidade técnica da trilha de auditoria (REQ-016) | Legado é append-only por convenção, sem proteção; compliance IN-TCU exige garantia técnica. | Alta | Baixa |
| N2  | Mascaramento de CPF/valores em logs (REQ-017) | LGPD/OWASP — não existe no terminal 3270 legado. | Alta | Baixa |
| N3  | API REST + autenticação federada gov.br (ADR-003) | Substitui terminal 3270; SSO e RBAC por perfil. | Alta | Média |

---

## Resumo de Escopo

| Decisão   | Quantidade | Percentual |
| --------- | ---------- | ---------- |
| Migrar    | 8          | 62%        |
| Descartar | 2 (provisório) | 15%    |
| Evoluir   | 3          | 23%        |
| **Total** | 13         | 100%       |

## Riscos de Escopo

> Liste os riscos das decisões tomadas:

| Risco | Probabilidade | Impacto | Mitigação |
| ----- | ------------- | ------- | --------- |
| FATOR-K (MYS-001) com constante `0.347215` sem origem documentada | Alta | Alto | Resolver com facilitador antes de implementar REQ-006; estrutura da fórmula já especificada. |
| `CALCCORR` órfão (MYS-002) descartado por engano sendo usado | Média | Alto | Confirmar uso real antes do Estágio 3; manter como "descartar provisório". |
| Divergência de arredondamento truncar vs. arredondar (MYS-005) | Alta | Médio | Definir política única em `shared/money` (ADR pendente / clarify). |
| 13º/abono natalino fora de escopo gera gap funcional | Média | Médio | Documentar como pendência; reavaliar em ciclo futuro com fórmula confirmada. |

## Aprovação

- [x] Par 1 (Product Owner) aprovou as decisões de escopo — Luana, 2026-06-10
- [x] Par 2 (Enterprise Architect) validou a viabilidade técnica
- [ ] Par 3 (Technical Lead) confirmou que cabe nas 3 horas do Estágio 3
- [ ] Time concordou com as prioridades

> ✅ Decisões preenchidas a partir do `discovery-report.md` §5 pelo Par 2 e aprovadas pelo Par 1 (Product Owner — Luana) em 2026-06-10.

> **Aprovação obrigatória na Passagem #2** (~16:00). Sem ela, o Estágio 3 não começa.

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR-TEMPLATE.md"><strong>ADR-TEMPLATE</strong></a><br/>
<sub>Template de ADR.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>

