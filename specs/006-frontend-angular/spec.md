# Especificação: Marco 6 — Front-end em Angular, com painel e lista de filmes

**Branch**: `marco/6-frontend`

**Criada em**: 2026-11-26

**Status**: Rascunho

**Entrada**: descrição do responsável: "Marco 6: front-end em Angular, num único bolt. Duas telas: Dashboard com quatro painéis (anos com mais de um vencedor; os 3 estúdios com mais vitórias; produtores com o maior e o menor intervalo, pelo endpoint existente; vencedores de um ano buscado) e Lista de filmes paginada, com filtro por ano e por vencedor. O back-end ganha os endpoints de agregação que os painéis precisarem, com ITs, sem alterar os existentes. O código fica em frontend/, com a versão estável mais recente do Angular; o ./mvnw verify do modo padrão não muda e não exige Node. Testes unitários no front (emenda da constituição para o front-end) e um teste de ponta a ponta no pipeline contra a aplicação real. Publicação num segundo serviço do Cloud Run (awards-web), com nginx servindo o Angular e repassando /api ao serviço awards, reaproveitando a identidade federada do Marco 5, sem segredos, max-instances=1. O README ganha a URL do front e a seção do Marco 6."

## Esclarecimentos

### Sessão 2026-11-26

- P: O front entra também no `compose.yaml` do Marco 3, como pede o briefing? → R: Não (opção B). O front
  fica só na publicação no `awards-web` e no desenvolvimento local do `frontend/`; o desvio do briefing é
  registrado em `docs/ai/decisoes.md`. FR-031 e FR-034.

## Cenários de uso e testes *(obrigatória)*

### História 1 - Avaliador vê o painel com os quatro resumos (Prioridade: P1)

O avaliador abre a URL do front publicada no README e cai no **Dashboard** (na interface, a tela e o item do
menu se chamam "Painel"; contracts/frontend-ui.md). Sem digitar nada, vê três painéis
preenchidos com os dados do arquivo carregado: os anos com mais de um vencedor, os três estúdios com mais
vitórias e os produtores com o maior e o menor intervalo entre vitórias. No quarto painel, digita um ano e vê
os filmes vencedores daquele ano.

**Por que esta prioridade**: é o valor do marco. O painel mostra, numa tela, o que a API calcula, inclusive o
resultado do enunciado.

**Teste independente**: com a aplicação real carregando o arquivo fornecido, o Dashboard mostra 1986, 1990 e
2015 (2 vencedores cada); Columbia Pictures (7), Paramount Pictures (6) e Warner Bros. (5); Joel Silver no
menor intervalo (1, 1990–1991) e Matthew Vaughn no maior (13, 2002–2015); e, com a busca por 1990, os
vencedores "The Adventures of Ford Fairlane" e "Ghosts Can't Do It".

**Cenários de aceitação**:

1. **Dado** a aplicação com o arquivo fornecido, **Quando** o avaliador abre o Dashboard, **Então** o painel de
   anos lista 1986, 1990 e 2015, em ordem crescente, cada um com 2 vencedores.
2. **Dado** a mesma aplicação, **Quando** o Dashboard carrega, **Então** o painel de estúdios mostra exatamente
   três linhas: Columbia Pictures (7), Paramount Pictures (6) e Warner Bros. (5).
3. **Dado** a mesma aplicação, **Quando** o Dashboard carrega, **Então** o painel de intervalos mostra, em duas
   tabelas, todos os produtores do `max` e do `min` devolvidos pela API, com produtor, intervalo, vitória
   anterior e seguinte: Matthew Vaughn (13, 2002, 2015) e Joel Silver (1, 1990, 1991).
4. **Dado** o Dashboard, **Quando** o avaliador busca o ano 1990, **Então** o painel mostra os dois vencedores
   do ano, com ano, título, estúdios e produtores.
5. **Dado** o Dashboard, **Quando** o avaliador busca um ano sem vencedor (por exemplo, 1979), **Então** o
   painel informa que não há vencedor naquele ano, sem erro.
6. **Dado** o Dashboard, **Quando** o avaliador tenta buscar um valor que não é ano (vazio, letras, negativo),
   **Então** a busca não é enviada e o campo mostra a mensagem de validação.
