---
description: "Verifique se o código atende aos critérios de aceitação de SPECIFICATION.md. Use durante UAT ou revisão de sprint."
argument-hint: "req=REQ-015 file=PaymentService.java"
agent: ask
tools: ['search/codebase']
---

<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# /acceptance-check

## Passos
1. Leia a seção relevante de SPECIFICATION.md
2. Extraia todos os critérios Given/When/Then
3. Busque no codebase implementações correspondentes
4. Busque cobertura nos arquivos de teste
5. Produza um relatório de conformidade

## Saída
| Critério | Implementado | Testado | Status |
|-----------|-------------|--------|--------|
| [text] | Sim/Não | Sim/Não | Passa/Falha/Lacuna |
