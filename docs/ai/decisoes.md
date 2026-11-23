# Registro de decisões

Sugestões e decisões relevantes, aceitas ou rejeitadas, com o motivo (item 5.2 do enunciado). As
decisões de arquitetura ficam em ADRs, em `docs/adr/`.

## 2026-11-22 — Três ambiguidades apontadas pelo testador adversário

- **Origem:** rodada do Codex como testador adversário do Marco 1, com os casos em
  `docs/ai/adversario/`. O testador escreveu os CSVs a partir da spec, sem ler o código de produção.
  Os casos 12 e 13 foram entregues sem resultado esperado, porque a spec não determinava uma resposta
  única. O caso 09 usa anos (0, 2147483648, 4294967296) que a spec não proibia.
- **Decisões (responsável):**
  1. **`and` sem diferenciar maiúsculas (caso 12).** `AND`, `And` e `aNd` são separadores, como `and`.
     A regra vale só para o separador: os nomes continuam comparados exatamente como escritos (FR-008).
     Spec: FR-007.
  2. **Ordem Unicode no desempate por nome (caso 13).** Os nomes empatados são ordenados por
     `String.compareTo`, caractere a caractere, independente de locale. Spec: FR-015.
  3. **Ano válido de 1 a 9999 (caso 09).** Fora dessa faixa, a linha é rejeitada como inválida, com
     aviso no log. Spec: FR-006, FR-016b e o motivo novo `YEAR_OUT_OF_RANGE` do `data-model.md`.
- **Motivos:**
  - (1) a spec já não diferencia maiúsculas no `winner` nem no cabeçalho, e um `AND` no meio de nomes é separador para
    quem lê o arquivo. Tratar `Iris AND Nilo` como um único produtor inventaria um nome;
  - (2) a ordem por código de caractere é determinística, não depende da máquina do avaliador e é a
    ordem natural da linguagem. Uma colação linguística mudaria com o locale da JVM;
  - (3) o enunciado trata de anos de premiação. A faixa elimina o ano 0 e os valores que estouram
    `int`, sem exigir tipos maiores no banco e no contrato.
- **Alternativas rejeitadas:**
  - `and` só em minúsculas, como estava: o caso 12 mostrou que o resultado mudaria só pela caixa do
    separador;
  - colação com `java.text.Collator`: o resultado dependeria do locale;
  - anos sem limite, com `long` ou `BigInteger`: exigiria mudar o esquema e o tipo do contrato para um
    caso que não representa uma premiação real.
- **Efeito sobre os casos:** os resultados esperados dos casos 12 e 13 foram escritos pelo implementador
  a partir das decisões (1) e (2), e o README dos casos registra isso.
- **Caso 09 diverge do testador (responsável):** com a decisão (3), as linhas dos anos 0, 2147483647,
  2147483648 e 4294967296 são rejeitadas. Só sobram os anos 1 (`Orion`) e 2 (`Nova`), de produtores
  diferentes, então o esperado passa a ser `min` e `max` vazios. O `expected.json` original do testador
  fica intacto em `docs/ai/adversario/`, como registro da rodada. O esperado ajustado existe só na cópia
  de `src/test/resources/scenarios/adversario/`, que é a usada pelo `Case09ExtremeYearsIT`.
- **O que o caso 09 revelou:** o leitor aceitava o ano 0. O padrão `\d{1,9}` só verificava os dígitos,
  sem faixa. Com o arquivo do testador, a aplicação respondia com `Nova` de 0 a 2 em `min` e em `max`.
  Os anos de 10 dígitos já eram rejeitados, como "ano não numérico", uma mensagem enganosa. Agora todos
  saem com o motivo `YEAR_OUT_OF_RANGE`.
- **Impacto:** spec (sessão 2026-11-22, FR-006, FR-007, FR-015, FR-016b e casos de borda),
  `data-model.md`, research R3 e R4, matriz do `plan.md` e 13 ITs novos em
  `src/test/java/com/outsera/awards/adversario/`.

## 2026-11-22 — Decisões do Marco 1 que divergem do briefing (T082)

Registro consolidado no fechamento do marco. As decisões foram tomadas na clarificação da spec
(sessão de 2026-11-18) e no plano 001. Os detalhes ficam na spec e na research.

