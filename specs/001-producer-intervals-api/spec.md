# Feature Specification: Marco 1 — Carga do CSV, intervalos de produtores e listagem de filmes

**Feature Branch**: `001-producer-intervals-api`

**Created**: 2026-11-18

**Status**: Draft

**Input**: User description: "Marco 1, conforme docs/ai/planejamento/briefing.md: carga do CSV em H2 na inicialização, com caminho configurável; cálculo dos intervalos entre vitórias consecutivas de cada produtor, com as regras de separação de nomes e as decisões sobre ambiguidades registradas no briefing; endpoint GET de intervalos com o formato exato do enunciado; listagem de filmes paginada com filtros por ano e vencedor; Swagger UI. O cálculo deve ser correto para qualquer CSV válido, já que a avaliação poderá usar outros conjuntos de dados. O CSV fornecido serve apenas como um dos cenários de aceitação, com resultado esperado de Joel Silver no mínimo (1990 a 1991) e Matthew Vaughn no máximo (2002 a 2015). Nenhum nome, ano ou valor pode ser fixado no código. Fora do escopo: Docker, Kafka, PostgreSQL, autenticação, pipeline e front-end."

## Clarifications

### Session 2026-11-18

- Q: O que fazer com uma linha inválida dentro de um CSV válido? → A: Erros estruturais (arquivo
  ausente, cabeçalho inválido ou separador inválido) impedem a inicialização com mensagem clara.
  Variações inofensivas são aceitas: linhas em branco, espaços extras, BOM e `yes` sem diferenciar
  maiúsculas. Linhas inválidas (ano não numérico, colunas faltando, título vazio ou `winner` diferente de
  `yes` ou vazio) são ignoradas, com log de aviso indicando o número da linha e o motivo, e um resumo na
  inicialização com o total de linhas lidas e rejeitadas. Cada caso tem um teste de integração.
- Q: Duas linhas com o mesmo ano e o mesmo título contam como dois filmes ou como um só? → A: Como um
  só. Ano e título, sem os espaços em volta, identificam o filme. A primeira ocorrência prevalece, e as
  repetições são ignoradas, com aviso no log e contagem como rejeitadas no resumo.
- Q: Uma linha com a coluna de produtores vazia deve ser aceita ou rejeitada? → A: Rejeitada. Ano,
  título e produtores são colunas obrigatórias: uma linha sem alguma delas é rejeitada, com aviso no log
  (número da linha e motivo) e contagem como rejeitada no resumo. Estúdios é opcional. `winner` vazio
  significa não vencedor.
- Q: Um cabeçalho com os nomes certos em maiúsculas diferentes ou em outra ordem deve ser aceito? → A:
  A diferença de maiúsculas é aceita, mas a ordem das colunas é fixa. Uma ordem diferente impede a
  inicialização.

### Session 2026-11-19

- Q: Como testar a carga do arquivo antes de existir qualquer endpoint de negócio? → A: Como manda o
  Princípio I da constituição (1.2.0): com a aplicação real no ar, verificando o log e o banco até o
  endpoint existir, e depois por HTTP. Os testes de falha de inicialização seguem a cláusula própria do
  princípio.

### Session 2026-11-22

Decisões tomadas a partir dos casos do testador adversário (Codex), em `docs/ai/adversario/`.

- Q: O separador `and` diferencia maiúsculas (`AND`, `aNd`)? → A: Não. A palavra `and` é reconhecida
  como separador em qualquer combinação de maiúsculas e minúsculas (FR-007). Os nomes continuam
  comparados exatamente como escritos (FR-008). Caso 12.
- Q: Com que regra os nomes empatados são ordenados? → A: Pela ordem dos caracteres Unicode
  (`String.compareTo`), independente de locale, sem colação linguística (FR-015). Caso 13.
- Q: Qual é a faixa válida de ano? → A: Um inteiro entre 1 e 9999. Fora disso, a linha é rejeitada como
  inválida (FR-006). Caso 09.

## User Scenarios & Testing *(mandatory)*

Os atores são o **avaliador da Outsera**, que sobe a aplicação e confere o resultado (manualmente ou por
testes automatizados com outros arquivos), e o **consumidor da API**, que lê os dados de filmes.

### User Story 1 - Consultar os produtores com menor e maior intervalo entre prêmios (Priority: P1)

