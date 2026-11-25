package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.KafkaTestSupport;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Com o broker fora do ar, a aplicação sobe viva e não pronta: a liveness responde 200 e a readiness 503
 * (spec 003, FR-013, caso de borda "Broker indisponível"; research R8). O endereço do broker é uma porta
 * local que o teste abre e fecha, sem nada atendendo nela. Usa {@code SpringApplication} pela cláusula
 * "ordem da subida" do Princípio I: o que se observa é o estado da aplicação real sem a dependência.
 */
class KafkaBrokerUnavailableIT {

    private static int closedPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    @DisplayName("S003 FR-013 broker indisponível: liveness 200 e readiness 503")
    void aliveButNotReady() {
        String brokers = "localhost:" + closedPort();
        long start = System.nanoTime();
        List<String> args = new ArrayList<>(KafkaTestSupport.args(brokers,
                KafkaTestSupport.topics(KafkaBrokerUnavailableIT.class)));
        // Os prazos curtos dos clientes vêm do perfil de teste kafkatest (KafkaTestSupport.args).
        Result result = ApplicationStarter.startAndCall("classpath:movielist.csv", args, port -> {
                    long deadline = System.nanoTime() + 5_000_000_000L;
                    while (System.nanoTime() < deadline) {
                        assertThat(Http.get(port, "/actuator/health/liveness").status()).isEqualTo(200);
                        assertThat(Http.get(port, "/actuator/health/readiness").status()).isEqualTo(503);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                });
        assertThat(result.failed()).as("a aplicação deveria subir sem o broker: %s", result.failure()).isFalse();
        long seconds = (System.nanoTime() - start) / 1_000_000_000L;
        assertThat(seconds).as("subida e observação com o broker fora (s)").isLessThan(90);
    }
}
