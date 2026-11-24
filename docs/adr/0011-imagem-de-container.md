# 0011. Imagem multi-stage, JRE Alpine por digest e usuário não root

- **Status:** Aceito
- **Data:** 2026-11-23
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Decisões de arquitetura sem entrada
  própria (ADRs)", seção "Imagem de container", e "Correções da segunda análise da spec 002" (H1); research
  R8 da spec 002.

## Contexto

O Marco 2 pede um Dockerfile multi-stage, com usuário não root e healthcheck, como caminho alternativo:
o projeto continua subindo e testando só com o `./mvnw`. A imagem é a base da entrega dos Marcos 4 e 5.

## Decisão

- Build em `eclipse-temurin:25-jdk-alpine` e execução em `eclipse-temurin:25-jre-alpine`, as duas fixadas
  por digest.
- O JAR é extraído em camadas para o `JarLauncher`. Os testes não rodam no build da imagem, e sim no
  pipeline.
- Usuário 10001, sem shell de login. `PORT=8080` e `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` definidos.
- Healthcheck na readiness, com o `wget` da imagem, em notação JSON com `sh -c` explícito, que expande
  o `PORT` e atende à regra DL3025 do hadolint.
- `ENTRYPOINT` em forma exec, com `-XX:MaxRAMPercentage=75` no próprio `ENTRYPOINT`. Com
  `JAVA_TOOL_OPTIONS`, a JVM escreveria uma linha fora do log, e o JSON quebraria.
- `.dockerignore` por lista de permissão: só `mvnw`, `.mvn/`, `pom.xml` e `src/main/`.

## Alternativas consideradas

- Distroless: não tem shell nem `wget`, e o healthcheck exigiria uma segunda JVM a cada verificação.
- Runtime gerado por `jlink`: imagem menor, mas a lista de módulos precisa de manutenção.
- Buildpacks (`spring-boot:build-image`): não produzem o Dockerfile pedido e exigem Docker no build do
  Maven.
- Rodar o `./mvnw verify` dentro do build da imagem: duplicaria os testes do pipeline.

## Consequências

- O build é reprodutível, e o Dependabot atualiza os digests.
- A JVM é o PID 1 e recebe o `SIGTERM` direto, com desligamento gracioso.
- Um arquivo novo no repositório fica fora do contexto por padrão, e o pipeline confere isso.
