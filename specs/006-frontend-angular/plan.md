# Plano de implementação: Marco 6 — Front-end em Angular, com painel e lista de filmes

**Branch**: `marco/6-frontend` | **Data**: 2026-11-26 | **Spec**: [spec.md](spec.md)

**Entrada**: especificação em `specs/006-frontend-angular/spec.md`

## Resumo

O marco entrega um front em Angular 22.3, em `frontend/`, com duas telas: o **Dashboard**, com quatro painéis, e
a **Lista de filmes**, paginada e com filtros na URL. A API ganha duas agregações de leitura, e o front é
publicado num segundo serviço do Cloud Run.

- **API:** `GET /api/aggregations/years-with-multiple-winners` e
  `GET /api/aggregations/studios-with-win-count`, num pacote novo, com o mesmo padrão dos endpoints existentes
  (ETag, autenticação opcional, Problem Details). O contrato passa a 1.3.0 e os endpoints existentes não mudam
  (research R6 e R7).
- **Front:** componentes standalone e signals, CSS próprio e responsivo, chamadas por caminho relativo
  `/api` (research R2 e R3). Testes unitários com Vitest (cobertura mínima de 80%) e de ponta a ponta com
  Playwright contra a API real e a imagem do front (research R4 e R5).
- **Imagem e publicação:** nginx sem root serve o Angular e repassa `/api` ao serviço `awards`, sem CORS
  (research R8). O pipeline ganha os jobs `frontend`, `web` e `deploy-web`. A publicação usa a mesma
  identidade federada, sem segredos, com 0 a 1 instância, sem tráfego até a conferência (research R9 e R10).
- **Governança:** emenda 2.1.0 da constituição para o front-end, antes do código (research R11).
- **O que não muda:** o `./mvnw verify` continua sem Node e só ganha os ITs novos do modo padrão; o
  `compose.yaml` não muda (FR-031).

## Contexto técnico

**Linguagem/versão**: Java 25 e Spring Boot 4.1.2 na API (sem mudança de versão); TypeScript 6.0 e Angular
22.3.0 no front; Node 26 LTS só no `frontend/` e no pipeline; Bash e YAML do GitHub Actions.

**Dependências principais**: na API, nenhuma nova. No front: `@angular/*` 22.3.0, `@angular/build` (Vitest),
`vitest` 5 com `@vitest/coverage-v8`, `jsdom`, `@playwright/test` 1.65 e `angular-eslint`. Na imagem:
`node:26-alpine` (build) e `nginx:1.31-alpine-slim` (execução, ajustada para rodar sem root), as duas por digest. No
pipeline, `actions/setup-node` por hash.

**Armazenamento**: sem mudança (H2 no modo padrão, PostgreSQL no modo Docker). Nenhuma migração Flyway nova.

**Testes**:
- API: 6 ITs novos no `./mvnw verify`, 1 no `./mvnw -Pextras verify` e 1 no `postgres-it` (research R7);
- front: unitários com Vitest e ponta a ponta com Playwright, no pipeline (jobs `frontend` e `web`).

**Plataforma**: navegadores modernos (Chromium, Firefox e Safari atuais), de 360 px a 1920 px; Cloud Run
`linux/amd64` para os dois serviços.

**Tipo de projeto**: aplicação web, com back-end (raiz, Maven) e front-end (`frontend/`, npm) em builds
separados.

**Metas de desempenho**: painéis preenchidos em até 3 s com as instâncias ativas e em até 30 s com partida a
frio dos dois serviços (SC-001). Bundle inicial de produção, medido sem compressão como os `budgets` do
`angular.json` medem: aviso acima de 350 kB e build reprovado acima de 500 kB (o Angular 22 com router, forms e
http já parte de cerca de 310 kB).

**Restrições**: o `./mvnw` não exige Node (FR-020); contrato de intervalos intacto (Princípio III); sem segredos
(Princípio VII); actions e imagens base fixadas por hash ou digest; imagem não root; 0 a 1 instância; sem CORS.

**Escala/escopo**: 2 endpoints novos; 2 telas e 4 painéis; 1 imagem nova; 1 serviço novo; 3 jobs novos e 2
alterados no pipeline.

## Verificação da constituição

*Portão: avaliado antes da Phase 0 e de novo depois da Phase 1.*

