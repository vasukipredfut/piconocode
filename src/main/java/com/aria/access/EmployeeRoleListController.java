package com.aria.access;

import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Employee-with-role listing — backs {@code aria:employee-role-list}
 * (V213). employee_details.roles_id / dept_id are convention-based
 * references (no DB foreign key), so this joins explicitly rather than
 * relying on generic-entity-ref resolution, and returns role/department as
 * plain string columns.
 */
@RestController
@RequestMapping("/api/m0/employee-roles")
public class EmployeeRoleListController {

    private final JdbcTemplate jdbc;

    public EmployeeRoleListController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false, name = "_q") String q,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "50") int pageSize) {

        StringBuilder where = new StringBuilder(" WHERE ed.tenant_id = ?");
        List<Object> args = new ArrayList<>(List.of(TenantContext.get()));

        if (q != null && !q.isBlank()) {
            where.append(" AND (ed.employeename LIKE ? OR ed.username LIKE ?)");
            String like = "%" + q.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (roleId != null) {
            where.append(" AND ed.roles_id = ?");
            args.add(roleId);
        }
        if (status != null && !status.isBlank()) {
            where.append(" AND ed.status = ?");
            args.add(status);
        }

        int size = Math.min(Math.max(pageSize, 1), 200);
        int pageNum = Math.max(page, 1);
        int offset = (pageNum - 1) * size;

        Integer total = jdbc.queryForObject(
                "SELECT COUNT(*) FROM employee_details ed" + where, Integer.class, args.toArray());

        List<Object> pagedArgs = new ArrayList<>(args);
        pagedArgs.add(size);
        pagedArgs.add(offset);
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT ed.id, ed.employeename, ed.username, ed.email, ed.phoneno, ed.status, ed.employeetype, " +
                "ed.roles_id, r.name AS role_name, ed.dept_id, d.name AS dept_name " +
                "FROM employee_details ed " +
                "LEFT JOIN role r ON r.id = ed.roles_id " +
                "LEFT JOIN department_details d ON d.id = ed.dept_id" +
                where + " ORDER BY ed.employeename LIMIT ? OFFSET ?",
                pagedArgs.toArray());

        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row.get("id"));
            item.put("employeeName", row.get("employeename"));
            item.put("username", row.get("username"));
            item.put("email", row.get("email"));
            item.put("phoneno", row.get("phoneno"));
            item.put("status", row.get("status"));
            item.put("employeeType", row.get("employeetype"));
            item.put("roleId", row.get("roles_id"));
            item.put("roleName", row.get("role_name"));
            item.put("deptId", row.get("dept_id"));
            item.put("deptName", row.get("dept_name"));
            out.add(item);
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ok", true);
        resp.put("rows", out);
        resp.put("total", total == null ? 0 : total);
        resp.put("page", pageNum);
        resp.put("pageSize", size);
        return ResponseEntity.ok(resp);
    }
}
