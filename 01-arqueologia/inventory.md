# Inventário Legado — <!-- placeholder: Nome da Equipe -->

> **Data:** 2026-06-10
> **Primeira passada (orientação top-down).** Esta é uma visão baseada **apenas em nomes de arquivos e estrutura de pastas** — nenhum arquivo de programa foi aberto. Será revisada conforme a equipe ler arquivos individuais e rastrear dependências (`/extract-business-rules`, `/map-dependencies`).

## Estrutura de Pastas

> **Primeira passada** — baseada **somente** em estrutura de pastas e nomes de arquivos (nenhum programa foi aberto). Data do escaneamento: 2026-06-10. Será revisada conforme a equipe ler arquivos individuais.

```text
01-arqueologia/legado-sifap/
├── README.md
├── COMO-LER-NATURAL.md
├── natural-programs/        ← Programas-fonte Natural (.NSN)
│   ├── README.md
│   └── *.NSN (15 programas)
├── adabas-ddms/             ← Data Definition Modules (.ddm)
│   ├── README.md
│   └── *.ddm (4 DDMs)
├── legacy-docs/             ← Documentação parcial/desatualizada
│   ├── README.md
│   ├── ARQUITETURA-ORIGINAL-1997.md
│   ├── MANUAL-TECNICO-SIFAP-2008.md
│   └── REGRAS-NEGOCIO-2012.md
└── demo/                    ← Demo de terminal interativa
    └── sifap-terminal.html
```

**Total de diretórios sob `legado-sifap/`:** 5 (a raiz + 4 subdiretórios: `natural-programs/`, `adabas-ddms/`, `legacy-docs/`, `demo/`).

```
01-arqueologia/legado-sifap/
├── COMO-LER-NATURAL.md
├── README.md
├── natural-programs/        (15 .NSN + README.md)
│   └── README.md
├── adabas-ddms/             (4 .ddm + README.md)
│   └── README.md
└── legacy-docs/             (3 docs em .md + 3 .docx + README.md)
    └── README.md
```

**Total de diretórios sob `legado-sifap/`:** 4 (a raiz `legado-sifap/` + 3 subpastas: `natural-programs/`, `adabas-ddms/`, `legacy-docs/`). _Nota:_ a demo interativa (`sifap-terminal.html`) fica fora desta pasta, em `01-arqueologia/demo/`.
X2CR
## Contagem de Arquivos por Tipo

> Contagens derivadas dos READMEs de cada pasta. Confirme com `find 01-arqueologia/legado-sifap/ -type f` antes de tratar como definitivo.

| Extensão | Contagem | Finalidade provável                                               |
| -------- | -------- | ----------------------------------------------------------------- |
| `.NSN`   | 15       | Programa-fonte Natural (lógica de negócio)                        |
| `.ddm`   | 4        | Data Definition Module Adabas (definição de dados)                |
| `.md`    | 7        | Documentação (READMEs + 3 docs históricos + guia de leitura)      |
| `.html`  | 1        | Demo interativa de terminal 3270 (apoio do Estágio 1)             |

> **Observação:** apesar de a documentação citar copycodes (`.cpy`) e maps (`.map`) na biblioteca Natural original, nenhum arquivo dessas extensões aparece nesta pasta.

| Extensão | Contagem | Finalidade provável |
| -------- | -------- | ------------------- |
| `.NSN`   | 15       | Programa-fonte Natural |
| `.ddm`   | 4        | Data Definition Module (Adabas) |
| `.md`    | 8        | Documentação (4 READMEs + `COMO-LER-NATURAL.md` + 3 docs históricos convertidos) |
| `.docx`  | 3        | Documentos históricos originais (Word) |

> `.cpy` (copycode) e `.map` (mapas 3270) são esperados em ambientes Natural/Adabas, mas **não aparecem** nesta pasta. Investigar se a lógica de tela/include está embutida nos `.NSN` ou ausente do dump legado.

## Padrões de Convenção de Nomes

Agrupamento por prefixo dos nomes dos 15 arquivos `.NSN` (sem abrir os arquivos):

| Prefixo  | Contagem | Hipótese                                                                                |
| -------- | -------- | --------------------------------------------------------------------------------------- |
| `BATCH-` | 3        | Programas batch / entry points agendados (`BATCHCON`, `BATCHPGT`, `BATCHREL`)           |
| `CAD-`   | 3        | Programas online de cadastro / CRUD (`CADBENEF`, `CADDEPEND`, `CADPROG`)                |
| `CALC-`  | 3        | Cálculo, possíveis subprogramas CALLNAT (`CALCBENF`, `CALCCORR`, `CALCDSCT`)            |
| `VAL-`   | 3        | Validação, possíveis subprogramas CALLNAT (`VALBENEF`, `VALDOCS`, `VALELEG`)            |
| `REL-`   | 2        | Geração de relatórios (`RELAUDIT`, `RELPGT`)                                            |
| `CONS-`  | 1        | Consulta online — prefixo ocorre uma só vez (`CONSBENF`); Desconhecido — investigar     |

DDMs (`.ddm`) não seguem prefixo: nomeados pela entidade (`BENEFICIARIO`, `PAGAMENTO`, `PROGRAMA-SOCIAL`, `AUDITORIA`).

Agrupamento por prefixo dos 15 arquivos `.NSN` (sem abrir os arquivos):

