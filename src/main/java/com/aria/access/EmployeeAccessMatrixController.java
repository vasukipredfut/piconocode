package com.aria.access;

import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * Employee Access Matrix — page-wise Read/Save/Edit/Approve/Delete,
 * assignable at the Role level (cascades to every employee holding that
 * role) and overridable at the individual Employee level. Save (create a
 * new record via this page) and Edit (update an existing one) are separate
 * flags — see {@code V318__role_page_permission_split_write_into_save_edit.sql}.
 *
 * <p>Screen universe: {@code page_definition} (the actual navigable pages —
 * "page-wise", not the 653-row entity_registry "entity-wise" set this
 * controller started out with).
 *
 * <p>Resolution per (employee, page): an {@code employee_page_permission}
 * row (explicit override for that one employee) wins outright over a
 * {@code role_page_permission} row (the employee's role default), which
 * wins over the hardcoded default: read=true, save=false, edit=false,
 * approve=false — same "missing row = open read, closed write" convention
 * entity_registry used, just moved to real editable tables (see
 * {@code V195__role_and_employee_page_permission.sql}).
 *
 * <p>Backs the {@code aria:employee-access-matrix} page (registered in
 * {@code V191__employee_access_matrix_page.sql}; unchanged by this rewrite).
 */
@RestController
@RequestMapping("/api/m0/access-matrix")
public class EmployeeAccessMatrixController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeAccessMatrixController.class);
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
            boolean read, save, edit, approve, delete;
            String source;
            if (override != null) {
                read = override.read(); save = override.save(); edit = override.edit(); approve = override.approve(); delete = override.delete();
                source = "employee-override";
            } else if (roleDefault != null) {
                read = roleDefault.read(); save = roleDefault.save(); edit = roleDefault.edit(); approve = roleDefault.approve(); delete = roleDefault.delete();
                source = "role";
            } else {
                read = true; save = false; edit = false; approve = false; delete = false;
                source = "default";
            }
            if (onlyGranted && !read && !save && !edit && !approve && !delete) continue;

            Map<String, Object> screen = new LinkedHashMap<>();
            screen.put("pageCode", pageCode);
            screen.put("title", title);
            screen.put("category", pageCategory);
            screen.put("read", read);
            screen.put("save", save);
            screen.put("edit", edit);
            screen.put("approve", approve);
            screen.put("delete", delete);
            screen.put("source", source);
            screens.add(screen);
            if (read || save || edit || approve || delete) granted++;
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
        boolean save = boolVal(body.get("save"));
        boolean edit = boolVal(body.get("edit"));
        boolean approve = boolVal(body.get("approve"));
        boolean delete = boolVal(body.get("delete"));
        jdbc.update(
                "INSERT INTO role_page_permission (tenant_id, role_id, page_code, can_read, can_save, can_edit, can_approve, can_delete) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE can_read = VALUES(can_read), can_save = VALUES(can_save), can_edit = VALUES(can_edit), can_approve = VALUES(can_approve), can_delete = VALUES(can_delete)",
                TenantContext.get(), roleId, pageCode, read, save, edit, approve, delete);

        try {
            // Entity-level sync treats "save" and "edit" the same way --
            // entity_registry.write_role is a single undifferentiated write
            // flag, it has no create/update split of its own.
            syncEntityWriteRole(pageCode, roleId, save || edit);
        } catch (Exception e) {
            // The page-level grant above is the actual API contract; the
            // entity-level convenience sync must never break it.
            log.warn("syncEntityWriteRole failed for role={} page={}: {}", roleId, pageCode, e.getMessage());
        }

        return ResponseEntity.ok(Map.of("ok", true, "roleId", roleId, "pageCode", pageCode,
                "read", read, "save", save, "edit", edit, "approve", approve, "delete", delete));
    }

    /**
     * Convenience mirror of a role_page_permission grant/revoke onto the
     * page's underlying entity's {@code entity_registry.write_role} — so an
     * admin ticking "Save"/"Edit" for a role on a page here also closes the
     * separate generic-entity-API door (see EntityWriteGuard / this
     * conversation's "two doors" explanation) for that same entity, without
     * having to visit a second screen.
     *
     * <p>Deliberately conservative in one direction: this NEVER turns an
     * entity that's currently open to everyone ({@code write_role} null or
     * {@code "*"}) into a restricted one. Doing that as a side effect of one
     * role's page grant would silently cut off every OTHER role that
     * currently writes that entity through the generic API — a much bigger,
     * cross-cutting decision than "grant/revoke this one role" should make
     * on its own. It only ever ADDS this role to an ALREADY-restricted list
     * (on grant) or REMOVES it from one (on revoke), and if a revoke would
     * empty the list out entirely, it leaves the list as-is rather than
     * accidentally opening the entity to everyone (there's no "nobody"
     * value in this column — blank/null means unrestricted).
     *
     * <p>No-ops silently when the page has no single {@code "entity"} in its
     * body_json, or that entity isn't registered — same "nothing to enforce"
     * convention as the rest of this RBAC layer.
     */
    private void syncEntityWriteRole(String pageCode, Long roleId, boolean write) {
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
        boolean save = boolVal(body.get("save"));
        boolean edit = boolVal(body.get("edit"));
        boolean approve = boolVal(body.get("approve"));
        boolean delete = boolVal(body.get("delete"));
        jdbc.update(
                "INSERT INTO employee_page_permission (tenant_id, employee_id, page_code, can_read, can_save, can_edit, can_approve, can_delete) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE can_read = VALUES(can_read), can_save = VALUES(can_save), can_edit = VALUES(can_edit), can_approve = VALUES(can_approve), can_delete = VALUES(can_delete)",
                TenantContext.get(), employeeId, pageCode, read, save, edit, approve, delete);

        return ResponseEntity.ok(Map.of("ok", true, "employeeId", employeeId, "pageCode", pageCode,
                "read", read, "save", save, "edit", edit, "approve", approve, "delete", delete));
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

    // ---- Role page visibility (nav allow-list) ---------------------------
    // A role with zero rows in role_page_visibility is unrestricted (sees
    // every published page — today's behaviour, unchanged). A role with any
    // rows there is restricted to exactly those pages. See
    // com.aria.access.PageVisibilityController for the self-service side
    // (what a given logged-in user is allowed to see) that this data feeds.

    @GetMapping("/role-visibility")
    public ResponseEntity<?> roleVisibility(@RequestParam Long roleId,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false, name = "_q") String q) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;
        if (!roleExists(roleId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("ok", false, "error", "Role not found: id=" + roleId));
        }

        Set<String> visiblePages = new HashSet<>(jdbc.queryForList(
                "SELECT page_code FROM role_page_visibility WHERE tenant_id IN (?, -1) AND role_id = ?",
                String.class, TenantContext.get(), roleId));
        boolean restricted = !visiblePages.isEmpty();

        List<Map<String, Object>> pages = jdbc.queryForList(
                "SELECT code, title, category FROM page_definition WHERE tenant_id IN (?, -1) AND status = 'PUBLISHED'",
                TenantContext.get());
        Map<String, PermRow> roleDefaults = fetchPermissionMap("role_page_permission", "role_id", roleId);

        String qLower = (q == null) ? null : q.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> out = new ArrayList<>();
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

            PermRow perm = roleDefaults.get(pageCode);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("pageCode", pageCode);
            row.put("title", title);
            row.put("category", pageCategory);
            row.put("visible", restricted ? visiblePages.contains(pageCode) : true);
            row.put("read", perm != null && perm.read());
            row.put("save", perm != null && perm.save());
            row.put("edit", perm != null && perm.edit());
            row.put("approve", perm != null && perm.approve());
            row.put("delete", perm != null && perm.delete());
            out.add(row);
        }
        out.sort(Comparator
                .comparing((Map<String, Object> s) -> (String) s.get("category"), Comparator.nullsLast(String::compareTo))
                .thenComparing(s -> (String) s.get("title"), Comparator.nullsLast(String::compareTo)));

        return ResponseEntity.ok(Map.of("ok", true, "roleId", roleId, "restricted", restricted, "pages", out));
    }

    /**
     * Replaces the entire visible-page set for a role in one call — matches
     * a checkbox-list UI where the admin ticks N pages and hits Save.
     * Passing an empty pageCodes list clears all rows, which reverts the
     * role to unrestricted (sees every published page).
     */
    @PutMapping("/role-visibility")
    public ResponseEntity<?> replaceRoleVisibility(@RequestBody Map<String, Object> body) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;

        Long roleId = longVal(body.get("roleId"));
        if (roleId == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "roleId is required"));
        }
        if (!roleExists(roleId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    Map.of("ok", false, "error", "Role not found: id=" + roleId));
        }
        Object rawCodes = body.get("pageCodes");
        List<String> pageCodes = new ArrayList<>();
        if (rawCodes instanceof List<?> list) {
            for (Object o : list) {
                String code = str(o);
                if (!code.isBlank()) pageCodes.add(code);
            }
        }
        for (String code : pageCodes) {
            if (!pageExists(code)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of("ok", false, "error", "Page not found: code=" + code));
            }
        }

        long tenantId = TenantContext.get();
        jdbc.update("DELETE FROM role_page_visibility WHERE tenant_id IN (?, -1) AND role_id = ?", tenantId, roleId);
        for (String code : pageCodes) {
            jdbc.update(
                    "INSERT INTO role_page_visibility (tenant_id, role_id, page_code) VALUES (?, ?, ?)",
                    tenantId, roleId, code);
        }

        return ResponseEntity.ok(Map.of("ok", true, "roleId", roleId,
                "restricted", !pageCodes.isEmpty(), "pageCodes", pageCodes));
    }

    /**
     * Toggles visibility for exactly one (role, page) pair — safe to call
     * from a UI that's showing a filtered/searched subset of pages, unlike
     * {@link #replaceRoleVisibility}, which replaces the role's entire set
     * and would silently drop visibility for any page not currently on
     * screen.
     */
    @PutMapping("/role-visibility/toggle")
    public ResponseEntity<?> toggleRoleVisibility(@RequestBody Map<String, Object> body) {
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

        boolean visible = boolVal(body.get("visible"));
        long tenantId = TenantContext.get();
        if (visible) {
            jdbc.update(
                    "INSERT INTO role_page_visibility (tenant_id, role_id, page_code) VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE page_code = page_code",
                    tenantId, roleId, pageCode);
        } else {
            jdbc.update(
                    "DELETE FROM role_page_visibility WHERE tenant_id IN (?, -1) AND role_id = ? AND page_code = ?",
                    tenantId, roleId, pageCode);
        }

        Integer remaining = jdbc.queryForObject(
                "SELECT COUNT(*) FROM role_page_visibility WHERE tenant_id IN (?, -1) AND role_id = ?",
                Integer.class, tenantId, roleId);
        return ResponseEntity.ok(Map.of("ok", true, "roleId", roleId, "pageCode", pageCode,
                "visible", visible, "restricted", remaining != null && remaining > 0));
    }

    // ---- Helpers ----------------------------------------------------------

private record PermRow(boolean read, boolean save, boolean edit, boolean approve, boolean delete) {}

    private Map<String, PermRow> fetchPermissionMap(String table, String keyColumn, Long keyValue) {
        // table/keyColumn are fixed literals passed by this class only — never
        // request-derived — so string-concat here carries no injection risk.
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT page_code, can_read, can_save, can_edit, can_approve, can_delete FROM " + table +
                " WHERE tenant_id IN (?, -1) AND " + keyColumn + " = ?",
                TenantContext.get(), keyValue);
        Map<String, PermRow> out = new HashMap<>();
        for (Map<String, Object> row : rows) {
            out.put((String) row.get("page_code"), new PermRow(
                    truthy(row.get("can_read")), truthy(row.get("can_save")), truthy(row.get("can_edit")),
                    truthy(row.get("can_approve")), truthy(row.get("can_delete"))));
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
