# Contrato: endpoints operacionais

Endpoints de saúde e de métricas, servidos pelo Actuator (research R1). Não fazem parte do contrato
OpenAPI de negócio (research R5). Ficam na mesma porta da API (`PORT`, padrão 8080) e são públicos neste
marco (spec, premissas).

| Método e caminho | Status | Corpo | Requisitos |
| --- | --- | --- | --- |
| `GET /actuator/health/liveness` | 200 se vivo, 503 se `BROKEN` | `{"status":"UP"}` ou `{"status":"DOWN"}` | FR-008, FR-011 |
| `GET /actuator/health/readiness` | 200 se pronto, 503 antes do fim da carga ou se `REFUSING_TRAFFIC` | `{"status":"UP"}` ou `{"status":"OUT_OF_SERVICE"}` | FR-008, FR-009, FR-010, FR-011 |
| `GET /actuator/health` | 200 ou 503 | `{"status":"...","groups":["liveness","readiness"]}`, sem `components` nem `details` | FR-011 |
| `GET /actuator/prometheus` | 200 | texto do Prometheus (`text/plain; version=0.0.4`, ou OpenMetrics se o `Accept` pedir) | FR-012 a FR-016 |

## Regras

- Os corpos de liveness e readiness têm **só** o campo `status`. A saúde geral tem `status` e a lista
  `groups`, com exatamente `liveness` e `readiness`, que o Boot inclui sempre que os probes estão
  ligados. Nada de componentes, banco, disco, caminhos, versões ou configuração (FR-011).
- Os demais endpoints do Actuator não são expostos: `/actuator`, `/actuator/env`, `/actuator/beans`,
  `/actuator/info`, `/actuator/metrics` e outros respondem 404.
- O content type das respostas de saúde é `application/vnd.spring-boot.actuator.v3+json`, um tipo JSON
  (sufixo `+json`). Para o Spring, ele não é "compatível" com `application/json`, e os testes conferem
  o sufixo (verificado na etapa 2).

## Séries obrigatórias em `/actuator/prometheus`

Nomes e rótulos detalhados em [../data-model.md](../data-model.md), "Série de métrica de requisição".

| Série | Rótulos mínimos | Requisito |
| --- | --- | --- |
| `http_server_requests_seconds_count` | `application`, `method`, `uri`, `status`, `outcome` | FR-013 |
| `http_server_requests_seconds_sum` | os mesmos | FR-013 |
| `http_server_requests_seconds_bucket` | os mesmos e `le` | FR-014 |
| `jvm_memory_used_bytes`, `jvm_threads_live_threads`, `process_uptime_seconds` | `application` | FR-016 |

## Consultas de referência (PromQL)

Servem para o SLO e os alertas do Marco 4, e mostram que as métricas atendem ao SC-004.

```promql
# Taxa de erro de um endpoint em 5 minutos
sum(rate(http_server_requests_seconds_count{uri="/api/producers/intervals",outcome=~"CLIENT_ERROR|SERVER_ERROR"}[5m]))
  / sum(rate(http_server_requests_seconds_count{uri="/api/producers/intervals"}[5m]))

# Percentil 95 de latência de um endpoint em 5 minutos
histogram_quantile(0.95,
  sum by (le) (rate(http_server_requests_seconds_bucket{uri="/api/producers/intervals"}[5m])))
```
