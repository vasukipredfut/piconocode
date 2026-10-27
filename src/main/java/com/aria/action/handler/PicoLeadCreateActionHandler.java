package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.crm.CrmMails;
import com.aria.security.TenantContext;
import com.aria.tenant.TenantSettings;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * pico.lead-create -- replaces ERPNext create_lead + send_lead_notification_email.
 *
 * <p>args: {@code lead_name} (required), {@code company_name}, {@code email_id}, {@code mobile_no},
 * {@code territory}, {@code city_town}, {@code sales_person_emp_id}, {@code source},
 * {@code estimated_value}, {@code notes}. Any employee may create a lead (the creator comes from
 * the JWT). Sales person: from args, else the city's (m8_lead_city, ERPNext "Lead City"); it must
 * be an active sales person. Owner: tenant setting crm.lead_owner_emp_id (ERPNext "Lead Owner").
 * Mails the owner and the sales person unless they created the lead themselves.
 */
@Component
public class PicoLeadCreateActionHandler implements ActionHandler {

    public static final String CODE = "pico.lead-create";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private CrmMails mails;
    @Autowired private TenantSettings settings;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        String leadName = text(args, "lead_name", 256, true);
        String company = text(args, "company_name", 256, false);
        String email = text(args, "email_id", 128, false);
        String mobile = text(args, "mobile_no", 32, false);
        String territory = text(args, "territory", 64, false);
        String city = text(args, "city_town", 64, false);
        String source = text(args, "source", 32, false);
        String notes = text(args, "notes", 1024, false);
        if (email != null && !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) throw new IllegalArgumentException("email_id is not a valid email");
        BigDecimal estimated = null;
        if (args.get("estimated_value") != null && !args.get("estimated_value").toString().isBlank()) {
            try {
                estimated = new BigDecimal(args.get("estimated_value").toString().trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("estimated_value must be a number");
            }
        }

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long creatorId = ((Number) emp.get("id")).longValue();
        Long tenantId = TenantContext.get();

        Long salesPerson = args.get("sales_person_emp_id") == null ? null : id(args.get("sales_person_emp_id"), "sales_person_emp_id");
        if (salesPerson == null && city != null) {
            salesPerson = jdbc.queryForList("SELECT sales_person_emp_id FROM m8_lead_city WHERE tenant_id=? AND city_name=? AND is_active=1",
                    Long.class, tenantId, city).stream().filter(Objects::nonNull).findFirst().orElse(null);
        }
        if (salesPerson != null) {
            Integer ok = jdbc.queryForObject("SELECT COUNT(*) FROM m7_employee WHERE tenant_id=? AND id=? AND status='ACTIVE' AND is_sales_person=1",
                    Integer.class, tenantId, salesPerson);
            if (ok == null || ok == 0) return ActionResult.error("Employee " + salesPerson + " is not an active sales person");
        }
        Long owner = null;
        String ownerSetting = settings.get("crm.lead_owner_emp_id", null);
        if (ownerSetting != null) {
            try {
                owner = Long.valueOf(ownerSetting.trim());
            } catch (NumberFormatException ignored) { /* misconfigured setting: no owner */ }
        }

        Object[] values = {tenantId, "TMP-" + UUID.randomUUID().toString().substring(0, 24), leadName, leadName, mobile, email, source,
                estimated, owner, company, territory, city, salesPerson, creatorId, notes};
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m8_lead (tenant_id, lead_number, lead_name, contact_name, contact_phone, contact_email, source, estimated_value, " +
                            "owner_id, company_name, territory, city_town, sales_person_emp_id, created_by_emp_id, notes, stage, status, current_step) " +
                            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'NEW','NEW','NEW')",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        long leadId = keys.getKey().longValue();
        String number = String.format("LEAD-%d-%05d", LocalDate.now().getYear(), leadId);
        WorkflowInstance inst;
        try {
            inst = workflowEngine.startInstance("M8.Lead", Map.of("businessRef", "Lead#" + leadId));
        } catch (RuntimeException e) {
            jdbc.update("DELETE FROM m8_lead WHERE tenant_id=? AND id=?", tenantId, leadId);
            throw e;
        }
        jdbc.update("UPDATE m8_lead SET lead_number=?, workflow_instance_id=? WHERE tenant_id=? AND id=?", number, inst.getId(), tenantId, leadId);

        Map<String, Object> lead = jdbc.queryForMap("SELECT * FROM m8_lead WHERE tenant_id=? AND id=?", tenantId, leadId);
        if (owner != null && owner != creatorId) mails.newLead(lead, owner);
        if (salesPerson != null && salesPerson != creatorId && !salesPerson.equals(owner)) mails.newLead(lead, salesPerson);

        List<Map<String, Object>> dupes = mobile == null ? List.of() : jdbc.queryForList(
                "SELECT id, lead_number, stage FROM m8_lead WHERE tenant_id=? AND contact_phone=? AND id<>? AND stage NOT IN ('WON','LOST') LIMIT 5",
                tenantId, mobile, leadId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("lead_id", leadId);
        data.put("lead_number", number);
        data.put("stage", "NEW");
        data.put("sales_person_emp_id", salesPerson);
        data.put("workflow_instance_id", inst.getId());
        data.put("open_leads_with_same_mobile", dupes);
        return ActionResult.builder().ok(true).message("Lead created successfully").recordId(leadId).data(data).build();
    }

    static String text(Map<String, Object> args, String key, int max, boolean required) {
        String v = PicoSelf.str(args.get(key));
        if (v != null && v.isEmpty()) v = null;
        if (required && v == null) throw new IllegalArgumentException(key + " is required");
        if (v != null && v.length() > max) throw new IllegalArgumentException(key + " must be at most " + max + " characters");
        return v;
    }

    static Long id(Object raw, String key) {
        try {
            return Long.valueOf(raw.toString().trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(key + " must be a number");
        }
    }
}
