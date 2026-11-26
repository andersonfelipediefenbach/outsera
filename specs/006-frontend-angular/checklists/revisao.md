# Checklist de revisão: Marco 6 — Front-end em Angular

**Objetivo**: o que o revisor confere no pull request para comprovar os requisitos de documentação e de governança
(FR-024 e FR-031 a FR-034) e o critério de usabilidade (SC-003), conforme o Princípio VI.
**Criado em**: 2026-11-26
**Especificação**: [spec.md](../spec.md)

Os itens são marcados pelo revisor ou pelo responsável no PR, e não pelo implementador.

## Constituição e AGENTS.md (FR-024)

- [ ] A `.specify/memory/constitution.md` está na versão 2.1.0, com o relatório de impacto no topo e:
  - no Princípio II, o front fora do `./mvnw`, sem exigir Node;
  - no Princípio VI, o tipo de comprovação "comportamento do front-end" (unitários com 80% de linhas e ponta a
    ponta contra a aplicação real, no pipeline), sem dispensar o IT dos requisitos da API;
  - o portão de merge com os jobs do front.
- [ ] O `AGENTS.md` traz o front fora do `./mvnw` nas regras invioláveis, os testes do front, a linha "Front-end"
  na stack e a versão do Angular entre os pontos decididos.
- [ ] O `docs/ai/decisoes.md` registra a emenda 2.1.0.

## Front fora do compose (FR-031)

- [ ] O `compose.yaml` não mudou, e o README diz que o front fica fora do compose.

## README (FR-032)

- [ ] Em "2. Avalie em 2 minutos", a URL do front (painel e lista filtrada), com o aviso da partida a frio dos dois
  serviços.
- [ ] A seção "Marco 6: front-end em Angular (opcional)" explica as duas telas, as agregações, como rodar o front
  com `npm start` contra o `./mvnw spring-boot:run`, os testes do front, a imagem e a publicação, e o que fica de
  fora.
- [ ] A seção 4 traz o contrato 1.3.0, os dois endpoints novos e a subseção "Agregações do painel".
- [ ] "Requisitos do Marco 6" traz cada requisito com a sua comprovação (FR-035).
- [ ] A tabela de jobs do pipeline traz `frontend`, `web` e `deploy-web`, os 12 checks obrigatórios e o
  `workflow-policy` atualizado.
- [ ] O `frontend/README.md` traz os pré-requisitos e os comandos (`npm ci`, `npm start`, `lint`, `test`, `build`,
  `e2e`).

## ADR e runbook (FR-033)

- [ ] O `docs/adr/0028-front-end-angular-com-nginx.md` tem contexto, decisão, alternativas e consequências,
  inclusive a mesma origem sem CORS, o segundo serviço com 0 a 1 instância, o log de acesso em JSON por
  `LOG_FORMAT` e o limite do `error_log` em texto.
- [ ] O índice `docs/adr/README.md` traz o ADR 0028.
- [ ] O `docs/runbook.md` tem a seção "Publicação do front no Cloud Run", com a volta do tráfego do `awards-web` e
  o caso do `/api` com 502 ou 504.

## Decisões (FR-034)

- [ ] O `docs/ai/decisoes.md` registra os desvios do briefing: Cloud Run em vez de hospedagem estática, mesma
  origem em vez de CORS, front fora do compose, jobs no mesmo workflow e a versão do Angular.
- [ ] O `docs/ai/decisoes.md` registra o contrato 1.3.0, as exceções do `ApiDocsIT` e do `ApiDocsSecurityIT` e a
  correção do `yq | grep -q` no `check-workflows.sh`.

## Usabilidade (SC-003)

Conferido na URL do front ou no `npm start`, sem ferramenta nova:

- [ ] DevTools em 360 px de largura: nenhuma rolagem horizontal no Painel e em Filmes.
- [ ] Só o teclado (Tab, Shift+Tab, Enter e Espaço): menu, busca por ano, filtros e paginação funcionam, com o foco
  visível em cada controle.
- [ ] Inspetor de cores do navegador: contraste de pelo menos 4,5:1 nos textos e 3:1 nos controles e no contorno
  de foco.
- [ ] Vencedores de 1990 encontrados em até 3 interações (digitar o ano, buscar e ler), no Painel e em Filmes.
