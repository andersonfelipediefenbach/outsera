# Feature Specification: Marco 2 — Operação: ETag, observabilidade, imagem, pipeline e ADRs

**Feature Branch**: `marco/2-operacao`

**Created**: 2026-11-23

**Status**: Draft

**Input**: User description: "Marco 2, operação, conforme docs/ai/planejamento/briefing.md. Escopo: 1. ETag no GET de intervalos e na listagem, com 304 quando If-None-Match bate; o contrato de resposta 200 não muda. 2. Health checks de liveness e readiness, com a aplicação pronta só depois da carga do CSV. 3. Métricas de latência e taxa de erro dos endpoints, expostas no formato do Prometheus. 4. Logs estruturados em JSON. 5. Dockerfile multi-stage, com usuário não root e healthcheck, e .dockerignore. A imagem é um caminho alternativo: o projeto continua subindo e testando só com ./mvnw, sem Docker. 6. Pipeline no GitHub Actions: em todo push e pull request, build, testes, cobertura, análise de segurança, lint do Dockerfile, build da imagem e um teste de fumaça que sobe o container e chama o endpoint. Actions fixadas por hash de commit. Dependabot para Maven, Actions e Docker. 7. ADRs curtos para as decisões de arquitetura já tomadas no marco 1 e neste marco. Fora do escopo: PostgreSQL, Kafka, autenticação, publicação de imagem, deploy e front-end. Os testes da aplicação continuam somente de integração; o teste do container roda só no pipeline."

## Clarifications

### Session 2026-11-23

