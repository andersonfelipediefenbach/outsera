# Feature Specification: Marco 3 — Modo Docker: PostgreSQL, ingestão por Kafka, autenticação e compose

**Feature Branch**: `marco/3-docker`

**Created**: 2026-11-24

**Status**: Implemented. Faltam as verificações que exigem o push e o PR: o job `postgres` (T020 e T051), os jobs do pipeline com o `compose` (T084), o testador adversário da ingestão por Kafka (T050) e os checks obrigatórios da `main` (T093), todas no `tasks.md`.

**Input**: User description: "Marco 3, modo Docker, conforme docs/ai/planejamento/briefing.md. Escopo: 1. Perfil docker com PostgreSQL no lugar do H2, usando as mesmas migrações do Flyway. 2. Kafka como caminho único de ingestão no perfil docker: um publicador lê o CSV e envia uma mensagem por filme; um consumidor grava pelo mesmo serviço do modo padrão; uma mensagem de carga concluída libera a readiness. Gravação idempotente por ano e título, e tópico de erros para mensagens inválidas. Novos vencedores podem chegar pelo mesmo tópico depois da carga. 3. Autenticação por token parametrizada e desligada por padrão: o token vem de variável de ambiente e é comparado com um cabeçalho; sem a variável, os endpoints ficam públicos; com ela, os endpoints operacionais de métricas exigem token. 4. compose.yaml com a aplicação, PostgreSQL, Kafka (imagem leve), Prometheus e Grafana, com o dashboard versionado. Restrições: o modo padrão continua idêntico, subindo e testando só com ./mvnw, sem Docker. Os testes do Kafka usam o broker embutido do Spring. Os testes com PostgreSQL ficam em um perfil Maven separado, executado no pipeline. Um teste prova que a carga direta e a carga por Kafka produzem a mesma resposta. Fora do escopo: publicação em nuvem e front-end."

## Clarifications

### Session 2026-11-24

- Q: Um evento com o ano e o título de um filme já gravado é ignorado ou atualiza o filme? → A:
  Atualiza. A última gravação prevalece: estúdios, produtores e `winner` passam a ser os do evento, e um
  indicado pode virar vencedor por evento. Consequência aceita: na subida, o arquivo é publicado de novo
  e reaplica os seus valores aos filmes que estão nele, então uma mudança recebida por evento num filme
  do arquivo é desfeita no reinício. Filmes que não estão no arquivo não são afetados. Duplicatas dentro
  do próprio arquivo continuam ignoradas pelo publicador (spec 001, FR-006d), o que preserva a
  equivalência com a carga direta. FR-014, US2-4 a US2-6.
- Q: Com o token definido, os endpoints de negócio também exigem o token? → A: Sim. Intervalos,
  listagem e métricas exigem o token. Liveness, readiness, Swagger UI e contrato OpenAPI continuam
  públicos. FR-020 a FR-022 e US4-4.
- Q: Como ficam as credenciais do PostgreSQL e do Grafana no compose? → A: Senhas de demonstração,
  documentadas no README como o token. O Princípio VII foi emendado para permiti-las (constituição
  1.6.0), com as condições da FR-033. FR-033.

## User Scenarios & Testing *(mandatory)*

Os atores são:

- o **avaliador da Outsera**, que roda só o modo padrão (`./mvnw`) e precisa encontrar exatamente o
  comportamento dos Marcos 1 e 2;
- o **operador do ambiente completo**, que sobe tudo com `docker compose up`, acompanha a carga, a
  saúde, as métricas e o dashboard, e reinicia a aplicação sem perder dados;
- o **sistema publicador de resultados**, qualquer cliente que envia ao tópico de filmes um novo
  filme ou um novo vencedor depois da carga inicial;
- o **consumidor da API**, que chama os endpoints de negócio com ou sem token, conforme a autenticação
  esteja ligada;
- o **mantenedor**, que recebe no pipeline a validação do perfil de PostgreSQL e do ambiente compose.

### User Story 1 - Carregar os dados pelo Kafka no modo Docker (Priority: P1)

No modo Docker, a aplicação não grava o CSV diretamente. Ela publica um evento por filme num tópico e
grava o que consome desse tópico, pelo mesmo serviço de gravação do modo padrão. Ao terminar de
publicar, envia um evento de carga concluída, e só se declara pronta depois de consumi-lo. Quem consulta
a API, depois disso, recebe as mesmas respostas que o modo padrão daria com o mesmo arquivo.