| Princípio | Situação | Como o plano atende |
| --- | --- | --- |
| I. Só testes de integração no back-end | ✅ | As agregações chegam com ITs `@SpringBootTest` completos, sem mocks, verificados por HTTP e pelo contrato. Os testes unitários ficam só em `frontend/`, como o princípio já permite |
| II. Execução só com `./mvnw` | ✅ | O `pom.xml` não ganha nada do front. Os ITs novos do modo padrão entram no `./mvnw verify`; o de autenticação, no `extras`. Node só no `frontend/` e no pipeline. A emenda 2.1.0 deixa isso explícito |
| III. Contrato idêntico | ✅ | `GET /api/producers/intervals` e `GET /api/movies` não mudam. O contrato 1.3.0 só acrescenta paths e esquemas. O painel de intervalos mostra todos os itens de `min` e `max`, como a API devolve |
| IV. Spec-anchored | ✅ | Spec e esclarecimento (FR-031) antes do código. A emenda é o primeiro commit. Os desvios do briefing (Cloud Run em vez de hospedagem estática, mesma origem em vez de CORS, front fora do compose, jobs no mesmo workflow) vão para `docs/ai/decisoes.md` |
| V. Extras isolados | ✅ | O front é um extra com infraestrutura própria: build, imagem e serviço separados, ligados só no pipeline. As agregações são extras da API sem infraestrutura, ligadas no modo padrão sem alterar o comportamento avaliado. O caminho de gravação não muda. Sem SSR, sem biblioteca de estado |
| VI. Rastreabilidade | ✅ | API: ITs (FR-001 a FR-006). Front: unitários e ponta a ponta no pipeline, o tipo de comprovação que a emenda 2.1.0 cria. Imagem e publicação: jobs `web` e `deploy-web`. Documentação: checklist da spec (FR-035) |
| VII. Sem segredos | ✅ | Identidade federada sem chaves; só identificadores públicos no workflow; `secrets.` continua proibido; o front não guarda nem envia token; nenhuma credencial nova |
| Cadeia de entrega | ✅ | `setup-node` fixado por hash; imagens base por digest; imagem do front multi-stage e não root, com hadolint, teste de fumaça e Trivy como a da API |
| Observabilidade | ✅ | A API não muda. O log de acesso do nginx sai em JSON no container, ligado pela variável `LOG_FORMAT=json` que o `Dockerfile` define, como a constituição exige para container e ambiente publicado (FR-026a). O teste de fumaça confere o formato. O log de erro do nginx não tem formato configurável e fica em texto, limite registrado no ADR 0028. `/health` serve ao probe |
| Fluxo | ✅ | Bolt único no branch `marco/6-frontend`, em etapas com commits próprios. PR, merge e tags ficam com o Anderson. O script do GCP só roda depois do login dele |

**Depois do design (Phase 1):** sem violações. Pontos de atenção, sem exceção constitucional:

- A emenda 2.1.0 precisa da aprovação do Anderson no PR (Governança). Até lá, as etapas 2 em diante seguem a
  versão emendada, já commitada no branch.
- Os checks obrigatórios passam de 10 para 12. A mudança na proteção da `main` é do Anderson e precisa
  acontecer antes do merge, senão o `deploy-web` espera um check que a proteção não exige (quickstart, passo 0).
- A política do workflow é ampliada só para o job `deploy-web`, com os mesmos limites do `deploy` (research R9).
- O `ApiDocsIT` e o `ApiDocsSecurityIT` mudam com as exceções que já existem; a entrada nova do contrato 1.3.0
  em `docs/ai/decisoes.md` registra o motivo.

## Estrutura do projeto

### Documentação (desta funcionalidade)

```text
specs/006-frontend-angular/
├── spec.md
├── plan.md                    # este arquivo
├── research.md                # R1 a R11
├── data-model.md              # respostas das agregações, estado do front e recursos novos do GCP
├── quickstart.md              # roteiro de validação
├── contracts/
│   ├── openapi.yaml           # contrato 1.3.0 (fonte; cópia em src/main/resources/static/)
│   ├── frontend-ui.md         # rotas, parâmetros da URL, painéis e estados
│   ├── web-image.md           # imagem do front: variáveis, rotas do nginx, cabeçalhos
│   └── ci-frontend.md         # jobs frontend, web e deploy-web; política e setup.sh
├── checklists/
│   └── requirements.md
└── tasks.md                   # /speckit-tasks
```