- Q: O formato JSON do log vale também no modo padrão (`./mvnw spring-boot:run`), que o avaliador lê
  no terminal? → A: Não. No `./mvnw`, o log continua em texto, legível no terminal. O JSON é ligado por
  uma variável de ambiente, que o Dockerfile define na imagem e que também permite ligar o JSON em
  qualquer outra execução. A constituição foi emendada para 1.3.0 (seção "Restrições técnicas e de
  segurança", Observabilidade), e o motivo está em `docs/ai/decisoes.md`.
- Q: O que reprova o pipeline na análise de segurança e na cobertura? → A: Segurança: achados de
  severidade alta ou crítica reprovam. Na análise de dependências e da imagem, contam só as
  vulnerabilidades com versão corrigida disponível. Médios e baixos aparecem no relatório sem reprovar.
  Cobertura: medida pelos testes de integração, com mínimo de 90% de linhas (medido 97,06% no fim do
  Marco 1, piso de 80%). FR-028a e `docs/ai/decisoes.md`.
- Q: A FR-006 deve cobrir a resposta 500? → A: Não. A FR-006 cobre só 400 e 405, e o caso de borda do 500
  saiu. A API não tem caminho que produza 500 sem um defeito ou um mock, então um requisito sobre o 500
  não teria comprovação (Princípio VI). O filtro de ETag só age em respostas 2xx, e as métricas registram
  qualquer status, inclusive 500, pelo próprio Spring (research R4). Registro em `docs/ai/decisoes.md`.
- Q: O evento JSON da falha de inicialização precisa trazer o rastro da exceção? → A: Não sempre. Ele
  traz a mensagem de erro; o rastro só quando existir, porque o analisador de falhas o substitui por uma
  mensagem clara. US4-3 e FR-017.
- Q: O banner do Spring Boot continua aparecendo? → A: Não. Fica desligado em todas as execuções
  (`spring.main.banner-mode: off`), porque não traz informação e, com o JSON ligado, seria uma saída fora
  do log, com desenho ASCII. FR-020 e `docs/ai/decisoes.md`.
- Q: Como comprovar os ADRs, que não têm teste possível? → A: Por revisão no pull request, com um item no
  checklist da spec (`checklists/requirements.md`, seção "Revisão dos ADRs"): cada ADR tem contexto,
  decisão, alternativas consideradas e consequências; cita a entrada correspondente em
  `docs/ai/decisoes.md`; e há um ADR para cada decisão de arquitetura listada na FR-030. Não há
  verificação automatizada no pipeline. FR-031 e FR-032.

## User Scenarios & Testing *(mandatory)*

Os atores são:

- o **consumidor da API**, que repete consultas e quer evitar baixar de novo um corpo que não mudou;
- o **operador**, que sobe a aplicação (pelo `./mvnw` ou pela imagem), verifica se ela está viva e
  pronta, acompanha latência e erros e lê os logs;
- o **avaliador da Outsera**, que continua rodando só o modo padrão e precisa encontrar o mesmo
  comportamento do Marco 1;
- o **mantenedor**, que recebe a validação automática de cada push e pull request e lê os ADRs para
  entender por que o projeto é como é.

### User Story 1 - Revalidar consultas com ETag (Priority: P1)

O consumidor da API consulta os intervalos ou a listagem, guarda o ETag recebido e, nas próximas
chamadas, envia esse valor em `If-None-Match`. Enquanto os dados não mudam, recebe 304 sem corpo; se a
representação for outra, recebe 200 com o corpo completo, exatamente como no Marco 1.

**Why this priority**: É o único item deste marco que toca os endpoints de negócio. Precisa provar que o
contrato de intervalos do enunciado continua idêntico (Princípio III) e que o modo padrão não muda para
quem não envia o cabeçalho.

**Independent Test**: Com a aplicação no ar, chamar cada endpoint sem `If-None-Match`, conferir o corpo
e o ETag, repetir com o ETag recebido e conferir o 304; repetir com um ETag diferente e conferir o 200.

**Acceptance Scenarios**:

1. **Given** a aplicação no ar com o arquivo fornecido, **When** os intervalos são consultados sem
   `If-None-Match`, **Then** a resposta tem status 200, o corpo é idêntico ao do Marco 1 (Joel Silver em
   `min` e Matthew Vaughn em `max`, sem nenhum campo a mais) e há um cabeçalho `ETag`.
2. **Given** o ETag recebido na consulta anterior, **When** os intervalos são consultados de novo com
   `If-None-Match` igual a esse ETag, **Then** a resposta tem status 304, sem corpo, com o mesmo `ETag`.
3. **Given** um `If-None-Match` com um valor diferente do ETag atual, **When** os intervalos são
   consultados, **Then** a resposta tem status 200 com o corpo completo e o ETag atual.
4. **Given** uma página da listagem consultada com certos filtros, **When** a mesma consulta é repetida
   com o ETag recebido, **Then** a resposta tem status 304, sem corpo.
5. **Given** o ETag de uma página da listagem, **When** ele é enviado numa consulta com outros
   parâmetros (outra página, outro tamanho ou outro filtro) cujo resultado é diferente, **Then** a
   resposta tem status 200 com o corpo dessa outra consulta e um ETag diferente.
6. **Given** um `If-None-Match` com uma lista de valores em que um deles é o ETag atual, ou com `*`,
   **When** o endpoint é consultado, **Then** a resposta tem status 304.
7. **Given** a aplicação iniciada com dois arquivos de resultados diferentes, em execuções separadas,
   **When** os intervalos são consultados em cada uma, **Then** os ETags são diferentes. **Given** duas
   execuções com o mesmo arquivo, **Then** os ETags são iguais. Dois arquivos com o mesmo resultado têm o
   mesmo ETag (FR-002).
8. **Given** uma consulta da listagem com parâmetro inválido e um `If-None-Match` qualquer, **When** a
   listagem é consultada, **Then** a resposta continua 400 em Problem Details, sem `ETag`.

---

### User Story 2 - Saber se a aplicação está viva e pronta (Priority: P1)

O operador, ou o orquestrador que roda a imagem, consulta a liveness para saber se o processo precisa
ser reiniciado e a readiness para saber se a aplicação já pode receber tráfego. A aplicação só se
declara pronta depois de terminar a carga do CSV.

**Why this priority**: A constituição exige os probes a partir deste marco, e o healthcheck da imagem e o
teste de fumaça do pipeline dependem deles.

**Independent Test**: Subir a aplicação, consultar a liveness e a readiness e conferir o estado; conferir
que a readiness só ficou positiva depois do resumo da carga.

**Acceptance Scenarios**:

1. **Given** a aplicação iniciada com um arquivo válido, **When** a liveness é consultada, **Then** a
   resposta tem status 200 e indica que a aplicação está viva.
2. **Given** a aplicação iniciada com um arquivo válido, **When** a readiness é consultada, **Then** a
   resposta tem status 200 e indica que a aplicação está pronta, e os dados do arquivo já estão
   disponíveis na listagem e nos intervalos.
3. **Given** a aplicação iniciando, **When** a carga ainda não terminou, **Then** a aplicação não se
   declara pronta: a passagem para "pronta" acontece depois do resumo da carga (spec 001, FR-006b).
4. **Given** um arquivo com linhas inválidas, mas estruturalmente válido, **When** a carga termina,
   **Then** a aplicação fica pronta normalmente (spec 001, FR-006).
5. **Given** um arquivo com erro estrutural, **When** a aplicação tenta iniciar, **Then** ela não sobe,
   como no Marco 1, e nunca chega a se declarar pronta.
6. **Given** a aplicação no ar, **When** a liveness, a readiness ou a saúde geral são consultadas sem
   autenticação, **Then** a resposta traz só o estado, sem detalhes internos (componentes, banco,
   caminhos ou versões).

---

### User Story 3 - Acompanhar latência e taxa de erro (Priority: P2)

O operador coleta, num formato que o Prometheus lê, a contagem e a distribuição de latência das
requisições de cada endpoint, separadas por método e status, e calcula a taxa de erro e os percentis de
latência.

**Why this priority**: É a medição de que dependem o SLO, os alertas e o teste de carga do Marco 4. Não
afeta o enunciado.

**Independent Test**: Com a aplicação no ar, chamar os endpoints de negócio com sucesso e com erro e
conferir, no endpoint de métricas, as séries de contagem e de latência com os rótulos esperados.

**Acceptance Scenarios**:

1. **Given** a aplicação no ar, **When** o endpoint de métricas é consultado, **Then** a resposta tem
   status 200 no formato de texto do Prometheus.
2. **Given** N chamadas bem-sucedidas aos intervalos, **When** as métricas são consultadas, **Then** a
   contagem de requisições desse endpoint, método GET e status 200, aumentou em N.
3. **Given** uma chamada à listagem com parâmetro inválido (400) e uma com verbo não permitido (405),
   **When** as métricas são consultadas, **Then** cada uma aparece na série do endpoint com o status
   correspondente, e a taxa de erro pode ser calculada pela razão entre as séries de erro e o total.
4. **Given** chamadas à listagem com parâmetros diferentes (`year=1990`, `page=3`), **When** as métricas
   são consultadas, **Then** todas aparecem na mesma série do endpoint, identificado pelo caminho, sem
   uma série por combinação de parâmetros.
5. **Given** chamadas a caminhos diferentes que não existem, **When** as métricas são consultadas,
   **Then** todas aparecem com o mesmo valor de rota e o status 404, sem uma série por caminho.
6. **Given** chamadas respondidas com 304, **When** as métricas são consultadas, **Then** elas contam como
   sucesso, no status 304.
7. **Given** a distribuição de latência exposta, **When** o operador calcula o percentil 95 e o 99 de um
   endpoint, **Then** os dados expostos permitem esse cálculo no Prometheus (faixas de latência
   cumulativas).

---

### User Story 4 - Ler logs estruturados (Priority: P2)

Na imagem de container, ou em qualquer execução em que a variável de formato de log liga o JSON, o
operador lê os logs da aplicação como uma linha JSON por evento, que um coletor de logs indexa sem
precisar de regras de análise de texto. Na execução local padrão (`./mvnw spring-boot:run`), o log
continua em texto, legível para quem avalia no terminal.

**Why this priority**: A constituição (1.3.0) exige os logs em JSON na execução em container e em
ambientes operacionais, e texto na execução local padrão. Os avisos de linha rejeitada do Marco 1
precisam continuar localizáveis nos dois formatos.

**Independent Test**: Subir a aplicação com um arquivo que tem linhas inválidas, uma vez sem a variável
e outra com o JSON ligado, e conferir o formato de cada linha e a presença dos avisos de linha rejeitada
e do resumo nos dois casos.

**Acceptance Scenarios**:

1. **Given** a aplicação iniciada com o JSON ligado pela variável de formato de log, **When** os logs da
   inicialização são lidos, **Then** cada evento é um objeto JSON válido numa única linha, com data e
   hora, nível, origem (logger), thread e mensagem.
2. **Given** um arquivo com linhas inválidas e o JSON ligado, **When** a carga termina, **Then** cada
   aviso de linha rejeitada continua trazendo o número da linha e o motivo, e o resumo continua trazendo
   lidas, aceitas e rejeitadas (spec 001, FR-006a e FR-006b).
3. **Given** uma falha de inicialização, como a de arquivo ausente, e o JSON ligado, **When** o log é
   lido, **Then** o evento JSON da falha traz a mensagem de erro inteira, mesmo com várias linhas, num
   único evento, e nenhuma linha fica fora de um evento JSON. O rastro da exceção só aparece quando
   existe, no mesmo evento: na falha de arquivo, o analisador de falhas o substitui pela mensagem clara
   do Marco 1.
4. **Given** os testes do Marco 1 que verificam mensagens de log, **When** eles rodam com o JSON ligado,
   **Then** as mesmas verificações passam.
5. **Given** a aplicação iniciada sem a variável de formato de log, como no `./mvnw spring-boot:run`,
   **When** os logs são lidos, **Then** eles estão no texto padrão do framework, como no Marco 1, e os
   testes do Marco 1 que verificam o log passam sem alteração.
6. **Given** o container iniciado a partir da imagem, sem nenhuma variável extra, **When** os logs são
   lidos, **Then** eles estão em JSON, porque a imagem liga o formato.

---

### User Story 5 - Rodar a aplicação pela imagem de container (Priority: P3)

O operador constrói a imagem a partir do repositório e roda a aplicação num container, com a mesma
configuração por variáveis de ambiente do modo padrão. A imagem é um caminho alternativo: quem não tem
Docker continua usando só o `./mvnw`.

**Why this priority**: Prepara a entrega (Marcos 3 a 5), mas o enunciado não pede container.

**Independent Test**: No pipeline, construir a imagem, subir o container, esperar o healthcheck ficar
saudável, chamar os intervalos e conferir o resultado de referência.

**Acceptance Scenarios**:

1. **Given** o repositório clonado, **When** a imagem é construída, **Then** a construção usa estágios
   separados de build e de execução, e a imagem final contém só o necessário para executar a aplicação
   (sem código-fonte, Maven nem ferramentas de compilação).
2. **Given** o container em execução, **When** o usuário do processo é verificado, **Then** ele não é
   root.
3. **Given** o container iniciado com o arquivo embarcado, **When** a carga termina, **Then** o
   healthcheck da imagem passa a saudável, e os intervalos trazem Joel Silver em `min` e Matthew Vaughn em
   `max`, e o log do container está em JSON (FR-024a).
4. **Given** a variável `PORT` definida, **When** o container inicia, **Then** a aplicação escuta nessa
   porta, e o healthcheck usa a mesma porta.
5. **Given** um outro arquivo CSV montado no container e o caminho dele na variável de ambiente do
   arquivo (spec 001, FR-002), **When** o container inicia, **Then** os dados vêm desse arquivo.
6. **Given** um caminho de arquivo inexistente, **When** o container inicia, **Then** o processo termina
   com código de saída diferente de zero e a mensagem clara do Marco 1 no log.
7. **Given** o container em execução, **When** ele recebe o sinal de parada, **Then** a aplicação encerra
   de forma ordenada, sem esperar o tempo-limite de parada forçada.
8. **Given** a máquina sem Docker, **When** `./mvnw verify` e `./mvnw spring-boot:run` são executados,
   **Then** os dois funcionam como antes, e nenhum teste do `./mvnw verify` exige Docker.

---

### User Story 6 - Validar cada mudança no pipeline (Priority: P2)

A cada push e pull request, o mantenedor recebe um resultado automático com build, testes, cobertura,
análise de segurança, lint do Dockerfile, build da imagem e o teste de fumaça do container. O pipeline
verde é o portão de merge do bolt do marco.

**Why this priority**: O fluxo de trabalho do `AGENTS.md` exige merge só com pipeline verde, e hoje não
há pipeline.

**Independent Test**: Abrir um pull request com uma mudança inofensiva e conferir todos os estágios
verdes; abrir um com um teste quebrado, um Dockerfile com problema ou uma dependência vulnerável e
conferir que o pipeline falha no estágio certo.

**Acceptance Scenarios**:

1. **Given** um push em qualquer branch ou um pull request, **When** o pipeline roda, **Then** ele
   executa build e testes com `./mvnw verify`, gera o relatório de cobertura, roda a análise de
   segurança e o lint do Dockerfile, constrói a imagem e roda o teste de fumaça.
2. **Given** um teste de integração falhando, **When** o pipeline roda, **Then** o estágio de testes
   falha e o pipeline fica vermelho.
3. **Given** o teste de fumaça, **When** ele roda, **Then** sobe o container da imagem construída no
   próprio pipeline, espera a readiness, chama os intervalos, confere o resultado de referência e
   derruba o container. Se qualquer passo falhar, o pipeline falha.
4. **Given** o arquivo do pipeline, **When** ele é inspecionado, **Then** toda action de terceiros é
   referenciada por hash de commit completo, nunca por tag ou branch.
5. **Given** o pipeline, **When** ele roda, **Then** o token do GitHub tem só permissão de leitura do
   repositório, exceto onde um estágio precisa de mais (por exemplo, publicar o resultado da análise de
   segurança), e nenhum segredo é necessário.
6. **Given** a configuração do Dependabot, **When** há atualização disponível de uma dependência Maven,
   de uma action ou da imagem base do Dockerfile, **Then** o Dependabot abre um pull request, e no caso
   das actions mantém a referência por hash.
7. **Given** o relatório de cobertura, **When** o pipeline termina, **Then** o relatório fica disponível
   para consulta no resultado da execução.
8. **Given** um achado de segurança de severidade alta ou crítica no código, ou uma vulnerabilidade alta
   ou crítica com versão corrigida disponível numa dependência ou na imagem, **When** o pipeline roda,
   **Then** o estágio de segurança falha e o pipeline fica vermelho.
9. **Given** apenas achados médios ou baixos, ou vulnerabilidades sem versão corrigida, **When** o
   pipeline roda, **Then** eles aparecem no relatório, e o pipeline não falha por causa deles.
10. **Given** uma mudança que deixa a cobertura de linhas dos testes de integração abaixo do mínimo
    (FR-028a), **When** o pipeline roda, **Then** o estágio de testes falha.

---

### User Story 7 - Entender as decisões de arquitetura pelos ADRs (Priority: P3)

O mantenedor, ou o avaliador, abre `docs/adr/` e encontra um registro curto por decisão de arquitetura,
com o contexto, a decisão, as consequências e as alternativas rejeitadas.

**Why this priority**: O briefing pede ADRs curtos, e o `docs/ai/decisoes.md` remete a eles, mas a pasta
ainda não existe. Não muda o comportamento da aplicação.

**Independent Test**: Na revisão do pull request, percorrer o item "Revisão dos ADRs" do checklist da
spec e conferir cada ADR contra ele.

**Acceptance Scenarios**:

1. **Given** a pasta `docs/adr/`, **When** o índice é aberto, **Then** ele lista todos os ADRs, com
   número, título e status.
2. **Given** cada ADR, **When** ele é lido, **Then** tem contexto, decisão, alternativas consideradas e
   consequências, e cita a entrada correspondente em `docs/ai/decisoes.md`.
3. **Given** a lista mínima de decisões do Marco 1 e do Marco 2 (FR-030), **When** os ADRs são
   conferidos, **Then** cada decisão tem o seu ADR.

---

### Edge Cases

- `If-None-Match` com ETag fraco (`W/"..."`) igual ao atual: 304, pela comparação fraca que o protocolo
  HTTP define para esse cabeçalho.
- `If-None-Match` malformado (sem aspas, vazio): tratado como "não bate", e a resposta é 200 com corpo.
- `If-None-Match` em 405 (verbo não permitido): continua 405, sem `ETag`.
- Listagem com página além da última (spec 001, caso de borda): 200 com `content` vazio e ETag próprio;
  a repetição com esse ETag dá 304.
- Duas consultas à listagem com parâmetros escritos de forma diferente, mas com o mesmo resultado
  (`winner=true` e `winner=TRUE`): podem ter o mesmo ETag, porque o ETag depende da representação.
- Arquivo só com cabeçalho: a aplicação fica pronta, e os intervalos vazios têm ETag e respondem 304 na
  revalidação.
- Chamadas ao endpoint de métricas e aos de saúde: não podem inflar a taxa de erro dos endpoints de
  negócio, porque cada endpoint tem sua própria série.
- Mensagem de log com aspas, quebras de linha ou acentos (por exemplo, um título do CSV citado num
  aviso): o JSON continua válido e o texto é preservado em UTF-8.
- Container iniciado sem `PORT`: usa a porta padrão do modo padrão.
- Container com o arquivo montado sem permissão de leitura para o usuário não root: falha de
  inicialização com a mensagem de arquivo ilegível (spec 001, FR-005). Comprovado no pipeline, no runner
  Linux. Localmente, a verificação é pulada, com o motivo, onde a permissão do host não vale dentro do
  container (por exemplo, NTFS no Windows ou Docker Desktop).
- Build da imagem com a pasta `target/` local presente: o `.dockerignore` impede que artefatos locais,
  o histórico do git ou a evidência de IA entrem no contexto da imagem.
- Pull request vindo de um fork: o pipeline roda sem acesso a segredos e sem permissão de escrita.

## Requirements *(mandatory)*

### Functional Requirements

**ETag e requisições condicionais**

- **FR-001**: As respostas 200 de `GET /api/producers/intervals` e de `GET /api/movies` MUST trazer o
  cabeçalho `ETag`.
- **FR-002**: O ETag MUST ser derivado do conteúdo da representação: duas respostas com o mesmo corpo
  têm o mesmo ETag, e corpos diferentes têm ETags diferentes, inclusive entre execuções. Na listagem,
  cada combinação de página, tamanho e filtros tem o ETag da sua própria resposta.
- **FR-003**: Quando `If-None-Match` contém o ETag atual (sozinho, numa lista ou na forma fraca) ou `*`,
  a resposta MUST ter status 304, sem corpo, com o mesmo `ETag`.
- **FR-004**: Quando `If-None-Match` está ausente, malformado ou não contém o ETag atual, a resposta MUST
  ser 200 com o corpo completo.
- **FR-005**: O corpo e o status das respostas 200 MUST continuar idênticos aos do Marco 1. O contrato de
  intervalos continua exatamente o do enunciado (Princípio III), e nenhum campo é adicionado ao corpo.
- **FR-006**: Respostas de erro (400 e 405) MUST NOT trazer `ETag` e MUST NOT virar 304 por causa de
  `If-None-Match`.
- **FR-007**: O contrato OpenAPI MUST documentar o cabeçalho `ETag`, o parâmetro `If-None-Match` e a
  resposta 304 nos dois endpoints, e as respostas reais, inclusive a 304, MUST continuar compatíveis com
  ele (spec 001, FR-025).

**Saúde: liveness e readiness**

- **FR-008**: O sistema MUST expor um endpoint de liveness e um de readiness, na mesma porta da API, que
  respondem 200 quando o estado é positivo e 503 quando não é.
- **FR-009**: A readiness MUST ficar positiva só depois que a carga do CSV terminar, com o resumo da
  carga já registrado (spec 001, FR-006b).
- **FR-010**: Linhas inválidas ignoradas MUST NOT impedir a readiness. Erros estruturais continuam
  impedindo a inicialização (spec 001, FR-005), e a aplicação nunca se declara pronta.
- **FR-011**: As respostas de saúde MUST trazer só o estado, sem detalhes de componentes, banco,
  caminhos, versões ou configuração.

**Métricas**

- **FR-012**: O sistema MUST expor um endpoint de métricas no formato de texto do Prometheus, na mesma
  porta da API.
- **FR-013**: Para cada requisição HTTP, o sistema MUST registrar a contagem e a duração, identificadas
  pelo modelo de rota (e não pelo caminho com os parâmetros), pelo método, pelo status e pelo tipo de
  resultado (sucesso, erro do cliente ou erro do servidor).
- **FR-014**: A duração MUST ser exposta como distribuição em faixas cumulativas, que permitam calcular
  percentis (como p95 e p99) e a fração de requisições abaixo de um limite de latência.
- **FR-015**: Caminhos inexistentes MUST cair num único valor fixo de rota, que não depende do caminho
  chamado. O número de séries não cresce com os caminhos chamados. Esse valor pode ser compartilhado com
  o tratador de recursos estáticos, e o status `404` distingue as chamadas a caminhos inexistentes.
- **FR-016**: As métricas MUST incluir, além das requisições HTTP, as métricas padrão da JVM e do
  processo (memória, threads e tempo de atividade) e um rótulo com o nome da aplicação.

**Logs**

- **FR-017**: Com o JSON ligado, cada evento de log MUST ser um objeto JSON numa única linha, com data e
  hora com fuso, nível, logger, thread e mensagem. Uma mensagem com várias linhas MUST ficar num único
  evento. Quando o evento tiver uma exceção, o rastro MUST ir no mesmo evento. Na falha de inicialização
  tratada pelo analisador de falhas, o evento traz a mensagem de erro, sem rastro (US4-3).
- **FR-018**: Nos dois formatos, os avisos de linha rejeitada e o resumo da carga MUST manter o conteúdo
  do Marco 1 (número da linha, motivo e contagens). Os testes do Marco 1 que verificam o log MUST passar
  no formato texto e as mesmas verificações MUST passar com o JSON ligado.
- **FR-019**: O log MUST NOT conter cabeçalhos de autenticação, tokens ou credenciais.
- **FR-020**: O formato do log MUST ser escolhido por uma variável de ambiente. Sem ela, o log é o texto
  padrão do framework, como no Marco 1, e esse é o formato da execução local padrão (`./mvnw
  spring-boot:run`). Com ela, o log é JSON (FR-017), em qualquer execução. A imagem de container MUST
  definir essa variável, ligando o JSON (FR-024a). O banner do framework MUST ficar desligado em todas
  as execuções, nos dois formatos: ele não traz informação e, com o JSON ligado, seria uma saída fora do
  log, com desenho ASCII, que quebraria o JSON.

**Imagem de container**

- **FR-021**: O repositório MUST ter um Dockerfile multi-stage: um estágio constrói o JAR, e a imagem
  final contém só o ambiente de execução Java e a aplicação.
- **FR-022**: O processo no container MUST rodar com um usuário não root.
- **FR-023**: A imagem MUST declarar um healthcheck baseado na readiness (FR-009), na porta configurada.
- **FR-024**: O container MUST aceitar a mesma configuração por variáveis de ambiente do modo padrão,
  inclusive `PORT` e o caminho do CSV, com os mesmos padrões.
- **FR-024a**: A imagem MUST definir a variável de formato de log com o JSON ligado (FR-020), e quem roda
  o container pode sobrescrevê-la.
- **FR-025**: O container MUST encerrar de forma ordenada ao receber o sinal de parada, e terminar com
  código diferente de zero quando a inicialização falha.
- **FR-026**: O repositório MUST ter um `.dockerignore` que exclua do contexto da imagem os artefatos de
  build locais, o histórico do git, a documentação e a evidência de IA.
- **FR-027**: Nada da imagem MUST ser exigido pelo modo padrão: `./mvnw spring-boot:run` e `./mvnw
  verify` continuam funcionando sem Docker (Princípio II), e nenhum teste do `./mvnw verify` usa
  container.

**Pipeline**

- **FR-028**: O repositório MUST ter um pipeline no GitHub Actions que roda em todo push e em todo pull
  request, com os estágios:
  - build e testes com `./mvnw verify`, no JDK 25;
  - relatório de cobertura dos testes de integração, publicado no resultado da execução;
  - análise de segurança do código, das dependências e da imagem;
  - lint do Dockerfile;
  - build da imagem;
  - teste de fumaça: sobe o container da imagem construída, espera a readiness, chama os intervalos,
    confere o resultado de referência e derruba o container.
- **FR-028a**: Critérios de reprovação do pipeline:
  - **segurança:** o pipeline MUST falhar com achados de severidade alta ou crítica. Na análise de
    dependências e na da imagem, contam só as vulnerabilidades com versão corrigida disponível. Achados
    médios e baixos MUST aparecer no relatório, sem reprovar;
  - **cobertura:** a cobertura MUST ser medida sobre a execução dos testes de integração do `./mvnw
    verify`. O build MUST falhar se a cobertura de linhas ficar abaixo de **90%**. O valor medido no fim
    do Marco 1 foi 97,06%, e o mínimo tem piso de 80%. A margem cobre um projeto de 340 linhas e o código
    do Marco 3 que só roda no perfil Docker, fora do `verify` padrão (registro em `docs/ai/decisoes.md`).
    A verificação do mínimo roda também no `./mvnw verify` local, sem Docker.
- **FR-028b**: Toda action MUST ser referenciada por hash de commit completo. O token do pipeline MUST ter
  permissão só de leitura por padrão, ampliada apenas no estágio que precisar, e o pipeline MUST NOT
  exigir segredos. A imagem MUST NOT ser publicada.
- **FR-029**: O repositório MUST ter a configuração do Dependabot para três ecossistemas: Maven,
  GitHub Actions e a imagem base do Dockerfile.

**ADRs**

- **FR-030**: `docs/adr/` MUST ter um índice e um ADR curto para cada decisão desta lista mínima:
  - do Marco 1:
    - stack (Java 25, Spring Boot e Maven com wrapper);
    - persistência em H2 com JPA e esquema por Flyway, com SQL portável;
    - carga do CSV na inicialização, com falha rápida nos erros estruturais e linha inválida ignorada
      com aviso;
    - somente testes de integração no back-end, sem Mockito;
    - contrato OpenAPI estático como fonte única, com o Swagger UI pelo webjar;
    - erros em Problem Details;
  - do Marco 2:
    - ETag derivado do conteúdo;
    - probes de liveness e readiness ligados à carga;
    - métricas no formato do Prometheus;
    - formato do log: texto na execução local padrão e JSON por variável de ambiente, ligado na imagem;
    - imagem multi-stage com usuário não root;
    - pipeline com actions fixadas por hash e Dependabot.
- **FR-031**: Cada ADR MUST ter número, título, data e status, e as seções contexto, decisão,
  alternativas consideradas e consequências. Cada ADR MUST citar a entrada correspondente em
  `docs/ai/decisoes.md`. Se a decisão ainda não tiver entrada (por exemplo, a stack ou o Problem Details,
  decididos no plano 001), a entrada é criada junto com o ADR.

**README**

- **FR-034**: O `README.md` MUST documentar o que o Marco 2 acrescenta:
  - o ETag e o 304 nos dois endpoints;
  - os endpoints de saúde e de métricas;
  - a variável de formato do log (texto no `./mvnw`, JSON na imagem);
  - como construir e rodar a imagem;
  - o pipeline e os seus critérios de reprovação;
  - a tabela de requisitos e testes, atualizada (Princípio VI);
  - os ADRs, com link para o índice.

  A seção "Avalie em 2 minutos" MUST continuar funcionando só com o JDK.

**Restrições**

- **FR-032**: Cada requisito desta spec MUST ser comprovado na forma que o Princípio VI (constituição
  1.4.0) define para o seu tipo. Cada requisito de comportamento da aplicação (FR-001 a FR-020) MUST ser
  comprovado por teste de integração no `./mvnw verify`, conforme o Princípio I. Os requisitos da imagem
  e do pipeline (FR-021 a FR-029) são comprovados pelos estágios do próprio pipeline: política do
  workflow e do Dependabot, lint, verificação do contexto de build, build da imagem e teste de fumaça. Os ADRs (FR-030 e FR-031) e o README (FR-034) são comprovados por revisão
  no pull request, com as seções "Revisão dos ADRs" e "Revisão do README" do checklist da spec
  (`checklists/requirements.md`), sem verificação automatizada no pipeline.
- **FR-033**: Nenhum extra deste marco pode exigir instalação além do JDK no modo padrão, nem mudar o
  comportamento dos endpoints de negócio para quem não envia `If-None-Match`.

### Key Entities

- **Representação**: o corpo de uma resposta 200 de um endpoint de negócio, para uma combinação de
  parâmetros. Tem um ETag derivado do seu conteúdo.
- **Estado de saúde**: vivo ou não, pronto ou não. A prontidão depende do fim da carga do CSV.
- **Série de métrica de requisição**: rota, método, status e tipo de resultado, com a contagem e a
  distribuição de duração.
- **Evento de log**: data e hora, nível, logger, thread, mensagem e, se houver, a exceção.
- **ADR**: número, título, data, status, contexto, decisão, consequências, alternativas e origem.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% das revalidações com o ETag atual, nos dois endpoints, respondem 304 sem corpo, e
  100% das respostas 200 têm corpo idêntico ao do Marco 1.
- **SC-002**: Todos os testes do Marco 1 continuam passando sem alteração de resultado esperado, com
  `./mvnw verify` sem Docker.
- **SC-003**: Em 100% das inicializações com arquivo válido, a aplicação só se declara pronta depois do
  resumo da carga.
- **SC-004**: A partir só das métricas expostas, o operador calcula, para cada endpoint de negócio, a
  taxa de erro e o percentil 95 de latência, sem consultar o log.
- **SC-005**: Com o JSON ligado (inclusive no container do teste de fumaça), 100% das linhas de log da
  inicialização e das requisições são JSON válido. Sem a variável, o log do `./mvnw spring-boot:run` é o
  mesmo texto do Marco 1, sem o banner (FR-020).
- **SC-006**: O teste de fumaça do pipeline sobe o container e obtém o resultado de referência em menos
  de 2 minutos a partir do fim do build da imagem.
- **SC-007**: 100% das actions do pipeline estão fixadas por hash de commit completo.
- **SC-008**: Um defeito introduzido de propósito em cada categoria deixa o pipeline vermelho no estágio
  correspondente. As categorias são: teste quebrado, cobertura abaixo de 90%, achado alto na análise de
  código, dependência com vulnerabilidade alta corrigível, Dockerfile com problema de lint, imagem que não
  sobe e workflow fora da política. A verificação acontece num branch temporário, que nunca é mergeado e
  é apagado depois. Nenhum defeito fica no código do marco, e o resultado é registrado em
  `docs/ai/decisoes.md`.
- **SC-009**: Na revisão do pull request, 100% dos itens da "Revisão dos ADRs" estão marcados: cada
  decisão da lista mínima (FR-030) tem um ADR, e cada ADR tem as seções e a citação da FR-031.

## Assumptions

- O modo padrão continua sendo o avaliado (Princípio II). Os endpoints de saúde e de métricas respondem
  também no `./mvnw spring-boot:run`, sem nada instalado além do JDK.
- Os endpoints de saúde e de métricas ficam públicos neste marco. A autenticação é do Marco 3, e o
  briefing prevê exigir token nos endpoints operacionais só no ambiente publicado.
- Os caminhos exatos de liveness, readiness e métricas são definidos no plano, seguindo o padrão do
  framework.
- O ETag é forte e calculado sobre o corpo serializado. Como os dados não mudam durante uma execução, o
  ETag só muda quando o arquivo carregado é outro. Não há mudança de `Cache-Control`.
- O teste de fumaça confere o resultado de referência do arquivo embarcado (Joel Silver e Matthew
  Vaughn). Ele roda só no pipeline e não faz parte do `./mvnw verify`.
- O pipeline roda só o perfil padrão. O perfil de PostgreSQL entra no Marco 3.
- Os estágios de entrega e publicação do briefing (imagem assinada no GHCR, SBOM, atestado, release e
  Cloud Run) ficam para os Marcos 4 e 5.
- A proteção da `main` (merge só com o pipeline verde) é configurada pelo Anderson no GitHub.
- Os ADRs e o README são do papel de documentação, e o Dockerfile, o pipeline e o Dependabot são do papel
  de infraestrutura (`AGENTS.md`). O README é atualizado com o que este marco acrescenta (FR-034), sem
  reescrever as seções do Marco 1.
- Fora do escopo: PostgreSQL, Kafka, autenticação, publicação de imagem, deploy, front-end, compose,
  Prometheus e Grafana rodando, alertas, SLO, teste de carga e log de acesso por requisição.
