package com.outsera.awards.aggregations.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 15 do testador adversário: {@code Café} composto (U+00E9) e decomposto (U+0065 U+0301), emoji e
 * caracteres japoneses, sem normalização (spec 006, FR-002; spec 001, FR-008). Os dois estúdios e os dois
 * produtores são distintos, então não há intervalo.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/adversario-front/15-unicode-sem-normalizacao.csv")
class FrontCase15UnicodeIT extends FrontAdversarialCaseIT {

    private static final String COMPOSED = "Caf\u00e9";
    private static final String DECOMPOSED = "Cafe\u0301";

    @Test
    @DisplayName("S006 adversário caso 15 FR-002 Café composto e decomposto são estúdios distintos, em ordem Unicode")
    void composedAndDecomposedAreDistinct() {
        // String.compareTo: 'e' (U+0065) vem antes de 'é' (U+00E9).
        assertBody(STUDIOS, "{\"studios\":[{\"name\":\"" + DECOMPOSED + "\",\"winCount\":1},{\"name\":\""
                + COMPOSED + "\",\"winCount\":1}]}");
    }

    @Test
    @DisplayName("S006 adversário caso 15 produtores distintos: sem intervalo")
    void noIntervals() {
        assertBody(INTERVALS, "{\"min\":[],\"max\":[]}");
    }

    @Test
    @DisplayName("S006 adversário caso 15 título com emoji e japonês preservado na listagem")
    void titleIsPreserved() {
        assertBody(MOVIES + "?year=2001", "{\"content\":[{\"year\":2001,\"title\":\"Estreia \uD83C\uDFAC \u2014 \u6771\u4eac\","
                + "\"studios\":\"" + COMPOSED + "\",\"producers\":\"" + COMPOSED + "\",\"winner\":true}],"
                + "\"page\":0,\"size\":20,\"totalElements\":1,\"totalPages\":1}");
    }
}
