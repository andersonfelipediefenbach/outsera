package com.outsera.awards.support;

/**
 * Item de {@code GET /api/movies}, lido do JSON da resposta. Escrito só no teste, sem depender das
 * classes de produção, para que o teste compile antes da implementação.
 */
public record MovieJson(int year, String title, String studios, String producers, boolean winner) {
}
