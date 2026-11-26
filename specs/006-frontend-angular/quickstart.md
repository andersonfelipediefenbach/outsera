# Quickstart: validar o Marco 6

Roteiro de validação de ponta a ponta. Os detalhes ficam nos contratos:
[`openapi.yaml`](contracts/openapi.yaml), [`frontend-ui.md`](contracts/frontend-ui.md),
[`web-image.md`](contracts/web-image.md) e [`ci-frontend.md`](contracts/ci-frontend.md).

## 0. Pré-requisitos

- JDK 25 para a API. Node 26 (`frontend/.nvmrc`) só para o front. Docker só para a imagem do front.
- **Anderson, antes do merge:** incluir `frontend` e `web` nos checks obrigatórios da proteção da `main`
  (passam de 10 para 12).

## 1. Modo padrão intacto (SC-005)

```bash
./mvnw verify            # sem Node e sem Docker; inclui os ITs novos das agregações
./mvnw -Pextras verify   # inclui o AggregationsAuthIT e a cobertura de 90%
```

Esperado: os dois verdes, e `frontend/` não é lido pelo Maven (o build passa mesmo sem `node_modules`).

## 2. Agregações na API (US4)

```bash
./mvnw spring-boot:run
curl -s localhost:8080/api/aggregations/years-with-multiple-winners
curl -s localhost:8080/api/aggregations/studios-with-win-count
```

Esperado: `{"years":[{"year":1986,"winnerCount":2},{"year":1990,"winnerCount":2},{"year":2015,"winnerCount":2}]}`
e a lista de estúdios começando por Columbia Pictures 7, Paramount Pictures 6 e Warner Bros. 5. O Swagger UI
mostra a tag `aggregations` e a versão 1.3.0. `GET /api/producers/intervals` continua com o resultado de
referência.

## 3. Front local contra a API local (FR-021)

```bash
cd frontend
npm ci
npm start                # ng serve com proxy /api → http://localhost:8080
```

Abrir `http://localhost:4200`:

- Dashboard com os valores de referência (US1, SC-002); busca por 1990 → dois vencedores; por 1979 → "Nenhum
  vencedor em 1979."; por `abc` → mensagem de validação e botão desabilitado.
- `/filmes`: total igual ao do arquivo, paginação, filtro 1990 + "Sim" → dois filmes; recarregar mantém o
  filtro (US2).
- DevTools em 360 px: sem rolagem horizontal, linhas como cartões (US3).

## 4. Testes do front (FR-022)

```bash
npm run lint
npm test -- --watch=false --coverage     # reprova abaixo de 80% de linhas
npm run build
```

## 5. Imagem e ponta a ponta locais (FR-023, FR-026)

```bash
./mvnw -DskipTests package && java -jar target/awards-*.jar &
docker build -t awards-web:local frontend
.github/scripts/web-smoke-test.sh awards-web:local http://host.docker.internal:8080
docker run --rm -d -p 127.0.0.1:8081:8080 --add-host host.docker.internal:host-gateway -e API_URL=http://host.docker.internal:8080 awards-web:local
cd frontend && npx playwright test       # projetos desktop e celular
```

No Windows, os comandos funcionam no Git Bash com o Docker Desktop. Se o Docker não estiver disponível, os passos 5 ficam para
o job `web` do pipeline.

Esperado: o teste de fumaça com os 10 passos aprovados e o Playwright verde nos dois projetos.

## 6. Configuração do GCP (FR-028)

```text
! gcloud auth login
! gcloud config set project outsera-awards
```

```bash
deploy/gcp/setup.sh      # primeira execução: passos novos "criado"
deploy/gcp/setup.sh      # segunda: só "já existia", zero mudanças, código 0
gcloud iam service-accounts keys list --iam-account=awards-web-runtime@outsera-awards.iam.gserviceaccount.com --managed-by=user   # vazio
```

`verify-web.sh` contra a imagem local: aprova com o `API_URL` certo e reprova com um `API_URL` que aponta para
uma porta sem API.

## 7. Pipeline do PR

- `workflow-policy`, `frontend`, `web` e os 10 checks anteriores verdes; `deploy` e `deploy-web` pulados.
- Os casos negativos do `check-workflows.sh` ([ci-frontend.md](contracts/ci-frontend.md)) conferidos à parte.

## 8. Depois do merge (SC-001, SC-007)

- O `deploy` e o `deploy-web` verdes na execução da `main`, com o resumo do `deploy-web` mostrando a revisão
  conferida.
- A URL pública do front, aberta num navegador sem nada instalado, mostra os quatro painéis em até 30 s com
  partida a frio.
- Com o serviço ocioso, `gcloud run services describe awards-web` mostra `maxScale: 1` e `minScale: 0` (ou
  ausente).
