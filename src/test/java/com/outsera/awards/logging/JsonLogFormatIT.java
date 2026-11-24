package com.outsera.awards.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.LoaderLog;
import com.outsera.awards.support.LoaderLog.Summary;
import com.outsera.awards.support.LogOutput;
import com.outsera.awards.support.LogOutput.Event;
import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;

/**
 * Com {@code LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs}, cada evento é uma linha JSON no formato ECS
 * (spec 002, FR-017 a FR-020, SC-005; história 4). Roda só na execução {@code json-logs} do Failsafe.
 */
@Tag("log")
@EnabledIfEnvironmentVariable(named = "LOGGING_STRUCTURED_FORMAT_CONSOLE", matches = "ecs")
class JsonLogFormatIT extends ScenarioIT {

    private static List<String> nonBlankLines(String output) {
        return output.lines().filter(line -> !line.isBlank()).toList();
    }

    @Test
    @DisplayName("FR-017 FR-020 SC-005 US4-1 toda linha é JSON ECS com os campos obrigatórios, sem banner")
    void everyLineIsEcsJson() {
        Result result = ApplicationStarter.start("classpath:movielist.csv");
        assertThat(result.failed()).isFalse();

        List<String> lines = nonBlankLines(result.output());
        assertThat(lines).isNotEmpty().allMatch(LogOutput::isJson);
        assertThat(result.output()).doesNotContain(TextLogFormatIT.BANNER);
        for (String line : lines) {
            JsonNode node = LogOutput.json(line);
            for (String field : List.of("@timestamp", "log.level", "log.logger", "process.thread.name", "message")) {
                assertThat(LogOutput.field(node, field)).as("campo %s em %s", field, line).isNotNull();
            }
            assertThat(LogOutput.field(node, "service.name")).as("service.name em %s", line).isEqualTo("awards");
        }
    }

    @Test
    @DisplayName("FR-018 US4-2 avisos de linha rejeitada e resumo da carga nas mensagens JSON")
    void loadMessagesInJson() {
        Result result = ApplicationStarter.start("classpath:scenarios/invalid-lines.csv");
        assertThat(result.failed()).isFalse();
        assertThat(nonBlankLines(result.output())).allMatch(LogOutput::isJson);

        assertThat(LoaderLog.loaderWarnings(result.output()))
                .hasSize(11)
                .contains("Linha 8 rejeitada: ano não numérico (19x0)")
                .contains("Linha 14 rejeitada: ano fora da faixa de 1 a 9999 (0)");
        Summary summary = LoaderLog.summary(result.output());
        assertThat(summary.read()).isEqualTo(13);
        assertThat(summary.accepted()).isEqualTo(2);
        assertThat(summary.rejected()).isEqualTo(11);
    }

    @Test
    @DisplayName("FR-017 US4-3 a falha de inicialização é um único evento ERROR com a mensagem inteira")
    void startupFailureIsOneEvent() {
        Result result = ApplicationStarter.start("file:./nao-existe.csv");
        assertThat(result.failed()).isTrue();
        assertThat(nonBlankLines(result.output())).allMatch(LogOutput::isJson);

        List<Event> failures = LogOutput.parse(result.output()).stream()
                .filter(event -> event.message() != null && event.message().contains("APPLICATION FAILED TO START"))
                .toList();
        assertThat(failures).hasSize(1);
        Event failure = failures.getFirst();
        assertThat(failure.level()).isEqualTo("ERROR");
        assertThat(failure.message()).contains("\n").contains("file:./nao-existe.csv").contains("NOT_FOUND");
    }

    @Test
    @DisplayName("Edge aspas e acentos da mensagem continuam corretos depois de decodificar o JSON")
    void escapingIsPreserved() {
        Result result = ApplicationStarter.start("classpath:scenarios/comma-separated.csv");
        assertThat(result.failed()).isTrue();
        assertThat(result.output()).doesNotContain("\";\"");
        assertThat(result.messages()).contains("\";\"");
    }

    @Test
    @DisplayName("FR-019 o cabeçalho Authorization não aparece no log")
    void authorizationIsNotLogged(CapturedOutput output) {
        String marker = "marcador-fr019-json";
        http.get().uri(INTERVALS).header(HttpHeaders.AUTHORIZATION, "Bearer " + marker)
                .exchange().expectStatus().isOk();
        assertThat(output.getAll()).doesNotContain(marker);
    }
}
