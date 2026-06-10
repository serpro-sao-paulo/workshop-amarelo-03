<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-002: Mapeamento de campos Adabas MU/PE de PROGRAMA-SOCIAL para JPA

## Status

Accepted

## Data

2026-06-10

## Decisores

Par 2 (Enterprise Architect + Software Architect) + DBA, com sign-off do Par 1 (Product Owner)

## Contexto

O DDM legado `PROGRAMA-SOCIAL` (FNR 151, ~45 registros) usa estruturas Adabas que **não existem** no modelo relacional: campos **MU** (multiple-value — listas dentro do registro) e grupos **PE** (periodic groups — faixas de valores por exercício). A documentação legada confirma: *"O DDM PROGRAMA-SOCIAL contém campos do tipo MU (multiple value) e PE (periodic group) para armazenar faixas de valores por exercício"* (`legado-sifap/README.md` §4.3).

Precisamos decidir **como persistir essas faixas no PostgreSQL 16** antes do `/speckit.plan`, porque a escolha define o schema do módulo `program`, o mapeamento JPA das entidades `ValueRange`/`KFactorParameters` (REQ-005, REQ-006) e a forma como `payment` consulta as faixas por exercício (REQ-007). A constituição do projeto exige consultas via JPA/JPQL (sem SQL concatenado) e PostgreSQL como sistema de registro.

Forças relevantes:

- Faixas de valor por exercício precisam ser **consultadas com filtro** (`getValueRange(programId, exercise)` — REQ-005) e auditadas.
- Volume é pequeno (~45 programas), mas as faixas são **dados financeiros** que dirigem o cálculo do benefício.
- A equipe usa Hibernate/JPA; queries relacionais com índice são desejáveis para a leitura síncrona feita por `payment`.

## Opções Consideradas

### Opção 1: `@OneToMany` com entidade filha `ProgramValueRange` (escolhida)

- **Descrição**: Modelar o grupo periódico (PE) como tabela filha `program_value_range` (programa, exercício, mínimo, máximo), referenciada por `@OneToMany` a partir de `SocialProgram`. Campos MU simples viram tabelas filhas via `@ElementCollection`.
- **Prós**:
  - Faixas viram linhas consultáveis por SQL/JPQL com `WHERE exercise = ?` — atende REQ-005 com índice composto `(program_id, exercise)`.
  - Integridade referencial e tipos numéricos nativos (`NUMERIC`) preservam a precisão financeira.
  - Auditável célula a célula (REQ-015) — cada faixa é uma linha versionável.
- **Contras**:
  - Mais tabelas e joins do que uma coluna única.
  - Migração precisa "desnormalizar" o PE do Adabas em linhas.
- **Risk**: Baixo — modelo relacional clássico, bem suportado por Hibernate.
- **Effort**: **Same** — esforço de mapeamento moderado, dentro do conhecido da equipe.

### Opção 2: Coluna JSONB com o array de faixas

- **Descrição**: Guardar todas as faixas por exercício como um documento JSONB único na linha de `social_program`.
- **Prós**:
  - Mapeamento 1:1 com a estrutura aninhada do PE; menos tabelas.
  - Flexível para faixas com formato variável.
- **Contras**:
  - Consulta por exercício exige operadores JSONB — atrita com a regra "JPA/JPQL, sem SQL específico" e dificulta índices simples.
  - Precisão financeira dentro de JSON é frágil (números viram `double`); auditoria de uma faixa isolada é mais difícil.
- **Risk**: Médio — erro de precisão em dado financeiro é inaceitável (choca com BR-001/BR-003).
- **Effort**: **Lower** no schema, **higher** na consulta/validação.

### Opção 3: Coluna serializada (string delimitada), espelhando o layout legado

- **Descrição**: Replicar o campo MU/PE como string compactada, como no Adabas (packed decimal serializado).
- **Prós**:
  - Migração trivial — copia o conteúdo legado quase como está.
- **Contras**:
  - Carrega a dívida técnica do legado para o sistema novo; ilegível, não consultável, não auditável.
  - Exige parsing em código para qualquer leitura — anti-padrão num greenfield relacional.
- **Risk**: Alto — recria o problema "regra mora só no código" que a modernização quer eliminar.
- **Effort**: **Lower** agora, **muito higher** de manutenção.

## Decisão

**Adotamos a Opção 1**: grupos periódicos (PE) de `PROGRAMA-SOCIAL` viram a entidade filha `ProgramValueRange` mapeada com `@OneToMany`; campos MU simples viram `@ElementCollection`.

**Motivo (uma frase):** é a única opção que torna as faixas por exercício consultáveis via JPQL com índice, preserva a precisão financeira em `NUMERIC` e mantém a auditabilidade exigida por REQ-005, REQ-007 e REQ-015.

## Consequências

### Positivas

- `getValueRange(programId, exercise)` (REQ-005) é um simples `findByProgramIdAndExercise`, com índice composto.
- Precisão financeira garantida por `BigDecimal`/`NUMERIC`; sem risco de `double`.
- Cada faixa é auditável individualmente (REQ-015).

### Negativas

- Schema com mais tabelas e joins — **mitigação**: volume é pequeno (~45 programas), impacto de performance desprezível.
- Migração precisa expandir o PE legado em linhas — **mitigação**: rotina de migração única, validada contra os ~45 registros conhecidos.

### Riscos

- Se surgirem faixas com estrutura muito irregular entre exercícios, o modelo tabular pode precisar de colunas opcionais — registrar em novo ADR se acontecer. Contingência: `@ElementCollection` adicional para atributos esparsos.

## Requisitos Relacionados

- REQ-005 (faixas por exercício), REQ-006 (FATOR-K — `KFactorParameters`), REQ-007 (cálculo usa a faixa)
- Bounded context: `program` em [`bounded-contexts.md`](../bounded-contexts.md) e [`modular-monolith-design.md`](../modular-monolith-design.md)
- ADRs relacionados: [ADR-001](adr-001-modular-monolith-vs-microservices.md) (monólito modular, banco único)
- Regras: BR-003 (cálculo por faixa), BR-008 (FATOR-K)
- Mistério associado: MYS-001 (valor da constante do FATOR-K) — não bloqueia o mapeamento, apenas o valor

---

**Definição de Pronto:** Formato MADR, 3 opções com prós/contras específicos do SIFAP, decisão datada (2026-06-10), consequências positivas e negativas, REQ-IDs/BRs relacionados.
