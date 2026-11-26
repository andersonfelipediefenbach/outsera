# Contrato: `.github/scripts/verify-deployment.sh`

Conferência da URL publicada (FR-014 a FR-016; research R6 e R11).

## Uso

```bash
.github/scripts/verify-deployment.sh <URL base> [espera máxima em segundos, padrão 180]
```

- A URL base é a da revisão (`https://c-<sha>---awards-….a.run.app`), a pública do serviço ou uma local
  (`http://127.0.0.1:<porta>`).
- Precisa de `curl` e `jq`. Sem token: as chamadas são anônimas (FR-004).

## Passos

| N | Passo | Aprova quando |
| --- | --- | --- |
| 1 | Readiness | `GET /actuator/health/readiness` devolve 200 com `{"status":"UP"}` dentro da espera máxima; tenta a cada 3 s, com tempo limite de 10 s por chamada (partida a frio) |
| 2 | Resultado de referência | `GET /api/producers/intervals` devolve 200, `application/json`, e o corpo, ordenado com `jq -S`, é igual à `REFERENCE` do `smoke-test.sh` (chaves exatas, sem campos extras) |
| 3 | Swagger UI e contrato | `GET /swagger-ui.html`, seguindo redirecionamentos, devolve 200, e `GET /openapi.yaml` devolve 200 |
| 4 | Métricas fechadas (só com `--expect-no-metrics`) | `GET /actuator/prometheus` devolve 404 (FR-004a) |

O passo 4 é ligado pela opção `--expect-no-metrics`, usada no pipeline e na T038. Na conferência local contra
`./mvnw`, ele fica desligado, porque o modo padrão expõe as métricas.

## Saída

- Uma linha `OK`/`FALHA` por passo, com o tempo de cada chamada (`curl -w '%{time_total}'`) e o tempo da
  primeira chamada até a readiness (SC-001). Em `FALHA`, o status e o corpo recebidos, truncados em 2 KB.
- Com `GITHUB_STEP_SUMMARY` definido, uma tabela com a URL, cada passo e o tempo até a readiness.
- Código de saída `0` se os três passos aprovam, `1` se algum falha e `2` se falta uma ferramenta.
