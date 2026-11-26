# Feature Specification: Marco 4 — Acabamento operacional: alertas de SLO, carga, dados gerados, cache AOT, assinatura e dev container

**Feature Branch**: `marco/4-acabamento`

**Created**: 2026-11-25

**Status**: Implemented, com escopo reduzido (2026-11-26). Entregues: US7 (pendências do Marco 3), US3 (gerador),
US2 (teste de carga) e US1 (SLO e alertas), com a página do SLO, o runbook e a seção do Marco 4 no README. Adiados,
por decisão do responsável: US4 (cache AOT, etapa 4), US5 (SBOM e assinatura, etapa 5), US6 (dev container, etapa 6)
e os ADRs 0020 a 0025 (FR-037). O T012 (verificação no pipeline com o `lz4-java` 1.11.2) não foi executado, por
decisão do responsável: a FR-034 fica comprovada pelas provas locais do T003 e do T007. Depende do responsável o T064
(proteção da `main` com os 10 checks).

**Input**: User description: "Marco 4: acabamento operacional, num único bolt. Nada muda no modo padrão do enunciado (./mvnw, H2, só testes de integração), e cada item tem a comprovação do seu tipo pelo Princípio VI. Escopo: 1. Alertas de SLO no Prometheus do compose (disponibilidade e latência do endpoint de intervalos), com regras versionadas e verificadas no pipeline. 2. Teste de carga com k6 contra o compose, com limites de latência e de erro que reprovam o job, rodando no pipeline. 3. Dados gerados: um gerador de CSV grande e determinístico para o k6 e para medir a carga inicial, sem substituir o movielist.csv padrão. 4. Cache de AOT (Spring Boot e JVM) na imagem, com o tempo de subida medido antes e depois. 5. SBOM da imagem e assinatura (cosign keyless no Actions), sem publicação da imagem neste marco. 6. Dev container para abrir o projeto pronto (JDK 25, Maven, Docker). Inclua também as pendências do marco 3: o registro da proteção da main (T093), a investigação de por que o job de dependências do Trivy não apontou as transitivas do kafka-clients (com correção) e a execução do testador adversário na ingestão por Kafka. O README ganha a seção do marco 4 entre os extras."

## Clarifications

### Session 2026-11-25

- Q: O cache AOT inclui o processamento AOT do Spring, que fixa na construção as condições de perfil e de
  propriedade? → A: Não. Só o cache AOT da JVM, gerado por uma execução de treino da aplicação no build da
  imagem. A escolha de perfil continua na execução, e a mesma imagem serve ao modo padrão e ao compose.
  US4-6, FR-020.
- Q: Sem publicar a imagem, o que é assinado e onde a assinatura fica? → A: A imagem vai a um registro
  temporário dentro do job do pipeline, é assinada lá e recebe o SBOM como atestado assinado. A verificação
  confere a identidade como imagem. O registro é descartado no fim do job; só a entrada no registro de
  transparência é pública. US5-6, FR-025.
- Q: Com os ITs dos Marcos 2 a 4, o `./mvnw verify` padrão chegou a cerca de 8 min. Ele continua rodando
  todos os ITs? → A: Não (emenda 2.0.0 da constituição, decidida pelo responsável). O `./mvnw verify`
  padrão roda só os ITs do modo padrão (H2, carga do CSV com todas as regras, inclusive as da spec 003,
  endpoints, Swagger e o que vem ligado sem configuração), numa única execução do Failsafe. Os do que vem
  desligado por padrão (Kafka, autenticação ligada, log em JSON e observabilidade que dependa de
  configuração), a execução com o log em JSON e a cobertura mínima vão para o perfil `extras` (`./mvnw -Pextras verify`), sem Docker,
  que o job `build` roda obrigatoriamente. Nenhum IT muda. FR-001, FR-038, SC-001.
- Q: O Marco 4 entrega as seis etapas? → A: Não (decisão do responsável, 2026-11-26). Entram as etapas 1 a 3, mais a
  página do SLO, o runbook (os alertas apontam para ele) e a seção do Marco 4 no README, com o roteiro do que ficou
  para depois. Ficam adiadas a etapa 4 (US4, FR-020 a FR-023), a etapa 5 (US5, FR-024 a FR-027) e a etapa 6 (US6,
  FR-028 a FR-030, e os ADRs da FR-037). Os checks obrigatórios da `main` (FR-031) passam a ser 10: os 8 do Marco 3,
  `alert-rules` e `load`. A spec e as tarefas dos itens adiados ficam guardadas para um marco futuro.

## User Scenarios & Testing *(mandatory)*

Os atores são:

- o **avaliador da Outsera**, que roda só o modo padrão (`./mvnw`) e precisa encontrar exatamente o
  comportamento dos Marcos 1 a 3;
- o **operador do ambiente completo**, que sobe o compose, acompanha os alertas ligados ao SLO e segue
  o runbook quando um deles dispara;
