# Research: Marco 6 — Front-end em Angular

Decisões técnicas do plano. Cada uma tem a decisão, o motivo e as alternativas recusadas. As referências a
requisitos são da [spec](spec.md).

## R1. Versões: Angular 22.3, Node 26 LTS (FR-019)

- **Decisão:** Angular **22.3.0** (`@angular/core` e `@angular/cli` com a tag `latest` do npm, publicada em
  2026-11-18), TypeScript na faixa que o `@angular/build` 22.3 exige (`>=6.0 <6.1`) e **Node 26** (LTS ativo
  desde outubro de 2026), que o `@angular/cli` 22.3 aceita (`^22.22.3 || ^24.15.0 || >=26.0.0`). As versões
  ficam exatas no `package.json` (sem `^` nem `~` nos pacotes do Angular) e o `package-lock.json` é versionado.
  O pipeline instala com `npm ci`. A versão do Node fica no `frontend/.nvmrc` e no `engines` do `package.json`.
- **Motivo:** o pedido é "a versão estável mais recente". A 22.3.0 é a `latest` do npm na data do plano; a
  linha 23 ainda não saiu. O Node 26 é o LTS ativo; o 24 já está em manutenção.
- **Alternativas recusadas:**
  - Angular 21 (LTS do Angular): não é a mais recente.
  - Node 24: aceito pelo Angular, mas já em manutenção; o 26 tem suporte até 2029.
  - Faixas com `^`: o lockfile já fixa, mas versões exatas deixam a escolha visível no `package.json` e
    tornam as atualizações do Dependabot explícitas.
- **Atualização:** o Dependabot ganha o ecossistema `npm` no diretório `/frontend`, como o Maven e as actions.

## R2. Estrutura do app: componentes standalone, signals e zoneless (FR-007 a FR-018)

- **Decisão:** app criado com `ng new` (padrão da 22: standalone, sem `NgModule`, detecção de mudanças
  zoneless), roteamento com duas rotas (`/` Dashboard e `/filmes` Lista) e um curinga que redireciona para `/`.
  O estado de cada painel é um `signal` com os quatro estados (carregando, dados, vazio, erro). As chamadas HTTP
  ficam em um serviço (`AwardsApi`) com `HttpClient` e caminhos relativos (`/api/...`; FR-018). Sem SSR, sem
  PWA, sem biblioteca de estado.
- **Componentes:**
  - `DashboardPage` com quatro painéis: `MultipleWinnersPanel`, `TopStudiosPanel`, `ProducerIntervalsPanel` e
    `WinnersByYearPanel`;
  - `MovieListPage`, com o formulário de filtros, a tabela ou cartões e a paginação (`Paginator`);
  - `PanelState`, um componente de apresentação para carregando, vazio e erro com "tentar de novo" (FR-009 e
    FR-014), reaproveitado pelos painéis e pela lista.
- **Filtros na URL (FR-013):** os parâmetros `ano`, `vencedor` (`sim` ou `nao`) e `pagina` (começando em 1, na
  interface) são lidos da rota e escritos com `router.navigate` com `queryParams`. A tradução para a API
  (`year`, `winner=true|false`, `page` começando em 0, `size=10`) fica no serviço. Um valor inválido na URL é
  descartado antes da chamada (spec, casos de borda).
- **Validação (FR-008 e FR-011):** formulários reativos com validador de inteiro não negativo (`^\d+$`). O botão
  de buscar fica desabilitado e a mensagem aparece abaixo do campo enquanto o valor é inválido.
- **Alternativas recusadas:**
  - Calcular as agregações no navegador a partir da listagem: exigiria várias páginas (máximo 100 por página)
    e duplicaria a regra de negócio fora da API (spec, US4).
  - NgRx ou outra biblioteca de estado: dois fluxos simples não justificam (Princípio V, complexidade sem
    necessidade).
  - Paginação com `page` começando em 0 na URL do front: a URL é lida por pessoas; a tradução fica num único
    lugar.

## R3. Visual e responsividade sem biblioteca de componentes (FR-015 a FR-017)

