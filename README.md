# Golden Raspberry Awards API

## 1. Visão geral

API REST que lê o CSV de indicados e vencedores da categoria Pior Filme do Golden Raspberry Awards ao
subir. Ela informa os produtores com o **menor** e o **maior** intervalo entre duas vitórias
consecutivas e lista os filmes com paginação e filtros.

---

## 2. Avalie em 2 minutos

**Pré-requisito:** só o **JDK 25** no `PATH`. Não é preciso Maven, Docker nem banco de dados: o
wrapper baixa o Maven, e o banco é um H2 em memória.

| | Linux, macOS ou Git Bash | Windows (PowerShell ou cmd) |
| --- | --- | --- |
| Subir a aplicação | `./mvnw spring-boot:run` | `.\mvnw.cmd spring-boot:run` |
| Rodar os testes do modo padrão | `./mvnw verify` | `.\mvnw.cmd verify` |
| Rodar também os testes do que vem desligado (opcional) | `./mvnw -Pextras verify` | `.\mvnw.cmd -Pextras verify` |

O `./mvnw verify` roda os **testes de integração de todo o modo padrão**: H2, a carga do CSV com todas as
regras, os endpoints de negócio, o Swagger e o que vem ligado sem configuração (ETag, sondas, métricas e log em
texto). Num clone limpo, com as dependências do Maven já baixadas, ele leva cerca de 1 minuto (63 s medidos, com a compilação). Os testes do que
vem desligado por padrão (Kafka, autenticação ligada e log em JSON) e a cobertura mínima de 90% rodam com
`./mvnw -Pextras verify`, também sem Docker, em cerca de 4 a 9 minutos. O pipeline roda os dois em todo push (seção 5).

A aplicação responde em `http://localhost:8080`. A carga do CSV termina antes de o servidor aceitar
conexões, e o log mostra o resumo:

```text
Carga do arquivo classpath:movielist.csv concluída: 206 linhas lidas, 206 aceitas, 0 rejeitadas
```

**Intervalos entre prêmios** (o requisito do enunciado):

```bash
curl -s http://localhost:8080/api/producers/intervals
```

```json
{"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}],"max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]}
```

**Listagem de filmes** (vencedores de 1990):

```bash
curl -s "http://localhost:8080/api/movies?year=1990&winner=true"
```

```json
{"content":[{"year":1990,"title":"Ghosts Can't Do It","studios":"Triumph Releasing","producers":"Bo Derek","winner":true},{"year":1990,"title":"The Adventures of Ford Fairlane","studios":"20th Century Fox","producers":"Steven Perry and Joel Silver","winner":true}],"page":0,"size":20,"totalElements":2,"totalPages":1}
```

> No Windows PowerShell 5.1, `curl` é um apelido de `Invoke-WebRequest`. Use `curl.exe`, com os mesmos
> argumentos.

Documentação interativa: <http://localhost:8080/swagger-ui.html>.

