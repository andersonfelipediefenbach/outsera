# Implementation Plan: Marco 4 — Acabamento operacional: alertas de SLO, carga, dados gerados, cache AOT, assinatura e dev container

**Branch**: `marco/4-acabamento` | **Date**: 2026-11-25 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-acabamento-operacional/spec.md`

## Summary

O Marco 4 fecha a operação e a cadeia de entrega sem tocar no modo padrão: nenhuma mudança em `src/main`,
no `pom.xml` ou nos `application*.yml`, salvo as correções que o testador adversário exigir (R13).

- **SLO e alertas:** os dois SLOs do endpoint de intervalos usam a métrica que já existe desde o Marco 2.
  A latência usa um bucket existente do histograma (R1). Os alertas medem a taxa de consumo do orçamento em
  várias janelas, mais o `AwardsAppDown`, cada um com o link do runbook (R2). As regras ficam em
  `observability/prometheus/rules/`, montadas no Prometheus do compose. O `promtool` da mesma imagem
  verifica e testa as regras no job `alert-rules`, e o `compose-test.sh` confere que elas carregaram e não
  disparam (R3).
- **Teste de carga:** k6 como serviço `k6` do compose no perfil `load`, com limites por endpoint e uma
  métrica de erros de contrato (R4). Os limites e o SLO vêm de ao menos 5 execuções no runner compartilhado,
  com folga proposta de 2× nos limites do pipeline, registrada em `docs/ai/decisoes.md` (R5, FR-011a).
- **Dados gerados:** `tools/csv-generator/GenerateMovies.java`, de arquivo único, roda com o `java` do JDK e
  usa `java.util.Random(seed)`. A saída é UTF-8 com `\n`, escrita de forma atômica, com os empates e o
  intervalo 0 plantados (R6). O job `load` gera o arquivo duas vezes e compara os SHA-256. Ele também mede
  a carga direta e a carga por Kafka com 100 mil filmes (R7).
- **Cache AOT:** só o da JVM 25 (clarificação). O `extract` deixa de usar o `--launcher` (classpath de
  JARs), o treino roda no estágio final com `spring.context.exit=onRefresh` e o `ENTRYPOINT` passa a usar
  `-XX:AOTCache`. O `startup-benchmark.sh` mede a mesma imagem com `AOTMode=off` e com o cache, em 7
  repetições, e publica a mediana (R8).
- **SBOM e assinatura:** o SBOM CycloneDX vem do Trivy (R9). O job `supply-chain` usa um registro
  temporário em `localhost:5000`, faz o `cosign sign` e o `attest` keyless e verifica com a identidade certa
  e com uma errada. Só ele tem `id-token: write` e só ele faz push, e só para `localhost:5000/`. A política
  do workflow é ampliada para isso, com casos que precisam falhar (R10).
- **Dev container:** imagem de Java 25 fixada por digest, com docker-in-docker, o `./mvnw` e as portas
  encaminhadas. O job `devcontainer` roda o `verify` dentro dele, e o Dependabot ganha o ecossistema
  `devcontainers` (R11).
- **Pendências do Marco 3:**
  - o job `dependencies` passa a analisar as dependências de runtime copiadas pelo Maven, e não o
    `pom.xml`. A causa da lacuna é investigada e registrada, e a correção é comprovada num branch
    temporário apagado (R12);
  - o testador adversário roda na ingestão por Kafka (R13);
  - a proteção da `main` passa a ter 12 checks e é registrada (R14).
- **Entrega:** um bolt e um PR, em seis etapas:
  1. pendências de código (testador adversário e Trivy);
  2. gerador e teste de carga;
  3. SLO e alertas, com os números da etapa 2;
  4. cache AOT;
  5. SBOM e assinatura;
  6. dev container, ADRs, README e registros.

## Technical Context

**Language/Version**: Java 25 (OpenJDK 25.0.2) na API e no gerador; JavaScript (ES2015+, runtime do k6) no
roteiro de carga; bash nos scripts do pipeline; YAML do Prometheus

**Primary Dependencies**: sem dependência nova no `pom.xml`. Ferramentas novas, todas fixadas:
- `grafana/k6` (imagem, tag e digest, serviço do compose);
- `registry:3` (imagem, digest, *service container* do `supply-chain`);
- `sigstore/cosign-installer` e `devcontainers/ci` (actions por hash);
- `mcr.microsoft.com/devcontainers/java` e o recurso `docker-in-docker`, ambos por digest;
- `promtool` da imagem do Prometheus que já está no compose;
- Trivy, pela action que já está no pipeline, também para o SBOM

**Storage**: sem mudança (H2 no modo padrão; PostgreSQL no compose). O CSV gerado fica em
`target/generated-data/`, fora do controle de versão

**Testing**:
- `./mvnw verify` sem mudança, mais os ITs do testador adversário, se houver falhas;
- `promtool test rules` para os alertas;
- k6 com limites para a carga;
- scripts bash no pipeline para o gerador, a subida, a assinatura e o dev container;
- sem teste unitário na API

**Target Platform**: runners `ubuntu-latest` (compartilhados); Docker Compose v2; JVM 25 em qualquer
sistema operacional para o `./mvnw` e o gerador; dev container em VS Code ou no `devcontainer` CLI

**Project Type**: web-service (API REST, módulo Maven único), mais infraestrutura de observabilidade,
cadeia de entrega e ferramentas do repositório

**Performance Goals**:
- objetivos do SLO e limites do k6 definidos pela medição (R5);
- subida com cache mais rápida que sem ele, pela mediana (SC-007);
- carga de 100 mil filmes sem linha rejeitada nos dois caminhos (SC-006)

**Constraints**:
- `./mvnw` sem Docker (Princípio II);
- contrato de intervalos idêntico (Princípio III);
- os ITs dos Marcos 1 a 3 sem alteração (`check-unchanged-tests.sh` contra `v0.2.0` e `v0.3.0`);
- nenhuma publicação de imagem;
- actions por hash e imagens por digest;
- nenhum segredo (a assinatura é keyless)

**Scale/Scope**: 3 alertas e 2 SLOs; 1 roteiro de carga com 2 cenários; 1 gerador; 4 jobs novos e 4
alterados; 1 dev container; 6 ADRs novos

Nenhum item ficou como NEEDS CLARIFICATION. As duas escolhas de escopo foram clarificadas na spec
(2026-11-25). Os pontos que dependem do comportamento exato das versões estão marcados como **verificar**
na [research.md](research.md) (R3, R5, R7, R8, R10 e R11), cada um com o que fazer se o resultado for
outro.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Princípio | Como o plano atende | Pré-design | Pós-design |
| --- | --- | --- | --- |
| I. Somente testes de integração | Nenhum teste unitário novo na API. O gerador é ferramenta do repositório, fora do back-end, comprovado pelo pipeline e pela carga real (spec, Assumptions). As falhas do testador adversário viram ITs com o broker embutido, sem mocks, verificados por HTTP (R13) | ✅ | ✅ |
| II. Execução só com `./mvnw` | Nada em `src/main`, `pom.xml` ou `application*.yml` muda para os extras. O k6, o `promtool`, o cosign e o dev container rodam só no compose, na imagem ou no pipeline. O gerador precisa só do JDK | ✅ | ✅ |
| III. Contrato idêntico | O contrato não muda. O k6 confere o formato exato em toda resposta (FR-012). A imagem com o cache passa no mesmo teste de fumaça, com o resultado de referência | ✅ | ✅ |
| IV. Spec-anchored | Duas clarificações e três ajustes do responsável entraram na spec antes do plano. Correção do testador que mude comportamento atualiza a spec 003 antes do código (R13) | ✅ | ✅ |
| V. Extras isolados, simplicidade | k6 no perfil `load` do compose; o Alertmanager fica de fora; o cache é só da JVM (sem AOT do Spring, que fixaria perfis); a assinatura fica sem publicação; o SBOM sai da ferramenta que já existe; nenhum plugin novo no `pom.xml` | ✅ | ✅ |
| VI. Rastreabilidade (1.7.0) | Matriz abaixo. Modo padrão: suíte existente. Correções do adversário: IT no `./mvnw verify`. Regras, carga, gerador, imagem, assinatura, dev container e Trivy: jobs. SLO, runbook, README, ADRs e registros: checklist da spec | ✅ | ✅ |
| VII. Sem segredos | Nenhum `secrets.*`: a assinatura usa o token OIDC da execução. O k6 usa o token de demonstração só quando `API_TOKEN` é definido. O dev container não define credenciais. Nenhum dado pessoal nos registros | ✅ | ✅ |
| Observabilidade (1.3.0) | Readiness e log sem mudança. O aviso da JVM com cache inválido só aparece nesse caso (R8, verificar) | ✅ | ✅ |
| Cadeia de entrega | Actions novas por hash, com as referências aninhadas conferidas; imagens novas por digest; imagem multi-stage com usuário não root mantida. A política só passa a permitir `id-token: write` e push para `localhost:5000/`, e só no `supply-chain`, com casos negativos (R10) | ✅ | ✅ |

**Resultado:** nenhuma violação. A ampliação da política do workflow é uma mudança do `check-workflows.sh`
(Marco 2) e não da constituição. A regra "as actions fixadas por hash, a imagem multi-stage e não root" não
muda. O "Complexity Tracking" não se aplica.

## Rastreabilidade (Princípio VI)

| Requisito | Comprovação |
| --- | --- |
| FR-001, FR-002, SC-001 | job `build` (`./mvnw -Pextras verify`, suíte sem mudança; o `./mvnw verify` padrão roda só os ITs do modo padrão, constituição 2.0.0); `workflow-policy` (`check-unchanged-tests.sh` contra `v0.2.0` e `v0.3.0`) |
| FR-003, FR-007, FR-011a | checklist "Revisão da documentação" (SLO, runbook e registro da medição e da folga) |
| FR-004, FR-006, FR-008, SC-002 | job `alert-rules` |
| FR-005 | `slo.test.yml` (5xx ruim; 2xx, 304 e 4xx bons; alvo fora) no job `alert-rules` |
| FR-009, SC-003 | passo novo do `compose-test.sh`, job `compose` |
| FR-010 a FR-013, SC-004 | job `load` (k6 com limites); verificação única com limite impossível, registrada |
| FR-014 a FR-016, FR-019, SC-005, SC-006 | job `load` (dois SHA-256 iguais; carga sem linhas rejeitadas nos dois caminhos) |
| FR-017 | job `build` (suíte sem mudança com o `movielist.csv`); `check-docker-context.sh` (o gerado fora da imagem) |
| FR-018 | job `load` (tempos no resumo); checklist (números no README) |
| FR-020, FR-021 | job `image` (fumaça) e job `compose` (perfil docker com a mesma imagem) |
| FR-023 | job `image` (passo de cache inválido do `startup-benchmark.sh`) |
| FR-022, SC-007 | job `image` (`startup-benchmark.sh`); checklist (números no README) |
| FR-024 a FR-027, SC-008 | job `supply-chain`; `workflow-policy` (política ampliada, com os casos negativos testados) |
| FR-028, FR-030, SC-009 | job `devcontainer` (`verify`); `workflow-policy` (Dependabot `devcontainers`) |
| FR-029 | job `devcontainer` (compose pronto e resultado de referência dentro do container); checklist (portas encaminhadas para a máquina) |
| FR-031, SC-011 | checklist (registro da proteção) e página do PR com os 12 checks obrigatórios |
| FR-032, FR-034 | checklist (causa, evidência e verificação única em branch apagado) |
| FR-033, SC-010 | job `dependencies` (inventário com `lz4-java` e `snappy-java` no resumo) |
| FR-035 | ITs `S004` em `kafka/adversario/` no job `build`; checklist (registro do resultado) |
| FR-036, FR-037 | checklist "Revisão da documentação" |

## Project Structure

### Documentation (this feature)

```text
specs/004-acabamento-operacional/
├── plan.md                 # Este arquivo
├── research.md             # Fase 0 (R1 a R14)
├── data-model.md           # Fase 1
├── quickstart.md           # Fase 1
├── contracts/
│   ├── ci-pipeline.md      # jobs novos e alterados, checks obrigatórios
│   └── operacao.md         # gerador, compose, alertas, imagem, assinatura, dev container
├── checklists/
│   └── requirements.md     # inclui "Revisão da documentação"
└── tasks.md                # Fase 2 (/speckit-tasks)
```

### Source Code (repository root)

Só o que muda ou é novo.

```text
.devcontainer/
└── devcontainer.json                      # novo (R11)
.github/
├── dependabot.yml                         # + devcontainers
├── workflows/ci.yml                       # + alert-rules, load, supply-chain, devcontainer; dependencies, image, workflow-policy alterados
└── scripts/
    ├── check-workflows.sh                 # regras 3, 5 e 6 ampliadas (R10, R11)
    ├── compose-test.sh                    # + passo dos alertas (R3)
    ├── load-test.sh                       # novo (R4 a R7)
    └── startup-benchmark.sh               # novo (R8)
