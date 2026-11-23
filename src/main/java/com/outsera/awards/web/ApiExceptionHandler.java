package com.outsera.awards.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte os erros da API em Problem Details (RFC 9457). */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Parâmetro inválido na consulta: 400 com {@code invalidParams} = {@code [{name, reason}]} (FR-023). */
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ProblemDetail> invalidParameter(InvalidParameterException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Parâmetro inválido na consulta");
        problem.setTitle(HttpStatus.BAD_REQUEST.getReasonPhrase());
        problem.setProperty("invalidParams", e.getInvalidParams());
        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }
}
