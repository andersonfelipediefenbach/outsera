package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.Http;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Topics;
import com.outsera.awards.support.TcpForwarder;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;

/**
 * Broker que só sobe depois da aplicação, sem criação automática de tópicos, como no compose (spec 003,
 * caso de borda "Broker indisponível"; apontamento do Codex no PR do Marco 3).
 *
 * <p>A aplicação sobe apontando para uma porta livre, onde nada escuta: o broker está fora de verdade, e o
 * {@code KafkaAdmin} falha ao criar os tópicos. Depois, o teste sobe o broker embutido, com
 * {@code auto.create.topics.enable=false}, e um {@link TcpForwarder} na porta que a aplicação usa. O
 * {@code EmbeddedKafkaKraftBroker} do Spring Kafka 4.1.2 ignora o {@code kafkaPorts} e sempre sobe numa porta
 * aleatória. O repassador só leva a conexão inicial ao broker; daí em diante, os clientes falam direto com
 * ele, pelo endereço que o próprio broker anuncia.
 *
 * <p>A aplicação precisa criar os tópicos sozinha, publicar o arquivo e ficar pronta, sem reinício. Usa
 * {@code SpringApplication} pela cláusula "ordem da subida" do Princípio I: o que se observa é a aplicação
 * real antes e depois de a dependência subir.
 */
class KafkaBrokerLateStartIT {

    private static int freePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    @DisplayName("S003 FR-013 broker que sobe depois: a aplicação cria os tópicos, publica e fica pronta")
    void recoversWhenTheBrokerStartsLater() {
        int kafkaPort = freePort();
        String brokers = "localhost:" + kafkaPort;
        Topics topics = KafkaTestSupport.topics(KafkaBrokerLateStartIT.class);
        EmbeddedKafkaBroker[] broker = new EmbeddedKafkaBroker[1];
        TcpForwarder[] forwarder = new TcpForwarder[1];

        Result result = ApplicationStarter.startAndCall("classpath:movielist.csv",
                KafkaTestSupport.args(brokers, topics), port -> {
                    try {
                        assertThat(Http.get(port, "/actuator/health/readiness").status())
                                .as("readiness sem o broker").isEqualTo(503);

                        EmbeddedKafkaKraftBroker late = new EmbeddedKafkaKraftBroker(1, 1);
                        late.brokerProperty("auto.create.topics.enable", "false");
                        late.afterPropertiesSet();
                        broker[0] = late;
                        int brokerPort = Integer.parseInt(late.getBrokersAsString().replaceAll(".*:", ""));
                        forwarder[0] = TcpForwarder.start(kafkaPort, brokerPort);

                        Http.awaitReady(port, Duration.ofSeconds(90));
                        assertThat(Http.get(port, "/api/producers/intervals").text())
                                .contains("\"producer\":\"Joel Silver\",\"interval\":1")
                                .contains("\"producer\":\"Matthew Vaughn\",\"interval\":13");
                        try (Admin admin = Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                                late.getBrokersAsString()))) {
                            Set<String> names = admin.listTopics().names().get(10, TimeUnit.SECONDS);
                            assertThat(names).as("tópicos criados pela aplicação")
                                    .contains(topics.movies(), topics.errors());
                        } catch (Exception e) {
                            throw new IllegalStateException(e);
                        }
                    } finally {
                        if (forwarder[0] != null) {
                            forwarder[0].close();
                        }
                        if (broker[0] != null) {
                            broker[0].destroy();
                        }
                    }
                });
        assertThat(result.failed()).as("a aplicação deveria subir sem o broker: %s", result.failure()).isFalse();
    }
}
