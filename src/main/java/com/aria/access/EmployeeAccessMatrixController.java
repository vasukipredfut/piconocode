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
 * Employee Access Matrix — for a chosen employee, shows their assigned role
 * and effective Read/Write/Approve on every registered screen/entity.
 *
 * <p>Source of truth: {@code employee_details.roles_id} (the login table's
 * single-role FK — see {@code AuthUserDetailsService}) resolved to
 * {@code role.name}, matched against {@code entity_registry}'s
 * read_role/write_role/approver_role CSV-or-"*" columns — the same access
 * data {@code GenericEntityController#checkRole} enforces for generic-entity
 * CRUD, just read here for display instead of enforced.
 *
 * <p>Backs the {@code aria:employee-access-matrix} page (see
 * {@code V184__employee_access_matrix_page.sql}).
 */
@RestController
@RequestMapping("/api/m0/access-matrix")
public class EmployeeAccessMatrixController {

    private static final Set<String> ALLOWED_ROLES = Set.of("TenantAdmin");

    private final JdbcTemplate jdbc;
    private final Map<Long, String> roleNameCache = new ConcurrentHashMap<>();

    public EmployeeAccessMatrixController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

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

    @GetMapping
    public ResponseEntity<?> matrix(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, name = "_q") String q,
            @RequestParam(required = false, defaultValue = "false") boolean onlyGranted) {
        ResponseEntity<?> denied = assertRole();
        if (denied != null) return denied;
        if (employeeId == null) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "error", "employeeId is required"));
        }

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
        String roleName = (String) employeeRow.get("role_name");

        // Per code, the tenant-specific entity_registry row wins over the
        // global (-1) one — same override rule as GenericEntityController's
        // per-code lookup, generalised to the whole table.
        List<Map<String, Object>> registryRows = jdbc.queryForList(
                "SELECT er.code, er.display_name, er.category, er.read_role, er.write_role, er.approver_role " +
                "FROM entity_registry er " +
                "JOIN (SELECT code, MAX(tenant_id) AS pick_tid FROM entity_registry " +
                "      WHERE tenant_id IN (?, -1) GROUP BY code) picked " +
                "  ON picked.code = er.code AND picked.pick_tid = er.tenant_id " +
                "WHERE er.tenant_id IN (?, -1)",
                TenantContext.get(), TenantContext.get());

        String qLower = (q == null) ? null : q.trim().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> screens = new ArrayList<>();
        int granted = 0;
        for (Map<String, Object> reg : registryRows) {
            String rowCategory = (String) reg.get("category");
            if (category != null && !category.isBlank() && !category.equalsIgnoreCase(rowCategory)) continue;

            String code = (String) reg.get("code");
            String displayName = (String) reg.get("display_name");
            if (qLower != null && !qLower.isEmpty()) {
                boolean codeMatches = code != null && code.toLowerCase(Locale.ROOT).contains(qLower);
                boolean nameMatches = displayName != null && displayName.toLowerCase(Locale.ROOT).contains(qLower);
                if (!codeMatches && !nameMatches) continue;
            }

            boolean read = openOrMatches((String) reg.get("read_role"), roleName, true);
            boolean write = openOrMatches((String) reg.get("write_role"), roleName, true);
            boolean approve = openOrMatches((String) reg.get("approver_role"), roleName, false);
            if (onlyGranted && !read && !write && !approve) continue;

            Map<String, Object> screen = new LinkedHashMap<>();
            screen.put("code", code);
            screen.put("displayName", displayName);
            screen.put("category", rowCategory);
            screen.put("read", read);
            screen.put("write", write);
            screen.put("approve", approve);
            screens.add(screen);
            if (read || write || approve) granted++;
        }

        screens.sort(Comparator
                .comparing((Map<String, Object> s) -> (String) s.get("category"), Comparator.nullsLast(String::compareTo))
                .thenComparing(s -> (String) s.get("displayName"), Comparator.nullsLast(String::compareTo)));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ok", true);
        resp.put("employee", toEmployeeJson(employeeRow));
        resp.put("screens", screens);
        resp.put("totalScreens", screens.size());
        resp.put("grantedScreens", granted);
        return ResponseEntity.ok(resp);
    }

    /**
     * {@code csv} is null/blank/"*" (open to any authenticated user) or a
     * comma-separated list of role names, matched case-insensitively —
     * identical semantics to {@code GenericEntityController#checkRole}.
     * {@code openIfBlank} controls what a blank column means: true for
     * read/write (blank == unrestricted), false for approve (blank == not
     * approvable by anyone — matches the observed data, where "*" never
     * appears in approver_role).
     */
    private static boolean openOrMatches(String csv, String roleName, boolean openIfBlank) {
        if (csv == null || csv.trim().isEmpty()) return openIfBlank;
        String trimmed = csv.trim();
        if ("*".equals(trimmed)) return true;
        if (roleName == null) return false;
        for (String candidate : trimmed.split(",")) {
            if (candidate.trim().equalsIgnoreCase(roleName)) return true;
        }
        return false;
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
    // Self-contained, mirroring GenericEntityController#currentRoleName and
    // OnboardingController#assertRole — no shared utility exists for this in
    // the codebase (checked); every bespoke controller replicates it.

    private ResponseEntity<?> assertRole() {
        String role = currentRoleName();
        if (role == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("ok", false, "code", "RBAC_NO_USER", "error", "Not authenticated"));
        }
        for (String allowed : ALLOWED_ROLES) {
            if (allowed.equalsIgnoreCase(role)) return null;
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Map.of("ok", false, "code", "RBAC_DENIED",
                        "error", role + " is not permitted to view the access matrix",
                        "allowed", ALLOWED_ROLES));
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