- o **mantenedor**, que recebe no pipeline a verificação das regras de alerta, o teste de carga, o SBOM,
  a assinatura e a análise de dependências, e que precisa da `main` protegida;
- o **colaborador novo**, que abre o repositório num dev container e encontra o ambiente pronto;
- quem **verifica a procedência** da imagem, conferindo que ela foi construída por este repositório e o
  que ela contém.

### User Story 1 - Alertas ligados ao SLO no compose (Priority: P1)

O operador sobe o compose e encontra, no Prometheus, as regras de alerta do SLO do endpoint de
intervalos: uma de disponibilidade e uma de latência. Cada alerta diz qual objetivo está em risco e
aponta para a seção do runbook com o que fazer. Os objetivos estão numa página de SLO, com valores que
vêm da medição do teste de carga (US2).

**Why this priority**: Fecha o raciocínio de operação do briefing (objetivo, medição, alerta e ação). É o
primeiro item do escopo e o que dá sentido ao teste de carga.

**Independent Test**: No pipeline, verificar a sintaxe das regras e rodar os testes das regras com séries
sintéticas (um cenário que dispara cada alerta e um que não dispara); no compose, conferir que o
Prometheus carregou as regras e que, em operação normal, nenhum alerta do SLO está disparando.

**Acceptance Scenarios**:

1. **Given** o compose no ar, **When** o Prometheus é consultado, **Then** as regras de disponibilidade e
   de latência do endpoint de intervalos estão carregadas, sem erro de avaliação.
2. **Given** séries sintéticas com a taxa de erro do endpoint acima do que o objetivo de disponibilidade
   tolera, pelo tempo da regra, **When** os testes das regras rodam, **Then** o alerta de disponibilidade
   dispara, com a gravidade e o link do runbook.
3. **Given** séries sintéticas com a fração de requisições lentas acima do que o objetivo de latência
   tolera, **When** os testes das regras rodam, **Then** o alerta de latência dispara.
4. **Given** séries sintéticas dentro dos objetivos, **When** os testes das regras rodam, **Then** nenhum
   alerta do SLO dispara.
5. **Given** a aplicação fora do ar para o Prometheus (alvo sem coleta), **When** o tempo da regra passa,
   **Then** o alerta de disponibilidade dispara: ausência de dados não é tratada como saúde.
6. **Given** um alerta disparado, **When** o operador segue o link da anotação, **Then** chega à seção do
   runbook daquele alerta, com o que conferir e o que fazer.
7. **Given** uma regra com erro de sintaxe ou um teste de regra que falha, **When** o pipeline roda,
   **Then** o job fica vermelho.

---

### User Story 2 - Teste de carga que reprova o pipeline (Priority: P1)

O mantenedor tem, no pipeline, um teste de carga contra o compose, com os dados gerados (US3). O teste
tem limites de latência e de taxa de erro; se algum limite é ultrapassado, o job fica vermelho. Os
números medidos aparecem no resumo do job e no README.

**Why this priority**: Os objetivos do SLO só são definidos depois da medição (briefing). Sem o teste de
carga, os alertas da US1 teriam números inventados.

**Independent Test**: No pipeline, subir o compose com o arquivo gerado, esperar a readiness, rodar o
teste de carga e conferir o resumo com os percentis e a taxa de erro. Rodar uma vez, fora do pipeline,
com um limite impossível e conferir que o teste termina com falha.

**Acceptance Scenarios**:

1. **Given** o compose pronto com o arquivo gerado, **When** o teste de carga roda, **Then** ele exercita
   o endpoint de intervalos e a listagem, e o resumo traz a vazão, os percentis 95 e 99 de latência e a
   taxa de erro de cada endpoint.
2. **Given** uma execução em que a latência ou a taxa de erro passa do limite, **When** o teste termina,
   **Then** ele sai com falha e o job do pipeline fica vermelho.
3. **Given** uma execução dentro dos limites, **When** o teste termina, **Then** o job fica verde e o
   resumo fica disponível na página da execução.
4. **Given** o teste de carga, **When** ele confere as respostas, **Then** cada resposta dos intervalos
   tem o formato exato do enunciado, e uma resposta fora do formato conta como erro.
5. **Given** o compose com a autenticação ligada, **When** o teste de carga roda com o token de
   demonstração, **Then** as respostas são 200; sem o token, o teste não mede latência de respostas 401
   como se fossem sucesso.

---

### User Story 3 - Gerar um CSV grande e determinístico (Priority: P2)

O mantenedor gera um CSV grande, válido pelas regras do Marco 1, com um comando que só precisa do JDK. O
mesmo tamanho e a mesma semente dão sempre o mesmo arquivo, byte a byte. O arquivo alimenta o teste de
carga e a medição do tempo da carga inicial, nos dois caminhos de carga. O `movielist.csv` continua sendo
o padrão.

