<!--
Sync Impact Report (2.1.0)
- Version change: 2.0.0 → 2.1.0
  Justificativa: MINOR. Orientação ampliada para o front-end, sem remover nem relaxar nenhuma regra:
  - Princípio II: o front-end fica fora do `./mvnw`; nenhum comando Maven pode exigir Node nem construir o
    front;
  - Princípio VI: novo tipo de comprovação, comportamento do front-end, por testes unitários com cobertura
    mínima de 80% de linhas e teste de ponta a ponta contra a aplicação real, no pipeline. Um requisito da API
    continua exigindo IT;
  - Princípio I: sem regra nova; a frase sobre os testes unitários do front passa a citar o Princípio VI;
  - portão de merge: inclui os jobs do front-end.
  Origem: spec 006 (Marco 6), FR-024.
- Modified principles: I (redação), II (item novo), VI (tipo de comprovação novo)
- Modified sections: Fluxo de desenvolvimento e portões de qualidade (portão de merge)
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - AGENTS.md ✅ regras invioláveis, testes, stack e pontos em aberto;
  - specs/006-frontend-angular ✅ FR-022 a FR-024 e verificação da constituição do plano;
  - docs/ai/decisoes.md ✅ emenda registrada;
  - specs/001 a 005: nenhuma mudança (não têm front-end).
- Templates requiring updates: nenhum
- Follow-up TODOs: nenhum

Sync Impact Report (2.0.0)
- Version change: 1.7.0 → 2.0.0
  Justificativa: MAJOR, por decisão do responsável. O Princípio II deixa de exigir que os testes dos
  extras passem com o `./mvnw verify` padrão: essa garantia literal deixa de valer. O `./mvnw verify`
  padrão passa a rodar só os ITs do **modo padrão**, numa única execução do Failsafe: todo comportamento
  com H2, a carga do CSV (inclusive as regras acrescentadas pela spec 003), os endpoints, o Swagger e o
  que vem ligado sem configuração. Os ITs do que vem **desligado por padrão** (Kafka, autenticação
  ligada, log em JSON e observabilidade que dependa de configuração) e a execução com o log em JSON vão
  para o perfil Maven `extras`, também sem Docker e só com o JDK, que o job `build` do pipeline executa
  obrigatoriamente. O PostgreSQL continua no perfil `postgres-it`. A cobertura mínima de 90% é verificada
  no pipeline sobre a soma das execuções. Nenhum teste é removido, relaxado ou alterado: muda só o
  perfil em que roda.
  Origem: o `./mvnw verify` padrão chegou a cerca de 8 min com os ITs dos Marcos 2 a 4, e é o comando
  que o avaliador roda para o core (spec 004, etapa 2).
- Modified principles: II. Execução só com ./mvnw; VI. Rastreabilidade requisito → teste
- Modified sections: Fluxo de desenvolvimento e portões de qualidade (portão de merge)
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - pom.xml ✅ execução padrão só com os ITs de `src/test/default-mode-its.txt`; perfil `extras` com os demais,
    a execução `json-logs` e a cobertura mínima;
  - .github/workflows/ci.yml ✅ o job `build` roda `./mvnw -B -Pextras verify`;
  - AGENTS.md ✅ regras invioláveis, fluxo de trabalho, testes e definição de pronto;
  - README.md ✅ "Avalie em 2 minutos", com o tempo medido, e o comando dos extras;
  - specs/004-acabamento-operacional ✅ FR-001, FR-002, SC-001, plano e tarefas;
  - specs/001 a 003: o texto histórico fala do `./mvnw verify` da época; a regra vigente é esta;
  - docs/ai/decisoes.md ✅ emenda registrada;
  - `check-unchanged-tests.sh`: sem mudança, porque nenhum IT muda.
- Templates requiring updates: nenhum
- Follow-up TODOs: nenhum

