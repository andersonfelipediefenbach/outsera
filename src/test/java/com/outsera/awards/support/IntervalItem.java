package com.outsera.awards.support;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Item de {@code min} ou {@code max} como o teste espera recebê-lo. Os valores são escritos à mão em
 * cada cenário (ou calculados pelo {@link IntervalOracle}), sem usar as classes de produção, para que o
 * teste compile antes da implementação e não dependa dela.
 */
public record IntervalItem(String producer, int interval, int previousWin, int followingWin) {

    public static IntervalItem item(String producer, int interval, int previousWin, int followingWin) {
        return new IntervalItem(producer, interval, previousWin, followingWin);
    }

    /** Corpo JSON esperado de {@code GET /api/producers/intervals}. */
    public static String json(List<IntervalItem> min, List<IntervalItem> max) {
        return "{\"min\":" + array(min) + ",\"max\":" + array(max) + "}";
    }

    private static String array(List<IntervalItem> items) {
        return items.stream().map(IntervalItem::toJson).collect(Collectors.joining(",", "[", "]"));
    }

    private String toJson() {
        return "{\"producer\":\"%s\",\"interval\":%d,\"previousWin\":%d,\"followingWin\":%d}"
                .formatted(escape(producer), interval, previousWin, followingWin);
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