O avaliador sobe a aplicação, que carrega a lista de indicados e vencedores da categoria Pior Filme do
Golden Raspberry Awards, e consulta quais produtores tiveram o menor e o maior intervalo entre duas
vitórias consecutivas.

**Why this priority**: É o único requisito funcional obrigatório do enunciado (seções 3 e 6). Sem ele não
há entrega. A avaliação poderá usar outros conjuntos de dados, então o cálculo precisa estar correto para
**qualquer** arquivo válido, e não só para o arquivo fornecido.

**Independent Test**: Subir a aplicação com arquivos de cenário diferentes, cada um com o resultado
esperado calculado de forma independente, e comparar com a resposta. O arquivo fornecido é só um desses
cenários.

**Acceptance Scenarios**:

1. **Given** a aplicação iniciada com o arquivo fornecido (um dos cenários), **When** os intervalos são
   consultados, **Then** a resposta tem status 200, `min` contém exatamente
   `{ "producer": "Joel Silver", "interval": 1, "previousWin": 1990, "followingWin": 1991 }` e `max`
   contém exatamente
   `{ "producer": "Matthew Vaughn", "interval": 13, "previousWin": 2002, "followingWin": 2015 }`.
2. **Given** um arquivo em que dois produtores empatam no menor intervalo, **When** os intervalos são
   consultados, **Then** os dois aparecem em `min`, ordenados por `previousWin` crescente e depois pelo
   nome.
3. **Given** um arquivo em que dois produtores empatam no maior intervalo, **When** os intervalos são
   consultados, **Then** os dois aparecem em `max`, com a mesma ordenação.
4. **Given** um produtor com vitórias em 2000, 2005 e 2020, **When** os intervalos são consultados,
   **Then** só os pares vizinhos (2000–2005 e 2005–2020) são candidatos, e o par 2000–2020 nunca aparece.
5. **Given** um arquivo com as linhas fora da ordem cronológica, **When** os intervalos são consultados,
   **Then** o resultado é idêntico ao do mesmo arquivo ordenado.
6. **Given** um produtor com duas vitórias no mesmo ano, **When** os intervalos são consultados,
   **Then** esse par gera um intervalo 0.
7. **Given** um arquivo em que nenhum produtor venceu duas vezes, **When** os intervalos são
   consultados, **Then** a resposta tem status 200 com `min` e `max` vazios.
8. **Given** um arquivo sem nenhum dos produtores do arquivo fornecido (por exemplo, `Producer A` com
   vitórias em 1950 e 1952 e `Producer B` com vitórias em 1960 e 2010), **When** os intervalos são
   consultados, **Then** `min` contém só `Producer A` (2, 1950, 1952) e `max` contém só `Producer B`
   (50, 1960, 2010). Nada do arquivo fornecido aparece na resposta.
9. **Given** a aplicação iniciada com dois arquivos diferentes, em execuções separadas, **When** os
   intervalos são consultados em cada uma, **Then** cada resposta reflete só o arquivo carregado naquela
   execução.

---

### User Story 2 - Carregar o arquivo de filmes na inicialização (Priority: P1)

Ao iniciar, a aplicação lê o arquivo CSV de filmes, a partir de um caminho configurável que tem o
arquivo fornecido como padrão, e disponibiliza os dados antes de atender qualquer consulta. Um arquivo
ausente ou malformado impede a inicialização com uma mensagem clara.

**Why this priority**: É pré-requisito da história 1, e o enunciado avisa que a correção usará outros
arquivos, então a leitura precisa ser fiel ao formato e falhar de forma explícita.

**Independent Test**: Iniciar a aplicação apontando para arquivos de cenário (válido, sem cabeçalho,
inexistente, com variações de formato) e verificar se ela sobe com os dados corretos ou falha com a
mensagem esperada.

**Acceptance Scenarios**:

1. **Given** nenhum caminho configurado, **When** a aplicação inicia, **Then** ela carrega o arquivo
   fornecido com o projeto.
2. **Given** um caminho configurado para outro arquivo válido, **When** a aplicação inicia, **Then** os
   dados consultados vêm desse arquivo.
3. **Given** um caminho para um arquivo inexistente, **When** a aplicação inicia, **Then** a
   inicialização é interrompida com uma mensagem que cita o caminho e o motivo.
