# Registro de decisões

Sugestões e decisões relevantes, aceitas ou rejeitadas, com o motivo (item 5.2 do enunciado). As
decisões de arquitetura ficam em ADRs, em `docs/adr/`.

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