# 0018. Compose com PostgreSQL, Kafka nativo, Prometheus e Grafana

- **Status:** Aceito
- **Data:** 2026-11-24
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Emenda 1.6.0 da constituição:
  credenciais de demonstração no compose local" e "Etapa 4 do Marco 3"; research R17 a R20.

## Contexto

O modo Docker mostra o ciclo completo: o banco persistente, a ingestão por eventos e a observabilidade
coletando de verdade. Ele precisa subir com um comando, num clone limpo, sem quebrar o modo padrão.

## Decisão

- `compose.yaml` com:
  - `postgres:18-alpine`, com collation `C` (ADR 0013) e sem porta publicada;
  - `apache/kafka-native`, a imagem nativa (GraalVM) em KRaft com um nó;
  - a aplicação, construída pelo Dockerfile do Marco 2, no perfil `docker`;
  - `prom/prometheus`;
  - `grafana/grafana`;
  - `kafka-cli` (`apache/kafka`), no perfil `tools`, porque a imagem nativa não traz as ferramentas de
    linha de comando.
- Toda imagem é fixada por tag e digest, conferida pelo `workflow-policy`, e o Dependabot cobre o
  `docker-compose`.
- Os dados ficam nos volumes `pgdata` e `kafkadata` e sobrevivem a `docker compose down` sem `-v`.
- O Prometheus coleta a aplicação a cada 5 s, com o token por `credentials_file` gravado na subida a partir
  de `API_TOKEN`. O token nunca fica em arquivo versionado.
- O Grafana recebe a datasource e o dashboard versionados por provisionamento de arquivo. O dashboard tem
  requisições, taxa de erro, p95 e p99, o atraso do consumidor e os eventos gravados e rejeitados. O
  acesso anônimo é só de leitura.
- **Credenciais de demonstração** do PostgreSQL e do Grafana: só no compose local, identificadas linha a
  linha, sobrescrevíveis por variável e nunca usadas em ambiente publicado (constituição 1.6.0).
- O job `compose` do pipeline roda o `compose-test.sh`, que confere:
  - a referência;
  - o Prometheus e o Grafana;
  - um vencedor novo em até 5 s;
  - o tópico de erros;
  - a persistência;
  - a queda real do banco;
  - o token.

## Alternativas consideradas

- A imagem JVM do Kafka no broker: mais pesada. A JVM só é usada sob demanda, no `kafka-cli`.
- ZooKeeper: removido do Kafka 4.
- Sem credencial (PostgreSQL em `trust` e Grafana só anônimo) ou um `.env` não versionado: apresentados ao
  responsável, que escolheu as senhas de demonstração.

## Consequências

- `docker compose up -d --build --wait` deixa a aplicação pronta depois da carga por Kafka. No teste
  local, os passos 1 a 4 levaram 28 s, contra um limite de 180 s (SC-008).
- O volume do Kafka fica em `/home/appuser`, porque a imagem roda com esse usuário. O `CLUSTER_ID` é fixo,
  para que um container recriado use o mesmo volume.
- Uma queda do banco menor que o tempo de espera do pool de conexões (30 s) não gera falha. O passo 7b
  mantém o banco fora até a falha aparecer no log.
