# Implementation Plan: Marco 2 — Operação: ETag, observabilidade, imagem, pipeline e ADRs

**Branch**: `marco/2-operacao` | **Date**: 2026-11-23 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-operacao/spec.md`

## Summary

O Marco 2 acrescenta a camada de operação à API do Marco 1, sem mudar o corpo de nenhuma resposta 200.

- **ETag:** o `ShallowEtagHeaderFilter` do Spring, só nos dois endpoints de negócio, dá ETag forte
  derivado do corpo e 304 na revalidação (R4). O contrato OpenAPI vai a 1.1.0, com o cabeçalho, o
  parâmetro e a resposta 304 (R5).
- **Saúde e métricas:** Spring Boot Actuator, com liveness e readiness ligados e sem detalhes, e
  Micrometer com o registro do Prometheus, com histograma de latência por rota (R1 a R3). A readiness
  fica positiva no `ApplicationReadyEvent`, que o ciclo de vida garante depois da carga.
- **Logs:** logging estruturado nativo do Boot. Texto no `./mvnw`; JSON ECS com
  `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`, que a imagem define (R6). Os testes de log rodam nos dois
  formatos, numa segunda execução do Failsafe.
- **Cobertura:** JaCoCo sobre o Failsafe, com mínimo de 90% de linhas no `./mvnw verify` (R7).
- **Imagem:** Dockerfile multi-stage, JDK e JRE Temurin 25 Alpine fixados por digest, usuário 10001,
  healthcheck na readiness e `.dockerignore` por lista de permissão (R8).
- **Pipeline:** GitHub Actions com `setup-java` e cache do Maven, CodeQL, Trivy para dependências e
  imagem (só com correção), hadolint, build da imagem e teste de fumaça. Actions por hash de commit e
  Dependabot para Maven, Actions e Docker (R9 a R14).
- **ADRs:** doze ADRs curtos em `docs/adr/`, comprovados por revisão (R15).
- **Entrega:** um único bolt no branch `marco/2-operacao`, com seis etapas por assunto (ETag,
  observabilidade, Dockerfile, pipeline e Dependabot, ADRs e README), um commit por etapa, um
  responsável por etapa (implementador, infraestrutura ou documentação) e um único PR (R16).
- **README:** atualizado com o que o marco acrescenta (FR-034), comprovado por revisão.

## Technical Context

**Language/Version**: Java 25 (OpenJDK 25.0.2), sem mudança

**Primary Dependencies**: as do Marco 1 (Spring Boot 4.1.2), mais `spring-boot-starter-actuator` e
`io.micrometer:micrometer-registry-prometheus` (versões do Boot). Plugin `jacoco-maven-plugin` 0.8.14.
Nenhuma dependência nova para o ETag (filtro do Spring) nem para o JSON (logging estruturado do Boot)

**Storage**: H2 em memória, sem mudança no esquema

**Testing**: JUnit 5, `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `RestTestClient` e
`OutputCaptureExtension`, como no Marco 1. Duas execuções do Failsafe: a padrão e a `json-logs`, com
`LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`. JaCoCo com `check` de 90% de linhas. O teste do container é um
script bash no pipeline, fora do `./mvnw verify`

**Target Platform**: JVM 25 em qualquer sistema operacional pelo `./mvnw`; imagem Linux
(`eclipse-temurin:25-jre-alpine`); runners `ubuntu-latest` do GitHub Actions

**Project Type**: web-service (API REST, módulo Maven único), mais arquivos de infraestrutura

**Performance Goals**: sem mudança nos endpoints (SC-005 da spec 001). Teste de fumaça em menos de
2 minutos depois do build da imagem (SC-006)

**Constraints**: `./mvnw` sem Docker (Princípio II); corpo das respostas 200 inalterado (Princípio III);
actions por hash e imagens por digest; nenhum segredo no pipeline; nenhuma publicação de imagem

**Scale/Scope**: 2 endpoints de negócio e 4 operacionais; 6 jobs de CI; 12 ADRs

