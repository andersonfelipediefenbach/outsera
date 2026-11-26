package com.outsera.awards.kafka.adversario;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 22 do testador adversário: os filmes do publicador processados com o marcador retido no transporte
 * ({@link com.outsera.awards.support.HoldingProducerInterceptor}). Sem o marcador, a readiness nunca fica
 * positiva, mesmo com todos os filmes gravados e o offset confirmado.
 */
@DirtiesContext
@TestPropertySource(properties = {KafkaAdversarialIT.TWO_LINES_CSV, KafkaAdversarialIT.HOLD_CLASS,
        KafkaAdversarialIT.HOLD_COMPLETION, KafkaAdversarialIT.HOLD_TOPIC,
        "awards.test.context=KafkaCase22MarkerWithheldIT"})
class KafkaCase22MarkerWithheldIT extends KafkaAdversarialIT {

    @Test
    @DisplayName("S004 FR-035 S003 FR-011 FR-013 SC-003 caso 22: com o marcador retido, a readiness fica 503 depois de todos os filmes")
    void readinessStaysNegativeWithoutTheMarker(CapturedOutput output) {
        held(1);
        await("os dois filmes do publicador gravados", 30, () -> total("") == 2);
        awaitConsumed();

        // Amostras durante 3 s, depois de tudo processado: nenhum prazo substitui o marcador.
        long until = System.nanoTime() + 3_000_000_000L;
        while (System.nanoTime() < until) {
            assertThat(readinessStatus()).as("readiness sem o marcador").isEqualTo(503);
        }
        assertErrors(output);
        assertMovies(movie(2000, "Base", null, "Iris", true), movie(2002, "Controle", null, "Iris", true));
        assertControlIntervals();
    }
}
