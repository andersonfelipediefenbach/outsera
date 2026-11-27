package com.outsera.awards.aggregations;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;

/**
 * Anos com mais de um vencedor (spec 006, FR-001), com o {@code aggregations/years.csv}, em linhas fora de ordem:
 *
 * <ul>
 *   <li>2001: 1 vencedor e 2 indicados (fica de fora);</li>
 *   <li>2002: 2 vencedores;</li>
 *   <li>2003: 3 vencedores;</li>
 *   <li>2004: só indicados (fica de fora).</li>
 * </ul>
 *
 * Resposta esperada: {@code {"years":[{"year":2002,"winnerCount":2},{"year":2003,"winnerCount":3}]}}.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/aggregations/years.csv")
class YearsWithMultipleWinnersIT extends ScenarioIT {

    @Test
    @DisplayName("S006 FR-001 só anos com dois ou mais vencedores, em ordem crescente, sem contar indicados")
    void onlyYearsWithTwoOrMoreWinners() {
        http.get().uri(AggregationsReferenceIT.YEARS).exchange()
                .expectStatus().isOk()
                .expectBody().json("""
                        {"years":[{"year":2002,"winnerCount":2},{"year":2003,"winnerCount":3}]}""",
                        JsonCompareMode.STRICT);
    }
}
