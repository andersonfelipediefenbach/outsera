package com.outsera.awards.aggregations.adversario;

import com.outsera.awards.support.ScenarioIT;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;

/**
 * Base dos casos do testador adversário do front e das agregações (spec 006; casos fora do repositório, como no
 * Marco 4). Os CSVs ficam em {@code src/test/resources/scenarios/adversario-front/}, copiados dos casos sem
 * alteração. Cada subclasse aponta {@code movies.csv.path} para o CSV do caso e compara as respostas reais com o
 * esperado do caso, como JSON estrito: mesma ordem, mesma multiplicidade e nenhum campo a mais.
 */
abstract class FrontAdversarialCaseIT extends ScenarioIT {

    static final String YEARS = "/api/aggregations/years-with-multiple-winners";
    static final String STUDIOS = "/api/aggregations/studios-with-win-count";

    protected void assertBody(String path, String expected) {
        http.get().uri(path).exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json(expected, JsonCompareMode.STRICT);
    }
}
