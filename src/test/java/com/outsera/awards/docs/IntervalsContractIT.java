package com.outsera.awards.docs;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ContractValidator;
import com.outsera.awards.support.ScenarioIT;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/** As respostas reais de {@code GET /api/producers/intervals} cumprem o {@code openapi.yaml} (FR-025). */
class IntervalsContractIT {

    abstract static class ContractScenario extends ScenarioIT {

        EntityExchangeResult<byte[]> get() {
            return http.get().uri(INTERVALS).exchange()
                    .expectBody(byte[].class)
                    .returnResult();
        }

        @Test
        @DisplayName("FR-025 resposta de intervalos compatível com o contrato")
        void matchesContract() {
            ContractValidator.assertValidGet(INTERVALS, get());
        }
    }

    @Nested
    @DisplayName("arquivo padrão")
    class DefaultFile extends ContractScenario {

        /**
         * Garante que a validação não é vazia: a mesma resposta, com um campo a mais num item, é
         * recusada pelo contrato ({@code additionalProperties: false}).
         */
        @Test
        @DisplayName("FR-025 o contrato recusa um campo a mais na resposta")
        void contractRejectsExtraField() {
            EntityExchangeResult<byte[]> result = get();
            String body = new String(result.getResponseBody(), StandardCharsets.UTF_8);
            String tampered = body.replaceFirst("\"producer\":", "\"extra\":true,\"producer\":");
            assertThat(tampered).isNotEqualTo(body);

            var report = ContractValidator.validateGet(INTERVALS, result.getStatus().value(),
                    result.getResponseHeaders(), tampered.getBytes(StandardCharsets.UTF_8));
            assertThat(report.hasErrors()).isTrue();
        }

        @Test
        @DisplayName("FR-007 resposta 200 traz o ETag declarado no contrato")
        void okDeclaresEtag() {
            EntityExchangeResult<byte[]> result = get();
            assertThat(result.getResponseHeaders().getETag()).isNotNull();
            ContractValidator.assertValidGet(INTERVALS, result);
        }

        @Test
        @DisplayName("FR-007 resposta 304 compatível com o contrato")
        void notModifiedMatchesContract() {
            String etag = get().getResponseHeaders().getETag();
            EntityExchangeResult<byte[]> result = http.get().uri(INTERVALS)
                    .header(HttpHeaders.IF_NONE_MATCH, etag)
                    .exchange().expectBody(byte[].class).returnResult();
            assertThat(result.getStatus().value()).isEqualTo(304);
            ContractValidator.assertValidGet(INTERVALS, result);
        }

        /** Garante que o cabeçalho é de fato verificado: uma resposta 200 sem ETag é recusada. */
        @Test
        @DisplayName("FR-007 o contrato recusa uma resposta 200 sem ETag")
        void contractRejectsMissingEtag() {
            EntityExchangeResult<byte[]> result = get();
            HttpHeaders withoutEtag = new HttpHeaders();
            withoutEtag.putAll(result.getResponseHeaders());
            withoutEtag.remove(HttpHeaders.ETAG);

            var report = ContractValidator.validateGet(INTERVALS, result.getStatus().value(), withoutEtag,
                    result.getResponseBody());
            assertThat(report.hasErrors()).isTrue();
        }
    }

    @Nested
    @DisplayName("nenhum produtor com duas vitórias")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/no-repeat-winners.csv")
    class NoRepeatWinners extends ContractScenario {
    }
}
