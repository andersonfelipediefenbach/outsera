# Quickstart: validação do Marco 3

Roteiro para conferir o marco de ponta a ponta. Os formatos estão nos contratos:
[eventos](contracts/kafka-events.md), [operacional](contracts/operational-endpoints.md),
[compose](contracts/compose.md) e [pipeline](contracts/ci-pipeline.md).

## 1. Modo padrão: só o JDK (Princípio II)

```bash
./mvnw verify
./mvnw spring-boot:run
curl -s localhost:8080/api/producers/intervals
```

Esperado:
- o `verify` passa sem Docker, com os testes dos Marcos 1 e 2, de Kafka (broker embutido) e de
  segurança, e com a cobertura mínima de 90%;
- os intervalos trazem Joel Silver em `min` e Matthew Vaughn em `max`;
- o log da subida não tem `Using generated security password` nem linhas do cliente Kafka;
- `curl -sI localhost:8080/api/movies` mostra o `ETag` e nenhum `Cache-Control`.

## 2. Autenticação ligada, ainda só com o JDK

O valor do token de demonstração está **só** no README, na seção "Autenticação" (definido no T088,
Princípio VII). Exporte-o antes:

```bash
export API_TOKEN='<valor da seção Autenticação do README>'
./mvnw spring-boot:run

curl -si localhost:8080/api/producers/intervals                    # 401 em Problem Details
curl -si -H 'Authorization: Bearer errado' localhost:8080/api/movies # 401, mesmo corpo
curl -s  -H "Authorization: Bearer $API_TOKEN" localhost:8080/api/producers/intervals  # 200
curl -si localhost:8080/actuator/prometheus                        # 401
curl -si localhost:8080/actuator/health/readiness                  # 200, sem token
curl -si localhost:8080/swagger-ui.html                            # 200, sem token
```

## 3. Perfil de PostgreSQL (precisa de um PostgreSQL com collation C)

O pipeline faz isto no job `postgres`. Localmente, com Docker:

```bash
docker run -d --name awards-pg -p 5432:5432 \
  -e POSTGRES_DB=awards -e POSTGRES_USER=awards -e POSTGRES_HOST_AUTH_METHOD=trust \
  -e POSTGRES_INITDB_ARGS='--locale=C --encoding=UTF8' postgres:18-alpine

DB_URL=jdbc:postgresql://localhost:5432/awards DB_USER=awards DB_PASSWORD= \
  ./mvnw -Ppostgres-it verify

docker rm -f awards-pg
```

Esperado: os `*PostgresIT` passam. São eles:
- esquema e resultado de referência;
- equivalência entre H2 e PostgreSQL;
- ordem da listagem;
- três reinícios sem duplicata.

## 4. Ambiente completo

```bash
docker compose up -d --build --wait
curl -s localhost:8080/api/producers/intervals
```

- Grafana: `http://localhost:3000/d/awards` (leitura sem login; admin com a senha de demonstração do
  README).
- Prometheus: `http://localhost:9090/targets` (alvo `app` em `UP`).

Publicar um vencedor novo e conferir:

```bash
printf '%s\n' '2030|Filme de Teste:{"year":2030,"title":"Filme de Teste","studios":null,"producers":"Joel Silver","winner":true}' \
  | docker compose run --rm -T kafka-cli /opt/kafka/bin/kafka-console-producer.sh \
      --bootstrap-server kafka:9092 --topic awards.movies \
      --property parse.key=true --property key.separator=:
curl -s 'localhost:8080/api/movies?year=2030'
```

Esperado: o filme aparece em até 5 segundos.

Publicar um evento inválido e ler o tópico de erros:

```bash
printf '%s\n' 'x:{"year":0,"title":"Ruim","producers":"A","winner":true}' \
  | docker compose run --rm -T kafka-cli /opt/kafka/bin/kafka-console-producer.sh \
      --bootstrap-server kafka:9092 --topic awards.movies \
      --property parse.key=true --property key.separator=:
docker compose run --rm kafka-cli /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 --topic awards.movies.errors --from-beginning \
  --property print.headers=true --timeout-ms 5000
```

Esperado: o registro aparece com `awards-error-reason:YEAR_OUT_OF_RANGE…`, e o dashboard mostra um
evento rejeitado.

Persistência:

```bash
docker compose down          # sem -v: recria todos os containers e mantém os volumes (US5-5)
docker compose up -d --wait
curl -s 'localhost:8080/api/movies?year=2030'   # o filme continua, sem duplicatas
docker compose down -v
```

## 5. Pipeline

No PR do marco, todos os jobs ficam verdes:
- `workflow-policy`, com a política nova e os ITs dos Marcos 1 e 2 sem alteração desde a `v0.2.0`;
- `build`;
- `postgres`;
- `compose`;
- `codeql`, `dependencies`, `dockerfile-lint` e `image`.
