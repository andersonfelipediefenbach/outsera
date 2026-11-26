# Contrato: pipeline do Marco 4

Complementa `specs/003-modo-docker/contracts/ci-pipeline.md`. Os jobs do Marco 3 continuam, com as
mudanças abaixo. Toda action nova é fixada por hash de commit, com as referências aninhadas conferidas
(spec 002, R10). Toda imagem nova é fixada por tag e digest.

## Jobs novos

| Job | Depende de | Permissões | O que faz | Falha quando | Requisitos |
| --- | --- | --- | --- | --- | --- |
| `alert-rules` | — | `contents: read` | `promtool check rules`, `check config` e `test rules`, com a imagem do Prometheus do compose | regra inválida ou teste de regra falhando | FR-004, FR-006, FR-008, SC-002 |
| `load` | `dockerfile-lint` | `contents: read` | gera o CSV duas vezes e compara os SHA-256; mede a carga direta; sobe o compose com o CSV gerado; mede a carga por Kafka; roda o k6; publica o resumo e os artefatos; derruba o compose | hashes diferentes; linha rejeitada; readiness fora do tempo; limite do k6 ultrapassado | FR-010 a FR-019, SC-004 a SC-006 |
| `supply-chain` | `dockerfile-lint` | `contents: read`, `id-token: write` | registro temporário; build com push só para `localhost:5000`; SBOM; `cosign sign` e `attest`; verificação com a identidade certa e com uma errada | qualquer passo; a verificação com identidade errada passando | FR-024 a FR-027, SC-008 |
| `devcontainer` | — | `contents: read` | constrói o dev container e roda dentro dele `java -version`, `./mvnw -v`, `docker version`, `./mvnw -B verify`, `docker compose up -d --build --wait` e a conferência do resultado de referência | qualquer comando | FR-028 a FR-030, SC-009 (as portas encaminhadas ficam na revisão) |

O `supply-chain` tem `if:` que o pula em pull request de fork (FR-027).

## Jobs alterados

| Job | Mudança | Requisitos |
| --- | --- | --- |
| `workflow-policy` | `check-workflows.sh`: `id-token: write` só no `supply-chain`; push só no `supply-chain` e só para `localhost:5000/`; Dependabot com `devcontainers`. `check-unchanged-tests.sh` também contra `v0.3.0` | FR-001, FR-026, FR-028 |
| `dependencies` | analisa `target/runtime-deps` (copiado pelo `maven-dependency-plugin`, escopo runtime) em vez do `pom.xml`; resumo com as versões de `lz4-java` e `snappy-java` | FR-033, SC-010 |
| `image` | imagem com o cache AOT; teste de fumaça sem mudança de passos; medição da subida sem e com o cache (7 repetições, mediana no resumo; reprova se a mediana com cache não for menor); subida com um cache cortado até a readiness, com o aviso da JVM no log; aviso no resumo quando o `supply-chain` é pulado | FR-020 a FR-023, FR-027, SC-007 |
| `compose` | passo novo: regras carregadas sem erro em `/api/v1/rules` e nenhum alerta `slo` disparando em `/api/v1/alerts` | FR-009, SC-003 |

## Checks obrigatórios da `main` depois do marco (FR-031)

`workflow-policy`, `build`, `postgres`, `codeql`, `dependencies`, `dockerfile-lint`, `image`, `compose`,
`alert-rules`, `load`, `supply-chain`, `devcontainer`. Bypass para administradores desligado.

## Scripts

| Script | Uso | Saída |
| --- | --- | --- |
| `.github/scripts/load-test.sh` | `load-test.sh <imagem>` | resumo do k6 e tempos de carga em `$GITHUB_STEP_SUMMARY`; artefatos em `target/load/` |
| `.github/scripts/startup-benchmark.sh` | `startup-benchmark.sh <imagem> [repetições]` | tabela com cada tempo e a mediana sem e com o cache |
| `.github/scripts/compose-test.sh` | sem mudança de uso | passo novo de alertas |

Os scripts seguem o padrão do Marco 2: `set -euo pipefail`, `OK`/`FALHA`/`PULADO` por passo, e `PULADO`
é falha com `CI=true`.