4. **Given** um arquivo sem o cabeçalho `year;title;studios;producers;winner`, **When** a aplicação
   inicia, **Then** a inicialização é interrompida com uma mensagem que cita o cabeçalho esperado.
5. **Given** linhas com `winner` igual a `yes`, `YES`, ` Yes ` e vazio, **When** a carga termina,
   **Then** as três primeiras são vencedoras e a última é apenas indicada.
6. **Given** produtores escritos como `Eric Fellner, and Tom Hooper`, `Gayle Dickie, Kenn Viselman`,
   `Brian Robbinsand Sharla Sumpter Bridgett` e `Sid and Marty Krofft`, **When** a carga termina,
   **Then** eles são separados, respectivamente, em `Eric Fellner` e `Tom Hooper`; `Gayle Dickie` e
   `Kenn Viselman`; um único nome `Brian Robbinsand Sharla Sumpter Bridgett`; e `Sid` e `Marty Krofft`.
7. **Given** os nomes `Steven Perry` e `Steve Perry`, **When** os intervalos são calculados, **Then**
   eles são tratados como produtores distintos.
8. **Given** um arquivo separado por vírgula em vez de `;`, **When** a aplicação inicia, **Then** a
   inicialização é interrompida com uma mensagem que cita o separador esperado.
9. **Given** um arquivo válido com BOM no início, linhas em branco e espaços extras em volta dos campos,
   **When** a carga termina, **Then** todas as linhas com dados são aceitas, sem aviso, e o resultado é
   igual ao do mesmo arquivo sem essas variações.
10. **Given** um arquivo válido com uma linha de ano não numérico, uma com ano vazio, uma com colunas
    faltando, uma com título vazio, uma com produtores vazios e uma com `winner` igual a `no`, **When** a
    aplicação inicia, **Then** ela sobe, as seis linhas são ignoradas, cada uma gera um aviso no log com o número da linha e o motivo, e as
    demais linhas são carregadas normalmente.
11. **Given** um arquivo com 10 linhas de dados, das quais 2 são inválidas, **When** a carga termina,
    **Then** o log de inicialização traz um resumo com 10 linhas lidas, 8 aceitas e 2 rejeitadas.
12. **Given** um arquivo em que uma linha inválida contém um produtor vencedor, **When** os intervalos são
    consultados, **Then** essa linha não influencia o resultado.
13. **Given** um arquivo em que a linha vencedora `1990;Filme X;Estúdio;Producer A;yes` aparece duas
    vezes, e `Producer A` também vence em 1995, **When** os intervalos são consultados, **Then** só existe
    o intervalo 1990–1995 (5). Não há intervalo 0, a repetição gera um aviso, e o resumo a conta como
    rejeitada.
14. **Given** uma linha com a coluna de estúdios vazia e os demais campos válidos, **When** a carga
    termina, **Then** a linha é aceita, sem aviso, e o filme aparece na listagem com estúdios vazio.
15. **Given** o cabeçalho `Year;Title;Studios;Producers;Winner`, **When** a aplicação inicia, **Then** a
    carga acontece normalmente, sem aviso.
16. **Given** o cabeçalho `title;year;studios;producers;winner` (colunas em outra ordem), **When** a
    aplicação inicia, **Then** a inicialização é interrompida com uma mensagem que mostra o cabeçalho
    esperado e o encontrado.

---

### User Story 3 - Listar os filmes com paginação e filtros (Priority: P2)

O consumidor da API lista os filmes carregados, página a página, e pode filtrar por ano e pela condição
de vencedor.

**Why this priority**: A seção 1 do enunciado fala em leitura da lista de indicados e vencedores, mas a
seção 3 exige só os intervalos. É um extra de leitura básica, que não altera o contrato obrigatório.

**Independent Test**: Com um arquivo de cenário de tamanho conhecido, listar sem filtros, com cada filtro
e com os dois combinados, e conferir itens, totais e paginação. Enviar parâmetros inválidos e conferir
o erro.

**Acceptance Scenarios**:

1. **Given** os filmes carregados, **When** a listagem é consultada sem parâmetros, **Then** a resposta
   traz a primeira página com o tamanho padrão, o total de itens e o total de páginas.
2. **Given** os filmes carregados, **When** a listagem é filtrada por um ano, **Then** só aparecem
   filmes desse ano.
