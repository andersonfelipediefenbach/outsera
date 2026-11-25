# Contrato: `compose.yaml` e observabilidade

Dono: papel de infraestrutura (`AGENTS.md`). As decisões estão nas R17 a R19 da research.

## Serviços

| Serviço | Imagem (tag e digest resolvidos na tarefa) | Sobe no `up` | Portas no host | Volume | Healthcheck |
| --- | --- | --- | --- | --- | --- |
| `postgres` | `postgres:18-alpine@sha256:…` | sim | nenhuma | `pgdata` | `pg_isready -U awards -d awards` |
| `kafka` | `apache/kafka-native:<versão>@sha256:…` (KRaft, nó único) | sim | `9094` (listener `EXTERNAL`) | `kafkadata` | porta 9092 aberta (research R17, verificar) |
| `app` | construída pelo `Dockerfile` (tag `awards:local`) | sim | `8080` | nenhum | o da imagem: readiness (spec 002, FR-023) |
| `prometheus` | `prom/prometheus:<versão>@sha256:…` | sim | `9090` | nenhum | `wget -qO- localhost:9090/-/ready` |
| `grafana` | `grafana/grafana:<versão>@sha256:…` | sim | `3000` | nenhum | `wget -qO- localhost:3000/api/health` |
| `kafka-cli` | `apache/kafka:<mesma versão>@sha256:…` | não (perfil `tools`) | nenhuma | nenhum | — |

Dependências: `app` depende de `postgres` e `kafka` (`service_healthy`). `prometheus` depende de `app`
(`service_started`) e `grafana` depende de `prometheus` (`service_started`).

## Variáveis

Todas têm padrão, e `docker compose up` funciona sem nenhuma definida (FR-028).

| Variável do host | Serviço | Padrão | Observação |
| --- | --- | --- | --- |
| `POSTGRES_PASSWORD` | `postgres` e `app` (`DB_PASSWORD`) | `awards-demo` | **credencial de demonstração**, só para uso local (Princípio VII 1.6.0, FR-033) |
| `GRAFANA_ADMIN_PASSWORD` | `grafana` | `awards-demo` | **credencial de demonstração**, idem |
| `API_TOKEN` | `app` e `prometheus` | vazio | autenticação desligada por padrão |
| `APP_PORT` | `app` | `8080` | porta publicada no host |

Cada linha do `compose.yaml` que contém uma credencial de demonstração traz o comentário
`# credencial de demonstração: só para desenvolvimento local (constituição 1.6.0, Princípio VII)`.

## Configuração fixa de cada serviço

- **`postgres`:**
  - `POSTGRES_DB=awards`, `POSTGRES_USER=awards`;
  - `POSTGRES_INITDB_ARGS=--locale=C --encoding=UTF8` (research R4).
- **`kafka`:** um nó que é `broker` e `controller` (KRaft):
  - listeners `PLAINTEXT://:9092` (rede do compose), `CONTROLLER://:9093` e `EXTERNAL://:9094`
    (anunciado como `localhost:9094`);
  - fator de replicação 1 para os tópicos internos;
  - `KAFKA_AUTO_CREATE_TOPICS_ENABLE=false`.
- **`app`:**
  - `SPRING_PROFILES_ACTIVE=docker`;
  - `DB_URL=jdbc:postgresql://postgres:5432/awards`, `DB_USER=awards`;
  - `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`;
  - o log continua em JSON, pela variável da imagem.
- **`prometheus`:**
  - `observability/prometheus/prometheus.yml`, montado só para leitura;
  - coleta `app:8080/actuator/prometheus` a cada 5 s, com `authorization.credentials_file` (research
    R18).
- **`grafana`:**
  - `observability/grafana/provisioning/` (datasources e dashboards) e
    `observability/grafana/dashboards/`, montados só para leitura;
  - `GF_AUTH_ANONYMOUS_ENABLED=true` e `GF_AUTH_ANONYMOUS_ORG_ROLE=Viewer`.

## Arquivos

```text
compose.yaml
observability/
├── prometheus/
│   └── prometheus.yml
└── grafana/
    ├── provisioning/
    │   ├── datasources/prometheus.yml   # uid: prometheus
    │   └── dashboards/awards.yml        # provedor de arquivo, aponta para /var/lib/grafana/dashboards
    └── dashboards/
        └── awards.json                  # uid: awards (research R19)
```

## O que o operador vê

| Ação | Resultado esperado |
| --- | --- |
| `docker compose up -d --wait` | todos `healthy`; a aplicação só fica `healthy` depois da carga por Kafka |
| `curl localhost:8080/api/producers/intervals` | o resultado de referência |
| Abrir `localhost:3000/d/awards` | o dashboard com dados, sem login |
| `localhost:9090/targets` | alvo `app` com estado `UP` |
| `docker compose down` e `up` de novo | os dados continuam, sem duplicata (US5-5) |
| `docker compose down -v` | ambiente apagado |
