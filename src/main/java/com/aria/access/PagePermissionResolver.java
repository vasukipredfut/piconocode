package com.aria.access;

import com.aria.security.TenantContext;
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
 * Enforces {@code role_page_permission} (V195, admin-editable via
 * {@code EmployeeAccessMatrixController} / {@code aria:role-page-access},
 * TenantAdmin+SuperAdmin only) for the CURRENT logged-in user against a page
 * code. Role-based only, by design (Dhivya, 2026-09-09) — this deliberately
 * does NOT consult {@code employee_page_permission}'s per-employee override;
 * every user's permission is purely a function of their role.
 *
 * <p>Deliberately DIFFERENT from V195's originally-documented "missing row
 * = read:true, write:false" default: role_page_permission starts out
 * completely empty in every environment (checked — no seed migration
 * populates it), so a deny-by-default here would lock every non-SuperAdmin
 * user out of every page's Save button the instant this guard is wired in,
 * until an admin manually re-grants each (role, page) pair. Instead, "no row
 * for this (role, page)" resolves to unrestricted — same "absence =
 * unrestricted" convention {@code entity_registry.write_role} already uses
 * (see {@link com.aria.security.EntityWriteGuard}). Behaviour only changes
 * once TenantAdmin/SuperAdmin explicitly grants or restricts a (role, page)
 * pair through the Employee Access Matrix.
 */
@Component
public class PagePermissionResolver {

    private static final String SUPER_ROLE = "SuperAdmin";

    @Autowired private JdbcTemplate jdbc;

    private final Map<Long, String> roleNameCache = new ConcurrentHashMap<>();

    private record Perm(boolean read, boolean save, boolean edit) {}

    /** Returns a 403 body when the current user can't perform {@code access}
     *  ("read"/"save"/"edit") on {@code pageCode}, else null. "save" means
     *  creating a brand-new record via this page; "edit" means updating an
     *  existing one -- see V318, which split what used to be one combined
     *  "write" flag into these two. */
    public ResponseEntity<?> check(String pageCode, String access) {
        if (pageCode == null) return null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "ok", false, "code", "RBAC_NO_USER",
                "error", "Not authenticated for " + access + " on " + pageCode));
        }

        Long roleId = null;
        String roleName = null;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String s = ga.getAuthority();
            if (s == null) continue;
            try {
                roleId = Long.parseLong(s.trim());
                roleName = roleNameCache.computeIfAbsent(roleId, this::loadRoleName);
            } catch (NumberFormatException nfe) {
                roleName = s; // already a role name
            }
            break;
        }
        if (SUPER_ROLE.equalsIgnoreCase(roleName)) return null;

        Perm effective = roleId != null ? fetchOne("role_page_permission", "role_id", roleId, pageCode) : null;
        if (effective == null) return null; // no grant/restriction configured yet -> unrestricted

        boolean allowed = switch (access) {
            case "save" -> effective.save();
            case "edit" -> effective.edit();
            default -> effective.read();
        };
        if (allowed) return null;
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
            "ok", false, "code", "PAGE_RBAC_DENIED",
            "error", (roleName == null ? "This role" : roleName) + " is not permitted to " + access + " " + pageCode));
    }

    private Perm fetchOne(String table, String keyColumn, Long keyValue, String pageCode) {
        // table/keyColumn are fixed literals from this class only, never
        // request-derived -- no injection risk in the concat.
        try {
            Map<String, Object> row = jdbc.queryForMap(
                "SELECT can_read, can_save, can_edit FROM " + table +
                " WHERE tenant_id IN (?, -1) AND " + keyColumn + " = ? AND page_code = ?",
                TenantContext.get(), keyValue, pageCode);
            return new Perm(truthy(row.get("can_read")), truthy(row.get("can_save")), truthy(row.get("can_edit")));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private String loadRoleName(Long id) {
        try {
            return jdbc.queryForObject("SELECT name FROM role WHERE id=?", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private static boolean truthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(o));
    }
}
