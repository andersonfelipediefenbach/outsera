#!/usr/bin/env bash
# Confere que os testes de integração dos marcos anteriores não mudaram desde a tag dada (spec 003,
# SC-002; research R14; contracts/ci-pipeline.md).
#
# Uso: .github/scripts/check-unchanged-tests.sh <tag>
#
# Cada src/test/java/**/*IT.java que existe na tag precisa estar idêntico no HEAD. As exceções ficam em
# .github/unchanged-tests-exceptions.txt, uma por linha, no formato "<caminho> <título da entrada>", em
# que o título é o de uma entrada de docs/ai/decisoes.md que autoriza a mudança. Uma exceção cujo título
# não existe no decisoes.md reprova.
set -euo pipefail

tag="${1:?uso: check-unchanged-tests.sh <tag>}"
root="$(git rev-parse --show-toplevel)"
exceptions_file="$root/.github/unchanged-tests-exceptions.txt"
decisions_file="$root/docs/ai/decisoes.md"

if ! git rev-parse --verify --quiet "refs/tags/$tag" >/dev/null; then
    echo "check-unchanged-tests.sh: a tag $tag não existe (o checkout precisa das tags: fetch-depth 0)." >&2
    exit 2
fi

declare -A allowed=()
failed=0
if [[ -f "$exceptions_file" ]]; then
    while IFS= read -r line || [[ -n "$line" ]]; do
        [[ -z "${line// /}" || "$line" == \#* ]] && continue
        path="${line%% *}"
        title="${line#* }"
        if [[ "$title" == "$line" || -z "$title" ]]; then
            echo "exceção sem título: $line" >&2
            failed=1
            continue
        fi
        if ! grep -qF -- "— $title" <(grep '^## ' "$decisions_file"); then
            echo "exceção de $path cita uma entrada que não existe em docs/ai/decisoes.md: $title" >&2
            failed=1
            continue
        fi
        allowed["$path"]=1
    done < "$exceptions_file"
fi

changed=()
while IFS= read -r file; do
    [[ -n "${allowed[$file]:-}" ]] && continue
    if [[ ! -f "$root/$file" ]]; then
        changed+=("$file (apagado)")
    elif ! git diff --quiet "$tag" -- "$file"; then
        changed+=("$file")
    fi
done < <(git ls-tree -r --name-only "$tag" -- src/test/java | grep -E 'IT\.java$')

if ((${#changed[@]} > 0)); then
    echo "Testes de integração alterados desde $tag sem exceção registrada:" >&2
    printf '  %s\n' "${changed[@]}" >&2
    failed=1
fi

if ((failed)); then
    exit 1
fi
count="$(git ls-tree -r --name-only "$tag" -- src/test/java | grep -cE 'IT\.java$')"
echo "check-unchanged-tests.sh: os $count testes de integração de $tag estão sem alteração (exceções: ${#allowed[@]})."
