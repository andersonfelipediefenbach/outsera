package com.outsera.awards.aggregations.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 18 do testador adversário: desempate com acentos e caixa em ordem Unicode ({@code String.compareTo}),
 * sem colação de idioma, como no Marco 1 (spec 006, FR-002; decisão do responsável).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/adversario-front/18-desempate-com-acentos-e-caixa.csv")
class FrontCase18AccentAndCaseOrderIT extends FrontAdversarialCaseIT {

    @Test
    @DisplayName("S006 adversário caso 18 FR-002 quatro nomes distintos em ordem Unicode: Alfa, Zeta, alfa, Álamo")
    void unicodeOrder() {
        assertBody(STUDIOS, "{\"studios\":[{\"name\":\"Alfa\",\"winCount\":1},{\"name\":\"Zeta\",\"winCount\":1},"
                + "{\"name\":\"alfa\",\"winCount\":1},{\"name\":\"\u00c1lamo\",\"winCount\":1}]}");
    }
}
