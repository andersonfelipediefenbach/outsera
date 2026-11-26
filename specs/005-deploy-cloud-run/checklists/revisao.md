# Checklist de revisão: Marco 5 — Publicação no Cloud Run

**Purpose**: o que o revisor confere no pull request para comprovar os requisitos de documentação
(FR-023, FR-024 e FR-025), conforme o Princípio VI.
**Created**: 2026-11-26
**Feature**: [spec.md](../spec.md)

Os itens são marcados pelo revisor ou pelo responsável no PR, e não pelo implementador.

## README (FR-023)

- [ ] Em "2. Avalie em 2 minutos", a URL pública de intervalos e do Swagger UI, com o aviso da partida a frio
  e um `curl` de exemplo.
- [ ] A seção "Marco 5: publicação no Cloud Run (opcional)" explica:
  - o que é publicado (só o modo padrão);
  - o fluxo sem tráfego, conferência e promoção;
  - a federação sem chaves;
  - o limite de uma instância por revisão e o motivo (ADR 0026);
  - as métricas fechadas;
  - o que fica de fora;
  - o `deploy/gcp/setup.sh`.
- [ ] "Requisitos do Marco 5" traz cada requisito com a sua comprovação (FR-026).
- [ ] A tabela de jobs do pipeline traz o job `deploy` e a regra nova do `workflow-policy`.

## ADR e runbook (FR-024)

- [ ] O `docs/adr/0027-publicacao-no-cloud-run.md` tem contexto, decisão, alternativas e consequências,
  inclusive as métricas fechadas e a convivência de duas revisões na troca.
- [ ] O índice `docs/adr/README.md` traz o ADR 0027.
- [ ] O `docs/runbook.md` tem a seção "Publicação no Cloud Run", com a falha antes e depois da troca de
  tráfego, a volta à revisão anterior e a nova publicação.

## Decisões (FR-025)

- [ ] O `docs/ai/decisoes.md` registra:
  - a publicação por merge, e não por tag;
  - a opção A da clarificação;
  - o serviço criado pelo script;
  - as métricas fechadas;
  - a `deploy-cloudrun` não usada;
  - os identificadores no workflow;
  - a política ampliada;
  - a retenção;
  - as conferências locais reduzidas.

## Segurança e dados pessoais

- [ ] Nenhum segredo, token ou chave nos arquivos novos, no workflow e nas evidências
  (`specs/005-deploy-cloud-run/evidencias/`).
- [ ] Nenhum e-mail pessoal nas evidências. Os e-mails das contas de serviço são identificadores públicos.