**Why this priority**: É o núcleo do marco e o único item que toca o caminho de gravação dos dados. A
equivalência com a carga direta é o que garante que o extra não altera o resultado avaliado
(Princípios III e V).

**Independent Test**: Com o broker embutido, sem Docker, subir a aplicação com a ingestão por Kafka e o
arquivo fornecido, conferir que a readiness só fica positiva depois do evento de carga concluída e que os
intervalos e a listagem são idênticos aos da carga direta com o mesmo arquivo.

**Acceptance Scenarios**:

1. **Given** a ingestão por Kafka ligada e o arquivo fornecido, **When** a aplicação termina de subir,
   **Then** os intervalos trazem Joel Silver em `min` (1990–1991) e Matthew Vaughn em `max` (2002–2015).
2. **Given** o mesmo arquivo carregado uma vez pela carga direta e outra pela ingestão por Kafka, em
   execuções separadas, **When** os intervalos e todas as páginas da listagem são consultados nas duas,
   **Then** os corpos das respostas são idênticos byte a byte, e os ETags também.
3. **Given** a ingestão por Kafka ligada, **When** a carga ainda não terminou de ser consumida, **Then**
   a readiness responde 503. **When** o evento de carga concluída desta execução é consumido, **Then**
   a readiness passa a 200, e nesse momento todos os filmes aceitos do arquivo já estão gravados.
4. **Given** um arquivo com linhas inválidas, mas estruturalmente válido, **When** a ingestão por Kafka
   termina, **Then** as linhas inválidas não são publicadas, cada uma gera o mesmo aviso do Marco 1
   (número da linha e motivo), o resumo traz lidas, aceitas e rejeitadas, e a aplicação fica pronta.
5. **Given** um arquivo ausente ou com cabeçalho inválido, **When** a aplicação sobe com a ingestão por
   Kafka, **Then** ela não sobe, com a mesma mensagem clara do Marco 1, e nada é publicado.
6. **Given** a ingestão por Kafka ligada, **When** a aplicação sobe, **Then** a carga direta não roda: o
   tópico é o único caminho de entrada dos dados.

---

### User Story 2 - Persistir os dados no PostgreSQL e sobreviver a reinícios (Priority: P1)

No modo Docker, os dados ficam num PostgreSQL, criado pelas mesmas migrações do modo padrão. Ao
reiniciar, a aplicação publica o arquivo de novo, e a gravação idempotente por ano e título impede
duplicatas. Os filmes novos recebidos por evento depois da carga continuam lá.

**Why this priority**: O banco persistente é o que dá sentido ao Kafka (briefing). Sem a
idempotência, um simples reinício duplicaria os dados e mudaria o resultado.

**Independent Test**: No perfil Maven de PostgreSQL, subir a aplicação duas vezes sobre o mesmo banco,
publicando um vencedor novo entre as duas execuções, e conferir por HTTP que o resultado da segunda
execução tem o vencedor novo e nenhuma duplicata.

**Acceptance Scenarios**:

1. **Given** um PostgreSQL vazio, **When** a aplicação sobe no perfil de PostgreSQL, **Then** as
   migrações do modo padrão criam o esquema, sem nenhuma migração específica de um banco, e a validação
   do esquema pelo mapeamento passa.
2. **Given** a aplicação no perfil de PostgreSQL com o arquivo fornecido, **When** os intervalos e a
   listagem são consultados, **Then** as respostas são idênticas às do modo padrão com H2, inclusive a
   ordem da listagem.
3. **Given** a aplicação já carregada sobre um banco persistente, **When** ela reinicia e publica o
   arquivo de novo, **Then** nenhum filme, produtor ou vínculo é duplicado, e as respostas continuam as
   mesmas.
4. **Given** um filme novo (ano e título que não estão no arquivo) recebido por evento depois da
   carga, **When** a aplicação reinicia sobre o mesmo banco, **Then** o filme novo continua nos
   intervalos e na listagem.
5. **Given** um evento com o ano e o título de um filme já gravado e outros estúdios, produtores ou
   `winner`, **When** ele é consumido, **Then** o filme gravado passa a ter os valores do evento, sem
   ser duplicado, e os intervalos e a listagem refletem a mudança.
6. **Given** um filme do arquivo alterado por evento (por exemplo, um indicado que virou vencedor),
   **When** a aplicação reinicia e publica o arquivo de novo, **Then** o filme volta aos valores do
   arquivo (Clarifications, 2026-11-24).

---

### User Story 3 - Receber novos vencedores depois da carga (Priority: P2)