- **Decisão:** CSS próprio, com variáveis e uma grade CSS: um painel por linha abaixo de 768 px e grade de
  duas colunas acima disso. Tabelas semânticas (`table`, `th scope`) no desktop; abaixo de 768 px, cada linha
  vira um cartão por CSS (`display: block` e o rótulo de cada célula em `data-label`), sem duplicar o HTML.
  Foco visível com `:focus-visible`, contraste AA, `aria-live="polite"` nos estados de carregamento e erro, e
  `aria-current="page"` no menu (FR-016). Textos em português, `lang="pt-BR"` (FR-017).
- **Motivo:** quatro painéis e uma tabela não pedem biblioteca; CSS próprio mantém o bundle pequeno e sem
  dependências a atualizar.
- **Alternativas recusadas:** Angular Material (peso e tema para pouca coisa), Tailwind (passo de build a mais),
  Bootstrap (CSS global e JavaScript que não serão usados).

## R4. Testes unitários: Vitest, o padrão do Angular 22 (FR-022)

- **Decisão:** o construtor de testes do `@angular/build` com **Vitest** (padrão das apps novas desde a 21),
  ambiente `jsdom`, `HttpTestingController` para as chamadas e `TestBed` para os componentes. Cobertura com
  `@vitest/coverage-v8` e limite de **80% de linhas** na configuração (o comando falha abaixo disso). Comando:
  `npm test -- --watch=false --coverage`.
- **O que cobrem (FR-022):** a validação do ano nos dois formulários; os quatro estados de cada painel e da
  lista; o corte dos três estúdios; o mapeamento dos filtros entre a URL e a API, inclusive valores inválidos;
  a paginação (botões desabilitados na primeira e na última página); a tradução de `winner`; o "tentar de
  novo".
- **Alternativas recusadas:** Karma e Jasmine (Karma está descontinuado e não é mais o padrão), Jest
  (precisaria de configuração própria fora do construtor oficial).
- **Princípio I:** esses testes ficam só em `frontend/`. A API continua só com testes de integração.

## R5. Ponta a ponta: Playwright contra a aplicação real, no pipeline (FR-023)

- **Decisão:** **Playwright** (`@playwright/test` 1.65), com o Chromium baixado pelo próprio Playwright no job.
  Projetos: `desktop` (1280×800) e `celular` (360×740, toque). O alvo é a **imagem do front** (nginx) rodando no
  runner, com `/api` repassado à API real:
  1. a API sobe com `java -jar` do JAR construído no job (modo padrão, arquivo fornecido), na porta 8080;
  2. a imagem do front sobe com `--network host`, `PORT=8081` e `API_URL=http://127.0.0.1:8080`;
  3. o Playwright espera a readiness da API e roda contra `http://127.0.0.1:8081`.
- **Cenários (`frontend/e2e/`):** Dashboard com os valores de referência (US1, SC-002); busca por 1990 e por um
  ano sem vencedor; validação do campo; lista com o total, a paginação e o filtro 1990 + vencedores; link com
  filtros aberto direto e recarregado (US2-6, US5-5); ausência de rolagem horizontal no projeto `celular`
  (US3-1, SC-004); navegação por teclado até a busca e a paginação (US3-3). Nenhuma rota simulada
  (`page.route` é proibido nos testes; o lint confere).
- **Motivo:** testar a imagem que vai ser publicada, com o repasse de `/api` real, comprova também a FR-026
  (rotas internas, repasse e porta). Subir a API pelo JAR evita depender do job `image` e de Docker para a API.
- **Alternativas recusadas:**
  - Cypress: mais pesado no pipeline e com suporte a múltiplos navegadores e viewports menos direto.
  - `ng serve` com proxy no pipeline: não testaria o nginx que vai para o Cloud Run.
  - Rodar contra a URL publicada depois do deploy: a publicação já é conferida pelo script da R9; o teste de
    ponta a ponta precisa reprovar o PR antes do merge.

## R6. Agregações na API: JPQL portável e contagem de estúdios no serviço (FR-001 a FR-006)

