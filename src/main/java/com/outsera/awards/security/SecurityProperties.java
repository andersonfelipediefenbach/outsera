package com.outsera.awards.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Token da autenticação ({@code awards.security.token}, variável {@code API_TOKEN}; spec 003, FR-019).
 * Vazio ou ausente desliga a autenticação.
 *
 * @param token valor esperado no cabeçalho {@code Authorization: Bearer}
 */
@ConfigurationProperties("awards.security")
public record SecurityProperties(String token) {

    /** A autenticação está ligada: o token foi definido e não é vazio. */
    public boolean enabled() {
        return token != null && !token.isEmpty();
    }

    /** Nunca mostra o valor (FR-025). */
    @Override
    public String toString() {
        return "SecurityProperties[enabled=" + enabled() + "]";
    }
}