Sync Impact Report (1.7.0)
- Version change: 1.6.0 → 1.7.0
  Justificativa: MINOR. O Princípio VI ganha uma regra de comprovação para o comportamento da
  aplicação que depende de infraestrutura indisponível no modo padrão, como o PostgreSQL. Esse
  comportamento é comprovado por testes de integração num perfil Maven dedicado, executado
  obrigatoriamente no pipeline. Todo o resto continua exigindo teste no `./mvnw verify`.
  Antes, o VI pedia o teste "no `./mvnw verify`" e o II punha os testes de PostgreSQL num perfil
  separado, sem dizer como os dois se combinavam. Nenhum requisito que cumpria a versão anterior deixa
  de cumprir.
  Origem: Constitution Check do plano 003 (Marco 3).
- Modified principles: VI. Rastreabilidade requisito → teste (regra nova)
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - AGENTS.md ✅ fluxo de trabalho (rastreabilidade) e seção Testes;
  - specs/003-modo-docker ✅ FR-038 e Constitution Check do plano (a nota de leitura conjunta virou
    referência à 1.7.0);
  - docs/ai/decisoes.md ✅ emenda registrada;
  - specs/001 e specs/002: nenhuma mudança (não têm comportamento dependente de PostgreSQL).
- Templates requiring updates: nenhum
- Follow-up TODOs: nenhum

Sync Impact Report (1.6.0)
- Version change: 1.5.0 → 1.6.0
  Justificativa: MINOR. O Princípio VII ganha uma segunda exceção, ampliando o que é permitido sem
  proibir nada que valia antes. Além do token de demonstração (sem mudança), passam a ser permitidas as
  credenciais de demonstração dos serviços do compose de desenvolvimento local, com quatro condições:
  - aparecem só nos arquivos do compose local e na configuração que ele monta;
  - são identificadas como de demonstração no README e no próprio arquivo;
  - podem ser sobrescritas por variável de ambiente;
  - nunca são usadas em ambiente publicado. O estágio do pipeline que testa o compose num runner
    temporário não é ambiente publicado.

  Origem: clarificação da spec 003 (Marco 3), que escolheu senhas de demonstração para o PostgreSQL e o
  Grafana do compose (FR-033).
- Modified principles: VII. Sem segredos; configuração por variável de ambiente (exceção nova e motivo)
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - AGENTS.md ✅ seção Segurança;
  - specs/003-modo-docker ✅ Clarifications e FR-033 deixam de depender de emenda pendente; o checklist
    foi atualizado;
  - docs/ai/decisoes.md ✅ emenda registrada;
  - specs/001 e specs/002: nenhuma mudança (não usam credenciais);
  - README.md: nenhuma mudança agora. A identificação das credenciais no README entra com o compose
    (spec 003, FR-036), pelo papel de documentação.
- Templates requiring updates: nenhum
- Follow-up TODOs: nenhum

Sync Impact Report (1.5.0)
- Version change: 1.4.1 → 1.5.0
  Justificativa: MINOR. A cláusula do Princípio I sobre `SpringApplication` foi ampliada de forma
  compatível. Antes, ela valia só para os testes de falha de inicialização. Agora vale sempre que o
  teste precisa controlar o ciclo de vida da aplicação real:
  - falha de inicialização (sem mudança);
  - múltiplas execuções;
  - ordem da subida;
  - saída completa da subida.

  Continua proibido usar mocks ou dublês. O resultado é verificado por HTTP real sempre que o
  comportamento é observável por HTTP; a ordem da subida e o próprio log são verificados pelos eventos
  reais e pela saída capturada. Nenhum teste que cumpria a versão anterior deixa de cumprir.
  Origem: revisão do Codex no PR do Marco 2, que apontou o `EtagAcrossRunsIT` e o
  `ReadinessAfterLoadIT` usando `SpringApplication` em execuções bem-sucedidas. O `JsonLogFormatIT` e o
  `TextLogFormatIT`, que a revisão não citou, estavam na mesma situação e ficaram cobertos pelo caso
  "saída completa da subida".
