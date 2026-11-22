package com.outsera.awards.intervals;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.MediaType;

/**
 * Resultado de referência com o arquivo do enunciado, sem {@code movies.csv.path} (FR-017, SC-001).
 * Os valores esperados ficam só aqui, nunca em {@code src/main} (FR-016a).
 */
class ReferenceFileIT extends ScenarioIT {

    private static final String EXPECTED = """
            {"min":[{"producer":"Joel Silver","interval":1,"previousWin":1990,"followingWin":1991}],\
            "max":[{"producer":"Matthew Vaughn","interval":13,"previousWin":2002,"followingWin":2015}]}""";

    @Test
    @DisplayName("FR-017 SC-001 resultado de referência")
    void referenceResult() throws Exception {
        String body = http.get().uri(INTERVALS).exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
        JSONAssert.assertEquals(EXPECTED, body, JSONCompareMode.STRICT);
    }
}