**Why this priority**: O arquivo fornecido tem cerca de 200 linhas e não estressa nada. O teste de carga
e a medição da carga inicial dependem de um volume maior e reproduzível.

**Independent Test**: Gerar o arquivo duas vezes com os mesmos parâmetros e comparar os resumos
criptográficos; subir a aplicação com ele e conferir a readiness, o resumo da carga sem linhas
rejeitadas e intervalos não vazios.

**Acceptance Scenarios**:

1. **Given** o mesmo tamanho e a mesma semente, **When** o gerador roda duas vezes, em máquinas ou sistemas
   operacionais diferentes, **Then** os arquivos são idênticos byte a byte.
2. **Given** sementes diferentes, **When** o gerador roda, **Then** os arquivos são diferentes.
3. **Given** um arquivo gerado, **When** a aplicação sobe com ele no modo padrão, **Then** a carga aceita
   todas as linhas, a aplicação fica pronta e os intervalos trazem `min` e `max` não vazios, no formato do
   enunciado.
4. **Given** um arquivo gerado, **When** ele é lido, **Then** usa o separador e o cabeçalho do Marco 1,
   tem produtores separados por vírgula e por ` and `, empates em `min` ou em `max` e duas vitórias de um
   produtor no mesmo ano, para exercitar as regras do enunciado em volume.
5. **Given** a aplicação subida sem nenhuma variável, **When** a carga termina, **Then** o arquivo
   carregado é o `movielist.csv`, como nos marcos anteriores: o gerado não é embarcado nem vira padrão.
6. **Given** um arquivo gerado, **When** a carga inicial é medida pela carga direta (H2) e pela ingestão por
   Kafka (compose), **Then** o tempo de cada uma e o número de linhas ficam registrados no README.

---

### User Story 4 - Subida mais rápida da imagem com cache AOT (Priority: P2)

A imagem da aplicação traz um cache AOT gerado no próprio build. O tempo de subida até a readiness é
medido sem e com o cache, nas mesmas condições, e os dois números ficam registrados. Fora da imagem, nada
muda: `./mvnw spring-boot:run` sobe como antes.

**Why this priority**: É uma melhoria mensurável da subida, mas o enunciado não depende dela e ela é o
primeiro item da ordem de corte do briefing.

**Independent Test**: No pipeline, construir a imagem com e sem o cache, subir cada uma várias vezes,
medir o tempo até a readiness e conferir que a imagem com cache passa no mesmo teste de fumaça do Marco 2
e no teste do compose.

**Acceptance Scenarios**:

1. **Given** a imagem com o cache, **When** ela sobe no modo padrão, **Then** o teste de fumaça do Marco 2
   passa inteiro, com o resultado de referência.
2. **Given** a imagem com o cache, **When** ela sobe no perfil Docker do compose, **Then** o teste do
   compose do Marco 3 passa inteiro, inclusive a ingestão por Kafka e o PostgreSQL.
3. **Given** a imagem com e sem o cache, **When** o tempo até a readiness é medido nas mesmas condições,
   **Then** a mediana de cada uma fica registrada no resumo do pipeline e no README.
4. **Given** um cache que a JVM não consegue usar (por exemplo, gerado com outra JVM), **When** a imagem
   sobe, **Then** ela sobe sem o cache, com um aviso no log, e continua funcionando.
5. **Given** a máquina sem Docker, **When** `./mvnw verify` e `./mvnw spring-boot:run` rodam, **Then** o
   comportamento e os testes são os do Marco 3.
6. **Given** a mesma imagem com o cache, **When** ela sobe com e sem o perfil Docker e com a
   autenticação ligada e desligada, **Then** cada combinação liga exatamente o que ligava no Marco 3: o
   cache não fixa perfil nem propriedade na construção (Clarifications, 2026-11-25).

---

### User Story 5 - SBOM e assinatura da imagem sem publicação (Priority: P2)

O pipeline gera o SBOM da imagem construída e a assina sem chave guardada, com a identidade do próprio
workflow. Quem baixa os artefatos da execução consegue verificar que a assinatura foi feita por este
repositório, neste workflow, e conferir os componentes da imagem. A imagem não é enviada a nenhum
registro público neste marco.

**Why this priority**: Fecha a cadeia de entrega do briefing (imagem assinada, com SBOM e procedência). A
publicação da imagem fica para o marco da entrega.

**Independent Test**: No pipeline, gerar o SBOM, assinar, verificar a assinatura com a identidade
esperada (repositório e workflow) e reprovar a verificação com uma identidade diferente.

**Acceptance Scenarios**:

1. **Given** a imagem construída no pipeline, **When** o SBOM é gerado, **Then** ele lista os pacotes do
   sistema e as bibliotecas Java da imagem, com as versões efetivas, inclusive as transitivas do
   `kafka-clients`.
