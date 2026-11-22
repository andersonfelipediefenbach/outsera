package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Três produtores empatam no menor intervalo (1). O {@code Producer D}, com o maior
 * {@code previousWin}, vem primeiro no arquivo, e {@code Producer C} vem antes de {@code Producer B},
 * com o mesmo {@code previousWin}: a resposta não pode seguir a ordem do arquivo.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/ties-min.csv")
class TiesMinIT extends ScenarioIT {

    @Test
    @DisplayName("FR-014 FR-015 empate no mínimo")
    void allTiedMinimumsOrderedByPreviousWinThenName() {
        assertIntervals(
                List.of(item("Producer B", 1, 2203, 2204),
                        item("Producer C", 1, 2203, 2204),
                        item("Producer D", 1, 2210, 2211)),
                List.of(item("Producer Z", 10, 2220, 2230)));
    }
}