### Código-fonte (raiz do repositório)

```text
src/main/java/com/outsera/awards/
├── aggregation/                         # novo
│   ├── AggregationController.java
│   ├── AggregationService.java
│   ├── StudioNameParser.java
│   └── (records de resposta)
├── movie/MovieRepository.java           # alterado: duas consultas JPQL
├── security/SecurityConfiguration.java  # alterado: dois caminhos em PROTECTED
└── web/EtagConfiguration.java           # alterado: dois caminhos no filtro
src/main/resources/static/openapi.yaml   # alterado: cópia do contrato 1.3.0
src/test/java/com/outsera/awards/
├── aggregations/                        # novo: 7 ITs (research R7)
├── postgres/AggregationsPostgresIT.java # novo
└── docs/ApiDocsIT.java, ApiDocsSecurityIT.java   # alterados (exceções existentes)
src/test/resources/scenarios/            # novos CSVs de cenário das agregações
src/test/default-mode-its.txt            # alterado: seção "Marco 6"

frontend/                                # novo
├── package.json, package-lock.json, .nvmrc, angular.json, tsconfig*.json, eslint.config.js
├── proxy.conf.json                      # ng serve: /api → http://localhost:8080 (FR-021)
├── vitest-base.config.ts                # limite de cobertura de 80% (se o construtor não aceitar no angular.json)
├── playwright.config.ts
├── Dockerfile, .dockerignore, nginx/default.conf.template
├── README.md
├── src/app/
│   ├── app.config.ts, app.routes.ts, app.ts
│   ├── api/awards-api.ts                # HttpClient e tradução dos filtros
│   ├── shared/panel-state/, shared/paginator/
│   ├── dashboard/                       # página e quatro painéis
│   └── movies/                          # lista e filtros
└── e2e/                                 # cenários do Playwright

.github/
├── workflows/ci.yml                     # alterado: jobs frontend, web, deploy-web; dockerfile-lint
├── dependabot.yml                       # alterado: npm em /frontend
└── scripts/
    ├── check-workflows.sh               # alterado: deploy-web e 12 checks
    ├── web-smoke-test.sh                # novo
    └── verify-web.sh                    # novo
deploy/gcp/setup.sh                      # alterado: awards-web e awards-web-runtime
.specify/memory/constitution.md          # alterado: 2.1.0
AGENTS.md                                # alterado: front-end
docs/adr/0028-front-end-angular-com-nginx.md   # novo
docs/adr/README.md, docs/runbook.md, docs/ai/decisoes.md   # alterados
README.md                                # alterado: URL do front, seção e requisitos do Marco 6
```

**Decisão de estrutura**: o front fica em `frontend/`, com build, testes, imagem e README próprios, como pede o
briefing. A API ganha um pacote `aggregation` ao lado de `movie` e `producer`, porque as agregações não são nem
listagem nem intervalos. O `.dockerignore` da raiz já exclui `frontend/`, então a imagem da API não muda.

## Etapas do bolt

Cada etapa termina com `./mvnw verify` e `./mvnw -Pextras verify` verdes, sem Docker, com o resultado real
informado, e tem commits próprios. A partir da etapa 3, também termina com os comandos do front verdes
(`npm run lint`, `npm test`, `npm run build`).

**Donos nesta rodada (AGENTS.md, um dono por arquivo compartilhado):** esta sessão do Claude Code acumula os
papéis de implementador, de front-end, de infraestrutura e de documentação do Marco 6. É a **única dona**,
nesta rodada, de `pom.xml` (sem mudança prevista), `src/main/resources/static/openapi.yaml`, `frontend/`,
`.github/`, `deploy/gcp/`, `README.md`, `AGENTS.md`, `.specify/memory/constitution.md`, `docs/adr/`,
`docs/runbook.md` e `docs/ai/decisoes.md`. A revisão do PR continua com o Codex.

1. **Etapa 1 — Emenda 2.1.0** (FR-024)
   - Constituição com o relatório de impacto, `AGENTS.md` alinhado e a entrada em `docs/ai/decisoes.md`.
   - Fecha com os dois `./mvnw` verdes.
