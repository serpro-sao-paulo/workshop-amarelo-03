<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Guia de Setup — Do Zero ao Código

![SETUP 00](https://img.shields.io/badge/SETUP-00-00A4EF?style=for-the-badge) ![DURAÇÃO 45 min](https://img.shields.io/badge/DURAÇÃO-45%20min-1A1A1A?style=for-the-badge) ![QUANDO Antes do dia 2](https://img.shields.io/badge/QUANDO-Antes%20do%20dia%202-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](README.md) → **Setup**


> **Para quem é isto?** Para a pessoa que vai preparar o ambiente do time antes do dia 2 — líder do time ou cada membro no próprio laptop.
>
> **O que você terá ao final desta leitura:**
>
> 1. Repositório do time criado via **"Use this template"** na organização [serpro-sao-paulo](https://github.com/serpro-sao-paulo), **público**, nomeado `workshop-<cor>-<numero>`
> 2. GitHub Copilot funcionando no VS Code (o **Spec-Kit já vem instalado** — não há nada para instalar)
> 3. Cada pessoa com seu persona-kit copiado e Ask/Plan/Agent validados
> 4. Estratégia de branches definida (`spec/*`, `impl/*`)
> 5. Time alinhado para começar pela **Arqueologia** (Estágio 1) antes das 10:00 do dia 2

> **Vocês são 5 pessoas. Cada pessoa usa 2 personas. Vocês têm um dia de trabalho.** Este guia leva vocês de "ainda não temos nada" até "repositório do time criado, Copilot funcionando, todas as personas prontas" em **45 minutos**.
>
> **Todas as pessoas do time devem acompanhar no próprio laptop.** Uma pessoa compartilha a tela com os passos, e as outras 4 repetem. Ao final, cada laptop estará totalmente configurado.
>
> [!WARNING]
> **Usuários de Windows:** todos os comandos de terminal abaixo assumem **Git Bash** ou **WSL**. **Não** use PowerShell ou CMD — `cp -R`, `chmod`, `rm -rf` e a sintaxe de heredoc não vão funcionar.

## Sumário

- [Guia de Setup — Do Zero ao Código](#guia-de-setup--do-zero-ao-código)
  - [Sumário](#sumário)
  - [📋 Antes de Começar — Modelo Mental](#-antes-de-começar--modelo-mental)
  - [✅ Passo 1 — Verifique se seu laptop tem os pré-requisitos](#-passo-1--verifique-se-seu-laptop-tem-os-pré-requisitos)
    - [Verificação de licença (uma pessoa verifica pelo time)](#verificação-de-licença-uma-pessoa-verifica-pelo-time)
  - [👥 Passo 2 — Crie o repositório do time com "Use this template" (somente líder)](#-passo-2--crie-o-repositório-do-time-com-use-this-template-somente-líder)
    - [Passo a passo](#passo-a-passo)
  - [📥 Passo 3 — O líder clona e cria a branch `develop` (somente líder)](#-passo-3--o-líder-clona-e-cria-a-branch-develop-somente-líder)
  - [🛡️ Passo 4 — Proteja a branch `main` (somente líder)](#️-passo-4--proteja-a-branch-main-somente-líder)
    - [Usando o site](#usando-o-site)
    - [Usando a CLI](#usando-a-cli)
  - [🎟️ Passo 5 — Adicione os outros 4 membros do time (somente líder)](#️-passo-5--adicione-os-outros-4-membros-do-time-somente-líder)
    - [Opção A — usando o site](#opção-a--usando-o-site)
    - [Opção B — usando a CLI](#opção-b--usando-a-cli)
  - [💻 Passo 6 — Cada membro clona o repositório do time](#-passo-6--cada-membro-clona-o-repositório-do-time)
    - [6.1 Aceite o convite](#61-aceite-o-convite)
    - [6.2 Clone e mude para `develop`](#62-clone-e-mude-para-develop)
    - [6.3 Abra no VS Code](#63-abra-no-vs-code)
  - [🤖 Passo 7 — Ative o GitHub Copilot no VS Code (todos)](#-passo-7--ative-o-github-copilot-no-vs-code-todos)
    - [7.1 Faça login](#71-faça-login)
    - [7.2 Abra o painel do Copilot Chat](#72-abra-o-painel-do-copilot-chat)
    - [7.3 Verifique se os 3 modos estão disponíveis](#73-verifique-se-os-3-modos-estão-disponíveis)
    - [7.4 Teste de fumaça do Copilot](#74-teste-de-fumaça-do-copilot)
  - [🎭 Passo 8 — Leia o `PERSONA.md` da sua persona (todos)](#-passo-8--leia-o-personamd-da-sua-persona-todos)
    - [8.1 Encontre seu papel e leia sua carta](#81-encontre-seu-papel-e-leia-sua-carta)
    - [8.2 Mapeamento persona → carta](#82-mapeamento-persona--carta)
    - [8.3 Verifique os agentes e slash commands no Copilot](#83-verifique-os-agentes-e-slash-commands-no-copilot)
  - [📐 Passo 9 — Confirme o Spec-Kit (já vem instalado)](#-passo-9--confirme-o-spec-kit-já-vem-instalado)
    - [9.1 Confirme os comandos no Copilot](#91-confirme-os-comandos-no-copilot)
    - [9.2 Escreva uma funcionalidade](#92-escreva-uma-funcionalidade)
    - [9.3 Regra do workshop](#93-regra-do-workshop)
  - [🎯 Passo 10 — Use o fluxo Spec-Kit (todos)](#-passo-10--use-o-fluxo-spec-kit-todos)
  - [🌿 Passo 11 — Entenda a estratégia de branches](#-passo-11--entenda-a-estratégia-de-branches)
    - [Convenção de nomes](#convenção-de-nomes)
    - [Criando uma branch de funcionalidade](#criando-uma-branch-de-funcionalidade)
    - [Abrindo um Pull Request](#abrindo-um-pull-request)
  - [🔄 Passo 12 — Fluxo diário por persona](#-passo-12--fluxo-diário-por-persona)
    - [🧠 Product Owner / Requirements Engineer](#-product-owner--requirements-engineer)
    - [🏗️ Enterprise Architect / Software Architect](#️-enterprise-architect--software-architect)
    - [🧠 Technical Lead](#-technical-lead)
    - [💻 Developer](#-developer)
    - [🗃️ DBA](#️-dba)
    - [🧪 QA Engineer](#-qa-engineer)
    - [⚙️ DevOps Engineer](#️-devops-engineer)
    - [📝 Tech Writer](#-tech-writer)
  - [🚦 Passo 13 — Rode o teste de fumaça (time inteiro, às 10:30)](#-passo-13--rode-o-teste-de-fumaça-time-inteiro-às-1030)
  - [Solução de problemas](#solução-de-problemas)
    - [Copilot não lê `copilot-instructions.md`](#copilot-não-lê-copilot-instructionsmd)
    - ["Use this template" não aparece ou o nome do repositório é recusado](#use-this-template-não-aparece-ou-o-nome-do-repositório-é-recusado)
    - [Os comandos `/speckit.*` não aparecem no Copilot](#os-comandos-speckit-não-aparecem-no-copilot)
    - [CI falha no primeiro push com "no tests found"](#ci-falha-no-primeiro-push-com-no-tests-found)
    - [Docker compose up trava ou falha](#docker-compose-up-trava-ou-falha)
    - [Copilot Agent mode não aparece no dropdown](#copilot-agent-mode-não-aparece-no-dropdown)
    - ["Permission denied" ao fazer push para `main`](#permission-denied-ao-fazer-push-para-main)
    - [Fiz pull do `develop` mais recente, mas minha IDE ainda mostra código antigo](#fiz-pull-do-develop-mais-recente-mas-minha-ide-ainda-mostra-código-antigo)
    - [Continuar a leitura](#continuar-a-leitura)

---

## 📋 Antes de Começar — Modelo Mental

Este kit é um **template do GitHub**. Vocês não clonam e copiam nada à mão: o líder clica em **"Use this template"** e o GitHub cria um repositório novo do time já com **tudo dentro** — documentação, personas, código legado SIFAP (`01-arqueologia/legado-sifap/`), workflows de CI e a base do Spec-Kit.

Você vai terminar com **1 repositório de trabalho**:

| Repositório | O que é | Onde fica |
| --- | --- | --- |
| **Kit / template** | Referência somente leitura. Origem do "Use this template". | github.com/serpro-sao-paulo/&lt;repo-do-kit&gt; |
| **`workshop-<cor>-<numero>`** | **Todo o trabalho do time vai aqui.** Criado pelo líder via template. | github.com/serpro-sao-paulo/workshop-&lt;cor&gt;-&lt;numero&gt; (**público**) |

> [!IMPORTANT]
> **O código legado SIFAP já vem dentro** do repositório do time (em `01-arqueologia/legado-sifap/`). Você lê durante a Arqueologia, mas **nunca edita** o legado.

> [!NOTE]
> **A nova aplicação ainda não existe.** Não há `Dockerfile`, backend nem frontend prontos neste kit — **o time cria tudo** a partir das specs, usando o Spec-Kit no Estágio 3. Você começa pela Arqueologia (ler o legado), não por código.

---

## ✅ Passo 1 — Verifique se seu laptop tem os pré-requisitos

**Cada membro do time roda este checklist no próprio laptop.**

| Ferramenta         | Versão mínima | Como verificar                           | Se estiver faltando                                 |
| ------------------ | ------------- | ---------------------------------------- | --------------------------------------------------- |
| **Git**            | 2.40+         | Abra um terminal, digite `git --version` | <https://git-scm.com/downloads>                     |
| **Conta GitHub**   | —             | Acesse github.com e faça login           | <https://github.com/signup>                         |
| **GitHub CLI**     | 2.40+         | `gh --version`                           | <https://cli.github.com>                            |
| **VS Code**        | 1.93+         | Abra o VS Code, **Help → About**         | <https://code.visualstudio.com/download>            |
| **Docker Desktop** | 4.30+         | `docker --version` E abra o app Docker   | <https://www.docker.com/products/docker-desktop>    |
| **Java 21 JDK**    | 21            | `java -version`                          | <https://learn.microsoft.com/java/openjdk/download> |
| **Node.js**        | 20 LTS        | `node --version`                         | <https://nodejs.org/en/download>                    |

> **Para o setup e a Arqueologia (Estágio 1) você só precisa de Git, conta GitHub, VS Code e Copilot.** Docker, Java 21 e Node.js só entram no Estágio 3 — quando o time **gera** o backend e o frontend com o Spec-Kit. Não há aplicação pronta neste repositório; instale essas ferramentas com calma até lá.

### Verificação de licença (uma pessoa verifica pelo time)

1. Abra <https://github.com/settings/copilot> no navegador.
2. Você deve ver **"Active subscription"** (Individual) ou **"Business plan"** no topo.
3. Se você vir "Get GitHub Copilot", levante a mão para o time de facilitadores do cordão azul.

---

## 👥 Passo 2 — Crie o repositório do time com "Use this template" (somente líder)

**Escolham uma pessoa para ser líder do time** (normalmente quem cobre a persona Technical Lead no Par 3). Somente a pessoa líder faz os Passos 2 a 5. As outras 4 pessoas aguardam e seguem a partir do Passo 6.

Este kit é um **template do GitHub**. Você não cria um repositório vazio nem copia arquivos à mão — o GitHub gera um repositório novo já com **todo o conteúdo** do kit (documentação, personas, código legado SIFAP, workflows de CI e a base do Spec-Kit).

### Passo a passo

1. Abra o repositório do kit no GitHub. A organização do workshop é <https://github.com/serpro-sao-paulo>.
2. Clique no botão verde **Use this template** (canto superior direito) → **Create a new repository**.
3. Preencha:

- **Owner**: selecione a organização do workshop **`serpro-sao-paulo`**.
- **Repository name**: `workshop-<cor>-<numero>` — use a cor do seu grupo e o número, por exemplo `workshop-azul-01`, `workshop-verde-03`.
- **Description**: `Workshop SIFAP 2.0 — Grupo <cor> <numero>`
- **Visibility**: **Public** ✅ (o repositório do time **deve ser público**)
- Deixe **Include all branches** desmarcado (só a `main` basta).

4. Clique em **Create repository from template**.

> [!IMPORTANT]
> **Nome e visibilidade são obrigatórios:** o repositório deve se chamar `workshop-<cor>-<numero>` e ser **público**, dentro da organização `serpro-sao-paulo`.

Pronto — o repositório do time já nasce com tudo dentro. Não há bootstrap, script de setup nem cópia manual.

---

## 📥 Passo 3 — O líder clona e cria a branch `develop` (somente líder)

Com o repositório criado, a pessoa líder clona e cria a branch de integração `develop`. **Não há script de bootstrap** — o conteúdo já veio do template.

```bash
# Escolha uma pasta para o seu código
mkdir -p ~/Code && cd ~/Code

# Clone o repositório do time (troque pela cor e número reais)
git clone https://github.com/serpro-sao-paulo/workshop-azul-01.git
cd workshop-azul-01

# Crie a branch de integração develop
git checkout -b develop
git push -u origin develop
```

`develop` é onde as branches de funcionalidade de todo mundo serão integradas. Promoções para `main` acontecem via PR depois de cada estágio.

> [!NOTE]
> O código legado SIFAP já está em `01-arqueologia/legado-sifap/` e o Spec-Kit já vem configurado. Você **não** precisa rodar nenhum script de validação ou bootstrap — comece a trabalhar direto pela Arqueologia.

> ⚠️ **Importante.** De agora em diante, ninguém faz push diretamente para `main`. O Passo 4 protege essa branch.

---

## 🛡️ Passo 4 — Proteja a branch `main` (somente líder)

Isso impede que qualquer pessoa (exceto o admin do repositório) faça push direto para `main`. Toda mudança deve passar por um Pull Request.

> ✅ **Repositório público:** como o repositório do time é **público**, a proteção de branch está disponível sem custo. Se por algum motivo a regra não puder ser criada, cumpram a regra de não fazer push direto para `main` por convenção.

### Usando o site

1. Vá para **Settings** → **Branches** (barra lateral esquerda).
2. Em **Branch protection rules**, clique em **Add rule**.
3. Padrão de nome da branch: `main`
4. Marque:
   - **Require a pull request before merging** ✅
   - **Require approvals** — defina como `1`
   - **Require conversation resolution before merging** ✅
5. Clique em **Create**.

### Usando a CLI

```bash
gh api -X PUT "repos/serpro-sao-paulo/workshop-azul-01/branches/main/protection" \
  --input - <<'JSON'
{
  "required_status_checks": null,
  "enforce_admins": true,
  "required_pull_request_reviews": {
    "required_approving_review_count": 1,
    "dismiss_stale_reviews": false,
    "require_code_owner_reviews": false
  },
  "restrictions": null,
  "required_conversation_resolution": true
}
JSON
```

> **Por que isso importa.** Sem essa regra, alguém do time eventualmente vai enviar um typo para `main` no minuto 90 e o demo vai falhar no minuto 480. Custo: 30 segundos. Economia: horas.

---

## 🎟️ Passo 5 — Adicione os outros 4 membros do time (somente líder)

A pessoa líder convida o restante do time para que todo mundo possa fazer push e pull.

### Opção A — usando o site

1. Vá para o repositório no GitHub: `https://github.com/serpro-sao-paulo/workshop-<cor>-<numero>`
2. Clique em **Settings** (aba superior — exige permissão de admin, que a pessoa líder tem).
3. Na barra lateral esquerda, clique em **Collaborators**.
4. Clique em **Add people**.
5. Digite o usuário GitHub (por exemplo, `alice-builder`) e escolha na lista.
6. **Escolha o papel**: escolha **Write** (não Admin, não Read).
7. Clique em **Adicionar ... a este repositório**.
8. Repita para as outras 3 pessoas.

> **Dica.** Cada pessoa convidada recebe um email e uma notificação dentro do GitHub. Ela precisa clicar em **Accept invitation** antes de conseguir fazer push.

### Opção B — usando a CLI

Uma vez por colega:

```bash
# Substitua alice pelo usuário GitHub real
gh api -X PUT "repos/serpro-sao-paulo/workshop-azul-01/collaborators/alice" \
  -f permission=write
```

Ou em loop:

```bash
for user in alice bob carla dani; do
  gh api -X PUT "repos/serpro-sao-paulo/workshop-azul-01/collaborators/${user}" \
    -f permission=write
done
```

---

## 💻 Passo 6 — Cada membro clona o repositório do time

**Agora todo mundo entra.** Os outros 4 membros do time fazem isto.

### 6.1 Aceite o convite

1. Abra o email do GitHub com o título **"[username] invited you to ..."** (ou confira o ícone de sino 🔔 em github.com). Se você já é membro da organização `serpro-sao-paulo`, pode pular direto para o clone.
2. Clique em **View invitation** → **Accept invitation**.

### 6.2 Clone e mude para `develop`

```bash
mkdir -p ~/Code && cd ~/Code

# Troque pela cor e número reais do seu grupo
git clone https://github.com/serpro-sao-paulo/workshop-azul-01.git
cd workshop-azul-01

# Mude para a branch develop (onde o trabalho diário acontece)
git checkout develop
```

### 6.3 Abra no VS Code

```bash
code .
```

Pronto. Não há script de bootstrap nem dev container para reconstruir — o repositório já vem completo do template. Abra o Copilot Chat (Passo 7) e comece.

---

## 🤖 Passo 7 — Ative o GitHub Copilot no VS Code (todos)

Cada pessoa faz isto no próprio laptop.

### 7.1 Faça login

1. No VS Code, olhe para a barra de status inferior. Clique no ícone do **Copilot** (🤖).
2. Escolha **Sign in with GitHub**.
3. Uma janela do navegador abre. Clique em **Authorize Visual Studio Code**.
4. Volte para o VS Code. Aguarde "Copilot ready" perto do canto inferior direito.

### 7.2 Abra o painel do Copilot Chat

| SO              | Atalho       |
| --------------- | ------------ |
| Mac             | <kbd>Cmd</kbd>+<kbd>Ctrl</kbd>+<kbd>I</kbd> |
| Windows / Linux | <kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>I</kbd> |

Um painel abre à direita.

### 7.3 Verifique se os 3 modos estão disponíveis

No topo do painel de chat há um dropdown:

| Modo      | Quando usar                                                      |
| --------- | ---------------------------------------------------------------- |
| **Ask**   | Fazer perguntas, explorar código, discutir opções                |
| **Plan**  | Planejar mudanças multi-arquivo antes da execução                |
| **Agent** | Delegar uma funcionalidade inteira via Issue e depois revisar o PR |

Clique no dropdown para confirmar que os três aparecem. Se **Plan** ou **Agent** não aparecerem, atualize o VS Code para uma versão recente ou use VS Code Insiders, que sempre tem o mais novo primeiro.

### 7.4 Teste de fumaça do Copilot

No painel de Chat, digite:

```
Qual stack estamos usando neste projeto?
```

Ele deve responder com **Java 21 + Spring Boot 3.3 + Next.js 15 + PostgreSQL 16**. Se não responder, o arquivo `.github/copilot-instructions.md` do projeto não está sendo carregado — veja [Solução de problemas](#-solução-de-problemas).

---

## 🎭 Passo 8 — Leia o `PERSONA.md` da sua persona (todos)

Cada uma das 10 personas do workshop já tem um **kit Copilot correspondente** — agentes, prompts e skills — **que já vem dentro do `.github/` do repositório do time** (veio do template). **Você não copia nada.** Os slash commands e agentes já estão disponíveis no Copilot Chat assim que você abre o repositório no VS Code.

### 8.1 Encontre seu papel e leia sua carta

Abra `05-personas/` no VS Code. Você verá 10 pastas, numeradas de 01 a 10. Dentro da pasta do seu papel, leia `PERSONA.md` de ponta a ponta (~10 minutos). Ele diz:

- O que você faz nos 4 estágios
- Qual modo do Copilot usar
- Prompts específicos que você pode copiar/colar
- De quem você depende e quem depende de você

### 8.2 Mapeamento persona → carta

As 10 personas cobertas pelas 5 pessoas do time estão nestas 10 cartas:

| Persona | Carta |
| --- | --- |
| Product Owner | `05-personas/01-product-owner/PERSONA.md` |
| Requirements Engineer | `05-personas/02-requirements-engineer/PERSONA.md` |
| Enterprise Architect | `05-personas/03-enterprise-architect/PERSONA.md` |
| Software Architect | `05-personas/04-software-architect/PERSONA.md` |
| Technical Lead | `05-personas/05-technical-lead/PERSONA.md` |
| Developer | `05-personas/06-developer/PERSONA.md` |
| DBA | `05-personas/07-dba/PERSONA.md` |
| QA Engineer | `05-personas/08-qa-engineer/PERSONA.md` |
| DevOps Engineer | `05-personas/09-devops-engineer/PERSONA.md` |
| Tech Writer | `05-personas/10-tech-writer/PERSONA.md` |

### 8.3 Verifique os agentes e slash commands no Copilot

Abra o Copilot Chat e digite `/` — você deve ver os slash commands das personas (por exemplo `/ears-convert`). No seletor de agentes do topo, você deve ver os 4 agentes de estágio (`@archaeologist`, `@architect`, `@builder`, `@evolution`). Tudo isso já vem no repositório; **não há cópia manual nem script de instalação**.

> [!NOTE]
> Se um slash command não aparecer, recarregue a janela do VS Code: Command Palette → **Developer: Reload Window**.

---

## 📐 Passo 9 — Confirme o Spec-Kit (já vem instalado)

[**Spec-Kit**](https://github.com/github/spec-kit) é o toolkit oficial do GitHub para desenvolvimento orientado por especificação. **Ele já vem configurado no repositório do time** (veio do template) — a configuração `.specify/` e os slash commands `/speckit.*` já estão prontos. **Você não instala nem inicializa nada.**

### 9.1 Confirme os comandos no Copilot

Abra o Copilot Chat, digite `/` e confirme que os comandos principais aparecem:

| Comando | Quando usar |
| --- | --- |
| `/speckit.constitution` | Define princípios, padrões e gates do projeto |
| `/speckit.specify` | Cria a spec da funcionalidade |
| `/speckit.clarify` | Resolve ambiguidades antes do plano |
| `/speckit.plan` | Cria o plano técnico |
| `/speckit.tasks` | Gera tasks implementáveis |
| `/speckit.analyze` | Checa consistência e cobertura |
| `/speckit.implement` | Implementa a funcionalidade guiada pela spec |

### 9.2 Escreva uma funcionalidade

No Copilot Chat:

```text
/speckit.specify Permitir que operadores gerem um ciclo mensal de pagamento para beneficiários ativos. Preserve a rastreabilidade legada com source_legacy em cada requisito.
```

O Spec-Kit cria uma branch numerada e a estrutura:

```text
specs/001-payment-cycle-generation/
├── spec.md
└── (outros artefatos criados nos próximos comandos)
```

Em seguida, rode:

```text
/speckit.clarify
/speckit.plan Use Java 21, Spring Boot 3.3, PostgreSQL 16, Next.js 15 e a arquitetura de monólito modular do workshop.
/speckit.tasks
```

### 9.3 Regra do workshop

Todo requisito que vier do legado continua precisando de `source_legacy:` apontando para `.NSN` ou `.ddm`. Requisitos sem paralelo no legado usam `[GREENFIELD]` com justificativa.

---

## 🎯 Passo 10 — Use o fluxo Spec-Kit (todos)

Para o workshop, use esta sequência única do Spec-Kit:

| Fase | Comando | Saída principal | Persona dona |
| --- | --- | --- | --- |
| Constituição | `/speckit.constitution` | `.specify/memory/constitution.md` | Technical Lead + Architect |
| Spec | `/speckit.specify` | `specs/<feature>/spec.md` | Requirements Engineer |
| Clarificação | `/speckit.clarify` | Perguntas resolvidas na spec | Requirements Engineer + Product Owner |
| Plano | `/speckit.plan` | `specs/<feature>/plan.md` | Software Architect |
| Tasks | `/speckit.tasks` | `specs/<feature>/tasks.md` | Technical Lead |
| Análise | `/speckit.analyze` | Lacunas e inconsistências | QA Engineer + Architect |
| Implementação | `/speckit.implement` | Código + testes guiados pela spec | Developer + QA Engineer |

> **Gates LGTM.** O time revisa explicitamente `spec.md`, `plan.md` e `tasks.md` antes de implementar.

---

## 🌿 Passo 11 — Entenda a estratégia de branches

Seu time tem **5 categorias de branches**. Use o tipo certo para o trabalho certo.

```
main                    ← pronto para release, protegido, exige 1 revisão
develop                 ← integração de todas as funcionalidades
spec/NNN-feature        ← trabalho de especificação (Estágio 2)
impl/NNN-feature        ← trabalho de implementação (Estágio 3)
infra/NNN-azure         ← trabalho de infraestrutura (Estágio 4)
```

### Convenção de nomes

| Tipo           | Padrão                 | Exemplo                             |
| -------------- | ---------------------- | ----------------------------------- |
| Spec           | `spec/NNN-kebab-name`  | `spec/001-payment-cycle-generation` |
| Implementação  | `impl/NNN-kebab-name`  | `impl/001-payment-cycle-generation` |
| Infrastructure | `infra/NNN-kebab-name` | `infra/001-azure-deployment`        |

`NNN` é o número da funcionalidade (corresponde à pasta em `specs/NNN-...`).

### Criando uma branch de funcionalidade

```bash
# Garanta que você está em develop com as mudanças mais recentes
git checkout develop
git pull

# Crie sua branch de funcionalidade
git checkout -b spec/001-payment-cycle-generation

# Trabalhe e faça commit
git add -A
git commit -m "feat(payments): draft EARS requirements for cycle generation"

# Envie para origin
git push -u origin spec/001-payment-cycle-generation
```

### Abrindo um Pull Request

1. Depois do push, o GitHub imprime uma URL como `https://github.com/.../pull/new/spec/001-...`. Clique nela (ou cole no navegador).
2. Título: use Conventional Commits — `feat(payments): add cycle generation spec`
3. Descrição: o GitHub carrega automaticamente o template (`.github/PULL_REQUEST_TEMPLATE.md`). Preencha:

- **O que mudou** (um parágrafo)
- **REQ-IDs implementados** (por exemplo, `REQ-PAY-014, REQ-PAY-015`)
- **Como testar** (a pessoa revisora faz pull e roda isto)
- **Issues vinculadas** (por exemplo, `Closes #12`)

4. **Pessoas revisoras**: adicione pelo menos uma pessoa de outra persona.
5. Clique em **Create pull request**.
6. O CI roda (workflow `.github/workflows/ci.yml`). Aguarde a verificação verde.
7. Depois da aprovação, clique em **Rebase and merge** (não Merge commit, não Squash).
8. Delete a branch de funcionalidade quando solicitado.

---

## 🔄 Passo 12 — Fluxo diário por persona

Cada persona tem um **ciclo diário padrão**. Rode-o quantas vezes forem necessárias durante o dia.

### 🧠 Product Owner / Requirements Engineer

```
1. Leia os achados do Estágio 1 (glossário, catálogo de regras de negócio)
2. Rode /speckit.specify "feature-name" com orientação de source_legacy
3. Rode /speckit.clarify e valide com personas stakeholder (PO + EA)
4. Rode /speckit.plan com a stack do workshop e as escolhas arquiteturais
5. Rode /speckit.tasks depois que o plano for aprovado
6. Abra um PR na branch spec/NNN-feature-name
7. Faça passagem para Software Architect (gate LGTM)
```

### 🏗️ Enterprise Architect / Software Architect

```
1. Faça pull do develop mais recente
2. git checkout spec/NNN-feature-name (leia a spec EARS)
3. Rode /speckit.plan → produz plan.md, research.md e contracts
4. Adicione ADRs em docs/adr/ para decisões não triviais
5. Abra um PR — revise a seção de design do PR da spec
6. Faça passagem para Tech Lead (gate LGTM)
```

### 🧠 Technical Lead

```
1. Leia o `plan.md` aprovado e os ADRs
2. Rode /speckit.tasks → produz tasks.md com IDs de tarefa (T001, T002, ...)
3. Abra uma GitHub Issue por tarefa usando .github/ISSUE_TEMPLATE/task.yml
4. Atribua cada issue a Developer / DBA / QA
5. Acompanhe CI verde/vermelho e desbloqueie pessoas
```

### 💻 Developer

```
1. Escolha uma issue de tarefa (T-NNN) no board do time
2. git checkout -b impl/NNN-task-name (a partir de develop, pelo VS Code ou site)
3. No Copilot, rode /implement (slash command já disponível no repositório)
4. Testes primeiro (vermelho), código (verde), refatoração
5. Rode os testes/build do módulo localmente (espelha o CI)
6. git commit, git push, abra PR
7. Marque a issue com "Closes #NN" no corpo do PR
```

### 🗃️ DBA

```
1. Escolha uma tarefa de schema/migração
2. git checkout -b impl/NNN-migration-name
3. Adicione a migração Flyway em backend/src/main/resources/db/migration/
4. Rode o prompt /migration (slash command já disponível no repositório)
5. Teste localmente com docker compose up
6. Abra PR e peça revisão de Developer
```

### 🧪 QA Engineer

```
1. Acompanhe todo PR de implementação
2. Rode o prompt /coverage-gaps para encontrar REQ-IDs sem cobertura
3. Adicione testes na branch de implementação (em par com Developer)
4. O prompt /test-strategy produz um plano de testes para novas funcionalidades
5. Bloqueie o merge se a cobertura cair abaixo de 70%
```

### ⚙️ DevOps Engineer

```
1. Escolha uma tarefa de infraestrutura (configuração Azure, CI/CD, deployment)
2. git checkout -b infra/NNN-azure-foo
3. Edite módulos Terraform em infra/
4. Rode `terraform fmt` + `terraform validate` localmente
5. Rode o prompt /iac-module (slash command já disponível no repositório)
6. Abra PR; workflows/ci.yml executa validação Terraform
```

### 📝 Tech Writer

```
1. Depois de cada merge em develop, procure drift em ADR/glossário
2. Rode o prompt /doc-drift (slash command já disponível no repositório)
3. Atualize docs/glossary.md, docs/adr/, READMEs
4. Abra um PR pequeno por atualização de documentação
```

---

## 🚦 Passo 13 — Rode o teste de fumaça (time inteiro, às 10:30)

A pessoa líder do time lê cada item em voz alta. Cada pessoa confirma no próprio laptop.

- [ ] Cada membro clonou `workshop-<cor>-<numero>`
- [ ] Cada membro consegue rodar `git checkout develop && git pull origin develop` (acesso de escrita confirmado)
- [ ] Cada Copilot Chat responde "Qual stack estamos usando neste projeto?" com a resposta certa
- [ ] Os comandos `/speckit.*` aparecem no Copilot Chat ao digitar `/` (já vêm do template)
- [ ] Os 4 agentes de estágio (`@archaeologist`…`@evolution`) aparecem no seletor de agentes
- [ ] Abrir **New issue** no GitHub mostra os templates (spec, adr, task)
- [ ] Todos os 5 membros do time aparecem em repo Settings → Collaborators
- [ ] Cada persona leu sua carta em `05-personas/XX-role/PERSONA.md`
- [ ] O time confirmou quais 2 personas cada pessoa cobre (e quem começa pela Arqueologia)
- [ ] [`00-TEAM-FLOW.md`](00-TEAM-FLOW.md) foi lido em voz alta uma vez (a linha do tempo do dia)

Quando todos os itens estiverem verdes, seu time está pronto para o **Estágio 1: Arqueologia**.

---

## Solução de problemas

<details>
<summary><strong>Erros comuns e como resolver</strong> — clique para expandir</summary>

### Copilot não lê `copilot-instructions.md`

- O VS Code precisa estar aberto **na raiz do repositório**, não dentro de uma subpasta.
- Reinicie o VS Code depois de editar o arquivo.
- Em Settings, verifique se `github.copilot.chat.useProjectInstructions` está como `true` (padrão na 1.93+).

### "Use this template" não aparece ou o nome do repositório é recusado

- O botão **Use this template** só aparece se você abrir o repositório do **kit/template** no GitHub (não o repositório do seu time).
- Selecione o owner **`serpro-sao-paulo`** e use o nome `workshop-<cor>-<numero>` (por exemplo `workshop-azul-01`). Se o nome já existir, ajuste o número.
- Garanta que a visibilidade está como **Public**.

### Os comandos `/speckit.*` não aparecem no Copilot

- O Spec-Kit já vem no repositório (`.specify/`). Você **não** precisa instalar nem rodar `specify init`.
- Abra o VS Code **na raiz do repositório do time**, não numa subpasta.
- Recarregue o VS Code: Command Palette → **Developer: Reload Window**.

### CI falha no primeiro push com "no tests found"

- Esperado. O fluxo de trabalho `ci.yml` só roda jobs cujos caminhos mudaram. Quando código backend/frontend entrar, os jobs relevantes vão rodar.

### Docker compose up trava ou falha

- As portas 5432, 8080 ou 3000 podem estar em uso. Rode:

  ```bash
  lsof -i :5432 -i :8080 -i :3000
  ```

  Mate o processo que está ocupando a porta (`kill -9 <PID>`) e tente de novo.

- Garanta que o Docker Desktop está **rodando** (o ícone na barra de menu deve estar estável, não animado).

### Copilot Agent mode não aparece no dropdown

- Atualize o VS Code para **1.93 ou posterior** (ou instale **VS Code Insiders**, que sempre tem esse recurso).
- Recarregue a janela: Command Palette → **Developer: Reload Window**.

### "Permission denied" ao fazer push para `main`

- A proteção de branch (Passo 4) está fazendo seu trabalho. Abra um Pull Request a partir da sua branch de funcionalidade.

### Fiz pull do `develop` mais recente, mas minha IDE ainda mostra código antigo

- Recarregue a janela do VS Code: Command Palette → **Developer: Reload Window**.

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="00-TEAM-FLOW.md"><strong>TEAM-FLOW</strong></a><br/>
<sub>Cronograma de 8h, passagens entre pares, regra dos 20 min, DoD.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="05-personas/OVERVIEW.md"><strong>OVERVIEW das 10 personas</strong></a><br/>
<sub>Tabela comparativa: par, líder de estágio, defaults de emergência.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

— Paula

</details>