- Modified principles: I. Somente testes de integração no back-end (cláusula de `SpringApplication`)
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - AGENTS.md ✅ regras invioláveis, item dos testes de integração;
  - src/test ✅ javadoc do `EtagAcrossRunsIT`, do `ReadinessAfterLoadIT`, do `JsonLogFormatIT`, do
    `TextLogFormatIT` e do `ApplicationStarter` citando a cláusula;
  - docs/ai/decisoes.md ✅ emenda registrada;
  - specs/002-operacao: nenhuma mudança (o Constitution Check do plano já descrevia esses testes);
  - specs/001-producer-intervals-api: nenhuma mudança (os testes de falha continuam no caso original);
  - ⚠ pendente, papel de documentação: README.md (seção de testes e tabela de rastreabilidade) e
    ADR 0004 ainda dizem que só os testes de falha de inicialização usam `SpringApplication`.
- Templates requiring updates: nenhum
- Follow-up TODOs: README.md e ADR 0004 (acima)

Sync Impact Report (1.4.1)
- Version change: 1.4.0 → 1.4.1
  Justificativa: PATCH. Esclarecimento do Princípio V. O texto anterior listava o ETag entre os
  extras que "MUST ser ativados só pelo seu perfil ou pela sua configuração", mas a intenção do
  princípio (e do briefing, que põe ETag e observabilidade na camada "Extra na API", rodando no
  `./mvnw`) é isolar os extras com infraestrutura própria. O texto passa a separar:
  - extras com infraestrutura própria: só com o seu perfil ou a sua configuração (sem mudança);
  - extras da API sem infraestrutura própria (listagem, Swagger UI, ETag, health, métricas): podem
    ficar ligados no modo padrão, desde que não alterem o comportamento avaliado.

  A autenticação continua desligada por padrão (Princípio VII). Nenhuma regra nova nem removida.
- Modified principles: V. Extras opcionais e isolados por perfil (esclarecimento de redação)
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - specs/002-operacao ✅ o Constitution Check do plano cita o Princípio V 1.4.1;
  - specs/001-producer-intervals-api: nenhuma mudança (listagem e Swagger UI já ficavam ligados no
    modo padrão);
  - AGENTS.md: nenhuma mudança (o isolamento por perfil já falava só de Kafka, PostgreSQL e modo
    Docker);
  - docs/ai/decisoes.md ✅ emenda registrada.
- Templates requiring updates: nenhum
- Follow-up TODOs: nenhum

Sync Impact Report (1.4.0)
- Version change: 1.3.0 → 1.4.0
  Justificativa: MINOR. O Princípio VI foi ampliado de forma compatível: cada requisito aponta para
  a forma de comprovação adequada ao seu tipo.
  - comportamento da aplicação: teste de integração (sem mudança);
  - documentação (README, ADRs e guias): revisão no pull request, com item no checklist da spec;
  - pipeline e imagem (fumaça do container, segurança, cobertura mínima e lint do Dockerfile):
    execução do pipeline no GitHub Actions.

  Todo requisito que cumpria a versão anterior continua cumprindo. Requisito de comportamento
  continua exigindo teste de integração e não pode ser comprovado só por revisão ou pelo pipeline.
- Modified principles: VI. Rastreabilidade requisito → teste (o título não mudou; o texto foi
  ampliado)
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - specs/002-operacao ✅ FR-032 passa a citar o Princípio VI; a seção "Revisão dos ADRs" do
    checklist é a comprovação da FR-030 e da FR-031;
  - specs/001-producer-intervals-api: nenhuma mudança (todos os requisitos são de comportamento e
    continuam com teste de integração);
  - AGENTS.md ✅ regra de rastreabilidade do fluxo de trabalho;
  - docs/ai/decisoes.md ✅ emenda registrada.
- Templates requiring updates: nenhum
- Follow-up TODOs: nenhum

