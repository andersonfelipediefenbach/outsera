---

description: "Tarefas do Marco 6: front-end em Angular, agregações na API e publicação do awards-web"
---

# Tarefas: Marco 6 — Front-end em Angular, com painel e lista de filmes

**Entrada**: documentos de `specs/006-frontend-angular/` (spec, plan, research, data-model, contracts e quickstart)

**Pré-requisitos**: plan.md, spec.md, research.md, data-model.md e contracts/

**Testes**: pedidos pela spec. A API ganha ITs (FR-035, Princípio I), escritos **antes** do código de cada
endpoint. O front ganha testes unitários (FR-022) e de ponta a ponta (FR-023). O Princípio VI vale para tudo:
cada requisito aponta para a sua comprovação.

**Organização**: as fases seguem as **etapas do bolt** do plano, e cada tarefa leva a história da spec que atende.
Cada etapa termina com **os testes da etapa verdes e um commit**:
- etapas que mexem na API: `./mvnw verify` e `./mvnw -Pextras verify` verdes, com o resultado real informado;
- etapas do front: `npm run lint`, `npm test -- --watch=false --coverage` e `npm run build` verdes em `frontend/`;
- antes do push: os dois `./mvnw` e os três comandos do front verdes, juntos (T066).

**Comprovação local:** só o que é rápido (comandos Maven, comandos do front, `curl` na API local, `bash -n` nos
scripts e o `setup.sh`). O que o pipeline comprova (imagem do front, teste de fumaça, Playwright, Trivy, PostgreSQL,
publicação) **não** é repetido na máquina.

**Paradas:** o implementador só para nas tarefas marcadas **[Anderson]** (push e checks obrigatórios da `main`) ou
quando algo falha. O login no `gcloud` já está feito (conta ativa e projeto `outsera-awards`). Antes de qualquer
`gcloud` no Git Bash, rodar `source ~/.bashrc`, que define o `CLOUDSDK_PYTHON` (research R9 do Marco 5). Não há
Python, `jq` nem `yq` na máquina; para conferir YAML, usar `npx --yes js-yaml@4.1.0 <arquivo>`.

## Formato: `[ID] [P?] [História] Descrição`

- **[P]**: pode rodar em paralelo (arquivos diferentes, sem dependência pendente)
- **[História]**: história da spec atendida (US1 a US6)
- **[Anderson]**: passo que depende do Anderson; o implementador para e pede

---

## Fase 1: Setup

**Objetivo**: confirmar o ponto de partida.

- [X] T001 Confirmar o branch `marco/6-frontend` com a árvore limpa, a não ser por `specs/006-frontend-angular/` e
  `docs/ai/prompts.md`. Fazer o commit da spec e do plano: `specs/006-frontend-angular/` com spec, checklist,
  plan, research, data-model, contracts, quickstart e este tasks.md (mensagem
  "Marco 6: spec, plano e tarefas"). Não incluir o `docs/ai/prompts.md`, que é do hook.

---

## Fase 2: Etapa 1 — Emenda 2.1.0 da constituição (FR-024; bloqueia as demais) 🧱

**Objetivo**: a regra do front-end fica na constituição antes de qualquer código (Princípio IV; research R11).

**Teste independente**: revisão no PR. Nenhum comando Maven muda; não é preciso rodar o `./mvnw` nesta etapa,
porque só texto muda.

- [X] T002 Emendar `.specify/memory/constitution.md` para a **2.1.0** (MINOR), com o "Sync Impact Report (2.1.0)"
  no topo, no formato dos anteriores (versão, justificativa, origem: spec 006 FR-024; princípios modificados;
  impacto com ✅):
  - **Princípio II:** novo item: "O front-end (`frontend/`) fica fora do `./mvnw`: nenhum comando Maven MUST
    exigir Node nem construir o front, e o front MUST NOT ser pré-requisito para subir a API ou rodar os testes
    dela.";
  - **Princípio VI:** novo tipo de comprovação, **comportamento do front-end**: testes unitários com cobertura
    mínima de linhas de 80% e teste de ponta a ponta contra a aplicação real (sem respostas simuladas),
    executados no pipeline, com o job vermelho reprovando o merge. Um requisito de comportamento da API continua
    exigindo IT e MUST NOT ser comprovado pelos testes do front;
  - **Princípio I:** a última frase ("Testes unitários são permitidos apenas no front-end...") ganha "comprovados
    conforme o Princípio VI";
  - **Portão de merge:** acrescentar "e os jobs do front-end verdes no pipeline";
  - rodapé: `**Version**: 2.1.0 | **Ratified**: 2026-11-18 | **Last Amended**: 2026-11-26`.
- [X] T003 [P] Alinhar o `AGENTS.md`:
  - "Regras invioláveis": item novo sobre o front fora do `./mvnw` (constituição 2.1.0, Princípio II);
  - "Testes": o front tem testes unitários (Vitest, 80% de linhas) e de ponta a ponta (Playwright) no pipeline;
  - tabela "Stack e convenções": linha "Front-end" com Angular 22.3.0, Node 26 LTS e nginx sem root;
  - "Pontos em aberto": tirar a versão do Angular e movê-la para a lista dos decididos, citando
    `specs/006-frontend-angular/` (research R1). Se não restar nenhum ponto em aberto, escrever "Nenhum no
    momento.".
- [X] T004 [P] Registrar a emenda em `docs/ai/decisoes.md`, numa entrada nova no formato das anteriores, com o
  título "Emenda 2.1.0 da constituição: comprovação do front-end", o motivo (spec 006, FR-024) e o impacto (AGENTS.md
  e spec 006).
- [X] T005 Commit da etapa 1: "Marco 6, etapa 1: emenda 2.1.0 da constituição para o front-end".

---

## Fase 3: Etapa 2 — Agregações na API (US4) 🔢

**Objetivo**: os dois endpoints de agregação existem, no padrão dos endpoints de negócio, e o contrato passa a
1.3.0 sem mudar o que já existe.

**Teste independente**: os ITs novos verdes no `./mvnw verify` e no `./mvnw -Pextras verify`, e
`curl localhost:8080/api/aggregations/years-with-multiple-winners` devolvendo 1986, 1990 e 2015 com 2.

