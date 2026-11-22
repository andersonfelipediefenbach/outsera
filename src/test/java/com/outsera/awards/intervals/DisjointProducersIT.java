package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Arquivo sem nenhum produtor nem ano em comum com o {@code movielist.csv}, que cobre 1980 a 2019
 * (SC-004a). O caminho vem pela chave {@code MOVIES_CSV_PATH}, a mesma da variável de ambiente, e não
 * por {@code movies.csv.path}: isso prova o mapeamento do {@code application.yml} (FR-002).
 */
@TestPropertySource(properties = "MOVIES_CSV_PATH=classpath:scenarios/disjoint-producers.csv")
class DisjointProducersIT extends ScenarioIT {

    @Test
    @DisplayName("FR-002 FR-016a FR-016b SC-004a arquivo sem relação com o fornecido")
    void resultComesOnlyFromTheLoadedFile() {
        assertIntervals(
                List.of(item("Producer A", 2, 1950, 1952)),
                List.of(item("Producer B", 50, 1925, 1975)));
    }
}
