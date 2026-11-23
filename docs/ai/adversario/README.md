# Casos de teste dos intervalos de produtores

Carregue cada CSV em uma execução separada, pelo caminho configurável da FR-002. Consulte `GET /api/producers/intervals` após a carga. Para os casos com `expected.json`, compare o corpo JSON da resposta HTTP 200, preservando a ordem e a multiplicidade dos itens em `min` e `max`.

Os casos 12 e 13 isolam pontos em que a especificação não determina uma resposta única. A coluna de observações descreve a decisão em aberto.

| Nº | Nome | Regras da spec | Por que pode falhar ou qual é a ambiguidade |
| --- | --- | --- | --- |
| 01 | Empates no mínimo e no máximo | FR-012, FR-014, FR-015, FR-016 | Os dois extremos têm três pares. A ordem de leitura e a ordem dos nomes no CSV diferem da ordem exigida. Ordenar só por nome ou guardar um único registro por extremo perde parte do resultado. |
| 02 | Vários pares do mesmo produtor | FR-012, FR-014, FR-015, FR-016 | Lia concentra dois mínimos e dois máximos, intercalados com outros intervalos. Agrupar por produtor na saída, comparar a primeira vitória com a última ou parar após as primeiras vitórias altera a resposta. |
| 03 | Intervalos zero com multiplicidade | FR-006d, FR-010, FR-012, FR-013, FR-014, FR-015 | Quatro filmes distintos de Ada no mesmo ano geram três objetos idênticos. Deduplicar anos ou objetos perde intervalos. Contar uma linha duplicada ou um crédito repetido no mesmo filme cria zeros excedentes. |
| 04 | Nomes parecidos, contidos e quase iguais | FR-008, FR-011, FR-012, FR-014 | `Perry` está contido nos demais nomes. `Steve Perry` tem uma variante em caixa alta, e `Alex  Silva` tem um espaço interno a mais que `Alex Silva`. Busca por substring, chave sem distinção de caixa ou compactação de espaços inventam pares. |
| 05 | Vírgulas vazias e `and` em sequência | FR-007, FR-008, FR-009, FR-010, FR-014 | Há `and` no início e no fim, dois `and` seguidos, vírgulas com trecho vazio, `, and` e crédito repetido. Separar por substring ainda pode fundir `Sharla` com parte de `Brian Robbinsand Sharla`. |
| 06 | BOM, cabeçalho misto e UTF-8 | FR-003, FR-004, FR-005a, FR-006e, FR-008, FR-011 | O arquivo combina BOM, linhas em branco antes e depois do cabeçalho, espaços em volta dos campos, `yes` em caixa mista e estúdio vazio. O indicado cairia dentro do maior intervalo se fosse contado. Decodificação incorreta altera os nomes no JSON. |
| 07 | Linhas inválidas entre vencedores | FR-006, FR-006d, FR-007, FR-010, FR-011, FR-012 | Linhas com colunas faltantes ou extras, anos inválidos, campos obrigatórios vazios, só separadores e `winner` inválido ficam entre vitórias. Uma inválida repete a chave de uma vencedora válida posterior. Reservar a chave cedo descarta uma vitória legítima. |
| 08 | Duplicatas com dados conflitantes | FR-004, FR-006d, FR-011, FR-012, FR-013 | Uma indicação vem antes de sua duplicata vencedora, e uma vitória vem antes de duplicatas com outros produtores e outro `winner`. O mesmo título em anos diferentes e os títulos distintos de 2004 são filmes independentes. |
| 09 | Anos e intervalos extremos | FR-006, FR-012, FR-014, FR-016b, FR-017 | Os anos incluem zero e cruzam o limite de 32 bits com sinal. Um intervalo de 4294967296 também excede 32 bits sem sinal. Conversões estreitas, ordenação textual ou validação que presume anos de quatro dígitos alteram os extremos. |
| 10 | Repetições sem uma segunda vitória | FR-004, FR-006, FR-006d, FR-010, FR-011, FR-018 | Cada produtor tem no máximo uma vitória aceita, apesar de créditos repetidos, duplicatas, indicados e uma linha inválida que parece vencedora. Contar aparições em lugar de filmes vencedores produz falsos intervalos. |
| 11 | Todos os pares nos dois extremos | FR-012, FR-014, FR-015, FR-016 | Os quatro pares têm o mesmo intervalo e precisam aparecer nas duas listas. Um `else if` para escolher um extremo ou uma saída abreviada quando mínimo e máximo coincidem deixa itens de fora. |
| 12 | Caixa do separador `and` | FR-007, FR-008, FR-012, FR-014 | FR-007 descreve `and` e seus limites, mas não explicita a comparação de caixa do separador. FR-008 trata a identidade do nome após a divisão. Reconhecer `AND` ou `aNd` cria pares de Iris, Nilo ou Eva, enquanto preservar o campo inteiro cria um par do nome composto. Sem oráculo. |
| 13 | Ordenação com acentos e caixa | FR-008, FR-014, FR-015 | Os quatro produtores são distintos e empatam em `previousWin` e no intervalo. FR-015 manda ordenar pelo nome, mas não fixa colação, locale nem desempate para nomes equivalentes sob a colação. A ordem ordinal e a linguística podem divergir. Sem oráculo. |

> **Casos 12 e 13: resultado esperado escrito pelo implementador (Claude Code), não pelo testador.**
> O testador entregou esses dois CSVs sem `expected.json`. Os arquivos
> `12-caixa-do-separador-and.expected.json` e `13-ordenacao-com-acentos-e-caixa.expected.json` foram
> escritos depois, a partir das decisões da sessão 2026-11-22 da spec (registradas em
> `docs/ai/decisoes.md`): no caso 12, `and` é separador sem diferenciar maiúsculas (FR-007); no caso 13,
> os nomes empatados seguem a ordem Unicode de `String.compareTo`, independente de locale (FR-015).
>
> **Caso 09: o teste diverge do testador por causa da decisão da faixa de anos.** A sessão 2026-11-22
> da spec limitou o ano a um inteiro de 1 a 9999 (FR-006). As linhas dos anos 0, 2147483647, 2147483648
> e 4294967296 passam a ser rejeitadas. Sobram os anos 1 e 2, de produtores diferentes, e o esperado vira
> `min` e `max` vazios. O `09-anos-e-intervalo-extremos.expected.json` desta pasta continua sendo o
> original do testador, sem alteração. O esperado ajustado está só na cópia de
> `src/test/resources/scenarios/adversario/`, usada pelo `Case09ExtremeYearsIT`. O caso revelou que o
> ano 0 era aceito.

No caso 09, a FR-016b abrange qualquer faixa de anos. O contrato declara `year`, `previousWin`, `followingWin` e `interval` como `integer`, sem formato `int32` nem máximo. Os anos do CSV são inteiros escritos só com dígitos.
