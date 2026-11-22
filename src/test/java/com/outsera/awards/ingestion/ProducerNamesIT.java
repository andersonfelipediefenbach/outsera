package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Separação da coluna de produtores em nomes (FR-007 a FR-010, research R4), com uma linha do
 * {@code producer-names.csv} por caso, inclusive os dois apontados pelo Codex no PR do bolt 2.
 *
 * <p>A verificação é pelo banco, permitida pelo Princípio I enquanto não há endpoint que mostre os
 * nomes separados. Estes casos passam para o {@code ProducerSeparatorsIT} (T043, bolt 3), por HTTP, e
 * esta classe é removida nesse momento.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/producer-names.csv")
class ProducerNamesIT extends ScenarioIT {

    private static final String PRODUCERS_OF_MOVIE = """
            select p.name from producer p
            join movie_producer mp on mp.producer_id = p.id
            join movie m on m.id = mp.movie_id
            where m.title = ?""";

    @ParameterizedTest(name = "FR-007 FR-008 FR-009 FR-010 {0}: {1}")
    @CsvSource(delimiter = ';', textBlock = """
            Filme 01 ; Eric Fellner|Tom Hooper
            Filme 02 ; Gayle Dickie|Kenn Viselman
            Filme 03 ; Brian Robbinsand Sharla Sumpter Bridgett
            Filme 04 ; Sid|Marty Krofft
            Filme 05 ; A|B
            Filme 06 ; Producer A
            Filme 07 ; Producer A
            Filme 08 ; Producer B
            Filme 09 ; Joel Silver|Anderson
            Filme 10 ; A|andrew B
            Filme 11 ; Producer A|Producer B
            Filme 12 ; Producer R
            Filme 13 ; 'Sid  Krofft|Marty'
            Filme 16 ; Anderson Silva
            """)
    void producersAreSplitIntoNames(String title, String expectedNames) {
        List<String> expected = Arrays.asList(expectedNames.split("\\|"));
        assertThat(jdbc.queryForList(PRODUCERS_OF_MOVIE, String.class, title))
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @ParameterizedTest(name = "FR-006 FR-007 só separadores: linha {0}")
    @CsvSource({"15", "16"})
    void onlySeparatorsIsRejected(int line, CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll()))
                .anyMatch(warning -> warning.endsWith("Linha " + line + " rejeitada: produtores vazios"));
    }

    @Test
    @DisplayName("FR-006 FR-007 linhas só com separadores não são gravadas")
    void onlySeparatorsLinesAreNotStored(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(16);
        assertThat(summary.accepted()).isEqualTo(14);
        assertThat(summary.rejected()).isEqualTo(2);
        assertThat(jdbc.queryForObject("select count(*) from movie where title in ('Filme 14', 'Filme 15')",
                Integer.class)).isZero();
    }
}
