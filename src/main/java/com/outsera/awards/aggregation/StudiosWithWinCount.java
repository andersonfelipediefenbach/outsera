package com.outsera.awards.aggregation;

import java.util.List;

/** Resposta de {@code GET /api/aggregations/studios-with-win-count} (spec 006, FR-002 e FR-003). */
public record StudiosWithWinCount(List<StudioWinCount> studios) {
}
