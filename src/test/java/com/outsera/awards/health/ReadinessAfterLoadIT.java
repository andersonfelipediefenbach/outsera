package com.outsera.awards.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * A aplicação só se declara pronta depois do fim da carga do CSV (spec 002, FR-009, FR-010, SC-003;
 * research R2).
 *
 * <p>Roda a aplicação real com {@code SpringApplication} e um ouvinte real dos eventos do ciclo de vida.
 * No momento de cada evento, o ouvinte escreve um marcador no console. Como a saída é capturada na
 * ordem em que é escrita, o teste confere a ordem entre o resumo da carga e a passagem para "pronta".
 *
 * <p>Usa {@code SpringApplication}, e não {@code @SpringBootTest}, pela cláusula de controle do ciclo de
 * vida do Princípio I (constituição 1.5.0), caso "ordem da subida": o ouvinte precisa estar registrado
 * antes do {@code run}. Sem mocks. A ordem não é observável por HTTP, porque a probe só responde depois
 * que o servidor sobe; por isso, é verificada pelos eventos reais. A resposta HTTP da readiness é
 * verificada no {@code HealthProbesIT} e no {@code ProbeStatesIT}.
 */
class ReadinessAfterLoadIT {

    private static final String READY_MARKER = "MARCADOR-PRONTIDAO ACCEPTING_TRAFFIC";
    private static final String STARTED_MARKER = "MARCADOR-INICIO readiness=";
    private static final String SUMMARY = "Carga do arquivo classpath:movielist.csv concluída";

    /** Escreve um marcador a cada evento relevante. Não altera nada na aplicação. */
    static final class MarkerListener implements ApplicationListener<ApplicationEvent> {

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ApplicationStartedEvent started) {
                ReadinessState state = started.getApplicationContext()
                        .getBean(ApplicationAvailability.class).getReadinessState();
                System.out.println(STARTED_MARKER + state);
            } else if (event instanceof AvailabilityChangeEvent<?> change
                    && change.getState() == ReadinessState.ACCEPTING_TRAFFIC) {
                System.out.println(READY_MARKER);
            }
        }
    }

    @Test
    @DisplayName("FR-009 SC-003 US2-3 a passagem para pronta acontece depois do resumo da carga")
    void readyOnlyAfterLoad() {
        Result result = ApplicationStarter.start("classpath:movielist.csv", new MarkerListener());
        assertThat(result.failed()).isFalse();

        String output = result.output();
        int summary = output.indexOf(SUMMARY);
        int started = output.indexOf(STARTED_MARKER);
        int ready = output.indexOf(READY_MARKER);
        assertThat(summary).as("resumo da carga").isNotNegative();
        assertThat(ready).as("evento de pronta").isGreaterThan(summary);
        assertThat(output.indexOf(READY_MARKER, ready + 1)).as("um único evento de pronta").isNegative();
        assertThat(started).as("evento de início").isGreaterThan(summary).isLessThan(ready);
        assertThat(output).contains(STARTED_MARKER + ReadinessState.REFUSING_TRAFFIC);
    }

    @Test
    @DisplayName("FR-010 US2-5 com erro estrutural, a aplicação nunca se declara pronta")
    void neverReadyOnStructuralError() {
        Result result = ApplicationStarter.start("file:./nao-existe.csv", new MarkerListener());
        assertThat(result.failed()).isTrue();
        assertThat(result.output()).doesNotContain(READY_MARKER);
    }
}
