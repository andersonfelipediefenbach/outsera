package com.outsera.awards.aggregations.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 14 do testador adversário: marcação HTML e um {@code script} inerte nos campos do CSV chegam à API como
 * texto, sem perda (spec 006, FR-002, FR-007 e FR-017a). A parte da tela fica no teste unitário do caso 14.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/adversario-front/14-html-literal.csv")
class FrontCase14HtmlLiteralIT extends FrontAdversarialCaseIT {

    private static final String PRODUCER = "<script type=application/json>{}</script>";

    @Test
    @DisplayName("S006 adversário caso 14 FR-002 estúdio com marcação conta 2 vitórias, com o nome literal")
    void studioWithMarkupIsLiteral() {
        assertBody(STUDIOS, "{\"studios\":[{\"name\":\"<i>MARCA-ESTUDIO</i>\",\"winCount\":2}]}");
    }

    @Test
    @DisplayName("S006 adversário caso 14 intervalo do produtor com script literal em min e max")
    void producerWithScriptIsLiteral() {
        String item = "{\"producer\":\"" + PRODUCER + "\",\"interval\":1,\"previousWin\":1990,\"followingWin\":1991}";
        assertBody(INTERVALS, "{\"min\":[" + item + "],\"max\":[" + item + "]}");
    }

    @Test
    @DisplayName("S006 adversário caso 14 FR-017a título com marcação literal na listagem")
    void titleWithMarkupIsLiteral() {
        assertBody(MOVIES + "?year=1990", """
                {"content":[{"year":1990,"title":"<b>MARCA-TITULO</b>","studios":"<i>MARCA-ESTUDIO</i>",
                "producers":"<script type=application/json>{}</script>","winner":true}],
                "page":0,"size":20,"totalElements":1,"totalPages":1}""");
    }
}
