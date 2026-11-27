package com.outsera.awards.aggregation;

import java.util.List;

/** Resposta de {@code GET /api/aggregations/years-with-multiple-winners} (spec 006, FR-001 e FR-003). */
public record YearsWithMultipleWinners(List<YearWinnerCount> years) {
}
