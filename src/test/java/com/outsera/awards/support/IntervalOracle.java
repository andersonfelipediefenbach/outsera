package com.outsera.awards.support;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Gera, a partir de uma semente fixa, um CSV válido e o resultado esperado de
 * {@code GET /api/producers/intervals} (SC-004, FR-016b).
 *
 * <p>O resultado é calculado direto da lista de linhas geradas, por uma implementação literal das FR-011
 * a FR-015 escrita só aqui, sem reaproveitar código de produção:
 * <ol>
 *   <li>ordena os anos de vitória de cada produtor, mantendo os repetidos;</li>
 *   <li>toma os pares consecutivos dessa lista (dois anos iguais dão intervalo 0);</li>
 *   <li>seleciona todos os pares com o menor e com o maior valor;</li>
 *   <li>ordena por {@code previousWin} e depois pelo nome.</li>
 * </ol>
 *
 * <p>Restrições do gerador: títulos únicos, para não gerar as duplicatas da FR-006d, e nomes sem a
 * palavra {@code and} e sem vírgula ({@code Producer 001}...).
 */
public final class IntervalOracle {

    private static final String[] SEPARATORS = {", ", " and ", ", and "};

    private static final Map<Long, Generated> CACHE = new ConcurrentHashMap<>();

    private IntervalOracle() {
    }

    /** Uma linha gerada. Os produtores de uma linha são sempre distintos. */
    private record Row(int year, String title, List<String> producers, boolean winner) {
    }

    /** O arquivo gerado e o resultado esperado. */
    public record Generated(Path file, List<IntervalItem> min, List<IntervalItem> max) {

        /** Valor para {@code movies.csv.path}. */
        public String location() {
            return file.toUri().toString();
        }
    }

    /** Gera (uma vez por semente, nesta JVM) o arquivo e o resultado esperado. */
    public static Generated generate(long seed) {
        return CACHE.computeIfAbsent(seed, IntervalOracle::create);
    }

    private static Generated create(long seed) {
        Random random = new Random(seed);
        List<String> pool = new ArrayList<>();
        int producerCount = 30 + random.nextInt(21);
        for (int i = 1; i <= producerCount; i++) {
            pool.add("Producer %03d".formatted(i));
        }

        List<Row> rows = new ArrayList<>();
        int movieCount = 120 + random.nextInt(61);
        for (int i = 1; i <= movieCount; i++) {
            rows.add(new Row(randomYear(random), "Filme %04d".formatted(i), pick(random, pool),
                    random.nextInt(100) < 40));
        }
        // Vitórias do mesmo produtor no mesmo ano, em filmes diferentes (FR-013).
        int sameYear = 1 + random.nextInt(3);
        for (int i = 1; i <= sameYear; i++) {
            String producer = pool.get(random.nextInt(pool.size()));
            int year = randomYear(random);
            rows.add(new Row(year, "Filme Mesmo Ano %d A".formatted(i), List.of(producer), true));
            rows.add(new Row(year, "Filme Mesmo Ano %d B".formatted(i), List.of(producer), true));
        }
        Collections.shuffle(rows, random);

        Path file = write(seed, rows, random);
        List<IntervalItem> pairs = neighbouringPairs(rows);
        return new Generated(file, select(pairs, true), select(pairs, false));
    }

    private static int randomYear(Random random) {
        return 1900 + random.nextInt(201);
    }

    private static List<String> pick(Random random, List<String> pool) {
        List<String> copy = new ArrayList<>(pool);
        Collections.shuffle(copy, random);
        return List.copyOf(copy.subList(0, 1 + random.nextInt(3)));
    }

    private static Path write(long seed, List<Row> rows, Random random) {
        StringBuilder csv = new StringBuilder("year;title;studios;producers;winner\n");
        for (Row row : rows) {
            StringBuilder producers = new StringBuilder(row.producers().getFirst());
            for (int i = 1; i < row.producers().size(); i++) {
                producers.append(SEPARATORS[random.nextInt(SEPARATORS.length)]).append(row.producers().get(i));
            }
            csv.append(row.year()).append(';').append(row.title()).append(";Estúdio;")
                    .append(producers).append(';').append(row.winner() ? "yes" : "").append('\n');
        }
        try {
            Path file = Files.createTempFile("oracle-" + seed + "-", ".csv");
            file.toFile().deleteOnExit();
            return Files.writeString(file, csv, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Todos os pares de vitórias vizinhas, de todos os produtores. */
    private static List<IntervalItem> neighbouringPairs(List<Row> rows) {
        Map<String, List<Integer>> winsByProducer = new TreeMap<>();
        for (Row row : rows) {
            if (row.winner()) {
                for (String producer : row.producers()) {
                    winsByProducer.computeIfAbsent(producer, name -> new ArrayList<>()).add(row.year());
                }
            }
        }
        List<IntervalItem> pairs = new ArrayList<>();
        winsByProducer.forEach((producer, years) -> {
            Collections.sort(years);
            for (int i = 0; i + 1 < years.size(); i++) {
                int previous = years.get(i);
                int following = years.get(i + 1);
                pairs.add(new IntervalItem(producer, following - previous, previous, following));
            }
        });
        return pairs;
    }

    private static List<IntervalItem> select(List<IntervalItem> pairs, boolean smallest) {
        if (pairs.isEmpty()) {
            return List.of();
        }
        int target = pairs.getFirst().interval();
        for (IntervalItem pair : pairs) {
            target = smallest ? Math.min(target, pair.interval()) : Math.max(target, pair.interval());
        }
        List<IntervalItem> selected = new ArrayList<>();
        for (IntervalItem pair : pairs) {
            if (pair.interval() == target) {
                selected.add(pair);
            }
        }
        selected.sort(Comparator.comparingInt(IntervalItem::previousWin).thenComparing(IntervalItem::producer));
        return List.copyOf(selected);
    }
}
