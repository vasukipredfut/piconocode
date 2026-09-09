package com.aria.access;

import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mirrors a role_page_permission grant/revoke onto the page's underlying
 * entity's {@code entity_registry.write_role} -- so restricting/granting a
 * role's Save+Edit on a page also closes/opens that same entity's separate
 * generic-entity-API door (see EntityWriteGuard), without an admin having to
 * touch entity_registry by hand.
 *
 * <p>Deliberately conservative in one direction: this NEVER turns an entity
 * that's currently open to everyone ({@code write_role} null or {@code "*"})
 * into a restricted one -- that would silently cut off every OTHER role
 * writing that entity through the generic API, a much bigger decision than
 * "grant/revoke this one role" should make on its own. It only ever ADDS
 * this role to an ALREADY-restricted list (on grant) or REMOVES it from one
 * (on revoke), and never blanks a list out to "unrestricted" as a side
 * effect of a revoke (there's no "nobody" value in that column).
 *
 * <p>Called from {@code GenericEntityController}'s generic
 * {@code /api/m0/entities/RolePagePermission} CRUD -- confirmed 2026-09-09
 * to be the ONLY screen used to manage grants ("Role Page Permission");
 * the earlier bespoke {@code EmployeeAccessMatrixController} / "Role Page
 * Access" path was removed outright as redundant with it (see
 * {@code V320}/{@code V321}).
 */
@Component
public class RolePagePermissionEntitySync {

    private static final Logger log = LoggerFactory.getLogger(RolePagePermissionEntitySync.class);

    @Autowired private JdbcTemplate jdbc;

    private final Map<Long, String> roleNameCache = new ConcurrentHashMap<>();

    /** Never throws -- a sync failure must never break the role_page_permission
     *  write that triggered it. */
    public void sync(String pageCode, Long roleId, boolean write) {
        try {
            doSync(pageCode, roleId, write);
        } catch (Exception e) {
            log.warn("RolePagePermission entity write_role sync failed for role={} page={}: {}",
                    roleId, pageCode, e.getMessage());
        }
    }

    private void doSync(String pageCode, Long roleId, boolean write) {
        if (pageCode == null || roleId == null) return;
        String entityCode;
        try {
            entityCode = jdbc.queryForObject(
                "SELECT JSON_UNQUOTE(JSON_EXTRACT(pv.body_json, '$.entity')) " +
                "FROM page_definition pd JOIN page_version pv ON pv.id = pd.current_version_id " +
                "WHERE pd.code = ? AND pd.tenant_id IN (?, -1) ORDER BY pd.tenant_id DESC LIMIT 1",
                String.class, pageCode, TenantContext.get());
        } catch (Exception e) {
            return; // page not found, or has no current_version_id yet
        }
        if (entityCode == null || entityCode.isBlank() || "null".equalsIgnoreCase(entityCode)) return;

        String roleName = roleNameCache.computeIfAbsent(roleId, this::loadRoleName);
        if (roleName == null) return;

        Map<String, Object> reg;
        try {
            reg = jdbc.queryForMap(
                "SELECT tenant_id, write_role FROM entity_registry " +
                "WHERE code = ? AND tenant_id IN (?, -1) ORDER BY tenant_id DESC LIMIT 1",
                entityCode, TenantContext.get());
        } catch (EmptyResultDataAccessException e) {
            return; // entity not registered
        }
        String current = (String) reg.get("write_role");
        if (current == null || current.isBlank() || "*".equals(current.trim())) return; // unrestricted -- leave it that way

        List<String> names = new ArrayList<>();
        for (String n : current.split(",")) {
            String t = n.trim();
            if (!t.isEmpty() && !t.equalsIgnoreCase(roleName)) names.add(t);
        }
        if (write) names.add(roleName);
        if (names.isEmpty()) return; // never blank the column out to "unrestricted" as a side effect

        Long regTenantId = ((Number) reg.get("tenant_id")).longValue();
        jdbc.update("UPDATE entity_registry SET write_role = ? WHERE code = ? AND tenant_id = ?",
                String.join(",", names), entityCode, regTenantId);
    }

    private String loadRoleName(Long id) {
        try {
            return jdbc.queryForObject("SELECT name FROM role WHERE id=?", String.class, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
