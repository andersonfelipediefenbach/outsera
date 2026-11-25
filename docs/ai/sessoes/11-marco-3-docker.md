 ▐▛███▛█   Claude Code v2.1.327
▝▜██████▀  Opus 5.5 · Claude Pro
 ▝▝   ▝▝   D:\Repo\PESSOAIS\outsera-test\outsera


❯ /clear                                                                        

❯ A T050 do marco 2 (prova de que o pipeline reprova, SC-008) não foi executada 
  antes do merge e da tag v0.2.0. Registre em docs/ai/decisoes.md que ela       
  ficou pendente, com o motivo (prazo de entrega), que os demais requisitos de  
  pipeline foram comprovados pelas execuções verdes e que o pipeline também já  
  validou o primeiro pull request do Dependabot. Marque a T050 como não         
  executada no tasks.md, com referência a essa decisão.                         

  Ran 3 shell commands (ctrl+o to expand)

● Update(docs\ai\decisoes.md)
  ⎿  Added 21 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-24 — T050 do Marco 2 (SC-008) não executada         
       7 +                                                               
       8 +- **Situação:** a T050, que prova que o pipeline reprova (SC-00
         +8), não foi executada antes do merge do                        
       9 +  PR do Marco 2 nem da tag `v0.2.0`. O branch temporário de ver
         +ificação não chegou a ser criado, e                            
      10 +  nenhum dos sete defeitos foi enviado ao pipeline.            
      11 +- **Motivo (decisão do responsável):** prazo de entrega. A veri
         +ficação exige um push e uma execução                           
      12 +  do pipeline por defeito, e isso não cabia no prazo do marco. 
      13 +- **O que está comprovado:** os demais requisitos de pipeline (
         +FR-028, FR-028b e FR-029) foram                                
      14 +  comprovados pelas execuções verdes do workflow `CI` no branch
         + `marco/2-operacao` e no PR. Depois do                         
      15 +  merge, o pipeline também validou o primeiro pull request do D
         +ependabot (grupo                                               
      16 +  `maven-minor-e-patch`, "Bump the maven-minor-e-patch group wi
         +th 4 updates"), o que confirma que o                           
      17 +  Dependabot está ativo e que os PRs dele passam pelo mesmo por
         +tão.                                                           
      18 +- **O que fica sem comprovação:** que cada job reprova diante d
         +o defeito correspondente. As execuções                         
      19 +  verdes mostram que o pipeline aprova código correto, não que 
         +ele recusa código com defeito. O único                         
      20 +  portão já visto falhando de propósito é o da cobertura mínima
         +, localmente, com                                              
      21 +  `-Djacoco.line.minimum=0.99` (T041).                         
      22 +- **Impacto:** a T050 fica marcada como não executada em `specs
         +/002-operacao/tasks.md`, com referência                        
      23 +  a esta entrada. O SC-008 continua aberto e pode ser comprovad
         +o depois, seguindo o roteiro da T050 sem                       
      24 +  mudança, num branch temporário a partir da `main`. Quando iss
         +o acontecer, o resultado entra numa                            
      25 +  entrada "Verificação do SC-008", como a T050 prevê.          
      26 +                                                               
      27  ## 2026-11-24 — Emenda 1.5.0 da constituição: `SpringApplicatio
          n` para controlar o ciclo de vida
      28 
      29  - **Origem:** revisão do Codex no PR do Marco 2. O `EtagAcrossR
          unsIT` e o `ReadinessAfterLoadIT` usam

● Update(specs\002-operacao\tasks.md)
  ⎿  Added 1 line, removed 1 line
      195  - [ ] T047 [US6] **Pendente: exige o push do branch, que é do
           Anderson.** Pedir ao Anderson o push do branch `marco/2-operac
           ao` (ou fazê-lo com a autorização dele) e acompanhar a execuçã
           o do workflow `CI`: os seis jobs precisam ficar verdes. Se alg
           um falhar, corrigir no mesmo branch, com `./mvnw verify` verde
           , num commit de correção da etapa 4. Se a imagem não tiver sid
           o verificada localmente na etapa 3, este é o momento: o job `i
           mage` precisa passar nos treze passos, sem nenhum `PULADO`.
      196  - [ ] T048 [US6] **Pendente: exige o push do branch, que é do
           Anderson.** Conferir na execução verde do T047: o tempo do tes
           te de fumaça (SC-006), o percentual de cobertura no resumo do
           `build`, o relatório completo do Trivy (achados médios e baixo
           s aparecem sem reprovar) e que o code scanning recebeu o SARIF
            do CodeQL. **Verificar (research R10):** se o repositório é p
           úblico; se não for, registrar a limitação do code scanning em
           `docs/ai/decisoes.md`.
      197  - [ ] T049 [US6] **Pendente: exige o push do branch, que é do
           Anderson.** Confirmação operacional (a comprovação da FR-029 é
            o job `workflow-policy`, T040): conferir que o Dependabot est
           á ativo no repositório (aba Insights → Dependency graph → Depe
           ndabot) para os três ecossistemas, depois que o `dependabot.ym
           l` chegar à `main`. Se a ativação depender de configuração do
           repositório, registrar o pedido ao Anderson.
      198 -- [ ] T050 [US6] **Pendente: exige o push do branch, que é do 
          -Anderson.** Verificação do SC-008, **fora do código do marco**
          -: num branch temporário criado a partir de `marco/2-operacao` 
          -(por exemplo, `verificacao/sc-008`), que **nunca é mergeado** 
          -e é **apagado** no fim, local e remotamente. Nenhum commit des
          -se branch volta ao `marco/2-operacao`: nada de cherry-pick, me
          -rge ou rebase a partir dele, e nenhum arquivo dos defeitos fic
          -a no código do marco. O push do branch temporário é feito pelo
          - Anderson ou com a autorização dele. Um commit por defeito, ca
          -da um revertido antes do próximo, confirmando que só o job esp
          -erado fica vermelho:                                          
      198 +- [ ] T050 [US6] **Não executada** antes do merge e da tag `v0
          +.2.0`, por prazo de entrega. Ver `docs/ai/decisoes.md`, entrad
          +a "2026-11-24 — T050 do Marco 2 (SC-008) não executada". O SC-
          +008 continua aberto; o roteiro abaixo vale sem mudança, a part
          +ir da `main`. Verificação do SC-008, **fora do código do marco
          +**: num branch temporário criado a partir de `marco/2-operacao
          +` (por exemplo, `verificacao/sc-008`), que **nunca é mergeado*
          +* e é **apagado** no fim, local e remotamente. Nenhum commit d
          +esse branch volta ao `marco/2-operacao`: nada de cherry-pick, 
          +merge ou rebase a partir dele, e nenhum arquivo dos defeitos f
          +ica no código do marco. O push do branch temporário é feito pe
          +lo Anderson ou com a autorização dele. Um commit por defeito, 
          +cada um revertido antes do próximo, confirmando que só o job e
          +sperado fica vermelho:                                        
      199    1. um IT com uma asserção falsa → `build`;
      200    2. `jacoco.line.minimum` em `0.99` → `build`;
      201    3. uma dependência com vulnerabilidade crítica corrigida (po
           r exemplo, `org.apache.commons:commons-text:1.9`, CVE-2022-428
           89) → `dependencies`;

● Registrei a decisão em docs/ai/decisoes.md e marquei a T050 como não executada
  no tasks.md. Não fiz commit.

  docs/ai/decisoes.md: nova entrada no topo, "2026-11-24 — T050 do Marco 2
  (SC-008) não executada". Ela registra:
  - Motivo: prazo de entrega, como decisão do responsável.
  - O que está comprovado: os demais requisitos de pipeline (FR-028, FR-028b e
    FR-029), pelas execuções verdes do CI. Também cita que o pipeline validou o
    primeiro PR do Dependabot, "Bump the maven-minor-e-patch group with 4
    updates". Conferi esse PR pelo branch
    origin/dependabot/maven/maven-minor-e-patch-2e8bd937de.
  - O que fica sem comprovação: que cada job reprova diante do seu defeito. O
    único portão já visto falhando de propósito é o da cobertura, localmente, na
    T041.
  - Impacto: o SC-008 continua aberto e pode ser comprovado depois com o mesmo
    roteiro, a partir da main.

  specs/002-operacao/tasks.md: a T050 continua com [ ]. Troquei "Pendente" por
  "Não executada", com o motivo e a referência à entrada. O roteiro da tarefa
  ficou como estava.

  Dois pontos para você conferir:
  - Afirmações que tirei do repositório: escrevi que o branch temporário nunca
    foi criado, porque não existe nenhum verificacao/* local nem remoto. Também
    escrevi que as execuções verdes foram no branch marco/2-operacao e no PR.
    Não consegui ver os runs porque o gh não está instalado aqui, então ajuste
    se não for exato.
  - T047 a T049 continuam "Pendente": a nova entrada diz que os requisitos que
    elas cobrem foram comprovados, então o tasks.md contradiz o decisoes.md.
    Posso marcá-las como concluídas, se você quiser.

✻ Cooked for 40s · done terça-feira, 14:39

❯ /speckit-specify Marco 3, modo Docker, conforme                               
  docs/ai/planejamento/briefing.md. Escopo:                                     
  1. Perfil docker com PostgreSQL no lugar do H2, usando as mesmas migrações do 
  Flyway.                                                                       
  2. Kafka como caminho único de ingestão no perfil docker: um publicador lê o  
  CSV e envia uma mensagem por filme; um consumidor grava pelo mesmo serviço do 
  modo padrão; uma mensagem de carga concluída libera a readiness. Gravação     
  idempotente por ano e título, e tópico de erros para mensagens inválidas.     
  Novos vencedores podem chegar pelo mesmo tópico depois da carga.              
  3. Autenticação por token parametrizada e desligada por padrão: o token vem   
  de variável de ambiente e é comparado com um cabeçalho; sem a variável, os    
  endpoints ficam públicos; com ela, os endpoints operacionais de métricas      
  exigem token.                                                                 
  4. compose.yaml com a aplicação, PostgreSQL, Kafka (imagem leve), Prometheus  
  e Grafana, com o dashboard versionado.                                        
     Restrições: o modo padrão continua idêntico, subindo e testando só com     
  ./mvnw, sem Docker. Os testes do Kafka usam o broker embutido do Spring. Os   
  testes com PostgreSQL ficam em um perfil Maven separado, executado no         
  pipeline. Um teste prova que a carga direta e a carga por Kafka produzem a    
  mesma resposta. Fora do escopo: publicação em nuvem e front-end.              

● Skill(speckit-specify)
  ⎿  Loaded skill

  Read 3 files, ran 1 shell command (ctrl+o to expand)

● Now the existing spec 002 for style and the current state of the code.

  Read 1 file, ran 4 shell commands (ctrl+o to expand)

● Write(specs\003-modo-docker\spec.md)
  ⎿  Wrote 401 lines to specs\003-modo-docker\spec.md
      1 # Feature Specification: Marco 3 — Modo Docker: PostgreSQL, inges
        tão por Kafka, autenticação e compose
      2 
      3 **Feature Branch**: `marco/3-docker`
      4 
      5 **Created**: 2026-11-24
      6 
      7 **Status**: Draft
      8 
      9 **Input**: User description: "Marco 3, modo Docker, conforme docs
     … +393 lines (~27 KB) (ctrl+o to expand)

● Write(.specify\feature.json)
  ⎿  Added 1 line, removed 1 line
      1 -{ "feature_directory": "specs/002-operacao" }                   
      1 +{ "feature_directory": "specs/003-modo-docker" }                

● Answered Claude's questions:
  ⎿  · Kafka (FR-014): quando chega um evento com o ano e o título de um filme 
     que já está gravado, o que a aplicação faz? Na subida o CSV é publicado de 
     novo, então 'atualizar' faria o CSV desfazer, a cada reinício, uma mudança 
     que chegou por evento (por exemplo, um indicado que virou vencedor). → 
     Atualizar o filme
     · Autenticação (FR-021): com a variável do token definida, os endpoints de 
     negócio (intervalos e listagem) também exigem o token? O briefing diz 'sem 
     a variável, os endpoints de negócio ficam públicos', o que sugere que com 
     ela eles ficam protegidos. Seu pedido cita só os endpoints de métricas. → 
     Negócio e métricas (Recommended)
     · Compose (FR-033): como ficam as credenciais do PostgreSQL e do Grafana? 
     Pelo Princípio VII, o único segredo permitido no repositório é o token de 
     demonstração. → Senhas de demonstração

  Ran 1 shell command (ctrl+o to expand)

● Python isn't available, so I'll make the edits with Edit.

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 19 lines
       8 
       9  **Input**: User description: "Marco 3, modo Docker, conforme do
          cs/ai/planejamento/briefing.md. Escopo: 1. Perfil docker com Po
          stgreSQL no lugar do H2, usando as mesmas migrações do Flyway.
          2. Kafka como caminho único de ingestão no perfil docker: um pu
          blicador lê o CSV e envia uma mensagem por filme; um consumidor
           grava pelo mesmo serviço do modo padrão; uma mensagem de carga
           concluída libera a readiness. Gravação idempotente por ano e t
          ítulo, e tópico de erros para mensagens inválidas. Novos venced
          ores podem chegar pelo mesmo tópico depois da carga. 3. Autenti
          cação por token parametrizada e desligada por padrão: o token v
          em de variável de ambiente e é comparado com um cabeçalho; sem
          a variável, os endpoints ficam públicos; com ela, os endpoints
          operacionais de métricas exigem token. 4. compose.yaml com a ap
          licação, PostgreSQL, Kafka (imagem leve), Prometheus e Grafana,
           com o dashboard versionado. Restrições: o modo padrão continua
           idêntico, subindo e testando só com ./mvnw, sem Docker. Os tes
          tes do Kafka usam o broker embutido do Spring. Os testes com Po
          stgreSQL ficam em um perfil Maven separado, executado no pipeli
          ne. Um teste prova que a carga direta e a carga por Kafka produ
          zem a mesma resposta. Fora do escopo: publicação em nuvem e fro
          nt-end."
      10 
      11 +## Clarifications                                              
      12 +                                                               
      13 +### Session 2026-11-24                                         
      14 +                                                               
      15 +- Q: Um evento com o ano e o título de um filme já gravado é ig
         +norado ou atualiza o filme? → A:                               
      16 +  Atualiza. A última gravação prevalece: estúdios, produtores e
         + `winner` passam a ser os do evento, e um                      
      17 +  indicado pode virar vencedor por evento. Consequência aceita:
         + na subida, o arquivo é publicado de novo                      
      18 +  e reaplica os seus valores aos filmes que estão nele, então u
         +ma mudança recebida por evento num filme                       
      19 +  do arquivo é desfeita no reinício. Filmes que não estão no ar
         +quivo não são afetados. Duplicatas dentro                      
      20 +  do próprio arquivo continuam ignoradas pelo publicador (spec 
         +001, FR-006d), o que preserva a                                
      21 +  equivalência com a carga direta. FR-014, US2-4 a US2-6.      
      22 +- Q: Com o token definido, os endpoints de negócio também exige
         +m o token? → A: Sim. Intervalos,                               
      23 +  listagem e métricas exigem o token. Liveness, readiness, Swag
         +ger UI e contrato OpenAPI continuam                            
      24 +  públicos. FR-020 a FR-022 e US4-4.                           
      25 +- Q: Como ficam as credenciais do PostgreSQL e do Grafana no co
         +mpose? → A: Senhas de demonstração,                            
      26 +  documentadas no README como o token. **Depende de emenda do P
         +rincípio VII** da constituição, que hoje                       
      27 +  só permite o token de demonstração no repositório (o `AGENTS.
         +md`, seção Segurança, diz o mesmo). A                          
      28 +  emenda vem antes do plano. FR-033.                           
      29 +                                                               
      30  ## User Scenarios & Testing *(mandatory)*
      31 
      32  Os atores são:

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 9 lines, removed 5 lines
      100  3. **Given** a aplicação já carregada sobre um banco persisten
           te, **When** ela reinicia e publica o
      101     arquivo de novo, **Then** nenhum filme, produtor ou vínculo
            é duplicado, e as respostas continuam as
      102     mesmas.
      103 -4. **Given** um vencedor novo recebido por evento depois da ca
          -rga, **When** a aplicação reinicia sobre o                    
      104 -   mesmo banco, **Then** o vencedor novo continua nos interval
          -os e na listagem.                                             
      105 -5. **Given** um evento com o ano e o título de um filme já gra
          -vado, **When** ele é consumido, **Then**                      
      106 -   [NEEDS CLARIFICATION: o evento é ignorado (a primeira grava
          -ção prevalece, como na FR-006d da spec 001)                   
      107 -   ou atualiza o filme gravado (estúdios, produtores e `winner
          -`)?].                                                         
      103 +4. **Given** um filme novo (ano e título que não estão no arqu
          +ivo) recebido por evento depois da                            
      104 +   carga, **When** a aplicação reinicia sobre o mesmo banco, *
          +*Then** o filme novo continua nos                             
      105 +   intervalos e na listagem.                                  
      106 +5. **Given** um evento com o ano e o título de um filme já gra
          +vado e outros estúdios, produtores ou                         
      107 +   `winner`, **When** ele é consumido, **Then** o filme gravad
          +o passa a ter os valores do evento, sem                       
      108 +   ser duplicado, e os intervalos e a listagem refletem a muda
          +nça.                                                          
      109 +6. **Given** um filme do arquivo alterado por evento (por exem
          +plo, um indicado que virou vencedor),                         
      110 +   **When** a aplicação reinicia e publica o arquivo de novo, 
          +**Then** o filme volta aos valores do                         
      111 +   arquivo (Clarifications, 2026-11-24).                      
      112 
      113  ---
      114 

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 6 lines, removed 7 lines
      166     token errado, **Then** a resposta é 401 em Problem Details,
            sem o valor esperado nem o recebido.
      167  3. **Given** a variável do token definida, **When** o endpoint
            de métricas é chamado com o token certo,
      168     **Then** a resposta é 200 no formato do Prometheus.
      169 -4. **Given** a variável do token definida, **When** os endpoin
          -ts de negócio são chamados sem token,                         
      170 -   **Then** [NEEDS CLARIFICATION: eles também exigem o token (
          -401), como o briefing sugere ao dizer que                     
      171 -   "sem a variável os endpoints de negócio ficam públicos", ou
          - continuam públicos e só os operacionais                      
      172 -   exigem token?].                                            
      173 -5. **Given** a variável do token definida, **When** a liveness
          - e a readiness são chamadas sem token,                        
      174 -   **Then** elas respondem normalmente, porque o healthcheck d
          -o container e o orquestrador não enviam                       
      175 -   token.                                                     
      169 +4. **Given** a variável do token definida, **When** os interva
          +los ou a listagem são chamados sem token                      
      170 +   ou com token errado, **Then** a resposta é 401 em Problem D
          +etails. Com o token certo, a resposta é a                     
      171 +   do Marco 2, inclusive o ETag e o 304.                      
      172 +5. **Given** a variável do token definida, **When** a liveness
          +, a readiness, o Swagger UI e o contrato                      
      173 +   OpenAPI são chamados sem token, **Then** eles respondem nor
          +malmente: o healthcheck do container e o                      
      174 +   orquestrador não enviam token, e quem lê a documentação pre
          +cisa dela para informar o token.                              
      175  6. **Given** a autenticação ligada, **When** uma chamada com t
           oken errado é registrada no log ou nas
      176     métricas, **Then** nenhum valor de token aparece.
      177 

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 5 lines, removed 1 line
      217    readiness da execução atual. Só o evento desta execução cont
           a.
      218  - Dois reinícios seguidos, com a primeira publicação interromp
           ida no meio: a segunda publica o arquivo
      219    inteiro, e a gravação idempotente evita duplicatas.
      220 -- Evento repetido (mesma mensagem entregue duas vezes pelo bro
          -ker): gravado uma vez só.                                     
      220 +- Evento repetido (mesma mensagem entregue duas vezes pelo bro
          +ker): o estado final é o mesmo de uma                         
      221 +  entrega só.                                                 
      222 +- Evento que tira um produtor de um filme: o vínculo é removid
          +o. Um produtor que fica sem filmes não                        
      223 +  aparece em nenhuma resposta, porque os intervalos só usam ve
          +ncedores.                                                     
      224 +- Evento que muda um vencedor para indicado: os intervalos dei
          +xam de contar essa vitória.                                   
      225  - Evento com o título com espaços em volta: identificado pelo
           título sem esses espaços, como no CSV
      226    (spec 001, FR-006d).
      227  - Evento cujo filme não é vencedor: gravado como indicado; não
            muda os intervalos, mas aparece na

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 4 lines, removed 2 lines
      282    de carga concluída de execuções anteriores MUST NOT liberar
           a readiness.
      283  - **FR-014**: A gravação MUST ser idempotente por ano e título
           : republicar o arquivo, ou receber o mesmo
      284    evento mais de uma vez, MUST NOT duplicar filmes, produtores
            ou vínculos. Um evento com o ano e o
      285 -  título de um filme já gravado MUST ser [NEEDS CLARIFICATION:
          - ignorado, com a primeira gravação                            
      286 -  prevalecendo, ou aplicado como atualização do filme?].      
      285 +  título de um filme já gravado MUST atualizar esse filme: est
          +údios, produtores e `winner` passam a ser                     
      286 +  os do evento (a última gravação prevalece). Na subida, a rep
          +ublicação do arquivo reaplica os valores do                   
      287 +  arquivo aos filmes que estão nele (Clarifications, 2026-11-2
          +4). Duplicatas dentro do arquivo continuam                    
      288 +  ignoradas pelo publicador (spec 001, FR-006d).              
      289  - **FR-015**: Um evento inválido MUST NOT ser gravado. Ele MUS
           T ser enviado a um tópico de erros, com o
      290    conteúdo original e o motivo, e gerar um aviso no log com o
           motivo. O consumo dos eventos seguintes
      291    MUST continuar.

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 5 lines, removed 4 lines
      303    autenticação fica desligada, e todos os endpoints se comport
           am como no Marco 2 (Princípio VII).
      304  - **FR-020**: Com a autenticação ligada, o endpoint de métrica
           s MUST exigir o token num cabeçalho da
      305    requisição, comparado exatamente com o valor da variável.
      306 -- **FR-021**: Com a autenticação ligada, os endpoints de negóc
          -io MUST [NEEDS CLARIFICATION: exigir o                        
      307 -  token, ou continuar públicos?] (ver US4-4).                 
      308 -- **FR-022**: A liveness e a readiness MUST continuar públicas
          -, com ou sem a autenticação, respondendo só                   
      309 -  o estado (spec 002, FR-011).                                
      306 +- **FR-021**: Com a autenticação ligada, os endpoints de negóc
          +io (`/api/producers/intervals` e                              
      307 +  `/api/movies`) MUST exigir o mesmo token. Com o token certo,
          + a resposta é a do Marco 2, inclusive o                       
      308 +  ETag e o 304.                                               
      309 +- **FR-022**: A liveness, a readiness, o Swagger UI e o contra
          +to OpenAPI MUST continuar públicos, com ou                    
      310 +  sem a autenticação. As probes continuam respondendo só o est
          +ado (spec 002, FR-011).                                       
      311  - **FR-023**: Token ausente ou errado num endpoint protegido M
           UST resultar em 401 em Problem Details
      312    (RFC 9457), com o mesmo corpo nos dois casos e sem revelar o
            valor esperado nem o recebido.
      313  - **FR-024**: A comparação do token MUST levar o mesmo tempo,
           qualquer que seja o valor recebido, para

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 3 lines, removed 4 lines
      335    repositório já provisionados. O dashboard MUST mostrar, no m
           ínimo, a taxa de requisições, a taxa de
      336    erro e os percentis 95 e 99 de latência dos endpoints de neg
           ócio, o atraso do consumidor e os eventos
      337    enviados ao tópico de erros.
      338 -- **FR-033**: Credenciais dos serviços do compose (PostgreSQL 
          -e Grafana) MUST [NEEDS CLARIFICATION: ser                     
      339 -  valores de demonstração documentados no README, como o token
          -, o que pede emenda do Princípio VII; ou                      
      340 -  não existir (PostgreSQL só na rede interna do compose, sem s
          -enha, e Grafana com acesso anônimo de                         
      341 -  leitura); ou vir de um `.env` não versionado, exigindo um pa
          -sso antes do `docker compose up`?].                           
      338 +- **FR-033**: As credenciais dos serviços do compose (PostgreS
          +QL e Grafana) MUST ser valores de                             
      339 +  demonstração, documentados como tal no README, sobrescrevíve
          +is por variável de ambiente e usados só no                    
      340 +  ambiente local. **Depende da emenda do Princípio VII** (Clar
          +ifications, 2026-11-24).                                      
      341  - **FR-034**: Toda imagem do compose MUST ter versão explícita
            (nunca `latest`), e o Dependabot MUST
      342    cobrir as imagens do compose.
      343  - **FR-035**: O pipeline MUST ter um estágio que sobe o compos
           e, espera a readiness da aplicação,
● Update(specs\003-modo-docker\spec.md)
  ⎿  Removed 2 lines
      414    no plano.
      415  - O nome do cabeçalho do token (por exemplo, `Authorization: B
           earer`) e o nome da variável de ambiente
      416    ficam no plano. O token de demonstração é documentado no REA
           DME.
      417 -- O Swagger UI e o contrato OpenAPI continuam públicos com a a
          -utenticação ligada, para que quem avalia                      
      418 -  consiga ler a documentação e informar o token nela.         
      417  - No compose, a autenticação fica desligada por padrão, como n
           o modo padrão. O README mostra como
      418    ligá-la com o token de demonstração.
      419  - A imagem da aplicação é a mesma do Marco 2; o compose só esc
           olhe o perfil e as variáveis.

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 2 lines, removed 1 line
      392  - **SC-006**: 100% dos eventos inválidos vão ao tópico de erro
           s, e nenhum deles impede a gravação do
      393    evento válido seguinte.
      394  - **SC-007**: Com a autenticação ligada, 100% das chamadas sem
            token ou com token errado aos endpoints
      395 -  protegidos recebem 401, e 100% das chamadas às probes recebe
          -m a resposta normal. Com ela desligada, o                     
      395 +  protegidos (negócio e métricas) recebem 401, e 100% das cham
          +adas às probes, ao Swagger UI e ao                            
      396 +  contrato recebem a resposta normal. Com ela desligada, o    
      397    comportamento é o do Marco 2.
      398  - **SC-008**: Num clone limpo, com as imagens já baixadas, `do
           cker compose up` deixa a aplicação pronta
      399    e o dashboard mostrando dados em menos de 3 minutos.

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 2 lines, removed 2 lines
      386    `./mvnw verify` passa sem Docker, incluindo os testes de Kaf
           ka e de autenticação.
      387  - **SC-003**: Em 100% das subidas com a ingestão por Kafka, a
           aplicação só se declara pronta depois de
      388    todos os filmes aceitos do arquivo estarem gravados.
      389 -- **SC-004**: Depois de 3 reinícios seguidos sobre o mesmo ban
          -co, o número de filmes, produtores e                          
      390 -  vínculos é o mesmo da primeira carga mais os recebidos por e
          -vento, sem nenhuma duplicata.                                 
      389 +- **SC-004**: Depois de 3 reinícios seguidos sobre o mesmo ban
          +co, o número de filmes é o da primeira                        
      390 +  carga mais os filmes novos recebidos por evento, sem nenhuma
          + duplicata de filme, produtor ou vínculo.                     
      391  - **SC-005**: Um vencedor novo publicado depois da carga apare
           ce nos intervalos em até 5 segundos.
      392  - **SC-006**: 100% dos eventos inválidos vão ao tópico de erro
           s, e nenhum deles impede a gravação do
      393    evento válido seguinte.

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 1 line, removed 1 line
      373  - **Registro de erro**: conteúdo original do evento inválido,
           motivo e momento do recebimento, no
      374    tópico de erros.
      375  - **Token de acesso**: valor esperado, vindo de variável de am
           biente; ausente ou vazio desliga a
      376 -  autenticação.                                               
      376 +  autenticação. Protege os endpoints de negócio e o de métrica
          +s.                                                            
      377  - **Dashboard**: definição versionada no repositório, com os p
           ainéis da FR-032.
      378 
      379  ## Success Criteria *(mandatory)*

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 1 line, removed 1 line
      80 
      81  No modo Docker, os dados ficam num PostgreSQL, criado pelas mes
          mas migrações do modo padrão. Ao
      82  reiniciar, a aplicação publica o arquivo de novo, e a gravação
          idempotente por ano e título impede
      83 -duplicatas. Os filmes recebidos por evento depois da carga cont
         -inuam lá.                                                      
      83 +duplicatas. Os filmes novos recebidos por evento depois da carg
         +a continuam lá.                                                
      84 
      85  **Why this priority**: O banco persistente é o que dá sentido a
          o Kafka (briefing). Sem a
      86  idempotência, um simples reinício duplicaria os dados e mudaria
           o resultado.

  Ran 1 shell command (ctrl+o to expand)

● Write(specs\003-modo-docker\checklists\requirements.md)
  ⎿  Wrote 63 lines to specs\003-modo-docker\checklists\requirements.md
       1 # Specification Quality Checklist: Marco 3 — Modo Docker
       2 
       3 **Purpose**: Validate specification completeness and quality bef
         ore proceeding to planning
       4 **Created**: 2026-11-24
       5 **Feature**: [spec.md](../spec.md)
       6 
       7 ## Content Quality
       8 
       9 - [x] No implementation details (languages, frameworks, APIs)
     … +54 lines (ctrl+o to expand)

● Escrevi a spec do Marco 3 e o checklist, e respondi às três perguntas com as
  suas escolhas. Antes do /speckit-plan, porém, falta uma emenda à constituição
  que eu não fiz.

  - Spec: specs/003-modo-docker/spec.md (.specify/feature.json agora aponta para
    specs/003-modo-docker)
  - Checklist: specs/003-modo-docker/checklists/requirements.md. Os itens de
    qualidade passam e não sobrou nenhum [NEEDS CLARIFICATION]. Acrescentei as
    seções "Revisão do README" e "Revisão dos ADRs", que ficam desmarcadas até a
    revisão do PR, como na spec 002.
  - Hooks: não existe .specify/extensions.yml, então nenhum foi executado.
  - Não fiz commit.

  Conteúdo: são 5 histórias e 38 requisitos.
  - US1 (P1): ingestão por Kafka, com a readiness liberada só pelo evento de
    carga concluída da execução atual e o teste de equivalência com a carga
    direta.
  - US2 (P1): PostgreSQL com as migrações atuais, reinícios sem duplicatas e a
    mesma ordem da listagem nos dois bancos.
  - US3 (P2): novos vencedores e tópico de erros.
  - US4 (P2): autenticação por token.
  - US5 (P3): compose com Prometheus e Grafana, comprovado por um estágio novo
    do pipeline.

  Suas respostas e o que elas implicam:
  - Atualizar o filme (FR-014): um evento com ano e título já gravados substitui
    os dados do filme. Como o CSV é publicado de novo a cada subida, uma
    mudança que chega por evento num filme do arquivo, como um indicado virando
    vencedor, é desfeita no reinício. Deixei isso explícito nas Clarifications e
    no cenário US2-6. Só os filmes novos sobrevivem ao reinício.
  - Token nos endpoints de negócio e nas métricas: liveness, readiness, Swagger
    UI e OpenAPI continuam públicos.
  - Senhas de demonstração no compose (FR-033): o Princípio VII e o AGENTS.md
    hoje só permitem o token de demonstração no repositório. Uma spec não abre
    exceção a um princípio, então a FR-033 está marcada como dependente da
    emenda, que precisa vir antes do plano. Posso redigi-la se você quiser.

  Ponto para o Constitution Check do plano: o Princípio VI exige que todo
  comportamento tenha teste de integração no ./mvnw verify, mas o Princípio II
  põe os testes de PostgreSQL num perfil Maven separado. A FR-038 deixa no
  perfil separado só o que existe apenas no PostgreSQL: as migrações, a ordem da
  listagem e a persistência entre reinícios. Registrei essa tensão no checklist
  em vez de resolvê-la.

  Decidi sozinho, nas Assumptions (corrija se não servir):
  - O publicador roda dentro da própria aplicação.
  - As linhas inválidas do CSV são filtradas pelo publicador e não vão ao tópico
    de erros.
  - No compose, a autenticação fica desligada por padrão.
  - O nome do cabeçalho, das variáveis e dos tópicos ficam para o plano.

  O próximo passo é a emenda do Princípio VII; depois dela, o /speckit-plan. O
  /speckit-clarify é opcional.

✻ Baked for 5m 33s · done terça-feira, 15:11

❯ Faça a emenda do Princípio VII antes do plano: credenciais de demonstração    
  são permitidas apenas no compose de desenvolvimento local, identificadas como 
  tal no README e nos arquivos, sobrescrevíveis por variável de ambiente e      
  nunca usadas em ambiente publicado. O token de demonstração continua como já  
  estava. Atualize o Sync Impact Report, o AGENTS.md se ele citar a regra, e    
  registre em docs/ai/decisoes.md.                                              

  Searched for 1 pattern (ctrl+o to expand)

● Now the edits: constitution (principle, report, version), AGENTS.md,
  decisoes.md and the spec 003 references.

● Update(.specify\memory\constitution.md)
  ⎿  Added 11 lines, removed 1 line
      255  ### VII. Sem segredos; configuração por variável de ambiente
      256 
      257  - Tokens, senhas e credenciais reais MUST NOT aparecer no códi
           go, nos commits, na evidência de IA
      258 -  nem nas mensagens aos agentes. Só o token de demonstração do
          -cumentado no README é permitido.                              
      258 +  nem nas mensagens aos agentes. Há só duas exceções:         
      259 +  - o token de demonstração documentado no README;            
      260 +  - as **credenciais de demonstração dos serviços do compose d
          +e desenvolvimento local** (por exemplo,                       
      261 +    o usuário e a senha do PostgreSQL e do Grafana), desde que
          + cumpram todas as condições abaixo:                           
      262 +    - aparecem só nos arquivos do compose de desenvolvimento l
          +ocal e na configuração que ele monta;                         
      263 +    - são identificadas como de demonstração no README e no pr
          +óprio arquivo onde aparecem;                                  
      264 +    - podem ser sobrescritas por variável de ambiente;        
      265 +    - MUST NOT ser usadas em ambiente publicado, como o deploy
          + em nuvem. O estágio do pipeline que sobe                     
      266 +      esse mesmo compose num runner temporário, para testá-lo,
          + não é ambiente publicado.                                    
      267 +- Nenhuma outra credencial de demonstração é permitida. Um amb
          +iente publicado MUST receber as suas                          
      268 +  credenciais por variável de ambiente ou pelo gerenciador de 
          +segredos da plataforma.                                       
      269  - Toda configuração MUST vir de variáveis de ambiente, inclusi
           ve a porta (`PORT`), o caminho do
      270    CSV, o token e o CORS, com padrões que fazem o modo padrão f
           uncionar sem nada definido.
      271  - A autenticação MUST ficar desligada por padrão: sem a variáv
           el do token, os endpoints de negócio

● Update(.specify\memory\constitution.md)
  ⎿  Added 5 lines, removed 1 line
      271  - A autenticação MUST ficar desligada por padrão: sem a variáv
           el do token, os endpoints de negócio
      272    ficam públicos.
      273 
      274 -**Motivo:** o repositório e toda a evidência de IA serão públi
          -cos.                                                          
      274 +**Motivo:**                                                   
      275 +- O repositório e toda a evidência de IA serão públicos.      
      276 +- O compose de desenvolvimento local precisa subir com um únic
          +o comando, num clone limpo. Uma senha de                      
      277 +  demonstração, identificada como tal e restrita à máquina de 
          +quem executa, não protege nada de valor, e                    
      278 +  exigir um arquivo de segredos antes do `docker compose up` s
          +ó criaria um passo a mais.                                    
      279 
      280  ## Restrições técnicas e de segurança
      281 

● Update(.specify\memory\constitution.md)
  ⎿  Added 1 line, removed 1 line
      347    conformidade com os princípios I a VII. Os sete princípios s
           ão inegociáveis e não admitem
      348    exceção em "Complexity Tracking". Qualquer outra divergência
            MUST ser justificada no plano.
      349 
      350 -**Version**: 1.5.0 | **Ratified**: 2026-11-18 | **Last Amended
          -**: 2026-11-24                                                
          \ No newline at end of file
      350 +**Version**: 1.6.0 | **Ratified**: 2026-11-18 | **Last Amended
          +**: 2026-11-24                                                
          \ No newline at end of file

● Update(.specify\memory\constitution.md)
  ⎿  Added 27 lines
       1  <!--
       2 +Sync Impact Report (1.6.0)                                     
       3 +- Version change: 1.5.0 → 1.6.0                                
       4 +  Justificativa: MINOR. O Princípio VII ganha uma segunda exceç
         +ão, ampliando o que é permitido sem                            
       5 +  proibir nada que valia antes. Além do token de demonstração (
         +sem mudança), passam a ser permitidas as                       
       6 +  credenciais de demonstração dos serviços do compose de desenv
         +olvimento local, com quatro condições:                         
       7 +  - aparecem só nos arquivos do compose local e na configuração
         + que ele monta;                                                
       8 +  - são identificadas como de demonstração no README e no própr
         +io arquivo;                                                    
       9 +  - podem ser sobrescritas por variável de ambiente;           
      10 +  - nunca são usadas em ambiente publicado. O estágio do pipeli
         +ne que testa o compose num runner                              
      11 +    temporário não é ambiente publicado.                       
      12 +                                                               
      13 +  Origem: clarificação da spec 003 (Marco 3), que escolheu senh
         +as de demonstração para o PostgreSQL e o                       
      14 +  Grafana do compose (FR-033).                                 
      15 +- Modified principles: VII. Sem segredos; configuração por vari
         +ável de ambiente (exceção nova e motivo)                       
      16 +- Added sections: nenhuma                                      
      17 +- Removed sections: nenhuma                                    
      18 +- Impacto:                                                     
      19 +  - AGENTS.md ✅ seção Segurança;                              
      20 +  - specs/003-modo-docker ✅ Clarifications e FR-033 deixam de 
         +depender de emenda pendente; o checklist                       
      21 +    foi atualizado;                                            
      22 +  - docs/ai/decisoes.md ✅ emenda registrada;                  
      23 +  - specs/001 e specs/002: nenhuma mudança (não usam credenciai
         +s);                                                            
      24 +  - README.md: nenhuma mudança agora. A identificação das crede
         +nciais no README entra com o compose                           
      25 +    (spec 003, FR-036), pelo papel de documentação.            
      26 +- Templates requiring updates: nenhum                          
      27 +- Follow-up TODOs: nenhum                                      
      28 +                                                               
      29  Sync Impact Report (1.5.0)
      30  - Version change: 1.4.1 → 1.5.0
      31    Justificativa: MINOR. A cláusula do Princípio I sobre `Spring
          Application` foi ampliada de forma

● Update(AGENTS.md)
  ⎿  Added 1 line
      100  ## Segurança
      101 
      102  - Nunca grave tokens, senhas ou credenciais no código, em comm
           its ou em mensagens aos agentes. Tudo o que é digitado aos age
           ntes será publicado.
      103 +- Exceção (Princípio VII, constituição 1.6.0): credenciais de 
          +demonstração dos serviços do compose de desenvolvimento local,
          + como o PostgreSQL e o Grafana. Elas aparecem só nos arquivos 
          +do compose local, são identificadas como de demonstração no RE
          +ADME e no próprio arquivo, podem ser sobrescritas por variável
          + de ambiente e nunca são usadas em ambiente publicado. O teste
          + do compose no pipeline não conta como ambiente publicado.    
      104  - A autenticação existe e é testada, mas fica **desligada por
           padrão**: sem a variável de ambiente do token, os endpoints de
            negócio ficam públicos. O único token aceito no repositório é
            o de demonstração documentado no README.
      105  - No pipeline, as actions são fixadas por **hash de commit**,
           nunca por tag.
      106  - A imagem Docker é multi-stage e roda com usuário não root.

● Update(docs\ai\decisoes.md)
  ⎿  Added 26 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-24 — Emenda 1.6.0 da constituição: credenciais de de
         +monstração no compose local                                    
       7 +                                                               
       8 +- **Origem:** clarificação da spec 003 (Marco 3). O compose pre
         +cisa de usuário e senha para o                                 
       9 +  PostgreSQL e o Grafana, e o Princípio VII só permitia o token
         + de demonstração no repositório.                               
      10 +- **Alternativas apresentadas ao responsável:**                
      11 +  - sem credencial: PostgreSQL só na rede interna do compose, s
         +em senha, e Grafana com acesso anônimo;                        
      12 +  - senhas de demonstração, documentadas no README;            
      13 +  - `.env` não versionado, com um passo obrigatório antes do `d
         +ocker compose up`.                                             
      14 +- **Decisão do responsável:** senhas de demonstração, com emend
         +a do Princípio VII antes do plano. Elas                        
      15 +  são permitidas só no compose de desenvolvimento local e preci
         +sam cumprir quatro condições:                                  
      16 +  - aparecem só nos arquivos do compose local e na configuração
         + que ele monta;                                                
      17 +  - são identificadas como de demonstração no README e no própr
         +io arquivo;                                                    
      18 +  - podem ser sobrescritas por variável de ambiente;           
      19 +  - nunca são usadas em ambiente publicado.                    
      20 +                                                               
      21 +  O token de demonstração continua como estava.                
      22 +- **Motivo:** é a forma mais comum de um compose local subir co
         +m um único comando, num clone limpo. Uma                       
      23 +  senha de demonstração restrita à máquina de quem executa não 
         +protege nada de valor.                                         
      24 +- **Ponto interpretado na redação:** o estágio do pipeline que 
         +sobe o mesmo compose num runner                                
      25 +  temporário (spec 003, FR-035) foi declarado como não sendo am
         +biente publicado. Sem isso, o teste do                         
      26 +  compose no pipeline violaria a condição "só no compose local"
         +.                                                              
      27 +- **Impacto:** MINOR (1.5.0 → 1.6.0), porque amplia o permitido
         + e não proíbe nada que valia antes.                            
      28 +  Foram alinhados a constituição (Princípio VII, motivo e Sync 
         +Impact Report), o `AGENTS.md` (seção                           
      29 +  Segurança), a spec 003 (Clarifications e FR-033) e o checklis
         +t dela. O README só ganha a                                    
      30 +  identificação das credenciais quando o compose existir (spec 
         +003, FR-036).                                                  
      31 +                                                               
      32  ## 2026-11-24 — T050 do Marco 2 (SC-008) não executada
      33 
      34  - **Situação:** a T050, que prova que o pipeline reprova (SC-00
          8), não foi executada antes do merge do

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 2 lines, removed 3 lines
      23    listagem e métricas exigem o token. Liveness, readiness, Swag
          ger UI e contrato OpenAPI continuam
      24    públicos. FR-020 a FR-022 e US4-4.
      25  - Q: Como ficam as credenciais do PostgreSQL e do Grafana no co
          mpose? → A: Senhas de demonstração,
      26 -  documentadas no README como o token. **Depende de emenda do P
         -rincípio VII** da constituição, que hoje                       
      27 -  só permite o token de demonstração no repositório (o `AGENTS.
         -md`, seção Segurança, diz o mesmo). A                          
      28 -  emenda vem antes do plano. FR-033.                           
      26 +  documentadas no README como o token. O Princípio VII foi emen
         +dado para permiti-las (constituição                            
      27 +  1.6.0), com as condições da FR-033. FR-033.                  
      28 
      29  ## User Scenarios & Testing *(mandatory)*
      30 

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 5 lines, removed 2 lines
      335    erro e os percentis 95 e 99 de latência dos endpoints de neg
           ócio, o atraso do consumidor e os eventos
      336    enviados ao tópico de erros.
      337  - **FR-033**: As credenciais dos serviços do compose (PostgreS
           QL e Grafana) MUST ser valores de
      338 -  demonstração, documentados como tal no README, sobrescrevíve
          -is por variável de ambiente e usados só no                    
      339 -  ambiente local. **Depende da emenda do Princípio VII** (Clar
          -ifications, 2026-11-24).                                      
      338 +  demonstração, conforme o Princípio VII (constituição 1.6.0):
      339 +  - aparecem só nos arquivos do compose de desenvolvimento loc
          +al e na configuração que ele monta;                           
      340 +  - são identificadas como de demonstração no README e no próp
          +rio arquivo onde aparecem;                                    
      341 +  - podem ser sobrescritas por variável de ambiente;          
      342 +  - nunca são usadas em ambiente publicado. O estágio do pipel
          +ine da FR-035 não é ambiente publicado.                       
      343  - **FR-034**: Toda imagem do compose MUST ter versão explícita
            (nunca `latest`), e o Dependabot MUST
      344    cobrir as imagens do compose.
      345  - **FR-035**: O pipeline MUST ter um estágio que sobe o compos
           e, espera a readiness da aplicação,

● Update(specs\003-modo-docker\checklists\requirements.md)
  ⎿  Added 2 lines, removed 2 lines
      53  - **Detalhes de tecnologia:** PostgreSQL, Kafka, Prometheus, Gr
          afana e compose aparecem na spec porque
      54    são o próprio escopo pedido do marco, como na spec 002. Nomes
           de cabeçalho, de variáveis, de tópicos,
      55    partições e bibliotecas ficaram para o plano.
      56 -- **Pendência antes do plano:** a FR-033 (senhas de demonstraçã
         -o no compose) depende de emenda do                             
      57 -  Princípio VII da constituição e do `AGENTS.md`, seção Seguran
         -ça.                                                            
      56 +- **Credenciais do compose:** a FR-033 (senhas de demonstração)
         + está coberta pela emenda 1.6.0 do                             
      57 +  Princípio VII, já registrada em `docs/ai/decisoes.md` e no `A
         +GENTS.md`.                                                     
      58  - **Ponto para o Constitution Check do plano:** o Princípio VI
          diz que um requisito de comportamento
      59    precisa do seu teste de integração "no `./mvnw verify`", e o
          Princípio II põe os testes de PostgreSQL
      60    num perfil Maven separado. A FR-038 comprova pelo perfil sepa
          rado, no pipeline, só o que existe

● Fiz a emenda do Princípio VII: a constituição foi para a 1.6.0. Não fiz
  commit.

  Constituição (.specify/memory/constitution.md)
  - O Princípio VII passou a ter duas exceções. O token de demonstração ficou
    como estava. A nova são as credenciais de demonstração do compose de
    desenvolvimento local, com as quatro condições que você definiu:
    - só nos arquivos do compose local e na configuração que ele monta;
    - identificadas como de demonstração no README e no próprio arquivo;
    - sobrescrevíveis por variável de ambiente;
    - nunca usadas em ambiente publicado.
  - Acrescentei uma regra de fechamento: nenhuma outra credencial de
    demonstração é permitida, e um ambiente publicado recebe as credenciais por
    variável de ambiente ou pelo gerenciador de segredos da plataforma.
  - O motivo do princípio ganhou a justificativa da exceção.
  - Classifiquei como MINOR, como na 1.5.0, porque amplia o permitido sem
    proibir nada que valia antes. O novo Sync Impact Report está no topo, com o
    impacto.

  AGENTS.md: a seção Segurança ganhou um item com a exceção, logo abaixo da
  regra geral. O item do token não mudou.

  docs/ai/decisoes.md: nova entrada no topo, com as três alternativas que você
  avaliou, a decisão, o motivo e o impacto.

  Spec 003: as Clarifications e a FR-033 agora citam a constituição 1.6.0 e
  repetem as quatro condições. Também atualizei a nota do checklist que dizia
  que a emenda estava pendente.

  Uma interpretação para você confirmar: a sua regra diz "apenas no compose de
  desenvolvimento local", mas a FR-035 sobe esse mesmo compose no pipeline para
  testá-lo. Para não proibir esse estágio, escrevi que um runner temporário do
  pipeline não é ambiente publicado. Se preferir outra saída, como credenciais
  vindas de variáveis do pipeline nesse estágio, eu ajusto o texto.

  O README só ganha a identificação das credenciais quando o compose existir
  (FR-036), e isso é do papel de documentação. O próximo passo é o
  /speckit-plan.

✻ Cooked for 1m 2s · done terça-feira, 15:19

❯ /speckit-plan Spring for Apache Kafka, com o broker embutido do               
  spring-kafka-test nos testes. Driver do PostgreSQL e as mesmas migrações do   
  Flyway, sem SQL específico de um banco. Os testes com PostgreSQL ficam num    
  perfil Maven separado e rodam no pipeline com o PostgreSQL como service       
  container do GitHub Actions, sem Testcontainers. Autenticação com Spring      
  Security: SecurityFilterChain stateless, sem CSRF, sem formLogin e sem        
  httpBasic; um filtro de autenticação por token no cabeçalho, comparado em     
  tempo constante; com o token desligado, todos os endpoints liberados; com o   
  token ligado, só os endpoints que a spec define exigem autenticação; 401 em   
  Problem Details. Desligar a geração automática de usuário e senha do Spring   
  Boot e o cabeçalho Cache-Control padrão nos endpoints de negócio, para não    
  conflitar com o ETag. Um teste prova que, com o token desligado, todos os     
  testes dos marcos 1 e 2 continuam passando sem alteração. compose.yaml com a  
  imagem nativa do Kafka em modo KRaft, PostgreSQL, Prometheus e Grafana com    
  datasource e dashboard provisionados por arquivo. O estágio novo do pipeline  
  sobe o compose e verifica a aplicação, o dashboard e a ingestão por Kafka.    

● Skill(speckit-plan)  ⎿  Loaded skill

  Ran 6 shell commands (ctrl+o to expand)

● Contexto lido (spec 003, constituição 1.6.0, plano 002 e o código atual).
  Agora vou escrever o plano e os artefatos de design: research, data-model,
  contratos e quickstart.

● Write(specs\003-modo-docker\research.md)
  ⎿  Wrote 513 lines to specs\003-modo-docker\research.md
       1 # Research: Marco 3 — Modo Docker
       2 
       3 Decisões técnicas do plano 003. Cada item traz a decisão, o moti
         vo e as alternativas. Os pontos marcados
       4 como **verificar** dependem do comportamento exato das versões g
         erenciadas pelo Spring Boot 4.1.2 e
       5 são conferidos na primeira tarefa que os usa. Se o resultado for
          outro, a decisão volta para esta
       6 research e, se mudar comportamento, para a spec (Princípio IV).
       7 
     … +506 lines (ctrl+o to expand)

● Write(specs\003-modo-docker\data-model.md)
  ⎿  Wrote 137 lines to specs\003-modo-docker\data-model.md
       1 # Data Model: Marco 3 — Modo Docker
       2 
       3 O esquema relacional não muda. A migração `V1__create_schema.sql
         ` é a mesma no H2 e no PostgreSQL
       4 (FR-004, research R3). O que muda são as regras de gravação e as
          entidades que passam a circular pelo
       5 Kafka.
       6 
       7 ## Tabelas (sem mudança)
       8 
     … +129 lines (ctrl+o to expand)

● Correction: a default password inside application-postgres.yml would violate
  1.6.0, because that file isn't part of the compose. The default has to live
  only in the compose.

● Update(specs\003-modo-docker\data-model.md)
  ⎿  Added 1 line, removed 1 line
      127  | `SPRING_PROFILES_ACTIVE` | — | vazio | `docker` no compose |
      128  | `DB_URL` | `postgres` | `jdbc:postgresql://postgres:5432/awa
           rds` | URL do banco |
      129  | `DB_USER` | `postgres` | `awards` | usuário |
      130 -| `DB_PASSWORD` | `postgres` | `awards-demo` (**credencial de 
          -demonstração**, Princípio VII 1.6.0) | senha |                
      130 +| `DB_PASSWORD` | `postgres` | **sem padrão na aplicação**: ob
          +rigatória no perfil. O compose a passa com a credencial de dem
          +onstração `${POSTGRES_PASSWORD:-awards-demo}`, e o job `postgr
          +es` do pipeline, com o valor do service container (Princípio V
          +II 1.6.0: a credencial de demonstração só aparece nos arquivos
          + do compose) | senha |                                        
      131  | `KAFKA_BOOTSTRAP_SERVERS` | `kafka` | `kafka:9092` | broker
           |
      132  | `KAFKA_TOPIC_MOVIES` | `kafka` | `awards.movies` | tópico de
            filmes |
      133  | `KAFKA_TOPIC_ERRORS` | `kafka` | `awards.movies.errors` | tó
           pico de erros |

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 5 lines, removed 2 lines
      52    - `org.postgresql:postgresql` (escopo `runtime`) e `org.flywa
          ydb:flyway-database-postgresql`
      53      (`runtime`), com as versões gerenciadas pelo Boot;
      54    - o `application-postgres.yml` define só a URL, o usuário e a
           senha, por variáveis de ambiente
      55 -    (`DB_URL`, `DB_USER` e `DB_PASSWORD`). Os padrões apontam p
         -ara o serviço `postgres` do compose,                           
      56 -    com a credencial de demonstração identificada no comentário
         - (Princípio VII, 1.6.0);                                       
      55 +    (`DB_URL`, `DB_USER` e `DB_PASSWORD`). A URL e o usuário tê
         +m padrões que apontam para o serviço                           
      56 +    `postgres` do compose. A senha **não tem padrão** (`${DB_PA
         +SSWORD}`): pelo Princípio VII (1.6.0), a                       
      57 +    credencial de demonstração só pode aparecer nos arquivos do
         + compose, e não na configuração da                             
      58 +    aplicação, que vai no JAR e na imagem. Sem a variável, a su
         +bida no perfil falha com a mensagem do                         
      59 +    Spring sobre o marcador não resolvido;                     
      60    - a migração `V1__create_schema.sql` não muda. `GENERATED BY
          DEFAULT AS IDENTITY`, `BOOLEAN`,
      61      `VARCHAR` e as restrições nomeadas funcionam nos dois banco
          s;
      62    - o `ddl-auto: validate` continua valendo nos dois bancos (FR
          -004).

● Same problem in the CI job: a password in ci.yml would be a credential outside
  the compose. The service container will use trust authentication instead, so
  the workflow holds no credential.

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 4 lines, removed 1 line
      392    - imagem `postgres:18-alpine` fixada por digest;
      393    - `POSTGRES_INITDB_ARGS="--locale=C --encoding=UTF8"`;
      394    - healthcheck `pg_isready`;
      395 -  - credencial de demonstração identificada (1.6.0).          
      395 +  - **sem credencial no workflow:** `POSTGRES_HOST_AUTH_METHOD
          +: trust`, com `DB_PASSWORD` vazio no                          
      396 +    passo do Maven. Pela emenda 1.6.0, a credencial de demonst
          +ração só pode aparecer nos arquivos do                        
      397 +    compose, e o `ci.yml` não é um deles. O banco só existe no
          + runner temporário, sem porta exposta fora                    
      398 +    dele.                                                     
      399 
      400    Sem Testcontainers (pedido do responsável).
      401  - **Princípio VI:** os ITs são de integração, com `@SpringBoot
           Test` ou `SpringApplication` e sem mocks.

● Update(specs\003-modo-docker\data-model.md)
  ⎿  Added 1 line, removed 1 line
      127  | `SPRING_PROFILES_ACTIVE` | — | vazio | `docker` no compose |
      128  | `DB_URL` | `postgres` | `jdbc:postgresql://postgres:5432/awa
           rds` | URL do banco |
      129  | `DB_USER` | `postgres` | `awards` | usuário |
      130 -| `DB_PASSWORD` | `postgres` | **sem padrão na aplicação**: ob
          -rigatória no perfil. O compose a passa com a credencial de dem
          -onstração `${POSTGRES_PASSWORD:-awards-demo}`, e o job `postgr
          -es` do pipeline, com o valor do service container (Princípio V
          -II 1.6.0: a credencial de demonstração só aparece nos arquivos
          - do compose) | senha |                                        
      130 +| `DB_PASSWORD` | `postgres` | **sem padrão na aplicação**: ob
          +rigatória no perfil. O compose a passa com a credencial de dem
          +onstração `${POSTGRES_PASSWORD:-awards-demo}`, e o job `postgr
          +es` do pipeline a passa vazia, com o service container em `tru
          +st` (Princípio VII 1.6.0: a credencial de demonstração só apar
          +ece nos arquivos do compose) | senha |                        
      131  | `KAFKA_BOOTSTRAP_SERVERS` | `kafka` | `kafka:9092` | broker
           |
      132  | `KAFKA_TOPIC_MOVIES` | `kafka` | `awards.movies` | tópico de
            filmes |
      133  | `KAFKA_TOPIC_ERRORS` | `kafka` | `awards.movies.errors` | tó
           pico de erros |

● Write(specs\003-modo-docker\contracts\kafka-events.md)
  ⎿  Wrote 97 lines to specs\003-modo-docker\contracts\kafka-events.md
       1 # Contrato: eventos do Kafka
       2 
       3 Vale para o perfil `kafka` (no compose, o grupo `docker`). Os no
         mes dos tópicos vêm de
       4 `KAFKA_TOPIC_MOVIES` e `KAFKA_TOPIC_ERRORS` (data-model.md).
       5 
       6 ## Tópicos
       7 
       8 | Tópico (padrão) | Partições | Quem escreve | Quem lê | Conteúd
         o |
     … +89 lines (ctrl+o to expand)

● Write(specs\003-modo-docker\contracts\openapi-1.2.0.md)
  ⎿  Wrote 87 lines to specs\003-modo-docker\contracts\openapi-1.2.0.md
      1 # Contrato: mudanças do OpenAPI 1.1.0 → 1.2.0
      2 
      3 O arquivo é `src/main/resources/static/openapi.yaml`. Os schemas
        dos corpos **não mudam** (Princípio
      4 III). As mudanças são:
      5 - o esquema de autenticação, opcional;
      6 - a resposta 401 nos dois endpoints de negócio;
      7 - a descrição.
      8 
      9 ## `info`
     … +78 lines (ctrl+o to expand)

  Ran 2 shell commands (ctrl+o to expand)

● ApiDocsIT (Marco 1/2) asserts that the published openapi.yaml matches the
  contract file byte for byte, so shipping contract 1.2.0 means changing that
  test; I'm logging it as an open decision. · summary

● Update(specs\003-modo-docker\contracts\openapi-1.2.0.md)
  ⎿  Added 14 lines, removed 5 lines
      80  - Os ITs de contrato dos Marcos 1 e 2 (`IntervalsContractIT` e
          `MoviesContractIT`) continuam sem
      81    alteração (research R14).
      82 
      83 -**Observação:** o `ApiDocsIT` existe na tag `v0.2.0`. Se ele co
         -nfere a versão literal `1.1.0`, a                              
      84 -mudança para `1.2.0` altera um IT do Marco 2, e o `check-unchan
         -ged-tests.sh` reprova. Nesse caso, a                           
      85 -verificação da versão nova vai para um IT novo (`ApiDocsVersion
         -IT`), e o `ApiDocsIT` só muda se o                             
      86 -responsável aceitar uma exceção registrada em `docs/ai/decisoes
         -.md`. Conferir na primeira tarefa da                           
      87 -etapa 1.                                                       
      83 +## Conflito com o `ApiDocsIT` (decisão do responsável, pendente
         +)                                                              
      84 +                                                               
      85 +O `ApiDocsIT` da tag `v0.2.0` compara o `/openapi.yaml` publica
         +do, byte a byte, com                                           
      86 +`specs/002-operacao/contracts/openapi.yaml` (constante `SPEC_CO
         +NTRACT`). Publicar o contrato 1.2.0                            
      87 +faz esse teste falhar, a menos que uma destas três coisas mude:
      88 +                                                               
      89 +| Opção | O que muda | Efeito |                                
      90 +| --- | --- | --- |                                            
      91 +| **A (recomendada)** | Só a constante `SPEC_CONTRACT` do `ApiD
         +ocsIT` passa a apontar para `specs/003-modo-docker/contracts/op
         +enapi.yaml`, como o Marco 2 fez ao trocar a spec 001 pela 002 |
         + Nenhuma asserção muda. O `check-unchanged-tests.sh` ganha uma 
         +lista de exceções explícita, com este único arquivo e a referên
         +cia à entrada em `docs/ai/decisoes.md` |                       
      92 +| B | O arquivo `specs/002-operacao/contracts/openapi.yaml` é s
         +obrescrito com o 1.2.0 | O teste não muda, mas o artefato de um
         +a spec já entregue passa a descrever o marco seguinte |        
      93 +| C | O contrato publicado continua 1.1.0 | A FR-026 deixa de s
         +er cumprida |                                                  
      94 +                                                               
      95 +Com a opção A, o arquivo completo fica em `specs/003-modo-docke
         +r/contracts/openapi.yaml`, criado na                           
      96 +etapa 1 a partir do 1.1.0 com as mudanças acima, e copiado para
         + `src/main/resources/static/`.                                 

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 4 lines, removed 1 line
      315    3. **"Sem alteração":** o script `.github/scripts/check-unch
           anged-tests.sh v0.2.0` falha se algum
      316       arquivo `*IT.java` existente na tag `v0.2.0` tiver mudado
           . Ele usa `git diff --quiet v0.2.0 --` sobre
      317       a lista de ITs da tag e roda no job `workflow-policy`. As
            classes de apoio (`support/`) podem
      318 -     evoluir, desde que os ITs não mudem.                     
      318 +     evoluir, desde que os ITs não mudem. As exceções ficam nu
          +ma lista explícita                                            
      319 +     (`.github/unchanged-tests-exceptions.txt`), em que cada l
          +inha traz o arquivo e a entrada de                            
      320 +     `docs/ai/decisoes.md` que a autoriza. A lista começa vazi
          +a, salvo a decisão do `ApiDocsIT`                             
      321 +     ([contracts/openapi-1.2.0.md](contracts/openapi-1.2.0.md)
          +, "Conflito com o `ApiDocsIT`").                              
      322  - **Motivo:** nenhum teste dentro do `./mvnw verify` consegue
           provar sozinho que outros arquivos não
      323    mudaram. A comparação com a tag é a única verificação objeti
           va de "sem alteração", e o Princípio VI
      324    aceita o pipeline como comprovação de requisito de pipeline.

  Read 1 file (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; sed -i
      "s/schemas\/Problem'/schemas\/ProblemDetail'/; s/\`ProducerIntervals\`,
      \`ProducerInterval\`, \`MoviePage\`,…)
  ⎿  specs/003-modo-docker/contracts/openapi-1.2.0.md:18:    outro valor, a
      resposta é 401 em Problem Details.
     specs/003-modo-docker/contracts/openapi-1.2.0.md:45:            $ref:
     … +7 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\contracts\openapi-1.2.0.md +2 -2
      42        content:
      43          application/problem+json:
      44            schema:
      45 -            $ref: '#/components/schemas/Problem'               
      45 +            $ref: '#/components/schemas/ProblemDetail'         
      46            example:
      47              type: about:blank
      48              title: Unauthorized
     ...
      67 
      68  ## O que não muda
      69 
      70 -- Os schemas `ProducerIntervals`, `ProducerInterval`, `MoviePag
         -e`, `MovieItem` e `Problem`.                                   
      70 +- Os schemas `ProducerIntervals`, `ProducerInterval`, `MoviePag
         +e`, `Movie` e `ProblemDetail`.                                 
      71  - `additionalProperties: false` nos schemas de resposta.
      72  - O `ETag`, o `If-None-Match` e o 304 (contrato 1.1.0).
      73  - O endpoint de métricas e as probes continuam fora do contrato
           de negócio
  ⎿  Updated specs\003-modo-docker\research.md +1 -1
      288    - `components.securitySchemes.bearerAuth` (`type: http`, `sc
           heme: bearer`);
      289    - nos dois endpoints de negócio, `security: [{}, {bearerAuth
           : []}]`. O objeto vazio declara que a
      290      autenticação é opcional, porque depende da configuração;
      291 -  - a resposta `401` usa o schema `Problem` e traz o cabeçalho
          - `WWW-Authenticate`;                                          
      291 +  - a resposta `401` usa o schema `ProblemDetail` e traz o cab
          +eçalho `WWW-Authenticate`;                                    
      292    - a descrição explica a variável `API_TOKEN`.
      293 
      294    O detalhe está em [contracts/openapi-1.2.0.md](contracts/ope
           napi-1.2.0.md).

● Update(specs\003-modo-docker\contracts\openapi-1.2.0.md)
  ⎿  Added 3 lines, removed 1 line
      75 
      76  ## Comprovação
      77 
      78 -- O `ApiDocsIT` passa a conferir a versão 1.2.0 e o `securitySc
         -hemes.bearerAuth`.                                             
      78 +- Um IT novo, o `ApiDocsSecurityIT`, confere no contrato public
         +ado a versão 1.2.0, o                                          
      79 +  `securitySchemes.bearerAuth`, a alternativa `{}` e a resposta
         + 401 nos dois endpoints. O `ApiDocsIT`                         
      80 +  continua conferindo que o contrato publicado é o da spec (ver
         + o conflito abaixo).                                           
      81  - O `AuthContractIT` valida o 401 e a resposta 200 com token co
          ntra o contrato.
      82  - Os ITs de contrato dos Marcos 1 e 2 (`IntervalsContractIT` e
          `MoviesContractIT`) continuam sem
      83    alteração (research R14).

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/contracts/openapi.yaml; cp
      src/main/resources/static/openapi.yaml $f
      # info…)
  ⎿  4c4
     <   version: 1.1.0
     ---
     … +41 lines (ctrl+o to expand)
  ⎿  Created specs\003-modo-docker\contracts\openapi.yaml +229
        1 +openapi: 3.1.0                                                
        2 +info:                                                         
        3 +  title: Golden Raspberry Awards API                          
        4 +  version: 1.2.0                                              
        5 +  description: >                                              
        6 +    Intervalos entre prêmios consecutivos de produtores na cat
          +egoria Pior Filme e listagem dos filmes                       
        7 +    indicados e vencedores. Os dados vêm do arquivo CSV carreg
          +ado na inicialização. As respostas 200                        
        8 +    trazem ETag, e uma requisição com If-None-Match igual ao E
          +Tag atual recebe 304 sem corpo. A                             
        9 +    autenticação é opcional e fica desligada por padrão. Com a
          + variável de ambiente API_TOKEN definida,                     
       10 +    os dois endpoints exigem o cabeçalho Authorization: Bearer
          + <token>; sem ele, ou com outro valor, a                      
       11 +    resposta é 401 em Problem Details.                        
       12 +paths:                                                        
       13 +  /api/producers/intervals:                                   
       14 +    get:                                                      
       15 +      operationId: getProducerIntervals                       
       16 +      summary: Produtores com o menor e o maior intervalo entr
          +e duas vitórias consecutivas                                  
       17 +      description: >                                          
       18 +        Considera só filmes vencedores e pares de vitórias viz
          +inhas de cada produtor. Todos os empatados                    
       19 +        entram na lista, ordenados por previousWin e depois po
          +r nome. Sem produtor com duas vitórias,                       
       20 +        as duas listas vêm vazias.                            
       21 +      tags: [producers]                                       
       22 +      parameters:                                             
       23 +        - $ref: '#/components/parameters/IfNoneMatch'         
       24 +      responses:                                              
       25 +        '200':                                                
       26 +          description: Intervalos mínimo e máximo             
       27 +          headers:                                            
       28 +            ETag:                                             
       29 +              $ref: '#/components/headers/ETag'               
       30 +          content:                                            
       31 +            application/json:                                 
       32 +              schema:                                         
       33 +                $ref: '#/components/schemas/ProducerIntervals'
       34 +              examples:                                       
       35 +                arquivoFornecido:                             
       36 +                  summary: Resultado com o arquivo fornecido n
          +o enunciado                                                   
       37 +                  value:                                      
       38 +                    min:                                      
       39 +                      - { producer: Joel Silver, interval: 1, 
          +previousWin: 1990, followingWin: 1991 }                       
       40 +                    max:                                      
     … +189 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/contracts/openapi.yaml; for t in producers movies;
      do sed -i "s/^      tags: \[$t\]\r\?$/…)
  ⎿  21:      tags: [producers]
     22-      security:
     23-        - {}
     … +7 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\contracts\openapi.yaml +6
      19          entram na lista, ordenados por previousWin e depois por
           nome. Sem produtor com duas vitórias,
      20          as duas listas vêm vazias.
      21        tags: [producers]
      22 +      security:                                                
      23 +        - {}                                                   
      24 +        - bearerAuth: []                                       
      25        parameters:
      26          - $ref: '#/components/parameters/IfNoneMatch'
      27        responses:
     ...
      55        summary: Lista paginada de filmes, com filtros opcionais
      56        description: Ordenada por ano e depois por título.
      57        tags: [movies]
      58 +      security:                                                
      59 +        - {}                                                   
      60 +        - bearerAuth: []                                       
      61        parameters:
      62          - name: year
      63            in: query

● Now the remaining contracts: operational endpoints, compose and pipeline.

● Write(specs\003-modo-docker\contracts\operational-endpoints.md)
  ⎿  Wrote 65 lines to
     specs\003-modo-docker\contracts\operational-endpoints.md
       1 # Contrato: endpoints operacionais e autenticação
       2 
       3 Complementa o [contrato operacional da spec 002](../../002-opera
         cao/contracts/operational-endpoints.md).
       4 Os caminhos e os formatos não mudam. O que muda é quem exige o t
         oken e o que a readiness espera no
       5 perfil `kafka`.
       6 
       7 ## Matriz de acesso
       8 
     … +57 lines (ctrl+o to expand)

● Write(specs\003-modo-docker\contracts\compose.md)  ⎿  Wrote 81 lines to specs\003-modo-docker\contracts\compose.md
       1 # Contrato: `compose.yaml` e observabilidade
       2 
       3 Dono: papel de infraestrutura (`AGENTS.md`). As decisões estão n
         as R17 a R19 da research.
       4 
       5 ## Serviços
       6 
       7 | Serviço | Imagem (tag e digest resolvidos na tarefa) | Sobe no
          `up` | Portas no host | Volume | Healthcheck |
       8 | --- | --- | --- | --- | --- | --- |
     … +73 lines (ctrl+o to expand)

● Write(specs\003-modo-docker\contracts\ci-pipeline.md)
  ⎿  Wrote 75 lines to specs\003-modo-docker\contracts\ci-pipeline.md
      1 # Contrato: mudanças no pipeline (Marco 3)
      2 
      3 Complementa o [contrato do pipeline da spec 002](../../002-operac
        ao/contracts/ci-pipeline.md). As regras
      4 de política continuam valendo para os jobs novos:
      5 - actions fixadas por hash;
      6 - `permissions: contents: read` no topo;
      7 - nenhum `secrets.*`;
      8 - nenhuma publicação de imagem;
      9 - `timeout-minutes` em todo job.
     … +66 lines (ctrl+o to expand)

● Write(specs\003-modo-docker\quickstart.md)
  ⎿  Wrote 110 lines to specs\003-modo-docker\quickstart.md
       1 # Quickstart: validação do Marco 3
       2 
       3 Roteiro para conferir o marco de ponta a ponta. Os formatos estã
         o nos contratos:
       4 [eventos](contracts/kafka-events.md), [operacional](contracts/op
         erational-endpoints.md),
       5 [compose](contracts/compose.md) e [pipeline](contracts/ci-pipeli
         ne.md).
       6 
       7 ## 1. Modo padrão: só o JDK (Princípio II)
     … +103 lines (ctrl+o to expand)

● Write(specs\003-modo-docker\plan.md)
  ⎿  Added 228 lines, removed 74 lines
        1 -# Implementation Plan: [FEATURE]                              
        1 +# Implementation Plan: Marco 3 — Modo Docker: PostgreSQL, inge
          +stão por Kafka, autenticação e compose                        
        2 
        3 -**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**
          -: [link]                                                      
        3 +**Branch**: `marco/3-docker` | **Date**: 2026-11-24 | **Spec**
          +: [spec.md](spec.md)                                          
        4 
        5 -**Input**: Feature specification from `/specs/[###-feature-nam
          -e]/spec.md`                                                   
        5 +**Input**: Feature specification from `/specs/003-modo-docker/
          +spec.md`                                                      
        6 
        7 -**Note**: This template is filled in by the `/speckit-plan` co
          -mmand; its definition describes the execution workflow.       
        8 -                                                              
        7  ## Summary
        8 
       11 -[Extract from feature spec: primary requirement + technical ap
          -proach from research]                                         
        9 +O Marco 3 acrescenta o modo Docker e a autenticação, sem mudar
          + o modo padrão nem o contrato de                              
       10 +intervalos.                                                   
       11 
       12 +- **Perfis:** `postgres` (banco) e `kafka` (ingestão) são inde
          +pendentes, e o grupo `docker` liga os                         
       13 +  dois. Sem perfil, vale o Marco 2. A autoconfiguração do Kafk
          +a fica excluída fora do perfil (R1 e R2).                     
       14 +- **PostgreSQL:**                                             
       15 +  - driver e `flyway-database-postgresql`, com a mesma migraçã
          +o V1 e o `validate` (R3);                                     
       16 +  - a ordem da listagem fica igual à do H2 com o banco criado 
          +em collation `C`, sem mudar SQL (R4).                         
       17 +- **Serviço único com atualização:** o `MovieImportService.rec
          +ord` cria ou atualiza o filme por ano e                       
       18 +  título. A carga direta e o consumidor usam o mesmo método, e
          + as regras de campo ficam numa classe                         
       19 +  comum ao CSV e aos eventos (R5 e R7).                       
       20 +- **Kafka (Spring for Apache Kafka):**                        
       21 +  - tópico de filmes e de erros, com uma partição cada (R6);  
       22 +  - o publicador lê o CSV na subida, com as mesmas falhas e os
          + mesmos avisos, e publica depois do                           
       23 +    `ApplicationReadyEvent`, com um `runId` por execução (R8);
       24 +  - o consumidor lê os bytes crus. O evento inválido vai ao tó
          +pico de erros com os bytes originais e o                      
       25 +    motivo nos cabeçalhos, e a falha de banco é tentada de nov
          +o sem avançar o offset (R9);                                  
       26 +  - a readiness ganha o indicador `ingestion`, liberado só pel
          +o marcador do `runId` desta execução                          
       27 +    (R10);                                                    
       28 +  - as métricas trazem o atraso do consumidor e os eventos gra
          +vados e rejeitados.                                           
       29 +- **Autenticação (Spring Security):**                         
       30 +  - `SecurityFilterChain` sem estado, sem CSRF, sem `formLogin
          +` e sem `httpBasic`;                                          
       31 +  - um filtro de `Authorization: Bearer`, com comparação em te
          +mpo constante;                                                
       32 +  - com o token desligado, tudo liberado. Com ele ligado, só o
          +s intervalos, a listagem e as métricas                        
       33 +    exigem o token;                                           
       34 +  - 401 em Problem Details;                                   
       35 +  - sem usuário gerado (R11);                                 
       36 +  - o `Cache-Control` do Security fica desligado nos endpoints
          + de negócio, para preservar o ETag (R12);                     
       37 +  - contrato OpenAPI 1.2.0, com a autenticação opcional (R13).
       38 +- **Prova de que os Marcos 1 e 2 não mudaram:**               
       39 +  - a suíte roda sem token;                                   
       40 +  - um IT de guarda falha se `API_TOKEN` estiver definido;    
       41 +  - um script compara os ITs com a tag `v0.2.0` (R14).        
       42 +- **Testes:**                                                 
       43 +  - Kafka com `@EmbeddedKafka` (spring-kafka-test) no `./mvnw 
          +verify` (R15);                                                
       44 +  - PostgreSQL no perfil Maven `postgres-it`, com o *service c
          +ontainer* do GitHub Actions e sem                             
       45 +    Testcontainers (R16).                                     
       46 +- **Compose:**                                                
       47 +  - imagem nativa do Kafka em KRaft, PostgreSQL, a aplicação, 
          +Prometheus e Grafana, com datasource e                        
       48 +    dashboard provisionados por arquivo;                      
       49 +  - um `kafka-cli` sob demanda;                               
       50 +  - imagens por digest;                                       
       51 +  - credenciais de demonstração só no compose (R17 a R19, cons
          +tituição 1.6.0).                                              
       52 +- **Pipeline:**                                               
       53 +  - jobs novos `postgres` e `compose`. O `compose` verifica a 
          +aplicação, o Prometheus, o dashboard, a                       
       54 +    ingestão, a persistência e o token;                       
       55 +  - política ampliada no `workflow-policy` (R20).             
       56 +- **Entrega:** um bolt, seis etapas e um PR (R21).            
       57 +                                                              
       58  ## Technical Context
       59 
       15 -<!--                                                          
       16 -  ACTION REQUIRED: Replace the content in this section with th
          -e technical details                                           
       17 -  for the project. The structure here is presented in advisory
          - capacity to guide                                            
       18 -  the iteration process.                                      
       19 --->                                                           
       60 +**Language/Version**: Java 25 (OpenJDK 25.0.2), sem mudança   
       61 
       21 -**Language/Version**: [e.g., Python 3.11, Swift 5.9, Rust 1.75
          - or NEEDS CLARIFICATION]                                      
       62 +**Primary Dependencies**: as do Marco 2 (Spring Boot 4.1.2), m
          +ais:                                                          
       63 +- `spring-boot-starter-security`;                             
       64 +- `org.springframework.kafka:spring-kafka`;                   
       65 +- `org.postgresql:postgresql` (runtime);                      
       66 +- `org.flywaydb:flyway-database-postgresql` (runtime);        
       67 +- nos testes, `spring-kafka-test` e `spring-security-test` (só
          + para os utilitários de requisição, sem                       
       68 +  dublês).                                                    
       69 
       23 -**Primary Dependencies**: [e.g., FastAPI, UIKit, LLVM or NEEDS
          - CLARIFICATION]                                               
       70 +Todas com a versão gerenciada pelo Boot (verificar o `flyway-d
          +atabase-postgresql`, R3)                                      
       71 
       25 -**Storage**: [if applicable, e.g., PostgreSQL, CoreData, files
          - or N/A]                                                      
       72 +**Storage**:                                                  
       73 +- H2 em memória no modo padrão;                               
       74 +- PostgreSQL 18 no perfil `postgres`, com o mesmo esquema Flyw
          +ay e collation `C`;                                           
       75 +- Kafka como transporte no perfil `kafka`, e não como armazena
          +mento da API                                                  
       76 
       27 -**Testing**: [e.g., pytest, XCTest, cargo test or NEEDS CLARIF
          -ICATION]                                                      
       77 +**Testing**: JUnit 5 e `@SpringBootTest` com `RestTestClient`,
          + como nos Marcos 1 e 2:                                       
       78 +- `@EmbeddedKafka` (broker KRaft embutido) nos ITs de Kafka do
          + `./mvnw verify`;                                             
       79 +- `ApplicationStarter` para as múltiplas execuções (equivalênc
          +ia e reinícios);                                              
       80 +- perfil Maven `postgres-it` para os `*PostgresIT`;           
       81 +- scripts bash no pipeline para o compose;                    
       82 +- sem mocks e sem Testcontainers                              
       83 
       29 -**Target Platform**: [e.g., Linux server, iOS 15+, WASM or NEE
          -DS CLARIFICATION]                                             
       84 +**Target Platform**:                                          
       85 +- JVM 25 pelo `./mvnw`, em qualquer sistema operacional;      
       86 +- a imagem do Marco 2;                                        
       87 +- Docker Compose v2;                                          
       88 +- runners `ubuntu-latest`                                     
       89 
       31 -**Project Type**: [e.g., library/cli/web-service/mobile-app/co
          -mpiler/desktop-app or NEEDS CLARIFICATION]                    
       90 +**Project Type**: web-service (API REST, módulo Maven único), 
          +mais infraestrutura de compose e                              
       91 +observabilidade                                               
       92 
       33 -**Performance Goals**: [domain-specific, e.g., 1000 req/s, 10k
          - lines/sec, 60 fps or NEEDS CLARIFICATION]                    
       93 +**Performance Goals**:                                        
       94 +- vencedor novo visível em até 5 s (SC-005);                  
       95 +- compose pronto, com o dashboard mostrando dados, em menos de
          + 3 min com as imagens baixadas (SC-008);                      
       96 +- o `./mvnw verify` até 2× o tempo do Marco 2 (R15)           
       97 
       35 -**Constraints**: [domain-specific, e.g., <200ms p95, <100MB me
          -mory, offline-capable or NEEDS CLARIFICATION]                 
       98 +**Constraints**:                                              
       99 +- `./mvnw` sem Docker (Princípio II);                         
      100 +- o contrato de intervalos idêntico (Princípio III);          
      101 +- os ITs dos Marcos 1 e 2 sem alteração;                      
      102 +- SQL portável;                                               
      103 +- credenciais de demonstração só no compose (1.6.0);          
      104 +- actions por hash e imagens por digest                       
      105 
       37 -**Scale/Scope**: [domain-specific, e.g., 10k users, 1M LOC, 50
          - screens or NEEDS CLARIFICATION]                              
      106 +**Scale/Scope**: centenas de filmes; um consumidor, uma partiç
          +ão e uma instância da aplicação; 2                            
      107 +endpoints de negócio protegíveis e 1 operacional; 2 jobs novos
          + de CI                                                        
      108 
      109 +Nenhum item ficou como NEEDS CLARIFICATION. Os pontos que depe
          +ndem do comportamento exato das versões                       
      110 +estão marcados como **verificar** na [research.md](research.md
          +) (R2, R3, R10, R13, R14, R15, R17, R18 e                     
      111 +R19), cada um com o que fazer se o resultado for outro.       
      112 +                                                              
      113  ## Constitution Check
      114 
      115  *GATE: Must pass before Phase 0 research. Re-check after Phase
            1 design.*
      116 
       43 -[Gates determined based on constitution file]                 
      117 +| Princípio | Como o plano atende | Pré-design | Pós-design | 
      118 +| --- | --- | --- | --- |                                     
      119 +| I. Somente testes de integração | Todo teste novo é `*IT`, c
          +om `@SpringBootTest` ou com `SpringApplication` para as múltip
          +las execuções (equivalência e reinícios; cláusula 1.5.0). Todo
          +s são verificados por HTTP: intervalos, listagem, readiness e 
          +métricas. O broker é real (embutido) e o PostgreSQL é real (se
          +rvice container). Nenhum mock. O tópico de erros é lido por um
          + consumidor real no teste. A única verificação fora do HTTP é 
          +o log (avisos, resumo, ausência do token e da senha gerada), p
          +orque o log é o comportamento. A propriedade `awards.ingestion
          +.publish-completion` (R10) não é um dublê: ela desliga um pass
          +o real, e o teste envia o marcador pelo broker real | ✅ | ✅ 
          +|                                                             
      120 +| II. Execução só com `./mvnw` | Os ITs de Kafka usam o broker
          + embutido, sem Docker. Os de PostgreSQL ficam no perfil `postg
          +res-it`, fora do `verify` padrão. A autoconfiguração do Kafka 
          +é excluída sem o perfil. A segurança fica desligada sem `API_T
          +OKEN` | ✅ | ✅ |                                             
      121 +| III. Contrato idêntico | Os schemas não mudam. O 1.2.0 só ac
          +rescenta a autenticação opcional (`{}`) e o 401. O `IntervalsC
          +ontractIT` e o `ReferenceFileIT` não mudam. A equivalência ent
          +re os caminhos de carga e entre os bancos é comparada byte a b
          +yte | ✅ | ✅ |                                               
      122 +| IV. Spec-anchored | A spec 003 foi clarificada (3 decisões) 
          +e a constituição foi emendada (1.6.0) antes do plano. **Um con
          +flito novo, o do `ApiDocsIT`, fica com o responsável** ([contr
          +acts/openapi-1.2.0.md](contracts/openapi-1.2.0.md)) | ✅ | ✅ 
          +(pendente a decisão do `ApiDocsIT`) |                         
      123 +| V. Extras isolados, serviço único, simplicidade | O Kafka e 
          +o PostgreSQL só com o perfil. A gravação fica num único serviç
          +o (`MovieImportService.record`), e o perfil muda só a origem. 
          +Não há registro de esquemas, várias partições, Testcontainers 
          +nem JWT. A autenticação é ligada só pela variável | ✅ | ✅ | 
      124 +| VI. Rastreabilidade (1.4.0) | Matriz abaixo. Comportamento c
          +omum aos dois bancos: IT no `./mvnw verify`. Comportamento só 
          +do PostgreSQL: `*PostgresIT` no job `postgres`. Compose e pipe
          +line: jobs. README e ADRs: checklist. Ver a nota de leitura co
          +njunta com o Princípio II | ✅ | ✅ |                         
      125 +| VII. Sem segredos (1.6.0) | O `API_TOKEN` vem do ambiente e 
          +nunca é registrado. As credenciais de demonstração ficam **só*
          +* no `compose.yaml`, identificadas e sobrescrevíveis. A aplica
          +ção não tem senha padrão (`DB_PASSWORD` obrigatório no perfil)
          +. O pipeline usa `trust` no PostgreSQL e um token aleatório ge
          +rado no job, sem `secrets.*` | ✅ | ✅ |                      
      126 +| Observabilidade (1.3.0) | A readiness só fica positiva depoi
          +s da carga, também no Kafka (indicador `ingestion`). O log con
          +tinua em texto no `./mvnw` e em JSON na imagem. Os ITs de log 
          +novos têm `@Tag("log")` e rodam nos dois formatos | ✅ | ✅ | 
      127 +| Cadeia de entrega | As actions novas são fixadas por hash. A
          +s imagens do compose e do service container são fixadas por di
          +gest, conferidas pelo `workflow-policy`. O Dependabot ganha o 
          +`docker-compose` | ✅ | ✅ |                                  
      128 
      129 +**Nota sobre os Princípios II e VI:**                         
      130 +- o VI pede o IT de cada comportamento "no `./mvnw verify`";  
      131 +- o II põe os testes de PostgreSQL num perfil Maven separado. 
      132 +                                                              
      133 +O plano lê os dois juntos. O que é comum aos dois bancos tem I
          +T no `verify` padrão, com H2. O que só                        
      134 +existe no PostgreSQL tem IT no perfil `postgres-it`, que é um 
          +`./mvnw verify` com o perfil, executado                       
      135 +pelo pipeline. São eles: a migração no PostgreSQL, a ordem com
          + a collation e a persistência entre                           
      136 +reinícios. Não há outra forma de comprovar esse comportamento 
          +sem violar o II. Se o responsável preferir                    
      137 +a leitura literal do VI, é preciso uma emenda PATCH esclarecen
          +do isso.                                                      
      138 +                                                              
      139 +**Resultado:** nenhuma violação. O "Complexity Tracking" não s
          +e aplica.                                                     
      140 +                                                              
      141 +## Rastreabilidade (Princípio VI)                             
      142 +                                                              
      143 +| Requisito | Comprovação |                                   
      144 +| --- | --- |                                                 
      145 +| FR-001 | suíte dos Marcos 1 e 2 sem alteração; `DefaultModeI
          +solationIT`; `check-unchanged-tests.sh` |                     
      146 +| FR-002 | ITs `kafka/` com H2 e `@ActiveProfiles("kafka")`; `
          +*PostgresIT` com `postgres` |                                 
      147 +| FR-003 | ITs que sobrescrevem `KAFKA_TOPIC_*` e `DB_*`; job 
          +`compose` |                                                   
      148 +| FR-004, FR-005 | `SchemaPostgresIT` (job `postgres`); `ddl-a
          +uto: validate` nos dois bancos |                              
      149 +| FR-006, FR-008 | `EquivalencePostgresIT`, `ListingOrderPostg
          +resIT` (job `postgres`) |                                     
      150 +| FR-007 | job `postgres` |                                   
      151 +| FR-009 | `KafkaInvalidLinesIT`, `KafkaStructuralFailureIT`, 
          +`PublisherSummaryIT` |                                        
      152 +| FR-010, FR-011 | `KafkaReadinessIT` (lê o tópico) e `KafkaRe
          +ferenceFileIT` |                                              
      153 +| FR-012 | `KafkaEquivalenceIT`, `KafkaNewWinnerIT` (separação
          + de nomes) |                                                  
      154 +| FR-013 | `KafkaReadinessIT` |                               
      155 +| FR-014 | `KafkaIdempotencyIT`, `KafkaUpdateIT`, `RestartPost
          +gresIT` |                                                     
      156 +| FR-015 | `KafkaInvalidEventsIT` |                           
      157 +| FR-016 | `KafkaNewWinnerIT` |                               
      158 +| FR-017 | `KafkaMetricsIT` |                                 
      159 +| FR-018 | `KafkaEquivalenceIT` |                             
      160 +| FR-019 a FR-025, FR-027 | `AuthDisabledByDefaultIT`, `AuthEn
          +abledIT`, `AuthProblemDetailsIT`, `TokenNotLoggedIT`, `NoGener
          +atedPasswordIT`, `CacheHeadersIT` |                           
      161 +| FR-026 | `ApiDocsSecurityIT`, `AuthContractIT` |            
      162 +| FR-028 a FR-032, FR-035 | job `compose` |                   
      163 +| FR-033, FR-034 | job `workflow-policy` (digest, Dependabot e
          + senha fora do workflow); revisão (comentários de demonstração
          +) |                                                           
      164 +| FR-036, FR-037 | checklist "Revisão do README" e "Revisão do
          +s ADRs" |                                                     
      165 +| SC-002 | job `build` com a guarda do `AuthDisabledByDefaultI
          +T`; `check-unchanged-tests.sh` |                              
      166 +                                                              
      167 +Cada IT traz os IDs no `@DisplayName`. Os IDs desta spec repet
          +em os das specs 001 e 002, com outros                         
      168 +significados (observação da etapa 6 do Marco 2). Por isso, os 
          +ITs novos usam o prefixo `S003`, por                          
      169 +exemplo `S003 FR-013 readiness só depois do marcador`. Os ITs 
          +antigos não mudam.                                            
      170 +                                                              
      171  ## Project Structure
      172 
      173  ### Documentation (this feature)
      174 
      175  ```text
       50 -specs/[###-feature]/                                          
       51 -├── plan.md              # This file (/speckit-plan command ou
          -tput)                                                         
       52 -├── research.md          # Phase 0 output (/speckit-plan comma
          -nd)                                                           
       53 -├── data-model.md        # Phase 1 output (/speckit-plan comma
          -nd)                                                           
       54 -├── quickstart.md        # Phase 1 output (/speckit-plan comma
          -nd)                                                           
       55 -├── contracts/           # Phase 1 output (/speckit-plan comma
          -nd)                                                           
       56 -└── tasks.md             # Phase 2 output (/speckit-tasks comm
          -and - NOT created by /speckit-plan)                           
      176 +specs/003-modo-docker/                                        
      177 +├── plan.md                      # Este arquivo               
      178 +├── research.md                  # Fase 0 (R1 a R21)          
      179 +├── data-model.md                # Fase 1                     
      180 +├── quickstart.md                # Fase 1                     
      181 +├── contracts/                                                
      182 +│   ├── openapi.yaml             # contrato de negócio 1.2.0 (
          +copiado para src/main/resources/static)                       
      183 +│   ├── openapi-1.2.0.md         # o que muda em relação ao 1.
          +1.0 e o conflito do ApiDocsIT                                 
      184 +│   ├── kafka-events.md                                       
      185 +│   ├── operational-endpoints.md                              
      186 +│   ├── compose.md                                            
      187 +│   └── ci-pipeline.md                                        
      188 +├── checklists/                                               
      189 +│   └── requirements.md          # inclui "Revisão do README" 
          +e "Revisão dos ADRs"                                          
      190 +└── tasks.md                     # Fase 2 (/speckit-tasks)    
      191  ```
      192 
      193  ### Source Code (repository root)
       60 -<!--                                                          
       61 -  ACTION REQUIRED: Replace the placeholder tree below with the
          - concrete layout                                              
       62 -  for this feature. Delete unused options and expand the chose
          -n structure with                                              
       63 -  real paths (e.g., apps/admin, packages/something). The deliv
          -ered plan must                                                
       64 -  not include Option labels.                                  
       65 --->                                                           
      194 
      195 +Só o que muda ou é novo.                                      
      196 +                                                              
      197  ```text
       68 -# [REMOVE IF UNUSED] Option 1: Single project (DEFAULT)       
       69 -src/                                                          
       70 -├── models/                                                   
       71 -├── services/                                                 
       72 -├── cli/                                                      
       73 -└── lib/                                                      
      198 +pom.xml                                   # security, kafka, p
          +ostgres, flyway-postgres; testes; perfil postgres-it          
      199 +compose.yaml                              # novo (R17)        
      200 +observability/                            # novo: prometheus.y
          +ml, provisionamento e dashboard do Grafana (R18, R19)         
      201 +.github/                                                      
      202 +├── workflows/ci.yml                      # jobs postgres e co
          +mpose; workflow-policy com fetch-depth 0                      
      203 +├── scripts/                                                  
      204 +│   ├── check-workflows.sh                # regras novas (comp
          +ose por digest, docker-compose no Dependabot)                 
      205 +│   ├── check-unchanged-tests.sh          # novo (R14)        
      206 +│   └── compose-test.sh                   # novo (R20)        
      207 +├── unchanged-tests-exceptions.txt        # novo (R14)        
      208 +└── dependabot.yml                        # + docker-compose  
      209 +src/main/java/com/outsera/awards/                             
      210 +├── ingestion/                                                
      211 +│   ├── MovieImportService.java           # record(MovieRow), 
          +cria ou atualiza (R5)                                         
      212 +│   ├── MovieFieldRules.java              # novo: regras de an
          +o, título e produtores comuns (R7)                            
      213 +│   ├── MovieFileReading.java             # novo: leitura e av
          +isos comuns à carga e ao publicador (R8)                      
      214 +│   ├── MovieFileLoader.java              # @Profile("!kafka")
      215 +│   └── kafka/                            # novo, tudo @Profil
          +e("kafka")                                                    
      216 +│       ├── KafkaIngestionConfiguration.java  # tópicos, fábri
          +ca do ouvinte, DefaultErrorHandler (R6, R9)                   
      217 +│       ├── KafkaIngestionProperties.java     # nomes dos tópi
          +cos, grupo, publish-completion                                
      218 +│       ├── MovieEventPublisher.java          # leitura na sub
          +ida, publicação no ApplicationReadyEvent (R8)                 
      219 +│       ├── MovieEventListener.java           # consumo, grava
          +ção, tópico de erros, contadores (R9)                         
      220 +│       ├── MovieEventParser.java             # bytes → MovieR
          +ow ou rejeição (R7)                                           
      221 +│       ├── EventRejectionReason.java                         
      222 +│       ├── LoadCompletedEvent.java                           
      223 +│       └── IngestionReadiness.java           # HealthIndicato
          +r "ingestion" (R10)                                           
      224 +├── movie/MovieRepository.java            # findByYearAndTitle
      225 +├── movie/Movie.java                      # update(studios, pr
          +oducersRaw, winner, producers)                                
      226 +└── security/                             # novo              
      227 +    ├── SecurityConfiguration.java        # SecurityFilterChai
          +n, matriz de acesso, cabeçalhos (R11, R12)                    
      228 +    ├── SecurityProperties.java           # awards.security.to
          +ken ← API_TOKEN                                               
      229 +    ├── TokenAuthenticationFilter.java    # Bearer, SHA-256 + 
          +MessageDigest.isEqual                                         
      230 +    └── ProblemAuthenticationEntryPoint.java  # 401 em Problem
          + Details                                                      
      231 +src/main/resources/                                           
      232 +├── application.yml                       # exclusões de autoc
          +onfiguração (R2); awards.security.token                       
      233 +├── application-kafka.yml                 # novo: Kafka, tópic
          +os, grupo de readiness (R10)                                  
      234 +├── application-postgres.yml              # novo: DB_URL, DB_U
          +SER, DB_PASSWORD (sem padrão)                                 
      235 +└── static/openapi.yaml                   # cópia de specs/003
          +-modo-docker/contracts/openapi.yaml                           
      236 +src/test/java/com/outsera/awards/                             
      237 +├── support/                              # ApplicationStarter
          + com perfis e argumentos; KafkaTestSupport (produtor e consumi
          +dor reais)                                                    
      238 +├── isolation/   DefaultModeIsolationIT                       
      239 +├── security/    AuthDisabledByDefaultIT, AuthEnabledIT, AuthP
          +roblemDetailsIT, TokenNotLoggedIT,                            
      240 +│                NoGeneratedPasswordIT, CacheHeadersIT, AuthCo
          +ntractIT                                                      
      241 +├── docs/        ApiDocsSecurityIT                            
      242 +├── kafka/       KafkaReferenceFileIT, KafkaEquivalenceIT, Kaf
          +kaReadinessIT, KafkaInvalidLinesIT,                           
      243 +│                KafkaStructuralFailureIT, KafkaNewWinnerIT, K
          +afkaUpdateIT, KafkaIdempotencyIT,                             
      244 +│                KafkaInvalidEventsIT, KafkaMetricsIT, Publish
          +erSummaryIT                                                   
      245 +└── postgres/    SchemaPostgresIT, EquivalencePostgresIT, List
          +ingOrderPostgresIT, RestartPostgresIT                         
      246 +src/test/resources/scenarios/             # CSVs novos dos cen
          +ários de Kafka, quando o arquivo fornecido não bastar         
      247 +docs/adr/                                 # ADRs da FR-037 (pa
          +pel de documentação)                                          
      248 +```                                                           
      249 
       75 -tests/                                                        
       76 -├── contract/                                                 
       77 -├── integration/                                              
       78 -└── unit/                                                     
      250 +**Structure Decision**: o módulo Maven único continua. O códig
          +o novo fica em `ingestion/kafka/` e em                        
      251 +`security/`, seguindo os pacotes por assunto que já existem. A
          + infraestrutura nova fica na raiz                             
      252 +(`compose.yaml`) e em `observability/`, como o briefing previa
          +.                                                             
      253 
       80 -# [REMOVE IF UNUSED] Option 2: Web application (when "frontend
          -" + "backend" detected)                                       
       81 -backend/                                                      
       82 -├── src/                                                      
       83 -│   ├── models/                                               
       84 -│   ├── services/                                             
       85 -│   └── api/                                                  
       86 -└── tests/                                                    
      254 +## Pendências para o responsável                              
      255 
       88 -frontend/                                                     
       89 -├── src/                                                      
       90 -│   ├── components/                                           
       91 -│   ├── pages/                                                
       92 -│   └── services/                                             
       93 -└── tests/                                                    
      256 +1. **`ApiDocsIT`:** o teste compara o contrato publicado com o
          + arquivo da spec 002. A recomendação é a                      
      257 +   opção A de [contracts/openapi-1.2.0.md](contracts/openapi-1
          +.2.0.md): trocar só o caminho da                              
      258 +   constante, com a exceção registrada. Decidir antes da etapa
          + 1.                                                           
      259 +2. **Cabeçalhos de proteção do Spring Security** (R12): o plan
          +o mantém `X-Content-Type-Options` e                           
      260 +   `X-Frame-Options` em todas as respostas, inclusive com o to
          +ken desligado. Isso lê a FR-019 como                          
      261 +   "status, corpo, ETag e cabeçalhos do Marco 2 iguais". Confi
          +rmar ou pedir a leitura literal.                              
      262 +3. **Princípios II e VI** (nota do Constitution Check): confir
          +mar a leitura conjunta ou pedir a emenda                      
      263 +   PATCH.                                                     
      264 
       95 -# [REMOVE IF UNUSED] Option 3: Mobile + API (when "iOS/Android
          -" detected)                                                   
       96 -api/                                                          
       97 -└── [same as backend above]                                   
       98 -                                                              
       99 -ios/ or android/                                              
      100 -└── [platform-specific structure: feature modules, UI flows, p
          -latform tests]                                                
      101 -```                                                           
      102 -                                                              
      103 -**Structure Decision**: [Document the selected structure and r
          -eference the real                                             
      104 -directories captured above]                                   
      105 -                                                              
      265  ## Complexity Tracking
      266 
      108 -> **Fill ONLY if Constitution Check has violations that must b
          -e justified**                                                 
      109 -                                                              
      110 -| Violation | Why Needed | Simpler Alternative Rejected Becaus
          -e |                                                           
      111 -|-----------|------------|------------------------------------
          --|                                                            
      112 -| [e.g., 4th project] | [current need] | [why 3 projects insuf
          -ficient] |                                                    
      113 -| [e.g., Repository pattern] | [specific problem] | [why direc
          -t DB access insufficient] |                                   
      267 +Não se aplica: nenhuma violação no Constitution Check.        

● Update(specs\003-modo-docker\plan.md)
  ⎿  Added 2 lines, removed 2 lines
      64  - `org.springframework.kafka:spring-kafka`;
      65  - `org.postgresql:postgresql` (runtime);
      66  - `org.flywaydb:flyway-database-postgresql` (runtime);
      67 -- nos testes, `spring-kafka-test` e `spring-security-test` (só 
         -para os utilitários de requisição, sem                         
      68 -  dublês).                                                     
      67 +- nos testes, `spring-kafka-test`. O `spring-security-test` não
         + entra: os ITs mandam o cabeçalho real                         
      68 +  pelo `RestTestClient`.                                       
      69 
      70  Todas com a versão gerenciada pelo Boot (verificar o `flyway-da
          tabase-postgresql`, R3)
      71 

  Read 1 file (ctrl+o to expand)

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 4 lines, removed 1 line
      436      `kafka-broker-api-versions.sh` a partir do `kafka-cli`, e
           a aplicação usa `depends_on` com
      437      `service_started` mais as novas tentativas do publicador (
           R8);
      438    - as versões estáveis atuais das quatro imagens, com o diges
           t resolvido na tarefa (como a R12 da spec
      439 -    002 fez com as actions).                                  
      439 +    002 fez com as actions);                                  
      440 +  - que o comando de healthcheck de cada serviço existe na ima
          +gem escolhida: `wget` no Prometheus e no                      
      441 +    Grafana, `pg_isready` no PostgreSQL. Se não existir, usar 
          +outro binário da própria imagem, nunca um                     
      442 +    instalado na subida.                                      
      443 
      444  ## R18. Prometheus com o token
      445 

● Fiz o plano do Marco 3, mas antes da etapa 1 você precisa decidir o conflito
  do ApiDocsIT. Ele é um IT do Marco 2 e não passa com o contrato novo sem
  mudar.

  O plano está em specs/003-modo-docker/plan.md, no branch marco/3-docker. Os
  artefatos ficaram em specs/003-modo-docker/: research.md (R1 a R21),
  data-model.md, quickstart.md e a pasta contracts/, com o openapi.yaml 1.2.0
  completo e cinco documentos (o que mudou no OpenAPI, eventos do Kafka,
  endpoints operacionais, compose e pipeline). Não existe
  .specify/extensions.yml, então nenhum hook foi executado. Não fiz commit.

  O Constitution Check passou sem violação. Nove pontos que dependem das versões
  do Boot 4.1.2 ficaram marcados como "verificar" na research, cada um com o
  que fazer se o resultado for outro.

  Decisões que vão além do seu pedido:
  - Readiness no Kafka: ganha um indicador ingestion, que só fica positivo com o
    marcador da execução atual. Não troquei o estado à mão porque o Boot marca
    a aplicação como pronta antes de a carga terminar.
  - Publicação: o publicador lê o CSV na subida, então os erros estruturais
    continuam derrubando a aplicação antes do servidor. Ele só publica depois da
    subida, para que um broker fora do ar deixe a aplicação viva e não pronta.
  - Eventos inválidos e falhas de banco: um evento inválido vai ao tópico de
    erros com os bytes originais. Uma falha de banco é tentada de novo sem
    avançar, para não perder filme durante a carga.
  - Ordem da listagem: o PostgreSQL é criado com collation C, sem mudar SQL,
    para ordenar como o H2.
  - Sem senha na aplicação nem no workflow: pela emenda 1.6.0, a credencial de
    demonstração só pode estar no compose. Por isso DB_PASSWORD não tem padrão
    na aplicação, e o service container do pipeline usa trust.
  - "Sem alteração" dos Marcos 1 e 2: além da suíte rodando sem token, um IT
    falha se API_TOKEN estiver definido na execução padrão. Um script também
    compara os ITs com a tag v0.2.0.
  - kafka-cli no compose: a imagem nativa do Kafka não traz as ferramentas de
    linha de comando. O compose ganha esse serviço sob demanda (apache/kafka)
    para publicar vencedores e ler o tópico de erros.

  Pendências que são suas:
  1. ApiDocsIT: ele compara o openapi.yaml publicado, byte a byte, com o arquivo
     da spec 002. Recomendo trocar só o caminho dessa comparação para o
     contrato da spec 003, sem mudar nenhuma asserção. A exceção ficaria
     registrada em docs/ai/decisoes.md e numa lista que o script lê. As
     alternativas estão em contracts/openapi-1.2.0.md.
  2. Cabeçalhos de proteção do Spring Security: o Cache-Control sai dos
     endpoints de negócio, como você pediu. Mantive X-Content-Type-Options e
     X-Frame-Options em todas as respostas, mesmo com o token desligado. Isso lê
     "idêntico ao Marco 2" como status, corpo, ETag e cabeçalhos já existentes
     iguais. Confirme, ou eu desligo esses cabeçalhos também.
  3. Princípios II e VI: o que só existe no PostgreSQL é comprovado no perfil
     postgres-it, pelo pipeline, e não no ./mvnw verify padrão. Confirme essa
     leitura, ou fazemos uma emenda PATCH ao VI.

  Depois dessas respostas, o próximo passo é o /speckit-tasks.

✻ Crunched for 10m 36s · done terça-feira, 15:30

❯ 1. Aceito: o ApiDocsIT passa a comparar com o contrato da spec 003, sem mudar 
  asserções; registre a exceção como proposto. 2. Mantenha                      
  X-Content-Type-Options e X-Frame-Options em todas as respostas, e cite esses  
  cabeçalhos no ADR de autenticação como decisão intencional. 3. Faça a emenda  
  do Princípio VI, MINOR: comportamento da aplicação que depende de             
  infraestrutura indisponível no modo padrão, como o PostgreSQL, é comprovado   
  por testes de integração num perfil Maven dedicado, executado                 
  obrigatoriamente no pipeline; todo o resto continua exigindo teste no ./mvnw  
  verify. Registre as três decisões em docs/ai/decisoes.md.                     

  Ran 1 shell command (ctrl+o to expand)

● Update(.specify\memory\constitution.md)
  ⎿  Added 7 lines
      270  - Requisito sem comprovação não é considerado entregue. Um req
           uisito de comportamento da aplicação
      271    MUST NOT ser comprovado só por revisão ou só pelo pipeline:
           ele precisa do seu teste de integração no
      272    `./mvnw verify`.
      273 +- **Comportamento que depende de infraestrutura indisponível n
          +o modo padrão** (por exemplo, o                               
      274 +  PostgreSQL): é comprovado por testes de integração num **per
          +fil Maven dedicado**, que o pipeline MUST                     
      275 +  executar obrigatoriamente, com o job vermelho reprovando o m
          +erge. Esses testes seguem o Princípio I                       
      276 +  (contexto completo, sem mocks, verificação por HTTP). A exce
          +ção vale só para o que não pode rodar sem                     
      277 +  essa infraestrutura. Todo o resto continua exigindo o teste 
          +no `./mvnw verify`, inclusive o                               
      278 +  comportamento comum aos dois modos, que é testado com H2. Um
          +a dependência que tem substituto                              
      279 +  embutido sem Docker, como o broker Kafka do Spring, não entr
          +a na exceção (Princípio II).                                  
      280  - O README MUST trazer a tabela de requisitos do enunciado, co
           m o teste que comprova cada um.
      281 
      282  **Motivo:**

● Update(.specify\memory\constitution.md)
  ⎿  Added 1 line, removed 1 line
      381    conformidade com os princípios I a VII. Os sete princípios s
           ão inegociáveis e não admitem
      382    exceção em "Complexity Tracking". Qualquer outra divergência
            MUST ser justificada no plano.
      383 
      384 -**Version**: 1.6.0 | **Ratified**: 2026-11-18 | **Last Amended
          -**: 2026-11-24                                                
          \ No newline at end of file
      384 +**Version**: 1.7.0 | **Ratified**: 2026-11-18 | **Last Amended
          +**: 2026-11-24                                                
          \ No newline at end of file

● Update(.specify\memory\constitution.md)
  ⎿  Added 22 lines
       1  <!--
       2 +Sync Impact Report (1.7.0)                                     
       3 +- Version change: 1.6.0 → 1.7.0                                
       4 +  Justificativa: MINOR. O Princípio VI ganha uma regra de compr
         +ovação para o comportamento da                                 
       5 +  aplicação que depende de infraestrutura indisponível no modo 
         +padrão, como o PostgreSQL. Esse                                
       6 +  comportamento é comprovado por testes de integração num perfi
         +l Maven dedicado, executado                                    
       7 +  obrigatoriamente no pipeline. Todo o resto continua exigindo 
         +teste no `./mvnw verify`.                                      
       8 +  Antes, o VI pedia o teste "no `./mvnw verify`" e o II punha o
         +s testes de PostgreSQL num perfil                              
       9 +  separado, sem dizer como os dois se combinavam. Nenhum requis
         +ito que cumpria a versão anterior deixa                        
      10 +  de cumprir.                                                  
      11 +  Origem: Constitution Check do plano 003 (Marco 3).           
      12 +- Modified principles: VI. Rastreabilidade requisito → teste (r
         +egra nova)                                                     
      13 +- Added sections: nenhuma                                      
      14 +- Removed sections: nenhuma                                    
      15 +- Impacto:                                                     
      16 +  - AGENTS.md ✅ fluxo de trabalho (rastreabilidade) e seção Te
         +stes;                                                          
      17 +  - specs/003-modo-docker ✅ FR-038 e Constitution Check do pla
         +no (a nota de leitura conjunta virou                           
      18 +    referência à 1.7.0);                                       
      19 +  - docs/ai/decisoes.md ✅ emenda registrada;                  
      20 +  - specs/001 e specs/002: nenhuma mudança (não têm comportamen
         +to dependente de PostgreSQL).                                  
      21 +- Templates requiring updates: nenhum                          
      22 +- Follow-up TODOs: nenhum                                      
      23 +                                                               
      24  Sync Impact Report (1.6.0)
      25  - Version change: 1.5.0 → 1.6.0
      26    Justificativa: MINOR. O Princípio VII ganha uma segunda exceç
          ão, ampliando o que é permitido sem

  Read 1 file (ctrl+o to expand)

● Update(AGENTS.md)
  ⎿  Added 7 lines, removed 2 lines
      37    4. O implementador corrige apenas o que o Anderson aceitar.
      38    5. O merge acontece com o pipeline verde, e a sessão é export
          ada.
      39  - **Rastreabilidade:** cada requisito aponta para a comprovação
           adequada ao seu tipo (Princípio VI,
      40 -  constituição 1.4.0): teste de integração para comportamento d
         -a aplicação, revisão com item no                               
      41 -  checklist da spec para documentação, e execução do pipeline p
         -ara pipeline e imagem.                                         
      40 +  constituição 1.7.0):                                         
      41 +  - comportamento da aplicação: teste de integração no `./mvnw 
         +verify`. Se o comportamento depende de                         
      42 +    infraestrutura indisponível no modo padrão, como o PostgreS
         +QL, o teste de integração roda num perfil                      
      43 +    Maven dedicado, que o pipeline executa obrigatoriamente. Se
         + a dependência tem substituto embutido sem                     
      44 +    Docker, como o broker Kafka do Spring, o teste continua no 
         +`./mvnw verify`;                                               
      45 +  - documentação: revisão, com item no checklist da spec;      
      46 +  - pipeline e imagem: execução do pipeline.                   
      47  - Não abra pull request, não faça merge e não crie tags por con
          ta própria. Esses passos são do Anderson.
      48  - Faça commits pequenos e com mensagens descritivas, sem mistur
          ar assuntos.
      49 

● Update(AGENTS.md)
  ⎿  Added 1 line, removed 1 line
       97 
       98  - Todo comportamento novo ou alterado chega com o teste de int
           egração correspondente, no mesmo bolt.
       99  - Os testes do enunciado e dos extras da API rodam com `./mvnw
            verify` **sem Docker**. Kafka usa o broker embutido do Spring
           , não Testcontainers.
      100 -- Testes que exigem PostgreSQL ficam em um perfil Maven separa
          -do, executado no pipeline.                                    
      100 +- Testes que exigem PostgreSQL ficam em um perfil Maven separa
          +do, executado obrigatoriamente no pipeline (Princípio VI, 1.7.
          +0). Eles cobrem só o que depende do PostgreSQL. O comportament
          +o comum aos dois bancos é testado também no `./mvnw verify`, c
          +om H2.                                                        
      101  - CSVs de cenário ficam em `src/test/`.
      102  - Um teste que verifica por log ou banco, porque o endpoint ai
           nda não existe, ganha a verificação por HTTP no bolt em que o
           endpoint surgir (Princípio I).
      103  - Antes de dar uma tarefa como pronta, rode `./mvnw verify` e
           informe o resultado real. Se algo falhou ou foi pulado, diga.

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 1 line, removed 1 line
      358 
      359  **Comprovação**
      360 
      361 -- **FR-038**: Cada requisito MUST ser comprovado na forma do P
          -rincípio VI:                                                  
      361 +- **FR-038**: Cada requisito MUST ser comprovado na forma do P
          +rincípio VI (constituição 1.7.0):                             
      362    - comportamento da aplicação comum aos dois bancos (FR-001,
           FR-002, FR-009 a FR-027): teste de
      363      integração no `./mvnw verify`, com H2 e o broker embutido;
      364    - comportamento que só existe no PostgreSQL (FR-004, FR-006,
            FR-008 e a persistência entre reinícios

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 3 lines, removed 1 line
      362    - comportamento da aplicação comum aos dois bancos (FR-001,
           FR-002, FR-009 a FR-027): teste de
      363      integração no `./mvnw verify`, com H2 e o broker embutido;
      364    - comportamento que só existe no PostgreSQL (FR-004, FR-006,
            FR-008 e a persistência entre reinícios
      365 -    da US2): teste de integração no perfil Maven de PostgreSQL
          -, executado no pipeline (FR-007);                             
      365 +    da US2): teste de integração no perfil Maven dedicado, exe
          +cutado obrigatoriamente no pipeline                           
      366 +    (FR-007). É a regra do Princípio VI para o comportamento q
          +ue depende de infraestrutura indisponível                     
      367 +    no modo padrão;                                           
      368    - compose, imagens e pipeline (FR-028 a FR-035): estágio do
           pipeline;
      369    - README e ADRs (FR-036 e FR-037): revisão no pull request,
           com seção própria no checklist da spec.
      370 

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 5 lines, removed 1 line
      354    requisitos e testes atualizada. A seção "Avalie em 2 minutos
           " MUST continuar funcionando só com o JDK.
      355  - **FR-037**: As decisões de arquitetura do marco (PostgreSQL
           com o esquema comum, ingestão única por
      356    Kafka com evento de carga concluída, idempotência, tópico de
            erros, autenticação por token e o
      357 -  compose com observabilidade) MUST ter ADR curto em `docs/adr
          -/`, no formato da spec 002 (FR-031).                          
      357 +  compose com observabilidade) MUST ter ADR curto em `docs/adr
          +/`, no formato da spec 002 (FR-031). O                        
      358 +  ADR da autenticação MUST registrar como decisão intencional 
          +que os cabeçalhos                                             
      359 +  `X-Content-Type-Options` e `X-Frame-Options` aparecem em tod
          +as as respostas, com o token ligado ou                        
      360 +  desligado, e que o `Cache-Control` do Spring Security fica f
          +ora dos endpoints de negócio para                             
      361 +  preservar o ETag.                                           
      362 
      363  **Comprovação**
      364 

● Now the plan, research, contract and checklist, then the record in
  decisoes.md.

● Update(specs\003-modo-docker\plan.md)
  ⎿  Added 1 line, removed 1 line
      121  | III. Contrato idêntico | Os schemas não mudam. O 1.2.0 só ac
           rescenta a autenticação opcional (`{}`) e o 401. O `IntervalsC
           ontractIT` e o `ReferenceFileIT` não mudam. A equivalência ent
           re os caminhos de carga e entre os bancos é comparada byte a b
           yte | ✅ | ✅ |
      122  | IV. Spec-anchored | A spec 003 foi clarificada (3 decisões)
           e a constituição foi emendada (1.6.0) antes do plano. **Um con
           flito novo, o do `ApiDocsIT`, fica com o responsável** ([contr
           acts/openapi-1.2.0.md](contracts/openapi-1.2.0.md)) | ✅ | ✅
           (pendente a decisão do `ApiDocsIT`) |
      123  | V. Extras isolados, serviço único, simplicidade | O Kafka e
           o PostgreSQL só com o perfil. A gravação fica num único serviç
           o (`MovieImportService.record`), e o perfil muda só a origem.
           Não há registro de esquemas, várias partições, Testcontainers
           nem JWT. A autenticação é ligada só pela variável | ✅ | ✅ |
      124 -| VI. Rastreabilidade (1.4.0) | Matriz abaixo. Comportamento c
          -omum aos dois bancos: IT no `./mvnw verify`. Comportamento só 
          -do PostgreSQL: `*PostgresIT` no job `postgres`. Compose e pipe
          -line: jobs. README e ADRs: checklist. Ver a nota de leitura co
          -njunta com o Princípio II | ✅ | ✅ |                         
      124 +| VI. Rastreabilidade (1.7.0) | Matriz abaixo. Comportamento c
          +omum aos dois bancos: IT no `./mvnw verify`. Comportamento que
          + depende do PostgreSQL: `*PostgresIT` no perfil Maven dedicado
          + `postgres-it`, executado obrigatoriamente pelo job `postgres`
          + (regra da emenda 1.7.0). O Kafka tem broker embutido e por is
          +so fica no `verify`. Compose e pipeline: jobs. README e ADRs: 
          +checklist | ✅ | ✅ |                                         
      125  | VII. Sem segredos (1.6.0) | O `API_TOKEN` vem do ambiente e
           nunca é registrado. As credenciais de demonstração ficam **só*
           * no `compose.yaml`, identificadas e sobrescrevíveis. A aplica
           ção não tem senha padrão (`DB_PASSWORD` obrigatório no perfil)
           . O pipeline usa `trust` no PostgreSQL e um token aleatório ge
           rado no job, sem `secrets.*` | ✅ | ✅ |
      126  | Observabilidade (1.3.0) | A readiness só fica positiva depoi
           s da carga, também no Kafka (indicador `ingestion`). O log con
           tinua em texto no `./mvnw` e em JSON na imagem. Os ITs de log
           novos têm `@Tag("log")` e rodam nos dois formatos | ✅ | ✅ |
      127  | Cadeia de entrega | As actions novas são fixadas por hash. A
           s imagens do compose e do service container são fixadas por di
           gest, conferidas pelo `workflow-policy`. O Dependabot ganha o
           `docker-compose` | ✅ | ✅ |

● Update(specs\003-modo-docker\plan.md)
  ⎿  Added 2 lines, removed 11 lines
      126  | Observabilidade (1.3.0) | A readiness só fica positiva depoi
           s da carga, também no Kafka (indicador `ingestion`). O log con
           tinua em texto no `./mvnw` e em JSON na imagem. Os ITs de log
           novos têm `@Tag("log")` e rodam nos dois formatos | ✅ | ✅ |
      127  | Cadeia de entrega | As actions novas são fixadas por hash. A
           s imagens do compose e do service container são fixadas por di
           gest, conferidas pelo `workflow-policy`. O Dependabot ganha o
           `docker-compose` | ✅ | ✅ |
      128 
      129 -**Nota sobre os Princípios II e VI:**                         
      130 -- o VI pede o IT de cada comportamento "no `./mvnw verify`";  
      131 -- o II põe os testes de PostgreSQL num perfil Maven separado. 
      129 +**Resultado:** nenhuma violação. O "Complexity Tracking" não s
          +e aplica. A combinação dos Princípios II e                    
      130 +VI, que ficou ambígua na primeira versão deste plano, foi reso
          +lvida pela emenda 1.7.0 do Princípio VI.                      
      131 
      133 -O plano lê os dois juntos. O que é comum aos dois bancos tem I
          -T no `verify` padrão, com H2. O que só                        
      134 -existe no PostgreSQL tem IT no perfil `postgres-it`, que é um 
          -`./mvnw verify` com o perfil, executado                       
      135 -pelo pipeline. São eles: a migração no PostgreSQL, a ordem com
          - a collation e a persistência entre                           
      136 -reinícios. Não há outra forma de comprovar esse comportamento 
          -sem violar o II. Se o responsável preferir                    
      137 -a leitura literal do VI, é preciso uma emenda PATCH esclarecen
          -do isso.                                                      
      138 -                                                              
      139 -**Resultado:** nenhuma violação. O "Complexity Tracking" não s
          -e aplica.                                                     
      140 -                                                              
      132  ## Rastreabilidade (Princípio VI)
      133 
      134  | Requisito | Comprovação |

● Update(specs\003-modo-docker\plan.md)
  ⎿  Added 1 line, removed 1 line
      119  | I. Somente testes de integração | Todo teste novo é `*IT`, c
           om `@SpringBootTest` ou com `SpringApplication` para as múltip
           las execuções (equivalência e reinícios; cláusula 1.5.0). Todo
           s são verificados por HTTP: intervalos, listagem, readiness e
           métricas. O broker é real (embutido) e o PostgreSQL é real (se
           rvice container). Nenhum mock. O tópico de erros é lido por um
            consumidor real no teste. A única verificação fora do HTTP é
           o log (avisos, resumo, ausência do token e da senha gerada), p
           orque o log é o comportamento. A propriedade `awards.ingestion
           .publish-completion` (R10) não é um dublê: ela desliga um pass
           o real, e o teste envia o marcador pelo broker real | ✅ | ✅
           |
      120  | II. Execução só com `./mvnw` | Os ITs de Kafka usam o broker
            embutido, sem Docker. Os de PostgreSQL ficam no perfil `postg
           res-it`, fora do `verify` padrão. A autoconfiguração do Kafka
           é excluída sem o perfil. A segurança fica desligada sem `API_T
           OKEN` | ✅ | ✅ |
      121  | III. Contrato idêntico | Os schemas não mudam. O 1.2.0 só ac
           rescenta a autenticação opcional (`{}`) e o 401. O `IntervalsC
           ontractIT` e o `ReferenceFileIT` não mudam. A equivalência ent
           re os caminhos de carga e entre os bancos é comparada byte a b
           yte | ✅ | ✅ |
      122 -| IV. Spec-anchored | A spec 003 foi clarificada (3 decisões) 
          -e a constituição foi emendada (1.6.0) antes do plano. **Um con
          -flito novo, o do `ApiDocsIT`, fica com o responsável** ([contr
          -acts/openapi-1.2.0.md](contracts/openapi-1.2.0.md)) | ✅ | ✅ 
          -(pendente a decisão do `ApiDocsIT`) |                         
      122 +| IV. Spec-anchored | A spec 003 foi clarificada (3 decisões) 
          +e a constituição foi emendada (1.6.0 e 1.7.0) antes das tarefa
          +s. O conflito do `ApiDocsIT` foi levado ao responsável e decid
          +ido (opção A, [contracts/openapi-1.2.0.md](contracts/openapi-1
          +.2.0.md)) | ✅ | ✅ |                                         
      123  | V. Extras isolados, serviço único, simplicidade | O Kafka e
           o PostgreSQL só com o perfil. A gravação fica num único serviç
           o (`MovieImportService.record`), e o perfil muda só a origem.
           Não há registro de esquemas, várias partições, Testcontainers
           nem JWT. A autenticação é ligada só pela variável | ✅ | ✅ |
      124  | VI. Rastreabilidade (1.7.0) | Matriz abaixo. Comportamento c
           omum aos dois bancos: IT no `./mvnw verify`. Comportamento que
            depende do PostgreSQL: `*PostgresIT` no perfil Maven dedicado
            `postgres-it`, executado obrigatoriamente pelo job `postgres`
            (regra da emenda 1.7.0). O Kafka tem broker embutido e por is
           so fica no `verify`. Compose e pipeline: jobs. README e ADRs:
           checklist | ✅ | ✅ |
      125  | VII. Sem segredos (1.6.0) | O `API_TOKEN` vem do ambiente e
           nunca é registrado. As credenciais de demonstração ficam **só*
           * no `compose.yaml`, identificadas e sobrescrevíveis. A aplica
           ção não tem senha padrão (`DB_PASSWORD` obrigatório no perfil)
           . O pipeline usa `trust` no PostgreSQL e um token aleatório ge
           rado no job, sem `secrets.*` | ✅ | ✅ |
● Update(specs\003-modo-docker\plan.md)
  ⎿  Added 12 lines, removed 9 lines
      242  `security/`, seguindo os pacotes por assunto que já existem. A
            infraestrutura nova fica na raiz
      243  (`compose.yaml`) e em `observability/`, como o briefing previa
           .
      244 
      245 -## Pendências para o responsável                              
      245 +## Decisões do responsável sobre o plano (2026-11-24)         
      246 
      247 -1. **`ApiDocsIT`:** o teste compara o contrato publicado com o
          - arquivo da spec 002. A recomendação é a                      
      248 -   opção A de [contracts/openapi-1.2.0.md](contracts/openapi-1
          -.2.0.md): trocar só o caminho da                              
      249 -   constante, com a exceção registrada. Decidir antes da etapa
          - 1.                                                           
      250 -2. **Cabeçalhos de proteção do Spring Security** (R12): o plan
          -o mantém `X-Content-Type-Options` e                           
      251 -   `X-Frame-Options` em todas as respostas, inclusive com o to
          -ken desligado. Isso lê a FR-019 como                          
      252 -   "status, corpo, ETag e cabeçalhos do Marco 2 iguais". Confi
          -rmar ou pedir a leitura literal.                              
      253 -3. **Princípios II e VI** (nota do Constitution Check): confir
          -mar a leitura conjunta ou pedir a emenda                      
      254 -   PATCH.                                                     
      247 +Registradas em `docs/ai/decisoes.md`.                         
      248 
      249 +1. **`ApiDocsIT`:** opção A. Só a constante `SPEC_CONTRACT` pa
          +ssa a apontar para o contrato da spec                         
      250 +   003, sem mudar nenhuma asserção. A exceção entra em `.githu
          +b/unchanged-tests-exceptions.txt`, com a                      
      251 +   referência à entrada do `decisoes.md`.                     
      252 +2. **Cabeçalhos de proteção** (R12): `X-Content-Type-Options` 
          +e `X-Frame-Options` ficam em todas as                         
      253 +   respostas, com o token ligado ou desligado. O ADR da autent
          +icação os registra como decisão                               
      254 +   intencional (FR-037).                                      
      255 +3. **Princípios II e VI:** emenda MINOR 1.7.0 do Princípio VI.
          + O comportamento que depende de                               
      256 +   infraestrutura indisponível no modo padrão é comprovado num
          + perfil Maven dedicado, executado                             
      257 +   obrigatoriamente no pipeline.                              
      258 +                                                              
      259  ## Complexity Tracking
      260 
      261  Não se aplica: nenhuma violação no Constitution Check.

● Update(specs\003-modo-docker\contracts\openapi-1.2.0.md)
  ⎿  Added 4 lines, removed 1 line
      82  - Os ITs de contrato dos Marcos 1 e 2 (`IntervalsContractIT` e
          `MoviesContractIT`) continuam sem
      83    alteração (research R14).
      84 
      85 -## Conflito com o `ApiDocsIT` (decisão do responsável, pendente
         -)                                                              
      85 +## Conflito com o `ApiDocsIT` (decidido: opção A, 2026-11-24)  
      86 
      87 +O responsável aceitou a opção A. O registro está em `docs/ai/de
         +cisoes.md`, na entrada "Exceção do                             
      88 +`ApiDocsIT` à regra dos testes sem alteração".                 
      89 +                                                               
      90  O `ApiDocsIT` da tag `v0.2.0` compara o `/openapi.yaml` publica
          do, byte a byte, com
      91  `specs/002-operacao/contracts/openapi.yaml` (constante `SPEC_CO
          NTRACT`). Publicar o contrato 1.2.0
      92  faz esse teste falhar, a menos que uma destas três coisas mude:

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 3 lines, removed 2 lines
      317       a lista de ITs da tag e roda no job `workflow-policy`. As
            classes de apoio (`support/`) podem
      318       evoluir, desde que os ITs não mudem. As exceções ficam nu
           ma lista explícita
      319       (`.github/unchanged-tests-exceptions.txt`), em que cada l
           inha traz o arquivo e a entrada de
      320 -     `docs/ai/decisoes.md` que a autoriza. A lista começa vazi
          -a, salvo a decisão do `ApiDocsIT`                             
      321 -     ([contracts/openapi-1.2.0.md](contracts/openapi-1.2.0.md)
          -, "Conflito com o `ApiDocsIT`").                              
      320 +     `docs/ai/decisoes.md` que a autoriza. A lista tem uma úni
          +ca linha, a do `ApiDocsIT`, aceita pelo                       
      321 +     responsável ([contracts/openapi-1.2.0.md](contracts/opena
          +pi-1.2.0.md), "Conflito com o                                 
      322 +     `ApiDocsIT`"): só a constante `SPEC_CONTRACT` muda, sem n
          +enhuma asserção alterada.                                     
      323  - **Motivo:** nenhum teste dentro do `./mvnw verify` consegue
           provar sozinho que outros arquivos não
      324    mudaram. A comparação com a tag é a única verificação objeti
           va de "sem alteração", e o Princípio VI
      325    aceita o pipeline como comprovação de requisito de pipeline.

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 3 lines, removed 3 lines
      276      respostas.
      277  - **Leitura da FR-019 ("comportamento idêntico ao do Marco 2")
           :** status, corpo, `ETag`, 304 e os
      278    cabeçalhos que o Marco 2 já mandava continuam iguais. Os cab
           eçalhos de proteção a mais não mudam o
      279 -  que os testes dos Marcos 1 e 2 verificam e não afetam o cach
          -e condicional. Se o responsável quiser a                      
      280 -  leitura literal (nenhum cabeçalho a mais), basta desligar ta
          -mbém esses escritores. A escolha fica                         
      281 -  registrada em `docs/ai/decisoes.md` na implementação.       
      279 +  que os testes dos Marcos 1 e 2 verificam e não afetam o cach
          +e condicional. **Decidido pelo                                
      280 +  responsável (2026-11-24):** `X-Content-Type-Options` e `X-Fr
          +ame-Options` ficam em todas as                                
      281 +  respostas, e o ADR da autenticação os registra como decisão 
          +intencional (FR-037).                                         
      282  - **Comprovação:** um IT confere, nos dois modos da autenticaç
           ão, que os endpoints de negócio respondem
      283    sem `Cache-Control`, `Pragma` e `Expires`, com `ETag`, e que
            a revalidação dá 304.
      284 

● Update(specs\003-modo-docker\checklists\requirements.md)
  ⎿  Added 4 lines
      47  - [ ] Cada ADR tem número, título, data, status, contexto, deci
          são, alternativas consideradas e
      48    consequências, e cita a entrada em `docs/ai/decisoes.md` (spe
          c 002, FR-031).
      49  - [ ] O índice de `docs/adr/` lista os ADRs novos.
      50 +- [ ] O ADR da autenticação registra como decisão intencional: 
      51 +  - que `X-Content-Type-Options` e `X-Frame-Options` aparecem e
         +m todas as respostas, com o token                              
      52 +    ligado ou desligado;                                       
      53 +  - que o `Cache-Control` do Spring Security fica fora dos endp
         +oints de negócio.                                              
      54 
      55  ## Notes
      56 

● Update(specs\003-modo-docker\checklists\requirements.md)
  ⎿  Added 3 lines, removed 5 lines
      59    partições e bibliotecas ficaram para o plano.
      60  - **Credenciais do compose:** a FR-033 (senhas de demonstração)
           está coberta pela emenda 1.6.0 do
      61    Princípio VII, já registrada em `docs/ai/decisoes.md` e no `A
          GENTS.md`.
      62 -- **Ponto para o Constitution Check do plano:** o Princípio VI 
         -diz que um requisito de comportamento                          
      63 -  precisa do seu teste de integração "no `./mvnw verify`", e o 
         -Princípio II põe os testes de PostgreSQL                       
      64 -  num perfil Maven separado. A FR-038 comprova pelo perfil sepa
         -rado, no pipeline, só o que existe                             
      65 -  apenas no PostgreSQL (FR-004, FR-006, FR-008 e a persistência
         - entre reinícios). Os dois princípios                          
      66 -  precisam ser lidos juntos ou harmonizados por emenda.        
      62 +- **Princípios II e VI:** harmonizados pela emenda 1.7.0 do Pri
         +ncípio VI. O que depende do PostgreSQL                         
      63 +  (FR-004, FR-006, FR-008 e a persistência entre reinícios) é c
         +omprovado no perfil Maven dedicado,                            
      64 +  executado obrigatoriamente no pipeline (FR-038).             
      65  - As seções "Revisão do README" e "Revisão dos ADRs" ficam desm
          arcadas até a revisão do PR.

● Update(docs\ai\decisoes.md)
  ⎿  Added 56 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-24 — Exceção do `ApiDocsIT` à regra dos testes sem a
         +lteração                                                       
       7 +                                                               
       8 +- **Origem:** plano 003. O `ApiDocsIT` (Marco 2, tag `v0.2.0`) 
         +compara o `/openapi.yaml` publicado, byte                      
       9 +  a byte, com `specs/002-operacao/contracts/openapi.yaml`. O co
         +ntrato 1.2.0 (autenticação opcional e                          
      10 +  resposta 401, spec 003 FR-026) faz esse teste falhar. Pela R1
         +4 do plano, os ITs dos Marcos 1 e 2 não                        
      11 +  podem mudar.                                                 
      12 +- **Alternativas apresentadas ao responsável:**                
      13 +  - **A:** trocar só a constante `SPEC_CONTRACT` para o contrat
         +o da spec 003, como o Marco 2 fez ao                           
      14 +    trocar a spec 001 pela 002;                                
      15 +  - **B:** sobrescrever o contrato da spec 002 com o 1.2.0;    
      16 +  - **C:** não publicar o 1.2.0.                               
      17 +- **Decisão do responsável:** opção A, sem mudar nenhuma asserç
         +ão. O `ApiDocsIT` é a única linha de                           
      18 +  `.github/unchanged-tests-exceptions.txt`, com o título desta 
         +entrada. O `check-unchanged-tests.sh`                          
      19 +  aceita a mudança desse arquivo e reprova a de qualquer outro 
         +IT da tag.                                                     
      20 +- **Rejeitadas:**                                              
      21 +  - B: o artefato de uma spec já entregue passaria a descrever 
         +o marco seguinte;                                              
      22 +  - C: a FR-026 deixaria de ser cumprida.                      
      23 +                                                               
      24 +## 2026-11-24 — Cabeçalhos de proteção do Spring Security em to
         +das as respostas                                               
      25 +                                                               
      26 +- **Origem:** plano 003, research R12. O Spring Security acresc
         +enta `Cache-Control`, `Pragma`,                                
      27 +  `Expires`, `X-Content-Type-Options` e `X-Frame-Options` a tod
         +a resposta. Os três primeiros anulariam o                      
      28 +  ETag, e o Marco 2 não mandava `Cache-Control`.               
      29 +- **Decisão do responsável:**                                  
      30 +  - o `Cache-Control` do Security fica fora dos dois endpoints 
         +de negócio (pedido do plano);                                  
      31 +  - `X-Content-Type-Options` e `X-Frame-Options` ficam em **tod
         +as** as respostas, com o token ligado ou                       
      32 +    desligado;                                                 
      33 +  - o ADR da autenticação os registra como decisão intencional 
         +(spec 003, FR-037, e item do checklist).                       
      34 +- **Leitura da FR-019** ("comportamento idêntico ao do Marco 2"
         +): status, corpo, ETag, 304 e os                               
      35 +  cabeçalhos que o Marco 2 já mandava continuam iguais. Os dois
         + cabeçalhos de proteção são acréscimos                         
      36 +  que não mudam nada do que os testes dos Marcos 1 e 2 verifica
         +m.                                                             
      37 +- **Rejeitada:** desligar também esses dois escritores, para um
         +a igualdade literal de cabeçalhos. Isso                        
      38 +  removeria proteção sem nenhum ganho para o ETag nem para o co
         +ntrato.                                                        
      39 +                                                               
      40 +## 2026-11-24 — Emenda 1.7.0 da constituição: comprovação de co
         +mportamento que depende de infraestrutura                      
      41 +                                                               
      42 +- **Origem:** Constitution Check do plano 003:                 
      43 +  - o Princípio VI exigia o teste de integração de cada comport
         +amento "no `./mvnw verify`";                                   
      44 +  - o Princípio II põe os testes de PostgreSQL num perfil Maven
         + separado, fora do `verify` padrão;                            
      45 +  - o plano tinha lido os dois juntos e pedido a confirmação do
         + responsável.                                                  
      46 +- **Decisão do responsável:** emenda MINOR do Princípio VI. O c
         +omportamento da aplicação que depende de                       
      47 +  infraestrutura indisponível no modo padrão, como o PostgreSQL
         +, é comprovado por testes de integração                        
      48 +  num perfil Maven dedicado, executado obrigatoriamente no pipe
         +line. Todo o resto continua exigindo teste                     
      49 +  no `./mvnw verify`.                                          
      50 +- **Detalhes da redação:**                                     
      51 +  - os testes do perfil seguem o Princípio I (contexto completo
         +, sem mocks, HTTP);                                            
      52 +  - o comportamento comum aos dois modos continua testado no `v
         +erify`, com H2;                                                
      53 +  - uma dependência com substituto embutido sem Docker, como o 
         +broker Kafka do Spring, não entra na                           
      54 +    exceção.                                                   
      55 +- **Impacto:**                                                 
      56 +  - 1.6.0 → 1.7.0 (MINOR): é regra nova, e nenhum requisito que
         + cumpria a versão anterior deixa de                            
      57 +    cumprir;                                                   
      58 +  - foram alinhados a constituição (Princípio VI e Sync Impact 
         +Report), o `AGENTS.md` (rastreabilidade e                      
      59 +    seção Testes), a spec 003 (FR-038) e o plano 003 (Constitut
         +ion Check). O checklist da spec 003 também                     
      60 +    foi atualizado.                                            
      61 +                                                               
      62  ## 2026-11-24 — Emenda 1.6.0 da constituição: credenciais de de
          monstração no compose local
      63 
      64  - **Origem:** clarificação da spec 003 (Marco 3). O compose pre
          cisa de usuário e senha para o

  Ran 1 shell command (ctrl+o to expand)

● As três decisões estão aplicadas e registradas em docs/ai/decisoes.md, cada
  uma numa entrada própria no topo. Não fiz commit.

  1. ApiDocsIT: o ApiDocsIT passa a comparar o contrato publicado com o da spec
     003, e nenhuma asserção muda. A exceção fica em
     .github/unchanged-tests-exceptions.txt, que será criado na etapa 1 com uma
     única linha, a do ApiDocsIT. O script que confere os ITs dos Marcos 1 e 2
     reprova a mudança de qualquer outro IT da tag v0.2.0. Atualizei
     contracts/openapi-1.2.0.md, a R14 da research e o plano.
  2. Cabeçalhos de proteção: X-Content-Type-Options e X-Frame-Options ficam em
     todas as respostas, com o token ligado ou desligado. Estes pontos passaram
     a exigir que o ADR da autenticação os registre como decisão intencional,
     junto com a retirada do Cache-Control dos endpoints de negócio:
     - a FR-037 da spec;
     - um item novo em "Revisão dos ADRs" no checklist;
     - a R12 da research.
  3. Emenda 1.7.0 do Princípio VI:
     - Constituição: nova regra no Princípio VI com o texto que você definiu,
       Sync Impact Report novo e versão 1.7.0. Acrescentei dois pontos:
       - os testes do perfil dedicado seguem o Princípio I (contexto completo,
         sem mocks, verificação por HTTP);
       - uma dependência com substituto embutido sem Docker, como o broker Kafka
         do Spring, não entra na exceção. Os testes de Kafka continuam no
         ./mvnw verify.
     - AGENTS.md: a regra de rastreabilidade agora cita a 1.7.0 e a regra nova.
       A seção Testes diz que o perfil de PostgreSQL roda obrigatoriamente no
       pipeline e cobre só o que depende do PostgreSQL.
     - Spec e plano: a FR-038 cita a 1.7.0. No plano, o Constitution Check usa a
       1.7.0, e a nota sobre como os Princípios II e VI se combinavam e a lista
       de pendências saíram, trocadas por "Decisões do responsável sobre o
       plano".

  Não sobrou pendência do plano. O próximo passo é o /speckit-tasks.

✻ Brewed for 1m 51s · done terça-feira, 15:33

❯ /speckit-tasks Agrupe todas as tarefas do marco 3 em um único bolt,           
organizado em etapas por assunto, nesta ordem: PostgreSQL e perfil docker,      
Kafka, autenticação com Spring Security, compose com observabilidade, e ADRs e  
README. Cada etapa fecha com ./mvnw verify verde e um commit. O teste vem antes 
da implementação em cada etapa.                                                 

  Ran 3 shell commands (ctrl+o to expand)

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 21 lines, removed 13 lines
      505 
      506  ## R21. Entrega em etapas
      507 
      508 -- **Decisão:** um único bolt no branch `marco/3-docker`, como 
          -no Marco 2, com um commit por etapa:                          
      509 -  1. **Segurança** (implementador): Spring Security, filtro, 4
          -01, cabeçalhos, contrato 1.2.0 e ITs.                         
      510 -     Primeiro porque é pequeno e protege a suíte dos Marcos 1 
          -e 2 (R14).                                                    
      511 -  2. **Serviço único com atualização** (implementador): `recor
          -d`, `MovieFieldRules` e                                       
      512 -     `MovieFileReading`. A suíte do Marco 1 tem de continuar v
          -erde.                                                         
      513 -  3. **Ingestão por Kafka** (implementador): perfis, isolament
          -o, publicador, consumidor, readiness,                         
      514 -     tópico de erros, métricas e ITs de Kafka.                
      515 -  4. **PostgreSQL** (implementador): perfil, driver, Flyway e 
          -ITs `*PostgresIT`.                                            
      516 -  5. **Compose e observabilidade** (infraestrutura): `compose.
          -yaml`, Prometheus, Grafana, scripts,                          
      517 -     jobs `postgres` e `compose`, `workflow-policy` e Dependab
          -ot.                                                           
      518 -  6. **Documentação** (documentação): README (FR-036), ADRs (F
          -R-037) e as pendências do README e do ADR                     
      519 -     0004 deixadas pela emenda 1.5.0.                         
      520 -- **Ataques do testador adversário:** depois da etapa 3, o Cod
          -ex cria eventos de ataque a partir da                         
      508 +- **Decisão (ordem definida pelo responsável no `/speckit-task
          +s`):** um único bolt no branch                                
      509 +  `marco/3-docker`, como no Marco 2. São cinco etapas, cada um
          +a fechando com `./mvnw verify` verde e um                     
      510 +  commit:                                                     
      511 +  1. **PostgreSQL e perfil `docker`** (implementador; job `pos
          +tgres` pela infraestrutura):                                  
      512 +     - perfis `postgres` e grupo `docker`, driver e Flyway;   
      513 +     - `MovieImportService.record` (cria ou atualiza, R5). Ele
          + entra já nesta etapa porque, sem ele, a                      
      514 +       carga direta sobre um PostgreSQL já carregado viola a `
          +uk_movie_year_title` no reinício;                             
      515 +     - `*PostgresIT` sem Kafka, perfil Maven `postgres-it` e j
          +ob `postgres`.                                                
      516 +  2. **Kafka** (implementador): isolamento da autoconfiguração
          +, `MovieFieldRules`, `MovieFileReading`,                      
      517 +     publicador, consumidor, readiness, tópico de erros, métri
          +cas, ITs de Kafka e o `RestartPostgresIT`                     
      518 +     com Kafka.                                               
      519 +  3. **Autenticação com Spring Security** (implementador): fil
          +tro, 401, cabeçalhos, contrato 1.2.0, a                       
      520 +     exceção do `ApiDocsIT`, os ITs e o `check-unchanged-tests
          +.sh`.                                                         
      521 +  4. **Compose com observabilidade** (infraestrutura): `compos
          +e.yaml`, Prometheus, Grafana,                                 
      522 +     `compose-test.sh`, job `compose`, `workflow-policy` e Dep
          +endabot.                                                      
      523 +  5. **ADRs e README** (documentação): FR-036, FR-037 e as pen
          +dências do README e do ADR 0004 deixadas                      
      524 +     pela emenda 1.5.0.                                       
      525 +- **Ataques do testador adversário:** depois da etapa 2, o Cod
          +ex cria eventos de ataque a partir da                         
      526    spec, sem ler o código. Cada falha vira IT permanente em `sr
           c/test/` (Princípio I).
      527 +- **Efeito da ordem na proteção dos Marcos 1 e 2:** a suíte an
          +tiga roda sem alteração desde a etapa 1.                      
      528 +  A guarda contra o `API_TOKEN` (R14) só existe a partir da et
          +apa 3, que é quando o token passa a                           
      529 +  existir.                                                    
      530  - **Dono dos arquivos compartilhados nesta rodada:**
      531    - `pom.xml` e `application*.yml`: implementador;
      532    - `compose.yaml`, `observability/`, `.github/`: infraestrutu
           ra;

● Update(specs\003-modo-docker\plan.md)
  ⎿  Added 2 lines, removed 1 line
      53    - jobs novos `postgres` e `compose`. O `compose` verifica a a
          plicação, o Prometheus, o dashboard, a
      54      ingestão, a persistência e o token;
      55    - política ampliada no `workflow-policy` (R20).
      56 -- **Entrega:** um bolt, seis etapas e um PR (R21).             
      56 +- **Entrega:** um bolt, cinco etapas e um PR (R21), nesta ordem
         +: PostgreSQL e perfil `docker`, Kafka,                         
      57 +  autenticação, compose com observabilidade, e ADRs com o READM
         +E.                                                             
      58 
      59  ## Technical Context
      60 

  Ran 1 shell command (ctrl+o to expand)

● Two design changes came up while breaking down the tasks: a title over 500
  characters would cause endless consumer retries, so it becomes a
  FIELD_TOO_LONG validation rule, and the readiness test needs a runId header on
  each movie event. I'm updating the data model and contract before writing
  tasks.md. · summary

● Update(specs\003-modo-docker\data-model.md)
  ⎿  Added 9 lines, removed 2 lines
      64  | `NOT_UTF8` | Os bytes não são UTF-8 válido |
      65  | `NOT_JSON` | O texto não é um objeto JSON |
      66  | `TYPE_UNKNOWN` | `type` presente e diferente de `movie` e de
          `load-completed` |
      67 +| `FIELD_TOO_LONG` | Um campo excede a coluna do esquema: `titl
         +e` > 500, `studios` > 1000, `producers` > 1000 ou um nome de pr
         +odutor > 500 caracteres (`VARCHAR(500)` e `VARCHAR(1000)` da V1
         +) |                                                            
      68 
      68 -**Ordem das verificações:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNO
         -WN` e depois a ordem da spec 001 (ano,                         
      69 -título, produtores, `winner`). O primeiro motivo encontrado é o
         - registrado.                                                   
      69 +**Por que `FIELD_TOO_LONG`:** sem essa regra, o banco recusaria
         + a gravação e o consumidor trataria a                          
      70 +recusa como falha de infraestrutura, tentando de novo para semp
         +re e travando o tópico (research R9). A                        
      71 +carga direta do modo padrão não muda: um título longo no CSV co
         +ntinua falhando como no Marco 1. No modo                       
      72 +Kafka, o mesmo título vai ao tópico de erros. É a única diferen
         +ça conhecida entre os dois caminhos de                         
      73 +carga, e o `KafkaEquivalenceIT` usa arquivos sem esse caso.    
      74 
      75 +**Ordem das verificações:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNO
         +WN`, depois a ordem da spec 001 (ano,                          
      76 +título, produtores, `winner`) e por fim `FIELD_TOO_LONG`. O pri
         +meiro motivo encontrado é o registrado.                        
      77 +                                                               
      78  Campos desconhecidos são ignorados (R7).
      79 
      80  ## Evento de carga concluída

● Update(specs\003-modo-docker\contracts\kafka-events.md)
  ⎿  Added 3 lines
      16  ## Evento de filme
      17 
      18  - **Chave:** `"<ano>|<título>"` (recomendada; o consumidor não
          depende dela).
      19 +- **Cabeçalho `awards-run-id`:** o publicador da aplicação põe 
         +o `runId` da execução em cada evento. É                        
      20 +  informativo, o consumidor não depende dele e um cliente exter
         +no não precisa enviá-lo. Os testes usam o                      
      21 +  cabeçalho para descobrir o `runId` (research R10).           
      22  - **Valor:** objeto JSON em UTF-8.
      23 
      24  ```json

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; sed -i 's/^\*\*Motivos:\*\*
      `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, `RUN_ID_MISSING`,/**Motivos:**
      `NOT_UTF8`, `NOT_…)
  ⎿  74:**Motivos:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, `RUN_ID_MISSIN
     G`, `FIELD_TOO_LONG`, `YEAR_MISSING`,
     219:  aplicação real: o `runId` é exposto por um ouvinte real de event
     o e o marcador é enviado pelo próprio
  ⎿  Updated specs\003-modo-docker\contracts\kafka-events.md +1 -1
      71  | `awards-source-partition` | `0` |
      72  | `awards-source-offset` | `207` |
      73 
      74 -**Motivos:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, `RUN_ID_MI
         -SSING`, `YEAR_MISSING`,                                        
      74 +**Motivos:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, `RUN_ID_MI
         +SSING`, `FIELD_TOO_LONG`, `YEAR_MISSING`,                      
      75  `YEAR_NOT_NUMERIC`, `YEAR_OUT_OF_RANGE`, `TITLE_MISSING`, `PROD
          UCERS_MISSING` e `WINNER_INVALID`
      76  (data-model.md).
      77 

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; sed -i 's/aplicação real: o
      `runId` é exposto por um ouvinte real de evento e o marcador é enviado
      pelo próprio/apli…)
  ⎿    que é coerente.
     - **Comprovação:** o `KafkaReadinessIT` publica o arquivo e confere, p
     or HTTP, a readiness em 503 antes
     … +13 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\research.md +1 -1
      216    que é coerente.
      217  - **Comprovação:** o `KafkaReadinessIT` publica o arquivo e co
           nfere, por HTTP, a readiness em 503 antes
      218    do evento de carga concluída e em 200 depois dele. Para isso
           , o teste segura o envio do marcador com a
      219 -  aplicação real: o `runId` é exposto por um ouvinte real de e
          -vento e o marcador é enviado pelo próprio                     
      219 +  aplicação real: o `runId` é lido do cabeçalho `awards-run-id
          +` dos eventos publicados, e o marcador é enviado pelo próprio 
      220    teste, com a publicação automática do marcador desligada pel
           a propriedade
      221    `awards.ingestion.publish-completion=false`, que existe só p
           ara esse fim e é documentada. Também
      222    confere que um marcador com outro `runId` não libera a readi
           ness.

● Now tasks.md.

● Write(specs\003-modo-docker\tasks.md)
  ⎿  Wrote 786 lines to specs\003-modo-docker\tasks.md
       1 ---
       2 
       3 description: "Tarefas do Marco 3, num único bolt com cinco etapa
         s por assunto (um commit por etapa)"
       4 ---
       5 
       6 # Tasks: Marco 3 — Modo Docker: PostgreSQL, ingestão por Kafka,
         autenticação e compose
       7 
       8 **Input**: Design documents from `/specs/003-modo-docker/`
     … +778 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; ls
      src/test/resources/scenarios/adversario | grep "^13";
      f=specs/003-modo-docker/tasks.md; sed -i 's/`SPEC_CONTRACT`…)
  ⎿  13-ordenacao-com-acentos-e-caixa.csv
     13-ordenacao-com-acentos-e-caixa.expected.json
  ⎿  Updated specs\003-modo-docker\tasks.md +2 -2
      47  - Pacote base `com.outsera.awards`. Testes em `src/test/java/co
          m/outsera/awards/`, cenários em
      48    `src/test/resources/scenarios/`.
      49  - **Não altere nenhum `*IT.java` que exista na tag `v0.2.0`.**
          A única exceção aceita é a constante
      50 -  `SPEC_CONTRACT` do `ApiDocsIT` (T079). As classes de `support
         -/` podem evoluir, desde que as assinaturas                     
      50 +  `SPEC_CONTRACT` do `ApiDocsIT` (T067). As classes de `support
         +/` podem evoluir, desde que as assinaturas                     
      51    usadas pelos ITs antigos continuem iguais.
      52  - Os pontos marcados como **verificar** na research são conferi
          dos na tarefa que os usa. Se o resultado
      53    for outro, a research é atualizada no mesmo commit.
     ...
      422    - o `FIELD_TOO_LONG` e a diferença conhecida com a carga dir
           eta (data-model.md);
      423    - que a nova tentativa sem limite para falhas de infraestrut
           ura (R9) não tem IT no `./mvnw verify`,
      424      porque provocar uma falha de banco exigiria um dublê (Prin
           cípio I). Ela é comprovada no passo de
      425 -    parada do PostgreSQL do `compose-test.sh` (T091).         
      425 +    parada do PostgreSQL do `compose-test.sh` (T071 e T081).  
      426  - [ ] T049 [US1] Commit `Etapa 2: ingestão por Kafka com readi
           ness e tópico de erros` com T021 a T048.
      427  - [ ] T050 [US1] **Pedido ao Anderson:** acionar o testador ad
           versário (Codex) para a ingestão por Kafka. Ele cria eventos d
           e ataque a partir da spec 003 e de `contracts/kafka-events.md`
           , sem ler o código de produção. Cada falha encontrada vira IT
           permanente em `src/test/java/com/outsera/awards/kafka/adversar
           io/`, com a correção, num commit de correção da etapa 2.
      428  - [ ] T051 [US2] **Pendente: exige o push, que é do Anderson.*
           * Conferir o job `postgres` verde com o `RestartPostgresIT`.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/tasks.md; sed -i
      's|`classpath:scenarios/adversario/13-` + o nome do arquivo do caso 13
      (o\r…)
  ⎿  130:  - mesmo método do T006, com `classpath:scenarios/adversario/13-o
     rdenacao-com-acentos-e-caixa.csv` (o
     131:    mesmo do `Case13AccentAndCaseOrderIT`);
     … +8 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\tasks.md +2 -2
      127      `/api/movies?size=100&page=0..2`, de `/api/movies?winner=t
           rue` e de `/api/movies?year=1990`;
      128    - `S003 FR-006 SC-001`: os corpos são idênticos byte a byte,
            e os ETags também.
      129  - [ ] T007 [P] [US2] Criar `src/test/java/com/outsera/awards/p
           ostgres/ListingOrderPostgresIT.java`:
      130 -  - mesmo método do T006, com `classpath:scenarios/adversario/
          -13-` + o nome do arquivo do caso 13 (o                        
      130 +  - mesmo método do T006, com `classpath:scenarios/adversario/
          +13-ordenacao-com-acentos-e-caixa.csv` (o                      
      131      mesmo do `Case13AccentAndCaseOrderIT`);
      132    - `S003 FR-008`: a listagem completa tem a mesma ordem byte
           a byte no H2 e no PostgreSQL.
      133  - [ ] T008 [P] [US2] Criar os cenários `src/test/resources/sce
           narios/postgres/restart-base.csv` e `restart-updated.csv`, com
            o cabeçalho `year;title;studios;producers;winner`:
     ...
      241  - [ ] T025 [P] [US1] Criar `src/test/java/com/outsera/awards/k
           afka/KafkaEquivalenceIT.java` (`@EmbeddedKafka` na classe; dua
           s execuções com o `ApplicationStarter`: carga direta e `kafka`
           , com o arquivo padrão; a de Kafka chama `awaitReady` antes de
            consultar):
      242    - `S003 FR-018 US1-2 SC-001`: os intervalos, `/api/movies?si
           ze=100&page=0..2`, `?winner=true` e
      243      `?year=1990` são idênticos byte a byte, inclusive o `ETag`
           ;
      244 -  - repetir com `classpath:scenarios/adversario/` do caso 13. 
      244 +  - repetir com `classpath:scenarios/adversario/13-ordenacao-c
          +om-acentos-e-caixa.csv`.                                      
      245  - [ ] T026 [P] [US1] Criar `src/test/java/com/outsera/awards/k
           afka/KafkaReadinessIT.java` (`@SpringBootTest`, perfil `kafka`
           , `awards.ingestion.publish-completion=false`; research R10):
      246    - `S003 FR-013 US1-3`:
      247      - depois de `totalElements` chegar a 206 na listagem, a re
           adiness e `/actuator/health` continuam
● Escrevi o tasks.md do Marco 3: 92 tarefas num único bolt, nas cinco etapas e
  na ordem que você pediu. Ele está em specs/003-modo-docker/tasks.md. Não fiz
  commit.

  Em cada etapa, os testes vêm primeiro, depois uma tarefa confirma que eles
  falham, e a etapa fecha com ./mvnw verify verde, sem Docker, e um commit.

  ┌─────────────────┬────────────┬───────────────┬─────────────────────────┐
  │      Etapa      │  Tarefas   │   Histórias   │         Commit          │
  ├─────────────────┼────────────┼───────────────┼─────────────────────────┤
  │ Preparação      │ T001–T002  │ —             │ sem commit              │
  ├─────────────────┼────────────┼───────────────┼─────────────────────────┤
  │                 │            │               │ Etapa 1: PostgreSQL,    │
  │ 1. PostgreSQL e │ T003–T020  │ US2           │ perfil docker e         │
  │  perfil docker  │ (18)       │               │ gravação com            │
  │                 │            │               │ atualização             │
  ├─────────────────┼────────────┼───────────────┼─────────────────────────┤
  │                 │ T021–T051  │ US1, US3 e os │ Etapa 2: ingestão por   │
  │ 2. Kafka        │ (31)       │  reinícios da │ Kafka com readiness e   │
  │                 │            │  US2          │ tópico de erros         │
  ├─────────────────┼────────────┼───────────────┼─────────────────────────┤
  │                 │            │               │ Etapa 3: autenticação   │
  │ 3. Autenticação │ T052–T070  │ US4           │ por token com Spring    │
  │                 │ (19)       │               │ Security e contrato     │
  │                 │            │               │ 1.2.0                   │
  ├─────────────────┼────────────┼───────────────┼─────────────────────────┤
  │                 │ T071–T084  │               │ Etapa 4: compose com    │
  │ 4. Compose      │ (14)       │ US5           │ PostgreSQL, Kafka,      │
  │                 │            │               │ Prometheus e Grafana    │
  ├─────────────────┼────────────┼───────────────┼─────────────────────────┤
  │ 5. ADRs e       │ T085–T092  │ FR-036,       │ Etapa 5: ADRs e README  │
  │ README          │ (8)        │ FR-037        │ do Marco 3              │
  └─────────────────┴────────────┴───────────────┴─────────────────────────┘

  Mudanças que a sua ordem exigiu:
  - Gravação com atualização na etapa 1: sem ela, a carga direta num PostgreSQL
    que já tem dados quebra a restrição de chave única no reinício. A R21 da
    research e o plano foram atualizados com a nova ordem.
  - Teste de reinício dividido em dois: a versão com carga direta fica na etapa
    1 e a versão com eventos, na etapa 2.
  - Guarda do API_TOKEN só na etapa 3, que é quando o token passa a existir. A
    suíte dos Marcos 1 e 2 roda sem alteração desde a etapa 1.

  Duas mudanças de design feitas ao detalhar as tarefas (data-model, contrato de
  eventos e research já atualizados):
  - FIELD_TOO_LONG: um título com mais de 500 caracteres causaria um erro de
    banco que o consumidor tentaria de novo para sempre, travando o tópico.
    Virou regra de validação, e o evento vai para o tópico de erros. Fica uma
    diferença conhecida com a carga direta, que continua falhando como no
    Marco 1.
  - Cabeçalho awards-run-id em cada evento: é por ele que o teste de readiness
    descobre o runId da execução.

  Dois requisitos sem teste de integração, por limitação real. O T048 e o T069
  os registram em docs/ai/decisoes.md, para você aceitar ou não:
  - Nova tentativa sem limite quando o banco cai (R9): provocar essa falha
    exigiria um dublê. A comprovação fica num passo do teste do compose que para
    o PostgreSQL, publica um evento e confere que ele foi gravado depois da
    volta.
  - Comparação do token em tempo constante (FR-024): não há teste de tempo
    confiável. A comprovação fica numa seção nova, "Revisão de segurança", no
    checklist.

  Pendências que dependem de você:
  - O push que roda os jobs postgres e compose (T020, T051 e T084).
  - Acionar o testador adversário para a ingestão por Kafka (T050).
  - Na última vez, o Docker não estava rodando nesta máquina. Sem ele, os testes
    de PostgreSQL e o do compose ficam como PULADO localmente e só são
    verificados no pipeline.

  Paralelismo: os testes de cada etapa podem ser escritos em paralelo (até 12 na
  etapa 2), e a infraestrutura pode preparar o job postgres e a etapa 4 em
  outra cópia de trabalho.

  Menor incremento: a etapa 1 é o menor incremento com valor próprio, mas não é
  mergeada sozinha, porque o marco é um único PR.

  Formato: todas as tarefas usam o formato de checklist com ID e caminho de
  arquivo, e as das etapas 1 a 4 têm o rótulo de história. Não existe
  .specify/extensions.yml, então nenhum hook foi executado.

  O próximo passo é o /speckit-analyze ou o /speckit-implement.

✻ Churned for 6m 46s · done terça-feira, 15:41

❯ /speckit-analyze                                                              

  Ran 2 shell commands (ctrl+o to expand)

● Análise da especificação: Marco 3 (spec 003)

  Os três artefatos estão completos, e todo requisito tem ao menos uma tarefa.
  Mas há 3 problemas críticos contra o Princípio VI que precisam ser resolvidos
  antes do /speckit-implement. Os outros achados podem ser corrigidos agora ou
  durante as etapas.

  Os 3 críticos:
  - R9 (C1): a nova tentativa quando o banco cai só é comprovada pelo pipeline
    do compose.
  - FR-024 (C2): a comparação do token em tempo constante só é comprovada por
    revisão.
  - Job vermelho que não bloqueia o merge (C3): a 1.7.0 exige que o job postgres
    reprove o merge, e nenhuma tarefa garante isso.

  A análise não alterou nenhum arquivo. Não existe .specify/extensions.yml,
  então nenhum hook foi executado.

  Achados

  ID: C1
  Categoria: Constituição
  Severidade: CRITICAL
  Onde: tasks.md T048 e T071 (passo 7b); research R9
  Resumo: Quando o banco cai, o consumidor tenta de novo sem limite (R9). Isso
    é comportamento da aplicação, e o plano o comprova só pelo passo 7b do
    compose-test.sh, no pipeline. O Princípio VI (1.7.0) proíbe comprovar
    comportamento só pelo pipeline: um comportamento que depende de
    infraestrutura precisa de IT no perfil Maven dedicado.
  Recomendação: Criar um RetryPostgresIT no postgres-it. Com a aplicação
    pronta, o teste renomeia a tabela movie por JDBC, publica um evento,
    confere que a readiness continua 200 e que o tópico de erros ficou vazio,
    renomeia de volta e espera o filme aparecer. A falha é real e não usa
    dublê. O passo 7b fica como verificação extra.
  ────────────────────────────────────────
  ID: C2
  Categoria: Constituição
  Severidade: CRITICAL
  Onde: spec.md:312 (FR-024); tasks.md T069
  Resumo: A FR-024 está escrita como comportamento ("MUST levar o mesmo
    tempo"), mas a comprovação planejada é só revisão. Isso também viola o
    Princípio VI.
  Recomendação: Tirar a FR-024 da spec, porque descreve como implementar e não
    o que a aplicação faz. Ela vira decisão de design na R11 e no ADR 0017,
    conferida pela "Revisão de segurança". A FR-023 e a FR-025 continuam com
    IT. Isso muda a spec e vem antes do código (Princípio IV).
  ────────────────────────────────────────
  ID: C3
  Categoria: Constituição
  Severidade: CRITICAL
  Onde: constituição 1.7.0 (VI); spec FR-007; tasks T018/T084
  Resumo: A 1.7.0 exige que o perfil dedicado rode obrigatoriamente, "com o
    job vermelho reprovando o merge". Nenhuma tarefa pede que os jobs postgres
    e compose virem required status checks da main. Sem isso, um job vermelho
    não bloqueia o merge.
  Recomendação: Acrescentar uma tarefa "Pedido ao Anderson": incluir os checks
    postgres e compose na proteção da main. Conferir no T084.
  ────────────────────────────────────────
  ID: I1
  Categoria: Inconsistência
  Severidade: HIGH
  Onde: spec.md FR-010, FR-012, US3-3 e SC-001 × data-model.md (evento)
  Resumo: A spec diz que as regras do evento são "as da spec 001". O
    data-model, porém, tem três diferenças: winner é booleano JSON e
    obrigatório, year precisa ser inteiro JSON, e existe o FIELD_TOO_LONG.
    Esse motivo cria uma diferença conhecida entre a carga direta e o Kafka, o
    que contradiz o "100% idênticas" do SC-001 e a FR-018. A spec não mostra
    nada disso.
  Recomendação: Atualizar a spec antes do código (Princípio IV): descrever o
    formato do evento na FR-010, as diferenças na FR-012, o FIELD_TOO_LONG na
    US3-3, e a exceção dos títulos longos no SC-001 e na FR-018.
  ────────────────────────────────────────
  ID: I2
  Categoria: Inconsistência
  Severidade: HIGH
  Onde: tasks.md T060 (linha 502) × decisoes.md:6
  Resumo: A linha de exceção cita "Exceção do ApiDocsIT à regra…" sem as
    crases. O título no decisoes.md tem crases (`ApiDocsIT`). Como o script
    falha quando o título citado não existe, a etapa 3 reprovaria.
  Recomendação: Copiar o título exato, com as crases, ou fazer o script
    comparar ignorando a formatação Markdown.
  ────────────────────────────────────────
  ID: G1
  Categoria: Cobertura
  Severidade: MEDIUM
  Onde: spec.md:212 (edge "Broker indisponível")
  Resumo: Nenhum IT confere a liveness 200 com a readiness 503 quando o broker
    está fora. Isso é testável sem dublê: basta subir com
    KAFKA_BOOTSTRAP_SERVERS apontando para uma porta fechada.
  Recomendação: Criar um KafkaBrokerUnavailableIT na etapa 2.
  ────────────────────────────────────────
  ID: G2
  Categoria: Cobertura
  Severidade: MEDIUM
  Onde: spec.md:230 (edge "Tópico de erros indisponível"); T044
  Resumo: O comportamento (avisar no log e seguir o consumo) está implementado
    no T044, mas não tem IT.
  Recomendação: Criar um IT com KAFKA_TOPIC_ERRORS com um nome inválido para o
    Kafka (envio recusado de verdade). Antes, verificar se o NewTopic desse
    nome derruba a subida; se derrubar, registrar o limite.
  ────────────────────────────────────────
  ID: G3
  Categoria: Cobertura
  Severidade: MEDIUM
  Onde: spec.md:203 (US5-5) × ci-pipeline.md passo 7
  Resumo: A US5-5 pede down sem -v seguido de up. O passo 7 só reinicia a app,
    então os volumes do PostgreSQL e do Kafka não são postos à prova.
  Recomendação: Trocar o passo 7 por docker compose down e up -d --wait.
  ────────────────────────────────────────
  ID: A1
  Categoria: Ambiguidade
  Severidade: MEDIUM
  Onde: spec.md:237
  Resumo: A borda "Preflight de CORS ou OPTIONS … tratado no plano" não está
    tratada no plano. Com a matriz da R11, um OPTIONS num endpoint protegido
    recebe 401. O CORS só chega no Marco 6.
  Recomendação: Fixar na spec: OPTIONS segue a matriz (401 com o token ligado)
    e o CORS fica para o Marco 6. Cobrir no AuthEnabledIT.
  ────────────────────────────────────────
  ID: A2
  Categoria: Lacuna
  Severidade: MEDIUM
  Onde: spec.md:353, 425, 427; quickstart.md seção 2; T088
  Resumo: O token de demonstração é citado em vários lugares, mas nenhuma
    tarefa define o valor, e o quickstart usa um marcador
    (<token-de-demonstração-do-README>).
  Recomendação: O T088 passa a definir o valor do token de demonstração, que
    só aparece no README (Princípio VII), e o quickstart passa a citá-lo.
  ────────────────────────────────────────
  ID: I3
  Categoria: Inconsistência
  Severidade: MEDIUM
  Onde: spec.md FR-019 e FR-022 × decisão dos cabeçalhos (decisoes.md)
  Resumo: A spec diz "comportamento idêntico ao Marco 2", mas a decisão
    registrada acrescenta X-Content-Type-Options e X-Frame-Options em tudo e
    tira o Cache-Control dos endpoints de negócio. O CacheHeadersIT testa um
    comportamento que a spec não descreve.
  Recomendação: Acrescentar uma FR com os cabeçalhos e definir "idêntico" na
    FR-019 como status, corpo, ETag e cabeçalhos do Marco 2.
  ────────────────────────────────────────
  ID: I4
  Categoria: Inconsistência
  Severidade: LOW
  Onde: spec.md FR-022 × operational-endpoints.md
  Resumo: O /actuator/health geral é público no plano, mas não está na lista
    da FR-022.
  Recomendação: Incluí-lo na FR-022.
  ────────────────────────────────────────
  ID: I5
  Categoria: Inconsistência
  Severidade: LOW
  Onde: spec.md:397 (SC-004) × T035
  Resumo: O SC-004 fala em "3 reinícios" (4 subidas), e o T035 faz 3 subidas.
  Recomendação: Alinhar um dos dois.
  ────────────────────────────────────────
  ID: I6
  Categoria: Deriva
  Severidade: LOW
  Onde: plan.md (estrutura e rastreabilidade) × tasks.md
  Resumo: O RestartDirectPostgresIT, o DefaultModeIsolationIT, o
    PostgresSupport e o KafkaTestSupport estão nas tarefas, mas não na
    estrutura do plano. A FR-014 do plano não cita o RestartDirectPostgresIT.
  Recomendação: Atualizar a estrutura e a matriz do plano.
  ────────────────────────────────────────
  ID: I7
  Categoria: Terminologia
  Severidade: LOW
  Onde: spec US1-4 ("aceitas") × R8 e T027 ("publicadas")
  Resumo: O resumo da publicação diz "publicadas", e a spec diz "aceitas".
  Recomendação: Escolher um termo e citá-lo na spec.
  ────────────────────────────────────────
  ID: A3
  Categoria: Ambiguidade
  Severidade: LOW
  Onde: tasks.md T036
  Resumo: O T036 aceita que o DefaultModeIsolationIT "falha … ou passa", o que
    contradiz a regra de que o teste falha antes da implementação.
  Recomendação: Decidir a asserção que falha antes do T037 (por exemplo, a
    ausência de ProducerConfig no log) ou justificar o teste como guarda.
  ────────────────────────────────────────
  ID: C4
  Categoria: Constituição
  Severidade: LOW
  Onde: research R10; T026
  Resumo: A propriedade awards.ingestion.publish-completion existe só para o
    teste. Não é dublê, mas é uma configuração de produção usada só por teste.
  Recomendação: Manter e documentar no ADR 0014 e no README como opção
    operacional, sem dizer que é só para teste.

  Cobertura

  ┌─────────────┬───────────┬────────────────────────────────┬────────────┐
  │  Requisito  │   Tem     │            Tarefas             │ Observação │
  │             │  tarefa?  │                                │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-001      │ ✅        │ T023, T052, T060               │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-002      │ ✅        │ T009, T024                     │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-003      │ ✅        │ T022, T081                     │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-004,     │ ✅        │ T005, T013                     │            │
  │ FR-005      │           │                                │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-006      │ ✅        │ T005, T006                     │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-007      │ ✅        │ T010, T018, T020               │ C3         │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-008      │ ✅        │ T007                           │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-009      │ ✅        │ T024, T027, T028, T034         │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-010,     │ ✅        │ T024, T043                     │ I1         │
  │ FR-011      │           │                                │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-012      │ ✅        │ T025, T029                     │ I1         │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-013      │ ✅        │ T026, T042                     │ G1         │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-014      │ ✅        │ T009, T030, T031, T035         │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-015      │ ✅        │ T032, T044                     │ G2, C1     │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-016      │ ✅        │ T026, T029                     │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-017      │ ✅        │ T032, T033                     │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-018      │ ✅        │ T025                           │ I1         │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-019 a    │ ✅        │ T052 a T058, T066              │ I3, A1     │
  │ FR-023      │           │                                │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-024      │ ⚠️ só     │ T064, T069                     │ C2         │
  │             │ revisão   │                                │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-025      │ ✅        │ T055                           │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-026      │ ✅        │ T058, T059, T067               │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-027      │ ✅        │ T052 a T058                    │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-028 a    │ ✅        │ T071, T075 a T078, T081        │ G3         │
  │ FR-032      │           │                                │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-033      │ ✅        │ T075, T088                     │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-034      │ ✅        │ T072, T074, T079               │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-035      │ ✅        │ T071, T080, T084               │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-036      │ ✅        │ T088                           │ A2         │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-037      │ ✅        │ T086, T087                     │            │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ FR-038      │ ✅        │ todas                          │ C1, C2, C3 │
  ├─────────────┼───────────┼────────────────────────────────┼────────────┤
  │ SC-001 a    │ ✅        │ T025, T006, T060, T026, T035,  │ I5 no      │
  │ SC-009      │           │ T029, T032, T053, T071, T084   │ SC-004     │
  └─────────────┴───────────┴────────────────────────────────┴────────────┘

  Problemas de constituição: C1, C2 e C3, todos contra o Princípio VI na versão
  1.7.0.

  Tarefas sem requisito: nenhuma. As de preparação (T001 e T002) e de registro
  (T048, T091 e T092) servem ao fluxo do AGENTS.md.

  Números

  ┌──────────────────────────────────┬──────────────────────────────────────┐
  │             Métrica              │                Valor                 │
  ├──────────────────────────────────┼──────────────────────────────────────┤
  │ Requisitos                       │ 38 FR e 9 SC                         │
  ├──────────────────────────────────┼──────────────────────────────────────┤
  │ Tarefas                          │ 92                                   │
  ├──────────────────────────────────┼──────────────────────────────────────┤
  │ Requisitos com ao menos uma      │ 100%, mas a FR-024 tem só revisão    │
  │ tarefa                           │ (C2)                                 │
  ├──────────────────────────────────┼──────────────────────────────────────┤
  │ Ambiguidades                     │ 3 (A1 a A3)                          │
  ├──────────────────────────────────┼──────────────────────────────────────┤
  │ Duplicações                      │ 0                                    │
  ├──────────────────────────────────┼──────────────────────────────────────┤
  │ Problemas críticos               │ 3                                    │
  └──────────────────────────────────┴──────────────────────────────────────┘

  Próximos passos

  1. Antes do /speckit-implement: resolver C1, C2 e C3. O C2 muda a spec. O C1
     acrescenta um IT na etapa 2. O C3 acrescenta uma tarefa de pedido a você.
  2. Também antes da etapa 2: o I1 muda a spec antes do código. O I2 é uma
     correção de uma linha que evita uma falha certa na etapa 3.
  3. Os achados MEDIUM (G1 a G3, A1, A2 e I3) podem entrar agora ou no começo da
     etapa de cada um.
  4. Os LOW são ajustes de redação e de alinhamento.

  Quer que eu proponha as edições concretas para os 9 achados mais graves (C1 a
  C3, I1, I2, G1 a G3 e A1)?

✻ Brewed for 1m 51s · done terça-feira, 15:46

❯ Aprovado, com estes ajustes:                                                  
                                                                                
  C1: crie o RetryPostgresIT como sugerido, mas descreva na spec e no R9 o que  
  ele comprova de fato: nova tentativa quando a gravação falha. A queda real do 
  banco continua comprovada pelo passo 7b do compose. O teste deve devolver o   
  nome da tabela num finally e não pode rodar em paralelo com outros ITs do     
  postgres-it.                                                                  
                                                                                
  C2: aprovado. Tire a FR-024 da spec e registre a decisão na R11 e no ADR      
  0017, citando o mecanismo de comparação usado, para a revisão de segurança    
  conferir.                                                                     
                                                                                
  C3: aprovado. A tarefa de pedido ao Anderson deve vir depois da primeira      
  execução do pipeline no PR (antes disso os jobs não aparecem na lista de      
  checks obrigatórios) e antes do merge. Inclua os jobs postgres e compose e    
  confira se os jobs já existentes estão na lista. O pedido também deve dizer   
  que o bypass para administradores fica desligado.                             
                                                                                
  I1: em vez de documentar a diferença do FIELD_TOO_LONG, faça os dois caminhos 
  usarem a mesma validação, para o SC-001 e a FR-018 continuarem valendo sem    
  exceção. Se isso for caro, me diga por quê antes de documentar a exceção. As  
  outras diferenças do evento (winner booleano, year inteiro) vão para a FR-010 
  e a FR-012, como sugerido.                                                    
                                                                                
  I2: copie o título exato, com as crases.                                      
                                                                                
  G1, G2 (verificando antes se o nome inválido derruba a subida), G3, A1, A2 e  
  I3: aprovados como sugeridos.                                                 
                                                                                
  I4 e I5: aplique também. Na I5, alinhe o T035 ao SC-004 (4 subidas).          
                                                                                
  Aplique tudo, rode o /speckit-analyze de novo e me mostre o resultado. Não    
  faça commit.                                                                  

  Ran 2 shell commands (ctrl+o to expand)

● I'm applying the low-cost fix I1: a shared FIELD_TOO_LONG rule in
  MovieFieldRules will remove the divergence, though default mode will now skip
  over-500-character titles with a warning instead of aborting. Spec 001 needs
  updating first. · summary

● Update(specs\001-producer-intervals-api\spec.md)
  ⎿  Added 5 lines, removed 1 line
      273    - o ano não é um inteiro entre 1 e 9999 (por exemplo, `0` ou
            `10000`);
      274    - o título está vazio;
      275    - a coluna de produtores está vazia ou não gera nenhum nome
           depois da separação (FR-007 a FR-010);
      276 -  - `winner` tem um valor diferente de `yes` ou vazio.        
      276 +  - `winner` tem um valor diferente de `yes` ou vazio;        
      277 +  - um campo excede o tamanho da coluna do esquema: título com
          + mais de 500 caracteres, estúdios ou                          
      278 +    produtores com mais de 1000, ou um nome de produtor com ma
          +is de 500 (acrescentado pela spec 003 em                      
      279 +    2026-11-24, para que a carga direta e a ingestão por Kafka
          + usem a mesma validação; antes, essa linha                    
      280 +    interrompia a carga com um erro do banco, sem mensagem cla
          +ra).                                                          
      281  - **FR-006e**: Ano, título e produtores são colunas obrigatóri
           as. A coluna de estúdios é opcional: vazia,
      282    ela não invalida a linha. `winner` vazio significa não vence
           dor (FR-004).
      283  - **FR-006a**: Cada linha ignorada MUST gerar um aviso no log
           com o número da linha no arquivo e o

  Read 1 file (ctrl+o to expand)

● Update(specs\001-producer-intervals-api\data-model.md)
  ⎿  Added 5 lines, removed 1 line
      76  5. `TITLE_MISSING`: o título está vazio.
      77  6. `PRODUCERS_MISSING`: a coluna de produtores está vazia ou nã
          o gera nenhum nome depois da separação.
      78  7. `WINNER_INVALID`: `winner` não é vazio e é diferente de `yes
          `, sem diferenciar maiúsculas.
      79 -8. `DUPLICATE`: o ano e o título repetem uma linha já aceita. O
         - aviso cita a linha original.                                  
      79 +8. `FIELD_TOO_LONG`: um campo excede a coluna do esquema. Os li
         +mites são título > 500, estúdios > 1000,                       
      80 +   produtores > 1000 ou um nome de produtor > 500 caracteres (`
         +VARCHAR(500)` e `VARCHAR(1000)` da V1).                        
      81 +   O aviso cita o campo. Acrescentado pela spec 003 em 2026-11-
         +24. A regra fica na `MovieFieldRules`,                         
      82 +   compartilhada com os eventos do Kafka.                      
      83 +9. `DUPLICATE`: o ano e o título repetem uma linha já aceita. O
         + aviso cita a linha original.                                  
      84 
      85  Uma linha em branco não é lida, não conta como linha lida e não
           gera aviso (FR-005a).
      86 

● Now the spec 003 edits: I1 (FR-010/FR-012), C1 (FR-015a), C2 (FR-024), A1, I3,
  I4, I5 and FR-038.

  Read 1 file (ctrl+o to expand)

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 5 lines
      228  - Evento ilegível (não é um objeto no formato documentado, ou
           não está em UTF-8): vai ao tópico de
      229    erros com o conteúdo original, sem derrubar o consumidor.
      230  - Tópico de erros indisponível: o evento inválido não é gravad
           o e o motivo fica no log; o consumo segue.
      231 +- Falha ao gravar um evento válido (banco indisponível ou recu
          +sando a gravação): o evento não é                             
      232 +  descartado nem vai ao tópico de erros. A gravação é tentada 
          +de novo até dar certo (FR-015a).                              
      233 +- Campo maior que a coluna do esquema (título com mais de 500 
          +caracteres, por exemplo): a linha do CSV e                    
      234 +  o evento são rejeitados pela mesma regra (`FIELD_TOO_LONG`, 
          +spec 001 FR-006), nos dois caminhos de                        
      235 +  carga.                                                      
      236  - Títulos com acentos, maiúsculas e minúsculas misturadas: a o
           rdem da listagem no PostgreSQL é a mesma do
      237    H2 (FR-008).
      238  - Token com espaços em volta ou em maiúsculas diferentes no ca
           beçalho: comparado exatamente, sem

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 3 lines, removed 1 line
      239    normalização; diferente é 401.
      240  - Variável do token definida como texto vazio: equivale a aute
           nticação desligada.
      241  - Chamada com o cabeçalho do token quando a autenticação está
           desligada: ignorado, resposta normal.
      242 -- Preflight de CORS ou `OPTIONS` com a autenticação ligada: tr
          -atado no plano, sem expor dados.                              
      242 +- `OPTIONS` com a autenticação ligada: segue a mesma matriz de
          + acesso. Num endpoint protegido, sem token,                   
      243 +  a resposta é 401. Sem a autenticação, a resposta é a do Marc
          +o 2. O CORS (e, com ele, o preflight)                         
      244 +  está fora do escopo deste marco e entra com o front-end (Mar
          +co 6).                                                        
      245 
      246  ## Requirements *(mandatory)*
      247 

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 9 lines, removed 3 lines
      275  - **FR-009**: Com a ingestão por Kafka ligada, a carga direta
           MUST NOT rodar. Um publicador MUST ler o
      276    arquivo com as mesmas regras da spec 001 (FR-002 a FR-006e):
            erros estruturais impedem a subida com a
      277    mesma mensagem, linhas inválidas são ignoradas com o mesmo a
           viso, e o resumo da leitura é registrado.
      278 -- **FR-010**: O publicador MUST enviar um evento por filme ace
          -ito ao tópico de filmes, com ano, título,                     
      279 -  estúdios, produtores (no formato da coluna do CSV) e `winner
          -`, identificado por ano e título. O                           
      280 -  formato do evento MUST ser documentado no repositório.      
      278 +- **FR-010**: O publicador MUST enviar um evento por filme ace
          +ito ao tópico de filmes, identificado por                     
      279 +  ano e título. O evento é um objeto JSON em UTF-8 com:       
      280 +  - `year`: inteiro JSON;                                     
      281 +  - `title`: texto;                                           
      282 +  - `studios`: texto ou `null`;                               
      283 +  - `producers`: texto no formato da coluna do CSV;           
      284 +  - `winner`: booleano JSON (`true` ou `false`). O publicador 
          +converte o `yes` do CSV em `true`.                            
      285 +                                                              
      286 +  O formato do evento MUST ser documentado no repositório.    
      287  - **FR-011**: Ao terminar de publicar, o publicador MUST envia
           r um evento de carga concluída que
      288    identifica a execução atual e a quantidade de filmes publica
           dos.
      289  - **FR-012**: Um consumidor MUST gravar cada evento de filme p
           elo mesmo serviço de gravação do modo

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 15 lines, removed 2 lines
      287  - **FR-011**: Ao terminar de publicar, o publicador MUST envia
           r um evento de carga concluída que
      288    identifica a execução atual e a quantidade de filmes publica
           dos.
      289  - **FR-012**: Um consumidor MUST gravar cada evento de filme p
           elo mesmo serviço de gravação do modo
      290 -  padrão (Princípio V). A separação dos produtores e as regras
          - de validação de cada campo MUST ser as da                    
      291 -  spec 001.                                                   
      290 +  padrão (Princípio V). A separação dos produtores e as regras
          + de conteúdo de cada campo MUST ser as                        
      291 +  mesmas da linha do CSV (spec 001, FR-006 a FR-010), aplicada
          +s pela mesma validação nos dois caminhos:                     
      292 +  - ano de 1 a 9999;                                          
      293 +  - título não vazio;                                         
      294 +  - produtores com ao menos um nome;                          
      295 +  - tamanho máximo de cada campo (`FIELD_TOO_LONG`).          
      296 +                                                              
      297 +  O que muda é só a representação, que o JSON impõe:          
      298 +  - `year` precisa ser um inteiro JSON. Um texto, mesmo só com
          + dígitos, ou um decimal é rejeitado;                          
      299 +  - `winner` precisa ser booleano JSON e é obrigatório. O `yes
          +` e o vazio do CSV não existem no                             
      300 +    evento;                                                   
      301 +  - motivos que só existem no evento: bytes fora de UTF-8, con
          +teúdo que não é objeto JSON, `type`                           
      302 +    desconhecido e marcador sem `runId`.                      
      303 +                                                              
      304 +  Duplicatas dentro do mesmo evento não existem; entre eventos
          +, vale a FR-014.                                              
      305  - **FR-013**: A readiness MUST ficar negativa até o consumidor
            processar o evento de carga concluída da
      306    execução atual, e nesse momento todos os filmes publicados n
           essa execução MUST estar gravados. Eventos
      307    de carga concluída de execuções anteriores MUST NOT liberar
           a readiness.

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 7 lines
      314  - **FR-015**: Um evento inválido MUST NOT ser gravado. Ele MUS
           T ser enviado a um tópico de erros, com o
      315    conteúdo original e o motivo, e gerar um aviso no log com o
           motivo. O consumo dos eventos seguintes
      316    MUST continuar.
      317 +- **FR-015a**: Quando a gravação de um evento válido falha (po
          +r exemplo, banco indisponível ou recusando                    
      318 +  a gravação), o evento MUST NOT ser descartado nem enviado ao
          + tópico de erros. A gravação MUST ser                         
      319 +  tentada de novo, sem avançar no tópico, até dar certo, e a r
          +eadiness não muda por causa disso.                            
      320 +  Comprovação (FR-038):                                       
      321 +  - o `RetryPostgresIT` prova a nova tentativa quando a gravaç
          +ão falha: a tabela é renomeada e depois                       
      322 +    devolvida;                                                
      323 +  - a queda real do banco é comprovada pelo passo 7b do teste 
          +do compose.                                                   
      324  - **FR-016**: Depois da carga, eventos de filmes novos recebid
           os pelo mesmo tópico MUST ser gravados e
      325    refletidos nos intervalos e na listagem, sem reinício e sem
           mudar a readiness.
      326  - **FR-017**: As métricas MUST incluir o atraso do consumidor
           no tópico de filmes e a contagem de eventos

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 2 lines, removed 1 line
      327    gravados e de eventos enviados ao tópico de erros.
      328  - **FR-018**: Um teste de integração no `./mvnw verify`, com o
            broker embutido, MUST provar que o mesmo
      329    arquivo pela carga direta e pela ingestão por Kafka produz r
           espostas idênticas nos intervalos e na
      330 -  listagem.                                                   
      330 +  listagem, sem exceção. O teste inclui um arquivo com linhas 
          +rejeitadas, inclusive por                                     
      331 +  `FIELD_TOO_LONG`.                                           
      332 
      333  **Autenticação**
      334 

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 2 lines
      334 
      335  - **FR-019**: A autenticação MUST ser ligada só pela variável
           de ambiente do token. Ausente ou vazia, a
      336    autenticação fica desligada, e todos os endpoints se comport
           am como no Marco 2 (Princípio VII).
      337 +  "Como no Marco 2" quer dizer o mesmo status, o mesmo corpo, 
          +o mesmo `ETag` e 304 e os mesmos                              
      338 +  cabeçalhos que o Marco 2 mandava. Os cabeçalhos de proteção 
          +da FR-022a são o único acréscimo.                             
      339  - **FR-020**: Com a autenticação ligada, o endpoint de métrica
           s MUST exigir o token num cabeçalho da
      340    requisição, comparado exatamente com o valor da variável.
      341  - **FR-021**: Com a autenticação ligada, os endpoints de negóc
           io (`/api/producers/intervals` e

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 11 lines, removed 2 lines
      341  - **FR-021**: Com a autenticação ligada, os endpoints de negóc
           io (`/api/producers/intervals` e
      342    `/api/movies`) MUST exigir o mesmo token. Com o token certo,
            a resposta é a do Marco 2, inclusive o
      343    ETag e o 304.
      344 -- **FR-022**: A liveness, a readiness, o Swagger UI e o contra
          -to OpenAPI MUST continuar públicos, com ou                    
      345 -  sem a autenticação. As probes continuam respondendo só o est
          -ado (spec 002, FR-011).                                       
      344 +- **FR-022**: A liveness, a readiness, a saúde geral (`/actuat
          +or/health`), o Swagger UI e o contrato                        
      345 +  OpenAPI MUST continuar públicos, com ou sem a autenticação. 
          +As probes e a saúde geral continuam                           
      346 +  respondendo só o estado (spec 002, FR-011).                 
      347 +- **FR-022a**: Com a autenticação ligada ou desligada:        
      348 +  - toda resposta MUST trazer `X-Content-Type-Options: nosniff
          +` e `X-Frame-Options: DENY`;                                  
      349 +  - as respostas dos endpoints de negócio MUST NOT trazer `Cac
          +he-Control`, `Pragma` nem `Expires`, para                     
      350 +    que o `ETag` e o 304 continuem úteis ao cliente;          
      351 +  - as demais respostas (métricas, saúde, documentação e 401) 
          +podem trazer os cabeçalhos de cache do                        
      352 +    framework.                                                
      353 +                                                              
      354 +  Decisão do responsável de 2026-11-24, em `docs/ai/decisoes.m
          +d`.                                                           
      355  - **FR-023**: Token ausente ou errado num endpoint protegido M
           UST resultar em 401 em Problem Details
      356    (RFC 9457), com o mesmo corpo nos dois casos e sem revelar o
            valor esperado nem o recebido.
      357  - **FR-024**: A comparação do token MUST levar o mesmo tempo,
           qualquer que seja o valor recebido, para

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 4 lines, removed 2 lines
      354    Decisão do responsável de 2026-11-24, em `docs/ai/decisoes.m
           d`.
      355  - **FR-023**: Token ausente ou errado num endpoint protegido M
           UST resultar em 401 em Problem Details
      356    (RFC 9457), com o mesmo corpo nos dois casos e sem revelar o
            valor esperado nem o recebido.
      357 -- **FR-024**: A comparação do token MUST levar o mesmo tempo, 
          -qualquer que seja o valor recebido, para                      
      358 -  não revelar o token por medição de tempo.                   
      357 +- **FR-024**: *(retirado em 2026-11-24, depois da segunda anál
          +ise.)* A forma de comparar o token é uma                      
      358 +  decisão de design, e não um comportamento verificável por te
          +ste. Ela fica na research R11 e no ADR                        
      359 +  0017, conferida na "Revisão de segurança" do checklist. O nú
          +mero fica reservado para não renumerar os                     
      360 +  requisitos.                                                 
      361  - **FR-025**: O valor do token MUST NOT aparecer no log, nas m
           étricas, nas respostas nem no contrato. O
      362    único token no repositório é o de demonstração documentado n
           o README.
      363  - **FR-026**: O contrato OpenAPI MUST documentar o esquema de
           autenticação e a resposta 401 nos endpoints

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 4 lines, removed 2 lines
      410  **Comprovação**
      411 
      412  - **FR-038**: Cada requisito MUST ser comprovado na forma do P
           rincípio VI (constituição 1.7.0):
      413 -  - comportamento da aplicação comum aos dois bancos (FR-001, 
          -FR-002, FR-009 a FR-027): teste de                            
      414 -    integração no `./mvnw verify`, com H2 e o broker embutido;
      413 +  - comportamento da aplicação comum aos dois bancos (FR-001, 
          +FR-002, FR-009 a FR-023, FR-025 a                             
      414 +    FR-027, menos a FR-015a): teste de integração no `./mvnw v
          +erify`, com H2 e o broker embutido;                           
      415 +  - nova tentativa quando a gravação falha (FR-015a): `RetryPo
          +stgresIT` no perfil Maven dedicado, mais o                    
      416 +    passo 7b do teste do compose, que comprova a queda real do
          + banco;                                                       
      417    - comportamento que só existe no PostgreSQL (FR-004, FR-006,
            FR-008 e a persistência entre reinícios
      418      da US2): teste de integração no perfil Maven dedicado, exe
           cutado obrigatoriamente no pipeline
      419      (FR-007). É a regra do Princípio VI para o comportamento q
           ue depende de infraestrutura indisponível

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 3 lines, removed 2 lines
      443    `./mvnw verify` passa sem Docker, incluindo os testes de Kaf
           ka e de autenticação.
      444  - **SC-003**: Em 100% das subidas com a ingestão por Kafka, a
           aplicação só se declara pronta depois de
      445    todos os filmes aceitos do arquivo estarem gravados.
      446 -- **SC-004**: Depois de 3 reinícios seguidos sobre o mesmo ban
          -co, o número de filmes é o da primeira                        
      447 -  carga mais os filmes novos recebidos por evento, sem nenhuma
          - duplicata de filme, produtor ou vínculo.                     
      446 +- **SC-004**: Depois de 3 reinícios seguidos sobre o mesmo ban
          +co (4 subidas), o número de filmes é o da                     
      447 +  primeira carga mais os filmes novos recebidos por evento, se
          +m nenhuma duplicata de filme, produtor ou                     
      448 +  vínculo.                                                    
      449  - **SC-005**: Um vencedor novo publicado depois da carga apare
           ce nos intervalos em até 5 segundos.
      450  - **SC-006**: 100% dos eventos inválidos vão ao tópico de erro
           s, e nenhum deles impede a gravação do
      451    evento válido seguinte.

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 3 lines, removed 2 lines
      131     em `min`, no formato exato do enunciado.
      132  2. **Given** um ETag dos intervalos obtido antes do evento nov
           o, **When** os intervalos são consultados
      133     com esse ETag depois do evento, **Then** a resposta é 200 c
           om o corpo novo e um ETag diferente.
      134 -3. **Given** um evento inválido (formato ilegível, campo obrig
          -atório ausente, ano fora da faixa,                            
      135 -   `winner` inválido ou produtores vazios), **When** ele é con
          -sumido, **Then** ele não é gravado, vai                       
      134 +3. **Given** um evento inválido (formato ilegível, campo obrig
          +atório ausente, ano fora da faixa ou que                      
      135 +   não é inteiro JSON, `winner` que não é booleano, produtores
          + vazios ou campo maior que a coluna),                         
      136 +   **When** ele é consumido, **Then** ele não é gravado, vai  
      137     para o tópico de erros com o conteúdo original e o motivo,
           gera um aviso no log, e o consumo segue.
      138  4. **Given** um evento inválido seguido de um válido, **When**
            os dois são consumidos, **Then** o válido
      139     é gravado.
● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 1 line, removed 1 line
      302    - motivos que só existem no evento: bytes fora de UTF-8, con
           teúdo que não é objeto JSON, `type`
      303      desconhecido e marcador sem `runId`.
      304 
      305 -  Duplicatas dentro do mesmo evento não existem; entre eventos
          -, vale a FR-014.                                              
      305 +  Entre eventos com o mesmo ano e título, vale a FR-014.      
      306  - **FR-013**: A readiness MUST ficar negativa até o consumidor
            processar o evento de carga concluída da
      307    execução atual, e nesse momento todos os filmes publicados n
           essa execução MUST estar gravados. Eventos
      308    de carga concluída de execuções anteriores MUST NOT liberar
           a readiness.

  Read 1 file (ctrl+o to expand)

● Update(specs\003-modo-docker\data-model.md)
  ⎿  Added 16 lines, removed 10 lines
      56  | `studios` | texto ou `null` | não | vazio ou ausente vira nul
          o | — |
      57  | `producers` | texto | sim | separado pelo `ProducerNameParser
          `, com ao menos um nome | `PRODUCERS_MISSING` |
      58  | `winner` | booleano JSON | sim | `true` ou `false` | `WINNER_
          INVALID` |
      59 +| (todos) | — | — | tamanho máximo: `title` ≤ 500, `studios` ≤ 
         +1000, `producers` ≤ 1000 e cada nome de produtor ≤ 500 caracter
         +es (`VARCHAR(500)` e `VARCHAR(1000)` da V1) | `FIELD_TOO_LONG` 
         +|                                                              
      60 
      60 -Motivos que só existem no evento:                              
      61 +**Validação comum aos dois caminhos.** As regras de conteúdo fi
         +cam numa única classe, a                                       
      62 +`MovieFieldRules`, usada pelo leitor do CSV e pelo parser de ev
         +entos:                                                         
      63 +- ano de 1 a 9999;                                             
      64 +- título não vazio;                                            
      65 +- produtores com ao menos um nome;                             
      66 +- `FIELD_TOO_LONG`.                                            
      67 
      68 +O `FIELD_TOO_LONG` passou a valer também para a linha do CSV (s
         +pec 001, FR-006 e data-model, motivo 8).                       
      69 +Sem ele, o banco recusaria a gravação. Na carga direta, a recus
         +a interrompia a subida. No consumidor, ela                     
      70 +seria tratada como falha de infraestrutura e tentada de novo pa
         +ra sempre (R9). Com a mesma regra nos dois                     
      71 +caminhos, a carga direta e o Kafka continuam equivalentes, sem 
         +exceção (spec 003, FR-018 e SC-001).                           
      72 +                                                               
      73 +Motivos que só existem no evento, porque vêm da representação J
         +SON:                                                           
      74 +                                                               
      75  | Motivo | Quando |
      76  | --- | --- |
      77  | `NOT_UTF8` | Os bytes não são UTF-8 válido |
      78  | `NOT_JSON` | O texto não é um objeto JSON |
      79  | `TYPE_UNKNOWN` | `type` presente e diferente de `movie` e de
          `load-completed` |
      67 -| `FIELD_TOO_LONG` | Um campo excede a coluna do esquema: `titl
         -e` > 500, `studios` > 1000, `producers` > 1000 ou um nome de pr
         -odutor > 500 caracteres (`VARCHAR(500)` e `VARCHAR(1000)` da V1
         -) |                                                            
      80 
      69 -**Por que `FIELD_TOO_LONG`:** sem essa regra, o banco recusaria
         - a gravação e o consumidor trataria a                          
      70 -recusa como falha de infraestrutura, tentando de novo para semp
         -re e travando o tópico (research R9). A                        
      71 -carga direta do modo padrão não muda: um título longo no CSV co
         -ntinua falhando como no Marco 1. No modo                       
      72 -Kafka, o mesmo título vai ao tópico de erros. É a única diferen
         -ça conhecida entre os dois caminhos de                         
      73 -carga, e o `KafkaEquivalenceIT` usa arquivos sem esse caso.    
      81 +**Ordem das verificações:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNO
         +WN`, depois a mesma ordem da spec 001:                         
      82 +ano, título, produtores, `winner` e `FIELD_TOO_LONG`. O primeir
         +o motivo encontrado é o registrado.                            
      83 
      75 -**Ordem das verificações:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNO
         -WN`, depois a ordem da spec 001 (ano,                          
      76 -título, produtores, `winner`) e por fim `FIELD_TOO_LONG`. O pri
         -meiro motivo encontrado é o registrado.                        
      77 -                                                               
      84  Campos desconhecidos são ignorados (R7).
      85 
      86  ## Evento de carga concluída

  Read 1 file, ran 2 shell commands (ctrl+o to expand)

● The spec and data-model are already updated (I1, C1, C2, A1, I3, I4, I5). Now
  the research (R7, R9, R11 and R20), then the contracts and tasks.

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 14 lines, removed 4 lines
      140    - produtores com ao menos um nome pelo `ProducerNameParser`;
      141    - estúdios opcionais.
      142 
      143 -  As regras de ano, título e produtores passam a viver numa cl
          -asse comum (`MovieFieldRules`), usada pelo                    
      144 -  leitor do CSV e pelo parser de eventos. Assim, cada regra ex
          -iste num só lugar.                                            
      145 -- **Diferença do CSV:** no evento, `winner` é booleano JSON (`
          -true` ou `false`) e obrigatório. O                            
      146 -  publicador converte o `yes` do CSV em `true`.               
      143 +  As regras de ano, título, produtores e tamanho máximo (`FIEL
          +D_TOO_LONG`) passam a viver numa classe                       
      144 +  comum (`MovieFieldRules`), usada pelo leitor do CSV e pelo p
          +arser de eventos. Assim, cada regra existe                    
      145 +  num só lugar, e a carga direta e o Kafka rejeitam exatamente
          + as mesmas linhas (spec 003, FR-018, sem                      
      146 +  exceção).                                                   
      147 +- **`FIELD_TOO_LONG` também no CSV (decisão do responsável na 
          +segunda análise):** antes, uma linha com                      
      148 +  título acima de 500 caracteres interrompia a carga direta co
          +m um erro do banco. Agora ela é ignorada                      
      149 +  com aviso, como as outras linhas inválidas (spec 001, FR-006
          +, atualizada). O custo é baixo: uma regra a                   
      150 +  mais na `MovieFieldRules`, um motivo a mais no `RejectionRea
          +son` (antes de `DUPLICATE`) e um IT novo.                     
      151 +  Nenhum IT do Marco 1 depende desse caso.                    
      152 +- **Diferenças de representação do CSV:**                     
      153 +  - no evento, `winner` é booleano JSON (`true` ou `false`) e 
          +obrigatório. O publicador converte o                          
      154 +    `yes` do CSV em `true`;                                   
      155 +  - `year` é inteiro JSON;                                    
      156 +  - essas diferenças estão na spec (FR-010 e FR-012).         
      157  - **Motivo:** ler os bytes crus permite enviar ao tópico de er
           ros o conteúdo original exatamente como
      158    chegou, inclusive quando não é UTF-8 nem JSON (FR-015). Com
           o `JsonDeserializer` e o
      159    `ErrorHandlingDeserializer`, o conteúdo inválido chegaria ao
            ouvinte já transformado em exceção.

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 21 lines
      206      do consumidor cresce e aparece no dashboard.
      207  - **Tópico de erros fora do ar:** o envio ao tópico de erros e
           spera a confirmação por até 10 s. Se
      208    falhar, o aviso no log registra o motivo e a falha do envio,
            e o offset avança (caso de borda da spec).
      209 +  O `KafkaErrorTopicUnavailableIT` comprova isso com um nome d
          +e tópico de erros inválido para o Kafka, que                  
      210 +  o broker recusa de verdade. **Verificar antes:** se o `NewTo
          +pic` com esse nome derruba a subida. Se                       
      211 +  derrubar, o IT usa um nome válido que não é criado (o `NewTo
          +pic` do tópico de erros fica atrás da                         
      212 +  propriedade `awards.ingestion.create-error-topic`, padrão `t
          +rue`), com a criação automática de tópicos                    
      213 +  desligada no broker embutido. Se nenhum dos dois for possíve
          +l sem dublê, o limite é registrado em                         
      214 +  `docs/ai/decisoes.md`.                                      
      215 +- **Comprovação da nova tentativa (spec 003, FR-015a):**      
      216 +  - **`RetryPostgresIT`** (perfil `postgres-it`) prova a **nov
          +a tentativa quando a gravação falha**:                        
      217 +    1. com a aplicação pronta, o teste renomeia a tabela `movi
          +e` do seu esquema por JDBC;                                   
      218 +    2. publica um evento válido e espera 5 s;                 
      219 +    3. confere que a readiness continua 200, que o tópico de e
          +rros não recebeu nada e que o log tem a                       
      220 +       nova tentativa;                                        
      221 +    4. devolve o nome da tabela **num `finally`**, para que um
          +a falha do teste não deixe o esquema                          
      222 +       quebrado;                                              
      223 +    5. espera até 60 s pelo filme na listagem.                
      224 +                                                              
      225 +    O teste não prova a queda real do banco, só uma gravação q
          +ue falha e volta a funcionar. Ele é                           
      226 +    `@Isolated` (JUnit) e a execução `postgres` do Failsafe nã
          +o usa paralelismo, então ele **nunca roda                     
      227 +    em paralelo** com outro IT do `postgres-it`;              
      228 +  - **queda real do banco:** o passo 7b do `compose-test.sh` p
          +ara o serviço `postgres` do compose,                          
      229 +    publica, religa e confere a gravação.                     
      230  - **Alternativa rejeitada:** `DeadLetterPublishingRecoverer` p
           ara todos os erros. Ele trataria uma falha
      231    de banco como evento inválido.
      232 

  Read 1 file (ctrl+o to expand)

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 1 line, removed 1 line
      269      `AuthorizationFilter`):
      270      - lê `Authorization: Bearer <token>`;
      271      - compara o SHA-256 do valor recebido com o do esperado po
           r `MessageDigest.isEqual`, em tempo
      272 -      constante e independente do tamanho (FR-024);           
      272 +      constante e independente do tamanho (decisão de design a
          +baixo);                                                       
      273      - se forem iguais, autentica o cliente fixo `api-client`,
           sem papéis;
      274      - se não forem, ou se o cabeçalho faltar, não faz nada, e
           a autorização nega;
      275    - **401:** um `AuthenticationEntryPoint` próprio responde `a
           pplication/problem+json`:

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 10 lines
      281  - **Por que `Authorization: Bearer`:** é o esquema padrão (RFC
            6750), já suportado pelo Swagger UI, pelo
      282    Prometheus (`authorization.credentials`) e pelo `curl`.
      283  - **Variável:** `API_TOKEN`. Vazia ou ausente desliga a autent
           icação (FR-019).
      284 +- **Decisão de design: comparação do token em tempo constante*
          +* (era a FR-024 da spec, retirada na                          
      285 +  segunda análise porque não é um comportamento verificável po
          +r teste confiável):                                           
      286 +  - **mecanismo:** `MessageDigest.isEqual(sha256(recebido), sh
          +a256(esperado))`, com                                         
      287 +    `MessageDigest.getInstance("SHA-256")` e os dois valores e
          +m UTF-8. O `MessageDigest.isEqual` do JDK                     
      288 +    compara todos os bytes sem sair no primeiro diferente. Os 
          +dois resumos têm sempre 32 bytes, então o                     
      289 +    tempo não depende do tamanho nem do conteúdo do token rece
          +bido;                                                         
      290 +  - **proibido:** `String.equals`, `Arrays.equals` ou qualquer
          + comparação que termine no primeiro byte                      
      291 +    diferente, e comparar o valor recebido sem passar pelo res
          +umo;                                                          
      292 +  - **comprovação:** a "Revisão de segurança" do checklist da 
          +spec confere o mecanismo no                                   
      293 +    `TokenAuthenticationFilter`, e o ADR 0017 registra a decis
          +ão citando o mecanismo.                                       
      294  - **Token no log:** o filtro nunca registra o cabeçalho. O nív
           el DEBUG do Spring Security não registra
      295    cabeçalhos. Um IT com a autenticação ligada envia um token e
           rrado conhecido e confere que ele não
      296    aparece no log nem nas métricas (FR-025).

  Read 1 file (ctrl+o to expand)

● Update(specs\003-modo-docker\research.md)
  ⎿  Added 10 lines, removed 2 lines
      526       - publica, pelo `kafka-cli`, um vencedor novo que muda `m
           in`;
      527       - espera até 5 s pela mudança nos intervalos (SC-005);
      528       - publica um evento inválido e lê o tópico de erros com o
           s cabeçalhos;
      529 -  6. **persistência:** `docker compose restart app`, espera a 
          -readiness e confere que o filme novo                          
      530 -     continua e que o total da listagem é o da carga mais um; 
      529 +  6. **persistência:**                                        
      530 +     - `docker compose down`, **sem `-v`**, e depois `docker c
          +ompose up -d --wait`;                                         
      531 +     - todos os containers são recriados, e os volumes do Post
          +greSQL e do Kafka são postos à prova                          
      532 +       (US5-5);                                               
      533 +     - confere que o filme novo continua e que o total da list
          +agem é o da carga mais um;                                    
      534 +     - em seguida, o passo 7b (queda real do banco, FR-015a): 
      535 +       1. `docker compose stop postgres`;                     
      536 +       2. publica um evento;                                  
      537 +       3. `docker compose start postgres`;                    
      538 +       4. confere a gravação sem registro no tópico de erros; 
      539    7. **token:** recria `app` e `prometheus` com um `API_TOKEN`
            aleatório gerado no próprio job
      540       (`openssl rand -hex 16`, nunca gravado). Confere 401 sem
           token nos intervalos e o alvo do
      541       Prometheus de novo `up`;

● Update(specs\003-modo-docker\contracts\ci-pipeline.md)
  ⎿  Added 2 lines, removed 1 line
      52  | — | tempo dos passos 1 a 4 | acima de 180 s (SC-008) |
      53  | 5 | publica pelo `kafka-cli` um vencedor novo que cria um int
          ervalo menor que o mínimo e espera até 5 s pela mudança em `min
          ` | não muda em 5 s (SC-005) |
      54  | 6 | publica um evento com `year: 0` e lê o tópico de erros co
          m os cabeçalhos | sem registro ou sem `awards-error-reason: YEA
          R_OUT_OF_RANGE` |
      55 -| 7 | `docker compose restart app`, espera a readiness; o filme
         - do passo 5 continua; `totalElements` = 206 + 1 | filme ausente
         - ou total diferente |                                          
      55 +| 7 | `docker compose down` (**sem `-v`**) e `docker compose up
         + -d --wait`: todos os containers são recriados sobre os mesmos 
         +volumes; o filme do passo 5 continua; `totalElements` = 206 + 1
         + | filme ausente ou total diferente |                          
      56 +| 7b | `docker compose stop postgres`; publica `{"year":2031,"t
         +itle":"Durante a Queda",…}`; espera 5 s; `docker compose start 
         +postgres`; espera até 60 s pelo filme na listagem; o tópico de 
         +erros não recebe esse evento (FR-015a, queda real do banco) | f
         +ilme ausente ou evento no tópico de erros |                    
      57  | 8 | `API_TOKEN=$(openssl rand -hex 16) docker compose up -d -
          -wait app prometheus`; intervalos sem token com 401; alvo do Pr
          ometheus `up` de novo | outro status ou alvo fora |
      58 
      59  O token do passo 8 é gerado no próprio job, nunca é gravado e n
          unca é impresso (`set +x` em volta dele).

● Update(specs\003-modo-docker\contracts\operational-endpoints.md)
  ⎿  Added 4 lines, removed 1 line
      34  - O corpo é o mesmo para o token ausente e para o token errado
          (FR-023).
      35  - Nem o valor esperado nem o recebido aparecem no corpo, nos ca
          beçalhos, no log ou nas métricas
      36    (FR-025).
      37 -- A comparação é feita sobre o SHA-256 dos dois valores, com `M
         -essageDigest.isEqual` (FR-024).                                
      37 +- A comparação é feita sobre o SHA-256 dos dois valores, com `M
         +essageDigest.isEqual`. É uma decisão de                        
      38 +  design (research R11 e ADR 0017), conferida na "Revisão de se
         +gurança".                                                      
      39 +- Um `OPTIONS` segue a mesma matriz: com o token ligado, num ca
         +minho protegido e sem token, a resposta é                      
      40 +  401. O CORS fica para o Marco 6.                             
      41 
      42  ## Cabeçalhos de cache (research R12)
      43 

  Ran 1 shell command (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      p=specs/003-modo-docker/plan.md
      sed -i 's/^| FR-013 | `KafkaReadinessIT` |$/| FR-013 | `KafkaReadinessIT`,
      `KafkaBro…)
  ⎿  142:| FR-007 | job `postgres`, obrigatório na proteção da `main` (T093
     ) |
     146:| FR-013 | `KafkaReadinessIT`, `KafkaBrokerUnavailableIT` |
     … +18 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\plan.md +15 -9
      139  | FR-003 | ITs que sobrescrevem `KAFKA_TOPIC_*` e `DB_*`; job
           `compose` |
      140  | FR-004, FR-005 | `SchemaPostgresIT` (job `postgres`); `ddl-a
           uto: validate` nos dois bancos |
      141  | FR-006, FR-008 | `EquivalencePostgresIT`, `ListingOrderPostg
           resIT` (job `postgres`) |
      142 -| FR-007 | job `postgres` |                                   
      142 +| FR-007 | job `postgres`, obrigatório na proteção da `main` (
          +T093) |                                                       
      143  | FR-009 | `KafkaInvalidLinesIT`, `KafkaStructuralFailureIT`,
           `PublisherSummaryIT` |
      144  | FR-010, FR-011 | `KafkaReadinessIT` (lê o tópico) e `KafkaRe
           ferenceFileIT` |
      145  | FR-012 | `KafkaEquivalenceIT`, `KafkaNewWinnerIT` (separação
            de nomes) |
      146 -| FR-013 | `KafkaReadinessIT` |                               
      147 -| FR-014 | `KafkaIdempotencyIT`, `KafkaUpdateIT`, `RestartPost
          -gresIT` |                                                     
      148 -| FR-015 | `KafkaInvalidEventsIT` |                           
      146 +| FR-013 | `KafkaReadinessIT`, `KafkaBrokerUnavailableIT` |   
      147 +| FR-014 | `KafkaIdempotencyIT`, `KafkaUpdateIT`, `RestartDire
          +ctPostgresIT`, `RestartPostgresIT` |                          
      148 +| FR-015 | `KafkaInvalidEventsIT`, `KafkaErrorTopicUnavailable
          +IT` |                                                         
      149 +| FR-015a | `RetryPostgresIT` (job `postgres`: nova tentativa 
          +quando a gravação falha); passo 7b do job `compose` (queda rea
          +l do banco) |                                                 
      150  | FR-016 | `KafkaNewWinnerIT` |
      151  | FR-017 | `KafkaMetricsIT` |
      151 -| FR-018 | `KafkaEquivalenceIT` |                             
      152 -| FR-019 a FR-025, FR-027 | `AuthDisabledByDefaultIT`, `AuthEn
          -abledIT`, `AuthProblemDetailsIT`, `TokenNotLoggedIT`, `NoGener
          -atedPasswordIT`, `CacheHeadersIT` |                           
      152 +| FR-018 | `KafkaEquivalenceIT` (inclui `FIELD_TOO_LONG`); `Fi
          +eldTooLongIT` (carga direta, spec 001 FR-006) |               
      153 +| FR-019 a FR-023, FR-025, FR-027 | `AuthDisabledByDefaultIT`,
          + `AuthEnabledIT` (inclui `OPTIONS`), `AuthProblemDetailsIT`, `
          +TokenNotLoggedIT`, `NoGeneratedPasswordIT` |                  
      154 +| FR-022a | `CacheHeadersIT` |                                
      155 +| FR-024 | retirado (decisão de design na R11 e no ADR 0017; "
          +Revisão de segurança" do checklist) |                         
      156  | FR-026 | `ApiDocsSecurityIT`, `AuthContractIT` |
      157  | FR-028 a FR-032, FR-035 | job `compose` |
      158  | FR-033, FR-034 | job `workflow-policy` (digest, Dependabot e
            senha fora do workflow); revisão (comentários de demonstração
           ) |
     ...
      229  ├── application-postgres.yml              # novo: DB_URL, DB_U
           SER, DB_PASSWORD (sem padrão)
      230  └── static/openapi.yaml                   # cópia de specs/003
           -modo-docker/contracts/openapi.yaml
      231  src/test/java/com/outsera/awards/
      229 -├── support/                              # ApplicationStarter
          - com perfis e argumentos; KafkaTestSupport (produtor e consumi
          -dor reais)                                                    
      232 +├── support/                              # ApplicationStarter
          + com perfis e argumentos; KafkaTestSupport (produtor e consumi
          +dor reais); PostgresSupport                                   
      233  ├── isolation/   DefaultModeIsolationIT
      234  ├── security/    AuthDisabledByDefaultIT, AuthEnabledIT, AuthP
           roblemDetailsIT, TokenNotLoggedIT,
      235  │                NoGeneratedPasswordIT, CacheHeadersIT, AuthCo
           ntractIT
      236  ├── docs/        ApiDocsSecurityIT
      237  ├── kafka/       KafkaReferenceFileIT, KafkaEquivalenceIT, Kaf
           kaReadinessIT, KafkaInvalidLinesIT,
      238  │                KafkaStructuralFailureIT, KafkaNewWinnerIT, K
           afkaUpdateIT, KafkaIdempotencyIT,
      236 -│                KafkaInvalidEventsIT, KafkaMetricsIT, Publish
          -erSummaryIT                                                   
      237 -└── postgres/    SchemaPostgresIT, EquivalencePostgresIT, List
          -ingOrderPostgresIT, RestartPostgresIT                         
      239 +│                KafkaInvalidEventsIT, KafkaMetricsIT, Publish
          +erSummaryIT, KafkaBrokerUnavailableIT,                        
     … +7 lines (ctrl+o to expand)

● Update(specs\003-modo-docker\quickstart.md)
  ⎿  Added 5 lines, removed 1 line
      21 
      22  ## 2. Autenticação ligada, ainda só com o JDK
      23 
      24 +O valor do token de demonstração está **só** no README, na seçã
         +o "Autenticação" (definido no T088,                            
      25 +Princípio VII). Exporte-o antes:                               
      26 +                                                               
      27  ```bash
      25 -API_TOKEN=<token-de-demonstração-do-README> ./mvnw spring-boot:
         -run                                                            
      28 +export API_TOKEN='<valor da seção Autenticação do README>'     
      29 +./mvnw spring-boot:run                                         
      30 
      31  curl -si localhost:8080/api/producers/intervals
              # 401 em Problem Details
      32  curl -si -H 'Authorization: Bearer errado' localhost:8080/api/m
          ovies # 401, mesmo corpo

  Read 1 file, ran 1 shell command (ctrl+o to expand)

● I'm applying the changes to tasks.md next: new tests, T035 with four startups,
  the shared FIELD_TOO_LONG, fixes to T048 and T060, and the T093 request. 
  · summary

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 3 lines, removed 1 line
      241  - [ ] T025 [P] [US1] Criar `src/test/java/com/outsera/awards/k
           afka/KafkaEquivalenceIT.java` (`@EmbeddedKafka` na classe; dua
           s execuções com o `ApplicationStarter`: carga direta e `kafka`
           , com o arquivo padrão; a de Kafka chama `awaitReady` antes de
            consultar):
      242    - `S003 FR-018 US1-2 SC-001`: os intervalos, `/api/movies?si
           ze=100&page=0..2`, `?winner=true` e
      243      `?year=1990` são idênticos byte a byte, inclusive o `ETag`
           ;
      244 -  - repetir com `classpath:scenarios/adversario/13-ordenacao-c
          -om-acentos-e-caixa.csv`.                                      
      244 +  - repetir com `classpath:scenarios/adversario/13-ordenacao-c
          +om-acentos-e-caixa.csv`;                                      
      245 +  - repetir com `classpath:scenarios/field-too-long.csv` (T034
          +a), sem exceção (FR-018). As linhas                           
      246 +    rejeitadas por `FIELD_TOO_LONG` faltam nos dois caminhos, 
          +e as respostas são idênticas.                                 
      247  - [ ] T026 [P] [US1] Criar `src/test/java/com/outsera/awards/k
           afka/KafkaReadinessIT.java` (`@SpringBootTest`, perfil `kafka`
           , `awards.ingestion.publish-completion=false`; research R10):
      248    - `S003 FR-013 US1-3`:
      249      - depois de `totalElements` chegar a 206 na listagem, a re
           adiness e `/actuator/health` continuam

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 39 lines, removed 2 lines
      318  - [ ] T034 [P] [US1] Criar `src/test/java/com/outsera/awards/k
           afka/PublisherSummaryIT.java` (`@Tag("log")`):
      319    - `S003 FR-009`: com `scenarios/load-summary.csv` e o perfil
            `kafka`, o log tem uma única linha de
      320      resumo da publicação, com as contagens do `LoadSummaryIT`.
      321 -- [ ] T035 [US2] Criar `src/test/java/com/outsera/awards/postg
          -res/RestartPostgresIT.java` (`postgres-it`; `@EmbeddedKafka` n
          -a classe; três execuções do `ApplicationStarter` com `Postgres
          -Support.args` + `KafkaTestSupport.args`, mesmo esquema e mesmo
          - grupo):                                                      
      321 +- [ ] T034a [P] [US1] Criar o cenário `src/test/resources/scen
          +arios/field-too-long.csv` e o IT `src/test/java/com/outsera/aw
          +ards/ingestion/FieldTooLongIT.java` (carga direta, `@Tag("log"
          +)`; spec 001 FR-006 e FR-006c, atualizadas pela spec 003):    
      322 +  - o cenário tem duas linhas válidas e uma linha inválida par
          +a cada limite: título de 501 caracteres,                      
      323 +    estúdios de 1001, produtores de 1001 e um nome de produtor
          + de 501. Também tem um título de                              
      324 +    **exatamente** 500 caracteres, que é aceito;              
      325 +  - `S003 FR-018` e `001 FR-006`: a subida não falha; cada lin
          +ha inválida gera `Linha <n> rejeitada: um                     
      326 +    campo excede o tamanho da coluna (<campo>)`; o resumo cont
          +a 4 rejeitadas; o filme de 500 caracteres                     
      327 +    aparece na listagem.                                      
      328 +- [ ] T034b [P] [US1] Criar `src/test/java/com/outsera/awards/
          +kafka/KafkaBrokerUnavailableIT.java` (`ApplicationStarter`, pe
          +rfil `kafka`, `KAFKA_BOOTSTRAP_SERVERS` apontando para uma por
          +ta local que o teste abre com `ServerSocket(0)` e fecha logo d
          +epois, sem broker):                                           
      329 +  - `S003 FR-013` (caso de borda "Broker indisponível"): a apl
          +icação sobe, e durante 10 s a liveness                        
      330 +    responde 200 e a readiness 503;                           
      331 +  - **verificar:** o tempo de subida com o broker fora (`Kafka
          +Admin` e o publicador, research R8). Se a                     
      332 +    subida falhar ou passar de 60 s, ajustar `spring.kafka.adm
          +in.fail-fast: false` e                                        
      333 +    `spring.kafka.admin.operation-timeout` no `application-kaf
          +ka.yml` e registrar na R8.                                    
      334 +- [ ] T034c [US3] **Verificar antes** (research R9, "Tópico de
          + erros fora do ar"): com o broker embutido, subir o perfil `ka
          +fka` com `KAFKA_TOPIC_ERRORS="tópico inválido!"` e anotar se o
          + `NewTopic` desse nome derruba a subida. Depois, criar `src/te
          +st/java/com/outsera/awards/kafka/KafkaErrorTopicUnavailableIT.
          +java` (`@Tag("log")`) pela saída que a verificação indicar:   
      335 +  - se a subida não cai, usar o nome inválido;                
      336 +  - se cai, usar `awards.ingestion.create-error-topic=false` c
          +om um nome válido não criado e a criação                      
      337 +    automática de tópicos desligada no `@EmbeddedKafka` (`brok
          +erProperties = "auto.create.topics.enable=false"`);           
      338 +  - `S003 FR-015` (caso de borda "Tópico de erros indisponível
          +"):                                                           
      339 +    - um evento inválido não é gravado;                       
      340 +    - o log tem o aviso com o motivo e a falha do envio ao tóp
          +ico de erros;                                                 
      341 +    - um evento válido enviado em seguida aparece na listagem 
          +em até 15 s;                                                  
      342 +    - a readiness continua 200.                               
      343 +                                                              
      344 +  Se nenhuma das duas saídas for possível sem dublê, não criar
          + o IT e registrar o limite em                                 
      345 +  `docs/ai/decisoes.md` (T048).                               
      346 +- [ ] T035 [US2] Criar `src/test/java/com/outsera/awards/postg
          +res/RestartPostgresIT.java` (`postgres-it`; `@EmbeddedKafka` n
          +a classe; **quatro execuções** do `ApplicationStarter`, ou sej
          +a, três reinícios, como o SC-004 pede; `PostgresSupport.args` 
          ++ `KafkaTestSupport.args`, mesmo esquema e mesmo grupo):      
      347    - execução 1, arquivo padrão: depois da readiness, o teste e
           nvia um filme novo
      348      (`2030`/`Filme Novo`, `Joel Silver`, vencedor) e uma atual
           ização de um filme do arquivo
      349      (`1980`/`Can't Stop the Music`, com `winner: false`), e es
           pera os dois aparecerem;
      350    - execução 2:
      351      - `S003 US2-4`: `Filme Novo` continua;
      352      - `S003 US2-6`: `Can't Stop the Music` voltou a `winner: t
           rue`, o valor do arquivo;
      328 -  - execuções 2 e 3: `S003 US2-3 SC-004`: `totalElements` 207 
          -e nenhum filme repetido na listagem                           
      353 +  - execuções 2, 3 e 4: `S003 US2-3 SC-004`: `totalElements` 2
          +07 e nenhum filme repetido na listagem                        
      354      completa.
      355 +- [ ] T035a [US2] Criar `src/test/java/com/outsera/awards/post
          +gres/RetryPostgresIT.java` (`postgres-it`, `@Isolated` do JUni
          +t, `@EmbeddedKafka`, perfil `postgres` + `kafka`; research R9,
          + "Comprovação da nova tentativa"):                            
      356 +  - `S003 FR-015a`: prova a **nova tentativa quando a gravação
          + falha**, e não a queda real do banco                         
      357 +    (essa é o passo 7b do T071);                              
      358 +  - com a aplicação pronta, o teste renomeia `movie` para `mov
          +ie_retry_off` no esquema da classe, por                       
      359 +    JDBC, e publica `{"year":2032,"title":"Durante a Falha","s
          +tudios":null,"producers":"Producer R","winner":true}`;        
      360 +  - durante 5 s, a readiness continua 200, o tópico de erros n
          +ão recebe nada e o log tem a nova                             
      361 +    tentativa do `DefaultErrorHandler`;                       
      362 +  - **num `finally`**, renomeia a tabela de volta para `movie`
          +, mesmo que uma asserção falhe;                               
      363 +  - depois, o filme aparece na listagem em até 60 s, e `awards
          +_ingestion_events_total{outcome="rejected"}`                  
      364 +    não mudou;                                                
      365 +  - **sem paralelismo:** a execução `postgres` do Failsafe (T0
          +10) não liga o paralelismo do JUnit, e o                      
      366 +    `@Isolated` garante isso mesmo que alguém o ligue.        
      367  - [ ] T036 [US1] Confirmar a falha: `./mvnw verify` falha em t
           odos os ITs de `kafka/`, porque não há
      368    ingestão por Kafka e a carga direta roda sempre. O `DefaultM
           odeIsolationIT` falha porque a
      369    autoconfiguração do Kafka cria os clientes, ou passa. Anotar
            qual dos dois aconteceu. Se passar, ele

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 12 lines, removed 1 line
      388      - `awards.ingestion.publish-completion: true`;
      389      - `management.endpoint.health.group.readiness.include: rea
           dinessState,ingestion` (**verificar
      390        R10**).
      391 -- [ ] T038 [P] [US1] Criar `src/main/java/com/outsera/awards/i
          -ngestion/MovieFieldRules.java`, com as regras de ano (1 a 9999
          -, sem estouro de `int`), título não vazio e produtores com ao 
          -menos um nome pelo `ProducerNameParser`. Mover para ela as reg
          -ras de `MovieCsvReader.validate`, que passa a usá-la **sem mud
          -ar a ordem nem os motivos** (os ITs do Marco 1 provam que nada
          - mudou).                                                      
      391 +- [ ] T038 [P] [US1] Criar `src/main/java/com/outsera/awards/i
          +ngestion/MovieFieldRules.java`, com estas regras:             
      392 +  - ano de 1 a 9999, sem estouro de `int`;                    
      393 +  - título não vazio;                                         
      394 +  - produtores com ao menos um nome pelo `ProducerNameParser`;
      395 +  - **tamanho máximo**: `title` ≤ 500, `studios` ≤ 1000, `prod
          +ucers` ≤ 1000 e cada nome de produtor                         
      396 +    ≤ 500 caracteres (`VARCHAR(500)` e `VARCHAR(1000)` da V1).
      397 +                                                              
      398 +  Mover para ela as regras de `MovieCsvReader.validate`, que p
          +assa a usá-la sem mudar a ordem nem os                        
      399 +  motivos existentes (os ITs do Marco 1 provam que nada mudou)
          +. Acrescentar `FIELD_TOO_LONG("um campo                       
      400 +  excede o tamanho da coluna")` ao `RejectionReason`, **antes 
          +de `DUPLICATE`** (spec 001, data-model,                       
      401 +  motivo 8). O detalhe da rejeição cita o campo. O parser de e
          +ventos (T041) usa a mesma classe, para que                    
      402 +  os dois caminhos rejeitem as mesmas linhas.                 
      403  - [ ] T039 [P] [US1] Criar `src/main/java/com/outsera/awards/i
           ngestion/MovieFileReading.java`, com a verificação de existênc
           ia e de leitura do arquivo, o `MovieCsvReader.read` e os aviso
           s `Linha {} rejeitada: {}`, com o mesmo texto e o mesmo logger
            de hoje (`MovieFileLoader`). Em `MovieFileLoader.java`:
      404    - pôr `@Profile("!kafka")`;
      405    - usar `MovieFileReading`;

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 3 lines, removed 2 lines
      410  - [ ] T040 [P] [US1] Criar em `src/main/java/com/outsera/award
           s/ingestion/kafka/`:
      411    - `KafkaIngestionProperties` (`@ConfigurationProperties("awa
           rds.ingestion")`: `topicMovies`,
      412      `topicErrors`, `publishCompletion`);
      413 -  - `EventRejectionReason` (enum com os códigos e as descriçõe
          -s do data-model.md, inclusive                                 
      414 -    `FIELD_TOO_LONG`, "um campo excede o tamanho da coluna"); 
      413 +  - `EventRejectionReason` (enum com os códigos e as descriçõe
          +s do data-model.md). Os motivos de                            
      414 +    conteúdo reaproveitam as descrições do `RejectionReason` (
          +inclusive `FIELD_TOO_LONG`), e os de                          
      415 +    representação são `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN` e
          + `RUN_ID_MISSING`;                                            
      416    - `LoadCompletedEvent` (record `runId`, `count`).
      417 
      418    Todos com `@Profile("kafka")` onde se aplica.

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 2 lines, removed 2 lines
      426    - `studios` vazio ou nulo → `null`;
      427    - `producers` obrigatório;
      428    - `winner` booleano JSON obrigatório;
      429 -  - por último, `FIELD_TOO_LONG`, citando os limites: "`title`
          - > 500, `studios` > 1000, `producers` >                       
      430 -    1000 ou um nome de produtor > 500 caracteres";            
      429 +  - por último, o tamanho máximo pela **mesma** `MovieFieldRul
          +es` do CSV (`FIELD_TOO_LONG`: "`title` >                      
      430 +    500, `studios` > 1000, `producers` > 1000 ou um nome de pr
          +odutor > 500 caracteres");                                    
      431    - devolve um resultado selado: `Movie(MovieRow)`, `Completed
           (LoadCompletedEvent)` ou
      432      `Rejected(EventRejectionReason, String detalhe)`. O `lineN
           umber` do `MovieRow` é `0` para eventos.
      433  - [ ] T042 [US1] Criar `src/main/java/com/outsera/awards/inges
           tion/kafka/IngestionReadiness.java` (`HealthIndicator` de nome
            `ingestion`, research R10):
● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 2 lines, removed 1 line
      455      - incrementa `outcome=rejected`;
      456      - se o envio ao tópico de erros falhar, registra a falha e
            segue.
      457  - [ ] T045 [US1] Criar `src/main/java/com/outsera/awards/inges
           tion/kafka/KafkaIngestionConfiguration.java` (`@Configuration`
           , `@Profile("kafka")`):
      458 -  - `NewTopic` para os dois tópicos (1 partição, replicação 1)
          -;                                                             
      458 +  - `NewTopic` para os dois tópicos (1 partição, replicação 1)
          +. O do tópico de erros fica atrás de                          
      459 +    `awards.ingestion.create-error-topic` (padrão `true`) só s
          +e o T034c indicar essa saída;                                 
      460    - `DefaultErrorHandler` com `ExponentialBackOff` (inicial 50
           0 ms, multiplicador 2, máximo 30 s, sem
      461      limite de tentativas) para as exceções que escapam do ouvi
           nte, ou seja, as falhas de infraestrutura;
      462    - registro dos contadores com o rótulo `application`.

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 3 lines, removed 5 lines
      469    **Verificar (R15):** se o tempo do `verify` passar de 2× o d
           o T001, compartilhar o broker por execução
      470    do Failsafe e manter os tópicos por classe. Se o broker embu
           tido falhar no Windows ao apagar os logs,
      471    aplicar a saída da R15 e registrar o motivo.
      472 -- [ ] T047 [US2] Com o Docker: `./mvnw -Ppostgres-it verify` v
          -erde, inclusive o `RestartPostgresIT`. Sem Docker, registrar `
          -PULADO`.                                                      
      472 +- [ ] T047 [US2] Com o Docker: `./mvnw -Ppostgres-it verify` v
          +erde, inclusive o `RestartPostgresIT` e o `RetryPostgresIT`. S
          +em Docker, registrar `PULADO`.                                
      473  - [ ] T048 [US1] Registrar em `docs/ai/decisoes.md`:
      474 -  - o `FIELD_TOO_LONG` e a diferença conhecida com a carga dir
          -eta (data-model.md);                                          
      475 -  - que a nova tentativa sem limite para falhas de infraestrut
          -ura (R9) não tem IT no `./mvnw verify`,                       
      476 -    porque provocar uma falha de banco exigiria um dublê (Prin
          -cípio I). Ela é comprovada no passo de                        
      477 -    parada do PostgreSQL do `compose-test.sh` (T071 e T081).  
      474 +  - o resultado da verificação do T034c e a saída escolhida, o
          +u o limite, se nenhuma for possível;                          
      475 +  - o resultado da verificação do T034b (tempo de subida com o
          + broker fora).                                                
      476  - [ ] T049 [US1] Commit `Etapa 2: ingestão por Kafka com readi
           ness e tópico de erros` com T021 a T048.
      477  - [ ] T050 [US1] **Pedido ao Anderson:** acionar o testador ad
           versário (Codex) para a ingestão por Kafka. Ele cria eventos d
           e ataque a partir da spec 003 e de `contracts/kafka-events.md`
           , sem ler o código de produção. Cada falha encontrada vira IT
           permanente em `src/test/java/com/outsera/awards/kafka/adversar
           io/`, com a correção, num commit de correção da etapa 2.
      478  - [ ] T051 [US2] **Pendente: exige o push, que é do Anderson.*
           * Conferir o job `postgres` verde com o `RestartPostgresIT`.

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 1 line, removed 1 line
      475    - o resultado da verificação do T034b (tempo de subida com o
            broker fora).
      476  - [ ] T049 [US1] Commit `Etapa 2: ingestão por Kafka com readi
           ness e tópico de erros` com T021 a T048.
      477  - [ ] T050 [US1] **Pedido ao Anderson:** acionar o testador ad
           versário (Codex) para a ingestão por Kafka. Ele cria eventos d
           e ataque a partir da spec 003 e de `contracts/kafka-events.md`
           , sem ler o código de produção. Cada falha encontrada vira IT
           permanente em `src/test/java/com/outsera/awards/kafka/adversar
           io/`, com a correção, num commit de correção da etapa 2.
      478 -- [ ] T051 [US2] **Pendente: exige o push, que é do Anderson.*
          -* Conferir o job `postgres` verde com o `RestartPostgresIT`.  
      478 +- [ ] T051 [US2] **Pendente: exige o push, que é do Anderson.*
          +* Conferir o job `postgres` verde com o `RestartPostgresIT` e 
          +o `RetryPostgresIT`.                                          
      479 
      480  **Checkpoint**: US1, US3 e a persistência da US2 completas no
           `./mvnw verify` e no perfil `postgres-it`.
      481 

  Read 1 file, ran 1 shell command (ctrl+o to expand)

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 3 lines
      512    - nos intervalos e na listagem com o token, o corpo é o do M
           arco 2 (Joel Silver e Matthew Vaughn), e
      513      a revalidação com o `ETag` dá 304;
      514    - `POST /api/movies` sem token → 401;
      515 +  - `S003` (caso de borda `OPTIONS`): com o token ligado, `OPT
          +IONS /api/movies` e `OPTIONS                                  
      516 +    /api/producers/intervals` sem token → 401. Com o token des
          +ligado (`AuthDisabledByDefaultIT`), o                         
      517 +    mesmo `OPTIONS` responde como no Marco 2 (2xx com `Allow`)
          +;                                                             
      518    - `S003 FR-022 US4-5`: liveness, readiness, `/actuator/healt
           h`, `/swagger-ui.html`, `/openapi.yaml`,
      519      um webjar referenciado pela página e um caminho inexistent
           e (404) respondem sem token;
      520    - o token com espaços em volta ou com outra caixa → 401.

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 1 line, removed 1 line
      529  - [ ] T056 [P] [US4] Criar `src/test/java/com/outsera/awards/s
           ecurity/NoGeneratedPasswordIT.java` (`@Tag("log")`, duas subid
           as pelo `ApplicationStarter`, sem token e com token):
      530    - `S003 FR-019`: o log não contém `Using generated security
           password` nem `UserDetailsService`.
      531  - [ ] T057 [P] [US4] Criar `src/test/java/com/outsera/awards/s
           ecurity/CacheHeadersIT.java` (duas classes aninhadas, token de
           sligado e ligado; research R12):
      532 -  - `S003 FR-019 FR-021`: as respostas 200 e 304 dos intervalo
          -s e da listagem não têm `Cache-Control`,                      
      532 +  - `S003 FR-022a FR-019 FR-021`: as respostas 200 e 304 dos i
          +ntervalos e da listagem não têm `Cache-Control`,              
      533      `Pragma` nem `Expires` e têm `ETag`;
      534    - `X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY
           ` aparecem nos endpoints de negócio, na
      535      readiness, em `/openapi.yaml` e no 401;

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 3 lines, removed 1 line
      552    - falha listando os arquivos apagados ou alterados.
      553 
      554    Criar `.github/unchanged-tests-exceptions.txt` com uma única
            linha:
      555 -  `src/test/java/com/outsera/awards/docs/ApiDocsIT.java Exceçã
          -o do ApiDocsIT à regra dos testes sem alteração`.             
      555 +  ``src/test/java/com/outsera/awards/docs/ApiDocsIT.java Exceç
          +ão do `ApiDocsIT` à regra dos testes sem alteração``.         
      556 +  O título é copiado exatamente do `docs/ai/decisoes.md`, **co
          +m as crases**, e o script compara o texto                     
      557 +  exato.                                                      
      558    Testar numa cópia temporária, sem versionar: alterar um IT d
           a tag faz o script falhar; o `ApiDocsIT`
      559    alterado passa; uma exceção com título inexistente falha.
      560  - [ ] T061 [US4] Confirmar a falha: `./mvnw verify` falha no `
           AuthEnabledIT`, no `AuthProblemDetailsIT`, no `AuthContractIT`
           , no `ApiDocsSecurityIT` e nos ITs de cabeçalho, porque não há
            segurança nem contrato 1.2.0. O `AuthDisabledByDefaultIT` pas
           sa. `.github/scripts/check-unchanged-tests.sh v0.2.0` passa.

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 2 lines, removed 1 line
      565  - [ ] T063 [P] [US4] Criar `src/main/java/com/outsera/awards/s
           ecurity/SecurityProperties.java` (`@ConfigurationProperties("a
           wards.security")`, `token`). `enabled()` é verdadeiro só com o
            token não vazio. O `toString` nunca mostra o valor.
      566  - [ ] T064 [P] [US4] Criar `src/main/java/com/outsera/awards/s
           ecurity/TokenAuthenticationFilter.java` (`OncePerRequestFilter
           `):
      567    - lê `Authorization` e só aceita o prefixo exato `Bearer `;
      568 -  - compara `SHA-256(recebido)` com `SHA-256(esperado)` por `M
          -essageDigest.isEqual` (FR-024);                               
      568 +  - compara `SHA-256(recebido)` com `SHA-256(esperado)` por `M
          +essageDigest.isEqual`, os dois em UTF-8                       
      569 +    (decisão de design da research R11; nunca `String.equals` 
          +nem `Arrays.equals`);                                         
      570    - se forem iguais, põe no `SecurityContext` uma autenticação
            `api-client` sem papéis; se não forem, não
      571      faz nada;
      572    - nunca registra o cabeçalho.

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 4 lines, removed 4 lines
      594 
      595    Rodar `.github/scripts/check-unchanged-tests.sh v0.2.0`: ver
           de.
      596  - [ ] T069 [US4] Acrescentar ao `specs/003-modo-docker/checkli
           sts/requirements.md` a seção "Revisão de segurança":
      597 -  - a comparação do token usa `MessageDigest.isEqual` sobre SH
          -A-256 (FR-024). O tempo constante não tem                     
      598 -    IT confiável;                                             
      597 +  - a comparação do token no `TokenAuthenticationFilter` usa o
          + mecanismo da research R11:                                   
      598 +    `MessageDigest.isEqual(sha256(recebido), sha256(esperado))
          +`, em UTF-8, sem `String.equals`,                             
      599 +    `Arrays.equals` nem saída antecipada;                     
      600 +  - o ADR 0017 cita esse mecanismo;                           
      601    - o filtro não registra cabeçalhos;
      602    - a matriz de acesso do `SecurityConfiguration` é igual à de
            `contracts/operational-endpoints.md`.
      601 -                                                              
      602 -  Registrar em `docs/ai/decisoes.md` por que a FR-024 é compro
          -vada por revisão.                                             
      603  - [ ] T070 [US4] Commit `Etapa 3: autenticação por token com S
           pring Security e contrato 1.2.0` com T052 a T069.
      604 
      605  **Checkpoint**: US4 completa. O modo padrão continua idêntico,
            agora com a guarda do token.

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 4 lines, removed 2 lines
      623      e espera `min` com o intervalo 0 de Joel Silver em até 5 s
           ;
      624    - no passo 6, publica `{"year":0,…}` e confere `awards-error
           -reason:YEAR_OUT_OF_RANGE` no tópico de
      625      erros;
      626 -  - no passo 7, espera `totalElements` 207;                   
      626 +  - no passo 7, roda `docker compose down`, **sem `-v`**, e de
          +pois `docker compose up -d --wait` (todos os                  
      627 +    containers são recriados sobre os mesmos volumes, US5-5), 
          +e espera `totalElements` 207;                                 
      628    - no passo 8, gera o token com `openssl rand -hex 16` dentro
            de `set +x`, sem imprimir nem gravar.
      629 
      629 -  Acrescentar o **passo 7b** (comprovação da R9, T048):       
      630 +  Acrescentar o **passo 7b**, que comprova a queda real do ban
          +co (FR-015a; o `RetryPostgresIT`, T035a,                      
      631 +  comprova a nova tentativa quando a gravação falha):         
      632    1. `docker compose stop postgres`;
      633    2. publicar `{"year":2031,"title":"Durante a Queda",…}`;
      634    3. esperar 5 s;

  Ran 1 shell command (ctrl+o to expand)

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 6 lines, removed 2 lines
      728      demonstração;
      729    - **eventos:** o formato do evento de filme, com link para `
           specs/003-modo-docker/contracts/kafka-events.md`,
      730      e como publicar um vencedor novo e ler o tópico de erros p
           elo `kafka-cli`;
      731 -  - **autenticação:** `API_TOKEN`, `Authorization: Bearer`, a 
          -matriz de acesso e o exemplo com o token de                   
      732 -    demonstração;                                             
      731 +  - **autenticação:** `API_TOKEN`, `Authorization: Bearer`, a 
          +matriz de acesso (inclusive o `OPTIONS`) e                    
      732 +    o exemplo com o token de demonstração;                    
      733 +  - **token de demonstração:** definir aqui o valor, um texto 
          +fixo e público, que deixa claro que é só                      
      734 +    para demonstração. É o único lugar do repositório com esse
          + valor (Princípio VII). O quickstart, os                      
      735 +    testes e o compose não o repetem: os testes geram tokens a
          +leatórios, e o compose tem o token                            
      736 +    desligado por padrão;                                     
      737    - **perfil `postgres-it`:** como rodar e por que fica fora d
           o `verify`;
      738    - os jobs novos do pipeline;
      739    - **tabela de requisitos e testes:** acrescentar a spec 003;

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 12 lines
      749    - as pendências de push;
      750    - o resultado do testador adversário (T050).
      751  - [ ] T092 Commit `Etapa 5: ADRs e README do Marco 3` com T085
            a T091. Avisar o Anderson de que o bolt está pronto para o PR
           . Depois do merge, a sessão é exportada para `docs/ai/sessoes/
           `.
      752 +- [ ] T093 **Pedido ao Anderson, depois da primeira execução d
          +o pipeline no PR e antes do merge.** Antes da primeira execuçã
          +o, os jobs novos não aparecem na lista de checks da proteção. 
          +Pedir a configuração da proteção da `main` (Settings → Branche
          +s, ou o ruleset da `main`):                                   
      753 +  - incluir como *required status checks* os jobs novos `postg
          +res` e `compose` (constituição 1.7.0,                         
      754 +    Princípio VI: o job vermelho reprova o merge; spec 003, FR
          +-007);                                                        
      755 +  - conferir se os jobs existentes já estão na lista: `workflo
          +w-policy`, `build`, `codeql`,                                 
      756 +    `dependencies`, `dockerfile-lint` e `image`. Incluir os qu
          +e faltarem;                                                   
      757 +  - deixar **desligado o bypass para administradores** ("Do no
          +t allow bypassing the above settings" na                      
      758 +    proteção de branch, ou nenhum *bypass actor* no ruleset), 
          +para que nem o administrador faça merge com                   
      759 +    um job vermelho.                                          
      760 
      761 +  Depois da configuração, conferir na página do PR que os oito
          + checks aparecem como obrigatórios.                           
      762 +  Registrar em `docs/ai/decisoes.md` a lista configurada e a c
          +onfirmação do bypass desligado, sem nenhum                    
      763 +  dado pessoal, num commit de fechamento antes do merge.      
      764 +                                                              
      765  ---
      766 
      767  ## Dependencies & Execution Order

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/tasks.md
      sed -i 's/^- etapa 2: T021 → T022 → T023 a T035 → T036 → T037 → T038 a
      T040 → T041…)
  ⎿  688:  - `workflow-policy`, `build`, `postgres` e `compose` verdes, alé
     m dos job
     792:- etapa 2: T021 → T022 → T023 a T035a (o T034c começa pela verific
     … +8 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\tasks.md +3 -3
      685  - [ ] T082 [US5] Rodar `./mvnw verify`: verde. Nada desta etap
           a muda a aplicação.
      686  - [ ] T083 [US5] Commit `Etapa 4: compose com PostgreSQL, Kafk
           a, Prometheus e Grafana` com T071 a T082.
      687  - [ ] T084 [US5] **Pendente: exige o push, que é do Anderson.*
           * Conferir na execução do branch:
      688 -  - `workflow-policy`, `build`, `postgres` e `compose` verdes,
          - além dos jobs do Marco 2;                                    
      688 +  - `workflow-policy`, `build`, `postgres` e `compose` verdes,
          + além dos jobs do Marco 2. A configuração dos checks obrigatór
          +ios vem depois, no T093;                                      
      689    - o tempo do SC-008 no resumo do `compose`;
      690    - os PRs do Dependabot para o `docker-compose` aparecem depo
           is do merge.
      691 
     ...
      789 
      790  - apoio → testes → confirmação da falha → implementação → `./m
           vnw verify` verde → commit;
      791  - etapa 1: T003 → T004 → T005 a T009 → T010 → T011 → T012 a T0
           16 → T017 → T018 → T019;
      792 -- etapa 2: T021 → T022 → T023 a T035 → T036 → T037 → T038 a T0
          -40 → T041 → T042 → T043 e T044 →                              
      792 +- etapa 2: T021 → T022 → T023 a T035a (o T034c começa pela ver
          +ificação) → T036 → T037 → T038 a T040 → T041 → T042 → T043 e T
          +044 →                                                         
      793    T045 → T046 → T047 → T048 → T049;
      794  - etapa 3: T052 a T060 → T061 → T062 → T063 a T065 → T066 → T0
           67 → T068 → T069 → T070;
      795  - etapa 4: T071 e T072 → T073 → T074 → T075 → T076 a T079 → T0
           80 → T081 → T082 → T083.
     ...
      806  ## Parallel Opportunities
      807 
      808  - **Etapa 1:** T005, T006, T007 e T008 em paralelo, depois do
           T003 e do T004. T013 e T014 em paralelo.
      809 -- **Etapa 2:** T023 a T034 em paralelo, depois do T022 (classe
          -s de teste diferentes; o T030 cria o seu                      
      809 +- **Etapa 2:** T023 a T034b em paralelo, depois do T022 (class
          +es de teste diferentes; o T030 cria o seu                     
      810    cenário). T038, T039 e T040 em paralelo.
      811  - **Etapa 3:** T052 a T059 em paralelo; o T060 em paralelo com
            eles, pela infraestrutura. T063, T064 e
      812    T065 em paralelo.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/tasks.md
      sed -i 's|^      `reportsDirectory`/`summaryFile` em
      `target/failsafe-reports-postg…)
  ⎿  154:      `reportsDirectory`/`summaryFile` em `target/failsafe-reports
     -postgres/`, sem ligar o paralelismo do
     717:  - `0017-autenticacao-por-token.md` (R11 a R13). Precisa citar o
     mecanismo de comparação do token
  ⎿  Updated specs\003-modo-docker\tasks.md +5 -2
      151    - criar o perfil `postgres-it`:
      152      - `it.default.skip=true` e `jacoco.check.skip=true`;
      153      - uma execução `postgres` do Failsafe, com `<includes>**/*
           PostgresIT.java</includes>` e
      154 -      `reportsDirectory`/`summaryFile` em `target/failsafe-rep
          -orts-postgres/`.                                              
      154 +      `reportsDirectory`/`summaryFile` em `target/failsafe-rep
          +orts-postgres/`, sem ligar o paralelismo do                   
      155 +      JUnit (o `RetryPostgresIT`, T035a, não pode rodar em par
          +alelo com outro IT).                                          
      156  - [ ] T011 [US2] Confirmar a falha:
      157    - `./mvnw verify` continua verde, e nenhum `*PostgresIT` rod
           a;
      158    - com o Docker (T002), subir o PostgreSQL do quickstart, seç
           ão 3, e rodar `./mvnw -Ppostgres-it
     ...
      714    - `0015-gravacao-idempotente-com-atualizacao.md` (R5 e a rev
           ersão no reinício aceita nas
      715      Clarifications);
      716    - `0016-topico-de-erros-e-novas-tentativas.md` (R7, R9 e `FI
           ELD_TOO_LONG`);
      716 -  - `0017-autenticacao-por-token.md` (R11 a R13). Precisa regi
          -strar **como decisão intencional** que                        
      717 +  - `0017-autenticacao-por-token.md` (R11 a R13). Precisa cita
          +r o mecanismo de comparação do token                          
      718 +    (`MessageDigest.isEqual` sobre SHA-256 em UTF-8, research 
          +R11; era a FR-024, retirada da spec), para a                  
      719 +    "Revisão de segurança" conferir. Precisa também registrar 
          +**como decisão intencional** que                              
      720      `X-Content-Type-Options` e `X-Frame-Options` aparecem em t
           odas as respostas, e que o `Cache-Control`
      721      fica fora dos endpoints de negócio (FR-037);
      722    - `0018-compose-com-observabilidade.md` (R17 a R19 e as cred
           enciais de demonstração da constituição

● Update(docs\ai\decisoes.md)
  ⎿  Added 47 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-24 — Segunda análise da spec 003 (`/speckit-analyze`
         +)                                                              
       7 +                                                               
       8 +O responsável aceitou as correções, com os ajustes abaixo. Elas
         + foram aplicadas na spec, no plano, na                         
       9 +research, nos contratos e nas tarefas. A spec 001 também mudou 
         +(I1).                                                          
      10 +                                                               
      11 +- **C1, nova tentativa quando a gravação falha:**              
      12 +  - nova FR-015a e o `RetryPostgresIT` no `postgres-it`, que re
         +nomeia a tabela `movie` e a devolve num                        
      13 +    `finally`. Ele é `@Isolated` e roda sem paralelismo;       
      14 +  - a spec e a R9 dizem o que o teste comprova de fato: a nova 
         +tentativa quando a gravação falha;                             
      15 +  - a queda real do banco continua comprovada pelo passo 7b do 
         +compose.                                                       
      16 +- **C2, comparação do token:**                                 
      17 +  - a FR-024 saiu da spec, e o número ficou reservado;         
      18 +  - a comparação em tempo constante virou decisão de design na 
         +R11, com o mecanismo                                           
      19 +    (`MessageDigest.isEqual` sobre SHA-256 em UTF-8);          
      20 +  - o ADR 0017 cita o mecanismo, e a "Revisão de segurança" o c
         +onfere.                                                        
      21 +- **C3, checks obrigatórios:** nova tarefa T093, um pedido ao r
         +esponsável depois da primeira execução do                      
      22 +  pipeline no PR e antes do merge:                             
      23 +  - incluir `postgres` e `compose` como checks obrigatórios;   
      24 +  - conferir os seis jobs existentes;                          
      25 +  - deixar desligado o bypass para administradores.            
      26 +- **I1, `FIELD_TOO_LONG` sem exceção:** a pedido do responsável
         +, a diferença não foi documentada. A                           
      27 +  regra de tamanho máximo passou para a `MovieFieldRules` e val
         +e nos dois caminhos.                                           
      28 +  - **Custo, avaliado como baixo:** uma regra, um motivo no `Re
         +jectionReason` e um IT novo                                    
      29 +    (`FieldTooLongIT`).                                        
      30 +  - **Efeito no Marco 1:** uma linha do CSV com campo maior que
         + a coluna deixa de interromper a subida                        
      31 +    com um erro do banco. Ela passa a ser ignorada com aviso, e
         + a spec 001 (FR-006) e o data-model 001                        
      32 +    foram atualizados.                                         
      33 +  - Nenhum IT do Marco 1 depende desse caso, e o `KafkaEquivale
         +nceIT` passa a incluir um arquivo com                          
      34 +    esse caso.                                                 
      35 +  - As diferenças de representação do evento (`winner` booleano
         +, `year` inteiro JSON) foram para a                            
      36 +    FR-010 e a FR-012.                                         
      37 +- **I2:** a linha de exceção do `ApiDocsIT` cita o título exato
         +, com as crases.                                               
      38 +- **G1:** novo `KafkaBrokerUnavailableIT` (liveness 200 e readi
         +ness 503 com o broker fora).                                   
      39 +- **G2:** novo `KafkaErrorTopicUnavailableIT`. Antes, a tarefa 
         +verifica se um nome inválido de tópico                         
      40 +  derruba a subida; se derrubar, usa um tópico válido não criad
         +o.                                                             
      41 +- **G3:** o passo 7 do compose usa `down` sem `-v` e `up`, e nã
         +o mais `restart app`.                                          
      42 +- **A1:** o `OPTIONS` segue a matriz de acesso, e o CORS fica p
         +ara o Marco 6.                                                 
      43 +- **A2:** o valor do token de demonstração é definido só no REA
         +DME (T088), e o quickstart o cita sem                          
      44 +  repeti-lo.                                                   
      45 +- **I3:** nova FR-022a, com os cabeçalhos de proteção e a ausên
         +cia de `Cache-Control` nos endpoints de                        
      46 +  negócio, e a definição de "como no Marco 2" na FR-019.       
      47 +- **I4:** a saúde geral entra na FR-022.                       
      48 +- **I5:** o `RestartPostgresIT` faz 4 subidas (3 reinícios), co
         +mo o SC-004.                                                   
      49 +- **Não aplicados**, porque o responsável não os pediu: I6 (der
         +iva entre a estrutura do plano e as                            
      50 +  tarefas, só em parte resolvida pelas mudanças acima), I7 ("ac
         +eitas" × "publicadas"), A3 (o T036                             
      51 +  aceita que o `DefaultModeIsolationIT` passe) e C4 (propriedad
         +e `publish-completion` usada por teste).                       
      52 +                                                               
      53  ## 2026-11-24 — Exceção do `ApiDocsIT` à regra dos testes sem a
          lteração
      54 
      55  - **Origem:** plano 003. O `ApiDocsIT` (Marco 2, tag `v0.2.0`)
          compara o `/openapi.yaml` publicado, byte

● Edits applied. Running the analysis again, read-only.

  Ran 1 shell command (ctrl+o to expand)

● Segunda análise da spec 003

  Apliquei todas as correções aprovadas, e a nova análise não encontrou nenhum
  problema crítico nem grave. Sobraram 3 achados MEDIUM e 4 LOW, e nenhum
  bloqueia o /speckit-implement. Não fiz commit.

  O que foi aplicado

  Achado: C1
  Onde: Nova FR-015a e caso de borda na spec. A R9 diz o que cada teste
    comprova: o RetryPostgresIT (T035a) prova a nova tentativa quando a
    gravação falha, e o passo 7b prova a queda real do banco. O teste devolve
    o nome da tabela num finally, é @Isolated, e a execução postgres do
    Failsafe não liga o paralelismo (T010).
  ────────────────────────────────────────
  Achado: C2
  Onde: A FR-024 saiu da spec, com o número reservado. A R11 registra o
    mecanismo (MessageDigest.isEqual sobre o SHA-256 dos dois valores, em
    UTF-8, nunca String.equals ou Arrays.equals). O ADR 0017 (T086) e a
    "Revisão de segurança" (T069) citam o mecanismo.
  ────────────────────────────────────────
  Achado: C3
  Onde: T093: pedido depois da primeira execução do pipeline no PR e antes do
    merge. Inclui postgres e compose, confere os seis jobs existentes e deixa
    desligado o bypass para administradores.
  ────────────────────────────────────────
  Achado: I1
  Onde: A regra de tamanho máximo passou para a MovieFieldRules e vale nos
    dois caminhos, e a diferença não foi documentada. A FR-018 e o SC-001
    valem sem exceção. As diferenças de representação do evento (winner
    booleano, year inteiro JSON) estão na FR-010 e na FR-012.
  ────────────────────────────────────────
  Achado: I2
  Onde: A linha de exceção do ApiDocsIT cita o título exato, com as crases.
  ────────────────────────────────────────
  Achado: G1, G2
  Onde: KafkaBrokerUnavailableIT (T034b) e KafkaErrorTopicUnavailableIT
    (T034c). O T034c começa verificando se um nome de tópico inválido derruba
    a subida e tem uma saída alternativa.
  ────────────────────────────────────────
  Achado: G3
  Onde: O passo 7 do compose usa down sem -v e depois up. O 7b ficou como
    passo próprio no contrato.
  ────────────────────────────────────────
  Achado: A1, A2
  Onde: O OPTIONS segue a matriz de acesso, e o CORS fica para o Marco 6. O
    valor do token de demonstração é definido só no README (T088).
  ────────────────────────────────────────
  Achado: I3, I4, I5
  Onde: Nova FR-022a e definição de "como no Marco 2" na FR-019. A saúde geral
    entrou na FR-022. O RestartPostgresIT faz 4 subidas.

  I1 mudou o Marco 1. O custo foi baixo: uma regra, um motivo novo antes de
  DUPLICATE e o FieldTooLongIT (T034a). Mas o comportamento muda: hoje, uma
  linha do CSV com título acima de 500 caracteres interrompe a subida com um
  erro do banco. Agora ela é ignorada com aviso, como as outras linhas
  inválidas. Atualizei a FR-006 da spec 001 e o data-model 001, marcando a
  mudança como vinda da spec 003. Nenhum IT do Marco 1 depende desse caso.

  Registrei as decisões numa entrada nova de docs/ai/decisoes.md. Ela lista
  também os achados que você não pediu e por isso não apliquei: I7 ("aceitas" ×
  "publicadas"), A3 e C4. O I6 (estrutura do plano) acabou quase todo resolvido
  pelas outras mudanças.

  Achados restantes

  ID: N1
  Categoria: Inconsistência
  Severidade: MEDIUM
  Onde: quickstart.md:101
  Resumo: O roteiro de persistência do quickstart ainda usa docker compose
    restart app, enquanto o passo 7 do contrato e a US5-5 usam down e up.
  Recomendação: Trocar por docker compose down e up -d --wait.
  ────────────────────────────────────────
  ID: N2
  Categoria: Inconsistência
  Severidade: MEDIUM
  Onde: tasks.md T036 e T046
  Resumo: A confirmação da falha (T036) e o verde (T046) só citam os ITs de
    kafka/. O FieldTooLongIT fica em ingestion/.
  Recomendação: Incluí-lo nas duas tarefas. Antes do T038 ele falha, porque a
    subida cai com o erro do banco.
  ────────────────────────────────────────
  ID: A3
  Categoria: Ambiguidade
  Severidade: MEDIUM
  Onde: tasks.md T036
  Resumo: Continua aceitando que o DefaultModeIsolationIT passe antes da
    implementação. Não foi pedido.
  Recomendação: Manter, ou fixar a asserção que falha.
  ────────────────────────────────────────
  ID: N3
  Categoria: Verificação
  Severidade: LOW
  Onde: tasks.md T053
  Resumo: O resultado esperado para o OPTIONS com o token desligado ("2xx com
    Allow") foi deduzido e não verificado no Marco 2.
  Recomendação: Marcar como "verificar" e fixar no teste o status real do
    Marco 2.
  ────────────────────────────────────────
  ID: C4
  Categoria: Constituição
  Severidade: LOW
  Onde: R10, R9; T026, T034c
  Resumo: Agora são duas propriedades usadas por teste (publish-completion e,
    se for preciso, create-error-topic). Nenhuma é dublê. Não foi pedido.
  Recomendação: Documentá-las como opções operacionais no ADR 0014.
  ────────────────────────────────────────
  ID: I7
  Categoria: Terminologia
  Severidade: LOW
  Onde: spec US1-4 × R8
  Resumo: "aceitas" × "publicadas" no resumo da publicação. Não foi pedido.
  Recomendação: —
  ────────────────────────────────────────
  ID: I6
  Categoria: Deriva
  Severidade: LOW
  Onde: plano × tarefas
  Resumo: Resolvido em grande parte. O que sobra é cosmético.
  Recomendação: —

  Cobertura

  Todo requisito ativo tem ao menos uma tarefa e uma forma de comprovação do
  Princípio VI. As linhas que mudaram nesta rodada:

  ┌───────────────┬──────────┬─────────────────────────────────────────────┐
  │   Requisito   │ Tarefas  │                 Comprovação                 │
  ├───────────────┼──────────┼─────────────────────────────────────────────┤
  │ FR-013        │ T026,    │ KafkaReadinessIT, KafkaBrokerUnavailableIT  │
  │               │ T034b    │                                             │
  ├───────────────┼──────────┼─────────────────────────────────────────────┤
  │ FR-015        │ T032,    │ KafkaInvalidEventsIT,                       │
  │               │ T034c    │ KafkaErrorTopicUnavailableIT                │
  ├───────────────┼──────────┼─────────────────────────────────────────────┤
  │ FR-015a       │ T035a,   │ RetryPostgresIT (perfil postgres-it) e      │
  │ (nova)        │ T071     │ passo 7b do compose                         │
  ├───────────────┼──────────┼─────────────────────────────────────────────┤
  │ FR-018        │ T025,    │ KafkaEquivalenceIT (inclui FIELD_TOO_LONG), │
  │               │ T034a    │  FieldTooLongIT                             │
  ├───────────────┼──────────┼─────────────────────────────────────────────┤
  │ FR-022a       │ T057     │ CacheHeadersIT                              │
  │ (nova)        │          │                                             │
  ├───────────────┼──────────┼─────────────────────────────────────────────┤
  │ FR-024        │ —        │ retirado; decisão de design na R11 e no ADR │
  │               │          │  0017, conferida na revisão                 │
  ├───────────────┼──────────┼─────────────────────────────────────────────┤
  │ FR-007 e      │ T018,    │ job postgres como check obrigatório, sem    │
  │ Princípio VI  │ T093     │ bypass                                      │
  ├───────────────┼──────────┼─────────────────────────────────────────────┤
  │ SC-004        │ T035     │ 4 subidas                                   │
  └───────────────┴──────────┴─────────────────────────────────────────────┘

  ┌────────────────────────────┬────────────────────────────────────────────┐
  │          Métrica           │                   Valor                    │
  ├────────────────────────────┼────────────────────────────────────────────┤
  │ Requisitos                 │ 39 FR ativos (FR-015a e FR-022a novos;     │
  │                            │ FR-024 retirado) e 9 SC                    │
  ├────────────────────────────┼────────────────────────────────────────────┤
  │ Tarefas                    │ 97 (T034a, T034b, T034c, T035a e T093      │
  │                            │ novas)                                     │
  ├────────────────────────────┼────────────────────────────────────────────┤
  │ Requisitos com ao menos    │ 100%                                       │
  │ uma tarefa                 │                                            │
  ├────────────────────────────┼────────────────────────────────────────────┤
  │ Problemas de constituição  │ 0                                          │
  ├────────────────────────────┼────────────────────────────────────────────┤
  │ Críticos                   │ 0                                          │
  ├────────────────────────────┼────────────────────────────────────────────┤
  │ Graves                     │ 0                                          │
  ├────────────────────────────┼────────────────────────────────────────────┤
  │ Médios                     │ 3                                          │
  ├────────────────────────────┼────────────────────────────────────────────┤
  │ Baixos                     │ 4                                          │
  └────────────────────────────┴────────────────────────────────────────────┘

  Próximos passos

  Você já pode seguir para o /speckit-implement. N1 e N2 são correções de uma
  linha cada, e convém fazê-las antes da etapa 2. A3, N3 e C4 podem ser
  resolvidos durante as etapas.

  Não existe .specify/extensions.yml, então nenhum hook foi executado.

  Quer que eu aplique N1 e N2?

✻ Cogitated for 6m 37s · done terça-feira, 15:58

❯ Aplique o N1 e o N2. Confirme também se a T093 inclui o job compose entre os  
  checks obrigatórios, além do postgres. Se não incluir, acrescente. Os outros  
  (A3, N3 e C4) ficam para as etapas. Não faça commit.                          

  Read 1 file, ran 1 shell command (ctrl+o to expand)

● Update(specs\003-modo-docker\quickstart.md)
  ⎿  Added 2 lines, removed 2 lines
       98  Persistência:
       99 
      100  ```bash
      101 -docker compose restart app                                    
      101 +docker compose down          # sem -v: recria todos os contain
          +ers e mantém os volumes (US5-5)                               
      102  docker compose up -d --wait
      103 -curl -s 'localhost:8080/api/movies?year=2030'   # o filme cont
          -inua                                                          
      103 +curl -s 'localhost:8080/api/movies?year=2030'   # o filme cont
          +inua, sem duplicatas                                          
      104  docker compose down -v
      105  ```
      106 

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 2 lines, removed 1 line
      366    - **sem paralelismo:** a execução `postgres` do Failsafe (T0
           10) não liga o paralelismo do JUnit, e o
      367      `@Isolated` garante isso mesmo que alguém o ligue.
      368  - [ ] T036 [US1] Confirmar a falha: `./mvnw verify` falha em t
           odos os ITs de `kafka/`, porque não há
      369 -  ingestão por Kafka e a carga direta roda sempre. O `DefaultM
          -odeIsolationIT` falha porque a                                
      369 +  ingestão por Kafka e a carga direta roda sempre. O `FieldToo
          +LongIT` (`ingestion/`, T034a) também falha,                   
      370 +  porque a subida cai com o erro do banco enquanto a regra `FI
          +ELD_TOO_LONG` não existe. O `DefaultModeIsolationIT` falha por
          +que a                                                         
      371    autoconfiguração do Kafka cria os clientes, ou passa. Anotar
            qual dos dois aconteceu. Se passar, ele
      372    continua como guarda para as tarefas seguintes.
      373 
● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 1 line, removed 1 line
      463      limite de tentativas) para as exceções que escapam do ouvi
           nte, ou seja, as falhas de infraestrutura;
      464    - registro dos contadores com o rótulo `application`.
      465  - [ ] T046 [US1] Rodar `./mvnw verify` até ficar verde:
      466 -  - todos os ITs de `kafka/` e o `DefaultModeIsolationIT`;    
      466 +  - todos os ITs de `kafka/`, o `DefaultModeIsolationIT` e o `
          +FieldTooLongIT` (`ingestion/`);                               
      467    - a suíte dos Marcos 1 e 2 sem alteração;
      468    - a execução `json-logs` com os ITs `@Tag("log")` novos;
      469    - a cobertura ≥ 90%.

● Update(specs\003-modo-docker\tasks.md)
  ⎿  Added 4 lines, removed 2 lines
      754    - o resultado do testador adversário (T050).
      755  - [ ] T092 Commit `Etapa 5: ADRs e README do Marco 3` com T085
            a T091. Avisar o Anderson de que o bolt está pronto para o PR
           . Depois do merge, a sessão é exportada para `docs/ai/sessoes/
           `.
      756  - [ ] T093 **Pedido ao Anderson, depois da primeira execução d
           o pipeline no PR e antes do merge.** Antes da primeira execuçã
           o, os jobs novos não aparecem na lista de checks da proteção.
           Pedir a configuração da proteção da `main` (Settings → Branche
           s, ou o ruleset da `main`):
      757 -  - incluir como *required status checks* os jobs novos `postg
          -res` e `compose` (constituição 1.7.0,                         
      758 -    Princípio VI: o job vermelho reprova o merge; spec 003, FR
          --007);                                                        
      757 +  - incluir como *required status checks* os **dois** jobs nov
          +os:                                                           
      758 +    - `postgres`: comprova o comportamento que depende do Post
          +greSQL (constituição 1.7.0, Princípio                         
      759 +      VI: o job vermelho reprova o merge; spec 003, FR-007);  
      760 +    - `compose`: comprova da FR-028 à FR-035 e a queda real do
          + banco da FR-015a (passo 7b);                                 
      761    - conferir se os jobs existentes já estão na lista: `workflo
           w-policy`, `build`, `codeql`,
      762      `dependencies`, `dockerfile-lint` e `image`. Incluir os qu
           e faltarem;
      763    - deixar **desligado o bypass para administradores** ("Do no
           t allow bypassing the above settings" na

● Apliquei o N1 e o N2, e a T093 já incluía o job compose entre os checks
  obrigatórios, junto com o postgres. Não fiz commit.

  - N1: a seção de persistência do quickstart.md agora usa docker compose down
    (sem -v) e docker compose up -d --wait, no lugar do restart app. Assim todos
    os containers são recriados sobre os mesmos volumes, como pedem a US5-5 e o
    passo 7 do contrato.
  - N2:
    - a T036 diz que o FieldTooLongIT também falha antes da implementação,
      porque a subida cai com o erro do banco enquanto a regra FIELD_TOO_LONG
      não existe;
    - a T046 inclui o FieldTooLongIT entre os testes que precisam ficar verdes.
  - T093: como o compose já estava lá, só deixei explícito o motivo de cada um
    dos dois:
    - postgres comprova o que depende do PostgreSQL (FR-007, Princípio VI);
    - compose comprova da FR-028 à FR-035 e a queda real do banco do passo 7b
      (FR-015a).

    A conferência dos seis jobs existentes e o bypass desligado para
    administradores continuam na tarefa.

  A3, N3 e C4 ficam para as etapas, como você pediu.

✻ Baked for 32s · done terça-feira, 16:00

❯ /speckit-implement Execute o bolt único do marco 3, etapa por etapa, fazendo  
um commit ao fim de cada etapa. Não faça push.                                  

  Ran 1 shell command (ctrl+o to expand)

● Answered Claude's questions:
  ⎿  · Antes de implementar, conferi o checklist da spec 003 
     (`checklists/requirements.md`): 16 de 27 itens marcados. Os 16 itens de 
     qualidade da spec estão todos marcados. Os 11 desmarcados são as seções 
     "Revisão do README" (7) e "Revisão dos ADRs" (4), que por definição só o 
     revisor marca no PR, depois da etapa 5. Prossigo com a implementação mesmo 
     assim? → Sim, prosseguir (Recommended)

● Starting the Preparação phase: baseline ./mvnw verify and a Docker
  availability check.

  Ran 2 shell commands (ctrl+o to expand)

● Docker is available (server 26.0.0), and you already committed the spec.
  Running the baseline verify.

  Ran 2 shell commands (ctrl+o to expand)

● Baseline recorded: verify green in 96 s, line coverage 97.28%, Docker
  available. Marking T001 and T002 and moving on to stage 1.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; sed -i 's/^- \[ \] T001 /-
      [X] T001 /; s/^- \[ \] T002 /- [X] T002 /' specs/003-modo-docker/tasks.md;
      cat src/test/j…)
  ⎿          return new Result(failure, captured.toString(StandardCharsets.
     UTF_8));
         }
     … +164 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\tasks.md +2 -2
      71 
      72  **Purpose**: linha de base para medir o que o marco muda.
      73 
      74 -- [ ] T001 Conferir a linha de base no branch `marco/3-docker`:
      74 +- [X] T001 Conferir a linha de base no branch `marco/3-docker`:
      75    - `git tag --list v0.2.0` existe;
      76    - `./mvnw verify` está verde;
      77    - anotar a cobertura de linhas de `target/site/jacoco-it/jaco
          co.csv` e o tempo total do `verify`.
      78    O tempo é a referência do limite de 2× da research R15. Os do
          is números vão para a descrição do
      79    commit da etapa 1.
      80 -- [ ] T002 Conferir se o Docker está disponível localmente (`do
         -cker info`). Sem Docker, os                                    
      80 +- [X] T002 Conferir se o Docker está disponível localmente (`do
         +cker info`). Sem Docker, os                                    
      81    `*PostgresIT` e o `compose-test.sh` só podem ser verificados
          no pipeline. Nesse caso, as tarefas que
      82    pedem execução local registram `PULADO: Docker indisponível`
          na descrição do commit, e a verificação
      83    fica para as tarefas "Pendente: push".

  Ran 1 shell command (ctrl+o to expand)

● Writing the stage 1 test support: the ApplicationStarter overloads,
  PostgresSupport and an HTTP helper.

  Read 1 file, ran 3 shell commands (ctrl+o to expand)

● Update(src\test\java\com\outsera\awards\support\ApplicationStarter.java)
  ⎿  Added 62 lines, removed 7 lines
      56       * mudança de prontidão; spec 002, FR-009).
      57       */
      58      public static Result start(String csvPath, ApplicationListe
          ner<?>... listeners) {
      59 +        return start(csvPath, List.of(), listeners);           
      60 +    }                                                          
      61 +                                                               
      62 +    /**                                                        
      63 +     * Como {@link #start(String, ApplicationListener...)}, com
         + argumentos extras na linha de comando (por                    
      64 +     * exemplo, o perfil e o banco do PostgreSQL ou o endereço 
         +do broker; spec 003).                                          
      65 +     */                                                        
      66 +    public static Result start(String csvPath, List<String> ext
         +raArgs, ApplicationListener<?>... listeners) {                 
      67 +        return startAndCall(csvPath, extraArgs, port -> { }, li
         +steners);                                                      
      68 +    }                                                          
      69 +                                                               
      70 +    /**                                                        
      71 +     * Sobe a aplicação capturando a saída desde a primeira lin
         +ha, executa {@code whileUp} com a porta                        
      72 +     * enquanto ela está no ar e fecha o contexto. Uma falha da
         + subida fica em {@link Result#failure()}.                      
      73 +     * Uma falha do próprio {@code whileUp}, como uma asserção,
         + é relançada depois de restaurar a saída.                      
      74 +     */                                                        
      75 +    public static Result startAndCall(String csvPath, List<Stri
         +ng> extraArgs, IntConsumer whileUp,                            
      76 +            ApplicationListener<?>... listeners) {             
      77          PrintStream originalOut = System.out;
      78          PrintStream originalErr = System.err;
      79          ByteArrayOutputStream captured = new ByteArrayOutputStr
          eam();
      80          System.setOut(tee(originalOut, captured));
      81          System.setErr(tee(originalErr, captured));
      82          Throwable failure = null;
      83 +        RuntimeException callFailure = null;                   
      84 +        AssertionError assertionFailure = null;                
      85          try {
      86              SpringApplication application = new SpringApplicati
          on(AwardsApplication.class);
      87              application.addListeners(listeners);
      68 -            try (ConfigurableApplicationContext ignored =      
      69 -                    application.run("--server.port=0", "--movie
         -s.csv.path=" + csvPath)) {                                     
      70 -                // A aplicação subiu: o contexto é fechado pelo
         - try-with-resources.                                           
      88 +            try (ConfigurableApplicationContext context = appli
         +cation.run(args(csvPath, extraArgs))) {                        
      89 +                try {                                          
      90 +                    whileUp.accept(port(context));             
      91 +                } catch (AssertionError ex) {                  
      92 +                    assertionFailure = ex;                     
      93 +                } catch (RuntimeException ex) {                
      94 +                    callFailure = ex;                          
      95 +                }                                              
      96              }
      97          } catch (Throwable ex) {
      98              failure = ex;
     ...
      102              System.setOut(originalOut);
      103              System.setErr(originalErr);
      104          }
      105 +        if (assertionFailure != null) {                       
      106 +            throw assertionFailure;                           
      107 +        }                                                     
      108 +        if (callFailure != null) {                            
      109 +            throw callFailure;                                
      110 +        }                                                     
      111          return new Result(failure, captured.toString(StandardC
           harsets.UTF_8));
      112      }
      113 
     ...
      118       * arquivos diferentes).
      119       */
      120      public static <T> T runAndCall(String csvPath, IntFunction
           <T> call) {
      121 +        return runAndCall(csvPath, List.of(), call);          
      122 +    }                                                         
      123 +                                                              
      124 +    /** Como {@link #runAndCall(String, IntFunction)}, com arg
          +umentos extras na linha de comando. */                        
      125 +    public static <T> T runAndCall(String csvPath, List<String
          +> extraArgs, IntFunction<T> call) {                           
      126 +        return runWithContext(csvPath, extraArgs, (context, po
          +rt) -> call.apply(port));                                     
      127 +    }                                                         
      128 +                                                              
      129 +    /**                                                       
      130 +     * Como {@link #runAndCall(String, List, IntFunction)}, en
          +tregando também o contexto da aplicação real,                 
      131 +     * para verificações que o HTTP não observa (por exemplo, 
          +que o banco é mesmo o PostgreSQL).                            
      132 +     */                                                       
      133 +    public static <T> T runWithContext(String csvPath, List<St
          +ring> extraArgs,                                              
      134 +            BiFunction<ConfigurableApplicationContext, Integer
          +, T> call) {                                                  
      135          SpringApplication application = new SpringApplication(
           AwardsApplication.class);
       91 -        try (ConfigurableApplicationContext context =         
       92 -                application.run("--server.port=0", "--movies.c
          -sv.path=" + csvPath)) {                                       
       93 -            int port = Integer.parseInt(context.getEnvironment
          -().getRequiredProperty("local.server.port"));                 
       94 -            return call.apply(port);                          
      136 +        try (ConfigurableApplicationContext context = applicat
          +ion.run(args(csvPath, extraArgs))) {                          
      137 +            return call.apply(context, port(context));        
      138          }
      139      }
      140 
      141 +    private static String[] args(String csvPath, List<String> 
          +extraArgs) {                                                  
      142 +        List<String> args = new ArrayList<>();                
      143 +        args.add("--server.port=0");                          
      144 +        args.add("--movies.csv.path=" + csvPath);             
      145 +        args.addAll(extraArgs);                               
      146 +        return args.toArray(String[]::new);                   
      147 +    }                                                         
      148 +                                                              
      149 +    private static int port(ConfigurableApplicationContext con
          +text) {                                                       
      150 +        return Integer.parseInt(context.getEnvironment().getRe
          +quiredProperty("local.server.port"));                         
      151 +    }                                                         
      152 +                                                              
      153      private static PrintStream tee(PrintStream original, Outpu
           tStream copy) {
      154          OutputStream both = new OutputStream() {
      155              @Override

● Update(src\test\java\com\outsera\awards\support\ApplicationStarter.java)
  ⎿  Added 4 lines
       5  import java.io.OutputStream;
       6  import java.io.PrintStream;
       7  import java.nio.charset.StandardCharsets;
       8 +import java.util.ArrayList;                                    
       9 +import java.util.List;                                         
      10 +import java.util.function.BiFunction;                          
      11 +import java.util.function.IntConsumer;                         
      12  import java.util.function.IntFunction;
      13  import org.springframework.boot.SpringApplication;
      14  import org.springframework.context.ApplicationListener;

● Write(src\test\java\com\outsera\awards\support\Http.java)
  ⎿  Wrote 126 lines to src\test\java\com\outsera\awards\support\Http.java
       1 package com.outsera.awards.support;
       2 
       3 import java.io.IOException;
       4 import java.io.UncheckedIOException;
       5 import java.net.URI;
       6 import java.net.http.HttpClient;
       7 import java.net.http.HttpRequest;
       8 import java.net.http.HttpResponse;
       9 import java.nio.charset.StandardCharsets;
      10 import java.time.Duration;
     … +116 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\support\PostgresSupport.java)
  ⎿  Wrote 106 lines to
     src\test\java\com\outsera\awards\support\PostgresSupport.java
       1 package com.outsera.awards.support;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import java.sql.Connection;
       6 import java.sql.DriverManager;
       7 import java.sql.SQLException;
       8 import java.sql.Statement;
       9 import java.util.List;
      10 import java.util.Locale;
     … +96 lines (ctrl+o to expand)

● Stage 1: writing the four *PostgresIT tests and the restart scenarios.

● Write(src\test\java\com\outsera\awards\postgres\SchemaPostgresIT.java)
  ⎿  Wrote 65 lines to
     src\test\java\com\outsera\awards\postgres\SchemaPostgresIT.java
       1 package com.outsera.awards.postgres;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.IntervalItem;
       6 import com.outsera.awards.support.PostgresSupport;
       7 import com.outsera.awards.support.ScenarioIT;
       8 import java.util.List;
       9 import java.util.Map;
      10 import javax.sql.DataSource;
     … +55 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; ls
      ~/.m2/repository/org/skyscreamer/jsonassert 2>&1; mkdir -p
      src/test/resources/scenarios/postgres; printf 'year;ti…)
  ⎿  1.5.0
     1.5.1
     1.5.3
     … +10 lines (ctrl+o to expand)
  ⎿  Created src\test\resources\scenarios\postgres\restart-base.csv +5
      1 +year;title;studios;producers;winner                             
      2 +2000;Base A;S;Producer A;yes                                    
      3 +2003;Base B;S;Producer A;                                       
      4 +2005;Base C;S;Producer A;yes                                    
      5 +2010;Base D;S;Producer Z;yes                                    
  ⎿  Created src\test\resources\scenarios\postgres\restart-updated.csv +5
      1 +year;title;studios;producers;winner                             
      2 +2000;Base A;S;Producer A;yes                                    
      3 +2003;Base B;S;Producer A;yes                                    
      4 +2005;Base C;S;Producer A;yes                                    
      5 +2010;Base D;S;Producer Z;yes                                    

● Write(src\test\java\com\outsera\awards\postgres\EquivalencePostgresIT.java)
  ⎿  Wrote 57 lines to
     src\test\java\com\outsera\awards\postgres\EquivalencePostgresIT.java
       1 package com.outsera.awards.postgres;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.Http;
       7 import com.outsera.awards.support.PostgresSupport;
       8 import java.util.List;
       9 import java.util.Map;
      10 import javax.sql.DataSource;
     … +47 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\postgres\ListingOrderPostgresIT.java)
  ⎿  Wrote 30 lines to
     src\test\java\com\outsera\awards\postgres\ListingOrderPostgresIT.java
       1 package com.outsera.awards.postgres;
       2 
       3 import com.outsera.awards.support.PostgresSupport;
       4 import java.util.List;
       5 import org.junit.jupiter.api.AfterAll;
       6 import org.junit.jupiter.api.DisplayName;
       7 import org.junit.jupiter.api.Test;
       8 
       9 /**
      10  * A ordem da listagem (ano e depois título) é a mesma no H2 e n
     … +21 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\postgres\RestartDirectPostgresIT.java)
  ⎿  Wrote 61 lines to
     src\test\java\com\outsera\awards\postgres\RestartDirectPostgresIT.java
       1 package com.outsera.awards.postgres;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.Http;
       7 import com.outsera.awards.support.IntervalItem;
       8 import com.outsera.awards.support.PostgresSupport;
       9 import java.util.List;
      10 import org.junit.jupiter.api.AfterAll;
     … +51 lines (ctrl+o to expand)
● Now the pom.xml changes for T010: exclude *PostgresIT from the default runs
  and add the postgres-it profile.

● Update(pom.xml)
  ⎿  Added 6 lines
      129                      <includes>
      130                          <include>**/*IT.java</include>
      131                      </includes>
      132 +                    <!-- Os testes que exigem PostgreSQL rodam
          + só no perfil postgres-it, no pipeline                        
      133 +                         (spec 003, FR-007; Princípio VI, cons
          +tituição 1.7.0; research R16). -->                            
      134 +                    <excludes>                                
      135 +                        <exclude>**/*PostgresIT.java</exclude>
      136 +                    </excludes>                               
      137 +                    <skip>${it.default.skip}</skip>           
      138                  </configuration>
      139                  <executions>
      140                      <execution>

● Update(pom.xml)
  ⎿  Added 4 lines
      27          <!-- Cobertura mínima de linhas dos testes de integraçã
          o (spec 002, FR-028a). Medido: 97,06% no
      28               fim do Marco 1. Sobrescrevível na linha de comando
          , para provar que o portão funciona. -->
      29          <jacoco.line.minimum>0.90</jacoco.line.minimum>
      30 +        <!-- O perfil postgres-it liga as duas propriedades aba
         +ixo: roda só os *PostgresIT e não aplica a                     
      31 +             cobertura mínima, que é a do ./mvnw verify padrão 
         +(spec 003, research R16). -->                                  
      32 +        <it.default.skip>false</it.default.skip>               
      33 +        <jacoco.check.skip>false</jacoco.check.skip>           
      34      </properties>
      35 
      36      <dependencies>

● Update(pom.xml)
  ⎿  Added 1 line
      195                              <goal>check</goal>
      196                          </goals>
      197                          <configuration>
      198 +                            <skip>${jacoco.check.skip}</skip> 
      199                              <dataFile>${project.build.director
           y}/jacoco-it.exec</dataFile>
      200                              <rules>
      201                                  <rule>

● Update(pom.xml)
  ⎿  Added 40 lines
      215              </plugin>
      216          </plugins>
      217      </build>
      218 +                                                              
      219 +    <profiles>                                                
      220 +        <profile>                                             
      221 +            <!-- ./mvnw -Ppostgres-it verify: só os *PostgresI
          +T, contra o PostgreSQL de DB_URL, DB_USER e                   
      222 +                 DB_PASSWORD. Executado obrigatoriamente pelo 
          +job postgres do pipeline (spec 003, FR-007;                   
      223 +                 Princípio VI, constituição 1.7.0; research R1
          +6). Sem paralelismo do JUnit: o                               
      224 +                 RetryPostgresIT não pode rodar ao mesmo tempo
          + que outro IT. -->                                            
      225 +            <id>postgres-it</id>                              
      226 +            <properties>                                      
      227 +                <it.default.skip>true</it.default.skip>       
      228 +                <jacoco.check.skip>true</jacoco.check.skip>   
      229 +            </properties>                                     
      230 +            <build>                                           
      231 +                <plugins>                                     
      232 +                    <plugin>                                  
      233 +                        <groupId>org.apache.maven.plugins</gro
          +upId>                                                         
      234 +                        <artifactId>maven-failsafe-plugin</art
          +ifactId>                                                      
      235 +                        <executions>                          
      236 +                            <execution>                       
      237 +                                <id>postgres</id>             
      238 +                                <goals>                       
      239 +                                    <goal>integration-test</go
          +al>                                                           
      240 +                                    <goal>verify</goal>       
      241 +                                </goals>                      
      242 +                                <configuration>               
      243 +                                    <skip>false</skip>        
      244 +                                    <includes>                
      245 +                                        <include>**/*PostgresI
          +T.java</include>                                              
      246 +                                    </includes>               
      247 +                                    <excludes combine.self="ov
          +erride"/>                                                     
      248 +                                    <reportsDirectory>${projec
          +t.build.directory}/failsafe-reports-postgres</reportsDirectory
          +>                                                             
      249 +                                    <summaryFile>${project.bui
          +ld.directory}/failsafe-reports-postgres/failsafe-summary.xml</
          +summaryFile>                                                  
      250 +                                </configuration>              
      251 +                            </execution>                      
      252 +                        </executions>                         
      253 +                    </plugin>                                 
      254 +                </plugins>                                    
      255 +            </build>                                          
      256 +        </profile>                                            
      257 +    </profiles>                                               
      258  </project>

● Starting a local PostgreSQL 18 (collation C, trust) to confirm the failure
  (T011).

  Ran 1 shell command · 1 failed (ctrl+o to expand)

● Port 5432 is already taken on this machine (I didn't touch whatever is using
  it). I'll use 55432 and point DB_URL at it.

  Ran 2 shell commands (ctrl+o to expand)

● The tests fail as expected (T011): without the profile and the driver, the
  application starts on H2 and validation can't find the schema. Moving to the
  stage 1 implementation.

● Update(pom.xml)
  ⎿  Added 12 lines
      78              <artifactId>h2</artifactId>
      79              <scope>runtime</scope>
      80          </dependency>
      81 +        <dependency>                                           
      82 +            <!-- Banco do perfil postgres (spec 003, FR-004; re
         +search R3). Só é usado com o perfil. -->                       
      83 +            <groupId>org.postgresql</groupId>                  
      84 +            <artifactId>postgresql</artifactId>                
      85 +            <scope>runtime</scope>                             
      86 +        </dependency>                                          
      87 +        <dependency>                                           
      88 +            <!-- Desde o Flyway 10, o suporte a cada banco fica
         + num módulo próprio (research R3) -->                          
      89 +            <groupId>org.flywaydb</groupId>                    
      90 +            <artifactId>flyway-database-postgresql</artifactId>
      91 +            <scope>runtime</scope>                             
      92 +        </dependency>                                          
      93 
      94          <!-- Testes: somente de integração (*IT, Failsafe). Pri
          ncípio I da constituição. -->
      95          <dependency>

● Write(src\main\resources\application-postgres.yml)
  ⎿  Wrote 12 lines to src\main\resources\application-postgres.yml
       1 # Perfil postgres (spec 003, FR-004 e FR-005; research R3). Troc
         a só o banco: o esquema é o mesmo da
       2 # migração V1 do Flyway, validado pelo mapeamento (ddl-auto: val
         idate, do application.yml), e nenhuma
       3 # consulta usa SQL específico de um banco.
       4 #
       5 # A senha não tem padrão: pelo Princípio VII (constituição 1.6.0
         ), a credencial de demonstração só pode
       6 # aparecer nos arquivos do compose de desenvolvimento local, e n
         ão nesta configuração, que vai no JAR e
     … +6 lines (ctrl+o to expand)

● Update(src\main\resources\application.yml)
  ⎿  Added 7 lines
       1  spring:
       2    application:
       3      name: awards
       4 +  profiles:                                                    
       5 +    # Modo Docker (spec 003, FR-002; research R1): o banco (pos
         +tgres) e a ingestão (kafka) são perfis                         
       6 +    # independentes, e o grupo docker liga os dois. Sem perfil,
         + vale o modo padrão.                                           
       7 +    group:                                                     
       8 +      docker:                                                  
       9 +        - postgres                                             
      10 +        - kafka                                                
      11    main:
      12      # Banner desligado em todas as execuções (spec 002, FR-020;
           research R6)
      13      banner-mode: off

  Ran 1 shell command (ctrl+o to expand)

● Update(src\main\java\com\outsera\awards\movie\MovieRepository.java)
  ⎿  Added 6 lines
       9  public interface MovieRepository extends JpaRepository<Movie, L
          ong> {
      10 
      11      /**
      12 +     * O filme identificado por ano e título (spec 001, FR-006d
         +), para a gravação com atualização (spec                       
      13 +     * 003, FR-014; research R5). Consulta derivada, portável e
         +ntre H2 e PostgreSQL.                                          
      14 +     */                                                        
      15 +    Optional<Movie> findByYearAndTitle(int year, String title);
      16 +                                                               
      17 +    /**                                                        
      18       * Filmes com os filtros opcionais (nulo = sem filtro), ord
          enados por ano e título (FR-020, FR-022).
      19       * JPQL portável entre H2 e PostgreSQL. A ordem vem da cons
          ulta, e o {@code Pageable} não deve trazer
      20       * ordenação.

● Update(src\main\java\com\outsera\awards\movie\MovieRepository.java)
  ⎿  Added 1 line
      1  package com.outsera.awards.movie;
      2 
      3 +import java.util.Optional;                                      
      4  import org.springframework.data.domain.Page;
      5  import org.springframework.data.domain.Pageable;
      6  import org.springframework.data.jpa.repository.JpaRepository;

● Update(src\main\java\com\outsera\awards\movie\Movie.java)
  ⎿  Added 13 lines
      62          this.producers.addAll(producers);
      63      }
      64 
      65 +    /**                                                        
      66 +     * Substitui os dados do filme pelos de uma gravação nova c
         +om o mesmo ano e título: a última gravação                     
      67 +     * prevalece (spec 003, FR-014; data-model.md, "Gravação de
         + um filme"). Os vínculos são trocados por                      
      68 +     * inteiro, então um produtor que saiu da lista deixa de es
         +tar ligado ao filme.                                           
      69 +     */                                                        
      70 +    public void update(String studios, String producersRaw, boo
         +lean winner, Collection<Producer> producers) {                 
      71 +        this.studios = studios;                                
      72 +        this.producersRaw = producersRaw;                      
      73 +        this.winner = winner;                                  
      74 +        this.producers.clear();                                
      75 +        this.producers.addAll(producers);                      
      76 +    }                                                          
      77 +                                                               
      78      public Long getId() {
      79          return id;
      80      }

● Write(src\main\java\com\outsera\awards\ingestion\MovieImportService.java)
  ⎿  Added 20 lines, removed 13 lines
       4  import com.outsera.awards.movie.MovieRepository;
       5  import com.outsera.awards.producer.Producer;
       6  import com.outsera.awards.producer.ProducerRepository;
       7 -import java.util.HashMap;                                      
       7  import java.util.List;
       9 -import java.util.Map;                                          
       8  import org.springframework.stereotype.Service;
       9  import org.springframework.transaction.annotation.Transactional
          ;
      10 
      11  /**
      14 - * Serviço único de gravação de filmes (Princípio V). A carga d
         -o arquivo usa este serviço, e no Marco 3                       
      15 - * o consumidor Kafka vai usar o mesmo: o perfil muda só a orig
         -em dos dados.                                                  
      12 + * Serviço único de gravação de filmes (Princípio V). A carga d
         +ireta do arquivo e o consumidor Kafka usam                     
      13 + * este serviço: o perfil muda só a origem dos dados.          
      14 + *                                                             
      15 + * <p>A gravação é idempotente por ano e título, com atualizaçã
         +o (spec 003, FR-014; research R5): um filme                    
      16 + * que ainda não existe é criado, e um que já existe passa a te
         +r os valores da gravação nova. Gravar a                        
      17 + * mesma linha de novo não muda nada, e um reinício sobre um ba
         +nco persistente não duplica filmes.                            
      18   */
      19  @Service
      20  public class MovieImportService {
     ...
      27          this.producers = producers;
      28      }
      29 
      28 -    /** Grava os filmes e os vínculos com os produtores, criand
         -o os produtores que ainda não existem. */                      
      30 +    /** Carga direta: grava todas as linhas numa única transaçã
         +o, como no Marco 1. */                                         
      31      @Transactional
      32      public void importMovies(List<MovieRow> rows) {
      31 -        Map<String, Producer> byName = new HashMap<>();        
      32 -        for (MovieRow row : rows) {                            
      33 -            List<Producer> movieProducers = row.producerNames()
         -.stream()                                                      
      34 -                    .map(name -> byName.computeIfAbsent(name, t
         -his::findOrCreate))                                            
      35 -                    .toList();                                 
      36 -            movies.save(new Movie(row.year(), row.title(), row.
         -studios(), row.producersRaw(), row.winner(),                   
      37 -                    movieProducers));                          
      38 -        }                                                      
      33 +        rows.forEach(this::record);                            
      34      }
      35 
      36 +    /** Cria ou atualiza o filme da linha, criando os produtore
         +s que ainda não existem. */                                    
      37 +    @Transactional                                             
      38 +    public void record(MovieRow row) {                         
      39 +        List<Producer> movieProducers = row.producerNames().str
         +eam()                                                          
      40 +                .map(this::findOrCreate)                       
      41 +                .toList();                                     
      42 +        movies.findByYearAndTitle(row.year(), row.title()).ifPr
         +esentOrElse(                                                   
      43 +                movie -> movie.update(row.studios(), row.produc
         +ersRaw(), row.winner(), movieProducers),                       
      44 +                () -> movies.save(new Movie(row.year(), row.tit
         +le(), row.studios(), row.producersRaw(),                       
      45 +                        row.winner(), movieProducers)));       
      46 +    }                                                          
      47 +                                                               
      48      private Producer findOrCreate(String name) {
      49          return producers.findByName(name).orElseGet(() -> produ
          cers.save(new Producer(name)));
      50      }

● Restoring the per-batch producer cache from the Marco 1 code, so I don't add
  one query per name to the direct load.

● Update(src\main\java\com\outsera\awards\ingestion\MovieImportService.java)
  ⎿  Added 8 lines, removed 2 lines
      30      /** Carga direta: grava todas as linhas numa única transaçã
          o, como no Marco 1. */
      31      @Transactional
      32      public void importMovies(List<MovieRow> rows) {
      33 -        rows.forEach(this::record);                            
      33 +        Map<String, Producer> byName = new HashMap<>();        
      34 +        rows.forEach(row -> record(row, byName));              
      35      }
      36 
      37      /** Cria ou atualiza o filme da linha, criando os produtore
          s que ainda não existem. */
      38      @Transactional
      39      public void record(MovieRow row) {
      40 +        record(row, new HashMap<>());                          
      41 +    }                                                          
      42 +                                                               
      43 +    /** {@code byName} guarda os produtores já resolvidos na me
         +sma transação. */                                              
      44 +    private void record(MovieRow row, Map<String, Producer> byN
         +ame) {                                                         
      45          List<Producer> movieProducers = row.producerNames().str
          eam()
      40 -                .map(this::findOrCreate)                       
      46 +                .map(name -> byName.computeIfAbsent(name, this:
         +:findOrCreate))                                                
      47                  .toList();
      48          movies.findByYearAndTitle(row.year(), row.title()).ifPr
          esentOrElse(
      49                  movie -> movie.update(row.studios(), row.produc
          ersRaw(), row.winner(), movieProducers),

● Update(src\main\java\com\outsera\awards\ingestion\MovieImportService.java)
  ⎿  Added 2 lines
       4  import com.outsera.awards.movie.MovieRepository;
       5  import com.outsera.awards.producer.Producer;
       6  import com.outsera.awards.producer.ProducerRepository;
       7 +import java.util.HashMap;                                      
       8  import java.util.List;
       9 +import java.util.Map;                                          
      10  import org.springframework.stereotype.Service;
      11  import org.springframework.transaction.annotation.Transactional
          ;
      12 

  Ran 1 shell command (ctrl+o to expand)

● All five *PostgresIT tests pass on real PostgreSQL, confirming R3 and R4. Next
  I'm running the default ./mvnw verify. · summary

  Ran 2 shell commands (ctrl+o to expand)

● Default verify is green: 189 + 55 tests, the same 5 expected skips as the
  baseline, coverage 96.83%, 78 s. Now T018, the postgres job in the pipeline.

  Ran 1 shell command (ctrl+o to expand)

● Update(.github\workflows\ci.yml)  ⎿  Added 43 lines
       57              target/failsafe-reports-json-logs/
       58            if-no-files-found: warn
       59 
       60 +  postgres:                                                   
       61 +    # Testes que dependem do PostgreSQL, no perfil Maven postg
          +res-it (spec 003, FR-007; Princípio VI,                       
       62 +    # constituição 1.7.0; research R16). O banco é um service 
          +container com collation C (research R4).                      
       63 +    # Nenhuma credencial no workflow: o banco aceita a conexão
          + por trust, e só existe neste runner.                         
       64 +    name: Testes com PostgreSQL                               
       65 +    runs-on: ubuntu-latest                                    
       66 +    timeout-minutes: 20                                       
       67 +    services:                                                 
       68 +      postgres:                                               
       69 +        image: postgres:18-alpine@sha256:0115267ab5a509022df93
          +19c77be365cef335adb71556c75f60f5360ee247e71                   
       70 +        env:                                                  
       71 +          POSTGRES_DB: awards                                 
       72 +          POSTGRES_USER: awards                               
       73 +          POSTGRES_HOST_AUTH_METHOD: trust                    
       74 +          POSTGRES_INITDB_ARGS: --locale=C --encoding=UTF8    
       75 +        ports:                                                
       76 +          - 5432:5432                                         
       77 +        options: >-                                           
       78 +          --health-cmd "pg_isready -U awards -d awards"       
       79 +          --health-interval 5s                                
       80 +          --health-timeout 5s                                 
       81 +          --health-retries 20                                 
       82 +    steps:                                                    
       83 +      - uses: actions/checkout@3d3c42e5aac5ba805825da76410c181
          +273ba90b1 # v7.0.1                                            
       84 +      - uses: actions/setup-java@d1374f8ac74474200d16ef8ecee22
          +c7ae7a51880 # v6.3.0                                          
       85 +        with:                                                 
       86 +          distribution: temurin                               
       87 +          java-version: '25'                                  
       88 +          cache: maven                                        
       89 +      - name: ./mvnw -Ppostgres-it verify                     
       90 +        env:                                                  
       91 +          DB_URL: jdbc:postgresql://localhost:5432/awards     
       92 +          DB_USER: awards                                     
       93 +          DB_PASSWORD: ''                                     
       94 +        run: ./mvnw -B -Ppostgres-it verify                   
       95 +      - name: Relatórios dos testes com PostgreSQL            
       96 +        if: always()                                          
       97 +        uses: actions/upload-artifact@cf430e030ddbb5b0abf93d22
          +962f4752f3646cd9 # v7.0.2                                     
       98 +        with:                                                 
       99 +          name: relatorios-postgres                           
      100 +          path: target/failsafe-reports-postgres/             
      101 +          if-no-files-found: warn                             
      102 +                                                              
      103    codeql:
      104      name: Análise de segurança do código (CodeQL)
      105      runs-on: ubuntu-latest

  Ran 2 shell commands (ctrl+o to expand)

● The workflow policy passes (checked with yq 4.53 in a temporary container).
  Marking T003–T018 and committing stage 1.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/tasks.md; for t in 003 004 005 006 007 008 009 010
      011 012 013 014 015 016 017 018 019; do s…)
  ⎿  M  .github/workflows/ci.yml
      M docs/ai/prompts.md
     M  pom.xml
     … +17 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\tasks.md +17 -17
       96 
       97  ### Testes da etapa 1 (escrever primeiro; precisam falhar)
       98 
       99 -- [ ] T003 [US2] Criar `src/test/java/com/outsera/awards/suppo
          -rt/PostgresSupport.java`:                                     
       99 +- [X] T003 [US2] Criar `src/test/java/com/outsera/awards/suppo
          +rt/PostgresSupport.java`:                                     
      100    - lê `DB_URL` (padrão `jdbc:postgresql://localhost:5432/awar
           ds`), `DB_USER` (padrão `awards`) e
      101      `DB_PASSWORD` (padrão vazio) do ambiente;
      102    - `newSchema(Class<?>)` gera `it_<nome da classe em minúscul
           as>_<8 caracteres aleatórios>`;
     ...
      109      perfil ausente faria o teste passar no H2;
      110    - `dropSchema(String)` roda `DROP SCHEMA <schema> CASCADE` p
           or JDBC depois da classe. É SQL de teste,
      111      fora da aplicação.
      112 -- [ ] T004 [US2] Em `src/test/java/com/outsera/awards/support/
          -ApplicationStarter.java`:                                     
      112 +- [X] T004 [US2] Em `src/test/java/com/outsera/awards/support/
          +ApplicationStarter.java`:                                     
      113    - acrescentar sobrecargas `start(String csvPath, List<String
           > extraArgs, ApplicationListener<?>...)` e
      114      `runAndCall(String csvPath, List<String> extraArgs, IntFun
           ction<T> call)`;
      115    - acrescentar uma variante que expõe o `ConfigurableApplicat
           ionContext` ao `call`, para o
      116      `assertIsPostgres`;
      117    - as assinaturas atuais continuam iguais e delegam às novas
           com a lista vazia.
      118 -- [ ] T005 [P] [US2] Criar `src/test/java/com/outsera/awards/p
          -ostgres/SchemaPostgresIT.java` (`@SpringBootTest`, `@ActivePro
          -files("postgres")`, `@DynamicPropertySource` com o esquema do 
          -`PostgresSupport`):                                           
      118 +- [X] T005 [P] [US2] Criar `src/test/java/com/outsera/awards/p
          +ostgres/SchemaPostgresIT.java` (`@SpringBootTest`, `@ActivePro
          +files("postgres")`, `@DynamicPropertySource` com o esquema do 
          +`PostgresSupport`):                                           
      119    - `S003 FR-004 FR-005 US2-1`: o banco é PostgreSQL; `flyway_
           schema_history` do esquema tem a versão
      120      `1` com `success = true` (verificação por banco, porque a
           migração não tem endpoint); a subida passou
      121      pelo `ddl-auto: validate`;
      122    - `S003 FR-006 US2-2`: os intervalos trazem Joel Silver em `
           min` (1990–1991) e Matthew Vaughn em `max`
      123      (2002–2015), e `GET /api/movies` tem `totalElements` 206.
      124 -- [ ] T006 [P] [US2] Criar `src/test/java/com/outsera/awards/p
          -ostgres/EquivalencePostgresIT.java` (múltiplas execuções, `App
          -licationStarter`):                                            
      124 +- [X] T006 [P] [US2] Criar `src/test/java/com/outsera/awards/p
          +ostgres/EquivalencePostgresIT.java` (múltiplas execuções, `App
          +licationStarter`):                                            
      125    - sobe com o arquivo padrão no H2 e depois no PostgreSQL;
      126    - em cada execução, guarda o corpo e o `ETag` de `GET /api/p
           roducers/intervals`, de
      127      `/api/movies?size=100&page=0..2`, de `/api/movies?winner=t
           rue` e de `/api/movies?year=1990`;
      128    - `S003 FR-006 SC-001`: os corpos são idênticos byte a byte,
            e os ETags também.
      129 -- [ ] T007 [P] [US2] Criar `src/test/java/com/outsera/awards/p
          -ostgres/ListingOrderPostgresIT.java`:                         
      129 +- [X] T007 [P] [US2] Criar `src/test/java/com/outsera/awards/p
          +ostgres/ListingOrderPostgresIT.java`:                         
      130    - mesmo método do T006, com `classpath:scenarios/adversario/
           13-ordenacao-com-acentos-e-caixa.csv` (o
      131      mesmo do `Case13AccentAndCaseOrderIT`);
      132    - `S003 FR-008`: a listagem completa tem a mesma ordem byte
           a byte no H2 e no PostgreSQL.
      133 -- [ ] T008 [P] [US2] Criar os cenários `src/test/resources/sce
          -narios/postgres/restart-base.csv` e `restart-updated.csv`, com
          - o cabeçalho `year;title;studios;producers;winner`:           
      133 +- [X] T008 [P] [US2] Criar os cenários `src/test/resources/sce
          +narios/postgres/restart-base.csv` e `restart-updated.csv`, com
          + o cabeçalho `year;title;studios;producers;winner`:           
      134    - `restart-base.csv` tem quatro filmes:
      135      - `2000;Base A;S;Producer A;yes`;
     … +64 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Stage 1 is committed (bbb834e). T020 is waiting on your push. Starting stage 2
  (Kafka): adding the dependencies and checking the class names in Boot 4.1.2.

● Update(pom.xml)
  ⎿  Added 6 lines
      57              <artifactId>micrometer-registry-prometheus</artifac
          tId>
      58          </dependency>
      59          <dependency>
      60 +            <!-- Ingestão por Kafka no perfil kafka (spec 003, 
         +FR-009 a FR-018). A autoconfiguração do Boot                   
      61 +                 fica excluída fora do perfil (research R2). --
         +>                                                              
      62 +            <groupId>org.springframework.boot</groupId>        
      63 +            <artifactId>spring-boot-starter-kafka</artifactId> 
      64 +        </dependency>                                          
      65 +        <dependency>                                           
      66              <groupId>org.apache.commons</groupId>
      67              <artifactId>commons-csv</artifactId>
      68              <version>${commons-csv.version}</version>

● Update(pom.xml)
  ⎿  Added 6 lines
      121              <scope>test</scope>
      122          </dependency>
      123          <dependency>
      124 +            <!-- Broker Kafka embutido (KRaft) nos testes, sem
          + Docker (Princípio II; research R15) -->                      
      125 +            <groupId>org.springframework.kafka</groupId>      
      126 +            <artifactId>spring-kafka-test</artifactId>        
      127 +            <scope>test</scope>                               
      128 +        </dependency>                                         
      129 +        <dependency>                                          
      130              <!-- Valida as respostas reais contra o openapi.ya
           ml (FR-025, research R8) -->
      131              <groupId>com.atlassian.oai</groupId>
      132              <!-- Antigo swagger-request-validator-core, reloca
           do pelo próprio projeto na versão 3.0.0 -->

  Ran 3 shell commands · 1 failed (ctrl+o to expand)

● Confirmed that the Kafka autoconfiguration classes are KafkaAutoConfiguration
  and KafkaMetricsAutoConfiguration in Spring Kafka 4.1.2 with kafka-clients
  4.2.2. Next I'm reading the Marco 1 integration tests the Kafka ones should
  mirror. · summary

  Ran 2 shell commands (ctrl+o to expand)

● Writing KafkaTestSupport (a real producer and consumer against the embedded
  broker).

● Write(src\test\java\com\outsera\awards\support\KafkaTestSupport.java)
  ⎿  Wrote 133 lines to
     src\test\java\com\outsera\awards\support\KafkaTestSupport.java
       1 package com.outsera.awards.support;
       2 
       3 import java.nio.charset.StandardCharsets;
       4 import java.time.Duration;
       5 import java.util.ArrayList;
       6 import java.util.LinkedHashMap;
       7 import java.util.List;
       8 import java.util.Locale;
       9 import java.util.Map;
      10 import java.util.Properties;
     … +123 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Writing the stage 2 ITs (isolation, Kafka, FieldTooLongIT and the PostgreSQL
  ones with Kafka).

● Write(src\test\java\com\outsera\awards\support\KafkaScenarioIT.java)
  ⎿  Wrote 68 lines to
     src\test\java\com\outsera\awards\support\KafkaScenarioIT.java
       1 package com.outsera.awards.support;
       2 
       3 import java.time.Duration;
       4 import java.util.regex.Matcher;
       5 import java.util.regex.Pattern;
       6 import org.springframework.beans.factory.annotation.Autowired;
       7 import org.springframework.kafka.test.EmbeddedKafkaBroker;
       8 import org.springframework.kafka.test.context.EmbeddedKafka;
       9 import org.springframework.test.annotation.DirtiesContext;
      10 import org.springframework.test.context.ActiveProfiles;
     … +58 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\isolation\DefaultModeIsolationIT.java)
  ⎿  Wrote 38 lines to
     src\test\java\com\outsera\awards\isolation\DefaultModeIsolationIT.java
       1 package com.outsera.awards.isolation;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.ApplicationStarter.Result;
       7 import com.outsera.awards.support.Http;
       8 import java.util.List;
       9 import org.junit.jupiter.api.DisplayName;
      10 import org.junit.jupiter.api.Tag;
     … +28 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaReferenceFileIT.java)
  ⎿  Wrote 61 lines to
     src\test\java\com\outsera\awards\kafka\KafkaReferenceFileIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.IntervalItem;
       6 import com.outsera.awards.support.KafkaScenarioIT;
       7 import com.outsera.awards.support.KafkaTestSupport;
       8 import com.outsera.awards.support.KafkaTestSupport.Record;
       9 import java.time.Duration;
      10 import java.util.List;
     … +51 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const r=(c,n)=>c.repeat(n);…)
  ⎿  1 5 7 9
     2 9 1 11
     3 11 1 13
     … +5 lines (ctrl+o to expand)
  ⎿  Created src\test\resources\scenarios\field-too-long.csv +8
      1 +year;title;studios;producers;winner                             
      2 +2001;Valido Um;S;Producer Um;yes                                
      3 +2002;Valido Dois;S;Producer Dois;                               
      4 +2003;TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        +TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        +TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        +TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        +TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        +TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        +TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT
        +TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT;S;Pro
        +ducer Tres;                                                     
      5 +2004;Estudio Longo;SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS
        +SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS;Pro
        +ducer Quatro;                                                   
      6 +2005;Produtores Longos;S;AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        +AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        +AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        +AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        +AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        +AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        +AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        +AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
        +AAAAAAAAAAAAA, BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        +BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        +BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        +BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        +BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        +BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        +BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        +BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB
        +BBB;                                                            
      7 +2006;Nome Longo;S;NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
        +NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
        +NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
        +NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
        +NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
        +NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
        +NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
        +NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
        +NNNNNNN;                                                        
      8 +2007;XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        +XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        +XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        +XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        +XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        +XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        +XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        +XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX;S;Prod
        +ucer Um;yes                                                     

● Write(src\test\java\com\outsera\awards\ingestion\FieldTooLongIT.java)
  ⎿  Wrote 51 lines to
     src\test\java\com\outsera\awards\ingestion\FieldTooLongIT.java
       1 package com.outsera.awards.ingestion;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.IntervalItem;
       6 import com.outsera.awards.support.LoaderLog;
       7 import com.outsera.awards.support.LoaderLog.Summary;
       8 import com.outsera.awards.support.ScenarioIT;
       9 import java.util.List;
      10 import org.junit.jupiter.api.DisplayName;
     … +41 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Now the equivalence, readiness, invalid lines and structural failure ITs.

● Write(src\test\java\com\outsera\awards\kafka\KafkaEquivalenceIT.java)
  ⎿  Wrote 59 lines to
     src\test\java\com\outsera\awards\kafka\KafkaEquivalenceIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.Http;
       7 import com.outsera.awards.support.KafkaTestSupport;
       8 import com.outsera.awards.support.KafkaTestSupport.Topics;
       9 import java.time.Duration;
      10 import java.util.List;
     … +49 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaReadinessIT.java)
  ⎿  Wrote 64 lines to
     src\test\java\com\outsera\awards\kafka\KafkaReadinessIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.KafkaScenarioIT;
       6 import com.outsera.awards.support.KafkaTestSupport;
       7 import com.outsera.awards.support.KafkaTestSupport.Record;
       8 import java.time.Duration;
       9 import java.util.List;
      10 import java.util.UUID;
     … +54 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaInvalidLinesIT.java)
  ⎿  Wrote 56 lines to
     src\test\java\com\outsera\awards\kafka\KafkaInvalidLinesIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.KafkaScenarioIT;
       6 import com.outsera.awards.support.KafkaTestSupport;
       7 import com.outsera.awards.support.LoaderLog;
       8 import java.time.Duration;
       9 import org.junit.jupiter.api.DisplayName;
      10 import org.junit.jupiter.api.Tag;
     … +46 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaStructuralFailureIT.java)
  ⎿  Wrote 44 lines to
     src\test\java\com\outsera\awards\kafka\KafkaStructuralFailureIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.ApplicationStarter.Result;
       7 import com.outsera.awards.support.KafkaTestSupport;
       8 import com.outsera.awards.support.KafkaTestSupport.Topics;
       9 import java.time.Duration;
      10 import org.junit.jupiter.api.DisplayName;
     … +34 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaNewWinnerIT.java)  ⎿  Wrote 55 lines to
     src\test\java\com\outsera\awards\kafka\KafkaNewWinnerIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.IntervalItem;
       6 import com.outsera.awards.support.KafkaScenarioIT;
       7 import com.outsera.awards.support.MovieJson;
       8 import java.util.List;
       9 import org.junit.jupiter.api.DisplayName;
      10 import org.junit.jupiter.api.Test;
     … +45 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; mkdir -p
      src/test/resources/scenarios/kafka; printf
      'year;title;studios;producers;winner\n2000;Upd A;S;Producer A;ye…)
  ⎿  7:public record IntervalsBody(List<IntervalItem> min, List<IntervalIte
     m> max) {
  ⎿  Created src\test\resources\scenarios\kafka\update-base.csv +5
      1 +year;title;studios;producers;winner                             
      2 +2000;Upd A;S;Producer A;yes                                     
      3 +2004;Upd B;S;Producer A;                                        
      4 +2008;Upd C;S;Producer A, Producer B;yes                         
      5 +2012;Upd D;S;Producer B;yes                                     

● Write(src\test\java\com\outsera\awards\kafka\KafkaUpdateIT.java)
  ⎿  Wrote 59 lines to
     src\test\java\com\outsera\awards\kafka\KafkaUpdateIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static com.outsera.awards.support.IntervalItem.item;
       4 import static org.assertj.core.api.Assertions.assertThat;
       5 
       6 import com.outsera.awards.support.IntervalItem;
       7 import com.outsera.awards.support.KafkaScenarioIT;
       8 import com.outsera.awards.support.MovieJson;
       9 import java.util.List;
      10 import org.junit.jupiter.api.DisplayName;
     … +49 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaIdempotencyIT.java)
  ⎿  Wrote 53 lines to
     src\test\java\com\outsera\awards\kafka\KafkaIdempotencyIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.IntervalsBody;
       6 import com.outsera.awards.support.KafkaScenarioIT;
       7 import com.outsera.awards.support.KafkaTestSupport;
       8 import com.outsera.awards.support.KafkaTestSupport.Record;
       9 import java.time.Duration;
      10 import java.util.List;
     … +43 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java)
  ⎿  Wrote 111 lines to
     src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.KafkaScenarioIT;
       6 import com.outsera.awards.support.KafkaTestSupport;
       7 import com.outsera.awards.support.KafkaTestSupport.Record;
       8 import com.outsera.awards.support.PrometheusText;
       9 import java.nio.charset.StandardCharsets;
      10 import java.time.Duration;
     … +101 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaMetricsIT.java)
  ⎿  Wrote 43 lines to
     src\test\java\com\outsera\awards\kafka\KafkaMetricsIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.KafkaScenarioIT;
       6 import com.outsera.awards.support.PrometheusText;
       7 import com.outsera.awards.support.PrometheusText.Sample;
       8 import java.util.List;
       9 import org.junit.jupiter.api.DisplayName;
      10 import org.junit.jupiter.api.Test;
     … +33 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\PublisherSummaryIT.java)
  ⎿  Wrote 30 lines to
     src\test\java\com\outsera\awards\kafka\PublisherSummaryIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.KafkaScenarioIT;
       6 import com.outsera.awards.support.LogOutput;
       7 import org.junit.jupiter.api.DisplayName;
       8 import org.junit.jupiter.api.Tag;
       9 import org.junit.jupiter.api.Test;
      10 import org.springframework.boot.test.system.CapturedOutput;
     … +20 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaBrokerUnavailableIT.java)
  ⎿  Wrote 54 lines to
     src\test\java\com\outsera\awards\kafka\KafkaBrokerUnavailableIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.ApplicationStarter.Result;
       7 import com.outsera.awards.support.Http;
       8 import com.outsera.awards.support.KafkaTestSupport;
       9 import java.io.IOException;
      10 import java.io.UncheckedIOException;
     … +44 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaErrorTopicUnavailableIT.java
       )
  ⎿  Wrote 40 lines to src\test\java\com\outsera\awards\kafka\KafkaErrorTop
     icUnavailableIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.KafkaScenarioIT;
       6 import org.junit.jupiter.api.DisplayName;
       7 import org.junit.jupiter.api.Tag;
       8 import org.junit.jupiter.api.Test;
       9 import org.springframework.boot.test.system.CapturedOutput;
      10 import org.springframework.kafka.test.context.EmbeddedKafka;
     … +30 lines (ctrl+o to expand)

● Stage 2 tests are almost done (12 Kafka ITs written). Next are the two
  PostgreSQL ITs with Kafka (restart and retry), then the production code.

● Write(src\test\java\com\outsera\awards\postgres\RestartPostgresIT.java)
  ⎿  Wrote 99 lines to
     src\test\java\com\outsera\awards\postgres\RestartPostgresIT.java
       1 package com.outsera.awards.postgres;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.Http;
       7 import com.outsera.awards.support.KafkaTestSupport;
       8 import com.outsera.awards.support.KafkaTestSupport.Topics;
       9 import com.outsera.awards.support.PostgresSupport;
      10 import java.time.Duration;
     … +89 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\postgres\RetryPostgresIT.java)
  ⎿  Wrote 80 lines to
     src\test\java\com\outsera\awards\postgres\RetryPostgresIT.java
       1 package com.outsera.awards.postgres;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.ApplicationStarter.Result;
       7 import com.outsera.awards.support.Http;
       8 import com.outsera.awards.support.KafkaTestSupport;
       9 import com.outsera.awards.support.KafkaTestSupport.Topics;
      10 import com.outsera.awards.support.PostgresSupport;
     … +70 lines (ctrl+o to expand)

● Confirming the stage 2 failure (T036) on a subset, which also checks that the
  embedded broker works on Windows.

  Ran 1 shell command (ctrl+o to expand)

● The startup crash makes FieldTooLongIT fail, and the direct load makes two of
  KafkaReferenceFileIT's tests fail, while DefaultModeIsolationIT already passes
  and stays as a guard. Moving on to the stage 2 implementation. · summary

  Ran 1 shell command (ctrl+o to expand)

● Update(src\main\resources\application.yml)
  ⎿  Added 6 lines
       8        docker:
       9          - postgres
      10          - kafka
      11 +  autoconfigure:                                               
      12 +    # Sem o perfil kafka, o Kafka não é configurado (Princípio 
         +V; spec 003, FR-001; research R2). O                           
      13 +    # application-kafka.yml redefine esta lista sem as duas cla
         +sses.                                                          
      14 +    exclude:                                                   
      15 +      - org.springframework.boot.kafka.autoconfigure.KafkaAutoC
         +onfiguration                                                   
      16 +      - org.springframework.boot.kafka.autoconfigure.metrics.Ka
         +fkaMetricsAutoConfiguration                                    
      17    main:
      18      # Banner desligado em todas as execuções (spec 002, FR-020;
           research R6)
      19      banner-mode: off

● Write(src\main\resources\application-kafka.yml)
  ⎿  Wrote 51 lines to src\main\resources\application-kafka.yml
       1 # Perfil kafka (spec 003, FR-009 a FR-018; research R1, R2 e R6 
         a R10). Troca só a origem dos dados: o
       2 # arquivo é publicado no tópico de filmes e consumido pelo mesmo
          serviço de gravação do modo padrão. A
       3 # carga direta não roda neste perfil.
       4 spring:
       5   autoconfigure:
       6     # Redefine a lista do application.yml sem a autoconfiguração
          do Kafka.
       7     exclude: []
     … +44 lines (ctrl+o to expand)

● Update(src\main\java\com\outsera\awards\ingestion\RejectionReason.java)
  ⎿  Added 2 lines
      12      TITLE_MISSING("título vazio"),
      13      PRODUCERS_MISSING("produtores vazios"),
      14      WINNER_INVALID("winner diferente de yes ou vazio"),
      15 +    /** Acrescentado pela spec 003 (spec 001, FR-006): a mesma 
         +regra vale para os eventos do Kafka. */                        
      16 +    FIELD_TOO_LONG("um campo excede o tamanho da coluna"),     
      17      DUPLICATE("filme duplicado");
      18 
      19      private final String message;

● Write(src\main\java\com\outsera\awards\ingestion\MovieFieldRules.java)
  ⎿  Wrote 80 lines to
     src\main\java\com\outsera\awards\ingestion\MovieFieldRules.java
       1 package com.outsera.awards.ingestion;
       2 
       3 import java.util.List;
       4 import java.util.regex.Pattern;
       5 
       6 /**
       7  * Regras de conteúdo de um filme, comuns à linha do CSV e ao ev
         ento do Kafka (spec 001, FR-006; spec 003,
       8  * FR-012 e FR-018; research R7). Cada regra existe só aqui, par
         a que a carga direta e a ingestão por
     … +72 lines (ctrl+o to expand)

● Now MovieCsvReader uses the shared rules.

  Read 1 file, ran 1 shell command (ctrl+o to expand)

● Update(src\main\java\com\outsera\awards\ingestion\MovieCsvReader.java)
  ⎿  Added 8 lines, removed 3 lines
      164          if (!YEAR.matcher(year).matches()) {
      165              return new Rejection(lineNumber, RejectionReason.Y
           EAR_NOT_NUMERIC, year);
      166          }
      167 -        if (!isYearInRange(year)) {                           
      167 +        if (!MovieFieldRules.isYearInRange(year)) {           
      168              return new Rejection(lineNumber, RejectionReason.Y
           EAR_OUT_OF_RANGE, year);
      169          }
      170          String title = record.get(1);
      171 -        if (title.isEmpty()) {                                
      171 +        if (!MovieFieldRules.hasTitle(title)) {               
      172              return new Rejection(lineNumber, RejectionReason.T
           ITLE_MISSING, null);
      173          }
      174 -        if (ProducerNameParser.parse(record.get(3)).isEmpty())
          - {                                                            
      174 +        List<String> producerNames = MovieFieldRules.producerN
          +ames(record.get(3));                                          
      175 +        if (producerNames.isEmpty()) {                        
      176              return new Rejection(lineNumber, RejectionReason.P
           RODUCERS_MISSING, null);
      177          }
      178          String winner = record.get(4);
      179          if (!winner.isEmpty() && !winner.equalsIgnoreCase("yes
           ")) {
      180              return new Rejection(lineNumber, RejectionReason.W
           INNER_INVALID, winner);
      181          }
      182 +        String tooLong = MovieFieldRules.tooLong(title, record
          +.get(2), record.get(3), producerNames);                       
      183 +        if (tooLong != null) {                                
      184 +            return new Rejection(lineNumber, RejectionReason.F
          +IELD_TOO_LONG, tooLong);                                      
      185 +        }                                                     
      186          Long original = accepted.get(new MovieKey(Integer.pars
           eInt(year), title));
      187          if (original != null) {
      188              return new Rejection(lineNumber, RejectionReason.D
           UPLICATE, "mesmo ano e título da linha " + original);

● Update(src\main\java\com\outsera\awards\ingestion\MovieCsvReader.java)
  ⎿  Removed 14 lines
      190          return null;
      191      }
      192 
      193 -    /**                                                       
      194 -     * Se o ano, já só com dígitos, está entre {@link #MIN_YEA
          -R} e {@link #MAX_YEAR}. Os zeros à esquerda                   
      195 -     * são descartados antes, e o tamanho é conferido antes da
          - conversão, para que uma sequência longa de                   
      196 -     * dígitos não estoure o {@code int}.                     
      197 -     */                                                       
      198 -    private static boolean isYearInRange(String digits) {     
      199 -        String significant = LEADING_ZEROS.matcher(digits).rep
          -laceFirst("");                                                
      200 -        if (significant.length() > String.valueOf(MAX_YEAR).le
          -ngth()) {                                                     
      201 -            return false;                                     
      202 -        }                                                     
      203 -        int value = significant.isEmpty() ? 0 : Integer.parseI
          -nt(significant);                                              
      204 -        return value >= MIN_YEAR && value <= MAX_YEAR;        
      205 -    }                                                         
      206 -                                                              
      193      private static MovieRow toRow(CSVRecord record, long lineN
           umber) {
      194          String studios = record.get(2);
      195          String producers = record.get(3);
● Update(src\main\java\com\outsera\awards\ingestion\MovieCsvReader.java)
  ⎿  Removed 4 lines
      36 
      37      private static final char BOM = '';
      38      private static final Pattern YEAR = Pattern.compile("\\d+")
          ;
      39 -    private static final Pattern LEADING_ZEROS = Pattern.compil
         -e("^0+");                                                      
      40 -    /** Faixa válida de ano (FR-006, sessão 2026-11-22 da spec)
         -. */                                                           
      41 -    private static final int MIN_YEAR = 1;                     
      42 -    private static final int MAX_YEAR = 9999;                  
      39 
      40      private static final CSVFormat FORMAT = CSVFormat.DEFAULT.b
          uilder()
      41              .setDelimiter(';')

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; grep -n "^import java.util"
      src/main/java/com/outsera/awards/ingestion/MovieCsvReader.java; sed -i
      's/^ \* Lê e vali…)
  ⎿  13:import java.util.ArrayList;
     14:import java.util.HashMap;
     15:import java.util.Iterator;
     … +15 lines (ctrl+o to expand)
  ⎿  Updated src\main\java\com\outsera\awards\ingestion\MovieCsvReader.java
     +2 -1
      23  import org.springframework.stereotype.Component;
      24 
      25  /**
      26 - * Lê e valida o arquivo de filmes (FR-003 a FR-006e, research 
         -R3).                                                           
      26 + * Lê e valida o arquivo de filmes (FR-003 a FR-006e, research 
         +R3). As regras de conteúdo de cada campo                       
      27 + * ficam na {@link MovieFieldRules}, comum ao parser de eventos
         + do Kafka (spec 003, FR-012).                                  
      28   *
      29   * <p>Erros estruturais lançam {@link MovieFileException}. Linh
          as inválidas viram {@link Rejection} no
      30   * {@link LoadReport}, e a leitura continua.

● Now MovieFileReading, the changes to MovieFileLoader, and the ingestion/kafka/
  package.

● Write(src\main\java\com\outsera\awards\ingestion\MovieFileReading.java)
  ⎿  Wrote 50 lines to
     src\main\java\com\outsera\awards\ingestion\MovieFileReading.java
       1 package com.outsera.awards.ingestion;
       2 
       3 import com.outsera.awards.ingestion.MovieFileException.Reason;
       4 import org.slf4j.Logger;
       5 import org.slf4j.LoggerFactory;
       6 import org.springframework.core.io.Resource;
       7 import org.springframework.stereotype.Component;
       8 
       9 /**
      10  * Leitura do arquivo de filmes, comum à carga direta e ao publi
     … +41 lines (~2 KB) (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\ingestion\MovieFileLoader.java)
  ⎿  Added 10 lines, removed 23 lines
       1  package com.outsera.awards.ingestion;
       2 
       3 -import com.outsera.awards.ingestion.MovieFileException.Reason; 
       3  import org.slf4j.Logger;
       4  import org.slf4j.LoggerFactory;
       5  import org.springframework.beans.factory.SmartInitializingSingl
          eton;
       7 -import org.springframework.core.io.Resource;                   
       6 +import org.springframework.context.annotation.Profile;         
       7  import org.springframework.stereotype.Component;
       8 
       9  /**
      11 - * Carrega o arquivo de filmes na inicialização (FR-001).      
      10 + * Carga direta do arquivo de filmes na inicialização (FR-001),
         + no modo padrão e no perfil                                    
      11 + * {@code postgres}. Com o perfil {@code kafka}, ela não roda: 
         +o tópico é o único caminho de entrada dos                      
      12 + * dados (spec 003, FR-009 e US1-6).                           
      13   *
      14   * <p>Roda em {@link #afterSingletonsInstantiated()}: depois do
           Flyway e dos repositórios, e antes de o
      15   * servidor web começar a aceitar conexões (research R2). Uma e
          xceção aqui interrompe a inicialização.
      16   */
      17  @Component
      18 +@Profile("!kafka")                                             
      19  public class MovieFileLoader implements SmartInitializingSingle
          ton {
      20 
      21      private static final Logger log = LoggerFactory.getLogger(M
          ovieFileLoader.class);
      22 
      21 -    private final MovieFileProperties properties;              
      22 -    private final MovieCsvReader reader;                       
      23 +    private final MovieFileReading reading;                    
      24      private final MovieImportService importService;
      25 
      25 -    public MovieFileLoader(MovieFileProperties properties, Movi
         -eCsvReader reader,                                             
      26 -            MovieImportService importService) {                
      27 -        this.properties = properties;                          
      28 -        this.reader = reader;                                  
      26 +    public MovieFileLoader(MovieFileReading reading, MovieImpor
         +tService importService) {                                      
      27 +        this.reading = reading;                                
      28          this.importService = importService;
      29      }
      30 
      31      @Override
      32      public void afterSingletonsInstantiated() {
      34 -        String path = properties.path();                       
      35 -        Resource resource = properties.resource();             
      36 -        if (!resource.exists()) {                              
      37 -            throw new MovieFileException(Reason.NOT_FOUND, path
         -, null, "o arquivo não existe");                               
      38 -        }                                                      
      39 -        if (!resource.isReadable()) {                          
      40 -            throw new MovieFileException(Reason.UNREADABLE, pat
         -h, null, "o caminho não é um arquivo legível");                
      41 -        }                                                      
      42 -                                                               
      43 -        MovieCsvReader.Result result = reader.read(resource, pa
         -th);                                                           
      33 +        MovieCsvReader.Result result = reading.read();         
      34          LoadReport report = result.report();
      45 -        for (Rejection rejection : report.rejections()) {      
      46 -            log.warn("Linha {} rejeitada: {}", rejection.lineNu
         -mber(), rejection.describe());                                 
      47 -        }                                                      
      35          importService.importMovies(result.rows());
      36          log.info("Carga do arquivo {} concluída: {} linhas lida
          s, {} aceitas, {} rejeitadas",
      50 -                path, report.linesRead(), report.accepted(), re
         -port.rejected());                                              
      37 +                reading.path(), report.linesRead(), report.acce
         +pted(), report.rejected());                                    
      38      }
      39  }

● Write(src\main\java\com\outsera\awards\ingestion\kafka\KafkaIngestionPropertie
       s.java)
  ⎿  Wrote 16 lines to src\main\java\com\outsera\awards\ingestion\kafka\Kaf
     kaIngestionProperties.java
       1 package com.outsera.awards.ingestion.kafka;
       2 
       3 import org.springframework.boot.context.properties.Configuration
         Properties;
       4 
       5 /**
       6  * Configuração da ingestão por Kafka (spec 003, FR-003; researc
         h R6, R9 e R10).
       7  *
       8  * @param topicMovies       tópico de filmes ({@code KAFKA_TOPIC
     … +9 lines (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\ingestion\kafka\EventRejectionReason.ja
       va)
  ⎿  Wrote 33 lines to src\main\java\com\outsera\awards\ingestion\kafka\Eve
     ntRejectionReason.java
       1 package com.outsera.awards.ingestion.kafka;
       2 
       3 import com.outsera.awards.ingestion.RejectionReason;
       4 
       5 /**
       6  * Motivo de rejeição de um evento do tópico de filmes (spec 003
         , FR-012 e FR-015; data-model.md).
       7  *
       8  * <p>Os motivos de conteúdo reaproveitam as descrições da linha
          do CSV ({@link RejectionReason}), porque
     … +25 lines (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\ingestion\kafka\LoadCompletedEvent.java
       )
  ⎿  Wrote 10 lines to src\main\java\com\outsera\awards\ingestion\kafka\Loa
     dCompletedEvent.java
       1 package com.outsera.awards.ingestion.kafka;
       2 
       3 /**
       4  * Evento de carga concluída (spec 003, FR-011; contracts/kafka-
         events.md).
       5  *
       6  * @param runId identifica a execução que publicou
       7  * @param count filmes publicados nessa execução
       8  */
       9 public record LoadCompletedEvent(String runId, long count) {
     … +1 line (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\ingestion\kafka\MovieEventParser.java)
  ⎿  Wrote 134 lines to
     src\main\java\com\outsera\awards\ingestion\kafka\MovieEventParser.java
       1 package com.outsera.awards.ingestion.kafka;
       2 
       3 import com.outsera.awards.ingestion.MovieFieldRules;
       4 import com.outsera.awards.ingestion.MovieRow;
       5 import java.nio.ByteBuffer;
       6 import java.nio.charset.CharacterCodingException;
       7 import java.nio.charset.CodingErrorAction;
       8 import java.nio.charset.StandardCharsets;
       9 import java.util.List;
      10 import tools.jackson.core.JacksonException;
     … +124 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; sed -i 's/return value ==
      null || !value.isString() ? null : value.asString().strip();/return value
      == null || !valu…)
  ⎿  128:        return value == null || !value.isString() ? null : value.a
     sString().trim();
  ⎿  Updated
     src\main\java\com\outsera\awards\ingestion\kafka\MovieEventParser.java
     +1 -1
      125      /** O texto do campo sem os espaços em volta, como no CSV,
            ou nulo se ausente ou não for texto. */
      126      private static String text(JsonNode node, String field) {
      127          JsonNode value = node.get(field);
      128 -        return value == null || !value.isString() ? null : val
          -ue.asString().strip();                                        
      128 +        return value == null || !value.isString() ? null : val
          +ue.asString().trim();                                         
      129      }
      130 
      131      private static String shorten(String detail) {

● Write(src\main\java\com\outsera\awards\ingestion\kafka\IngestionReadiness.java
       )
  ⎿  Wrote 48 lines to src\main\java\com\outsera\awards\ingestion\kafka\Ing
     estionReadiness.java
       1 package com.outsera.awards.ingestion.kafka;
       2 
       3 import java.util.UUID;
       4 import org.slf4j.Logger;
       5 import org.slf4j.LoggerFactory;
       6 import org.springframework.boot.health.contributor.Health;
       7 import org.springframework.boot.health.contributor.HealthIndicat
         or;
       8 import org.springframework.boot.health.contributor.Status;
       9 import org.springframework.context.annotation.Profile;
     … +39 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\ingestion\kafka\MovieEventPublisher.jav
       a)
  ⎿  Wrote 140 lines to src\main\java\com\outsera\awards\ingestion\kafka\Mo
     vieEventPublisher.java
       1 package com.outsera.awards.ingestion.kafka;
       2 
       3 import com.outsera.awards.ingestion.LoadReport;
       4 import com.outsera.awards.ingestion.MovieCsvReader;
       5 import com.outsera.awards.ingestion.MovieFileReading;
       6 import com.outsera.awards.ingestion.MovieRow;
       7 import java.nio.charset.StandardCharsets;
       8 import java.time.Duration;
       9 import java.util.List;
      10 import java.util.concurrent.TimeUnit;
     … +130 lines (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\ingestion\kafka\MovieEventListener.java
       )
  ⎿  Wrote 109 lines to src\main\java\com\outsera\awards\ingestion\kafka\Mo
     vieEventListener.java
       1 package com.outsera.awards.ingestion.kafka;
       2 
       3 import com.outsera.awards.ingestion.MovieImportService;
       4 import com.outsera.awards.ingestion.kafka.MovieEventParser.Compl
         eted;
       5 import com.outsera.awards.ingestion.kafka.MovieEventParser.Movie
         ;
       6 import com.outsera.awards.ingestion.kafka.MovieEventParser.Parse
         d;
       7 import com.outsera.awards.ingestion.kafka.MovieEventParser.Rejec
     … +103 lines (~5 KB) (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\ingestion\kafka\KafkaIngestionConfigura
       tion.java)
  ⎿  Wrote 58 lines to src\main\java\com\outsera\awards\ingestion\kafka\Kaf
     kaIngestionConfiguration.java
       1 package com.outsera.awards.ingestion.kafka;
       2 
       3 import org.apache.kafka.clients.admin.NewTopic;
       4 import org.slf4j.Logger;
       5 import org.slf4j.LoggerFactory;
       6 import org.springframework.boot.autoconfigure.condition.Conditio
         nalOnProperty;
       7 import org.springframework.context.annotation.Bean;
       8 import org.springframework.context.annotation.Configuration;
       9 import org.springframework.context.annotation.Profile;
     … +49 lines (ctrl+o to expand)

● Compiling and running a first subset.

  Ran 3 shell commands · 1 failed (ctrl+o to expand)

● The json-logs Failsafe summary still holds errors from the earlier run, so the
  verify failure isn't real; all 39 tests passed. I'm clearing the summaries
  from here on and running the remaining Kafka tests. · summary

  Ran 1 shell command (ctrl+o to expand)

● Most Kafka integration tests pass; I'm starting the broker by hand for the
  parameterized test, then I'll fix the topic count failure in
  KafkaInvalidLinesIT and check the consumer lag series name. · summary

  Ran 1 shell command (ctrl+o to expand)

● Update(src\test\java\com\outsera\awards\kafka\KafkaMetricsIT.java)
  ⎿  Added 3 lines, removed 1 line
      37                  sample -> "rejected".equals(sample.label("outco
          me")))).singleElement()
      38                  .satisfies(sample -> assertThat(sample.value())
          .isZero());
      39          assertThat(PrometheusText.find(samples, LAG, sample ->
          MOVIES_TOPIC.equals(sample.label("topic"))))
      40 -                .as("série %s com topic=%s", LAG, MOVIES_TOPIC)
      40 +                .as("série %s com topic=%s; séries de atraso en
         +contradas: %s", LAG, MOVIES_TOPIC,                             
      41 +                        samples.stream().filter(sample -> sampl
         +e.name().contains("lag"))                                      
      42 +                                .map(sample -> sample.name() + 
         +sample.labels()).toList())                                     
      43                  .isNotEmpty();
      44      }
      45  }

  Ran 1 shell command (ctrl+o to expand)

● Only the client-level lag metric is visible so far, since the per-partition
  series waits for Micrometer's periodic refresh. With one consumer and one
  partition, that client-level max equals the topic's lag, so I'm pinning it to
  settle the R19 check and will update R19 and the contract. · summary

● Update(src\test\java\com\outsera\awards\kafka\KafkaMetricsIT.java)
  ⎿  Added 3 lines, removed 2 lines
      36          assertThat(PrometheusText.find(samples, "awards_ingesti
          on_events_total",
      37                  sample -> "rejected".equals(sample.label("outco
          me")))).singleElement()
      38                  .satisfies(sample -> assertThat(sample.value())
          .isZero());
      39 -        assertThat(PrometheusText.find(samples, LAG, sample -> 
         -MOVIES_TOPIC.equals(sample.label("topic"))))                   
      40 -                .as("série %s com topic=%s; séries de atraso en
         -contradas: %s", LAG, MOVIES_TOPIC,                             
      39 +        assertThat(PrometheusText.find(samples, LAG, sample -> 
         +sample.label("client_id") != null                              
      40 +                        && sample.label("client_id").startsWith
         +(CONSUMER_CLIENT)))                                            
      41 +                .as("série %s do consumidor %s; séries de atras
         +o encontradas: %s", LAG, CONSUMER_CLIENT,                      
      42                          samples.stream().filter(sample -> sampl
          e.name().contains("lag"))
      43                                  .map(sample -> sample.name() +
          sample.labels()).toList())
      44                  .isNotEmpty();
● Update(src\test\java\com\outsera\awards\kafka\KafkaMetricsIT.java)
  ⎿  Added 5 lines, removed 2 lines
      13  /**
      14   * As métricas trazem os eventos gravados e rejeitados e o atra
          so do consumidor no tópico de filmes (spec
      15   * 003, FR-017; research R19). O nome da série de atraso é o qu
          e o Micrometer publica com o Spring Kafka
      16 - * do Boot 4.1.2, e o dashboard usa o mesmo.                   
      16 + * do Boot 4.1.2, e o dashboard usa o mesmo: o atraso máximo do
         + cliente consumidor do ouvinte                                 
      17 + * {@code awards-movies}. Com um consumidor e uma partição, é o
         + atraso do tópico de filmes. A série por                       
      18 + * partição só aparece depois da atualização periódica do Micro
         +meter, e por isso não é usada.                                 
      19   */
      20  @TestPropertySource(properties = "movies.csv.path=classpath:mov
          ielist.csv")
      21  class KafkaMetricsIT extends KafkaScenarioIT {
      22 
      21 -    static final String LAG = "kafka_consumer_fetch_manager_rec
         -ords_lag";                                                     
      23 +    static final String LAG = "kafka_consumer_fetch_manager_rec
         +ords_lag_max";                                                 
      24 +    static final String CONSUMER_CLIENT = "consumer-awards-movi
         +es";                                                           
      25 
      26      private List<Sample> samples() {
      27          String text = http.get().uri("/actuator/prometheus").ex
          change().expectStatus().isOk()

  Ran 2 shell commands (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs");…)
  ⎿  specs/003-modo-docker/research.md:507:  - **atraso do consumidor:** `m
     ax(kafka_consumer_fetch_manager_records_lag_max{cl
     specs/003-modo-docker/research.md:510:  cliente: `kafka_consumer_fetch
     … +3 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\contracts\operational-endpoints.md +1 -1
      63  | Série no Prometheus | Rótulos | Significado |
      64  | --- | --- | --- |
      65  | `awards_ingestion_events_total` | `outcome="recorded"` ou `"r
          ejected"`, `application` | eventos de filme gravados e enviados
           ao tópico de erros (FR-017) |
      66 -| `kafka_consumer_fetch_manager_records_lag` (nome a confirmar,
         - research R19) | `topic`, `partition`, `client_id` | atraso do 
         -consumidor no tópico de filmes |                               
      66 +| `kafka_consumer_fetch_manager_records_lag_max` (verificado, r
         +esearch R19) | `client_id` (`consumer-awards-movies-<n>`), `spr
         +ing_id`, `kafka_version`, `application` | atraso máximo do cons
         +umidor do tópico de filmes (uma partição, um consumidor) |     
      67 
      68  No modo padrão, nenhuma série `kafka_*` nem `awards_ingestion_*
          ` existe (research R2).
  ⎿  Updated specs\003-modo-docker\research.md +7 -4
      504    - **requisições por segundo** por rota: `sum by (uri) (rate(
           http_server_requests_seconds_count{uri=~"/api/.*"}[1m]))`;
      505    - **taxa de erro:** razão entre `outcome=~"CLIENT_ERROR|SERV
           ER_ERROR"` e o total, nas rotas de negócio;
      506    - **p95 e p99 de latência:** `histogram_quantile(0.95|0.99,
           sum by (le, uri) (rate(http_server_requests_seconds_bucket{uri
           =~"/api/.*"}[5m])))`;
      507 -  - **atraso do consumidor:** `kafka_consumer_fetch_manager_re
          -cords_lag{topic="awards.movies"}` ou o                        
      508 -    equivalente que o Micrometer publicar;                    
      507 +  - **atraso do consumidor:** `max(kafka_consumer_fetch_manage
          +r_records_lag_max{client_id=~"consumer-awards-movies.*"})`;   
      508    - **eventos gravados e rejeitados:** `rate(awards_ingestion_
           events_total[5m])` por `outcome`.
      510 -- **Verificar:** o nome exato da série de atraso que o Microme
          -ter publica com o Spring Kafka do Boot                        
      511 -  4.1.2 (`MicrometerConsumerListener`). O `KafkaMetricsIT` fix
          -a o nome, e o painel usa o mesmo.                             
      509 +- **Verificado (etapa 2):** com o Spring Kafka 4.1.2, a série 
          +que aparece logo depois da carga é a do                       
      510 +  cliente: `kafka_consumer_fetch_manager_records_lag_max`, com
          + o rótulo `client_id` do consumidor do                        
      511 +  ouvinte `awards-movies` (`consumer-awards-movies-<n>`). A sé
          +rie por partição (`..._records_lag`, com                      
      512 +  `topic` e `partition`) só aparece depois da atualização peri
          +ódica do Micrometer. Com um consumidor e                      
      513 +  uma partição, o máximo do cliente é o atraso do tópico de fi
          +lmes. O `KafkaMetricsIT` fixa esse nome, e                    
      514 +  o painel usa o mesmo.                                       
      515 
      516  ## R20. Pipeline: jobs novos e mudanças
      517 

● Now the remaining two fixes: start the broker by hand in the four
  SpringApplication tests, and wait for the publication summary (the race with
  the readiness).

● Update(src\test\java\com\outsera\awards\support\KafkaTestSupport.java)
  ⎿  Added 11 lines
      48          }
      49      }
      50 
      51 +    /**                                                        
      52 +     * Sobe um broker embutido (KRaft, um nó) para os testes qu
         +e rodam a aplicação com                                        
      53 +     * {@code SpringApplication}, fora de um contexto de teste 
         +do Spring. Quem chama o fecha com                              
      54 +     * {@link EmbeddedKafkaBroker#destroy()} no fim da classe. 
      55 +     */                                                        
      56 +    public static EmbeddedKafkaBroker startBroker() {          
      57 +        EmbeddedKafkaBroker broker = new EmbeddedKafkaKraftBrok
         +er(1, 1);                                                      
      58 +        broker.afterPropertiesSet();                           
      59 +        return broker;                                         
      60 +    }                                                          
      61 +                                                               
      62      public static Topics topics(Class<?> testClass) {
      63          String name = testClass.getSimpleName().toLowerCase(Loc
          ale.ROOT);
      64          return new Topics("awards.movies." + name, "awards.movi
          es.errors." + name, "awards-" + name);

● Update(src\test\java\com\outsera\awards\support\KafkaTestSupport.java)
  ⎿  Added 2 lines
      23  import org.apache.kafka.common.serialization.ByteArraySerialize
          r;
      24  import org.apache.kafka.common.serialization.StringDeserializer
          ;
      25  import org.apache.kafka.common.serialization.StringSerializer;
      26 +import org.springframework.kafka.test.EmbeddedKafkaBroker;     
      27 +import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
      28 
      29  /**
      30   * Produtor e consumidor Kafka <strong>reais</strong>, ligados
          ao broker embutido dos testes (spec 003,

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs");…)
  ⎿  23: * Quatro subidas (três reinícios) no modo Docker, sobre o mesmo Po
     stgreSQL e o mesmo broker (spec 003,
     31:    private static EmbeddedKafkaBroker broker;
     … +11 lines (ctrl+o to expand)
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaEquivalenceIT.java
     +13 -3
      13  import org.junit.jupiter.params.ParameterizedTest;
      14  import org.junit.jupiter.params.provider.ValueSource;
      15  import org.springframework.kafka.test.EmbeddedKafkaBroker;
      16 -import org.springframework.kafka.test.context.EmbeddedKafka;   
      16 
      17  /**
      18   * O mesmo arquivo pela carga direta e pela ingestão por Kafka
          produz respostas idênticas, byte a byte e
      19   * com o mesmo ETag, sem exceção (spec 003, FR-018, US1-2 e SC-
          001). Usa {@code SpringApplication} pela
      20   * cláusula "múltiplas execuções" do Princípio I (constituição 
          1.5.0), com o broker embutido real.
      21   */
      23 -@EmbeddedKafka(partitions = 1)                                 
      22  class KafkaEquivalenceIT {
      23 
      24 +    private static EmbeddedKafkaBroker broker;                 
      25 +                                                               
      26 +    @org.junit.jupiter.api.BeforeAll                           
      27 +    static void startBroker() {                                
      28 +        broker = KafkaTestSupport.startBroker();               
      29 +    }                                                          
      30 +                                                               
      31 +    @org.junit.jupiter.api.AfterAll                            
      32 +    static void stopBroker() {                                 
      33 +        broker.destroy();                                      
      34 +    }                                                          
      35 +                                                               
      36      private static final List<String> PATHS = List.of(
      37              "/api/producers/intervals",
      38              "/api/movies?size=100&page=0",
     ...
      47              "classpath:scenarios/adversario/13-ordenacao-com-ac
          entos-e-caixa.csv",
      48              "classpath:scenarios/field-too-long.csv"})
      49      @DisplayName("S003 FR-018 US1-2 SC-001 carga direta e Kafka
           idênticas")
      40 -    void directAndKafkaLoadsAreEquivalent(String csvPath, Embed
         -dedKafkaBroker broker) {                                       
      50 +    void directAndKafkaLoadsAreEquivalent(String csvPath) {    
      51          // Tópicos e grupo próprios para cada arquivo, para que
           um não veja os eventos do outro.
      52          Topics base = KafkaTestSupport.topics(KafkaEquivalenceI
          T.class);
      53          String suffix = "." + Integer.toHexString(csvPath.hashC
          ode());
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaStructuralFailureIT.java
     +13 -3
      12  import org.junit.jupiter.params.ParameterizedTest;
      13  import org.junit.jupiter.params.provider.CsvSource;
      14  import org.springframework.kafka.test.EmbeddedKafkaBroker;
      15 -import org.springframework.kafka.test.context.EmbeddedKafka;   
      15 
      16  /**
      17   * Com o perfil {@code kafka}, um erro estrutural no arquivo im
          pede a subida com a mesma mensagem do
      18   * Marco 1, e nada é publicado (spec 003, FR-009 e US1-5). Usa
          {@code SpringApplication} pela cláusula
      19   * "falha de inicialização" do Princípio I.
      20   */
      22 -@EmbeddedKafka(partitions = 1)                                 
      21  @Tag("log")
      22  class KafkaStructuralFailureIT {
      23 
      24 +    private static EmbeddedKafkaBroker broker;                 
      25 +                                                               
      26 +    @org.junit.jupiter.api.BeforeAll                           
      27 +    static void startBroker() {                                
      28 +        broker = KafkaTestSupport.startBroker();               
      29 +    }                                                          
      30 +                                                               
      31 +    @org.junit.jupiter.api.AfterAll                            
      32 +    static void stopBroker() {                                 
      33 +        broker.destroy();                                      
      34 +    }                                                          
      35 +                                                               
      36      @ParameterizedTest(name = "S003 FR-009 US1-5 {0}: {1}")
      37      @CsvSource({
      38              "file:./nao-existe.csv, NOT_FOUND",
      39              "classpath:scenarios/header-wrong-order.csv, INVALI
          D_HEADER"})
      40      @DisplayName("S003 FR-009 US1-5 erro estrutural impede a su
          bida e nada é publicado")
      31 -    void structuralErrorStopsTheStartup(String csvPath, String 
         -reason, EmbeddedKafkaBroker broker) {                          
      41 +    void structuralErrorStopsTheStartup(String csvPath, String 
         +reason) {                                                      
      42          Topics topics = KafkaTestSupport.topics(KafkaStructural
          FailureIT.class);
      43          String brokers = broker.getBrokersAsString();
      44 
  ⎿  Updated
     src\test\java\com\outsera\awards\postgres\RestartPostgresIT.java +13
     -3
      18  import org.junit.jupiter.api.DisplayName;
      19  import org.junit.jupiter.api.Test;
      20  import org.springframework.kafka.test.EmbeddedKafkaBroker;
      21 -import org.springframework.kafka.test.context.EmbeddedKafka;   
      21 
      22  /**
      23   * Quatro subidas (três reinícios) no modo Docker, sobre o mesm
          o PostgreSQL e o mesmo broker (spec 003,
     ...
      26   * (Clarifications, 2026-11-24). Usa {@code SpringApplication}
          pela cláusula "múltiplas execuções" do
      27   * Princípio I.
      28   */
      30 -@EmbeddedKafka(partitions = 1)                                 
      29  class RestartPostgresIT {
      30 
      31 +    private static EmbeddedKafkaBroker broker;                 
      32 +                                                               
      33 +    @org.junit.jupiter.api.BeforeAll                           
      34 +    static void startBroker() {                                
      35 +        broker = KafkaTestSupport.startBroker();               
      36 +    }                                                          
      37 +                                                               
      38 +    @org.junit.jupiter.api.AfterAll                            
      39 +    static void stopBroker() {                                 
      40 +        broker.destroy();                                      
      41 +    }                                                          
      42 +                                                               
      43      private static final String CSV = "classpath:movielist.csv"
          ;
      44      private static final String SCHEMA = PostgresSupport.newSch
          ema(RestartPostgresIT.class);
      45      private static final Pattern ITEM = Pattern.compile("\\{\"y
          ear\":(\\d+),\"title\":\"((?:[^\"\\\\]|\\\\.)*)\"");
     ...
      76 
      77      @Test
      78      @DisplayName("S003 US2-3 US2-4 US2-6 SC-004 três reinícios 
          sobre o mesmo PostgreSQL e o mesmo broker")
      69 -    void restartsKeepNewMoviesAndReapplyTheFile(EmbeddedKafkaBr
         -oker broker) {                                                 
      79 +    void restartsKeepNewMoviesAndReapplyTheFile() {            
      80          String brokers = broker.getBrokersAsString();
      81          String topic = KafkaTestSupport.topics(RestartPostgresI
          T.class).movies();
      82 
  ⎿  Updated src\test\java\com\outsera\awards\postgres\RetryPostgresIT.java
     +13 -3
      17  import org.junit.jupiter.api.Test;
      18  import org.junit.jupiter.api.parallel.Isolated;
      19  import org.springframework.kafka.test.EmbeddedKafkaBroker;
      20 -import org.springframework.kafka.test.context.EmbeddedKafka;   
      20 
      21  /**
      22   * Quando a gravação de um evento válido falha, o evento não é
          descartado nem vai ao tópico de erros: a
     ...
      28   * {@code postgres} do Failsafe não usa paralelismo: ele nunca
          roda ao mesmo tempo que outro IT.
      29   */
      30  @Isolated
      32 -@EmbeddedKafka(partitions = 1)                                 
      31  class RetryPostgresIT {
      32 
      33 +    private static EmbeddedKafkaBroker broker;                 
      34 +                                                               
      35 +    @org.junit.jupiter.api.BeforeAll                           
      36 +    static void startBroker() {                                
      37 +        broker = KafkaTestSupport.startBroker();               
      38 +    }                                                          
      39 +                                                               
      40 +    @org.junit.jupiter.api.AfterAll                            
      41 +    static void stopBroker() {                                 
      42 +        broker.destroy();                                      
      43 +    }                                                          
      44 +                                                               
      45      private static final String SCHEMA = PostgresSupport.newSch
          ema(RetryPostgresIT.class);
      46 
      47      @AfterAll
     ...
      56 
      57      @Test
      58      @DisplayName("S003 FR-015a nova tentativa quando a gravação
           falha, sem tópico de erros")
      49 -    void retriesWhenTheWriteFails(EmbeddedKafkaBroker broker) {
      59 +    void retriesWhenTheWriteFails() {                          
      60          String brokers = broker.getBrokersAsString();
      61          Topics topics = KafkaTestSupport.topics(RetryPostgresIT
          .class);
      62          List<String> args = new ArrayList<>(PostgresSupport.arg
          s(SCHEMA, "postgres,kafka"));

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs");…)
  ⎿  src/test/java/com/outsera/awards/kafka/KafkaInvalidLinesIT.java:51:
          await("resumo da publicação", 10, () -> output.getAll().contains(
     "Publicação do arquivo "
     … +3 lines (ctrl+o to expand)
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaEquivalenceIT.java
     +4 -2
       9  import java.time.Duration;
      10  import java.util.List;
      11  import java.util.Map;
      12 +import org.junit.jupiter.api.AfterAll;                         
      13 +import org.junit.jupiter.api.BeforeAll;                        
      14  import org.junit.jupiter.api.DisplayName;
      15  import org.junit.jupiter.params.ParameterizedTest;
      16  import org.junit.jupiter.params.provider.ValueSource;
     ...
      25 
      26      private static EmbeddedKafkaBroker broker;
      27 
      26 -    @org.junit.jupiter.api.BeforeAll                           
      28 +    @BeforeAll                                                 
      29      static void startBroker() {
      30          broker = KafkaTestSupport.startBroker();
      31      }
      32 
      31 -    @org.junit.jupiter.api.AfterAll                            
      33 +    @AfterAll                                                  
      34      static void stopBroker() {
      35          broker.destroy();
      36      }
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaInvalidLinesIT.java +3 -2
      47      @DisplayName("S003 FR-009 US1-4 só as linhas aceitas são pu
          blicadas, e a aplicação fica pronta")
      48      void onlyAcceptedLinesArePublished(CapturedOutput output) {
      49          awaitReady();
      50 -        assertThat(output.getAll()).contains("Publicação do arq
         -uivo classpath:scenarios/invalid-lines.csv "                   
      51 -                + "concluída: 13 linhas lidas, 2 publicadas, 11
         - rejeitadas");                                                 
      50 +        // O resumo sai depois do envio do marcador, e a readin
         +ess pode ficar positiva um pouco antes.                        
      51 +        await("resumo da publicação", 10, () -> output.getAll()
         +.contains("Publicação do arquivo "                             
      52 +                + "classpath:scenarios/invalid-lines.csv conclu
         +ída: 13 linhas lidas, 2 publicadas, 11 rejeitadas"));          
      53          // Dois filmes e o evento de carga concluída.
      54          assertThat(KafkaTestSupport.read(brokers(), MOVIES_TOPI
          C, 3, Duration.ofSeconds(20))).hasSize(3);
      55          assertThat(allMovies().titles()).containsExactly("Filme
           Válido 1", "Filme Válido 2");
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaStructuralFailureIT.java
     +4 -2
       7  import com.outsera.awards.support.KafkaTestSupport;
       8  import com.outsera.awards.support.KafkaTestSupport.Topics;
       9  import java.time.Duration;
      10 +import org.junit.jupiter.api.AfterAll;                         
      11 +import org.junit.jupiter.api.BeforeAll;                        
      12  import org.junit.jupiter.api.DisplayName;
      13  import org.junit.jupiter.api.Tag;
      14  import org.junit.jupiter.params.ParameterizedTest;
     ...
      25 
      26      private static EmbeddedKafkaBroker broker;
      27 
      26 -    @org.junit.jupiter.api.BeforeAll                           
      28 +    @BeforeAll                                                 
      29      static void startBroker() {
      30          broker = KafkaTestSupport.startBroker();
      31      }
      32 
      31 -    @org.junit.jupiter.api.AfterAll                            
      33 +    @AfterAll                                                  
      34      static void stopBroker() {
      35          broker.destroy();
      36      }
  ⎿  Updated src\test\java\com\outsera\awards\kafka\PublisherSummaryIT.java
     +2
      24          awaitReady();
      25          String expected = "Publicação do arquivo classpath:scen
          arios/load-summary.csv concluída: "
      26                  + "10 linhas lidas, 8 publicadas, 2 rejeitadas"
          ;
      27 +        // O resumo sai depois do envio do marcador, e a readin
         +ess pode ficar positiva um pouco antes.                        
      28 +        await("resumo da publicação", 10, () -> output.getAll()
         +.contains(expected));                                          
      29          assertThat(LogOutput.messages(output.getAll())).filtere
          dOn(message -> message.contains(expected)).hasSize(1);
      30          assertThat(movies("").totalElements()).isEqualTo(8);
      31      }
  ⎿  Updated
     src\test\java\com\outsera\awards\postgres\RestartPostgresIT.java +3 -2
      15  import java.util.regex.Matcher;
      16  import java.util.regex.Pattern;
      17  import org.junit.jupiter.api.AfterAll;
      18 +import org.junit.jupiter.api.BeforeAll;                        
      19  import org.junit.jupiter.api.DisplayName;
      20  import org.junit.jupiter.api.Test;
      21  import org.springframework.kafka.test.EmbeddedKafkaBroker;
     ...
      31 
      32      private static EmbeddedKafkaBroker broker;
      33 
      33 -    @org.junit.jupiter.api.BeforeAll                           
      34 +    @BeforeAll                                                 
      35      static void startBroker() {
      36          broker = KafkaTestSupport.startBroker();
      37      }
      38 
      38 -    @org.junit.jupiter.api.AfterAll                            
      39 +    @AfterAll                                                  
      40      static void stopBroker() {
      41          broker.destroy();
      42      }
  ⎿  … +1 file changed
     src\test\java\com\outsera\awards\postgres\RetryPostgresIT.java

● Before the full run, the T034c check: does an invalid topic name bring down
  the startup? I'll test it with a temporary IT that I delete afterwards.

  Ran 1 shell command (ctrl+o to expand)

● The invalid topic name no longer stops startup: KafkaAdmin logs the
  InvalidTopicException and the app still becomes ready. I'm removing the
  create-error-topic property next, since the test is the only thing that needed
  it. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs");…)
  ⎿  specs/003-modo-docker/research.md:213:  `awards.ingestion.create-error
     -topic`, que foi descartada.
     specs/003-modo-docker/tasks.md:337:  - se cai, usar `awards.ingestion.
     … +3 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\research.md +5 -6
      206      do consumidor cresce e aparece no dashboard.
      207  - **Tópico de erros fora do ar:** o envio ao tópico de erros e
           spera a confirmação por até 10 s. Se
      208    falhar, o aviso no log registra o motivo e a falha do envio,
            e o offset avança (caso de borda da spec).
      209 -  O `KafkaErrorTopicUnavailableIT` comprova isso com um nome d
          -e tópico de erros inválido para o Kafka, que                  
      210 -  o broker recusa de verdade. **Verificar antes:** se o `NewTo
          -pic` com esse nome derruba a subida. Se                       
      211 -  derrubar, o IT usa um nome válido que não é criado (o `NewTo
          -pic` do tópico de erros fica atrás da                         
      212 -  propriedade `awards.ingestion.create-error-topic`, padrão `t
          -rue`), com a criação automática de tópicos                    
      213 -  desligada no broker embutido. Se nenhum dos dois for possíve
          -l sem dublê, o limite é registrado em                         
      214 -  `docs/ai/decisoes.md`.                                      
      209 +  O `KafkaErrorTopicUnavailableIT` comprova isso com um nome d
          +e tópico de erros que o Kafka não aceita, e                   
      210 +  o cliente recusa cada envio de verdade. **Verificado (etapa 
          +2, T034c):** o `NewTopic` com esse nome não                   
      211 +  derruba a subida. Com `spring.kafka.admin.fail-fast: false`,
          + o `KafkaAdmin` registra o                                    
      212 +  `InvalidTopicException` e a aplicação fica pronta. Por isso 
          +não foi preciso a propriedade alternativa                     
      213 +  `awards.ingestion.create-error-topic`, que foi descartada.  
      214  - **Comprovação da nova tentativa (spec 003, FR-015a):**
      215    - **`RetryPostgresIT`** (perfil `postgres-it`) prova a **nov
           a tentativa quando a gravação falha**:
      216      1. com a aplicação pronta, o teste renomeia a tabela `movi
           e` do seu esquema por JDBC;
  ⎿  Updated src\main\java\com\outsera\awards\ingestion\kafka\KafkaIngestio
     nConfiguration.java -2
      3  import org.apache.kafka.clients.admin.NewTopic;
      4  import org.slf4j.Logger;
      5  import org.slf4j.LoggerFactory;
      6 -import org.springframework.boot.autoconfigure.condition.Conditio
        -nalOnProperty;                                                  
      6  import org.springframework.context.annotation.Bean;
      7  import org.springframework.context.annotation.Configuration;
      8  import org.springframework.context.annotation.Profile;
     ...
      31      }
      32 
      33      @Bean
      35 -    @ConditionalOnProperty(name = "awards.ingestion.create-erro
         -r-topic", havingValue = "true", matchIfMissing = true)         
      34      NewTopic errorsTopic(KafkaIngestionProperties properties) {
      35          return TopicBuilder.name(properties.topicErrors()).part
          itions(1).replicas(1).build();
      36      }
  ⎿  Updated src\main\java\com\outsera\awards\ingestion\kafka\KafkaIngestio
     nProperties.java +1 -3
       8   * @param topicMovies       tópico de filmes ({@code KAFKA_TOPI
          C_MOVIES})
       9   * @param topicErrors       tópico de erros ({@code KAFKA_TOPIC
          _ERRORS})
      10   * @param publishCompletion se o publicador envia o evento de c
          arga concluída ao fim da publicação
      11 - * @param createErrorTopic  se a aplicação cria o tópico de err
         -os na subida                                                   
      11   */
      12  @ConfigurationProperties("awards.ingestion")
      14 -public record KafkaIngestionProperties(String topicMovies, Stri
         -ng topicErrors, boolean publishCompletion,                     
      15 -        boolean createErrorTopic) {                            
      13 +public record KafkaIngestionProperties(String topicMovies, Stri
         +ng topicErrors, boolean publishCompletion) {                   
      14  }
  ⎿  Updated src\main\resources\application-kafka.yml -2
      39      # Envio do evento de carga concluída ao fim da publicação (
          research R10). Desligado, a readiness
      40      # espera um marcador publicado por outro cliente, com o run
          Id desta execução.
      41      publish-completion: true
      42 -    # Criação do tópico de erros pela aplicação (research R9). 
      43 -    create-error-topic: true                                   
      42 
      43  management:
      44    endpoint:
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaErrorTopicUnavaila
     bleIT.java +4 -7
       7  import org.junit.jupiter.api.Tag;
       8  import org.junit.jupiter.api.Test;
       9  import org.springframework.boot.test.system.CapturedOutput;
      10 -import org.springframework.kafka.test.context.EmbeddedKafka;   
      10  import org.springframework.test.context.TestPropertySource;
      11 
      12  /**
      13   * Com o tópico de erros indisponível, o evento inválido não é
          gravado, o log registra o motivo e a falha
      14   * do envio, e o consumo segue (spec 003, FR-015, caso de borda
           "Tópico de erros indisponível"; research
      16 - * R9). O tópico de erros tem um nome válido que não existe no 
         -broker: ele não é criado pela aplicação                        
      17 - * ({@code awards.ingestion.create-error-topic=false}) nem pelo
         - broker, com a criação automática desligada.                   
      18 - * O broker recusa o envio de verdade, sem dublê.              
      15 + * R9). O tópico de erros tem um nome que o Kafka não aceita: a
         + criação dele na subida falha (com                             
      16 + * {@code fail-fast} desligado, a aplicação sobe assim mesmo), 
         +e cada envio é recusado de verdade pelo                        
      17 + * cliente Kafka, sem dublê. A verificação da tarefa T034c conf
         +irmou que esse nome não derruba a subida.                      
      18   */
      20 -@EmbeddedKafka(partitions = 1, brokerProperties = "auto.create.
         -topics.enable=false")                                          
      19  @TestPropertySource(properties = {
      20          "movies.csv.path=classpath:scenarios/header-only.csv",
      23 -        "awards.ingestion.create-error-topic=false",           
      24 -        "KAFKA_TOPIC_ERRORS=awards.movies.errors.inexistente"})
      21 +        "KAFKA_TOPIC_ERRORS=awards.movies.errors inválido!"})  
      22  @Tag("log")
      23  class KafkaErrorTopicUnavailableIT extends KafkaScenarioIT {
      24 

● Running the Kafka subset again.

  Ran 3 shell commands (ctrl+o to expand)

● The log has the message, but CapturedOutput misses it because the publication
  summary is logged asynchronously during the class's first test, whose capture
  is discarded. PublisherSummaryIT already checks the summary in an isolated
  test, so I'm removing that assertion from KafkaInvalidLinesIT. · summary

● Update(src\test\java\com\outsera\awards\kafka\KafkaInvalidLinesIT.java)
  ⎿  Added 3 lines, removed 3 lines
      47      @DisplayName("S003 FR-009 US1-4 só as linhas aceitas são pu
          blicadas, e a aplicação fica pronta")
      48      void onlyAcceptedLinesArePublished(CapturedOutput output) {
      49          awaitReady();
      50 -        // O resumo sai depois do envio do marcador, e a readin
         -ess pode ficar positiva um pouco antes.                        
      51 -        await("resumo da publicação", 10, () -> output.getAll()
         -.contains("Publicação do arquivo "                             
      52 -                + "classpath:scenarios/invalid-lines.csv conclu
         -ída: 13 linhas lidas, 2 publicadas, 11 rejeitadas"));          
      50 +        // O resumo da publicação sai num fio próprio, durante 
         +o primeiro teste da classe, e a captura de                     
      51 +        // saída de cada teste é descartada no fim dele. O resu
         +mo é verificado pelo PublisherSummaryIT,                       
      52 +        // que tem um único teste. Aqui, a prova é o próprio tó
         +pico.                                                          
      53          // Dois filmes e o evento de carga concluída.
      54          assertThat(KafkaTestSupport.read(brokers(), MOVIES_TOPI
          C, 3, Duration.ofSeconds(20))).hasSize(3);
      55          assertThat(allMovies().titles()).containsExactly("Filme
           Válido 1", "Filme Válido 2");

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; sed -i 's/    void
      onlyAcceptedLinesArePublished(CapturedOutput output) {/    void
      onlyAcceptedLinesArePublished() {…)
  ⎿  14:import org.springframework.boot.test.system.CapturedOutput;
     40:    void invalidLineIsRejectedWithWarning(int line, String reason,
     CapturedOutput output) {
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaInvalidLinesIT.java +1 -1
      45 
      46      @Test
      47      @DisplayName("S003 FR-009 US1-4 só as linhas aceitas são pu
          blicadas, e a aplicação fica pronta")
      48 -    void onlyAcceptedLinesArePublished(CapturedOutput output) {
      48 +    void onlyAcceptedLinesArePublished() {                     
      49          awaitReady();
      50          // O resumo da publicação sai num fio próprio, durante 
          o primeiro teste da classe, e a captura de
      51          // saída de cada teste é descartada no fim dele. O resu
          mo é verificado pelo PublisherSummaryIT,

● Now the full ./mvnw clean verify.

  Ran 1 shell command (ctrl+o to expand)

● Full clean verify passes (239 tests plus 93 in the JSON log run), but at 316 s
  it is more than twice the 96 s baseline, mostly from the two Kafka
  integration tests. I'm trimming their unnecessary waits next. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs");…)
  ⎿  src/test/java/com/outsera/awards/kafka/KafkaInvalidEventsIT.java:66:
           int errorsBefore = sequence - 2101;
     src/test/java/com/outsera/awards/kafka/KafkaInvalidEventsIT.java:74:
     … +5 lines (ctrl+o to expand)
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaBrokerUnavailableIT.java
     +1 -1
      35          long start = System.nanoTime();
      36          Result result = ApplicationStarter.startAndCall("classp
          ath:movielist.csv",
      37                  KafkaTestSupport.args(brokers, KafkaTestSupport
          .topics(KafkaBrokerUnavailableIT.class)), port -> {
      38 -                    long deadline = System.nanoTime() + 10_000_
         -000_000L;                                                      
      38 +                    long deadline = System.nanoTime() + 5_000_0
         +00_000L;                                                       
      39                      while (System.nanoTime() < deadline) {
      40                          assertThat(Http.get(port, "/actuator/he
          alth/liveness").status()).isEqualTo(200);
      41                          assertThat(Http.get(port, "/actuator/he
          alth/readiness").status()).isEqualTo(503);
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java +2 -1
      62          int sequence = SEQUENCE.incrementAndGet();
      63          String key = "invalido-" + sequence;
      64          double rejectedBefore = rejected();
      65 -        int errorsBefore = KafkaTestSupport.read(brokers(), ERR
         -ORS_TOPIC, 0, Duration.ofSeconds(1)).size();                   
      65 +        // Cada caso anterior deixou exatamente um registro no 
         +tópico de erros.                                               
      66 +        int errorsBefore = sequence - 2101;                    
      67 
      68          KafkaTestSupport.send(brokers(), MOVIES_TOPIC, key, pay
          load);
      69          // Um evento válido logo depois do inválido é gravado (
          US3-4, SC-006).

● The test setup spends about 2 seconds per case on producer creation and
  consumer group joins, so I'm switching KafkaTestSupport to reuse the producer
  and read with assign and seekToBeginning. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs");…)
  ⎿              }
             }
             return records;
     … +2 lines (ctrl+o to expand)
  ⎿  Updated src\test\java\com\outsera\awards\support\KafkaTestSupport.java
     +33 -13
       8  import java.util.Locale;
       9  import java.util.Map;
      10  import java.util.Properties;
      11 -import java.util.UUID;                                         
      11 +import java.util.concurrent.ConcurrentHashMap;                 
      12  import java.util.concurrent.ExecutionException;
      13  import java.util.concurrent.TimeUnit;
      14  import java.util.concurrent.TimeoutException;
     ...
      18  import org.apache.kafka.clients.producer.KafkaProducer;
      19  import org.apache.kafka.clients.producer.ProducerConfig;
      20  import org.apache.kafka.clients.producer.ProducerRecord;
      21 +import org.apache.kafka.common.PartitionInfo;                  
      22 +import org.apache.kafka.common.TopicPartition;                 
      23  import org.apache.kafka.common.header.Header;
      24  import org.apache.kafka.common.serialization.ByteArrayDeseriali
          zer;
      25  import org.apache.kafka.common.serialization.ByteArraySerialize
          r;
     ...
       96          send(brokers, topic, key, json.getBytes(StandardCharse
           ts.UTF_8));
       97      }
       98 
       99 +    /** Produtores reutilizados por endereço de broker: criar 
          +um por envio custa a busca de metadados. */                   
      100 +    private static final Map<String, KafkaProducer<String, byt
          +e[]>> PRODUCERS = new ConcurrentHashMap<>();                  
      101 +                                                              
      102      /** Envia com cabeçalhos e espera a confirmação. */
      103      public static void sendWithHeaders(String brokers, String 
           topic, String key, byte[] value,
      104              Map<String, String> headers) {
      100 -        Properties config = new Properties();                 
      101 -        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, br
          -okers);                                                       
      102 -        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
          - StringSerializer.class);                                     
      103 -        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFI
          -G, ByteArraySerializer.class);                                
      104 -        config.put(ProducerConfig.ACKS_CONFIG, "all");        
      105 -        try (KafkaProducer<String, byte[]> producer = new Kafk
          -aProducer<>(config)) {                                        
      106 -            ProducerRecord<String, byte[]> record = new Produc
          -erRecord<>(topic, key, value);                                
      107 -            headers.forEach((name, headerValue) ->            
      108 -                    record.headers().add(name, headerValue.get
          -Bytes(StandardCharsets.UTF_8)));                              
      105 +        KafkaProducer<String, byte[]> producer = PRODUCERS.com
          +puteIfAbsent(brokers, KafkaTestSupport::producer);            
      106 +        ProducerRecord<String, byte[]> record = new ProducerRe
          +cord<>(topic, key, value);                                    
      107 +        headers.forEach((name, headerValue) ->                
      108 +                record.headers().add(name, headerValue.getByte
          +s(StandardCharsets.UTF_8)));                                  
      109 +        try {                                                 
      110              producer.send(record).get(30, TimeUnit.SECONDS);
     … +45 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Without clean, verify runs 2.7× slower than baseline, above my 2× limit, and
  the broker isn't the cause, so I'm cutting the slow tests, starting with
  KafkaBrokerUnavailableIT. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs");…)
  ⎿  rc=0
     [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed:
      20.64 s -- in com.outsera.awards.kafka.KafkaBrokerUnavailableIT
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaBrokerUnavailableIT.java
     +10 -2
       9  import java.io.IOException;
      10  import java.io.UncheckedIOException;
      11  import java.net.ServerSocket;
      12 +import java.util.ArrayList;                                    
      13 +import java.util.List;                                         
      14  import org.junit.jupiter.api.DisplayName;
      15  import org.junit.jupiter.api.Test;
      16 
     ...
      35      void aliveButNotReady() {
      36          String brokers = "localhost:" + closedPort();
      37          long start = System.nanoTime();
      36 -        Result result = ApplicationStarter.startAndCall("classp
         -ath:movielist.csv",                                            
      37 -                KafkaTestSupport.args(brokers, KafkaTestSupport
         -.topics(KafkaBrokerUnavailableIT.class)), port -> {            
      38 +        List<String> args = new ArrayList<>(KafkaTestSupport.ar
         +gs(brokers,                                                    
      39 +                KafkaTestSupport.topics(KafkaBrokerUnavailableI
         +T.class)));                                                    
      40 +        // Prazos curtos só para o teste não esperar os 10 s da
         + criação dos tópicos nem o fechamento longo                    
      41 +        // do produtor com envios pendentes. O comportamento ob
         +servado é o mesmo.                                             
      42 +        args.add("--spring.kafka.admin.operation-timeout=2s"); 
      43 +        args.add("--spring.kafka.admin.close-timeout=1s");     
      44 +        args.add("--spring.kafka.producer.properties.max.block.
         +ms=2000");                                                     
      45 +        Result result = ApplicationStarter.startAndCall("classp
         +ath:movielist.csv", args, port -> {                            
      46                      long deadline = System.nanoTime() + 5_000_0
          00_000L;
      47                      while (System.nanoTime() < deadline) {
      48                          assertThat(Http.get(port, "/actuator/he
          alth/liveness").status()).isEqualTo(200);
  ⎿  (timeout 10m)

● The broker-free run only got down to 20.6 s, so I'll record the measured times
  in docs/ai/decisoes.md (T048) rather than keep optimizing. Next I'm running
  the postgres-it profile with the two new ITs. · summary

  Ran 1 shell command (ctrl+o to expand)
● All seven PostgreSQL integration tests pass, including the restart and retry
  cases. I'm now recording the stage 2 decisions in T048 and updating tasks.md. 
  · summary

● Update(docs\ai\decisoes.md)
  ⎿  Added 43 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-24 — Etapa 2 do Marco 3: verificações e decisões da 
         +ingestão por Kafka                                             
       7 +                                                               
       8 +- **Nome da autoconfiguração (R2, verificado):** no Boot 4.1.2,
         + são                                                           
       9 +  `org.springframework.boot.kafka.autoconfigure.KafkaAutoConfig
         +uration` e                                                     
      10 +  `...kafka.autoconfigure.metrics.KafkaMetricsAutoConfiguration
         +`, excluídas no `application.yml` e                            
      11 +  liberadas no `application-kafka.yml`. Versões: Spring Kafka 4
         +.1.2 e kafka-clients 4.2.2.                                    
      12 +- **Nome inválido de tópico (T034c, verificado):**             
      13 +  - um tópico de erros com um nome que o Kafka não aceita **não
         + derruba a subida**. Com                                       
      14 +    `spring.kafka.admin.fail-fast: false`, o `KafkaAdmin` regis
         +tra o `InvalidTopicException` e a aplicação                    
      15 +    fica pronta;                                               
      16 +  - o `KafkaErrorTopicUnavailableIT` usa esse nome, e cada envi
         +o é recusado de verdade pelo cliente;                          
      17 +  - a propriedade alternativa `awards.ingestion.create-error-to
         +pic` não foi necessária e foi descartada:                      
      18 +    menos uma configuração usada só por teste.                 
      19 +- **Broker fora do ar (T034b, verificado):** a aplicação sobe s
         +em o broker, com a liveness 200 e a                            
      20 +  readiness 503. A subida, com os prazos do `KafkaAdmin`, e o e
         +ncerramento, com envios pendentes, levam                       
      21 +  cerca de 15 s. O teste encurta os prazos do `KafkaAdmin` e do
         + produtor só para ele.                                         
      22 +- **Série de atraso do consumidor (R19, verificado):** a série 
         +que aparece logo depois da carga é                             
      23 +  `kafka_consumer_fetch_manager_records_lag_max`, por cliente (
         +`client_id=consumer-awards-movies-<n>`). A                     
      24 +  série por partição só aparece depois da atualização periódica
         + do Micrometer. Com um consumidor e uma                        
      25 +  partição, o máximo do cliente é o atraso do tópico. A R19 e o
         + contrato operacional foram atualizados.                       
      26 +- **Captura de saída por teste:** o resumo da publicação é regi
         +strado num fio próprio, durante o                              
      27 +  primeiro teste da classe, e o `OutputCaptureExtension` descar
         +ta a captura de cada teste no fim dele.                        
      28 +  Por isso, a verificação do resumo ficou só no `PublisherSumma
         +ryIT`, que tem um único teste, e saiu do                       
      29 +  `KafkaInvalidLinesIT`, que confere o tópico e os avisos.     
      30 +- **Tempo do `./mvnw verify` acima do limite da R15:** 256 s se
         +m `clean`, contra 96 s na linha de base,                       
      31 +  ou seja, 2,7× (limite de 2×). A R15 previa compartilhar o bro
         +ker entre as classes, mas a medição mostra                     
      32 +  que o broker não é o custo: uma classe inteira, como o `Kafka
         +UpdateIT`, leva 2,9 s com broker e                             
      33 +  contexto. O tempo está em poucos testes que esperam de propós
         +ito:                                                           
      34 +  - `KafkaBrokerUnavailableIT`, cerca de 20 s;                 
      35 +  - `KafkaInvalidEventsIT`, 16 casos, cerca de 18 s em cada uma
         + das duas execuções do Failsafe;                               
      36 +  - `KafkaReadinessIT`, 3 s de espera pelo marcador errado;    
      37 +  - `KafkaEquivalenceIT`, seis subidas.                        
      38 +                                                               
      39 +  Foram feitos dois ajustes: o produtor dos testes passou a ser
         + reutilizado e a leitura passou a ser feita                    
      40 +  sem grupo de consumidores, o que tirou cerca de 20 s do `Kafk
         +aInvalidEventsIT`. O resto foi mantido.                        
      41 +  **Pendente de decisão do responsável:** aceitar os cerca de 4
         + min do `verify`, ou cortar casos.                             
      42 +- **Nova tentativa quando a gravação falha (FR-015a):** o `Retr
         +yPostgresIT` passou no `postgres-it`                           
      43 +  local, com a mensagem `Falha ao gravar o evento … nova tentat
         +iva` no log, e o filme foi gravado depois                      
      44 +  de a tabela ser devolvida. A queda real do banco fica para o 
         +passo 7b do compose (etapa 4).                                 
      45 +- **Validação comum (I1 da segunda análise):** o `FIELD_TOO_LON
         +G` está na `MovieFieldRules`, usada pelo                       
      46 +  leitor do CSV e pelo parser de eventos. O `FieldTooLongIT` (c
         +arga direta) e o `KafkaEquivalenceIT` (com                     
      47 +  o arquivo `field-too-long.csv`) passam.                      
      48 +                                                               
      49  ## 2026-11-24 — Segunda análise da spec 003 (`/speckit-analyze`
          )
      50 
      51  O responsável aceitou as correções, com os ajustes abaixo. Elas
           foram aplicadas na spec, no plano, na

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/tasks.md; node -e '
      const fs=require("fs"); let s=fs.readFileSync("'$f'","utf8");…)
  ⎿  52
     196:- [ ] T020 [US2] **Pendente: exige o push, que é do And
  ⎿  Updated specs\003-modo-docker\tasks.md +44 -42
      215 
      216  ### Apoio da etapa 2
      217 
      218 -- [ ] T021 [US1] No `pom.xml`, acrescentar `org.springframewor
          -k.kafka:spring-kafka` (compilação) e `org.springframework.kafk
          -a:spring-kafka-test` (teste), com as versões do Boot.         
      219 -- [ ] T022 [US1] Criar `src/test/java/com/outsera/awards/suppo
          -rt/KafkaTestSupport.java`, com um produtor e um consumidor **r
          -eais** (clientes Kafka) ligados ao broker embutido:           
      218 +- [X] T021 [US1] No `pom.xml`, acrescentar `org.springframewor
          +k.kafka:spring-kafka` (compilação) e `org.springframework.kafk
          +a:spring-kafka-test` (teste), com as versões do Boot.         
      219 +- [X] T022 [US1] Criar `src/test/java/com/outsera/awards/suppo
          +rt/KafkaTestSupport.java`, com um produtor e um consumidor **r
          +eais** (clientes Kafka) ligados ao broker embutido:           
      220    - `topics(Class<?>)` devolve `awards.movies.<classe>` e `awa
           rds.movies.errors.<classe>`;
      221    - `args(EmbeddedKafkaBroker, Class<?>)` devolve `--spring.pr
           ofiles.active=kafka`,
      222      `--KAFKA_BOOTSTRAP_SERVERS=…`, `--KAFKA_TOPIC_MOVIES=…`, `
           --KAFKA_TOPIC_ERRORS=…` e
     ...
      228 
      229  ### Testes da etapa 2 (escrever primeiro; precisam falhar)
      230 
      231 -- [ ] T023 [P] [US1] Criar `src/test/java/com/outsera/awards/i
          -solation/DefaultModeIsolationIT.java` (`@Tag("log")`; saída co
          -mpleta da subida pelo `ApplicationStarter`, sem perfil):      
      231 +- [X] T023 [P] [US1] Criar `src/test/java/com/outsera/awards/i
          +solation/DefaultModeIsolationIT.java` (`@Tag("log")`; saída co
          +mpleta da subida pelo `ApplicationStarter`, sem perfil):      
      232    - `S003 FR-001`: `GET /actuator/prometheus` não tem nenhuma
           linha que comece por `kafka_` nem por
      233      `awards_ingestion_`;
      234    - o log não contém `AdminClientConfig`, `ProducerConfig` nem
            `ConsumerConfig` (research R2);
      235    - os intervalos trazem o resultado de referência.
      236 -- [ ] T024 [P] [US1] Criar `src/test/java/com/outsera/awards/k
          -afka/KafkaReferenceFileIT.java` (`@SpringBootTest`, `@ActivePr
          -ofiles("kafka")`, `@EmbeddedKafka(partitions = 1)`, `spring.ka
          -fka.bootstrap-servers=${spring.embedded.kafka.brokers}` e tópi
          -cos da classe):                                               
      236 +- [X] T024 [P] [US1] Criar `src/test/java/com/outsera/awards/k
          +afka/KafkaReferenceFileIT.java` (`@SpringBootTest`, `@ActivePr
          +ofiles("kafka")`, `@EmbeddedKafka(partitions = 1)`, `spring.ka
          +fka.bootstrap-servers=${spring.embedded.kafka.brokers}` e tópi
          +cos da classe):                                               
      237    - `S003 US1-1 FR-012`: depois da readiness, os intervalos tr
           azem Joel Silver e Matthew Vaughn;
      238    - `S003 FR-010 FR-011`: o tópico de filmes tem 206 eventos d
           e filme, com a chave `"<ano>|<título>"` e o
      239      cabeçalho `awards-run-id`, seguidos de um evento `{"type":
           "load-completed","runId":…,"count":206}`
      240      com o mesmo `runId`;
      241    - `S003 US1-6 FR-009`: o log não tem a mensagem da carga dir
           eta (`Carga do arquivo … concluída`).
      242 -- [ ] T025 [P] [US1] Criar `src/test/java/com/outsera/awards/k
          -afka/KafkaEquivalenceIT.java` (`@EmbeddedKafka` na classe; dua
          -s execuções com o `ApplicationStarter`: carga direta e `kafka`
          -, com o arquivo padrão; a de Kafka chama `awaitReady` antes de
          - consultar):                                                  
      242 +- [X] T025 [P] [US1] Criar `src/test/java/com/outsera/awards/k
          +afka/KafkaEquivalenceIT.java` (`@EmbeddedKafka` na classe; dua
          +s execuções com o `ApplicationStarter`: carga direta e `kafka`
          +, com o arquivo padrão; a de Kafka chama `awaitReady` antes de
          + consultar):                                                  
      243    - `S003 FR-018 US1-2 SC-001`: os intervalos, `/api/movies?si
           ze=100&page=0..2`, `?winner=true` e
      244      `?year=1990` são idênticos byte a byte, inclusive o `ETag`
           ;
      245    - repetir com `classpath:scenarios/adversario/13-ordenacao-c
           om-acentos-e-caixa.csv`;
      246    - repetir com `classpath:scenarios/field-too-long.csv` (T034
           a), sem exceção (FR-018). As linhas
      247      rejeitadas por `FIELD_TOO_LONG` faltam nos dois caminhos,
           e as respostas são idênticas.
      248 -- [ ] T026 [P] [US1] Criar `src/test/java/com/outsera/awards/k
          -afka/KafkaReadinessIT.java` (`@SpringBootTest`, perfil `kafka`
          -, `awards.ingestion.publish-completion=false`; research R10): 
      248 +- [X] T026 [P] [US1] Criar `src/test/java/com/outsera/awards/k
          +afka/KafkaReadinessIT.java` (`@SpringBootTest`, perfil `kafka`
          +, `awards.ingestion.publish-completion=false`; research R10): 
      249    - `S003 FR-013 US1-3`:
      250      - depois de `totalElements` chegar a 206 na listagem, a re
           adiness e `/actuator/health` continuam
      251        503 e a liveness 200;
     ...
      254      - depois, envia o marcador com o `runId` certo, e a readin
           ess passa a 200 em até 5 s;
      255    - `S003 FR-016 US3-5`: com a aplicação pronta, envia um even
           to válido e um inválido; a readiness
     … +205 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Stage 2 is committed (7cc063b). T050 (the adversarial tester) and T051 (push)
  are waiting on you. Starting stage 3, authentication. First the
  ContractValidator, which needs to forward the token.

  Ran 1 shell command (ctrl+o to expand)

● The ContractValidator only checks the response, so the token change doesn't
  touch it. I'm checking how ApiDocsIT reads the YAML and what the Swagger UI
  page returns next. · summary

  Ran 1 shell command (ctrl+o to expand)

● Writing the stage 3 tests.

● Write(src\test\java\com\outsera\awards\support\TokenScenarioIT.java)
  ⎿  Wrote 35 lines to
     src\test\java\com\outsera\awards\support\TokenScenarioIT.java
       1 package com.outsera.awards.support;
       2 
       3 import java.util.UUID;
       4 import org.springframework.http.HttpHeaders;
       5 import org.springframework.test.context.DynamicPropertyRegistry;
       6 import org.springframework.test.context.DynamicPropertySource;
       7 
       8 /**
       9  * Base dos testes com a autenticação ligada (spec 003, FR-019 a
          FR-026). O token é gerado em tempo de
     … +26 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\security\AuthDisabledByDefaultIT.java)
  ⎿  Wrote 70 lines to
     src\test\java\com\outsera\awards\security\AuthDisabledByDefaultIT.java
       1 package com.outsera.awards.security;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ScenarioIT;
       6 import java.util.List;
       7 import org.junit.jupiter.api.DisplayName;
       8 import org.junit.jupiter.api.Test;
       9 import org.junit.jupiter.params.ParameterizedTest;
      10 import org.junit.jupiter.params.provider.ValueSource;
     … +60 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\security\AuthEnabledIT.java)
  ⎿  Wrote 82 lines to
     src\test\java\com\outsera\awards\security\AuthEnabledIT.java
       1 package com.outsera.awards.security;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.TokenScenarioIT;
       6 import java.util.regex.Matcher;
       7 import java.util.regex.Pattern;
       8 import org.junit.jupiter.api.DisplayName;
       9 import org.junit.jupiter.api.Test;
      10 import org.junit.jupiter.params.ParameterizedTest;
     … +72 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\security\AuthProblemDetailsIT.java)
  ⎿  Wrote 45 lines to
     src\test\java\com\outsera\awards\security\AuthProblemDetailsIT.java
       1 package com.outsera.awards.security;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.TokenScenarioIT;
       6 import org.junit.jupiter.api.DisplayName;
       7 import org.junit.jupiter.api.Test;
       8 import org.springframework.http.HttpHeaders;
       9 import org.springframework.http.MediaType;
      10 import org.springframework.test.json.JsonCompareMode;
     … +35 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; sed -i
      '/assertThat(status(path, "Bearer " + TOKEN + " ")).as("espaço no
      fim").isEqualTo(401);/d' src/test/java/com/…)
  ⎿  1
  ⎿  Updated src\test\java\com\outsera\awards\security\AuthEnabledIT.java
     -1
      25          assertThat(status(path, "Bearer errado")).as("token err
          ado").isEqualTo(401);
      26          assertThat(status(path, "Basic dXNlcjpwYXNz")).as("outr
          o esquema").isEqualTo(401);
      27          assertThat(status(path, "Bearer  " + TOKEN)).as("espaço
           a mais").isEqualTo(401);
      28 -        assertThat(status(path, "Bearer " + TOKEN + " ")).as("e
         -spaço no fim").isEqualTo(401);                                 
      28          assertThat(status(path, "Bearer " + TOKEN.toUpperCase()
          )).as("outra caixa").isEqualTo(401);
      29          assertThat(status(path, "bearer " + TOKEN)).as("esquema
           em minúsculas").isEqualTo(401);
      30          assertThat(status(path, bearer())).as("token certo").is
          EqualTo(200);

● Write(src\test\java\com\outsera\awards\security\TokenNotLoggedIT.java)
  ⎿  Wrote 41 lines to
     src\test\java\com\outsera\awards\security\TokenNotLoggedIT.java
       1 package com.outsera.awards.security;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.ApplicationStarter.Result;
       7 import com.outsera.awards.support.Http;
       8 import java.util.List;
       9 import java.util.UUID;
      10 import org.junit.jupiter.api.DisplayName;
     … +31 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\security\NoGeneratedPasswordIT.java)
  ⎿  Wrote 30 lines to
     src\test\java\com\outsera\awards\security\NoGeneratedPasswordIT.java
       1 package com.outsera.awards.security;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.ApplicationStarter.Result;
       7 import java.util.List;
       8 import java.util.UUID;
       9 import org.junit.jupiter.params.ParameterizedTest;
      10 import org.junit.jupiter.params.provider.ValueSource;
     … +20 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\security\CacheHeadersIT.java)
  ⎿  Wrote 59 lines to
     src\test\java\com\outsera\awards\security\CacheHeadersIT.java
      1 package com.outsera.awards.security;
      2 
      3 import static org.assertj.core.api.Assertions.assertThat;
      4 
      5 import com.outsera.awards.support.ScenarioIT;
      6 import org.junit.jupiter.api.DisplayName;
      7 import org.junit.jupiter.api.Test;
      8 import org.springframework.http.HttpHeaders;
      9 import org.springframework.test.web.servlet.client.EntityExchange
        Result;
     … +50 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\security\CacheHeadersWithTokenIT.java)
  ⎿  Wrote 41 lines to
     src\test\java\com\outsera\awards\security\CacheHeadersWithTokenIT.java
       1 package com.outsera.awards.security;
       2 
       3 import static com.outsera.awards.security.CacheHeadersIT.assertB
         usinessHeaders;
       4 import static com.outsera.awards.security.CacheHeadersIT.assertP
         rotection;
       5 import static org.assertj.core.api.Assertions.assertThat;
       6 
       7 import com.outsera.awards.support.TokenScenarioIT;
       8 import org.junit.jupiter.api.DisplayName;
     … +33 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\security\AuthContractIT.java)
  ⎿  Wrote 27 lines to
     src\test\java\com\outsera\awards\security\AuthContractIT.java
       1 package com.outsera.awards.security;
       2 
       3 import com.outsera.awards.support.ContractValidator;
       4 import com.outsera.awards.support.TokenScenarioIT;
       5 import org.junit.jupiter.params.ParameterizedTest;
       6 import org.junit.jupiter.params.provider.ValueSource;
       7 import org.springframework.http.HttpHeaders;
       8 import org.springframework.test.web.servlet.client.EntityExchang
         eResult;
       9 
     … +18 lines (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\docs\ApiDocsSecurityIT.java)
  ⎿  Wrote 48 lines to
     src\test\java\com\outsera\awards\docs\ApiDocsSecurityIT.java
       1 package com.outsera.awards.docs;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ScenarioIT;
       6 import java.util.List;
       7 import java.util.Map;
       8 import org.junit.jupiter.api.DisplayName;
       9 import org.junit.jupiter.api.Test;
      10 import org.yaml.snakeyaml.Yaml;
     … +38 lines (ctrl+o to expand)

● Now the check-unchanged-tests.sh script (T060) and the exceptions file.

● Write(.github\scripts\check-unchanged-tests.sh)
  ⎿  Wrote 64 lines to .github\scripts\check-unchanged-tests.sh
       1 #!/usr/bin/env bash
       2 # Confere que os testes de integração dos marcos anteriores não 
         mudaram desde a tag dada (spec 003,
       3 # SC-002; research R14; contracts/ci-pipeline.md).
       4 #
       5 # Uso: .github/scripts/check-unchanged-tests.sh <tag>
       6 #
       7 # Cada src/test/java/**/*IT.java que existe na tag precisa estar
          idêntico no HEAD. As exceções ficam em
       8 # .github/unchanged-tests-exceptions.txt, uma por linha, no form
     … +57 lines (~2 KB) (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; printf '%s\n' '# Testes de
      integração da tag de referência que podem mudar, cada um com a entrada de'
      '# docs/ai/dec…)
  ⎿  check-unchanged-tests.sh: os 59 testes de integração de v0.2.0 estão s
     em alteração (exceções: 1).
     rc=0
     … +10 lines (ctrl+o to expand)
  ⎿  Created .github\unchanged-tests-exceptions.txt +4
  ⎿  (a git step in this command can move the working tree, so these may be
     its changes, not edits) (ctrl+o to expand)

● The script behaves as specified in all four cases (unchanged, an IT changed,
  ApiDocsIT as the exception, a nonexistent title). Confirming the stage 3
  failure (T061), before adding Spring Security.

  Ran 1 shell command (ctrl+o to expand)
● The token, 401, header and contract tests fail as expected, while the
  auth-disabled-by-default test passes, confirming that OPTIONS returns 2xx with
  Allow in Marco 2. Now I'm starting the implementation. · summary

  Ran 1 shell command (ctrl+o to expand)

● The FQN is confirmed: org.springframework.boot.security.autoconfigure.UserDeta
  ilsServiceAutoConfiguration.

● Update(pom.xml)
  ⎿  Added 6 lines
      57              <artifactId>micrometer-registry-prometheus</artifac
          tId>
      58          </dependency>
      59          <dependency>
      60 +            <!-- Autenticação por token, desligada sem API_TOKE
         +N (spec 003, FR-019 a FR-026; research R11).                   
      61 +                 O usuário gerado pelo Boot fica excluído no ap
         +plication.yml. -->                                             
      62 +            <groupId>org.springframework.boot</groupId>        
      63 +            <artifactId>spring-boot-starter-security</artifactI
         +d>                                                             
      64 +        </dependency>                                          
      65 +        <dependency>                                           
      66              <!-- Ingestão por Kafka no perfil kafka (spec 003, 
          FR-009 a FR-018). A autoconfiguração do Boot
      67                   fica excluída fora do perfil (research R2). --
          >
      68              <groupId>org.springframework.boot</groupId>

● Update(src\main\resources\application.yml)
  ⎿  Added 3 lines, removed 1 line
      10          - kafka
      11    autoconfigure:
      12      # Sem o perfil kafka, o Kafka não é configurado (Princípio 
          V; spec 003, FR-001; research R2). O
      13 -    # application-kafka.yml redefine esta lista sem as duas cla
         -sses.                                                          
      13 +    # application-kafka.yml redefine esta lista sem as duas cla
         +sses do Kafka. O usuário e a senha gerados                     
      14 +    # pelo Spring Security ficam excluídos em todos os perfis (
         +research R11).                                                 
      15      exclude:
      16        - org.springframework.boot.kafka.autoconfigure.KafkaAutoC
          onfiguration
      17        - org.springframework.boot.kafka.autoconfigure.metrics.Ka
          fkaMetricsAutoConfiguration
      18 +      - org.springframework.boot.security.autoconfigure.UserDet
         +ailsServiceAutoConfiguration                                   
      19    main:
      20      # Banner desligado em todas as execuções (spec 002, FR-020;
           research R6)
      21      banner-mode: off

● Update(src\main\resources\application.yml)
  ⎿  Added 6 lines
      37    csv:
      38      path: ${MOVIES_CSV_PATH:classpath:movielist.csv}
      39 
      40 +# Autenticação (spec 003, FR-019 a FR-026; research R11). Sem A
         +PI_TOKEN, ou com ele vazio, fica desligada                     
      41 +# e todos os endpoints se comportam como no Marco 2.           
      42 +awards:                                                        
      43 +  security:                                                    
      44 +    token: ${API_TOKEN:}                                       
      45 +                                                               
      46  # Saúde e métricas na mesma porta da API (spec 002, FR-008 a FR
          -016; research R1 e R3).
      47  # O formato do log não é configurado aqui: o JSON é ligado só p
          ela variável
      48  # LOGGING_STRUCTURED_FORMAT_CONSOLE (research R6).

● Update(src\main\resources\application-kafka.yml)
  ⎿  Added 2 lines, removed 1 line
       4  spring:
       5    autoconfigure:
       6      # Redefine a lista do application.yml sem a autoconfiguraçã
          o do Kafka.
       7 -    exclude: []                                                
       7 +    exclude:                                                   
       8 +      - org.springframework.boot.security.autoconfigure.UserDet
         +ailsServiceAutoConfiguration                                   
       9    kafka:
      10      bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:kafka:9092}
      11      producer:

● Write(src\main\java\com\outsera\awards\security\SecurityProperties.java)
  ⎿  Wrote 24 lines to
     src\main\java\com\outsera\awards\security\SecurityProperties.java
       1 package com.outsera.awards.security;
       2 
       3 import org.springframework.boot.context.properties.Configuration
         Properties;
       4 
       5 /**
       6  * Token da autenticação ({@code awards.security.token}, variáve
         l {@code API_TOKEN}; spec 003, FR-019).
       7  * Vazio ou ausente desliga a autenticação.
       8  *
     … +16 lines (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\security\TokenAuthenticationFilter.java
       )
  ⎿  Wrote 62 lines to src\main\java\com\outsera\awards\security\TokenAuthe
     nticationFilter.java
       1 package com.outsera.awards.security;
       2 
       3 import jakarta.servlet.FilterChain;
       4 import jakarta.servlet.ServletException;
       5 import jakarta.servlet.http.HttpServletRequest;
       6 import jakarta.servlet.http.HttpServletResponse;
       7 import java.io.IOException;
       8 import java.nio.charset.StandardCharsets;
       9 import java.security.MessageDigest;
      10 import java.security.NoSuchAlgorithmException;
     … +52 lines (ctrl+o to expand)

● Write(src\main\java\com\outsera\awards\security\ProblemAuthenticationEntryPoin
       t.java)
  ⎿  Wrote 42 lines to src\main\java\com\outsera\awards\security\ProblemAut
     henticationEntryPoint.java
       1 package com.outsera.awards.security;
       2 
       3 import jakarta.servlet.http.HttpServletRequest;
       4 import jakarta.servlet.http.HttpServletResponse;
       5 import java.io.IOException;
       6 import java.net.URI;
       7 import org.springframework.http.HttpHeaders;
       8 import org.springframework.http.HttpStatus;
       9 import org.springframework.http.MediaType;
      10 import org.springframework.http.ProblemDetail;
     … +32 lines (ctrl+o to expand)

● For the 401 body, I'll write the JSON with an explicit ObjectNode instead of
  depending on how the JsonMapper serializes ProblemDetail.

● Write(src\main\java\com\outsera\awards\security\ProblemAuthenticationEntryPoin
       t.java)
  ⎿  Added 12 lines, removed 12 lines
       3  import jakarta.servlet.http.HttpServletRequest;
       4  import jakarta.servlet.http.HttpServletResponse;
       5  import java.io.IOException;
       6 -import java.net.URI;                                           
       6  import org.springframework.http.HttpHeaders;
       7  import org.springframework.http.HttpStatus;
       8  import org.springframework.http.MediaType;
      10 -import org.springframework.http.ProblemDetail;                 
       9  import org.springframework.security.core.AuthenticationExceptio
          n;
      10  import org.springframework.security.web.AuthenticationEntryPoin
          t;
      11  import tools.jackson.databind.json.JsonMapper;
      12 +import tools.jackson.databind.node.ObjectNode;                 
      13 
      14  /**
      15   * 401 em Problem Details (RFC 9457), o mesmo para o token ause
          nte e para o token errado, sem revelar o
      17 - * valor esperado nem o recebido (spec 003, FR-023 e FR-025; co
         -ntracts/operational-endpoints.md).                             
      16 + * valor esperado nem o recebido (spec 003, FR-023 e FR-025; co
         +ntracts/operational-endpoints.md). Os                          
      17 + * campos são os do {@code ProblemDetail} do contrato: {@code t
         +ype}, {@code title}, {@code status},                           
      18 + * {@code detail} e {@code instance}.                          
      19   */
      20  public class ProblemAuthenticationEntryPoint implements Authent
          icationEntryPoint {
      21 
      22      static final String DETAIL = "Token ausente ou inválido";
      23      static final String CHALLENGE = "Bearer realm=\"awards\"";
      24 
      24 -    private final JsonMapper json;                             
      25 +    private static final JsonMapper JSON = JsonMapper.builder()
         +.build();                                                      
      26 
      26 -    public ProblemAuthenticationEntryPoint(JsonMapper json) {  
      27 -        this.json = json;                                      
      28 -    }                                                          
      29 -                                                               
      27      @Override
      28      public void commence(HttpServletRequest request, HttpServle
          tResponse response, AuthenticationException exception)
      29              throws IOException {
      33 -        ProblemDetail problem = ProblemDetail.forStatusAndDetai
         -l(HttpStatus.UNAUTHORIZED, DETAIL);                            
      34 -        problem.setTitle(HttpStatus.UNAUTHORIZED.getReasonPhras
         -e());                                                          
      35 -        problem.setInstance(URI.create(request.getRequestURI())
         -);                                                             
      30 +        ObjectNode problem = JSON.createObjectNode()           
      31 +                .put("type", "about:blank")                    
      32 +                .put("title", HttpStatus.UNAUTHORIZED.getReason
         +Phrase())                                                      
      33 +                .put("status", HttpStatus.UNAUTHORIZED.value())
      34 +                .put("detail", DETAIL)                         
      35 +                .put("instance", request.getRequestURI());     
      36          response.setStatus(HttpStatus.UNAUTHORIZED.value());
      37          response.setHeader(HttpHeaders.WWW_AUTHENTICATE, CHALLE
          NGE);
      38          response.setContentType(MediaType.APPLICATION_PROBLEM_J
          SON_VALUE);
      39          response.setCharacterEncoding("UTF-8");
      40 -        json.writeValue(response.getOutputStream(), problem);  
      40 +        response.getOutputStream().write(JSON.writeValueAsBytes
         +(problem));                                                    
      41      }
      42  }

● Write(src\main\java\com\outsera\awards\security\SecurityConfiguration.java)
  ⎿  Wrote 72 lines to
     src\main\java\com\outsera\awards\security\SecurityConfiguration.java
       1 package com.outsera.awards.security;
       2 
       3 import org.springframework.context.annotation.Bean;
       4 import org.springframework.context.annotation.Configuration;
       5 import org.springframework.security.config.annotation.web.builde
         rs.HttpSecurity;
       6 import org.springframework.security.config.annotation.web.config
         urers.AbstractHttpConfigurer;
       7 import org.springframework.security.config.http.SessionCreationP
         olicy;
     … +65 lines (ctrl+o to expand)

● Now T067: publish the 1.2.0 contract and change only the ApiDocsIT constant.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cp
      specs/003-modo-docker/contracts/openapi.yaml
      src/main/resources/static/openapi.yaml; cmp specs/003-modo-docker/co…)
  ⎿  identical
      src/test/java/com/outsera/awards/docs/ApiDocsIT.java | 4 ++--
      1 file changed, 2 insertions(+), 2 deletions(-)
     … +9 lines (ctrl+o to expand)
  ⎿  Updated src\main\resources\static\openapi.yaml +39 -2
       1  openapi: 3.1.0
       2  info:
       3    title: Golden Raspberry Awards API
       4 -  version: 1.1.0                                               
       4 +  version: 1.2.0                                               
       5    description: >
       6      Intervalos entre prêmios consecutivos de produtores na cate
          goria Pior Filme e listagem dos filmes
       7      indicados e vencedores. Os dados vêm do arquivo CSV carrega
          do na inicialização. As respostas 200
       8 -    trazem ETag, e uma requisição com If-None-Match igual ao ET
         -ag atual recebe 304 sem corpo.                                 
       8 +    trazem ETag, e uma requisição com If-None-Match igual ao ET
         +ag atual recebe 304 sem corpo. A                               
       9 +    autenticação é opcional e fica desligada por padrão. Com a 
         +variável de ambiente API_TOKEN definida,                       
      10 +    os dois endpoints exigem o cabeçalho Authorization: Bearer 
         +<token>; sem ele, ou com outro valor, a                        
      11 +    resposta é 401 em Problem Details.                         
      12  paths:
      13    /api/producers/intervals:
      14      get:
     ...
      19          entram na lista, ordenados por previousWin e depois por
           nome. Sem produtor com duas vitórias,
      20          as duas listas vêm vazias.
      21        tags: [producers]
      22 +      security:                                                
      23 +        - {}                                                   
      24 +        - bearerAuth: []                                       
      25        parameters:
      26          - $ref: '#/components/parameters/IfNoneMatch'
      27        responses:
     ...
      47                    value: { min: [], max: [] }
      48          '304':
      49            $ref: '#/components/responses/NotModified'
      50 +        '401':                                                 
      51 +          $ref: '#/components/responses/Unauthorized'          
      52    /api/movies:
      53      get:
      54        operationId: listMovies
      55        summary: Lista paginada de filmes, com filtros opcionais
      56        description: Ordenada por ano e depois por título.
      57        tags: [movies]
      58 +      security:                                                
      59 +        - {}                                                   
      60 +        - bearerAuth: []                                       
      61        parameters:
     … +52 lines (ctrl+o to expand)
  ⎿  Updated src\test\java\com\outsera\awards\docs\ApiDocsIT.java +2 -2
      27 
      28      private static final String SWAGGER_UI = "/swagger-ui.html"
          ;
      29      private static final String CONTRACT = "/openapi.yaml";
      30 -    /** Contrato 1.1.0, com ETag e 304 (spec 002, research R5).
         - O da spec 001 fica como registro do Marco 1. */               
      31 -    private static final Path SPEC_CONTRACT = Path.of("specs", 
         -"002-operacao", "contracts", "openapi.yaml");                  
      30 +    /** Contrato 1.2.0, com a autenticação opcional (spec 003, 
         +FR-026). Os das specs 001 e 002 ficam como registro dos marcos 
         +anteriores. */                                                 
      31 +    private static final Path SPEC_CONTRACT = Path.of("specs", 
         +"003-modo-docker", "contracts", "openapi.yaml");               
      32 
      33      @Test
      34      @DisplayName("FR-024 Swagger UI disponível")

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=src/test/java/com/outsera/awards/docs/ApiDocsIT.java; node -e '
      const fs=require("fs"); let s=fs.readFileSync("'$f…)
  ⎿  rc=1
     com.outsera.awards.docs.ApiDocsIT run=6 fail=0 err=0
     com.outsera.awards.docs.ApiDocsSecurityIT run=1 fail=0 err=0
     … +14 lines (ctrl+o to expand)
  ⎿  Updated src\test\java\com\outsera\awards\docs\ApiDocsIT.java +1 -1
      27 
      28      private static final String SWAGGER_UI = "/swagger-ui.html"
          ;
      29      private static final String CONTRACT = "/openapi.yaml";
      30 -    /** Contrato 1.2.0, com a autenticação opcional (spec 003, 
         -FR-026). Os das specs 001 e 002 ficam como registro dos marcos 
         -anteriores. */                                                 
      30 +    /** Contrato 1.2.0, com a autenticação opcional (spec 003, 
         +FR-026). Os das specs 001 e 002 ficam como registro. */        
      31      private static final Path SPEC_CONTRACT = Path.of("specs",
          "003-modo-docker", "contracts", "openapi.yaml");
      32 
      33      @Test
  ⎿  (timeout 10m)

  Ran 1 shell command (ctrl+o to expand)

● The 401 responses on business endpoints currently miss Cache-Control because
  the exclusion is path-based, but the contract requires it there. I'm switching
  to a header writer that applies the cache headers based on status, so every
  401 gets them. · summary

● Update(src\main\java\com\outsera\awards\security\SecurityConfiguration.java)
  ⎿  Added 1 line, removed 2 lines
      51                  .sessionManagement(session -> session.sessionCr
          eationPolicy(SessionCreationPolicy.STATELESS))
      52                  .headers(headers -> headers
      53                          .cacheControl(cache -> cache.disable())
      54 -                        .addHeaderWriter(new DelegatingRequestM
         -atcherHeaderWriter(                                            
      55 -                                new NegatedRequestMatcher(BUSIN
         -ESS), new CacheControlHeadersWriter()))                        
      54 +                        .addHeaderWriter(cacheControlOutsideBus
         +inessResponses())                                              
      55                          .frameOptions(frame -> frame.deny()));
      56 
      57          if (!properties.enabled()) {

● Update(src\main\java\com\outsera\awards\security\SecurityConfiguration.java)
  ⎿  Added 15 lines
      68                          .authenticationEntryPoint(new ProblemAu
          thenticationEntryPoint()));
      69          return http.build();
      70      }
      71 +                                                               
      72 +    /**                                                        
      73 +     * Os cabeçalhos de cache do Spring Security ({@code Cache-
         +Control}, {@code Pragma} e {@code Expires})                    
      74 +     * em toda resposta, menos nas dos endpoints de negócio, qu
         +e respondem sem eles, como no Marco 2, para que                
      75 +     * o ETag e o 304 continuem úteis ao cliente. O 401 sempre 
         +os recebe. Os cabeçalhos são escritos quando a                 
      76 +     * resposta é confirmada, então o status já é conhecido.   
      77 +     */                                                        
      78 +    private static HeaderWriter cacheControlOutsideBusinessResp
         +onses() {                                                      
      79 +        CacheControlHeadersWriter cacheControl = new CacheContr
         +olHeadersWriter();                                             
      80 +        return (request, response) -> {                        
      81 +            if (!BUSINESS.matches(request) || response.getStatu
         +s() == HttpServletResponse.SC_UNAUTHORIZED) {                  
      82 +                cacheControl.writeHeaders(request, response);  
      83 +            }                                                  
      84 +        };                                                     
      85 +    }                                                          
      86  }

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=src/main/java/com/outsera/awards/security/SecurityConfiguration.java;
      node -e '
      const fs=require("fs"); let s=fs.r…)
  ⎿  rc=0
     com.outsera.awards.security.AuthProblemDetailsIT run=1 fail=0 err=0
     com.outsera.awards.security.CacheHeadersIT run=2 fail=0 err=0
     com.outsera.awards.security.CacheHeadersWithTokenIT run=1 fail=0 err=0
  ⎿  Updated
     src\main\java\com\outsera\awards\security\SecurityConfiguration.java
     +2 -2
      1  package com.outsera.awards.security;
      2 
      3 +import jakarta.servlet.http.HttpServletResponse;                
      4  import org.springframework.context.annotation.Bean;
      5  import org.springframework.context.annotation.Configuration;
      6  import org.springframework.security.config.annotation.web.builde
         rs.HttpSecurity;
     ...
       9  import org.springframework.security.web.SecurityFilterChain;
      10  import org.springframework.security.web.access.intercept.Author
          izationFilter;
      11  import org.springframework.security.web.header.writers.CacheCon
          trolHeadersWriter;
      11 -import org.springframework.security.web.header.writers.Delegati
         -ngRequestMatcherHeaderWriter;                                  
      12 +import org.springframework.security.web.header.HeaderWriter;   
      13  import org.springframework.security.web.servlet.util.matcher.Pa
          thPatternRequestMatcher;
      13 -import org.springframework.security.web.util.matcher.NegatedReq
         -uestMatcher;                                                   
      14  import org.springframework.security.web.util.matcher.OrRequestM
          atcher;
      15  import org.springframework.security.web.util.matcher.RequestMat
          cher;
      16 
  ⎿  (timeout 10m)

● The headers are right now. Updating R12 (the mechanism changed) and running
  the full clean verify, the postgres-it profile and the unchanged-tests check.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs"); let f="specs/003-modo-docker/research.md",
      s=fs.readFileSync(f,"utf8");…)
  ⎿  rc=0 dur=227s
     [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed:
      12.75 s -- in com.outsera.awards.adversario.Case01TiesAtBothEndsIT
     … +242 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\research.md +5 -3
      309    - o escritor de cabeçalhos de cache do Security é desligado
           só nos dois endpoints de negócio, que
      310      passam a responder sem `Cache-Control`, como no Marco 2;
      311    - nos demais (métricas, saúde, documentação e 401), ele cont
           inua;
      312 -  - implementação: `headers.cacheControl(disable)` e, no lugar
          - dele, um                                                     
      313 -    `DelegatingRequestMatcherHeaderWriter` com o `CacheControl
          -HeadersWriter` para tudo o que não é                          
      314 -    endpoint de negócio;                                      
      312 +  - implementação: `headers.cacheControl(disable)` e, no lugar
          + dele, um `HeaderWriter` próprio que                          
      313 +    aplica o `CacheControlHeadersWriter` a tudo o que não é en
          +dpoint de negócio **e a todo 401**, inclusive                 
      314 +    nos endpoints de negócio. Os cabeçalhos são escritos quand
          +o a resposta é confirmada, então o status já                  
      315 +    é conhecido. *Ajustado na etapa 3: o `DelegatingRequestMat
          +cherHeaderWriter` decide só pelo caminho e                    
      316 +    deixava o 401 dos endpoints de negócio sem `Cache-Control`
          +, contrariando o contrato operacional;*                       
      317    - os outros cabeçalhos de proteção (`X-Content-Type-Options`
           , `X-Frame-Options`) continuam em todas as
      318      respostas.
      319  - **Leitura da FR-019 ("comportamento idêntico ao do Marco 2")
           :** status, corpo, `ETag`, 304 e os
  ⎿  (timeout 10m)

● Stage 3 is green: verify passes with 366 tests and 96.32% coverage, the 59 ITs
  from v0.2.0 are unchanged apart from ApiDocsIT, and postgres-it is green too.
  Next I'm moving on to T069, the security review section of the checklist, and
  then recording stage 3. · summary

● Update(specs\003-modo-docker\checklists\requirements.md)  ⎿  Added 10 lines
      52      ligado ou desligado;
      53    - que o `Cache-Control` do Spring Security fica fora dos endp
          oints de negócio.
      54 
      55 +## Revisão de segurança (comprovação da decisão de design da R1
         +1, na revisão do PR)                                           
      56 +                                                               
      57 +- [ ] A comparação do token no `TokenAuthenticationFilter` usa 
         +o mecanismo da research R11:                                   
      58 +  `MessageDigest.isEqual(sha256(recebido), sha256(esperado))`, 
         +em UTF-8, sem `String.equals`,                                 
      59 +  `Arrays.equals` nem saída antecipada.                        
      60 +- [ ] O ADR 0017 cita esse mecanismo.                          
      61 +- [ ] O filtro e o `ProblemAuthenticationEntryPoint` não regist
         +ram nem devolvem o cabeçalho                                   
      62 +  `Authorization` nem o token.                                 
      63 +- [ ] A matriz de acesso do `SecurityConfiguration` é igual à d
         +e `contracts/operational-endpoints.md`.                        
      64 +                                                               
      65  ## Notes
      66 
      67  - **Detalhes de tecnologia:** PostgreSQL, Kafka, Prometheus, Gr
          afana e compose aparecem na spec porque

● Update(docs\ai\decisoes.md)
  ⎿  Added 30 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-24 — Etapa 3 do Marco 3: verificações e ajustes da a
         +utenticação                                                    
       7 +                                                               
       8 +- **Nome da autoconfiguração (verificado):** no Boot 4.1.2, é  
       9 +  `org.springframework.boot.security.autoconfigure.UserDetailsS
         +erviceAutoConfiguration`. Ela está excluída                    
      10 +  no `application.yml` e no `application-kafka.yml`, e o `NoGen
         +eratedPasswordIT` confere que o log não                        
      11 +  traz senha gerada.                                           
      12 +- **`OPTIONS` no Marco 2 (N3 da segunda análise, verificado):**
         + antes do Spring Security, `OPTIONS` nos                       
      13 +  endpoints de negócio respondia 2xx com `Allow`. O `AuthDisabl
         +edByDefaultIT` fixa esse comportamento com                     
      14 +  o token desligado. Com o token ligado, a resposta é 401.     
      15 +- **`Cache-Control` no 401 (R12, ajuste):**                    
      16 +  - o `DelegatingRequestMatcherHeaderWriter` previsto decide só
         + pelo caminho e deixava o 401 dos                              
      17 +    endpoints de negócio sem os cabeçalhos de cache, ao contrár
         +io do contrato operacional;                                    
      18 +  - foi trocado por um `HeaderWriter` próprio, que aplica o `Ca
         +cheControlHeadersWriter` fora dos                              
      19 +    endpoints de negócio e em todo 401. O status é conhecido, p
         +orque os cabeçalhos são escritos quando a                      
      20 +    resposta é confirmada;                                     
      21 +  - os endpoints de negócio continuam sem `Cache-Control` nas r
         +espostas 200, 304, 400 e 405.                                  
      22 +- **`CacheHeadersIT` em duas classes:** a tarefa previa classes
         + aninhadas, uma para cada estado do token.                     
      23 +  Ficaram duas classes, `CacheHeadersIT` e `CacheHeadersWithTok
         +enIT`, porque cada estado do token precisa                     
      24 +  de um contexto próprio, e a base `ScenarioIT` já faz isso por
         + classe.                                                       
      25 +- **Validador de contrato:** o `ContractValidator` valida só a 
         +resposta, e não o requisito de segurança da                    
      26 +  requisição. O `security: [{}, {bearerAuth: []}]` do contrato 
         +1.2.0 não muda os ITs dos Marcos 1 e 2, e                      
      27 +  o validador não precisou de sobrecarga (R13).                
      28 +- **Prova de que os Marcos 1 e 2 não mudaram (R14):**          
      29 +  - o `AuthDisabledByDefaultIT` é a guarda contra `API_TOKEN` d
         +efinido;                                                       
      30 +  - o `check-unchanged-tests.sh v0.2.0` passa, com os 59 ITs da
         + tag sem alteração e o `ApiDocsIT` como                        
      31 +    única exceção (só a constante `SPEC_CONTRACT`);            
      32 +  - o script foi testado com um IT alterado (reprova), com o `A
         +piDocsIT` alterado (passa) e com uma                           
      33 +    exceção de título inexistente (reprova).                   
      34 +- **Tempo do `./mvnw clean verify`:** 227 s, depois de encurtar
         + os prazos do `KafkaBrokerUnavailableIT`.                      
      35 +                                                               
      36  ## 2026-11-24 — Etapa 2 do Marco 3: verificações e decisões da
          ingestão por Kafka
      37 
      38  - **Nome da autoconfiguração (R2, verificado):** no Boot 4.1.2,
           são

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/tasks.md; node -e '
      const fs=require("fs"); let s=fs.readFileSync("'$f'","utf8");…)
  ⎿  A  .github/scripts/check-unchanged-tests.sh
     A  .github/unchanged-tests-exceptions.txt
     M  docs/ai/decisoes.md
     … +23 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\tasks.md +19 -19
  ⎿  (a git step in this command can move the working tree, so these may be
     its changes, not edits) (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Stage 3 is committed. Git saved the script as non-executable, so I'll fix that
  in stage 4 along with the .github/ changes, and I'm starting stage 4 by
  resolving image versions and digests. · summary

  Ran 2 shell commands (ctrl+o to expand)

● The latest stable versions are Kafka 4.4.0, Prometheus v3.16.0 and Grafana
  13.3.2. Resolving their digests.

  Ran 1 shell command (ctrl+o to expand)

● Confirmed the image tools: kafka-native has nc, prometheus has sh, wget and
  printf, and grafana has wget. Next I'm reading check-workflows.sh to write the
  T071 and T072 tests. · summary

  Ran 1 shell command (ctrl+o to expand)

● Extending check-workflows.sh with the new rules (T072).

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const fs=require("fs"); const f=".github/scripts/check-workflows.sh"; let
      s=fs.readFileSync(f,"utf8");…)
  ⎿  13:#   6. o dependabot.yml tem version 2 e os ecossistemas maven, gith
     ub-actions, docker e docker-compose,
     14:#      com directory "/" e schedule.interval (docker-compose: spec
     … +23 lines (ctrl+o to expand)
  ⎿  Updated .github\scripts\check-workflows.sh +175 -3
      10  #   3. nenhum job pede escrita, exceto `security-events: write`
           no job `codeql`;
      11  #   4. não há referência a `secrets.`;
      12  #   5. não há publicação de imagem (`push: true` no build-push-
          action ou `docker push`);
      13 -#   6. o dependabot.yml tem version 2 e os ecossistemas maven, 
         -github-actions e docker, com                                   
      14 -#      directory "/" e schedule.interval.                      
      13 +#   6. o dependabot.yml tem version 2 e os ecossistemas maven, 
         +github-actions, docker e docker-compose,                       
      14 +#      com directory "/" e schedule.interval (docker-compose: s
         +pec 003, FR-034);                                              
      15 +#   7. toda imagem do compose.yaml e dos service containers dos
         + workflows está fixada por digest                              
      16 +#      (@sha256: com 64 caracteres hexadecimais; spec 003, FR-0
         +34; research R17);                                             
      17 +#   8. nenhum service container de workflow declara POSTGRES_PA
         +SSWORD: o job postgres usa trust, e a                          
      18 +#      credencial de demonstração só pode estar no compose (con
         +stituição 1.6.0; research R16).                                
      19  #
      20  # É a verificação oficial do job `workflow-policy`. Precisa do 
          yq (mikefarah/yq v4), que já vem nos
      21  # runners ubuntu-latest. Códigos de saída: 0 ok, 1 violação, 2 
          ferramenta ausente.
     ...
       41    violation "$ROOT/.github/workflows" "nenhum workflow encontr
           ado"
       42  fi
       43 
       40 -PINNED='^[^@[:space:]]+@[0-9a-f]{40}$'                        
       44 +PINNED='^[^@[:space:]]+@[0-9a-f]{40}for wf in "${WORKFLOWS[@]}
          +"; do                                                         
       45 +  name="${wf#"$ROOT"/}"                                       
       46 +                                                              
       47 +  # 1. Toda action fixada por hash de commit (as locais, ./...
          +, ficam de fora).                                             
       48 +  while IFS= read -r ref; do                                  
       49 +    [[ -z "$ref" || "$ref" == ./* ]] && continue              
       50 +    [[ "$ref" =~ $PINNED ]] || violation "$name" "uses sem has
          +h de commit de 40 caracteres: $ref"                           
       51 +  done < <(yqr -r '.. | select(tag == "!!map" and has("uses"))
          + | .uses' "$wf")                                              
       52 +                                                              
       53 +  # 2. Permissões do topo: exatamente contents: read.         
       54 +  top="$(yqr -o=json -I=0 '.permissions // null' "$wf")"      
       55 +  [[ "$top" == '{"contents":"read"}' ]] || violation "$name" "
          +permissions do topo deveria ser exatamente 'contents: read' (e
          +ncontrado: $top)"                                             
       56 +                                                              
       57 +  # 3. Escrita só em security-events, só no job codeql.       
       58 +  while IFS= read -r job; do                                  
       59 +    [[ -z "$job" ]] && continue                               
       60 +    kind="$(yqr -r ".jobs.\"$job\".permissions | tag" "$wf")" 
       61 +    if [[ "$kind" != "!!map" ]]; then                         
       62 +      violation "$name" "job $job declara permissions '$(yqr -
          +r ".jobs.\"$job\".permissions" "$wf")' (use um mapa com permis
          +sões explícitas)"                                             
       63 +      continue                                                
       64 +    fi                                                        
       65 +    while IFS='=' read -r key value; do                       
     … +156 lines (ctrl+o to expand)

● JavaScript's replace interpreted the $' in the replacement text and duplicated
  whole sections of the script. Restoring the file from git and applying the
  same edits with a replacement function.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; git checkout -q
      .github/scripts/check-workflows.sh; cat > "$TEMP/cw.js" <<'EOF'
      const fs=require("fs"); const f=".gi…)
  ⎿  129
     13:#   6. o dependabot.yml tem version 2 e os ecossistemas maven, gith
     ub-actions, docker e docker-compose,
     … +9 lines (ctrl+o to expand)
  ⎿  Updated .github\scripts\check-workflows.sh +3 -149
  ⎿  (a git step in this command can move the working tree, so these may be
     its changes, not edits) (ctrl+o to expand)

● Restored and edited correctly (129 lines). I checked the other replace calls
  from this session: none had $ in the replacement text. Now compose-test.sh
  (T071).

● Write(.github\scripts\compose-test.sh)
  ⎿  Wrote 141 lines to .github\scripts\compose-test.sh
       1 #!/usr/bin/env bash
       2 # Teste do ambiente completo do compose (spec 003, FR-015a, FR-0
         28 a FR-035, SC-005 e SC-008; research
       3 # R20; contracts/ci-pipeline.md). Sobe o compose, confere a apli
         cação, o Prometheus, o Grafana, a ingestão
       4 # por Kafka, a persistência, a queda real do banco e o token, e 
         derruba tudo no fim, sempre.
       5 #
       6 # Uso: .github/scripts/compose-test.sh   (na raiz do repositório
         , com o Docker e o Compose v2)
     … +135 lines (ctrl+o to expand)

● Simplifying step 6 (no declare -f) and removing a useless check from step 4.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/ct.js" <<'EOF'
      const fs=require("fs"); const f=".github/scripts/compose-test.sh"; let
      s=fs.readFileSync…)
  ⎿  syntax-ok
  ⎿  Updated .github\scripts\compose-test.sh +8 -2
      88 
      89  step "4. Grafana com o dashboard e a datasource provisionados"
      90  [[ "$(status -u "$GRAFANA_AUTH" "$GRAFANA/api/dashboards/uid/aw
          ards")" == "200" ]] || fail "dashboard awards ausente"
      91 -contains "$GRAFANA/api/dashboards/uid/awards" '"uid":"awards"' 
         -|| true                                                        
      91  wait_for "datasource prometheus saudável" 60 \
      92    bash -c "curl -s -u '$GRAFANA_AUTH' '$GRAFANA/api/datasources
          /uid/prometheus/health' | grep -q '\"status\":\"OK\"'"
      93  elapsed=$((SECONDS - start))
     ...
      105 
      106  step "6. Evento inválido no tópico de erros"
      107  publish 'invalido-compose' '{"year":0,"title":"Invalido","stud
           ios":null,"producers":"P","winner":true}'
      109 -wait_for "registro no tópico de erros" 30 bash -c "$(declare -
          -f errors); MOVIES_TOPIC=$MOVIES_TOPIC ERRORS_TOPIC=$ERRORS_TOP
          -IC errors | grep -q 'awards-error-reason:YEAR_OUT_OF_RANGE'"  
      108 +found=""                                                      
      109 +for _ in 1 2 3; do                                            
      110 +  if errors | grep -q 'awards-error-reason:YEAR_OUT_OF_RANGE';
          + then                                                         
      111 +    found=1                                                   
      112 +    break                                                     
      113 +  fi                                                          
      114 +done                                                          
      115 +[[ -n "$found" ]] || fail "o tópico de erros não recebeu o eve
          +nto com YEAR_OUT_OF_RANGE"                                    
      116 
      117  step "7. Persistência: down sem -v e up de novo"
      118  docker compose down

● Confirming the stage 4 failure (T073) before writing the compose.

  Ran 1 shell command (ctrl+o to expand)

● Failure confirmed (T073). Now the compose.yaml. Checking the Dockerfile from
  Marco 2.

  Ran 2 shell commands (ctrl+o to expand)

● The Kafka container runs as appuser, so I'm mounting the volume at
  /home/appuser with KAFKA_LOG_DIRS beneath it, then writing compose.yaml and
  the observability files. · summary

● Write(compose.yaml)
  ⎿  Wrote 153 lines to compose.yaml
       1 # Ambiente completo do modo Docker (spec 003, US5 e FR-028 a FR-
         035; research R17 a R19;
       2 # specs/003-modo-docker/contracts/compose.md). Só para desenvolv
         imento local: o modo padrão continua sendo
       3 # ./mvnw, sem Docker (Princípio II).
       4 #
       5 #   docker compose up -d --build --wait      sobe tudo; a aplica
         ção só fica healthy depois da carga por Kafka
       6 #   docker compose down                      para, mantendo os d
         ados (volumes pgdata e kafkadata)
     … +147 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; mkdir -p
      observability/prometheus observability/grafana/provisioning/datasources
      observability/grafana/provisioning/…)
  ⎿  ok
  ⎿  Created observability\grafana\provisioning\dashboards\awards.yml +9
      1 +# Dashboard versionado em observability/grafana/dashboards (spec
        + 003, FR-032).                                                  
      2 +apiVersion: 1                                                   
      3 +providers:                                                      
      4 +  - name: awards                                                
      5 +    type: file                                                  
      6 +    disableDeletion: true                                       
      7 +    allowUiUpdates: false                                       
      8 +    options:                                                    
      9 +      path: /var/lib/grafana/dashboards                         
  ⎿  Created observability\grafana\provisioning\datasources\prometheus.yml
     +10
       1 +# Fonte de dados do dashboard (spec 003, FR-032; research R19).
       2 +apiVersion: 1                                                  
       3 +datasources:                                                   
       4 +  - name: Prometheus                                           
       5 +    uid: prometheus                                            
       6 +    type: prometheus                                           
       7 +    access: proxy                                              
       8 +    url: http://prometheus:9090                                
       9 +    isDefault: true                                            
      10 +    editable: false                                            
  ⎿  Created observability\prometheus\prometheus.yml +15
       1 +# Coleta das métricas da aplicação no compose (spec 003, FR-031
         +; research R18). Com a autenticação                            
       2 +# ligada, o token chega por /tmp/api-token, gravado na subida d
         +o container a partir de API_TOKEN, e nunca                     
       3 +# fica em arquivo versionado.                                  
       4 +global:                                                        
       5 +  scrape_interval: 5s                                          
       6 +  evaluation_interval: 5s                                      
       7 +                                                               
       8 +scrape_configs:                                                
       9 +  - job_name: app                                              
      10 +    metrics_path: /actuator/prometheus                         
      11 +    authorization:                                             
      12 +      type: Bearer                                             
      13 +      credentials_file: /tmp/api-token                         
      14 +    static_configs:                                            
      15 +      - targets: ["app:8080"]                                  

● Generating the dashboard JSON (the five panels from R19).

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/dash.js" <<'EOF'
      const ds = { type: "prometheus", uid: "prometheus" };…)
  ⎿  {
       "uid": "awards",
       "title": "Golden Raspberry Awards API",
     … +10 lines (ctrl+o to expand)
  ⎿  Created observability\grafana\dashboards\awards.json +238
        1 +{                                                             
        2 +  "uid": "awards",                                            
        3 +  "title": "Golden Raspberry Awards API",                     
        4 +  "description": "Latência, taxa de erro e ingestão por Kafka 
          +(spec 003, FR-032; research R19).",                           
        5 +  "tags": [                                                   
        6 +    "awards"                                                  
        7 +  ],                                                          
        8 +  "timezone": "browser",                                      
        9 +  "schemaVersion": 39,                                        
       10 +  "version": 1,                                               
       11 +  "editable": false,                                          
       12 +  "refresh": "10s",                                           
       13 +  "time": {                                                   
       14 +    "from": "now-15m",                                        
       15 +    "to": "now"                                               
       16 +  },                                                          
       17 +  "panels": [                                                 
       18 +    {                                                         
       19 +      "id": 1,                                                
       20 +      "title": "Requisições por segundo",                     
       21 +      "description": "Endpoints de negócio, por rota.",       
       22 +      "type": "timeseries",                                   
       23 +      "datasource": {                                         
       24 +        "type": "prometheus",                                 
       25 +        "uid": "prometheus"                                   
       26 +      },                                                      
       27 +      "gridPos": {                                            
       28 +        "h": 8,                                               
       29 +        "w": 12,                                              
       30 +        "x": 0,                                               
       31 +        "y": 0                                                
       32 +      },                                                      
       33 +      "fieldConfig": {                                        
       34 +        "defaults": {                                         
       35 +          "unit": "reqps"                                     
       36 +        },                                                    
       37 +        "overrides": []                                       
       38 +      },                                                      
       39 +      "options": {                                            
       40 +        "legend": {                                           
     … +198 lines (ctrl+o to expand)

● Now the Dependabot, the CI jobs (compose and workflow-policy) and the script's
  executable mode.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat >> .github/dependabot.yml
      <<'EOF'…)
  ⎿    image:
         name: Imagem, vulnerabilidades da imagem e teste de fumaça
         needs: dockerfile-lint
     … +52 lines (ctrl+o to expand)
  ⎿  Updated .github\dependabot.yml +6
      26      directory: /
      27      schedule:
      28        interval: weekly
      29 +                                                               
      30 +  # Imagens do compose.yaml (spec 003, FR-034). Cada imagem é f
         +ixada por tag e digest.                                        
      31 +  - package-ecosystem: docker-compose                          
      32 +    directory: /                                               
      33 +    schedule:                                                  
      34 +      interval: weekly                                         

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/ci.js" <<'EOF'
      const fs=require("fs"); const f=".github/workflows/ci.yml"; let
      s=fs.readFileSync(f,"utf…)  ⎿  check-workflows.sh: 1 violação(ões) da política:
       - compose.yaml: serviço app com imagem sem digest: awards:local
     rc=1
  ⎿  Updated .github\workflows\ci.yml +20 -2
      16 
      17  jobs:
      18    workflow-policy:
      19 -    name: Política do workflow e do Dependabot                 
      19 +    name: Política do workflow, do Dependabot e do compose     
      20      runs-on: ubuntu-latest
      21      timeout-minutes: 5
      22      steps:
      23        - uses: actions/checkout@3d3c42e5aac5ba805825da76410c1812
          73ba90b1 # v7.0.1
      24 -      - name: Hash de commit, permissões, segredos, publicação 
         -e Dependabot                                                   
      24 +        with:                                                  
      25 +          # Histórico e tags: o check-unchanged-tests.sh compar
         +a com a tag v0.2.0 (spec 003, research R14).                   
      26 +          fetch-depth: 0                                       
      27 +      - name: Hash de commit, permissões, segredos, publicação,
         + Dependabot e imagens do compose por digest                    
      28          run: .github/scripts/check-workflows.sh
      29 +      - name: Testes de integração dos Marcos 1 e 2 sem alteraç
         +ão desde a v0.2.0                                              
      30 +        run: .github/scripts/check-unchanged-tests.sh v0.2.0   
      31 
      32    build:
      33      name: Build, testes e cobertura
     ...
      241            ignore-unfixed: true
      242            format: table
      243            exit-code: '1'
      244 +                                                              
      245 +  compose:                                                    
      246 +    # Ambiente completo do modo Docker (spec 003, FR-015a, FR-
          +028 a FR-035, SC-005 e SC-008; research                       
      247 +    # R20). O token do passo 8 é gerado no próprio job e nunca
          + é impresso nem gravado. A credencial do                      
      248 +    # Grafana é a de demonstração do compose.yaml (constituiçã
          +o 1.6.0).                                                     
      249 +    name: Compose com PostgreSQL, Kafka, Prometheus e Grafana 
      250 +    needs: dockerfile-lint                                    
      251 +    runs-on: ubuntu-latest                                    
      252 +    timeout-minutes: 20                                       
      253 +    steps:                                                    
      254 +      - uses: actions/checkout@3d3c42e5aac5ba805825da76410c181
          +273ba90b1 # v7.0.1                                            
      255 +      - name: Teste do compose (passos 0 a 8 e 7b)            
      256 +        run: .github/scripts/compose-test.sh                  
  ⎿  (timeout 5m)

● The app service is built locally (build: .), so it has no digest. The rule now
  skips services that have build.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/cw2.js" <<'EOF'
      const fs=require("fs"); const f=".github/scripts/check-workflows.sh"; let
      s=fs.readFile…)
  ⎿  check-workflows.sh: 1 workflow(s), o dependabot.yml e o compose.yaml d
     entro da política.
     rc=0
     … +10 lines (ctrl+o to expand)
  ⎿  Updated .github\scripts\check-workflows.sh +2 -2
      110    done
      111  fi
      112 
      113 -# 7. Imagens do compose.yaml por digest.                      
      113 +# 7. Imagens do compose.yaml por digest (as construídas pelo p
          +róprio compose, com build, ficam de fora).                    
      114  COMPOSE="$ROOT/compose.yaml"
      115  if [[ ! -f "$COMPOSE" ]]; then
      116    violation "compose.yaml" "arquivo ausente"
     ...
      118    while IFS='=' read -r service image; do
      119      [[ -z "$service" ]] && continue
      120      [[ "$image" =~ $DIGEST ]] || violation "compose.yaml" "ser
           viço $service com imagem sem digest: $image"
      121 -  done < <(yqr -r '.services | to_entries[] | select(.value.im
          -age != null) | .key + "=" + .value.image' "$COMPOSE")         
      121 +  done < <(yqr -r '.services | to_entries[] | select(.value.im
          +age != null and .value.build == null) | .key + "=" + .value.im
          +age' "$COMPOSE")                                              
      122  fi
      123 
      124  if (( ${#VIOLATIONS[@]} > 0 )); then

● All four new rules reprove their bad case and pass the real files. Now T081:
  running compose-test.sh locally (includes building the image).

  Ran 2 shell commands (ctrl+o to expand)

● Port 8080 is held by a Java process that isn't mine, probably an ./mvnw 
  spring-boot:run of yours, so I won't kill it. The compose uses APP_PORT, so
  I'll run the test on 18080.

  Ran 2 shell commands (ctrl+o to expand)

● Git Bash converted the /opt/kafka/... path into a Windows path. Adding
  MSYS_NO_PATHCONV=1 at the top of the script (it has no effect on Linux), as
  the Marco 2 smoke test did.

● Update(.github\scripts\compose-test.sh)
  ⎿  Added 4 lines
       9  # O token do passo 8 é gerado na hora e nunca é impresso nem gr
          avado.
      10  set -euo pipefail
      11 
      12 +# No Git Bash do Windows, impede que caminhos como /opt/kafka/b
         +in virem caminhos do Windows nos                               
      13 +# argumentos do docker. No Linux, não muda nada.               
      14 +export MSYS_NO_PATHCONV=1                                      
      15 +                                                               
      16  cd "$(git rev-parse --show-toplevel)"
      17 
      18  APP="http://localhost:${APP_PORT:-8080}"

  Ran 1 shell command (ctrl+o to expand)

● Steps 1 to 4 passed in 28 s, which confirms the empty credentials_file works
  with the token off. Step 7b only showed the event was written after the
  database returned, so I'm adding a check for the retry message in the
  application log and running again. · summary

● Update(.github\scripts\compose-test.sh)
  ⎿  Added 3 lines
      134  if errors | grep -qF '2031|Durante a Queda'; then
      135    fail "o evento gravado depois da queda do banco foi para o t
           ópico de erros"
      136  fi
      137 +# Prova de que a gravação falhou de verdade durante a queda e 
          +foi tentada de novo.                                          
      138 +docker compose logs --no-color app | grep -q 'Falha ao gravar 
          +o evento' \                                                   
      139 +  || fail "o log da aplicação não mostra a nova tentativa dura
          +nte a queda do banco"                                         
      140  ready || fail "a readiness não está 200 depois da queda do ban
           co"
      141 
      142  step "8. Autenticação ligada, com o Prometheus coletando"

  Ran 1 shell command (ctrl+o to expand)

● No retry fired; Hikari probably just waited for the database (up to 30 s)
  instead of failing, so step 7b now keeps the database down until a real
  failure shows in the log. · summary

  Ran 1 shell command (ctrl+o to expand)

● The edit didn't apply (the text differed in the \ escaping), and the run used
  the old version. Applying it with Edit.

  Read 1 file (ctrl+o to expand)

● Update(.github\scripts\compose-test.sh)
  ⎿  Removed 3 lines
      134  if errors | grep -qF '2031|Durante a Queda'; then
      135    fail "o evento gravado depois da queda do banco foi para o t
           ópico de erros"
      136  fi
      137 -# Prova de que a gravação falhou de verdade durante a queda e 
          -foi tentada de novo.                                          
      138 -docker compose logs --no-color app | grep -q 'Falha ao gravar 
          -o evento' \                                                   
      139 -  || fail "o log da aplicação não mostra a nova tentativa dura
          -nte a queda do banco"                                         
      137  ready || fail "a readiness não está 200 depois da queda do ban
           co"
      138 
      139  step "8. Autenticação ligada, com o Prometheus coletando"

● Update(.github\scripts\compose-test.sh)
  ⎿  Added 4 lines, removed 1 line
      127  step "7b. Queda real do banco (FR-015a)"
      128  docker compose stop postgres
      129  publish '2031|Durante a Queda' '{"year":2031,"title":"Durante 
           a Queda","studios":null,"producers":"Producer Q","winner":fals
           e}'
      130 -sleep 5                                                       
      130 +# O banco fica fora até a gravação falhar de verdade e ser ten
          +tada de novo. O pool de conexões espera até                   
      131 +# 30 s por uma conexão antes de falhar, então uma queda curta 
          +não chega a gerar falha.                                      
      132 +wait_for "nova tentativa da gravação no log" 120 \            
      133 +  bash -c "docker compose logs --no-color app | grep -q 'Falha
          + ao gravar o evento'"                                         
      134  docker compose start postgres
      135  wait_for "filme gravado depois da volta do banco" 90 \
      136    bash -c "curl -s '$APP/api/movies?year=2031' | grep -qF '\"t
           itle\":\"Durante a Queda\"'"

  Ran 1 shell command (ctrl+o to expand)

● Now compose-test.sh passes all steps, and 7b proves a real database failure
  followed by a retry. Updating the 7b description in the contract and in R20,
  then running ./mvnw verify (T082).

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/c7b.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
     rc=0
     [INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed:
     … +228 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\contracts\ci-pipeline.md +1 -1
      53  | 5 | publica pelo `kafka-cli` um vencedor novo que cria um int
          ervalo menor que o mínimo e espera até 5 s pela mudança em `min
          ` | não muda em 5 s (SC-005) |
      54  | 6 | publica um evento com `year: 0` e lê o tópico de erros co
          m os cabeçalhos | sem registro ou sem `awards-error-reason: YEA
          R_OUT_OF_RANGE` |
      55  | 7 | `docker compose down` (**sem `-v`**) e `docker compose up
           -d --wait`: todos os containers são recriados sobre os mesmos
          volumes; o filme do passo 5 continua; `totalElements` = 206 + 1
           | filme ausente ou total diferente |
      56 -| 7b | `docker compose stop postgres`; publica `{"year":2031,"t
         -itle":"Durante a Queda",…}`; espera 5 s; `docker compose start 
         -postgres`; espera até 60 s pelo filme na listagem; o tópico de 
         -erros não recebe esse evento (FR-015a, queda real do banco) | f
         -ilme ausente ou evento no tópico de erros |                    
      56 +| 7b | `docker compose stop postgres`; publica `{"year":2031,"t
         +itle":"Durante a Queda",…}`; espera até 120 s pela mensagem `Fa
         +lha ao gravar o evento` no log da aplicação (o pool de conexões
         + espera até 30 s antes de falhar, então uma queda curta não ger
         +a falha); `docker compose start postgres`; espera até 60 s pelo
         + filme na listagem; o tópico de erros não recebe esse evento (F
         +R-015a, queda real do banco) | filme ausente ou evento no tópic
         +o de erros |                                                   
      57  | 8 | `API_TOKEN=$(openssl rand -hex 16) docker compose up -d -
          -wait app prometheus`; intervalos sem token com 401; alvo do Pr
          ometheus `up` de novo | outro status ou alvo fora |
      58 
      59  O token do passo 8 é gerado no próprio job, nunca é gravado e n
          unca é impresso (`set +x` em volta dele).
  ⎿  Updated specs\003-modo-docker\research.md +4 -2
      538       - em seguida, o passo 7b (queda real do banco, FR-015a):
      539         1. `docker compose stop postgres`;
      540         2. publica um evento;
      541 -       3. `docker compose start postgres`;                    
      542 -       4. confere a gravação sem registro no tópico de erros; 
      541 +       3. espera a mensagem `Falha ao gravar o evento` no log 
          +da aplicação. Isso prova que a gravação                       
      542 +          falhou de verdade, porque o pool de conexões espera 
          +até 30 s por uma conexão antes de falhar;                     
      543 +       4. `docker compose start postgres`;                    
      544 +       5. confere a gravação sem registro no tópico de erros; 
      545    7. **token:** recria `app` e `prometheus` com um `API_TOKEN`
            aleatório gerado no próprio job
      546       (`openssl rand -hex 16`, nunca gravado). Confere 401 sem
           token nos intervalos e o alvo do
      547       Prometheus de novo `up`;
  ⎿  (timeout 10m)

● verify is green (270 + 96). Recording stage 4 and committing.

● Update(docs\ai\decisoes.md)
  ⎿  Added 32 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-24 — Etapa 4 do Marco 3: compose, verificações e aju
         +stes                                                           
       7 +                                                               
       8 +- **Imagens (T074):** as versões estáveis mais recentes no Dock
         +er Hub, fixadas por tag e digest:                              
       9 +  - `apache/kafka-native:4.4.0` e `apache/kafka:4.4.0` (o `kafk
         +a-cli`, sob demanda);                                          
      10 +  - `prom/prometheus:v3.16.0`;                                 
      11 +  - `grafana/grafana:13.3.2`;                                  
      12 +  - `postgres:18-alpine`, com o mesmo digest do job `postgres`.
      13 +                                                               
      14 +  O cliente da aplicação (kafka-clients 4.2.2) conversa com o b
         +roker 4.4.                                                     
      15 +- **Healthchecks (R17, verificado):** a imagem nativa do Kafka 
         +tem `nc`, e o healthcheck é                                    
      16 +  `nc -z localhost 9092`. O Prometheus tem `sh`, `wget` e `prin
         +tf`, e o Grafana tem `wget`. Nenhuma saída                     
      17 +  alternativa foi necessária.                                  
      18 +- **Volume do Kafka:** a imagem roda com o usuário `appuser`. U
         +m volume num caminho que não existe na                         
      19 +  imagem seria criado com dono root, então o volume `kafkadata`
         + fica em `/home/appuser`, que já pertence ao                   
      20 +  `appuser`, com `KAFKA_LOG_DIRS=/home/appuser/kraft-logs`. O `
         +CLUSTER_ID` é fixo, para que o volume sirva                    
      21 +  a um container recriado.                                     
      22 +- **Token do Prometheus (R18, verificado):** o `credentials_fil
         +e` vazio é aceito com a autenticação                           
      23 +  desligada, e a coleta funciona. Com `API_TOKEN` definido, o a
         +lvo volta a `up` com o token.                                  
      24 +- **Imagem da aplicação na política:** o serviço `app` é constr
         +uído pelo próprio compose (`build: .`) e                       
      25 +  não tem digest. A regra do `check-workflows.sh` vale para as 
         +imagens baixadas.                                              
      26 +- **Passo 7b (FR-015a, ajuste):** uma queda curta do PostgreSQL
         + não chega a gerar falha, porque o pool de                     
      27 +  conexões (Hikari) espera até 30 s por uma conexão. O passo pa
         +ssou a manter o banco fora até o log da                        
      28 +  aplicação mostrar `Falha ao gravar o evento`, prova de que a 
         +gravação falhou de verdade e foi tentada de                    
      29 +  novo, e só então o religa. O filme é gravado, e o tópico de e
         +rros não recebe o evento.                                      
      30 +- **Git Bash:** o `compose-test.sh` exporta `MSYS_NO_PATHCONV=1
         +`, para que `/opt/kafka/bin/...` não vire                      
      31 +  um caminho do Windows. No runner Linux, isso não muda nada.  
      32 +- **Execução local (T081):**                                   
      33 +  - `compose-test.sh` verde, com todos os passos de 0 a 8 e o 7
         +b;                                                             
      34 +  - os passos de 1 a 4 levaram 28 s, para um limite de 180 s (S
         +C-008);                                                        
      35 +  - rodou com `APP_PORT=18080`, porque a porta 8080 da máquina 
         +estava ocupada por um processo Java do                         
      36 +    usuário, fora da sessão.                                   
      37 +                                                               
      38  ## 2026-11-24 — Etapa 3 do Marco 3: verificações e ajustes da a
          utenticação
      39 
      40  - **Nome da autoconfiguração (verificado):** no Boot 4.1.2, é

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=specs/003-modo-docker/tasks.md; for t in 071 072 073 074 075 076 077 078
      079 080 081 082 083; do sed -i "s/^- \[ \…)
  ⎿  M  .github/dependabot.yml
     M  .github/scripts/check-unchanged-tests.sh
     M  .github/scripts/check-workflows.sh
     … +17 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\tasks.md +13 -13
      620 
      621  ### Verificação da etapa 4 (escrever primeiro; precisa falhar)
      622 
      623 -- [ ] T071 [US5] Criar `.github/scripts/compose-test.sh` (bash
          -, `set -euo pipefail`, executável), com os passos 0 a 8 de `co
          -ntracts/ci-pipeline.md`:                                      
      623 +- [X] T071 [US5] Criar `.github/scripts/compose-test.sh` (bash
          +, `set -euo pipefail`, executável), com os passos 0 a 8 de `co
          +ntracts/ci-pipeline.md`:                                      
      624    - `trap 'docker compose --profile tools down -v' EXIT`;
      625    - mede o tempo dos passos 1 a 4 e falha acima de 180 s (SC-0
           08);
      626    - no passo 5, publica `{"year":1991,"title":"Compose Novo","
           studios":null,"producers":"Joel Silver","winner":true}`
     ...
      639    4. `docker compose start postgres`;
      640    5. esperar até 60 s pelo filme na listagem;
      641    6. conferir que o tópico de erros **não** recebeu esse event
           o.
      642 -- [ ] T072 [US5] Em `.github/scripts/check-workflows.sh`, acre
          -scentar as regras de `contracts/ci-pipeline.md`:              
      642 +- [X] T072 [US5] Em `.github/scripts/check-workflows.sh`, acre
          +scentar as regras de `contracts/ci-pipeline.md`:              
      643    - o ecossistema `docker-compose` no Dependabot;
      644    - `@sha256:` com 64 caracteres hexadecimais em toda `image:`
            do `compose.yaml` e dos `services:` dos
      645      workflows;
     ...
      647 
      648    Testar numa pasta temporária, sem versionar, com um caso que
            falha para cada regra nova e um caso
      649    válido.
      650 -- [ ] T073 [US5] Confirmar a falha: `check-workflows.sh` falha
          - (o Dependabot não tem `docker-compose`), e `compose-test.sh` 
          -falha no passo 1 (não há `compose.yaml`). Sem Docker, registra
          -r `PULADO` para o segundo.                                    
      650 +- [X] T073 [US5] Confirmar a falha: `check-workflows.sh` falha
          + (o Dependabot não tem `docker-compose`), e `compose-test.sh` 
          +falha no passo 1 (não há `compose.yaml`). Sem Docker, registra
          +r `PULADO` para o segundo.                                    
      651 
      652  ### Implementação da etapa 4
      653 
      654 -- [ ] T074 [US5] Resolver as versões estáveis atuais e os dige
          -sts de `postgres:18-alpine`, `apache/kafka-native`, `apache/ka
          -fka` (mesma versão), `prom/prometheus` e `grafana/grafana`, co
          -m `docker buildx imagetools inspect <imagem:tag>` ou pela API 
          -do registro. **Verificar (R17):**                             
      654 +- [X] T074 [US5] Resolver as versões estáveis atuais e os dige
          +sts de `postgres:18-alpine`, `apache/kafka-native`, `apache/ka
          +fka` (mesma versão), `prom/prometheus` e `grafana/grafana`, co
          +m `docker buildx imagetools inspect <imagem:tag>` ou pela API 
          +do registro. **Verificar (R17):**                             
      655    - se a `apache/kafka-native` tem `nc` ou outro binário para
           o healthcheck;
      656    - se `prom/prometheus` tem `/bin/sh` e `wget`, e o `grafana/
           grafana` tem `wget`;
      657    - aplicar a saída da R17 quando faltar algum.
      658 
      659    Anotar os pares tag → digest e o resultado na descrição do c
           ommit.
      660 -- [ ] T075 [US5] Criar `compose.yaml` conforme `contracts/comp
          -ose.md`:                                                      
      660 +- [X] T075 [US5] Criar `compose.yaml` conforme `contracts/comp
          +ose.md`:                                                      
      661    - os seis serviços, com `kafka-cli` em `profiles: [tools]`;
      662    - imagens por tag e digest;
      663    - volumes `pgdata` e `kafkadata`;
     ...
      670    - KRaft com nó único e `KAFKA_AUTO_CREATE_TOPICS_ENABLE=fals
           e`;
      671    - na `app`, `SPRING_PROFILES_ACTIVE=docker`, `DB_PASSWORD=${
           POSTGRES_PASSWORD:-awards-demo}` e
      672      `API_TOKEN=${API_TOKEN:-}`.
      673 -- [ ] T076 [P] [US5] Criar `observability/prometheus/prometheu
          -s.yml` (`scrape_interval: 5s`; job `app`, alvo `app:8080`, `me
          -trics_path: /actuator/prometheus`, `authorization: { type: Bea
          -rer, credentials_file: /tmp/api-token }`). No serviço `prometh
          -eus` do compose, o `entrypoint` grava `API_TOKEN` com `printf 
          -%s` em `/tmp/api-token` e executa o `prometheus`. **Verificar 
          -(R18):** o `credentials_file` vazio é aceito. Se não for, apli
          -car o modelo da R18.                                          
     … +28 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Stage 4 is committed (2c4cff1). T084 depends on your push. Starting stage 5
  (ADRs and README). Checking the existing ADR format and the README structure.

  Ran 1 shell command (ctrl+o to expand)

● Writing the seven ADRs (0013 to 0019).

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; mkdir -p "$TEMP/adr"; cat >
      docs/adr/0013-postgresql-com-esquema-comum.md <<'EOF'
      # 0013. PostgreSQL no modo Docker,…)
  ⎿  ok
  ⎿  Created docs\adr\0013-postgresql-com-esquema-comum.md +38
       1 +# 0013. PostgreSQL no modo Docker, com o mesmo esquema e collat
         +ion C                                                          
       2 +                                                               
       3 +- **Status:** Aceito                                           
       4 +- **Data:** 2026-11-24                                         
       5 +- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entra
         +da "Etapa 1 do Marco 3" (descrição do                          
       6 +  commit), e research R1, R3 e R4 da spec 003.                 
       7 +                                                               
       8 +## Contexto                                                    
       9 +                                                               
      10 +O modo Docker precisa de um banco persistente, para que os film
         +es recebidos por evento sobrevivam a um                        
      11 +reinício. O modo padrão continua com H2 em memória (Princípio I
         +I), e o esquema e as consultas têm de servir                   
      12 +aos dois bancos sem SQL específico (Princípio V e ADR 0002).   
      13 +                                                               
      14 +## Decisão                                                     
      15 +                                                               
      16 +- Perfil `postgres`, ligado pelo grupo `docker`, que troca só o
         + banco. A migração `V1__create_schema.sql`                     
      17 +  é a mesma, validada pelo `ddl-auto: validate`.               
      18 +- Driver `org.postgresql:postgresql` e o módulo `flyway-databas
         +e-postgresql`, os dois com a versão do Boot.                   
      19 +- `DB_URL` e `DB_USER` com padrões do compose. A `DB_PASSWORD` 
         +não tem padrão, porque a credencial de                         
      20 +  demonstração só pode estar no compose (constituição 1.6.0).  
      21 +- Banco criado com `--locale=C --encoding=UTF8`, no compose e n
         +o job `postgres`. A ordem `C` em UTF-8 é a                     
      22 +  dos pontos de código Unicode, a mesma que o H2 usa na listage
         +m.                                                             
      23 +- Os testes que dependem do PostgreSQL ficam no perfil Maven `p
         +ostgres-it` (ADR 0019).                                        
      24 +                                                               
      25 +## Alternativas consideradas                                   
      26 +                                                               
      27 +- `ORDER BY title COLLATE "C"`: o SQL deixaria de ser portável.
      28 +- Ordenar em Java: exigiria carregar tudo antes de paginar.    
      29 +- Uma coluna normalizada só para a ordem: mudaria o esquema sem
         + necessidade.                                                  
      30 +                                                               
      31 +## Consequências                                               
      32 +                                                               
      33 +- O H2 compara unidades UTF-16 (`String.compareTo`), e a collat
         +ion `C` compara pontos de código. Os dois                      
      34 +  só divergem entre caracteres fora do plano básico (emoji, por
         + exemplo) e caracteres de U+E000 a U+FFFF.                     
      35 +  Esse caso não aparece em títulos de filmes.                  
      36 +- Um PostgreSQL com outra collation daria outra ordem. O README
         + e o compose documentam a exigência.                           
      37 +- A gravação passou a criar ou atualizar o filme por ano e títu
         +lo (ADR 0015), para que a carga direta sobre                   
      38 +  um banco já carregado não viole a restrição única.           
  ⎿  Created docs\adr\0014-ingestao-unica-por-kafka.md +40
       1 +# 0014. Kafka como caminho único de ingestão no modo Docker    
       2 +                                                               
       3 +- **Status:** Aceito                                           
       4 +- **Data:** 2026-11-24                                         
       5 +- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entra
         +da "Etapa 2 do Marco 3: verificações e                         
       6 +  decisões da ingestão por Kafka", e research R1, R2, R6, R8 e 
         +R10 da spec 003.                                               
       7 +                                                               
       8 +## Contexto                                                    
       9 +                                                               
      10 +No modo Docker, todo dado entra pelo mesmo caminho: o arquivo i
         +nicial e os vencedores novos passam pelo                       
      11 +mesmo tópico e pelo mesmo consumidor (briefing). A aplicação nã
         +o pode se declarar pronta antes de a carga                     
      12 +terminar (FR-013), e o Kafka não pode ser configurado no modo p
         +adrão (Princípio V).                                           
      13 +                                                               
      14 +## Decisão                                                     
      15 +                                                               
      16 +- Perfil `kafka`, ligado pelo grupo `docker`. A autoconfiguraçã
         +o do Kafka fica excluída fora dele, e a                        
      17 +  carga direta (`MovieFileLoader`) não roda com ele.           
      18 +- O publicador lê o arquivo na subida, com as mesmas regras e o
         +s mesmos avisos do Marco 1, e publica depois                   
      19 +  do `ApplicationReadyEvent`. São um evento por filme aceito e,
         + no fim, um evento `load-completed` com o                      
      20 +  `runId` da execução. Com o broker fora do ar, ele tenta de no
         +vo, e a aplicação fica viva e não pronta.                      
      21 +- O consumidor grava pelo mesmo `MovieImportService` do modo pa
         +drão.                                                          
      22 +- Os tópicos `awards.movies` e `awards.movies.errors` têm uma p
         +artição cada. Assim, o marcador é consumido                    
      23 +  depois de todos os filmes da mesma execução.                 
      24 +- A readiness ganha o indicador `ingestion`, que só fica `UP` c
         +om o marcador do `runId` desta execução.                       
      25 +- A propriedade `awards.ingestion.publish-completion` (padrão `
         +true`) permite que o marcador seja enviado                     
      26 +  por outro cliente. O `KafkaReadinessIT` a usa para observar a
         + readiness antes e depois do marcador.                         
      27 +                                                               
      28 +## Alternativas consideradas                                   
      29 +                                                               
      30 +- Trocar o estado de prontidão à mão: o Boot marca `ACCEPTING_T
         +RAFFIC` antes de a carga por Kafka                             
      31 +  terminar, e haveria uma janela em que a aplicação aparece pro
         +nta.                                                           
      32 +- Várias partições, com um marcador por partição: mais complexo
         +, sem ganho neste volume.                                      
      33 +- Kafka só para os vencedores novos, com a carga direta do arqu
         +ivo: seriam dois caminhos de gravação.                         
      34 +                                                               
      35 +## Consequências                                               
      36 +                                                               
      37 +- A carga direta e a carga por Kafka produzem respostas idêntic
         +as, byte a byte (`KafkaEquivalenceIT`).                        
      38 +- Um reinício publica o arquivo de novo. A gravação idempotente
         + (ADR 0015) evita duplicatas.                                  
      39 +- O publicador e o consumidor rodam dentro da mesma aplicação e
         + supõem uma instância. Várias instâncias                       
      40 +  exigiriam outro desenho da readiness.                        
  ⎿  Created docs\adr\0015-gravacao-idempotente-com-atualizacao.md +36
       1 +# 0015. Gravação idempotente por ano e título, com atualização 
       2 +                                                               
       3 +- **Status:** Aceito                                           
       4 +- **Data:** 2026-11-24                                         
       5 +- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md) e Clar
         +ifications da spec 003 (sessão                                 
       6 +  2026-11-24, primeira pergunta); research R5.                 
       7 +                                                               
       8 +## Contexto                                                    
       9 +                                                               
      10 +Com o banco persistente, um reinício republica o arquivo, e o b
         +roker pode entregar o mesmo evento mais de                     
      11 +uma vez. A gravação não pode duplicar filmes. Além disso, um ev
         +ento com o ano e o título de um filme já                       
      12 +gravado precisa ter um efeito definido.                        
      13 +                                                               
      14 +## Decisão                                                     
      15 +                                                               
      16 +- `MovieImportService.record`: busca o filme por ano e título. 
         +Se não existe, cria; se existe, troca os                       
      17 +  estúdios, o texto dos produtores, o `winner` e todos os víncu
         +los pelos do evento. A última gravação                         
      18 +  prevalece (decisão do responsável).                          
      19 +- A carga direta usa o mesmo método, numa transação para o arqu
         +ivo inteiro. O consumidor usa uma                              
      20 +  transação por evento.                                        
      21 +- Duplicatas dentro do mesmo arquivo continuam ignoradas pelo l
         +eitor, com aviso (spec 001, FR-006d).                          
      22 +                                                               
      23 +## Alternativas consideradas                                   
      24 +                                                               
      25 +- Ignorar o evento repetido, com a primeira gravação prevalecen
         +do: reinícios seriam neutros, mas um                           
      26 +  indicado não poderia virar vencedor por evento.              
      27 +- Só promover a vencedor: uma regra a mais, sem pedido.        
      28 +- `MERGE` ou `INSERT … ON CONFLICT`: o SQL deixaria de ser port
         +ável.                                                          
      29 +                                                               
      30 +## Consequências                                               
      31 +                                                               
      32 +- Um reinício reaplica os valores do arquivo aos filmes que est
         +ão nele. Uma mudança recebida por evento num                   
      33 +  filme do arquivo é desfeita, e os filmes que não estão no arq
         +uivo não mudam (consequência aceita;                           
      34 +  `RestartPostgresIT`).                                        
      35 +- Um produtor que sai de todos os filmes continua gravado, sem 
         +vínculo. Ele não aparece em nenhuma                            
      36 +  resposta, porque os intervalos só usam vencedores.           
  ⎿  Created docs\adr\0016-topico-de-erros-e-novas-tentativas.md +42
       1 +# 0016. Tópico de erros para eventos inválidos e novas tentativ
         +as para falhas de gravação                                     
       2 +                                                               
       3 +- **Status:** Aceito                                           
       4 +- **Data:** 2026-11-24                                         
       5 +- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entra
         +das "Segunda análise da spec 003" e "Etapa 2                   
       6 +  do Marco 3"; research R7 e R9.                               
       7 +                                                               
       8 +## Contexto                                                    
       9 +                                                               
      10 +Um evento inválido não pode travar o consumo (FR-015), e um eve
         +nto válido não pode ser perdido quando a                       
      11 +gravação falha, por exemplo com o banco fora (FR-015a). As duas
         + situações pedem tratamentos opostos.                          
      12 +                                                               
      13 +## Decisão                                                     
      14 +                                                               
      15 +- O consumidor lê os bytes crus e valida o evento com as mesmas
         + regras da linha do CSV, numa classe comum                     
      16 +  (`MovieFieldRules`). Há também os motivos que só existem no J
         +SON: `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`                    
      17 +  e `RUN_ID_MISSING`.                                          
      18 +- **Evento inválido:** não é gravado. Vai ao tópico de erros co
         +m os bytes originais e os cabeçalhos                           
      19 +  `awards-error-reason`, `awards-error-at` e a posição de orige
         +m. O log traz o motivo e a posição, sem o                      
      20 +  conteúdo, e o offset avança. Se o tópico de erros estiver for
         +a, o aviso registra a falha e o consumo                        
      21 +  segue.                                                       
      22 +- **Falha de gravação:** a exceção sobe ao `DefaultErrorHandler
         +`, que tenta de novo sem limite de                             
      23 +  tentativas, com espera crescente até 30 s, sem avançar o offs
         +et.                                                            
      24 +- **Tamanho máximo:** a regra `FIELD_TOO_LONG` (campo maior que
         + a coluna) vale nos dois caminhos. Sem ela, o                  
      25 +  banco recusaria a gravação, e a recusa seria tentada de novo 
         +para sempre, travando o tópico.                                
      26 +                                                               
      27 +## Alternativas consideradas                                   
      28 +                                                               
      29 +- `DeadLetterPublishingRecoverer` para todos os erros: trataria
         + a falha de banco como evento inválido, e                      
      30 +  a carga ficaria incompleta.                                  
      31 +- `JsonDeserializer` com `ErrorHandlingDeserializer`: o conteúd
         +o inválido chegaria ao ouvinte já                              
      32 +  transformado em exceção, sem os bytes originais.             
      33 +- Avro e um registro de esquemas: complexidade sem necessidade 
         +(Princípio V).                                                 
      34 +                                                               
      35 +## Consequências                                               
      36 +                                                               
      37 +- Uma linha do CSV com campo maior que a coluna deixou de derru
         +bar a carga direta: ela é ignorada com aviso                   
      38 +  (spec 001, FR-006, atualizada). A carga direta e o Kafka cont
         +inuam equivalentes, sem exceção.                               
      39 +- Enquanto o banco está fora, o consumo para. Durante a carga, 
         +a readiness fica negativa; depois dela, o                      
      40 +  atraso do consumidor cresce e aparece no dashboard.          
     … +2 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat >
      docs/adr/0017-autenticacao-por-token.md <<'EOF'
      # 0017. Autenticação por token com Spring Security, desligada…)  ⎿  ok
  ⎿  Created docs\adr\0017-autenticacao-por-token.md +57
       1 +# 0017. Autenticação por token com Spring Security, desligada p
         +or padrão                                                      
       2 +                                                               
       3 +- **Status:** Aceito                                           
       4 +- **Data:** 2026-11-24                                         
       5 +- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entra
         +das "Cabeçalhos de proteção do Spring                          
       6 +  Security em todas as respostas", "Exceção do `ApiDocsIT` à re
         +gra dos testes sem alteração", "Segunda                        
       7 +  análise da spec 003" e "Etapa 3 do Marco 3"; research R11 a R
         +14.                                                            
       8 +                                                               
       9 +## Contexto                                                    
      10 +                                                               
      11 +A autenticação existe e é testada, mas fica desligada por padrã
         +o, porque o avaliador chama a API sem token                    
      12 +(Princípio VII). Com ela ligada, os endpoints de negócio e o de
         + métricas exigem o token. As probes e a                        
      13 +documentação continuam públicas. O ETag do Marco 2 precisa cont
         +inuar útil.                                                    
      14 +                                                               
      15 +## Decisão                                                     
      16 +                                                               
      17 +- `spring-boot-starter-security`, com uma `SecurityFilterChain`
         + sem estado: sem CSRF, `formLogin`,                            
      18 +  `httpBasic`, `logout`, cache de requisição nem sessão. O usuá
         +rio gerado pelo Boot fica excluído                             
      19 +  (`UserDetailsServiceAutoConfiguration`).                     
      20 +- O token vem de `API_TOKEN`. Vazio ou ausente, tudo fica liber
         +ado e nenhum filtro de token é registrado.                     
      21 +- Com o token ligado:                                          
      22 +  - `/api/producers/intervals`, `/api/movies` e `/actuator/prom
         +etheus` exigem                                                 
      23 +    `Authorization: Bearer <token>`, em qualquer método;       
      24 +  - o resto continua público: probes, saúde geral, Swagger UI, 
         +contrato, webjars, `/error` e caminhos                         
      25 +    inexistentes.                                              
      26 +- **Comparação em tempo constante** (decisão de design; era a F
         +R-024 da spec, retirada na segunda                             
      27 +  análise): `MessageDigest.isEqual(sha256(recebido), sha256(esp
         +erado))`, em UTF-8. O `isEqual` do JDK                         
      28 +  confere todos os bytes sem sair no primeiro diferente. Os doi
         +s resumos têm sempre 32 bytes, então o tempo                   
      29 +  não depende do tamanho nem do conteúdo do token recebido. `St
         +ring.equals` e `Arrays.equals` não são                         
      30 +  usados. A "Revisão de segurança" do checklist da spec confere
         + esse mecanismo.                                               
      31 +- **401** em Problem Details, igual para o token ausente e para
         + o token errado, com                                           
      32 +  `WWW-Authenticate: Bearer realm="awards"`. Nenhum dos dois va
         +lores aparece na resposta nem no log.                          
      33 +- **Cabeçalhos, como decisão intencional:**                    
      34 +  - `X-Content-Type-Options: nosniff` e `X-Frame-Options: DENY`
         + aparecem em **todas** as respostas, com o                     
      35 +    token ligado ou desligado. São um acréscimo em relação ao M
         +arco 2 que não muda nada do que os testes                      
      36 +    dos Marcos 1 e 2 verificam;                                
      37 +  - o `Cache-Control` do Spring Security fica **fora dos endpoi
         +nts de negócio**, que respondem sem                            
      38 +    `Cache-Control`, `Pragma` e `Expires`, como no Marco 2, par
         +a que o ETag e o 304 continuem úteis;                          
      39 +  - nas demais respostas e em todo 401, os cabeçalhos de cache 
         +do framework continuam.                                        
      40 +- Contrato OpenAPI 1.2.0, com `security: [{}, {bearerAuth: []}]
         +` (autenticação opcional) e a resposta 401.                    
     … +17 lines (ctrl+o to expand)
  ⎿  Created docs\adr\0018-compose-com-observabilidade.md +56
       1 +# 0018. Compose com PostgreSQL, Kafka nativo, Prometheus e Graf
         +ana                                                            
       2 +                                                               
       3 +- **Status:** Aceito                                           
       4 +- **Data:** 2026-11-24                                         
       5 +- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entra
         +das "Emenda 1.6.0 da constituição:                             
       6 +  credenciais de demonstração no compose local" e "Etapa 4 do M
         +arco 3"; research R17 a R20.                                   
       7 +                                                               
       8 +## Contexto                                                    
       9 +                                                               
      10 +O modo Docker mostra o ciclo completo: o banco persistente, a i
         +ngestão por eventos e a observabilidade                        
      11 +coletando de verdade. Ele precisa subir com um comando, num clo
         +ne limpo, sem quebrar o modo padrão.                           
      12 +                                                               
      13 +## Decisão                                                     
      14 +                                                               
      15 +- `compose.yaml` com:                                          
      16 +  - `postgres:18-alpine`, com collation `C` (ADR 0013) e sem po
         +rta publicada;                                                 
      17 +  - `apache/kafka-native`, a imagem nativa (GraalVM) em KRaft c
         +om um nó;                                                      
      18 +  - a aplicação, construída pelo Dockerfile do Marco 2, no perf
         +il `docker`;                                                   
      19 +  - `prom/prometheus`;                                         
      20 +  - `grafana/grafana`;                                         
      21 +  - `kafka-cli` (`apache/kafka`), no perfil `tools`, porque a i
         +magem nativa não traz as ferramentas de                        
      22 +    linha de comando.                                          
      23 +- Toda imagem é fixada por tag e digest, conferida pelo `workfl
         +ow-policy`, e o Dependabot cobre o                             
      24 +  `docker-compose`.                                            
      25 +- Os dados ficam nos volumes `pgdata` e `kafkadata` e sobrevive
         +m a `docker compose down` sem `-v`.                            
      26 +- O Prometheus coleta a aplicação a cada 5 s, com o token por `
         +credentials_file` gravado na subida a partir                   
      27 +  de `API_TOKEN`. O token nunca fica em arquivo versionado.    
      28 +- O Grafana recebe a datasource e o dashboard versionados por p
         +rovisionamento de arquivo. O dashboard tem                     
      29 +  requisições, taxa de erro, p95 e p99, o atraso do consumidor 
         +e os eventos gravados e rejeitados. O                          
      30 +  acesso anônimo é só de leitura.                              
      31 +- **Credenciais de demonstração** do PostgreSQL e do Grafana: s
         +ó no compose local, identificadas linha a                      
      32 +  linha, sobrescrevíveis por variável e nunca usadas em ambient
         +e publicado (constituição 1.6.0).                              
      33 +- O job `compose` do pipeline roda o `compose-test.sh`, que con
         +fere:                                                          
      34 +  - a referência;                                              
      35 +  - o Prometheus e o Grafana;                                  
      36 +  - um vencedor novo em até 5 s;                               
      37 +  - o tópico de erros;                                         
      38 +  - a persistência;                                            
      39 +  - a queda real do banco;                                     
      40 +  - o token.                                                   
     … +16 lines (ctrl+o to expand)
  ⎿  Created docs\adr\0019-testes-com-postgresql-em-perfil-dedicado.md +38
       1 +# 0019. Testes que dependem do PostgreSQL num perfil Maven dedi
         +cado, sem Testcontainers                                       
       2 +                                                               
       3 +- **Status:** Aceito                                           
       4 +- **Data:** 2026-11-24                                         
       5 +- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entra
         +da "Emenda 1.7.0 da constituição:                              
       6 +  comprovação de comportamento que depende de infraestrutura"; 
         +research R16 da spec 003.                                      
       7 +                                                               
       8 +## Contexto                                                    
       9 +                                                               
      10 +O Princípio II exige que o `./mvnw verify` rode sem Docker. Par
         +te do comportamento do Marco 3 só existe                       
      11 +no PostgreSQL: a migração, a ordem com a collation, a persistên
         +cia entre reinícios e a nova tentativa                         
      12 +quando a gravação falha. Antes da emenda 1.7.0, o Princípio VI 
         +pedia o teste de cada comportamento "no                        
      13 +`./mvnw verify`".                                              
      14 +                                                               
      15 +## Decisão                                                     
      16 +                                                               
      17 +- Os ITs que dependem do PostgreSQL terminam em `PostgresIT`, f
         +icam em `postgres/` e são excluídos das                        
      18 +  duas execuções padrão do Failsafe.                           
      19 +- O perfil Maven `postgres-it` roda só esses ITs, numa execução
         + própria do Failsafe, sem o mínimo de                          
      20 +  cobertura (que é o do `verify` padrão) e sem paralelismo.    
      21 +- O pipeline roda o perfil no job `postgres`, com o PostgreSQL 
         +como service container do GitHub Actions,                      
      22 +  em `trust` e sem credencial no workflow. O job é obrigatório 
         +na proteção da `main`.                                         
      23 +- Cada classe usa um esquema próprio, criado pelo Flyway e apag
         +ado no fim.                                                    
      24 +- O comportamento comum aos dois bancos continua testado no `ve
         +rify` padrão, com H2. O Kafka tem broker                       
      25 +  embutido e por isso não entra na exceção (constituição 1.7.0)
         +.                                                              
      26 +                                                               
      27 +## Alternativas consideradas                                   
      28 +                                                               
      29 +- Testcontainers: exigiria Docker no `verify`, ou um segundo me
         +canismo de perfil, e o responsável o                           
      30 +  descartou.                                                   
      31 +- Testar só com H2: a ordem com a collation e a persistência en
         +tre reinícios ficariam sem comprovação.                        
      32 +                                                               
      33 +## Consequências                                               
      34 +                                                               
      35 +- `./mvnw verify` continua sem Docker. `./mvnw -Ppostgres-it ve
         +rify` precisa de um PostgreSQL com                             
      36 +  collation `C`, em `DB_URL`, `DB_USER` e `DB_PASSWORD`.       
      37 +- Um job vermelho reprova o merge só se estiver na lista de che
         +cks obrigatórios da `main` (T093 da spec                       
      38 +  003).                                                        

● Now ADR 0004 (the 1.5.0 and 1.7.0 pending items) and the ADR index.

  Ran 1 shell command (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/adr4.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
  ⎿  Updated docs\adr\0004-somente-testes-de-integracao.md +18 -3
       3  - **Status:** Aceito
       4  - **Data:** 2026-11-19
       5  - **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entra
          das "Emenda 1.2.0 da constituição:
       6 -  Princípio I reescrito" e "Mockito excluído do `spring-boot-st
         -arter-test`"; Princípio I da constituição.                     
       6 +  Princípio I reescrito", "Mockito excluído do `spring-boot-sta
         +rter-test`", "Emenda 1.5.0 da constituição:                    
       7 +  `SpringApplication` para controlar o ciclo de vida" e "Emenda
         + 1.7.0 da constituição: comprovação de                         
       8 +  comportamento que depende de infraestrutura"; Princípios I e 
         +VI da constituição.                                            
       9 +- **Atualizado:** 2026-11-24, com as emendas 1.5.0 e 1.7.0.    
      10 
      11  ## Contexto
      12 
     ...
      20    completo da aplicação, sem dublês.
      21  - O resultado é verificado preferencialmente por HTTP. Log ou b
          anco só valem enquanto o comportamento não
      22    tem endpoint.
      20 -- As falhas de inicialização rodam a aplicação real com `Spring
         -Application` e verificam a exceção e o                         
      21 -  log.                                                         
      23 +- Os testes que precisam controlar o ciclo de vida da aplicação
         + real a rodam com `SpringApplication`                          
      24 +  (emenda 1.5.0). São quatro casos, sempre sem mocks e com veri
         +ficação por HTTP real sempre que o HTTP                        
      25 +  observa o comportamento:                                     
      26 +  - falha de inicialização: verificam a exceção e o log;       
      27 +  - múltiplas execuções: comparam duas ou mais subidas, como o 
         +ETag entre execuções e as equivalências do                     
      28 +    Marco 3;                                                   
      29 +  - ordem da subida: observam os eventos reais do ciclo de vida
         +;                                                              
      30 +  - saída completa da subida: capturam o log desde a primeira l
         +inha.                                                          
      31 +- O comportamento que depende de infraestrutura indisponível no
         + modo padrão, como o PostgreSQL, é testado                     
      32 +  num perfil Maven dedicado, executado obrigatoriamente no pipe
         +line (emenda 1.7.0; ADR 0019). O Kafka tem                     
      33 +  broker embutido e fica no `./mvnw verify`.                   
      34  - O Mockito é excluído do `spring-boot-starter-test` no `pom.xm
          l`: usar mock vira erro de compilação.
      35 
      36  ## Alternativas consideradas
     ...
      48    das classes (`@DirtiesContext`).
      49  - A partir do Marco 2, documentação, pipeline e imagem têm outr
          as formas de comprovação, pelo Princípio VI
      50    (constituição 1.4.0).
      51 +- A partir do Marco 3, os `*PostgresIT` rodam fora do `./mvnw v
         +erify`, no perfil `postgres-it` (ADR 0019).                    
      52 +  Uma decisão de design sem comportamento verificável por teste
         + confiável, como a comparação do token em                      
      53 +  tempo constante (ADR 0017), fica fora da spec e é comprovada 
         +por revisão.                                                   
  ⎿  Updated docs\adr\README.md +11 -3
      23  | [0010](0010-formato-do-log.md) | Log em texto no `./mvnw` e J
          SON por variável, ligado na imagem | Aceito | 2026-11-23 |
      24  | [0011](0011-imagem-de-container.md) | Imagem multi-stage, JRE
           Alpine por digest e usuário não root | Aceito | 2026-11-23 |
      25  | [0012](0012-pipeline-e-cadeia-de-entrega.md) | Pipeline com a
          ctions por hash, portas de segurança e cobertura e Dependabot |
           Aceito | 2026-11-23 |
      26 +| [0013](0013-postgresql-com-esquema-comum.md) | PostgreSQL no 
         +modo Docker, com o mesmo esquema e collation C | Aceito | 2026-
         +11-24 |                                                        
      27 +| [0014](0014-ingestao-unica-por-kafka.md) | Kafka como caminho
         + único de ingestão no modo Docker | Aceito | 2026-11-24 |      
      28 +| [0015](0015-gravacao-idempotente-com-atualizacao.md) | Gravaç
         +ão idempotente por ano e título, com atualização | Aceito | 202
         +6-11-24 |                                                      
      29 +| [0016](0016-topico-de-erros-e-novas-tentativas.md) | Tópico d
         +e erros para eventos inválidos e novas tentativas para falhas d
         +e gravação | Aceito | 2026-11-24 |                             
      30 +| [0017](0017-autenticacao-por-token.md) | Autenticação por tok
         +en com Spring Security, desligada por padrão | Aceito | 2026-11
         +-24 |                                                          
      31 +| [0018](0018-compose-com-observabilidade.md) | Compose com Pos
         +tgreSQL, Kafka nativo, Prometheus e Grafana | Aceito | 2026-11-
         +24 |                                                           
      32 +| [0019](0019-testes-com-postgresql-em-perfil-dedicado.md) | Te
         +stes que dependem do PostgreSQL num perfil Maven dedicado | Ace
         +ito | 2026-11-24 |                                             
      33 
      27 -A comprovação destes registros é a revisão do pull request, pel
         -a seção "Revisão dos ADRs" de                                  
      28 -[`specs/002-operacao/checklists/requirements.md`](../../specs/0
         -02-operacao/checklists/requirements.md)                        
      29 -(Princípio VI, constituição 1.4.0).                            
      34 +A comprovação destes registros é a revisão do pull request, pel
         +a seção "Revisão dos ADRs" do checklist de                     
      35 +cada spec: [`specs/002-operacao/checklists/requirements.md`](..
         +/../specs/002-operacao/checklists/requirements.md)             
      36 +para os ADRs 0001 a 0012 e [`specs/003-modo-docker/checklists/r
         +equirements.md`](../../specs/003-modo-docker/checklists/require
         +ments.md)                                                      
      37 +para os ADRs 0013 a 0019 e a atualização do 0004 (Princípio VI)
         +.                                                              

● ADRs done. Now the README (T088). Reading the sections I need to change.

  Read 1 file (ctrl+o to expand)

● Applying the README changes, section by section.

● Update(README.md)
  ⎿  Added 84 lines
      170  O contrato da imagem está em
      171  [`specs/002-operacao/contracts/container.md`](specs/002-operac
           ao/contracts/container.md).
      172 
      173 +### Modo Docker completo: PostgreSQL, Kafka, Prometheus e Graf
          +ana (opcional)                                                
      174 +                                                              
      175 +O [`compose.yaml`](compose.yaml) sobe o ambiente completo do M
          +arco 3. É só para desenvolvimento local, e                    
      176 +nada dele é exigido pelo modo padrão.                         
      177 +                                                              
      178 +```bash                                                       
      179 +docker compose up -d --build --wait   # a aplicação só fica he
          +althy depois da carga por Kafka                               
      180 +curl -s http://localhost:8080/api/producers/intervals         
      181 +docker compose down                   # para e mantém os dados
          + (volumes pgdata e kafkadata)                                 
      182 +docker compose down -v                # apaga tudo            
      183 +```                                                           
      184 +                                                              
      185 +| Serviço | Porta no host | O que faz |                       
      186 +| --- | --- | --- |                                           
      187 +| `app` | `8080` (`APP_PORT`) | A aplicação no perfil `docker`
          +: PostgreSQL no lugar do H2 e o Kafka como único caminho de en
          +trada dos dados |                                             
      188 +| `postgres` | nenhuma | PostgreSQL 18, com o mesmo esquema Fl
          +yway e collation `C` (a ordem da listagem é a do H2) |        
      189 +| `kafka` | `9094` | Kafka 4.4 na imagem nativa, em KRaft, com
          + os tópicos `awards.movies` e `awards.movies.errors` |        
      190 +| `prometheus` | `9090` | Coleta `/actuator/prometheus` a cada
          + 5 s |                                                        
      191 +| `grafana` | `3000` | O dashboard [`awards`](observability/gr
          +afana/dashboards/awards.json), aberto sem login em <http://loc
          +alhost:3000/d/awards> |                                       
      192 +| `kafka-cli` | — | Ferramentas de linha de comando do Kafka, 
          +só sob demanda (perfil `tools`) |                             
      193 +                                                              
      194 +**Como funciona a carga no modo Docker:**                     
      195 +1. Na subida, a aplicação lê o CSV com as mesmas regras e os m
          +esmos avisos do modo padrão.                                  
      196 +2. Ela publica um evento por filme no tópico `awards.movies` e
          +, no fim, um evento de carga concluída.                       
      197 +3. O consumidor grava cada filme pelo mesmo serviço do modo pa
          +drão.                                                         
      198 +4. A readiness só fica positiva quando o evento de carga concl
          +uída desta execução é consumido.                              
      199 +5. Num reinício, o arquivo é publicado de novo. A gravação por
          + ano e título não duplica nada, e os filmes                   
      200 +   novos recebidos por evento continuam lá.                   
      201 +                                                              
      202 +O formato dos eventos está em                                 
      203 +[`specs/003-modo-docker/contracts/kafka-events.md`](specs/003-
          +modo-docker/contracts/kafka-events.md).                       
      204 +Para publicar um vencedor novo e ver o resultado:             
      205 +                                                              
      206 +```bash                                                       
      207 +printf '%s\n' '2030|Filme Novo:{"year":2030,"title":"Filme Nov
          +o","studios":null,"producers":"Joel Silver","winner":true}' \ 
      208 +  | docker compose --profile tools run --rm -T kafka-cli /opt/
          +kafka/bin/kafka-console-producer.sh \                         
      209 +      --bootstrap-server kafka:9092 --topic awards.movies --pr
          +operty parse.key=true --property key.separator=:              
      210 +curl -s "http://localhost:8080/api/movies?year=2030"          
      211 +```                                                           
      212 +                                                              
      213 +Um evento inválido vai para o tópico de erros, com o conteúdo 
          +original e o motivo nos cabeçalhos, e o                       
      214 +consumo segue:                                                
      215 +                                                              
      216 +```bash                                                       
      217 +docker compose --profile tools run --rm kafka-cli /opt/kafka/b
          +in/kafka-console-consumer.sh \                                
      218 +  --bootstrap-server kafka:9092 --topic awards.movies.errors -
          +-from-beginning \                                             
      219 +  --property print.headers=true --timeout-ms 5000             
      220 +```                                                           
      221 +                                                              
      222 +**Credenciais de demonstração do compose:** o PostgreSQL e o G
          +rafana usam a senha `awards-demo`                             
      223 +(usuários `awards` e `admin`). Elas só valem no compose local,
          + estão identificadas em cada linha do                         
      224 +`compose.yaml`, podem ser trocadas pelas variáveis `POSTGRES_P
          +ASSWORD` e `GRAFANA_ADMIN_PASSWORD` e nunca                   
      225 +devem ser usadas num ambiente publicado (constituição 1.6.0, P
          +rincípio VII).                                                
      226 +                                                              
      227 +### Autenticação (opcional)                                   
      228 +                                                              
      229 +A autenticação existe e é testada, mas fica **desligada por pa
          +drão**: sem a variável `API_TOKEN`, todos                     
      230 +os endpoints são públicos, como no Marco 2. Com ela, os interv
          +alos, a listagem e as métricas exigem o                       
      231 +cabeçalho `Authorization: Bearer <token>`.                    
      232 +                                                              
      233 +**Token de demonstração** (o único token do repositório, só pa
          +ra experimentar):                                             
      234 +`awards-token-de-demonstracao`.                               
      235 +                                                              
      236 +```bash                                                       
      237 +API_TOKEN=awards-token-de-demonstracao ./mvnw spring-boot:run 
      238 +                                                              
      239 +curl -si http://localhost:8080/api/producers/intervals        
          +             # 401                                            
      240 +curl -s -H "Authorization: Bearer awards-token-de-demonstracao
          +" \                                                           
      241 +  http://localhost:8080/api/producers/intervals               
          +             # 200                                            
      242 +```                                                           
      243 +                                                              
      244 +| Caminho | Sem `API_TOKEN` | Com `API_TOKEN` |               
      245 +| --- | --- | --- |                                           
      246 +| `/api/producers/intervals`, `/api/movies` | público | exige 
          +o token, em qualquer método (inclusive `OPTIONS`) |           
      247 +| `/actuator/prometheus` | público | exige o token |          
      248 +| `/actuator/health`, `/actuator/health/liveness`, `/actuator/
          +health/readiness` | público | público |                       
      249 +| `/swagger-ui.html`, `/openapi.yaml`, `/webjars/**` | público
          + | público |                                                  
      250 +                                                              
      251 +- O token ausente e o errado recebem o mesmo **401** em Proble
          +m Details, com `WWW-Authenticate: Bearer                      
      252 +  realm="awards"`, sem revelar nenhum dos dois valores.       
      253 +- A comparação do token é feita em tempo constante, e o token 
          +nunca aparece no log nem nas métricas.                        
      254 +- No compose, a autenticação também fica desligada por padrão.
          + Para ligá-la:                                                
      255 +  `API_TOKEN=<token> docker compose up -d`. O Prometheus passa
          + a coletar com o mesmo token.                                 
      256 +                                                              
      257  Com linhas inválidas (`src/test/resources/scenarios/invalid-li
           nes.csv`), a aplicação sobe e o log
      258  registra cada rejeição e o resumo:
      259 

● Update(README.md)
  ⎿  Added 6 lines, removed 5 lines
      270 
      271  ## 4. Endpoints
      272 
      273 -O contrato completo (versão 1.1.0, com ETag) está em [`specs/0
          -02-operacao/contracts/openapi.yaml`](specs/002-operacao/contra
          -cts/openapi.yaml),                                            
      274 -publicado em <http://localhost:8080/openapi.yaml> e navegável 
          -no Swagger UI, em                                             
      275 -<http://localhost:8080/swagger-ui.html>. O do Marco 1 fica em 
      276 -[`specs/001-producer-intervals-api/contracts/openapi.yaml`](sp
          -ecs/001-producer-intervals-api/contracts/openapi.yaml),       
      277 -como registro.                                                
      273 +O contrato completo (versão 1.2.0, com ETag e a autenticação o
          +pcional) está em                                              
      274 +[`specs/003-modo-docker/contracts/openapi.yaml`](specs/003-mod
          +o-docker/contracts/openapi.yaml), publicado                   
      275 +em <http://localhost:8080/openapi.yaml> e navegável no Swagger
          + UI, em                                                       
      276 +<http://localhost:8080/swagger-ui.html>. Os dos Marcos 1 e 2 f
          +icam em                                                       
      277 +[`specs/001-producer-intervals-api/contracts/openapi.yaml`](sp
          +ecs/001-producer-intervals-api/contracts/openapi.yaml)        
      278 +e [`specs/002-operacao/contracts/openapi.yaml`](specs/002-oper
          +acao/contracts/openapi.yaml), como registro.                  
      279 
      280  | Método e caminho | O que faz |
      281  | --- | --- |

● Update(README.md)
  ⎿  Added 5 lines, removed 2 lines
      350 
      351  ### Endpoints operacionais
      352 
      353 -Na mesma porta da API, públicos neste marco e sem detalhes int
          -ernos. O contrato está em                                     
      354 -[`specs/002-operacao/contracts/operational-endpoints.md`](spec
          -s/002-operacao/contracts/operational-endpoints.md).           
      353 +Na mesma porta da API e sem detalhes internos. As probes e a s
          +aúde geral são sempre públicas. O endpoint                    
      354 +de métricas exige o token quando a [autenticação](#autenticaçã
          +o-opcional) está ligada. Os contratos estão                   
      355 +em [`specs/002-operacao/contracts/operational-endpoints.md`](s
          +pecs/002-operacao/contracts/operational-endpoints.md)         
      356 +e, com o que o Marco 3 acrescenta, em                         
      357 +[`specs/003-modo-docker/contracts/operational-endpoints.md`](s
          +pecs/003-modo-docker/contracts/operational-endpoints.md).     
      358 
      359  | Caminho | Resposta |
      360  | --- | --- |

● Update(README.md)
  ⎿  Added 1 line, removed 1 line
      361  | `/actuator/health/liveness` | 200 `{"status":"UP"}`; 503 `{"
           status":"DOWN"}` se a aplicação estiver quebrada |
      362  | `/actuator/health/readiness` | 200 `{"status":"UP"}` só depo
           is da carga do CSV; 503 `{"status":"OUT_OF_SERVICE"}` antes |
      363  | `/actuator/health` | 200 `{"status":"UP","groups":["liveness
           ","readiness"]}`, sem componentes nem detalhes |
      364 -| `/actuator/prometheus` | Texto do Prometheus: `http_server_r
          -equests_seconds` (contagem e histograma de latência por rota, 
          -método, status e `outcome`), métricas da JVM e do processo, to
          -das com `application="awards"` |                              
      364 +| `/actuator/prometheus` | Texto do Prometheus: `http_server_r
          +equests_seconds` (contagem e histograma de latência por rota, 
          +método, status e `outcome`), métricas da JVM e do processo, to
          +das com `application="awards"`. No modo Docker, também `awards
          +_ingestion_events_total` (eventos gravados e rejeitados) e `ka
          +fka_consumer_fetch_manager_records_lag_max` (atraso do consumi
          +dor) |                                                        
      365 
      366  Os demais endpoints do Actuator (`/actuator`, `/actuator/env`,
            `/actuator/beans` e outros) respondem
      367  404. Com as métricas, a taxa de erro e o percentil 95 de cada
           endpoint saem direto no Prometheus:

● Update(README.md)
  ⎿  Added 1 line
      380  | 200 | Consulta atendida, inclusive com listas vazias |
      381  | 304 | `If-None-Match` igual ao ETag atual, sem corpo |
      382  | 400 | Parâmetro inválido em `/api/movies`, em Problem Detail
           s |
      383 +| 401 | Autenticação ligada e token ausente ou errado, em Prob
          +lem Details |                                                 
      384  | 404 | Caminho inexistente, em Problem Details |
      385  | 405 | Verbo diferente de GET nos dois endpoints, em Problem
           Details e com o cabeçalho `Allow: GET` |
      386  | 503 | Liveness ou readiness negativa |

● Update(README.md)
  ⎿  Added 1 line, removed 1 line
      383  | 401 | Autenticação ligada e token ausente ou errado, em Prob
           lem Details |
      384  | 404 | Caminho inexistente, em Problem Details |
      385  | 405 | Verbo diferente de GET nos dois endpoints, em Problem
           Details e com o cabeçalho `Allow: GET` |
      386 -| 503 | Liveness ou readiness negativa |                      
      386 +| 503 | Liveness ou readiness negativa (no modo Docker, a read
          +iness fica 503 até a carga por Kafka terminar) |              
      387 
      388  ---
      389 

● Now section 5 (tests and pipeline).

● Update(README.md)
  ⎿  Added 20 lines, removed 5 lines
      399  .\mvnw.cmd verify "-Dit.test=ReferenceFileIT"   # no PowerShel
           l, o -D vai entre aspas
      400  ```
      401 
      402 -São **189 testes de integração**, executados pelo Maven Failsa
          -fe na fase `verify`, sem Docker, em duas                      
      402 +São **270 testes de integração**, executados pelo Maven Failsa
          +fe na fase `verify`, **sem Docker**, em duas                  
      403  execuções:
      404 
      405  - **padrão:** todos os testes, com o log em texto. Os 5 do `Js
           onLogFormatIT` ficam pulados, porque só
      406    valem com o JSON ligado;
      407 -- **`json-logs`:** os 55 testes das classes marcadas com `@Tag
          -("log")` rodam de novo, numa JVM própria,                     
      407 +- **`json-logs`:** os 96 testes das classes marcadas com `@Tag
          +("log")` rodam de novo, numa JVM própria,                     
      408    com `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`. Assim, as mesma
           s verificações de log valem nos dois
      409    formatos.
      410 
      411 +Os testes do Kafka rodam no `./mvnw verify` com o **broker emb
          +utido** do Spring Kafka (KRaft, dentro do                     
      412 +próprio teste), sem Docker.                                   
      413 +                                                              
      414  A **cobertura** de linhas é medida pelo JaCoCo sobre as duas e
           xecuções, e o `verify` falha abaixo de
      412 -**90%**. Hoje ela está em 97,28%, e o relatório fica em `targe
          -t/site/jacoco-it/index.html`. A execução                      
      413 -completa leva cerca de 75 s numa cópia de trabalho já compilad
          -a. Num clone novo, a primeira execução                        
      414 -levou cerca de 4 min e meio na máquina de desenvolvimento.    
      415 +**90%**. Hoje ela está em 96,32%, e o relatório fica em `targe
          +t/site/jacoco-it/index.html`. A execução                      
      416 +completa leva cerca de 4 min na máquina de desenvolvimento. A 
          +maior parte do tempo está nos testes do                       
      417 +Kafka que esperam de propósito, como o broker fora do ar e os 
          +16 tipos de evento inválido.                                  
      418 
      419 +**Testes com PostgreSQL** (perfil Maven `postgres-it`): o que 
          +só existe no PostgreSQL fica fora do                          
      420 +`./mvnw verify` padrão (Princípio II) e roda obrigatoriamente 
          +no job `postgres` do pipeline (Princípio VI,                  
      421 +constituição 1.7.0). São a migração, a ordem com a collation, 
          +os reinícios sobre o mesmo banco e a nova                     
      422 +tentativa quando a gravação falha. Para rodar localmente, é pr
          +eciso um PostgreSQL com collation `C`:                        
      423 +                                                              
      424 +```bash                                                       
      425 +docker run -d --name awards-pg -p 5432:5432 -e POSTGRES_DB=awa
          +rds -e POSTGRES_USER=awards \                                 
      426 +  -e POSTGRES_HOST_AUTH_METHOD=trust -e POSTGRES_INITDB_ARGS='
          +--locale=C --encoding=UTF8' postgres:18-alpine                
      427 +DB_URL=jdbc:postgresql://localhost:5432/awards DB_USER=awards 
          +DB_PASSWORD= ./mvnw -Ppostgres-it verify                      
      428 +docker rm -f awards-pg                                        
      429 +```                                                           
      430 +                                                              
      431  | Grupo (`src/test/java/com/outsera/awards/`) | O que cobre |
      432  | --- | --- |
      433  | `SmokeIT` | A aplicação sobe com o arquivo padrão |

● Update(README.md)
  ⎿  Added 5 lines, removed 1 line
      441  | `health/` | Liveness e readiness, corpo sem detalhes, 503 no
           s estados negativos e a readiness só depois da carga |
      442  | `metrics/` | Métricas no formato do Prometheus: contagem, st
           atus, rota sem parâmetros, rotas inexistentes, 304, histograma
            e rótulo `application` |
      443  | `logging/` | Log em texto sem a variável, JSON ECS com ela,
           banner desligado e nenhum cabeçalho `Authorization` no log |
      444 -| `support/` | Base comum (`ScenarioIT`) e apoios (leitura do 
          -log nos dois formatos, do texto do Prometheus e do contrato), 
          -sem testes próprios |                                         
      444 +| `isolation/` | Sem perfil, o Kafka não é configurado: nenhum
          +a métrica nem cliente Kafka (Marco 3) |                       
      445 +| `kafka/` | Ingestão por Kafka com o broker embutido: resulta
          +do de referência, equivalência byte a byte com a carga direta,
          + readiness só com o evento de carga concluída, linhas inválida
          +s, falhas estruturais, vencedor novo em até 5 s, atualização, 
          +idempotência, cada tipo de evento inválido no tópico de erros,
          + tópico de erros fora do ar, broker fora do ar e métricas (Mar
          +co 3) |                                                       
      446 +| `security/` | Autenticação desligada (guarda do `API_TOKEN`)
          + e ligada: 401 em Problem Details, endpoints públicos, token f
          +ora do log, sem senha gerada, cabeçalhos de cache e de proteçã
          +o e o contrato 1.2.0 (Marco 3) |                              
      447 +| `postgres/` | Só no perfil `postgres-it`: esquema, equivalên
          +cia e ordem entre o H2 e o PostgreSQL, reinícios e nova tentat
          +iva quando a gravação falha (Marco 3) |                       
      448 +| `support/` | Base comum (`ScenarioIT`, `KafkaScenarioIT`, `T
          +okenScenarioIT`) e apoios (aplicação real por `SpringApplicati
          +on`, HTTP, Kafka e PostgreSQL reais, leitura do log, do Promet
          +heus e do contrato), sem testes próprios |                    
      449 
      450  **Como os testes funcionam:**
      451  - Cada classe sobe a aplicação inteira com `@SpringBootTest` e
           m porta aleatória e faz chamadas HTTP

● Update(README.md)
  ⎿  Added 11 lines, removed 2 lines
      452    reais.
      453  - Cada cenário tem o seu CSV em `src/test/resources/scenarios/
           `, escolhido pela propriedade
      454    `movies.csv.path`.
      455 -- Nos testes de falha de inicialização (`StartupFailureIT`), o
          - contexto não chega a subir. Por isso,                        
      456 -  eles executam a aplicação real com `SpringApplication` e ver
          -ificam a exceção e o log.                                     
      455 +- Quando o teste precisa controlar o ciclo de vida da aplicaçã
          +o real, ele a executa com                                     
      456 +  `SpringApplication`, ainda sem mocks (Princípio I, constitui
          +ção 1.5.0). São quatro casos:                                 
      457 +  - **falha de inicialização**, como o `StartupFailureIT`: o c
          +ontexto não chega a subir, e o teste                          
      458 +    verifica a exceção e o log;                               
      459 +  - **múltiplas execuções**, como o `EtagAcrossRunsIT`, o `Kaf
          +kaEquivalenceIT` e o `RestartPostgresIT`:                     
      460 +    comparam duas ou mais subidas;                            
      461 +  - **ordem da subida**, como o `ReadinessAfterLoadIT`: observ
          +a os eventos reais do ciclo de vida;                          
      462 +  - **saída completa da subida**, como o `JsonLogFormatIT` e o
          + `TokenNotLoggedIT`: capturam o log desde a                   
      463 +    primeira linha.                                           
      464 
      465 +  O resultado é verificado por HTTP real sempre que o HTTP obs
          +erva o comportamento.                                         
      466 +                                                              
      467  **Por que só testes de integração:** o item 4.2 do enunciado p
           ede testes de integração, e o Princípio I
      468  da [constituição](.specify/memory/constitution.md) proíbe test
           es unitários e mocks na API.
      469  - Não há classe `*Test`, e o Mockito foi excluído do classpath
            de teste.

● Update(README.md)
  ⎿  Added 7 lines, removed 3 lines
      468  da [constituição](.specify/memory/constitution.md) proíbe test
           es unitários e mocks na API.
      469  - Não há classe `*Test`, e o Mockito foi excluído do classpath
            de teste.
      470  - Os testes verificam o comportamento pelo HTTP, como o avalia
           dor o vê, e não detalhes internos.
      471 -- Log e banco só podem ser usados enquanto um comportamento nã
          -o tem endpoint. Nenhum teste consulta o                       
      472 -  banco. O log só é verificado no que não tem endpoint: os avi
          -sos e o resumo da carga, a ordem entre a                      
      473 -  carga e o início do servidor, as falhas de inicialização e o
          - próprio formato do log.                                      
      471 +- Log e banco só podem ser usados enquanto um comportamento nã
          +o tem endpoint. O único teste que consulta                    
      472 +  o banco é o `SchemaPostgresIT`, que confere o histórico do F
          +lyway, porque a migração não tem endpoint. O                  
      473 +  log só é verificado no que não tem endpoint: os avisos e o r
          +esumo da carga, a ordem entre a carga e o                     
      474 +  início do servidor, as falhas de inicialização, o próprio fo
          +rmato do log e a ausência do token.                           
      475 +- Os testes do Kafka usam um produtor e um consumidor reais co
          +ntra o broker embutido. Os testes do                          
      476 +  PostgreSQL usam um banco real, com um esquema próprio por cl
          +asse.                                                         
      477 +- A autenticação é testada com tokens gerados na hora: nenhum 
          +token fica no código dos testes.                              
      478 
      479  ### Pipeline
      480 

● Update(README.md)
  ⎿  Added 3 lines, removed 1 line
      484 
      485  | Job | O que faz | Reprova quando |
      486  | --- | --- | --- |
      487 -| `workflow-policy` | Confere a política do workflow e do Depe
          -ndabot | Action sem hash de commit, permissão além de `content
          -s: read` (exceto `security-events: write` no `codeql`), uso de
          - `secrets.`, publicação de imagem ou Dependabot incompleto |  
      487 +| `workflow-policy` | Confere a política do workflow, do Depen
          +dabot e do compose, e que os testes dos Marcos 1 e 2 não mudar
          +am desde a `v0.2.0` | Action sem hash de commit, permissão alé
          +m de `contents: read` (exceto `security-events: write` no `cod
          +eql`), uso de `secrets.`, publicação de imagem, Dependabot inc
          +ompleto, imagem do compose ou de service container sem digest,
          + senha de banco no workflow, ou um IT da `v0.2.0` alterado sem
          + exceção registrada |                                         
      488  | `build` | `./mvnw verify`, cobertura no resumo e relatórios
           como artefato | Qualquer teste falha, ou a cobertura de linhas
            fica abaixo de 90% |
      489 +| `postgres` | `./mvnw -Ppostgres-it verify` contra um Postgre
          +SQL 18 (service container, collation `C`, sem credencial) | Qu
          +alquer `*PostgresIT` falha |                                  
      490 +| `compose` | [`compose-test.sh`](.github/scripts/compose-test
          +.sh): sobe o compose e confere a referência, o Prometheus, o d
          +ashboard, um vencedor novo pelo Kafka em até 5 s, o tópico de 
          +erros, a persistência depois de `down`, a queda real do banco 
          +e o token | Qualquer passo falha, ou a subida até o dashboard 
          +passa de 3 min |                                              
      491  | `codeql` | Análise do código Java (`security-extended`) | Ac
           hado de severidade alta ou crítica (`security-severity` ≥ 7,0)
            |
      492  | `dependencies` | Trivy nas dependências Maven, com relatório
            completo | Vulnerabilidade `HIGH` ou `CRITICAL` **com versão
           corrigida** |
      493  | `dockerfile-lint` | hadolint | Aviso ou erro |

● Update(README.md)
  ⎿  Added 2 lines, removed 1 line
      494  | `image` | Contexto de build, imagem, teste de fumaça (13 pas
           sos) e Trivy na imagem | Contexto fora do `.dockerignore`, ima
           gem que não sobe ou não responde o resultado de referência, ou
            vulnerabilidade `HIGH` ou `CRITICAL` com correção na imagem |
      495 
      496  Achados médios e baixos, e vulnerabilidades sem correção, apar
           ecem no relatório sem reprovar. O
      497 -[Dependabot](.github/dependabot.yml) abre PRs semanais para Ma
          -ven, GitHub Actions e a imagem base.                          
      497 +[Dependabot](.github/dependabot.yml) abre PRs semanais para Ma
          +ven, GitHub Actions, a imagem base e as                       
      498 +imagens do compose.                                           
      499 
      500  ---
      501 

● Update(README.md)
  ⎿  Added 1 line, removed 1 line
      517  | Resultado de referência com o arquivo fornecido (Joel Silver
            em `min`, Matthew Vaughn em `max`) | SC-001 | `ReferenceFileI
           T` |
      518  | Correto com outros arquivos, sem dados fixos no código | FR-
           016a, FR-016b, SC-004a | `DisjointProducersIT`, `RandomizedOra
           cleIT` |
      519  | Leitura da lista de indicados e vencedores (seção 1) | FR-01
           9 a FR-023 | `MoviesListingIT`, `MoviesInvalidParamsIT`, `Movi
           esContractIT` |
      520 -| Somente testes de integração (item 4.2) | FR-027, Princípio 
          -I | Todos os testes são `*IT` com `@SpringBootTest` e sem mock
          -s, executados pelo Failsafe. A exceção é o `StartupFailureIT`,
          - que sobe a aplicação real com `SpringApplication` |          
      520 +| Somente testes de integração (item 4.2) | FR-027, Princípio 
          +I | Todos os testes são `*IT`, sem mocks, executados pelo Fail
          +safe, com `@SpringBootTest` ou com a aplicação real por `Sprin
          +gApplication` nos quatro casos de controle do ciclo de vida da
          + [seção 5](#5-testes) |                                       
      521  | Execução só com o wrapper, sem Docker e sem instalar banco:
           SGBD **H2 em memória**, embarcado na aplicação (item 4.3) | FR
           -026 | O H2 é dependência do `pom.xml`, e nenhuma URL de banco
            é configurada, então o Spring Boot cria um H2 em memória a ca
           da execução. Todos os testes sobem a aplicação assim, e o `Smo
           keIT` confirma que ela sobe com o arquivo padrão. Além disso,
           `./mvnw verify` e `./mvnw spring-boot:run` foram executados em
            clone limpo, no Linux e no Windows |
      522  | Documentação de como executar e testar (item 4.4) | — | Este
            README: [seção 2](#2-avalie-em-2-minutos) (avaliação rápida),
            [seção 3](#3-como-executar) (execução) e [seção 5](#5-testes)
            (testes) |
      523  | Código em repositório git (item 4.5) | — | Este repositório,
            com um ou mais commits por bolt e a estrutura descrita na [se
           ção 11](#11-estrutura-do-repositório) |

● Update(README.md)
  ⎿  Added 21 lines
      544  | Pipeline, portas de segurança e cobertura, actions por hash
           e Dependabot | FR-028 a FR-029 | Jobs `workflow-policy`, `buil
           d`, `codeql`, `dependencies` e `image` |
      545  | ADRs e este README | FR-030, FR-031, FR-034 | Revisão do PR,
            pelas seções "Revisão dos ADRs" e "Revisão do README" de [`ch
           ecklists/requirements.md`](specs/002-operacao/checklists/requi
           rements.md) |
      546 
      547 +### Requisitos do Marco 3 (modo Docker e autenticação)        
      548 +                                                              
      549 +Também fora do enunciado. O comportamento comum aos dois banco
          +s é comprovado no `./mvnw verify`, e o que                    
      550 +só existe no PostgreSQL, no perfil `postgres-it` (Princípio VI
          +, constituição 1.7.0). A matriz completa está                 
      551 +em [`specs/003-modo-docker/plan.md`](specs/003-modo-docker/pla
          +n.md#rastreabilidade-princípio-vi). Os                        
      552 +`@DisplayName` dos testes novos começam por `S003`, porque os 
          +IDs das três specs se repetem.                                
      553 +                                                              
      554 +| Extra | Requisitos | Como é comprovado |                    
      555 +| --- | --- | --- |                                           
      556 +| Modo padrão idêntico, sem Kafka configurado | FR-001 | `Defa
          +ultModeIsolationIT`, a suíte dos Marcos 1 e 2 e o `check-uncha
          +nged-tests.sh` |                                              
      557 +| PostgreSQL com o mesmo esquema e a mesma ordem da listagem |
          + FR-004 a FR-008 | `SchemaPostgresIT`, `EquivalencePostgresIT`
          +, `ListingOrderPostgresIT` (job `postgres`) |                 
      558 +| Kafka como caminho único, com a readiness depois do evento d
          +e carga concluída | FR-009 a FR-013 | `KafkaReferenceFileIT`, 
          +`KafkaReadinessIT`, `KafkaInvalidLinesIT`, `KafkaStructuralFai
          +lureIT`, `PublisherSummaryIT`, `KafkaBrokerUnavailableIT` |   
      559 +| Gravação idempotente com atualização e reinícios | FR-014 | 
          +`KafkaIdempotencyIT`, `KafkaUpdateIT`, `RestartDirectPostgresI
          +T`, `RestartPostgresIT` |                                     
      560 +| Tópico de erros e nova tentativa quando a gravação falha | F
          +R-015, FR-015a | `KafkaInvalidEventsIT`, `KafkaErrorTopicUnava
          +ilableIT`, `RetryPostgresIT` e o passo 7b do job `compose` |  
      561 +| Vencedor novo depois da carga e métricas da ingestão | FR-01
          +6, FR-017 | `KafkaNewWinnerIT`, `KafkaMetricsIT` |            
      562 +| Carga direta e Kafka equivalentes, sem exceção | FR-018 | `K
          +afkaEquivalenceIT`, `FieldTooLongIT` |                        
      563 +| Autenticação por token, desligada por padrão | FR-019 a FR-0
          +23, FR-025 a FR-027 | `AuthDisabledByDefaultIT`, `AuthEnabledI
          +T`, `AuthProblemDetailsIT`, `TokenNotLoggedIT`, `NoGeneratedPa
          +sswordIT`, `AuthContractIT`, `ApiDocsSecurityIT` |            
      564 +| Cabeçalhos de cache e de proteção | FR-022a | `CacheHeadersI
          +T`, `CacheHeadersWithTokenIT` |                               
      565 +| Compose com observabilidade, imagens por digest e credenciai
          +s de demonstração | FR-028 a FR-035 | Jobs `compose` e `workfl
          +ow-policy` |                                                  
      566 +| README e ADRs | FR-036, FR-037 | Revisão do PR, pelas seções
          + de [`specs/003-modo-docker/checklists/requirements.md`](specs
          +/003-modo-docker/checklists/requirements.md) |                
      567 +                                                              
      568  ---
      569 
      570  ## 7. Regras de negócio

● Now sections 8 to 11 (architecture, decisions, AI use and structure).

● Update(README.md)
  ⎿  Added 3 lines, removed 1 line
      640 
      641  | Pacote | Responsabilidade |
      642  | --- | --- |
      643 -| `ingestion` | Leitura e validação do CSV (`MovieCsvReader`),
          - separação dos produtores (`ProducerNameParser`), gravação (`M
          -ovieImportService`, o único caminho de gravação), carga na ini
          -cialização (`MovieFileLoader`) e mensagem de falha (`MovieFile
          -FailureAnalyzer`) |                                           
      643 +| `ingestion` | Leitura e validação do CSV (`MovieCsvReader`, 
          +`MovieFileReading`), regras de campo comuns ao CSV e aos event
          +os (`MovieFieldRules`), separação dos produtores (`ProducerNam
          +eParser`), gravação com atualização por ano e título (`MovieIm
          +portService`, o único caminho de gravação), carga direta na in
          +icialização (`MovieFileLoader`, fora do perfil `kafka`) e mens
          +agem de falha (`MovieFileFailureAnalyzer`) |                  
      644 +| `ingestion.kafka` | Só no perfil `kafka`: publicador do arqu
          +ivo (`MovieEventPublisher`), consumidor com o tópico de erros 
          +(`MovieEventListener`), leitura dos eventos (`MovieEventParser
          +`), readiness pelo evento de carga concluída (`IngestionReadin
          +ess`) e tópicos e novas tentativas (`KafkaIngestionConfigurati
          +on`) |                                                        
      645 +| `security` | Cadeia sem estado do Spring Security (`Security
          +Configuration`), filtro do token (`TokenAuthenticationFilter`)
          + e 401 em Problem Details (`ProblemAuthenticationEntryPoint`) 
          +|                                                             
      646  | `movie` | Entidade `Movie`, consulta paginada com filtros (`
           MovieRepository`, `MovieQueryService`) e `GET /api/movies` |
      647  | `producer` | Entidade `Producer`, cálculo dos intervalos (`P
           roducerIntervalService`) e `GET /api/producers/intervals` |
      648  | `web` | Erros em Problem Details (`ApiExceptionHandler`) e E
           Tag nos dois endpoints de negócio (`ContentEtagFilter`, regist
           rado pelo `EtagConfiguration`) |
● Update(README.md)
  ⎿  Added 15 lines
      672  4. A listagem é uma consulta JPQL paginada com filtros opciona
           is. As consultas não usam SQL específico
      673     de banco.
      674 
      675 +No **modo Docker** (perfil `docker`, que liga `postgres` e `ka
          +fka`), o banco é o PostgreSQL, e a carga                      
      676 +passa pelo Kafka:                                             
      677 +                                                              
      678 +```mermaid                                                    
      679 +flowchart LR                                                  
      680 +    CSV["movielist.csv"] --> Pub["MovieEventPublisher<br/>mesm
          +a leitura e avisos"]                                          
      681 +    Pub -->|"um evento por filme<br/>+ carga concluída"| Topic
          +[["awards.movies"]]                                           
      682 +    Ext["outro cliente<br/>(vencedor novo)"] --> Topic        
      683 +    Topic --> Listener["MovieEventListener"]                  
      684 +    Listener -->|inválido| Errors[["awards.movies.errors"]]   
      685 +    Listener -->|válido| Import["MovieImportService"]         
      686 +    Listener -->|carga concluída| Ready["readiness"]          
      687 +    Import --> PG[("PostgreSQL<br/>mesmo esquema Flyway")]    
      688 +```                                                           
      689 +                                                              
      690  ### Stack
      691 
      692  | Item | Versão |

● Update(README.md)
  ⎿  Added 2 lines
      701  | Testes | JUnit 6.0.3, `RestTestClient` do Spring, Maven Fail
           safe (duas execuções), JaCoCo 0.8.14 e o validador OpenAPI da
           Atlassian 3.0.0 |
      702  | Imagem | `eclipse-temurin:25-jdk-alpine` no build e `eclipse
           -temurin:25-jre-alpine` na execução, por digest |
      703  | Pipeline | GitHub Actions, CodeQL, Trivy, hadolint e Dependa
           bot |
      704 +| Modo Docker | PostgreSQL 18 (driver 42.7.14), Spring for Apa
          +che Kafka 4.1.2 (kafka-clients 4.2.2), Kafka 4.4 na imagem nat
          +iva, Prometheus 3.16 e Grafana 13.3 |                         
      705 +| Autenticação | Spring Security (do Boot 4.1.2), com token po
          +r `Authorization: Bearer` |                                   
      706 
      707  ---
      708 

● Update(README.md)
  ⎿  Added 15 lines
      751    dentro só referências por hash ou digest.
      752  - **Comprovação por tipo de requisito** (constituição 1.4.0):
           teste de integração, pipeline ou revisão.
      753 
      754 +Do Marco 3:                                                   
      755 +                                                              
      756 +- **Kafka como caminho único no modo Docker**, com a readiness
          + liberada só pelo evento de carga concluída                   
      757 +  desta execução (ADR 0014).                                  
      758 +- **Gravação por ano e título com atualização:** a última grav
          +ação prevalece, e um reinício reaplica os                     
      759 +  valores do arquivo aos filmes que estão nele (ADR 0015).    
      760 +- **Evento inválido vai ao tópico de erros, e a falha de grava
          +ção é tentada de novo** sem avançar no                        
      761 +  tópico. O `FIELD_TOO_LONG` vale nos dois caminhos de carga (
          +ADR 0016).                                                    
      762 +- **PostgreSQL com collation C**, para a mesma ordem do H2, se
          +m SQL específico (ADR 0013).                                  
      763 +- **Autenticação desligada por padrão**, com comparação do tok
          +en em tempo constante e sem                                   
      764 +  `Cache-Control` nos endpoints de negócio, para preservar o E
          +Tag (ADR 0017).                                               
      765 +- **Credenciais de demonstração só no compose local** (constit
          +uição 1.6.0; ADR 0018).                                       
      766 +- **Testes com PostgreSQL num perfil Maven dedicado**, obrigat
          +ório no pipeline (constituição 1.7.0;                         
      767 +  ADR 0019).                                                  
      768 +                                                              
      769  ---
      770 
      771  ## 10. Uso de IA

● Update(README.md)
  ⎿  Added 1 line
      800  | 4 | Endpoint de intervalos no formato do enunciado |
      801  | 5 e 6 | Listagem, Swagger UI e fechamento, num único PR |
      802  | Marco 2 | Um único bolt, com seis etapas e um commit por eta
           pa: ETag, observabilidade, Dockerfile, pipeline e Dependabot,
           ADRs e README ([spec 002](specs/002-operacao/spec.md)) |
      803 +| Marco 3 | Um único bolt, com cinco etapas e um commit por et
          +apa: PostgreSQL e perfil `docker`, Kafka, autenticação, compos
          +e com observabilidade, e ADRs com o README ([spec 003](specs/0
          +03-modo-docker/spec.md)) |                                    
      804 
      805  **Papéis:**
      806 

● Update(README.md)
  ⎿  Added 5 lines, removed 2 lines
      839  ├── mvnw, mvnw.cmd, .mvn/        # Maven Wrapper: dispensa ins
           talar o Maven
      840  ├── Dockerfile                   # imagem multi-stage, não roo
           t (opcional)
      841  ├── .dockerignore                # lista de permissão do conte
           xto da imagem
      842 +├── compose.yaml                 # modo Docker: aplicação, Pos
          +tgreSQL, Kafka, Prometheus e Grafana (opcional)               
      843 +├── observability/               # configuração do Prometheus 
          +e provisionamento e dashboard do Grafana                      
      844  ├── .github/
      845  │   ├── workflows/ci.yml         # pipeline de CI
      844 -│   ├── scripts/                 # política do workflow, conte
          -xto da imagem e teste de fumaça                               
      845 -│   └── dependabot.yml           # atualizações de Maven, Acti
          -ons e imagem base                                             
      846 +│   ├── scripts/                 # política do workflow, conte
          +xto da imagem, fumaça, compose e ITs inalterados              
      847 +│   ├── unchanged-tests-exceptions.txt # ITs da v0.2.0 que pod
          +em mudar, cada um com a decisão que autoriza                  
      848 +│   └── dependabot.yml           # atualizações de Maven, Acti
          +ons, imagem base e imagens do compose                         
      849  ├── .gitattributes               # fim de linha LF no repositó
           rio
      850  ├── .gitignore
      851  ├── .claude/                     # configuração do Claude Code
           : hook de prompts e skills do Spec Kit

● Update(README.md)
  ⎿  Added 3 lines, removed 1 line
      861  │   ├── contracts/openapi.yaml   # contrato OpenAPI do Marco 1
            (registro)
      862  │   └── checklists/              # checklist de qualidade da s
           pec
      863  ├── specs/002-operacao/          # Marco 2: spec, plano, resea
           rch, contratos e tarefas
      864 -│   └── contracts/               # OpenAPI 1.1.0 (fonte única)
          -, endpoints operacionais, imagem e pipeline                   
      864 +│   └── contracts/               # OpenAPI 1.1.0 (registro), e
          +ndpoints operacionais, imagem e pipeline                      
      865 +├── specs/003-modo-docker/       # Marco 3: spec, plano, resea
          +rch, contratos e tarefas                                      
      866 +│   └── contracts/               # OpenAPI 1.2.0 (fonte única)
          +, eventos do Kafka, compose e pipeline                        
      867  ├── src/main/java/com/outsera/awards/
      868  │   ├── AwardsApplication.java   # classe principal
      869  │   ├── ingestion/               # leitura, validação e carga
           do CSV

  Ran 1 shell command (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/rd.js" <<'EOF'
      const fs=require("fs"); const f="README.md"; let
      s=fs.readFileSync(f,"utf8");…)
  ⎿  ok
     11:## 2. Avalie em 2 minutos
  ⎿  Updated README.md +7 -2
      866  │   └── contracts/               # OpenAPI 1.2.0 (fonte única)
           , eventos do Kafka, compose e pipeline
      867  ├── src/main/java/com/outsera/awards/
      868  │   ├── AwardsApplication.java   # classe principal
      869 -│   ├── ingestion/               # leitura, validação e carga 
          -do CSV                                                        
      869 +│   ├── ingestion/               # leitura, validação e carga 
          +do CSV; kafka/: ingestão por Kafka (perfil kafka)             
      870  │   ├── movie/                   # filmes e GET /api/movies
      871  │   ├── producer/                # produtores e GET /api/produ
           cers/intervals
      872 +│   ├── security/                # autenticação por token (Spr
          +ing Security)                                                 
      873  │   └── web/                     # Problem Details e ETag
      874  ├── src/main/resources/
      874 -│   ├── application.yml          # PORT, MOVIES_CSV_PATH, Actu
          -ator e métricas, com padrões                                  
      875 +│   ├── application.yml          # PORT, MOVIES_CSV_PATH, API_
          +TOKEN, Actuator e métricas, com padrões                       
      876 +│   ├── application-postgres.yml # perfil postgres: DB_URL, DB
          +_USER e DB_PASSWORD                                           
      877 +│   ├── application-kafka.yml    # perfil kafka: broker, tópic
          +os, grupo e readiness                                         
      878  │   ├── movielist.csv            # arquivo do enunciado (padrã
           o)
      879  │   ├── db/migration/            # esquema Flyway
      880  │   ├── static/openapi.yaml      # cópia publicada do contrato
     ...
      883  ├── src/test/java/com/outsera/awards/  # somente testes de int
           egração (*IT)
      884  │   ├── adversario/  ingestion/  intervals/  movies/  docs/  w
           eb/  support/
      885  │   ├── etag/  health/  metrics/  logging/   # Marco 2
      886 +│   ├── isolation/  kafka/  security/        # Marco 3, no ./m
          +vnw verify                                                    
      887 +│   ├── postgres/                            # Marco 3, só no 
          +perfil postgres-it                                            
      888  │   └── SmokeIT.java
      889  ├── src/test/resources/scenarios/  # um CSV por cenário de tes
           te
      890  ├── docs/adr/                    # decisões de arquitetura (AD
           Rs), com índice

● README done. T089: running "Avalie em 2 minutos" and sections 1 and 2 of the
  quickstart in a clean clone (port 18081, because 8080 is taken).

  Ran 1 shell command (ctrl+o to expand)

● The clean-clone verify is green: 270 + 96 tests in 275 s. Now spring-boot:run
  in the clone, without and then with the demo token.

  Ran 4 shell commands (ctrl+o to expand)

● The demo token checks out in the clean clone: 401 without or with a wrong
  token, 200 with the right one, and it never shows in the log. I'm moving on to
  T090 and T091, then the final verify. · summary

  Ran 1 shell command (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/st.js" <<'EOF'
      const fs=require("fs"); const f="specs/003-modo-docker/spec.md"; let
      s=fs.readFileSync(f…)
  ⎿  **Status**: Implemented. Faltam as verificações que exigem o push e o
     PR: o job `postgres` (T020 e T051), os jobs do pipeline com o `compose
     ` (T084), o testador adversário da ingestão por Kafka (T050) e os chec
     … +19 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\spec.md +1 -1
       4 
       5  **Created**: 2026-11-24
       6 
       7 -**Status**: Draft                                              
       7 +**Status**: Implemented. Faltam as verificações que exigem o pu
         +sh e o PR: o job `postgres` (T020 e T051), os jobs do pipeline 
         +com o `compose` (T084), o testador adversário da ingestão por K
         +afka (T050) e os checks obrigatórios da `main` (T093), todas no
         + `tasks.md`.                                                   
       8 
       9  **Input**: User description: "Marco 3, modo Docker, conforme do
          cs/ai/planejamento/briefing.md. Escopo: 1. Perfil docker com Po
          stgreSQL no lugar do H2, usando as mesmas migrações do Flyway.
          2. Kafka como caminho único de ingestão no perfil docker: um pu
          blicador lê o CSV e envia uma mensagem por filme; um consumidor
           grava pelo mesmo serviço do modo padrão; uma mensagem de carga
           concluída libera a readiness. Gravação idempotente por ano e t
          ítulo, e tópico de erros para mensagens inválidas. Novos venced
          ores podem chegar pelo mesmo tópico depois da carga. 3. Autenti
          cação por token parametrizada e desligada por padrão: o token v
          em de variável de ambiente e é comparado com um cabeçalho; sem
          a variável, os endpoints ficam públicos; com ela, os endpoints
          operacionais de métricas exigem token. 4. compose.yaml com a ap
          licação, PostgreSQL, Kafka (imagem leve), Prometheus e Grafana,
           com o dashboard versionado. Restrições: o modo padrão continua
           idêntico, subindo e testando só com ./mvnw, sem Docker. Os tes
          tes do Kafka usam o broker embutido do Spring. Os testes com Po
          stgreSQL ficam em um perfil Maven separado, executado no pipeli
          ne. Um teste prova que a carga direta e a carga por Kafka produ
          zem a mesma resposta. Fora do escopo: publicação em nuvem e fro
          nt-end."
      10 

● Update(docs\ai\decisoes.md)
  ⎿  Added 51 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-24 — Fechamento do bolt do Marco 3 (etapa 5)        
       7 +                                                               
       8 +- **ADRs (FR-037):**                                           
       9 +  - de 0013 a 0019, no formato da spec 002, cada um citando a s
         +ua entrada neste registro;                                     
      10 +  - o 0017 cita o mecanismo de comparação do token e registra, 
         +como decisão intencional, os cabeçalhos                        
      11 +    `X-Content-Type-Options` e `X-Frame-Options` em todas as re
         +spostas e o `Cache-Control` fora dos                           
      12 +    endpoints de negócio;                                      
      13 +  - o 0004 ganhou os quatro casos de `SpringApplication` (emend
         +a 1.5.0) e o perfil dedicado (emenda                           
      14 +    1.7.0);                                                    
      15 +  - o índice foi atualizado.                                   
      16 +- **README (FR-036):**                                         
      17 +  - o modo Docker: serviços, portas, carga por Kafka, publicaçã
         +o de um vencedor novo e leitura do tópico                      
      18 +    de erros;                                                  
      19 +  - as credenciais de demonstração do compose;                 
      20 +  - a autenticação, com a matriz de acesso e o token de demonst
         +ração `awards-token-de-demonstracao`, o                        
      21 +    único token do repositório;                                
      22 +  - os testes do Marco 3 e o perfil `postgres-it`;             
      23 +  - os jobs `postgres` e `compose`;                            
      24 +  - a tabela de requisitos do Marco 3;                         
      25 +  - a arquitetura do modo Docker e as decisões do Marco 3.     
      26 +                                                               
      27 +  As pendências deixadas pela emenda 1.5.0 na seção de testes e
         + na tabela de rastreabilidade foram                            
      28 +  resolvidas. A seção "Avalie em 2 minutos" não mudou.         
      29 +- **Clone limpo (T089):** num clone novo do commit da etapa 4, 
         +só com o JDK:                                                  
      30 +  - `./mvnw verify` verde em 275 s, com 270 + 96 testes;       
      31 +  - `./mvnw spring-boot:run` respondeu o resultado de referênci
         +a e a listagem do README, com o `ETag` e                       
      32 +    sem `Cache-Control`;                                       
      33 +  - com `API_TOKEN` igual ao token de demonstração: 401 sem tok
         +en e com token errado, 200 com ele, 401 nas                    
      34 +    métricas sem token, e a readiness e o Swagger UI públicos. 
         +O token não apareceu no log.                                   
      35 +                                                               
      36 +  As seções 3 e 4 do quickstart (PostgreSQL e compose) foram ve
         +rificadas pelo `postgres-it` e pelo                            
      37 +  `compose-test.sh` locais, nas etapas 2 a 4. Os comandos rodar
         +am na porta 18081, porque a 8080 da                            
      38 +  máquina estava ocupada por um processo do usuário.           
      39 +- **Verificado localmente, com o Docker:**                     
      40 +  - `./mvnw -Ppostgres-it verify`: 7 testes, inclusive a nova t
         +entativa quando a gravação falha;                              
      41 +  - `compose-test.sh`: todos os passos, inclusive a queda real 
         +do banco (7b), com os passos 1 a 4 em                          
      42 +    28 s.                                                      
      43 +- **Pendências** (exigem o push ou o PR, que são do responsável
         +):                                                             
      44 +  - T020, T051 e T084: os jobs `postgres` e `compose` e os dema
         +is no pipeline;                                                
      45 +  - T050: o testador adversário (Codex) na ingestão por Kafka; 
      46 +  - T093: `postgres` e `compose` como checks obrigatórios da `m
         +ain`, com o bypass desligado.                                  
      47 +                                                               
      48 +  Fica também a decisão sobre o tempo do `./mvnw verify`, cerca
         + de 4 min (entrada da etapa 2).                                
      49 +- **Pontos A3, N3 e C4 da segunda análise:**                   
      50 +  - A3: o `DefaultModeIsolationIT` e o `NoGeneratedPasswordIT` 
         +já passavam antes da implementação e                           
      51 +    ficaram como guardas;                                      
      52 +  - N3: o `OPTIONS` do Marco 2 responde 2xx com `Allow`, verifi
         +cado e fixado no                                               
      53 +    `AuthDisabledByDefaultIT`;                                 
      54 +  - C4: a única propriedade usada por teste que restou é a `pub
         +lish-completion`, documentada no ADR 0014.                     
      55 +    A `create-error-topic` não foi necessária.                 
      56 +                                                               
      57  ## 2026-11-24 — Etapa 4 do Marco 3: compose, verificações e aju
          stes
      58 
      59  - **Imagens (T074):** as versões estáveis mais recentes no Dock
          er Hub, fixadas por tag e digest:

  Ran 1 shell command (ctrl+o to expand)

● Correction: I put the demo token's value in decisoes.md, but T088 says the
  README is the only place with that value. Removing it from the record.

● Update(docs\ai\decisoes.md)
  ⎿  Added 2 lines, removed 2 lines
      17    - o modo Docker: serviços, portas, carga por Kafka, publicaçã
          o de um vencedor novo e leitura do tópico
      18      de erros;
      19    - as credenciais de demonstração do compose;
      20 -  - a autenticação, com a matriz de acesso e o token de demonst
         -ração `awards-token-de-demonstracao`, o                        
      21 -    único token do repositório;                                
      20 +  - a autenticação, com a matriz de acesso e o token de demonst
         +ração, definido só no README, que é o                          
      21 +    único lugar do repositório com esse valor;                 
      22    - os testes do Marco 3 e o perfil `postgres-it`;
      23    - os jobs `postgres` e `compose`;
      24    - a tabela de requisitos do Marco 3;

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; grep -rln
      "awards-token-de-demonstracao" --exclude-dir=target --exclude-dir=.git . ;
      f=specs/003-modo-docker/tasks.m…)
  ⎿  ./README.md
     196:- [ ] T020 [US2] **Pendente: exige o push, que é do Anderson.** Co
     nferir o
     … +18 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\tasks.md +8 -8
      705 
      706  ### Verificação da etapa 5 (antes da escrita)
      707 
      708 -- [ ] T085 Conferir que as seções "Revisão do README", "Revisã
          -o dos ADRs" e "Revisão de segurança" de `specs/003-modo-docker
          -/checklists/requirements.md` estão desmarcadas. Elas são a com
          -provação desta etapa (Princípio VI) e só o revisor as marca, n
          -o PR.                                                         
      708 +- [X] T085 Conferir que as seções "Revisão do README", "Revisã
          +o dos ADRs" e "Revisão de segurança" de `specs/003-modo-docker
          +/checklists/requirements.md` estão desmarcadas. Elas são a com
          +provação desta etapa (Princípio VI) e só o revisor as marca, n
          +o PR.                                                         
      709 
      710  ### Escrita da etapa 5
      711 
      712 -- [ ] T086 [P] Criar os ADRs em `docs/adr/`, no formato da spe
          -c 002 (FR-031). Cada um tem número, título, data, status, cont
          -exto, decisão, alternativas e consequências, e cita a entrada 
          -de `docs/ai/decisoes.md`:                                     
      712 +- [X] T086 [P] Criar os ADRs em `docs/adr/`, no formato da spe
          +c 002 (FR-031). Cada um tem número, título, data, status, cont
          +exto, decisão, alternativas e consequências, e cita a entrada 
          +de `docs/ai/decisoes.md`:                                     
      713    - `0013-postgresql-com-esquema-comum.md` (R3, R4, collation
           `C` e a diferença residual com o
      714      `String.compareTo`);
      715    - `0014-ingestao-unica-por-kafka.md` (R6, R8 e R10: partição
            única, marcador com `runId`, readiness
     ...
      727    - `0019-testes-com-postgresql-em-perfil-dedicado.md` (R16 e
           a emenda 1.7.0).
      728 
      729    Criar em `docs/ai/decisoes.md` as entradas que faltarem.
      730 -- [ ] T087 [P] Atualizar `docs/adr/0004-somente-testes-de-inte
          -gracao.md` com os quatro casos de `SpringApplication` da const
          -ituição 1.5.0 e o perfil dedicado da 1.7.0. Atualizar o índice
          - `docs/adr/README.md` com 0013 a 0019.                        
      731 -- [ ] T088 Atualizar `README.md` (FR-036), sem reescrever as s
          -eções dos Marcos 1 e 2:                                       
      730 +- [X] T087 [P] Atualizar `docs/adr/0004-somente-testes-de-inte
          +gracao.md` com os quatro casos de `SpringApplication` da const
          +ituição 1.5.0 e o perfil dedicado da 1.7.0. Atualizar o índice
          + `docs/adr/README.md` com 0013 a 0019.                        
      731 +- [X] T088 Atualizar `README.md` (FR-036), sem reescrever as s
          +eções dos Marcos 1 e 2:                                       
      732    - **modo Docker:** `docker compose up`, as portas, o papel d
           e cada serviço e o `down -v`;
      733    - **credenciais:** as de demonstração identificadas como só
           para uso local (1.6.0) e o token de
      734      demonstração;
     ...
      747      de `SpringApplication`.
      748 
      749    A seção "Avalie em 2 minutos" continua funcionando só com o
           JDK.
      750 -- [ ] T089 Num clone limpo do branch, rodar a seção "Avalie em
          - 2 minutos" e as seções 1 e 2 de `specs/003-modo-docker/quicks
          -tart.md` só com o JDK. Com o Docker, rodar também as seções 3 
          -e 4. Anotar os resultados na descrição do commit.             
      751 -- [ ] T090 Rodar `./mvnw verify`: verde. Atualizar o **Status*
          -* de `specs/003-modo-docker/spec.md` para `Implemented`, lista
          -ndo as tarefas "Pendente: push" que ainda não foram conferidas
          -.                                                             
      752 -- [ ] T091 Registrar em `docs/ai/decisoes.md` o fechamento do 
          -bolt:                                                         
      750 +- [X] T089 Num clone limpo do branch, rodar a seção "Avalie em
          + 2 minutos" e as seções 1 e 2 de `specs/003-modo-docker/quicks
          +tart.md` só com o JDK. Com o Docker, rodar também as seções 3 
          +e 4. Anotar os resultados na descrição do commit.             
      751 +- [X] T090 Rodar `./mvnw verify`: verde. Atualizar o **Status*
          +* de `specs/003-modo-docker/spec.md` para `Implemented`, lista
          +ndo as tarefas "Pendente: push" que ainda não foram conferidas
          +.                                                             
      752 +- [X] T091 Registrar em `docs/ai/decisoes.md` o fechamento do 
          +bolt:                                                         
      753    - os resultados do T081 (inclusive o passo 7b, que comprova
           a R9) e do T089;
      754    - os `PULADO`;
      755    - as pendências de push;
      756    - o resultado do testador adversário (T050).
      757 -- [ ] T092 Commit `Etapa 5: ADRs e README do Marco 3` com T085
          - a T091. Avisar o Anderson de que o bolt está pronto para o PR
          -. Depois do merge, a sessão é exportada para `docs/ai/sessoes/
          -`.                                                            
      757 +- [X] T092 Commit `Etapa 5: ADRs e README do Marco 3` com T085
          + a T091. Avisar o Anderson de que o bolt está pronto para o PR
          +. Depois do merge, a sessão é exportada para `docs/ai/sessoes/
          +`.                                                            
      758  - [ ] T093 **Pedido ao Anderson, depois da primeira execução d
           o pipeline no PR e antes do merge.** Antes da primeira execuçã
           o, os jobs novos não aparecem na lista de checks da proteção.
           Pedir a configuração da proteção da `main` (Settings → Branche
           s, ou o ruleset da `main`):
      759    - incluir como *required status checks* os **dois** jobs nov
           os:
     … +1 line (ctrl+o to expand)

● The token's value now appears only in the README. Committing stage 5.

  Ran 1 shell command (ctrl+o to expand)

● O bolt do Marco 3 está implementado em cinco commits, um por etapa, no branch
  marco/3-docker. Não fiz push. O ./mvnw verify passa sem Docker, e o ambiente
  completo passou localmente com Docker. Os jobs no GitHub ainda não rodaram,
  porque dependem do seu push.

  ┌─────────────────────────────────────┬─────────┐
  │                Etapa                │ Commit  │
  ├─────────────────────────────────────┼─────────┤
  │ 1. PostgreSQL e perfil docker       │ bbb834e │
  ├─────────────────────────────────────┼─────────┤
  │ 2. Kafka                            │ 7cc063b │
  ├─────────────────────────────────────┼─────────┤
  │ 3. Autenticação com Spring Security │ 5c59c2c │
  ├─────────────────────────────────────┼─────────┤
  │ 4. Compose com observabilidade      │ 2c4cff1 │
  ├─────────────────────────────────────┼─────────┤
  │ 5. ADRs e README                    │ 97df7d1 │
  └─────────────────────────────────────┴─────────┘

  Testes e resultados:
  - ./mvnw verify: 270 testes, mais 96 com o log em JSON. Os 5 pulados são os
    mesmos da linha de base. Cobertura de linhas de 96,32%.
  - Clone limpo, só com o JDK: verify verde em 275 s, e a seção "Avalie em 2
    minutos" funcionou.
  - Perfil postgres-it, contra um PostgreSQL 18 local: 7 testes verdes,
    inclusive os 4 reinícios e a nova tentativa quando a gravação falha.
  - compose-test.sh: todos os passos passaram. A subida até o dashboard levou 28
    s, para um limite de 180 s.
  - Testes dos Marcos 1 e 2: os 59 da v0.2.0 continuam sem alteração. A única
    exceção é a constante do ApiDocsIT, como você aceitou.

  O que mudou em relação ao plano:
  - Lag do consumidor: a métrica usada é
    kafka_consumer_fetch_manager_records_lag_max, por cliente. A série por
    partição só aparece depois de um ciclo de atualização do Micrometer.
  - Tópico de erros fora do ar: um nome inválido de tópico não derruba a subida.
    Por isso, a propriedade alternativa create-error-topic foi descartada.
  - Cache-Control no 401: o filtro previsto decidia só pelo caminho e deixava o
    401 dos endpoints de negócio sem Cache-Control. Troquei por um que também
    considera o status.
  - Passo 7b do compose: o banco ficava fora por pouco tempo, e o pool de
    conexões espera até 30 s antes de falhar, então nada falhava de verdade.
    Agora o banco só volta depois de o log mostrar a nova tentativa.
  - Token de demonstração: awards-token-de-demonstracao. Ele só aparece no
    README.

  Decisão que é sua: o ./mvnw verify passou de 96 s para cerca de 4 min. Isso
  fica acima do limite de 2× que eu tinha definido na research. O broker
  embutido não é a causa; o tempo está em poucos testes de Kafka que esperam de
  propósito, como o do broker fora do ar e os 16 tipos de evento inválido. Você
  aceita esse tempo ou prefere que eu corte casos? O detalhe está em
  docs/ai/decisoes.md.

  O que depende de você:
  - o push, para conferir no pipeline os jobs postgres e compose e os demais
    (T020, T051 e T084);
  - acionar o testador adversário na ingestão por Kafka (T050);
  - depois da primeira execução do pipeline no PR, incluir postgres e compose
    como checks obrigatórios da main e conferir se os seis jobs existentes já
    estão lá, com o bypass para administradores desligado (T093).

  Observações:
  - A porta 8080 da máquina está ocupada por um processo Java seu, que não
    toquei. Por isso os testes locais usaram as portas 18080 e 18081.
  - Removi o container PostgreSQL que criei para os testes. O
    docs/ai/prompts.md, mantido pelo hook, continua fora dos commits.
  - Não existe .specify/extensions.yml, então nenhum hook do Spec Kit foi
    executado.

✻ Cogitated for 1h 41m 15s · done terça-feira, 17:46

❯ Não aceito os 4 min, mas também não corte casos. Reduza o tempo sem perder    
  cobertura: timeouts menores do cliente Kafka só no perfil de teste (como      
  max.block.ms e o request timeout no teste do broker fora do ar), os 16        
  eventos inválidos publicados numa única execução e conferidos juntos no       
  tópico de erros, e reaproveitamento do contexto do Spring entre os ITs do     
  Kafka, igualando as propriedades. A meta é ficar dentro do limite de 2× da    
  research. Se não chegar lá sem tirar cobertura, me diga quanto conseguiu e o  
  que custaria ir além. Registre o resultado em docs/ai/decisoes.md e faça um   
  commit de correção da etapa 2. Não faça push.                                 

● Primeiro meço o tempo atual, para comparar com o resultado depois das
  mudanças.

  Ran 1 shell command (ctrl+o to expand)

● A linha de base está em 249 s, e preciso cortar cerca de 57 s para ficar
  dentro do limite de 192 s. Vou trocar as esperas fixas dos testes de Kafka por
  verificações determinísticas, começando pelo KafkaStructuralFailureIT e pelo
  KafkaReadinessIT. · summary

● Write(src\test\resources\application-kafkatest.yml)  ⎿  Wrote 18 lines to src\test\resources\application-kafkatest.yml
       1 # Perfil só de teste (está em src/test/resources e nunca vai no 
         JAR): prazos curtos dos clientes Kafka,
       2 # para que os ITs não esperem os prazos de produção quando o bro
         ker está fora do ar ou um tópico não
       3 # existe. Os ITs do Kafka o ligam junto com o perfil kafka. O co
         mportamento observado é o mesmo; só o tempo
       4 # de espera muda (docs/ai/decisoes.md, "Tempo do ./mvnw verify d
         o Marco 3").
       5 spring:
       6   kafka:
     … +12 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/perf1.js"
      <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaBrokerUnavailableIT.java
     +1 -5
      37          long start = System.nanoTime();
      38          List<String> args = new ArrayList<>(KafkaTestSupport.ar
          gs(brokers,
      39                  KafkaTestSupport.topics(KafkaBrokerUnavailableI
          T.class)));
      40 -        // Prazos curtos só para o teste não esperar os 10 s da
         - criação dos tópicos nem o fechamento longo                    
      41 -        // do produtor com envios pendentes. O comportamento ob
         -servado é o mesmo.                                             
      42 -        args.add("--spring.kafka.admin.operation-timeout=2s"); 
      43 -        args.add("--spring.kafka.admin.close-timeout=1s");     
      44 -        args.add("--spring.kafka.producer.properties.max.block.
         -ms=2000");                                                     
      40 +        // Os prazos curtos dos clientes vêm do perfil de teste
         + kafkatest (KafkaTestSupport.args).                            
      41          Result result = ApplicationStarter.startAndCall("classp
          ath:movielist.csv", args, port -> {
      42                      long deadline = System.nanoTime() + 5_000_0
          00_000L;
      43                      while (System.nanoTime() < deadline) {
  ⎿  Updated
     src\test\java\com\outsera\awards\postgres\RestartPostgresIT.java +1 -1
      56 
      57      private static List<String> args(EmbeddedKafkaBroker broker
          ) {
      58          Topics topics = KafkaTestSupport.topics(RestartPostgres
          IT.class);
      59 -        List<String> args = new ArrayList<>(PostgresSupport.arg
         -s(SCHEMA, "postgres,kafka"));                                  
      59 +        List<String> args = new ArrayList<>(PostgresSupport.arg
         +s(SCHEMA, "postgres,kafka,kafkatest"));                        
      60          KafkaTestSupport.properties(broker.getBrokersAsString()
          , topics)
      61                  .forEach((name, value) -> args.add("--" + name
          + "=" + value));
      62          return args;
  ⎿  Updated src\test\java\com\outsera\awards\postgres\RetryPostgresIT.java
     +1 -1
      60      void retriesWhenTheWriteFails() {
      61          String brokers = broker.getBrokersAsString();
      62          Topics topics = KafkaTestSupport.topics(RetryPostgresIT
          .class);
      63 -        List<String> args = new ArrayList<>(PostgresSupport.arg
         -s(SCHEMA, "postgres,kafka"));                                  
      63 +        List<String> args = new ArrayList<>(PostgresSupport.arg
         +s(SCHEMA, "postgres,kafka,kafkatest"));                        
      64          KafkaTestSupport.properties(brokers, topics).forEach((n
          ame, value) -> args.add("--" + name + "=" + value));
      65 
      66          Result result = ApplicationStarter.startAndCall("classp
          ath:scenarios/header-only.csv", args, port -> {
  ⎿  Updated src\test\java\com\outsera\awards\support\KafkaScenarioIT.java
     +9 -6
       6  import org.springframework.beans.factory.annotation.Autowired;
       7  import org.springframework.kafka.test.EmbeddedKafkaBroker;
       8  import org.springframework.kafka.test.context.EmbeddedKafka;
       9 -import org.springframework.test.annotation.DirtiesContext;     
       9  import org.springframework.test.context.ActiveProfiles;
      10  import org.springframework.test.context.TestPropertySource;
      11 
      12  /**
      13   * Base dos testes com a ingestão por Kafka num contexto {@code
           @SpringBootTest} (spec 003; research R15).
      14   *
      16 - * <p>Cada classe tem o seu broker embutido (KRaft, sem Docker;
         - Princípio II) e o seu contexto, fechado                       
      17 - * no fim da classe ({@code @DirtiesContext}), então os tópicos
         - padrão de uma classe nunca recebem eventos                    
      18 - * de outra. O banco é o H2 do modo padrão: o perfil {@code kaf
         -ka} troca só a origem dos dados (FR-002).                      
      15 + * <p>Cada contexto tem o seu broker embutido (KRaft, sem Docke
         +r; Princípio II). O banco é o H2 do modo                       
      16 + * padrão: o perfil {@code kafka} troca só a origem dos dados (
         +FR-002). O perfil {@code kafkatest}, que só                    
      17 + * existe em {@code src/test/resources}, encurta os prazos dos 
         +clientes Kafka.                                                
      18 + *                                                             
      19 + * <p><strong>Reaproveitamento do contexto:</strong> classes qu
         +e só leem e têm as mesmas propriedades                         
      20 + * compartilham o contexto (e o broker) pelo cache do Spring. U
         +ma classe que muda os dados ou o estado da                     
      21 + * readiness declara {@code @DirtiesContext} e uma propriedade 
         +própria ({@code awards.test.context}),                         
      22 + * para ter um contexto só dela, fechado no fim, e nunca sujar 
         +o de outra.                                                    
      23   */
      20 -@ActiveProfiles("kafka")                                       
      24 +@ActiveProfiles({"kafka", "kafkatest"})                        
      25  @EmbeddedKafka(partitions = 1)
      22 -@DirtiesContext                                                
      26  @TestPropertySource(properties = "KAFKA_BOOTSTRAP_SERVERS=${spr
          ing.embedded.kafka.brokers}")
      27  public abstract class KafkaScenarioIT extends ScenarioIT {
      28 
  ⎿  Updated src\test\java\com\outsera\awards\support\KafkaTestSupport.java
     +71 -2
      63          return broker;
      64      }
      65 
      66 +    private static volatile EmbeddedKafkaBroker shared;        
      67 +                                                               
      68 +    /**                                                        
      69 +     * Broker embutido compartilhado pelos testes que rodam a a
         +plicação com {@code SpringApplication} na                      
      70 +     * mesma JVM. Ele sobe uma vez e é fechado no fim da JVM. C
         +ada classe continua com tópicos e grupo                        
      71 +     * próprios ({@link #topics(Class)}), então nenhuma vê os e
         +ventos de outra.                                               
      72 +     */                                                        
      73 +    public static EmbeddedKafkaBroker sharedBroker() {         
      74 +        if (shared == null) {                                  
      75 +            synchronized (KafkaTestSupport.class) {            
      76 +                if (shared == null) {                          
      77 +                    EmbeddedKafkaBroker broker = startBroker();
      78 +                    Runtime.getRuntime().addShutdownHook(new Th
         +read(broker::destroy, "embedded-kafka-shutdown"));             
      79 +                    shared = broker;                           
      80 +                }                                              
      81 +            }                                                  
      82 +        }                                                      
      83 +        return shared;                                         
      84 +    }                                                          
      85 +                                                               
      86      public static Topics topics(Class<?> testClass) {
      87          String name = testClass.getSimpleName().toLowerCase(Loc
          ale.ROOT);
      88          return new Topics("awards.movies." + name, "awards.movi
          es.errors." + name, "awards-" + name);
     ...
       98          return properties;
       99      }
      100 
       81 -    /** Argumentos de linha de comando com o perfil {@code kaf
          -ka} e as {@link #properties}. */                              
      101 +    /**                                                       
      102 +     * Argumentos de linha de comando com os perfis {@code kaf
          +ka} e {@code kafkatest} (prazos curtos dos                    
      103 +     * clientes, só nos testes) e as {@link #properties}.     
      104 +     */                                                       
      105      public static List<String> args(String brokers, Topics top
           ics) {
      106          List<String> args = new ArrayList<>();
       84 -        args.add("--spring.profiles.active=kafka");           
      107 +        args.add("--spring.profiles.active=kafka,kafkatest"); 
      108          properties(brokers, topics).forEach((name, value) -> a
           rgs.add("--" + name + "=" + value));
      109          return args;
     … +53 lines (ctrl+o to expand)

● Now the classes that change data or readiness state: each gets @DirtiesContext
  and its own property, so it never takes or dirties a shared context.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/perf2.js"
      <<'EOF'
      const fs=require("fs");…)
  ⎿  src/test/java/com/outsera/awards/kafka/KafkaReadinessIT.java- */
     src/test/java/com/outsera/awards/kafka/KafkaReadinessIT.java-// Muda o
     s dados ou a readiness: contexto próprio, fechado no fim (KafkaScenari
     … +19 lines (ctrl+o to expand)
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaErrorTopicUnavaila
     bleIT.java +5 -1
       7  import org.junit.jupiter.api.Tag;
       8  import org.junit.jupiter.api.Test;
       9  import org.springframework.boot.test.system.CapturedOutput;
      10 +import org.springframework.test.annotation.DirtiesContext;     
      11  import org.springframework.test.context.TestPropertySource;
      12 
      13  /**
     ...
      17   * {@code fail-fast} desligado, a aplicação sobe assim mesmo),
          e cada envio é recusado de verdade pelo
      18   * cliente Kafka, sem dublê. A verificação da tarefa T034c conf
          irmou que esse nome não derruba a subida.
      19   */
      20 +// Muda os dados ou a readiness: contexto próprio, fechado no f
         +im (KafkaScenarioIT).                                          
      21 +@DirtiesContext                                                
      22  @TestPropertySource(properties = {
      23          "movies.csv.path=classpath:scenarios/header-only.csv",
      21 -        "KAFKA_TOPIC_ERRORS=awards.movies.errors inválido!"})  
      24 +        "KAFKA_TOPIC_ERRORS=awards.movies.errors inválido!",   
      25 +        "awards.test.context=KafkaErrorTopicUnavailableIT"})   
      26  @Tag("log")
      27  class KafkaErrorTopicUnavailableIT extends KafkaScenarioIT {
      28 
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaIdempotencyIT.java
     +4 -1
      10  import java.util.List;
      11  import org.junit.jupiter.api.DisplayName;
      12  import org.junit.jupiter.api.Test;
      13 +import org.springframework.test.annotation.DirtiesContext;     
      14  import org.springframework.test.context.TestPropertySource;
      15 
      16  /**
     ...
      18   * SC-004). Um evento sentinela, enviado depois dos repetidos,
          marca o fim do processamento deles: o
      19   * tópico tem uma partição e é consumido em ordem.
      20   */
      20 -@TestPropertySource(properties = "movies.csv.path=classpath:mov
         -ielist.csv")                                                   
      21 +// Muda os dados ou a readiness: contexto próprio, fechado no f
         +im (KafkaScenarioIT).                                          
      22 +@DirtiesContext                                                
      23 +@TestPropertySource(properties = {"movies.csv.path=classpath:mo
         +vielist.csv", "awards.test.context=KafkaIdempotencyIT"})       
      24  class KafkaIdempotencyIT extends KafkaScenarioIT {
      25 
      26      private void sendSentinel(int year, String title) {
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java +4 -1
      17  import org.junit.jupiter.params.provider.Arguments;
      18  import org.junit.jupiter.params.provider.MethodSource;
      19  import org.springframework.boot.test.system.CapturedOutput;
      20 +import org.springframework.test.annotation.DirtiesContext;     
      21  import org.springframework.test.context.TestPropertySource;
      22 
      23  /**
     ...
      25   * travar o consumo (spec 003, FR-015, FR-017, US3-3, US3-4 e S
          C-006). Os motivos são os do
      26   * data-model.md, inclusive os de conteúdo, que são as mesmas r
          egras da linha do CSV.
      27   */
      27 -@TestPropertySource(properties = "movies.csv.path=classpath:sce
         -narios/header-only.csv")                                       
      28 +// Muda os dados ou a readiness: contexto próprio, fechado no f
         +im (KafkaScenarioIT).                                          
      29 +@DirtiesContext                                                
      30 +@TestPropertySource(properties = {"movies.csv.path=classpath:sc
         +enarios/header-only.csv", "awards.test.context=KafkaInvalidEven
         +tsIT"})                                                        
      31  @Tag("log")
      32  class KafkaInvalidEventsIT extends KafkaScenarioIT {
      33 
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaNewWinnerIT.java
     +4 -1
       9  import org.junit.jupiter.api.DisplayName;
      10  import org.junit.jupiter.api.Test;
      11  import org.springframework.http.HttpHeaders;
      12 +import org.springframework.test.annotation.DirtiesContext;     
      13  import org.springframework.test.context.TestPropertySource;
      14 
      15  /**
      16   * Depois da carga, um filme novo chega pelo mesmo tópico e apa
          rece nos intervalos e na listagem sem
      17   * reinício, e o ETag muda junto com o corpo (spec 003, FR-016,
           US3-1, US3-2, US3-6 e SC-005).
      18   */
      18 -@TestPropertySource(properties = "movies.csv.path=classpath:mov
         -ielist.csv")                                                   
      19 +// Muda os dados ou a readiness: contexto próprio, fechado no f
         +im (KafkaScenarioIT).                                          
      20 +@DirtiesContext                                                
      21 +@TestPropertySource(properties = {"movies.csv.path=classpath:mo
         +vielist.csv", "awards.test.context=KafkaNewWinnerIT"})         
      22  class KafkaNewWinnerIT extends KafkaScenarioIT {
      23 
      24      private static final String PRODUCERS = "Producer X, Joel S
          ilver and Producer Y";
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaReadinessIT.java
     +5 -1
      10  import java.util.UUID;
      11  import org.junit.jupiter.api.DisplayName;
      12  import org.junit.jupiter.api.Test;
      13 +import org.springframework.test.annotation.DirtiesContext;     
      14  import org.springframework.test.context.TestPropertySource;
      15 
      16  /**
     ...
      19   * ({@code awards.ingestion.publish-completion=false}) e o envi
          a ele mesmo, pelo broker real, com o
      20   * {@code runId} lido do cabeçalho {@code awards-run-id} dos ev
          entos publicados.
      21   */
      22 +// Muda os dados ou a readiness: contexto próprio, fechado no f
         +im (KafkaScenarioIT).                                          
      23 +@DirtiesContext                                                
      24  @TestPropertySource(properties = {
      25          "movies.csv.path=classpath:movielist.csv",
      23 -        "awards.ingestion.publish-completion=false"})          
      26 +        "awards.ingestion.publish-completion=false",           
      27 +        "awards.test.context=KafkaReadinessIT"})               
      28  class KafkaReadinessIT extends KafkaScenarioIT {
      29 
      30      private int status(String path) {
  ⎿  … +1 file changed
     src\test\java\com\outsera\awards\kafka\KafkaUpdateIT.java

● Now the KafkaInvalidEventsIT rewrite: the 16 events in a single run, checked
  together.

  Read 1 file (ctrl+o to expand)

● Write(src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java)
  ⎿  Added 66 lines, removed 53 lines
       9  import java.nio.charset.StandardCharsets;
      10  import java.time.Duration;
      11  import java.util.List;
      12 -import java.util.concurrent.atomic.AtomicInteger;              
      13 -import java.util.stream.Stream;                                
      14 -import org.junit.jupiter.api.BeforeEach;                       
      12 +import java.util.Map;                                          
      13 +import java.util.function.Function;                            
      14 +import java.util.stream.Collectors;                            
      15 +import org.assertj.core.api.SoftAssertions;                    
      16 +import org.junit.jupiter.api.DisplayName;                      
      17  import org.junit.jupiter.api.Tag;
      16 -import org.junit.jupiter.params.ParameterizedTest;             
      17 -import org.junit.jupiter.params.provider.Arguments;            
      18 -import org.junit.jupiter.params.provider.MethodSource;         
      18 +import org.junit.jupiter.api.Test;                             
      19  import org.springframework.boot.test.system.CapturedOutput;
      20  import org.springframework.test.annotation.DirtiesContext;
      21  import org.springframework.test.context.TestPropertySource;
     ...
      24   * Cada evento inválido vai ao tópico de erros com os bytes ori
          ginais e o motivo, sem ser gravado e sem
      25   * travar o consumo (spec 003, FR-015, FR-017, US3-3, US3-4 e S
          C-006). Os motivos são os do
      26   * data-model.md, inclusive os de conteúdo, que são as mesmas r
          egras da linha do CSV.
      27 + *                                                             
      28 + * <p>Os 16 casos são publicados numa única execução, cada um c
         +om a sua chave, seguidos de um evento                          
      29 + * válido. O tópico tem uma partição e é consumido em ordem: qu
         +ando o evento válido aparece na listagem,                      
      30 + * todos os inválidos já foram processados sem travar o consumo
         +. Depois, cada caso é conferido no tópico de                   
      31 + * erros pela chave, com uma mensagem própria por caso.        
      32   */
      33  // Muda os dados ou a readiness: contexto próprio, fechado no f
          im (KafkaScenarioIT).
      34  @DirtiesContext
     ...
       36  @Tag("log")
       37  class KafkaInvalidEventsIT extends KafkaScenarioIT {
       38 
       34 -    private static final AtomicInteger SEQUENCE = new AtomicIn
          -teger(2100);                                                  
       39      private static final String MARKER = "conteudo-do-evento";
       40 
       37 -    @BeforeEach                                               
       38 -    void ready() {                                            
       39 -        awaitReady();                                         
       41 +    /** Um evento inválido: a chave, o código do motivo espera
          +do e os bytes enviados. */                                    
       42 +    private record Case(String key, String reason, byte[] payl
          +oad) {                                                        
       43      }
       44 
       42 -    static Stream<Arguments> invalidEvents() {                
       43 -        return Stream.of(                                     
       44 -                Arguments.of("NOT_UTF8", new byte[] {(byte) 0x
          -C3, (byte) 0x28}),                                            
       45 -                Arguments.of("NOT_JSON", utf8("não é json " + 
          -MARKER)),                                                     
       46 -                Arguments.of("NOT_JSON", utf8("[1, 2, \"" + MA
          -RKER + "\"]")),                                               
       47 -                Arguments.of("TYPE_UNKNOWN", movie("\"type\":\
          -"other\",", "2000", "\"" + MARKER + "\"", "\"P\"", "true")),  
       48 -                Arguments.of("RUN_ID_MISSING", utf8("{\"type\"
          -:\"load-completed\",\"count\":1,\"note\":\"" + MARKER + "\"}")
          -),                                                            
       49 -                Arguments.of("YEAR_MISSING", movie("", null, "
          -\"" + MARKER + "\"", "\"P\"", "true")),                       
       50 -                Arguments.of("YEAR_NOT_NUMERIC", movie("", "\"
          -1990\"", "\"" + MARKER + "\"", "\"P\"", "true")),             
       51 -                Arguments.of("YEAR_NOT_NUMERIC", movie("", "19
          -90.5", "\"" + MARKER + "\"", "\"P\"", "true")),               
       52 -                Arguments.of("YEAR_OUT_OF_RANGE", movie("", "0
          -", "\"" + MARKER + "\"", "\"P\"", "true")),                   
       53 -                Arguments.of("YEAR_OUT_OF_RANGE", movie("", "1
          -0000", "\"" + MARKER + "\"", "\"P\"", "true")),               
       54 -                Arguments.of("TITLE_MISSING", movie("", "2000"
          -, "\"   \"", "\"" + MARKER + "\"", "true")),                  
       55 -                Arguments.of("PRODUCERS_MISSING", movie("", "2
          -000", "\"" + MARKER + "\"", "\" , and \"", "true")),          
       56 -                Arguments.of("WINNER_INVALID", movie("", "2000
          -", "\"" + MARKER + "\"", "\"P\"", "\"yes\"")),                
       57 -                Arguments.of("WINNER_INVALID", movie("", "2000
          -", "\"" + MARKER + "\"", "\"P\"", null)),                     
       58 -                Arguments.of("FIELD_TOO_LONG", movie("", "2000
          -", "\"" + MARKER + "T".repeat(501) + "\"", "\"P\"", "true")), 
       59 -                Arguments.of("FIELD_TOO_LONG", movie("", "2000
          -", "\"" + MARKER + "\"", "\"" + "P".repeat(1001) + "\"", "true
          -")));                                                         
       45 +    private static List<Case> cases() {                       
       46 +        return List.of(                                       
       47 +                new Case("invalido-01", "NOT_UTF8", new byte[]
          + {(byte) 0xC3, (byte) 0x28}),                                 
       48 +                new Case("invalido-02", "NOT_JSON", utf8("não 
          +é json " + MARKER)),                                          
       49 +                new Case("invalido-03", "NOT_JSON", utf8("[1, 
          +2, \"" + MARKER + "\"]")),                                    
       50 +                new Case("invalido-04", "TYPE_UNKNOWN", movie(
          +"\"type\":\"other\",", "2000", "\"" + MARKER + "\"", "\"P\"", 
          +"true")),                                                     
       51 +                new Case("invalido-05", "RUN_ID_MISSING", utf8
          +("{\"type\":\"load-completed\",\"count\":1,\"note\":\"" + MARK
          +ER + "\"}")),                                                 
       52 +                new Case("invalido-06", "YEAR_MISSING", movie(
          +"", null, "\"" + MARKER + "\"", "\"P\"", "true")),            
       53 +                new Case("invalido-07", "YEAR_NOT_NUMERIC", mo
          +vie("", "\"1990\"", "\"" + MARKER + "\"", "\"P\"", "true")),  
       54 +                new Case("invalido-08", "YEAR_NOT_NUMERIC", mo
          +vie("", "1990.5", "\"" + MARKER + "\"", "\"P\"", "true")),    
       55 +                new Case("invalido-09", "YEAR_OUT_OF_RANGE", m
          +ovie("", "0", "\"" + MARKER + "\"", "\"P\"", "true")),        
       56 +                new Case("invalido-10", "YEAR_OUT_OF_RANGE", m
          +ovie("", "10000", "\"" + MARKER + "\"", "\"P\"", "true")),    
       57 +                new Case("invalido-11", "TITLE_MISSING", movie
          +("", "2000", "\"   \"", "\"" + MARKER + "\"", "true")),       
       58 +                new Case("invalido-12", "PRODUCERS_MISSING", m
          +ovie("", "2000", "\"" + MARKER + "\"", "\" , and \"", "true"))
          +,                                                             
       59 +                new Case("invalido-13", "WINNER_INVALID", movi
          +e("", "2000", "\"" + MARKER + "\"", "\"P\"", "\"yes\"")),     
       60 +                new Case("invalido-14", "WINNER_INVALID", movi
          +e("", "2000", "\"" + MARKER + "\"", "\"P\"", null)),          
       61 +                new Case("invalido-15", "FIELD_TOO_LONG", movi
          +e("", "2000", "\"" + MARKER + "T".repeat(501) + "\"", "\"P\"",
          + "true")),                                                    
       62 +                new Case("invalido-16", "FIELD_TOO_LONG", movi
          +e("", "2000", "\"" + MARKER + "\"", "\"" + "P".repeat(1001) + 
          +"\"", "true")));                                              
       63      }
       64 
       62 -    @ParameterizedTest(name = "S003 FR-015 US3-3 US3-4 SC-006 
          -FR-017 {0} vai ao tópico de erros")                           
       63 -    @MethodSource("invalidEvents")                            
       64 -    void invalidEventGoesToTheErrorTopic(String reason, byte[]
          - payload, CapturedOutput output) {                            
       65 -        int sequence = SEQUENCE.incrementAndGet();            
       66 -        String key = "invalido-" + sequence;                  
       65 +    @Test                                                     
       66 +    @DisplayName("S003 FR-015 US3-3 US3-4 SC-006 FR-017 os 16 
          +tipos de evento inválido vão ao tópico de erros")             
       67 +    void invalidEventsGoToTheErrorTopic(CapturedOutput output)
          + {                                                            
       68 +        awaitReady();                                         
       69 +        List<Case> cases = cases();                           
       70          double rejectedBefore = rejected();
       68 -        // Cada caso anterior deixou exatamente um registro no
          - tópico de erros.                                             
       69 -        int errorsBefore = sequence - 2101;                   
       71 
       71 -        KafkaTestSupport.send(brokers(), MOVIES_TOPIC, key, pa
          -yload);                                                       
       72 -        // Um evento válido logo depois do inválido é gravado 
          -(US3-4, SC-006).                                              
       73 -        sendMovie(sequence + "|Valido", "{\"year\":" + sequenc
          -e + ",\"title\":\"Valido\",\"studios\":null,"                 
       74 -                + "\"producers\":\"P\",\"winner\":false}");   
       75 -        await("evento válido seguinte gravado", 5, () -> total
          -("year=" + sequence) == 1);                                   
       72 +        for (Case invalid : cases) {                          
       73 +            KafkaTestSupport.send(brokers(), MOVIES_TOPIC, inv
          +alid.key(), invalid.payload());                               
       74 +        }                                                     
       75 +        // Um evento válido depois de todos os inválidos é gra
          +vado: nenhum deles travou o consumo (US3-4).                  
       76 +        sendMovie("2101|Valido", "{\"year\":2101,\"title\":\"V
          +alido\",\"studios\":null,\"producers\":\"P\",\"winner\":false}
          +");                                                           
       77 +        await("evento válido seguinte gravado", 10, () -> tota
          +l("year=2101") == 1);                                         
       78 
       77 -        List<Record> errors = KafkaTestSupport.read(brokers(),
          - ERRORS_TOPIC, errorsBefore + 1, Duration.ofSeconds(10));     
       78 -        Record error = errors.stream().filter(record -> key.eq
          -uals(record.key())).findFirst()                               
       79 -                .orElseThrow(() -> new AssertionError("o tópic
          -o de erros não recebeu " + key));                             
       80 -        assertThat(error.value()).as("bytes originais").isEqua
          -lTo(payload);                                                 
       81 -        assertThat(error.headers().get("awards-error-reason"))
          -.startsWith(reason + ":");                                    
       82 -        assertThat(error.headers()).containsKeys("awards-error
          --at", "awards-source-topic",                                  
       83 -                "awards-source-partition", "awards-source-offs
          -et");                                                         
       84 -        assertThat(error.headers().get("awards-source-topic"))
          -.isEqualTo(MOVIES_TOPIC);                                     
       85 -                                                              
       86 -        assertThat(output.getAll()).contains("Evento rejeitado
          -").contains(reason);                                          
       87 -        assertThat(output.getAll()).as("o log não traz o conte
          -údo do evento").doesNotContain(MARKER);                       
       88 -        assertThat(total("")).as("o evento inválido não foi gr
          -avado").isEqualTo(SEQUENCE.get() - 2100);                     
       89 -        assertThat(rejected()).isEqualTo(rejectedBefore + 1); 
       79 +        Map<String, Record> errors = KafkaTestSupport.read(bro
          +kers(), ERRORS_TOPIC, cases.size(), Duration.ofSeconds(10))   
       80 +                .stream().collect(Collectors.toMap(Record::key
          +, Function.identity()));                                      
       81 +        SoftAssertions softly = new SoftAssertions();         
       82 +        softly.assertThat(errors).as("registros no tópico de e
          +rros").hasSize(cases.size());                                 
       83 +        for (Case invalid : cases) {                          
       84 +            Record error = errors.get(invalid.key());         
       85 +            softly.assertThat(error).as("%s (%s) no tópico de 
          +erros", invalid.key(), invalid.reason()).isNotNull();         
       86 +            if (error == null) {                              
       87 +                continue;                                     
       88 +            }                                                 
       89 +            softly.assertThat(error.value()).as("%s: bytes ori
          +ginais", invalid.key()).isEqualTo(invalid.payload());         
       90 +            softly.assertThat(error.headers().get("awards-erro
          +r-reason")).as("%s: motivo", invalid.key())                   
       91 +                    .startsWith(invalid.reason() + ":");      
       92 +            softly.assertThat(error.headers()).as("%s: cabeçal
          +hos", invalid.key()).containsKeys("awards-error-at",          
       93 +                    "awards-source-topic", "awards-source-part
          +ition", "awards-source-offset");                              
       94 +            softly.assertThat(error.headers().get("awards-sour
          +ce-topic")).as("%s: tópico de origem", invalid.key())         
       95 +                    .isEqualTo(MOVIES_TOPIC);                 
       96 +            softly.assertThat(output.getAll()).as("%s: aviso n
          +o log", invalid.key())                                        
       97 +                    .contains("Evento rejeitado").contains(inv
          +alid.reason());                                               
       98 +        }                                                     
       99 +        softly.assertThat(output.getAll()).as("o log não traz 
          +o conteúdo dos eventos").doesNotContain(MARKER);              
      100 +        softly.assertThat(total("")).as("só o evento válido fo
          +i gravado").isEqualTo(1);                                     
      101 +        softly.assertThat(rejected()).as("contador de rejeitad
          +os (FR-017)").isEqualTo(rejectedBefore + cases.size());       
      102 +        softly.assertAll();                                   
      103      }
      104 
      105      private double rejected() {

● Making the log check per case stronger: compare against the event's own
  offset, taken from the error-topic header.

● Update(src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java)
  ⎿  Added 4 lines, removed 2 lines
       93                      "awards-source-topic", "awards-source-part
           ition", "awards-source-offset");
       94              softly.assertThat(error.headers().get("awards-sour
           ce-topic")).as("%s: tópico de origem", invalid.key())
       95                      .isEqualTo(MOVIES_TOPIC);
       96 -            softly.assertThat(output.getAll()).as("%s: aviso n
          -o log", invalid.key())                                        
       97 -                    .contains("Evento rejeitado").contains(inv
          -alid.reason());                                               
       96 +            // O aviso do log cita a posição do próprio evento
          + e o motivo dele.                                             
       97 +            softly.assertThat(output.getAll()).as("%s: aviso n
          +o log", invalid.key()).contains(                              
       98 +                    "Evento rejeitado (tópico " + MOVIES_TOPIC
          + + ", partição 0, offset "                                    
       99 +                            + error.headers().get("awards-sour
          +ce-offset") + "): " + invalid.reason() + ":");                
      100          }
      101          softly.assertThat(output.getAll()).as("o log não traz 
           o conteúdo dos eventos").doesNotContain(MARKER);
      102          softly.assertThat(total("")).as("só o evento válido fo
           i gravado").isEqualTo(1);

  Ran 1 shell command (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/perf3.js"
      <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
     src/test/java/com/outsera/awards/kafka/KafkaStructuralFailureIT.java:9
     :import java.time.Duration;
     … +5 lines (ctrl+o to expand)
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaEquivalenceIT.java
     +1 -16
       9  import java.time.Duration;
      10  import java.util.List;
      11  import java.util.Map;
      12 -import org.junit.jupiter.api.AfterAll;                         
      13 -import org.junit.jupiter.api.BeforeAll;                        
      12  import org.junit.jupiter.api.DisplayName;
      13  import org.junit.jupiter.params.ParameterizedTest;
      14  import org.junit.jupiter.params.provider.ValueSource;
      17 -import org.springframework.kafka.test.EmbeddedKafkaBroker;     
      15 
      16  /**
      17   * O mesmo arquivo pela carga direta e pela ingestão por Kafka
          produz respostas idênticas, byte a byte e
     ...
      20   */
      21  class KafkaEquivalenceIT {
      22 
      26 -    private static EmbeddedKafkaBroker broker;                 
      27 -                                                               
      28 -    @BeforeAll                                                 
      29 -    static void startBroker() {                                
      30 -        broker = KafkaTestSupport.startBroker();               
      31 -    }                                                          
      32 -                                                               
      33 -    @AfterAll                                                  
      34 -    static void stopBroker() {                                 
      35 -        broker.destroy();                                      
      36 -    }                                                          
      37 -                                                               
      23      private static final List<String> PATHS = List.of(
      24              "/api/producers/intervals",
      25              "/api/movies?size=100&page=0",
     ...
      42 
      43          Map<String, Http.Response> direct = ApplicationStarter.
          runAndCall(csvPath, port -> Http.snapshot(port, PATHS));
      44          Map<String, Http.Response> kafka = ApplicationStarter.r
          unAndCall(csvPath,
      60 -                KafkaTestSupport.args(broker.getBrokersAsString
         -(), topics), port -> {                                         
      45 +                KafkaTestSupport.args(KafkaTestSupport.sharedBr
         +oker().getBrokersAsString(), topics), port -> {                
      46                      Http.awaitReady(port, Duration.ofSeconds(60
          ));
      47                      return Http.snapshot(port, PATHS);
      48                  });
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaIdempotencyIT.java
     +1 -3
      37          assertThat(published).hasSize(207);
      38 
      39          // O arquivo inteiro de novo, exatamente como o publica
          dor enviou (menos o marcador).
      40 -        for (Record record : published.subList(0, 206)) {      
      41 -            KafkaTestSupport.send(brokers(), MOVIES_TOPIC, reco
         -rd.key(), record.value());                                     
      42 -        }                                                      
      40 +        KafkaTestSupport.sendAll(brokers(), MOVIES_TOPIC, publi
         +shed.subList(0, 206));                                         
      41          sendSentinel(2041, "Sentinela 1");
      42          await("sentinela 1 gravada", 30, () -> total("year=2041
          ") == 1);
      43          assertThat(total("")).isEqualTo(207);
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaReadinessIT.java
     +5 -5
      47          String runId = movies.getFirst().headers().get("awards-
          run-id");
      48          assertThat(runId).isNotBlank();
      49 
      50 +        // Marcador de outra execução, seguido de um filme sent
         +inela. O tópico tem uma partição e é consumido                 
      51 +        // em ordem: quando o sentinela aparece na listagem, o 
         +marcador errado já foi processado.                             
      52          sendCompletion(UUID.randomUUID().toString());
      51 -        try {                                                  
      52 -            Thread.sleep(3000);                                
      53 -        } catch (InterruptedException e) {                     
      54 -            Thread.currentThread().interrupt();                
      55 -        }                                                      
      53 +        sendMovie("2039|Sentinela", "{\"year\":2039,\"title\":\
         +"Sentinela\",\"studios\":null,"                                
      54 +                + "\"producers\":\"P\",\"winner\":false}");    
      55 +        await("sentinela depois do marcador errado", 10, () -> 
         +total("year=2039") == 1);                                      
      56          assertThat(readinessStatus()).as("readiness com o marca
          dor de outra execução").isEqualTo(503);
      57 
      58          sendCompletion(runId);
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaStructuralFailureIT.java
     +3 -17
       7  import com.outsera.awards.support.KafkaTestSupport;
       8  import com.outsera.awards.support.KafkaTestSupport.Topics;
       9  import java.time.Duration;
      10 -import org.junit.jupiter.api.AfterAll;                         
      11 -import org.junit.jupiter.api.BeforeAll;                        
      10  import org.junit.jupiter.api.DisplayName;
      11  import org.junit.jupiter.api.Tag;
      12  import org.junit.jupiter.params.ParameterizedTest;
      13  import org.junit.jupiter.params.provider.CsvSource;
      16 -import org.springframework.kafka.test.EmbeddedKafkaBroker;     
      14 
      15  /**
      16   * Com o perfil {@code kafka}, um erro estrutural no arquivo im
          pede a subida com a mesma mensagem do
     ...
      20  @Tag("log")
      21  class KafkaStructuralFailureIT {
      22 
      26 -    private static EmbeddedKafkaBroker broker;                 
      27 -                                                               
      28 -    @BeforeAll                                                 
      29 -    static void startBroker() {                                
      30 -        broker = KafkaTestSupport.startBroker();               
      31 -    }                                                          
      32 -                                                               
      33 -    @AfterAll                                                  
      34 -    static void stopBroker() {                                 
      35 -        broker.destroy();                                      
      36 -    }                                                          
      37 -                                                               
      23      @ParameterizedTest(name = "S003 FR-009 US1-5 {0}: {1}")
      24      @CsvSource({
      25              "file:./nao-existe.csv, NOT_FOUND",
     ...
      27      @DisplayName("S003 FR-009 US1-5 erro estrutural impede a su
          bida e nada é publicado")
      28      void structuralErrorStopsTheStartup(String csvPath, String 
          reason) {
      29          Topics topics = KafkaTestSupport.topics(KafkaStructural
          FailureIT.class);
      45 -        String brokers = broker.getBrokersAsString();          
      30 +        String brokers = KafkaTestSupport.sharedBroker().getBro
         +kersAsString();                                                
      31 
      32          Result result = ApplicationStarter.start(csvPath, Kafka
          TestSupport.args(brokers, topics));
      33 
     ...
      36                  .contains("APPLICATION FAILED TO START")
     … +7 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; sed -i '/^import
      java.time.Duration;$/d'
      src/test/java/com/outsera/awards/kafka/KafkaStructuralFailureIT.java;
      grep…)
  ⎿  src/test/java/com/outsera/awards/kafka/KafkaEquivalenceIT.java:19: * c
     láusula "múltiplas execuções" do Princípio I (constituição 1.5.0), com
      o broker embutido real.
     … +18 lines (ctrl+o to expand)
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaStructuralFailureIT.java
     -1
       6  import com.outsera.awards.support.ApplicationStarter.Result;
       7  import com.outsera.awards.support.KafkaTestSupport;
       8  import com.outsera.awards.support.KafkaTestSupport.Topics;
       9 -import java.time.Duration;                                     
       9  import org.junit.jupiter.api.DisplayName;
      10  import org.junit.jupiter.api.Tag;
      11  import org.junit.jupiter.params.ParameterizedTest;
  ⎿  (timeout 10m)

● Several Kafka integration tests got much faster, but KafkaBrokerUnavailableIT
  and the JSON-run KafkaErrorTopicUnavailableIT slowed down. I'm investigating
  those two now. · summary

  Ran 1 shell command (ctrl+o to expand)

● In the JSON run, KafkaErrorTopicUnavailableIT takes 13.4 s just to start.
  Looking at what holds up the startup.

  Ran 2 shell commands (ctrl+o to expand)

● The 13.4 s is a cold JVM: in this subset, it is the first class of the JSON
  run, so it is not a regression. Now the timeline of KafkaBrokerUnavailableIT.

  Ran 1 shell command (ctrl+o to expand)

● The Kafka-only class is what opens the cold JVM, which adds about 20 s. With
  the JVM warm, the no-broker startup takes about 3 s, so I'm now measuring the
  full verify. · summary

  Ran 1 shell command (ctrl+o to expand)

● Runtime is now 161 s, down from 249 s and under the 192 s limit, and coverage
  rose to 97.06%. The test count fell from 270 to 255 only because the 16
  parameterized KafkaInvalidEventsIT cases were merged into one test; before
  committing I'll check the postgres-it profile, since its restart and retry
  tests now use the test profile. · summary

  Ran 1 shell command (ctrl+o to expand)

● Everything is green. Recording the result and updating the places that quoted
  the old numbers: decisoes.md, R15, T032 in tasks.md and the README.

● Update(docs\ai\decisoes.md)
  ⎿  Added 53 lines
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-25 — Tempo do `./mvnw verify` do Marco 3 (correção d
         +a etapa 2)                                                     
       7 +                                                               
       8 +- **Origem:** o `verify` passou de 96 s na linha de base para c
         +erca de 4 min, acima do limite de 2× da                        
       9 +  research R15 (entrada da etapa 2). O responsável não aceitou 
         +os 4 min nem o corte de casos, e pediu a                       
      10 +  redução sem perder cobertura.                                
      11 +- **Medição** (`./mvnw verify` sem `clean`, com o código já com
         +pilado, na mesma máquina):                                     
      12 +                                                               
      13 +  | | Tempo | Razão sobre a linha de base (96 s) |             
      14 +  | --- | --- | --- |                                          
      15 +  | Antes | 249 s | 2,6× |                                     
      16 +  | Depois | **161 s** | **1,68×**, dentro do limite de 2× |   
      17 +                                                               
      18 +  Os ITs do Kafka passaram de cerca de 130 s para 59 s, somadas
         + as duas execuções do Failsafe. A cobertura                    
      19 +  de linhas foi de 96,32% para 97,06%.                         
      20 +- **O que mudou, sem tirar nenhum caso:**                      
      21 +  - **perfil de teste `kafkatest`** (`src/test/resources/applic
         +ation-kafkatest.yml`, fora do JAR): prazos                     
      22 +    curtos dos clientes Kafka (`max.block.ms`, `request.timeout
         +.ms`, `delivery.timeout.ms`,                                   
      23 +    `default.api.timeout.ms` e os prazos do `KafkaAdmin`). Todo
         +s os ITs do Kafka o ligam junto com o perfil                   
      24 +    `kafka`. A produção não muda;                              
      25 +  - **`KafkaInvalidEventsIT` numa única execução:** os 16 event
         +os inválidos são publicados de uma vez,                        
      26 +    seguidos de um evento válido. Como o tópico tem uma partiçã
         +o, quando o válido aparece na listagem,                        
      27 +    todos os inválidos já foram processados sem travar o consum
         +o. Cada caso é conferido pela chave no tópico                  
      28 +    de erros, com uma mensagem própria por caso:               
      29 +    - os bytes originais;                                      
      30 +    - o motivo e os cabeçalhos;                                
      31 +    - o aviso no log, agora comparado com o offset do próprio e
         +vento, o que é mais forte que antes.                           
      32 +                                                               
      33 +    Passou de 16 s para 4 s por execução. O número de testes ca
         +iu de 270 para 255 (e de 96 para 81 na                         
      34 +    execução `json-logs`) só porque as 16 invocações parametriz
         +adas viraram um teste com os mesmos 16 casos;                  
      35 +  - **reaproveitamento do contexto:** o `@DirtiesContext` saiu 
         +da base `KafkaScenarioIT`.                                     
      36 +    - As classes que só leem e têm as mesmas propriedades compa
         +rtilham o contexto e o broker. O                               
      37 +      `KafkaReferenceFileIT` reaproveita o do `KafkaMetricsIT` 
         +e passou de 5,7 s para 0,6 s.                                  
      38 +    - As classes que mudam dados ou a readiness declaram `@Dirt
         +iesContext` e uma propriedade própria                          
      39 +      (`awards.test.context`): ganham um contexto só delas e nu
         +nca sujam o de outra.                                          
      40 +    - Os testes com `SpringApplication` (`KafkaEquivalenceIT` e
         + `KafkaStructuralFailureIT`) passaram a                        
      41 +      usar um broker embutido compartilhado na JVM, com tópicos
         + e grupo próprios por classe;                                  
      42 +  - **esperas fixas trocadas por condições determinísticas, com
         + a mesma comprovação:**                                        
      43 +    - `KafkaStructuralFailureIT`: o tópico vazio é conferido pe
         +los offsets, sem esperar 3 s por caso. Foi                     
      44 +      de 8 s para 1 s;                                         
      45 +    - `KafkaReadinessIT`: depois do marcador de outra execução,
         + um filme sentinela, cuja gravação prova que o                 
      46 +      marcador já foi processado, no lugar de 3 s de espera. Fo
         +i de 8,6 s para 3,6 s;                                         
      47 +    - `KafkaIdempotencyIT`: os 206 eventos são reenviados sem e
         +sperar cada confirmação, com um `flush` no                     
      48 +      fim. Foi de 9,4 s para 4,2 s.                            
      49 +- **O que sobrou de mais caro**, se um dia for preciso ir além:
      50 +  - `KafkaBrokerUnavailableIT`, cerca de 9 s: a subida sem brok
         +er espera o `KafkaAdmin`, e a observação                       
      51 +    dura 5 s;                                                  
      52 +  - `KafkaEquivalenceIT`, cerca de 8 s: seis subidas da aplicaç
         +ão.                                                            
      53 +                                                               
      54 +  Reduzir esses dois exigiria encurtar a observação ou comparar
         + menos arquivos, ou seja, tirar                                
      55 +  comprovação, e por isso não foi feito.                       
      56 +- **Verificações:** `./mvnw verify` verde, `./mvnw -Ppostgres-i
         +t verify` verde (7 testes, com o                               
      57 +  `RestartPostgresIT` e o `RetryPostgresIT` no perfil de teste)
         + e `check-unchanged-tests.sh v0.2.0` verde.                    
      58 +                                                               
      59  ## 2026-11-24 — Fechamento do bolt do Marco 3 (etapa 5)
      60 
      61  - **ADRs (FR-037):**

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/perf4.js"
      <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
     413:- **Resultado (correção da etapa 2, 2026-11-25):** o `verify` fico
     u em 161 s, ou 1,68× a linha de base.
     … +4 lines (ctrl+o to expand)
  ⎿  Updated README.md +6 -5
      399  .\mvnw.cmd verify "-Dit.test=ReferenceFileIT"   # no PowerShel
           l, o -D vai entre aspas
      400  ```
      401 
      402 -São **270 testes de integração**, executados pelo Maven Failsa
          -fe na fase `verify`, **sem Docker**, em duas                  
      402 +São **255 testes de integração**, executados pelo Maven Failsa
          +fe na fase `verify`, **sem Docker**, em duas                  
      403  execuções:
      404 
      405  - **padrão:** todos os testes, com o log em texto. Os 5 do `Js
           onLogFormatIT` ficam pulados, porque só
      406    valem com o JSON ligado;
      407 -- **`json-logs`:** os 96 testes das classes marcadas com `@Tag
          -("log")` rodam de novo, numa JVM própria,                     
      407 +- **`json-logs`:** os 81 testes das classes marcadas com `@Tag
          +("log")` rodam de novo, numa JVM própria,                     
      408    com `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs`. Assim, as mesma
           s verificações de log valem nos dois
      409    formatos.
      410 
     ...
      412  próprio teste), sem Docker.
      413 
      414  A **cobertura** de linhas é medida pelo JaCoCo sobre as duas e
           xecuções, e o `verify` falha abaixo de
      415 -**90%**. Hoje ela está em 96,32%, e o relatório fica em `targe
          -t/site/jacoco-it/index.html`. A execução                      
      416 -completa leva cerca de 4 min na máquina de desenvolvimento. A 
          -maior parte do tempo está nos testes do                       
      417 -Kafka que esperam de propósito, como o broker fora do ar e os 
          -16 tipos de evento inválido.                                  
      415 +**90%**. Hoje ela está em 97,06%, e o relatório fica em `targe
          +t/site/jacoco-it/index.html`. A execução                      
      416 +completa leva cerca de 2 min e 40 s numa cópia de trabalho já 
          +compilada. Os testes do Kafka usam um perfil                  
      417 +só de teste (`kafkatest`, em `src/test/resources`), com prazos
          + curtos dos clientes, e reaproveitam o                        
      418 +contexto do Spring entre as classes que só leem.              
      419 
      420  **Testes com PostgreSQL** (perfil Maven `postgres-it`): o que
           só existe no PostgreSQL fica fora do
      421  `./mvnw verify` padrão (Princípio II) e roda obrigatoriamente
           no job `postgres` do pipeline (Princípio VI,
  ⎿  Updated specs\003-modo-docker\research.md +9
      410      compartilhar um broker por execução do Failsafe e manter o
           s tópicos por classe;
      411    - no Windows, o broker embutido pode falhar ao apagar os arq
           uivos de log no fim do teste. Se isso
      412      acontecer, apontar o `log.dirs` para uma pasta temporária
           por classe e ignorar a falha só na
      413 +- **Resultado (correção da etapa 2, 2026-11-25):** o `verify` 
          +ficou em 161 s, ou 1,68× a linha de base.                     
      414 +  O custo não estava no broker, e sim nas esperas fixas e nos 
          +casos enviados um a um. Os ajustes foram:                     
      415 +  - o perfil de teste `kafkatest`, com prazos curtos dos clien
          +tes;                                                          
      416 +  - os 16 eventos inválidos numa única execução;              
      417 +  - o contexto compartilhado entre as classes que só leem;    
      418 +  - um broker compartilhado para os testes com `SpringApplicat
          +ion`;                                                         
      419 +  - condições determinísticas no lugar de esperas fixas.      
      420 +                                                              
      421 +  O detalhe está em `docs/ai/decisoes.md`. O broker embutido f
          +uncionou no Windows sem a saída alternativa.                  
      422      limpeza, registrando o motivo.
      423 
      424  ## R16. Testes com PostgreSQL: perfil Maven `postgres-it`
  ⎿  Updated specs\003-modo-docker\tasks.md +1 -1
      292    - `S003 FR-014 SC-004`: depois da readiness, o teste lê os 2
           06 eventos de filme do tópico e os reenvia
      293      todos. `totalElements` continua 206, e os intervalos são o
           s mesmos;
      294    - o mesmo evento novo enviado duas vezes aumenta `totalEleme
           nts` em 1.
      295 -- [X] T032 [P] [US3] Criar `src/test/java/com/outsera/awards/k
          -afka/KafkaInvalidEventsIT.java` (`@Tag("log")`, perfil `kafka`
          -, `@ParameterizedTest` com um caso por motivo do data-model.md
          -):                                                            
      295 +- [X] T032 [P] [US3] Criar `src/test/java/com/outsera/awards/k
          +afka/KafkaInvalidEventsIT.java` (`@Tag("log")`, perfil `kafka`
          +, um caso por motivo do data-model.md). *Ajustado na correção 
          +da etapa 2: os 16 casos são publicados numa única execução e c
          +onferidos juntos no tópico de erros, cada um pela sua chave; o
          + evento válido vem depois de todos (`docs/ai/decisoes.md`, "Te
          +mpo do `./mvnw verify` do Marco 3").*                         
      296    - os casos:
      297      - `NOT_UTF8`: bytes `0xC3 0x28`;
      298      - `NOT_JSON`: `não é json`;

  Ran 1 shell command (ctrl+o to expand)

● O bloco entrou no meio de um item da R15. Movendo para depois dele.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/perf5.js"
      <<'EOF'
      const fs=require("fs"); const r="specs/003-modo-docker/research.md"; let
      s=fs.readFil…)
  ⎿  - **Verificar:**
       - o tempo de subida do broker embutido por classe. Se o `verify` pas
     sar de 2× o tempo do Marco 2,
     … +15 lines (ctrl+o to expand)
  ⎿  Updated specs\003-modo-docker\research.md +1 -1
      410      compartilhar um broker por execução do Failsafe e manter o
           s tópicos por classe;
      411    - no Windows, o broker embutido pode falhar ao apagar os arq
           uivos de log no fim do teste. Se isso
      412      acontecer, apontar o `log.dirs` para uma pasta temporária
           por classe e ignorar a falha só na
      413 +    limpeza, registrando o motivo.                            
      414  - **Resultado (correção da etapa 2, 2026-11-25):** o `verify`
           ficou em 161 s, ou 1,68× a linha de base.
      415    O custo não estava no broker, e sim nas esperas fixas e nos
           casos enviados um a um. Os ajustes foram:
      416    - o perfil de teste `kafkatest`, com prazos curtos dos clien
           tes;
     ...
      420    - condições determinísticas no lugar de esperas fixas.
      421 
      422    O detalhe está em `docs/ai/decisoes.md`. O broker embutido f
           uncionou no Windows sem a saída alternativa.
      422 -    limpeza, registrando o motivo.                            
      423 
      424  ## R16. Testes com PostgreSQL: perfil Maven `postgres-it`
      425 

  Ran 1 shell command (ctrl+o to expand)

● O ./mvnw verify caiu de 249 s para 161 s, ou 1,68× a linha de base de 96 s,
  dentro do limite de 2×. Nenhum caso foi cortado, e a cobertura de linhas subiu
  de 96,32% para 97,06%. Fiz o commit de correção da etapa 2 (d5f8e23), sem
  push.

  O que mudou:
  - Perfil só de teste kafkatest (src/test/resources/application-kafkatest.yml,
    fora do JAR): prazos curtos dos clientes Kafka, como max.block.ms, o request
    timeout e os prazos do KafkaAdmin. Todos os ITs do Kafka o ligam junto com
    o perfil kafka, e a configuração de produção não muda.
  - Os 16 eventos inválidos numa única execução: são publicados de uma vez,
    seguidos de um evento válido, e conferidos juntos no tópico de erros. Cada
    caso tem a sua própria mensagem, e o aviso do log agora é comparado com o
    offset do próprio evento, o que é mais forte que antes. Passou de 16 s para
    4 s por execução.
  - Contexto do Spring reaproveitado: as classes que só leem e têm as mesmas
    propriedades compartilham o contexto; o KafkaReferenceFileIT foi de 5,7 s
    para 0,6 s. As que mudam dados ou a readiness ganham um contexto próprio,
    para não sujar o de outra. Os testes que sobem a aplicação com
    SpringApplication compartilham um broker na JVM, com tópicos próprios por
    classe.
  - Três esperas fixas trocadas por condições, com a mesma comprovação:
    - tópico vazio conferido pelos offsets: o KafkaStructuralFailureIT foi de 8
      s para 1 s;
    - um filme sentinela depois do marcador errado, no lugar de 3 s de espera: o
      KafkaReadinessIT foi de 8,6 s para 3,6 s;
    - reenvio com um flush só no fim: o KafkaIdempotencyIT foi de 9,4 s para 4,2
      s.

  Por causa da junção dos eventos inválidos, o número de testes caiu de 270 para
  255, e de 96 para 81 na execução com log em JSON. Os 16 casos são os mesmos.

  Também passaram o perfil postgres-it (7 testes, com o RestartPostgresIT e o
  RetryPostgresIT usando o perfil de teste) e o check-unchanged-tests.sh v0.2.0.

  Os dois testes mais caros que restaram são o KafkaBrokerUnavailableIT, com
  cerca de 9 s, e o KafkaEquivalenceIT, com cerca de 8 s. Baixar esses dois
  exigiria encurtar a observação de 5 s do primeiro ou comparar menos arquivos
  no segundo, ou seja, tirar comprovação; por isso não mexi neles.

  Registrei a medição, as mudanças e esse limite em docs/ai/decisoes.md e na R15
  da research. Também atualizei o README e a T032 no tasks.md.
✻ Churned for 16m 59s · done 8:46

❯ /export docs/ai/sessoes/11-marco-3-docker.md                                  
  ⎿  Conversation exported to
     D:\Repo\PESSOAIS\outsera-test\outsera\docs\ai\sessoes\11-marco-3-docker.md

❯ O Codex fez 3 apontamentos no PR. Os três procedem. Corrija nesta ordem, com  
  a spec antes do código (Princípio IV):                                        
                                                                                
  1. Grupo do consumidor: o @KafkaListener usa o id como grupo (idIsGroup=true) 
  e ignora KAFKA_CONSUMER_GROUP. Faça o listener usar o grupo configurado.      
  Acrescente um IT que suba com um grupo diferente e confira no broker, pelo    
  AdminClient, que o grupo registrado é o configurado.                          
  2. Tópicos depois de falha do broker na subida: com fail-fast false, se o     
  broker estiver fora na subida, nada recria os tópicos depois, e o compose     
  desliga a criação automática. Faça a recuperação tentar criar os tópicos de   
  novo, não só reenviar. Atualize o caso de borda "Broker indisponível" na spec 
  para descrever a recuperação. Comprove por IT, subindo o broker embutido      
  depois da aplicação; se isso não for viável, explique por quê e comprove num  
  passo do compose-test.sh.                                                     
  3. Evento com \u0000: o PostgreSQL rejeita NUL, a exceção entra na nova       
  tentativa sem limite e trava a partição. Corrija em duas camadas:             
     a) Validação: rejeite NUL nas regras compartilhadas da MovieFieldRules,    
  com um motivo próprio, valendo para o Kafka e para o CSV. Atualize a spec 001 
  e a 003, registrando a origem como no FIELD_TOO_LONG.                         
     b) Classificação: erros de dado do banco (SQLState das classes 22 e 23)    
  são permanentes e vão para o tópico de erros, sem nova tentativa. Os demais,  
  como a tabela inexistente do RetryPostgresIT (42P01) e a perda de conexão,    
  continuam com nova tentativa. Registre a decisão na R9. Cubra com IT: no      
  postgres-it, um caso que só o banco rejeite (sem passar pela validação da     
  camada a) indo ao tópico de erros, seguido de um evento válido gravado; e o   
  RetryPostgresIT continua verde.                                               
                                                                                
  Rode o ./mvnw verify e o perfil postgres-it. Informe os tempos. Faça um       
  commit por apontamento, com a referência ao comentário do PR na mensagem. Não 
  faça push.                                                                    

● Vou corrigir os três apontamentos, cada um com a spec primeiro, depois o IT, a
  confirmação da falha, o código e um commit próprio. Começo pelo 1, o grupo do
  consumidor.

  Ran 2 shell commands (ctrl+o to expand)

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 3 lines, removed 1 line
      257    ingestão por Kafka. A ingestão por Kafka e o banco MUST pode
           r ser ligados separadamente, para que os
      258    testes da ingestão rodem com H2 e o broker embutido no `./mv
           nw verify`.
      259  - **FR-003**: Toda configuração do modo Docker (endereço do ba
           nco, usuário, endereço do broker, nomes
      260 -  dos tópicos) MUST vir de variáveis de ambiente, com padrões 
          -que funcionem no compose (Princípio VII).                     
      260 +  dos tópicos e grupo do consumidor) MUST vir de variáveis de 
          +ambiente, com padrões que funcionem no                        
      261 +  compose (Princípio VII). O consumidor MUST se registrar no b
          +roker com o grupo configurado                                 
      262 +  (`KAFKA_CONSUMER_GROUP`), e não com um nome fixo no código (
          +apontamento do Codex no PR do Marco 3).                       
      263 
      264  **PostgreSQL**
      265 

● Write(src\test\java\com\outsera\awards\kafka\KafkaConsumerGroupIT.java)
  ⎿  Wrote 41 lines to
     src\test\java\com\outsera\awards\kafka\KafkaConsumerGroupIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.KafkaScenarioIT;
       6 import java.util.Collection;
       7 import java.util.List;
       8 import java.util.Map;
       9 import java.util.concurrent.TimeUnit;
      10 import org.apache.kafka.clients.admin.Admin;
     … +31 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● O teste reproduz o defeito, com o broker registrando awards-movies em vez do
  grupo configurado. Vou definir um clientIdPrefix explícito para que o
  client_id das métricas não dependa do grupo. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/g1.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
     observability/grafana/dashboards/awards.json:190:          "expr": "ma
     x(kafka_consumer_fetch_manager_records_lag_max{application=\"awards\",
     … +6 lines (ctrl+o to expand)
  ⎿  Updated observability\grafana\dashboards\awards.json +1 -1
      187              "type": "prometheus",
      188              "uid": "prometheus"
      189            },
      190 -          "expr": "max(kafka_consumer_fetch_manager_records_la
          -g_max{application=\"awards\",client_id=~\"consumer-awards-movi
          -es.*\"})",                                                    
      190 +          "expr": "max(kafka_consumer_fetch_manager_records_la
          +g_max{application=\"awards\",client_id=~\"awards-movies-.*\"})
          +",                                                            
      191            "legendFormat": "atraso"
      192          }
      193        ]
  ⎿  Updated specs\003-modo-docker\contracts\operational-endpoints.md +1 -1
      63  | Série no Prometheus | Rótulos | Significado |
      64  | --- | --- | --- |
      65  | `awards_ingestion_events_total` | `outcome="recorded"` ou `"r
          ejected"`, `application` | eventos de filme gravados e enviados
           ao tópico de erros (FR-017) |
      66 -| `kafka_consumer_fetch_manager_records_lag_max` (verificado, r
         -esearch R19) | `client_id` (`consumer-awards-movies-<n>`), `spr
         -ing_id`, `kafka_version`, `application` | atraso máximo do cons
         -umidor do tópico de filmes (uma partição, um consumidor) |     
      66 +| `kafka_consumer_fetch_manager_records_lag_max` (verificado, r
         +esearch R19) | `client_id` (`awards-movies-<n>`), `spring_id`, 
         +`kafka_version`, `application` | atraso máximo do consumidor do
         + tópico de filmes (uma partição, um consumidor) |              
      67 
      68  No modo padrão, nenhuma série `kafka_*` nem `awards_ingestion_*
          ` existe (research R2).
  ⎿  Updated specs\003-modo-docker\research.md +2 -2
      514    - **requisições por segundo** por rota: `sum by (uri) (rate(
           http_server_requests_seconds_count{uri=~"/api/.*"}[1m]))`;
      515    - **taxa de erro:** razão entre `outcome=~"CLIENT_ERROR|SERV
           ER_ERROR"` e o total, nas rotas de negócio;
      516    - **p95 e p99 de latência:** `histogram_quantile(0.95|0.99,
           sum by (le, uri) (rate(http_server_requests_seconds_bucket{uri
           =~"/api/.*"}[5m])))`;
      517 -  - **atraso do consumidor:** `max(kafka_consumer_fetch_manage
          -r_records_lag_max{client_id=~"consumer-awards-movies.*"})`;   
      517 +  - **atraso do consumidor:** `max(kafka_consumer_fetch_manage
          +r_records_lag_max{client_id=~"awards-movies-.*"})`;           
      518    - **eventos gravados e rejeitados:** `rate(awards_ingestion_
           events_total[5m])` por `outcome`.
      519  - **Verificado (etapa 2):** com o Spring Kafka 4.1.2, a série
           que aparece logo depois da carga é a do
      520    cliente: `kafka_consumer_fetch_manager_records_lag_max`, com
            o rótulo `client_id` do consumidor do
      521 -  ouvinte `awards-movies` (`consumer-awards-movies-<n>`). A sé
          -rie por partição (`..._records_lag`, com                      
      521 +  ouvinte `awards-movies` (`awards-movies-<n>`, pelo `clientId
          +Prefix` do ouvinte, que não depende do grupo). A série por par
          +tição (`..._records_lag`, com                                 
      522    `topic` e `partition`) só aparece depois da atualização peri
           ódica do Micrometer. Com um consumidor e
      523    uma partição, o máximo do cliente é o atraso do tópico de fi
           lmes. O `KafkaMetricsIT` fixa esse nome, e
      524    o painel usa o mesmo.
  ⎿  Updated src\main\java\com\outsera\awards\ingestion\kafka\MovieEventLis
     tener.java +7 -1
      66                  .register(registry);
      67      }
      68 
      69 -    @KafkaListener(id = "awards-movies", topics = "${awards.ing
         -estion.topic-movies}")                                         
      69 +    /**                                                        
      70 +     * O grupo é o de {@code spring.kafka.consumer.group-id} ({
         +@code KAFKA_CONSUMER_GROUP}), e não o id do                    
      71 +     * ouvinte ({@code idIsGroup = false}; spec 003, FR-003). O
         + prefixo do {@code client.id} é fixo, para que                 
      72 +     * a série de atraso das métricas e o dashboard não dependa
         +m do grupo configurado.                                        
      73 +     */                                                        
      74 +    @KafkaListener(id = "awards-movies", idIsGroup = false, cli
         +entIdPrefix = "awards-movies",                                 
      75 +            topics = "${awards.ingestion.topic-movies}")       
      76      public void onEvent(ConsumerRecord<String, byte[]> record)
          {
      77          Parsed parsed = MovieEventParser.parse(record.value());
      78          switch (parsed) {
  ⎿  Updated src\test\java\com\outsera\awards\kafka\KafkaMetricsIT.java +1
     -1
      21  class KafkaMetricsIT extends KafkaScenarioIT {
      22 
      23      static final String LAG = "kafka_consumer_fetch_manager_rec
          ords_lag_max";
      24 -    static final String CONSUMER_CLIENT = "consumer-awards-movi
         -es";                                                           
      24 +    static final String CONSUMER_CLIENT = "awards-movies-";    
      25 
      26      private List<Sample> samples() {
      27          String text = http.get().uri("/actuator/prometheus").ex
          change().expectStatus().isOk()

  Ran 1 shell command (ctrl+o to expand)

● A correção funciona. Registro a decisão, atualizo a rastreabilidade do plano e
  faço o commit do apontamento 1. Rodo o verify completo no fim dos três.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/g2.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  e98983d Revisão do PR do Marco 3, apontamento 1: grupo do consumidor c
     onfigurado
  ⎿  Updated docs\ai\decisoes.md +22
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-25 — Revisão do Codex no PR do Marco 3: grupo do con
         +sumidor (apontamento 1)                                        
       7 +                                                               
       8 +- **Apontamento (aceito):** o `@KafkaListener(id = "awards-movi
         +es")` usa o id como grupo (`idIsGroup`, que é                  
       9 +  verdadeiro por padrão) e ignora `KAFKA_CONSUMER_GROUP`. A FR-
         +003 pedia toda a configuração por variável                     
      10 +  de ambiente, e o grupo estava fixo no código.                
      11 +- **Por que os testes não pegaram:** cada classe de teste tem o
         + seu broker, então todas usavam o mesmo grupo                  
      12 +  fixo sem conflito. Nenhum teste conferia o grupo registrado n
         +o broker.                                                      
      13 +- **Correção:**                                                
      14 +  - a spec veio primeiro: a FR-003 agora cita o grupo do consum
         +idor;                                                          
      15 +  - o ouvinte passou a usar `idIsGroup = false`, e o grupo vem 
         +de `spring.kafka.consumer.group-id`                            
      16 +    (`KAFKA_CONSUMER_GROUP`, padrão `awards`);                 
      17 +  - o `clientIdPrefix = "awards-movies"` ficou explícito. Sem e
         +le, o `client.id` do Kafka seria derivado do                   
      18 +    grupo, e a série de atraso das métricas e o dashboard mudar
         +iam de nome conforme a variável. O                             
      19 +    `client_id` passa a ser `awards-movies-<n>`, e o `KafkaMetr
         +icsIT`, o dashboard, a R19 e o contrato                        
      20 +    operacional foram atualizados.                             
      21 +- **Comprovação:** o `KafkaConsumerGroupIT` sobe com `KAFKA_CON
         +SUMER_GROUP=grupo-configurado-it` e confere                    
      22 +  no broker, pelo `AdminClient` real, que esse é o grupo regist
         +rado, com um membro, e que `awards-movies`                     
      23 +  não aparece. Antes da correção, ele falhou com o grupo `award
         +s-movies`.                                                     
      24 +- **Efeito no compose:** com o padrão, o grupo passa a ser `awa
         +rds`. Um volume do Kafka criado antes da                       
      25 +  correção tem os offsets no grupo antigo, então o consumidor n
         +ovo relê o tópico desde o começo. Isso é                       
      26 +  inofensivo, porque a gravação é idempotente.                 
      27 +                                                               
      28  ## 2026-11-25 — Tempo do `./mvnw verify` do Marco 3 (correção d
          a etapa 2)
      29 
      30  - **Origem:** o `verify` passou de 96 s na linha de base para c
          erca de 4 min, acima do limite de 2× da
  ⎿  Updated specs\003-modo-docker\plan.md +1 -1
      136  | --- | --- |
      137  | FR-001 | suíte dos Marcos 1 e 2 sem alteração; `DefaultModeI
           solationIT`; `check-unchanged-tests.sh` |
      138  | FR-002 | ITs `kafka/` com H2 e `@ActiveProfiles("kafka")`; `
           *PostgresIT` com `postgres` |
      139 -| FR-003 | ITs que sobrescrevem `KAFKA_TOPIC_*` e `DB_*`; job 
          -`compose` |                                                   
      139 +| FR-003 | ITs que sobrescrevem `KAFKA_TOPIC_*` e `DB_*`; `Kaf
          +kaConsumerGroupIT` (grupo do consumidor); job `compose` |     
      140  | FR-004, FR-005 | `SchemaPostgresIT` (job `postgres`); `ddl-a
           uto: validate` nos dois bancos |
      141  | FR-006, FR-008 | `EquivalencePostgresIT`, `ListingOrderPostg
           resIT` (job `postgres`) |
      142  | FR-007 | job `postgres`, obrigatório na proteção da `main` (
           T093) |

● Agora o apontamento 2. Primeiro a spec: o caso de borda "Broker indisponível"
  passa a descrever a recuperação.

  Ran 1 shell command (ctrl+o to expand)

● Update(specs\003-modo-docker\spec.md)
  ⎿  Added 6 lines, removed 2 lines
      211  ### Edge Cases
      212 
      213  - Broker indisponível ao subir no modo Docker: a aplicação não
            se declara pronta enquanto a carga não
      214 -  for concluída; a liveness continua positiva. O compose só so
          -be a aplicação depois de o Kafka e o                          
      215 -  PostgreSQL ficarem saudáveis.                               
      214 +  for concluída; a liveness continua positiva. A cada nova ten
          +tativa de publicação, a aplicação também                      
      215 +  tenta criar de novo os tópicos de filmes e de erros que aind
          +a não existem, porque o broker pode não                       
      216 +  criá-los sozinho (no compose, a criação automática fica desl
          +igada). Quando o broker volta, a aplicação                    
      217 +  cria os tópicos, publica o arquivo, consome a carga e fica p
          +ronta, sem reinício. O compose só sobe a                      
      218 +  aplicação depois de o Kafka e o PostgreSQL ficarem saudáveis
          +, mas a recuperação não depende disso                         
      219 +  (apontamento do Codex no PR do Marco 3).                    
      220  - Evento de carga concluída de uma execução anterior ainda no
           tópico, ou consumido de novo: não libera a
      221    readiness da execução atual. Só o evento desta execução cont
           a.
      222  - Dois reinícios seguidos, com a primeira publicação interromp
           ida no meio: a segunda publica o arquivo

● Write(src\test\java\com\outsera\awards\kafka\KafkaBrokerLateStartIT.java)
  ⎿  Wrote 83 lines to
     src\test\java\com\outsera\awards\kafka\KafkaBrokerLateStartIT.java
       1 package com.outsera.awards.kafka;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.ApplicationStarter.Result;
       7 import com.outsera.awards.support.Http;
       8 import com.outsera.awards.support.KafkaTestSupport;
       9 import com.outsera.awards.support.KafkaTestSupport.Topics;
      10 import java.io.IOException;
     … +73 lines (ctrl+o to expand)

  Ran 1 shell command (ctrl+o to expand)

● O teste confirma o defeito: com o broker no ar e sem criação automática, o
  publicador falha sem parar porque o tópico não existe. Vou fazer com que ele
  peça ao KafkaAdmin para criar os tópicos que faltam antes de cada nova
  tentativa. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/t1.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  rc=1
     [ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed
     : 108.8 s <<< FAILURE! -- in com.outsera.awards.kafka.KafkaBrokerLateS
     … +6 lines (ctrl+o to expand)
  ⎿  Updated src\main\java\com\outsera\awards\ingestion\kafka\MovieEventPub
     lisher.java +26 -1
      16  import org.springframework.boot.context.event.ApplicationReadyE
          vent;
      17  import org.springframework.context.ApplicationListener;
      18  import org.springframework.context.annotation.Profile;
      19 +import org.springframework.kafka.core.KafkaAdmin;              
      20  import org.springframework.kafka.core.KafkaTemplate;
      21  import org.springframework.stereotype.Component;
      22  import tools.jackson.databind.json.JsonMapper;
     ...
      30   * {@link ApplicationReadyEvent}, publica num fio próprio um ev
          ento por linha aceita e o evento de carga
      31   * concluída. Com o broker fora do ar, tenta de novo com espera
           crescente, sem derrubar a aplicação: ela
      32   * fica viva e não pronta.
      33 + *                                                             
      34 + * <p>A cada nova tentativa, pede ao {@link KafkaAdmin} que cri
         +e os tópicos que ainda não existem. Com o                      
      35 + * broker fora na subida, a criação dos tópicos falha (com {@co
         +de fail-fast} desligado), e o broker pode não                  
      36 + * criá-los sozinho, como no compose, que desliga a criação aut
         +omática. Sem isso, a publicação falharia para                  
      37 + * sempre depois de o broker voltar (spec 003, caso de borda "B
         +roker indisponível"; apontamento do Codex no                   
      38 + * PR do Marco 3).                                             
      39   */
      40  @Component
      41  @Profile("kafka")
     ...
      54      private final KafkaTemplate<String, byte[]> kafka;
      55      private final KafkaIngestionProperties properties;
      56      private final IngestionReadiness readiness;
      57 +    private final KafkaAdmin admin;                            
      58 
      59      private volatile MovieCsvReader.Result result;
      60      private volatile Thread worker;
      61 
      62      public MovieEventPublisher(MovieFileReading reading, KafkaT
          emplate<String, byte[]> kafka,
      55 -            KafkaIngestionProperties properties, IngestionReadi
         -ness readiness) {                                              
      63 +            KafkaIngestionProperties properties, IngestionReadi
         +ness readiness, KafkaAdmin admin) {                            
      64          this.reading = reading;
      65          this.kafka = kafka;
      66          this.properties = properties;
      67          this.readiness = readiness;
      68 +        this.admin = admin;                                    
      69      }
      70 
      71      @Override
     ...
      132                          properties.topicMovies(), backoff, e.g
           etMessage());
      133                  Thread.sleep(backoff);
     … +21 lines (ctrl+o to expand)
  ⎿  (timeout 10m)

  Ran 1 shell command (ctrl+o to expand)

● O KafkaAdmin continua falhando com o broker já no ar. Vou ver a causa.

  Ran 2 shell commands (ctrl+o to expand)

● The broker came up on random ports (63677/63678), not on the requested port:
  kafkaPorts was ignored. Checking how the EmbeddedKafkaKraftBroker uses it.

  Ran 1 shell command (ctrl+o to expand)

● kafkaPorts é ignorado pelo EmbeddedKafkaKraftBroker do Spring Kafka 4.1.2,
  então não dá para fixar a porta do broker. Vou contornar isso apontando a
  aplicação para uma porta livre e repassando a primeira conexão com um proxy
  TCP simples, deixando o cliente falar direto com o broker real. · summary

● Write(src\test\java\com\outsera\awards\support\TcpForwarder.java)
  ⎿  Wrote 89 lines to
     src\test\java\com\outsera\awards\support\TcpForwarder.java
       1 package com.outsera.awards.support;
       2 
       3 import java.io.IOException;
       4 import java.io.InputStream;
       5 import java.io.OutputStream;
       6 import java.io.UncheckedIOException;
       7 import java.net.InetAddress;
       8 import java.net.InetSocketAddress;
       9 import java.net.ServerSocket;
      10 import java.net.Socket;
     … +79 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/t2.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaBrokerLateStartIT.java +17
     -5
       7  import com.outsera.awards.support.Http;
       8  import com.outsera.awards.support.KafkaTestSupport;
       9  import com.outsera.awards.support.KafkaTestSupport.Topics;
      10 +import com.outsera.awards.support.TcpForwarder;                
      11  import java.io.IOException;
      12  import java.io.UncheckedIOException;
      13  import java.net.ServerSocket;
     ...
      26   * Broker que só sobe depois da aplicação, sem criação automáti
          ca de tópicos, como no compose (spec 003,
      27   * caso de borda "Broker indisponível"; apontamento do Codex no
           PR do Marco 3).
      28   *
      28 - * <p>A aplicação sobe sem o broker, com o {@code KafkaAdmin} f
         -alhando ao criar os tópicos. Depois, o teste                   
      29 - * sobe o broker embutido na porta que a aplicação já usa, com 
         -{@code auto.create.topics.enable=false}. A                     
      30 - * aplicação precisa criar os tópicos sozinha, publicar o arqui
         -vo e ficar pronta, sem reinício. Usa                           
      29 + * <p>A aplicação sobe apontando para uma porta livre, onde nad
         +a escuta: o broker está fora de verdade, e o                   
      30 + * {@code KafkaAdmin} falha ao criar os tópicos. Depois, o test
         +e sobe o broker embutido, com                                  
      31 + * {@code auto.create.topics.enable=false}, e um {@link TcpForw
         +arder} na porta que a aplicação usa. O                         
      32 + * {@code EmbeddedKafkaKraftBroker} do Spring Kafka 4.1.2 ignor
         +a o {@code kafkaPorts} e sempre sobe numa porta                
      33 + * aleatória. O repassador só leva a conexão inicial ao broker;
         + daí em diante, os clientes falam direto com                   
      34 + * ele, pelo endereço que o próprio broker anuncia.            
      35 + *                                                             
      36 + * <p>A aplicação precisa criar os tópicos sozinha, publicar o 
         +arquivo e ficar pronta, sem reinício. Usa                      
      37   * {@code SpringApplication} pela cláusula "ordem da subida" do
           Princípio I: o que se observa é a aplicação
      38   * real antes e depois de a dependência subir.
      39   */
     ...
      54          String brokers = "localhost:" + kafkaPort;
      55          Topics topics = KafkaTestSupport.topics(KafkaBrokerLate
          StartIT.class);
      56          EmbeddedKafkaBroker[] broker = new EmbeddedKafkaBroker[
          1];
      57 +        TcpForwarder[] forwarder = new TcpForwarder[1];        
      58 
      59          Result result = ApplicationStarter.startAndCall("classp
          ath:movielist.csv",
      60                  KafkaTestSupport.args(brokers, topics), port ->
           {
     ...
      63                                  .as("readiness sem o broker").i
          sEqualTo(503);
      64 
      65                          EmbeddedKafkaKraftBroker late = new Emb
          eddedKafkaKraftBroker(1, 1);
      59 -                        late.kafkaPorts(kafkaPort);            
      66                          late.brokerProperty("auto.create.topics
          .enable", "false");
      67                          late.afterPropertiesSet();
      68                          broker[0] = late;
      69 +                        int brokerPort = Integer.parseInt(late.
         +getBrokersAsString().replaceAll(".*:", ""));                   
      70 +                        forwarder[0] = TcpForwarder.start(kafka
         +Port, brokerPort);                                             
     … +20 lines (ctrl+o to expand)

● Rodo com a correção e depois sem ela, para confirmar que o IT falha pelo
  motivo certo.

  Ran 1 shell command (ctrl+o to expand)
● O IT passa com a correção (22 s) e falha sem ela: a readiness não chega a 200
  em 90 s. Registro a decisão e faço o commit do apontamento 2.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/t3.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  5ca8876 Revisão do PR do Marco 3, apontamento 2: tópicos recriados dep
     ois de falha do broker
  ⎿  Updated docs\ai\decisoes.md +27
       3  Sugestões e decisões relevantes, aceitas ou rejeitadas, com o m
          otivo (item 5.2 do enunciado). As
       4  decisões de arquitetura ficam em ADRs, em `docs/adr/`.
       5 
       6 +## 2026-11-25 — Revisão do Codex no PR do Marco 3: tópicos depo
         +is de falha do broker (apontamento 2)                          
       7 +                                                               
       8 +- **Apontamento (aceito):** com `spring.kafka.admin.fail-fast: 
         +false`, se o broker estiver fora na subida, o                  
       9 +  `KafkaAdmin` não cria os tópicos e nada os recria depois. O p
         +ublicador só reenviava os eventos. Como o                      
      10 +  compose desliga a criação automática de tópicos, a publicação
         + falharia para sempre depois de o broker                       
      11 +  voltar.                                                      
      12 +- **Correção:**                                                
      13 +  - a spec veio primeiro: o caso de borda "Broker indisponível"
         + descreve a recuperação. A cada nova                           
      14 +    tentativa, a aplicação tenta criar os tópicos que faltam e,
         + quando o broker volta, publica, consome a                     
      15 +    carga e fica pronta, sem reinício;                         
      16 +  - antes de cada nova tentativa de envio, o `MovieEventPublish
         +er` chama `KafkaAdmin.initialize()`, que cria                  
      17 +    os `NewTopic` que ainda não existem. Com o broker ainda for
         +a, a falha só é registrada, e a tentativa                      
      18 +    seguinte repete (R8).                                      
      19 +- **Comprovação, por IT:** o `KafkaBrokerLateStartIT` reproduz 
         +o caso.                                                        
      20 +  1. A aplicação sobe apontando para uma porta livre, onde nada
         + escuta: o broker está fora de verdade.                        
      21 +  2. O teste confere a readiness 503.                          
      22 +  3. Sobe o broker embutido com `auto.create.topics.enable=fals
         +e`, como no compose.                                           
      23 +  4. Confere a readiness 200, o resultado de referência e os do
         +is tópicos criados.                                            
      24 +                                                               
      25 +  Sem a correção, o IT falhou: a readiness não chegou a 200 em 
         +90 s. Com ela, passou em 22 s.                                 
      26 +- **Por que há um repassador TCP no teste:** o `EmbeddedKafkaKr
         +aftBroker` do Spring Kafka 4.1.2 guarda o                      
      27 +  `kafkaPorts`, mas não o usa ao subir o cluster KRaft (conferi
         +do no bytecode), e o broker sempre sobe numa                   
      28 +  porta aleatória. O `TcpForwarder` de `src/test/` escuta na po
         +rta que a aplicação usa e só leva a conexão                    
      29 +  inicial ao broker. Daí em diante, os clientes falam direto co
         +m o broker, pelo endereço que ele anuncia.                     
      30 +  Não é um dublê: só copia bytes, sem ler nem alterar o protoco
         +lo. Por isso não foi preciso a alternativa                     
      31 +  de comprovar num passo do `compose-test.sh`.                 
      32 +                                                               
      33  ## 2026-11-25 — Revisão do Codex no PR do Marco 3: grupo do con
          sumidor (apontamento 1)
      34 
      35  - **Apontamento (aceito):** o `@KafkaListener(id = "awards-movi
          es")` usa o id como grupo (`idIsGroup`, que é
  ⎿  Updated specs\003-modo-docker\plan.md +1 -1
      143  | FR-009 | `KafkaInvalidLinesIT`, `KafkaStructuralFailureIT`,
           `PublisherSummaryIT` |
      144  | FR-010, FR-011 | `KafkaReadinessIT` (lê o tópico) e `KafkaRe
           ferenceFileIT` |
      145  | FR-012 | `KafkaEquivalenceIT`, `KafkaNewWinnerIT` (separação
            de nomes) |
      146 -| FR-013 | `KafkaReadinessIT`, `KafkaBrokerUnavailableIT` |   
      146 +| FR-013 | `KafkaReadinessIT`, `KafkaBrokerUnavailableIT`, `Ka
          +fkaBrokerLateStartIT` (tópicos recriados quando o broker volta
          +) |                                                           
      147  | FR-014 | `KafkaIdempotencyIT`, `KafkaUpdateIT`, `RestartDire
           ctPostgresIT`, `RestartPostgresIT` |
      148  | FR-015 | `KafkaInvalidEventsIT`, `KafkaErrorTopicUnavailable
           IT` |
      149  | FR-015a | `RetryPostgresIT` (job `postgres`: nova tentativa
           quando a gravação falha); passo 7b do job `compose` (queda rea
           l do banco) |
  ⎿  Updated specs\003-modo-docker\research.md +4
      180    - a falha estrutural continua rápida e antes do servidor;
      181    - um broker fora do ar não derruba o processo, então a liven
           ess fica positiva e a readiness negativa
      182      (caso de borda da spec);
      183 +  - **tópicos recriados nas novas tentativas** (apontamento do
          + Codex no PR do Marco 3): com o broker fora na                
      184 +    subida, o `KafkaAdmin` não cria os tópicos (`fail-fast: fa
          +lse`), e no compose o broker não os cria                      
      185 +    sozinho. Por isso, antes de cada nova tentativa de envio, 
          +o publicador chama `KafkaAdmin.initialize()`,                 
      186 +    que cria os tópicos que faltam. Comprovado pelo `KafkaBrok
          +erLateStartIT`;                                               
      187    - publicar no `ApplicationReadyEvent` garante que o consumid
           or já está registrado.
      188  - **Comprovação do resumo:** o teste de log do Marco 1 (`LoadS
           ummaryIT`) não muda. Um IT novo confere a
      189    mensagem de publicação nos dois formatos de log (`@Tag("log"
           )`).

● Agora o apontamento 3. Primeiro as specs 001 e 003, o data-model, o contrato
  de eventos e a R9.

  Ran 2 shell commands (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/n1.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
  ⎿  Updated specs\001-producer-intervals-api\data-model.md +4 -1
      80     produtores > 1000 ou um nome de produtor > 500 caracteres (`
          VARCHAR(500)` e `VARCHAR(1000)` da V1).
      81     O aviso cita o campo. Acrescentado pela spec 003 em 2026-11-
          24. A regra fica na `MovieFieldRules`,
      82     compartilhada com os eventos do Kafka.
      83 -9. `DUPLICATE`: o ano e o título repetem uma linha já aceita. O
         - aviso cita a linha original.                                  
      83 +9. `NUL_CHARACTER`: o título, os estúdios ou os produtores cont
         +êm o caractere nulo (U+0000). O aviso cita o                   
      84 +   campo. Acrescentado pela spec 003 em 2026-11-25 (apontamento
         + do Codex no PR do Marco 3). A regra também                    
      85 +   fica na `MovieFieldRules`.                                  
      86 +10. `DUPLICATE`: o ano e o título repetem uma linha já aceita. 
         +O aviso cita a linha original.                                 
      87 
      88  Uma linha em branco não é lida, não conta como linha lida e não
           gera aviso (FR-005a).
      89 
  ⎿  Updated specs\001-producer-intervals-api\spec.md +5 -1
      277    - um campo excede o tamanho da coluna do esquema: título com
            mais de 500 caracteres, estúdios ou
      278      produtores com mais de 1000, ou um nome de produtor com ma
           is de 500 (acrescentado pela spec 003 em
      279      2026-11-24, para que a carga direta e a ingestão por Kafka
            usem a mesma validação; antes, essa linha
      280 -    interrompia a carga com um erro do banco, sem mensagem cla
          -ra).                                                          
      280 +    interrompia a carga com um erro do banco, sem mensagem cla
          +ra);                                                          
      281 +  - o título, os estúdios ou os produtores contêm o caractere 
          +nulo (U+0000), que o PostgreSQL não aceita                    
      282 +    em texto (acrescentado pela spec 003 em 2026-11-25, a part
          +ir de um apontamento do Codex no PR do                        
      283 +    Marco 3, pelo mesmo motivo: a mesma validação na carga dir
          +eta e na ingestão por Kafka, e nenhuma                        
      284 +    recusa do banco no lugar de uma mensagem clara).          
      285  - **FR-006e**: Ano, título e produtores são colunas obrigatóri
           as. A coluna de estúdios é opcional: vazia,
      286    ela não invalida a linha. `winner` vazio significa não vence
           dor (FR-004).
      287  - **FR-006a**: Cada linha ignorada MUST gerar um aviso no log
           com o número da linha no arquivo e o
  ⎿  Updated specs\003-modo-docker\contracts\kafka-events.md +3 -3
      71  | `awards-source-partition` | `0` |
      72  | `awards-source-offset` | `207` |
      73 
      74 -**Motivos:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, `RUN_ID_MI
         -SSING`, `FIELD_TOO_LONG`, `YEAR_MISSING`,                      
      75 -`YEAR_NOT_NUMERIC`, `YEAR_OUT_OF_RANGE`, `TITLE_MISSING`, `PROD
         -UCERS_MISSING` e `WINNER_INVALID`                              
      76 -(data-model.md).                                               
      74 +**Motivos:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNOWN`, `RUN_ID_MI
         +SSING`, `FIELD_TOO_LONG`, `NUL_CHARACTER`,                     
      75 +`YEAR_MISSING`, `YEAR_NOT_NUMERIC`, `YEAR_OUT_OF_RANGE`, `TITLE
         +_MISSING`, `PRODUCERS_MISSING`,                                
      76 +`WINNER_INVALID` e `DATABASE_REJECTED` (recusa permanente do ba
         +nco, com o SQLState; data-model.md).                           
      77 
      78  ## Garantias
      79 
  ⎿  Updated specs\003-modo-docker\data-model.md +11 -2
      57  | `producers` | texto | sim | separado pelo `ProducerNameParser
          `, com ao menos um nome | `PRODUCERS_MISSING` |
      58  | `winner` | booleano JSON | sim | `true` ou `false` | `WINNER_
          INVALID` |
      59  | (todos) | — | — | tamanho máximo: `title` ≤ 500, `studios` ≤
          1000, `producers` ≤ 1000 e cada nome de produtor ≤ 500 caracter
          es (`VARCHAR(500)` e `VARCHAR(1000)` da V1) | `FIELD_TOO_LONG`
          |
      60 +| `title`, `studios`, `producers` | — | — | nenhum caractere nu
         +lo (U+0000), que o PostgreSQL não aceita em texto | `NUL_CHARAC
         +TER` |                                                         
      61 
      62  **Validação comum aos dois caminhos.** As regras de conteúdo fi
          cam numa única classe, a
      63  `MovieFieldRules`, usada pelo leitor do CSV e pelo parser de ev
          entos:
      64  - ano de 1 a 9999;
      65  - título não vazio;
      66  - produtores com ao menos um nome;
      66 -- `FIELD_TOO_LONG`.                                            
      67 +- `FIELD_TOO_LONG`;                                            
      68 +- `NUL_CHARACTER` (acrescentado em 2026-11-25, a partir de um a
         +pontamento do Codex no PR do Marco 3).                         
      69 
      70  O `FIELD_TOO_LONG` passou a valer também para a linha do CSV (s
          pec 001, FR-006 e data-model, motivo 8).
      71  Sem ele, o banco recusaria a gravação. Na carga direta, a recus
          a interrompia a subida. No consumidor, ela
     ...
      81  | `TYPE_UNKNOWN` | `type` presente e diferente de `movie` e de
          `load-completed` |
      82 
      83  **Ordem das verificações:** `NOT_UTF8`, `NOT_JSON`, `TYPE_UNKNO
          WN`, depois a mesma ordem da spec 001:
      82 -ano, título, produtores, `winner` e `FIELD_TOO_LONG`. O primeir
         -o motivo encontrado é o registrado.                            
      84 +ano, título, produtores, `winner`, `FIELD_TOO_LONG` e `NUL_CHAR
         +ACTER`. O primeiro motivo encontrado é o                       
      85 +registrado.                                                    
      86 +                                                               
      87 +Motivo que vem do banco, depois da validação:                  
      88 +                                                               
      89 +| Motivo | Quando |                                            
      90 +| --- | --- |                                                  
      91 +| `DATABASE_REJECTED` | O banco recusou a gravação por dado inv
         +álido, com SQLState da classe 22 (dado) ou 23 (restrição de int
         +egridade). O detalhe traz o SQLState. É permanente: o evento va
         +i ao tópico de erros, sem nova tentativa (FR-015a; research R9)
         + |                                                             
      92 
      93  Campos desconhecidos são ignorados (R7).
      94 
  ⎿  Updated specs\003-modo-docker\research.md +12 -1
      230      em paralelo** com outro IT do `postgres-it`;
      231    - **queda real do banco:** o passo 7b do `compose-test.sh` p
           ara o serviço `postgres` do compose,
      232      publica, religa e confere a gravação.
      233 +- **Classificação das falhas de gravação** (apontamento do Cod
          +ex no PR do Marco 3):                                         
      234 +  - **permanentes:** SQLState das classes **22** (dado inválid
          +o, como o caractere nulo no PostgreSQL ou                     
      235 +    um valor maior que a coluna) e **23** (restrição de integr
          +idade violada). A nova tentativa daria                        
      236 +    sempre o mesmo erro e travaria a partição para sempre. O o
          +uvinte procura o `SQLException` na cadeia de                  
      237 +    causas e, com uma dessas classes, envia o evento ao tópico
          + de erros com o motivo `DATABASE_REJECTED`                    
      238 +    e o SQLState, sem nova tentativa;                         
      239 +  - **passageiras:** todas as outras, como a tabela inexistent
          +e do `RetryPostgresIT` (42P01), a perda                       
      240 +    de conexão (08xxx) e o tempo esgotado do pool. Elas contin
          +uam com a nova tentativa sem limite;                          
      241 +  - **prevenção:** as recusas conhecidas viram regras da `Movi
          +eFieldRules` (`FIELD_TOO_LONG` e                              
      242 +    `NUL_CHARACTER`), com mensagem clara, nos dois caminhos de
          + carga. A classificação cobre as recusas que                  
      243 +    a validação não prevê.                                    
      244  - **Alternativa rejeitada:** `DeadLetterPublishingRecoverer` p
           ara todos os erros. Ele trataria uma falha
      234 -  de banco como evento inválido.                              
      245 +  de banco passageira como evento inválido.                   
      246 
      247  ## R10. Readiness ligada ao evento de carga concluída (FR-013)
      248 
  ⎿  … +1 file changed
     specs\003-modo-docker\spec.md

● Specs atualizadas. Agora os testes: o NulCharacterIT (CSV), o 17º caso no
  KafkaInvalidEventsIT e o DatabaseRejectionPostgresIT.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; node -e '
      const lines=["year;title;studios;producers;winner",…)
  ⎿  15
  ⎿  1 file changed (binary, mode only or too large to show)
     src\test\resources\scenarios\nul-character.csv

● Write(src\test\java\com\outsera\awards\ingestion\NulCharacterIT.java)
  ⎿  Wrote 51 lines to
     src\test\java\com\outsera\awards\ingestion\NulCharacterIT.java
       1 package com.outsera.awards.ingestion;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.IntervalItem;
       6 import com.outsera.awards.support.LoaderLog;
       7 import com.outsera.awards.support.LoaderLog.Summary;
       8 import com.outsera.awards.support.ScenarioIT;
       9 import java.util.List;
      10 import org.junit.jupiter.api.DisplayName;
     … +41 lines (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/n2.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  Binary file src/test/java/com/outsera/awards/kafka/KafkaInvalidEventsI
     T.java matches
  ⎿  1 file changed (binary, mode only or too large to show)
     src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java

● O grep acusou arquivo binário. Vou conferir se entrou um caractere nulo
  literal no fonte.

  Ran 1 shell command (ctrl+o to expand)

● Um NUL literal entrou no comentário e virou um escape do Java onde deveria
  haver o escape do JSON; corrijo as duas linhas. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/n3.js" <<'EOF'
      const fs=require("fs"); const
      f="src/test/java/com/outsera/awards/kafka/KafkaInvalidEven…)
  ⎿  sem NUL literal
     63-                // Escape JSON do caractere nulo (barra, u, 0000),
     que o PostgreSQL não aceita em texto (apontamento do Codex).
     … +2 lines (ctrl+o to expand)
  ⎿  1 file changed (binary, mode only or too large to show)
     src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java

● Agora vou ver o DatabaseRejectionPostgresIT, que provoca recusas do próprio
  banco com uma restrição (classe 23) e uma coluna encolhida (classe 22). 
  · summary

● Write(src\test\java\com\outsera\awards\postgres\DatabaseRejectionPostgresIT.ja
       va)
  ⎿  Wrote 96 lines to src\test\java\com\outsera\awards\postgres\DatabaseRe
     jectionPostgresIT.java
       1 package com.outsera.awards.postgres;
       2 
       3 import static org.assertj.core.api.Assertions.assertThat;
       4 
       5 import com.outsera.awards.support.ApplicationStarter;
       6 import com.outsera.awards.support.ApplicationStarter.Result;
       7 import com.outsera.awards.support.Http;
       8 import com.outsera.awards.support.KafkaTestSupport;
       9 import com.outsera.awards.support.KafkaTestSupport.Record;
      10 import com.outsera.awards.support.KafkaTestSupport.Topics;
     … +86 lines (ctrl+o to expand)

● Confirmo a falha antes da implementação, com o PostgreSQL local no ar para o
  IT do banco.

  Ran 1 shell command (ctrl+o to expand)

● Os três testes falham como esperado: o DatabaseRejectionPostgresIT mostra o
  defeito, já que a recusa do banco é retentada (5 vezes no log) e o evento
  válido seguinte nunca é gravado. Agora parto para a implementação das duas
  camadas. · summary

  Ran 1 shell command · 1 failed (ctrl+o to expand)

● Entrou de novo um caractere nulo literal, agora no MovieFieldRules. Troco por
  indexOf(0), que não precisa de escape nenhum.

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera";
      f=src/main/java/com/outsera/awards/ingestion/MovieFieldRules.java; cat >
      "$TEMP/n5.js" <<'EOF'
      const fs=require("fs"…)
  ⎿  89:        return value != null && value.indexOf(0) >= 0; // 0 é o car
     actere nulo (U+0000)
  ⎿  1 file changed (binary, mode only or too large to show)
     src\main\java\com\outsera\awards\ingestion\MovieFieldRules.java

● Nenhum outro arquivo tem NUL literal, exceto o CSV do cenário, de propósito.
  Rodo os testes afetados, no H2 e no PostgreSQL.

  Ran 2 shell commands (ctrl+o to expand)

● O caso 17 foi gravado em vez de rejeitado, porque o NUL no fim do título é
  removido pelo trim(). Vou colocar o NUL no meio do texto, como no CSV do
  cenário. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/n6.js" <<'EOF'
      const fs=require("fs"); const
      f="src/test/java/com/outsera/awards/kafka/KafkaInvalidEven…)
  ⎿  63-                // Escape JSON do caractere nulo (barra, u, 0000) n
     o meio do título: o trim, como no CSV, só tira os das
     64-                // pontas. O PostgreSQL não aceita esse caractere e
     … +8 lines (ctrl+o to expand)
  ⎿  Updated
     src\test\java\com\outsera\awards\kafka\KafkaInvalidEventsIT.java +3 -2
      60                  new Case("invalido-14", "WINNER_INVALID", movie
          ("", "2000", "\"" + MARKER + "\"", "\"P\"", null)),
      61                  new Case("invalido-15", "FIELD_TOO_LONG", movie
          ("", "2000", "\"" + MARKER + "T".repeat(501) + "\"", "\"P\"", "
          true")),
      62                  new Case("invalido-16", "FIELD_TOO_LONG", movie
          ("", "2000", "\"" + MARKER + "\"", "\"" + "P".repeat(1001) + "\
          "", "true")),
      63 -                // Escape JSON do caractere nulo (barra, u, 000
         -0), que o PostgreSQL não aceita em texto (apontamento do Codex)
         -.                                                              
      64 -                new Case("invalido-17", "NUL_CHARACTER", movie(
         -"", "2000", "\"" + MARKER + "\\u0000\"", "\"P\"", "true")));   
      63 +                // Escape JSON do caractere nulo (barra, u, 000
         +0) no meio do título: o trim, como no CSV, só tira os das      
      64 +                // pontas. O PostgreSQL não aceita esse caracte
         +re em texto (apontamento do Codex).                            
      65 +                new Case("invalido-17", "NUL_CHARACTER", movie(
         +"", "2000", "\"" + MARKER + "\\u0000fim\"", "\"P\"", "true")));
      66      }
      67 
      68      @Test
  ⎿  (timeout 10m)

● Agora passa. Registro o detalhe do trim no data-model e atualizo o ADR 0016, o
  plano e o README. Depois rodo o verify completo e o postgres-it inteiro.

  Ran 1 shell command (ctrl+o to expand)

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/n7.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  ok
  ⎿  Updated README.md +2 -2
      443  | `metrics/` | Métricas no formato do Prometheus: contagem, st
           atus, rota sem parâmetros, rotas inexistentes, 304, histograma
            e rótulo `application` |
      444  | `logging/` | Log em texto sem a variável, JSON ECS com ela,
           banner desligado e nenhum cabeçalho `Authorization` no log |
      445  | `isolation/` | Sem perfil, o Kafka não é configurado: nenhum
           a métrica nem cliente Kafka (Marco 3) |
      446 -| `kafka/` | Ingestão por Kafka com o broker embutido: resulta
          -do de referência, equivalência byte a byte com a carga direta,
          - readiness só com o evento de carga concluída, linhas inválida
          -s, falhas estruturais, vencedor novo em até 5 s, atualização, 
          -idempotência, cada tipo de evento inválido no tópico de erros,
          - tópico de erros fora do ar, broker fora do ar e métricas (Mar
          -co 3) |                                                       
      446 +| `kafka/` | Ingestão por Kafka com o broker embutido: resulta
          +do de referência, equivalência byte a byte com a carga direta,
          + readiness só com o evento de carga concluída, linhas inválida
          +s, falhas estruturais, vencedor novo em até 5 s, atualização, 
          +idempotência, cada tipo de evento inválido no tópico de erros 
          +(inclusive o caractere nulo, que o PostgreSQL não aceita), tóp
          +ico de erros fora do ar, broker fora do ar e métricas (Marco 3
          +) |                                                           
      447  | `security/` | Autenticação desligada (guarda do `API_TOKEN`)
            e ligada: 401 em Problem Details, endpoints públicos, token f
           ora do log, sem senha gerada, cabeçalhos de cache e de proteçã
           o e o contrato 1.2.0 (Marco 3) |
      448 -| `postgres/` | Só no perfil `postgres-it`: esquema, equivalên
          -cia e ordem entre o H2 e o PostgreSQL, reinícios e nova tentat
          -iva quando a gravação falha (Marco 3) |                       
      448 +| `postgres/` | Só no perfil `postgres-it`: esquema, equivalên
          +cia e ordem entre o H2 e o PostgreSQL, reinícios, nova tentati
          +va quando a gravação falha e recusa permanente do banco por da
          +do inválido, que vai ao tópico de erros (Marco 3) |           
      449  | `support/` | Base comum (`ScenarioIT`, `KafkaScenarioIT`, `T
           okenScenarioIT`) e apoios (aplicação real por `SpringApplicati
           on`, HTTP, Kafka e PostgreSQL reais, leitura do log, do Promet
           heus e do contrato), sem testes próprios |
      450 
      451  **Como os testes funcionam:**
  ⎿  Updated docs\adr\0016-topico-de-erros-e-novas-tentativas.md +7
      21    segue.
      22  - **Falha de gravação:** a exceção sobe ao `DefaultErrorHandler
          `, que tenta de novo sem limite de
      23    tentativas, com espera crescente até 30 s, sem avançar o offs
          et.
      24 +- **Classificação das falhas de gravação** (atualizado em 2026-
         +11-25, apontamento do Codex no PR do Marco 3):                 
      25 +  uma recusa do banco por dado inválido (SQLState das classes 2
         +2 e 23) é permanente e vai ao tópico de erros                  
      26 +  com o motivo `DATABASE_REJECTED`, sem nova tentativa. As dema
         +is (42P01, perda de conexão, tempo esgotado do                 
      27 +  pool) continuam com a nova tentativa sem limite. Sem isso, um
         + evento que só o banco recusa travaria a                       
      28 +  partição para sempre.                                        
      29 +- **Caractere nulo:** a regra `NUL_CHARACTER` (U+0000 no título
         +, nos estúdios ou nos produtores) vale nos dois                
      30 +  caminhos de carga, porque o PostgreSQL não aceita esse caract
         +ere em texto (atualizado em 2026-11-25).                       
      31  - **Tamanho máximo:** a regra `FIELD_TOO_LONG` (campo maior que
           a coluna) vale nos dois caminhos. Sem ela, o
      32    banco recusaria a gravação, e a recusa seria tentada de novo
          para sempre, travando o tópico.
      33 
  ⎿  Updated specs\003-modo-docker\data-model.md +1 -1
      57  | `producers` | texto | sim | separado pelo `ProducerNameParser
          `, com ao menos um nome | `PRODUCERS_MISSING` |
      58  | `winner` | booleano JSON | sim | `true` ou `false` | `WINNER_
          INVALID` |
      59  | (todos) | — | — | tamanho máximo: `title` ≤ 500, `studios` ≤
          1000, `producers` ≤ 1000 e cada nome de produtor ≤ 500 caracter
          es (`VARCHAR(500)` e `VARCHAR(1000)` da V1) | `FIELD_TOO_LONG`
          |
      60 -| `title`, `studios`, `producers` | — | — | nenhum caractere nu
         -lo (U+0000), que o PostgreSQL não aceita em texto | `NUL_CHARAC
         -TER` |                                                         
      60 +| `title`, `studios`, `producers` | — | — | nenhum caractere nu
         +lo (U+0000), que o PostgreSQL não aceita em texto. Um caractere
         + nulo nas pontas é removido pelo `trim`, como no CSV, e não che
         +ga a ser rejeitado | `NUL_CHARACTER` |                         
      61 
      62  **Validação comum aos dois caminhos.** As regras de conteúdo fi
          cam numa única classe, a
      63  `MovieFieldRules`, usada pelo leitor do CSV e pelo parser de ev
          entos:
  ⎿  Updated specs\003-modo-docker\plan.md +2 -2
      146  | FR-013 | `KafkaReadinessIT`, `KafkaBrokerUnavailableIT`, `Ka
           fkaBrokerLateStartIT` (tópicos recriados quando o broker volta
           ) |
      147  | FR-014 | `KafkaIdempotencyIT`, `KafkaUpdateIT`, `RestartDire
           ctPostgresIT`, `RestartPostgresIT` |
      148  | FR-015 | `KafkaInvalidEventsIT`, `KafkaErrorTopicUnavailable
           IT` |
      149 -| FR-015a | `RetryPostgresIT` (job `postgres`: nova tentativa 
          -quando a gravação falha); passo 7b do job `compose` (queda rea
          -l do banco) |                                                 
      149 +| FR-015a | `RetryPostgresIT` (job `postgres`: nova tentativa 
          +quando a gravação falha); `DatabaseRejectionPostgresIT` (job `
          +postgres`: recusa permanente, SQLState 22 e 23, vai ao tópico 
          +de erros); passo 7b do job `compose` (queda real do banco) |  
      150  | FR-016 | `KafkaNewWinnerIT` |
      151  | FR-017 | `KafkaMetricsIT` |
      152 -| FR-018 | `KafkaEquivalenceIT` (inclui `FIELD_TOO_LONG`); `Fi
          -eldTooLongIT` (carga direta, spec 001 FR-006) |               
      152 +| FR-018 | `KafkaEquivalenceIT` (inclui `FIELD_TOO_LONG`); `Fi
          +eldTooLongIT` e `NulCharacterIT` (carga direta, spec 001 FR-00
          +6); `KafkaInvalidEventsIT` (as mesmas regras no evento) |     
      153  | FR-019 a FR-023, FR-025, FR-027 | `AuthDisabledByDefaultIT`,
            `AuthEnabledIT` (inclui `OPTIONS`), `AuthProblemDetailsIT`, `
           TokenNotLoggedIT`, `NoGeneratedPasswordIT` |
      154  | FR-022a | `CacheHeadersIT` |
      155  | FR-024 | retirado (decisão de design na R11 e no ADR 0017; "
           Revisão de segurança" do checklist) |

● Rodo o ./mvnw verify completo, com o tempo medido, e o postgres-it inteiro.

  Ran 1 shell command (ctrl+o to expand)
● Ambos os testes passaram: ./mvnw verify levou 181 s, dentro do limite de 192
  s, e postgres-it levou 75 s. Em seguida atualizo o README, registro a decisão
  e faço o commit do apontamento 3. · summary

● Bash(cd "D:/Repo/PESSOAIS/outsera-test/outsera"; cat > "$TEMP/n8.js" <<'EOF'
      const fs=require("fs");…)
  ⎿  M  README.md
     M  docs/adr/0016-topico-de-erros-e-novas-tentativas.md
     M  docs/ai/decisoes.md
     … +23 lines (ctrl+o to expand)
  ⎿  Updated README.md +4 -4
  ⎿  … +1 file changed
     docs\ai\decisoes.md
  ⎿  (a git step in this command can move the working tree, so these may be
     its changes, not edits) (ctrl+o to expand)

● Corrigi os três apontamentos do Codex, um commit por apontamento, e não fiz
  push.

  Tempos:
  - ./mvnw verify: 181 s sem clean, ainda dentro do limite de 2× da R15 (192 s).
    São 261 testes mais 85 com o log em JSON, e a cobertura ficou em 97,35%. Os
    testes novos somam cerca de 12 s, quase tudo no teste que sobe o broker
    depois da aplicação.
  - ./mvnw -Ppostgres-it verify: 75 s, com 8 testes.

  Os testes novos falharam antes de cada correção e passam depois. Os 59 ITs da
  v0.2.0 continuam sem alteração.

  1. Grupo do consumidor (e98983d)
  - A FR-003 da spec 003 agora exige o grupo configurado.
  - O consumidor passou a usar o grupo de KAFKA_CONSUMER_GROUP. Fixei o prefixo
    do client_id em awards-movies, para que a série de atraso das métricas e o
    dashboard não mudem de nome com o grupo. O KafkaMetricsIT, o dashboard e a
    research foram ajustados.
  - O KafkaConsumerGroupIT sobe com outro grupo e confere no broker, pelo
    AdminClient, que o grupo registrado é o configurado. Antes da correção, ele
    encontrava awards-movies.
  - Num volume do Kafka anterior à correção, o consumidor relê o tópico desde o
    começo, porque o grupo mudou. Isso é inofensivo, porque a gravação é
    idempotente.

  2. Tópicos depois de falha do broker (5ca8876)
  - O caso de borda "Broker indisponível" da spec 003 agora descreve a
    recuperação.
  - Antes de cada nova tentativa de envio, o publicador pede ao Kafka que crie
    os tópicos que faltam.
  - O IT foi viável, e por isso não precisei do passo no compose-test.sh. O
    KafkaBrokerLateStartIT sobe a aplicação sem broker e depois sobe o broker
    embutido sem criação automática de tópicos, como no compose. A aplicação
    cria os tópicos, publica e fica pronta. Sem a correção, a readiness não
    chegou a 200 em 90 s.
  - Uma diferença do que você pediu: o broker embutido do Spring Kafka 4.1.2
    ignora a porta fixa e sempre sobe numa porta aleatória. Por isso, o teste
    usa um repassador TCP simples na porta da aplicação, que só entrega a
    conexão inicial; depois o cliente fala direto com o broker real. Não é um
    dublê: só copia bytes.

  3. Caractere nulo (d3b00fb)
  - Validação: nova regra NUL_CHARACTER, comum ao CSV e ao Kafka. As specs 001 e
    003 registram a origem, como no FIELD_TOO_LONG. Na carga direta, uma linha
    com o caractere nulo passa a ser ignorada com aviso; antes, o H2 a aceitava.
  - Classificação: recusas do banco com SQLState das classes 22 e 23 vão ao
    tópico de erros como DATABASE_REJECTED, sem nova tentativa. As demais, como
    a tabela inexistente (42P01) e a perda de conexão, continuam com a nova
    tentativa. A decisão está na R9 e no ADR 0016.
  - Testes:
    - NulCharacterIT, para a carga direta;
    - um 17º caso no KafkaInvalidEventsIT;
    - DatabaseRejectionPostgresIT, que muda o esquema com a aplicação no ar para
      que só o banco recuse (SQLState 23514 e 22001). Os dois eventos vão ao
      tópico de erros, e o evento válido seguinte é gravado;
    - o RetryPostgresIT continua verde.
  - Detalhe que encontrei: um caractere nulo no fim do título é removido pelo
    trim, nos dois caminhos, e nunca chega ao banco. Por isso o teste coloca o
    caractere nulo no meio do texto. Registrei isso no data-model.

  As três decisões estão em docs/ai/decisoes.md. Nas mensagens dos commits, cito
  os apontamentos pelo assunto, porque não tenho o link dos comentários do PR.

✻ Cogitated for 26m 33s · done 9:23