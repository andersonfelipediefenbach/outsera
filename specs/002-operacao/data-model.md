# Data Model: Marco 2 — Operação

**Spec**: [spec.md](spec.md) | **Research**: [research.md](research.md)

Este marco não muda o esquema do banco (`V1__create_schema.sql`) nem as entidades `Movie` e `Producer`
do Marco 1. As entidades abaixo são as da spec. Nenhuma é persistida: são derivadas ou só existem em
memória enquanto a aplicação roda.

## Representação e ETag

| Campo | Descrição |
| --- | --- |
| endpoint | `/api/producers/intervals` ou `/api/movies` |
| parâmetros | na listagem, `year`, `winner`, `page` e `size`; nos intervalos, nenhum |
| corpo | o JSON da resposta 200, idêntico ao do Marco 1 |
| ETag | `"0" + MD5(corpo)` em hexadecimal, entre aspas, forte (research R4) |

**Regras:**

- Mesmo corpo → mesmo ETag, inclusive entre execuções. Corpo diferente → ETag diferente (FR-002).
- Só respostas 2xx de GET (e HEAD) têm ETag. 400 e 405 não têm (FR-006). Uma eventual 500 também não
  teria, pelo mesmo motivo, mas sem requisito próprio (research R4).
- `If-None-Match` bate quando contém o ETag atual (sozinho, numa lista ou com `W/`) ou é `*`. Nesse
  caso, a resposta é 304, sem corpo, com o mesmo `ETag` (FR-003). Malformado ou diferente: 200 (FR-004).

## Estado de saúde

| Estado | Valores | Probe | HTTP |
| --- | --- | --- | --- |
| Vivacidade (`LivenessState`) | `CORRECT`, `BROKEN` | `/actuator/health/liveness` | 200 `UP`, 503 `DOWN` |
| Prontidão (`ReadinessState`) | `REFUSING_TRAFFIC`, `ACCEPTING_TRAFFIC` | `/actuator/health/readiness` | 200 `UP`, 503 `OUT_OF_SERVICE` |

**Transições na inicialização com arquivo válido** (research R2):

```text
contexto sobe ──► Flyway ──► carga do CSV (avisos + resumo) ──► servidor web sobe
   readiness: REFUSING_TRAFFIC (padrão) ─────────────────────────────┐
                                                                     ▼
ApplicationStartedEvent ──► liveness CORRECT
ApplicationReadyEvent   ──► readiness ACCEPTING_TRAFFIC  (sempre depois do resumo da carga, FR-009)
```

- Com linhas inválidas, a carga termina com o resumo, e a readiness fica positiva normalmente (FR-010).
- Com erro estrutural, a carga lança exceção, o contexto não sobe e nenhum dos dois eventos acontece
  (FR-010). No container, o processo termina com código diferente de zero (FR-025).
- No encerramento, o Boot publica `REFUSING_TRAFFIC` antes de parar o servidor.

## Série de métrica de requisição

Métrica `http.server.requests` do Micrometer, um timer com histograma (research R3).

| Rótulo | Valores | Observação |
| --- | --- | --- |
| `application` | `awards` (`spring.application.name`) | em todas as séries (FR-016) |
| `method` | `GET`, `POST`... | |
| `uri` | modelo de rota: `/api/producers/intervals`, `/api/movies`, `/actuator/...` | sem a query string. Caminhos inexistentes caem numa identificação única (FR-015) |
| `status` | `200`, `304`, `400`, `405`, `500`... | |
| `outcome` | `SUCCESS` (2xx), `REDIRECTION` (3xx, inclui 304), `CLIENT_ERROR` (4xx), `SERVER_ERROR` (5xx) | |
| `exception`, `error` | nome da exceção ou `none` | |

**Séries no Prometheus:** `http_server_requests_seconds_count`, `_sum` e `_bucket{le=...}`, com faixas
entre 1 ms e 10 s (FR-014).

**Taxa de erro:** requisições com `outcome` igual a `CLIENT_ERROR` ou `SERVER_ERROR` divididas pelo
total do mesmo `uri`. O 304 (`REDIRECTION`) conta como sucesso (US3-6). Consultas de referência em
[contracts/operational-endpoints.md](contracts/operational-endpoints.md).

## Evento de log

Dois formatos, escolhidos pela variável `LOGGING_STRUCTURED_FORMAT_CONSOLE` (FR-020, research R6). Os
campos JSON abaixo são caminhos ECS: o Boot 4.1.2 os escreve aninhados (`log.level` é
`{"log":{"level":…}}`).

| Campo da spec (FR-017) | Texto (sem a variável) | JSON ECS (`=ecs`) |
| --- | --- | --- |
| data e hora com fuso | `2026-11-23T11:44:54.191-03:00` | `@timestamp` (UTC, `Z`) |
| nível | `INFO`, `WARN`... | `log.level` |
| logger | `c.o.awards.ingestion.MovieFileLoader` (abreviado) | `log.logger` (nome completo) |
| thread | `[main]` | `process.thread.name` |
| mensagem | resto da linha | `message` |
| exceção | linhas seguintes, com a pilha | `error.type`, `error.message`, `error.stack_trace` no mesmo evento |
| aplicação | `[awards]` | `service.name` |

**Mensagens do Marco 1 que os testes verificam** (FR-018), iguais nos dois formatos:

- `Linha <n> rejeitada: <motivo>` (nível `WARN`, logger `MovieFileLoader`);
- `Carga do arquivo <caminho> concluída: <l> linhas lidas, <a> aceitas, <r> rejeitadas` (nível `INFO`);
- o relatório do `MovieFileFailureAnalyzer`, nas falhas estruturais (nível `ERROR`, várias linhas numa
  só mensagem).

O `LogOutput` dos testes (research R6) lê os dois formatos e devolve eventos com esses campos.

**Saída fora do log:** nenhuma, nos dois formatos. O banner do Boot fica desligado
(`spring.main.banner-mode: off`, FR-020), e a imagem não usa `JAVA_TOOL_OPTIONS`, cuja nota da JVM sairia
fora do log (research R8).

## ADR

| Campo | Regra (FR-031) |
| --- | --- |
| número | 4 dígitos, sequencial, a partir de `0001`, nunca reutilizado |
| título | a decisão, em uma linha |
| data | data da decisão original |
| status | `Aceito` ou `Substituído por NNNN` |
| contexto, decisão, alternativas consideradas, consequências | seções obrigatórias |
| origem | link para a entrada em `docs/ai/decisoes.md` (criada junto com o ADR, se não existir) |

Lista e origens em [research.md](research.md), R15.
