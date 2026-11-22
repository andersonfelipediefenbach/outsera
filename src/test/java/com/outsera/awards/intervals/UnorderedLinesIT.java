package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * As mesmas linhas, em ordem cronológica e embaralhadas, dão o mesmo resultado (FR-016). No arquivo
 * embaralhado, as vitórias do {@code Producer A} aparecem como 2484, 2480 e 2483.
 */
class UnorderedLinesIT {

    private static final List<IntervalItem> MIN = List.of(
            item("Producer C", 1, 2475, 2476),
            item("Producer A", 1, 2483, 2484));

    private static final List<IntervalItem> MAX = List.of(
            item("Producer B", 29, 2470, 2499));

    @Nested
    @DisplayName("linhas em ordem cronológica")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/ordered-lines.csv")
    class Ordered extends ScenarioIT {

        @Test
        @DisplayName("FR-016 independe da ordem das linhas")
        void sameResult() {
            assertIntervals(MIN, MAX);
        }
    }

    @Nested
    @DisplayName("linhas embaralhadas")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/unordered-lines.csv")
    class Unordered extends ScenarioIT {

        @Test
        @DisplayName("FR-016 independe da ordem das linhas")
        void sameResult() {
            assertIntervals(MIN, MAX);
        }
    }
}
