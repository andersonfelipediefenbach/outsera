package com.outsera.awards.health;

import static com.outsera.awards.support.IntervalItem.item;
import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;

/**
 * Liveness, readiness e saúde geral com o arquivo padrão (spec 002, FR-008, FR-009 e FR-011; história 2;
 * contracts/operational-endpoints.md).
 */
class HealthProbesIT extends ScenarioIT {

    static final String LIVENESS = "/actuator/health/liveness";
    static final String READINESS = "/actuator/health/readiness";
    static final String HEALTH = "/actuator/health";

    /**
     * 200 com o corpo exatamente {@code {"status": ...}}, num tipo JSON. O Actuator responde
     * {@code application/vnd.spring-boot.actuator.v3+json}, que tem o sufixo {@code +json}.
     */
    private void assertStatusOnly(String path, String status) {
        assertBody(path, "{\"status\":\"" + status + "\"}");
    }

    private void assertBody(String path, String expectedJson) {
        var result = http.get().uri(path).exchange()
                .expectStatus().isOk()
                .expectBody().json(expectedJson, JsonCompareMode.STRICT)
                .returnResult();
        MediaType type = result.getResponseHeaders().getContentType();
        assertThat(type).isNotNull();
        assertThat(type.isCompatibleWith(MediaType.APPLICATION_JSON) || "json".equals(type.getSubtypeSuffix()))
                .as("Content-Type %s é JSON", type).isTrue();
    }

    @Test
    @DisplayName("FR-008 US2-1 liveness responde 200 com status UP")
    void livenessIsUp() {
        assertStatusOnly(LIVENESS, "UP");
    }

    @Test
    @DisplayName("FR-008 FR-009 US2-2 readiness responde 200 com status UP, com os dados já disponíveis")
    void readinessIsUpWithData() {
        assertStatusOnly(READINESS, "UP");
        assertThat(movies("").totalElements()).isEqualTo(206);
        assertThat(intervals().min()).containsExactly(item("Joel Silver", 1, 1990, 1991));
    }

    @Test
    @DisplayName("FR-011 US2-6 saúde geral traz só o status e os nomes dos dois probes, sem componentes nem detalhes")
    void healthHasOnlyStatusAndProbeGroups() {
        assertBody(HEALTH, "{\"status\":\"UP\",\"groups\":[\"liveness\",\"readiness\"]}");
    }

    @ParameterizedTest(name = "FR-011 {0} não é exposto")
    @ValueSource(strings = {"/actuator", "/actuator/env", "/actuator/beans", "/actuator/info",
            "/actuator/metrics"})
    void otherActuatorEndpointsAreNotExposed(String path) {
        http.get().uri(path).exchange().expectStatus().isNotFound();
    }
}
