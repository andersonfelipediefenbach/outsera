package com.outsera.awards.web;

import java.util.List;

/**
 * Um ou mais parâmetros da consulta são inválidos (FR-023). Vira um {@code ProblemDetail} 400 no
 * {@link ApiExceptionHandler}.
 */
public class InvalidParameterException extends RuntimeException {

    /** Nome do parâmetro e motivo da recusa, como aparecem em {@code invalidParams}. */
    public record InvalidParam(String name, String reason) {
    }

    private final List<InvalidParam> invalidParams;

    public InvalidParameterException(List<InvalidParam> invalidParams) {
        super("Parâmetros inválidos: " + invalidParams);
        if (invalidParams.isEmpty()) {
            throw new IllegalArgumentException("ao menos um parâmetro inválido é necessário");
        }
        this.invalidParams = List.copyOf(invalidParams);
    }

    public List<InvalidParam> getInvalidParams() {
        return invalidParams;
    }
}