### Testes primeiro (devem falhar antes da implementação)

**Regra para todos os ITs desta etapa (Princípio VI, "cada teste MUST indicar o requisito que cobre"):** cada
método `@Test` tem `@DisplayName("S006 FR-00x <o que confere>")` com o requisito da spec 006 que cobre, no padrão
dos ITs existentes (por exemplo, `"S005 FR-027 bearerAuth descrito como opcional"`), e o javadoc da classe cita a
spec 006 e os requisitos.

- [X] T006 [US4] Copiar `specs/006-frontend-angular/contracts/openapi.yaml` para
  `src/main/resources/static/openapi.yaml` (cópia idêntica, byte a byte; conferir com `diff`).
- [X] T007 [P] [US4] Criar os CSVs de cenário em `src/test/resources/scenarios/aggregations/`, com o cabeçalho
  `year;title;studios;producers;winner` e o separador `;`:
  - `years.csv`: 2001 com 1 vencedor e 2 indicados; 2002 com 2 vencedores; 2003 com 3 vencedores; 2004 só com
    indicados; linhas fora de ordem de ano (para provar a ordem crescente);
  - `studios.csv`: vencedores cuja coluna de estúdios tem, **sem aspas** no arquivo (o separador do CSV é `;`, então
    a vírgula não precisa de aspas), os valores `A, B`, `A,B`, `B ,C`, `A, A` (mesmo estúdio repetido no filme),
    vazio, `A,,C`, `Mgm` e `MGM` (diferentes), um filme só com `Delta` e outro só com `Charlie` (empatados com 1
    vitória cada), e um indicado com `Z` (não conta). Por exemplo, a linha `2002;Filme 2;A,B;Prod;yes`;
  - `no-winners.csv`: três filmes, nenhum vencedor.
  Os CSVs não levam comentários (o leitor não os aceita). A resposta esperada de cada cenário fica no javadoc do
  IT correspondente (T009 e T010), calculada à mão e conferida.
- [X] T008 [P] [US4] Criar `src/test/java/com/outsera/awards/aggregations/AggregationsReferenceIT.java`
  (`ScenarioIT`, arquivo fornecido), com `@DisplayName` citando "S006 FR-001"/"S006 FR-002":
  - anos: corpo exatamente `{"years":[{"year":1986,"winnerCount":2},{"year":1990,"winnerCount":2},{"year":2015,"winnerCount":2}]}`;
  - estúdios: os cinco primeiros `Columbia Pictures 7`, `Paramount Pictures 6`, `Warner Bros. 5`,
    `20th Century Fox 4`, `MGM 3`, e a **lista completa** igual a um oráculo calculado no próprio teste a partir
    de `src/main/resources/movielist.csv` (lido com Commons CSV, separado por vírgula, `strip`, sem vazios, um
    `Set` por filme, ordem `winCount` decrescente e depois `String.compareTo`).
- [X] T009 [P] [US4] Criar `src/test/java/com/outsera/awards/aggregations/YearsWithMultipleWinnersIT.java`
  (`scenarios/aggregations/years.csv`): só 2002 (2) e 2003 (3), nessa ordem; 2001 e 2004 ausentes; indicados não
  contam (FR-001).
- [X] T010 [P] [US4] Criar `src/test/java/com/outsera/awards/aggregations/StudiosWinCountIT.java`
  (`scenarios/aggregations/studios.csv`): a lista completa esperada, com o estúdio repetido no mesmo filme contado
  uma vez, nomes sem espaços nas pontas, vazios descartados, `MGM` e `Mgm` separados, `Charlie` antes de `Delta`
  no empate e `Z` ausente (FR-002; data-model, "Regras de separação dos estúdios").
- [X] T011 [P] [US4] Criar `src/test/java/com/outsera/awards/aggregations/AggregationsEmptyIT.java`
  (`no-winners.csv`): os dois endpoints com 200, `{"years":[]}` e `{"studios":[]}` (FR-003).
- [X] T012 [P] [US4] Criar `src/test/java/com/outsera/awards/aggregations/AggregationsEtagIT.java` (arquivo
  fornecido): `ETag` presente nos dois endpoints; o mesmo pedido com `If-None-Match` igual dá 304 sem corpo; um
  ETag diferente dá 200 (FR-003). Seguir o padrão do `etag/MoviesEtagIT.java`.
- [X] T013 [P] [US4] Criar `src/test/java/com/outsera/awards/aggregations/AggregationsContractIT.java`: as
  respostas 200 dos dois endpoints (arquivo fornecido e `no-winners.csv`, em classes aninhadas ou dois métodos com
  o cenário do arquivo fornecido) válidas pelo `support/ContractValidator.assertValidGet` contra o contrato 1.3.0
  (FR-004).
- [X] T014 [P] [US4] Criar `src/test/java/com/outsera/awards/aggregations/AggregationsAuthIT.java`
  (`TokenScenarioIT`): sem token e com token errado, 401 em `application/problem+json` com `WWW-Authenticate`;
  com `bearer()`, 200, nos dois endpoints (FR-003). Fica **fora** do `default-mode-its.txt` (roda no `extras`).
- [X] T015 [P] [US4] Criar `src/test/java/com/outsera/awards/postgres/AggregationsPostgresIT.java`, no padrão do
  `ListingOrderPostgresIT` (`PostgresSupport.newSchema`, `@AfterAll` que apaga o esquema): com o arquivo
  fornecido, as duas agregações iguais às do `AggregationsReferenceIT` (research R7). Não roda localmente: o job
  `postgres` do pipeline comprova.
- [X] T016 [US4] Acrescentar ao `src/test/default-mode-its.txt` a seção `# Marco 6, no modo padrão (spec 006,
  FR-001 a FR-006)` com os seis ITs do modo padrão (T008 a T013), um por linha, relativos a `src/test/java`.
