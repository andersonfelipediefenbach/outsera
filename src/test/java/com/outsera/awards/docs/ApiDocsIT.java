package com.outsera.awards.docs;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.yaml.snakeyaml.Yaml;

/**
 * Documentação interativa em {@code /swagger-ui.html}, servida a partir do contrato estático
 * {@code /openapi.yaml}, que é idêntico ao de {@code specs/} (FR-024, FR-025, research R8).
 */
class ApiDocsIT extends ScenarioIT {

    private static final String SWAGGER_UI = "/swagger-ui.html";
    private static final String CONTRACT = "/openapi.yaml";
    /** Contrato 1.2.1 (spec 005, FR-027). Os das specs 001 a 003 ficam como registro. */
    private static final Path SPEC_CONTRACT = Path.of("specs", "005-deploy-cloud-run", "contracts", "openapi.yaml");

    @Test
    @DisplayName("FR-024 Swagger UI disponível")
    void swaggerUiIsAvailable() {
        EntityExchangeResult<byte[]> page = getFollowingRedirects(SWAGGER_UI);
        assertThat(page.getStatus().value()).isEqualTo(200);
        assertThat(page.getResponseHeaders().getContentType()).isNotNull()
                .satisfies(type -> assertThat(type.isCompatibleWith(MediaType.TEXT_HTML)).isTrue());
        assertThat(text(page)).contains("swagger-ui");
    }

    @Test
    @DisplayName("FR-024 Swagger UI aponta para /openapi.yaml")
    void swaggerUiPointsToTheStaticContract() {
        String page = text(getFollowingRedirects(SWAGGER_UI));
        assertThat(page).containsPattern("url:\\s*\"" + Pattern.quote(CONTRACT) + "\"");

        // Os arquivos do Swagger UI referenciados pela página existem: sem eles, a página fica em branco.
        Matcher assets = Pattern.compile("(?:src|href)=\"(/webjars/[^\"]+)\"").matcher(page);
        int found = 0;
        while (assets.find()) {
            found++;
            assertThat(getFollowingRedirects(assets.group(1)).getStatus().value())
                    .as("GET %s", assets.group(1))
                    .isEqualTo(200);
        }
        assertThat(found).as("arquivos do webjar referenciados pela página").isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("FR-024 a geração automática do contrato está desligada")
    void generatedContractIsDisabled() {
        http.get().uri("/v3/api-docs").exchange().expectStatus().isNotFound();
    }

    @Test
    @DisplayName("FR-024 FR-025 contrato publicado idêntico ao da spec")
    void publishedContractIsTheSpecContract() throws IOException {
        byte[] published = http.get().uri(CONTRACT).exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .returnResult()
                .getResponseBody();
        assertThat(published).isEqualTo(Files.readAllBytes(basedir().resolve(SPEC_CONTRACT)));
    }

    @Test
    @DisplayName("FR-024 contrato descreve os dois endpoints")
    @SuppressWarnings("unchecked")
    void contractDescribesBothEndpoints() {
        String yaml = text(http.get().uri(CONTRACT).exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .returnResult());
        Map<String, Object> paths = (Map<String, Object>) new Yaml().<Map<String, Object>>load(yaml).get("paths");
        assertThat(paths).containsOnlyKeys("/api/producers/intervals", "/api/movies");

        for (String path : paths.keySet()) {
            Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get(path)).get("get");
            assertThat(get).as("GET %s", path).containsKeys("summary", "responses");
            assertThat(yamlText(get.get("responses"))).as("exemplos de GET %s", path).contains("example");
        }
        Map<String, Object> movies = (Map<String, Object>) ((Map<String, Object>) paths.get("/api/movies")).get("get");
        assertThat((List<Map<String, Object>>) movies.get("parameters"))
                .extracting(p -> p.containsKey("$ref") ? p.get("$ref") : p.get("name"))
                .containsExactly("year", "winner", "page", "size", "#/components/parameters/IfNoneMatch");
    }

    @Test
    @DisplayName("FR-007 contrato declara ETag, If-None-Match e 304 nos dois endpoints")
    @SuppressWarnings("unchecked")
    void contractDeclaresConditionalRequests() {
        String yaml = text(http.get().uri(CONTRACT).exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .returnResult());
        Map<String, Object> root = new Yaml().load(yaml);
        Map<String, Object> paths = (Map<String, Object>) root.get("paths");
        for (String path : paths.keySet()) {
            Map<String, Object> get = (Map<String, Object>) ((Map<String, Object>) paths.get(path)).get("get");
            Map<String, Object> responses = (Map<String, Object>) get.get("responses");
            assertThat(responses).as("respostas de GET %s", path).containsKey("304");
            assertThat(yamlText(((Map<String, Object>) responses.get("200")).get("headers")))
                    .as("cabeçalhos do 200 de GET %s", path).contains("ETag");
            assertThat((List<Map<String, Object>>) get.get("parameters"))
                    .as("parâmetros de GET %s", path)
                    .anySatisfy(p -> assertThat(p.get("$ref")).isEqualTo("#/components/parameters/IfNoneMatch"));
        }
        Map<String, Object> ifNoneMatch = (Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>)
                root.get("components")).get("parameters")).get("IfNoneMatch");
        assertThat(ifNoneMatch).containsEntry("name", "If-None-Match").containsEntry("in", "header");
    }

    /** GET que segue redirecionamentos, para não depender de como a página do Swagger UI é servida. */
    private EntityExchangeResult<byte[]> getFollowingRedirects(String uri) {
        String current = uri;
        for (int hops = 0; hops < 5; hops++) {
            EntityExchangeResult<byte[]> result = http.get().uri(current).exchange()
                    .expectBody(byte[].class)
                    .returnResult();
            if (!result.getStatus().is3xxRedirection()) {
                return result;
            }
            String location = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
            assertThat(location).as("Location do redirecionamento de %s", current).isNotNull();
            URI target = URI.create(location);
            current = target.getRawPath() + (target.getRawQuery() == null ? "" : "?" + target.getRawQuery());
        }
        throw new AssertionError("redirecionamentos demais a partir de " + uri);
    }

    private static String text(EntityExchangeResult<byte[]> result) {
        assertThat(result.getStatus().value()).as("status de %s", result.getUrl()).isEqualTo(200);
        return new String(result.getResponseBody(), StandardCharsets.UTF_8);
    }

    private static String yamlText(Object node) {
        return new Yaml().dump(node);
    }

    /** Raiz do projeto: a propriedade {@code basedir} que o Failsafe define, ou o diretório atual. */
    private static Path basedir() {
        return Path.of(System.getProperty("basedir", System.getProperty("user.dir")));
    }
}
