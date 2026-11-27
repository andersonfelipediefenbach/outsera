package com.outsera.awards.aggregation;

/** Um estúdio e a quantidade de filmes vencedores em que aparece (spec 006, FR-002; {@code StudioWinCount}). */
public record StudioWinCount(String name, long winCount) {
}
