# Specification Quality Checklist: Marco 3 — Modo Docker

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-11-24
**Feature**: [spec.md](../spec.md)

## Content Quality

- [x] No implementation details (languages, frameworks, APIs)
- [x] Focused on user value and business needs
- [x] Written for non-technical stakeholders
- [x] All mandatory sections completed

## Requirement Completeness

- [x] No [NEEDS CLARIFICATION] markers remain
- [x] Requirements are testable and unambiguous
- [x] Success criteria are measurable
- [x] Success criteria are technology-agnostic (no implementation details)
- [x] All acceptance scenarios are defined
- [x] Edge cases are identified
- [x] Scope is clearly bounded
- [x] Dependencies and assumptions identified

## Feature Readiness

- [x] All functional requirements have clear acceptance criteria
- [x] User scenarios cover primary flows
- [x] Feature meets measurable outcomes defined in Success Criteria
- [x] No implementation details leak into specification

## Revisão do README (comprovação da FR-036, na revisão do PR)

- [ ] O README explica como subir o modo Docker, as portas e o papel de cada serviço do compose.
- [ ] O README documenta o formato do evento de filme e como publicar um vencedor novo no compose.
- [ ] O README documenta a autenticação: variável, cabeçalho, token de demonstração, endpoints
  protegidos e públicos.
- [ ] O README documenta as credenciais de demonstração do compose como valores só para uso local
  (FR-033).
- [ ] O README explica como rodar o perfil Maven de PostgreSQL.
- [ ] A tabela de requisitos e testes inclui os requisitos do Marco 3 (Princípio VI).
- [ ] A seção "Avalie em 2 minutos" continua funcionando só com o JDK.

## Revisão dos ADRs (comprovação da FR-037, na revisão do PR)

- [ ] Há um ADR para cada decisão listada na FR-037.
- [ ] Cada ADR tem número, título, data, status, contexto, decisão, alternativas consideradas e
  consequências, e cita a entrada em `docs/ai/decisoes.md` (spec 002, FR-031).
- [ ] O índice de `docs/adr/` lista os ADRs novos.
- [ ] O ADR da autenticação registra como decisão intencional:
  - que `X-Content-Type-Options` e `X-Frame-Options` aparecem em todas as respostas, com o token
    ligado ou desligado;
  - que o `Cache-Control` do Spring Security fica fora dos endpoints de negócio.

## Revisão de segurança (comprovação da decisão de design da R11, na revisão do PR)

- [ ] A comparação do token no `TokenAuthenticationFilter` usa o mecanismo da research R11:
  `MessageDigest.isEqual(sha256(recebido), sha256(esperado))`, em UTF-8, sem `String.equals`,
  `Arrays.equals` nem saída antecipada.
- [ ] O ADR 0017 cita esse mecanismo.
- [ ] O filtro e o `ProblemAuthenticationEntryPoint` não registram nem devolvem o cabeçalho
  `Authorization` nem o token.
- [ ] A matriz de acesso do `SecurityConfiguration` é igual à de `contracts/operational-endpoints.md`.

## Notes

- **Detalhes de tecnologia:** PostgreSQL, Kafka, Prometheus, Grafana e compose aparecem na spec porque
  são o próprio escopo pedido do marco, como na spec 002. Nomes de cabeçalho, de variáveis, de tópicos,
  partições e bibliotecas ficaram para o plano.
- **Credenciais do compose:** a FR-033 (senhas de demonstração) está coberta pela emenda 1.6.0 do
  Princípio VII, já registrada em `docs/ai/decisoes.md` e no `AGENTS.md`.
- **Princípios II e VI:** harmonizados pela emenda 1.7.0 do Princípio VI. O que depende do PostgreSQL
  (FR-004, FR-006, FR-008 e a persistência entre reinícios) é comprovado no perfil Maven dedicado,
  executado obrigatoriamente no pipeline (FR-038).
- As seções "Revisão do README" e "Revisão dos ADRs" ficam desmarcadas até a revisão do PR.
