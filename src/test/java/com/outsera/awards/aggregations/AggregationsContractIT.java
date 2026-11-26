package com.outsera.awards.aggregations;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ContractValidator;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * As respostas reais das duas agregações cumprem o contrato 1.3.0 publicado em {@code /openapi.yaml} (spec 006,
 * FR-004), com o arquivo fornecido. O caso das listas vazias é validado no {@link AggregationsEmptyIT}, com o
 * mesmo método.
 */
class AggregationsContractIT extends ScenarioIT {

    /** {@code GET path} com 200 e a resposta (status, cabeçalhos e corpo) válida no contrato. */
    static void assertOkMatchesContract(RestTestClient http, String path) {
        EntityExchangeResult<byte[]> result = http.get().uri(path).exchange()
                .expectBody(byte[].class).returnResult();
        assertThat(result.getStatus().value()).as("GET %s", path).isEqualTo(200);
        ContractValidator.assertValidGet(path, result);
    }

    @ParameterizedTest(name = "S006 FR-004 {0} com o arquivo fornecido cumpre o contrato 1.3.0")
    @ValueSource(strings = {AggregationsReferenceIT.YEARS, AggregationsReferenceIT.STUDIOS})
    void withDataMatchesContract(String path) {
        assertOkMatchesContract(http, path);
    }
}