- **Linha inválida é ignorada com aviso, e a carga continua (FR-006).**
  - *Briefing:* "a carga inteira é rejeitada", marcado como decisão em aberto (`briefing.md` e
    `decisoes-planejamento.md`).
  - *Decisão (responsável, na clarificação):* só os erros estruturais (arquivo ausente, cabeçalho ou
    separador inválido) impedem a inicialização. Uma linha inválida é ignorada, com aviso no log (número
    da linha e motivo), e a carga termina com um resumo de lidas, aceitas e rejeitadas.
  - *Efeito:* uma linha ruim não derruba a aplicação nem esconde o resultado das linhas boas, e o aviso
    com o número da linha continua mostrando o problema.
  - *Alternativa rejeitada:* rejeitar a carga inteira, como no briefing.
- **Duplicata identificada por ano + título (FR-006d).**
  - *Briefing:* não tratava linhas repetidas.
  - *Decisão (responsável, na clarificação):* ano e título, sem os espaços em volta, identificam o
    filme. A primeira ocorrência prevalece, e as repetições são ignoradas com aviso.
  - *Efeito:* sem isso, uma linha vencedora repetida geraria um intervalo 0 falso para os seus produtores.
  - *Alternativa rejeitada:* contar cada linha como um filme.
- **Cabeçalho sem diferenciar maiúsculas, mas com ordem fixa (FR-003).**
  - *Briefing:* cabeçalho `year;title;studios;producers;winner`, sem dizer se a comparação é exata.
  - *Decisão (responsável, na clarificação):* `Year;Title;...` é aceito, e uma ordem diferente, uma
    coluna faltando ou uma a mais impedem a inicialização.
  - *Efeito:* a diferença de maiúsculas não muda o sentido do arquivo. Já uma ordem diferente indica um
    arquivo de outro formato, e aceitá-la exigiria mapear colunas por nome, sem pedido do enunciado.
  - *Alternativas rejeitadas:* comparação exata, que recusaria um arquivo inofensivo, e colunas em
    qualquer ordem.
- **Contrato em `/openapi.yaml`, e não em `/v3/api-docs` (research R8).**
  - *Briefing:* `GET /v3/api-docs` como contrato OpenAPI.
  - *Decisão (plano 001, research R8):* o contrato estático, escrito antes do código, é a única fonte
    e é publicado em `/openapi.yaml`. No bolt 6, isso levou a tirar o springdoc do projeto (entrada
    "Swagger UI pelo webjar, sem springdoc", abaixo).
  - *Motivo:* o briefing exige que o contrato seja a fonte do Swagger UI e da validação das respostas.
    Um contrato gerado pelo código seria um segundo contrato, que poderia divergir do desenhado.
  - *Alternativa rejeitada:* contrato gerado por anotações em `/v3/api-docs`.

## 2026-11-22 — Swagger UI pelo webjar, sem springdoc (bolt 6)

- **Origem:** implementador, na T076. Com `springdoc.api-docs.enabled=false`, o `/swagger-ui.html`
  respondia 404. Nas classes do springdoc 3.2.0, o `SwaggerConfig` (Swagger UI) tem
  `@ConditionalOnBean(SpringDocConfiguration.class)`, e a `SpringDocConfiguration` tem
  `@ConditionalOnProperty("springdoc.api-docs.enabled", matchIfMissing = true)`. Ou seja, nessa versão
  o Swagger UI do springdoc exige a geração automática do contrato ligada.
- **Decisão (responsável):** tirar o springdoc e servir o Swagger UI pelo webjar
  `org.webjars:swagger-ui` 5.34.0 (a mesma versão que o springdoc 3.2.0 traria), com o
  `webjars-locator-lite` gerenciado pelo Boot e uma página estática `static/swagger-ui.html` que aponta
  para `/openapi.yaml`.
- **Motivo:** mantém o contrato estático como fonte única (research R8). Não há contrato gerado pelo
  código, `/v3/api-docs` responde 404 e nada varre os controllers na inicialização.
- **Alternativas rejeitadas:**
  - springdoc com `api-docs` ligado: publicaria em `/v3/api-docs` um segundo contrato, gerado pelo
    código, que poderia divergir do estático;
  - springdoc com `api-docs` ligado e nenhum caminho gerado (`paths-to-exclude=/**`): `/v3/api-docs`
    continuaria publicado, vazio.
