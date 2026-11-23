# Contrato: imagem de container

Interface da imagem construída pelo `Dockerfile` da raiz (research R8). A imagem não é publicada neste
marco (FR-028b).

## Construção

```bash
docker build -t awards:local .
```

- Contexto restrito pelo `.dockerignore` a `mvnw`, `.mvn/`, `pom.xml` e `src/main/` (FR-026).
- Os testes não rodam no build da imagem. Eles rodam no estágio `build` do pipeline.

## Execução

| Item | Valor | Requisito |
| --- | --- | --- |
| Usuário | UID e GID 10001 (`app`), nunca root | FR-022 |
| Porta | `PORT`, padrão `8080` (`EXPOSE 8080`) | FR-024 |
| Arquivo de filmes | `MOVIES_CSV_PATH`, padrão o arquivo embarcado (`classpath:movielist.csv`). Aceita `file:/caminho` ou um caminho sem prefixo | FR-024 |
| Formato do log | `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` definido na imagem (JSON). Vazio ou ausente, o log é texto | FR-020, FR-024a |
| Memória | `-XX:MaxRAMPercentage=75` no `ENTRYPOINT`. Nada em `JAVA_TOOL_OPTIONS`, porque a JVM escreveria `Picked up JAVA_TOOL_OPTIONS` fora do log, quebrando o JSON | SC-005 |
| Banner | desligado (`spring.main.banner-mode: off`, no `application.yml`, em todas as execuções) | FR-020, SC-005 |
| Healthcheck | `GET http://127.0.0.1:${PORT}/actuator/health/readiness` a cada 10 s, tempo-limite 3 s, período inicial 30 s, 3 tentativas | FR-023 |
| Sinal de parada | `SIGTERM` direto à JVM (PID 1), desligamento gracioso | FR-025 |
| Código de saída | diferente de zero quando a inicialização falha (por exemplo, arquivo ausente) | FR-025 |

## Exemplos

```bash
# Arquivo embarcado, porta padrão
docker run --rm -p 8080:8080 awards:local

# Outro arquivo, montado só para leitura, e outra porta
docker run --rm -p 9090:9090 -e PORT=9090 \
  -v "$PWD/src/test/resources/scenarios/disjoint-producers.csv:/data/movies.csv:ro" \
  -e MOVIES_CSV_PATH=file:/data/movies.csv awards:local

# Log em texto dentro do container
docker run --rm -p 8080:8080 -e LOGGING_STRUCTURED_FORMAT_CONSOLE= awards:local
```