2. **Given** a imagem e o SBOM, **When** o pipeline assina, **Then** nenhuma chave privada ou segredo do
   repositório é usado: a identidade é a do workflow, emitida para aquela execução.
3. **Given** a assinatura, **When** ela é verificada com a identidade esperada (este repositório e este
   workflow), **Then** a verificação passa. **When** é verificada com outra identidade, **Then** falha.
4. **Given** qualquer execução do pipeline deste marco, **When** ela termina, **Then** nenhuma imagem foi
   enviada a um registro público.
5. **Given** um pull request vindo de um fork, sem a permissão de identidade do workflow, **When** o
   pipeline roda, **Then** a assinatura é pulada com o motivo no resumo, e os demais jobs rodam.
6. **Given** a imagem num registro temporário dentro do job, **When** ela é assinada e recebe o SBOM
   como atestado, **Then** a verificação da assinatura e a do atestado passam como imagem, pelo digest, e
   o registro é descartado no fim do job (Clarifications, 2026-11-25).

---

### User Story 6 - Abrir o projeto pronto num dev container (Priority: P3)

O colaborador abre o repositório num dev container e encontra o JDK 25, o Maven pelo wrapper e o Docker
disponíveis, sem instalar nada na máquina além do suporte a dev containers. Dentro dele, `./mvnw verify`
passa e o compose sobe.

**Why this priority**: Facilita a quem quiser ver o ambiente completo, mas o avaliador do enunciado só
precisa do JDK.

**Independent Test**: No pipeline, construir o dev container e rodar dentro dele `java -version`,
`./mvnw -v`, `docker version` e `./mvnw verify`.

**Acceptance Scenarios**:

1. **Given** o repositório aberto no dev container, **When** o terminal é aberto, **Then** `java -version`
   mostra o JDK 25 e `./mvnw -v` funciona.
2. **Given** o dev container, **When** `./mvnw verify` roda, **Then** passa, como fora dele.
3. **Given** o dev container, **When** `docker compose up` roda, **Then** o compose sobe e a aplicação fica
   pronta.
4. **Given** o dev container, **When** a aplicação ou o compose sobem, **Then** as portas da API, do
   Prometheus e do Grafana ficam acessíveis a partir da máquina.
5. **Given** um clone sem o dev container, **When** `./mvnw verify` roda só com o JDK, **Then** nada muda:
   o dev container é opcional.

---

### User Story 7 - Fechar as pendências do Marco 3 (Priority: P1)

O mantenedor fecha o que ficou do Marco 3: a proteção da `main` com os checks obrigatórios registrada, a
causa de o job de dependências não ter apontado as vulnerabilidades das transitivas do `kafka-clients`,
com a correção, e o resultado do testador adversário na ingestão por Kafka.

**Why this priority**: São pendências de um marco já mergeado. A falha do job de dependências é uma
lacuna real de segurança: uma vulnerabilidade de dependência só foi pega na análise da imagem.

**Independent Test**: Conferir em `docs/ai/decisoes.md` a lista de checks obrigatórios e o bypass
desligado; no pipeline, conferir que o job de dependências lista as transitivas de runtime com as versões
efetivas e reprova uma versão vulnerável com correção; conferir o resultado do testador adversário e os
testes permanentes de cada falha encontrada.

**Acceptance Scenarios**:

1. **Given** a proteção da `main` configurada pelo Anderson, **When** o registro é conferido, **Then**
   `docs/ai/decisoes.md` traz a lista dos checks obrigatórios (os do Marco 3 e os novos deste marco) e a
   confirmação de que o bypass para administradores está desligado, sem dado pessoal.
2. **Given** a investigação do job de dependências, **When** ela termina, **Then** a causa está registrada
   em `docs/ai/decisoes.md`, com a evidência que a comprova.
3. **Given** o job de dependências corrigido, **When** ele roda, **Then** o inventário analisado inclui as
   dependências transitivas de runtime com as versões efetivas, como `lz4-java` e `snappy-java` trazidas
   pelo `kafka-clients`.
4. **Given** uma versão de dependência transitiva com vulnerabilidade alta ou crítica e versão corrigida
   (por exemplo, `lz4-java` 1.11.2), num branch temporário que nunca é mergeado e é apagado no fim (roteiro
   da T050 do Marco 2), **When** o job de dependências roda contra ela, **Then** o job fica vermelho, e
   nenhum commit do branch temporário volta ao branch do marco.
5. **Given** o testador adversário (Codex) acionado pelo Anderson, **When** ele cria eventos de ataque a
   partir da spec 003 e do contrato dos eventos, sem ler o código de produção, **Then** cada falha
   encontrada vira teste de integração permanente no `./mvnw verify`, com a correção, e o resultado (com
   ou sem falhas) fica registrado.

---

### Edge Cases

