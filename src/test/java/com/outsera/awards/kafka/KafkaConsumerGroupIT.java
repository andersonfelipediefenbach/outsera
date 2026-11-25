package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaScenarioIT;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.GroupListing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * O consumidor se registra no broker com o grupo configurado em {@code KAFKA_CONSUMER_GROUP}, e não com
 * um nome fixo no código (spec 003, FR-003; apontamento do Codex no PR do Marco 3). A verificação é no
 * próprio broker, pelo {@code AdminClient} real.
 */
@TestPropertySource(properties = {
        "movies.csv.path=classpath:scenarios/header-only.csv",
        "KAFKA_CONSUMER_GROUP=grupo-configurado-it"})
class KafkaConsumerGroupIT extends KafkaScenarioIT {

    @Test
    @DisplayName("S003 FR-003 o consumidor usa o grupo de KAFKA_CONSUMER_GROUP")
    void consumerUsesTheConfiguredGroup() throws Exception {
        awaitReady();
        try (Admin admin = Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, brokers()))) {
            Collection<GroupListing> groups = admin.listGroups().all().get(10, TimeUnit.SECONDS);
            List<String> ids = groups.stream().map(GroupListing::groupId).toList();
            assertThat(ids).as("grupos registrados no broker").contains("grupo-configurado-it")
                    .doesNotContain("awards-movies", "awards");
            assertThat(admin.describeConsumerGroups(List.of("grupo-configurado-it")).all()
                    .get(10, TimeUnit.SECONDS).get("grupo-configurado-it").members())
                    .as("membros do grupo configurado").hasSize(1);
        }
    }
}
