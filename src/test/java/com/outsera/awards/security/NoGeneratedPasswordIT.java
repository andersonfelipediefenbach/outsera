package com.outsera.awards.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ApplicationStarter;
import com.outsera.awards.support.ApplicationStarter.Result;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Tag;

/**
 * O Spring Boot não gera usuário nem senha, com o token ligado ou desligado (spec 003, FR-019; research
 * R11). Usa {@code SpringApplication} pela cláusula "saída completa da subida" do Princípio I.
 */
@Tag("log")
class NoGeneratedPasswordIT {

    @ParameterizedTest(name = "S003 FR-019 sem senha gerada, token ligado: {0}")
    @ValueSource(booleans = {false, true})
    void noGeneratedPassword(boolean tokenOn) {
        List<String> args = tokenOn ? List.of("--awards.security.token=" + UUID.randomUUID()) : List.of();
        Result result = ApplicationStarter.start("classpath:movielist.csv", args);
        assertThat(result.failed()).isFalse();
        assertThat(result.output())
                .doesNotContain("Using generated security password")
                .doesNotContain("UserDetailsServiceAutoConfiguration");
    }
}