- Prometheus sem nenhuma requisição ao endpoint de intervalos (compose recém-subido, sem tráfego): a
  regra de latência não dispara por divisão por zero nem por falta de série; a de disponibilidade só
  dispara se o alvo estiver fora do ar.
- Respostas 4xx do endpoint (por exemplo, 401 com a autenticação ligada, ou 304 do ETag): não contam como
  indisponibilidade. Só 5xx e alvo sem coleta contam. O 304 conta como sucesso.
- Runner do pipeline mais lento ou com vizinhos ruidosos: os limites do teste de carga têm margem sobre o
  medido, e a página do SLO registra a diferença entre o limite do pipeline e o objetivo do SLO.
- Teste de carga começando antes da readiness: não pode acontecer; o teste só começa com a readiness 200.
- Gerador chamado com tamanho zero, negativo ou maior que o limite documentado: termina com erro e
  mensagem clara, sem arquivo parcial.
- Gerador chamado sem semente: usa uma semente padrão fixa e documentada, para continuar determinístico.
- Arquivo gerado com fim de linha: sempre o mesmo, independente do sistema operacional, para que o resumo
  criptográfico seja o mesmo no Windows e no Linux.
- Arquivo gerado grande demais para o heap da imagem: o tamanho padrão cabe na memória da imagem com a
  configuração do Marco 2; o README informa o limite medido.
- Cache AOT gerado num build e usado com outra versão da JVM ou outro classpath: a JVM o ignora, com aviso,
  e a aplicação sobe sem ele.
- Pipeline sem a permissão de identidade (fork): a assinatura é pulada, sem reprovar os demais jobs; o
  check da assinatura não é obrigatório nesses casos, mas é obrigatório nos pull requests do próprio
  repositório.
- Serviço de transparência da assinatura indisponível: o job de assinatura fica vermelho com o motivo, sem
  repetição infinita.
- Dev container numa máquina sem suporte a Docker aninhado: o README informa o requisito; `./mvnw verify`
  continua funcionando dentro dele.
- Testador adversário sem falhas encontradas: o resultado é registrado assim mesmo, com a lista dos
  ataques tentados.

## Requirements *(mandatory)*

### Functional Requirements

**Modo padrão**

- **FR-001**: O modo padrão MUST continuar funcionando sem Docker, com H2 e o `movielist.csv` embarcado:
  `./mvnw spring-boot:run` sobe a aplicação, e `./mvnw verify` roda os ITs de todo o modo padrão numa única
  execução do Failsafe. `./mvnw -Pextras verify` roda também os ITs do que vem desligado por padrão
  (Kafka, autenticação ligada e log em JSON), a execução com o log em JSON e a cobertura mínima, também
  sem Docker. Nenhum teste dos Marcos 1 a 3 tem o resultado
  esperado alterado (Princípio II, constituição 2.0.0).
- **FR-002**: Nenhum item deste marco MUST adicionar dependência de execução, configuração obrigatória ou
  teste unitário à API. O contrato de intervalos continua idêntico ao do enunciado (Princípio III).

**SLO e alertas**

- **FR-003**: O repositório MUST ter uma página de SLO com o objetivo de disponibilidade e o de latência do
  endpoint de intervalos, a janela de cada um, o que conta como requisição boa e ruim, e a origem dos
  valores (a medição do teste de carga, FR-010).
- **FR-004**: O Prometheus do compose MUST carregar regras de alerta versionadas no repositório: ao menos
  uma de disponibilidade e uma de latência do endpoint de intervalos, derivadas dos objetivos da FR-003.
- **FR-005**: A disponibilidade MUST contar como ruins as respostas 5xx e o período em que o alvo da
  aplicação não é coletado. Respostas 2xx, 304 e 4xx MUST contar como boas para a disponibilidade.
- **FR-006**: Cada alerta MUST ter gravidade, um resumo do objetivo em risco e um link para a seção do
  runbook correspondente.
- **FR-007**: O repositório MUST ter um runbook com uma seção por alerta: o que o alerta significa, o que
  conferir (painéis do dashboard, logs, readiness) e o que fazer.
- **FR-008**: O pipeline MUST verificar a sintaxe das regras e rodar testes das regras com séries
  sintéticas: para cada alerta, ao menos um cenário que dispara e um que não dispara. Uma regra inválida ou
  um teste que falha MUST deixar o job vermelho.
- **FR-009**: O teste do compose no pipeline MUST conferir que o Prometheus carregou as regras e que nenhum
  alerta do SLO está disparando com a aplicação pronta.

**Teste de carga**

- **FR-010**: O repositório MUST ter um teste de carga versionado contra o compose, que exercita o endpoint
  de intervalos e a listagem com o arquivo gerado (FR-014), e publica a vazão, os percentis 95 e 99 de
  latência e a taxa de erro por endpoint.
- **FR-011**: O teste de carga MUST ter limites de latência e de taxa de erro. Passar de qualquer limite
  MUST encerrar o teste com falha e deixar o job vermelho. Os limites MUST estar versionados junto do teste
  e explicados na página do SLO.
