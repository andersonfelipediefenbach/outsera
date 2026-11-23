# Specification Quality Checklist: Marco 2 — Operação

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-11-23
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

## Revisão dos ADRs (no pull request)

Comprovação da FR-030 e da FR-031 (FR-032). Marcada pelo revisor no pull request que entrega os ADRs,
e não na validação da spec.

- [ ] Cada ADR tem contexto, decisão, alternativas consideradas e consequências.
- [ ] Cada ADR cita a entrada correspondente em `docs/ai/decisoes.md`.
- [ ] Há um ADR para cada decisão de arquitetura listada na FR-030.

## Revisão do README (no pull request)

Comprovação da FR-034 (FR-032). Marcada pelo revisor no pull request do marco.

- [ ] O README documenta o ETag e o 304, os endpoints de saúde e de métricas, a variável de formato do
  log, a imagem, o pipeline com os critérios de reprovação e o índice dos ADRs.
- [ ] A tabela de requisitos e testes inclui os requisitos do Marco 2, com a comprovação de cada um.
- [ ] Os comandos da seção "Avalie em 2 minutos" funcionam só com o JDK, num clone limpo.

## Notes

- Clarificações de 2026-11-23, todas resolvidas:
  - formato do log: texto no `./mvnw`, JSON por variável ligada na imagem (constituição 1.3.0);
  - pipeline: segurança alta ou crítica com correção disponível reprova; cobertura mínima de 90% de
    linhas, com 97,06% medidos;
  - ADRs comprovados por revisão, pela seção "Revisão dos ADRs" acima.
- Detalhes de tecnologia aparecem só onde o próprio pedido os fixa como escopo (Prometheus, GitHub
  Actions, Dependabot, Dockerfile, `./mvnw`, JDK 25). Os critérios de sucesso não citam ferramentas além
  do que o pedido impõe.
- Os FRs de comportamento referenciam a spec 001 quando estendem um requisito dela.