Depois da carga inicial, um sistema publicador envia ao mesmo tópico um evento com um filme novo. A
aplicação o grava pelo mesmo serviço, e os intervalos e a listagem passam a refleti-lo, sem reinício.
Eventos inválidos vão para um tópico de erros, com o motivo, e não travam o consumo dos seguintes.

**Why this priority**: É o uso real da ingestão por eventos, mas o resultado de referência não depende
dele.

**Independent Test**: Com o broker embutido, depois da carga, publicar um evento válido que cria um
intervalo novo, conferir por HTTP a mudança nos intervalos e no ETag, publicar um evento inválido e
conferir o tópico de erros e que um evento válido seguinte é gravado.

**Acceptance Scenarios**:

1. **Given** a aplicação pronta, **When** chega um evento de um filme vencedor novo que dá a um produtor
   um intervalo menor que o mínimo atual, **Then** em até 5 segundos os intervalos trazem esse produtor
   em `min`, no formato exato do enunciado.
2. **Given** um ETag dos intervalos obtido antes do evento novo, **When** os intervalos são consultados
   com esse ETag depois do evento, **Then** a resposta é 200 com o corpo novo e um ETag diferente.
3. **Given** um evento inválido (formato ilegível, campo obrigatório ausente, ano fora da faixa ou que
   não é inteiro JSON, `winner` que não é booleano, produtores vazios ou campo maior que a coluna),
   **When** ele é consumido, **Then** ele não é gravado, vai
   para o tópico de erros com o conteúdo original e o motivo, gera um aviso no log, e o consumo segue.
4. **Given** um evento inválido seguido de um válido, **When** os dois são consumidos, **Then** o válido
   é gravado.
5. **Given** a aplicação pronta, **When** um evento válido é gravado ou um inválido vai ao tópico de
   erros, **Then** a readiness continua 200.
6. **Given** um evento com produtores no mesmo formato da coluna do CSV (vírgulas, ` and `, `, and`),
   **When** ele é gravado, **Then** os nomes são separados pelas mesmas regras do Marco 1 (spec 001,
   FR-007 a FR-010).

---

### User Story 4 - Proteger a API com um token, quando ligado (Priority: P2)

O operador liga a autenticação definindo a variável de ambiente do token. A partir daí, os endpoints
protegidos exigem esse token num cabeçalho, e uma chamada sem token ou com token errado recebe 401 em
Problem Details. Sem a variável, tudo continua como no Marco 2.

**Why this priority**: É um extra da API previsto desde o briefing, testável no `./mvnw verify`, mas o
avaliador chama sem token, então o padrão desligado é o que importa para a entrega.

**Independent Test**: Subir a aplicação sem a variável e conferir os endpoints públicos; subir com a
variável e conferir 401 sem token, 401 com token errado e 200 com o token certo, nos endpoints
protegidos, e 200 sem token nos públicos.

**Acceptance Scenarios**:

1. **Given** a variável do token ausente ou vazia, **When** qualquer endpoint é chamado sem token,
   **Then** o comportamento é idêntico ao do Marco 2.
2. **Given** a variável do token definida, **When** o endpoint de métricas é chamado sem token ou com
   token errado, **Then** a resposta é 401 em Problem Details, sem o valor esperado nem o recebido.
3. **Given** a variável do token definida, **When** o endpoint de métricas é chamado com o token certo,
   **Then** a resposta é 200 no formato do Prometheus.
4. **Given** a variável do token definida, **When** os intervalos ou a listagem são chamados sem token
   ou com token errado, **Then** a resposta é 401 em Problem Details. Com o token certo, a resposta é a
   do Marco 2, inclusive o ETag e o 304.
5. **Given** a variável do token definida, **When** a liveness, a readiness, o Swagger UI e o contrato
   OpenAPI são chamados sem token, **Then** eles respondem normalmente: o healthcheck do container e o
   orquestrador não enviam token, e quem lê a documentação precisa dela para informar o token.
6. **Given** a autenticação ligada, **When** uma chamada com token errado é registrada no log ou nas
   métricas, **Then** nenhum valor de token aparece.

---

### User Story 5 - Subir o ambiente completo com um comando (Priority: P3)

O operador roda `docker compose up` num clone limpo e obtém a aplicação no perfil Docker, o PostgreSQL,
o Kafka, o Prometheus coletando as métricas da aplicação e o Grafana com o dashboard versionado já
carregado, mostrando latência, taxa de erro e o atraso do consumidor.

