package com.outsera.awards.support;

import static org.assertj.core.api.Assertions.assertThat;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.SimpleValidationReportFormat;
import com.atlassian.oai.validator.report.ValidationReport;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Valida respostas reais contra o contrato publicado em {@code classpath:static/openapi.yaml}
 * (FR-025, research R8). O contrato é a única fonte: este validador não conhece as classes de
 * produção, só o arquivo.
 *
 * <p>Verifica o status (que precisa estar declarado na operação), os cabeçalhos declarados (como o
 * {@code ETag} obrigatório das respostas 200 e 304, FR-007), o {@code Content-Type} e o corpo, contra o
 * schema, inclusive {@code additionalProperties: false}. Uma resposta sem corpo (304) é validada sem
 * corpo.
 */
public final class ContractValidator {

    private static final String CONTRACT = "static/openapi.yaml";

    private static final OpenApiInteractionValidator VALIDATOR =
            OpenApiInteractionValidator.createForInlineApiSpecification(readContract()).build();

    private ContractValidator() {
    }

    /** Falha o teste se a resposta de {@code GET path} não cumpre o contrato. */
    public static void assertValidGet(String path, EntityExchangeResult<byte[]> result) {
        ValidationReport report = validateGet(path, result.getStatus().value(), result.getResponseHeaders(),
                result.getResponseBody());
        assertThat(report.hasErrors())
                .as("resposta de GET %s fora do contrato:%n%s", path,
                        SimpleValidationReportFormat.getInstance().apply(report))
                .isFalse();
    }

    /**
     * Relatório de validação de uma resposta de {@code GET path}, montada a partir das partes. Todos os
     * cabeçalhos recebidos são repassados ao validador.
     */
    public static ValidationReport validateGet(String path, int status, HttpHeaders headers, byte[] body) {
        SimpleResponse.Builder response = SimpleResponse.Builder.status(status);
        headers.forEach((name, values) -> response.withHeader(name, values));
        if (body != null && body.length > 0) {
            response.withBody(body);
        }
        return VALIDATOR.validateResponse(path, Request.Method.GET, response.build());
    }

    private static String readContract() {
        try (InputStream in = new ClassPathResource(CONTRACT).getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("contrato não encontrado em classpath:" + CONTRACT, e);
        }
    }
}
