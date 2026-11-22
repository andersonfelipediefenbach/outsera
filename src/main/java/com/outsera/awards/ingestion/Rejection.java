package com.outsera.awards.ingestion;

/**
 * Uma linha de dados rejeitada (FR-006a).
 *
 * @param lineNumber linha física no arquivo (o cabeçalho é a linha 1)
 * @param reason     o primeiro motivo encontrado
 * @param detail     complemento do motivo (por exemplo, o valor encontrado), ou nulo
 */
public record Rejection(long lineNumber, RejectionReason reason, String detail) {

    /** Texto legível do motivo, por exemplo {@code ano não numérico (19x0)}. */
    public String describe() {
        return detail == null ? reason.message() : reason.message() + " (" + detail + ")";
    }
}
