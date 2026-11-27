package com.outsera.awards.aggregations;

import com.outsera.awards.support.ScenarioIT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.json.JsonCompareMode;

/**
 * Vitórias por estúdio (spec 006, FR-002; data-model, "Regras de separação dos estúdios"), com o
 * {@code aggregations/studios.csv}. Coluna de estúdios de cada vencedor:
 *
 * <ul>
 *   <li>{@code A, B}, {@code A,B}, {@code B ,C} e {@code A,,C}: vírgula com e sem espaço, e parte vazia;</li>
 *   <li>{@code A, A}: o mesmo estúdio repetido no filme conta uma vez;</li>
 *   <li>vazio: não conta para ninguém;</li>
 *   <li>{@code Mgm} e {@code MGM}: estúdios diferentes, sem normalizar a grafia;</li>
 *   <li>{@code Delta} e {@code Charlie}: empatados com 1, ordenados pelo nome;</li>
 *   <li>{@code Z}, num indicado: fica de fora.</li>
 * </ul>
 *
 * Resposta esperada: A 4, B 3, C 2 e, com 1, Charlie, Delta, MGM e Mgm ({@code String.compareTo}: {@code G}
 * maiúsculo vem antes de {@code g}).
 */
@TestPropertySource(properties = "movies.csv.path=classpath:scenarios/aggregations/studios.csv")
class StudiosWinCountIT extends ScenarioIT {

    @Test
    @DisplayName("S006 FR-002 separação por vírgula, um por filme, sem vazios, sem normalizar e empate pelo nome")
    void studiosAreSplitCountedAndOrdered() {
        http.get().uri(AggregationsReferenceIT.STUDIOS).exchange()
                .expectStatus().isOk()
                .expectBody().json("""
                        {"studios":[
                          {"name":"A","winCount":4},
                          {"name":"B","winCount":3},
                          {"name":"C","winCount":2},
                          {"name":"Charlie","winCount":1},
                          {"name":"Delta","winCount":1},
                          {"name":"MGM","winCount":1},
                          {"name":"Mgm","winCount":1}
                        ]}""", JsonCompareMode.STRICT);
    }
}
