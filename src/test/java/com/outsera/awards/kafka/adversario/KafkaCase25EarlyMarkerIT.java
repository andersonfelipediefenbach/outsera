package com.outsera.awards.kafka.adversario;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaTestSupport.Record;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 25 do testador adversário: uma cópia do marcador atual entregue antes do último filme da carga. O
 * achado foi decidido pelo Anderson: o marcador vale pelo {@code runId}, e a readiness é um sinal operacional,
 * não uma barreira de segurança. O publicador nunca antecipa o marcador (uma partição e envio síncrono, um
 * a um), então a cópia antecipada só existe com escrita indevida no tópico, cuja proteção é do broker (ACLs),
 * fora do escopo (contrato dos eventos, "Evento de carga concluída"; docs/ai/decisoes.md).
 *
 * <p>O teste fixa esse comportamento documentado: a cópia antecipada libera a readiness, e a carga termina
 * certa quando os eventos legítimos chegam.
 */
@DirtiesContext
@TestPropertySource(properties = {KafkaAdversarialIT.TWO_LINES_CSV, KafkaAdversarialIT.HOLD_CLASS,
        KafkaAdversarialIT.HOLD_AFTER_FIRST, KafkaAdversarialIT.HOLD_TOPIC,
        "awards.test.context=KafkaCase25EarlyMarkerIT"})
class KafkaCase25EarlyMarkerIT extends KafkaAdversarialIT {

    @Test
    @DisplayName("S004 FR-035 S003 FR-011 FR-013 SC-003 caso 25: o marcador antecipado por escrita indevida libera a readiness, como documentado")
    void earlyMarkerReleasesReadinessAsDocumented(CapturedOutput output) {
        List<Record> held = held(2);
        Record control = held.get(0);
        Record marker = held.get(1);
        assertThat(control.key()).isEqualTo("2002|Controle");
        assertThat(marker.key()).isEqualTo("load-completed");
        awaitConsumed();
        assertThat(readinessStatus()).as("readiness só com Base").isEqualTo(503);
        assertMovies(movie(2000, "Base", null, "Iris", true));

        release(marker);
        awaitConsumed();
        assertThat(readinessStatus()).as("readiness com a cópia antecipada (sinal operacional)").isEqualTo(200);
        assertMovies(movie(2000, "Base", null, "Iris", true));
        assertIntervals(List.of(), List.of());

        release(control);
        release(marker);
        awaitConsumed();
        assertErrors(output);
        assertMovies(movie(2000, "Base", null, "Iris", true), movie(2002, "Controle", null, "Iris", true));
        assertReady();
        assertControlIntervals();
    }
}
