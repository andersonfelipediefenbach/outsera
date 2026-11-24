package com.outsera.awards.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.LogOutput;
import com.outsera.awards.support.LogOutput.Event;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.http.HttpHeaders;

/**
 * Sem a variável de formato, o log é o texto padrão do Boot, como no Marco 1, e sem o banner (spec 002,
 * FR-019, FR-020, US4-5). Roda só na execução padrão do Failsafe.
 */
@DisabledIfEnvironmentVariable(named = "LOGGING_STRUCTURED_FORMAT_CONSOLE", matches = ".+")
class TextLogFormatIT extends ScenarioIT {

    static final String BANNER = ":: Spring Boot ::";

    @Test
    @DisplayName("FR-020 US4-5 sem a variável, nenhuma linha é JSON e o resumo está no texto do Boot")
    void textFormatByDefault() {
        Result result = ApplicationStarter.start("classpath:movielist.csv");
        assertThat(result.failed()).isFalse();
        assertThat(result.output().lines()).noneMatch(LogOutput::isJson);

        Event summary = LogOutput.parse(result.output()).stream()
                .filter(event -> event.message() != null && event.message().startsWith("Carga do arquivo"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("resumo da carga ausente"));
        assertThat(summary.timestamp()).isNotNull();
        assertThat(summary.level()).isEqualTo("INFO");
        assertThat(summary.logger()).endsWith("MovieFileLoader");
        assertThat(summary.thread()).isNotBlank();
    }

    @Test
    @DisplayName("FR-020 o banner do framework fica desligado")
    void noBanner() {
        Result result = ApplicationStarter.start("classpath:movielist.csv");
        assertThat(result.output()).doesNotContain(BANNER);
    }

    @Test
    @DisplayName("FR-019 o cabeçalho Authorization não aparece no log")
    void authorizationIsNotLogged(CapturedOutput output) {
        String marker = "marcador-fr019-texto";
        http.get().uri(INTERVALS).header(HttpHeaders.AUTHORIZATION, "Bearer " + marker)
                .exchange().expectStatus().isOk();
        assertThat(output.getAll()).doesNotContain(marker);
    }
}
