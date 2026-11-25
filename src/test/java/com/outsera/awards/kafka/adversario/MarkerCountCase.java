package com.outsera.awards.kafka.adversario;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaTestSupport.Record;
import java.util.List;
import org.springframework.boot.test.system.CapturedOutput;

/**
 * Variantes do caso 26 do testador adversário: o marcador atual com {@code count} alterado antes do
 * marcador legítimo. O achado foi decidido pelo Anderson: o {@code count} é validado como inteiro JSON
 * maior ou igual a zero, e um marcador sem ele ou com outro tipo é {@code COUNT_INVALID} e não libera a
 * readiness. O valor do {@code count} é informativo, não uma barreira: um inteiro inconsistente libera
 * (docs/ai/decisoes.md). Cada variante é uma classe, com aplicação nova, como o caso pede.
 */
abstract class MarkerCountCase extends KafkaAdversarialIT {

    /**
     * Envia o marcador alterado ({@code countJson} no lugar do {@code count}, ou nada, se nulo) e depois o
     * legítimo. Com {@code reason} nulo, o alterado libera a readiness; senão, vai ao tópico de erros.
     */
    void run(CapturedOutput output, String countJson, String reason) {
        Record current = held(1).getFirst();
        String altered = "{\"type\":\"load-completed\",\"runId\":\"" + runId(current) + "\""
                + (countJson == null ? "" : ",\"count\":" + countJson) + "}";
        send("load-completed", altered);
        awaitConsumed();
        assertThat(readinessStatus()).as("readiness depois do marcador alterado").isEqualTo(reason == null ? 200 : 503);

        release(current);
        awaitConsumed();
        assertReady();
        if (reason == null) {
            assertErrors(output);
        } else {
            assertErrors(output, rejected("load-completed", reason, altered));
        }
        assertMovies();
        assertIntervals(List.of(), List.of());
    }
}
