package com.outsera.awards.producer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Resultado dos intervalos guardado entre as leituras, invalidado a cada gravação de filme (spec 004,
 * SLO de latência; docs/ai/decisoes.md, "Intervalos guardados entre as leituras"). O resultado só muda
 * quando os dados mudam, e calculá-lo a cada chamada custava centenas de milissegundos com 100 mil filmes.
 *
 * <p><strong>Consistência imediata:</strong> toda gravação chama {@link #registerWrite()} dentro da sua
 * transação. A partir daí, até o fim da transação (commit ou rollback), há uma gravação em andamento: a
 * leitura vai direto ao banco e não guarda o resultado, então vê exatamente o que o banco mostra. Uma
 * geração avança no começo e no fim de cada gravação. Fora de uma gravação, a leitura serve o resultado
 * guardado só se ele é da geração atual, e só guarda um resultado novo se nenhuma gravação começou ou
 * terminou durante o cálculo. Assim, depois de uma gravação confirmada, nenhuma leitura recebe um resultado
 * anterior a ela.
 *
 * <p><strong>Uma única instância:</strong> o estado fica em memória, e só as gravações desta instância o
 * atualizam. Com várias instâncias (Marco 5, Cloud Run), a invalidação precisa passar a um contador de
 * versão no banco ou a um evento compartilhado (ADR 0026). Não exige mudança no esquema: uma tabela de
 * versão mudaria o histórico do Flyway que o {@code SchemaPostgresIT} confere.
 */
@Component
public class ProducerIntervalsCache {

    private static final Object TRANSACTION_KEY = new Object();

    private final AtomicLong generation = new AtomicLong();
    private final AtomicInteger writesInProgress = new AtomicInteger();
    private final AtomicReference<Cached> cached = new AtomicReference<>();

    private record Cached(long generation, ProducerIntervalsResponse response) {
    }

    /**
     * Marca uma gravação em andamento até o fim da transação corrente. Chamado uma vez ou mais por
     * transação de gravação; só a primeira chamada vale.
     */
    public void registerWrite() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            generation.incrementAndGet();
            return;
        }
        if (TransactionSynchronizationManager.hasResource(TRANSACTION_KEY)) {
            return;
        }
        TransactionSynchronizationManager.bindResource(TRANSACTION_KEY, Boolean.TRUE);
        // A ordem importa: a leitura confere a geração antes de conferir as gravações em andamento.
        writesInProgress.incrementAndGet();
        generation.incrementAndGet();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                TransactionSynchronizationManager.unbindResourceIfPossible(TRANSACTION_KEY);
                writesInProgress.decrementAndGet();
                generation.incrementAndGet();
            }
        });
    }

    /** O resultado guardado, se ainda vale, ou o calculado agora por {@code compute}. */
    ProducerIntervalsResponse get(Supplier<ProducerIntervalsResponse> compute) {
        long current = generation.get();
        boolean quiet = writesInProgress.get() == 0;
        Cached hit = cached.get();
        if (quiet && hit != null && hit.generation() == current) {
            return hit.response();
        }
        ProducerIntervalsResponse response = compute.get();
        if (quiet && writesInProgress.get() == 0 && generation.get() == current) {
            Cached fresh = new Cached(current, response);
            cached.accumulateAndGet(fresh, (old, candidate) ->
                    old == null || candidate.generation() >= old.generation() ? candidate : old);
        }
        return response;
    }
}
