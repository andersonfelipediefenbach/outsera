# Registro de decisões de planejamento — Teste Outsera

18/11/2026 · Anderson

Este documento resume as decisões tomadas antes do código, com as alternativas consideradas e o motivo de cada escolha. É uma síntese do planejamento feito com apoio de IA; as conversas originais não foram incluídas.

## Como a IA foi usada no planejamento

Dois assistentes foram usados antes do código, um para a primeira análise e outro para revisá-la; as decisões finais foram minhas.

| Etapa | Ferramenta | O que foi feito |
| --- | --- | --- |
| Primeira análise | ChatGPT | Leitura do enunciado e do CSV, casos de borda e proposta inicial de abordagem |
| Segunda opinião | Claude | Revisão crítica da primeira análise e conferência contra o PDF |
| Verificação | Claude | Recálculo independente dos intervalos a partir do CSV fornecido |
| Pesquisa | Claude | Estado atual das ferramentas e das opções de hospedagem, com fontes |
| Consolidação | Claude | Briefing do projeto usado como entrada para os agentes de código |

O recálculo confirmou o resultado de referência: Joel Silver com 1 ano (1990 a 1991) e Matthew Vaughn com 13 anos (2002 a 2015). Ele também levantou os formatos de nome de produtor que viraram cenários de teste.

Os dois assistentes divergiram em um ponto principal: a primeira análise empilhava várias metodologias e uma arquitetura em quatro camadas, e a revisão apontou que isso excedia o tamanho do problema. Fiquei com uma ferramenta condutora, um princípio e um ciclo.

## Decisões de escopo

O enunciado é cumprido ao pé da letra no modo padrão, e tudo o que vai além dele é opcional e isolado.

| Decisão | Alternativa considerada | Motivo |
| --- | --- | --- |
| Entregar extras em camadas, além do exigido | Entregar só o mínimo | A vaga é full stack, e o ciclo completo mostra mais do perfil. O risco é contido por marcos entregáveis e pelo modo padrão intacto |
| Somente testes de integração no back-end | Incluir testes unitários | O item 4.2 pede somente testes de integração |
| Implementar também a listagem de filmes | Expor só o endpoint de intervalos | A seção 1 fala em leitura da lista de indicados e vencedores, embora a seção 3 exija só os intervalos |
| Manter o contrato de intervalos idêntico ao do PDF | Enriquecer a resposta com campos extras | A correção usará outros conjuntos de dados, provavelmente de forma automatizada |
| Comparar nomes de produtores exatamente como escritos | Normalizar grafias parecidas | Corrigir nomes exigiria adivinhar, e um erro mudaria o resultado |
| Todos os empatados entram em `min` e em `max` | Devolver só o primeiro | O formato do enunciado traz listas com mais de um item |
| Executar e testar só com `./mvnw` | Exigir Docker para subir | O item 4.3 proíbe depender de instalação externa |

## Decisões de arquitetura

A arquitetura tem dois modos de execução sobre o mesmo código, e cada tecnologia extra só é ativada no modo em que faz sentido.

| Decisão | Alternativa considerada | Motivo |
| --- | --- | --- |
| Java 25 | Java 27 | O 25 é o LTS vigente; o 27 tem suporte curto e menos chance de estar instalado na máquina do avaliador |
| Maven com wrapper | Gradle | É o mais comum em projetos Java e tem menos risco de incompatibilidade com o JDK |
| Modo padrão com H2 e modo Docker com PostgreSQL | Um único modo | O padrão cumpre o enunciado; o Docker mostra um ambiente próximo de produção |
| PostgreSQL como banco do modo Docker | MongoDB | H2 e PostgreSQL são relacionais, então o mesmo código de persistência e o mesmo esquema servem aos dois modos; os dados são tabulares, sem estrutura variável que peça um banco de documentos |
| Esquema versionado por Flyway | Tabelas geradas automaticamente | Garante o mesmo esquema nos dois bancos |
| Kafka só no modo Docker, como caminho único de ingestão | Não usar Kafka, ou usá-lo só para novos vencedores | Com banco persistente a ingestão por eventos tem uso real, e um caminho único evita duas formas de gravar |
| Kafka embutido do Spring nos testes | Testcontainers | Roda sem Docker, respeitando o item 4.3 |
| Autenticação parametrizada, desligada por padrão | Token obrigatório em toda requisição | O avaliador chamará o endpoint sem token; exigir um faria a correção falhar |
| Publicação da API no Cloud Run | AWS, Render e outras | A cota gratuita é mensal e não expira; os créditos da AWS para contas novas duram até seis meses |
| Publicar só a aplicação | Publicar o ambiente completo | Kafka, PostgreSQL e Grafana não cabem em plano gratuito e ficam no compose local |
| Front-end em Angular como último extra, em pasta própria | Sem front, ou front dentro do build da API | Mostra o lado full stack sem interferir no que o enunciado avalia |
| ETag no GET | Resposta sem cache | Os dados mudam pouco, e o cache condicional é parte do uso correto de HTTP |
| Sem imagem nativa, tracing distribuído e Kubernetes | Incluí-los | Há um único serviço e não há carga que os justifique |

