package com.outsera.awards.adversario;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

/**
 * Caso 09 do testador: anos 0 e acima de 32 bits.
 *
 * <p>O esperado diverge do testador por causa da faixa de ano da sessão 2026-11-22 (FR-006: de 1 a
 * 9999). Os anos 0, 2147483647, 2147483648 e 4294967296 são rejeitados, e só sobram os anos 1 e 2, de
 * produtores diferentes, então {@code min} e {@code max} vêm vazios. O ajuste está só na cópia de
 * {@code src/test/resources}; o original do testador fica intacto em {@code docs/ai/adversario/}.
 */
@TestPropertySource(properties = "movies.csv.path=classpath:" + AdversarialCaseIT.DIR + Case09ExtremeYearsIT.CASE + ".csv")
class Case09ExtremeYearsIT extends AdversarialCaseIT {

    static final String CASE = "09-anos-e-intervalo-extremos";

    @Test
    @DisplayName("Adversário 09 FR-006 FR-016b anos e intervalos extremos")
    void matchesExpected() {
        assertMatchesExpected(CASE);
    }
}
