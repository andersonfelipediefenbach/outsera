package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Três produtores empatam no maior intervalo (12). O {@code Producer Y}, com o maior
 * {@code previousWin}, vem primeiro no arquivo, e {@code Producer X} vem antes de {@code Producer W},
 * com o mesmo {@code previousWin}.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/ties-max.csv")
class TiesMaxIT extends ScenarioIT {

    @Test
    @DisplayName("FR-014 FR-015 empate no máximo")
    void allTiedMaximumsOrderedByPreviousWinThenName() {
        assertIntervals(
                List.of(item("Producer M", 2, 2330, 2332)),
                List.of(item("Producer W", 12, 2310, 2322),
                        item("Producer X", 12, 2310, 2322),
                        item("Producer Y", 12, 2340, 2352)));
    }
}
