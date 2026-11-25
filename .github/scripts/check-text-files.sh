#!/usr/bin/env bash
# Reprova qualquer byte NUL em arquivo de texto versionado (spec 003, contracts/ci-pipeline.md).
#
# Um byte NUL faz o grep e o git tratarem o arquivo como binário. Foi assim que o check-unchanged-tests.sh
# deixou de achar uma entrada do docs/ai/decisoes.md no PR do Marco 3 (registro no decisoes.md).
#
# É texto todo arquivo versionado cujo atributo `text` não está desligado no .gitattributes. Um arquivo
# com NUL de propósito, como um cenário de teste, precisa ser declarado `binary` lá. Os arquivos
# declarados assim aparecem na saída, para não ficarem escondidos.
set -euo pipefail

root="$(git rev-parse --show-toplevel)"
cd "$root"

bad=()
binary=()
checked=0
while IFS= read -r -d '' path && IFS= read -r -d '' _attribute && IFS= read -r -d '' value; do
    [[ -f "$path" ]] || continue
    if [[ "$value" == "unset" ]]; then
        binary+=("$path")
        continue
    fi
    checked=$((checked + 1))
    if ! tr -d '\000' < "$path" | cmp -s - "$path"; then
        bad+=("$path")
    fi
done < <(git ls-files -z | git check-attr -z --stdin text)

if ((${#binary[@]} > 0)); then
    echo "Declarados binários no .gitattributes (fora da verificação):"
    printf '  %s\n' "${binary[@]}"
fi
if ((${#bad[@]} > 0)); then
    echo "Arquivos de texto versionados com byte NUL:" >&2
    printf '  %s\n' "${bad[@]}" >&2
    echo "Troque o byte por um escape em texto (por exemplo, \u0000 entre crases) ou declare o arquivo binary no .gitattributes." >&2
    exit 1
fi
echo "check-text-files.sh: $checked arquivos de texto versionados sem byte NUL."
