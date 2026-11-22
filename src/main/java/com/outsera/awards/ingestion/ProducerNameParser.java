package com.outsera.awards.ingestion;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Separa a coluna de produtores em nomes (FR-007 a FR-010, research R4).
 *
 * <p>Os separadores são a vírgula, a palavra {@code and} e a combinação {@code , and}. O {@code and} só
 * separa como palavra inteira: antes dele vem vírgula, espaço ou o início do campo, e depois dele vem
 * espaço ou o fim do campo. Assim, {@code Robbinsand}, {@code Anderson} e {@code andrew} continuam
 * inteiros.
 */
public final class ProducerNameParser {

    private static final Pattern SEPARATOR =
            Pattern.compile("\\s*,\\s*(?:and(?=\\s|$)\\s*)?|(?:^|\\s+)and(?=\\s|$)\\s*");

    private ProducerNameParser() {
    }

    /**
     * Os nomes, sem espaços em volta, sem vazios e sem repetidos, na ordem em que aparecem. A lista vem
     * vazia quando nenhum nome resulta.
     */
    public static List<String> parse(String producers) {
        Set<String> names = new LinkedHashSet<>();
        for (String part : SEPARATOR.split(producers, -1)) {
            String name = part.trim();
            if (!name.isEmpty()) {
                names.add(name);
            }
        }
        return List.copyOf(names);
    }
}
