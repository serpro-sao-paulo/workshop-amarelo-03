# Prompts Index

Este diretório guarda os prompt files do GitHub Copilot para o workshop.

> Importante: mantenha os arquivos `*.prompt.md` diretamente em `.github/prompts/`. A localização workspace documentada pelo Copilot é flat (`.github/prompts/*.prompt.md`). A organização por estágio/persona fica no prefixo do nome do arquivo e neste índice.

## Convenção de Nome

| Prefixo | Uso |
| --- | --- |
| `stage-<agent>-<task>.prompt.md` | Prompts dos agentes de estágio (`archaeologist`, `architect`, `builder`, `evolution`). |
| `persona-<persona>-<task>.prompt.md` | Prompts dos kits de persona (`product-owner`, `developer`, `qa-engineer` etc.). |

## Prompts de Estágio

| Agente | Arquivos |
| --- | --- |
| `archaeologist` | `stage-archaeologist-*.prompt.md` |
| `architect` | `stage-architect-*.prompt.md` |
| `builder` | `stage-builder-*.prompt.md` |
| `evolution` | `stage-evolution-*.prompt.md` |

## Prompts de Persona

| Persona | Arquivos |
| --- | --- |
| Product Owner | `persona-product-owner-*.prompt.md` |
| Requirements Engineer | `persona-requirements-engineer-*.prompt.md` |
| Enterprise Architect | `persona-enterprise-architect-*.prompt.md` |
| Software Architect | `persona-software-architect-*.prompt.md` |
| Technical Lead | `persona-technical-lead-*.prompt.md` |
| Developer | `persona-developer-*.prompt.md` |
| DBA | `persona-dba-*.prompt.md` |
| QA Engineer | `persona-qa-engineer-*.prompt.md` |
| DevOps Engineer | `persona-devops-engineer-*.prompt.md` |
| Tech Writer | `persona-tech-writer-*.prompt.md` |

## Regra de Manutenção

- Cada prompt deve ter frontmatter YAML válido.
- Prefira `description`, `argument-hint`, `agent` e `tools` explícitos. Não fixe `model` — deixe a escolha do modelo a cargo de quem executa o prompt.
- Evite tools em excesso; use o menor conjunto necessário para a tarefa.
- Se adicionar um prompt novo, use um dos prefixos acima para manter a descoberta e a organização.