7. **Dado** um arquivo em que nenhum produtor venceu duas vezes, **Quando** o Dashboard carrega, **Então** o
   painel de intervalos informa que não há dados suficientes, sem erro.

---

### História 2 - Avaliador navega pela lista de filmes com filtros (Prioridade: P1)

O avaliador abre a tela **Lista de filmes**, vê a tabela paginada com todos os filmes do arquivo, ordenada por
ano e título, e filtra por ano e por vencedor (todos, só vencedores, só não vencedores). Os filtros e a página
atual ficam na URL, de modo que recarregar ou compartilhar o link mostra a mesma página.

**Por que esta prioridade**: é a segunda tela pedida e a única forma de ver os dados brutos sem o Swagger UI.

**Teste independente**: com o arquivo fornecido, a lista mostra o total de filmes do arquivo, a navegação entre
páginas muda os itens, o filtro "1990" + "vencedores" mostra os dois vencedores de 1990, e o link copiado abre
o mesmo resultado.

**Cenários de aceitação**:

1. **Dado** a aplicação com o arquivo fornecido, **Quando** a lista abre, **Então** mostra a primeira página,
   com ano, título, estúdios, produtores e se venceu, e o total de filmes igual ao do arquivo.
2. **Dado** a lista, **Quando** o avaliador vai para a próxima, a anterior, a primeira ou a última página,
   **Então** os itens mudam conforme a ordem por ano e título, e os botões sem destino ficam desabilitados.
3. **Dado** a lista, **Quando** o avaliador filtra pelo ano 1990 e por "vencedores", **Então** só os dois
   vencedores de 1990 aparecem, e a paginação volta para a primeira página.
4. **Dado** um filtro que não encontra nenhum filme, **Quando** a lista atualiza, **Então** mostra "nenhum filme
   encontrado", sem erro.
5. **Dado** um filtro de ano inválido, **Quando** o avaliador o digita, **Então** a consulta não é enviada e o
   campo mostra a mensagem de validação.
6. **Dado** uma URL da lista com filtros e página, **Quando** o avaliador a abre direto, **Então** a tela mostra
   aquele filtro e aquela página.

---

### História 3 - Front usável no celular e no desktop (Prioridade: P2)

O avaliador abre o front no celular ou numa janela estreita e consegue ler os painéis e a lista sem rolagem
horizontal: no celular, os painéis ficam empilhados e as tabelas viram cartões; no desktop, ficam em grade e em
tabela.

**Por que esta prioridade**: o briefing pede layout responsivo; sem ele, a demonstração pública só funciona no
desktop.

**Teste independente**: o teste de ponta a ponta roda as jornadas principais também numa largura de celular e
confere que não há rolagem horizontal e que os dados aparecem.

**Cenários de aceitação**:

1. **Dado** uma tela de 360 px de largura, **Quando** o avaliador abre o Dashboard e a lista, **Então** todo o
   conteúdo cabe na largura, sem rolagem horizontal, e as linhas viram cartões.
2. **Dado** uma tela de desktop, **Quando** o avaliador abre o Dashboard, **Então** os quatro painéis aparecem
   em grade, e a lista em tabela.
3. **Dado** qualquer tela, **Quando** o avaliador usa só o teclado, **Então** alcança a navegação, os filtros, a
   busca e a paginação, com foco visível.

---

### História 4 - A API ganha as agregações do painel (Prioridade: P1)

Quem integra com a API (inclusive o front) consulta duas agregações novas: os anos com mais de um vencedor e a
contagem de vitórias por estúdio. Elas seguem o mesmo padrão dos endpoints existentes (contrato OpenAPI, ETag,
Problem Details, autenticação opcional) e não mudam nada nos endpoints de intervalos e de filmes.

**Por que esta prioridade**: sem as agregações, dois painéis não têm de onde vir os dados; calcular no navegador a
partir da lista exigiria baixar todos os filmes.

**Teste independente**: os ITs das agregações sobem a aplicação com o arquivo fornecido e com CSVs de cenário e
conferem as respostas por HTTP, validadas pelo contrato.

**Cenários de aceitação**:

