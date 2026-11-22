package com.outsera.awards.producer;

/**
 * Intervalo entre duas vitórias vizinhas de um produtor. Tem só os 4 campos do enunciado (Princípio
 * III, FR-017): nenhum campo pode ser adicionado, renomeado ou removido.
 */
public record ProducerInterval(String producer, int interval, int previousWin, int followingWin) {
}