Sync Impact Report (1.3.0)
- Version change: 1.2.0 → 1.3.0
  Justificativa: MINOR. A seção "Restrições técnicas e de segurança" > Observabilidade passa a
  definir onde vale o log em JSON:
  - na execução em container e em ambientes operacionais, ligado por variável de ambiente que a
    imagem define;
  - na execução local padrão (`./mvnw spring-boot:run`), o log continua em texto.

  Nenhum princípio foi alterado. Os probes de liveness e readiness não mudaram. Os testes de log
  passam a valer nos dois formatos.
- Modified sections: "Restrições técnicas e de segurança" > Observabilidade
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - specs/002-operacao ✅ clarificação de 2026-11-23, história 4, FR-017, FR-018, FR-020, FR-024a,
    FR-030 e SC-005;
  - specs/001-producer-intervals-api: nenhuma mudança (o Marco 1 já usa texto);
  - AGENTS.md ✅ linha "Logs" da tabela de stack;
  - docs/ai/decisoes.md ✅ emenda registrada.
- Templates requiring updates: nenhum
- Follow-up TODOs: nenhum

Sync Impact Report (1.2.0)
- Version change: 1.1.0 → 1.2.0 (nesta sessão também 1.0.0 → 1.1.0, observabilidade a partir do Marco 2)
  Justificativa: MINOR. O Princípio I foi reescrito de forma compatível:
  - todo teste que cumpria a versão anterior continua cumprindo;
  - passa a ser explícito o uso de `@SpringBootTest` com o contexto completo, sem mocks;
  - a verificação por log ou banco fica permitida só enquanto o comportamento não tem endpoint;
  - os testes de falha de inicialização ganham uma cláusula própria.

  Nenhum princípio foi removido, e nenhuma regra antes permitida ficou proibida para um teste
  existente.
- Modified principles: I. Somente testes de integração no back-end (o título não mudou; o texto foi
  reescrito)
- Modified sections (1.1.0): "Restrições técnicas e de segurança" > Observabilidade
- Added sections: nenhuma
- Removed sections: nenhuma
- Impacto:
  - specs/001-producer-intervals-api ✅ a FR-027 deixou de ter exceções próprias e só referencia o
    Princípio I; o plano, as tarefas e a research foram alinhados;
  - AGENTS.md ✅ atualizado (regras invioláveis, testes e stack);
  - docs/ai/decisoes.md ✅ as duas emendas foram registradas.
- Templates requiring updates: nenhum (os templates leem este arquivo em tempo de execução)
- Follow-up TODOs: nenhum
-->

# Constituição da API Golden Raspberry Awards (Teste Outsera)

## Core Principles

Os sete princípios abaixo são **inegociáveis**. Uma violação bloqueia o merge.

### I. Somente testes de integração no back-end

- Todo teste do back-end MUST ser de integração:
  - usa `@SpringBootTest` com o contexto completo da aplicação;
  - não usa mocks nem dublês de serviços, repositórios ou banco (por exemplo, `@MockitoBean`).

  Testes unitários MUST NOT existir na API (item 4.2 do enunciado).
- O resultado MUST ser verificado preferencialmente por HTTP, chamando os endpoints expostos.
  - A verificação por log ou banco só é permitida enquanto o comportamento ainda não tem endpoint
    exposto.
  - Quando o endpoint passar a existir, o teste MUST ganhar a verificação por HTTP.
- **Testes que controlam o ciclo de vida da aplicação:** quando o teste precisa controlar o ciclo de
  vida da aplicação real, ele MAY executá-la com `SpringApplication` em vez de `@SpringBootTest`. Os
  casos são:
  - **falha de inicialização:** um contexto que não sobe não pode ser verificado por
    `@SpringBootTest`; o teste verifica a exceção e o log;
  - **múltiplas execuções:** o teste compara o resultado de duas ou mais subidas da aplicação, por
    exemplo com arquivos diferentes;
  - **ordem da subida:** o teste observa, com ouvintes reais dos eventos do ciclo de vida, a ordem em
    que as etapas da subida acontecem;
  - **saída completa da subida:** o teste captura o log desde a primeira linha, num formato que ele
    controla.

  Esses testes MUST continuar sem mocks nem dublês: sobem a aplicação completa. O resultado MUST ser
  verificado por chamadas HTTP reais sempre que o comportamento for observável por HTTP. A ordem da
  subida e o próprio log, que o HTTP não observa, são verificados pelos eventos reais e pela saída
  capturada.
