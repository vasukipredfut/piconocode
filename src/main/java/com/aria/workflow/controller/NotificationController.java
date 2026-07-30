package com.aria.workflow.controller;

import com.aria.security.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Read side of m0_notification (V141) — rows are written by
 * {@link com.aria.workflow.projection.NotificationListener} every time a
 * transition lands an instance on a new assigned role.
 */
@RestController
@RequestMapping("/api/workflow/notifications")
@Tag(name = "M0 Workflow Notifications")
public class NotificationController {

    private final JdbcTemplate jdbc;

    public NotificationController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/{role}")
    @Operation(summary = "List notifications for a persona/role, unread first (unread only by default).")
    public List<Map<String, Object>> forRole(
            @PathVariable String role,
            @RequestParam(defaultValue = "false") boolean includeRead) {
        String sql = "SELECT id, workflow_instance_id, gate_pass_id, vehicle_no, assigned_role, " +
                "current_state, " +
                "payload_json, is_read, created_at FROM m0_notification " +
                "WHERE tenant_id = ? AND assigned_role = ? " +
                (includeRead ? "" : "AND is_read = 0 ") +
                "ORDER BY created_at DESC";
        return jdbc.queryForList(sql, TenantContext.get(), role);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark one notification as read.")
    public Map<String, Object> markRead(@PathVariable Long id) {
        jdbc.update("UPDATE m0_notification SET is_read = 1 WHERE id = ? AND tenant_id = ?",
                id, TenantContext.get());
        return Map.of("ok", true);
    }
}
