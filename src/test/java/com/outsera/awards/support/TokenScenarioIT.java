package com.outsera.awards.support;

import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Base dos testes com a autenticação ligada (spec 003, FR-019 a FR-026). O token é gerado em tempo de
 * execução: nenhum token literal fica no código (Princípio VII).
 */
public abstract class TokenScenarioIT extends ScenarioIT {

    /** Token desta execução dos testes. */
    protected static final String TOKEN = UUID.randomUUID().toString();

    @DynamicPropertySource
    static void token(DynamicPropertyRegistry registry) {
        registry.add("awards.security.token", () -> TOKEN);
    }

    /** Valor do cabeçalho {@code Authorization} com o token certo. */
    protected static String bearer() {
        return "Bearer " + TOKEN;
    }

    /** Status de {@code GET path} com o cabeçalho {@code Authorization} dado, ou sem ele se nulo. */
    protected int status(String path, String authorization) {
        var request = http.get().uri(path);
        if (authorization != null) {
            request = request.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        return request.exchange().returnResult().getStatus().value();
    }
}
