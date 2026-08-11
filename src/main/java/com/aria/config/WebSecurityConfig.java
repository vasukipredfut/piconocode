package com.aria.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.aria.security.TenantContextFilter;
import com.aria.auth.AuthUserDetailsService;

/**
 * Spring Security configuration, migrated from Spring Security 5
 * (WebSecurityConfigurerAdapter) to Spring Security 6 (SecurityFilterChain bean).
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    AuthUserDetailsService userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private TenantContextFilter tenantContextFilter;

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Prevent Spring Boot from auto-registering {@link AuthTokenFilter} as a
     * plain servlet filter. Because it is a {@code Filter}-typed bean, Boot
     * would otherwise run it once in the servlet container (BEFORE Spring
     * Security's {@code FilterChainProxy}) — where it sets the authentication
     * and marks the request "filtered" via {@code OncePerRequestFilter}. The
     * chain's {@code SecurityContextHolderFilter} then resets the context to
     * empty (stateless), and the in-chain copy of this filter is skipped by
     * the once-per-request guard, leaving authorization with no authentication
     * (→ 401 on every protected endpoint). Disabling the servlet registration
     * makes the filter run ONLY where we explicitly add it in the chain.
     */
    @Bean
    public FilterRegistrationBean<AuthTokenFilter> authTokenFilterRegistration(AuthTokenFilter filter) {
        FilterRegistrationBean<AuthTokenFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(eh -> eh.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Container ERROR/FORWARD dispatches (e.g. Boot's /error page after a
                // controller 4xx/5xx) re-enter the security chain as ANONYMOUS. Without
                // this, a real 404/500 gets masked by a 401 on /error. Permit those
                // internal dispatches; the ORIGINAL request was already authorized.
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR,
                        jakarta.servlet.DispatcherType.FORWARD,
                        jakarta.servlet.DispatcherType.ASYNC).permitAll()
                .requestMatchers(
                    "/api/auth/**",
                    // Builder palette is permit-all so the page-author UI can
                    // populate before login (REOPEN WITH TASK_M0_BUILDER_AUTH).
                    "/api/m0/element-catalog",
                    // M11 inbound-webhook endpoint — auth carried in the URL token.
                    "/api/integrations/inbound/**",
                    // Raspberry Pi conveyor devices — no interactive login on the
                    // device, so these run tokenless (falls back to DEFAULT tenant
                    // via TenantContextFilter, same as any other permit-all route).
                    "/api/scanner/workflows/**",
                    "/api/conveyor/**",
                    // Raspberry Pi ParkingBayScanner — same tokenless-device
                    // convention as the conveyor scanner above.
                    "/api/parking/**",
                    // Raspberry Pi hand scanner (Gate Pass QR -> gate pass /
                    // workflow instance / order dispatch / bay assignment
                    // lookup) — same tokenless-device convention.
                    "/api/handscanner/**",
                    // Uploaded avatars/documents served as plain static files
                    // (<img src>, direct links) — browsers don't attach the JWT
                    // Authorization header to those requests, so this must stay
                    // public. Filenames are randomized (user id + timestamp), not
                    // guessable/enumerable.
                    "/uploads/**",
                    "/v2/api-docs",
                    "/v3/api-docs/**",
                    "/api-docs/**",
                    "/configuration/ui",
                    "/swagger-resources/**",
                    "/configuration/security",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/webjars/**",
                    // Standalone static admin tools (e.g. role-page-access.html)
                    // that handle their own login and call the JWT-protected
                    // APIs client-side — the HTML shell itself must be
                    // reachable before the user has a token.
                    "/admin/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .userDetailsService(userDetailsService);

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        // Tenant context runs AFTER auth — it reads the JWT 'tid' claim and
        // populates a ThreadLocal consumed by workflow + m0 services.
        http.addFilterAfter(tenantContextFilter, AuthTokenFilter.class);
        return http.build();
    }
}
