package com.outsera.awards.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Autentica a requisição pelo cabeçalho {@code Authorization: Bearer <token>} (spec 003, FR-020 e
 * FR-021; research R11).
 *
 * <p><strong>Comparação em tempo constante</strong> (decisão de design da research R11, ADR 0017): o
 * valor recebido e o esperado passam por SHA-256, em UTF-8, e os dois resumos, sempre com 32 bytes, são
 * comparados por {@link MessageDigest#isEqual(byte[], byte[])}, que confere todos os bytes sem sair no
 * primeiro diferente. O tempo não depende do tamanho nem do conteúdo do token recebido.
 *
 * <p>Com o token certo, a requisição fica autenticada como o cliente fixo {@code api-client}, sem papéis.
 * Sem o cabeçalho, ou com outro valor, o filtro não faz nada, e a autorização nega com 401. O cabeçalho
 * nunca é registrado no log (FR-025).
 */
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";
    private static final String CLIENT = "api-client";

    private final byte[] expectedDigest;

    public TokenAuthenticationFilter(String expectedToken) {
        this.expectedDigest = sha256(expectedToken);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIX)
                && MessageDigest.isEqual(sha256(header.substring(PREFIX.length())), expectedDigest)) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(CLIENT, null, List.of()));
            SecurityContextHolder.setContext(context);
        }
        chain.doFilter(request, response);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }
}
