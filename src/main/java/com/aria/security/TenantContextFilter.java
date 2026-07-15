package com.aria.security;

import com.aria.config.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the {@code tid} (tenant id) claim from the JWT and stores it in
 * {@link TenantContext} for the duration of the request.
 *
 * <p>Wired into the security filter chain AFTER {@code AuthTokenFilter}
 * so the SecurityContext is already populated by the time we run. The
 * filter is permissive: missing claim → DEFAULT tenant + WARN log; absent
 * JWT (permit-all endpoints) → DEFAULT tenant + DEBUG log. The actual
 * cross-tenant guard happens at the repository/service layer.
 *
 * <p>Always clears the ThreadLocal in {@code finally} so a worker thread
 * never carries one request's tenant into the next.
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            Long tenantId = resolveTenant(request);
            TenantContext.set(tenantId);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private Long resolveTenant(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            return TenantContext.DEFAULT_TENANT_ID;
        }
        String token = header.substring(7);
        try {
            Long tid = jwtUtils.getTenantIdFromJwtToken(token);
            if (tid == null) {
                log.warn("JWT missing 'tid' claim; defaulting to tenant {}", TenantContext.DEFAULT_TENANT_ID);
                return TenantContext.DEFAULT_TENANT_ID;
            }
            return tid;
        } catch (RuntimeException e) {
            // AuthTokenFilter has already (or will) reject malformed/expired
            // tokens with a structured 401; we just default and let it handle.
            return TenantContext.DEFAULT_TENANT_ID;
        }
    }
}
