package com.outsera.awards.intervals;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Nenhum produtor vence duas vezes. {@code Producer A} e {@code Producer C} têm uma vitória e uma
 * indicação, que não conta.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/no-repeat-winners.csv")
class NoRepeatWinnersIT extends ScenarioIT {

    @Test
    @DisplayName("FR-018 nenhum produtor com duas vitórias")
    void emptyLists() {
        assertIntervals(List.of(), List.of());
    }
}
