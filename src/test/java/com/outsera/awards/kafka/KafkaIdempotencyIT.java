package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalsBody;
import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.KafkaTestSupport;
import com.outsera.awards.support.KafkaTestSupport.Record;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Receber de novo o arquivo inteiro, ou o mesmo evento duas vezes, não duplica nada (spec 003, FR-014 e
 * SC-004). Um evento sentinela, enviado depois dos repetidos, marca o fim do processamento deles: o
 * tópico tem uma partição e é consumido em ordem.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:movielist.csv")
class KafkaIdempotencyIT extends KafkaScenarioIT {

    private void sendSentinel(int year, String title) {
        sendMovie(year + "|" + title, "{\"year\":" + year + ",\"title\":\"" + title + "\",\"studios\":null,"
                + "\"producers\":\"Sentinela\",\"winner\":false}");
    }

    @Test
    @DisplayName("S003 FR-014 SC-004 arquivo reenviado e evento repetido não duplicam")
    void redeliveryDoesNotDuplicate() {
        awaitReady();
        IntervalsBody before = intervals();
        List<Record> published = KafkaTestSupport.read(brokers(), MOVIES_TOPIC, 207, Duration.ofSeconds(30));
        assertThat(published).hasSize(207);

        // O arquivo inteiro de novo, exatamente como o publicador enviou (menos o marcador).
        for (Record record : published.subList(0, 206)) {
            KafkaTestSupport.send(brokers(), MOVIES_TOPIC, record.key(), record.value());
        }
        sendSentinel(2041, "Sentinela 1");
        await("sentinela 1 gravada", 30, () -> total("year=2041") == 1);
        assertThat(total("")).isEqualTo(207);
        assertThat(intervals()).isEqualTo(before);

        // O mesmo evento novo duas vezes aumenta o total em 1.
        sendSentinel(2042, "Repetido");
        sendSentinel(2042, "Repetido");
        sendSentinel(2043, "Sentinela 2");
        await("sentinela 2 gravada", 10, () -> total("year=2043") == 1);
        assertThat(total("year=2042")).isEqualTo(1);
        assertThat(total("")).isEqualTo(209);
    }
}