- **FR-011a**: O objetivo do SLO e os limites do teste de carga MUST vir da medição do teste de carga num
  runner compartilhado do GitHub Actions, o mesmo tipo de máquina em que o job roda. Os limites do pipeline
  MUST ser mais folgados que o objetivo do SLO, com uma folga escolhida para absorver o ruído do runner
  compartilhado e o job não ficar instável. A medição (número de execuções, valores de cada uma e
  dispersão) e a folga escolhida, com o motivo, MUST ser registradas em `docs/ai/decisoes.md`.
- **FR-012**: O teste de carga MUST conferir o formato das respostas dos intervalos (campos exatos do
  enunciado) e contar como erro qualquer resposta fora do formato ou com status diferente do esperado.
- **FR-013**: O pipeline MUST rodar o teste de carga num job próprio, que sobe o compose com o arquivo
  gerado, espera a readiness, roda o teste, publica o resumo na página da execução e derruba o ambiente.

**Dados gerados**

- **FR-014**: O repositório MUST ter um gerador de CSV que roda só com o JDK, recebe o número de filmes e
  uma semente, e produz um arquivo válido pelas regras da spec 001 (separador, cabeçalho, `winner`,
  separação de produtores).
- **FR-015**: O gerador MUST ser determinístico: os mesmos parâmetros produzem o mesmo arquivo byte a byte,
  em qualquer sistema operacional (mesma codificação e mesmo fim de linha).
- **FR-016**: O arquivo gerado MUST exercitar as regras do enunciado em volume: produtores separados por
  vírgula, por ` and ` e por `, and`, empates em `min` e em `max`, e duas vitórias de um produtor no mesmo
  ano. Parâmetros inválidos MUST terminar com erro e mensagem clara, sem arquivo parcial.
- **FR-017**: O arquivo gerado MUST NOT ser embarcado na aplicação nem substituir o `movielist.csv` como
  padrão. Ele é passado pela variável do caminho do CSV, que já existe (spec 001, FR-002).
- **FR-018**: O tempo da carga inicial do arquivo gerado MUST ser medido pela carga direta e pela ingestão
  por Kafka e registrado no README, com o número de linhas e o ambiente da medição.
- **FR-019**: O pipeline MUST comprovar o determinismo do gerador: gera o arquivo duas vezes, com o mesmo
  número de filmes e a mesma semente, calcula o hash de cada um e reprova se forem diferentes. Os dois
  hashes MUST aparecer no resumo da execução.

**Cache AOT**

- **FR-020**: A imagem MUST trazer um cache AOT gerado no próprio build da imagem, sem mudar o
  comportamento da aplicação em nenhum perfil. O cache é só o da JVM, gerado por uma execução de treino
  da aplicação no build. O processamento AOT do Spring MUST NOT ser usado, porque fixaria na construção as
  condições de perfil e de propriedade que ligam o Kafka, o PostgreSQL e a autenticação (Clarifications,
  2026-11-25). A mesma imagem MUST servir ao modo padrão e ao compose.
- **FR-021**: A imagem com o cache MUST passar no teste de fumaça do Marco 2 e no teste do compose do
  Marco 3 sem alteração dos passos.
- **FR-022**: O pipeline MUST medir o tempo da subida até a readiness da imagem sem e com o cache, nas
  mesmas condições e com várias repetições, e publicar a mediana de cada uma no resumo. Os números medidos
  MUST ficar no README.
- **FR-023**: Um cache que a JVM não consegue usar MUST NOT impedir a subida: a aplicação sobe sem ele.

**SBOM e assinatura**

- **FR-024**: O pipeline MUST gerar o SBOM da imagem num formato padrão, listando os pacotes do sistema e as
  bibliotecas Java com as versões efetivas, e guardá-lo como artefato da execução.
- **FR-025**: O pipeline MUST assinar sem chave guardada, com a identidade do workflow emitida para a
  execução, e MUST verificar a assinatura em seguida, aceitando só a identidade deste repositório e deste
  workflow. A imagem MUST ser enviada a um registro temporário dentro do próprio job, assinada pelo
  digest e receber o SBOM como atestado assinado; a verificação MUST conferir a assinatura e o atestado
  como imagem. O registro temporário é descartado no fim do job (Clarifications, 2026-11-25).
- **FR-026**: Nenhum job deste marco MUST enviar a imagem a um registro público. A permissão de identidade
  MUST ficar só no job que assina, e a política do workflow (Marco 2) MUST continuar reprovando qualquer
  publicação.
- **FR-027**: Em execuções sem a permissão de identidade (pull request de fork), a assinatura MUST ser
  pulada com o motivo no resumo, sem reprovar os demais jobs.

**Dev container**

