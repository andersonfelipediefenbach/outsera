package com.outsera.awards.docs;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * O contrato publicado é o 1.2.1 (spec 005, FR-027), com a autenticação opcional e a resposta 401 nos
 * dois endpoints de negócio (spec 003, FR-026; contracts/openapi-1.2.0.md). O {@code ApiDocsIT} continua
 * conferindo que o contrato publicado é idêntico ao da spec.
 */
class ApiDocsSecurityIT extends ScenarioIT {

    @SuppressWarnings("unchecked")
    private Map<String, Object> contract() {
        String yaml = http.get().uri("/openapi.yaml").exchange().expectStatus().isOk()
                .expectBody(String.class).returnResult().getResponseBody();
        return new Yaml().load(yaml);
    }

    @Test
    @DisplayName("S003 FR-026 contrato 1.2.1 com bearerAuth opcional e 401 nos endpoints de negócio")
    @SuppressWarnings("unchecked")
    void contractDocumentsTheAuthentication() {
        Map<String, Object> root = contract();
        assertThat((Map<String, Object>) root.get("info")).containsEntry("version", "1.2.1");
        Map<String, Object> schemes = (Map<String, Object>) ((Map<String, Object>) root.get("components"))
                .get("securitySchemes");
        assertThat((Map<String, Object>) schemes.get("bearerAuth"))
                .containsEntry("type", "http")
                .containsEntry("scheme", "bearer");

        Map<String, Object> paths = (Map<String, Object>) root.get("paths");
        for (String path : List.of("/api/producers/intervals", "/api/movies")) {
            Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get(path)).get("get");
            assertThat((List<Object>) get.get("security")).as("security de %s", path)
                    .containsExactly(Map.of(), Map.of("bearerAuth", List.of()));
            Map<String, Object> unauthorized = (Map<String, Object>) ((Map<String, Object>) get.get("responses"))
                    .get("401");
            assertThat(unauthorized).containsEntry("$ref", "#/components/responses/Unauthorized");
        }
    }
}