| Prefixo | Contagem | Hipótese |
| ------- | -------- | -------- |
| `BATCH` | 3 | Programas de processamento batch (entry points de job) — `BATCHCON`, `BATCHPGT`, `BATCHREL` |
| `CAD`   | 3 | Programas de cadastro/CRUD — `CADBENEF`, `CADDEPEND`, `CADPROG` |
| `CALC`  | 3 | Sub-rotinas de cálculo (provável CALLNAT) — `CALCBENF`, `CALCCORR`, `CALCDSCT` |
| `VAL`   | 3 | Rotinas de validação (provável CALLNAT) — `VALBENEF`, `VALDOCS`, `VALELEG` |
| `REL`   | 2 | Geração de relatórios — `RELAUDIT`, `RELPGT` |
| `CONS`  | 1 | Consulta — `CONSBENF` (prefixo ocorre uma única vez; ver Itens Incomuns) |

DDMs (`.ddm`) seguem nomenclatura por entidade de domínio, sem prefixo: `BENEFICIARIO`, `PROGRAMA-SOCIAL`, `PAGAMENTO`, `AUDITORIA`.

## Itens Incomuns (Top 3)

| #   | Caminho do Arquivo                                          | O Que o Torna Incomum                                                          | Investigação Sugerida                                                            |
| --- | ---------------------------------------------------------- | ----------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| 1   | `01-arqueologia/legado-sifap/demo/sifap-terminal.html`     | Única extensão `.html` e único arquivo da subpasta `demo/`                     | Confirmar se é só material de apoio (demo 3270), não fonte de regra de negócio |
| 2   | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN`| Único programa com prefixo `CONS-` (padrão que ocorre uma só vez)              | Verificar se há consultas embutidas em programas `CAD-`                          |
| 3   | `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`    | DDM de 2005, ausente das docs de 1997 e 2008 (FNR 153) — divergência código/doc | Mapear quais programas escrevem nele (provável `RELAUDIT`/rotina de log)        |

1. **`adabas-ddms/AUDITORIA.ddm`** — Existem **4** DDMs na pasta, mas vários documentos históricos referem-se apenas a **3** DDMs. `AUDITORIA` parece ser um acréscimo posterior. _Ação:_ ao chegar nos DDMs, confirmar se `AUDITORIA` é referenciado pelos programas e por que está ausente da documentação base.
2. **`natural-programs/CONSBENF.NSN`** — Único programa com prefixo `CONS` (todos os outros prefixos têm 2+ ocorrências). _Ação:_ verificar se é o único ponto de consulta online ou se há consultas embutidas em outros programas.
3. **`legacy-docs/*.docx`** — Os 3 documentos históricos existem em par `.docx` (binário) + `.md` (texto); apenas os `.md` são pesquisáveis/diffáveis. _Ação:_ usar os `.md` como fonte primária e confirmar se os `.docx` contêm algo ausente da conversão; não citar `.docx` como evidência textual `source_legacy:`.

## Ordem de Leitura Proposta

> **Hipótese inicial** — a ordem real mudará quando a equipe rastrear dependências (CALLNAT / READ / STORE).

1. **DDMs primeiro (dados antes do código):** `BENEFICIARIO.ddm` → `PROGRAMA-SOCIAL.ddm` → `PAGAMENTO.ddm` → `AUDITORIA.ddm`.
2. **Entry points batch:** `BATCHPGT.NSN` (provável orquestrador do ciclo de pagamento), depois `BATCHCON.NSN` e `BATCHREL.NSN`.
3. **Programas mais conectados (subprogramas candidatos):** rotinas `CALC-` e `VAL-`, cujos nomes sugerem CALLNAT a partir dos cadastros e do batch.
4. **Cadastros online:** `CADBENEF.NSN` → `CADDEPEND.NSN` → `CADPROG.NSN`.
5. **Consulta e relatórios:** `CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`.

_Justificativa:_ (a) DDMs primeiro para entender o modelo de dados; (b) `BATCH-` são os entry points; (c) `CALC-`/`VAL-` priorizados por serem prováveis subprogramas compartilhados.

> **Hipótese de leitura — sujeita a mudança** assim que a equipe começar a rastrear dependências (CALLNAT / READ / STORE / UPDATE).

1. **DDMs primeiro** (dados antes do código): `BENEFICIARIO.ddm` → `PROGRAMA-SOCIAL.ddm` → `PAGAMENTO.ddm` → `AUDITORIA.ddm`.
2. **Entry points batch** (orquestram o fluxo): `BATCHPGT.NSN` → `BATCHREL.NSN` → `BATCHCON.NSN`.
3. **Programas mais conectados** (prováveis CALLNAT a partir de batch e cadastro): família `CALC*` (`CALCBENF`, `CALCCORR`, `CALCDSCT`) e família `VAL*` (`VALBENEF`, `VALDOCS`, `VALELEG`).
4. **Cadastro/CRUD online**: `CADBENEF.NSN` → `CADDEPEND.NSN` → `CADPROG.NSN`.
5. **Consulta e relatórios**: `CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`.

**Justificativa:** DDMs primeiro porque definem o modelo de dados que todo programa toca; depois os `BATCH*` por serem entry points que provavelmente chamam (`CALLNAT`) as famílias `CALC*`/`VAL*`, que aparecem como sufixos de domínio reutilizados (ex.: `CALCBENF`/`VALBENEF`/`CADBENEF`/`CONSBENF` compartilham o radical `BENEF`), sugerindo forte acoplamento em torno da entidade Beneficiário.
