# Inventário Legado — <!-- placeholder: Nome da Equipe -->

> **Data:** 2026-06-10
> **Primeira passada (orientação top-down).** Esta é uma visão baseada **apenas em nomes de arquivos e estrutura de pastas** — nenhum arquivo de programa foi aberto. Será revisada conforme a equipe ler arquivos individuais e rastrear dependências (`/extract-business-rules`, `/map-dependencies`).

## Estrutura de Pastas

```
01-arqueologia/legado-sifap/
├── README.md
├── natural-programs/        (15 .NSN + README.md)
│   └── README.md
├── adabas-ddms/             (4 .ddm + README.md)
│   └── README.md
├── legacy-docs/             (3 docs em .md + .docx + README.md)
│   └── README.md
└── demo/                    (demo terminal interativa)
    └── sifap-terminal.html
```

**Total de diretórios sob `legado-sifap/`:** 5 (a raiz `legado-sifap/` + 4 subpastas: `natural-programs/`, `adabas-ddms/`, `legacy-docs/`, `demo/`).

## Contagem de Arquivos por Tipo

| Extensão | Contagem | Finalidade provável |
| -------- | -------- | ------------------- |
| `.NSN`   | 15       | Programa-fonte Natural |
| `.ddm`   | 4        | Data Definition Module (Adabas) |
| `.md`    | 7        | Documentação (4 READMEs + 3 docs históricos convertidos) |
| `.docx`  | 3        | Documentos históricos originais (Word) |
| `.html`  | 1        | Demo terminal interativa |

> `.cpy` (copycode) e `.map` (mapas 3270) são esperados em ambientes Natural/Adabas, mas **não aparecem** nesta pasta. Investigar se a lógica de tela/include está embutida nos `.NSN` ou ausente do dump legado.

## Padrões de Convenção de Nomes

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

1. **`adabas-ddms/AUDITORIA.ddm`** — Existem **4** DDMs na pasta, mas vários documentos históricos referem-se apenas a **3** DDMs. `AUDITORIA` parece ser um acréscimo posterior. _Ação:_ ao chegar nos DDMs, confirmar se `AUDITORIA` é referenciado pelos programas e por que está ausente da documentação base.
2. **`natural-programs/CONSBENF.NSN`** — Único programa com prefixo `CONS` (todos os outros prefixos têm 2+ ocorrências). _Ação:_ verificar se é o único ponto de consulta online ou se há consultas embutidas em outros programas.
3. **`demo/sifap-terminal.html`** — Único arquivo `.html` e único arquivo na pasta `demo/`; extensão e localização sem par no restante do legado. _Ação:_ tratar como artefato de apoio/demonstração, não como fonte de regra de negócio; não usar como evidência `source_legacy:`.

## Ordem de Leitura Proposta

> **Hipótese de leitura — sujeita a mudança** assim que a equipe começar a rastrear dependências (CALLNAT / READ / STORE / UPDATE).

1. **DDMs primeiro** (dados antes do código): `BENEFICIARIO.ddm` → `PROGRAMA-SOCIAL.ddm` → `PAGAMENTO.ddm` → `AUDITORIA.ddm`.
2. **Entry points batch** (orquestram o fluxo): `BATCHPGT.NSN` → `BATCHREL.NSN` → `BATCHCON.NSN`.
3. **Programas mais conectados** (prováveis CALLNAT a partir de batch e cadastro): família `CALC*` (`CALCBENF`, `CALCCORR`, `CALCDSCT`) e família `VAL*` (`VALBENEF`, `VALDOCS`, `VALELEG`).
4. **Cadastro/CRUD online**: `CADBENEF.NSN` → `CADDEPEND.NSN` → `CADPROG.NSN`.
5. **Consulta e relatórios**: `CONSBENF.NSN`, `RELPGT.NSN`, `RELAUDIT.NSN`.

**Justificativa:** DDMs primeiro porque definem o modelo de dados que todo programa toca; depois os `BATCH*` por serem entry points que provavelmente chamam (`CALLNAT`) as famílias `CALC*`/`VAL*`, que aparecem como sufixos de domínio reutilizados (ex.: `CALCBENF`/`VALBENEF`/`CADBENEF`/`CONSBENF` compartilham o radical `BENEF`), sugerindo forte acoplamento em torno da entidade Beneficiário.
