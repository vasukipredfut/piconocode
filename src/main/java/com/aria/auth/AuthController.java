package com.aria.auth;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.aria.config.JwtUtils;

/**
 * Substrate-native authentication endpoints — the sole login surface after the
 * legacy {@code com.aria.controller.UserController} was retired. Everything runs
 * on {@link AuthenticationManager} (wired to {@link AuthUserDetailsService}) +
 * {@link JwtUtils} + a thin {@link JdbcTemplate}; no JPA, no legacy model/dao.
 *
 * <p>Login is a straight credential check → JWT (no shift/attendance gate — the
 * legacy controller's biometric precondition was deliberately dropped). The
 * response shape matches what the frontend consumes:
 * {@code {accessToken, tokenType, refreshToken, id, username, roles[], permissions[]}}.
 *
 * <p>{@code permissions} is the older Module/SubModule/ActionItem-shaped grant
 * ({@code permissions} table, role_id-keyed) — kept as-is, untouched here.
 * {@code role_page_permission} (V195/V210) grants are not shipped at login —
 * they're enforced server-side per request instead: see
 * {@code PageDefinitionRepository#findResolvedForRole}, which filters the
 * {@code GET /api/m0/pages} sidebar listing directly by the caller's role, so
 * there's no login-time snapshot for a client to keep in sync.
 */
@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final JdbcTemplate jdbc;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, JdbcTemplate jdbc) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.jdbc = jdbc;
    }

    @PostMapping("/api/auth/signin")
    public ResponseEntity<?> signin(@RequestBody Map<String, Object> body) {
        String username = str(body.get("username"));
        String password = str(body.get("password"));
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            AuthUserDetails principal = (AuthUserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(principal);

            List<String> roles = principal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList();

            // Persist a refresh token (7-day TTL).
            String refresh = UUID.randomUUID().toString();
            jdbc.update("INSERT INTO refresh_token (token, user_id, expiry_date) VALUES (?, ?, ?)",
                    refresh, principal.getId(),
                    Timestamp.from(Instant.now().plus(Duration.ofDays(7))));

            List<Map<String, Object>> permissions = List.of();
            String landingRoute = null;
            String roleName = null;
            Integer roleId = parseRoleId(roles);
            if (roleId != null) {
                permissions = jdbc.queryForList("SELECT * FROM permissions WHERE role_id = ?", roleId);
                // DB-driven post-login landing route (per role — customizable data,
                // not hardcoded in the frontend). NULL → frontend falls back to
                // the home dashboard. See V126__role_landing_route.sql.
                landingRoute = jdbc.query(
                        "SELECT landing_route FROM role WHERE id = ?",
                        rs -> rs.next() ? rs.getString(1) : null, roleId);
                // roles[] only ever carries the numeric role_id (see
                // parseRoleId) — the frontend needs the human-readable name
                // too (e.g. for menu/role labels), same lookup
                // PageDefinitionController#currentRoleName does per-request.
                roleName = jdbc.query(
                        "SELECT name FROM role WHERE id = ?",
                        rs -> rs.next() ? rs.getString(1) : null, roleId);
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("accessToken", jwt);
            resp.put("tokenType", "Bearer");
            resp.put("refreshToken", refresh);
            resp.put("id", principal.getId());
            resp.put("username", principal.getUsername());
            resp.put("roles", roles);
            resp.put("roleName", roleName);
            resp.put("permissions", permissions);
            resp.put("landingRoute", landingRoute);
            return ResponseEntity.ok(resp);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", "401", "message", "Invalid username or password"));
        }
    }

    @PostMapping("/api/auth/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, Object> body) {
        String token = str(body.get("refreshToken"));
        try {
            Map<String, Object> row = jdbc.queryForMap(
                    "SELECT user_id, expiry_date FROM refresh_token WHERE token = ?", token);
            Timestamp expiry = (Timestamp) row.get("expiry_date");
            if (expiry == null || expiry.toInstant().isBefore(Instant.now())) {
                jdbc.update("DELETE FROM refresh_token WHERE token = ?", token);
                return ResponseEntity.status(403).body(Map.of("message", "Refresh token expired — sign in again"));
            }
            Long userId = ((Number) row.get("user_id")).longValue();
            Map<String, Object> u = jdbc.queryForMap(
                    "SELECT username FROM employee_details WHERE id = ?", userId);
            String jwt = jwtUtils.generateTokenFromUsername((String) u.get("username"));

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("accessToken", jwt);
            resp.put("tokenType", "Bearer");
            resp.put("refreshToken", token);
            return ResponseEntity.ok(resp);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(403).body(Map.of("message", "Refresh token not found"));
        }
    }

    @PostMapping("/api/auth/signout")
    public ResponseEntity<?> signout(@RequestBody(required = false) Map<String, Object> body) {
        if (body != null && body.get("refreshToken") != null) {
            jdbc.update("DELETE FROM refresh_token WHERE token = ?", str(body.get("refreshToken")));
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "signed out"));
    }

    private static String str(Object o) { return o == null ? "" : String.valueOf(o); }

    private static Integer parseRoleId(List<String> roles) {
        if (roles.isEmpty()) return null;
        try {
            return Integer.valueOf(roles.get(0));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
