package com.outsera.awards.intervals;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;

/**
 * Formato exato da resposta de intervalos (FR-017, FR-018): só {@code min} e {@code max}, sempre listas,
 * e cada item só com os 4 campos do enunciado. Um cenário sem intervalos e outro com intervalos.
 */
class IntervalsShapeIT {

    private static final ParameterizedTypeReference<Map<String, List<Map<String, Object>>>> JSON_OBJECT =
            new ParameterizedTypeReference<>() { };

    abstract static class ShapeScenario extends ScenarioIT {

        private Map<String, List<Map<String, Object>>> body() {
            return http.get().uri(INTERVALS).exchange()
                    .expectStatus().isOk()
                    .expectBody(JSON_OBJECT)
                    .returnResult()
                    .getResponseBody();
        }

        @Test
        @DisplayName("FR-017 só os 4 campos por item")
        void onlyTheFourFields() {
            Map<String, List<Map<String, Object>>> body = body();
            assertThat(body).containsOnlyKeys("min", "max");
            for (String list : List.of("min", "max")) {
                assertThat(body.get(list)).allSatisfy(item -> assertThat(item)
                        .containsOnlyKeys("producer", "interval", "previousWin", "followingWin"));
            }
        }

        @Test
        @DisplayName("FR-018 listas vazias como [] e não null nem ausentes")
        void listsAreAlwaysPresent() {
            http.get().uri(INTERVALS).exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.min").isArray()
                    .jsonPath("$.max").isArray();
        }

        @Test
        @DisplayName("FR-017 Content-Type application/json")
        void contentTypeIsJson() {
            MediaType contentType = http.get().uri(INTERVALS).exchange()
                    .expectStatus().isOk()
                    .returnResult()
                    .getResponseHeaders()
                    .getContentType();
            assertThat(contentType).isNotNull();
            assertThat(contentType.equalsTypeAndSubtype(MediaType.APPLICATION_JSON))
                    .as("Content-Type %s", contentType)
                    .isTrue();
        }
    }

    @Nested
    @DisplayName("sem intervalos (no-repeat-winners.csv)")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/no-repeat-winners.csv")
    class NoIntervals extends ShapeScenario {

        @Test
        @DisplayName("FR-018 listas vazias como [] e não null nem ausentes: corpo exato")
        void emptyListsBody() {
            http.get().uri(INTERVALS).exchange()
                    .expectStatus().isOk()
                    .expectBody().json("{\"min\":[],\"max\":[]}", JsonCompareMode.STRICT);
        }
    }

    @Nested
    @DisplayName("com intervalos (disjoint-producers.csv)")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/disjoint-producers.csv")
    class WithIntervals extends ShapeScenario {

        @Test
        @DisplayName("FR-017 o cenário tem itens em min e max, para a checagem dos campos não ser vazia")
        void hasItems() {
            http.get().uri(INTERVALS).exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.min.length()").isEqualTo(1)
                    .jsonPath("$.max.length()").isEqualTo(1);
        }
    }
}