1. **Dado** a aplicação com o arquivo fornecido, **Quando** se consulta a agregação de anos, **Então** a
   resposta traz 1986, 1990 e 2015, em ordem crescente, cada um com a contagem 2, e nenhum ano com um só
   vencedor.
2. **Dado** a mesma aplicação, **Quando** se consulta a agregação de estúdios, **Então** a resposta traz todos os
   estúdios com pelo menos uma vitória, ordenados pela contagem decrescente e depois pelo nome, começando por
   Columbia Pictures (7), Paramount Pictures (6) e Warner Bros. (5).
3. **Dado** um filme vencedor com mais de um estúdio separado por vírgula, **Quando** se consulta a agregação de
   estúdios, **Então** a vitória conta uma vez para cada estúdio, com o nome sem espaços nas pontas.
4. **Dado** um arquivo sem vencedores, **Quando** se consultam as duas agregações, **Então** as listas vêm vazias,
   com status 200.
5. **Dado** a autenticação ligada, **Quando** se consultam as agregações sem token, **Então** a resposta é 401
   em Problem Details, como nos demais endpoints de negócio.
6. **Dado** uma resposta já recebida, **Quando** a mesma consulta é repetida com o ETag, **Então** a resposta é
   304, como nos demais endpoints de negócio.

---

### História 5 - Cada merge na main publica o front sozinho, conferido (Prioridade: P2)

Depois de publicar e conferir a API (Marco 5), o pipeline publica o front num segundo serviço, `awards-web`,
com a mesma identidade federada e sem nenhum segredo. O serviço serve as telas e repassa as chamadas de `/api`
ao serviço `awards`, de modo que o navegador fala com uma única origem. A revisão nova sobe sem tráfego, é
conferida e só então recebe o tráfego.

**Por que esta prioridade**: a URL pública do front é o que o avaliador abre; sem a publicação automática, ela
envelhece a cada merge.

**Teste independente**: um merge na `main` produz uma execução em que a publicação do front fica verde e a
conferência pela URL do front encontra a tela e o resultado de referência pelo `/api` repassado.

**Cenários de aceitação**:

1. **Dado** um merge na `main` com todos os checks verdes e a API já conferida, **Quando** o pipeline termina,
   **Então** o `awards-web` roda uma revisão nova com a imagem do front testada naquele commit.
2. **Dado** a revisão nova do front sem tráfego, **Quando** a conferência roda pela URL própria dela, **Então**
   a página inicial responde e `GET /api/producers/intervals` por ela devolve o resultado de referência.
3. **Dado** a conferência reprovada, **Quando** o job termina, **Então** o tráfego continua na revisão anterior
   do front, e o job fica vermelho com o motivo no resumo.
4. **Dado** um pull request ou outra branch, **Quando** o pipeline roda, **Então** nada é publicado.
5. **Dado** o avaliador numa rota interna do front (por exemplo, a lista), **Quando** recarrega a página,
   **Então** a mesma tela abre, sem 404.

---

### História 6 - O README apresenta o front (Prioridade: P3)

O README ganha a URL pública do front em "Avalie em 2 minutos" e a seção do Marco 6 entre os extras: as duas
telas, as agregações novas, como rodar o front localmente, como são os testes e como é a publicação.

**Por que esta prioridade**: documentação, comprovada por revisão.

**Teste independente**: revisão do README no pull request, com o item no checklist da spec.

**Cenários de aceitação**:

1. **Dado** o README, **Quando** o avaliador lê "Avalie em 2 minutos", **Então** encontra a URL do front, com o
   aviso da partida a frio.
2. **Dado** a seção do Marco 6, **Quando** o avaliador a lê, **Então** sabe rodar o front localmente contra a API
   do `./mvnw spring-boot:run` e rodar os testes do front, e entende que o `./mvnw` não depende do front.

---

### Casos de borda

- **API indisponível ou lenta** (por exemplo, partida a frio do `awards`): cada painel mostra um estado de
  carregamento e, se a chamada falha, uma mensagem de erro com opção de tentar de novo, sem derrubar os outros
  painéis.
- **Partida a frio dos dois serviços:** a primeira abertura pode demorar alguns segundos; o front mostra o
  carregamento, e a conferência do pipeline tolera a demora.
