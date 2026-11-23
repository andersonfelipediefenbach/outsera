package com.outsera.awards.ingestion;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Separa a coluna de produtores em nomes (FR-007 a FR-010, research R4), em etapas:
 *
 * <ol>
 *   <li>divide o campo pelas vírgulas;</li>
 *   <li>em cada parte, lê as palavras (sequências sem espaço). A palavra {@code and}, sem diferenciar
 *       maiúsculas ({@code AND} e {@code aNd} também), é separador. Por ser reconhecida pela palavra, e não pelos espaços em volta, ela é
 *       separador também no início e no fim da parte e quando se repete ({@code and and});</li>
 *   <li>o nome é o trecho original da primeira à última palavra do grupo, então o texto interno é
 *       preservado como está (FR-008);</li>
 *   <li>descarta as partes vazias e os nomes repetidos no mesmo filme (FR-010).</li>
 * </ol>
 *
 * <p>Um {@code and} dentro de uma palavra, como em {@code Robbinsand}, {@code Anderson} ou
 * {@code andrew}, não separa nomes (FR-009).
 */
public final class ProducerNameParser {

    private static final String AND = "and";
    private static final Pattern WORD = Pattern.compile("\\S+");

    private ProducerNameParser() {
    }

    /**
     * Os nomes, sem espaços em volta, sem vazios e sem repetidos, na ordem em que aparecem. A lista vem
     * vazia quando nenhum nome resulta.
     */
    public static List<String> parse(String producers) {
        Set<String> names = new LinkedHashSet<>();
        for (String part : producers.split(",", -1)) {
            splitOnAnd(part, names);
        }
        return List.copyOf(names);
    }

    /** Separa uma parte (já sem vírgulas) pelas palavras {@code and} e acrescenta os nomes. */
    private static void splitOnAnd(String part, Set<String> names) {
        Matcher word = WORD.matcher(part);
        int nameStart = -1;
        int nameEnd = -1;
        while (word.find()) {
            if (word.group().equalsIgnoreCase(AND)) {
                addName(part, nameStart, nameEnd, names);
                nameStart = -1;
            } else {
                if (nameStart < 0) {
                    nameStart = word.start();
                }
                nameEnd = word.end();
            }
        }
        addName(part, nameStart, nameEnd, names);
    }

    private static void addName(String part, int start, int end, Set<String> names) {
        if (start >= 0) {
            names.add(part.substring(start, end));
        }
    }
}
