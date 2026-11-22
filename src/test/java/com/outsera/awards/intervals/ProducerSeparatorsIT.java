package com.outsera.awards.intervals;

import static com.outsera.awards.support.IntervalItem.item;
import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Separação da coluna de produtores em nomes (FR-007 a FR-010, research R4), verificada por HTTP.
 * Absorve os casos do antigo {@code ProducerNamesIT} (bolt 2), que verificava pelo banco.
 *
 * <p>Cada forma do {@code producer-separators.csv} aparece em duas vitórias de anos seguidos, e os nomes
 * que se repetem entre formas ({@code Producer A}, {@code Producer B}, {@code A} e {@code Producer R})
 * estão em formas de anos também seguidos. Assim, todo intervalo correto vale 1, e {@code min} e
 * {@code max} trazem a lista completa de nomes. Um nome separado errado aparece como item a mais ou a
 * menos; dois nomes unidos por engano, ou um nome contado duas vezes no mesmo filme, geram um
 * intervalo 0 ou maior que 1.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/producer-separators.csv")
class ProducerSeparatorsIT extends ScenarioIT {

    /** Todos os itens esperados, ordenados por {@code previousWin} e depois pelo nome. */
    private static final List<IntervalItem> ALL = List.of(
            one("Eric Fellner", 2101),
            one("Tom Hooper", 2101),
            one("Gayle Dickie", 2103),
            one("Kenn Viselman", 2103),
            one("Brian Robbinsand Sharla Sumpter Bridgett", 2105),
            one("Marty Krofft", 2107),
            one("Sid", 2107),
            one("Producer A", 2109),
            one("Producer A", 2110),
            one("Producer A", 2111),
            one("Producer A", 2112),
            one("Producer A", 2113),
            one("Producer B", 2113),
            one("Producer B", 2114),
            one("Producer B", 2115),
            one("Anderson", 2117),
            one("Joel Silver", 2117),
            one("Anderson Silva", 2119),
            one("A", 2121),
            one("B", 2121),
            one("A", 2122),
            one("A", 2123),
            one("andrew B", 2123),
            one("Producer R", 2125),
            one("Producer R", 2126),
            one("Producer R", 2127),
            one("Marty", 2129),
            one("Sid  Krofft", 2129),
            one("Steve Perry", 2131),
            one("Steven Perry", 2131),
            one("Producer S", 2133),
            one("Producer T", 2133),
            one("Producer U", 2133),
            one("José Núñez", 2135));

    /** Intervalo de 1 ano a partir de {@code previousWin}. */
    private static IntervalItem one(String producer, int previousWin) {
        return item(producer, 1, previousWin, previousWin + 1);
    }

    private Set<String> producerNames() {
        return intervals().all().map(IntervalItem::producer).collect(Collectors.toSet());
    }

    @Test
    @DisplayName("FR-007 FR-008 FR-009 FR-010 nomes separados corretamente")
    void namesAreSplitCorrectly() {
        assertIntervals(ALL, ALL);
    }

    @Test
    @DisplayName("FR-007 FR-009 Brian Robbinsand Sharla Sumpter Bridgett é um único produtor")
    void andGluedToAWordIsNotASeparator() {
        assertThat(producerNames())
                .contains("Brian Robbinsand Sharla Sumpter Bridgett")
                .doesNotContain("Brian Robbins", "Brian Robbinsand", "Sharla Sumpter Bridgett");
    }

    @Test
    @DisplayName("FR-007 Sid and Marty Krofft são dois produtores")
    void andBetweenNamesSeparates() {
        assertThat(producerNames())
                .contains("Sid", "Marty Krofft")
                .doesNotContain("Sid and Marty Krofft");
    }

    @Test
    @DisplayName("FR-007 and no fim do campo")
    void trailingAndIsDropped() {
        assertThat(producerNames())
                .contains("Producer A")
                .doesNotContain("and", "Producer A and", "Producer A,", "Producer A, and", "");
    }

    @Test
    @DisplayName("FR-008 Steven Perry e Steve Perry são produtores distintos")
    void similarNamesAreNotMerged() {
        assertThat(intervals().min())
                .contains(one("Steve Perry", 2131), one("Steven Perry", 2131))
                .allMatch(item -> item.interval() == 1);
    }

    @Test
    @DisplayName("FR-010 nome repetido no mesmo filme conta uma vez")
    void repeatedNameInSameMovieCountsOnce() {
        assertThat(intervals().all().filter(item -> item.producer().equals("Producer R")))
                .isNotEmpty()
                .allMatch(item -> item.interval() == 1);
    }

    @Test
    @DisplayName("Edge UTF-8 preservado")
    void utf8IsPreserved() {
        assertThat(producerNames()).contains("José Núñez");
    }

    @ParameterizedTest(name = "FR-006 FR-007 só separadores: linha {0}")
    @ValueSource(ints = {40, 41, 42, 43})
    void onlySeparatorsIsRejected(int line, CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll()))
                .anyMatch(warning -> warning.endsWith("Linha " + line + " rejeitada: produtores vazios"));
    }

    @Test
    @DisplayName("FR-006 FR-007 linhas só com separadores não entram no cálculo")
    void onlySeparatorsLinesAreNotLoaded(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(42);
        assertThat(summary.accepted()).isEqualTo(38);
        assertThat(summary.rejected()).isEqualTo(4);
        assertThat(intervals().all()).noneMatch(item -> item.previousWin() >= 2137);
    }
}
