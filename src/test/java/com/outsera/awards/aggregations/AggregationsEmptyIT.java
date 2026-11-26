package com.outsera.awards.aggregations;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;

/**
 * Sem nenhum vencedor, as duas agregações devolvem listas vazias com 200 (spec 006, FR-003; US4-4), com o
 * {@code aggregations/no-winners.csv} (três indicados). Respostas esperadas: {@code {"years":[]}} e
 * {@code {"studios":[]}}.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/aggregations/no-winners.csv")
class AggregationsEmptyIT extends ScenarioIT {

    @Test
    @DisplayName("S006 FR-003 US4-4 sem vencedores, a lista de anos vem vazia com 200")
    void noYears() {
        http.get().uri(AggregationsReferenceIT.YEARS).exchange()
                .expectStatus().isOk()
                .expectBody().json("{\"years\":[]}", JsonCompareMode.STRICT);
    }

    @Test
    @DisplayName("S006 FR-003 US4-4 sem vencedores, a lista de estúdios vem vazia com 200")
    void noStudios() {
        http.get().uri(AggregationsReferenceIT.STUDIOS).exchange()
                .expectStatus().isOk()
                .expectBody().json("{\"studios\":[]}", JsonCompareMode.STRICT);
    }

    @ParameterizedTest(name = "S006 FR-004 {0} com a lista vazia cumpre o contrato 1.3.0")
    @ValueSource(strings = {AggregationsReferenceIT.YEARS, AggregationsReferenceIT.STUDIOS})
    void emptyMatchesContract(String path) {
        AggregationsContractIT.assertOkMatchesContract(http, path);
    }
}