- **Fim de linha:** o `ApiDocsIT` compara `/openapi.yaml` byte a byte com o contrato de `specs/`. Nesta
  cópia de trabalho, o arquivo de `specs/` estava com CRLF, extraído antes do `.gitattributes`
  (`eol=lf`), e o teste falhava. O conteúdo versionado tem LF nos dois arquivos, então o arquivo foi
  extraído de novo. Outros 19 arquivos da cópia de trabalho estão na mesma situação e não foram
  tocados. Num clone novo, todos vêm com LF.
- **Impacto:** research R1 e R8, plano (stack e estrutura), T074 a T076 e `pom.xml`.

## 2026-11-22 — Detalhes da listagem de filmes (bolt 5)

- **Origem:** implementador, ao escrever os testes do bolt 5. A spec não dizia o que acontece com uma
  página além da última. Com `page=2147483647`, o deslocamento (`page` × `size`) passa de
  `Integer.MAX_VALUE`, o Spring Data recusa a consulta e a resposta seria 500.
- **Decisão (implementador, para revisão do responsável no PR):**
  - a spec ganhou um caso de borda antes do código (Princípio IV): uma página além da última responde
    200 com `content` vazio e os totais reais, e um `page` que não cabe em 32 bits é inválido (400);
  - quando o deslocamento passa do limite, o `MovieQueryService` consulta só a contagem;
  - todos os parâmetros inválidos da consulta aparecem em `invalidParams`, e não só o primeiro;
  - o `ResponseTimeIT` usa `@DirtiesContext`, para que a primeira chamada medida aconteça num contexto
    sem nenhuma requisição anterior, incluindo a inicialização preguiçosa do Spring MVC (SC-005).
- **Alternativas rejeitadas:**
  - recusar com 400 uma página além da última: o FR-021 aceita qualquer `page` maior ou igual a 0, e o
    US3-6 já responde 200 com lista vazia quando não há resultado;
  - limitar `page` a um máximo arbitrário: seria uma regra sem origem na spec.
- **T067:** os ITs de carga do bolt 2 (inclusive o `BomVariationsIT`, criado depois da T067) passaram a
  verificar os filmes por `GET /api/movies`, e o `JdbcTemplate` saiu da `ScenarioIT`. No
  `HarmlessVariationsIT`, a consulta à tabela `producer` não tem equivalente na listagem, que devolve os
  produtores como texto. O nome sem espaços em volta continua provado pelos intervalos
  (`Producer A` 1990→1991, classe `WithVariations`).
- **Impacto:** caso de borda novo na spec, research R7, T063 e T071.

## 2026-11-22 — Bolts 5 e 6 entregues juntos

- **Decisão (responsável):** os bolts 5 (listagem) e 6 (Swagger UI e fechamento do Marco 1) são
  entregues em um único branch, `bolt/5-6-listagem-swagger`, e em um único pull request, com um commit
  por etapa:
  1. fase 6 — listagem (T062 a T073);
  2. fase 7 — Swagger UI (T074 a T077);
  3. fase 8 — fechamento (T078 a T083).
- **Regras mantidas:** cada etapa começa pelos testes, que precisam falhar antes da implementação, e
  termina com `./mvnw verify` verde antes do commit. O PR, o merge e a tag continuam com o responsável.
- **Alternativa rejeitada:** um branch e um PR por bolt, como previa o mapeamento original do
  `tasks.md`.
- **Impacto:** mapeamento bolt → fase, checkpoints das fases 6 e 7, dependências e notas do
  `specs/001-producer-intervals-api/tasks.md`. Nenhuma mudança na spec nem no contrato.

## 2026-11-22 — Validador do contrato com a coordenada nova da Atlassian (T055)

- **Origem:** implementador, no bolt 4. Ao resolver
  `com.atlassian.oai:swagger-request-validator-core:3.0.0`, o Maven avisa que o artifact foi relocado
  pelo próprio projeto para `com.atlassian.oai:openapi-request-validator-core:3.0.0`.
- **Decisão (implementador, para revisão do responsável no PR):** declarar no `pom.xml` a coordenada
  nova, na mesma versão 3.0.0 da research R8. O código e os pacotes Java são os mesmos
  (`com.atlassian.oai.validator`), então o plano B (`networknt/json-schema-validator`) não foi
  necessário.