- Todo comportamento novo ou alterado MUST chegar com o seu teste de integração, no mesmo bolt.
- O resultado de referência com o arquivo fornecido MUST ser testado: Joel Silver em `min`
  (1990–1991) e Matthew Vaughn em `max` (2002–2015).
- Toda falha encontrada pelo testador adversário MUST virar teste de integração permanente.
- Testes unitários são permitidos apenas no front-end, que está fora do escopo do enunciado, comprovados
  conforme o Princípio VI.

**Motivo:**
- É uma exigência explícita do enunciado, e os testes de ponta a ponta são os que provam o contrato.
- A verificação por log ou banco existe para que um comportamento seja testado no mesmo bolt em que
  nasce, mesmo antes de ter endpoint (por exemplo, a carga do arquivo), sem recorrer a teste
  unitário.

### II. Execução só com ./mvnw

- A aplicação MUST subir com `./mvnw spring-boot:run`, e os testes de todo comportamento do **modo
  padrão** MUST passar com `./mvnw verify`, numa única execução do Failsafe, sem Docker e sem nenhuma
  instalação além do JDK (item 4.3 do enunciado). O modo padrão é o que roda sem nenhuma configuração:
  H2, a carga do CSV com todas as suas regras (inclusive as acrescentadas pelos extras), os endpoints, o
  Swagger e o que vem ligado por padrão.
- Os testes do que vem **desligado por padrão** (Kafka, autenticação ligada, log em JSON e
  observabilidade que dependa de configuração) MUST passar com `./mvnw -Pextras verify`, que roda também
  os do modo padrão, igualmente sem Docker e só com o JDK. O pipeline MUST executar esse perfil no job
  `build`, com o job vermelho reprovando o merge.
- O modo padrão MUST usar H2 em memória e carregar o CSV fornecido como padrão.
- Os testes de Kafka MUST usar o broker embutido do Spring, e não Testcontainers. Os testes que
  dependem de PostgreSQL ficam em um perfil Maven separado, fora do `./mvnw verify` padrão.
- O front-end (`frontend/`) fica fora do `./mvnw`: nenhum comando Maven MUST exigir Node nem construir o
  front, e o front MUST NOT ser pré-requisito para subir a API ou rodar os testes dela.

**Motivo:** o avaliador executa só o modo padrão. Se ele falha, a entrega inteira falha.

### III. Contrato de intervalos idêntico ao enunciado

- `GET /api/producers/intervals` MUST devolver exatamente o formato da página 2 do PDF:
  `{ "min": [...], "max": [...] }`, com itens `producer`, `interval`, `previousWin` e
  `followingWin`. Nenhum campo pode ser adicionado, renomeado ou removido.
- `min` e `max` MUST conter todos os empatados, ordenados por `previousWin` crescente e depois
  pelo nome. Sem produtor com duas vitórias, as duas listas MUST vir vazias, com status 200.
- Os intervalos MUST ser calculados só entre vitórias vizinhas de um mesmo produtor, depois de
  ordenar os anos. Duas vitórias no mesmo ano resultam em intervalo 0.
- Os nomes de produtores MUST ser separados por vírgula e por ` and ` (inclusive `, and`) e
  comparados exatamente como escritos, sem normalizar a grafia.
- O contrato OpenAPI MUST descrever esse formato e MUST validar as respostas nos testes.

**Motivo:** a correção usará outros conjuntos de dados, provavelmente de forma automatizada.
Qualquer desvio reprova a entrega.

