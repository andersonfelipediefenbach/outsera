package com.outsera.awards.aggregations.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Caso 16 do testador adversário: anos em ordem crescente, com a contagem maior no meio (spec 006, FR-001). */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/adversario-front/16-anos-empatados.csv")
class FrontCase16TiedYearsIT extends FrontAdversarialCaseIT {

    @Test
    @DisplayName("S006 adversário caso 16 FR-001 ordem pelo ano, sem contar indicados")
    void yearsInOrder() {
        assertBody(YEARS, """
                {"years":[{"year":1989,"winnerCount":2},{"year":1991,"winnerCount":3},{"year":1995,"winnerCount":2}]}""");
    }
}
