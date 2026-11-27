# Contrato: pipeline do front e publicação do `awards-web`

Alterações no `.github/workflows/ci.yml`, no `check-workflows.sh` e no `deploy/gcp/setup.sh` (FR-025 a FR-030).
Detalhes em [research R9 e R10](../research.md). Os jobs existentes que não aparecem aqui não mudam.

## Job `frontend` (novo; todo push e PR)

| Passo | Comando | Reprova quando |
| --- | --- | --- |
| Node | `actions/setup-node@<hash>` com `node-version-file: frontend/.nvmrc` e cache do npm | — |
| Dependências | `npm ci` em `frontend/` | lockfile divergente |
| Lint | `npm run lint` | erro do angular-eslint, inclusive `page.route` nos testes de ponta a ponta |
| Unitários | `npm test -- --watch=false --coverage` | teste falhando ou cobertura de linhas abaixo de 80% |
| Build | `npm run build` | erro de compilação ou orçamento de tamanho estourado |
| Auditoria | `npm audit --omit=dev --audit-level=high` | vulnerabilidade alta ou crítica em dependência de execução |

O relatório de cobertura vai para o resumo da execução. `permissions: contents: read`, `timeout-minutes: 15`.

## Job `dockerfile-lint` (alterado)

O hadolint roda também em `frontend/Dockerfile`, com o mesmo `failure-threshold: warning`.

## Job `web` (novo; todo push e PR; `needs: dockerfile-lint`)

1. Build da imagem do front com o Buildx: saídas `type=docker,name=awards-web:ci` e
   `type=oci,dest=…/awards-web-oci.tar`, `provenance: false`, `sbom: false`, contexto `frontend/`.
2. Digest e identidade da imagem testada, como o passo do job `image` (um manifesto `linux/amd64`; `Id` igual
   ao digest da configuração ou do manifesto). Saída `digest`.
3. JDK 25 (`actions/setup-java` já fixado) e `./mvnw -B -DskipTests package`; a API sobe com `java -jar` em
   segundo plano, e o job espera `/actuator/health/readiness`.
4. `web-smoke-test.sh awards-web:ci http://127.0.0.1:8080` ([web-image.md](web-image.md)).
5. Front em `:8081` com a mesma imagem; `npx playwright install --with-deps chromium`; `npx playwright test`
   (projetos `desktop` e `celular`). Na falha, o relatório e os traces vão como artefato.
6. Trivy na `awards-web:ci`: relatório completo no resumo e porta de alta e crítica com correção, como a da API.
7. Artefato `awards-web-oci` só em push na `main`, retenção de 1 dia.

`permissions: contents: read`, `timeout-minutes: 30`.

## Job `deploy-web` (novo; só push na `main` deste repositório)

- `if`: o mesmo do `deploy`.
- `needs`: os 12 checks obrigatórios (`workflow-policy`, `build`, `postgres`, `codeql`, `dependencies`,
  `dockerfile-lint`, `image`, `alert-rules`, `load`, `compose`, `frontend`, `web`) e `deploy`.
- `permissions: contents: read, id-token: write`; `concurrency: { group: deploy-cloud-run-web,
  cancel-in-progress: false }`; `environment: { name: cloud-run-web, url: <URL pública do awards-web> }`.
- `env`: os identificadores do Marco 5 mais `GCP_WEB_SERVICE: awards-web`,
  `GCP_WEB_RUNTIME_SA: awards-web-runtime@outsera-awards.iam.gserviceaccount.com`, `WEB_URL` e
  `API_URL` (URL pública do `awards`), todos públicos.
- Passos, na ordem:
  1. download do `awards-web-oci` e conferência do digest com `needs.web.outputs.digest`;
  2. `google-github-actions/auth` e `setup-gcloud` (mesmos hashes e mesma versão do `gcloud` do `deploy`);
  3. `skopeo copy --preserve-digests` para `…/awards/awards-web:$GITHUB_SHA` e conferência do digest no registro;
  4. `gcloud run deploy awards-web` com `--image=…@$IMAGE_DIGEST`, `--service-account`, `--min-instances=0`,
     `--max-instances=1`, `--cpu=1`, `--memory=256Mi`, `--port=8080`, `--set-env-vars=API_URL=$API_URL`,
     startup probe HTTP em `/health`, `--no-traffic --tag=c-<sha curto>`;
  5. `verify-web.sh "$TAG_URL"`;
  6. tráfego 100% para a revisão, removendo as tags antigas;
  7. `verify-web.sh "$WEB_URL"`;
  8. resumo (`if: always()`): URL pública, imagem, revisão, URL da tag, resultado e duração.

## `verify-web.sh <url>` (novo)

Com novas tentativas até 120 s, para a partida a frio dos dois serviços:

1. `<url>/health` → 200.
2. `<url>/` → 200 com `<app-root`; `<url>/filmes` → 200 com o mesmo HTML.
3. `<url>/api/producers/intervals` → corpo idêntico à `REFERENCE` do `smoke-test.sh`.
4. `<url>/api/aggregations/years-with-multiple-winners` → 1986, 1990 e 2015, cada um com 2.
5. `<url>/api/aggregations/studios-with-win-count` → os três primeiros: Columbia Pictures 7, Paramount
   Pictures 6, Warner Bros. 5.

Código 0 aprova; qualquer divergência imprime a resposta recebida e sai com 1. O script imprime também o tempo
da primeira resposta completa do passo 3 (`tempo_primeira_resposta=<s>`), usado para registrar o SC-001.

## Job `workflow-policy` (alterado)

Ganha um passo `.github/scripts/check-unchanged-tests.sh v0.5.0` ("Testes de integração dos Marcos 1 a 5 sem
alteração desde a v0.5.0"), para a FR-005 valer também para os ITs dos Marcos 4 e 5. As exceções do `ApiDocsIT`
e do `ApiDocsSecurityIT` já estão em `.github/unchanged-tests-exceptions.txt`.

## Política do workflow (`check-workflows.sh`)

- `REQUIRED_CHECKS` passa a ter os 12 jobs; a regra 9 vale para os `needs` do `deploy` e do `deploy-web`.
- A regra 6 (Dependabot) passa a exigir também os ecossistemas `npm` e `docker` com `directory "/frontend"`.
- As regras de escrita (`id-token: write`, `skopeo`, `gcloud run`) aceitam os jobs `deploy` e `deploy-web` e
  nenhum outro; a regra de não rebuild vale para os dois.
- `secrets.` continua proibido; `uses:` fora da lista conferida, ou sem hash, continua reprovando.
- Casos negativos conferidos (como no Marco 5): `id-token: write` no `web`; `docker build` no `deploy-web`;
  `deploy-web` sem `frontend` nos `needs`; `secrets.` no `deploy-web`; `setup-node` por tag.

## `deploy/gcp/setup.sh` (alterado)

Passos novos, com o padrão de convergência existente e o motivo de cada vínculo no script:

1. Conta `awards-web-runtime`, sem papéis.
2. Serviço `awards-web` com a imagem de exemplo, `--service-account=awards-web-runtime`, `--min-instances=0`,
   `--max-instances=1` (só se não existir).
3. `roles/run.developer` para `github-deployer` no serviço `awards-web`.
4. `roles/iam.serviceAccountUser` para `github-deployer` na conta `awards-web-runtime`.
5. `roles/run.invoker` para `allUsers` no serviço `awards-web`.
6. Saída com `GCP_WEB_SERVICE`, `GCP_WEB_RUNTIME_SA` e a URL do serviço.

A segunda execução seguida termina com todos os passos "OK (já existia)" e zero mudanças.
