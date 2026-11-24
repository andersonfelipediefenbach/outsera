package com.outsera.awards.support;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lê o texto exposto em {@code /actuator/prometheus} como uma lista de amostras (spec 002, FR-012 a
 * FR-016). Linhas de comentário ({@code # HELP}, {@code # TYPE}) ficam de fora; use {@link #hasType} para
 * conferir o tipo de uma métrica.
 */
public final class PrometheusText {

    /** Uma amostra: nome da série, rótulos e valor. */
    public record Sample(String name, Map<String, String> labels, double value) {

        public String label(String key) {
            return labels.get(key);
        }
    }

    private static final Pattern SAMPLE = Pattern.compile("^([a-zA-Z_:][a-zA-Z0-9_:]*)(\\{(.*)})?\\s+(\\S+)(\\s+\\S+)?$");
    private static final Pattern LABEL = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)=\"((?:[^\"\\\\]|\\\\.)*)\"");

    private PrometheusText() {
    }

    /** Todas as amostras do texto. */
    public static List<Sample> parse(String text) {
        return text.lines()
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .map(PrometheusText::sample)
                .toList();
    }

    /** Amostras com o nome dado que cumprem o filtro de rótulos. */
    public static List<Sample> find(List<Sample> samples, String name, Predicate<Sample> filter) {
        return samples.stream().filter(s -> s.name().equals(name)).filter(filter).toList();
    }

    /** Soma dos valores das amostras com o nome dado que cumprem o filtro. */
    public static double sum(List<Sample> samples, String name, Predicate<Sample> filter) {
        return find(samples, name, filter).stream().mapToDouble(Sample::value).sum();
    }

    /** O texto declara {@code # TYPE <metric> <type>}. */
    public static boolean hasType(String text, String metric, String type) {
        return text.lines().anyMatch(line -> line.equals("# TYPE " + metric + " " + type));
    }

    private static Sample sample(String line) {
        Matcher matcher = SAMPLE.matcher(line.strip());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("linha do Prometheus não reconhecida: " + line);
        }
        Map<String, String> labels = new LinkedHashMap<>();
        if (matcher.group(3) != null) {
            Matcher label = LABEL.matcher(matcher.group(3));
            while (label.find()) {
                labels.put(label.group(1), label.group(2).replace("\\\"", "\"").replace("\\\\", "\\"));
            }
        }
        return new Sample(matcher.group(1), labels, parseValue(matcher.group(4)));
    }

    private static double parseValue(String value) {
        return switch (value) {
            case "+Inf" -> Double.POSITIVE_INFINITY;
            case "-Inf" -> Double.NEGATIVE_INFINITY;
            case "NaN" -> Double.NaN;
            default -> Double.parseDouble(value);
        };
    }
}
