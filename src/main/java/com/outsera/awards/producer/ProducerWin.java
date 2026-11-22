package com.outsera.awards.producer;

/** Projeção (nome do produtor, ano) de uma vitória. Um filme vencedor gera uma por produtor. */
public record ProducerWin(String producer, int year) {
}
