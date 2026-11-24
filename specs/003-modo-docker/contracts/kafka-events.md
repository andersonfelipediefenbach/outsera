# Contrato: eventos do Kafka

Vale para o perfil `kafka` (no compose, o grupo `docker`). Os nomes dos tópicos vêm de
`KAFKA_TOPIC_MOVIES` e `KAFKA_TOPIC_ERRORS` (data-model.md).

## Tópicos

| Tópico (padrão) | Partições | Quem escreve | Quem lê | Conteúdo |
| --- | --- | --- | --- | --- |
| `awards.movies` | 1 | o publicador da aplicação e qualquer cliente externo | o consumidor da aplicação (grupo `awards`) | eventos de filme e de carga concluída |
| `awards.movies.errors` | 1 | o consumidor da aplicação | pessoas e ferramentas de diagnóstico | eventos rejeitados, com os bytes originais |

A aplicação cria os dois tópicos se não existirem. A partição única garante que o evento de carga
concluída seja consumido depois dos filmes publicados antes dele (research R6).

## Evento de filme

- **Chave:** `"<ano>|<título>"` (recomendada; o consumidor não depende dela).
- **Cabeçalho `awards-run-id`:** o publicador da aplicação põe o `runId` da execução em cada evento. É
  informativo, o consumidor não depende dele e um cliente externo não precisa enviá-lo. Os testes usam o
  cabeçalho para descobrir o `runId` (research R10).
- **Valor:** objeto JSON em UTF-8.

```json
{
  "year": 2020,
  "title": "Exemplo de Filme",
  "studios": "Estúdio A, Estúdio B",
  "producers": "Producer A, Producer B and Producer C",
  "winner": true
}
```

| Campo | Tipo | Obrigatório | Regra |
| --- | --- | --- | --- |
| `type` | string | não | ausente ou `"movie"` |
| `year` | integer | sim | 1 a 9999 |
| `title` | string | sim | não vazio depois de retirar os espaços em volta |
| `studios` | string ou null | não | vazio equivale a ausente |
| `producers` | string | sim | mesmo formato da coluna do CSV: vírgula, ` and ` e `, and` separam nomes (spec 001, FR-007 a FR-010); precisa gerar ao menos um nome |
| `winner` | boolean | sim | `true` para vencedor, `false` para indicado |

Campos não listados são ignorados.

**Efeito:** o filme identificado por `year` + `title` é criado ou, se já existir, passa a ter os valores
do evento (spec 003, FR-014). Receber o mesmo evento de novo não muda nada.

## Evento de carga concluída

- **Chave:** `"load-completed"`.
- **Valor:**

```json
{ "type": "load-completed", "runId": "3f0c2a8e-6d1b-4a59-9a63-0f7e1f4d2b10", "count": 206 }
```

Só a aplicação que gerou o `runId` muda de estado ao consumi-lo: a readiness passa a 200 (FR-013). Um
evento com outro `runId` é ignorado. Um cliente externo não tem motivo para enviar este evento.

## Registro no tópico de erros

- **Chave:** a do evento original.
- **Valor:** os bytes originais, sem mudança.
- **Cabeçalhos:**

| Cabeçalho | Exemplo |
| --- | --- |
| `awards-error-reason` | `YEAR_OUT_OF_RANGE: ano fora da faixa de 1 a 9999` |
| `awards-error-at` | `2026-11-24T18:03:11.402Z` |
| `awards-source-topic` | `awards.movies` |
| `awards-source-partition` | `0` |
| `awards-source-offset` | `207` |

**Motivos:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, `RUN_ID_MISSING`, `FIELD_TOO_LONG`, `YEAR_MISSING`,
`YEAR_NOT_NUMERIC`, `YEAR_OUT_OF_RANGE`, `TITLE_MISSING`, `PRODUCERS_MISSING` e `WINNER_INVALID`
(data-model.md).

## Garantias

| Garantia | Como |
| --- | --- |
| Nenhuma duplicata | Gravação por ano + título, com atualização (research R5) |
| Evento inválido não trava o consumo | Vai ao tópico de erros e o offset avança (R9) |
| Falha de banco não perde evento | Novas tentativas sem avançar o offset (R9) |
| Readiness só com a carga completa | Indicador `ingestion` liberado pelo `runId` desta execução (R10) |

## Exemplo de uso no compose

Pelo serviço `kafka-cli` (perfil `tools` do compose):

```bash
printf '%s\n' '2020|Exemplo de Filme:{"year":2020,"title":"Exemplo de Filme","studios":"Estúdio A","producers":"Joel Silver","winner":true}' \
  | docker compose run --rm -T kafka-cli /opt/kafka/bin/kafka-console-producer.sh \
      --bootstrap-server kafka:9092 --topic awards.movies \
      --property parse.key=true --property key.separator=:

docker compose run --rm kafka-cli /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 --topic awards.movies.errors --from-beginning \
  --property print.headers=true --timeout-ms 5000
```