- **Empate na terceira posição dos estúdios:** o painel mostra só três, pela ordem da API (contagem
  decrescente e depois nome); os empatados de fora aparecem só na API.
- **Menos de três estúdios vencedores:** o painel mostra os que houver; sem nenhum, informa que não há dados.
- **Empates nos intervalos:** o painel mostra todos os empatados, como a API devolve (Princípio III).
- **Produtor com nome longo ou muitos estúdios:** o texto quebra linha, sem estourar a largura no celular.
- **Página além da última na URL** (por exemplo, `page=999`): a lista mostra "nenhum filme encontrado" e
  permite voltar à primeira página.
- **Parâmetro inválido na URL da lista** (ano com letras, vencedor diferente de sim/não): o front ignora o
  parâmetro inválido e mostra o filtro vazio, sem chamar a API com ele.
- **Autenticação ligada na API:** fora do escopo do front; sem token, os painéis mostram a mensagem de erro do painel,
  com "tentar de novo", como em qualquer falha da API (FR-009). A
  publicação mantém a autenticação desligada (Marco 5).
- **Estúdio vazio num vencedor:** não conta para nenhum estúdio.
- **Mesmo estúdio repetido num filme:** conta uma vez para aquele filme.
- **Rota inexistente no front:** redireciona para o Dashboard.

## Requisitos *(obrigatória)*

### Requisitos funcionais

**Agregações na API**

- **FR-001**: A API MUST ter uma agregação dos **anos com mais de um vencedor**: cada item traz o ano e a
  quantidade de vencedores, só para anos com dois ou mais, em ordem crescente de ano.
- **FR-002**: A API MUST ter uma agregação das **vitórias por estúdio**: cada item traz o nome do estúdio e a
  quantidade de filmes vencedores em que ele aparece, só para estúdios com pelo menos uma vitória, ordenados
  pela quantidade decrescente e depois pelo nome. O campo de estúdios é separado por vírgula, com espaços nas
  pontas removidos; nomes vazios são descartados, e um estúdio repetido no mesmo filme conta uma vez. Os nomes
  são comparados exatamente como escritos, sem normalizar a grafia, como os produtores.
- **FR-003**: As duas agregações MUST devolver as listas vazias, com status 200, quando não houver dados, e
  MUST seguir o padrão dos endpoints de negócio existentes: ETag com 304, 401 em Problem Details com a
  autenticação ligada, e nenhum parâmetro obrigatório.
- **FR-004**: O contrato OpenAPI MUST passar à versão **1.3.0**, com o arquivo-fonte em
  `specs/006-frontend-angular/contracts/openapi.yaml` e a cópia publicada idêntica a ele, descrevendo as duas
  agregações com esquemas fechados (`additionalProperties: false`). Os paths, os parâmetros, as respostas e os
  esquemas existentes MUST NOT mudar (Princípio III).
- **FR-005**: `GET /api/producers/intervals` e `GET /api/movies` MUST continuar idênticos. Os ITs existentes
  MUST NOT mudar, exceto o número da versão do contrato nos ITs que já o conferem, com exceção registrada em
  `docs/ai/decisoes.md` (`check-unchanged-tests.sh`).
- **FR-006**: O painel de vencedores de um ano MUST usar a listagem existente (`year` e `winner=true`), sem
  endpoint novo.

**Dashboard**

- **FR-007**: O Dashboard MUST ser a tela inicial e mostrar quatro painéis independentes:
  - anos com mais de um vencedor (ano e quantidade), pela FR-001;
  - os **três primeiros** estúdios da FR-002 (nome e quantidade);
  - produtores com o **maior** e o **menor** intervalo, em duas tabelas com produtor, intervalo, vitória
    anterior e seguinte, com **todos** os itens de `max` e de `min` devolvidos por `GET /api/producers/intervals`;
  - vencedores de um ano buscado (ano, título, estúdios e produtores), pela FR-006.
- **FR-008**: A busca por ano MUST aceitar só um número inteiro não negativo; um valor inválido MUST NOT gerar
  chamada à API e MUST mostrar a mensagem de validação. Um ano sem vencedor MUST mostrar a mensagem de lista
  vazia.
