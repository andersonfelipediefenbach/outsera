package com.outsera.awards.ingestion.kafka;

import com.outsera.awards.ingestion.MovieFieldRules;
import com.outsera.awards.ingestion.MovieRow;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converte os bytes de um evento do tópico de filmes num filme, num evento de carga concluída ou numa
 * rejeição (spec 003, FR-010 a FR-012 e FR-015; research R7; data-model.md).
 *
 * <p>As regras de conteúdo são as da linha do CSV, pela mesma {@link MovieFieldRules}. O que muda é só a
 * representação: {@code year} é um inteiro JSON e {@code winner} um booleano JSON obrigatório. Campos
 * desconhecidos são ignorados.
 */
public final class MovieEventParser {

    /**
     * Com a detecção estrita, um objeto com uma propriedade repetida é {@code NOT_JSON}, em vez de valer a
     * última ocorrência. O Jackson 3 já reprova duas raízes no mesmo valor e não aceita BOM num texto
     * (spec 003, FR-012, Clarifications de 2026-11-25).
     */
    private static final JsonMapper JSON = JsonMapper.builder()
            .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
            .build();
    private static final int DETAIL_MAX = 60;

    private MovieEventParser() {
    }

    /** Resultado da leitura de um evento. */
    public sealed interface Parsed {
    }

    /** Evento de filme válido. */
    public record Movie(MovieRow row) implements Parsed {
    }

    /** Evento de carga concluída. */
    public record Completed(LoadCompletedEvent event) implements Parsed {
    }

    /** Evento rejeitado, com o primeiro motivo encontrado e um complemento opcional. */
    public record Rejected(EventRejectionReason reason, String detail) implements Parsed {

        /** {@code CÓDIGO: descrição (complemento)}, usado no cabeçalho do tópico de erros e no log. */
        public String describe() {
            return reason.name() + ": " + reason.message() + (detail == null ? "" : " (" + detail + ")");
        }
    }

    public static Parsed parse(byte[] value) {
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(value == null ? new byte[0] : value))
                    .toString();
        } catch (CharacterCodingException e) {
            return new Rejected(EventRejectionReason.NOT_UTF8, null);
        }
        JsonNode node;
        try {
            node = JSON.readTree(text);
        } catch (JacksonException e) {
            return new Rejected(EventRejectionReason.NOT_JSON, null);
        }
        if (node == null || !node.isObject()) {
            return new Rejected(EventRejectionReason.NOT_JSON, null);
        }
        JsonNode type = node.get("type");
        if (type == null || type.isNull() || (type.isString() && type.asString().equals("movie"))) {
            return movie(node);
        }
        if (type.isString() && type.asString().equals("load-completed")) {
            return completed(node);
        }
        return new Rejected(EventRejectionReason.TYPE_UNKNOWN, shorten(type.isString() ? type.asString() : type.toString()));
    }

    private static Parsed completed(JsonNode node) {
        JsonNode runId = node.get("runId");
        if (runId == null || !runId.isString() || runId.asString().isBlank()) {
            return new Rejected(EventRejectionReason.RUN_ID_MISSING, null);
        }
        // O count é validado, mas é informativo: o marcador vale pelo runId (spec 003, FR-013).
        JsonNode count = node.get("count");
        if (count == null || !count.isIntegralNumber() || !count.canConvertToLong() || count.longValue() < 0) {
            return new Rejected(EventRejectionReason.COUNT_INVALID, count == null ? "ausente" : shorten(count.toString()));
        }
        return new Completed(new LoadCompletedEvent(runId.asString(), count.longValue()));
    }

    private static Parsed movie(JsonNode node) {
        JsonNode year = node.get("year");
        if (year == null || year.isNull()) {
            return new Rejected(EventRejectionReason.YEAR_MISSING, null);
        }
        if (!year.isIntegralNumber()) {
            return new Rejected(EventRejectionReason.YEAR_NOT_NUMERIC, shorten(year.toString()));
        }
        if (!year.canConvertToLong() || !MovieFieldRules.isYearInRange(year.longValue())) {
            return new Rejected(EventRejectionReason.YEAR_OUT_OF_RANGE, shorten(year.toString()));
        }
        if (!isTextOrAbsent(node, "title")) {
            return new Rejected(EventRejectionReason.FIELD_TYPE_INVALID, "title");
        }
        String title = text(node, "title");
        if (!MovieFieldRules.hasTitle(title)) {
            return new Rejected(EventRejectionReason.TITLE_MISSING, null);
        }
        if (!isTextOrAbsent(node, "producers")) {
            return new Rejected(EventRejectionReason.FIELD_TYPE_INVALID, "producers");
        }
        String producers = text(node, "producers");
        List<String> producerNames = MovieFieldRules.producerNames(producers);
        if (producerNames.isEmpty()) {
            return new Rejected(EventRejectionReason.PRODUCERS_MISSING, null);
        }
        JsonNode winner = node.get("winner");
        if (winner == null || !winner.isBoolean()) {
            return new Rejected(EventRejectionReason.WINNER_INVALID,
                    winner == null ? "ausente" : shorten(winner.toString()));
        }
        if (!isTextOrAbsent(node, "studios")) {
            return new Rejected(EventRejectionReason.FIELD_TYPE_INVALID, "studios");
        }
        String studios = text(node, "studios");
        String tooLong = MovieFieldRules.tooLong(title, studios, producers, producerNames);
        if (tooLong != null) {
            return new Rejected(EventRejectionReason.FIELD_TOO_LONG, tooLong);
        }
        String nul = MovieFieldRules.nulCharacter(title, studios, producers);
        if (nul != null) {
            return new Rejected(EventRejectionReason.NUL_CHARACTER, nul);
        }
        return new Movie(new MovieRow(0, year.intValue(), title, studios == null || studios.isEmpty() ? null : studios,
                producers, producerNames, winner.booleanValue()));
    }

    /** O campo está ausente, é {@code null} (que vale ausente) ou é texto JSON. */
    private static boolean isTextOrAbsent(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || value.isString();
    }

    /**
     * O texto do campo sem os espaços em volta, pela mesma regra do CSV ({@link MovieFieldRules#strip}), ou
     * nulo se ausente ou {@code null}. O tipo já foi conferido por {@link #isTextOrAbsent}.
     */
    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || !value.isString() ? null : MovieFieldRules.strip(value.asString());
    }

    private static String shorten(String detail) {
        return detail.length() <= DETAIL_MAX ? detail : detail.substring(0, DETAIL_MAX) + "…";
    }
}
