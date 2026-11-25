package com.outsera.awards.security;

import com.outsera.awards.support.ContractValidator;
import com.outsera.awards.support.TokenScenarioIT;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.client.EntityExchangeResult;

/**
 * Com o token ligado, o 401 e as respostas 200 com o token cumprem o contrato publicado, versão 1.2.0
 * (spec 003, FR-026; contracts/openapi-1.2.0.md).
 */
class AuthContractIT extends TokenScenarioIT {

    @ParameterizedTest(name = "S003 FR-026 {0}: 401 e 200 com o token dentro do contrato")
    @ValueSource(strings = {"/api/producers/intervals", "/api/movies"})
    void responsesMatchTheContract(String path) {
        EntityExchangeResult<byte[]> unauthorized = http.get().uri(path).exchange()
                .expectStatus().isUnauthorized().expectBody(byte[].class).returnResult();
        ContractValidator.assertValidGet(path, unauthorized);

        EntityExchangeResult<byte[]> ok = http.get().uri(path).header(HttpHeaders.AUTHORIZATION, bearer())
                .exchange().expectStatus().isOk().expectBody(byte[].class).returnResult();
        ContractValidator.assertValidGet(path, ok);
    }
}