- **Exclusão:** o `jakarta.validation-api` que chega pelo `swagger-core` foi excluído, só nessa
  dependência de teste. Sem a exclusão, o Spring tentava montar um Bean Validation nos testes e
  registrava `Failed to set up a Bean Validation provider`, um comportamento que não existe em
  produção. Com a exclusão, o classpath de teste volta a ser igual ao de produção e os 78 testes
  passam.
- **Controle:** o `IntervalsContractIT` inclui um caso que adultera a resposta real com um campo a
  mais e exige que o validador a recuse (`additionalProperties: false`), para provar que a validação
  não é vazia.
- **T060:** nenhum ajuste foi necessário no `ProducerIntervalController` nem nos records. O controller
  mínimo do bolt 3 já cumpria o formato, e o T056 e o T057 passaram antes da implementação do bolt 4.
  Só o T058 falhou antes do T059, por falta do contrato em `static/`.

## 2026-11-22 — Anos do `disjoint-producers.csv` fora do `movielist.csv` (T041)

- **Origem:** conflito encontrado pelo implementador no bolt 3. A T041 pedia que o `Producer B`
  vencesse em 1960 e 2010, mas o SC-004a exige um arquivo sem nenhum ano em comum com o arquivo
  fornecido, que cobre 1980 a 2019.
- **Decisão (responsável):** seguir a spec, que tem prioridade sobre a task. O `Producer B` vence em
  1925 e 1975, e o intervalo continua 50. O texto da T041 foi corrigido.
- **Alternativa rejeitada:** manter 1960–2010. O cenário deixaria de cumprir o SC-004a.

## 2026-11-22 — Uma vitória a mais no `harmless-variations.csv` (T048)

- **Origem:** a T048 pede que as 4 linhas do `harmless-variations.csv` gerem ao menos um intervalo, e o
  arquivo do bolt 2 não tinha nenhum produtor com duas vitórias.
- **Decisão (implementador):** o filme de 1991 (`winner` igual a ` Yes `) passa a ser do
  `Producer A`, o mesmo do filme de 1990 (`YES`, com espaços em volta do nome). O intervalo
  `Producer A` 1990→1991 só aparece se o BOM, os espaços e as duas variações de `yes` forem tratados
  corretamente. Os testes do bolt 2 sobre esse arquivo continuam valendo sem alteração.

## 2026-11-22 — `ProducerNamesIT` absorvido pelo `ProducerSeparatorsIT` (T043)

- **Decisão (planejada no T022b):** os casos de separação de nomes passam a ser verificados por HTTP no
  `ProducerSeparatorsIT`, e o `ProducerNamesIT` e o `producer-names.csv`, que verificavam pelo banco,
  foram removidos (Princípio I).
- **Como os nomes aparecem nos intervalos:** cada forma está em duas vitórias de anos seguidos, e os
  nomes que se repetem entre formas estão em anos também seguidos. Todo intervalo correto vale 1, e
  `min` e `max` trazem a lista completa de nomes. Um nome separado errado vira um item a mais ou a
  menos, e dois nomes unidos por engano, ou um nome contado duas vezes no mesmo filme, geram um
  intervalo diferente de 1.

## 2026-11-22 — Separação de produtores em etapas, no lugar da regex (segunda revisão do Codex)

- **Origem:** segundo comentário do Codex, como revisor, no pull request do bolt 2, sobre a mesma regex
  do `ProducerNameParser`. A regex `\s*,\s*(?:and(?=\s|$)\s*)?|(?:^|\s+)and(?=\s|$)\s*`, adotada após a
  primeira revisão (entrada "`and` no fim do campo de produtores", de 2026-11-19), consome o espaço
  depois de um `and`, e o `and` seguinte deixa de ser reconhecido:
  - `Producer A and and Producer B` vira `Producer A` e `and Producer B`;
  - `and and` vira o produtor `and`, e a linha é aceita em vez de rejeitada.

  Os dois casos foram reproduzidos com testes antes da correção.
- **Decisão (responsável):** aceitar e, por ser o segundo defeito na mesma regex, trocá-la por uma
  separação em etapas:
  1. dividir o campo pelas vírgulas;
  2. em cada parte, a palavra `and` isolada (separada por espaços) é separador;
  3. o nome é o trecho original entre os separadores, então o texto interno é preservado;
  4. as partes vazias são descartadas.