3. **Given** os filmes carregados, **When** a listagem é filtrada por vencedor verdadeiro ou falso,
   **Then** só aparecem os vencedores, ou só os não vencedores.
4. **Given** os dois filtros combinados, **When** a listagem é consultada, **Then** só aparecem filmes
   que atendem aos dois.
5. **Given** um ano não numérico, um valor de vencedor que não seja booleano, uma página negativa ou um
   tamanho fora do limite, **When** a listagem é consultada, **Then** a resposta tem status 400 no
   formato padronizado de erro (Problem Details), indicando o parâmetro inválido.
6. **Given** um filtro sem resultados, **When** a listagem é consultada, **Then** a resposta tem status
   200 com a lista vazia e total zero.

---

### User Story 4 - Explorar a API pela documentação interativa (Priority: P3)

O avaliador abre a documentação interativa no navegador, vê os dois endpoints, com parâmetros e
exemplos de resposta, e consegue executar as chamadas por ali.

**Why this priority**: Facilita a avaliação, mas não é exigida pelo enunciado.

**Independent Test**: Com a aplicação no ar, abrir a página de documentação e o contrato legível por
máquina e conferir se os dois endpoints estão descritos. Validar as respostas reais contra o contrato.

**Acceptance Scenarios**:

1. **Given** a aplicação no ar, **When** a página de documentação é aberta, **Then** ela lista o
   endpoint de intervalos e o de listagem, com parâmetros, formatos de resposta e exemplos.
2. **Given** o contrato publicado, **When** as respostas reais dos endpoints são comparadas com ele,
   **Then** todas são compatíveis.

---

### Edge Cases

- Um único intervalo em todo o arquivo: ele aparece em `min` e em `max`.
- Todos os intervalos iguais: todos aparecem em `min` e em `max`.
- Ano sem vencedor (como 2007 no arquivo fornecido): não afeta o cálculo.
- Indicados que não venceram: não contam para os intervalos de nenhum produtor.
- Espaços extras em volta dos nomes de produtores ou depois de vírgulas: são removidos antes da
  comparação; o restante do nome é comparado exatamente como escrito.
- Separador que gera um nome vazio (por exemplo, `A, , B`): o nome vazio é descartado, e a linha é
  aceita com `A` e `B`.
- Coluna de produtores só com separadores (por exemplo, ` , and `): nenhum nome resulta, então a linha é
  rejeitada como produtores vazios (FR-006).
- Coluna de estúdios vazia: a linha é aceita (FR-006e).
- Cabeçalho com maiúsculas diferentes: aceito. Cabeçalho com as colunas em outra ordem: erro estrutural
  (FR-003).
- Mesmo filme listado com o mesmo produtor duas vezes: o produtor conta uma única vez para aquele filme.
- Arquivo apenas com o cabeçalho, sem linhas: a aplicação sobe, a listagem vem vazia e os intervalos
  vêm com `min` e `max` vazios.
- Arquivo com codificação UTF-8 e acentos em títulos ou nomes: os textos são preservados.
- Arquivo com BOM, linhas em branco (inclusive no final) ou espaços extras em volta dos campos e do
  cabeçalho: aceito sem aviso (FR-005a).
- Linha inválida dentro de um arquivo válido (ano não numérico, colunas faltando, título vazio ou
  `winner` com valor diferente de `yes` ou vazio): a linha é ignorada, com aviso (FR-006).
- Arquivo em que todas as linhas de dados são inválidas: a aplicação sobe, o resumo mostra zero linhas
  aceitas, e a listagem e os intervalos vêm vazios.
- Linha com colunas a mais que o cabeçalho: tratada como inválida (colunas em número diferente do
  esperado).
- Ano `0`, ano com mais de quatro dígitos significativos (como `10000` ou `2147483648`) ou ano com
  sinal: a linha é rejeitada (FR-006). Zeros à esquerda não mudam o valor (`0042` é o ano 42).
- Separador `and` em maiúsculas (`AND`, `aNd`): separa nomes como `and` (FR-007).
- Nomes empatados que diferem só em acento ou em maiúsculas (`ana`, `Ana`, `Álvaro`): são produtores
  distintos (FR-008), ordenados pela ordem Unicode (FR-015).
