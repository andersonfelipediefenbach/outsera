package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import static com.outsera.awards.support.IntervalItem.item;

import com.outsera.awards.support.IntervalItem;
import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.MovieJson;
import com.outsera.awards.support.MoviesBody;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.context.NestedTestConfiguration.EnclosingConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Variações inofensivas do arquivo são aceitas sem aviso (FR-005a), com um método por variação
 * (FR-006c). O {@code harmless-variations.csv} tem BOM, linhas em branco no meio e no fim (uma delas só
 * com espaços), espaços em volta dos campos e do cabeçalho e {@code winner} em {@code YES} e
 * {@code " Yes "}.
 *
 * <p>As duas classes {@code @Nested} comparam os intervalos desse arquivo com os do
 * {@code harmless-clean.csv}, que tem as mesmas 4 linhas sem nenhuma variação. O {@code @AfterEach}
 * desta classe também vale para elas.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/harmless-variations.csv")
@Tag("log")
class HarmlessVariationsIT extends ScenarioIT {

    @Test
    @DisplayName("FR-005a BOM no início do arquivo")
    void bomBeforeHeader(CapturedOutput output) {
        assertThat(output.getAll()).doesNotContain("INVALID_HEADER");
        assertThat(movies("year=1990").totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("FR-005a linhas em branco")
    void blankLinesAreNotRead(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.read()).isEqualTo(4);
        assertThat(summary.accepted()).isEqualTo(4);
        assertThat(summary.rejected()).isZero();
        assertThat(allMovies().totalElements()).isEqualTo(4);
    }

    /**
     * O nome do produtor sem os espaços em volta é provado também pelos intervalos ({@code WithVariations}):
     * as vitórias de 1990 e 1991 só formam o intervalo se os dois nomes forem o mesmo {@code Producer A}.
     */
    @Test
    @DisplayName("FR-005a espaços em volta dos campos e do cabeçalho")
    void surroundingSpacesAreRemoved() {
        MoviesBody movies = movies("year=1990");
        assertThat(movies.content()).hasSize(1);
        MovieJson movie = movies.content().getFirst();
        assertThat(movie.title()).isEqualTo("Filme Com Espaços");
        assertThat(movie.studios()).isEqualTo("Estúdio A");
        assertThat(movie.producers()).isEqualTo("Producer A");
        assertThat(allMovies().titles()).allSatisfy(title -> assertThat(title).doesNotContain("﻿"));
    }

    @Test
    @DisplayName("FR-004 FR-005a yes em qualquer caixa")
    void yesInAnyCase() {
        assertThat(movies("winner=true").content()).extracting(MovieJson::year).containsExactly(1990, 1991);
    }

    /** Em todos os casos, o carregador não emite {@code WARN}. */
    @AfterEach
    void loaderEmitsNoWarning(CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll())).isEmpty();
    }

    /**
     * Resultado esperado nos dois arquivos: as vitórias de 1990 ({@code YES}, com espaços em volta do
     * nome) e de 1991 ({@code " Yes "}) são do mesmo {@code Producer A}.
     */
    private static final List<IntervalItem> EXPECTED = List.of(item("Producer A", 1, 1990, 1991));

    @Nested
    @NestedTestConfiguration(EnclosingConfiguration.OVERRIDE)
    @DisplayName("arquivo com as variações")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/harmless-variations.csv")
    class WithVariations extends ScenarioIT {

        @Test
        @DisplayName("FR-005a US2-9 variações não mudam o resultado")
        void sameIntervals() {
            assertIntervals(EXPECTED, EXPECTED);
        }
    }

    @Nested
    @NestedTestConfiguration(EnclosingConfiguration.OVERRIDE)
    @DisplayName("mesmas linhas sem as variações")
    @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/harmless-clean.csv")
    class Clean extends ScenarioIT {

        @Test
        @DisplayName("FR-005a US2-9 variações não mudam o resultado")
        void sameIntervals() {
            assertIntervals(EXPECTED, EXPECTED);
        }
    }
}
