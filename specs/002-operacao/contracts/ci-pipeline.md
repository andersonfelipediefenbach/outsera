# Contrato: pipeline de CI

Workflow `.github/workflows/ci.yml` (research R9 a R14). Este contrato diz o que cada job garante e o que
o reprova. Pelo Princípio VI (1.4.0), a execução desses jobs é a comprovação dos requisitos de pipeline e
de imagem.

## Gatilhos e permissões

- `push` em qualquer branch e `pull_request`.
- Token com `contents: read` por padrão. Só o job `codeql` ganha `security-events: write`.
- Nenhum segredo é usado. Nenhuma imagem é publicada.
- Uma execução nova no mesmo ref cancela a anterior.

## Jobs

| Job | Reprova quando | Relata sem reprovar | Requisitos |
| --- | --- | --- | --- |
| `workflow-policy` | `.github/scripts/check-workflows.sh` encontra: `uses:` sem hash de commit de 40 caracteres; `permissions` do topo diferente de `contents: read`; escrita além de `security-events: write`, ou essa escrita fora do job `codeql`; referência a `secrets.`; `push: true` ou `docker push`; `dependabot.yml` ausente, com `version` diferente de 2 ou sem os ecossistemas `maven`, `github-actions` e `docker` | — | FR-028b, FR-029, SC-007 |
| `build` | `./mvnw -B verify` falha: compilação, qualquer IT das duas execuções do Failsafe ou cobertura de linhas abaixo de 90% | total de cobertura no resumo do job; relatórios do JaCoCo e do Failsafe como artefatos | FR-028, FR-028a, FR-027, SC-002, SC-008 |
| `codeql` | resultado com `security-severity` ≥ 7,0 (alta ou crítica) | demais resultados, no code scanning e no resumo | FR-028, FR-028a |
| `dependencies` | vulnerabilidade `HIGH` ou `CRITICAL` **com versão corrigida** nas dependências Maven | tabela completa, todas as severidades, inclusive sem correção | FR-028a, SC-008 |
| `dockerfile-lint` | hadolint com nível `warning` ou `error` | níveis `info` e `style` | FR-028, SC-008 |
| `image` | o contexto de build tem algo fora da lista de permissão (`check-docker-context.sh`); build da imagem falha; vulnerabilidade `HIGH` ou `CRITICAL` **com correção** na imagem; qualquer passo do teste de fumaça falha | tabela completa da imagem | FR-021 a FR-026, FR-024a, FR-028, FR-028a, SC-005, SC-006, SC-008 |

## Teste de fumaça (job `image`)

Script `.github/scripts/smoke-test.sh`. Cada passo que falha reprova o job. O script também roda
localmente, no Linux e no Git Bash do Windows com Docker Desktop. Um passo só pode ser **pulado** fora
do pipeline, com a mensagem `PULADO` e o motivo. No pipeline (`CI=true`), um passo pulado é falha.

| Passo | Verificação | Requisito |
| --- | --- | --- |
| 1 | container fica `healthy` em até 90 s | FR-023 |
| 2 | `GET /api/producers/intervals` igual ao resultado de referência | US5-3 |
| 3 | mesma chamada com `If-None-Match` do `ETag` recebido → 304 | FR-003 |
| 4 | `id -u` no container ≠ 0 | FR-022 |
| 5 | toda linha do `docker logs` é JSON | FR-024a, SC-005 |
| 6 | `docker stop` termina em menos de 10 s | FR-025 |
| 7 | com `PORT=9090`, fica `healthy` e responde nessa porta | FR-024 |
| 8 | com `MOVIES_CSV_PATH` inexistente, termina com código ≠ 0 e a mensagem do Marco 1 | FR-025 |
| 9 | com um CSV de cenário montado, responde o resultado desse cenário | US5-5 |
| 10 | na imagem final, com `--entrypoint sh`: `/app/src`, `/build` e `/app/pom.xml` não existem, e `mvn` e `javac` não estão no `PATH` | FR-021, US5-1 |
| 11 | o contexto de build, inspecionado por `check-docker-context.sh`, tem só `mvnw`, `.mvn/`, `pom.xml` e `src/main/` | FR-026 |
| 12 | com `-e LOGGING_STRUCTURED_FORMAT_CONSOLE=`, fica `healthy`, e a primeira linha do log não é JSON | FR-024a |
| 13 | com uma cópia do CSV com `chmod 000` montada em `/data/movies.csv`, termina com código ≠ 0 e a mensagem de arquivo ilegível (`UNREADABLE`). Antes, uma sonda com o usuário 10001 confere se a permissão do host vale no container. Se não valer (por exemplo, NTFS no Git Bash do Windows ou Docker Desktop), o passo é pulado localmente, com o motivo, e falha no pipeline | caso de borda, FR-025 |
| — | do fim do build à resposta correta do passo 2, menos de 2 minutos | SC-006 |

## Dependabot

`.github/dependabot.yml`: `maven`, `github-actions` e `docker`, na raiz, semanalmente (research R13,
FR-029).