Nenhum item ficou como NEEDS CLARIFICATION. Os pontos que dependem do comportamento exato do Boot 4.1.2
estão marcados como **verificar** na [research.md](research.md) (R1, R3 e R8), com o que fazer em cada
caso.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Como o plano atende | Pré-design | Pós-design |
| --- | --- | --- | --- |
| I. Somente testes de integração | Todos os testes novos são `*IT` com `@SpringBootTest` e contexto completo, verificando por HTTP (ETag, probes, métricas). O ordenamento carga → pronta usa a aplicação real com `SpringApplication` e um ouvinte real de evento (R2). O 503 é provocado pela API pública de disponibilidade do Spring, sem dublê. Os testes de log continuam verificando o log, porque o log é o próprio comportamento (FR-017 a FR-020). Nenhum mock | ✅ | ✅ |
| II. Execução só com `./mvnw` | Actuator, ETag e JaCoCo rodam no `./mvnw`. O JSON é ligado só por variável. Docker, hadolint, Trivy e CodeQL ficam só no pipeline. O `./mvnw verify` não usa container (FR-027) | ✅ | ✅ |
| III. Contrato idêntico ao enunciado | Os schemas dos corpos não mudam. O contrato 1.1.0 só acrescenta cabeçalho, parâmetro e a resposta 304. O validador continua com `additionalProperties: false`, e o `ReferenceFileIT` continua passando sem alteração | ✅ | ✅ |
| IV. Spec-anchored | A spec 002 foi escrita e clarificada antes do plano, com as emendas 1.3.0 e 1.4.0. Os pontos "verificar" da research voltam para a research, e para a spec se mudarem comportamento | ✅ | ✅ |
| V. Extras isolados e simplicidade (1.4.1) | Sem Kafka, PostgreSQL nem autenticação. ETag, probes e métricas são extras sem infraestrutura própria e ficam ligados no `./mvnw`, como o Princípio V esclarecido em 1.4.1 permite, porque não alteram o comportamento avaliado: a suíte do Marco 1 passa sem mudar nenhum resultado esperado (SC-002). A imagem e o pipeline, que têm infraestrutura própria, ficam fora do modo padrão. Nenhum código próprio onde o framework resolve (filtro de ETag, Actuator, logging estruturado). Sem tracing, Kubernetes nem imagem nativa | ✅ | ✅ |
| VI. Rastreabilidade (1.4.0) | Matriz abaixo, por tipo: comportamento → IT; pipeline e imagem → job do pipeline; documentação → item do checklist | ✅ | ✅ |
| VII. Sem segredos; config por ambiente | O pipeline não usa segredos. `PORT`, `MOVIES_CSV_PATH` e `LOGGING_STRUCTURED_FORMAT_CONSOLE` vêm do ambiente, com padrões que fazem o modo padrão funcionar sem nada definido | ✅ | ✅ |
| Observabilidade (1.3.0) | Probes de liveness e readiness, readiness só depois da carga, texto no `./mvnw`, JSON por variável definida na imagem, testes de log nos dois formatos | ✅ | ✅ |
| Cadeia de entrega | Actions por hash de commit (job `workflow-policy`), imagem multi-stage e não root, imagens base por digest | ✅ | ✅ |

**Resultado:** nenhuma violação, e o "Complexity Tracking" não se aplica.

## Project Structure

### Documentation (this feature)

```text
specs/002-operacao/
├── plan.md                  # Este arquivo
├── research.md              # Fase 0 (R1 a R16)
├── data-model.md            # Fase 1
├── quickstart.md            # Fase 1
├── contracts/
│   ├── openapi.yaml         # contrato de negócio 1.1.0 (copiado para src/main/resources/static)
│   ├── operational-endpoints.md
│   ├── container.md
│   └── ci-pipeline.md
├── checklists/
│   └── requirements.md      # inclui "Revisão dos ADRs"
└── tasks.md                 # Fase 2 (/speckit-tasks)
```

### Source Code (repository root)

Só o que muda ou é novo; o resto é o do Marco 1 (plano 001).