**Why this priority**: Mostra o ambiente completo, mas o enunciado não pede nada disso e o avaliador
não depende dele.

**Independent Test**: No pipeline, subir o compose, esperar a readiness da aplicação, conferir o
resultado de referência, conferir que o Prometheus tem o alvo da aplicação ativo e que o Grafana tem o
dashboard provisionado.

**Acceptance Scenarios**:

1. **Given** um clone limpo e o Docker disponível, **When** `docker compose up` é executado sem
   nenhuma variável definida, **Then** a aplicação fica pronta e os intervalos trazem o resultado de
   referência.
2. **Given** o compose no ar, **When** o Prometheus é consultado, **Then** o alvo da aplicação está
   ativo e as séries de requisições HTTP e de atraso do consumidor estão disponíveis.
3. **Given** o compose no ar, **When** o Grafana é aberto, **Then** o dashboard do repositório está
   carregado, com fonte de dados apontando para o Prometheus, sem configuração manual.
4. **Given** o compose no ar com a autenticação ligada pelo token, **When** o Prometheus coleta,
   **Then** a coleta continua funcionando, com o token passado pela configuração do compose.
5. **Given** o compose derrubado sem apagar os volumes e subido de novo, **When** os intervalos são
   consultados, **Then** os dados continuam lá, sem duplicatas, inclusive os recebidos por evento.
6. **Given** a máquina sem Docker, **When** `./mvnw verify` e `./mvnw spring-boot:run` são executados,
   **Then** os dois funcionam como no Marco 2.

---

### Edge Cases

- Broker indisponível ao subir no modo Docker: a aplicação não se declara pronta enquanto a carga não
  for concluída; a liveness continua positiva. O compose só sobe a aplicação depois de o Kafka e o
  PostgreSQL ficarem saudáveis.
- Evento de carga concluída de uma execução anterior ainda no tópico, ou consumido de novo: não libera a
  readiness da execução atual. Só o evento desta execução conta.
- Dois reinícios seguidos, com a primeira publicação interrompida no meio: a segunda publica o arquivo
  inteiro, e a gravação idempotente evita duplicatas.
- Evento repetido (mesma mensagem entregue duas vezes pelo broker): o estado final é o mesmo de uma
  entrega só.
- Evento que tira um produtor de um filme: o vínculo é removido. Um produtor que fica sem filmes não
  aparece em nenhuma resposta, porque os intervalos só usam vencedores.
- Evento que muda um vencedor para indicado: os intervalos deixam de contar essa vitória.
- Evento com o título com espaços em volta: identificado pelo título sem esses espaços, como no CSV
  (spec 001, FR-006d).
- Evento cujo filme não é vencedor: gravado como indicado; não muda os intervalos, mas aparece na
  listagem.
- Evento ilegível (não é um objeto no formato documentado, ou não está em UTF-8): vai ao tópico de
  erros com o conteúdo original, sem derrubar o consumidor.
- Tópico de erros indisponível: o evento inválido não é gravado e o motivo fica no log; o consumo segue.
- Falha ao gravar um evento válido (banco indisponível ou recusando a gravação): o evento não é
  descartado nem vai ao tópico de erros. A gravação é tentada de novo até dar certo (FR-015a).
- Campo maior que a coluna do esquema (título com mais de 500 caracteres, por exemplo): a linha do CSV e
  o evento são rejeitados pela mesma regra (`FIELD_TOO_LONG`, spec 001 FR-006), nos dois caminhos de
  carga.
- Títulos com acentos, maiúsculas e minúsculas misturadas: a ordem da listagem no PostgreSQL é a mesma do
  H2 (FR-008).
- Token com espaços em volta ou em maiúsculas diferentes no cabeçalho: comparado exatamente, sem
  normalização; diferente é 401.
- Variável do token definida como texto vazio: equivale a autenticação desligada.
- Chamada com o cabeçalho do token quando a autenticação está desligada: ignorado, resposta normal.
- `OPTIONS` com a autenticação ligada: segue a mesma matriz de acesso. Num endpoint protegido, sem token,
  a resposta é 401. Sem a autenticação, a resposta é a do Marco 2. O CORS (e, com ele, o preflight)
  está fora do escopo deste marco e entra com o front-end (Marco 6).

## Requirements *(mandatory)*

### Functional Requirements

**Perfis e isolamento**