2. **Etapa 2 — Agregações na API** (FR-001 a FR-006; US4)
   - Contrato 1.3.0 copiado para `src/main/resources/static/openapi.yaml`.
   - ITs primeiro, depois o pacote `aggregation`, as consultas, o ETag e a proteção.
   - `ApiDocsIT` e `ApiDocsSecurityIT` atualizados, com a entrada do contrato 1.3.0 em `docs/ai/decisoes.md`.
   - `default-mode-its.txt` com a seção do Marco 6. Os dois `./mvnw` verdes, a cobertura de 90% mantida, e o
     `postgres-it` no pipeline do PR.
3. **Etapa 3 — Projeto do front e Dashboard** (FR-007 a FR-009, FR-015 a FR-019, FR-021 e FR-022; US1 e US3)
   - `ng new` com Angular 22.3.0, lint, Vitest com o limite de 80% e o proxy do `ng serve`.
   - Serviço da API, `PanelState` e os quatro painéis, com testes unitários.
   - Conferência manual com `./mvnw spring-boot:run` e `npm start`.
4. **Etapa 4 — Lista de filmes** (FR-010 a FR-014; US2)
   - Filtros na URL, paginação e cartões no celular, com testes unitários.
5. **Etapa 5 — Imagem, ponta a ponta e pipeline do PR** (FR-020, FR-023, FR-025 e FR-026; US3 e US5-5)
   - `frontend/Dockerfile`, template do nginx e `web-smoke-test.sh`, testados localmente com Docker.
   - Cenários do Playwright, rodados localmente contra o JAR e a imagem.
   - Jobs `frontend` e `web`, `dockerfile-lint` com o segundo Dockerfile, Dependabot do npm.
   - O PR mostra `frontend` e `web` verdes. O Anderson põe os dois na proteção da `main`.
6. **Etapa 6 — GCP e publicação** (FR-027 a FR-030; US5)
   - `setup.sh` estendido; o Anderson faz o login; o Claude Code roda o script duas vezes e registra as saídas
     (a segunda sem mudanças).
   - `verify-web.sh`, testado contra a imagem local nos casos de aprovação e de reprovação.
   - Job `deploy-web`, `check-workflows.sh` (`deploy-web` e 12 checks) com os casos negativos conferidos.
   - No PR, o `deploy-web` aparece como pulado. A prova real vem com o merge.
7. **Etapa 7 — Documentação** (FR-031 a FR-034; US6)
   - README (URL do front, seção e requisitos do Marco 6), `frontend/README.md`, ADR 0028 e índice, runbook
     (volta do tráfego do `awards-web`), `docs/ai/decisoes.md` (desvios do briefing).
   - Itens de revisão no checklist da spec.
8. **Depois do merge** (o Anderson faz o merge)
   - A execução da `main` publica a API e o front e confere os dois. O resumo do `deploy-web` comprova FR-027 a
     FR-029 e SC-001 e SC-007, e o link entra na spec, que passa a `Implemented`.
   - Sessão exportada.

## Riscos

| Risco | Mitigação |
| --- | --- |
| O nginx resolve o DNS do `awards` só na subida e perde o endereço depois | `resolver` com `proxy_pass` por variável (research R8), conferido no teste de fumaça |
| O Cloud Run do `awards` recusa a chamada repassada por causa do `Host` | `proxy_set_header Host` com o host de `API_URL` e `proxy_ssl_server_name on`; a conferência pela URL da tag reprova antes do tráfego |
| Partida a frio dos dois serviços passa do tempo limite do repasse | Tempo limite de 60 s no nginx; estado de carregamento nos painéis; o `verify-web.sh` espera com novas tentativas |
| O construtor do Angular não aceita o limite de cobertura direto | Configuração do Vitest (`vitest-base.config.ts`) com `coverage.thresholds.lines = 80`, conferida reprovando de propósito uma vez |
| Testes de ponta a ponta instáveis no runner | Esperas pelo estado da tela (sem `sleep`), readiness da API antes de começar, uma nova tentativa só no pipeline e trace guardado como artefato na falha |
| O Anderson não atualiza a proteção da `main` antes do merge | Passo 0 do quickstart e aviso no PR; a regra 9 do `check-workflows.sh` exige os 12 nos `needs` |
| `npm audit` reprova por vulnerabilidade sem correção numa dependência de desenvolvimento | `--omit=dev` no portão; o relatório completo vai para o resumo sem reprovar |

## Acompanhamento de complexidade

Sem violações da constituição. Nada a justificar.
