package com.aria.access;

import com.aria.security.TenantContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Employee Access Matrix — page-wise Read/Write/Approve, assignable at the
 * Role level (cascades to every employee holding that role) and overridable
 * at the individual Employee level.
 *
 * <p>Screen universe: {@code page_definition} (the actual navigable pages —
 * "page-wise", not the 653-row entity_registry "entity-wise" set this
 * controller started out with).
 *
 * <p>Resolution per (employee, page): an {@code employee_page_permission}
 * row (explicit override for that one employee) wins outright over a
 * {@code role_page_permission} row (the employee's role default), which
 * wins over the hardcoded default: read=true, write=false, approve=false —
 * same "missing row = open read, closed write" convention entity_registry
 * used, just moved to real editable tables (see
 * {@code V195__role_and_employee_page_permission.sql}).
 *
 * <p>Backs the {@code aria:employee-access-matrix} page (registered in
 * {@code V191__employee_access_matrix_page.sql}; unchanged by this rewrite).
 */
@RestController
@RequestMapping("/api/m0/access-matrix")
public class EmployeeAccessMatrixController {

    private static final String SUPER_ROLE = "SuperAdmin";
    private static final Set<String> ALLOWED_ROLES = Set.of("TenantAdmin");

    private final JdbcTemplate jdbc;
    private final Map<Long, String> roleNameCache = new ConcurrentHashMap<>();

    public EmployeeAccessMatrixController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ---- Dropdown data -------------------------------------------------

    @GetMapping("/employees")
    public ResponseEntity<?> employees(@RequestParam(required = false) String q) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;

        StringBuilder sql = new StringBuilder(
                "SELECT ed.id, ed.username, ed.employeename, ed.status, ed.roles_id, r.name AS role_name " +
                "FROM employee_details ed LEFT JOIN role r ON r.id = ed.roles_id " +
                "WHERE ed.tenant_id = ?");
        List<Object> args = new ArrayList<>(List.of(TenantContext.get()));
        if (q != null && !q.isBlank()) {
            sql.append(" AND (ed.employeename LIKE ? OR ed.username LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
        }
        sql.append(" ORDER BY ed.employeename LIMIT 500");

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), args.toArray());
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) out.add(toEmployeeJson(row));
        return ResponseEntity.ok(Map.of("ok", true, "employees", out));
    }

    @GetMapping("/roles")
    public ResponseEntity<?> roles(@RequestParam(required = false) String q) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;

        StringBuilder sql = new StringBuilder("SELECT id, name FROM role");
        List<Object> args = new ArrayList<>();
        if (q != null && !q.isBlank()) {
            sql.append(" WHERE name LIKE ?");
            args.add("%" + q.trim() + "%");
        }
        sql.append(" ORDER BY name, id");

        List<Map<String, Object>> rows = jdbc.queryForList(sql.toString(), args.toArray());
        return ResponseEntity.ok(Map.of("ok", true, "roles", rows));
    }

    // ---- Matrix ---------------------------------------------------------

    @GetMapping
    public ResponseEntity<?> matrix(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "_q") String q,
            @RequestParam(required = false, defaultValue = "false") boolean onlyGranted) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;

        if ((employeeId == null) == (roleId == null)) {
            return ResponseEntity.badRequest().body(
                    Map.of("ok", false, "error", "Exactly one of employeeId or roleId is required"));
        }

        Map<String, Object> employeeJson = null;
        Map<String, Object> roleJson = null;
        Long targetRoleId = null;
        Map<String, PermRow> employeeOverrides = Map.of();

        if (employeeId != null) {
            Map<String, Object> employeeRow;
            try {
                employeeRow = jdbc.queryForMap(
                        "SELECT ed.id, ed.username, ed.employeename, ed.status, ed.roles_id, r.name AS role_name " +
                        "FROM employee_details ed LEFT JOIN role r ON r.id = ed.roles_id " +
                        "WHERE ed.tenant_id = ? AND ed.id = ?",
                        TenantContext.get(), employeeId);
            } catch (EmptyResultDataAccessException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("ok", false, "error", "Employee not found: id=" + employeeId));
            }
            employeeJson = toEmployeeJson(employeeRow);
            Object rid = employeeRow.get("roles_id");
            targetRoleId = rid == null ? null : ((Number) rid).longValue();
            if (targetRoleId != null) {
                roleJson = new LinkedHashMap<>();
                roleJson.put("id", targetRoleId);
                roleJson.put("name", employeeRow.get("role_name"));
            }
            employeeOverrides = fetchPermissionMap("employee_page_permission", "employee_id", employeeId);
        } else {
            Map<String, Object> roleRow;
            try {
                roleRow = jdbc.queryForMap("SELECT id, name FROM role WHERE id = ?", roleId);
            } catch (EmptyResultDataAccessException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("ok", false, "error", "Role not found: id=" + roleId));
            }
            targetRoleId = roleId;
            roleJson = new LinkedHashMap<>(roleRow);
        }

        List<Map<String, Object>> pages = jdbc.queryForList(
                "SELECT code, title, category FROM page_definition WHERE tenant_id IN (?, -1) AND status = 'PUBLISHED'",
                TenantContext.get());

        Map<String, PermRow> roleDefaults = targetRoleId != null
                ? fetchPermissionMap("role_page_permission", "role_id", targetRoleId)
                : Map.of();

        String qLower = (q == null) ? null : q.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> screens = new ArrayList<>();
        int granted = 0;
        for (Map<String, Object> page : pages) {
            String pageCode = (String) page.get("code");
            String title = (String) page.get("title");
            String pageCategory = (String) page.get("category");

            if (category != null && !category.isBlank() && !category.equalsIgnoreCase(pageCategory)) continue;
            if (qLower != null && !qLower.isEmpty()) {
                boolean codeMatches = pageCode != null && pageCode.toLowerCase(Locale.ROOT).contains(qLower);
                boolean titleMatches = title != null && title.toLowerCase(Locale.ROOT).contains(qLower);
                if (!codeMatches && !titleMatches) continue;
            }

            PermRow override = employeeOverrides.get(pageCode);
            PermRow roleDefault = roleDefaults.get(pageCode);
            boolean read, write, approve;
            String source;
            if (override != null) {
                read = override.read(); write = override.write(); approve = override.approve();
                source = "employee-override";
            } else if (roleDefault != null) {
                read = roleDefault.read(); write = roleDefault.write(); approve = roleDefault.approve();
                source = "role";
            } else {
                read = true; write = false; approve = false;
                source = "default";
            }
            if (onlyGranted && !read && !write && !approve) continue;

            Map<String, Object> screen = new LinkedHashMap<>();
            screen.put("pageCode", pageCode);
            screen.put("title", title);
            screen.put("category", pageCategory);
            screen.put("read", read);
            screen.put("write", write);
            screen.put("approve", approve);
            screen.put("source", source);
            screens.add(screen);
            if (read || write || approve) granted++;
        }

        screens.sort(Comparator
                .comparing((Map<String, Object> s) -> (String) s.get("category"), Comparator.nullsLast(String::compareTo))
                .thenComparing(s -> (String) s.get("title"), Comparator.nullsLast(String::compareTo)));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ok", true);
        if (employeeJson != null) resp.put("employee", employeeJson);
        resp.put("role", roleJson);
        resp.put("screens", screens);
        resp.put("totalScreens", screens.size());
        resp.put("grantedScreens", granted);
        return ResponseEntity.ok(resp);
    }

    // ---- Admin: assign / update / reset ---------------------------------

    @PutMapping("/role-permission")
    public ResponseEntity<?> upsertRolePermission(@RequestBody Map<String, Object> body) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;

        Long roleId = longVal(body.get("roleId"));
        String pageCode = str(body.get("pageCode"));
        if (roleId == null || pageCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "roleId and pageCode are required"));
        }
        if (!roleExists(roleId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("ok", false, "error", "Role not found: id=" + roleId));
        }
        if (!pageExists(pageCode)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("ok", false, "error", "Page not found: code=" + pageCode));
        }

        boolean read = boolVal(body.get("read"));
        boolean write = boolVal(body.get("write"));
        boolean approve = boolVal(body.get("approve"));
        jdbc.update(
                "INSERT INTO role_page_permission (tenant_id, role_id, page_code, can_read, can_write, can_approve) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE can_read = VALUES(can_read), can_write = VALUES(can_write), can_approve = VALUES(can_approve)",
                TenantContext.get(), roleId, pageCode, read, write, approve);

        return ResponseEntity.ok(Map.of("ok", true, "roleId", roleId, "pageCode", pageCode,
                "read", read, "write", write, "approve", approve));
    }

    @PutMapping("/employee-permission")
    public ResponseEntity<?> upsertEmployeePermission(@RequestBody Map<String, Object> body) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;

        Long employeeId = longVal(body.get("employeeId"));
        String pageCode = str(body.get("pageCode"));
        if (employeeId == null || pageCode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "employeeId and pageCode are required"));
        }
        if (!employeeExists(employeeId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("ok", false, "error", "Employee not found: id=" + employeeId));
        }
        if (!pageExists(pageCode)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("ok", false, "error", "Page not found: code=" + pageCode));
        }

        boolean read = boolVal(body.get("read"));
        boolean write = boolVal(body.get("write"));
        boolean approve = boolVal(body.get("approve"));
        jdbc.update(
                "INSERT INTO employee_page_permission (tenant_id, employee_id, page_code, can_read, can_write, can_approve) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE can_read = VALUES(can_read), can_write = VALUES(can_write), can_approve = VALUES(can_approve)",
                TenantContext.get(), employeeId, pageCode, read, write, approve);

        return ResponseEntity.ok(Map.of("ok", true, "employeeId", employeeId, "pageCode", pageCode,
                "read", read, "write", write, "approve", approve));
    }

    @DeleteMapping("/role-permission")
    public ResponseEntity<?> deleteRolePermission(@RequestParam Long roleId, @RequestParam String pageCode) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;
        int n = jdbc.update(
                "DELETE FROM role_page_permission WHERE tenant_id IN (?, -1) AND role_id = ? AND page_code = ?",
                TenantContext.get(), roleId, pageCode);
        return ResponseEntity.ok(Map.of("ok", true, "deleted", n));
    }

    @DeleteMapping("/employee-permission")
    public ResponseEntity<?> deleteEmployeePermission(@RequestParam Long employeeId, @RequestParam String pageCode) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;
        int n = jdbc.update(
                "DELETE FROM employee_page_permission WHERE tenant_id IN (?, -1) AND employee_id = ? AND page_code = ?",
                TenantContext.get(), employeeId, pageCode);
        return ResponseEntity.ok(Map.of("ok", true, "deleted", n));
    }

    // ---- Helpers ----------------------------------------------------------

    private record PermRow(boolean read, boolean write, boolean approve) {}

    private Map<String, PermRow> fetchPermissionMap(String table, String keyColumn, Long keyValue) {
        // table/keyColumn are fixed literals passed by this class only — never
        // request-derived — so string-concat here carries no injection risk.
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT page_code, can_read, can_write, can_approve FROM " + table +
                " WHERE tenant_id IN (?, -1) AND " + keyColumn + " = ?",
                TenantContext.get(), keyValue);
        Map<String, PermRow> out = new HashMap<>();
        for (Map<String, Object> row : rows) {
            out.put((String) row.get("page_code"), new PermRow(
                    truthy(row.get("can_read")), truthy(row.get("can_write")), truthy(row.get("can_approve"))));
        }
        return out;
    }

    private boolean roleExists(Long roleId) {
        Integer c = jdbc.queryForObject("SELECT COUNT(*) FROM role WHERE id = ?", Integer.class, roleId);
        return c != null && c > 0;
    }

    private boolean pageExists(String pageCode) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM page_definition WHERE tenant_id IN (?, -1) AND code = ? AND status = 'PUBLISHED'",
                Integer.class, TenantContext.get(), pageCode);
        return c != null && c > 0;
    }

    private boolean employeeExists(Long employeeId) {
        Integer c = jdbc.queryForObject(
                "SELECT COUNT(*) FROM employee_details WHERE tenant_id = ? AND id = ?",
                Integer.class, TenantContext.get(), employeeId);
        return c != null && c > 0;
    }

    private static boolean truthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue() != 0;
        return Boolean.parseBoolean(String.valueOf(o));
    }

    private static boolean boolVal(Object o) {
        return truthy(o);
    }

    private static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static Long longVal(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(o).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, Object> toEmployeeJson(Map<String, Object> row) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", row.get("id"));
        out.put("username", row.get("username"));
        out.put("employeeName", row.get("employeename"));
        out.put("status", row.get("status"));
        out.put("roleId", row.get("roles_id"));
        out.put("roleName", row.get("role_name"));
        return out;
    }

    // ---- RBAC ---------------------------------------------------------
    // SuperAdmin bypasses unconditionally (mirrors
    // GenericEntityController#checkRole's SUPER_ROLE convention, added in
    // V182__superadmin_role_and_user.sql); TenantAdmin is the standard admin
    // role otherwise. No shared utility exists for this in the codebase
    // (checked); every bespoke controller replicates it.

    private ResponseEntity<?> assertRole() {
        String role = currentRoleName();
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("ok", false, "code", "RBAC_NO_USER", "error", "Not authenticated"));
        }
        if (SUPER_ROLE.equalsIgnoreCase(role)) return null;
        for (String allowed : ALLOWED_ROLES) {
            if (allowed.equalsIgnoreCase(role)) return null;
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of("ok", false, "code", "RBAC_DENIED",
                        "error", role + " is not permitted to view the access matrix",
                        "allowed", List.of("TenantAdmin", "SuperAdmin")));
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