- [X] T017 [US4] Atualizar os ITs com exceção já registrada:
  - `src/test/java/com/outsera/awards/docs/ApiDocsIT.java`: `SPEC_CONTRACT` passa a
    `specs/006-frontend-angular/contracts/openapi.yaml`, com o javadoc "Contrato 1.3.0 (spec 006, FR-004). Os das
    specs 001 a 005 ficam como registro.";
  - `src/test/java/com/outsera/awards/docs/ApiDocsSecurityIT.java`: versão `1.3.0` no `containsEntry`, no
    `@DisplayName` e no javadoc, e os dois caminhos de agregação na lista de `paths` conferidos (mesmo `security` e
    mesma resposta 401).
  Registrar em `docs/ai/decisoes.md` a entrada "Contrato 1.3.0: agregações do painel (spec 006)", explicando que os
  dois ITs mudam pelas exceções já existentes em `.github/unchanged-tests-exceptions.txt`, sem título novo. Conferir
  que os títulos citados nesse arquivo continuam no `decisoes.md`.
- [X] T018 [US4] Rodar `./mvnw verify` e confirmar que os ITs novos **falham** (404 nos endpoints) e que nenhum IT
  antigo quebrou além dos que dependem dos endpoints novos. Registrar o resultado na tarefa.

### Implementação

- [X] T019 [P] [US4] Criar os records de resposta em `src/main/java/com/outsera/awards/aggregation/`:
  `YearWinnerCount(int year, long winnerCount)`, `YearsWithMultipleWinners(List<YearWinnerCount> years)`,
  `StudioWinCount(String name, long winCount)` e `StudiosWithWinCount(List<StudioWinCount> studios)`, com os nomes
  de campo exatos do contrato (`years`, `year`, `winnerCount`, `studios`, `name`, `winCount`) e nenhum campo a mais.
- [X] T020 [P] [US4] Criar `src/main/java/com/outsera/awards/aggregation/StudioNameParser.java`: recebe o texto de
  `studios` (pode ser `null`) e devolve um `Set<String>` em ordem de aparição. Regras do data-model: "`null`
  (coluna vazia no CSV, spec 001 FR-006e) → nenhum estúdio"; "Separa por `,`; cada parte com `strip()`; partes
  vazias saem"; "Um conjunto por filme"; "Sem separar por ` and `"; "Maiúsculas e acentos são mantidos".
- [X] T021 [US4] Acrescentar ao `src/main/java/com/outsera/awards/movie/MovieRepository.java` duas consultas JPQL
  portáveis, com javadoc citando a spec 006, sem depender do pacote `aggregation` (o `movie` não importa classes
  dele):
  - anos: `select m.year as year, count(m) as winnerCount from Movie m where m.winner = true group by m.year
    having count(m) > 1 order by m.year`, devolvendo uma projeção por interface declarada no próprio
    `MovieRepository` (`interface WinnerCountByYear { int getYear(); long getWinnerCount(); }`). O
    `AggregationService` monta os records `YearWinnerCount` a partir dela;
  - estúdios: `select m.studios from Movie m where m.winner = true and m.studios is not null`, devolvendo
    `List<String>`.
- [X] T022 [US4] Criar `src/main/java/com/outsera/awards/aggregation/AggregationService.java` (transação só de
  leitura, como o `MovieQueryService`): anos direto da consulta; estúdios com o `StudioNameParser`, contagem num
  mapa e ordem por `winCount` decrescente e depois `String.compareTo` do nome (a mesma ordem de nome do
  `ProducerIntervalService`). Sem cache (research R6).
- [X] T023 [US4] Criar `src/main/java/com/outsera/awards/aggregation/AggregationController.java`:
  `@RequestMapping("/api/aggregations")`, `GET /years-with-multiple-winners` e `GET /studios-with-win-count`, com
  `produces = application/json` e sem parâmetros.
- [X] T024 [US4] Registrar os dois caminhos novos no `src/main/java/com/outsera/awards/web/EtagConfiguration.java`
  (`addUrlPatterns`) e no `PROTECTED` de `src/main/java/com/outsera/awards/security/SecurityConfiguration.java`,
  atualizando os javadocs (spec 006, FR-003).
- [X] T025 [US4] Rodar `./mvnw verify` e `./mvnw -Pextras verify` e informar o resultado real (testes, falhas e
  cobertura do `extras`, que precisa continuar ≥ 90%). Subir `./mvnw spring-boot:run` e conferir com `curl` os dois
  endpoints e o `/api/producers/intervals` (resultado de referência intacto). Parar o processo depois.
- [X] T026 [US4] Commit da etapa 2: "Marco 6, etapa 2: agregações de anos e estúdios na API (contrato 1.3.0)".

---

## Fase 4: Etapa 3 — Projeto do front e Dashboard (US1 e US3) 🎯 MVP

**Objetivo**: o Dashboard funciona localmente contra a API do `./mvnw spring-boot:run`, com os quatro painéis,
responsivo e acessível.

**Teste independente**: `npm run lint`, `npm test -- --watch=false --coverage` (≥ 80%) e `npm run build` verdes;
`npm start` com a API local mostra os valores de referência (quickstart, passo 3).

- [X] T027 [US1] Criar o projeto com `npx --yes @angular/cli@22.3.0 new frontend --directory frontend --routing --style=css
  --ssr=false --skip-git --package-manager=npm` (zoneless e standalone, o padrão da 22). Conferir que o construtor de
  testes é o Vitest; se o `ng new` perguntar, escolher Vitest. Depois:
  - fixar as versões exatas dos pacotes `@angular/*` (sem `^` nem `~`) no `frontend/package.json` e regerar o
    `package-lock.json` com `npm install`;
  - `frontend/.nvmrc` com `26` e `"engines": { "node": ">=26" }`;
  - `npx ng add angular-eslint@<versão compatível com a 22.3>` e script `lint`;
  - `@vitest/coverage-v8` e `jsdom` como dependências de desenvolvimento, e o limite `coverage.thresholds.lines: 80`
    (no `angular.json`, se o construtor aceitar; senão em `frontend/vitest-base.config.ts`, como diz o risco do
    plano). Conferir uma vez que o comando reprova com um limite de 100 e voltar a 80;
  - `budgets` do `angular.json`: aviso em 350 kB e erro em 500 kB no bundle inicial (o build básico tem cerca de
    310 kB);
  - `frontend/proxy.conf.json` com `/api` → `http://localhost:8080` e `"proxyConfig"` no alvo `serve` (FR-021);
  - `index.html` com `lang="pt-BR"` e título "Golden Raspberry Awards".
  Conferir que o `.gitignore` da raiz ou o de `frontend/` deixa fora `node_modules/`, `dist/`, `coverage/`,
  `test-results/` e `playwright-report/`.
