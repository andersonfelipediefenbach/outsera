package com.outsera.awards.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra o {@link ContentEtagFilter} só nos dois endpoints de negócio (spec 002, FR-001 a FR-006;
 * research R4). Os controllers e o corpo das respostas 200 não mudam (FR-005).
 */
@Configuration(proxyBeanMethods = false)
public class EtagConfiguration {

    @Bean
    public FilterRegistrationBean<ContentEtagFilter> etagFilter() {
        FilterRegistrationBean<ContentEtagFilter> registration =
                new FilterRegistrationBean<>(new ContentEtagFilter());
        registration.addUrlPatterns("/api/producers/intervals", "/api/movies");
        return registration;
    }
}
