package com.outsera.awards.ingestion;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Regras de conteúdo de um filme, comuns à linha do CSV e ao evento do Kafka (spec 001, FR-006; spec 003,
 * FR-012 e FR-018; research R7). Cada regra existe só aqui, para que a carga direta e a ingestão por
 * Kafka rejeitem exatamente as mesmas linhas.
 */
public final class MovieFieldRules {

    /** Faixa válida de ano (spec 001, FR-006, sessão 2026-11-22). */
    public static final int MIN_YEAR = 1;
    public static final int MAX_YEAR = 9999;

    /** Tamanhos máximos, iguais às colunas da migração V1 ({@code VARCHAR(500)} e {@code VARCHAR(1000)}). */
    public static final int TITLE_MAX = 500;
    public static final int STUDIOS_MAX = 1000;
    public static final int PRODUCERS_MAX = 1000;
    public static final int PRODUCER_NAME_MAX = 500;

    private static final Pattern LEADING_ZEROS = Pattern.compile("^0+");

    private MovieFieldRules() {
    }

    /**
     * Se o ano, já só com dígitos, está na faixa. Os zeros à esquerda são descartados antes, e o tamanho é
     * conferido antes da conversão, para que uma sequência longa de dígitos não estoure o {@code int}.
     */
    public static boolean isYearInRange(String digits) {
        String significant = LEADING_ZEROS.matcher(digits).replaceFirst("");
        if (significant.length() > String.valueOf(MAX_YEAR).length()) {
            return false;
        }
        int value = significant.isEmpty() ? 0 : Integer.parseInt(significant);
        return isYearInRange(value);
    }

    public static boolean isYearInRange(long year) {
        return year >= MIN_YEAR && year <= MAX_YEAR;
    }

    /** O título, já sem os espaços em volta, não é vazio. */
    public static boolean hasTitle(String title) {
        return title != null && !title.isEmpty();
    }

    /** Nomes dos produtores pelas regras de separação (spec 001, FR-007 a FR-010). Vazio é inválido. */
    public static List<String> producerNames(String producers) {
        return producers == null ? List.of() : ProducerNameParser.parse(producers);
    }

    /**
     * O detalhe do primeiro campo maior que a coluna do esquema, ou nulo se todos cabem (motivo
     * {@code FIELD_TOO_LONG}). O tamanho é contado em caracteres, como as colunas {@code VARCHAR}.
     */
    public static String tooLong(String title, String studios, String producers, List<String> producerNames) {
        String detail = exceeds("título", title, TITLE_MAX);
        if (detail == null) {
            detail = exceeds("estúdios", studios, STUDIOS_MAX);
        }
        if (detail == null) {
            detail = exceeds("produtores", producers, PRODUCERS_MAX);
        }
        for (int i = 0; detail == null && i < producerNames.size(); i++) {
            detail = exceeds("nome de produtor", producerNames.get(i), PRODUCER_NAME_MAX);
        }
        return detail;
    }

    /**
     * O nome do primeiro campo de texto com o caractere nulo (U+0000), ou nulo se nenhum tem (motivo
     * {@code NUL_CHARACTER}). O PostgreSQL não aceita esse caractere em texto, e sem esta regra a recusa do
     * banco só apareceria na gravação (apontamento do Codex no PR do Marco 3).
     */
    public static String nulCharacter(String title, String studios, String producers) {
        if (hasNul(title)) {
            return "título";
        }
        if (hasNul(studios)) {
            return "estúdios";
        }
        return hasNul(producers) ? "produtores" : null;
    }

    private static boolean hasNul(String value) {
        return value != null && value.indexOf(0) >= 0; // 0 é o caractere nulo (U+0000)
    }

    private static String exceeds(String field, String value, int max) {
        if (value == null) {
            return null;
        }
        int length = value.codePointCount(0, value.length());
        return length > max ? field + " com " + length + " caracteres; máximo " + max : null;
    }
}
