# Quickstart — validação do Marco 1

Guia para comprovar, de ponta a ponta, que a funcionalidade atende à [spec](spec.md). Os contratos estão
em [contracts/openapi.yaml](contracts/openapi.yaml), e as regras de validação em
[data-model.md](data-model.md).

## Pré-requisitos

- Só o JDK 25 no `PATH`. Não é preciso Maven, Docker nem banco instalados.
- O arquivo `movielist.csv` do enunciado em `src/main/resources/`.

## 1. Testes de integração (FR-026, FR-027)

```bash
./mvnw verify          # Linux/macOS/Git Bash
mvnw.cmd verify        # Windows (cmd/PowerShell)
```

Resultado esperado: `BUILD SUCCESS`, com todos os `*IT` executados pelo Failsafe e nenhum teste
unitário.

## 2. Subir com o arquivo padrão (FR-001, FR-002, SC-003)

```bash
./mvnw spring-boot:run
```

No log aparece `Carga do arquivo classpath:movielist.csv concluída: N linhas lidas, N aceitas,
0 rejeitadas`.

## 3. Intervalos (FR-017, SC-001)

```bash
curl -s http://localhost:8080/api/producers/intervals
```

Resposta esperada:

```json
{"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}],
 "max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]}
```

## 4. Outro arquivo (FR-002, FR-016a, SC-004a)

```bash
MOVIES_CSV_PATH=src/test/resources/scenarios/disjoint-producers.csv ./mvnw spring-boot:run
curl -s http://localhost:8080/api/producers/intervals
```

Resultado esperado: só `Producer A` (2, 1950, 1952) em `min` e `Producer B` (50, 1960, 2010) em `max`.

Falhas esperadas na inicialização (SC-006), cada uma com o bloco "APPLICATION FAILED TO START" e o
motivo:

- `MOVIES_CSV_PATH=/nao/existe.csv` dá `NOT_FOUND`;
- `MOVIES_CSV_PATH=src/test/resources/scenarios/comma-separated.csv` dá `INVALID_SEPARATOR`;
- `MOVIES_CSV_PATH=src/test/resources/scenarios/header-wrong-order.csv` dá `INVALID_HEADER`.

## 5. Listagem e erros (FR-019 a FR-023)

```bash
curl -s "http://localhost:8080/api/movies?year=1990&winner=true"
curl -s "http://localhost:8080/api/movies?page=0&size=5"
curl -si "http://localhost:8080/api/movies?winner=yes"   # 400, application/problem+json
```

## 6. Nenhum dado fixo no código (SC-004b)

```bash
grep -rnE "Joel Silver|Matthew Vaughn|19[89][0-9]|20[0-2][0-9]" src/main/java && echo "FALHOU" || echo "OK"
```

Resultado esperado: `OK`.

## 7. Documentação (FR-024)

- Abra `http://localhost:8080/swagger-ui.html`: os dois endpoints aparecem, com exemplos.
- `http://localhost:8080/openapi.yaml` é idêntico a `specs/001-producer-intervals-api/contracts/openapi.yaml`.