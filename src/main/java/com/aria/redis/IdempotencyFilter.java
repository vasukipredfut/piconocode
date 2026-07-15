package com.aria.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Servlet filter that implements the Idempotency-Key contract (REARCH ADR-007).
 *
 * Only acts on POSTs that carry an {@code Idempotency-Key} header — leaves every
 * other request untouched. On a cache hit we replay the original 2xx response
 * verbatim, so retried network calls cannot create duplicate side effects
 * (the canonical IRN / payment / work-order-release problem in M1).
 *
 * Scope (intentionally narrow for v1):
 *   - We only cache 2xx responses; 4xx/5xx are left to retry naturally.
 *   - Path-scoped: key = "{path}::{header}" so the same key against /api/foo and
 *     /api/bar can't collide.
 *   - 24h TTL — long enough to cover client back-off + manual retries, short
 *     enough that key reuse for a *new* logical request the next day is fine.
 *
 * To opt a controller method in there's nothing to do: just have the client
 * send the header. The classic demonstration is POST {@code /api/saveHsn}
 * twice with the same key — the second call returns the first call's body
 * and no second {@code hsn_master} row is inserted.
 */
@Configuration
public class IdempotencyFilter {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyFilter.class);
    private static final String HEADER = "Idempotency-Key";

    @Autowired
    private IdempotencyService idempotencyService;

    @Bean
    public FilterRegistrationBean<Filter> idempotencyFilterRegistration() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter(coreFilter());
        // Run before the auth filter does heavy lifting? No — we want auth to have
        // already vetted the caller. Use a high order so we sit *after* security.
        reg.setOrder(100);
        reg.addUrlPatterns("/api/*");
        reg.setName("idempotencyFilter");
        return reg;
    }

    private Filter coreFilter() {
        return new Filter() {
            @Override
            public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
                    throws IOException, ServletException {
                HttpServletRequest http = (HttpServletRequest) req;
                HttpServletResponse httpRes = (HttpServletResponse) res;

                String key = http.getHeader(HEADER);
                boolean applicable = key != null && !key.isEmpty()
                        && "POST".equalsIgnoreCase(http.getMethod());

                if (!applicable) {
                    chain.doFilter(req, res);
                    return;
                }

                String scopedKey = http.getRequestURI() + "::" + key;

                // Cache hit? Replay.
                java.util.Optional<String> cached = idempotencyService.find(scopedKey);
                if (cached.isPresent()) {
                    log.info("IDEM_REPLAY path={} key={}", http.getRequestURI(), key);
                    httpRes.setStatus(HttpServletResponse.SC_OK);
                    httpRes.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                    httpRes.setHeader("X-Idempotency-Replay", "true");
                    httpRes.getOutputStream().write(cached.get().getBytes(StandardCharsets.UTF_8));
                    httpRes.getOutputStream().flush();
                    return;
                }

                // First in. Reserve (best-effort — concurrent same-key callers serialise
                // at the DB level too, but reserve makes the race visible).
                idempotencyService.reserve(scopedKey, IdempotencyService.DEFAULT_INFLIGHT_TTL);

                ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(httpRes);
                try {
                    chain.doFilter(http, wrapped);
                    int status = wrapped.getStatus();
                    byte[] body = wrapped.getContentAsByteArray();
                    if (status >= 200 && status < 300 && body.length > 0) {
                        String text = new String(body, StandardCharsets.UTF_8);
                        idempotencyService.store(scopedKey, text, IdempotencyService.DEFAULT_RESPONSE_TTL);
                        log.info("IDEM_STORED path={} key={} status={} bytes={}",
                                http.getRequestURI(), key, status, body.length);
                    } else {
                        // Non-2xx: drop the reservation so retries actually re-execute.
                        idempotencyService.release(scopedKey);
                    }
                    wrapped.copyBodyToResponse();
                } catch (RuntimeException ex) {
                    idempotencyService.release(scopedKey);
                    throw ex;
                }
            }
        };
    }

    /** Test-only: expose body capture util in case other filters want it. */
    @SuppressWarnings("unused")
    private static byte[] readAll(java.io.InputStream in) throws IOException {
        return StreamUtils.copyToByteArray(in);
    }
}