- **Decisão:**
  - **Anos:** consulta JPQL no `MovieRepository`:
    `select m.year, count(m) from Movie m where m.winner = true group by m.year having count(m) > 1 order by m.year`.
    Portável entre H2 e PostgreSQL (Princípio V, SQL portável).
  - **Estúdios:** consulta JPQL dos campos `studios` dos vencedores (`where m.winner = true and m.studios is not
    null`). A separação por vírgula, o `strip`, o descarte de vazios e o "uma vez por filme" (um `Set` por
    filme) ficam num `StudioNameParser` do pacote novo; a contagem e a ordem (quantidade decrescente e depois
    `String.compareTo`, a mesma ordem de nome dos intervalos) ficam no `AggregationService`.
  - Pacote novo `com.outsera.awards.aggregation`: `AggregationController` (`/api/aggregations/...`),
    `AggregationService` (transação só de leitura), `StudioNameParser` e os records de resposta
    (`YearsWithMultipleWinners`, `YearWinnerCount`, `StudiosWithWinCount`, `StudioWinCount`).
  - **Sem cache:** com 206 filmes, as duas contas custam pouco e são sempre atuais, inclusive no modo Kafka,
    em que a base muda depois da subida. O cache do ADR 0026 continua só nos intervalos.
  - **ETag e autenticação:** os dois caminhos entram no `EtagConfiguration` e na lista `PROTECTED` do
    `SecurityConfiguration` (FR-003). Nenhuma outra classe existente muda.
- **Motivo:** o mesmo padrão dos endpoints existentes, com a regra de negócio num único lugar.
- **Alternativas recusadas:**
  - Tabela de estúdios normalizada no Flyway: mudaria o esquema e a gravação (o único caminho de gravação)
    para uma agregação de leitura; a separação em memória basta para o volume.
  - `?projection=` em `/api/movies`: mudaria o comportamento de um endpoint existente (FR-005).
  - Endpoint de "top 3" com corte na API: a spec mantém o corte no painel e a lista completa na API (casos de
    borda: empate na terceira posição).

## R7. ITs das agregações (FR-035, Princípio I)

- **Decisão:** ITs novos em `src/test/java/com/outsera/awards/aggregations/`, todos com `ScenarioIT`
  (`@SpringBootTest` completo, HTTP real, sem mocks) e validação pelo `ContractValidator` contra o contrato
  1.3.0:
  - `AggregationsReferenceIT` (arquivo fornecido): anos 1986, 1990 e 2015 com 2; os três primeiros estúdios e a
    lista completa contra um oráculo calculado no próprio teste a partir do CSV;
  - `YearsWithMultipleWinnersIT` (cenário): ano com 1, 2 e 3 vencedores; indicados não contam; ordem crescente;
  - `StudiosWinCountIT` (cenário): vírgula com e sem espaço, estúdio repetido no mesmo filme, estúdios vazios,
    empate ordenado pelo nome, maiúsculas distintas sem normalização;
  - `AggregationsEmptyIT` (cenário sem vencedores): listas vazias e 200;
  - `AggregationsEtagIT`: ETag presente e 304 com `If-None-Match`;
  - `AggregationsContractIT`: 200 dos dois endpoints válidos no contrato;
  - `AggregationsAuthIT` (extras, `TokenScenarioIT`): 401 em Problem Details sem token e 200 com o token;
  - `AggregationsPostgresIT` (perfil `postgres-it`): o resultado de referência com PostgreSQL, porque o
    `group by ... having` precisa ser portável (Princípio VI, comportamento dependente do PostgreSQL).
- Os seis primeiros entram em `src/test/default-mode-its.txt`, numa seção "Marco 6"; o de autenticação fica no
  `extras`; o do PostgreSQL, no `postgres-it`.
- **ITs existentes:** só o `ApiDocsIT` (caminho do contrato da spec 006) e o `ApiDocsSecurityIT` (versão 1.3.0)
  mudam, e os dois já estão no `.github/unchanged-tests-exceptions.txt`. O `docs/ai/decisoes.md` ganha a entrada
  do contrato 1.3.0; os títulos das exceções existentes continuam válidos. O `ContractTextIT` passa sem
  mudança: a descrição nova continua com duas frases, cita "CSV carregado na inicialização" e não tem `<` nem
  `>`.

## R8. Imagem do front: build com Node e nginx sem root (FR-025, FR-026)

