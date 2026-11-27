package com.outsera.awards.aggregations.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 17 do testador adversário: empate na terceira posição, estúdio repetido no filme, partes vazias e
 * {@code and} dentro do nome (spec 006, FR-002). O corte dos três fica no front.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/adversario-front/17-empate-no-terceiro-estudio.csv")
class FrontCase17ThirdStudioTieIT extends FrontAdversarialCaseIT {

    @Test
    @DisplayName("S006 adversário caso 17 FR-002 lista completa, um por filme, empate pelo nome")
    void allStudiosInOrder() {
        assertBody(STUDIOS, """
                {"studios":[{"name":"Zeta","winCount":3},{"name":"Alfa","winCount":2},{"name":"Beta","winCount":1},
                {"name":"Delta","winCount":1},{"name":"Gama","winCount":1},{"name":"Norte and Sul","winCount":1}]}""");
    }
}
