# Contrato da interface: rotas, painéis e estados do front

Contrato de comportamento do front, conferido pelos testes unitários e pelo de ponta a ponta (FR-007 a
FR-018). Os textos entre aspas são os exibidos e servem de seletor nos testes.

## Rotas

| Rota | Tela | Título da página |
| --- | --- | --- |
| `/` | Dashboard | "Painel — Golden Raspberry Awards" |
| `/filmes` | Lista de filmes | "Filmes — Golden Raspberry Awards" |
| qualquer outra | redireciona para `/` | — |

Menu fixo no topo com "Painel" e "Filmes"; o item da tela atual tem `aria-current="page"`. No celular, o menu
continua visível, sem menu sanfona.

## Dashboard (`/`)

Quatro painéis, cada um com um `h2` e o seu próprio estado (carregando, dados, vazio, erro).

| Painel (`h2`) | Fonte | Conteúdo | Vazio |
| --- | --- | --- | --- |
| "Anos com mais de um vencedor" | `GET /api/aggregations/years-with-multiple-winners` | tabela Ano, Vencedores | "Nenhum ano com mais de um vencedor." |
| "Estúdios com mais vitórias" | `GET /api/aggregations/studios-with-win-count` | os 3 primeiros itens: Estúdio, Vitórias | "Nenhum estúdio vencedor." |
| "Produtores com o maior e o menor intervalo" | `GET /api/producers/intervals` | duas tabelas, "Maior intervalo" (`max`) e "Menor intervalo" (`min`): Produtor, Intervalo, Vitória anterior, Vitória seguinte; todos os itens | "Nenhum produtor com duas vitórias." |
| "Vencedores por ano" | `GET /api/movies?year={ano}&winner=true&size=100` | campo "Ano" + botão "Buscar"; tabela Ano, Título, Estúdios, Produtores | "Nenhum vencedor em {ano}." |

- Antes da primeira busca, o quarto painel mostra só o campo e o botão.
- Ano inválido: "Informe um ano válido (só números)." abaixo do campo; o botão fica desabilitado e nada é
  enviado.
- Erro em qualquer painel: "Não foi possível carregar os dados." e o botão "Tentar de novo", que repete só a
  chamada daquele painel.
- Carregando: "Carregando…" com `aria-live="polite"`.
- A busca mais recente vence: uma busca nova cancela a pendente, e só a resposta dela aparece (FR-008a).
- Os dados do CSV (títulos, estúdios e produtores), em todas as telas, aparecem sempre como texto, com `<` e `>`
  visíveis, sem virar elementos da página (FR-017a).

## Lista de filmes (`/filmes`)

### Parâmetros da URL

| Parâmetro | Valores válidos | Inválido ou ausente |
| --- | --- | --- |
| `ano` | só dígitos (até 9), depois de remover os espaços das pontas; um espaço interno é inválido | sem filtro de ano |
| `vencedor` | `sim`, `nao` (exatamente) | todos |
| `pagina` | só dígitos, de 1 até 2^53 − 1 (inteiro seguro) | 1 |

- Num parâmetro repetido, vale a primeira ocorrência. Parâmetros desconhecidos (inclusive `page`, `size`,
  `year` e `winner`) são ignorados: a consulta à API é montada só com os valores validados (spec 006, FR-013).
- Uma página válida cujo índice passa do máximo da API (2147483647) consulta esse máximo, que está sempre além da
  última página: a tela mostra "Nenhum filme encontrado." e o link de volta.

Mudar o ano ou o vencedor grava a URL com `pagina` removida (volta à 1). Mudar a página grava só `pagina`. A
URL é a fonte do estado: abrir, recarregar ou voltar no navegador refaz a consulta.

### Conteúdo

- Filtros: campo "Ano" (com a mesma validação do Dashboard) e seletor "Vencedor" com "Todos", "Sim" e "Não". O
  filtro de ano é aplicado com o botão "Filtrar" ou com Enter; o de vencedor, ao mudar.
- Tabela no desktop e cartões abaixo de 768 px: Ano, Título, Estúdios, Produtores, Vencedor ("Sim"/"Não").
  Estúdios nulos aparecem como "—".
- Resumo: "{totalItens} filmes" e "Página {pagina} de {totalPaginas}".
- Paginação: "Primeira", "Anterior", "Próxima", "Última". Na primeira página, "Primeira" e "Anterior" ficam
  desabilitados; na última, "Próxima" e "Última".
- Vazio (inclusive página além da última): "Nenhum filme encontrado." e o link "Voltar à primeira página"
  quando `pagina > 1`.
- Erro: o mesmo do Dashboard, com "Tentar de novo".

## Acessibilidade e layout (FR-015 e FR-016)

- `lang="pt-BR"`; um `h1` por tela; `label` associado a cada campo; tabelas com `caption` e `th scope="col"`.
- Ordem de tabulação: menu, filtros ou busca, conteúdo, paginação. Foco visível em todos os controles.
- Sem rolagem horizontal de 360 px a 1920 px: textos longos quebram linha (`overflow-wrap: anywhere`).
- Abaixo de 768 px: painéis empilhados e linhas como cartões. A partir de 768 px: grade de duas colunas no
  Dashboard e tabela na lista.
