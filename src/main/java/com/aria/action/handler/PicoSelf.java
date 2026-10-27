package com.aria.action.handler;

import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * Resolves the m7_employee row of the logged-in user (m7_employee.user_id = employee_details.id).
 * The PICO mobile actions never take an employee id from the client: ERPNext's pico_api accepted
 * employee_number in the body, so anyone with the URL could punch for anyone.
 */
final class PicoSelf {

    private PicoSelf() {}

    /** The caller's active employee row, or null when the login is not linked to an employee. */
    static Map<String, Object> employee(JdbcTemplate jdbc) {
        Long userId = CurrentUser.id();
        if (userId == null) return null;
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, emp_code, full_name, shift_code, holiday_list_id FROM m7_employee " +
                        "WHERE tenant_id=? AND user_id=? AND status='ACTIVE' LIMIT 1",
                TenantContext.get(), userId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    static String str(Object o) {
        return o == null ? null : o.toString().trim();
    }
}
