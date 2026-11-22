package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/** Casos de borda em que {@code min} e {@code max} são iguais. */
class IntervalEdgeCasesIT {

    @Nested
    @DisplayName("um único intervalo")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/single-interval.csv")
    class SingleInterval extends ScenarioIT {

        @Test
        @DisplayName("Edge intervalo único aparece em min e max")
        void singleIntervalInBoth() {
            List<IntervalItem> only = List.of(item("Producer A", 3, 2090, 2093));
            assertIntervals(only, only);
        }
    }

    @Nested
    @DisplayName("todos os intervalos iguais")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/all-equal-intervals.csv")
    class AllEqual extends ScenarioIT {

        @Test
        @DisplayName("Edge todos iguais aparecem em min e max")
        void allIntervalsInBoth() {
            List<IntervalItem> all = List.of(
                    item("Producer A", 2, 2601, 2603),
                    item("Producer C", 2, 2605, 2607),
                    item("Producer C", 2, 2607, 2609),
                    item("Producer B", 2, 2610, 2612));
            assertIntervals(all, all);
        }
    }
}