- **FR-009**: Cada painel MUST ter estado de carregamento, de vazio e de erro próprios, com opção de tentar de
  novo; a falha de um painel MUST NOT impedir os outros de aparecer.

**Lista de filmes**

- **FR-010**: A Lista de filmes MUST mostrar os filmes pela listagem existente, na ordem dela (ano e título),
  com ano, título, estúdios, produtores e se venceu, **10 por página**, e o total de filmes encontrados.
- **FR-011**: A lista MUST ter filtro por ano (inteiro não negativo, opcional) e por vencedor (todos, sim,
  não). Mudar um filtro MUST voltar à primeira página. Um ano inválido MUST NOT gerar chamada à API.
- **FR-012**: A lista MUST ter navegação para a primeira, a anterior, a próxima e a última página e mostrar a
  página atual e o total de páginas; os botões sem destino ficam desabilitados.
- **FR-013**: Os filtros e a página MUST ficar na URL da tela; abrir essa URL MUST reproduzir a mesma consulta.
  Parâmetros inválidos na URL MUST ser ignorados.
- **FR-014**: Sem resultado, a lista MUST mostrar "nenhum filme encontrado", e o erro da API MUST aparecer com
  opção de tentar de novo.

**Aparência e acessibilidade**

- **FR-015**: O layout MUST ser responsivo: a partir de 360 px de largura, sem rolagem horizontal, com painéis
  empilhados e linhas em cartões no celular, e painéis em grade e listas em tabela no desktop.
- **FR-016**: As telas MUST ser navegáveis só pelo teclado, com foco visível, rótulos nos campos e tabelas com
  cabeçalho, e o menu MUST indicar a tela atual.
- **FR-017**: Os textos da interface MUST estar em português.
- **FR-018**: O front MUST chamar a API por caminho relativo (`/api/...`), sem URL fixa no código: o servidor
  do front repassa as chamadas ao serviço da API, e o navegador fala com uma única origem. A API MUST NOT
  precisar de CORS para o front funcionar.

**Projeto do front e modo padrão**

- **FR-019**: O código do front MUST ficar em `frontend/`, com a versão estável mais recente do Angular na data
  do plano, fixada no projeto (lockfile versionado).
- **FR-020**: O `./mvnw verify` e o `./mvnw spring-boot:run` MUST NOT exigir Node nem construir o front. Os ITs
  novos das agregações entram no `./mvnw verify`, porque são do modo padrão (Princípio II), e a cobertura
  mínima de 90% continua valendo no `./mvnw -Pextras verify`.
- **FR-021**: O front MUST poder rodar localmente contra a API do `./mvnw spring-boot:run`, com repasse de
  `/api` no servidor de desenvolvimento, sem mudar nada na API.

**Testes do front**

- **FR-022**: O front MUST ter testes **unitários** de componentes e serviços, cobrindo a validação dos campos,
  os estados de carregamento, vazio e erro, o corte dos três estúdios, a paginação e a leitura dos filtros da
  URL, com cobertura mínima de **80%** de linhas. Eles rodam no pipeline, não no `./mvnw`.
- **FR-023**: O front MUST ter teste de **ponta a ponta** contra a aplicação real (API do modo padrão com o
  arquivo fornecido e o front servido como na publicação, com o repasse de `/api`), sem respostas simuladas,
  cobrindo as US1, US2 e US5-5, inclusive o resultado de referência, numa largura de desktop e numa de
  celular (US3-1).
- **FR-024**: A constituição MUST ganhar emenda MINOR (2.1.0) para o front-end, antes do código:
  - o comportamento do front é comprovado por testes unitários e pelo teste de ponta a ponta no pipeline, e
    não por IT (Princípio VI);
  - o front fica fora do `./mvnw` e não pode ser exigido por ele (Princípio II);
  - o Princípio I continua: no back-end, só testes de integração.
  O `AGENTS.md` MUST ser alinhado, e a emenda registrada em `docs/ai/decisoes.md`.

**Pipeline e publicação**

- **FR-025**: O pipeline MUST ter jobs do front para lint, testes unitários com a cobertura mínima, build de
  produção e o teste de ponta a ponta, rodando em todo pull request e na `main`, e MUST construir a imagem do
  front com o mesmo tratamento da imagem da API: multi-stage, usuário não root, lint do Dockerfile, teste de
  fumaça e porta de vulnerabilidades. As actions novas MUST ser fixadas por hash de commit.
