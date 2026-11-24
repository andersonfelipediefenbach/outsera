package com.outsera.awards.docs;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ContractValidator;
import com.outsera.awards.support.ScenarioIT;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * As respostas reais de {@code GET /api/movies} cumprem o {@code openapi.yaml} (FR-025). O
 * {@code listing-sample.csv} tem um filme com estúdios vazios, então a página validada traz um
 * {@code studios} nulo.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/listing-sample.csv")
class MoviesContractIT extends ScenarioIT {

    private EntityExchangeResult<byte[]> get(String query) {
        return http.get().uri(MOVIES + query).exchange()
                .expectBody(byte[].class)
                .returnResult();
    }

    @Test
    @DisplayName("FR-025 respostas da listagem compatíveis com o contrato: 200")
    void okMatchesContract() {
        EntityExchangeResult<byte[]> result = get("?year=1981");
        assertThat(result.getStatus().value()).isEqualTo(200);
        ContractValidator.assertValidGet(MOVIES, result);
    }

    @Test
    @DisplayName("FR-025 respostas da listagem compatíveis com o contrato: 400")
    void badRequestMatchesContract() {
        EntityExchangeResult<byte[]> result = get("?winner=yes");
        assertThat(result.getStatus().value()).isEqualTo(400);
        ContractValidator.assertValidGet(MOVIES, result);
    }

    /** Garante que a validação não é vazia: um campo a mais num filme é recusado pelo contrato. */
    @Test
    @DisplayName("FR-025 o contrato recusa um campo a mais num filme")
    void contractRejectsExtraField() {
        EntityExchangeResult<byte[]> result = get("");
        String body = new String(result.getResponseBody(), StandardCharsets.UTF_8);
        String tampered = body.replaceFirst("\"title\":", "\"extra\":true,\"title\":");
        assertThat(tampered).isNotEqualTo(body);

        var report = ContractValidator.validateGet(MOVIES, result.getStatus().value(),
                result.getResponseHeaders(), tampered.getBytes(StandardCharsets.UTF_8));
        assertThat(report.hasErrors()).isTrue();
    }

    @Test
    @DisplayName("FR-007 resposta 200 da listagem traz o ETag declarado no contrato")
    void okDeclaresEtag() {
        EntityExchangeResult<byte[]> result = get("?winner=true");
        assertThat(result.getResponseHeaders().getETag()).isNotNull();
        ContractValidator.assertValidGet(MOVIES, result);
    }

    @Test
    @DisplayName("FR-007 resposta 304 da listagem compatível com o contrato")
    void notModifiedMatchesContract() {
        String etag = get("?winner=true").getResponseHeaders().getETag();
        EntityExchangeResult<byte[]> result = http.get().uri(MOVIES + "?winner=true")
                .header(HttpHeaders.IF_NONE_MATCH, etag)
                .exchange().expectBody(byte[].class).returnResult();
        assertThat(result.getStatus().value()).isEqualTo(304);
        ContractValidator.assertValidGet(MOVIES, result);
    }
}
