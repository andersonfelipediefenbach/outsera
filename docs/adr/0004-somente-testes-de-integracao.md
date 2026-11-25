# 0004. Somente testes de integração no back-end, sem Mockito

- **Status:** Aceito
- **Data:** 2026-11-19
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Emenda 1.2.0 da constituição:
  Princípio I reescrito", "Mockito excluído do `spring-boot-starter-test`", "Emenda 1.5.0 da constituição:
  `SpringApplication` para controlar o ciclo de vida" e "Emenda 1.7.0 da constituição: comprovação de
  comportamento que depende de infraestrutura"; Princípios I e VI da constituição.
- **Atualizado:** 2026-11-24, com as emendas 1.5.0 e 1.7.0.

## Contexto

O item 4.2 do enunciado pede testes de integração. A constituição fez disso um princípio inegociável: no
back-end, nada de testes unitários nem de mocks. Era preciso definir como testar comportamentos que ainda
não têm endpoint (a carga do CSV) e falhas de inicialização, em que o contexto nem sobe.

## Decisão

- Todo teste é um `*IT`, executado pelo Failsafe no `./mvnw verify`, com `@SpringBootTest` e o contexto
  completo da aplicação, sem dublês.
- O resultado é verificado preferencialmente por HTTP. Log ou banco só valem enquanto o comportamento não
  tem endpoint.
- Os testes que precisam controlar o ciclo de vida da aplicação real a rodam com `SpringApplication`
  (emenda 1.5.0). São quatro casos, sempre sem mocks e com verificação por HTTP real sempre que o HTTP
  observa o comportamento:
  - falha de inicialização: verificam a exceção e o log;
  - múltiplas execuções: comparam duas ou mais subidas, como o ETag entre execuções e as equivalências do
    Marco 3;
  - ordem da subida: observam os eventos reais do ciclo de vida;
  - saída completa da subida: capturam o log desde a primeira linha.
- O comportamento que depende de infraestrutura indisponível no modo padrão, como o PostgreSQL, é testado
  num perfil Maven dedicado, executado obrigatoriamente no pipeline (emenda 1.7.0; ADR 0019). O Kafka tem
  broker embutido e fica no `./mvnw verify`.
- O Mockito é excluído do `spring-boot-starter-test` no `pom.xml`: usar mock vira erro de compilação.

## Alternativas consideradas

- Manter o Mockito e só proibi-lo por convenção: a ferramenta proibida continuaria disponível.
- Um `@SpringBootTest` externo que sobe a aplicação com falha por dentro: o contexto externo não teria
  função.
- Exceções ao princípio registradas em cada spec: a spec não pode abrir exceção a um princípio.

## Consequências

- Cada requisito de comportamento é provado pela aplicação real, e o mesmo teste serve de documentação do
  contrato.
- Os testes que leem o log da inicialização precisam de um contexto próprio, para não depender da ordem
  das classes (`@DirtiesContext`).
- A partir do Marco 2, documentação, pipeline e imagem têm outras formas de comprovação, pelo Princípio VI
  (constituição 1.4.0).
- A partir do Marco 3, os `*PostgresIT` rodam fora do `./mvnw verify`, no perfil `postgres-it` (ADR 0019).
  Uma decisão de design sem comportamento verificável por teste confiável, como a comparação do token em
  tempo constante (ADR 0017), fica fora da spec e é comprovada por revisão.
