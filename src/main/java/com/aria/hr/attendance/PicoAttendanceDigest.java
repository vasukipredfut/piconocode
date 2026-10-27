package com.aria.hr.attendance;

import com.aria.integration.adapter.OutboundResult;
import com.aria.integration.adapter.TwilioWhatsappAdapter;
import com.aria.integration.service.IntegrationOutboundCaller;
import com.aria.security.TenantContext;
import com.aria.security.TenantIterator;
import com.aria.tenant.TenantSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Daily attendance summary on WhatsApp (ERPNext attendance_report.send_attendance_report_to_whatsapp).
 * Runs at 00:30 for the previous day (attendance rows are written by PicoAttendanceProcessor at 23:59),
 * for every tenant with whatsapp.attendance_report_enabled=true, to whatsapp.attendance_report_to.
 *
 * <p>Text only, split into Twilio-sized messages. ERPNext's other variant published a PDF of every
 * employee's attendance as a public file and sent its URL; that is deliberately not reproduced.
 */
@Component
public class PicoAttendanceDigest {

    private static final Logger log = LoggerFactory.getLogger(PicoAttendanceDigest.class);

    private final JdbcTemplate jdbc;
    private final TenantIterator tenants;
    private final TenantSettings settings;
    private final IntegrationOutboundCaller outbound;

    public PicoAttendanceDigest(JdbcTemplate jdbc, TenantIterator tenants, TenantSettings settings, IntegrationOutboundCaller outbound) {
        this.jdbc = jdbc;
        this.tenants = tenants;
        this.settings = settings;
        this.outbound = outbound;
    }

    @Scheduled(cron = "${pico.attendance.digest.cron:0 30 0 * * *}")
    public void daily() {
        LocalDate day = LocalDate.now().minusDays(1);
        tenants.forEach(tenantId -> {
            if (!"true".equalsIgnoreCase(settings.get("whatsapp.attendance_report_enabled", "false"))) return;
            try {
                Map<String, Object> r = send(day, null);
                log.info("attendance digest tenant {} {}: {}", tenantId, day, r);
            } catch (Exception e) {
                log.warn("attendance digest failed for tenant {}: {}", tenantId, e.toString());
            }
        });
    }

    /** Sends the day's summary for TenantContext.get(); {@code to} null = the tenant setting. */
    public Map<String, Object> send(LocalDate day, List<String> to) {
        List<String> recipients = to != null && !to.isEmpty() ? to
                : Arrays.stream(settings.get("whatsapp.attendance_report_to", "").split(","))
                        .map(String::trim).filter(s -> !s.isEmpty()).toList();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("date", day.toString());
        if (recipients.isEmpty()) {
            out.put("sent", 0);
            out.put("error", "No recipients (whatsapp.attendance_report_to)");
            return out;
        }
        List<Long> ids = jdbc.queryForList(
                "SELECT id FROM integration_instance WHERE tenant_id=? AND connector_code=? AND status='ACTIVE' ORDER BY id LIMIT 1",
                Long.class, TenantContext.get(), TwilioWhatsappAdapter.CODE);
        if (ids.isEmpty()) {
            out.put("sent", 0);
            out.put("error", "No ACTIVE " + TwilioWhatsappAdapter.CODE + " integration");
            return out;
        }
        List<String> messages = messages(day);
        int sent = 0;
        List<String> failures = new ArrayList<>();
        for (String number : recipients) {
            for (String m : messages) {
                OutboundResult r = outbound.invoke(ids.get(0), "sendMessage", Map.of("to", number, "body", m));
                if (r.isOk()) sent++; else failures.add(number + ": " + r.getErrorCode());
            }
        }
        out.put("recipients", recipients.size());
        out.put("messages_per_recipient", messages.size());
        out.put("sent", sent);
        out.put("failures", failures);
        return out;
    }

    /** "Attendance Report - date" + one line per employee (code | name | shift | in | out | hrs | status), chunked. */
    List<String> messages(LocalDate day) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT e.emp_code, e.full_name, a.shift_code, a.in_time, a.out_time, a.worked_hours, a.status " +
                        "FROM m7_employee e LEFT JOIN m7_attendance a ON a.tenant_id = e.tenant_id AND a.employee_id = e.id AND a.date_of = ? " +
                        "WHERE e.tenant_id=? AND e.status='ACTIVE' ORDER BY e.emp_code",
                day, TenantContext.get());
        long present = rows.stream().filter(r -> r.get("status") != null).count();
        String header = "*Attendance Report - " + day + "*\nMarked: " + present + " / " + rows.size() + "\n\n";
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder(header);
        for (Map<String, Object> r : rows) {
            String line = r.get("emp_code") + " | " + r.get("full_name") + " | " + dash(r.get("shift_code")) + " | " + dash(r.get("in_time"))
                    + " | " + dash(r.get("out_time")) + " | " + dash(r.get("worked_hours")) + " | "
                    + (r.get("status") == null ? "ABSENT" : r.get("status")) + "\n";
            if (cur.length() + line.length() > TwilioWhatsappAdapter.MAX_BODY - 20) {
                out.add(cur.toString());
                cur = new StringBuilder("(contd.)\n");
            }
            cur.append(line);
        }
        out.add(cur.toString());
        return out;
    }

    private static String dash(Object o) {
        return o == null ? "-" : o.toString();
    }
}
