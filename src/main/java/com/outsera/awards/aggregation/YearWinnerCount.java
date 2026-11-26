package com.outsera.awards.aggregation;

/** Um ano com dois ou mais filmes vencedores (spec 006, FR-001; contrato 1.3.0, {@code YearWinnerCount}). */
public record YearWinnerCount(int year, long winnerCount) {
}
