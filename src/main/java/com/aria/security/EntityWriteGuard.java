package com.aria.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * entity_registry.read_role/write_role enforcement for write paths OTHER
 * than GenericEntityController's own /api/m0/entities/{code} CRUD (which
 * already enforces this inline via its own checkRole). Every ActionHandler
 * dispatched through ActionDispatcher (save, gatepass.create, delete, ...)
 * was found to skip entity_registry entirely -- a role excluded from an
 * entity's write_role via the Designer could still write that entity by
 * hitting any page whose Save button fires one of those actions. This
 * mirrors GenericEntityController#checkRole's exact semantics (same
 * "* / null = unrestricted", same SuperAdmin bypass, same 403 shape) so
 * both paths agree on who can write what.
 */
@Component
public class EntityWriteGuard {

    private static final String SUPER_ROLE = "SuperAdmin";

    @Autowired private JdbcTemplate jdbc;

    private final Map<Long, String> roleNameCache = new ConcurrentHashMap<>();

    /**
     * Returns a 403 body when the current user can't perform {@code access}
     * ("read"/"write") on {@code entityCode}, else null. An entity with no
     * read_role/write_role row, or one this tenant hasn't registered at all,
     * is treated as unrestricted -- this guard only enforces what's actually
     * configured in entity_registry, it never invents a new failure mode
     * beyond what GenericEntityController already applies for the same
     * entity.
     */
    public ResponseEntity<?> check(String entityCode, String access) {
        if (entityCode == null) return null;
        Map<String, Object> reg;
        try {
            reg = jdbc.queryForMap(
                "SELECT read_role, write_role FROM entity_registry " +
                "WHERE code=? AND tenant_id IN (?, -1) ORDER BY tenant_id DESC LIMIT 1",
                entityCode, TenantContext.get());
        } catch (Exception e) {
            return null;
        }
        String allowed = (String) reg.get("write".equals(access) ? "write_role" : "read_role");
        if (allowed == null || allowed.trim().isEmpty() || "*".equals(allowed.trim())) return null;
        String currentName = currentRoleName();
        if (SUPER_ROLE.equalsIgnoreCase(currentName)) return null;
        if (currentName == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "ok", false, "code", "RBAC_NO_USER",
                "error", "Not authenticated for " + access + " on " + entityCode));
        }
        for (String rl : allowed.split(",")) {
            if (rl.trim().equalsIgnoreCase(currentName)) return null;
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "ok", false, "code", "RBAC_DENIED",
            "error", currentName + " is not permitted to " + access + " " + entityCode,
            "allowed", allowed));
    }

    private String currentRoleName() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || a.getAuthorities() == null) return null;
        for (GrantedAuthority ga : a.getAuthorities()) {
            String s = ga.getAuthority();
            if (s == null) continue;
            try {
                long id = Long.parseLong(s.trim());
                String name = roleNameCache.computeIfAbsent(id, this::loadRoleName);
                if (name != null) return name;
            } catch (NumberFormatException nfe) {
                return s; // already a role name
            }
        }
        return null;
    }

    private String loadRoleName(Long id) {
        try {
            return jdbc.queryForObject("SELECT name FROM role WHERE id=?", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
