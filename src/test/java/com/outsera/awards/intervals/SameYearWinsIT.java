package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** {@code Producer A} vence dois filmes diferentes em 1995 e outro em 2000. */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/same-year-wins.csv")
class SameYearWinsIT extends ScenarioIT {

    @Test
    @DisplayName("FR-013 duas vitórias no mesmo ano = intervalo 0")
    void sameYearGivesZero() {
        assertIntervals(
                List.of(item("Producer A", 0, 1995, 1995)),
                List.of(item("Producer A", 5, 1995, 2000)));
    }
}
