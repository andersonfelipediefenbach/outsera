package com.outsera.awards;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/** O projeto sobe e responde só com o wrapper, sem Docker nem serviço externo (FR-026). */
class SmokeIT extends ScenarioIT {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("FR-026 a aplicação sobe só com o wrapper")
    void applicationStartsOnRandomPort() {
        assertThat(port).isPositive();
    }

    @Test
    @DisplayName("FR-026 rota inexistente responde 404 em Problem Details")
    void unknownRouteAnswersProblemDetails() {
        http.get().uri("/rota-inexistente")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }
}
