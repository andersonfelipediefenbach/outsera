# Decisões de arquitetura (ADRs)

Registros curtos das decisões de arquitetura do projeto, no formato de Michael Nygard. Cada ADR tem
status, data, contexto, decisão, alternativas consideradas e consequências, e cita a entrada
correspondente em [`docs/ai/decisoes.md`](../ai/decisoes.md), onde fica o registro completo do que foi
aceito, rejeitado e por quê.

- A numeração é sequencial, com quatro dígitos, e um número nunca é reutilizado.
- Uma decisão revista não é apagada: o ADR antigo passa a "Substituído por NNNN", e um ADR novo explica
  a mudança.

| Nº | Decisão | Status | Data |
| --- | --- | --- | --- |
| [0001](0001-stack-java-spring-boot-maven.md) | Stack: Java 25, Spring Boot 4.1.2 e Maven com wrapper | Aceito | 2026-11-18 |
| [0002](0002-h2-jpa-flyway.md) | H2 com JPA e esquema por Flyway, com SQL portável | Aceito | 2026-11-18 |
| [0003](0003-carga-do-csv-na-inicializacao.md) | Carga do CSV na inicialização: falha rápida e linha inválida ignorada | Aceito | 2026-11-18 |
| [0004](0004-somente-testes-de-integracao.md) | Somente testes de integração no back-end, sem Mockito | Aceito | 2026-11-19 |
| [0005](0005-contrato-openapi-estatico.md) | Contrato OpenAPI estático como fonte única, com o Swagger UI pelo webjar | Aceito | 2026-11-22 |
| [0006](0006-problem-details.md) | Erros em Problem Details (RFC 9457) | Aceito | 2026-11-18 |
| [0007](0007-etag-pelo-conteudo.md) | ETag derivado do conteúdo | Aceito | 2026-11-23 |
| [0008](0008-liveness-e-readiness.md) | Liveness e readiness pelo Actuator, com a readiness depois da carga | Aceito | 2026-11-23 |
| [0009](0009-metricas-prometheus.md) | Métricas no formato do Prometheus, com histograma de latência | Aceito | 2026-11-23 |
| [0010](0010-formato-do-log.md) | Log em texto no `./mvnw` e JSON por variável, ligado na imagem | Aceito | 2026-11-23 |
| [0011](0011-imagem-de-container.md) | Imagem multi-stage, JRE Alpine por digest e usuário não root | Aceito | 2026-11-23 |
| [0012](0012-pipeline-e-cadeia-de-entrega.md) | Pipeline com actions por hash, portas de segurança e cobertura e Dependabot | Aceito | 2026-11-23 |
| [0013](0013-postgresql-com-esquema-comum.md) | PostgreSQL no modo Docker, com o mesmo esquema e collation C | Aceito | 2026-11-24 |
| [0014](0014-ingestao-unica-por-kafka.md) | Kafka como caminho único de ingestão no modo Docker | Aceito | 2026-11-24 |
| [0015](0015-gravacao-idempotente-com-atualizacao.md) | Gravação idempotente por ano e título, com atualização | Aceito | 2026-11-24 |
| [0016](0016-topico-de-erros-e-novas-tentativas.md) | Tópico de erros para eventos inválidos e novas tentativas para falhas de gravação | Aceito | 2026-11-24 |
| [0017](0017-autenticacao-por-token.md) | Autenticação por token com Spring Security, desligada por padrão | Aceito | 2026-11-24 |
| [0018](0018-compose-com-observabilidade.md) | Compose com PostgreSQL, Kafka nativo, Prometheus e Grafana | Aceito | 2026-11-24 |
| [0019](0019-testes-com-postgresql-em-perfil-dedicado.md) | Testes que dependem do PostgreSQL num perfil Maven dedicado | Aceito | 2026-11-24 |

A comprovação destes registros é a revisão do pull request, pela seção "Revisão dos ADRs" do checklist de
cada spec: [`specs/002-operacao/checklists/requirements.md`](../../specs/002-operacao/checklists/requirements.md)
para os ADRs 0001 a 0012 e [`specs/003-modo-docker/checklists/requirements.md`](../../specs/003-modo-docker/checklists/requirements.md)
para os ADRs 0013 a 0019 e a atualização do 0004 (Princípio VI).
