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
    /** {@code count} ausente ou que não é inteiro JSON ≥ 0 no marcador (spec 003, FR-013, 2026-11-25). */
    COUNT_INVALID("evento de carga concluída sem count inteiro maior ou igual a zero"),
    /** {@code title}, {@code producers} ou {@code studios} presente com outro tipo que texto (2026-11-25). */
    FIELD_TYPE_INVALID("campo de texto com outro tipo JSON"),
    YEAR_MISSING(RejectionReason.YEAR_MISSING.message()),
    YEAR_NOT_NUMERIC("ano não é um inteiro JSON"),
    YEAR_OUT_OF_RANGE(RejectionReason.YEAR_OUT_OF_RANGE.message()),
    TITLE_MISSING(RejectionReason.TITLE_MISSING.message()),
    PRODUCERS_MISSING(RejectionReason.PRODUCERS_MISSING.message()),
    WINNER_INVALID("winner não é booleano"),
    FIELD_TOO_LONG(RejectionReason.FIELD_TOO_LONG.message()),
    NUL_CHARACTER(RejectionReason.NUL_CHARACTER.message()),
    /**
     * O banco recusou a gravação por dado inválido (SQLState das classes 22 e 23). É permanente: o evento vai
     * ao tópico de erros, sem nova tentativa (spec 003, FR-015a; research R9).
     */
    DATABASE_REJECTED("o banco recusou o dado");

    private final String message;

    EventRejectionReason(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