```text
pom.xml                               # actuator, prometheus, jacoco, execução json-logs do Failsafe
Dockerfile                            # novo (R8)
.dockerignore                         # novo, lista de permissão (R8)
.github/
├── workflows/ci.yml                  # novo (R9 a R12)
├── scripts/
│   ├── check-workflows.sh            # novo: política do workflow e do Dependabot (R12, FR-028b, FR-029)
│   ├── check-docker-context.sh       # novo: contexto de build x .dockerignore (FR-026)
│   └── smoke-test.sh                 # novo (R14)
└── dependabot.yml                    # novo (R13)
docs/adr/
├── README.md                         # índice
└── 0001-…md a 0012-…md               # R15
src/main/java/com/outsera/awards/
└── web/
    └── EtagConfiguration.java        # FilterRegistrationBean do ShallowEtagHeaderFilter (R4)
src/main/resources/
├── application.yml                   # management.* (R1, R3); banner desligado; nada de logging estruturado (R6)
└── static/openapi.yaml               # cópia de specs/002-operacao/contracts/openapi.yaml (R5)
src/test/java/com/outsera/awards/
├── support/
│   ├── LogOutput.java                # novo: lê texto e JSON ECS (R6)
│   ├── PrometheusText.java           # novo: lê o texto do Prometheus (MetricsIT)
│   ├── LoaderLog.java                # passa a devolver mensagens, via LogOutput
│   ├── ApplicationStarter.java       # Result.messages(), runAndCall e ouvintes
│   └── ContractValidator.java        # repassa os cabeçalhos e valida o 304
├── etag/        IntervalsEtagIT, MoviesEtagIT, EtagEmptyIT, EtagAcrossRunsIT
├── health/      HealthProbesIT, ReadinessInvalidLinesIT, ProbeStatesIT, ReadinessAfterLoadIT
├── metrics/     MetricsIT
├── logging/     TextLogFormatIT, JsonLogFormatIT
├── docs/        ApiDocsIT (aponta para a spec 002), IntervalsContractIT, MoviesContractIT (304)
└── ingestion/   StartupFailureIT e ITs com LoaderLog ganham @Tag("log")
```

**Structure Decision:** o código novo de produção é uma classe de configuração em `web/`, porque Actuator,
métricas e logs são só configuração. Os testes novos ficam em pacotes por assunto, como no Marco 1. Os
arquivos de infraestrutura ficam na raiz e em `.github/`, de dono único por rodada (R16).

## Rastreabilidade (requisito → comprovação)

Pelo Princípio VI (1.4.0): comportamento → IT no `./mvnw verify`; pipeline e imagem → job do pipeline;
documentação → item do checklist.

| Requisitos | Comprovação | Tipo |
| --- | --- | --- |
| FR-001, FR-003, FR-004, FR-005 | `IntervalsEtagIT`, `MoviesEtagIT`; `ReferenceFileIT` e a suíte do Marco 1 sem alteração (FR-005) | IT |
| FR-002 | `MoviesEtagIT` (um ETag por combinação), `EtagAcrossRunsIT` (mesmo arquivo → mesmo ETag; arquivo diferente → outro ETag, em execuções separadas) | IT |
| FR-006 | `MoviesEtagIT` (400 com `If-None-Match`), `IntervalsEtagIT` (405 com `If-None-Match`) | IT |
| FR-007 | `IntervalsContractIT`, `MoviesContractIT` (200 com `ETag` e 304 validados), `ApiDocsIT` (contrato publicado igual ao da spec 002) | IT |
| FR-008, FR-011 | `HealthProbesIT` (200, corpo só com `status`, endpoints não expostos dão 404), `ProbeStatesIT` (503) | IT |
| FR-009 | `ReadinessAfterLoadIT` | IT |
| FR-010 | `ReadinessInvalidLinesIT` (cenário `invalid-lines.csv`), `ReadinessAfterLoadIT` (arquivo ausente: o evento de pronta nunca acontece) | IT |
| FR-012 a FR-016 | `MetricsIT` (um método por requisito e por cenário da história 3) | IT |
| FR-017, FR-019 | `JsonLogFormatIT` (execução `json-logs`) | IT |
| FR-018 | os ITs com `@Tag("log")` nas duas execuções do Failsafe | IT |
| FR-020 | `TextLogFormatIT` (padrão), `JsonLogFormatIT` (`json-logs`) | IT |
| FR-021, FR-022, FR-023, FR-024, FR-024a, FR-025 | job `image`: build e passos 1 a 10, 12 e 13 do `smoke-test.sh`; job `dockerfile-lint` | Pipeline |
| FR-026 | job `image`: `check-docker-context.sh` (passo 11) | Pipeline |
| Caso de borda (arquivo montado sem permissão de leitura) | job `image`: passo 13 do `smoke-test.sh` | Pipeline |
| FR-027 | job `build`: `./mvnw verify` sem Docker | Pipeline |
| FR-028 | jobs `build`, `codeql`, `dependencies`, `dockerfile-lint` e `image` | Pipeline |
| FR-028a | jobs `codeql`, `dependencies` e `image` (portas de severidade); `build` (`check` do JaCoCo, também local) | Pipeline |
| FR-028b | job `workflow-policy` (`check-workflows.sh`: hash de commit, `permissions`, nenhum `secrets.`, nenhuma publicação) | Pipeline |
| FR-029 | job `workflow-policy` (`check-workflows.sh`: `dependabot.yml` com `maven`, `github-actions` e `docker`) | Pipeline |
| FR-030, FR-031 | seção "Revisão dos ADRs" de `checklists/requirements.md` | Revisão |
| FR-034 | seção "Revisão do README" de `checklists/requirements.md` | Revisão |
| FR-032, FR-033 | esta matriz; `./mvnw verify` sem Docker | IT e revisão |
| SC-001 | `IntervalsEtagIT`, `MoviesEtagIT`, suíte do Marco 1 | IT |
| SC-002 | `./mvnw verify` (job `build`) | Pipeline |
| SC-003 | `ReadinessAfterLoadIT` | IT |
| SC-004 | `MetricsIT` (séries por `uri` e `outcome` e faixas do histograma); consultas de `contracts/operational-endpoints.md` | IT |
| SC-005 | `JsonLogFormatIT`, `TextLogFormatIT`; passo 5 do teste de fumaça | IT e pipeline |
| SC-006 | job `image` (tempo medido) | Pipeline |
| SC-007 | job `workflow-policy` | Pipeline |
| SC-008 | PR de verificação com um defeito por categoria, no fechamento do marco | Pipeline |
| SC-009 | seção "Revisão dos ADRs" | Revisão |
| Casos de borda de ETag (fraco, malformado, `*`, lista, página além da última, arquivo só com cabeçalho) | `IntervalsEtagIT`, `MoviesEtagIT`, `EtagEmptyIT` | IT |
| Casos de borda de log (aspas, quebra de linha e acentos no JSON) | `JsonLogFormatIT` | IT |

