package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.crm.CrmAccess;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * pico.lead-convert -- replaces ERPNext convert_lead_to_customer: creates the m8_customer from the
 * lead (name = company name, else lead name), links lead.customer_id and moves the lead to WON via
 * pico:lead-convert. Converting twice returns the existing customer.
 *
 * <p>args: {@code lead_id} (required), {@code customer_type} (COMPANY | INDIVIDUAL, default
 * COMPANY as in ERPNext), {@code gstin}, {@code address}.
 */
@Component
public class PicoLeadConvertActionHandler implements ActionHandler {

    public static final String CODE = "pico.lead-convert";
    private static final Set<String> OPEN = Set.of("NEW", "CONTACTED", "QUALIFIED", "QUOTED");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private CrmAccess access;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        if (args.get("lead_id") == null) throw new IllegalArgumentException("lead_id is required");
        long leadId = PicoLeadCreateActionHandler.id(args.get("lead_id"), "lead_id");
        String type = PicoLeadCreateActionHandler.text(args, "customer_type", 16, false);
        type = type == null ? "COMPANY" : type.toUpperCase();
        if (!"COMPANY".equals(type) && !"INDIVIDUAL".equals(type)) throw new IllegalArgumentException("customer_type must be COMPANY or INDIVIDUAL");
        String gstin = PicoLeadCreateActionHandler.text(args, "gstin", 16, false);
        if (gstin != null && !gstin.matches("[0-9]{2}[A-Z0-9]{13}")) throw new IllegalArgumentException("gstin must be 15 characters (state code + PAN + ...)");
        String address = PicoLeadCreateActionHandler.text(args, "address", 512, false);

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long employeeId = ((Number) emp.get("id")).longValue();
        Map<String, Object> lead = access.lead(leadId);
        if (lead == null || !access.canSeeLead(lead, employeeId)) return ActionResult.error("Lead not found");
        Long tenantId = TenantContext.get();

        if (lead.get("customer_id") != null) {
            Map<String, Object> existing = jdbc.queryForMap("SELECT id, customer_code, customer_name FROM m8_customer WHERE tenant_id=? AND id=?",
                    tenantId, lead.get("customer_id"));
            return ActionResult.builder().ok(true).message("Lead already converted").recordId(existing.get("id"))
                    .data(Map.of("customer_id", existing.get("id"), "customer_code", existing.get("customer_code"),
                            "customer_name", existing.get("customer_name"), "stage", lead.get("stage")))
                    .build();
        }
        if (!OPEN.contains((String) lead.get("stage"))) return ActionResult.error("Lead is already " + lead.get("stage"));
        if (lead.get("workflow_instance_id") == null) return ActionResult.error("Lead has no workflow instance");

        String name = lead.get("company_name") != null && "COMPANY".equals(type) ? (String) lead.get("company_name") : (String) lead.get("lead_name");
        Object[] values = {tenantId, "TMP-" + UUID.randomUUID().toString().substring(0, 24), name, type, lead.get("lead_name"),
                lead.get("contact_phone"), lead.get("contact_email"), gstin, address, lead.get("territory"), lead.get("city_town"),
                lead.get("sales_person_emp_id"), leadId, employeeId};
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO m8_customer (tenant_id, customer_code, customer_name, customer_type, contact_name, mobile_no, email_id, gstin, " +
                            "address, territory, city_town, sales_person_emp_id, lead_id, created_by_emp_id, source) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,'LEAD')",
                    Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.length; i++) ps.setObject(i + 1, values[i]);
            return ps;
        }, keys);
        long customerId = keys.getKey().longValue();
        String code = String.format("CUST-%05d", customerId);
        jdbc.update("UPDATE m8_customer SET customer_code=? WHERE tenant_id=? AND id=?", code, tenantId, customerId);
        jdbc.update("UPDATE m8_lead SET customer_id=? WHERE tenant_id=? AND id=?", customerId, tenantId, leadId);
        try {
            workflowEngine.handlePageSubmission(((Number) lead.get("workflow_instance_id")).longValue(), "pico:lead-convert",
                    Map.of("customer_code", code), CurrentUser.id());
        } catch (RuntimeException e) {
            jdbc.update("UPDATE m8_lead SET customer_id=NULL WHERE tenant_id=? AND id=?", tenantId, leadId);
            jdbc.update("DELETE FROM m8_customer WHERE tenant_id=? AND id=?", tenantId, customerId);
            return ActionResult.error("Could not convert lead: " + e.getMessage());
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("lead_id", leadId);
        data.put("customer_id", customerId);
        data.put("customer_code", code);
        data.put("customer_name", name);
        data.put("stage", "WON");
        return ActionResult.builder().ok(true).message("Lead converted successfully").recordId(customerId).data(data).build();
    }
}
