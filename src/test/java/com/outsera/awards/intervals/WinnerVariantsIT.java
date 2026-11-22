package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * {@code Producer A} vence com {@code yes} (2050), {@code YES} (2054) e {@code " Yes "} (2060), e tem
 * uma indicação com {@code winner} vazio em 2051. Se a indicação contasse, o mínimo seria 2050→2051; se
 * alguma variação de {@code yes} não contasse, os intervalos seriam outros.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/winner-variants.csv")
class WinnerVariantsIT extends ScenarioIT {

    @Test
    @DisplayName("FR-004 FR-011 variações de yes; vazio não conta")
    void yesVariantsCountAndEmptyDoesNot() {
        assertIntervals(
                List.of(item("Producer A", 4, 2050, 2054)),
                List.of(item("Producer A", 6, 2054, 2060)));
    }
}
