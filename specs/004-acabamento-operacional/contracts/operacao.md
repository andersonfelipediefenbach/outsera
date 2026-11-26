# Contrato: artefatos de operação do Marco 4

Interfaces que o operador, o mantenedor e o colaborador usam diretamente.

## Gerador de CSV

```text
java tools/csv-generator/GenerateMovies.java [--movies N] [--seed S] --output ARQUIVO
```

| Parâmetro | Padrão | Regra |
| --- | --- | --- |
| `--movies` | 100000 | inteiro de 1 a 2000000 |
| `--seed` | 20261125 | inteiro de 64 bits |
| `--output` | obrigatório | pasta precisa existir; o arquivo é escrito por inteiro ou não é escrito |

| Código de saída | Significado |
| --- | --- |
| 0 | arquivo escrito; imprime o caminho, o número de filmes e o SHA-256 |
| 2 | parâmetro inválido ou ausente; mensagem em uma linha no erro padrão; nenhum arquivo criado |
| 1 | erro de escrita; arquivo temporário removido |

Formato da saída: spec 001 (FR-002 a FR-010) e [data-model.md](../data-model.md#arquivo-gerado).

## Compose

| Comando | Efeito |
| --- | --- |
| `docker compose up -d --build --wait` | como no Marco 3; o Prometheus passa a carregar `observability/prometheus/rules/` |
| `docker compose -f compose.yaml -f compose.load.yaml up -d --build --wait` | aplicação com o arquivo de `target/generated-data/movies.csv` |
| `docker compose --profile load run --rm k6` | teste de carga contra `http://app:8080`; `API_TOKEN` é repassado se definido |
| `docker compose run --rm --no-deps --entrypoint promtool prometheus test rules /etc/prometheus/rules/slo.test.yml` | testes das regras |

Alertas visíveis em `http://localhost:9090/alerts`. Sem Alertmanager.

## Alertas

| Alerta | Gravidade | SLO | Runbook |
| --- | --- | --- | --- |
| `AwardsIntervalsAvailabilityBudgetBurn` | `page` (1 h/5 min, 14,4×) ou `ticket` (6 h/30 min, 6×) | `intervals-availability` | `docs/runbook.md#awardsintervalsavailabilitybudgetburn` |
| `AwardsIntervalsLatencyBudgetBurn` | idem | `intervals-latency` | `docs/runbook.md#awardsintervalslatencybudgetburn` |
| `AwardsAppDown` | `page` | `intervals-availability` | `docs/runbook.md#awardsappdown` |

## Imagem

| Item | Valor |
| --- | --- |
| `ENTRYPOINT` | `java -XX:MaxRAMPercentage=75 -XX:AOTCache=app.aot -jar application.jar` |
| sem o cache (medição) | `docker run --entrypoint java <imagem> -XX:MaxRAMPercentage=75 -XX:AOTMode=off -jar application.jar` |
| variáveis, porta, usuário, healthcheck | sem mudança (spec 002, `contracts/container.md`) |

## Verificação da assinatura (quem baixa os artefatos)

Durante o job, contra `localhost:5000/awards@<digest>`:

```text
cosign verify \
  --certificate-identity "https://github.com/<repositório>/.github/workflows/ci.yml@<ref>" \
  --certificate-oidc-issuer https://token.actions.githubusercontent.com \
  localhost:5000/awards@<digest>
cosign verify-attestation --type cyclonedx (mesma identidade) localhost:5000/awards@<digest>
```

Depois do job, o registro não existe mais. O resumo do job traz o digest e o índice da entrada no Rekor,
que qualquer pessoa confere com `rekor-cli get --log-index <índice>` (identidade, digest e horário). O SBOM
fica nos artefatos da execução.

## Dev container

| Item | Valor |
| --- | --- |
| arquivo | `.devcontainer/devcontainer.json` |
| JDK | 25 (Temurin) |
| Maven | só o `./mvnw` |
| Docker | docker-in-docker com compose v2 |
| portas | 8080 (API), 9090 (Prometheus), 3000 (Grafana) |
| pós-criação | `./mvnw -B -q dependency:go-offline` |
