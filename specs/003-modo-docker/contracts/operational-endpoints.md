# Contrato: endpoints operacionais e autenticação

Complementa o [contrato operacional da spec 002](../../002-operacao/contracts/operational-endpoints.md).
Os caminhos e os formatos não mudam. O que muda é quem exige o token e o que a readiness espera no
perfil `kafka`.

## Matriz de acesso

| Caminho | Token desligado (padrão) | Token ligado (`API_TOKEN` definido) |
| --- | --- | --- |
| `GET /api/producers/intervals` | público | exige `Authorization: Bearer <token>` |
| `GET /api/movies` | público | exige o token |
| `GET /actuator/prometheus` | público | exige o token |
| `GET /actuator/health/liveness` | público | público |
| `GET /actuator/health/readiness` | público | público |
| `GET /actuator/health` | público | público |
| `GET /swagger-ui.html`, `/openapi.yaml`, `/webjars/**` | público | público |
| Qualquer outro caminho (404) e `/error` | público | público |

Num caminho protegido, a regra vale para qualquer método: com o token ligado, um `POST /api/movies`
sem token recebe 401, e não 405 (research R11).

## Resposta 401

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/problem+json
WWW-Authenticate: Bearer realm="awards"
Cache-Control: no-cache, no-store, max-age=0, must-revalidate

{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Token ausente ou inválido","instance":"/api/producers/intervals"}
```

- O corpo é o mesmo para o token ausente e para o token errado (FR-023).
- Nem o valor esperado nem o recebido aparecem no corpo, nos cabeçalhos, no log ou nas métricas
  (FR-025).
- A comparação é feita sobre o SHA-256 dos dois valores, com `MessageDigest.isEqual`. É uma decisão de
  design (research R11 e ADR 0017), conferida na "Revisão de segurança".
- Um `OPTIONS` segue a mesma matriz: com o token ligado, num caminho protegido e sem token, a resposta é
  401. O CORS fica para o Marco 6.

## Cabeçalhos de cache (research R12)

| Respostas | `Cache-Control`, `Pragma` e `Expires` |
| --- | --- |
| Endpoints de negócio (200, 304, 400, 405) | **ausentes**, como no Marco 2, para não anular o ETag |
| 401, métricas, saúde e documentação | os do Spring Security (`no-cache, no-store…`) |

`X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY` aparecem em todas as respostas.

## Readiness no perfil `kafka`

| Momento | `GET /actuator/health/readiness` |
| --- | --- |
| Da subida até o consumo do evento de carga concluída desta execução | 503 `{"status":"OUT_OF_SERVICE"}` |
| Depois disso | 200 `{"status":"UP"}` |

O grupo é `readinessState` + `ingestion` (research R10). A resposta continua só com o estado
(`show-components: never`).

## Métricas novas (perfil `kafka`)

| Série no Prometheus | Rótulos | Significado |
| --- | --- | --- |
| `awards_ingestion_events_total` | `outcome="recorded"` ou `"rejected"`, `application` | eventos de filme gravados e enviados ao tópico de erros (FR-017) |
| `kafka_consumer_fetch_manager_records_lag_max` (verificado, research R19) | `client_id` (`consumer-awards-movies-<n>`), `spring_id`, `kafka_version`, `application` | atraso máximo do consumidor do tópico de filmes (uma partição, um consumidor) |

No modo padrão, nenhuma série `kafka_*` nem `awards_ingestion_*` existe (research R2).
