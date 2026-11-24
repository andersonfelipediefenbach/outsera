package com.outsera.awards.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.springframework.http.ETag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.DigestUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * ETag forte derivado do corpo e revalidação por {@code If-None-Match} (spec 002, FR-001 a FR-006;
 * research R4).
 *
 * <p>Só respostas 2xx de GET e HEAD ganham ETag. O valor é {@code "0"} seguido do MD5 do corpo, em
 * hexadecimal, entre aspas: o mesmo corpo tem sempre o mesmo ETag, inclusive entre execuções (FR-002).
 * Quando o {@code If-None-Match} contém o ETag atual (sozinho, numa lista ou na forma fraca) ou
 * {@code *}, a resposta é 304, sem corpo, com o mesmo ETag (FR-003). Respostas de erro passam sem ETag
 * (FR-006).
 *
 * <p>Faz o mesmo que o {@code ShallowEtagHeaderFilter} do Spring, que não trata o {@code *}
 * (RFC 9110, seção 13.1.2).
 */
public class ContentEtagFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (!HttpMethod.GET.matches(request.getMethod()) && !HttpMethod.HEAD.matches(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }
        ContentCachingResponseWrapper wrapper = new ContentCachingResponseWrapper(response);
        chain.doFilter(request, wrapper);

        int status = wrapper.getStatus();
        if (status >= 200 && status < 300) {
            String etag = "\"0" + DigestUtils.md5DigestAsHex(wrapper.getContentAsByteArray()) + "\"";
            wrapper.setHeader(HttpHeaders.ETAG, etag);
            if (notModified(request, etag)) {
                wrapper.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }
        wrapper.copyBodyToResponse();
    }

    /** O {@code If-None-Match} contém {@code *} ou um ETag igual ao atual pela comparação fraca. */
    private static boolean notModified(HttpServletRequest request, String etag) {
        List<String> headers = Collections.list(request.getHeaders(HttpHeaders.IF_NONE_MATCH));
        ETag current = ETag.create(etag);
        for (String header : headers) {
            for (ETag requested : ETag.parse(header)) {
                if (requested.isWildcard() || requested.compare(current, false)) {
                    return true;
                }
            }
        }
        return false;
    }
}
