package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.ScenarioIT;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.context.TestPropertySource;

/** A coluna de estúdios é opcional: vazia, ela não invalida a linha (FR-006e). */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/optional-studios.csv")
class OptionalStudiosIT extends ScenarioIT {

    @Test
    @DisplayName("FR-006e estúdios é opcional")
    void emptyStudiosIsAccepted(CapturedOutput output) {
        assertThat(LoaderLog.loaderWarnings(output.getAll())).isEmpty();
        Map<String, Object> movie = jdbc.queryForMap("select title, studios from movie");
        assertThat(movie).containsEntry("TITLE", "Filme Sem Estúdio");
        assertThat(movie.get("STUDIOS")).isNull();
    }
}
