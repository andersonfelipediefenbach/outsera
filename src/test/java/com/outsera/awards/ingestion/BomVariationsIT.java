package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * O BOM é removido antes de o arquivo ser analisado, então ele não interfere nas aspas do cabeçalho nem
 * transforma uma linha em branco em cabeçalho (FR-005a, research R3; revisão do Codex no PR do bolt 2).
 */
class BomVariationsIT {

    @Nested
    @DisplayName("FR-005a BOM com cabeçalho entre aspas")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/bom-quoted-header.csv")
    class QuotedHeader extends ScenarioIT {

        @Test
        @DisplayName("FR-005a BOM com cabeçalho entre aspas carrega normalmente")
        void loadsNormally(CapturedOutput output) {
            assertLoadedTwoMovies(output, jdbc.queryForObject("select count(*) from movie", Integer.class));
        }
    }

    @Nested
    @DisplayName("FR-005a BOM seguido de linha em branco")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/bom-blank-line.csv")
    class BlankLineAfterBom extends ScenarioIT {

        @Test
        @DisplayName("FR-005a BOM seguido de linha em branco carrega normalmente")
        void loadsNormally(CapturedOutput output) {
            assertLoadedTwoMovies(output, jdbc.queryForObject("select count(*) from movie", Integer.class));
        }
    }

    private static void assertLoadedTwoMovies(CapturedOutput output, Integer moviesInDatabase) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(2);
        assertThat(summary.accepted()).isEqualTo(2);
        assertThat(summary.rejected()).isZero();
        assertThat(LoaderLog.loaderWarnings(output.getAll())).isEmpty();
        assertThat(moviesInDatabase).isEqualTo(2);
    }
}
