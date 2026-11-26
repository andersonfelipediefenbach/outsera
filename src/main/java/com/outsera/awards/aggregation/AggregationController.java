package com.outsera.awards.aggregation;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agregações do painel (spec 006, FR-001 a FR-004; contrato 1.3.0). Sem parâmetros: os desconhecidos são
 * ignorados, como na listagem. O ETag e a autenticação opcional vêm da configuração comum dos endpoints de
 * negócio ({@code EtagConfiguration} e {@code SecurityConfiguration}).
 */
@RestController
@RequestMapping("/api/aggregations")
public class AggregationController {

    private final AggregationService service;

    public AggregationController(AggregationService service) {
        this.service = service;
    }

    @GetMapping(path = "/years-with-multiple-winners", produces = MediaType.APPLICATION_JSON_VALUE)
    public YearsWithMultipleWinners yearsWithMultipleWinners() {
        return service.yearsWithMultipleWinners();
    }

    @GetMapping(path = "/studios-with-win-count", produces = MediaType.APPLICATION_JSON_VALUE)
    public StudiosWithWinCount studiosWithWinCount() {
        return service.studiosWithWinCount();
    }
}
