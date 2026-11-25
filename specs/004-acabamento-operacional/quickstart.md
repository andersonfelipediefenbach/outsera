# Quickstart: validação do Marco 4

Roteiro para conferir o marco de ponta a ponta. Os detalhes de cada interface estão em
[contracts/operacao.md](contracts/operacao.md) e [contracts/ci-pipeline.md](contracts/ci-pipeline.md).

## 1. Modo padrão, só com o JDK (FR-001, SC-001)

```bash
./mvnw verify
./mvnw spring-boot:run
curl -s http://localhost:8080/api/producers/intervals
```

Esperado: `verify` verde; resultado de referência (Joel Silver em `min`, 1990–1991; Matthew Vaughn em
`max`, 2002–2015), como no Marco 3. O arquivo carregado é o `movielist.csv`.

## 2. Gerador de CSV (FR-014 a FR-017, SC-005)

```bash
mkdir -p target/generated-data
java tools/csv-generator/GenerateMovies.java --output target/generated-data/a.csv
java tools/csv-generator/GenerateMovies.java --output target/generated-data/b.csv
sha256sum target/generated-data/a.csv target/generated-data/b.csv
java tools/csv-generator/GenerateMovies.java --movies 0 --output target/generated-data/x.csv; echo $?
```

Esperado: os dois hashes iguais, e iguais ao hash publicado no README (também no Windows, pelo Git Bash);
o último comando sai com 2 e não cria `x.csv`.

```bash
MOVIES_CSV_PATH=target/generated-data/a.csv ./mvnw spring-boot:run
```

Esperado: resumo da carga com zero rejeitadas; readiness 200; intervalos com `min` e `max` não vazios, com
os empates e o intervalo 0 plantados.

## 3. Imagem com o cache AOT (FR-020 a FR-023, SC-007)

```bash
docker build -t awards:local .
.github/scripts/smoke-test.sh awards:local
.github/scripts/startup-benchmark.sh awards:local 7
```

Esperado: fumaça com todos os passos `OK`; tabela com a mediana com o cache menor que sem ele.

## 4. Compose com alertas (FR-004 a FR-009, SC-003)

```bash
docker compose up -d --build --wait
docker compose run --rm --no-deps --entrypoint promtool prometheus test rules /etc/prometheus/rules/slo.test.yml
curl -s http://localhost:9090/api/v1/rules | jq '.data.groups[].rules[] | {name, health}'
curl -s http://localhost:9090/api/v1/alerts | jq '.data.alerts'
docker compose stop app   # espere 1 a 2 min
curl -s http://localhost:9090/api/v1/alerts | jq '.data.alerts[] | {labels, state}'
docker compose down
```

Esperado: testes das regras `SUCCESS`; regras com `health: ok`; nenhum alerta antes de parar a aplicação;
`AwardsAppDown` em `firing` depois, com o `runbook_url` levando à seção certa de `docs/runbook.md`.

## 5. Teste de carga (FR-010 a FR-013, SC-004)

```bash
java tools/csv-generator/GenerateMovies.java --output target/generated-data/movies.csv
docker compose -f compose.yaml -f compose.load.yaml up -d --build --wait
docker compose --profile load run --rm k6
docker compose down -v
```

Esperado: saída 0, resumo com p95, p99, vazão e erro por endpoint, dentro dos limites. Para conferir que o
limite reprova, rode de novo com um limite impossível (variável documentada no roteiro, por exemplo
`K6_P95_INTERVALS_MS=1`): saída 99.

## 6. Pipeline (FR-024 a FR-027, FR-030, FR-033, SC-008 a SC-011)

No PR do marco, conferir:

- os doze checks verdes (lista em [contracts/ci-pipeline.md](contracts/ci-pipeline.md));
- no `supply-chain`: o digest, o índice do Rekor, a verificação com a identidade certa `OK` e com a errada
  falhando como esperado; o SBOM nos artefatos;
- no `dependencies`: `lz4-java` e `snappy-java` listados com as versões efetivas;
- no `load`: os dois hashes do gerador e os tempos de carga;
- no `image`: as medianas da subida.

## 7. Dev container (FR-028, FR-029, SC-009)

Abrir o repositório no VS Code com "Reopen in Container" (ou `devcontainer up --workspace-folder .`) e,
no terminal do container:

```bash
java -version && ./mvnw -v && docker version
./mvnw verify
docker compose up -d --build --wait && curl -s http://localhost:8080/api/producers/intervals
```

Esperado: JDK 25; `verify` verde; resultado de referência; portas 8080, 9090 e 3000 acessíveis da máquina.

## 8. Verificações únicas, fora do código do marco

- **Trivy contra versão vulnerável (FR-034):** branch temporário `verificacao/trivy-transitivas`, com
  `lz4-java` 1.11.2 → job `dependencies` vermelho; o branch é apagado; nada dele volta ao marco
  ([research.md](research.md#r12-por-que-o-job-dependencies-não-apontou-as-transitivas-do-kafka-clients-fr-032-e-fr-033)).
- **Testador adversário (FR-035):** acionado pelo Anderson; falhas viram ITs em
  `src/test/java/com/outsera/awards/kafka/adversario/`.
- **Proteção da `main` (FR-031):** configurada pelo Anderson e registrada em `docs/ai/decisoes.md`.