- **Decisão:** `frontend/Dockerfile` multi-stage:
  1. `node:26-alpine` (por digest) roda `npm ci` e `ng build` (configuração de produção);
  2. `nginxinc/nginx-unprivileged:1.29-alpine` (por digest), que já roda como usuário não root (UID 101),
     recebe o `dist/` e um template `default.conf.template`. O `envsubst` do entrypoint oficial gera a
     configuração na subida a partir de `PORT` (padrão 8080), `API_URL` e `LOG_FORMAT` (`json`, definido no
     `Dockerfile`), restrito a essas três variáveis (`NGINX_ENVSUBST_FILTER`).
- **Log de acesso em JSON (FR-026a):** `log_format json escape=json` e `access_log /dev/stdout ${LOG_FORMAT}`,
  com o formato `json` ligado pela variável que a imagem define, como a constituição exige para container e
  ambiente publicado; sem a variável (`LOG_FORMAT=main`), o texto padrão. O `error_log` do nginx não aceita
  formato e fica em texto. As alternativas recusadas foram um sidecar ou o `njs` para reformatar o log, que são
  complexidade sem necessidade para um servidor de arquivos estáticos.
- **Configuração do nginx:**
  - `location /api/`: `proxy_pass` para `API_URL`, com `proxy_ssl_server_name on` e o `Host` do destino (o
    Cloud Run roteia pelo `Host`); `resolver` para o DNS ser resolvido em tempo de execução; `If-None-Match` e
    `ETag` passam sem mudança; tempo limite de 60 s, para tolerar a partida a frio do `awards`;
  - `location /`: `try_files $uri $uri/ /index.html` (rotas internas; US5-5);
  - `index.html` com `Cache-Control: no-cache`; arquivos com hash no nome com cache de um ano e `immutable`;
  - cabeçalhos de segurança: `Content-Security-Policy` restrita a `'self'`, `X-Content-Type-Options`,
    `Referrer-Policy` e `X-Frame-Options: DENY`; `server_tokens off`; gzip;
  - `location = /healthz` devolve 200 sem acessar a API (probe do Cloud Run e teste de fumaça).
- **Contexto do build:** `frontend/` com `.dockerignore` em lista de permissão (como o da raiz). O
  `.dockerignore` da raiz já exclui `frontend/` da imagem da API, então ela não muda.
- **Alternativas recusadas:**
  - Servir o Angular pelo Spring Boot (dentro do JAR): poria o Node no `./mvnw` e misturaria os builds
    (FR-020; decisão de planejamento "front em pasta própria").
  - Imagem distroless com Caddy ou servidor em Node: o pedido é nginx; a imagem `unprivileged` resolve o não
    root sem ajustes.
  - CORS na API em vez do repasse: duas origens, preflight e configuração por variável a mais; o repasse
    mantém a mesma origem (FR-018; desvio do briefing registrado, FR-034).

## R9. Pipeline: jobs `frontend` e `web`, e publicação `deploy-web` (FR-025 a FR-030)

- **Decisão:**
  - **`frontend`** (todo push e PR): `actions/setup-node` por hash, Node da `.nvmrc`, `npm ci`, `ng lint`
    (angular-eslint), testes unitários com cobertura mínima de 80%, `ng build` de produção e `npm audit
    --audit-level=high --omit=dev`.
  - **`dockerfile-lint`**: ganha o `frontend/Dockerfile` no mesmo hadolint.
  - **`web`** (todo push e PR; depende de `dockerfile-lint`): build da imagem do front com as mesmas duas saídas
    do job `image` (Docker e tar OCI, digest como saída; research R1 do Marco 5); JDK 25 e `./mvnw -B -DskipTests
    package` para o JAR da API; teste de fumaça da imagem do front (`web-smoke-test.sh`: `/healthz`, `/`, rota
    interna, repasse de `/api/producers/intervals` com a `REFERENCE` do `smoke-test.sh`, cabeçalhos de segurança
    e usuário não root); Playwright nos dois projetos; Trivy com a mesma porta da imagem da API; artefato
    `awards-web-oci` só em push na `main`.
  - **`deploy-web`** (só push na `main` deste repositório): `needs` com os checks obrigatórios e o `deploy`;
    mesma identidade federada (`auth` e `setup-gcloud` já fixados), mesma concorrência sem cancelamento
    (grupo próprio `deploy-cloud-run-web`); envio do tar ao Artifact Registry com `skopeo --preserve-digests`
    como `awards-web:<sha>`; `gcloud run deploy awards-web` com `--no-traffic --tag`, `API_URL` da URL pública do
    `awards`, `--min-instances=0 --max-instances=1 --cpu=1 --memory=256Mi`, startup probe em `/healthz`;
    conferência pela URL da tag (`verify-web.sh`), tráfego, conferência pela URL pública e resumo.
