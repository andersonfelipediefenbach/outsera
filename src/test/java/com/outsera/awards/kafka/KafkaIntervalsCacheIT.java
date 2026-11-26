package com.outsera.awards.kafka;

import static com.outsera.awards.support.IntervalItem.item;
import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.IntervalsBody;
import com.outsera.awards.support.KafkaScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

/**
 * O resultado dos intervalos é guardado entre as leituras e invalidado a cada gravação, com consistência
 * imediata (docs/ai/decisoes.md, "Intervalos guardados entre as leituras"). O teste lê, grava por evento e
 * lê de novo, sem nenhuma espera depois de a gravação ficar visível na listagem: a primeira leitura depois
 * dela já precisa trazer o resultado novo, com outro ETag. Depois, uma atualização desfaz a mudança, e o
 * resultado e o ETag voltam aos de antes.
 */
// Muda os dados: contexto próprio, fechado no fim (KafkaScenarioIT).
@DirtiesContext
@TestPropertySource(properties = {"movies.csv.path=classpath:movielist.csv", "awards.test.context=KafkaIntervalsCacheIT"})
class KafkaIntervalsCacheIT extends KafkaScenarioIT {

    private static final List<IntervalItem> REFERENCE_MIN = List.of(item("Joel Silver", 1, 1990, 1991));
    private static final List<IntervalItem> REFERENCE_MAX = List.of(item("Matthew Vaughn", 13, 2002, 2015));

    private static String event(String title, boolean winner) {
        return "{\"year\":2030,\"title\":\"" + title + "\",\"studios\":null,\"producers\":\"Cache Producer\",\"winner\":"
                + winner + "}";
    }

    private String etag() {
        return http.get().uri(INTERVALS).exchange().expectStatus().isOk().returnResult().getResponseHeaders().getETag();
    }

    @Test
    @DisplayName("S004 SLO S003 FR-014 FR-016 SC-005 a leitura depois de uma gravação confirmada já traz o resultado novo, com outro ETag")
    void readWriteReadSeesTheNewResult() {
        awaitReady();
        // Duas leituras: a segunda vem do resultado guardado.
        assertIntervals(REFERENCE_MIN, REFERENCE_MAX);
        assertIntervals(REFERENCE_MIN, REFERENCE_MAX);
        String referenceEtag = etag();
        http.get().uri(INTERVALS).header("If-None-Match", referenceEtag).exchange().expectStatus().isNotModified();

        // Duas vitórias do mesmo produtor no mesmo ano: intervalo 0, que passa a ser o mínimo.
        sendMovie("2030|Cache A", event("Cache A", true));
        sendMovie("2030|Cache B", event("Cache B", true));
        await("as duas vitórias gravadas", 10, () -> total("year=2030&winner=true") == 2);
        IntervalsBody after = intervals();
        assertThat(after.min()).as("min logo depois da gravação").containsExactly(item("Cache Producer", 0, 2030, 2030));
        assertThat(after.max()).as("max logo depois da gravação").isEqualTo(REFERENCE_MAX);
        String newEtag = etag();
        assertThat(newEtag).as("ETag depois da gravação").isNotEqualTo(referenceEtag);
        http.get().uri(INTERVALS).header("If-None-Match", referenceEtag).exchange().expectStatus().isOk();
        http.get().uri(INTERVALS).header("If-None-Match", newEtag).exchange().expectStatus().isNotModified();

        // A atualização desfaz a mudança: os dois viram indicados.
        sendMovie("2030|Cache A", event("Cache A", false));
        sendMovie("2030|Cache B", event("Cache B", false));
        await("as duas atualizações gravadas", 10, () -> total("year=2030&winner=false") == 2);
        assertIntervals(REFERENCE_MIN, REFERENCE_MAX);
        assertThat(etag()).as("ETag depois de desfazer a mudança").isEqualTo(referenceEtag);
    }
}
