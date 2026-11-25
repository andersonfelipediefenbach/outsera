package com.outsera.awards.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * Cadeia de segurança sem estado (spec 003, FR-019 a FR-023 e FR-022a; research R11 e R12;
 * contracts/operational-endpoints.md).
 *
 * <ul>
 *   <li>Sem CSRF, sem {@code formLogin}, sem {@code httpBasic}, sem sessão.</li>
 *   <li>Token desligado: tudo liberado, sem filtro de token.</li>
 *   <li>Token ligado: os intervalos, a listagem e as métricas exigem o token, em qualquer método. O resto
 *       (probes, saúde geral, Swagger UI, contrato, webjars, {@code /error} e caminhos inexistentes)
 *       continua público.</li>
 *   <li>Cabeçalhos: o {@code Cache-Control} do Spring Security fica fora dos endpoints de negócio, para
 *       não anular o ETag. {@code X-Content-Type-Options} e {@code X-Frame-Options} ficam em todas as
 *       respostas, por decisão registrada (ADR 0017).</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
public class SecurityConfiguration {

    private static final RequestMatcher BUSINESS = new OrRequestMatcher(
            PathPatternRequestMatcher.withDefaults().matcher("/api/producers/intervals"),
            PathPatternRequestMatcher.withDefaults().matcher("/api/movies"));

    private static final RequestMatcher PROTECTED = new OrRequestMatcher(
            BUSINESS, PathPatternRequestMatcher.withDefaults().matcher("/actuator/prometheus"));

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties properties) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .cacheControl(cache -> cache.disable())
                        .addHeaderWriter(cacheControlOutsideBusinessResponses())
                        .frameOptions(frame -> frame.deny()));

        if (!properties.enabled()) {
            http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
            return http.build();
        }

        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PROTECTED).authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(new TokenAuthenticationFilter(properties.token()), AuthorizationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new ProblemAuthenticationEntryPoint()));
        return http.build();
    }

    /**
     * Os cabeçalhos de cache do Spring Security ({@code Cache-Control}, {@code Pragma} e {@code Expires})
     * em toda resposta, menos nas dos endpoints de negócio, que respondem sem eles, como no Marco 2, para que
     * o ETag e o 304 continuem úteis ao cliente. O 401 sempre os recebe. Os cabeçalhos são escritos quando a
     * resposta é confirmada, então o status já é conhecido.
     */
    private static HeaderWriter cacheControlOutsideBusinessResponses() {
        CacheControlHeadersWriter cacheControl = new CacheControlHeadersWriter();
        return (request, response) -> {
            if (!BUSINESS.matches(request) || response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED) {
                cacheControl.writeHeaders(request, response);
            }
        };
    }
}
