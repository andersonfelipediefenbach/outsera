package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.MovieJson;
import com.outsera.awards.support.MoviesBody;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/**
 * Um filme é identificado por ano + título, sem os espaços em volta. A primeira ocorrência prevalece, e
 * as repetições são rejeitadas com aviso citando a linha original (FR-006d).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/duplicate-lines.csv")
@Tag("log")
class DuplicateLinesIT extends ScenarioIT {

    @Test
    @DisplayName("FR-006d duplicata ano+título")
    void duplicateYearAndTitleIsRejected(CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll()))
                .hasSize(2)
                .anyMatch(w -> w.endsWith("Linha 4 rejeitada: filme duplicado (mesmo ano e título da linha 2)"))
                .anyMatch(w -> w.endsWith("Linha 5 rejeitada: filme duplicado (mesmo ano e título da linha 2)"));

        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.accepted()).isEqualTo(1);
        assertThat(summary.rejected()).isEqualTo(2);

        MoviesBody movies = allMovies();
        assertThat(movies.totalElements()).isEqualTo(1);
        MovieJson movie = movies.content().getFirst();
        assertThat(movie.title()).isEqualTo("Filme X");
        assertThat(movie.studios()).isEqualTo("Estúdio");
        assertThat(movie.producers()).isEqualTo("Producer A");
    }
}
