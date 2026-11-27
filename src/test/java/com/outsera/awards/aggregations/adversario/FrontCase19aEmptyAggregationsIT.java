package com.outsera.awards.aggregations.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 19, primeira execução: só indicados, as duas agregações e os intervalos vazios (spec 006, FR-003). */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/adversario-front/19a-agregacoes-vazias-e-dois-estudios.csv")
class FrontCase19aEmptyAggregationsIT extends FrontAdversarialCaseIT {

    @Test
    @DisplayName("S006 adversário caso 19 FR-003 só indicados: anos, estúdios e intervalos vazios com 200")
    void everythingEmpty() {
        assertBody(YEARS, "{\"years\":[]}");
        assertBody(STUDIOS, "{\"studios\":[]}");
        assertBody(INTERVALS, "{\"min\":[],\"max\":[]}");
    }
}