- Linha vencedora duplicada (mesmo ano e título): ignorada (FR-006d), então não gera um intervalo 0
  falso. Dois filmes **diferentes** do mesmo produtor no mesmo ano continuam gerando intervalo 0
  (FR-013).

## Requirements *(mandatory)*

### Functional Requirements

**Carga do arquivo**

- **FR-001**: O sistema MUST carregar o arquivo CSV de filmes durante a inicialização e só aceitar
  consultas depois que a carga terminar.
- **FR-002**: O caminho do arquivo MUST ser configurável por variável de ambiente ou propriedade, e o
  padrão MUST ser o arquivo fornecido no enunciado, embarcado no projeto.
- **FR-003**: O sistema MUST ler o arquivo com `;` como separador de colunas e exigir o cabeçalho
  `year;title;studios;producers;winner` nessa ordem exata. Os nomes das colunas são comparados sem
  diferenciar maiúsculas (por exemplo, `Year;Title;Studios;Producers;Winner` é aceito). Uma ordem
  diferente, uma coluna faltando ou uma coluna a mais no cabeçalho é erro estrutural (FR-005).
- **FR-004**: Um filme MUST ser considerado vencedor quando a coluna `winner` contém `yes`, sem
  diferenciar maiúsculas e ignorando espaços em volta. Vazio significa apenas indicado.
- **FR-005**: Erros estruturais MUST interromper a inicialização com uma mensagem que identifique o
  arquivo e o motivo. São erros estruturais: arquivo ausente ou ilegível, cabeçalho diferente do
  esperado e separador de colunas diferente de `;`.
- **FR-005a**: Variações inofensivas MUST ser aceitas sem aviso: BOM no início do arquivo, linhas em
  branco em qualquer posição, espaços extras em volta dos campos e do cabeçalho, nomes de colunas do
  cabeçalho em qualquer combinação de maiúsculas e minúsculas, e `yes` em qualquer combinação de
  maiúsculas e minúsculas.
- **FR-006**: Uma linha de dados inválida MUST ser ignorada, sem interromper a carga. A linha é
  inválida quando:
  - o número de colunas difere do cabeçalho;
  - o ano está vazio ou não é numérico (só dígitos, sem sinal nem decimal);
  - o ano não é um inteiro entre 1 e 9999 (por exemplo, `0` ou `10000`);
  - o título está vazio;
  - a coluna de produtores está vazia ou não gera nenhum nome depois da separação (FR-007 a FR-010);
  - `winner` tem um valor diferente de `yes` ou vazio.
- **FR-006e**: Ano, título e produtores são colunas obrigatórias. A coluna de estúdios é opcional: vazia,
  ela não invalida a linha. `winner` vazio significa não vencedor (FR-004).
- **FR-006a**: Cada linha ignorada MUST gerar um aviso no log com o número da linha no arquivo e o
  motivo da rejeição.
- **FR-006b**: Ao terminar a carga, o sistema MUST registrar no log um resumo com o total de linhas de
  dados lidas, aceitas e rejeitadas.
- **FR-006d**: Um filme MUST ser identificado pelo par ano + título, sem os espaços em volta. Se uma
  linha repetir o ano e o título de uma linha já aceita, ela MUST ser ignorada como duplicata, com aviso
  no log (número da linha, motivo e linha original) e contagem como rejeitada no resumo. A primeira
  ocorrência prevalece, mesmo que as demais colunas sejam diferentes.
- **FR-006c**: Cada tipo de erro estrutural, cada variação inofensiva e cada motivo de rejeição de
  linha MUST ter um teste de integração próprio.

**Separação de produtores**

- **FR-007**: A coluna de produtores MUST ser dividida em nomes usando como separadores a vírgula, a
  palavra `and` e a combinação `, and`. O `and` só separa quando é uma palavra inteira: antes dele vem
  espaço, vírgula ou o início do campo, e depois dele vem espaço ou o fim do campo. Por exemplo,
  `Producer A and` e `Producer A, and` dão só `Producer A`, e `, and` não dá nenhum nome. Vários `and`
  seguidos são todos separadores: `Producer A and and Producer B` dá `Producer A` e `Producer B`, e
  `and and` não dá nenhum nome. O `and` é reconhecido sem diferenciar maiúsculas: `AND`, `And` e `aNd`
  também são separadores, e `Iris AND Nilo aNd Eva` dá `Iris`, `Nilo` e `Eva`. Essa regra vale só para
  o separador. Os nomes continuam comparados exatamente como escritos (FR-008).
