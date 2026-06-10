# Instructions Index

Este diretório guarda as file-specific instructions do GitHub Copilot para o workshop.

> Importante: mantenha os arquivos `*.instructions.md` diretamente em `.github/instructions/`. A localização reconhecida pelo Copilot para instructions de workspace é flat (`.github/instructions/*.instructions.md`). Subdiretórios podem dificultar ou impedir a descoberta automática. A organização fica neste índice e nos nomes/descriptions dos arquivos.

## Arquitetura e Legado

| Arquivo | Quando se aplica |
| --- | --- |
| `modular-monolith.instructions.md` | Código Java/Spring, Maven/Gradle e arquitetura de Modular Monolith. |
| `natural-adabas.instructions.md` | Leitura do legado Natural/Adabas, DDMs, copycodes e artefatos em `01-arqueologia/legado-sifap/`. |

## Implementação

| Arquivo | Quando se aplica |
| --- | --- |
| `backend.instructions.md` | APIs, services, controllers, validação e fronteiras de backend. |
| `frontend.instructions.md` | Componentes e páginas frontend genéricas. |
| `frontend-spec.instructions.md` | Next.js 15 App Router, TypeScript strict, Tailwind CSS e shadcn/ui. |
| `database.instructions.md` | Repositories, migrations, schema, SQL, índices e mudanças de dados. |

## Entrega e Operação

| Arquivo | Quando se aplica |
| --- | --- |
| `cicd.instructions.md` | GitHub Actions, workflows YAML, gates de CI/CD e automação de build/deploy. |
| `infrastructure.instructions.md` | Terraform, Bicep, Azure IaC e configuração de ambientes. |

## Qualidade, Segurança e Requisitos

| Arquivo | Quando se aplica |
| --- | --- |
| `requirements.instructions.md` | Requisitos, EARS, critérios de aceite, rastreabilidade e documentação de requisitos. |
| `security.instructions.md` | Autenticação, autorização, crypto, secrets, configuração segura e código sensível. |
| `tests.instructions.md` | Testes automatizados, estratégia de testes, coverage gaps, regressão e quality gates. |

## Regra de Manutenção

- Cada arquivo deve manter frontmatter YAML válido com `description` e `applyTo`.
- Evite `applyTo: "**"`; prefira globs específicos.
- Quando criar uma nova área, adicione uma nova `*.instructions.md` flat neste diretório e atualize este índice.