- **Checks obrigatórios:** a lista passa de 10 para **12** (`frontend` e `web`). O `REQUIRED_CHECKS` do
  `check-workflows.sh` e os `needs` dos dois jobs de publicação mudam juntos, e o Anderson atualiza a proteção da
  `main` (passo dele, registrado no quickstart).
- **Política do workflow (`check-workflows.sh`):** as regras de escrita (`id-token: write`, publicação no
  registro, `gcloud run`) passam a aceitar exatamente os jobs `deploy` e `deploy-web`; a regra de não rebuild
  vale para os dois; `secrets.` continua proibido em todo o arquivo. Os casos negativos do contrato
  `workflow-policy.md` do Marco 5 ganham os equivalentes do `deploy-web`.
- **Alternativas recusadas:**
  - Workflow separado para o front: o briefing fala em "pipeline próprio", mas um segundo workflow duplicaria a
    política, a concorrência e os `needs` da publicação; jobs próprios no mesmo workflow dão o mesmo
    isolamento (desvio registrado em `docs/ai/decisoes.md`, FR-034).
  - Publicar o front antes da API: o front novo poderia chamar um endpoint de agregação que a API publicada
    ainda não tem.
  - Front e API num só serviço: o pedido é um segundo serviço.

## R10. GCP: extensão idempotente do `setup.sh` (FR-027, FR-028)

- **Decisão:** o `deploy/gcp/setup.sh` ganha, com o mesmo padrão de convergência (`run_step`, `ensure_sa`,
  `ensure_binding`):
  - a conta de execução `awards-web-runtime`, **sem nenhum papel** no projeto (o nginx só faz chamadas HTTP
    públicas);
  - o serviço `awards-web` criado com a imagem de exemplo, `min-instances=0`, `max-instances=1` e a conta de
    execução própria (research R4 do Marco 5: o serviço existe antes, para as permissões ficarem no nível dele);
  - `roles/run.developer` da conta `github-deployer` **no serviço `awards-web`**;
  - `roles/iam.serviceAccountUser` da conta `github-deployer` **na conta `awards-web-runtime`**;
  - `roles/run.invoker` para `allUsers` **no serviço `awards-web`**;
  - os identificadores novos na saída (`GCP_WEB_SERVICE`, `GCP_WEB_RUNTIME_SA`).
  O repositório de imagens, o pool, o provedor e a conta de publicação são os mesmos. O registro guarda a imagem
  `awards-web` no mesmo repositório `awards`; a política de limpeza vale por pacote, então as 10 mais recentes
  de cada imagem ficam.
- **Execução:** o Claude Code roda o script duas vezes depois do login do Anderson; a segunda execução fica sem
  mudanças (SC-004 do Marco 5 continua valendo).
- **Alternativas recusadas:** reaproveitar a `awards-runtime` (mistura as identidades dos dois serviços);
  repositório de imagens separado (sem ganho, mais um recurso e mais um vínculo).

## R11. Emenda constitucional 2.1.0 (FR-024)

- **Decisão:** MINOR, porque amplia a orientação sem remover nada:
  - **Princípio II:** o front fica fora do `./mvnw`; nenhum comando Maven pode exigir Node ou construir o front;
  - **Princípio VI:** novo tipo de comprovação, **comportamento do front-end**: testes unitários (com cobertura
    mínima) e teste de ponta a ponta contra a aplicação real, no pipeline, com o job vermelho reprovando o merge.
    Um requisito da API continua exigindo IT;
  - **Princípio I:** sem mudança de regra; a frase final passa a citar o Princípio VI para o front;
  - **Portão de merge:** inclui os jobs do front.
- **Ordem:** a emenda é o primeiro commit do bolt, antes do código (Princípio IV), com o `AGENTS.md` alinhado
  (regras invioláveis, testes, stack e pontos em aberto, que perdem a versão do Angular) e a entrada em
  `docs/ai/decisoes.md`.
