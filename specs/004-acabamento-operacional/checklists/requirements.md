# Specification Quality Checklist: Marco 4 — Acabamento operacional

**Purpose**: Validate specification completeness and quality before proceeding to planning
**Created**: 2026-11-25
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

## Revisão da documentação (comprovação do Princípio VI, FR-038)

Itens conferidos pelo revisor no pull request do marco.

- [ ] Página do SLO (FR-003): objetivos de disponibilidade e de latência, janela, requisição boa e ruim,
  origem dos valores na medição do teste de carga e a diferença para os limites do pipeline.
- [ ] Runbook (FR-007): uma seção por alerta, com significado, o que conferir e o que fazer; o link de cada
  alerta chega à seção certa.
- [ ] README (FR-018, FR-036): seção do Marco 4 entre os extras, com os números medidos (carga, tempos da
  carga inicial, subida sem e com o cache), como verificar a assinatura e como abrir o dev container; tabela
  de requisitos com o Marco 4; "Avalie em 2 minutos" ainda só com o JDK.
- [ ] ADRs (FR-037): um por decisão do marco, no formato dos anteriores.
- [ ] Dev container, portas encaminhadas (FR-029): com o dev container aberto e o compose no ar, a API
  (8080), o Prometheus (9090) e o Grafana (3000) abrem na máquina. O job `devcontainer` comprova o compose
  e o resultado de referência dentro do container, mas não o encaminhamento para a máquina.
- [ ] `docs/ai/decisoes.md`: proteção da `main` com a lista de checks e o bypass desligado (FR-031); causa
  da lacuna do job de dependências, com a evidência (FR-032); verificação única contra a versão vulnerável
  (FR-034), feita em branch temporário apagado, sem commit de volta ao marco; resultado do testador
  adversário (FR-035); medição do k6 no runner compartilhado e folga escolhida para os limites do pipeline
  (FR-011a). Sem dado pessoal.

## Notes

- A spec nomeia Prometheus, k6, cosign, SBOM e dev container porque são o próprio escopo pedido (itens de
  operação e de cadeia de entrega), como a spec 003 nomeia Kafka e PostgreSQL. Os critérios de sucesso
  continuam descritos por resultado (alerta dispara ou não, job verde ou vermelho, tempos medidos).
- Duas clarificações resolvidas em 2026-11-25: cache só da JVM (sem o AOT do Spring) e assinatura num
  registro temporário dentro do job.
- Os números dos objetivos do SLO e dos limites do teste de carga são definidos pela medição no próprio
  bolt (briefing), não nesta spec.
