package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Variações inofensivas do arquivo são aceitas sem aviso (FR-005a), com um método por variação
 * (FR-006c). O {@code harmless-variations.csv} tem BOM, linhas em branco no meio e no fim (uma delas só
 * com espaços), espaços em volta dos campos e do cabeçalho e {@code winner} em {@code YES} e
 * {@code " Yes "}.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/harmless-variations.csv")
class HarmlessVariationsIT extends ScenarioIT {

    @Test
    @DisplayName("FR-005a BOM no início do arquivo")
    void bomBeforeHeader(CapturedOutput output) {
        assertThat(output.getAll()).doesNotContain("INVALID_HEADER");
        assertThat(jdbc.queryForObject("select count(*) from movie where release_year = 1990", Integer.class))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("FR-005a linhas em branco")
    void blankLinesAreNotRead(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(4);
        assertThat(summary.accepted()).isEqualTo(4);
        assertThat(summary.rejected()).isZero();
    }

    @Test
    @DisplayName("FR-005a espaços em volta dos campos e do cabeçalho")
    void surroundingSpacesAreRemoved() {
        assertThat(jdbc.queryForMap("select title, studios, producers from movie where release_year = 1990"))
                .containsEntry("TITLE", "Filme Com Espaços")
                .containsEntry("STUDIOS", "Estúdio A")
                .containsEntry("PRODUCERS", "Producer A");
        assertThat(jdbc.queryForList("select name from producer where name like '%Producer A%'", String.class))
                .containsExactly("Producer A");
    }

    @Test
    @DisplayName("FR-004 FR-005a yes em qualquer caixa")
    void yesInAnyCase() {
        assertThat(jdbc.queryForList("select release_year from movie where winner order by release_year",
                Integer.class)).containsExactly(1990, 1991);
    }

    /** Em todos os casos, o carregador não emite {@code WARN}. */
    @AfterEach
    void loaderEmitsNoWarning(CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll())).isEmpty();
    }
}