- **FR-001**: O modo padrão MUST continuar idêntico ao do Marco 2: H2 em memória, carga direta do CSV na
  inicialização, sem nenhuma configuração, dependência em execução ou conexão de Kafka ou PostgreSQL.
  `./mvnw spring-boot:run` e `./mvnw verify` MUST funcionar sem Docker (Princípio II).
- **FR-002**: O modo Docker MUST ser ligado por um perfil (`docker`), que liga o PostgreSQL e a
  ingestão por Kafka. A ingestão por Kafka e o banco MUST poder ser ligados separadamente, para que os
  testes da ingestão rodem com H2 e o broker embutido no `./mvnw verify`.
- **FR-003**: Toda configuração do modo Docker (endereço do banco, usuário, endereço do broker, nomes
  dos tópicos e grupo do consumidor) MUST vir de variáveis de ambiente, com padrões que funcionem no
  compose (Princípio VII). O consumidor MUST se registrar no broker com o grupo configurado
  (`KAFKA_CONSUMER_GROUP`), e não com um nome fixo no código (apontamento do Codex no PR do Marco 3).

**PostgreSQL**

- **FR-004**: No perfil de PostgreSQL, o esquema MUST ser criado pelas mesmas migrações do modo padrão,
  sem migração específica de um banco, e validado pelo mapeamento na subida, como no H2.
- **FR-005**: Nenhuma consulta MUST usar SQL específico de H2 ou de PostgreSQL.
- **FR-006**: No perfil de PostgreSQL, as respostas dos intervalos e da listagem MUST ser idênticas às do
  modo padrão com o mesmo arquivo.
- **FR-007**: Os testes que exigem PostgreSQL MUST ficar num perfil Maven separado, fora do `./mvnw
  verify` padrão, e MUST rodar no pipeline, que fica vermelho se algum falhar.
- **FR-008**: A ordem da listagem (ano crescente e depois título, spec 001, FR-022) MUST ser a mesma nos
  dois bancos, inclusive para títulos com acentos e com maiúsculas e minúsculas misturadas.

**Ingestão por Kafka**

- **FR-009**: Com a ingestão por Kafka ligada, a carga direta MUST NOT rodar. Um publicador MUST ler o
  arquivo com as mesmas regras da spec 001 (FR-002 a FR-006e): erros estruturais impedem a subida com a
  mesma mensagem, linhas inválidas são ignoradas com o mesmo aviso, e o resumo da leitura é registrado.
- **FR-010**: O publicador MUST enviar um evento por filme aceito ao tópico de filmes, identificado por
  ano e título. O evento é um objeto JSON em UTF-8 com:
  - `year`: inteiro JSON;
  - `title`: texto;
  - `studios`: texto ou `null`;
  - `producers`: texto no formato da coluna do CSV;
  - `winner`: booleano JSON (`true` ou `false`). O publicador converte o `yes` do CSV em `true`.

  O formato do evento MUST ser documentado no repositório.
- **FR-011**: Ao terminar de publicar, o publicador MUST enviar um evento de carga concluída que
  identifica a execução atual e a quantidade de filmes publicados.
- **FR-012**: Um consumidor MUST gravar cada evento de filme pelo mesmo serviço de gravação do modo
  padrão (Princípio V). A separação dos produtores e as regras de conteúdo de cada campo MUST ser as
  mesmas da linha do CSV (spec 001, FR-006 a FR-010), aplicadas pela mesma validação nos dois caminhos:
  - ano de 1 a 9999;
  - título não vazio;
  - produtores com ao menos um nome;
  - tamanho máximo de cada campo (`FIELD_TOO_LONG`).

  O que muda é só a representação, que o JSON impõe:
  - `year` precisa ser um inteiro JSON. Um texto, mesmo só com dígitos, ou um decimal é rejeitado;
  - `winner` precisa ser booleano JSON e é obrigatório. O `yes` e o vazio do CSV não existem no
    evento;
  - motivos que só existem no evento: bytes fora de UTF-8, conteúdo que não é objeto JSON, `type`
    desconhecido e marcador sem `runId`.

  Entre eventos com o mesmo ano e título, vale a FR-014.
- **FR-013**: A readiness MUST ficar negativa até o consumidor processar o evento de carga concluída da
  execução atual, e nesse momento todos os filmes publicados nessa execução MUST estar gravados. Eventos
  de carga concluída de execuções anteriores MUST NOT liberar a readiness.
