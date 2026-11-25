package com.outsera.awards.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * 401 em Problem Details (RFC 9457), o mesmo para o token ausente e para o token errado, sem revelar o
 * valor esperado nem o recebido (spec 003, FR-023 e FR-025; contracts/operational-endpoints.md). Os
 * campos são os do {@code ProblemDetail} do contrato: {@code type}, {@code title}, {@code status},
 * {@code detail} e {@code instance}.
 */
public class ProblemAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String DETAIL = "Token ausente ou inválido";
    static final String CHALLENGE = "Bearer realm=\"awards\"";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        ObjectNode problem = JSON.createObjectNode()
                .put("type", "about:blank")
                .put("title", HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .put("status", HttpStatus.UNAUTHORIZED.value())
                .put("detail", DETAIL)
                .put("instance", request.getRequestURI());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, CHALLENGE);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getOutputStream().write(JSON.writeValueAsBytes(problem));
    }
}
