package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * {@code Producer A} vence em 2000, 2005 e 2020, e {@code Producer B} em 2001 e 2003. O máximo é o par
 * vizinho 2005→2020 (15), nunca a primeira contra a última vitória, 2000→2020 (20).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/three-or-more-wins.csv")
class ThreeOrMoreWinsIT extends ScenarioIT {

    @Test
    @DisplayName("FR-012 só pares vizinhos")
    void onlyNeighbouringWins() {
        assertIntervals(
                List.of(item("Producer B", 2, 2001, 2003)),
                List.of(item("Producer A", 15, 2005, 2020)));
    }
}
