# Data Model: Marco 4 — Acabamento operacional

**Spec**: [spec.md](spec.md) | **Research**: [research.md](research.md)

O marco não muda o esquema do banco, as entidades JPA nem o contrato da API. As "entidades" abaixo são
artefatos versionados ou gerados pelo pipeline, com os campos e as regras que os testes e as revisões
conferem.

## Objetivo de SLO (`docs/slo.md`)

| Campo | Regra |
| --- | --- |
| `indicador` | `disponibilidade` ou `latência`, ambos de `GET /api/producers/intervals` |
| `alvo` | disponibilidade: 99,5%; latência: fração de requisições até o limite de um bucket do histograma (R1), valor exato do bucket na página |
| `janela` | 28 dias, móvel |
| `boa` / `ruim` | disponibilidade: ruim = 5xx ou alvo sem coleta; boa = 2xx, 304 e 4xx (FR-005). Latência: boa = duração até o limite |
| `origem` | medição do job `load` em runner compartilhado (R5), com link para a entrada em `docs/ai/decisoes.md` |
| `limite do pipeline` | valor do k6 e a folga sobre a medição, com o motivo (FR-011a) |

## Regra de alerta (`observability/prometheus/rules/slo.yml`)

| Campo | Regra |
| --- | --- |
| `alert` | `AwardsIntervalsAvailabilityBudgetBurn`, `AwardsIntervalsLatencyBudgetBurn`, `AwardsAppDown` |
| `expr` | taxa de consumo do orçamento em duas janelas (R2), sobre regras de gravação `awards:intervals_*:ratio_rate<janela>` |
| `for` | 2 min nos alertas de consumo; 1 min no `AwardsAppDown` |
| `labels.severity` | `page` ou `ticket` |
| `labels.slo` | `intervals-availability` ou `intervals-latency` |
| `annotations.summary`, `description` | objetivo em risco e valor atual |
| `annotations.runbook_url` | `.../docs/runbook.md#<âncora-do-alerta>`, e a âncora existe no runbook (revisão) |

Relação: cada regra pertence a exatamente um objetivo de SLO; cada objetivo tem ao menos uma regra.

## Teste de regra (`observability/prometheus/rules/slo.test.yml`)

| Campo | Regra |
| --- | --- |
| `input_series` | séries sintéticas de `http_server_requests_seconds_count` e `_bucket` e de `up` |
| `alert_rule_test` | por alerta: ≥ 1 caso com o alerta `firing` e ≥ 1 caso sem alerta (dentro do objetivo e sem tráfego) |

## Cenário de carga (`load/intervals.js`)

| Campo | Regra |
| --- | --- |
| `scenarios` | `intervals` e `movies`, `constant-arrival-rate`, aquecimento de 15 s fora dos limites |
| `tags.endpoint` | `intervals` ou `movies` em toda requisição |
| `thresholds` | por endpoint: `http_req_failed`, `p(95)` e `p(99)` de `http_req_duration`, `contract_errors`; valores de R5 |
| `BASE_URL`, `API_TOKEN` | variáveis de ambiente; o token é opcional |
| saída | `summary.json` e `summary.md` |

## Arquivo gerado

| Campo | Regra |
| --- | --- |
| parâmetros | `--movies` (1 a 2.000.000; padrão 100.000), `--seed` (padrão 20261125), `--output` |
| formato | UTF-8 sem BOM, `\n`, separador `;`, cabeçalho `year;title;studios;producers;winner` |
| chave | (`year`, `title`) única, sem duplicatas |
| casos plantados | dois produtores com duas vitórias no mesmo ano (empate em `min`, intervalo 0); dois com vitórias em 1900 e 2099 (empate em `max`) |
| invariante | mesmo `--movies` e `--seed` → mesmo SHA-256 em qualquer sistema operacional |
| local | fora do controle de versão (`target/generated-data/`), nunca embarcado na imagem |

## Medição

| Campo | Regra |
| --- | --- |
| `tipo` | `subida-sem-cache`, `subida-com-cache`, `carga-direta`, `carga-kafka`, `k6` |
| `repetições` | subida: 7 de cada, alternadas; carga: 1 por caminho por execução; k6: ≥ 5 execuções para o SLO |
| `valor` | segundos até a readiness 200 (subida e carga); p95, p99, vazão e erro (k6) |
| `resumo` | mediana (subida), valor (carga), menor, maior e mediana (k6) |
| `ambiente` | tipo do runner e link da execução |

## SBOM e assinatura

| Campo | Regra |
| --- | --- |
| SBOM | CycloneDX JSON da imagem, gerado pelo Trivy; contém `lz4-java` e `snappy-java` nas versões efetivas |
| alvo | `localhost:5000/awards@sha256:<digest>`, registro descartado no fim do job |
| assinatura | keyless, certificado do Fulcio, entrada no Rekor |
| atestado | tipo `cyclonedx`, predicado = SBOM |
| identidade aceita | `https://github.com/<repositório>/.github/workflows/ci.yml@<ref>`, emissor `https://token.actions.githubusercontent.com` |

Estados do job `supply-chain`: `pulado` (fork, com o motivo no resumo) → ou `construído` → `enviado ao
registro temporário` → `assinado` → `atestado` → `verificado` (identidade certa passa, identidade errada
falha). Qualquer transição que falha deixa o job vermelho.