- [X] T028 [P] [US1] Criar `frontend/src/app/api/awards-api.ts` (`@Injectable({ providedIn: 'root' })`, `HttpClient`, só
  caminhos relativos `/api/...`; FR-018) com os tipos do contrato (`ProducerIntervals`, `MoviePage`, `Movie`,
  `YearsWithMultipleWinners`, `StudiosWithWinCount`) e os métodos `intervals()`, `yearsWithMultipleWinners()`,
  `studiosWithWinCount()`, `winnersOf(year)` (`/api/movies?year={ano}&winner=true&size=100`) e
  `movies(filtro)`, que traduz o filtro do front para a API conforme a tabela "Tradução entre a URL do front e a
  API" do data-model (`vencedor=sim` → `winner=true`, `pagina=3` → `page=2`, `size=10` fixo). Testes em
  `awards-api.spec.ts` com `HttpTestingController`, cobrindo cada URL gerada.
- [X] T029 [P] [US1] Criar `frontend/src/app/shared/panel-state/` (componente e `panel-state.ts`): o tipo
  `PanelState<T>` com `status` ∈ {`carregando`, `dados`, `vazio`, `erro`} e um componente que mostra
  "Carregando…" com `aria-live="polite"`, a mensagem de vazio recebida e, no erro, "Não foi possível carregar os
  dados." com o botão "Tentar de novo", que emite um evento (FR-009). Uma função utilitária `load(obs, isEmpty)`
  converte um `Observable` num `signal<PanelState<T>>`. Testes unitários dos quatro estados e do evento.
- [X] T030 [P] [US1] Criar `frontend/src/app/shared/validators.ts` com o validador de ano (`^\d+$`, inteiro não
  negativo) e a mensagem "Informe um ano válido (só números)." (FR-008 e FR-011), com testes de vazio, letras,
  negativo, decimal, espaços e válido.
- [X] T031 [US1] Criar o layout e as rotas: `frontend/src/app/app.ts` (cabeçalho com o menu "Painel" e "Filmes",
  `routerLinkActive` com `aria-current="page"`; FR-016) e `frontend/src/app/app.routes.ts` com `''` → Dashboard
  (título "Painel — Golden Raspberry Awards"), `'filmes'` → Lista (título "Filmes — Golden Raspberry Awards", com
  um componente provisório até a etapa 4) e `'**'` → redireciona para `''` (contracts/frontend-ui.md, "Rotas").
  `provideHttpClient(withFetch())` no `app.config.ts`. Teste unitário do redirecionamento e do menu ativo.
- [X] T032 [P] [US1] Criar `frontend/src/app/dashboard/multiple-winners-panel/`: `h2` "Anos com mais de um
  vencedor", tabela Ano e Vencedores com `caption`, vazio "Nenhum ano com mais de um vencedor.". Testes: dados,
  vazio, erro e "tentar de novo" refazendo só a chamada deste painel.
- [X] T033 [P] [US1] Criar `frontend/src/app/dashboard/top-studios-panel/`: `h2` "Estúdios com mais vitórias", só
  os **3 primeiros** itens da API (Estúdio, Vitórias), vazio "Nenhum estúdio vencedor.". Testes: com 5 itens mostra 3
  na ordem da API; com 2 mostra 2; vazio; erro.
- [X] T034 [P] [US1] Criar `frontend/src/app/dashboard/producer-intervals-panel/`: `h2` "Produtores com o maior e o
  menor intervalo", duas tabelas "Maior intervalo" (`max`) e "Menor intervalo" (`min`) com Produtor, Intervalo,
  Vitória anterior e Vitória seguinte, **todos** os itens (Princípio III); vazio "Nenhum produtor com duas
  vitórias." quando `min` e `max` vêm vazios. Testes: empates com vários itens; vazio; erro.
- [X] T035 [P] [US1] Criar `frontend/src/app/dashboard/winners-by-year-panel/`: `h2` "Vencedores por ano", campo
  "Ano" (formulário reativo com o validador do T030) e botão "Buscar" desabilitado com valor inválido; antes da
  primeira busca, só o formulário; tabela Ano, Título, Estúdios ("—" quando nulo), Produtores; vazio "Nenhum
  vencedor em {ano}.". Testes: inválido não chama a API; válido chama `winnersOf`; vazio com o ano na mensagem;
  erro.
- [X] T036 [US1] Criar `frontend/src/app/dashboard/dashboard-page.ts` com o `h1` "Painel" e os quatro painéis
  (FR-007), cada um com o seu estado independente (a falha de um não esconde os outros; teste unitário com um
  painel em erro e os outros com dados).
- [X] T037 [US3] Estilos globais em `frontend/src/styles.css` e nos componentes (research R3): variáveis de cor com
  contraste AA, `:focus-visible` em todos os controles, `overflow-wrap: anywhere` nos textos de tabela; abaixo de
  768 px, painéis empilhados e linhas de tabela como cartões (`display: block` e `content: attr(data-label)` nas
  células); a partir de 768 px, grade de duas colunas no Dashboard. Todas as células com `data-label` igual ao
  cabeçalho (FR-015 e FR-016).
- [X] T038 [US1] Rodar `npm run lint`, `npm test -- --watch=false --coverage` (≥ 80% de linhas) e `npm run build` em
  `frontend/`, e informar o resultado real. Conferência rápida: `./mvnw spring-boot:run` e `npm start`, e
  `curl -s localhost:4200/api/aggregations/years-with-multiple-winners` pelo proxy, para confirmar o repasse.
  Parar os dois processos.
- [X] T039 [US1] Commit da etapa 3: "Marco 6, etapa 3: projeto Angular 22.3 e Dashboard".