Dockerfile                                 # extract sem --launcher, treino AOT, ENTRYPOINT (R8)
compose.yaml                               # + volume das regras no prometheus; + serviço k6 (perfil load)
compose.load.yaml                          # novo: CSV gerado montado no app
load/
└── intervals.js                           # novo (R4)
observability/prometheus/
├── prometheus.yml                         # + rule_files
└── rules/
    ├── slo.yml                            # novo (R1, R2)
    └── slo.test.yml                       # novo (R3)
tools/csv-generator/
└── GenerateMovies.java                    # novo (R6)
docs/
├── slo.md                                 # novo (papel de documentação)
├── runbook.md                             # novo (papel de documentação)
├── adr/0020 a 0025-*.md                   # novos (papel de documentação)
└── ai/decisoes.md                         # registros de R5, R12, R13, R14
README.md                                  # seção do Marco 4 (papel de documentação)
src/test/java/com/outsera/awards/kafka/adversario/   # só se o testador adversário achar falhas (R13)
```

ADRs propostos:
- 0020: SLO e alertas por taxa de consumo do orçamento;
- 0021: teste de carga com k6 no pipeline, com a folga sobre o runner compartilhado;
- 0022: gerador de CSV determinístico;
- 0023: cache AOT da JVM, sem o AOT do Spring;
- 0024: SBOM e assinatura keyless num registro temporário;
- 0025: dev container e inventário efetivo no job de dependências.

**Structure Decision**: módulo Maven único, sem mudança. As ferramentas ficam em pastas próprias na raiz
(`tools/`, `load/`, `.devcontainer/`), fora do build do Maven e do contexto da imagem. O `.dockerignore`
é uma lista de permissão e já as exclui.

**Propriedade dos arquivos (AGENTS.md):**
- **Implementador:** as correções do testador adversário (`src/`).
- **Infraestrutura e SRE:** `Dockerfile`, `compose*.yaml`, `observability/`, `.github/`, `.devcontainer/`,
  `load/` e `tools/`.
- **Documentação:** `docs/slo.md`, `docs/runbook.md`, `docs/adr/` e `README.md`.

Os registros em `docs/ai/decisoes.md` são de quem fez cada verificação.

## Ordem das etapas

1. **Pendências de código:**
   - pedido ao Anderson para acionar o testador adversário (R13);
   - investigação e correção do job `dependencies` (R12);
   - pedido da verificação única em branch temporário.
2. **Gerador e carga:**
   - gerador e `compose.load.yaml`;
   - roteiro do k6 com limites provisórios;
   - job `load`;
   - 5 execuções no runner. É o primeiro ponto que exige push, que é do Anderson.
3. **SLO e alertas:**
   - números a partir da etapa 2;
   - regras e testes das regras;
   - job `alert-rules` e passo novo do `compose-test.sh`;
   - limites finais do k6;
   - registro da medição e da folga.
4. **Cache AOT:** Dockerfile, `startup-benchmark.sh` e as mudanças do job `image`.
5. **SBOM e assinatura:** política ampliada, com os casos negativos, e o job `supply-chain`.
6. **Fechamento:**
   - dev container e job `devcontainer`;
   - página do SLO, runbook, ADRs e README;
   - pedido da proteção da `main` com os 12 checks (R14);
   - `./mvnw verify` final e Status da spec.

A etapa 3 depende da 2, porque os números vêm da medição. As etapas 4, 5 e 6 são independentes entre si.

## Decisões do responsável sobre o plano (2026-11-25)

A registrar em `docs/ai/decisoes.md`, no commit da etapa 1.

1. **Limite do SLO de latência:** é o bucket que já existe no histograma, cerca de 112 ms
   (`le="0.111848106"`), sem mudar o `application.yml` (R1).
2. **Folga do pipeline:** os limites do k6 são 2× a maior medição de cada percentil, arredondados para
   cima (R5).
3. **Execuções de medição:** as 5 são feitas com "Re-run jobs" do job `load` no mesmo commit, sem
   commits vazios (R5).
4. **Etapas:** cada etapa termina com o `./mvnw verify` e o `./mvnw -Pextras verify` verdes e um commit (constituição 2.0.0).

## Complexity Tracking

Não se aplica: nenhuma violação da constituição.
