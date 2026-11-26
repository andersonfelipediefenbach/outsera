package com.outsera.awards.aggregations.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 20 do testador adversário: empates de produtores, com dois pares do mesmo produtor em {@code min}
 * (spec 006, FR-005 e FR-007; Princípio III).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/adversario-front/20-empates-nos-intervalos.csv")
class FrontCase20IntervalTiesIT extends FrontAdversarialCaseIT {

    @Test
    @DisplayName("S006 adversário caso 20 FR-005 todos os empatados, ordem por previousWin e nome")
    void allTies() {
        assertBody(INTERVALS, """
                {"min":[
                  {"producer":"Rena","interval":1,"previousWin":1989,"followingWin":1990},
                  {"producer":"Ana","interval":1,"previousWin":1990,"followingWin":1991},
                  {"producer":"Rena","interval":1,"previousWin":1990,"followingWin":1991},
                  {"producer":"Zoe","interval":1,"previousWin":1990,"followingWin":1991}],
                 "max":[
                  {"producer":"Bruno","interval":10,"previousWin":1980,"followingWin":1990},
                  {"producer":"Caio","interval":10,"previousWin":1980,"followingWin":1990}]}""");
    }
}
