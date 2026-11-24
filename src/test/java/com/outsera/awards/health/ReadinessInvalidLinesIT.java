package com.outsera.awards.health;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;

/**
 * Linhas inválidas ignoradas não impedem a prontidão (spec 002, FR-010, US2-4).
 *
 * <p>O {@code @DirtiesContext} fecha o contexto no fim da classe: o {@code InvalidLinesIT} usa o mesmo
 * CSV e lê o log da inicialização, então não pode receber um contexto já criado aqui.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/invalid-lines.csv")
@DirtiesContext
class ReadinessInvalidLinesIT extends ScenarioIT {

    @Test
    @DisplayName("FR-010 US2-4 com linhas rejeitadas, a readiness responde 200 com status UP")
    void readyDespiteRejectedLines() {
        http.get().uri(HealthProbesIT.READINESS).exchange()
                .expectStatus().isOk()
                .expectBody().json("{\"status\":\"UP\"}", JsonCompareMode.STRICT);
    }
}
