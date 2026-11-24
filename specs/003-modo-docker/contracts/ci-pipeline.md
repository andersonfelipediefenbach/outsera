# Contrato: mudanças no pipeline (Marco 3)

Complementa o [contrato do pipeline da spec 002](../../002-operacao/contracts/ci-pipeline.md). As regras
de política continuam valendo para os jobs novos:
- actions fixadas por hash;
- `permissions: contents: read` no topo;
- nenhum `secrets.*`;
- nenhuma publicação de imagem;
- `timeout-minutes` em todo job.

## Jobs

| Job | Novo ou alterado | O que faz | Comprova |
| --- | --- | --- | --- |
| `workflow-policy` | alterado | `fetch-depth: 0`; `check-workflows.sh` com as regras novas; `check-unchanged-tests.sh v0.2.0` | FR-034 e SC-002 ("sem alteração", research R14) |
| `build` | sem mudança | `./mvnw -B verify`, que agora inclui os ITs de Kafka (broker embutido) e de segurança | FR-001, FR-002, FR-009 a FR-027 |
| `postgres` | novo | PostgreSQL como *service container*; `./mvnw -B -Ppostgres-it verify` | FR-004, FR-006, FR-007, FR-008 e US2 |
| `compose` | novo (`needs: dockerfile-lint`) | `.github/scripts/compose-test.sh` | FR-028 a FR-035, SC-005 e SC-008 |
| `codeql`, `dependencies`, `dockerfile-lint`, `image` | sem mudança | — | spec 002 |

## `postgres`

```yaml
services:
  postgres:
    image: postgres:18-alpine@sha256:…   # digest resolvido na tarefa
    env:
      POSTGRES_DB: awards
      POSTGRES_USER: awards
      POSTGRES_HOST_AUTH_METHOD: trust    # sem credencial no workflow (research R16)
      POSTGRES_INITDB_ARGS: --locale=C --encoding=UTF8
    ports: ['5432:5432']
    options: >-
      --health-cmd "pg_isready -U awards -d awards" --health-interval 5s --health-retries 20
```

O passo do Maven usa `DB_URL=jdbc:postgresql://localhost:5432/awards`, `DB_USER=awards` e
`DB_PASSWORD=` (vazio). Os relatórios do Failsafe vão para o artefato do job, com `if: always()`.

## `compose-test.sh`

O script usa `set -euo pipefail` e derruba o ambiente (`down -v`) num `trap`, sempre. Ele falha no
primeiro passo que não confere.

| Passo | Verificação | Falha quando |
| --- | --- | --- |
| 0 | `docker compose pull` e `docker compose --profile tools pull` (fora da medição) | — |
| 1 | `docker compose up -d --build --wait --wait-timeout 300` | algum serviço não fica `healthy` |
| 2 | intervalos com o resultado de referência | corpo diferente |
| 3 | `GET :9090/api/v1/targets`: alvo `app` com `health: "up"`; `GET :9090/api/v1/query?query=awards_ingestion_events_total` não vazio | alvo ausente ou fora; série ausente |
| 4 | `GET :3000/api/dashboards/uid/awards` com 200; `GET :3000/api/datasources/uid/prometheus/health` com `status: OK` (credencial de demonstração do compose) | dashboard ou datasource ausente |
| — | tempo dos passos 1 a 4 | acima de 180 s (SC-008) |
| 5 | publica pelo `kafka-cli` um vencedor novo que cria um intervalo menor que o mínimo e espera até 5 s pela mudança em `min` | não muda em 5 s (SC-005) |
| 6 | publica um evento com `year: 0` e lê o tópico de erros com os cabeçalhos | sem registro ou sem `awards-error-reason: YEAR_OUT_OF_RANGE` |
| 7 | `docker compose down` (**sem `-v`**) e `docker compose up -d --wait`: todos os containers são recriados sobre os mesmos volumes; o filme do passo 5 continua; `totalElements` = 206 + 1 | filme ausente ou total diferente |
| 7b | `docker compose stop postgres`; publica `{"year":2031,"title":"Durante a Queda",…}`; espera 5 s; `docker compose start postgres`; espera até 60 s pelo filme na listagem; o tópico de erros não recebe esse evento (FR-015a, queda real do banco) | filme ausente ou evento no tópico de erros |
| 8 | `API_TOKEN=$(openssl rand -hex 16) docker compose up -d --wait app prometheus`; intervalos sem token com 401; alvo do Prometheus `up` de novo | outro status ou alvo fora |

O token do passo 8 é gerado no próprio job, nunca é gravado e nunca é impresso (`set +x` em volta dele).

## `check-workflows.sh` (regras novas)

- O `dependabot.yml` tem também o ecossistema `docker-compose`, com `directory: /` e
  `schedule.interval`.
- Toda linha `image:` do `compose.yaml` termina em `@sha256:` seguido de 64 caracteres
  hexadecimais.
- Toda linha `image:` de `services:` nos workflows segue a mesma regra.
- O job `postgres` não declara `POSTGRES_PASSWORD` (research R16).

## `check-unchanged-tests.sh <tag>`

- Lista os `src/test/java/**/*IT.java` que existem na tag (`git ls-tree -r --name-only <tag>`).
- Para cada um que não está em `.github/unchanged-tests-exceptions.txt`, roda `git diff --quiet <tag>
  -- <arquivo>`. Um arquivo apagado ou alterado reprova, e o script lista os arquivos.
- Cada linha da lista de exceções tem o formato `<caminho> <título da entrada em docs/ai/decisoes.md>`.
  O script falha se a entrada citada não existir no `decisoes.md`.
