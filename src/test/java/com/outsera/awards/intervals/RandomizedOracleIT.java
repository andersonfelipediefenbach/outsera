package com.outsera.awards.intervals;

import static org.assertj.core.api.Assertions.assertThat;

import com.outsera.awards.support.IntervalOracle;
import com.outsera.awards.support.IntervalOracle.Generated;
import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Para cada semente fixa, o {@link IntervalOracle} gera um CSV válido com dezenas de produtores e anos
 * entre 1900 e 2100, e a resposta HTTP é comparada, como JSON estrito, com o cálculo independente do
 * oráculo (SC-004, FR-016b). A semente aparece no nome de cada classe, para reproduzir uma falha.
 */
class RandomizedOracleIT {

    /** Um cenário por semente. Cada subclasse aponta {@code movies.csv.path} para o arquivo gerado. */
    abstract static class SeedScenario extends ScenarioIT {

        abstract long seed();

        @Test
        @DisplayName("SC-004 FR-016b arquivo gerado confere com o cálculo independente")
        void matchesOracle() {
            Generated expected = IntervalOracle.generate(seed());
            assertThat(expected.min()).as("o arquivo gerado precisa ter ao menos um intervalo").isNotEmpty();
            assertIntervals(expected.min(), expected.max());
        }
    }

    @Nested
    @DisplayName("semente 20261122")
    class Seed20261122 extends SeedScenario {

        private static final long SEED = 20261122L;

        @DynamicPropertySource
        static void csv(DynamicPropertyRegistry registry) {
            registry.add("movies.csv.path", () -> IntervalOracle.generate(SEED).location());
        }

        @Override
        long seed() {
            return SEED;
        }
    }

    @Nested
    @DisplayName("semente 42")
    class Seed42 extends SeedScenario {

        private static final long SEED = 42L;

        @DynamicPropertySource
        static void csv(DynamicPropertyRegistry registry) {
            registry.add("movies.csv.path", () -> IntervalOracle.generate(SEED).location());
        }

        @Override
        long seed() {
            return SEED;
        }
    }

    @Nested
    @DisplayName("semente 7340033")
    class Seed7340033 extends SeedScenario {

        private static final long SEED = 7340033L;

        @DynamicPropertySource
        static void csv(DynamicPropertyRegistry registry) {
            registry.add("movies.csv.path", () -> IntervalOracle.generate(SEED).location());
        }

        @Override
        long seed() {
            return SEED;
        }
    }
}
