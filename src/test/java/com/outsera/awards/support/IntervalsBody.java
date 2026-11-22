package com.outsera.awards.support;

import java.util.List;
import java.util.stream.Stream;

/** Corpo de {@code GET /api/producers/intervals} lido pelo teste, para verificações pontuais. */
public record IntervalsBody(List<IntervalItem> min, List<IntervalItem> max) {

    /** Todos os itens das duas listas. */
    public Stream<IntervalItem> all() {
        return Stream.concat(min.stream(), max.stream());
    }
}
