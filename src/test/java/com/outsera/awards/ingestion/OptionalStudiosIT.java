package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.MovieJson;
import com.outsera.awards.support.MoviesBody;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/** A coluna de estúdios é opcional: vazia, ela não invalida a linha (FR-006e). */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/optional-studios.csv")
@Tag("log")
class OptionalStudiosIT extends ScenarioIT {

    @Test
    @DisplayName("FR-006e estúdios é opcional")
    void emptyStudiosIsAccepted(CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll())).isEmpty();
        MoviesBody movies = movies("");
        assertThat(movies.totalElements()).isEqualTo(1);
        MovieJson movie = movies.content().getFirst();
        assertThat(movie.title()).isEqualTo("Filme Sem Estúdio");
        assertThat(movie.studios()).isNull();
    }
}