- **FR-028**: O repositório MUST ter uma configuração de dev container com o JDK 25, o Maven pelo wrapper e
  o Docker com o compose, com as versões das imagens e dos recursos fixadas.
- **FR-029**: Dentro do dev container, `./mvnw verify` MUST passar e `docker compose up` MUST deixar a
  aplicação pronta, com as portas da API, do Prometheus e do Grafana encaminhadas.
- **FR-030**: O pipeline MUST construir o dev container e rodar dentro dele `./mvnw verify`.

**Pendências do Marco 3**

- **FR-031**: A configuração da proteção da `main` (T093 da spec 003) MUST ficar registrada em
  `docs/ai/decisoes.md`: a lista dos checks obrigatórios, incluindo os jobs novos deste marco, e a
  confirmação do bypass desligado, sem dado pessoal. A configuração é feita pelo Anderson.
- **FR-032**: A causa de o job de dependências não ter apontado as vulnerabilidades das transitivas do
  `kafka-clients` MUST ser investigada e registrada em `docs/ai/decisoes.md`, com a evidência.
- **FR-033**: O job de dependências MUST analisar o inventário completo das dependências de runtime, com as
  transitivas e as versões efetivas depois da gestão de versões do build. O resumo do job MUST mostrar que
  `lz4-java` e `snappy-java` estão no inventário.
- **FR-034**: A correção da FR-033 MUST ser comprovada uma vez contra uma versão vulnerável conhecida (por
  exemplo, `lz4-java` 1.11.2), com o job de dependências vermelho. A verificação segue o roteiro da T050 do
  Marco 2 (spec 002):
  - é feita num branch temporário criado a partir do branch do marco, que **nunca é mergeado** e é
    **apagado** no fim, local e remotamente;
  - nenhum commit desse branch volta ao branch do marco: nada de cherry-pick, merge ou rebase a partir
    dele, e a versão vulnerável não fica no código do marco nem na `main`;
  - o push do branch temporário é feito pelo Anderson ou com a autorização dele;
  - depois de apagar o branch, `git branch -a` e o log do branch do marco confirmam que nenhum commit dele
    ficou.

  O registro em `docs/ai/decisoes.md` (nome do branch, link da execução, job vermelho, resultado esperado e
  obtido, confirmação de que o branch foi apagado) é a única coisa da verificação que entra no marco.
- **FR-035**: O testador adversário (Codex) MUST ser acionado pelo Anderson para a ingestão por Kafka (T050
  da spec 003), criando eventos de ataque a partir da spec 003 e do contrato dos eventos, sem ler o código
  de produção. Cada falha encontrada MUST virar teste de integração permanente no `./mvnw -Pextras verify`, com o
  broker embutido, e a correção, no mesmo bolt (Princípio I). O resultado MUST ser registrado, mesmo sem
  falhas.

**README, ADRs e decisões**

- **FR-036**: O `README.md` MUST ganhar a seção do Marco 4 entre os extras: alertas e SLO (com links para a
  página do SLO e o runbook), teste de carga (como rodar e os números medidos), gerador de CSV (como usar e
  os tempos de carga), cache AOT (os tempos de subida antes e depois), SBOM e assinatura (como verificar) e
  dev container (como abrir). A tabela de requisitos e comprovações MUST ganhar o Marco 4. A seção "Avalie
  em 2 minutos" MUST continuar funcionando só com o JDK.
- **FR-037**: As decisões do marco (alertas ligados ao SLO, teste de carga no pipeline, gerador
  determinístico, cache AOT, SBOM e assinatura sem publicação, dev container) MUST ter ADR curto em
  `docs/adr/`, no formato dos anteriores.

**Comprovação**

- **FR-038**: Cada requisito MUST ser comprovado na forma do Princípio VI (constituição 2.0.0):
  - modo padrão (FR-001, FR-002, FR-017): a suíte existente, verde e sem alteração dos resultados
    esperados: os ITs do modo padrão no `./mvnw verify` e os do que vem desligado por padrão no
    `./mvnw -Pextras verify`, que o job `build` roda;
  - correções do testador adversário (FR-035): teste de integração no `./mvnw -Pextras verify`;
  - regras de alerta, teste de carga, gerador, cache AOT, SBOM, assinatura, dev container e job de
    dependências (FR-008 a FR-016, FR-019 a FR-030, FR-033): estágio do pipeline, indicado no plano;
  - SLO, runbook, README, ADRs e registros em `docs/ai/decisoes.md` (FR-003, FR-007, FR-011a, FR-018,
    FR-031, FR-032, FR-034, FR-036, FR-037): revisão no pull request, com seção própria no checklist da
    spec.

### Key Entities

- **Objetivo de SLO**: indicador (disponibilidade ou latência do endpoint de intervalos), alvo, janela e
  definição de requisição boa e ruim.
- **Regra de alerta**: expressão sobre as métricas, tempo mínimo, gravidade, resumo e link do runbook.
  Ligada a um objetivo de SLO.
