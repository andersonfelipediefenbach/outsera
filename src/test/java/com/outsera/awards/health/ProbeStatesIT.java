package com.outsera.awards.health;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.json.JsonCompareMode;

/**
 * Os probes respondem 503 quando o estado não é positivo (spec 002, FR-008). O estado é mudado pela API
 * pública de disponibilidade do Spring, com a aplicação real no ar, sem dublê (research R2). O contexto
 * é descartado no fim da classe.
 */
@DirtiesContext
class ProbeStatesIT extends ScenarioIT {

    @Autowired
    private ApplicationContext context;

    @AfterEach
    void restore() {
        AvailabilityChangeEvent.publish(context, ReadinessState.ACCEPTING_TRAFFIC);
        AvailabilityChangeEvent.publish(context, LivenessState.CORRECT);
    }

    @Test
    @DisplayName("FR-008 readiness responde 503 com OUT_OF_SERVICE quando recusa tráfego")
    void readinessRefusingIs503() {
        AvailabilityChangeEvent.publish(context, ReadinessState.REFUSING_TRAFFIC);
        http.get().uri(HealthProbesIT.READINESS).exchange()
                .expectStatus().isEqualTo(503)
                .expectBody().json("{\"status\":\"OUT_OF_SERVICE\"}", JsonCompareMode.STRICT);
    }

    @Test
    @DisplayName("FR-008 liveness responde 503 com DOWN quando quebrada")
    void livenessBrokenIs503() {
        AvailabilityChangeEvent.publish(context, LivenessState.BROKEN);
        http.get().uri(HealthProbesIT.LIVENESS).exchange()
                .expectStatus().isEqualTo(503)
                .expectBody().json("{\"status\":\"DOWN\"}", JsonCompareMode.STRICT);
    }
}
