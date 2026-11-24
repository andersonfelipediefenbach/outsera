package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.LogOutput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * O publicador registra um único resumo, com as contagens do {@code LoadSummaryIT} do Marco 1 (spec 003,
 * FR-009; research R8). Roda nos dois formatos de log.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/load-summary.csv")
@Tag("log")
class PublisherSummaryIT extends KafkaScenarioIT {

    @Test
    @DisplayName("S003 FR-009 resumo da publicação")
    void summary(CapturedOutput output) {
        awaitReady();
        String expected = "Publicação do arquivo classpath:scenarios/load-summary.csv concluída: "
                + "10 linhas lidas, 8 publicadas, 2 rejeitadas";
        // O resumo sai depois do envio do marcador, e a readiness pode ficar positiva um pouco antes.
        await("resumo da publicação", 10, () -> output.getAll().contains(expected));
        assertThat(LogOutput.messages(output.getAll())).filteredOn(message -> message.contains(expected)).hasSize(1);
        assertThat(movies("").totalElements()).isEqualTo(8);
    }
}
