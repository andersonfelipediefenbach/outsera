package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * A linha {@code 1990;Filme X;Estúdio;Producer A;yes} aparece duas vezes, e {@code Producer A} também
 * vence em 1995. A repetição é rejeitada (FR-006d) e não pode virar uma segunda vitória em 1990.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/duplicate-winner.csv")
class DuplicateWinnerIT extends ScenarioIT {

    @Test
    @DisplayName("FR-006d duplicata não gera intervalo 0")
    void duplicateDoesNotCountTwice() {
        assertIntervals(
                List.of(item("Producer A", 5, 1990, 1995)),
                List.of(item("Producer A", 5, 1990, 1995)));
    }
}
