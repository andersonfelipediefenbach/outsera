package com.outsera.awards.support;

import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Base de todos os testes de cenário. Sobe a aplicação completa em porta aleatória, sem mocks
 * (Princípio I da constituição).
 *
 * <p>Cada subclasse escolhe o seu CSV com
 * {@code @TestPropertySource(properties = "movies.csv.path=classpath:scenarios/<arquivo>.csv")}, e cada
 * CSV gera um contexto próprio, guardado no cache de contextos do Spring. A subclasse não deve
 * redeclarar {@code @SpringBootTest}: a anotação dela substituiria a desta base, e o
 * {@code webEnvironment} voltaria a {@code MOCK}.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@ExtendWith(OutputCaptureExtension.class)
public abstract class ScenarioIT {

    /** Cliente HTTP real contra a porta aleatória da aplicação. */
    @Autowired
    protected RestTestClient http;

    /**
     * Verificação pelo banco, permitida pelo Princípio I só enquanto o comportamento não tem endpoint
     * (testes de carga do bolt 2). Usa apenas SQL sobre as tabelas do {@code data-model.md}, sem
     * depender das classes de produção. Temporário: sai no T067, quando a listagem existir.
     */
    @Autowired
    protected JdbcTemplate jdbc;

    /** Endpoint de intervalos (FR-017). */
    protected static final String INTERVALS = "/api/producers/intervals";

    /**
     * Compara a resposta de {@code GET /api/producers/intervals} com {@code min} e {@code max} inteiros,
     * como JSON estrito: mesma ordem nas listas e nenhum campo a mais ou a menos.
     */
    protected void assertIntervals(List<IntervalItem> min, List<IntervalItem> max) {
        http.get().uri(INTERVALS).exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json(IntervalItem.json(min, max), JsonCompareMode.STRICT);
    }

    /** Corpo de {@code GET /api/producers/intervals}, para verificações pontuais. */
    protected IntervalsBody intervals() {
        return http.get().uri(INTERVALS).exchange()
                .expectStatus().isOk()
                .expectBody(IntervalsBody.class)
                .returnResult()
                .getResponseBody();
    }
}