### IV. Spec-anchored: a spec é a fonte da verdade

- O trabalho MUST seguir Spec-Driven Development com o GitHub Spec Kit. A spec em `specs/` é a
  fonte da verdade do comportamento.
- Quando um comportamento muda, a spec MUST ser atualizada antes do código e dos testes. Código
  que diverge da spec é defeito, mesmo que os testes passem.
- A spec evolui a cada ciclo (bolt). Não se escreve a especificação inteira de uma vez.
- Uma ambiguidade sem decisão registrada MUST NOT ser resolvida por um agente sozinho. Ela é
  levada ao responsável e registrada na spec ou em `docs/ai/decisoes.md`.

**Motivo:** uma única fonte da verdade, que evolui a cada ciclo, mantém código, testes e
documentação coerentes e evita o modelo cascata.

### V. Extras opcionais e isolados por perfil

- Os extras são de dois tipos:
  - **com infraestrutura própria** (PostgreSQL, Kafka, Prometheus e Grafana em execução, front-end,
    imagem e deploy): são opcionais e MUST ser ativados só pelo seu perfil ou pela sua configuração;
  - **da API, sem infraestrutura própria** (por exemplo, listagem, Swagger UI, ETag, health e
    métricas): MAY ficar ligados no modo padrão, desde que não alterem o comportamento avaliado. O
    contrato de intervalos, os resultados e os testes do enunciado continuam idênticos com eles ligados.
- A autenticação é ligada só pela sua configuração e fica desligada por padrão (Princípio VII).
- Nenhum extra pode quebrar a subida ou os testes do modo padrão. Kafka e PostgreSQL MUST NOT ser
  configurados nem exigidos sem o perfil ativo.
- A regra de registrar um filme MUST existir em um único serviço. O perfil muda só a origem dos
  dados (carga direta ou consumidor Kafka).
- Um extra só entra na entrega se estiver completo. O marco 1 nunca é reduzido.
- Complexidade sem necessidade comprovada MUST NOT ser adicionada (por exemplo, imagem nativa,
  tracing distribuído ou Kubernetes). Toda exceção MUST ser justificada em um ADR.

**Motivo:** os extras mostram o perfil full stack sem pôr em risco o que o enunciado avalia.

### VI. Rastreabilidade requisito → teste

- Cada requisito da spec MUST apontar para a forma de comprovação adequada ao seu tipo:
  - **comportamento da aplicação:** teste de integração, conforme o Princípio I. Cada teste MUST
    indicar o requisito que cobre;
  - **documentação** (README, ADRs e guias): revisão no pull request, com um item no checklist da
    spec que diga o que o revisor confere;
  - **pipeline e imagem** (teste de fumaça do container, análise de segurança, cobertura mínima e lint
    do Dockerfile): execução do pipeline no GitHub Actions, com o estágio que comprova cada requisito
    indicado na spec ou no plano;
  - **comportamento do front-end:** testes unitários, com cobertura mínima de 80% das linhas, e teste de ponta
    a ponta contra a aplicação real (a API do modo padrão e o front servido como na publicação, sem respostas
    simuladas), executados no pipeline, com o job vermelho reprovando o merge. Um requisito de comportamento da
    API continua exigindo IT e MUST NOT ser comprovado só pelos testes do front.
- Requisito sem comprovação não é considerado entregue. Um requisito de comportamento da aplicação
  MUST NOT ser comprovado só por revisão ou só pelo pipeline: ele precisa do seu teste de integração no
  `./mvnw verify`, se for do modo padrão, ou no `./mvnw -Pextras verify`, se depender de algo desligado
  por padrão (Princípio II). Os dois rodam sem Docker, e o pipeline executa o segundo obrigatoriamente.
- A **cobertura mínima** de linhas dos testes de integração (90%) é verificada no `./mvnw -Pextras
  verify`, sobre a soma das execuções do Failsafe, e o pipeline a exige no job `build`.
