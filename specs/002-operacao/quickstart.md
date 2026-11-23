# Quickstart: validação do Marco 2

Roteiro para conferir o marco de ponta a ponta. Os passos 1 a 5 usam só o JDK 25. O passo 6 precisa de
Docker e é opcional fora do pipeline. Os comandos estão em bash; no Windows, use `mvnw.cmd` e o
`curl.exe`.

## 1. Testes e cobertura, sem Docker

```bash
./mvnw verify
```

**Esperado:**

- as duas execuções do Failsafe passam: a padrão (log em texto) e a `json-logs` (research R6);
- o `check` do JaCoCo passa, com cobertura de linhas de pelo menos 90% (FR-028a). O relatório fica em
  `target/site/jacoco-it/index.html`;
- nenhum container é iniciado (FR-027).

## 2. Aplicação no modo padrão

```bash
./mvnw spring-boot:run
```

**Esperado:** o log está em texto, como no Marco 1, com o resumo da carga e sem o banner (FR-020).

## 3. ETag

```bash
curl -i http://localhost:8080/api/producers/intervals
# 200, corpo do Marco 1 (Joel Silver em min, Matthew Vaughn em max) e cabeçalho ETag

curl -i -H 'If-None-Match: <ETag recebido>' http://localhost:8080/api/producers/intervals
# 304, sem corpo, mesmo ETag

curl -i -H 'If-None-Match: "outro"' http://localhost:8080/api/producers/intervals
# 200 com corpo

curl -i 'http://localhost:8080/api/movies?winner=true&size=5'
# 200 com ETag; repetir com If-None-Match dá 304; mudar size dá outro ETag

curl -i -H 'If-None-Match: *' 'http://localhost:8080/api/movies?winner=talvez'
# 400 em Problem Details, sem ETag
```

Requisitos: FR-001 a FR-006. Contrato: [contracts/openapi.yaml](contracts/openapi.yaml).

## 4. Saúde e métricas

```bash
curl -i http://localhost:8080/actuator/health/liveness    # 200 {"status":"UP"}
curl -i http://localhost:8080/actuator/health/readiness   # 200 {"status":"UP"}
curl -i http://localhost:8080/actuator/health             # 200 {"status":"UP"}, sem detalhes
curl -i http://localhost:8080/actuator/env                # 404

curl -s http://localhost:8080/actuator/prometheus | grep 'http_server_requests_seconds_count'
# séries por uri, method, status e outcome, com application="awards"
```

Requisitos: FR-008 a FR-016. Contrato: [contracts/operational-endpoints.md](contracts/operational-endpoints.md).

## 5. Log em JSON sem Docker

```bash
LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs ./mvnw spring-boot:run
```

**Esperado:** cada linha do log é um objeto JSON com `@timestamp`, `log.level`, `log.logger`,
`process.thread.name` e `message` (FR-017, FR-020). O resumo da carga continua na `message`.

## 6. Imagem (opcional, com Docker)

```bash
docker build -t awards:local .
docker run --rm -p 8080:8080 awards:local
```

**Esperado:**

- log em JSON sem nenhuma variável extra (FR-024a);
- `docker ps` mostra `healthy` depois da carga (FR-023);
- `docker exec <id> id -u` mostra 10001 (FR-022);
- `curl http://localhost:8080/api/producers/intervals` dá o resultado de referência.

Mais exemplos em [contracts/container.md](contracts/container.md). O mesmo roteiro, automatizado, é o
teste de fumaça do pipeline ([contracts/ci-pipeline.md](contracts/ci-pipeline.md)).

## 7. Pipeline

Abrir um pull request e conferir os jobs `workflow-policy`, `build`, `codeql`, `dependencies`,
`dockerfile-lint` e `image` verdes. Para o SC-008, um branch temporário, que nunca é mergeado e é
apagado depois, recebe um defeito por categoria, e cada um deve deixar vermelho só o job
correspondente. O resultado fica registrado em `docs/ai/decisoes.md`.

## 8. ADRs

Na revisão do PR do marco, no commit da etapa de documentação (R16), percorrer a seção "Revisão dos ADRs" de
[checklists/requirements.md](checklists/requirements.md).
