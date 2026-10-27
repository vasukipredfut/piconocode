package com.aria.security;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * Role name of the logged-in user (employee_details.roles_id -> role.name). The JWT authority only
 * carries the role id; GenericEntityController resolves the name the same way for read_role /
 * write_role. Note: ActionDispatcher does not enforce action_registry.requires_role, so actions
 * that need a role check it here.
 */
public final class CurrentRole {

    public static final String SUPER_ADMIN = "SuperAdmin";
    public static final String TENANT_ADMIN = "TenantAdmin";
    public static final String MANAGER = "Manager";

    private CurrentRole() {}

    public static String name(JdbcTemplate jdbc) {
        Long userId = CurrentUser.id();
        if (userId == null) return null;
        List<String> rows = jdbc.queryForList(
                "SELECT r.name FROM employee_details e JOIN role r ON r.id = e.roles_id WHERE e.id=?",
                String.class, userId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /** True when the user's role is one of {@code roles} (case-insensitive); SuperAdmin always passes. */
    public static boolean isAny(JdbcTemplate jdbc, String... roles) {
        String name = name(jdbc);
        if (name == null) return false;
        if (SUPER_ADMIN.equalsIgnoreCase(name)) return true;
        for (String r : roles) {
            if (r.equalsIgnoreCase(name)) return true;
        }
        return false;
    }
}
