package com.outsera.awards.producer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Calcula os intervalos entre vitórias vizinhas de cada produtor (FR-011 a FR-016, research R6). O
 * resultado vem só dos dados carregados, sem nenhum valor fixo (FR-016a), e é recalculado a cada
 * chamada.
 */
@Service
public class ProducerIntervalService {

    /** Ordem de {@code min} e {@code max}: {@code previousWin} crescente e depois o nome (FR-015). */
    private static final Comparator<ProducerInterval> ORDER = Comparator
            .comparingInt(ProducerInterval::previousWin)
            .thenComparing(ProducerInterval::producer);

    private final ProducerRepository producers;

    public ProducerIntervalService(ProducerRepository producers) {
        this.producers = producers;
    }

    @Transactional(readOnly = true)
    public ProducerIntervalsResponse intervals() {
        List<ProducerInterval> intervals = neighbouringIntervals();
        if (intervals.isEmpty()) {
            return new ProducerIntervalsResponse(List.of(), List.of());
        }
        int min = intervals.stream().mapToInt(ProducerInterval::interval).min().orElseThrow();
        int max = intervals.stream().mapToInt(ProducerInterval::interval).max().orElseThrow();
        return new ProducerIntervalsResponse(withValue(intervals, min), withValue(intervals, max));
    }

    /** Um intervalo por par de vitórias vizinhas, depois de ordenar os anos de cada produtor (FR-012). */
    private List<ProducerInterval> neighbouringIntervals() {
        Map<String, List<Integer>> yearsByProducer = new HashMap<>();
        for (ProducerWin win : producers.findWins()) {
            yearsByProducer.computeIfAbsent(win.producer(), name -> new ArrayList<>()).add(win.year());
        }
        List<ProducerInterval> intervals = new ArrayList<>();
        yearsByProducer.forEach((producer, years) -> {
            years.sort(Comparator.naturalOrder());
            for (int i = 1; i < years.size(); i++) {
                int previous = years.get(i - 1);
                int following = years.get(i);
                // O mesmo ano dá intervalo 0 (FR-013).
                intervals.add(new ProducerInterval(producer, following - previous, previous, following));
            }
        });
        return intervals;
    }

    /** Todos os intervalos com o valor dado, inclusive os empatados (FR-014), na ordem da FR-015. */
    private static List<ProducerInterval> withValue(List<ProducerInterval> intervals, int value) {
        return intervals.stream()
                .filter(interval -> interval.interval() == value)
                .sorted(ORDER)
                .toList();
    }
}