- **Comportamento que depende de infraestrutura indisponível no modo padrão** (por exemplo, o
  PostgreSQL): é comprovado por testes de integração num **perfil Maven dedicado**, que o pipeline MUST
  executar obrigatoriamente, com o job vermelho reprovando o merge. Esses testes seguem o Princípio I
  (contexto completo, sem mocks, verificação por HTTP). A exceção vale só para o que não pode rodar sem
  essa infraestrutura. Todo o resto continua exigindo o teste sem Docker (`./mvnw verify` ou
  `./mvnw -Pextras verify`), inclusive o
  comportamento comum aos dois modos, que é testado com H2. Uma dependência que tem substituto
  embutido sem Docker, como o broker Kafka do Spring, não entra na exceção (Princípio II).
- O README MUST trazer a tabela de requisitos do enunciado, com o teste que comprova cada um.

**Motivo:**
- O avaliador consegue conferir cada exigência do enunciado sem ler o código.
- Documentos, pipeline e imagem não têm comportamento que um teste de integração no `./mvnw verify`
  consiga exercitar: o conteúdo de um ADR só uma leitura avalia, e o container e o pipeline só existem
  no GitHub Actions, fora do modo padrão (Princípio II). Exigir teste de integração para eles levaria a
  testes de fachada ou a deixar os requisitos sem comprovação.

### VII. Sem segredos; configuração por variável de ambiente

- Tokens, senhas e credenciais reais MUST NOT aparecer no código, nos commits, na evidência de IA
  nem nas mensagens aos agentes. Há só duas exceções:
  - o token de demonstração documentado no README;
  - as **credenciais de demonstração dos serviços do compose de desenvolvimento local** (por exemplo,
    o usuário e a senha do PostgreSQL e do Grafana), desde que cumpram todas as condições abaixo:
    - aparecem só nos arquivos do compose de desenvolvimento local e na configuração que ele monta;
    - são identificadas como de demonstração no README e no próprio arquivo onde aparecem;
    - podem ser sobrescritas por variável de ambiente;
    - MUST NOT ser usadas em ambiente publicado, como o deploy em nuvem. O estágio do pipeline que sobe
      esse mesmo compose num runner temporário, para testá-lo, não é ambiente publicado.
- Nenhuma outra credencial de demonstração é permitida. Um ambiente publicado MUST receber as suas
  credenciais por variável de ambiente ou pelo gerenciador de segredos da plataforma.
- Toda configuração MUST vir de variáveis de ambiente, inclusive a porta (`PORT`), o caminho do
  CSV, o token e o CORS, com padrões que fazem o modo padrão funcionar sem nada definido.
- A autenticação MUST ficar desligada por padrão: sem a variável do token, os endpoints de negócio
  ficam públicos.

**Motivo:**
- O repositório e toda a evidência de IA serão públicos.
- O compose de desenvolvimento local precisa subir com um único comando, num clone limpo. Uma senha de
  demonstração, identificada como tal e restrita à máquina de quem executa, não protege nada de valor, e
  exigir um arquivo de segredos antes do `docker compose up` só criaria um passo a mais.

## Restrições técnicas e de segurança

- **Stack:** Java 25 (LTS), Spring Boot (versão estável atual), Maven com wrapper versionado, JPA
  e Apache Commons CSV.
- **Esquema:** versionado por Flyway, comum a H2 e a PostgreSQL. O SQL MUST ser portável.
- **CSV:** separador `;` e cabeçalho `year;title;studios;producers;winner`. A coluna `winner` com
  `yes` marca vencedor, sem diferenciar maiúsculas e ignorando espaços. Arquivo ausente ou com
  cabeçalho inválido MUST interromper a inicialização com mensagem clara.
- **Erros:** a API MUST responder em Problem Details (RFC 9457). Parâmetro inválido devolve 400,
  e token ausente ou errado, com a autenticação ligada, devolve 401.
