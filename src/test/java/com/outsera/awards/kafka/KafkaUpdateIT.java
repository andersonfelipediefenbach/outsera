package com.outsera.awards.kafka;

import static com.outsera.awards.support.IntervalItem.item;
import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.MovieJson;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Um evento com o ano e o título de um filme gravado atualiza o filme: a última gravação prevalece (spec
 * 003, FR-014, US2-5 e casos de borda). Os passos são sequenciais, sobre o mesmo estado.
 *
 * <p>Na carga, {@code Producer A} vence em 2000 e 2008 (8) e {@code Producer B} em 2008 e 2012 (4).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/kafka/update-base.csv")
class KafkaUpdateIT extends KafkaScenarioIT {

    private void send(int year, String title, String producers, boolean winner) {
        sendMovie(year + "|" + title, "{\"year\":" + year + ",\"title\":\"" + title + "\",\"studios\":\"S\","
                + "\"producers\":\"" + producers + "\",\"winner\":" + winner + "}");
    }

    private void awaitIntervals(List<IntervalItem> min, List<IntervalItem> max) {
        await("intervalos " + IntervalItem.json(min, max), 10,
                () -> intervals().min().equals(min) && intervals().max().equals(max));
        assertIntervals(min, max);
        assertThat(total("")).isEqualTo(4);
    }

    @Test
    @DisplayName("S003 FR-014 US2-5 atualização: indicado vira vencedor, vencedor vira indicado, produtor sai")
    void lastRecordWins() {
        awaitReady();
        awaitIntervals(List.of(item("Producer B", 4, 2008, 2012)), List.of(item("Producer A", 8, 2000, 2008)));

        // Indicado que vira vencedor (US2-5): Producer A passa a vencer em 2000, 2004 e 2008.
        send(2004, "Upd B", "Producer A", true);
        List<IntervalItem> allFours = List.of(
                item("Producer A", 4, 2000, 2004),
                item("Producer A", 4, 2004, 2008),
                item("Producer B", 4, 2008, 2012));
        awaitIntervals(allFours, allFours);

        // Vencedor que vira indicado: a vitória de 2004 deixa de contar.
        send(2004, "Upd B", "Producer A", false);
        awaitIntervals(List.of(item("Producer B", 4, 2008, 2012)), List.of(item("Producer A", 8, 2000, 2008)));

        // Produtor removido: Producer B sai de Upd C, fica com uma só vitória e some dos intervalos.
        send(2008, "Upd C", "Producer A", true);
        awaitIntervals(List.of(item("Producer A", 8, 2000, 2008)), List.of(item("Producer A", 8, 2000, 2008)));
        assertThat(movies("year=2008").content()).singleElement()
                .extracting(MovieJson::producers).isEqualTo("Producer A");
    }
}
