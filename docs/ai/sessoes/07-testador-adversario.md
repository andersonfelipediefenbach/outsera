# Sessão 07: testador adversário (Codex)

> Transcrição, não exportação. A conversa foi feita no app do Codex, que não gerou um arquivo de
> exportação. Este registro reúne o prompt enviado, na íntegra, e a resposta final do Codex. O
> trabalho produzido por ele está completo, sem edição, em [`docs/ai/adversario/`](../adversario/).

## Ambiente

- **Ferramenta:** Codex, no papel de testador adversário.
- **Isolamento:** a sessão rodou na pasta `outsera-test/adversario`, fora do repositório, contendo só
  três arquivos: `enunciado.md` (transcrição do PDF do enunciado), `spec.md` e `openapi.yaml`
  (cópias da spec e do contrato do marco 1). O Codex não teve acesso ao código.
- **Data:** 2026-11-22.

## Prompt enviado

```text
Você é um testador adversário de uma API. Nesta pasta há três arquivos: enunciado.md (o enunciado do teste técnico), spec.md (a especificação da implementação) e openapi.yaml (o contrato da API). Use somente esses arquivos. Não procure nem leia nada fora desta pasta; você não deve ter acesso ao código.

Objetivo: criar arquivos CSV que tentem fazer a implementação devolver um resultado errado no endpoint de intervalos de produtores, sem violar o que a spec define como entrada válida ou inválida.

Crie uma pasta casos/ com, para cada caso:
- NN-nome.csv: o arquivo completo, no formato definido pela spec;
- NN-nome.expected.json: a resposta JSON exata que a spec exige para esse arquivo, no formato do contrato.

Crie também casos/README.md com uma tabela: número, nome, regra da spec que o caso testa (cite a FR) e por que ele pode quebrar uma implementação ingênua.

Priorize o que um implementador costuma esquecer, combinando situações: empates múltiplos no mínimo e no máximo ao mesmo tempo, o mesmo produtor aparecendo em min e em max, produtores com muitas vitórias, intervalo 0, nomes parecidos ou contidos em outros, separadores incomuns, maiúsculas em "and", anos extremos, linhas fora de ordem, linhas inválidas e duplicadas misturadas a vencedores. Não repita casos óbvios. Entre 10 e 15 casos.

Se a spec for ambígua para algum caso, não invente: registre a ambiguidade no README.md e não crie o expected.json desse caso.
```

## Resposta final do Codex

Trabalhou por 2 min 28 s.

> Criei 13 CSVs, 11 arquivos `expected.json` e um `README.md` com as regras testadas em
> `D:\Repo\PESSOAIS\outsera-test\adversario\casos\`.
>
> Conferi os 11 resultados esperados com um cálculo independente, incluindo a ordem e a
> multiplicidade dos itens em `min` e `max`. Os casos 12 e 13 ficaram sem JSON esperado porque a
> spec deixa em aberto a distinção de maiúsculas no separador `and` e a ordenação de nomes com
> acentos ou diferenças de caixa. A verificação usou apenas os três arquivos indicados, sem
> executar a API.

## Resultado

Os 13 casos viraram testes de integração na sessão
[`08-adversario-testes-e-correcoes.md`](08-adversario-testes-e-correcoes.md). Na primeira execução,
11 passaram sem nenhuma correção. Os casos 09 (anos extremos) e 12 (`AND` em maiúsculas) falharam e
levaram às decisões registradas em [`docs/ai/decisoes.md`](../decisoes.md).