- **FR-014**: A gravação MUST ser idempotente por ano e título: republicar o arquivo, ou receber o mesmo
  evento mais de uma vez, MUST NOT duplicar filmes, produtores ou vínculos. Um evento com o ano e o
  título de um filme já gravado MUST atualizar esse filme: estúdios, produtores e `winner` passam a ser
  os do evento (a última gravação prevalece). Na subida, a republicação do arquivo reaplica os valores do
  arquivo aos filmes que estão nele (Clarifications, 2026-11-24). Duplicatas dentro do arquivo continuam
  ignoradas pelo publicador (spec 001, FR-006d).
- **FR-015**: Um evento inválido MUST NOT ser gravado. Ele MUST ser enviado a um tópico de erros, com o
  conteúdo original e o motivo, e gerar um aviso no log com o motivo. O consumo dos eventos seguintes
  MUST continuar.
- **FR-015a**: Quando a gravação de um evento válido falha (por exemplo, banco indisponível ou recusando
  a gravação), o evento MUST NOT ser descartado nem enviado ao tópico de erros. A gravação MUST ser
  tentada de novo, sem avançar no tópico, até dar certo, e a readiness não muda por causa disso.
  Comprovação (FR-038):
  - o `RetryPostgresIT` prova a nova tentativa quando a gravação falha: a tabela é renomeada e depois
    devolvida;
  - a queda real do banco é comprovada pelo passo 7b do teste do compose.
- **FR-016**: Depois da carga, eventos de filmes novos recebidos pelo mesmo tópico MUST ser gravados e
  refletidos nos intervalos e na listagem, sem reinício e sem mudar a readiness.
- **FR-017**: As métricas MUST incluir o atraso do consumidor no tópico de filmes e a contagem de eventos
  gravados e de eventos enviados ao tópico de erros.
- **FR-018**: Um teste de integração no `./mvnw verify`, com o broker embutido, MUST provar que o mesmo
  arquivo pela carga direta e pela ingestão por Kafka produz respostas idênticas nos intervalos e na
  listagem, sem exceção. O teste inclui um arquivo com linhas rejeitadas, inclusive por
  `FIELD_TOO_LONG`.

**Autenticação**

- **FR-019**: A autenticação MUST ser ligada só pela variável de ambiente do token. Ausente ou vazia, a
  autenticação fica desligada, e todos os endpoints se comportam como no Marco 2 (Princípio VII).
  "Como no Marco 2" quer dizer o mesmo status, o mesmo corpo, o mesmo `ETag` e 304 e os mesmos
  cabeçalhos que o Marco 2 mandava. Os cabeçalhos de proteção da FR-022a são o único acréscimo.
- **FR-020**: Com a autenticação ligada, o endpoint de métricas MUST exigir o token num cabeçalho da
  requisição, comparado exatamente com o valor da variável.
- **FR-021**: Com a autenticação ligada, os endpoints de negócio (`/api/producers/intervals` e
  `/api/movies`) MUST exigir o mesmo token. Com o token certo, a resposta é a do Marco 2, inclusive o
  ETag e o 304.
- **FR-022**: A liveness, a readiness, a saúde geral (`/actuator/health`), o Swagger UI e o contrato
  OpenAPI MUST continuar públicos, com ou sem a autenticação. As probes e a saúde geral continuam
  respondendo só o estado (spec 002, FR-011).
- **FR-022a**: Com a autenticação ligada ou desligada:
  - toda resposta MUST trazer `X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY`;
  - as respostas dos endpoints de negócio MUST NOT trazer `Cache-Control`, `Pragma` nem `Expires`, para
    que o `ETag` e o 304 continuem úteis ao cliente;
  - as demais respostas (métricas, saúde, documentação e 401) podem trazer os cabeçalhos de cache do
    framework.

  Decisão do responsável de 2026-11-24, em `docs/ai/decisoes.md`.
- **FR-023**: Token ausente ou errado num endpoint protegido MUST resultar em 401 em Problem Details
  (RFC 9457), com o mesmo corpo nos dois casos e sem revelar o valor esperado nem o recebido.
- **FR-024**: *(retirado em 2026-11-24, depois da segunda análise.)* A forma de comparar o token é uma
  decisão de design, e não um comportamento verificável por teste. Ela fica na research R11 e no ADR
  0017, conferida na "Revisão de segurança" do checklist. O número fica reservado para não renumerar os
  requisitos.
- **FR-025**: O valor do token MUST NOT aparecer no log, nas métricas, nas respostas nem no contrato. O
  único token no repositório é o de demonstração documentado no README.