---

## Fase 5: Etapa 4 — Lista de filmes (US2) 📋

**Objetivo**: a lista paginada, com filtros por ano e por vencedor guardados na URL.

**Teste independente**: os três comandos do front verdes, com os testes unitários da lista cobrindo a URL, os
filtros e a paginação.

- [X] T040 [P] [US2] Criar `frontend/src/app/movies/movie-filter.ts`: leitura dos parâmetros `ano`, `vencedor` e
  `pagina` da URL conforme contracts/frontend-ui.md ("`ano`: inteiro não negativo"; "`vencedor`: `sim`, `nao`";
  "`pagina`: inteiro ≥ 1"; inválido ou ausente → sem filtro, todos, 1) e a escrita de volta (filtro novo remove
  `pagina`). Testes com valores válidos, inválidos (`ano=abc`, `ano=-1`, `vencedor=talvez`, `pagina=0`,
  `pagina=x`) e ausentes.
- [X] T041 [P] [US2] Criar `frontend/src/app/shared/paginator/`: "Primeira", "Anterior", "Próxima" e "Última", com
  "Página {pagina} de {totalPaginas}"; "Primeira" e "Anterior" desabilitados na página 1, "Próxima" e "Última" na
  última (FR-012). Testes das bordas e do evento de página.
- [X] T042 [US2] Criar `frontend/src/app/movies/movie-list-page.ts` (substitui o provisório do T031): `h1` "Filmes";
  filtros "Ano" (validador do T030, aplicado com "Filtrar" ou Enter) e "Vencedor" (Todos, Sim, Não, aplicado ao
  mudar); a URL é a fonte do estado (`ActivatedRoute.queryParamMap` → `movies(filtro)`); tabela no desktop e cartões
  no celular com Ano, Título, Estúdios ("—" se nulo), Produtores e Vencedor ("Sim"/"Não"); resumo "{totalItens}
  filmes"; vazio "Nenhum filme encontrado." com o link "Voltar à primeira página" quando `pagina > 1`; erro com
  "Tentar de novo" (FR-010 a FR-014). Testes: URL com filtro gera a chamada certa; mudar filtro volta à página 1;
  ano inválido não chama a API; vazio com e sem o link; erro.
- [X] T043 [US2] Rodar os três comandos do front e informar o resultado real (cobertura ≥ 80%).
- [X] T044 [US2] Commit da etapa 4: "Marco 6, etapa 4: lista de filmes com filtros na URL".

---

## Fase 6: Etapa 5 — Imagem, ponta a ponta e pipeline do PR (US3 e US5) 🐳

**Objetivo**: a imagem nginx do front e os testes de ponta a ponta rodam no pipeline de todo PR.

**Teste independente**: no pipeline do PR, os jobs `frontend` e `web` verdes. Localmente, só as conferências rápidas
(`bash -n`, YAML válido, `check-workflows.sh`).

