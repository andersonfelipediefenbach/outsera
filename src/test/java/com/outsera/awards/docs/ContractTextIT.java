package com.outsera.awards.docs;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * Textos do contrato publicado 1.2.1 (spec 005, FR-027): a descrição em duas frases, a autenticação
 * declarada como opcional e nenhum texto com {@code <} ou {@code >}, que o Swagger UI interpreta como HTML
 * e some na renderização (o {@code Bearer <token>} do 1.2.0). O {@code ApiDocsIT} confere que o contrato
 * publicado é idêntico ao da spec 005, e o {@code ApiDocsSecurityIT}, a versão e o {@code security} de cada
 * operação.
 */
class ContractTextIT extends ScenarioIT {

    /** Campos que o Swagger UI renderiza como Markdown. */
    private static final List<String> RENDERED_TEXTS = List.of("description", "summary", "title");

    @SuppressWarnings("unchecked")
    private Map<String, Object> contract() {
        String yaml = http.get().uri("/openapi.yaml").exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        return new Yaml().load(yaml);
    }

    @Test
    @DisplayName("S005 FR-027 info.description em duas frases, sem ETag nem autenticação")
    @SuppressWarnings("unchecked")
    void descriptionHasTwoSentences() {
        String description = ((String) ((Map<String, Object>) contract().get("info")).get("description")).strip();
        assertThat(description.split("(?<=\\.)\\s+"))
                .as("frases de info.description: %s", description)
                .hasSize(2);
        assertThat(description).contains("CSV carregado na inicialização")
                .doesNotContain("ETag", "API_TOKEN", "Bearer", "401");
    }

    @Test
    @DisplayName("S005 FR-027 bearerAuth descrito como opcional")
    @SuppressWarnings("unchecked")
    void bearerAuthIsDescribedAsOptional() {
        Map<String, Object> components = (Map<String, Object>) contract().get("components");
        Map<String, Object> bearer = (Map<String, Object>) ((Map<String, Object>) components.get("securitySchemes"))
                .get("bearerAuth");
        assertThat(bearer).containsEntry("description",
                "Opcional. Exigido só quando a variável API_TOKEN está definida.");
    }

    @Test
    @DisplayName("S005 FR-027 nenhum texto do contrato com < ou >")
    void noAngleBracketsInRenderedTexts() {
        List<String> offending = new ArrayList<>();
        collect(contract(), "", offending);
        assertThat(offending).as("textos com < ou >").isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static void collect(Object node, String path, List<String> offending) {
        if (node instanceof Map<?, ?> map) {
            map.forEach((key, value) -> {
                String child = path + "/" + key;
                if (RENDERED_TEXTS.contains(String.valueOf(key)) && value instanceof String text
                        && (text.contains("<") || text.contains(">"))) {
                    offending.add(child + ": " + text.strip());
                }
                collect(value, child, offending);
            });
        } else if (node instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                collect(list.get(i), path + "[" + i + "]", offending);
            }
        }
    }
}