- **FR-026**: A imagem do front MUST servir os arquivos estáticos com nginx, devolver a página do Angular para
  as rotas internas (US5-5) e repassar `/api` ao serviço `awards`, cuja URL vem de variável de ambiente. O
  servidor MUST ler a porta de `PORT`.
- **FR-026a**: O log de acesso do nginx MUST sair em JSON no container, ligado por uma variável de ambiente
  que a imagem define (constituição, Observabilidade), com pelo menos data e hora, método, caminho, status,
  bytes, duração e user agent, uma linha por requisição. Sem a variável, o log de acesso fica no texto padrão
  do nginx. O log de erro do nginx não tem formato configurável e continua em texto, limite registrado no ADR
  do marco (FR-033).
- **FR-027**: O front MUST ser publicado num segundo serviço do Cloud Run, `awards-web`, no mesmo projeto e
  região do Marco 5, com no máximo **uma** instância e no mínimo zero, aceitando chamadas anônimas.
- **FR-028**: A publicação do front MUST rodar só na `main`, depois da publicação e da conferência da API, com
  a **mesma identidade federada** do Marco 5 e sem segredos nem chaves. A conta de publicação ganha só a
  permissão de implantar o `awards-web` com uma conta de execução sem papéis no projeto. O script de
  configuração do Marco 5 MUST ser estendido de forma idempotente, com o motivo de cada permissão nova.
- **FR-029**: A revisão nova do front MUST subir sem tráfego e ser conferida pela URL própria: a página inicial
  responde 200 e `GET /api/producers/intervals` por ela devolve o resultado de referência. Só então o tráfego
  passa para ela, e a URL pública do front é conferida de novo. Uma revisão reprovada MUST NOT receber
  tráfego; o resumo da execução registra a URL, o digest, a revisão e o resultado.
- **FR-030**: As publicações do front MUST ser serializadas como as da API (FR-011 do Marco 5). A política do
  workflow (`check-workflows.sh`) MUST permitir as permissões novas só no job de publicação e continuar
  reprovando referência a `secrets.`.
