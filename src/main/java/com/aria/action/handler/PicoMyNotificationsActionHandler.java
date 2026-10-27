package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.my-notifications -- replaces ERPNext get_notifications (company "Notifications" list) and
 * adds the app's home badges: approvals waiting for the caller (leave, leave cancellation,
 * expense). args: optional {@code announcement_id} for one notice.
 */
@Component
public class PicoMyNotificationsActionHandler implements ActionHandler {

    public static final String CODE = "pico.my-notifications";

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> a = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Long tenantId = TenantContext.get();
        LocalDate today = LocalDate.now();
        String visible = "tenant_id=? AND is_active=1 AND (publish_from IS NULL OR publish_from <= ?) AND (publish_until IS NULL OR publish_until >= ?)";
        Map<String, Object> data = new LinkedHashMap<>();
        if (a.get("announcement_id") != null) {
            long id = PicoLeadCreateActionHandler.id(a.get("announcement_id"), "announcement_id");
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, title, body, publish_from, created_at FROM m7_announcement WHERE " +
                    visible + " AND id=?", tenantId, today, today, id);
            if (rows.isEmpty()) return ActionResult.error("Notification not found");
            data.put("announcement", rows.get(0));
            return ActionResult.builder().ok(true).data(data).build();
        }
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, title, body, publish_from, created_at FROM m7_announcement WHERE " +
                visible + " ORDER BY created_at DESC LIMIT 100", tenantId, today, today);
        data.put("count", rows.size());
        data.put("announcements", rows);

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp != null) {
            Object me = emp.get("id");
            Map<String, Object> pending = new LinkedHashMap<>();
            pending.put("leave", count("SELECT COUNT(*) FROM m7_leave_request WHERE tenant_id=? AND approver_emp_id=? AND status='PENDING'", tenantId, me));
            pending.put("leave_cancellation", count("SELECT COUNT(*) FROM m7_leave_request WHERE tenant_id=? AND approver_emp_id=? AND cancel_status='REQUESTED'", tenantId, me));
            pending.put("expense", count("SELECT COUNT(*) FROM m7_expense_claim WHERE tenant_id=? AND approver_emp_id=? AND status='SUBMITTED'", tenantId, me));
            pending.put("installation_otp", count("SELECT COUNT(*) FROM m9_installation_report WHERE tenant_id=? AND engineer_emp_id=? AND status='OTP_PENDING'", tenantId, me));
            data.put("pending_for_me", pending);
        }
        return ActionResult.builder().ok(true).data(data).build();
    }

    private int count(String sql, Object... params) {
        Integer n = jdbc.queryForObject(sql, Integer.class, params);
        return n == null ? 0 : n;
    }
}
