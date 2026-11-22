package com.outsera.awards.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

/**
 * Sem a propriedade {@code movies.csv.path}, a aplicação carrega o arquivo do enunciado embarcado no
 * projeto, e a carga termina antes de o servidor aceitar conexões (FR-001, FR-002).
 *
 * <p>A configuração é a mesma do {@code SmokeIT}. O {@code @DirtiesContext} garante um contexto novo,
 * para que o log da inicialização seja capturado por esta classe.
 */
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
class DefaultFileIT extends ScenarioIT {

    @Test
    @DisplayName("FR-001 FR-002 carrega o arquivo padrão")
    void loadsTheDefaultFile(CapturedOutput output) {
        Summary summary = LoaderLog.summary(output.getAll());
        assertThat(summary.path()).isEqualTo("classpath:movielist.csv");
        assertThat(summary.rejected()).isZero();
        assertThat(summary.accepted()).isPositive();
        assertThat(jdbc.queryForObject("select count(*) from movie", Integer.class))
                .isEqualTo(summary.accepted());
    }

    @Test
    @DisplayName("FR-001 a carga termina antes de o servidor aceitar conexões")
    void loadFinishesBeforeTheServerStarts(CapturedOutput output) {
        String log = output.getAll();
        int loadFinished = log.indexOf("Carga do arquivo classpath:movielist.csv concluída");
        int serverStarted = log.indexOf("Tomcat started on port");
        assertThat(loadFinished).as("linha do resumo da carga").isNotNegative();
        assertThat(serverStarted).as("linha de início do servidor web").isNotNegative();
        assertThat(loadFinished).isLessThan(serverStarted);
    }
}
