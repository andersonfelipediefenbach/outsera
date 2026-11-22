# Registro de decisões

Sugestões e decisões relevantes, aceitas ou rejeitadas, com o motivo (item 5.2 do enunciado). As
decisões de arquitetura ficam em ADRs, em `docs/adr/`.

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