**Ou sem instalar nada: a demonstração publicada.** A mesma API, no modo padrão e com o arquivo
fornecido, roda no Google Cloud Run. Ela é publicada pelo pipeline a cada merge na `main` (Marco 5,
[seção 3](#marco-5-publicação-no-cloud-run-opcional)).

```bash
curl -s https://awards-2564509384.southamerica-east1.run.app/api/producers/intervals
```

- Swagger UI: <https://awards-2564509384.southamerica-east1.run.app/swagger-ui.html>.
- **A primeira chamada pode levar alguns segundos.** O serviço fica com zero instâncias quando ninguém o
  usa, e a primeira chamada espera a aplicação subir e carregar o CSV. As seguintes respondem na hora.
- É uma demonstração: uma instância só, sem autenticação e sem garantia de disponibilidade.

**E o front, também publicado.** O painel e a lista de filmes, em Angular, rodam num segundo serviço do Cloud
Run, que repassa as chamadas de `/api` a essa mesma API (Marco 6,
[seção 3](#marco-6-front-end-em-angular-opcional)):

- Painel: <https://awards-web-2564509384.southamerica-east1.run.app>, com os anos com mais de um vencedor, os
  três estúdios com mais vitórias, os produtores com o maior e o menor intervalo e a busca dos vencedores de um
  ano.
- Lista de filmes, já filtrada pelos vencedores de 1990:
  <https://awards-web-2564509384.southamerica-east1.run.app/filmes?ano=1990&vencedor=sim>.
- **A primeira abertura pode levar alguns segundos**: os dois serviços ficam com zero instâncias quando ninguém
  os usa, e o painel mostra "Carregando…" enquanto eles sobem.

---

## 3. Como executar

### Porta

A porta padrão é `8080` e muda pela variável `PORT`.

```bash
PORT=9090 ./mvnw spring-boot:run
```

```powershell
$env:PORT = "9090"; .\mvnw.cmd spring-boot:run
```

### Outro arquivo CSV

O padrão é o arquivo do enunciado, embarcado em `src/main/resources/movielist.csv`. Para usar outro,
defina `MOVIES_CSV_PATH`. O valor é um caminho no sistema de arquivos, absoluto ou relativo à raiz do
projeto, e os prefixos `file:` e `classpath:` também são aceitos.

```bash
MOVIES_CSV_PATH=src/test/resources/scenarios/disjoint-producers.csv ./mvnw spring-boot:run
```

```powershell
$env:MOVIES_CSV_PATH = "src\test\resources\scenarios\disjoint-producers.csv"; .\mvnw.cmd spring-boot:run
```

Com esse arquivo, os intervalos ficam assim:

```json
{"min":[{"producer":"Producer A","interval":2,"previousWin":1950,"followingWin":1952}],"max":[{"producer":"Producer B","interval":50,"previousWin":1925,"followingWin":1975}]}
```

No PowerShell, `$env:` vale para a sessão inteira. Remova a variável com
`Remove-Item Env:MOVIES_CSV_PATH` para voltar ao arquivo padrão.

### Arquivo inválido

| Situação | Comportamento |
| --- | --- |
| Arquivo ausente ou ilegível, separador diferente de `;` ou cabeçalho diferente de `year;title;studios;producers;winner` | A aplicação **não sobe**. O log mostra o bloco `APPLICATION FAILED TO START` com o arquivo, o motivo e a correção |
| Linha de dados inválida dentro de um arquivo válido | A linha é **ignorada**, com um aviso que cita o número da linha e o motivo. A carga continua |
| Variações inofensivas (BOM, linhas em branco, espaços em volta, maiúsculas no cabeçalho e no `yes`) | Aceitas sem aviso |

Exemplo, com as colunas do cabeçalho fora de ordem:

```bash
MOVIES_CSV_PATH=src/test/resources/scenarios/header-wrong-order.csv ./mvnw spring-boot:run
```

```text
APPLICATION FAILED TO START
***************************

Description:

Não foi possível carregar o arquivo de filmes.

    Arquivo: src/test/resources/scenarios/header-wrong-order.csv
    Motivo: INVALID_HEADER
    Detalhe: o cabeçalho esperado é "year;title;studios;producers;winner", nessa ordem, mas o encontrado é "title;year;studios;producers;winner"
    Cabeçalho encontrado: title;year;studios;producers;winner

Action:

Informe um arquivo CSV existente, em UTF-8, com o separador ";" e o cabeçalho "year;title;studios;producers;winner", nessa ordem. O caminho do arquivo é configurado pela variável de ambiente MOVIES_CSV_PATH (propriedade movies.csv.path).
```

Os motivos possíveis são `NOT_FOUND`, `UNREADABLE`, `INVALID_SEPARATOR` e `INVALID_HEADER`.

### Formato do log

No `./mvnw`, o log é o **texto** padrão do Spring Boot, legível no terminal, e sem o banner. Para ter
**JSON** (formato ECS, um objeto por linha), defina `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`. A imagem
Docker já vem com essa variável.

```bash
LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs ./mvnw spring-boot:run
```

```powershell
$env:LOGGING_STRUCTURED_FORMAT_CONSOLE = "ecs"; .\mvnw.cmd spring-boot:run
```

```json
{"@timestamp":"2026-11-24T13:38:51.169Z","log":{"level":"INFO","logger":"com.outsera.awards.ingestion.MovieFileLoader"},"process":{"pid":49512,"thread":{"name":"main"}},"service":{"name":"awards","version":"0.1.0-SNAPSHOT"},"message":"Carga do arquivo classpath:movielist.csv concluída: 206 linhas lidas, 206 aceitas, 0 rejeitadas","ecs":{"version":"8.11"}}
```

### Imagem Docker (opcional)

A imagem é um caminho alternativo. Nada do modo padrão precisa de Docker.

```bash
docker build -t awards:local .
docker run --rm -p 8080:8080 awards:local
```

| Item | Como é na imagem |
| --- | --- |
| Construção | Multi-stage: JDK 25 no build e só a JRE 25 na execução (Temurin Alpine, fixadas por digest). Os testes não rodam no build da imagem: rodam no pipeline |
| Usuário | `10001`, não root |
| Configuração | As mesmas variáveis do modo padrão: `PORT` (padrão `8080`) e `MOVIES_CSV_PATH` |
| Log | JSON (`LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`). Para texto: `-e LOGGING_STRUCTURED_FORMAT_CONSOLE=` |
| Healthcheck | `/actuator/health/readiness`: o container só fica `healthy` depois da carga do CSV |
| Parada | A JVM recebe o `SIGTERM` direto e encerra de forma ordenada. Uma falha de inicialização termina com código diferente de 0 |

Outro arquivo, montado só para leitura, e outra porta:

```bash
docker run --rm -p 9090:9090 -e PORT=9090 \
  -v "$PWD/src/test/resources/scenarios/disjoint-producers.csv:/data/movies.csv:ro" \
  -e MOVIES_CSV_PATH=file:/data/movies.csv awards:local
```

O contrato da imagem está em
[`specs/002-operacao/contracts/container.md`](specs/002-operacao/contracts/container.md).

### Modo Docker completo: PostgreSQL, Kafka, Prometheus e Grafana (opcional)

O [`compose.yaml`](compose.yaml) sobe o ambiente completo do Marco 3. É só para desenvolvimento local, e
nada dele é exigido pelo modo padrão.

```bash
docker compose up -d --build --wait   # a aplicação só fica healthy depois da carga por Kafka
curl -s http://localhost:8080/api/producers/intervals
docker compose down                   # para e mantém os dados (volumes pgdata e kafkadata)
docker compose down -v                # apaga tudo
```

| Serviço | Porta no host | O que faz |
| --- | --- | --- |
| `app` | `8080` (`APP_PORT`) | A aplicação no perfil `docker`: PostgreSQL no lugar do H2 e o Kafka como único caminho de entrada dos dados |
| `postgres` | nenhuma | PostgreSQL 18, com o mesmo esquema Flyway e collation `C` (a ordem da listagem é a do H2) |
| `kafka` | `9094` | Kafka 4.4 na imagem nativa, em KRaft, com os tópicos `awards.movies` e `awards.movies.errors` |
| `prometheus` | `9090` | Coleta `/actuator/prometheus` a cada 5 s |
| `grafana` | `3000` | O dashboard [`awards`](observability/grafana/dashboards/awards.json), aberto sem login em <http://localhost:3000/d/awards> |
| `kafka-cli` | — | Ferramentas de linha de comando do Kafka, só sob demanda (perfil `tools`) |

**Como funciona a carga no modo Docker:**
1. Na subida, a aplicação lê o CSV com as mesmas regras e os mesmos avisos do modo padrão.
2. Ela publica um evento por filme no tópico `awards.movies` e, no fim, um evento de carga concluída.
3. O consumidor grava cada filme pelo mesmo serviço do modo padrão.
4. A readiness só fica positiva quando o evento de carga concluída desta execução é consumido.
5. Num reinício, o arquivo é publicado de novo. A gravação por ano e título não duplica nada, e os filmes
   novos recebidos por evento continuam lá.

O formato dos eventos está em
[`specs/003-modo-docker/contracts/kafka-events.md`](specs/003-modo-docker/contracts/kafka-events.md).
Para publicar um vencedor novo e ver o resultado:

```bash
printf '%s\n' '2030|Filme Novo:{"year":2030,"title":"Filme Novo","studios":null,"producers":"Joel Silver","winner":true}' \
  | docker compose --profile tools run --rm -T kafka-cli /opt/kafka/bin/kafka-console-producer.sh \
      --bootstrap-server kafka:9092 --topic awards.movies --property parse.key=true --property key.separator=:
curl -s "http://localhost:8080/api/movies?year=2030"
```

Um evento inválido vai para o tópico de erros, com o conteúdo original e o motivo nos cabeçalhos, e o
consumo segue:

```bash
docker compose --profile tools run --rm kafka-cli /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server kafka:9092 --topic awards.movies.errors --from-beginning \
  --property print.headers=true --timeout-ms 5000
```

**Credenciais de demonstração do compose:** o PostgreSQL e o Grafana usam a senha `awards-demo`
(usuários `awards` e `admin`). Elas só valem no compose local, estão identificadas em cada linha do
`compose.yaml`, podem ser trocadas pelas variáveis `POSTGRES_PASSWORD` e `GRAFANA_ADMIN_PASSWORD` e nunca
devem ser usadas num ambiente publicado (constituição 1.6.0, Princípio VII).

### Autenticação (opcional)

A autenticação existe e é testada, mas fica **desligada por padrão**: sem a variável `API_TOKEN`, todos
os endpoints são públicos, como no Marco 2. Com ela, os intervalos, a listagem e as métricas exigem o
cabeçalho `Authorization: Bearer <token>`.

**Token de demonstração** (o único token do repositório, só para experimentar):
`awards-token-de-demonstracao`.

```bash
API_TOKEN=awards-token-de-demonstracao ./mvnw spring-boot:run

curl -si http://localhost:8080/api/producers/intervals                     # 401
curl -s -H "Authorization: Bearer awards-token-de-demonstracao" \
  http://localhost:8080/api/producers/intervals                            # 200
```

| Caminho | Sem `API_TOKEN` | Com `API_TOKEN` |
| --- | --- | --- |
| `/api/producers/intervals`, `/api/movies` | público | exige o token, em qualquer método (inclusive `OPTIONS`) |
| `/actuator/prometheus` | público | exige o token |
| `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` | público | público |
| `/swagger-ui.html`, `/openapi.yaml`, `/webjars/**` | público | público |

- O token ausente e o errado recebem o mesmo **401** em Problem Details, com `WWW-Authenticate: Bearer
  realm="awards"`, sem revelar nenhum dos dois valores.
- A comparação do token é feita em tempo constante, e o token nunca aparece no log nem nas métricas.
- No compose, a autenticação também fica desligada por padrão. Para ligá-la:
  `API_TOKEN=<token> docker compose up -d`. O Prometheus passa a coletar com o mesmo token.

Com linhas inválidas (`src/test/resources/scenarios/invalid-lines.csv`), a aplicação sobe e o log
registra cada rejeição e o resumo:

```text
WARN ... Linha 4 rejeitada: número de colunas diferente de 5 (encontradas 4)
WARN ... Linha 8 rejeitada: ano não numérico (19x0)
WARN ... Linha 13 rejeitada: winner diferente de yes ou vazio (no)
WARN ... Linha 14 rejeitada: ano fora da faixa de 1 a 9999 (0)
...
Carga do arquivo src/test/resources/scenarios/invalid-lines.csv concluída: 13 linhas lidas, 2 aceitas, 11 rejeitadas
```

---

### Marco 4: SLO, alertas, teste de carga e dados gerados (opcional)

Tudo aqui é extra e roda só com o Docker. O modo padrão não muda (spec 004, FR-001).

**SLO e alertas.** O endpoint de intervalos tem dois objetivos em 28 dias: **99,5% de disponibilidade** e
**99% das requisições em até cerca de 112 ms** (o bucket `0.111848106` s do histograma). O detalhe está em
[`docs/slo.md`](docs/slo.md).
- O Prometheus do compose carrega as regras de
  [`observability/prometheus/rules/slo.yml`](observability/prometheus/rules/slo.yml), e os alertas aparecem
  em <http://localhost:9090/alerts>.
- Os alertas medem a taxa de consumo do orçamento de erro em duas janelas por gravidade. Cada um leva ao
  [runbook](docs/runbook.md).

| Alerta | Dispara quando |
| --- | --- |
| `AwardsIntervalsAvailabilityBudgetBurn` | as respostas 5xx consomem o orçamento 14,4× (página) ou 6× (aviso) mais rápido que o permitido |
| `AwardsIntervalsLatencyBudgetBurn` | as requisições acima de 112 ms consomem o orçamento 14,4× ou 6× mais rápido |
| `AwardsAppDown` | o Prometheus não coleta a aplicação há mais de 1 minuto |

Os testes das regras rodam com o `promtool` da mesma imagem do compose:

```bash
docker compose run --rm --no-deps --entrypoint promtool prometheus test rules /etc/prometheus/rules/slo.test.yml
```

**Gerador de CSV.** [`tools/csv-generator/GenerateMovies.java`](tools/csv-generator/GenerateMovies.java)
gera um arquivo grande no formato do enunciado. É só o JDK, sem nenhuma dependência.
- **Determinístico:** os mesmos parâmetros dão o mesmo SHA-256 em qualquer sistema operacional.
- **Casos plantados:** dois produtores empatados em `min` (intervalo 0) e dois empatados em `max` (1900 a
  2099).

```bash
java tools/csv-generator/GenerateMovies.java --output target/generated-data/movies.csv   # 100 mil filmes
java tools/csv-generator/GenerateMovies.java --movies 500000 --seed 7 --output /tmp/filmes.csv
MOVIES_CSV_PATH=target/generated-data/movies.csv ./mvnw spring-boot:run
```

| Medida | Valor |
| --- | --- |
| SHA-256 do padrão (100 mil filmes, semente 20261125) | `c4b741e30f85898ce6a2649554ca2c5b65836a4217a03dae33be243d7880f4a6`, igual no Windows e no Linux do pipeline |
| Carga direta dos 100 mil até a readiness, no runner do pipeline | de 16,6 s a 20,1 s (3 execuções) |
| Carga por Kafka dos 100 mil até a readiness, no runner | de 108 s a 193 s |
| Maior arquivo carregado sem erro de memória | 2 milhões de filmes, em 222 s e com 11,8 GiB, numa VM do Docker com 15,5 GiB (máquina local; a memória cresce cerca de 6 GiB por milhão) |

**Teste de carga.** O k6 roda no compose, no perfil `load`, contra os 100 mil filmes gerados. Ele mede a
latência e confere o formato de cada resposta.

```bash
java tools/csv-generator/GenerateMovies.java --output target/generated-data/movies.csv
docker compose -f compose.yaml -f compose.load.yaml up -d --wait
docker compose -f compose.yaml -f compose.load.yaml --profile load run --rm k6
docker compose -f compose.yaml -f compose.load.yaml --profile load down -v
```

O job `load` do pipeline faz tudo isso com o [`load-test.sh`](.github/scripts/load-test.sh) e reprova se:
- o p99 dos intervalos passar de 111,848 ms, o mesmo bucket do alerta de latência;
- o p99 da listagem passar de 100 ms;
- a taxa de erro passar de 1%.

Os limites ficam no objetivo do SLO para pegar regressões reais, como a perda do cache dos intervalos, e
não o ruído do runner. Medido no runner, em 3 execuções verdes do commit `38334af`
([push](https://github.com/andersonfelipediefenbach/outsera/actions/runs/45826809441) e
[PR](https://github.com/andersonfelipediefenbach/outsera/actions/runs/45826811997)):

| Endpoint | p95 | p99 | Vazão pedida | Erros |
| --- | --- | --- | --- | --- |
| `GET /api/producers/intervals` | de 0,78 a 1,40 ms | de 1,04 a 2,02 ms | 50 req/s | 0 |
| `GET /api/movies` | de 9,23 a 10,13 ms | de 12,09 a 13,15 ms | 10 req/s | 0 |

O resultado dos intervalos fica guardado entre as leituras e é invalidado a cada gravação, com
consistência imediata, numa única instância ([ADR 0026](docs/adr/0026-intervalos-guardados-entre-leituras.md)).
Antes disso, com os 100 mil filmes, o p99 dos intervalos era de 927 ms.

**Roteiro: o que o Marco 4 deixou para depois.** O escopo do marco foi reduzido por decisão do responsável
(spec 004, Status). Ficaram adiados, com a spec e as tarefas guardadas:

| Item | Spec 004 | Situação |
| --- | --- | --- |
| Cache AOT da JVM na imagem e medição da subida | US4, FR-020 a FR-023 (etapa 4) | adiado |
| SBOM da imagem e assinatura keyless com cosign | US5, FR-024 a FR-027 (etapa 5) | adiado |
| Dev container | US6, FR-028 a FR-030 (etapa 6) | adiado |
| ADRs 0020 a 0025 das decisões do Marco 4 | FR-037 (etapa 6) | adiado; as decisões estão em [`docs/ai/decisoes.md`](docs/ai/decisoes.md) |
| Várias instâncias da aplicação | ADR 0026 | o cache dos intervalos precisa passar a um contador no banco ou a um evento compartilhado |

### Marco 5: publicação no Cloud Run (opcional)

A API está publicada em <https://awards-2564509384.southamerica-east1.run.app>. Nada muda no modo padrão
local nem no `./mvnw verify` (spec 005, FR-022).

**O que é publicado.**
- Só o modo padrão: H2 em memória, a carga do `movielist.csv` e a autenticação desligada. Kafka,
  PostgreSQL, Prometheus, Grafana e a autenticação ligada ficam no compose local.
- A imagem é **a mesma** que o pipeline testou (teste de fumaça e Trivy), identificada pelo digest e
  publicada no Artifact Registry do projeto, sem rebuild.
- As métricas ficam fechadas na URL pública: `/actuator/prometheus` responde 404. A publicação liga só o
  `health`, que os probes usam.

**Como a publicação acontece.** O job `deploy` roda só em push na `main` deste repositório, depois dos 10
checks obrigatórios:

1. Baixa a imagem testada do job `image` e confere o digest.
2. Entra no GCP por **Workload Identity Federation**, sem chave nenhuma no GitHub ou no repositório. O
   provedor de identidade só aceita tokens deste repositório e do dono, pelos IDs numéricos, e da branch
   `main`.
3. Envia a imagem ao registro com o digest preservado.
4. Implanta uma revisão nova **sem tráfego** e a confere pela URL própria dela: readiness, resultado de
   referência, Swagger e métricas fechadas, com o [`verify-deployment.sh`](.github/scripts/verify-deployment.sh).
5. Só então passa o tráfego para ela e confere a URL pública de novo. Uma revisão reprovada nunca recebe
   tráfego.

O resumo do job traz a URL, o digest, a revisão e o tempo de cada chamada. Se algo falhar depois da troca, o
[runbook](docs/runbook.md#publicação-no-cloud-run) mostra como voltar à revisão anterior.

**Uma instância por revisão (0 a 1).** O mínimo de zero instâncias mantém o custo em zero com o serviço
ocioso, e por isso a primeira chamada passa pela partida a frio. O máximo de uma mantém válido o cache dos
intervalos do [ADR 0026](docs/adr/0026-intervalos-guardados-entre-leituras.md). Na troca de revisão, a antiga
e a nova podem ter uma instância cada por alguns instantes. No modo padrão isso não importa: cada instância
tem o próprio H2 e não grava nada depois da carga.

**Configuração do GCP.** Tudo o que existe no projeto vem do [`deploy/gcp/setup.sh`](deploy/gcp/setup.sh):
- as APIs;
- o registro de imagens, com retenção de 30 dias que mantém as 10 imagens mais recentes;
- as contas de publicação e de execução;
- o pool e o provedor de identidade;
- o serviço;
- os vínculos de IAM.

O script é idempotente: a segunda execução seguida não muda nada (saídas em
[`specs/005-deploy-cloud-run/evidencias/`](specs/005-deploy-cloud-run/evidencias/)). A conta de publicação
tem papéis só no serviço, no registro e na conta de execução, e não pode mudar quem invoca o serviço. A conta
de execução não tem papel nenhum. A decisão completa está no
[ADR 0027](docs/adr/0027-publicacao-no-cloud-run.md).

### Marco 6: front-end em Angular (opcional)

O front fica em [`frontend/`](frontend/), com build, testes e imagem próprios. Ele está publicado em
<https://awards-web-2564509384.southamerica-east1.run.app>. O `./mvnw` não lê o `frontend/` nem precisa de Node
(constituição 2.1.0, Princípio II).

**As duas telas.**
- **Painel** (`/`): quatro painéis independentes, cada um com "Carregando…", mensagem de vazio e erro com "Tentar
  de novo":
  - os anos com mais de um vencedor;
  - os três estúdios com mais vitórias;
  - os produtores com o maior e o menor intervalo, com todos os empatados que `GET /api/producers/intervals`
    devolve;
  - os vencedores de um ano buscado, pela listagem existente (`year` e `winner=true`).
- **Filmes** (`/filmes`): a lista paginada, 10 por página, com filtro por ano e por vencedor. A URL guarda o
  filtro e a página (`/filmes?ano=1990&vencedor=sim&pagina=2`), então recarregar ou compartilhar o link mostra a
  mesma página.
- No celular, os painéis ficam empilhados e as linhas viram cartões; no desktop, ficam em grade e em tabela. Tudo
  funciona só pelo teclado, com o foco visível.

**Agregações novas na API.** Os dois painéis de totais usam endpoints novos, descritos no contrato 1.3.0 e
comprovados por ITs, sem mudar os existentes ([seção 4](#agregações-do-painel)).

**Rodar o front localmente**, com Node 26 ([`frontend/.nvmrc`](frontend/.nvmrc)), contra a API do
`./mvnw spring-boot:run`:

```bash
./mvnw spring-boot:run          # num terminal: a API em http://localhost:8080
cd frontend && npm ci && npm start   # noutro: o front em http://localhost:4200, com /api repassado à API
```

**Testes do front** ([`frontend/README.md`](frontend/README.md)):
- **unitários**, com o Vitest: componentes, serviços, validação, estados dos painéis, filtros da URL e paginação,
  com cobertura mínima de 80% das linhas (`npm test -- --watch=false --coverage`);
- **ponta a ponta**, com o Playwright, contra a API real e a imagem do front com o repasse de `/api`, sem
  nenhuma resposta simulada (o lint proíbe `page.route`), numa largura de desktop e numa de celular.

Os dois rodam no pipeline, nos jobs `frontend` e `web` ([seção 5](#pipeline)), como pede a emenda 2.1.0 da
constituição (Princípio VI).

**Imagem e publicação.**
- [`frontend/Dockerfile`](frontend/Dockerfile) é multi-stage: o Node 26 constrói o Angular, e o nginx sem root
  serve os arquivos.
- O nginx devolve o `index.html` nas rotas internas e repassa `/api` ao serviço da API, cuja origem vem de
  `API_URL`. O navegador fala com uma única origem, então a API não precisa de CORS.
- Ele aplica CSP e cabeçalhos de segurança.
- O log de acesso sai em JSON (`LOG_FORMAT=json`, definido na imagem).
- O job `deploy-web` publica a imagem testada no serviço `awards-web` só depois de a API ser publicada e
  conferida. O fluxo é o mesmo do Marco 5: mesma identidade federada, sem chaves, revisão sem tráfego até o
  [`verify-web.sh`](.github/scripts/verify-web.sh) aprovar, e 0 a 1 instância.
- O `setup.sh` cria o serviço e a conta de execução do front, sem nenhum papel
  ([evidências](specs/006-frontend-angular/evidencias/)).
- A decisão está no [ADR 0028](docs/adr/0028-front-end-angular-com-nginx.md).

**O que fica de fora.** O front não entra no `compose.yaml`, não tem login (com a autenticação ligada na API, os
painéis mostram o erro) e não usa SSR.

## 4. Endpoints

O contrato completo (versão 1.3.0, com ETag e a autenticação opcional) está em
[`specs/006-frontend-angular/contracts/openapi.yaml`](specs/006-frontend-angular/contracts/openapi.yaml), publicado
em <http://localhost:8080/openapi.yaml> e navegável no Swagger UI, em
<http://localhost:8080/swagger-ui.html>. Os dos Marcos 1 a 5 ficam em
[`specs/001-producer-intervals-api/contracts/openapi.yaml`](specs/001-producer-intervals-api/contracts/openapi.yaml),
[`specs/002-operacao/contracts/openapi.yaml`](specs/002-operacao/contracts/openapi.yaml),
[`specs/003-modo-docker/contracts/openapi.yaml`](specs/003-modo-docker/contracts/openapi.yaml) (1.2.0) e
[`specs/005-deploy-cloud-run/contracts/openapi.yaml`](specs/005-deploy-cloud-run/contracts/openapi.yaml) (1.2.1),
como registro. A 1.3.0 só acrescenta as duas agregações do painel (spec 006, FR-004): os endpoints existentes não
mudam.

| Método e caminho | O que faz |
| --- | --- |
| `GET /api/producers/intervals` | Produtores com o menor e o maior intervalo entre vitórias consecutivas |
| `GET /api/movies` | Lista paginada de filmes, com filtros |
| `GET /api/aggregations/years-with-multiple-winners` | Anos com mais de um vencedor ([agregações](#agregações-do-painel)) |
| `GET /api/aggregations/studios-with-win-count` | Estúdios com a quantidade de filmes vencedores |
| `GET /swagger-ui.html` | Documentação interativa |
| `GET /openapi.yaml` | Contrato OpenAPI 3.1 |
| `GET /actuator/health/liveness`, `GET /actuator/health/readiness`, `GET /actuator/health` | Saúde da aplicação ([endpoints operacionais](#endpoints-operacionais)) |
| `GET /actuator/prometheus` | Métricas no formato do Prometheus |

### `GET /api/producers/intervals`

Sem parâmetros. Sempre responde **200** com as listas `min` e `max`, no formato exato da página 2 do
enunciado. Cada item tem só `producer`, `interval`, `previousWin` e `followingWin`.

- Os empatados entram todos, ordenados por `previousWin` e depois pelo nome.
- Sem nenhum produtor com duas vitórias, a resposta é `{"min":[],"max":[]}`, também com 200.

O exemplo com o arquivo do enunciado está na [seção 2](#2-avalie-em-2-minutos).

### `GET /api/movies`

| Parâmetro | Valores aceitos | Padrão |
| --- | --- | --- |
| `year` | Ano exato, só dígitos (até 9) | sem filtro |
| `winner` | `true` ou `false`, sem diferenciar maiúsculas | sem filtro |
| `page` | Inteiro de 0 a 2147483647 | `0` |
| `size` | Inteiro de 1 a 100 | `20` |

- A ordem é sempre por ano e depois por título. Parâmetros desconhecidos são ignorados.
- `studios` vem `null` quando a coluna está vazia no arquivo. `producers` e `studios` trazem o texto
  como está no arquivo.
- Uma página além da última, ou um filtro sem resultado, responde 200 com `content` vazio e os totais
  reais.

```bash
curl -s "http://localhost:8080/api/movies?size=2"
```

```json
{"content":[{"year":1980,"title":"Can't Stop the Music","studios":"Associated Film Distribution","producers":"Allan Carr","winner":true},{"year":1980,"title":"Cruising","studios":"Lorimar Productions, United Artists","producers":"Jerry Weintraub","winner":false}],"page":0,"size":2,"totalElements":206,"totalPages":103}
```

Um parâmetro inválido responde **400** em Problem Details (RFC 9457), com `Content-Type:
application/problem+json`. A propriedade `invalidParams` lista todos os parâmetros inválidos da
consulta:

```bash
curl -s "http://localhost:8080/api/movies?page=-1&size=500"
```

```json
{"detail":"Parâmetro inválido na consulta","instance":"/api/movies","status":400,"title":"Bad Request","invalidParams":[{"name":"page","reason":"deve ser um inteiro maior ou igual a 0"},{"name":"size","reason":"deve ser um inteiro de 1 a 100"}]}
```

### Agregações do painel

Duas agregações de leitura, criadas para o front do Marco 6 (spec 006, FR-001 a FR-003). Não têm parâmetros e
seguem o padrão dos endpoints de negócio: ETag e 304, 401 com a autenticação ligada, e listas vazias com 200
quando não há dados.

```bash
curl -s http://localhost:8080/api/aggregations/years-with-multiple-winners
# {"years":[{"year":1986,"winnerCount":2},{"year":1990,"winnerCount":2},{"year":2015,"winnerCount":2}]}

curl -s http://localhost:8080/api/aggregations/studios-with-win-count
# {"studios":[{"name":"Columbia Pictures","winCount":7},{"name":"Paramount Pictures","winCount":6},{"name":"Warner Bros.","winCount":5},...]}
```

- **Anos:** só os anos com dois ou mais vencedores, em ordem crescente.
- **Estúdios:** cada estúdio com a quantidade de filmes vencedores em que aparece. A ordem é pela quantidade
  decrescente e depois pelo nome.
  - O campo de estúdios é separado por vírgula, sem espaços nas pontas, e partes vazias saem.
  - Um estúdio repetido no mesmo filme conta uma vez.
  - Os nomes ficam como no arquivo, sem normalizar a grafia.
  - O arquivo fornecido não usa ` and ` entre estúdios, então ele não separa estúdios, ao contrário dos
    produtores.

### ETag e 304

As respostas 200 dos endpoints de negócio trazem um `ETag` forte, derivado do conteúdo: o mesmo corpo tem
sempre o mesmo ETag, inclusive entre execuções com o mesmo arquivo. O corpo da resposta 200 não muda.

```bash
curl -si http://localhost:8080/api/producers/intervals | grep -i etag
# ETag: "010f231232ae1af42532e0827a126190d"

curl -si -H 'If-None-Match: "010f231232ae1af42532e0827a126190d"' http://localhost:8080/api/producers/intervals
# HTTP/1.1 304, sem corpo
```

- O `If-None-Match` bate com o ETag atual sozinho, numa lista, na forma fraca (`W/"..."`) ou com `*`.
- Na listagem, cada combinação de página, tamanho e filtros tem o seu próprio ETag.
- Respostas de erro (400 e 405) não têm ETag e nunca viram 304.

### Endpoints operacionais

Na mesma porta da API e sem detalhes internos. As probes e a saúde geral são sempre públicas. O endpoint
de métricas exige o token quando a [autenticação](#autenticação-opcional) está ligada. Os contratos estão
em [`specs/002-operacao/contracts/operational-endpoints.md`](specs/002-operacao/contracts/operational-endpoints.md)
e, com o que o Marco 3 acrescenta, em
[`specs/003-modo-docker/contracts/operational-endpoints.md`](specs/003-modo-docker/contracts/operational-endpoints.md).

| Caminho | Resposta |
| --- | --- |
| `/actuator/health/liveness` | 200 `{"status":"UP"}`; 503 `{"status":"DOWN"}` se a aplicação estiver quebrada |
| `/actuator/health/readiness` | 200 `{"status":"UP"}` só depois da carga do CSV; 503 `{"status":"OUT_OF_SERVICE"}` antes |
| `/actuator/health` | 200 `{"status":"UP","groups":["liveness","readiness"]}`, sem componentes nem detalhes |
| `/actuator/prometheus` | Texto do Prometheus: `http_server_requests_seconds` (contagem e histograma de latência por rota, método, status e `outcome`), métricas da JVM e do processo, todas com `application="awards"`. No modo Docker, também `awards_ingestion_events_total` (eventos gravados e rejeitados) e `kafka_consumer_fetch_manager_records_lag_max` (atraso do consumidor) |

Os demais endpoints do Actuator (`/actuator`, `/actuator/env`, `/actuator/beans` e outros) respondem
404. Com as métricas, a taxa de erro e o percentil 95 de cada endpoint saem direto no Prometheus:

```promql
sum(rate(http_server_requests_seconds_count{uri="/api/producers/intervals",outcome=~"CLIENT_ERROR|SERVER_ERROR"}[5m]))
  / sum(rate(http_server_requests_seconds_count{uri="/api/producers/intervals"}[5m]))

histogram_quantile(0.95, sum by (le) (rate(http_server_requests_seconds_bucket{uri="/api/producers/intervals"}[5m])))
```

### Códigos HTTP

| Código | Quando |
| --- | --- |
| 200 | Consulta atendida, inclusive com listas vazias |
| 304 | `If-None-Match` igual ao ETag atual, sem corpo |
| 400 | Parâmetro inválido em `/api/movies`, em Problem Details |
| 401 | Autenticação ligada e token ausente ou errado, em Problem Details |
| 404 | Caminho inexistente, em Problem Details |
| 405 | Verbo diferente de GET nos dois endpoints, em Problem Details e com o cabeçalho `Allow: GET` |
| 503 | Liveness ou readiness negativa (no modo Docker, a readiness fica 503 até a carga por Kafka terminar) |

---

## 5. Testes

```bash
./mvnw verify                          # testes do enunciado (Linux, macOS ou Git Bash)
./mvnw -Pextras verify                 # todos os testes, com os dos extras e a cobertura mínima
./mvnw -Pextras verify -Dit.test=ReferenceFileIT   # uma classe só
```

```powershell
.\mvnw.cmd verify
.\mvnw.cmd -Pextras verify
.\mvnw.cmd -Pextras verify "-Dit.test=ReferenceFileIT"   # no PowerShell, o -D vai entre aspas
```

Todos são **testes de integração**, executados pelo Maven Failsafe na fase `verify`, **sem Docker**
(constituição 2.0.0, Princípios II e VI):

- **`./mvnw verify`:** uma execução, com os **209 testes do modo padrão**, listados em
  [`src/test/default-mode-its.txt`](src/test/default-mode-its.txt): os do enunciado (Marco 1) e os dos Marcos 2
  e 3 que valem sem nenhuma configuração. Leva cerca de 1 minuto num clone limpo (63 s medidos, com a compilação);
- **`./mvnw -Pextras verify`:** a mesma execução e mais duas:
  - **`extras`:** os testes do que vem desligado por padrão (Kafka, inclusive os do testador adversário,
    e a autenticação ligada), com o log em texto. Os 5 do `JsonLogFormatIT` ficam
    pulados, porque só valem com o JSON ligado;
  - **`json-logs`:** os 85 testes das classes marcadas com `@Tag("log")` rodam de novo, numa JVM
    própria, com `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`. Assim, as mesmas verificações de log valem nos
    dois formatos.

Os testes do Kafka rodam no `./mvnw -Pextras verify` com o **broker embutido** do Spring Kafka (KRaft,
dentro do próprio teste), sem Docker.

A **cobertura** de linhas é medida pelo JaCoCo sobre a soma das execuções do `./mvnw -Pextras verify`, que
falha abaixo de **90%**. O relatório fica em `target/site/jacoco-it/index.html`. O job `build` do pipeline
roda esse perfil em todo push. Os testes do Kafka usam um perfil só de teste (`kafkatest`, em
`src/test/resources`), com prazos curtos dos clientes, e reaproveitam o contexto do Spring entre as
classes que só leem.

**Testes com PostgreSQL** (perfil Maven `postgres-it`): o que só existe no PostgreSQL fica fora do
`./mvnw verify` padrão (Princípio II) e roda obrigatoriamente no job `postgres` do pipeline (Princípio VI,
constituição 1.7.0). São a migração, a ordem com a collation, os reinícios sobre o mesmo banco e a nova
tentativa quando a gravação falha. Para rodar localmente, é preciso um PostgreSQL com collation `C`:

```bash
docker run -d --name awards-pg -p 5432:5432 -e POSTGRES_DB=awards -e POSTGRES_USER=awards \
  -e POSTGRES_HOST_AUTH_METHOD=trust -e POSTGRES_INITDB_ARGS='--locale=C --encoding=UTF8' postgres:18-alpine
DB_URL=jdbc:postgresql://localhost:5432/awards DB_USER=awards DB_PASSWORD= ./mvnw -Ppostgres-it verify
docker rm -f awards-pg
```

| Grupo (`src/test/java/com/outsera/awards/`) | O que cobre |
| --- | --- |
| `SmokeIT` | A aplicação sobe com o arquivo padrão |
| `ingestion/` | Carga do CSV: falhas de inicialização, variações inofensivas, BOM, linhas inválidas, duplicatas, estúdios vazios, resumo da carga e arquivos sem filmes válidos |
| `intervals/` | Cálculo e formato dos intervalos: resultado de referência, empates em `min` e `max`, três ou mais vitórias, linhas fora de ordem, vitórias no mesmo ano, separação de produtores, arquivo com anos diferentes dos do enunciado e um oráculo independente com arquivos gerados |
| `adversario/` | Os 13 casos do testador adversário ([seção 10](#10-uso-de-ia)), um IT por caso, com o CSV e o resultado esperado em `src/test/resources/scenarios/adversario/` |
| `movies/` | Listagem: paginação, filtros, ordem, parâmetros inválidos e tempo de resposta abaixo de 1 s |
| `docs/` | As respostas reais dos dois endpoints validadas contra o `openapi.yaml`, inclusive o `ETag` e o 304, mais o Swagger UI e o contrato publicado |
| `web/` | Verbos não suportados nos dois endpoints respondem 405 |
| `etag/` | ETag e 304: valor único, lista, forma fraca, `*`, malformado, erros sem ETag e o mesmo ETag entre execuções |
| `health/` | Liveness e readiness, corpo sem detalhes, 503 nos estados negativos e a readiness só depois da carga |
| `metrics/` | Métricas no formato do Prometheus: contagem, status, rota sem parâmetros, rotas inexistentes, 304, histograma e rótulo `application` |
| `logging/` | Log em texto sem a variável, JSON ECS com ela, banner desligado e nenhum cabeçalho `Authorization` no log |
| `isolation/` | Sem perfil, o Kafka não é configurado: nenhuma métrica nem cliente Kafka (Marco 3) |
| `kafka/` | Ingestão por Kafka com o broker embutido: resultado de referência, equivalência byte a byte com a carga direta, readiness só com o evento de carga concluída, linhas inválidas, falhas estruturais, vencedor novo em até 5 s, atualização, idempotência, cada tipo de evento inválido no tópico de erros (inclusive o caractere nulo, que o PostgreSQL não aceita), tópico de erros fora do ar, broker fora do ar e métricas (Marco 3) |
| `security/` | Autenticação desligada (guarda do `API_TOKEN`) e ligada: 401 em Problem Details, endpoints públicos, token fora do log, sem senha gerada, cabeçalhos de cache e de proteção e o contrato publicado (1.2.0 no Marco 3, 1.2.1 desde o Marco 5) |
| `postgres/` | Só no perfil `postgres-it`: esquema, equivalência e ordem entre o H2 e o PostgreSQL, reinícios, nova tentativa quando a gravação falha e recusa permanente do banco por dado inválido, que vai ao tópico de erros (Marco 3) |
| `support/` | Base comum (`ScenarioIT`, `KafkaScenarioIT`, `TokenScenarioIT`) e apoios (aplicação real por `SpringApplication`, HTTP, Kafka e PostgreSQL reais, leitura do log, do Prometheus e do contrato), sem testes próprios |

**Como os testes funcionam:**
- Cada classe sobe a aplicação inteira com `@SpringBootTest` em porta aleatória e faz chamadas HTTP
  reais.
- Cada cenário tem o seu CSV em `src/test/resources/scenarios/`, escolhido pela propriedade
  `movies.csv.path`.
- Quando o teste precisa controlar o ciclo de vida da aplicação real, ele a executa com
  `SpringApplication`, ainda sem mocks (Princípio I, constituição 1.5.0). São quatro casos:
  - **falha de inicialização**, como o `StartupFailureIT`: o contexto não chega a subir, e o teste
    verifica a exceção e o log;
  - **múltiplas execuções**, como o `EtagAcrossRunsIT`, o `KafkaEquivalenceIT` e o `RestartPostgresIT`:
    comparam duas ou mais subidas;
  - **ordem da subida**, como o `ReadinessAfterLoadIT`: observa os eventos reais do ciclo de vida;
  - **saída completa da subida**, como o `JsonLogFormatIT` e o `TokenNotLoggedIT`: capturam o log desde a
    primeira linha.

  O resultado é verificado por HTTP real sempre que o HTTP observa o comportamento.

**Por que só testes de integração:** o item 4.2 do enunciado pede testes de integração, e o Princípio I
da [constituição](.specify/memory/constitution.md) proíbe testes unitários e mocks na API.
- Não há classe `*Test`, e o Mockito foi excluído do classpath de teste.
- Os testes verificam o comportamento pelo HTTP, como o avaliador o vê, e não detalhes internos.
- Log e banco só podem ser usados enquanto um comportamento não tem endpoint. O único teste que consulta
  o banco é o `SchemaPostgresIT`, que confere o histórico do Flyway, porque a migração não tem endpoint. O
  log só é verificado no que não tem endpoint: os avisos e o resumo da carga, a ordem entre a carga e o
  início do servidor, as falhas de inicialização, o próprio formato do log e a ausência do token.
- Os testes do Kafka usam um produtor e um consumidor reais contra o broker embutido. Os testes do
  PostgreSQL usam um banco real, com um esquema próprio por classe.
- A autenticação é testada com tokens gerados na hora: nenhum token fica no código dos testes.

### Pipeline

O [GitHub Actions](.github/workflows/ci.yml) roda em todo push e pull request, sem segredos. Toda action é
fixada por hash de commit. As únicas publicações são as dos jobs `deploy` (API, Marco 5) e `deploy-web` (front,
Marco 6), só em push na `main`. O contrato completo está em
[`specs/002-operacao/contracts/ci-pipeline.md`](specs/002-operacao/contracts/ci-pipeline.md), e o das
publicações, em [`specs/005-deploy-cloud-run/contracts/ci-deploy.md`](specs/005-deploy-cloud-run/contracts/ci-deploy.md)
e [`specs/006-frontend-angular/contracts/ci-frontend.md`](specs/006-frontend-angular/contracts/ci-frontend.md).
Os checks obrigatórios da `main` são 12: os 10 do Marco 4, mais `frontend` e `web`.

| Job | O que faz | Reprova quando |
| --- | --- | --- |
| `workflow-policy` | Confere a política do workflow, do Dependabot e do compose, e que os ITs dos marcos anteriores não mudaram desde a `v0.2.0`, a `v0.3.0` e a `v0.5.0` | Action sem hash de commit, permissão além de `contents: read` (exceto `security-events: write` no `codeql` e `id-token: write` no `deploy` e no `deploy-web`), uso de `secrets.`, publicação de imagem fora do `deploy` e do `deploy-web`, job de publicação sem os 12 checks em `needs` (o `deploy-web` também sem o `deploy`), com outra condição, com cancelamento ou com build, Dependabot incompleto (inclusive o npm e o docker do `frontend/`), imagem do compose ou de service container sem digest, senha de banco no workflow, ou um IT de uma dessas tags alterado sem exceção registrada |
| `build` | `./mvnw -Pextras verify` (enunciado, extras e log em JSON), cobertura no resumo e relatórios como artefato | Qualquer teste falha, ou a cobertura de linhas fica abaixo de 90% |
| `postgres` | `./mvnw -Ppostgres-it verify` contra um PostgreSQL 18 (service container, collation `C`, sem credencial) | Qualquer `*PostgresIT` falha |
| `compose` | [`compose-test.sh`](.github/scripts/compose-test.sh): sobe o compose e confere a referência, o Prometheus, o dashboard, as regras do SLO carregadas sem alerta disparando, um vencedor novo pelo Kafka em até 5 s, o tópico de erros, a persistência depois de `down`, a queda real do banco e o token | Qualquer passo falha, ou a subida até o dashboard passa de 3 min |
| `codeql` | Análise do código Java (`security-extended`) | Achado de severidade alta ou crítica (`security-severity` ≥ 7,0) |
| `dependencies` | Trivy nas dependências de runtime que o Maven copia (inventário efetivo, com as transitivas), com relatório completo e as versões de `lz4-java` e `snappy-java` no resumo | Vulnerabilidade `HIGH` ou `CRITICAL` **com versão corrigida**, ou `lz4-java` e `snappy-java` fora do inventário |
| `alert-rules` | `promtool check rules`, `check config` e `test rules` das regras do SLO, com a imagem do Prometheus do compose | Regra inválida ou teste de regra falhando |
| `load` | [`load-test.sh`](.github/scripts/load-test.sh): gera o CSV de 100 mil filmes duas vezes e compara os SHA-256, mede a carga direta e a carga por Kafka e roda o k6 | Hashes diferentes, linha rejeitada, readiness fora do prazo, ou p99 e taxa de erro acima dos limites |
| `frontend` | No `frontend/`, com Node 26: `npm ci`, lint (angular-eslint), testes unitários com cobertura no resumo, build de produção e `npm audit` ([Marco 6](#marco-6-front-end-em-angular-opcional)) | Erro de lint, teste falhando, cobertura de linhas abaixo de 80%, orçamento de tamanho estourado, ou vulnerabilidade alta ou crítica numa dependência de execução |
| `web` | Imagem do front (no Docker e em tar OCI, do mesmo build), digest, a API pelo JAR, [`web-smoke-test.sh`](.github/scripts/web-smoke-test.sh) (10 passos, inclusive o log em JSON), Playwright nos projetos `desktop` e `celular` contra a API real, e Trivy. Em push na `main`, guarda o tar para o `deploy-web` | Imagem que não sobe, repasse ou rota interna quebrados, cabeçalho ou log fora do contrato, cenário de ponta a ponta falhando, ou vulnerabilidade `HIGH` ou `CRITICAL` com correção |
| `dockerfile-lint` | hadolint no `Dockerfile` e no `frontend/Dockerfile` | Aviso ou erro |
| `image` | Contexto de build, imagem (no Docker e em tar OCI, do mesmo build), digest, teste de fumaça (13 passos) e Trivy na imagem. Em push na `main`, guarda o tar para o `deploy` | Contexto fora do `.dockerignore`, tar com mais de um manifesto ou fora de `linux/amd64`, imagem do Docker diferente da do tar, imagem que não sobe ou não responde o resultado de referência, ou vulnerabilidade `HIGH` ou `CRITICAL` com correção na imagem |
| `deploy` | Só em push na `main`, depois dos 10 checks: envia a imagem testada ao Artifact Registry pelo digest, implanta sem tráfego, confere pela URL da revisão, passa o tráfego e confere a URL pública ([Marco 5](#marco-5-publicação-no-cloud-run-opcional)). Não é check obrigatório, porque só roda depois do merge | Digest diferente do testado, falha de identidade ou de permissão, revisão que não sobe, ou conferência reprovada (readiness, resultado de referência, Swagger ou métricas abertas) |
| `deploy-web` | Só em push na `main`, depois dos 12 checks e do `deploy`: envia a imagem do front testada pelo digest, implanta no `awards-web` sem tráfego, confere pela URL da revisão com o [`verify-web.sh`](.github/scripts/verify-web.sh), passa o tráfego e confere a URL pública ([Marco 6](#marco-6-front-end-em-angular-opcional)). Não é check obrigatório | Digest diferente do testado, falha de identidade ou de permissão, ou conferência reprovada (página, rota interna, repasse de `/api` com o resultado de referência e as agregações) |

Achados médios e baixos, e vulnerabilidades sem correção, aparecem no relatório sem reprovar. O
[Dependabot](.github/dependabot.yml) abre PRs semanais para Maven, GitHub Actions, as imagens base, as
imagens do compose e as dependências do npm do front.

---

## 6. Requisitos do enunciado

Cada item exigido pelo enunciado e como ele é comprovado: pelo teste de integração ou, nos itens de
entrega (4.4, 4.5, 5.1 e 5.2), pelo artefato correspondente. A matriz completa, requisito por
requisito da spec, está em
[`specs/001-producer-intervals-api/plan.md`](specs/001-producer-intervals-api/plan.md#rastreabilidade-requisito--teste).

| Item do enunciado | Requisitos da spec | Como é comprovado |
| --- | --- | --- |
| API RESTful no nível 2 de Richardson: recursos nomeados (`/api/producers/intervals`, `/api/movies`), verbo GET e códigos HTTP adequados (item 4.1) | FR-017, FR-018, FR-019, FR-023, FR-023a | **200:** `ReferenceFileIT`, `IntervalsShapeIT`, `MoviesListingIT`. **400** em Problem Details: `MoviesInvalidParamsIT`, `MoviesContractIT`. **404** em Problem Details para rota inexistente: `SmokeIT`. **405** para POST, PUT e DELETE nos dois endpoints, com `Allow: GET`: `HttpMethodsIT` |
| Carga do CSV na inicialização, antes de o servidor aceitar conexões | FR-001, FR-002 | `DefaultFileIT` |
| Formato de `min` e `max` (`producer`, `interval`, `previousWin`, `followingWin`), página 2 | FR-017, FR-025 | `IntervalsShapeIT`, `IntervalsContractIT` |
| Intervalo só entre vitórias consecutivas do mesmo produtor | FR-011, FR-012, FR-013, FR-016 | `ThreeOrMoreWinsIT`, `UnorderedLinesIT`, `SameYearWinsIT`, `WinnerVariantsIT` |
| Empates: todos os empatados em `min` e em `max` | FR-014, FR-015 | `TiesMinIT`, `TiesMaxIT`, `IntervalEdgeCasesIT` |
| Listas vazias quando nenhum produtor venceu duas vezes | FR-018 | `NoRepeatWinnersIT`, `IntervalsShapeIT`, `EmptyLoadIT` |
| Resultado de referência com o arquivo fornecido (Joel Silver em `min`, Matthew Vaughn em `max`) | SC-001 | `ReferenceFileIT` |
| Correto com outros arquivos, sem dados fixos no código | FR-016a, FR-016b, SC-004a | `DisjointProducersIT`, `RandomizedOracleIT` |
| Leitura da lista de indicados e vencedores (seção 1) | FR-019 a FR-023 | `MoviesListingIT`, `MoviesInvalidParamsIT`, `MoviesContractIT` |
| Somente testes de integração (item 4.2) | FR-027, Princípio I | Todos os testes são `*IT`, sem mocks, executados pelo Failsafe, com `@SpringBootTest` ou com a aplicação real por `SpringApplication` nos quatro casos de controle do ciclo de vida da [seção 5](#5-testes) |
| Execução só com o wrapper, sem Docker e sem instalar banco: SGBD **H2 em memória**, embarcado na aplicação (item 4.3) | FR-026 | O H2 é dependência do `pom.xml`, e nenhuma URL de banco é configurada, então o Spring Boot cria um H2 em memória a cada execução. Todos os testes sobem a aplicação assim, e o `SmokeIT` confirma que ela sobe com o arquivo padrão. Além disso, `./mvnw verify` e `./mvnw spring-boot:run` foram executados em clone limpo, no Linux e no Windows |
| Documentação de como executar e testar (item 4.4) | — | Este README: [seção 2](#2-avalie-em-2-minutos) (avaliação rápida), [seção 3](#3-como-executar) (execução) e [seção 5](#5-testes) (testes) |
| Código em repositório git (item 4.5) | — | Este repositório, com um ou mais commits por bolt e a estrutura descrita na [seção 11](#11-estrutura-do-repositório) |
| Uso de IA no desenvolvimento (item 5.1) | — | Spec Kit, Claude Code e Codex, com o fluxo por bolts e os papéis descritos na [seção 10](#10-uso-de-ia) |
| Registro das interações com a IA (item 5.2) | — | [`docs/ai/README.md`](docs/ai/README.md): prompts com data e hora, sessões exportadas e decisões aceitas ou rejeitadas |

Cada método de teste tem um `@DisplayName` que começa pelos IDs dos requisitos que comprova (por
exemplo, `"FR-014 FR-015 empate no mínimo"`, no `TiesMinIT`).

### Requisitos do Marco 2 (extras de operação)

Fora do enunciado, mas com a mesma rastreabilidade. Cada requisito é comprovado na forma do seu tipo
(Princípio VI, constituição 1.4.0): comportamento da aplicação por teste de integração; imagem e
pipeline pela execução do pipeline; documentação por revisão, com item no checklist da spec. A matriz
completa está em [`specs/002-operacao/plan.md`](specs/002-operacao/plan.md#rastreabilidade-requisito--comprovação).

| Extra | Requisitos | Como é comprovado |
| --- | --- | --- |
| ETag e 304 nos dois endpoints, com o corpo do Marco 1 inalterado | FR-001 a FR-007 | `IntervalsEtagIT`, `MoviesEtagIT`, `EtagEmptyIT`, `EtagAcrossRunsIT`, `IntervalsContractIT`, `MoviesContractIT`, `ApiDocsIT` e toda a suíte do Marco 1 |
| Liveness e readiness, com a readiness só depois da carga | FR-008 a FR-011 | `HealthProbesIT`, `ReadinessInvalidLinesIT`, `ProbeStatesIT`, `ReadinessAfterLoadIT` |
| Métricas de latência e de erro no formato do Prometheus | FR-012 a FR-016 | `MetricsIT` |
| Log em texto no `./mvnw` e JSON por variável | FR-017 a FR-020 | `TextLogFormatIT`, `JsonLogFormatIT` e as classes com `@Tag("log")` nas duas execuções |
| Imagem multi-stage, não root, com healthcheck e `.dockerignore` | FR-021 a FR-027 | Jobs `dockerfile-lint` e `image` (contexto de build e teste de fumaça) |
| Pipeline, portas de segurança e cobertura, actions por hash e Dependabot | FR-028 a FR-029 | Jobs `workflow-policy`, `build`, `codeql`, `dependencies` e `image` |
| ADRs e este README | FR-030, FR-031, FR-034 | Revisão do PR, pelas seções "Revisão dos ADRs" e "Revisão do README" de [`checklists/requirements.md`](specs/002-operacao/checklists/requirements.md) |

### Requisitos do Marco 3 (modo Docker e autenticação)

Também fora do enunciado. O comportamento comum aos dois bancos é comprovado no `./mvnw -Pextras verify`,
e o que só existe no PostgreSQL, no perfil `postgres-it` (Princípio VI, constituição 2.0.0). A matriz completa está
em [`specs/003-modo-docker/plan.md`](specs/003-modo-docker/plan.md#rastreabilidade-princípio-vi). Os
`@DisplayName` dos testes novos começam por `S003`, porque os IDs das três specs se repetem.

| Extra | Requisitos | Como é comprovado |
| --- | --- | --- |
| Modo padrão idêntico, sem Kafka configurado | FR-001 | `DefaultModeIsolationIT`, a suíte dos Marcos 1 e 2 e o `check-unchanged-tests.sh` |
| PostgreSQL com o mesmo esquema e a mesma ordem da listagem | FR-004 a FR-008 | `SchemaPostgresIT`, `EquivalencePostgresIT`, `ListingOrderPostgresIT` (job `postgres`) |
| Kafka como caminho único, com a readiness depois do evento de carga concluída | FR-009 a FR-013 | `KafkaReferenceFileIT`, `KafkaReadinessIT`, `KafkaInvalidLinesIT`, `KafkaStructuralFailureIT`, `PublisherSummaryIT`, `KafkaBrokerUnavailableIT` |
| Gravação idempotente com atualização e reinícios | FR-014 | `KafkaIdempotencyIT`, `KafkaUpdateIT`, `RestartDirectPostgresIT`, `RestartPostgresIT` |
| Tópico de erros e nova tentativa quando a gravação falha | FR-015, FR-015a | `KafkaInvalidEventsIT`, `KafkaErrorTopicUnavailableIT`, `RetryPostgresIT` e o passo 7b do job `compose` |
| Vencedor novo depois da carga e métricas da ingestão | FR-016, FR-017 | `KafkaNewWinnerIT`, `KafkaMetricsIT` |
| Carga direta e Kafka equivalentes, sem exceção | FR-018 | `KafkaEquivalenceIT`, `FieldTooLongIT` |
| Autenticação por token, desligada por padrão | FR-019 a FR-023, FR-025 a FR-027 | `AuthDisabledByDefaultIT`, `AuthEnabledIT`, `AuthProblemDetailsIT`, `TokenNotLoggedIT`, `NoGeneratedPasswordIT`, `AuthContractIT`, `ApiDocsSecurityIT` |
| Cabeçalhos de cache e de proteção | FR-022a | `CacheHeadersIT`, `CacheHeadersWithTokenIT` |
| Compose com observabilidade, imagens por digest e credenciais de demonstração | FR-028 a FR-035 | Jobs `compose` e `workflow-policy` |
| README e ADRs | FR-036, FR-037 | Revisão do PR, pelas seções de [`specs/003-modo-docker/checklists/requirements.md`](specs/003-modo-docker/checklists/requirements.md) |

### Requisitos do Marco 4 (SLO, alertas, carga e dados gerados)

Também fora do enunciado, com o escopo reduzido descrito no roteiro da seção 3. A matriz completa está em
[`specs/004-acabamento-operacional/plan.md`](specs/004-acabamento-operacional/plan.md#rastreabilidade-princípio-vi).
Os `@DisplayName` dos testes novos começam por `S004`.

| Extra | Requisitos | Como é comprovado |
| --- | --- | --- |
| Modo padrão sem mudança | FR-001, FR-002, SC-001 | Job `build` e `check-unchanged-tests.sh` contra a `v0.2.0` e a `v0.3.0` |
| SLO, alertas e runbook | FR-003 a FR-009, SC-002, SC-003 | Job `alert-rules`, passo 4b do job `compose` e revisão de [`docs/slo.md`](docs/slo.md) e [`docs/runbook.md`](docs/runbook.md) |
| Teste de carga com limites | FR-010 a FR-013, FR-011a, SC-004 | Job `load` e o registro da medição em `docs/ai/decisoes.md` |
| Gerador de CSV e carga inicial medida | FR-014 a FR-019, SC-005, SC-006 | Job `load` (dois SHA-256 iguais ao do Windows, cargas sem linha rejeitada) |
| Inventário efetivo no job de dependências | FR-032, FR-033, SC-010 | Job `dependencies` e o registro da causa em `docs/ai/decisoes.md` |
| Testador adversário na ingestão por Kafka | FR-035 | `KafkaAdversarialEventsIT` e os ITs `KafkaCase*` (job `build`), `KafkaCase29RestartReappliesCsvPostgresIT` (job `postgres`) |

### Requisitos do Marco 5 (publicação no Cloud Run)

Também fora do enunciado. O marco não muda o comportamento da aplicação, então não tem IT novo. Cada requisito
é comprovado pela execução do pipeline, pelas execuções do script ou pela revisão (Princípio VI). A spec está
em [`specs/005-deploy-cloud-run/spec.md`](specs/005-deploy-cloud-run/spec.md).

| Extra | Requisitos | Como é comprovado |
| --- | --- | --- |
| Serviço no modo padrão, com 0 a 1 instância por revisão, probes e métricas fechadas | FR-001 a FR-007, FR-004a, SC-001, SC-006 | Job `deploy` na `main` (as duas conferências do `verify-deployment.sh`, com o 404 de `/actuator/prometheus` e os tempos de cada chamada) e a leitura do serviço depois do merge |
| A mesma imagem testada, pelo digest, sem rebuild | FR-002, FR-010 | Jobs `image` (digest e identidade da imagem) e `deploy` (digest do artefato e do registro) |
| Publicação só em push na `main`, depois dos checks, serializada e sem chaves | FR-008, FR-009, FR-011 a FR-013, SC-002, SC-003 | Job `workflow-policy` (regras 3, 4, 5, 9 e 10), com os casos negativos conferidos antes do push ([evidência](specs/005-deploy-cloud-run/evidencias/politica-workflow.txt)), o job `deploy` pulado nos PRs e a condição do provedor de identidade conferida no GCP ([evidência](specs/005-deploy-cloud-run/evidencias/setup-conferencias.txt)) |
| Revisão sem tráfego até ser conferida | FR-014 a FR-016, FR-015a | Job `deploy`: a conferência pela URL da revisão antes da troca de tráfego e o resumo da execução |
| Configuração do GCP idempotente, com permissões mínimas | FR-017 a FR-021, SC-004 | As duas execuções do `deploy/gcp/setup.sh` (a segunda com zero mudanças) em [`evidencias/`](specs/005-deploy-cloud-run/evidencias/) e a revisão do script no PR |
| Modo padrão sem mudança | FR-022, SC-007 | Jobs `build` e `workflow-policy` (`check-unchanged-tests.sh`) |
| README, ADR 0027, runbook e decisões | FR-023 a FR-025 | Revisão do PR, por [`specs/005-deploy-cloud-run/checklists/revisao.md`](specs/005-deploy-cloud-run/checklists/revisao.md) |
| Contrato 1.2.1: descrição em duas frases, autenticação opcional e nenhum `<` ou `>` nos textos | FR-027 | `ContractTextIT`, `ApiDocsSecurityIT` (versão) e `ApiDocsIT` (publicado idêntico ao da spec 005) |

### Requisitos do Marco 6 (front-end em Angular)

Também fora do enunciado. As agregações da API são comprovadas por IT. O front é comprovado pelos testes
unitários e pelo teste de ponta a ponta no pipeline, como a emenda 2.1.0 da constituição define (Princípio VI). A
spec está em [`specs/006-frontend-angular/spec.md`](specs/006-frontend-angular/spec.md).

| Extra | Requisitos | Como é comprovado |
| --- | --- | --- |
| Agregações de anos com mais de um vencedor e de vitórias por estúdio, com ETag, autenticação opcional e listas vazias | FR-001 a FR-003 | `AggregationsReferenceIT`, `YearsWithMultipleWinnersIT`, `StudiosWinCountIT`, `AggregationsEmptyIT` e `AggregationsEtagIT` (`./mvnw verify`), `AggregationsAuthIT` (`-Pextras`) e `AggregationsPostgresIT` (`postgres-it`) |
| Contrato 1.3.0, sem mudar os endpoints existentes | FR-004 a FR-006 | `AggregationsContractIT`, `ApiDocsIT`, `ApiDocsSecurityIT` e `ContractTextIT`; `check-unchanged-tests.sh` contra a `v0.2.0`, a `v0.3.0` e a `v0.5.0` |
| Painel com os quatro painéis e os estados de carregando, vazio e erro | FR-007 a FR-009, SC-002 | Testes unitários (job `frontend`) e `e2e/dashboard.spec.ts` (job `web`) |
| Lista paginada com filtros na URL | FR-010 a FR-014 | Testes unitários (job `frontend`) e `e2e/movies.spec.ts` (job `web`) |
| Responsivo, acessível, em português e com chamadas por caminho relativo | FR-015 a FR-018, SC-004 | `e2e/layout.spec.ts` nos projetos `desktop` e `celular`; usabilidade (SC-003) pelo checklist de revisão |
| Projeto em `frontend/`, fora do `./mvnw`, com testes e cobertura mínima de 80% | FR-019 a FR-023, SC-005, SC-006 | Jobs `build` (sem Node), `frontend` e `web` |
| Imagem nginx sem root, com repasse de `/api`, rotas internas e log de acesso em JSON | FR-025, FR-026, FR-026a | Job `web` ([`web-smoke-test.sh`](.github/scripts/web-smoke-test.sh), 10 passos) e `dockerfile-lint` |
| Publicação no `awards-web`, sem segredos, com 0 a 1 instância e revisão conferida antes do tráfego | FR-027 a FR-030, SC-001, SC-007 | Job `deploy-web` na `main`, `workflow-policy` com os casos negativos ([evidência](specs/006-frontend-angular/evidencias/politica-workflow.txt)) e as duas execuções do `setup.sh` ([evidência](specs/006-frontend-angular/evidencias/setup-execucoes.txt)) |
| Emenda 2.1.0, README, ADR 0028, runbook e decisões | FR-024, FR-031 a FR-034 | Revisão do PR, por [`specs/006-frontend-angular/checklists/revisao.md`](specs/006-frontend-angular/checklists/revisao.md) |
| Os 27 casos do testador adversário do front e das agregações (texto literal, busca mais recente, URL com valores inválidos, repetidos ou desconhecidos, páginas enormes, Unicode, empates, rede e teclado) | FR-008, FR-008a, FR-013, FR-017a | ITs em `aggregations/adversario/` (`./mvnw verify`), `frontend/src/app/adversario.spec.ts` (job `frontend`) e `frontend/e2e/adversario.spec.ts` (job `web`); registro em [`docs/ai/decisoes.md`](docs/ai/decisoes.md) |

---

## 7. Regras de negócio

As regras completas estão na [spec](specs/001-producer-intervals-api/spec.md).

### Arquivo e cabeçalho

- O separador é `;` e o cabeçalho é `year;title;studios;producers;winner`, **nessa ordem**. Os nomes das
  colunas não diferenciam maiúsculas (`Year;Title;...` é aceito). Ordem diferente, coluna faltando ou
  coluna a mais impedem a aplicação de subir.
- O arquivo é lido em UTF-8. O **BOM** no início é removido antes da leitura, então não atrapalha um
  cabeçalho entre aspas nem uma linha em branco logo depois dele.
- Linhas em branco, inclusive só com espaços, e espaços em volta dos campos são ignorados sem aviso.
- `winner` igual a `yes`, sem diferenciar maiúsculas e ignorando espaços, marca o vencedor. Vazio
  significa só indicado.

### Linhas inválidas e duplicadas

Uma linha inválida é ignorada, com um aviso que cita o número da linha física no arquivo (o cabeçalho
é a linha 1) e o motivo. A carga continua. Os motivos, verificados nesta ordem:

1. número de colunas diferente de 5;
2. ano vazio;
3. ano não numérico (aceita só dígitos, sem sinal);
4. ano fora da faixa de 1 a 9999, como `0` ou `10000`;
5. título vazio;
6. produtores vazios, inclusive quando a separação não gera nenhum nome;
7. `winner` diferente de `yes` ou vazio;
8. **duplicata:** o filme é identificado por ano + título. A primeira ocorrência vale, e as repetições
   são ignoradas com aviso que cita a linha original.

A coluna de estúdios é opcional. Ao final, o log traz o resumo de linhas lidas, aceitas e rejeitadas.

### Separação dos nomes de produtores

- Os separadores são a vírgula, a palavra `and` e a combinação `, and`.
- O `and` só separa como palavra inteira, sem diferenciar maiúsculas: `AND` e `aNd` também separam.
  `Robbinsand`, `Anderson` e `andrew` não são cortados.
- Vários `and` seguidos contam todos como separadores. Partes vazias são descartadas, e um nome
  repetido no mesmo filme conta uma vez.
- Os nomes são comparados **exatamente como escritos**, depois de tirar os espaços em volta. Não há
  correção de grafia, abreviação ou maiúsculas, e os espaços internos são preservados.

| Coluna `producers` | Nomes |
| --- | --- |
| `Eric Fellner, and Tom Hooper` | `Eric Fellner`, `Tom Hooper` |
| `Sid and Marty Krofft` | `Sid`, `Marty Krofft` |
| `Iris AND Nilo aNd Eva` | `Iris`, `Nilo`, `Eva` |
| `Brian Robbinsand Sharla Sumpter Bridgett` | um nome só |
| `A, , B` | `A`, `B` |
| `Producer A and and Producer B` | `Producer A`, `Producer B` |
| `, and` | nenhum (linha rejeitada) |

### Intervalos e empates

- Só os filmes **vencedores** contam.
- Para cada produtor, os anos de vitória são ordenados, e cada par de vitórias **vizinhas** gera um
  intervalo (`followingWin - previousWin`). A primeira vitória nunca é comparada com a última quando há
  outras entre elas.
- Duas vitórias no mesmo ano, em filmes diferentes, dão intervalo 0.
- `min` traz **todos** os intervalos iguais ao menor valor, e `max` **todos** os iguais ao maior. Dentro
  de cada lista, a ordem é por `previousWin` e depois pelo nome (por código Unicode, sem depender do
  idioma da máquina).
- O resultado não depende da ordem das linhas no arquivo. Nada do resultado está fixo no código: ele
  vem só do arquivo carregado.

---

## 8. Arquitetura

### Pacotes (`src/main/java/com/outsera/awards/`)

| Pacote | Responsabilidade |
| --- | --- |
| `ingestion` | Leitura e validação do CSV (`MovieCsvReader`, `MovieFileReading`), regras de campo comuns ao CSV e aos eventos (`MovieFieldRules`), separação dos produtores (`ProducerNameParser`), gravação com atualização por ano e título (`MovieImportService`, o único caminho de gravação), carga direta na inicialização (`MovieFileLoader`, fora do perfil `kafka`) e mensagem de falha (`MovieFileFailureAnalyzer`) |
| `ingestion.kafka` | Só no perfil `kafka`: publicador do arquivo (`MovieEventPublisher`), consumidor com o tópico de erros (`MovieEventListener`), leitura dos eventos (`MovieEventParser`), readiness pelo evento de carga concluída (`IngestionReadiness`) e tópicos e novas tentativas (`KafkaIngestionConfiguration`) |
| `security` | Cadeia sem estado do Spring Security (`SecurityConfiguration`), filtro do token (`TokenAuthenticationFilter`) e 401 em Problem Details (`ProblemAuthenticationEntryPoint`) |
| `movie` | Entidade `Movie`, consulta paginada com filtros (`MovieRepository`, `MovieQueryService`) e `GET /api/movies` |
| `producer` | Entidade `Producer`, cálculo dos intervalos (`ProducerIntervalService`) e `GET /api/producers/intervals` |
| `aggregation` | Agregações do painel (Marco 6): anos com mais de um vencedor e vitórias por estúdio (`AggregationService`, `StudioNameParser`) e `GET /api/aggregations/...` |
| `web` | Erros em Problem Details (`ApiExceptionHandler`) e ETag nos endpoints de negócio (`ContentEtagFilter`, registrado pelo `EtagConfiguration`) |

Saúde, métricas e o formato do log são só configuração, no `application.yml` e na variável
`LOGGING_STRUCTURED_FORMAT_CONSOLE`, sem código próprio.

### Fluxo da carga até a resposta

```mermaid
flowchart LR
    CSV["movielist.csv<br/>ou MOVIES_CSV_PATH"] --> Reader["MovieCsvReader<br/>BOM, cabeçalho, linhas"]
    Reader -->|linhas válidas| Parser["ProducerNameParser"]
    Reader -->|avisos e resumo| Log[(log)]
    Parser --> Import["MovieImportService"]
    Import --> H2[("H2 em memória<br/>esquema Flyway")]
    H2 --> Intervals["ProducerIntervalService"] --> E1["GET /api/producers/intervals"]
    H2 --> Query["MovieQueryService"] --> E2["GET /api/movies"]
```

1. O Flyway cria o esquema (`movie`, `producer` e `movie_producer`) no H2 em memória.
2. O `MovieFileLoader` roda depois de todos os beans estarem prontos e **antes** de o Tomcat aceitar
   conexões. Ele lê o arquivo, registra os avisos, grava os filmes e loga o resumo. Um erro estrutural
   interrompe a inicialização.
3. A cada requisição de intervalos, uma consulta JPQL traz os pares (produtor, ano) dos vencedores, e o
   serviço calcula os intervalos. Não há cache.
4. A listagem é uma consulta JPQL paginada com filtros opcionais. As consultas não usam SQL específico
   de banco.

No **modo Docker** (perfil `docker`, que liga `postgres` e `kafka`), o banco é o PostgreSQL, e a carga
passa pelo Kafka:

```mermaid
flowchart LR
    CSV["movielist.csv"] --> Pub["MovieEventPublisher<br/>mesma leitura e avisos"]
    Pub -->|"um evento por filme<br/>+ carga concluída"| Topic[["awards.movies"]]
    Ext["outro cliente<br/>(vencedor novo)"] --> Topic
    Topic --> Listener["MovieEventListener"]
    Listener -->|inválido| Errors[["awards.movies.errors"]]
    Listener -->|válido| Import["MovieImportService"]
    Listener -->|carga concluída| Ready["readiness"]
    Import --> PG[("PostgreSQL<br/>mesmo esquema Flyway")]
```

### Stack

| Item | Versão |
| --- | --- |
| Java | 25 |
| Spring Boot | 4.1.2 (Spring Framework 7.0.10, Tomcat 11.0.27, Jackson 3.1.7) |
| Persistência | Spring Data JPA com Hibernate 7.4.13, H2 2.4.240 e Flyway 12.4.0 |
| CSV | Apache Commons CSV 1.14.1 |
| Documentação | Swagger UI 5.34.0 (webjar) sobre o `openapi.yaml` estático |
| Operação | Spring Boot Actuator (liveness e readiness), Micrometer com o registro do Prometheus e o logging estruturado do Boot (ECS) |
| Build | Maven 3.9.16 pelo Maven Wrapper 3.3.4 |
| Testes | JUnit 6.0.3, `RestTestClient` do Spring, Maven Failsafe (duas execuções), JaCoCo 0.8.14 e o validador OpenAPI da Atlassian 3.0.0 |
| Imagem | `eclipse-temurin:25-jdk-alpine` no build e `eclipse-temurin:25-jre-alpine` na execução, por digest |
| Pipeline | GitHub Actions, CodeQL, Trivy, hadolint e Dependabot |
| Modo Docker | PostgreSQL 18 (driver 42.7.14), Spring for Apache Kafka 4.1.2 (kafka-clients 4.2.2), Kafka 4.4 na imagem nativa, Prometheus 3.16 e Grafana 13.3 |
| Autenticação | Spring Security (do Boot 4.1.2), com token por `Authorization: Bearer` |

---

## 9. Decisões

As decisões de arquitetura têm um ADR curto cada, com contexto, alternativas e consequências, no
[índice de `docs/adr/`](docs/adr/README.md). Abaixo, as principais, em uma linha cada. O motivo e as
alternativas de cada uma estão em [`docs/ai/decisoes.md`](docs/ai/decisoes.md).

- **Linha inválida é ignorada com aviso**, em vez de rejeitar o arquivo inteiro. Só erros estruturais
  impedem a aplicação de subir.
- **Duplicata por ano + título:** a primeira ocorrência vale, para que uma linha vencedora repetida não
  gere um intervalo 0 falso.
- **Cabeçalho sem diferenciar maiúsculas, mas com ordem fixa.**
- **Separação de produtores em etapas, e não por uma regex**, depois de dois defeitos na regex
  apontados pelo Codex na revisão.
- **BOM removido antes do parser do CSV**, também após revisão do Codex.
- **`and` separa sem diferenciar maiúsculas**, a partir de um caso do testador adversário. Os nomes
  continuam comparados exatamente como escritos.
- **Ano válido de 1 a 9999 no CSV**, também a partir do testador: o ano 0 era aceito.
- **Desempate por nome em ordem Unicode** (`String.compareTo`), para que o resultado não dependa do
  idioma da máquina.
- **Carga antes de o servidor aceitar conexões**, para que nenhuma consulta veja dados incompletos.
- **Cálculo dos intervalos a cada requisição, sem cache**, porque leva milissegundos.
- **Contrato estático em `/openapi.yaml` como fonte única:** ele alimenta o Swagger UI e valida as
  respostas nos testes. Não há contrato gerado pelo código.
- **Swagger UI pelo webjar, sem springdoc:** na versão 3.2.0, o springdoc só serve o Swagger UI com a
  geração automática do contrato ligada.
- **Página além da última responde 200 com a lista vazia**, inclusive quando o deslocamento passa do
  limite de `int`.
- **Mockito fora do classpath de teste**, para que a proibição de mocks seja garantida pelo compilador.

Do Marco 2:

- **Log em texto no `./mvnw` e JSON por variável, ligado na imagem**, para que o avaliador leia o
  terminal. A constituição foi emendada (1.3.0).
- **ETag pelo conteúdo, com um filtro próprio:** o `ShallowEtagHeaderFilter` do Spring não trata o
  `If-None-Match: *`.
- **Readiness pelo próprio ciclo de vida do Boot:** como a carga termina antes do servidor, não houve
  código novo.
- **Banner desligado em todas as execuções**, porque não traz informação.
- **Cobertura mínima de 90% de linhas**, abaixo dos 97,06% medidos no fim do Marco 1, para dar margem ao
  código que o Marco 3 só roda no perfil Docker.
- **Segurança reprova só alto e crítico**, e, nas dependências e na imagem, só com versão corrigida.
- **Referências aninhadas das actions conferidas**: a `trivy-action` e a `hadolint-action` usam por
  dentro só referências por hash ou digest.
- **Comprovação por tipo de requisito** (constituição 1.4.0): teste de integração, pipeline ou revisão.

Do Marco 3:

- **Kafka como caminho único no modo Docker**, com a readiness liberada só pelo evento de carga concluída
  desta execução (ADR 0014).
- **Gravação por ano e título com atualização:** a última gravação prevalece, e um reinício reaplica os
  valores do arquivo aos filmes que estão nele (ADR 0015).
- **Evento inválido vai ao tópico de erros, e a falha de gravação é tentada de novo** sem avançar no
  tópico. O `FIELD_TOO_LONG` vale nos dois caminhos de carga (ADR 0016).
- **PostgreSQL com collation C**, para a mesma ordem do H2, sem SQL específico (ADR 0013).
- **Autenticação desligada por padrão**, com comparação do token em tempo constante e sem
  `Cache-Control` nos endpoints de negócio, para preservar o ETag (ADR 0017).
- **Credenciais de demonstração só no compose local** (constituição 1.6.0; ADR 0018).
- **Testes com PostgreSQL num perfil Maven dedicado**, obrigatório no pipeline (constituição 1.7.0;
  ADR 0019).

Do Marco 5 (ADR 0027):

- **Cloud Run com 0 a 1 instância por revisão,** só no modo padrão e com as métricas fechadas na URL
  pública.
- **Publicação a cada merge na `main`, pela mesma imagem testada,** identificada pelo digest e sem rebuild.
- **Identidade federada sem chaves,** restrita ao repositório e à `main` pelos IDs numéricos, com papéis só
  no serviço, no registro e na conta de execução.
- **Revisão sem tráfego até ser conferida pela URL própria,** para que uma revisão ruim nunca chegue à URL
  pública.

---

## 10. Uso de IA

O projeto foi feito com **Spec-Driven Development** pelo [GitHub Spec Kit](https://github.com/github/spec-kit),
no modo *spec-anchored*: a spec é a fonte da verdade e muda antes do código.

**Artefatos do Spec Kit:**
- a [constituição](.specify/memory/constitution.md), com sete princípios inegociáveis;
- a [spec](specs/001-producer-intervals-api/spec.md);
- o [plano](specs/001-producer-intervals-api/plan.md);
- a [research](specs/001-producer-intervals-api/research.md);
- as [tarefas](specs/001-producer-intervals-api/tasks.md).

As regras comuns a todos os agentes estão no [`AGENTS.md`](AGENTS.md).

**Fluxo por bolts:** o trabalho é dividido em ciclos curtos, os *bolts*, cada um num branch próprio.

1. O implementador escreve primeiro os testes de integração, que precisam falhar, e depois o código.
2. O responsável abre o pull request.
3. O revisor comenta no PR.
4. O implementador corrige só o que o responsável aceitar.
5. O merge acontece com os testes verdes, e a sessão é exportada.

PR, merge e tags são feitos só pelo responsável, nunca por um agente.

| Bolt | Entrega |
| --- | --- |
| 1 | Projeto Spring Boot e infraestrutura de teste |
| 2 | Carga do CSV |
| 3 | Cálculo dos intervalos |
| 4 | Endpoint de intervalos no formato do enunciado |
| 5 e 6 | Listagem, Swagger UI e fechamento, num único PR |
| Marco 2 | Um único bolt, com seis etapas e um commit por etapa: ETag, observabilidade, Dockerfile, pipeline e Dependabot, ADRs e README ([spec 002](specs/002-operacao/spec.md)) |
| Marco 3 | Um único bolt, com cinco etapas e um commit por etapa: PostgreSQL e perfil `docker`, Kafka, autenticação, compose com observabilidade, e ADRs com o README ([spec 003](specs/003-modo-docker/spec.md)) |
| Marco 5 | Um único bolt, com quatro etapas e um commit por etapa: configuração do GCP pelo script, política do workflow e imagem como artefato, job `deploy` e documentação ([spec 005](specs/005-deploy-cloud-run/spec.md)) |

**Papéis:**

| Papel | Ferramenta | O que faz |
| --- | --- | --- |
| Implementador | Claude Code | Escreve testes e código de cada bolt |
| Revisor de pull request | Codex | Comenta cada PR e justifica os apontamentos, sem reescrever o código |
| Testador adversário | Codex | Cria CSVs de ataque a partir da spec, sem ler o código de produção. Cada falha vira teste permanente |
| Documentação | Claude Code, em sessão separada | README e guia da evidência de IA |

**Rodada do testador adversário:** feita sobre o Marco 1. O Codex trabalhou numa pasta isolada, só com
o enunciado, a spec e o contrato, e entregou 13 casos de ataque, em
[`docs/ai/adversario/`](docs/ai/adversario/). Cada caso virou um teste de integração permanente em
`src/test/java/com/outsera/awards/adversario/`.
- **11 dos 13 casos passaram sem nenhuma correção.**
- Os outros dois levaram às decisões registradas em [`docs/ai/decisoes.md`](docs/ai/decisoes.md):
  - o caso 12 fez o separador `and` deixar de diferenciar maiúsculas (FR-007);
  - o caso 09 fez o ano válido ficar limitado de 1 a 9999 (FR-006), porque o ano 0 era aceito.
- O caso 13 também pedia uma decisão (a ordem dos nomes com acento e caixa), mas o código já seguia a
  ordem escolhida.

**Evidência:** cada mensagem enviada aos agentes é gravada com data e hora por um hook. As sessões são
exportadas por bolt, e as sugestões aceitas ou rejeitadas ficam registradas com o motivo. O guia de
leitura está em [`docs/ai/README.md`](docs/ai/README.md).

---

## 11. Estrutura do repositório

```text
.
├── README.md                    # este arquivo
├── AGENTS.md                    # regras comuns a todos os agentes de IA
├── CLAUDE.md                    # aponta o Claude Code para o AGENTS.md
├── pom.xml                      # build Maven (dependências, Failsafe, perfis extras e postgres-it, JaCoCo)
├── mvnw, mvnw.cmd, .mvn/        # Maven Wrapper: dispensa instalar o Maven
├── Dockerfile                   # imagem multi-stage, não root (opcional)
├── .dockerignore                # lista de permissão do contexto da imagem
├── compose.yaml                 # modo Docker: aplicação, PostgreSQL, Kafka, Prometheus e Grafana; k6 no perfil load (opcional)
├── compose.load.yaml            # teste de carga: monta o CSV gerado na aplicação (Marco 4)
├── observability/               # Prometheus (com as regras do SLO em rules/) e provisionamento e dashboard do Grafana
├── load/intervals.js            # roteiro do k6, com os limites do SLO (Marco 4)
├── tools/csv-generator/         # gerador de CSV grande e determinístico, só com o JDK (Marco 4)
├── deploy/gcp/                  # configuração idempotente do GCP (setup.sh) e retenção do registro (Marcos 5 e 6)
├── frontend/                    # front em Angular 22.3 (Marco 6): src/, e2e/ (Playwright), nginx/, Dockerfile e README próprio
├── .github/
│   ├── workflows/ci.yml         # pipeline de CI e publicação no Cloud Run (jobs deploy e deploy-web)
│   ├── scripts/                 # política do workflow, contexto da imagem, fumaça, compose, conferência da publicação e ITs inalterados
│   ├── unchanged-tests-exceptions.txt # ITs da v0.2.0 que podem mudar, cada um com a decisão que autoriza
│   └── dependabot.yml           # atualizações de Maven, Actions, imagens base, imagens do compose e npm do front
├── .gitattributes               # fim de linha LF no repositório
├── .gitignore
├── .claude/                     # configuração do Claude Code: hook de prompts e skills do Spec Kit
├── .specify/                    # Spec Kit: constituição, templates e scripts
│   └── memory/constitution.md   # os sete princípios do projeto
├── specs/001-producer-intervals-api/
│   ├── spec.md                  # requisitos (FR/SC), cenários e casos de borda
│   ├── plan.md                  # plano técnico e matriz requisito → teste
│   ├── research.md              # decisões técnicas (R1 a R12)
│   ├── data-model.md            # tabelas, validação do arquivo e mensagens de log
│   ├── quickstart.md            # roteiro de validação de ponta a ponta
│   ├── tasks.md                 # tarefas agrupadas em bolts
│   ├── contracts/openapi.yaml   # contrato OpenAPI do Marco 1 (registro)
│   └── checklists/              # checklist de qualidade da spec
├── specs/002-operacao/          # Marco 2: spec, plano, research, contratos e tarefas
│   └── contracts/               # OpenAPI 1.1.0 (registro), endpoints operacionais, imagem e pipeline
├── specs/003-modo-docker/       # Marco 3: spec, plano, research, contratos e tarefas
│   └── contracts/               # OpenAPI 1.2.0 (registro), eventos do Kafka, compose e pipeline
├── specs/004-acabamento-operacional/ # Marco 4: spec, plano, research, contratos e tarefas
├── specs/005-deploy-cloud-run/  # Marco 5: spec, plano, research, contratos, tarefas e evidências do GCP
│   └── contracts/               # OpenAPI 1.2.1 (registro), publicação, script do GCP e política do workflow
├── specs/006-frontend-angular/  # Marco 6: spec, plano, research, contratos, tarefas e evidências
│   └── contracts/               # OpenAPI 1.3.0 (fonte única), interface, imagem do front e pipeline
├── src/main/java/com/outsera/awards/
│   ├── AwardsApplication.java   # classe principal
│   ├── ingestion/               # leitura, validação e carga do CSV; kafka/: ingestão por Kafka (perfil kafka)
│   ├── movie/                   # filmes e GET /api/movies
│   ├── producer/                # produtores e GET /api/producers/intervals
│   ├── aggregation/             # agregações do painel (Marco 6)
│   ├── security/                # autenticação por token (Spring Security)
│   └── web/                     # Problem Details e ETag
├── src/main/resources/
│   ├── application.yml          # PORT, MOVIES_CSV_PATH, API_TOKEN, Actuator e métricas, com padrões
│   ├── application-postgres.yml # perfil postgres: DB_URL, DB_USER e DB_PASSWORD
│   ├── application-kafka.yml    # perfil kafka: broker, tópicos, grupo e readiness
│   ├── movielist.csv            # arquivo do enunciado (padrão)
│   ├── db/migration/            # esquema Flyway
│   ├── static/openapi.yaml      # cópia publicada do contrato
│   ├── static/swagger-ui.html   # página do Swagger UI
│   └── META-INF/spring.factories # registra a mensagem de falha da carga
├── src/test/java/com/outsera/awards/  # somente testes de integração (*IT)
│   ├── adversario/  ingestion/  intervals/  movies/  docs/  web/  support/   # modo padrão (lista em src/test/default-mode-its.txt)
│   ├── etag/  health/  metrics/  logging/   # Marco 2; o JsonLogFormatIT só no perfil extras
│   ├── isolation/  kafka/  security/        # Marco 3; kafka/ e a autenticação ligada no perfil extras
│   ├── aggregations/                        # Marco 6; o AggregationsAuthIT só no perfil extras
│   ├── postgres/                            # Marco 3, só no perfil postgres-it
│   └── SmokeIT.java
├── src/test/resources/scenarios/  # um CSV por cenário de teste
├── docs/slo.md                  # objetivos do SLO, orçamento e alertas (Marco 4)
├── docs/runbook.md              # o que fazer quando cada alerta dispara (Marco 4) e quando a publicação falha (Marcos 5 e 6)
├── docs/adr/                    # decisões de arquitetura (ADRs), com índice
└── docs/ai/                     # evidência do uso de IA (ver docs/ai/README.md)
```
