package com.outsera.awards.ingestion.kafka;

import com.outsera.awards.ingestion.RejectionReason;

/**
 * Motivo de rejeição de um evento do tópico de filmes (spec 003, FR-012 e FR-015; data-model.md).
 *
 * <p>Os motivos de conteúdo reaproveitam as descrições da linha do CSV ({@link RejectionReason}), porque
 * são as mesmas regras. Os de representação existem só no evento.
 */
public enum EventRejectionReason {
    NOT_UTF8("os bytes não são UTF-8 válido"),
    NOT_JSON("o conteúdo não é um objeto JSON"),
    TYPE_UNKNOWN("type desconhecido"),
    RUN_ID_MISSING("evento de carga concluída sem runId"),
    YEAR_MISSING(RejectionReason.YEAR_MISSING.message()),
    YEAR_NOT_NUMERIC("ano não é um inteiro JSON"),
    YEAR_OUT_OF_RANGE(RejectionReason.YEAR_OUT_OF_RANGE.message()),
    TITLE_MISSING(RejectionReason.TITLE_MISSING.message()),
    PRODUCERS_MISSING(RejectionReason.PRODUCERS_MISSING.message()),
    WINNER_INVALID("winner não é booleano"),
    FIELD_TOO_LONG(RejectionReason.FIELD_TOO_LONG.message());

    private final String message;

    EventRejectionReason(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
