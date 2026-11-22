package com.outsera.awards.producer;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** {@code GET /api/producers/intervals} (FR-017). */
@RestController
@RequestMapping("/api/producers")
public class ProducerIntervalController {

    private final ProducerIntervalService service;

    public ProducerIntervalController(ProducerIntervalService service) {
        this.service = service;
    }

    @GetMapping(path = "/intervals", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProducerIntervalsResponse intervals() {
        return service.intervals();
    }
}
