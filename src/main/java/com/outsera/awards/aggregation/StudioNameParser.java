package com.outsera.awards.aggregation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Separa o campo de estúdios de um filme (spec 006, FR-002; data-model, "Regras de separação dos estúdios"):
 * por vírgula, cada parte sem espaços nas pontas, sem partes vazias e com cada estúdio uma vez por filme. Não
 * separa por {@code and}, porque o arquivo fornecido não usa esse separador nos estúdios, e mantém maiúsculas e
 * acentos como escritos.
 */
final class StudioNameParser {

    private StudioNameParser() {
    }

    /** Os estúdios do campo, na ordem em que aparecem; vazio para um campo nulo (spec 001, FR-006e). */
    static Set<String> parse(String studios) {
        if (studios == null) {
            return Collections.emptySet();
        }
        Set<String> names = new LinkedHashSet<>();
        for (String part : studios.split(",")) {
            String name = part.strip();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return names;
    }
}
