package com.outsera.awards.ingestion;

/**
 * Motivo de rejeição de uma linha de dados, na ordem em que as regras são verificadas (data-model.md,
 * FR-006). Cada motivo tem uma mensagem legível, usada no aviso do log.
 */
public enum RejectionReason {
    COLUMN_COUNT("número de colunas diferente de 5"),
    YEAR_MISSING("ano vazio"),
    YEAR_NOT_NUMERIC("ano não numérico"),
    YEAR_OUT_OF_RANGE("ano fora da faixa de 1 a 9999"),
    TITLE_MISSING("título vazio"),
    PRODUCERS_MISSING("produtores vazios"),
    WINNER_INVALID("winner diferente de yes ou vazio"),
    /** Acrescentado pela spec 003 (spec 001, FR-006): a mesma regra vale para os eventos do Kafka. */
    FIELD_TOO_LONG("um campo excede o tamanho da coluna"),
    DUPLICATE("filme duplicado");

    private final String message;

    RejectionReason(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
