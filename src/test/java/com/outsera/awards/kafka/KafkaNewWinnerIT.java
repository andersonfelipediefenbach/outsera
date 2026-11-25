package com.outsera.awards.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.KafkaScenarioIT;
import com.outsera.awards.support.MovieJson;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * Depois da carga, um filme novo chega pelo mesmo tópico e aparece nos intervalos e na listagem sem
 * reinício, e o ETag muda junto com o corpo (spec 003, FR-016, US3-1, US3-2, US3-6 e SC-005).
 */
// Muda os dados ou a readiness: contexto próprio, fechado no fim (KafkaScenarioIT).
@DirtiesContext
@TestPropertySource(properties = {"movies.csv.path=classpath:movielist.csv", "awards.test.context=KafkaNewWinnerIT"})
class KafkaNewWinnerIT extends KafkaScenarioIT {

    private static final String PRODUCERS = "Producer X, Joel Silver and Producer Y";

    @Test
    @DisplayName("S003 FR-016 US3-1 US3-2 US3-6 SC-005 vencedor novo depois da carga, em até 5 s")
    void newWinnerAfterTheLoad() {
        awaitReady();
        String etagBefore = http.get().uri(INTERVALS).exchange().expectStatus().isOk()
                .returnResult().getResponseHeaders().getETag();

        sendMovie("1991|Evento Novo", "{\"year\":1991,\"title\":\"Evento Novo\",\"studios\":null,"
                + "\"producers\":\"" + PRODUCERS + "\",\"winner\":true}");
        await("Joel Silver com intervalo 0 em min", 5,
                () -> intervals().min().stream().anyMatch(item -> item.interval() == 0));

        // Formato exato do enunciado, sem nenhum campo a mais (Princípio III).
        assertIntervals(
                List.of(new IntervalItem("Joel Silver", 0, 1991, 1991)),
                List.of(new IntervalItem("Matthew Vaughn", 13, 2002, 2015)));

        // O ETag antigo deixa de valer: 200 com o corpo novo e outro ETag (US3-2).
        String etagAfter = http.get().uri(INTERVALS).header(HttpHeaders.IF_NONE_MATCH, etagBefore).exchange()
                .expectStatus().isOk()
                .returnResult().getResponseHeaders().getETag();
        assertThat(etagAfter).isNotNull().isNotEqualTo(etagBefore);

        // O texto dos produtores é gravado como veio, e os nomes são separados pelas regras do Marco 1.
        List<MovieJson> movies = movies("year=1991&size=100").content();
        assertThat(movies).filteredOn(movie -> movie.title().equals("Evento Novo"))
                .singleElement()
                .satisfies(movie -> {
                    assertThat(movie.producers()).isEqualTo(PRODUCERS);
                    assertThat(movie.winner()).isTrue();
                });
    }
}
