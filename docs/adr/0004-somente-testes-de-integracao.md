# 0004. Somente testes de integração no back-end, sem Mockito

- **Status:** Aceito
- **Data:** 2026-11-19
- **Origem:** [`docs/ai/decisoes.md`](../ai/decisoes.md), entradas "Emenda 1.2.0 da constituição:
  Princípio I reescrito" e "Mockito excluído do `spring-boot-starter-test`"; Princípio I da constituição.

## Contexto

O item 4.2 do enunciado pede testes de integração. A constituição fez disso um princípio inegociável: no
back-end, nada de testes unitários nem de mocks. Era preciso definir como testar comportamentos que ainda
não têm endpoint (a carga do CSV) e falhas de inicialização, em que o contexto nem sobe.

## Decisão

- Todo teste é um `*IT`, executado pelo Failsafe no `./mvnw verify`, com `@SpringBootTest` e o contexto
  completo da aplicação, sem dublês.
- O resultado é verificado preferencialmente por HTTP. Log ou banco só valem enquanto o comportamento não
  tem endpoint.
- As falhas de inicialização rodam a aplicação real com `SpringApplication` e verificam a exceção e o
  log.
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
