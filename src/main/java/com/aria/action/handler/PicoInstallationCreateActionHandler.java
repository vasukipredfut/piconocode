package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.fieldservice.InstallationOtpService;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * pico.installation-create -- replaces ERPNext create_installation_report (+ send_otp_email /
 * send_otp_sms). The engineer comes from the JWT; the customer by {@code customer_id} or an exact
 * active {@code customer_name}. ERPNext's uw / amc / ow / o checkboxes are accepted and mapped to
 * warranty_kind. Starts M9.InstallationReport (OTP_PENDING) and sends the one-time code to the
 * report's email / contact number (InstallationOtpService).
 */
@Component
public class PicoInstallationCreateActionHandler implements ActionHandler {

    public static final String CODE = "pico.installation-create";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private InstallationOtpService otp;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> a = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        Long tenantId = TenantContext.get();

        Long customerId;
        if (a.get("customer_id") != null) {
            customerId = PicoLeadCreateActionHandler.id(a.get("customer_id"), "customer_id");
            Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM m8_customer WHERE tenant_id=? AND id=? AND is_active=1", Integer.class, tenantId, customerId);
            if (n == null || n == 0) return ActionResult.error("Customer not found");
        } else {
            String name = PicoLeadCreateActionHandler.text(a, "customer_name", 256, true);
            List<Long> ids = jdbc.queryForList("SELECT id FROM m8_customer WHERE tenant_id=? AND customer_name=? AND is_active=1", Long.class, tenantId, name);
            if (ids.isEmpty()) return ActionResult.error("Customer '" + name + "' not found");
            if (ids.size() > 1) return ActionResult.error("More than one customer is called '" + name + "'; send customer_id");
            customerId = ids.get(0);
        }
        Long ticketId = a.get("ticket_id") == null ? null : PicoLeadCreateActionHandler.id(a.get("ticket_id"), "ticket_id");
        if (ticketId != null) {
            Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM m9_service_ticket WHERE tenant_id=? AND id=?", Integer.class, tenantId, ticketId);
            if (n == null || n == 0) return ActionResult.error("Ticket not found");
        }
        String email = PicoLeadCreateActionHandler.text(a, "email", 128, false);
        if (email != null && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new IllegalArgumentException("email is not a valid email");
        String mobile = PicoLeadCreateActionHandler.text(a, "contact_number", 32, false);
        if (email == null && mobile == null) throw new IllegalArgumentException("email or contact_number is required to send the customer's code");

        Object[] values = {
                tenantId, "TMP-" + UUID.randomUUID().toString().substring(0, 24),
                PicoLeadCreateActionHandler.text(a, "doc_no", 64, false), date(a, "date", LocalDate.now()),
                emp.get("id"), customerId, ticketId,
                PicoLeadCreateActionHandler.text(a, "address", 512, false),
                PicoLeadCreateActionHandler.text(a, "contact_person_name", 128, false), mobile, email, warranty(a),
                date(a, "call_receipt_date", null), PicoLeadCreateActionHandler.text(a, "nature_of_call", 140, false),
                date(a, "call_date_from", null), date(a, "call_date_to", a.get("to") == null ? null : date(a, "to", null)),
                PicoLeadCreateActionHandler.text(a, "machine_type", 140, false), PicoLeadCreateActionHandler.text(a, "machine_model", 140, false),
                longText(a, "primary_communication", "brief_of_primary_communication_with_or_by_customer"),
                longText(a, "pre_work_observation", null), longText(a, "action_summary", null), longText(a, "remarks_by_customer", null),
                flag(a.get("work_done_nature")), flag(a.get("system_operation")), flag(a.get("suggestions_for_trouble")), flag(a.get("purpose_reason"))
        };
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m9_installation_report (tenant_id, report_number, doc_no, report_date, engineer_emp_id, customer_id, ticket_id, " +
                            "address, contact_person_name, contact_number, email, warranty_kind, call_receipt_date, nature_of_call, call_date_from, " +
                            "call_date_to, machine_type, machine_model, primary_communication, pre_work_observation, action_summary, " +
                            "remarks_by_customer, work_done_nature, system_operation, suggestions_for_trouble, purpose_reason, status, current_step) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'OTP_PENDING','OTP_PENDING')",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        long id = keys.getKey().longValue();
        String number = String.format("IR-%d-%05d", LocalDate.now().getYear(), id);
        WorkflowInstance inst;
        try {
            inst = workflowEngine.startInstance("M9.InstallationReport", Map.of("businessRef", "InstallationReport#" + id));
        } catch (RuntimeException e) {
            jdbc.update("DELETE FROM m9_installation_report WHERE tenant_id=? AND id=?", tenantId, id);
            throw e;
        }
        jdbc.update("UPDATE m9_installation_report SET report_number=?, workflow_instance_id=? WHERE tenant_id=? AND id=?",
                number, inst.getId(), tenantId, id);

        List<String> sentTo = otp.issue(jdbc.queryForMap("SELECT * FROM m9_installation_report WHERE tenant_id=? AND id=?", tenantId, id));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("report_id", id);
        data.put("report_number", number);
        data.put("status", "OTP_PENDING");
        data.put("otp_sent_to", sentTo);
        data.put("otp_valid_minutes", otp.ttlMinutes());
        data.put("workflow_instance_id", inst.getId());
        return ActionResult.builder().ok(true)
                .message(sentTo.isEmpty()
                        ? "Installation Report created, but the code could not be sent (check email / SMS setup); use resend"
                        : "Installation Report Created & OTP Sent")
                .recordId(id).data(data).build();
    }

    /** ERPNext sent four checkboxes (uw / amc / ow / o); Aria keeps one warranty_kind. */
    private static String warranty(Map<String, Object> a) {
        String w = PicoSelf.str(a.get("warranty_kind"));
        if (w != null && !w.isEmpty()) {
            w = w.toUpperCase();
            if (!w.matches("UW|AMC|OW|O")) throw new IllegalArgumentException("warranty_kind must be UW, AMC, OW or O");
            return w;
        }
        if (flag(a.get("uw")) == 1) return "UW";
        if (flag(a.get("amc")) == 1) return "AMC";
        if (flag(a.get("ow")) == 1) return "OW";
        if (flag(a.get("o")) == 1) return "O";
        return null;
    }

    private static int flag(Object o) {
        if (o instanceof Boolean b) return b ? 1 : 0;
        if (o instanceof Number n) return n.intValue() != 0 ? 1 : 0;
        return o != null && ("1".equals(o.toString().trim()) || "true".equalsIgnoreCase(o.toString().trim())) ? 1 : 0;
    }

    private static String longText(Map<String, Object> a, String key, String erpKey) {
        Object v = a.get(key) != null ? a.get(key) : (erpKey == null ? null : a.get(erpKey));
        String s = v == null ? null : v.toString().trim();
        if (s != null && s.length() > 10000) throw new IllegalArgumentException(key + " must be at most 10000 characters");
        return s == null || s.isEmpty() ? null : s;
    }

    private static LocalDate date(Map<String, Object> a, String key, LocalDate fallback) {
        Object v = a.get(key);
        if (v == null || v.toString().isBlank()) return fallback;
        try {
            String s = v.toString().trim();
            return LocalDate.parse(s.length() > 10 ? s.substring(0, 10) : s);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(key + " must be YYYY-MM-DD");
        }
    }
}
