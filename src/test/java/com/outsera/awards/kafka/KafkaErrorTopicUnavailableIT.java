package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.KafkaScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Com o tópico de erros indisponível, o evento inválido não é gravado, o log registra o motivo e a falha
 * do envio, e o consumo segue (spec 003, FR-015, caso de borda "Tópico de erros indisponível"; research
 * R9). O tópico de erros tem um nome que o Kafka não aceita: a criação dele na subida falha (com
 * {@code fail-fast} desligado, a aplicação sobe assim mesmo), e cada envio é recusado de verdade pelo
 * cliente Kafka, sem dublê. A verificação da tarefa T034c confirmou que esse nome não derruba a subida.
 */
@TestPropertySource(properties = {
        "movies.csv.path=classpath:scenarios/header-only.csv",
        "KAFKA_TOPIC_ERRORS=awards.movies.errors inválido!"})
@Tag("log")
class KafkaErrorTopicUnavailableIT extends KafkaScenarioIT {

    @Test
    @DisplayName("S003 FR-015 tópico de erros indisponível: aviso no log e o consumo segue")
    void consumptionContinues(CapturedOutput output) {
        awaitReady();
        sendMovie("invalido", "{\"year\":0,\"title\":\"Invalido\",\"producers\":\"P\",\"winner\":true}");
        sendMovie("2050|Valido", "{\"year\":2050,\"title\":\"Valido\",\"studios\":null,\"producers\":\"P\",\"winner\":false}");
        await("evento válido gravado", 15, () -> total("year=2050") == 1);
        assertThat(total("")).isEqualTo(1);
        assertThat(output.getAll()).contains("Evento rejeitado").contains("YEAR_OUT_OF_RANGE")
                .contains("Falha ao enviar o evento rejeitado ao tópico de erros");
        assertThat(readinessStatus()).isEqualTo(200);
    }
}
