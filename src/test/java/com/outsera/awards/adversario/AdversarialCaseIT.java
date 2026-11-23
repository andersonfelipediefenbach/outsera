package com.outsera.awards.adversario;

import com.outsera.awards.support.ScenarioIT;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;

/**
 * Base dos casos do testador adversário (Codex), copiados sem alteração de {@code docs/ai/adversario/}
 * para {@code src/test/resources/scenarios/adversario/}.
 *
 * <p>Cada subclasse aponta {@code movies.csv.path} para o CSV do caso e chama
 * {@link #assertMatchesExpected(String)} com o nome do caso. O corpo de
 * {@code GET /api/producers/intervals} é comparado ao {@code <caso>.expected.json} como JSON estrito,
 * preservando a ordem e a multiplicidade dos itens de {@code min} e {@code max}, como pede o README dos
 * casos.
 */
abstract class AdversarialCaseIT extends ScenarioIT {

    static final String DIR = "scenarios/adversario/";

    protected void assertMatchesExpected(String caseName) {
        http.get().uri(INTERVALS).exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json(expected(caseName), JsonCompareMode.STRICT);
    }

    private static String expected(String caseName) {
        try (InputStream in = new ClassPathResource(DIR + caseName + ".expected.json").getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
