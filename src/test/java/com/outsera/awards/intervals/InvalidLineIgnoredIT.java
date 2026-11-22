package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Três linhas inválidas criariam intervalos menores se fossem aceitas: {@code Producer A} com
 * {@code winner=no} em 2072, {@code Producer B} com o ano {@code 207x} e {@code Producer B} em 2076 com
 * uma coluna a menos.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/invalid-line-in-calc.csv")
class InvalidLineIgnoredIT extends ScenarioIT {

    @Test
    @DisplayName("FR-006 linha inválida não influencia o resultado")
    void invalidLinesAreIgnored() {
        assertIntervals(
                List.of(item("Producer B", 4, 2071, 2075)),
                List.of(item("Producer A", 10, 2070, 2080)));
    }
}