- **FR-008**: Cada nome MUST ter os espaços em volta removidos e ser comparado exatamente como escrito,
  sem corrigir grafia, abreviação ou maiúsculas.
- **FR-009**: Um `and` que faz parte de uma palavra (por exemplo, `Robbinsand`, `Anderson` ou
  `andrew`) MUST NOT separar nomes.
- **FR-010**: Nomes vazios resultantes da separação MUST ser descartados, e um mesmo nome repetido no
  mesmo filme MUST contar uma única vez.

**Cálculo dos intervalos**

- **FR-011**: Só os filmes vencedores MUST ser considerados no cálculo.
- **FR-012**: Para cada produtor, o sistema MUST ordenar os anos de vitória e gerar um intervalo para
  cada par de vitórias vizinhas, com `interval = followingWin - previousWin`. A primeira vitória nunca
  é comparada diretamente com a última quando há outras entre elas.
- **FR-013**: Duas vitórias do mesmo produtor no mesmo ano MUST gerar um intervalo 0.
- **FR-014**: `min` MUST conter todos os intervalos iguais ao menor valor, e `max` todos os intervalos
  iguais ao maior valor.
- **FR-015**: Os itens de `min` e de `max` MUST ser ordenados por `previousWin` crescente e, em caso de
  empate, pelo nome do produtor. Os nomes são comparados pela ordem dos caracteres Unicode, um a um
  (`String.compareTo` do Java), independente de locale e sem colação linguística. Por exemplo, `Ana`,
  `Zeca`, `ana` e `Álvaro` ficam nessa ordem, porque `A` < `Z` < `a` < `Á`.
- **FR-016**: O resultado MUST ser o mesmo, qualquer que seja a ordem das linhas no arquivo.
- **FR-016a**: O resultado MUST ser derivado só do conteúdo do arquivo carregado. Nenhum nome de
  produtor, ano, intervalo ou resultado esperado pode ser fixado no código ou na configuração. O único
  valor padrão permitido é o caminho do arquivo (FR-002).
- **FR-016b**: O cálculo MUST estar correto para qualquer arquivo que siga o formato do enunciado,
  qualquer que seja a quantidade de filmes ou de produtores e a faixa de anos válidos (1 a 9999, FR-006).

**Endpoint de intervalos**

- **FR-017**: O sistema MUST expor `GET /api/producers/intervals`, que responde com status 200 e
  exatamente o formato da página 2 do enunciado: um objeto com as listas `min` e `max`, cujos itens têm
  somente os campos `producer` (texto), `interval`, `previousWin` e `followingWin` (inteiros).
- **FR-018**: Quando nenhum produtor tem duas vitórias, a resposta MUST ter status 200 com `min` e `max`
  vazios.

**Listagem de filmes**

- **FR-019**: O sistema MUST expor `GET /api/movies`, que devolve uma página de filmes com ano, título,
  estúdios, produtores e a indicação de vencedor, além do número da página, do tamanho, do total de
  itens e do total de páginas.
- **FR-020**: A listagem MUST aceitar os filtros opcionais `year` (inteiro) e `winner` (booleano),
  combináveis entre si.
- **FR-021**: A listagem MUST aceitar `page` (começando em 0, padrão 0) e `size` (padrão 20, de 1 a
  100).
- **FR-022**: A listagem MUST ser ordenada por ano crescente e depois pelo título.
- **FR-023**: Parâmetros inválidos MUST resultar em status 400 no formato Problem Details (RFC 9457),
  identificando o parâmetro e o motivo.

**Documentação**

- **FR-024**: O sistema MUST publicar uma página de documentação interativa e um contrato legível por
  máquina que descrevam os dois endpoints, com parâmetros, respostas e exemplos.
- **FR-025**: As respostas reais dos dois endpoints MUST ser compatíveis com o contrato publicado.

**Restrições do enunciado**

- **FR-026**: A aplicação MUST subir e todos os testes desta funcionalidade MUST passar usando só o
  wrapper de build do projeto, sem instalação externa nem contêineres.
- **FR-027**: Cada requisito desta spec MUST ser comprovado por pelo menos um teste que siga o
  Princípio I da constituição (testes de integração).