- **Observabilidade:** valem **a partir do marco de observabilidade (Marco 2)**:
  - os probes de liveness e readiness, com a readiness positiva só depois do fim da carga dos dados;
  - os logs estruturados em JSON na **execução em container e em ambientes operacionais**. O JSON
    MUST ser ligado por uma variável de ambiente, que a imagem de container MUST definir e que pode
    ligá-lo em qualquer outra execução;
  - na **execução local padrão** (`./mvnw spring-boot:run`, sem a variável), o log MUST continuar no
    texto padrão do framework.

  Até o Marco 2, vale só o log em texto, sem probes.
  - **Motivo:**
    - O Marco 1 se limita ao que o enunciado avalia, e o enunciado não pede nenhum dos dois itens.
    - Antecipá-los exigiria dependências e configuração novas no modo padrão, que é o que o
      avaliador executa.
    - A garantia que a readiness daria já existe no Marco 1: a carga termina antes de o servidor
      aceitar conexões (FR-001 da spec 001).
    - Quem avalia lê o log do `./mvnw` no terminal, onde o texto é legível e o JSON não é. O JSON
      serve a coletores de log, que só existem na execução em container e nos ambientes operacionais.
  - Os testes que verificam mensagens de log MUST passar no formato texto, e as mesmas verificações
    MUST passar com o JSON ligado.
- **Cadeia de entrega:** as actions do pipeline MUST ser fixadas por hash de commit, nunca por
  tag. A imagem MUST ser multi-stage e rodar com usuário não root.

## Fluxo de desenvolvimento e portões de qualidade

- Cada unidade de trabalho é um bolt, feito em um branch próprio. Commits diretos na `main` MUST
  NOT acontecer.
- O bolt segue esta ordem:
  1. O implementador escreve o teste de integração e o código.
  2. O responsável abre o pull request.
  3. O revisor (Codex) comenta.
  4. O implementador corrige só o que o responsável aceitar.
  5. O merge acontece com o pipeline verde, e a sessão é exportada.
- Pull request, merge e tags são feitos só pelo responsável, nunca por um agente.
- No máximo três agentes trabalham ao mesmo tempo, cada um na sua cópia de trabalho. Cada arquivo
  compartilhado tem um único dono por rodada.
- O testador adversário MUST criar os casos a partir da spec, sem ler o código de produção.
- **Evidência de IA (item 5.2):**
  - As mensagens aos agentes são gravadas com data e hora em `docs/ai/prompts.md`, pelo hook, sem
    edição manual.
  - As sessões são exportadas para `docs/ai/sessoes/`.
  - As sugestões aceitas ou rejeitadas são registradas, com o motivo, em `docs/ai/decisoes.md`.
  - Dados pessoais MUST NOT entrar nessa evidência.
- **Portão de merge:** os princípios I a VII estão atendidos, `./mvnw verify` e `./mvnw -Pextras
  verify` estão verdes sem Docker, os jobs do front-end estão verdes no pipeline e as decisões estão
  registradas.

## Governance

- Esta constituição prevalece sobre as demais práticas do projeto. Só o enunciado da Outsera tem
  precedência sobre ela. O `AGENTS.md` é o guia de execução para os agentes e MUST permanecer
  coerente com ela.
- Uma emenda MUST ser feita por pull request, aprovada pelo responsável e registrada em
  `docs/ai/decisoes.md`, com o impacto em specs, planos e tarefas existentes.
- O versionamento segue SemVer:
  - MAJOR: remoção ou redefinição incompatível de um princípio.
  - MINOR: princípio ou seção nova, ou orientação ampliada de forma material.
  - PATCH: esclarecimento ou ajuste de redação.
- Todo plano MUST passar pelo "Constitution Check". Toda revisão de pull request MUST verificar a
  conformidade com os princípios I a VII. Os sete princípios são inegociáveis e não admitem
  exceção em "Complexity Tracking". Qualquer outra divergência MUST ser justificada no plano.

**Version**: 2.1.0 | **Ratified**: 2026-11-18 | **Last Amended**: 2026-11-26