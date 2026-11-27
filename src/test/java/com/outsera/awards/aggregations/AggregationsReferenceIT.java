package com.outsera.awards.aggregations;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.ScenarioIT;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;

/**
 * As duas agregações com o arquivo fornecido (spec 006, FR-001 e FR-002; US4-1 e US4-2). A lista completa dos
 * estúdios é comparada com um oráculo calculado aqui, direto do {@code movielist.csv}, sem passar pelo código
 * de produção.
 */
class AggregationsReferenceIT extends ScenarioIT {

    static final String YEARS = "/api/aggregations/years-with-multiple-winners";
    static final String STUDIOS = "/api/aggregations/studios-with-win-count";

    private static final Path PROVIDED_FILE = Path.of("src", "main", "resources", "movielist.csv");

    @Test
    @DisplayName("S006 FR-001 US4-1 anos com mais de um vencedor no arquivo fornecido: 1986, 1990 e 2015")
    void yearsWithMultipleWinners() {
        http.get().uri(YEARS).exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody().json("""
                        {"years":[{"year":1986,"winnerCount":2},{"year":1990,"winnerCount":2},
                        {"year":2015,"winnerCount":2}]}""", JsonCompareMode.STRICT);
    }

    @Test
    @DisplayName("S006 FR-002 US4-2 estúdios do arquivo fornecido começam por Columbia, Paramount e Warner")
    void topStudios() {
        List<StudioCount> studios = studios();
        assertThat(studios).hasSizeGreaterThanOrEqualTo(5);
        assertThat(studios.subList(0, 5)).containsExactly(
                new StudioCount("Columbia Pictures", 7),
                new StudioCount("Paramount Pictures", 6),
                new StudioCount("Warner Bros.", 5),
                new StudioCount("20th Century Fox", 4),
                new StudioCount("MGM", 3));
    }

    @Test
    @DisplayName("S006 FR-002 lista completa de estúdios igual ao oráculo calculado do arquivo fornecido")
    void allStudiosMatchTheOracle() throws IOException {
        assertThat(studios()).containsExactlyElementsOf(oracle());
    }

    private List<StudioCount> studios() {
        StudiosBody body = http.get().uri(STUDIOS).exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(StudiosBody.class).returnResult().getResponseBody();
        assertThat(body).isNotNull();
        return body.studios().stream().map(s -> new StudioCount(s.name(), s.winCount())).toList();
    }

    /** Separação por vírgula, sem espaços nas pontas, sem vazios, um conjunto por filme (data-model). */
    private static List<StudioCount> oracle() throws IOException {
        List<String> lines = Files.readAllLines(PROVIDED_FILE, StandardCharsets.UTF_8);
        Map<String, Long> counts = new TreeMap<>();
        for (String line : lines.subList(1, lines.size())) {
            String[] fields = line.replace("﻿", "").split(";", -1);
            if (fields.length < 5 || !fields[4].strip().equalsIgnoreCase("yes")) {
                continue;
            }
            Set<String> names = new LinkedHashSet<>();
            for (String part : fields[2].split(",")) {
                if (!part.strip().isEmpty()) {
                    names.add(part.strip());
                }
            }
            names.forEach(name -> counts.merge(name, 1L, Long::sum));
        }
        List<StudioCount> result = new ArrayList<>();
        counts.forEach((name, count) -> result.add(new StudioCount(name, count)));
        result.sort(Comparator.comparingLong(StudioCount::winCount).reversed()
                .thenComparing(StudioCount::name));
        return result;
    }

    record StudioCount(String name, long winCount) {
    }

    record StudiosBody(List<StudioCount> studios) {
    }
}