### Key Entities

- **Filme**: um registro do arquivo, identificado de forma única pelo par ano + título. Tem ano, título, estúdios (texto como no arquivo), produtores
  (texto como no arquivo, mais a lista de nomes separados) e a indicação de vencedor.
- **Produtor**: um nome obtido da separação da coluna de produtores. A identidade é o texto exato,
  depois de remover os espaços em volta. Um produtor se relaciona a vários filmes.
- **Intervalo**: derivado, não armazenado. Liga um produtor a duas vitórias vizinhas (`previousWin` e
  `followingWin`) e à diferença entre elas (`interval`).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Em 100% dos arquivos de cenário, a consulta de intervalos devolve exatamente o resultado
  esperado calculado de forma independente. No cenário do arquivo fornecido, isso é Joel Silver em `min`
  (1, 1990, 1991) e Matthew Vaughn em `max` (13, 2002, 2015), sem nenhum outro item.
- **SC-002**: Todos os cenários de teste do briefing passam: arquivo fornecido, empates no mínimo e no
  máximo, três ou mais vitórias, linhas fora de ordem, duas vitórias no mesmo ano, nenhum produtor com
  duas vitórias, formatos de `and` e de vírgula, variações de `yes` e arquivo sem cabeçalho válido.
- **SC-003**: Uma pessoa com apenas o ambiente Java instalado sobe a aplicação e obtém a resposta de
  intervalos com um único comando, em menos de 2 minutos a partir do repositório clonado (com as
  dependências já baixadas).
- **SC-004**: Para qualquer arquivo de ataque que siga o formato do enunciado, o resultado coincide com
  um cálculo de referência feito à mão ou por um script independente.
- **SC-004a**: Pelo menos um cenário de aceitação usa um arquivo sem nenhum produtor nem ano em comum com
  o arquivo fornecido, e passa.
- **SC-004b**: Uma busca no código de produção não encontra nenhum nome de produtor, ano de prêmio ou
  valor de intervalo vindo dos dados.
- **SC-005**: Com o arquivo fornecido, as consultas de intervalos e de listagem respondem em menos de
  1 segundo.
- **SC-006**: Com um arquivo ausente, de cabeçalho inválido ou de separador inválido, 100% das tentativas
  de inicialização terminam com uma mensagem que permite identificar a causa sem consultar o código.
- **SC-006a**: Com um arquivo que tem linhas inválidas, a aplicação sobe, e 100% das linhas rejeitadas
  podem ser localizadas no arquivo a partir do log (número da linha e motivo), com o resumo batendo com
  a contagem real.
- **SC-007**: A documentação interativa descreve 100% dos endpoints expostos, e 100% das respostas
  testadas são compatíveis com o contrato.

## Assumptions

- O arquivo fornecido pela Outsera (`movielist.csv`) será adicionado ao projeto como recurso padrão; ele
  ainda não está no repositório. Ele é só o arquivo carregado por padrão e um dos cenários de aceitação,
  sem nenhum tratamento especial no cálculo.
- A avaliação poderá trocar o arquivo por outro conjunto de dados no mesmo formato, usando o caminho
  configurável (FR-002) ou substituindo o recurso padrão.
- Os dados ficam em um banco relacional em memória (H2) no modo padrão, recarregados a cada
  inicialização. Não há escrita pela API neste marco.
- A documentação interativa é o Swagger UI, servido pela própria aplicação em `/swagger-ui.html`. O
  contrato legível por máquina é servido em `/openapi.yaml` e é o mesmo arquivo usado na validação das
  respostas pelos testes (contrato definido antes do código; ver `research.md`, R8).
- Os caminhos `/api/producers/intervals` e `/api/movies` são propostas, porque o enunciado não fixa a
  URL.
- Na listagem, estúdios e produtores são devolvidos como texto, exatamente como estão no arquivo, porque
  é uma leitura básica.
- O arquivo está em UTF-8.
- Fora do escopo deste marco: Docker, Kafka, PostgreSQL, autenticação, ETag, observabilidade além do
  padrão do framework, pipeline, deploy e front-end.
- Limitação conhecida e aceita: nomes compostos com `and` (como `Sid and Marty Krofft`) são separados em
  dois produtores, e grafias diferentes da mesma pessoa contam como produtores distintos.
