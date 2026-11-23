package com.outsera.awards.movies;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Com o arquivo fornecido, as duas consultas respondem em menos de 1 segundo (SC-005).
 *
 * <p>O {@code @DirtiesContext} dá a esta classe um contexto novo, sem nenhuma requisição anterior. Assim, a
 * primeira chamada medida inclui a inicialização preguiçosa do servlet do Spring MVC, como a primeira
 * consulta de um avaliador. Não há aquecimento.
 */
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class ResponseTimeIT extends ScenarioIT {

    private static final Duration LIMIT = Duration.ofSeconds(1);

    @Test
    @DisplayName("SC-005 respostas abaixo de 1 segundo")
    void firstCallsAnswerInLessThanOneSecond() {
        Duration intervals = time(INTERVALS);
        Duration movies = time(MOVIES);
        assertThat(intervals).as("primeira chamada a %s", INTERVALS).isLessThan(LIMIT);
        assertThat(movies).as("primeira chamada a %s", MOVIES).isLessThan(LIMIT);
    }

    private Duration time(String uri) {
        long start = System.nanoTime();
        http.get().uri(uri).exchange().expectStatus().isOk();
        return Duration.ofNanos(System.nanoTime() - start);
    }
}
