#!/usr/bin/env bash
# Porta de severidade do CodeQL: achados de severidade alta ou crítica (security-severity >= 7,0) reprovam
# (spec 002, research R9; spec 003, contracts/ci-pipeline.md).
#
# Uso: .github/scripts/codeql-gate.sh <arquivo.sarif>...
#
# A porta lê o SARIF, e não o code scanning do GitHub: dispensar um alerta lá não muda o resultado. As
# exceções ficam em .github/codeql-exceptions.txt, uma por linha, no formato
# "<id da regra> <caminho> <título da entrada>", em que o título é o de uma entrada de docs/ai/decisoes.md
# que justifica a exceção. Uma exceção cujo título não existe no decisoes.md reprova. Os achados ignorados
# continuam no resumo, numa tabela própria, para não ficarem escondidos.
set -euo pipefail

(($# > 0)) || { echo "nenhum SARIF gerado" >&2; exit 1; }
root="$(git rev-parse --show-toplevel)"
exceptions_file="$root/.github/codeql-exceptions.txt"
decisions_file="$root/docs/ai/decisoes.md"
summary="${GITHUB_STEP_SUMMARY:-/dev/stdout}"

declare -A justified=()
failed=0
if [[ -f "$exceptions_file" ]]; then
    while IFS= read -r line || [[ -n "$line" ]]; do
        line="${line%$'\r'}"
        [[ -z "${line// /}" || "$line" == \#* ]] && continue
        read -r rule path title <<< "$line"
        if [[ -z "${rule:-}" || -z "${path:-}" || -z "${title:-}" ]]; then
            echo "exceção incompleta (regra, caminho e título): $line" >&2
            failed=1
            continue
        fi
        if ! grep -qF -- "— $title" <(grep '^## ' "$decisions_file"); then
            echo "exceção de $rule em $path cita uma entrada que não existe em docs/ai/decisoes.md: $title" >&2
            failed=1
            continue
        fi
        justified["$rule $path"]="$title"
    done < "$exceptions_file"
fi

findings="$(mktemp)"
trap 'rm -f "$findings"' EXIT
jq -r '
  .runs[] as $run
  | ([$run.tool.driver.rules[]?, $run.tool.extensions[]?.rules[]?]
     | map({key: .id, value: ((.properties["security-severity"] // "0") | tonumber)})
     | from_entries) as $severity
  | $run.results[]?
  | [($severity[.ruleId] // 0), .ruleId,
     (.locations[0].physicalLocation.artifactLocation.uri // "?"),
     ((.locations[0].physicalLocation.region.startLine // 0) | tostring)]
  | @tsv' "$@" > "$findings"

active=()
ignored=()
high=0
while IFS=$'\t' read -r severity rule path line; do
    row="| $severity | $rule | $path:$line |"
    if [[ -n "${justified["$rule $path"]:-}" ]]; then
        ignored+=("$row ${justified["$rule $path"]} |")
        continue
    fi
    active+=("$row")
    if awk -v s="$severity" 'BEGIN { exit !(s >= 7.0) }'; then
        high=$((high + 1))
    fi
done < "$findings"

{
    echo "### CodeQL"
    echo
    echo "Achados: $(( ${#active[@]} + ${#ignored[@]} )); ignorados por exceção: ${#ignored[@]}; de severidade alta ou crítica (≥ 7,0) entre os demais: $high."
    if ((${#active[@]} > 0)); then
        echo
        echo "| Severidade | Regra | Local |"
        echo "| --- | --- | --- |"
        printf '%s\n' "${active[@]}"
    fi
    if ((${#ignored[@]} > 0)); then
        echo
        echo "Ignorados pela porta (.github/codeql-exceptions.txt):"
        echo
        echo "| Severidade | Regra | Local | Justificativa em docs/ai/decisoes.md |"
        echo "| --- | --- | --- | --- |"
        printf '%s\n' "${ignored[@]}"
    fi
} >> "$summary"

((failed == 0 && high == 0))
