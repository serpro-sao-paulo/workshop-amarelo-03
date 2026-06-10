<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-003: Autenticação e autorização do SIFAP 2.0

## Status

Accepted

## Data

2026-06-10

## Decisores

Par 2 (Enterprise Architect + Software Architect) + DevOps, com sign-off do Par 1 (Product Owner)

## Contexto

O SIFAP legado autentica via terminal 3270 e controla acesso por um campo `COD-PERFIL` no DDM `AUDITORIA`, com cinco perfis: **ADM, OPR, CON, AUD, SUP** (discovery-report §2.3). O sistema moderno é uma API REST (Spring Boot 3.3) consumida por um frontend Next.js 15, e precisa de um modelo de autenticação/autorização **antes** do `/speckit.plan`, porque define a configuração do Spring Security, a proteção dos endpoints do [`openapi.yaml`](../openapi.yaml) e o tratamento de dados sensíveis (REQ-017).

Forças relevantes:

- Os 5 perfis legados precisam ser preservados (mapeiam para autorização por papel).
- Dados sensíveis (CPF, valores de benefício) exigem mascaramento em logs (REQ-017) e acesso restrito.
- A constituição do projeto exige **OAuth2/JWT (Spring Security)** no backend, Managed Identity para serviço-a-serviço e CORS explícito (sem wildcard em produção).
- Contexto Serpro/gov: o **gov.br** é o provedor de identidade federado padrão para serviços públicos brasileiros.

## Opções Consideradas

### Opção 1: OAuth2/OIDC federado no gov.br + RBAC por perfil (escolhida)

- **Descrição**: O frontend autentica usuários no **gov.br** (OIDC). O backend valida o JWT via Spring Security (resource server) e aplica autorização baseada em papéis mapeados dos 5 perfis legados (ADM/OPR/CON/AUD/SUP). Serviço-a-serviço usa Managed Identity no Azure.
- **Prós**:
  - Alinha com a constituição (OAuth2/JWT + Spring Security) e com o padrão de identidade do governo brasileiro.
  - Sem gestão de senhas no SIFAP — reduz superfície de ataque (OWASP A07).
  - Os 5 perfis legados mapeiam diretamente para `roles`/authorities; autorização por endpoint declarativa.
- **Contras**:
  - Dependência de disponibilidade do provedor gov.br.
  - Exige mapeamento de claims gov.br → perfis SIFAP.
- **Risk**: Baixo-Médio — risco operacional concentrado no IdP externo; mitigável.
- **Effort**: **Same** — Spring Security tem suporte first-class a resource server OIDC.

### Opção 2: Autenticação local (usuários/senha no PostgreSQL) + JWT próprio

- **Descrição**: O SIFAP gerencia seus próprios usuários e emite JWTs após login local.
- **Prós**:
  - Independente de provedores externos.
  - Controle total do ciclo de vida do usuário.
- **Contras**:
  - Reintroduz gestão de senhas, hashing, reset, MFA — toda a superfície que o gov.br elimina.
  - Diverge do padrão de identidade do governo; mais código de segurança para manter e auditar.
- **Risk**: Médio-Alto — toda falha de gestão de credenciais vira responsabilidade do time (OWASP A07/A02).
- **Effort**: **Higher** — implementar e endurecer fluxo de credenciais.

### Opção 3: Manter modelo legado de perfis sem IdP (basic/sessão)

- **Descrição**: Replicar o controle por `COD-PERFIL` com autenticação básica ou sessão de servidor.
- **Prós**:
  - Mínima mudança conceitual em relação ao legado.
- **Contras**:
  - Basic auth/sessão não atende OAuth2/JWT exigido pela constituição; fraco para API + SPA.
  - Sem federação, sem SSO, sem padrão moderno de token.
- **Risk**: Alto — não conforme com os requisitos de segurança do projeto.
- **Effort**: **Lower** agora, **higher** em retrabalho de conformidade.

## Decisão

**Adotamos a Opção 1**: autenticação federada via **gov.br (OIDC)**, backend como OAuth2 resource server validando JWT com Spring Security, e autorização **RBAC** mapeando os 5 perfis legados (ADM/OPR/CON/AUD/SUP) para authorities.

**Motivo (uma frase):** é a única opção conforme à constituição (OAuth2/JWT + Spring Security), que elimina a gestão de senhas e preserva os perfis legados como papéis de autorização.

## Consequências

### Positivas

- Sem armazenamento de senhas no SIFAP; SSO gov.br para os usuários.
- Autorização declarativa por endpoint no [`openapi.yaml`](../openapi.yaml) (ex.: auditoria só leitura para CON/AUD).
- Conformidade direta com os requisitos OWASP/segurança do projeto.

### Negativas

- Acoplamento à disponibilidade do gov.br — **mitigação**: cache de chaves públicas (JWKS), timeouts e telas de fallback no frontend.
- Necessário mapear claims gov.br → perfis SIFAP — **mitigação**: tabela de mapeamento configurável em `shared/security`, auditada.

### Riscos

- Se a integração gov.br não estiver disponível no ambiente do workshop, o desenvolvimento fica bloqueado. Contingência: perfil `mock-oidc` local (Spring profile) que emula os claims/perfis para desenvolvimento, **nunca** habilitado em produção.

## Requisitos Relacionados

- REQ-017 (mascaramento de dados sensíveis em logs)
- REQ-015/REQ-016 (auditoria por perfil; consultas restritas por papel)
- Perfis legados ADM/OPR/CON/AUD/SUP — discovery-report §2.3
- Bounded context transversal: `shared/security` em [`modular-monolith-design.md`](../modular-monolith-design.md)
- ADRs relacionados: [ADR-001](adr-001-modular-monolith-vs-microservices.md)

---

**Definição de Pronto:** Formato MADR, 3 opções com prós/contras específicos do SIFAP, decisão datada (2026-06-10), consequências positivas e negativas, REQ-IDs/perfis relacionados.