## Riscos

| Risco | Mitigação |
| --- | --- |
| Logback global: um contexto com JSON muda o formato dos testes seguintes na mesma JVM | Execução `json-logs` do Failsafe em JVM própria (R6); nenhum IT liga o JSON por `@TestPropertySource` |
| O `./mvnw verify` fica mais lento com a segunda execução | Só as classes com `@Tag("log")` rodam de novo. Se o tempo total passar de 3 min, reduzir o conjunto aos testes que verificam mensagens |
| Nomes de propriedades ou módulos diferentes no Boot 4 (R1, R3) | Itens "verificar" na research, confirmados pelo primeiro teste de cada etapa, com alternativa já escolhida |
| `wget` do BusyBox não falhar num 503 (R8) | Verificado na etapa 4. Se não falhar, usar `wget --spider` ou conferir o corpo `"UP"` |
| Um PR único fica grande para revisar | Um commit por etapa e por assunto (R16), e a matriz de rastreabilidade acima, para o revisor percorrer o PR etapa a etapa |
| Limite de download do banco do Trivy no runner | Cache do banco por dia (`actions/cache`) |
| Code scanning indisponível em repositório privado ou em PR de fork (R9, R10) | Envio do SARIF condicionado; a porta de severidade roda sobre o SARIF local |
| Action de terceiros comprometida (cadeia de entrega de 2026) | Hash de commit conferido no repositório oficial, job `workflow-policy` e atualização só pelo Dependabot, com revisão |
| Action composta chamando outra por tag (ataque de 2026 ao `setup-trivy`) | Referências aninhadas da `trivy-action` verificadas (v0.36.0 toda por hash, R10). Se uma versão nova perder isso, troca pelo binário oficial com checksum (T042) |
| O passo 13 do teste de fumaça (arquivo sem permissão) não se aplicar em todo host | Sonda antes do passo: pulado com motivo fora do pipeline, falha no pipeline (research R14) |
| Contextos novos (Actuator) deixarem a cobertura perto do mínimo | O mínimo é 90%, com 97,06% medidos. O código novo é configuração coberta pelos ITs de ETag, saúde e métricas |

## Complexity Tracking

Não se aplica: nenhuma violação da constituição.
