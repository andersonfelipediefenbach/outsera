package com.outsera.awards.producer;

import java.util.List;

/** Resposta de {@code GET /api/producers/intervals}. As listas nunca são nulas (FR-018). */
public record ProducerIntervalsResponse(List<ProducerInterval> min, List<ProducerInterval> max) {

    public ProducerIntervalsResponse {
        min = List.copyOf(min);
        max = List.copyOf(max);
    }
}
