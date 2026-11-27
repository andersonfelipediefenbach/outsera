package com.outsera.awards.aggregation;

import com.outsera.awards.movie.MovieRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Agregações de leitura para o painel do front (spec 006, FR-001 a FR-003; research R6). Sem cache: com o volume
 * do arquivo, as duas contas custam pouco e acompanham a base mesmo quando ela muda depois da subida (modo
 * Kafka). O cache do ADR 0026 continua só nos intervalos.
 */
@Service
public class AggregationService {

    /** Quantidade decrescente e depois o nome, na mesma ordem de nome dos intervalos ({@code String.compareTo}). */
    private static final Comparator<StudioWinCount> STUDIO_ORDER = Comparator
            .comparingLong(StudioWinCount::winCount).reversed()
            .thenComparing(StudioWinCount::name);

    private final MovieRepository movies;

    public AggregationService(MovieRepository movies) {
        this.movies = movies;
    }

    /** Anos com dois ou mais vencedores, em ordem crescente (FR-001). */
    @Transactional(readOnly = true)
    public YearsWithMultipleWinners yearsWithMultipleWinners() {
        List<YearWinnerCount> years = movies.countWinnersByYearWithMoreThanOne().stream()
                .map(row -> new YearWinnerCount(row.getYear(), row.getWinnerCount()))
                .toList();
        return new YearsWithMultipleWinners(years);
    }

    /** Estúdios com pelo menos uma vitória, contados uma vez por filme vencedor (FR-002). */
    @Transactional(readOnly = true)
    public StudiosWithWinCount studiosWithWinCount() {
        Map<String, Long> counts = new HashMap<>();
        for (String studios : movies.findWinnerStudios()) {
            for (String name : StudioNameParser.parse(studios)) {
                counts.merge(name, 1L, Long::sum);
            }
        }
        List<StudioWinCount> result = counts.entrySet().stream()
                .map(entry -> new StudioWinCount(entry.getKey(), entry.getValue()))
                .sorted(STUDIO_ORDER)
                .toList();
        return new StudiosWithWinCount(result);
    }
}