- [X] T045 [P] [US5] Criar `frontend/Dockerfile` e `frontend/.dockerignore` conforme contracts/web-image.md
  ("Construção"): `node:26-alpine@sha256:<digest>` com `npm ci` e `npm run build`;
  `nginxinc/nginx-unprivileged:1.29-alpine@sha256:<digest>` (trocada depois pela `nginx:1.31-alpine-slim` oficial, ajustada para rodar sem root; PR #16) com o `dist/frontend/browser` em
  `/usr/share/nginx/html`; `NGINX_ENVSUBST_FILTER` restrito a `PORT`, `API_URL` e `LOG_FORMAT`; `ENV PORT=8080`
  e `ENV LOG_FORMAT=json` (log de acesso em JSON no container, FR-026a); `EXPOSE 8080`;
  sem `USER root` na etapa final. Os digests vêm de `docker buildx imagetools inspect` ou da API do Docker Hub,
  com a tag no comentário. `.dockerignore` em lista de permissão (`*` e depois `!src/`, `!public/`,
  `!package.json`, `!package-lock.json`, `!angular.json`, `!tsconfig*.json`, `!nginx/`).
- [X] T046 [P] [US5] Criar `frontend/nginx/default.conf.template` e `frontend/nginx/10-require-api-url.sh` (copiado
  para `/docker-entrypoint.d/`) conforme contracts/web-image.md ("Rotas servidas" e "Cabeçalhos"): `listen
  ${PORT}`; `/health`; `location /api/` com `resolver` lido do `/etc/resolv.conf` (gerado por um script de
  entrypoint), `proxy_pass` por variável, `proxy_ssl_server_name on`, `Host` do destino e `proxy_read_timeout
  60s`; `try_files $uri $uri/ /index.html`; os cabeçalhos de segurança e de cache; `server_tokens off`; gzip. O
  script termina com "API_URL não definida" e código 1 sem a variável, e com "LOG_FORMAT deve ser json ou main" e
  código 1 para outro valor. Log conforme contracts/web-image.md ("Log"): `log_format json escape=json` com os dez
  campos (`time`, `method`, `path`, `query`, `status`, `bytes`, `duration`, `upstream_duration`, `user_agent`,
  `remote_addr`; `status`, `bytes` e `duration` sem aspas, como números), `access_log /dev/stdout ${LOG_FORMAT};`,
  `access_log off` no `/health`, e nenhum cabeçalho `Authorization` ou `Cookie` no formato.
- [X] T047 [P] [US5] Criar `.github/scripts/web-smoke-test.sh <imagem> <api_url>` com os 10 passos de
  contracts/web-image.md ("Teste de fumaça"), no estilo do `smoke-test.sh` (resultado por passo e resumo), lendo a
  `REFERENCE` do próprio `smoke-test.sh` em vez de copiá-la. Os passos 9 e 10 conferem o formato do log: cada linha
  de stdout do `docker logs` passa por `node -e` com `JSON.parse`, com os dez campos presentes, `status` e `bytes`
  numéricos, a linha do `/api/producers/intervals` com 200, nenhuma linha do `/health` e nenhum `Authorization`
  (o passo 4 envia um `Authorization: Bearer teste` para provar que ele não aparece no log); com `LOG_FORMAT=main`,
  a linha não é JSON; com `LOG_FORMAT=xml`, a subida falha. Conferir `bash -n`.
- [X] T048 [P] [US1] Criar `frontend/playwright.config.ts` (projetos `desktop` 1280×800 e `celular` 360×740 com
  toque; `baseURL` de `E2E_BASE_URL`, padrão `http://127.0.0.1:8081`; uma nova tentativa só com `CI`; trace e
  relatório guardados na falha) e os cenários em `frontend/e2e/`:
  - `dashboard.spec.ts`: valores de referência dos quatro painéis (US1-1 a US1-4, SC-002); busca por 1979 (US1-5);
    `abc` com a mensagem e o botão desabilitado (US1-6);
  - `movies.spec.ts`: total igual ao número de linhas de filmes do arquivo fornecido (206), paginação, filtro 1990 +
    "Sim" com os dois títulos (US2-1 a US2-3); URL `/filmes?ano=1990&vencedor=sim` aberta direto e recarregada
    (US2-6 e US5-5);
  - `layout.spec.ts`: no projeto `celular`, `document.documentElement.scrollWidth <= innerWidth` nas duas telas
    (US3-1, SC-004), com os painéis empilhados (cada um com a largura do conteúdo) e a lista sem cabeçalho de
    tabela visível (cartões); no projeto `desktop`, os dois primeiros painéis do Dashboard lado a lado (mesmo `y`
    do `boundingBox` e `x` diferentes) e a lista com o `thead` visível (US3-2, FR-015); navegação por Tab até
    "Buscar" e "Próxima" (US3-3).
  Sem `page.route` nem respostas simuladas: regra `no-restricted-syntax` no `eslint.config.js` para
  `e2e/**/*.ts` (research R5). Scripts `e2e` no `package.json`.
- [X] T049 [US5] Alterar `.github/workflows/ci.yml` conforme contracts/ci-frontend.md:
  - job novo `frontend` ("Front-end: lint, unitários, build e auditoria"), com `actions/setup-node` fixado por hash
    de commit (versão estável atual, conferida no repositório da action) e `node-version-file: frontend/.nvmrc`;
  - `dockerfile-lint` com um segundo passo do hadolint para `frontend/Dockerfile`;
  - job novo `web` ("Imagem do front, fumaça e ponta a ponta"), com `needs: dockerfile-lint`, as duas saídas do
    Buildx, o passo de digest copiado do job `image` (trocando `awards` por `awards-web`), a API pelo JAR, o
    `web-smoke-test.sh`, o Playwright, o Trivy (relatório e porta) e o artefato `awards-web-oci` só em push na
    `main`; os relatórios do Playwright como artefato na falha;
  - job `workflow-policy` com o passo novo `.github/scripts/check-unchanged-tests.sh v0.5.0` ("Testes de
    integração dos Marcos 1 a 5 sem alteração desde a v0.5.0"), depois dos passos da `v0.2.0` e da `v0.3.0`
    (FR-005; contracts/ci-frontend.md, "Job `workflow-policy`"). Conferência local rápida:
    `.github/scripts/check-unchanged-tests.sh v0.5.0` precisa aprovar com o `ApiDocsIT` e o `ApiDocsSecurityIT`
    alterados (exceções existentes).
  Conferir o YAML com `npx --yes js-yaml@4.1.0 .github/workflows/ci.yml`.
- [X] T050 [US5] Alterar `.github/scripts/check-workflows.sh`:
  - `REQUIRED_CHECKS=(workflow-policy build postgres codeql dependencies dockerfile-lint image alert-rules load
    compose frontend web)`, com o comentário da regra 9 citando a spec 006;
  - a regra 1 (todo `uses:` por hash de 40 caracteres) já cobre o `setup-node`: não há lista de actions
    permitidas, então nada muda nela;
  - a regra 6 (Dependabot) ganha a conferência dos ecossistemas `npm` e `docker` com `directory "/frontend"` e
    `schedule.interval`, sem mudar a dos ecossistemas da raiz.
  Atualizar no mesmo commit os `needs` do job `deploy` com os 12 checks. **Dependência:** com os `needs` em 12, o
  merge na `main` só publica depois que o Anderson incluir `frontend` e `web` na proteção da `main` (T068); a
  descrição do PR cita isso. Rodar o script num contêiner (`docker run --rm -v "$PWD":/repo -w /repo
  alpine:3.22 sh -c 'apk add --no-cache bash yq >/dev/null && bash .github/scripts/check-workflows.sh'`) e
  confirmar que aprova. (O `deploy-web` entra na etapa 6, T057.)
- [X] T051 [P] [US5] Acrescentar ao `.github/dependabot.yml` o ecossistema `npm` no diretório `/frontend` e o
  `docker` no diretório `/frontend`, com a mesma frequência e o mesmo agrupamento dos existentes.
- [X] T052 [US5] Rodar os três comandos do front (os cenários de `e2e/` ficam fora do Vitest; conferir que o `npm
  test` não os pega) e o `check-workflows.sh`. Não construir a imagem nem rodar o Playwright localmente: o job `web`
  comprova (FR-023, FR-025 e FR-026).
- [X] T053 [US5] Commit da etapa 5: "Marco 6, etapa 5: imagem nginx do front, ponta a ponta e jobs frontend e web".

---

## Fase 7: Etapa 6 — GCP e publicação do `awards-web` (US5) ☁️

**Objetivo**: cada merge na `main` publica o front no `awards-web`, sem segredos, com a revisão conferida antes do
tráfego.

**Teste independente**: duas execuções seguidas do `setup.sh`, a segunda sem mudanças; `check-workflows.sh`
aprovando o workflow e reprovando os casos negativos. A publicação real vem com o merge.

- [X] T054 [US5] Estender `deploy/gcp/setup.sh` conforme contracts/ci-frontend.md ("`deploy/gcp/setup.sh`
  (alterado)") e data-model ("Recursos novos no GCP" e "Vínculos de IAM novos"): constantes `WEB_SERVICE=awards-web`
  e `WEB_RUNTIME_SA=awards-web-runtime`; os passos 1 a 6 com `run_step`, `ensure_sa` e `ensure_binding`; o motivo de
  cada vínculo num comentário; a saída com `GCP_WEB_SERVICE`, `GCP_WEB_RUNTIME_SA` e `WEB_URL`. Conferir `bash -n`.
- [X] T055 [US5] `source ~/.bashrc` e rodar `deploy/gcp/setup.sh` duas vezes. A primeira cria os recursos novos
  (`criado`); a segunda termina com código 0 e tudo em `OK (já existia)`. Guardar as duas saídas em
  `specs/006-frontend-angular/evidencias/setup-execucoes.txt` (sem e-mail nem dados pessoais) e anotar a URL do
  `awards-web`. Conferir que `gcloud iam service-accounts keys list --iam-account=awards-web-runtime@outsera-awards.iam.gserviceaccount.com --managed-by=user`
  vem vazio. Se a primeira execução falhar, parar e informar.
- [X] T056 [P] [US5] Criar `.github/scripts/verify-web.sh <url>` com os 5 passos de contracts/ci-frontend.md
  ("`verify-web.sh <url>`"), com novas tentativas até 120 s, lendo a `REFERENCE` do `smoke-test.sh`. Conferência
  rápida contra a URL do `awards-web` com a imagem de exemplo: precisa **reprovar** no passo 2 (não é o front). O
  caso que aprova é comprovado pelo `deploy-web` depois do merge.
- [X] T057 [US5] Acrescentar ao `.github/workflows/ci.yml` o job `deploy-web` ("Publicação do front no Cloud Run e
  conferência"), conforme contracts/ci-frontend.md ("Job `deploy-web`"), com os identificadores públicos do T055 e os
  mesmos hashes de `auth`, `setup-gcloud` e `download-artifact` do `deploy`; e alterar o `check-workflows.sh`: as
  regras de escrita aceitam `deploy` e `deploy-web`, a regra de não rebuild vale para os dois, e a regra 9 confere os
  `needs` dos dois. Rodar o `check-workflows.sh` (aprova) e os cinco casos negativos do contrato ("Política do
  workflow") numa cópia temporária do repositório no scratchpad, com o mesmo contêiner alpine do T050 (todos reprovam). Conferir o YAML com o `js-yaml`.
- [X] T058 [US5] Commit da etapa 6: "Marco 6, etapa 6: serviço awards-web e job deploy-web".

---

## Fase 8: Etapa 7 — Documentação (US6) 📝

**Objetivo**: o avaliador encontra a URL do front e entende o Marco 6 sem ler o código.

**Teste independente**: revisão no PR, com os itens novos em `checklists/revisao.md`.

- [X] T059 [P] [US6] `README.md`: URL pública do front em "Avalie em 2 minutos", com o aviso da partida a frio dos
  dois serviços; seção do Marco 6 entre os extras (as duas telas, as agregações com exemplo de resposta, como rodar o
  front com `npm start` contra o `./mvnw spring-boot:run`, os testes do front, a imagem e a publicação, e o que
  fica de fora: compose, login no front, SSR); os requisitos do Marco 6 na seção de requisitos, com a comprovação
  de cada um (FR-035) (FR-032).
- [X] T060 [P] [US6] `frontend/README.md` curto: pré-requisitos (Node 26), `npm ci`, `npm start`, `npm run lint`,
  `npm test`, `npm run build`, `npm run e2e` (com a API e a imagem no ar) e o link para a seção do README.
- [X] T061 [P] [US6] `docs/adr/0028-front-end-angular-com-nginx.md` (contexto, decisão, consequências), no formato
  dos ADRs existentes: Angular em pasta própria e fora do `./mvnw`; nginx com repasse de `/api` e mesma origem, sem
  CORS; segundo serviço do Cloud Run com 0 a 1 instância; testes unitários e de ponta a ponta; log de acesso em
  JSON pela variável `LOG_FORMAT` da imagem e o limite do `error_log` do nginx, que fica em texto (FR-026a e
  FR-033). Atualizar o índice em `docs/adr/README.md`.
- [X] T062 [P] [US6] `docs/runbook.md`: voltar o tráfego do `awards-web` para a revisão anterior; o que fazer quando
  o front sobe mas o `/api` dá 502 ou 504 (conferir o `API_URL` da revisão e o `awards`).
- [X] T063 [P] [US6] `docs/ai/decisoes.md`: uma entrada por desvio do briefing (FR-034): publicação no Cloud Run em
  vez de hospedagem estática; mesma origem em vez de CORS por variável; front fora do compose (FR-031); jobs do front
  no mesmo workflow em vez de pipeline próprio (research R9); e a versão do Angular (research R1).
- [X] T064 [US6] Criar `specs/006-frontend-angular/checklists/revisao.md`, no formato do
  `specs/005-deploy-cloud-run/checklists/revisao.md`, com um item por requisito de documentação e de governança
  (FR-024, FR-031 a FR-034) dizendo o que o revisor confere, e o item de **usabilidade (SC-003)** com os
  critérios objetivos da spec, conferidos na URL do front ou no `npm start`, sem ferramenta nova:
  - DevTools em 360 px: nenhuma rolagem horizontal no Painel e em Filmes;
  - só o teclado (Tab, Shift+Tab, Enter, Espaço): menu, busca por ano, filtros e paginação, com o foco visível em
    cada controle;
  - contraste no inspetor de cores do navegador: pelo menos 4,5:1 nos textos e 3:1 nos controles e no contorno
    de foco;
  - vencedores de 1990 em até 3 interações (digitar o ano, buscar, ler), no Painel e em Filmes.
- [X] T065 [US6] Commit da etapa 7: "Marco 6, etapa 7: README, ADR 0028, runbook e decisões".

---

## Fase 9: Pull request, checks e comprovação na `main`

**Objetivo**: o pipeline do PR verde e, depois do merge, a publicação da API e do front conferida.

- [X] T066 Rodada final antes do push: `./mvnw verify`, `./mvnw -Pextras verify` e os três comandos do front, todos
  verdes; informar o resultado real de cada um. Conferir `git status` limpo (a não ser pelo `docs/ai/prompts.md`).
- [ ] T067 **[Anderson]** Push do branch `marco/6-frontend` e abertura do PR. O implementador para aqui e pede.
- [ ] T068 **[Anderson]** Incluir `frontend` e `web` nos checks obrigatórios da proteção da `main` (de 10 para 12),
  antes do merge (quickstart, passo 0). O implementador para aqui e pede.
- [ ] T069 Acompanhar o pipeline do PR: os 12 checks verdes, `deploy` e `deploy-web` pulados. Se algum falhar,
  corrigir no branch, informar e voltar ao T066.
- [ ] T070 Revisão do Codex no PR: corrigir só o que o Anderson aceitar, registrando aceitas e rejeitadas em
  `docs/ai/decisoes.md`, com um commit por assunto.
- [ ] T071 Depois do merge (feito pelo Anderson): conferir a execução da `main` (`deploy` e `deploy-web` verdes),
  rodar `.github/scripts/verify-web.sh <URL do awards-web>` e registrar o resultado em
  `specs/006-frontend-angular/evidencias/publicacao-main.txt`, com o link da execução, a revisão e o digest. Para o
  SC-001: esperar os dois serviços ficarem sem instância (cerca de 15 minutos sem chamadas), rodar o
  `verify-web.sh` e registrar o `tempo_primeira_resposta` (meta ≤ 30 s, partida a frio); rodar de novo logo
  depois e registrar o tempo (meta ≤ 3 s, instâncias ativas). Para o SC-007: registrar a saída de
  `gcloud run services describe awards-web --region=southamerica-east1 --format='value(spec.template.metadata.annotations)'`
  com `autoscaling.knative.dev/maxScale: 1` e sem `minScale` maior que 0. Se alguma meta não for atingida, informar
  ao Anderson em vez de marcar a tarefa.
- [ ] T072 Marcar a spec como `Implemented` com o link da execução, como na spec 005, e exportar a sessão para
  `docs/ai/sessoes/`.

---

## Dependências e ordem de execução

- **Fase 1 → Fase 2 (emenda)** antes de tudo (Princípio IV).
- **Etapa 2 (US4)** antes das etapas 3 e 4: os painéis de anos e estúdios dependem dos endpoints.
- **Etapa 3** (projeto do front, `AwardsApi`, `PanelState`, validador e layout) antes da **etapa 4** (a lista usa
  esses pedaços).
- **Etapa 5** depende das etapas 3 e 4 (o Playwright cobre as duas telas).
- **Etapa 6** depende da 5 (o `deploy-web` usa o artefato do job `web`).
- **Etapa 7** pode começar depois da 6 (a URL do `awards-web` sai do T055).
- **Fase 9** no fim.

### Histórias

| História | Fases | Depende de |
| --- | --- | --- |
| US4 — agregações (P1) | 3 | emenda |
| US1 — Dashboard (P1) | 4 e 6 (e2e) | US4 |
| US2 — Lista (P1) | 5 e 6 (e2e) | etapa 3 |
| US3 — responsivo (P2) | 4 (CSS) e 6 (e2e) | US1 e US2 |
| US5 — publicação (P2) | 6 e 7 | US1 a US3 |
| US6 — README (P3) | 8 | US5 (URL) |

## Oportunidades de paralelismo

- **Etapa 1:** T003 e T004 depois do T002.
- **Etapa 2:** os ITs T008 a T015 entre si (arquivos diferentes), depois do T006 e do T007; T019 e T020 entre si.
- **Etapa 3:** T028, T029 e T030 entre si; depois, os quatro painéis T032 a T035 entre si.
- **Etapa 4:** T040 e T041 entre si.
- **Etapa 5:** T045, T046, T047, T048 e T051 entre si.
- **Etapa 7:** T059 a T063 entre si.

Exemplo (etapa 3, depois do T031):

```text
T032 multiple-winners-panel   T033 top-studios-panel
T034 producer-intervals-panel T035 winners-by-year-panel
```

## Estratégia de implementação

- **MVP:** etapas 1 a 3. A API com as agregações e o Dashboard rodando localmente já mostram o valor do marco
  (US4 e US1).
- **Incrementos:** a lista (etapa 4), a imagem e os testes de ponta a ponta no PR (etapa 5), a publicação (etapa
  6) e a documentação (etapa 7), cada um com o seu commit e os testes da etapa verdes.
- **Um extra só entra completo:** o PR só é aberto com as sete etapas prontas. O Marco 6 só é dado como entregue
  depois da comprovação na `main` (T071).

---

## Fase 10: Testador adversário do front e das agregações

**Objetivo**: os 27 casos do testador adversário (Codex, fora do repositório) viram testes permanentes, com as
8 lacunas decididas pelo responsável aplicadas na spec antes dos testes.

- [X] T073 Aplicar as decisões dos casos 03, 05, 07, 08, 13, 14, 18 e 24 na spec (FR-002, FR-008, FR-008a, FR-013,
  FR-017a, casos de borda, premissas e esclarecimentos) e no `contracts/frontend-ui.md`.
- [X] T074 [P] ITs da API em `src/test/java/com/outsera/awards/aggregations/adversario/` (casos 14 a 20 e 27), com
  os CSVs em `src/test/resources/scenarios/adversario-front/` e a seção no `src/test/default-mode-its.txt`.
- [X] T075 [P] Testes unitários em `frontend/src/app/adversario.spec.ts` (casos 01, 03 a 08 e 10 a 24).
- [X] T076 [P] Ponta a ponta em `frontend/e2e/adversario.spec.ts` (casos 01 a 09, 11, 12, 25 e 26), com a segunda
  API e o segundo front do caso 26 no job `web` do `.github/workflows/ci.yml`.
- [X] T077 Rodar os testes novos antes de corrigir: só o caso 03 falhou. Corrigir só ele
  (`frontend/src/app/movies/movie-filter.ts` e `frontend/src/app/api/awards-api.ts`) e rodar de novo.
- [X] T078 Registrar os 27 casos, com o resultado de cada um, em `docs/ai/decisoes.md`, e a linha do README nos
  requisitos do Marco 6.
