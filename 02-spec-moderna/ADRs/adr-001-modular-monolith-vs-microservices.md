<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-001: Adotar Monolito Modular em vez de Microsserviços

## Status

Accepted

## Data

2026-06-10

## Decisores

Par 2 (Enterprise Architect + Software Architect), com sign-off do Par 1 (Product Owner)

## Contexto

Estamos modernizando o **SIFAP** (29 anos em Natural/Adabas) para a stack-alvo Java 21 + Spring Boot 3.3 + PostgreSQL 16 + Next.js 15. No Estágio 2 recortamos **5 bounded contexts** (ver [`bounded-contexts.md`](../bounded-contexts.md)): Gestão de Beneficiários, Processamento de Pagamentos, Gestão de Programas Sociais, Conciliação Bancária e Trilha de Auditoria.

A decisão a tomar agora é **a topologia de deployment**: um único deployable com módulos internos, ou serviços independentes por contexto. Ela precisa ser tomada **antes** do `/speckit.plan` porque define a estrutura do projeto Maven, os contratos entre módulos e o modelo de comunicação (in-process vs. rede).

Restrições e forças relevantes às descobertas do Estágio 1:

- O grafo de dependências legado é **acíclico**, mas `BATCHPGT` é o nó mais conectado: lê `BENEFICIARIO` e `PROGRAMA-SOCIAL` e chama `VALELEG`/`CALCBENF`/`CALCDSCT`. O acoplamento de leitura entre Pagamentos, Beneficiários e Programas é alto (ver [`dependency-map.md`](../../01-arqueologia/dependency-map.md)).
- O núcleo financeiro concentra **9 regras críticas** e regras financeiras (FATOR-K — MYS-001) ainda têm mistérios abertos; consistência transacional forte é desejável durante a migração.
- O ciclo de pagamento é **batch mensal**, não um sistema de alta concorrência distribuída.
- A equipe **não tem experiência operacional** com a malha de microsserviços (service discovery, tracing distribuído, sagas).
- Constituição do projeto exige PostgreSQL 16 como sistema de registro e CI com `legacy-traceability` — favorece um schema único versionado.

## Opções Consideradas

### Opção 1: Monolito Modular (escolhida)

- **Descrição**: Um único deployable Spring Boot 3.3, projeto Maven multi-módulo — um módulo por bounded context (package-by-feature). Comunicação inter-context **in-process** via interfaces e domain events; fronteiras impostas por `ArchUnit`/Spring Modulith. Banco único PostgreSQL 16 com schema por módulo.
- **Prós**:
  - Consistência transacional forte no núcleo financeiro (folha mensal + descontos numa única transação), evitando sagas para BR-001/BR-003/BR-016.
  - Resolve o acoplamento de leitura de `BATCHPGT` com chamadas de método locais, sem latência de rede nem chamadas remotas frágeis.
  - Cabe na experiência da equipe e nas 3h do Estágio 3; um único pipeline de CI/CD e um deploy.
  - Fronteiras de módulo preservam a opção futura de extrair um serviço se um contexto justificar (Strangler Fig reverso).
- **Contras**:
  - Escalonamento é do processo inteiro, não de um contexto isolado.
  - Disciplina de fronteira depende de enforcement automatizado (ArchUnit), senão vira "big ball of mud".
- **Risk**: Módulos podem vazar fronteiras se a disciplina relaxar — mitigado por testes de arquitetura no CI.
- **Effort**: **Lower** — menor esforço de infraestrutura e operação.

### Opção 2: Microsserviços (um serviço por bounded context)

- **Descrição**: 5 serviços independentes (Beneficiários, Pagamentos, Programas, Conciliação, Auditoria), cada um com seu banco, comunicando via REST/eventos sobre a rede.
- **Prós**:
  - Escalonamento e deploy independentes por contexto.
  - Isolamento de falhas entre contextos.
- **Contras**:
  - O cálculo da folha (Pagamentos lê Beneficiários + Programas) viraria múltiplas chamadas de rede + **sagas** para manter consistência — alto risco para regras financeiras com mistérios ainda abertos.
  - Exige infraestrutura (service mesh, tracing distribuído, observabilidade) que a equipe não opera hoje.
  - Não cabe no prazo do workshop; multiplica pipelines e ambientes.
- **Risk**: Consistência eventual introduz divergência de centavos — choca com MYS-005 (truncar vs. arredondar) e BR-017 (tolerância ±0,01).
- **Effort**: **Higher** — muito maior esforço operacional.

### Opção 3: Monolito tradicional em camadas (package-by-layer)

- **Descrição**: Um deployable organizado por camadas técnicas (controllers, services, repositories), sem fronteiras de domínio explícitas.
- **Prós**:
  - Simples de começar; familiar.
- **Contras**:
  - Apaga as fronteiras dos 5 bounded contexts recortados no Estágio 2 — regras de Pagamentos e Beneficiários se misturam.
  - Sem caminho de extração futura; refatoração custosa se um contexto precisar virar serviço.
- **Risk**: Regride ao acoplamento do legado (CALCBENF de 4.800 linhas) num novo idioma.
- **Effort**: **Same** que a Opção 1, porém com pior estrutura de longo prazo.

## Decisão

**Adotamos o Monolito Modular (Opção 1)**: um único deployable Spring Boot 3.3 com um módulo Maven por bounded context, comunicação in-process e enforcement de fronteira via ArchUnit/Spring Modulith.

**Motivo (uma frase):** é a única opção que preserva as fronteiras dos 5 bounded contexts, dá consistência transacional forte ao núcleo financeiro e cabe na experiência da equipe e no prazo — sem o custo operacional de microsserviços.

## Consequências

### Positivas

- O cálculo mensal (Pagamentos → Beneficiários/Programas) roda numa única transação ACID, protegendo BR-001, BR-003 e BR-016.
- Um pipeline de CI/CD, um deploy, um schema PostgreSQL versionado — alinhado à constituição do projeto.
- Fronteiras explícitas mantêm a opção de extrair um serviço no futuro (ex.: Conciliação Bancária, que já tem integração externa) sem reescrever o domínio.

### Negativas

- Todo o processo escala junto — **mitigação**: o gargalo conhecido é o batch mensal; planejar escalonamento vertical e janelas de processamento.
- Risco de erosão das fronteiras de módulo — **mitigação**: testes de arquitetura (ArchUnit) obrigatórios no CI verificando que módulos só se comunicam via interfaces públicas/eventos.

### Riscos

- Se um contexto (ex.: Auditoria, ~25M registros, ou Pagamentos, ~180M) exigir escalonamento independente no futuro, será necessário extraí-lo — registrar um novo ADR que substitua este parcialmente. Plano de contingência: as fronteiras de módulo já isolam o domínio, tornando a extração incremental (Strangler Fig).

## Requisitos Relacionados

- Bounded contexts: [`bounded-contexts.md`](../bounded-contexts.md) (5 contextos)
- Regras de negócio: BR-001 (teto de descontos), BR-003 (fórmula do benefício), BR-016 (ciclo mensal), BR-017 (conciliação CNAB)
- REQ-IDs: a definir em [`SPECIFICATION.md`](../SPECIFICATION.md) (`/write-ears-spec`)
- ADRs relacionados: [ADR-002](adr-002-adabas-mu-pe-mapping.md) (mapeamento Adabas MU/PE → JPA), [ADR-003](adr-003-authentication-authorization.md) (autenticação)

---

**Definição de Pronto:** Formato MADR, 3 opções com prós/contras específicos do SIFAP, decisão datada (2026-06-10), consequências positivas e negativas, REQ-IDs/BRs relacionados.