- **Teste de regra**: séries sintéticas e os alertas esperados (disparando ou não) em cada instante.
- **Cenário de carga**: endpoints exercitados, perfil de tráfego, limites de latência e de erro.
- **Arquivo gerado**: número de filmes, semente, resumo criptográfico. Válido pela spec 001.
- **Medição**: tempo da subida (sem e com o cache) ou da carga inicial (direta e por Kafka), com o ambiente
  e o número de repetições.
- **SBOM**: inventário dos componentes da imagem, com nome, versão e tipo.
- **Assinatura**: o que foi assinado, a identidade do workflow e o registro de transparência.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: `./mvnw verify` (ITs do modo padrão) e `./mvnw -Pextras verify` (todos os ITs dos Marcos 1 a
  4, fora os do PostgreSQL) passam sem Docker, com os resultados esperados sem alteração.
- **SC-002**: 100% das regras de alerta têm ao menos um teste que dispara e um que não dispara, e todos
  passam no pipeline.
- **SC-003**: Com a aplicação pronta e sem falhas injetadas, nenhum alerta do SLO dispara no compose.
- **SC-004**: O teste de carga fica verde no pipeline do pull request do marco, com a taxa de erro e os
  percentis dentro dos limites, e fica vermelho numa execução com um limite impossível.
- **SC-005**: Duas gerações com os mesmos parâmetros dão o mesmo resumo criptográfico, no Linux e no
  Windows.
- **SC-006**: A aplicação carrega um arquivo gerado com ao menos 100 mil filmes sem nenhuma linha rejeitada
  e fica pronta, nos dois caminhos de carga.
- **SC-007**: A mediana do tempo de subida até a readiness com o cache AOT é menor que a mediana sem ele, e
  os dois números estão no README.
- **SC-008**: A verificação da assinatura passa com a identidade deste repositório e falha com qualquer
  outra, em 100% das execuções do próprio repositório.
- **SC-009**: Um colaborador abre o dev container e roda `./mvnw verify` com sucesso sem instalar nada além
  do suporte a dev containers.
- **SC-010**: O job de dependências aponta uma vulnerabilidade alta conhecida numa dependência transitiva
  de runtime, que hoje ele não aponta.
- **SC-011**: Todos os checks obrigatórios da `main`, inclusive os novos deste marco, ficam verdes no pull
  request do marco.

## Assumptions

- O modo padrão continua sendo o avaliado (Princípio II). Nada deste marco é exigido para subir ou testar
  com `./mvnw`.
- Os valores dos objetivos do SLO são definidos depois da medição do teste de carga num runner
  compartilhado, como pede o briefing. O limite do teste de carga no pipeline é mais folgado que o objetivo
  do SLO, por causa do ruído do runner compartilhado, para o job não ficar instável (FR-011a). A página do
  SLO explica a diferença, e `docs/ai/decisoes.md` registra a medição e a folga.
- Os alertas são avaliados e exibidos pelo Prometheus do compose. Notificação (Alertmanager, e-mail,
  chat) fica fora do escopo.
- O teste de carga roda contra o compose completo (PostgreSQL e Kafka), que é o ambiente operacional do
  projeto, com a autenticação desligada, como o compose padrão. O cenário com o token de demonstração é
  coberto, mas não é o padrão.
- O tamanho padrão do arquivo gerado é de 100 mil filmes, ajustável por parâmetro; o limite superior fica
  no plano, a partir da medição de memória.
- O gerador fica fora do código da API e não tem teste unitário: é ferramenta do repositório, comprovada
  pelo pipeline (FR-019) e pela carga real (SC-006). Ele não é teste do back-end, então o Princípio I não
  se aplica a ele.
- A medição da carga inicial e do tempo de subida é feita no runner do pipeline; números da máquina local,
  se publicados, vêm identificados como tal.
- A assinatura usa a identidade emitida pelo GitHub Actions para a execução, e a entrada no registro de
  transparência é pública. Ela traz só o repositório e o workflow, que já são públicos.
- A publicação da imagem no GHCR, o atestado de procedência publicado e o release ficam para o marco da
  entrega (briefing, estágio "Entrega").
- A proteção da `main` é configurada pelo Anderson; este marco só registra a configuração e inclui os jobs
  novos na lista.
- O testador adversário é acionado pelo Anderson; o implementador só transforma as falhas em testes e
  correções.
- Os papéis seguem o `AGENTS.md`: o Dockerfile, o compose, o Prometheus, o pipeline, o dev container, o
  teste de carga e o gerador são do papel de infraestrutura; o README, os ADRs, a página do SLO e o runbook
  são do papel de documentação; as correções do testador adversário são do implementador.
- Fora do escopo: publicação da imagem, deploy em nuvem, notificação de alertas, tracing distribuído,
  imagem nativa, front-end e qualquer mudança no contrato da API.
