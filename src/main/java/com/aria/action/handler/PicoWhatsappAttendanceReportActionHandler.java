package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.hr.attendance.PicoAttendanceDigest;
import com.aria.security.CurrentRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * pico.whatsapp-attendance-report -- on-demand version of PicoAttendanceDigest (ERPNext's
 * whitelisted send_attendance_report_to_whatsapp). Manager / TenantAdmin.
 * args: {@code date} (default yesterday), {@code to} (E.164 numbers; default whatsapp.attendance_report_to).
 */
@Component
public class PicoWhatsappAttendanceReportActionHandler implements ActionHandler {

    public static final String CODE = "pico.whatsapp-attendance-report";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PicoAttendanceDigest digest;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        if (!CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN)) {
            return ActionResult.error("Requires Manager or TenantAdmin");
        }
        Map<String, Object> a = inv.getArgs() == null ? Map.of() : inv.getArgs();
        LocalDate day = LocalDate.now().minusDays(1);
        if (a.get("date") != null && !a.get("date").toString().isBlank()) {
            try {
                day = LocalDate.parse(a.get("date").toString().trim().substring(0, 10));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("date must be YYYY-MM-DD");
            }
        }
        List<String> to = null;
        if (a.get("to") instanceof List<?> list) {
            to = new ArrayList<>();
            for (Object o : list) {
                String n = String.valueOf(o).trim();
                if (!n.matches("\\+?[0-9]{10,15}")) throw new IllegalArgumentException("to must be phone numbers in E.164 form");
                to.add(n.startsWith("+") ? n : "+" + n);
            }
            if (to.size() > 20) throw new IllegalArgumentException("At most 20 recipients");
        }
        Map<String, Object> r = digest.send(day, to);
        boolean ok = r.get("error") == null && ((Number) r.get("sent")).intValue() > 0;
        return ActionResult.builder().ok(ok).message(ok ? "Report sent via WhatsApp" : String.valueOf(r.getOrDefault("error", "Nothing was sent")))
                .data(r).build();
    }
}
