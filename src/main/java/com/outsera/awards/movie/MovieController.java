package com.outsera.awards.movie;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * {@code GET /api/movies} (FR-019 a FR-023). Os parâmetros chegam como texto e são validados no
 * {@link MovieQueryService}. Parâmetros desconhecidos são ignorados.
 */
@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieQueryService service;

    public MovieController(MovieQueryService service) {
        this.service = service;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public MoviePage list(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String winner,
            @RequestParam(required = false) String page,
            @RequestParam(required = false) String size) {
        return service.list(year, winner, page, size);
    }
}
