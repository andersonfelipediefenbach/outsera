# Imagem da API (spec 002, FR-021 a FR-026; research R8; contracts/container.md).
# Caminho alternativo: o projeto continua subindo e testando só com ./mvnw, sem Docker (FR-027).
# Os testes não rodam aqui: rodam no job `build` do pipeline.

# Estágio de build: JDK 25 (Temurin, Alpine), fixado por digest (tag 25-jdk-alpine).
FROM eclipse-temurin:25-jdk-alpine@sha256:22edbb1021420ae6e248519d1af07577742314d67d4c60fcbb2e285b7e47fa78 AS build
WORKDIR /build

# Dependências numa camada própria, com cache do Maven entre builds.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN --mount=type=cache,target=/root/.m2 sh ./mvnw -B -q dependency:go-offline

COPY src/main src/main
# JAR sem testes, extraído em camadas para o JarLauncher. As quatro pastas são criadas sempre, porque o
# extract não cria a de uma camada vazia (por exemplo, sem dependências SNAPSHOT).
RUN --mount=type=cache,target=/root/.m2 sh ./mvnw -B -q -Dmaven.test.skip=true package \
    && java -Djarmode=tools -jar target/awards-*.jar extract --layers --launcher --destination extracted \
    && mkdir -p extracted/dependencies extracted/spring-boot-loader extracted/snapshot-dependencies extracted/application

# Estágio de execução: só a JRE 25 (Temurin, Alpine), fixada por digest (tag 25-jre-alpine).
FROM eclipse-temurin:25-jre-alpine@sha256:69c2a26e9894016e274c880fa542956d005b3755f84db765145185e87405bd89

# Usuário sem privilégios e sem shell de login (FR-022).
RUN addgroup -S -g 10001 app \
    && adduser -S -D -H -u 10001 -G app -s /sbin/nologin app

WORKDIR /app
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

# PORT com o mesmo padrão do modo padrão (FR-024). O log sai em JSON ECS no container; quem roda pode
# sobrescrever a variável (FR-020, FR-024a). MOVIES_CSV_PATH não é definido: vale o arquivo embarcado.
ENV PORT=8080 \
    LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs
EXPOSE 8080

USER 10001:10001

# Saudável só quando a readiness responde 200, ou seja, depois da carga do CSV (FR-009, FR-023). Notação
# JSON com o shell explícito: o sh -c expande o PORT a cada verificação (hadolint DL3025).
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
    CMD ["sh", "-c", "wget -q -O /dev/null \"http://127.0.0.1:${PORT}/actuator/health/readiness\" || exit 1"]

# Forma exec: a JVM é o PID 1 e recebe o SIGTERM direto, com desligamento gracioso (FR-025). A memória
# vai aqui, e não em JAVA_TOOL_OPTIONS, que faria a JVM escrever uma linha fora do log (research R8).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