## Decisões de processo e de uso de IA

O trabalho com os agentes segue Spec-Driven Development, com um ciclo curto por unidade e papéis separados entre quem implementa e quem revisa.

| Decisão | Alternativa considerada | Motivo |
| --- | --- | --- |
| GitHub Spec Kit como ferramenta condutora | Ferramenta oficial do AI-DLC, OpenSpec ou markdown sem ferramenta | Já trabalho com SDD, e é a ferramenta mais adotada, o que facilita a leitura por quem avalia |
| Spec no modo spec-anchored | Escrever a especificação inteira antes do código | A spec evolui a cada ciclo, o que evita o modelo cascata |
| Ciclo inspirado no AI-DLC, sem instalar a ferramenta dele | Usar as duas ferramentas juntas | Duas condutoras gerariam duas especificações e nenhuma fonte única da verdade |
| Claude Code implementa e Codex revisa cada pull request | Um único agente para tudo | Um revisor de outro fornecedor, com contexto limpo, não herda os vieses de quem escreveu |
| Codex também como testador adversário | Testes escritos só pelo implementador | O enunciado avisa que a correção usará outros conjuntos de dados |
| No máximo três agentes ao mesmo tempo | Paralelismo maior | O limite é a minha capacidade de ler e aprovar o que é produzido |
| Evidência em quatro frentes: mensagens, sessões, revisões e decisões | Só as transcrições | O registro de decisões mostra o julgamento, não apenas o uso |
| Planejamento registrado como síntese | Publicar as conversas brutas | As conversas são longas; a síntese deixa as decisões e os motivos à vista |
| Actions do pipeline fixadas por hash de commit | Fixar por tag | Tags podem ser reescritas, como nos ataques à cadeia de entrega de 2026 |
| Seis marcos com tag e ordem de corte definida | Uma entrega única no final | Com dez dias de prazo, cada marco precisa ser entregável sozinho |

## Onde decidi diferente da IA e onde ela mudou o plano

A IA foi usada como contraponto: em alguns pontos mantive a minha posição contra a recomendação dela, e em outros aceitei a correção.

| Ponto | Recomendação da IA | Minha decisão |
| --- | --- | --- |
| Tamanho do escopo | Entrega enxuta, com poucos extras | Ampliar, com extras isolados e marcos entregáveis |
| Kafka | Só descrever o desenho no README | Implementar no modo Docker |
| Front-end | Deixar de fora e usar o Swagger UI | Incluir em Angular, como último marco |
| Publicação em servidor | Parar na imagem publicada | Publicar a API em nuvem |
| Ordem dos marcos | Listagem de filmes só no fim | Antecipar para o marco 1, pela leitura da seção 1 do PDF |
| Segundo banco de dados | Não proposto | Modo Docker com PostgreSQL, ideia minha |
| Revisor externo | Revisor na mesma ferramenta | Codex como revisor e testador, ideia minha |

| Ponto | Minha ideia inicial | O que mudou após a análise |
| --- | --- | --- |
| Docker | Parte da execução | Passou a ser um modo opcional, por causa do item 4.3 |
| Testes unitários | Incluí-los no back-end | Ficaram restritos ao front-end, por causa do item 4.2 |
| Token de autenticação | Exigido nas requisições | Parametrizado e desligado por padrão |
| Ferramentas de metodologia | Spec Kit e a ferramenta do AI-DLC juntas | Uma só ferramenta condutora |
| Carga por Kafka | Consumo direto do arquivo | Acrescentado o sinal de carga concluída antes de a aplicação ficar pronta |

## Decisões ainda a confirmar

Estes pontos têm uma proposta registrada no briefing, mas só serão fechados na especificação.

| Ponto | Proposta atual |
| --- | --- |
| Linha inválida dentro de um CSV válido | Rejeitar a carga inteira; a alternativa é pular a linha e registrar em log |
| Mesmo produtor com duas vitórias no mesmo ano | Contar como intervalo 0 |
| Ordem dos itens empatados na resposta | Por `previousWin` crescente e depois por nome |
| Acesso a dados | JPA; a alternativa é JDBC |
| Datas de cada marco | Cronograma proposto no briefing, a ajustar às horas disponíveis |
| Versões de Spring Boot e Angular | As estáveis na data de criação do projeto |