- **Motivo:** cada etapa é simples de ler e de conferir caso a caso. Como o `and` é reconhecido pela
  palavra, e não pelos espaços em volta, os casos de início e fim do campo (primeira revisão) e de `and`
  repetido (segunda revisão) deixam de precisar de tratamento especial.
- **Alternativas rejeitadas:**
  - ajustar a regex de novo, por exemplo com lookbehind: seria a terceira correção na mesma expressão;
  - dividir por espaços e juntar as palavras com um espaço: normalizaria os espaços internos dos nomes,
    o que a FR-008 proíbe.
- **Verificação:**
  - o novo `ProducerNamesIT` (T022b) cobre todos os casos da research R4, mais `and and` (linha
    rejeitada, sem produtores), `Producer A and and Producer B` (2 produtores) e um nome com espaços
    internos duplos. Os casos antigos já passavam com a regex anterior e continuam passando;
  - a regex antiga e o parser novo foram comparados nas 206 linhas do `movielist.csv` do enunciado,
    sem nenhuma diferença.
- **Impacto:** FR-007 da spec (exemplos com `and` repetido), research R4 (decisão, histórico das duas
  revisões e casos), T030 (texto da separação), nova T022b e T043 (absorve o `producer-names.csv`, e o
  `ProducerNamesIT` sai nesse bolt). A verificação pelo banco é temporária (Princípio I), porque a
  listagem devolve os produtores como texto e não prova a separação.

## 2026-11-22 — BOM removido do `Reader`, antes do Commons CSV (revisão do Codex)

- **Origem:** comentário do Codex, como revisor, no pull request do bolt 2. O leitor removia o BOM só
  do primeiro campo, depois de o Commons CSV já ter analisado a linha. Isso é tarde demais em dois
  casos:
  - BOM seguido de cabeçalho entre aspas (`"year";...`): com o BOM na frente, o Commons CSV não
    reconhece a aspa, o campo vira `"year"` literal e a inicialização falha com `INVALID_HEADER`;
  - BOM seguido de linha em branco: a linha contém só o BOM, deixa de ser uma linha em branco e vira
    o cabeçalho, o que também dá `INVALID_HEADER`.

  Os dois cenários foram reproduzidos com testes antes da correção.
- **Decisão (responsável):** aceitar. O BOM é removido do `Reader` antes de ele ser entregue ao Commons
  CSV, lendo o primeiro caractere com `mark`/`reset`. O tratamento no primeiro campo foi retirado.
- **Motivo:** a FR-005a manda aceitar o BOM sem aviso, qualquer que seja o resto do arquivo. Tirar o BOM
  antes da análise faz o Commons CSV ver o arquivo exatamente como ele seria sem o BOM.
- **Alternativa rejeitada:** `BOMInputStream`, do Commons IO. O Commons IO só chega como dependência
  transitiva do Commons CSV, e usá-lo direto exigiria declará-lo no `pom.xml`: uma dependência nova só
  para um caractere.
- **Impacto:** research R3, tarefa T031 (a remoção do BOM sai da validação do cabeçalho e vai para a
  leitura) e nova tarefa T022a, com o `BomVariationsIT` e os cenários `bom-quoted-header.csv` e
  `bom-blank-line.csv`. A matriz do `plan.md` (FR-005a e FR-006c) foi atualizada.

## 2026-11-22 — Linha só com espaços e arquivo que não pode ser analisado (R3, T031)