- **FR-026**: O contrato OpenAPI MUST documentar o esquema de autenticação e a resposta 401 nos endpoints
  protegidos, e as respostas reais MUST continuar compatíveis com ele (spec 001, FR-025).
- **FR-027**: Os testes da autenticação MUST rodar no `./mvnw verify`, sem Docker, com a autenticação
  desligada e ligada.

**Compose e observabilidade**

- **FR-028**: O repositório MUST ter um `compose.yaml` com a aplicação no perfil Docker, o PostgreSQL, o
  Kafka numa imagem leve (sem ZooKeeper), o Prometheus e o Grafana. `docker compose up` num clone limpo,
  sem variável definida, MUST deixar a aplicação pronta com o arquivo fornecido.
- **FR-029**: A aplicação no compose MUST subir só depois de o PostgreSQL e o Kafka estarem saudáveis, e
  cada serviço MUST ter healthcheck.
- **FR-030**: Os dados do PostgreSQL e do Kafka MUST ficar em volumes, para sobreviver a `docker compose
  down` sem `-v`.
- **FR-031**: O Prometheus MUST coletar as métricas da aplicação, inclusive com a autenticação ligada,
  com o token passado pela configuração do compose, sem gravá-lo em arquivo versionado além do token de
  demonstração.
- **FR-032**: O Grafana MUST subir com a fonte de dados do Prometheus e o dashboard versionado no
  repositório já provisionados. O dashboard MUST mostrar, no mínimo, a taxa de requisições, a taxa de
  erro e os percentis 95 e 99 de latência dos endpoints de negócio, o atraso do consumidor e os eventos
  enviados ao tópico de erros.
- **FR-033**: As credenciais dos serviços do compose (PostgreSQL e Grafana) MUST ser valores de
  demonstração, conforme o Princípio VII (constituição 1.6.0):
  - aparecem só nos arquivos do compose de desenvolvimento local e na configuração que ele monta;
  - são identificadas como de demonstração no README e no próprio arquivo onde aparecem;
  - podem ser sobrescritas por variável de ambiente;
  - nunca são usadas em ambiente publicado. O estágio do pipeline da FR-035 não é ambiente publicado.
- **FR-034**: Toda imagem do compose MUST ter versão explícita (nunca `latest`), e o Dependabot MUST
  cobrir as imagens do compose.
- **FR-035**: O pipeline MUST ter um estágio que sobe o compose, espera a readiness da aplicação,
  confere o resultado de referência, confere o alvo da aplicação ativo no Prometheus e o dashboard
  provisionado no Grafana, e derruba o ambiente.

**README e decisões**

- **FR-036**: O `README.md` MUST documentar o modo Docker (como subir, portas, o que cada serviço faz),
  o formato do evento de filme, como publicar um vencedor novo no compose, a autenticação (variável,
  cabeçalho, token de demonstração e endpoints protegidos), o perfil Maven de PostgreSQL e a tabela de
  requisitos e testes atualizada. A seção "Avalie em 2 minutos" MUST continuar funcionando só com o JDK.
- **FR-037**: As decisões de arquitetura do marco (PostgreSQL com o esquema comum, ingestão única por
  Kafka com evento de carga concluída, idempotência, tópico de erros, autenticação por token e o
  compose com observabilidade) MUST ter ADR curto em `docs/adr/`, no formato da spec 002 (FR-031). O
  ADR da autenticação MUST registrar como decisão intencional que os cabeçalhos
  `X-Content-Type-Options` e `X-Frame-Options` aparecem em todas as respostas, com o token ligado ou
  desligado, e que o `Cache-Control` do Spring Security fica fora dos endpoints de negócio para
  preservar o ETag.

**Comprovação**

- **FR-038**: Cada requisito MUST ser comprovado na forma do Princípio VI (constituição 1.7.0):
  - comportamento da aplicação comum aos dois bancos (FR-001, FR-002, FR-009 a FR-023, FR-025 a
    FR-027, menos a FR-015a): teste de integração no `./mvnw verify`, com H2 e o broker embutido;
  - nova tentativa quando a gravação falha (FR-015a): `RetryPostgresIT` no perfil Maven dedicado, mais o
    passo 7b do teste do compose, que comprova a queda real do banco;
  - comportamento que só existe no PostgreSQL (FR-004, FR-006, FR-008 e a persistência entre reinícios
    da US2): teste de integração no perfil Maven dedicado, executado obrigatoriamente no pipeline
    (FR-007). É a regra do Princípio VI para o comportamento que depende de infraestrutura indisponível
    no modo padrão;
  - compose, imagens e pipeline (FR-028 a FR-035): estágio do pipeline;
  - README e ADRs (FR-036 e FR-037): revisão no pull request, com seção própria no checklist da spec.