- **FR-031**: O front MUST NOT entrar no `compose.yaml` do Marco 3: ele roda só pela publicação no
  `awards-web` e pelo desenvolvimento local do `frontend/` (FR-021). O compose, o job `compose` do pipeline e
  o seu dono não mudam. O desvio do briefing ("um container serve o front e encaminha as chamadas para a
  API") MUST ser registrado em `docs/ai/decisoes.md` (FR-034).

**Documentação e decisões**

- **FR-032**: O README MUST trazer a URL pública do front em "Avalie em 2 minutos", com o aviso da partida a
  frio, a seção do Marco 6 entre os extras (telas, agregações, como rodar e testar o front, publicação) e os
  requisitos do Marco 6 na seção de requisitos. O `frontend/` MUST ter um README curto com os comandos.
- **FR-033**: A arquitetura do front (Angular em pasta própria, nginx com repasse de `/api` e mesma origem, sem
  CORS) e a publicação no Cloud Run MUST virar ADR curto em `docs/adr/`, no próximo número livre depois do 0027.
- **FR-034**: As mudanças em relação ao briefing MUST ser registradas em `docs/ai/decisoes.md`: publicação no
  Cloud Run em vez de hospedagem estática (Vercel ou Firebase), mesma origem em vez de CORS por variável e a
  versão do Angular, que sai dos pontos em aberto do `AGENTS.md`, e o front fora do compose (FR-031).

**Comprovação (Princípio VI)**

- **FR-035**: Os requisitos MUST ser comprovados assim:
  - FR-001 a FR-006: ITs novos das agregações no `./mvnw verify` (arquivo fornecido e CSVs de cenário em
    `src/test/`), com o resultado validado pelo contrato; o caso com autenticação ligada no
    `./mvnw -Pextras verify`; o `ApiDocsIT` com o contrato 1.3.0;
  - FR-007 a FR-018 e FR-021: testes unitários e de ponta a ponta do front, no pipeline;
  - FR-019, FR-020, FR-022 a FR-030: execução do pipeline (jobs do front, imagem, publicação e conferência) e
    conferência da URL pública depois do merge; os dois comandos Maven verdes sem Node;
  - FR-024 e FR-031 a FR-034: revisão no PR, com item no checklist da spec;
  - SC-003: revisão no PR, com o item de usabilidade do checklist de revisão (`checklists/revisao.md`), com
    critérios objetivos, e não por uma ferramenta nova.

### Entidades principais

- **Ano com mais de um vencedor**: ano e quantidade de filmes vencedores naquele ano (≥ 2).
- **Vitórias por estúdio**: nome do estúdio, como escrito no arquivo, e quantidade de filmes vencedores em que
  ele aparece.
- **Intervalo de produtor**: o item do contrato do PDF (produtor, intervalo, vitória anterior e seguinte), sem
  mudança.
- **Filme**: o item da listagem existente (ano, título, estúdios, produtores, vencedor), sem mudança.
- **Filtro da lista**: ano opcional, vencedor (todos, sim, não) e página, refletidos na URL.
- **Serviço do front**: o `awards-web` no Cloud Run, que serve as telas e repassa `/api` ao serviço `awards`.

## Critérios de sucesso *(obrigatória)*

### Resultados mensuráveis

- **SC-001**: Um avaliador sem nada instalado vê os quatro painéis preenchidos pela URL do README em até 30
  segundos, contando a partida a frio dos dois serviços, e em até 3 segundos com as instâncias ativas.
- **SC-002**: Com o arquivo fornecido, 100% dos valores dos painéis batem com o resultado de referência
  (anos 1986, 1990 e 2015; Columbia Pictures 7, Paramount Pictures 6, Warner Bros. 5; Joel Silver 1 e Matthew
  Vaughn 13; vencedores de 1990).
- **SC-003**: O front passa no item de usabilidade do checklist de revisão do PR, com critérios objetivos
  conferidos pelo revisor:
  - nenhuma rolagem horizontal em 360 px de largura, nas duas telas;
  - todas as ações (menu, busca, filtros e paginação) feitas só pelo teclado, com o foco sempre visível;
  - contraste legível: pelo menos 4,5:1 nos textos e 3:1 nos controles e no contorno de foco (WCAG AA),
    medido no inspetor do navegador;
  - os vencedores de um ano encontrados em até 3 interações, pela busca do Dashboard ou pelos filtros da lista.
- **SC-004**: Nenhuma das telas tem rolagem horizontal entre 360 px e 1920 px de largura.
- **SC-005**: O `./mvnw verify` e o `./mvnw -Pextras verify` continuam verdes sem Node e sem Docker, e as
  respostas de intervalos e de filmes ficam idênticas às de antes do marco.
- **SC-006**: Os testes unitários do front cobrem pelo menos 80% das linhas, e o teste de ponta a ponta passa
  em todo pull request.
- **SC-007**: Depois de cada merge na `main` com o pipeline verde, a URL do front serve o commit mais recente,
  conferida pelo pipeline, sem passo manual e sem nenhum segredo; com o serviço ocioso, zero instâncias.

## Premissas

- O front é público e não envia token: a publicação mantém a autenticação desligada (Marco 5). Com a
  autenticação ligada, o front mostra o erro de acesso; login no front fica fora do escopo.
- O serviço `awards` continua aceitando chamadas anônimas; o nginx do `awards-web` o chama pela URL pública,
  sem identidade do GCP.
- O tamanho de página do front (10) é escolha de apresentação; a listagem da API não muda (padrão 20, máximo
  100). O painel do ano busca até 100 vencedores, mais do que qualquer ano tem.
- O campo de estúdios do arquivo fornecido não usa ` and ` como separador; por isso a FR-002 separa só por
  vírgula, ao contrário dos produtores.
- A versão exata do Angular, a ferramenta de testes unitários e a de ponta a ponta são escolhidas no plano
  (research), na versão estável mais recente.
- O Node necessário fica restrito ao `frontend/` e ao pipeline; o avaliador do core continua só com o JDK.
- Fora do escopo: login no front, internacionalização além do português, gráficos, edição de filmes, SSR,
  PWA, domínio próprio e publicação do front fora do Cloud Run.