- **Origem:** sonda do implementador com o Commons CSV 1.14.1, no início do bolt 2. O
  `ignoreEmptyLines` só pula linhas totalmente vazias. Uma linha só com espaços vira um registro de
  uma coluna vazia, que seria rejeitado por `COLUMN_COUNT`, contrariando a FR-005a ("linhas em branco
  em qualquer posição" são aceitas sem aviso). A spec também não dizia o que fazer com aspas sem
  fechamento ou com bytes fora de UTF-8.
- **Decisão (implementador, a confirmar pelo responsável no PR):**
  - um registro de uma coluna vazia é tratado como linha em branco;
  - aspas sem fechamento e bytes que não são UTF-8 válido dão erro estrutural `UNREADABLE`.
- **Motivo:** a linha só com espaços é uma linha em branco para quem edita o arquivo. No segundo caso,
  o leitor não consegue separar as linhas com segurança, e decodificar com substituição gravaria nomes
  corrompidos sem aviso.
- **Alternativa rejeitada:** rejeitar só a linha com aspas abertas. O Commons CSV lê até o fim do
  arquivo procurando o fechamento, então não há como saber onde a linha termina.
- **Impacto:** research R3 e tarefas T012, T013, T015 e T031, com dois cenários novos de falha de
  inicialização (`unterminated-quote.csv` e `invalid-encoding.csv`) e uma linha só com espaços no
  `harmless-variations.csv`.

## 2026-11-22 — Critério "sem `WARN`" restrito ao carregador do CSV (T014 e T015)

- **Origem:** observação do implementador ao fim do bolt 1. A saída do `./mvnw verify` já trazia
  `WARN` e `WARNING` que não vêm da aplicação: o aviso do Flyway sobre a versão do H2 (2.4.240, mais
  nova que a testada) e o aviso de agente dinâmico do Mockito. Com o critério "o log não contém
  `WARN`", o T014 e o T015 falhariam por causa de bibliotecas, e não do carregador.
- **Decisão (responsável):** o T014 e o T015 verificam apenas as linhas `WARN` emitidas pelo
  carregador do CSV (logger `MovieFileLoader`, pacote `com.outsera.awards.ingestion`), e não o log
  inteiro. O aviso do Flyway sobre a versão do H2 é mantido, sem supressão.
- **Motivo:** o que os testes precisam provar é que o carregador não rejeitou nem reclamou de
  nenhuma linha. Avisos de terceiros não dizem nada sobre isso e podem mudar a cada versão.
- **Alternativa rejeitada:** suprimir os avisos de terceiros na configuração de log. Esconderia
  informação útil e acoplaria o teste a uma lista de avisos externos.
- **Impacto:** texto do T014 e do T015 em `specs/001-producer-intervals-api/tasks.md`, com uma nota
  explicando o critério.

## 2026-11-22 — Mockito excluído do `spring-boot-starter-test`

- **Origem:** a mesma observação acima. O Mockito vem junto com o `spring-boot-starter-test` e emite
  um aviso de agente dinâmico ao se acoplar à JVM.
- **Decisão (responsável):** excluir `mockito-core` e `mockito-junit-jupiter` do
  `spring-boot-starter-test` no `pom.xml` (nova tarefa T011a, no início do bolt 2).
- **Motivo:** o Princípio I proíbe mocks, então o Mockito não tem uso no projeto. Tirá-lo do
  classpath transforma a regra em erro de compilação e elimina o aviso.
- **Alternativa rejeitada:** manter o Mockito e configurar o agente (`-javaagent`) no Failsafe para
  silenciar o aviso. Manteria disponível uma ferramenta que o projeto proíbe.
- **Impacto:** `pom.xml` e `tasks.md` (T011a). Nenhuma mudança na spec nem no contrato.

## 2026-11-19 — Emenda 1.1.0 da constituição: observabilidade a partir do Marco 2

- **Origem:** achado H1 do `/speckit-analyze` da spec 001. A constituição pedia logs em JSON e
  readiness, mas a spec do Marco 1 punha observabilidade fora do escopo.
- **Decisão (responsável):** emendar a constituição. Os logs estruturados em JSON e os probes de
  liveness e readiness passam a valer a partir do marco de observabilidade (Marco 2).
- **Motivo:**
  - o Marco 1 se limita ao que o enunciado avalia, e o enunciado não pede nenhum dos dois itens;
  - antecipá-los acrescentaria dependências e configuração ao modo padrão, que é o que o avaliador
    executa;
  - a garantia da readiness já existe no Marco 1, porque a carga termina antes de o servidor aceitar
    conexões (FR-001).
- **Alternativa rejeitada:** incluir logs em JSON e readiness no Marco 1.
- **Impacto:**
  - versão 1.0.0 → 1.1.0 (MINOR);
  - o `AGENTS.md` foi atualizado;
  - nenhuma mudança na spec 001;
  - no Marco 2, os testes que verificam mensagens de log precisam continuar passando com o formato
    JSON.

## 2026-11-19 — Emenda 1.2.0 da constituição: Princípio I reescrito

- **Origem:** achado C1′ do segundo `/speckit-analyze`. A exceção dos testes de carga tinha sido
  registrada na FR-027 da spec, mas a spec não pode abrir exceção a um princípio da constituição, que
  tem prioridade maior.
- **Decisão (responsável):** emendar a constituição, e não a spec. Novo texto do Princípio I:
  - todo teste é de integração, com `@SpringBootTest` e o contexto completo da aplicação, sem mocks;
  - o resultado é verificado preferencialmente por HTTP, e por log ou banco somente enquanto o
    comportamento não tem endpoint exposto;
  - os testes de falha de inicialização rodam a aplicação real com `SpringApplication`, sem mocks,
    e verificam a exceção e o log, porque um contexto que não sobe não pode ser verificado por
    `@SpringBootTest`.
- **Motivo:** permitir que cada comportamento seja testado no bolt em que nasce, mesmo antes de ter
  endpoint (como a carga do CSV), sem teste unitário e sem exceções espalhadas pelas specs.
- **Alternativas rejeitadas** para a falha de inicialização:
  - um `@SpringBootTest` externo que sobe a aplicação com falha por dentro: cumpre a letra do
    princípio, mas o contexto externo não teria função;
  - executar classes `@SpringBootTest` pelo `EngineTestKit`: acrescenta complexidade sem ganho.
- **Impacto:**
  - versão 1.1.0 → 1.2.0 (MINOR, compatível: todo teste que cumpria a versão anterior continua
    cumprindo);
  - a FR-027 deixou de ter exceções e só referencia o Princípio I;
  - o plano, as tarefas, a research e o `AGENTS.md` foram alinhados.

## 2026-11-19 — `and` no fim do campo de produtores (revisão do Codex)

- **Origem:** comentário do Codex, como revisor, no pull request do bolt 2. O leitor de CSV aplica
  `trim` no campo antes do parser de nomes (T031), então ` , and ` chega como `, and`. A regex da
  research R4, `\s*,\s*(?:and\s+)?|\s+and\s+`, exigia espaço depois do `and` e deixava `and` como
  nome de produtor. Com isso, a linha deixava de ser rejeitada e o caso `PRODUCERS_MISSING`
  (` , and `) do T017 falhava. O mesmo acontecia com `Producer A and`, que virava um único nome.
- **Decisão (responsável):** aceitar. O `and` passa a ser separador como palavra inteira, também no
  início e no fim do campo, com a regex `\s*,\s*(?:and(?=\s|$)\s*)?|(?:^|\s+)and(?=\s|$)\s*`. Nomes
  que contêm `and`, como `Anderson`, `Robbinsand` e `andrew`, continuam inteiros.
- **Motivo:** o parser não pode depender dos espaços em volta do campo, porque o `trim` do leitor os
  remove. A regex nova foi executada contra os casos antigos e os novos, que estão listados na R4.
- **Alternativa rejeitada:** tirar o `trim` do leitor de CSV. A regra continuaria frágil, e os outros
  campos (ano, título, `winner`) dependem do `trim`.
- **Impacto:** a R4, a T030 (regex), a T031 (nota sobre o `trim`) e as FR-007 e FR-009 da spec foram
  atualizadas. O T017 continua com o caso ` , and ` e passa a ser atendido. O T043 ganhou o caso
  `Producer A and`, que deve resultar em 1 produtor.
- **Briefing:** continua dizendo "`and` cercada de espaços" e não foi alterado. Ele é o registro do
  planejamento, e a fonte da verdade é a spec (FR-007).

## 2026-11-19 — Testes de carga antes dos endpoints (substituída pela emenda 1.2.0)

- **Origem:** achado C2 do `/speckit-analyze`. O Princípio I e a FR-027 falam em testes que "chamam
  os endpoints por HTTP", mas o bolt 2 (carga do CSV) ainda não tem endpoint de negócio.
- **Decisão (responsável):**
  - no bolt 2, os ITs de carga verificam o resultado pelo log e pelo banco, com a aplicação real no
    ar (`@SpringBootTest`);
  - nos bolts 3 e 5, os mesmos ITs ganham a verificação por HTTP;
  - os testes de falha de inicialização verificam a exceção e a mensagem, porque não chega a existir
    servidor.

  A exceção foi registrada primeiro na FR-027 da spec 001. Foi retirada de lá e passou para o
  Princípio I da constituição (emenda 1.2.0, acima).
- **Motivo:** manter os bolts pequenos e com os testes antes da implementação, sem criar testes
  unitários nem mocks.
- **Controle:** a tarefa T078 confere, no fechamento do marco, que todo IT de `ingestion/` (menos o
  `StartupFailureIT`) faz pelo menos uma chamada HTTP.