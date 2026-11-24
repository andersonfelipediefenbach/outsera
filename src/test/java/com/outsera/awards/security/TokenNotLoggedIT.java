package com.outsera.awards.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import com.outsera.awards.support.Http;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Nenhum valor de token aparece no log nem nas métricas, nem o certo nem o errado (spec 003, FR-025 e
 * US4-6). Usa {@code SpringApplication} pela cláusula "saída completa da subida" do Princípio I, para ler
 * o log inteiro da execução.
 */
@Tag("log")
class TokenNotLoggedIT {

    @Test
    @DisplayName("S003 FR-025 US4-6 nenhum token no log nem nas métricas")
    void tokensAreNeverLogged() {
        String token = UUID.randomUUID().toString();
        String wrong = UUID.randomUUID().toString();
        String[] metrics = new String[1];
        Result result = ApplicationStarter.startAndCall("classpath:movielist.csv",
                List.of("--awards.security.token=" + token, "--logging.level.org.springframework.security=DEBUG"),
                port -> {
                    assertThat(Http.get(port, "/api/producers/intervals", "Authorization", "Bearer " + token).status())
                            .isEqualTo(200);
                    assertThat(Http.get(port, "/api/movies", "Authorization", "Bearer " + wrong).status())
                            .isEqualTo(401);
                    metrics[0] = Http.get(port, "/actuator/prometheus", "Authorization", "Bearer " + token).text();
                });
        assertThat(result.failed()).isFalse();
        assertThat(result.output()).doesNotContain(token).doesNotContain(wrong);
        assertThat(metrics[0]).isNotBlank().doesNotContain(token).doesNotContain(wrong);
    }
}