### Key Entities

- **Evento de filme**: ano, título, estúdios, produtores (texto no formato da coluna do CSV) e `winner`.
  A chave é o par ano + título, sem os espaços em volta do título.
- **Evento de carga concluída**: identificador da execução que publicou e quantidade de filmes
  publicados. Libera a readiness só na execução que o publicou.
- **Registro de erro**: conteúdo original do evento inválido, motivo e momento do recebimento, no
  tópico de erros.
- **Token de acesso**: valor esperado, vindo de variável de ambiente; ausente ou vazio desliga a
  autenticação. Protege os endpoints de negócio e o de métricas.
- **Dashboard**: definição versionada no repositório, com os painéis da FR-032.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Com o mesmo arquivo, 100% das respostas dos intervalos e da listagem são idênticas entre a
  carga direta e a ingestão por Kafka, e entre o H2 e o PostgreSQL.
- **SC-002**: Todos os testes dos Marcos 1 e 2 continuam passando sem alteração do resultado esperado, e
  `./mvnw verify` passa sem Docker, incluindo os testes de Kafka e de autenticação.
- **SC-003**: Em 100% das subidas com a ingestão por Kafka, a aplicação só se declara pronta depois de
  todos os filmes aceitos do arquivo estarem gravados.
- **SC-004**: Depois de 3 reinícios seguidos sobre o mesmo banco (4 subidas), o número de filmes é o da
  primeira carga mais os filmes novos recebidos por evento, sem nenhuma duplicata de filme, produtor ou
  vínculo.
- **SC-005**: Um vencedor novo publicado depois da carga aparece nos intervalos em até 5 segundos.
- **SC-006**: 100% dos eventos inválidos vão ao tópico de erros, e nenhum deles impede a gravação do
  evento válido seguinte.
- **SC-007**: Com a autenticação ligada, 100% das chamadas sem token ou com token errado aos endpoints
  protegidos (negócio e métricas) recebem 401, e 100% das chamadas às probes, ao Swagger UI e ao
  contrato recebem a resposta normal. Com ela desligada, o
  comportamento é o do Marco 2.
- **SC-008**: Num clone limpo, com as imagens já baixadas, `docker compose up` deixa a aplicação pronta
  e o dashboard mostrando dados em menos de 3 minutos.
- **SC-009**: Os testes do perfil de PostgreSQL e o estágio do compose ficam verdes no pipeline do pull
  request do marco.

## Assumptions

- O modo padrão continua sendo o avaliado (Princípio II). Nada deste marco é exigido para subir ou
  testar com `./mvnw`.
- O publicador roda dentro da própria aplicação, na subida, como a carga direta do modo padrão. Novos
  vencedores podem ser publicados por qualquer cliente do broker; no compose, o README mostra como
  fazer isso com a ferramenta de linha de comando do próprio Kafka.
- As linhas inválidas do arquivo são filtradas pelo publicador, com o mesmo aviso do Marco 1, e não vão
  ao tópico de erros. O tópico de erros recebe os eventos inválidos que chegam ao consumidor, de
  qualquer origem.
- O tópico de filmes preserva a ordem necessária para que o evento de carga concluída seja consumido
  depois de todos os filmes da mesma execução. O número de partições e a forma de garantir isso ficam
  no plano.
- O nome do cabeçalho do token (por exemplo, `Authorization: Bearer`) e o nome da variável de ambiente
  ficam no plano. O token de demonstração é documentado no README.
- No compose, a autenticação fica desligada por padrão, como no modo padrão. O README mostra como
  ligá-la com o token de demonstração.
- A imagem da aplicação é a mesma do Marco 2; o compose só escolhe o perfil e as variáveis.
- O testador adversário (Codex) cria ataques à ingestão por Kafka a partir desta spec, e cada falha
  encontrada vira teste permanente (Princípio I).
- Os papéis seguem o `AGENTS.md`: o código da API é do implementador; o `compose.yaml`, o Prometheus, o
  Grafana, o Dependabot e o pipeline são do papel de infraestrutura; o README e os ADRs são do papel de
  documentação.
- Fora do escopo: publicação em nuvem, front-end, alertas de SLO, teste de carga, imagem assinada,
  esquema de registro de eventos (schema registry), TLS e autenticação entre os serviços do compose, e
  mais de um token ou papéis de usuário.
