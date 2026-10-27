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
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * pico.lead-update -- replaces ERPNext update_lead_qualification_status, plus the follow-up fields
 * the app needs. Only people on the lead (sales person / creator / owner) or a CRM admin.
 *
 * <p>args: {@code lead_id} (required); any of {@code qualification_status}
 * (UNQUALIFIED | IN_PROCESS | QUALIFIED), {@code next_action_on}, {@code notes},
 * {@code sales_person_emp_id} (CRM admin only), {@code stage} (CONTACTED | LOST, with {@code lost_reason}).
 * QUALIFIED on a NEW / CONTACTED lead also moves it to QUALIFIED (pico:lead-qualify).
 */
@Component
public class PicoLeadUpdateActionHandler implements ActionHandler {

    public static final String CODE = "pico.lead-update";
    private static final Set<String> QUALIFICATIONS = Set.of("UNQUALIFIED", "IN_PROCESS", "QUALIFIED");
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
        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        long employeeId = ((Number) emp.get("id")).longValue();
        Map<String, Object> lead = access.lead(leadId);
        if (lead == null || !access.canSeeLead(lead, employeeId)) return ActionResult.error("Lead not found");
        String stage = (String) lead.get("stage");
        Long tenantId = TenantContext.get();

        String qualification = PicoLeadCreateActionHandler.text(args, "qualification_status", 16, false);
        if (qualification != null) {
            qualification = qualification.toUpperCase().replace(' ', '_');
            if (!QUALIFICATIONS.contains(qualification)) throw new IllegalArgumentException("qualification_status must be UNQUALIFIED, IN_PROCESS or QUALIFIED");
        }
        String notes = PicoLeadCreateActionHandler.text(args, "notes", 1024, false);
        String target = PicoLeadCreateActionHandler.text(args, "stage", 16, false);
        if (target != null && !"CONTACTED".equals(target) && !"LOST".equals(target)) throw new IllegalArgumentException("stage must be CONTACTED or LOST");
        String lostReason = PicoLeadCreateActionHandler.text(args, "lost_reason", 512, false);
        if ("LOST".equals(target) && lostReason == null) throw new IllegalArgumentException("lost_reason is required to close a lead as LOST");
        LocalDate nextAction = null;
        if (args.get("next_action_on") != null && !args.get("next_action_on").toString().isBlank()) {
            try {
                nextAction = LocalDate.parse(args.get("next_action_on").toString().trim().substring(0, 10));
            } catch (RuntimeException e) {
                throw new IllegalArgumentException("next_action_on must be YYYY-MM-DD");
            }
        }
        Long salesPerson = null;
        if (args.get("sales_person_emp_id") != null) {
            if (!access.isAdmin()) return ActionResult.error("Only a Manager can reassign a lead");
            salesPerson = PicoLeadCreateActionHandler.id(args.get("sales_person_emp_id"), "sales_person_emp_id");
            Integer ok = jdbc.queryForObject("SELECT COUNT(*) FROM m7_employee WHERE tenant_id=? AND id=? AND status='ACTIVE' AND is_sales_person=1",
                    Integer.class, tenantId, salesPerson);
            if (ok == null || ok == 0) return ActionResult.error("Employee " + salesPerson + " is not an active sales person");
        }
        if ((target != null || qualification != null || salesPerson != null) && !OPEN.contains(stage)) {
            return ActionResult.error("Lead is already " + stage);
        }

        List<String> sets = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        if (qualification != null) { sets.add("qualification_status=?"); params.add(qualification); }
        if (notes != null) { sets.add("notes=?"); params.add(notes); }
        if (nextAction != null) { sets.add("next_action_on=?"); params.add(nextAction); }
        if (salesPerson != null) { sets.add("sales_person_emp_id=?"); params.add(salesPerson); }
        if (!sets.isEmpty()) {
            params.add(tenantId);
            params.add(leadId);
            jdbc.update("UPDATE m8_lead SET " + String.join(", ", sets) + " WHERE tenant_id=? AND id=?", params.toArray());
        }

        String page = null;
        Map<String, Object> form = new HashMap<>();
        if ("LOST".equals(target)) {
            page = "pico:lead-lost";
            form.put("lost_reason", lostReason);
        } else if ("CONTACTED".equals(target)) {
            if (!"NEW".equals(stage)) return ActionResult.error("Only a NEW lead can be marked CONTACTED");
            page = "aria:lead-contact";
        } else if ("QUALIFIED".equals(qualification) && ("NEW".equals(stage) || "CONTACTED".equals(stage))) {
            page = "pico:lead-qualify";
            form.put("notes", notes);
        }
        if (page != null) {
            if (lead.get("workflow_instance_id") == null) return ActionResult.error("Lead has no workflow instance");
            try {
                workflowEngine.handlePageSubmission(((Number) lead.get("workflow_instance_id")).longValue(), page, form, CurrentUser.id());
            } catch (RuntimeException e) {
                return ActionResult.error("Lead fields saved, but the stage could not change: " + e.getMessage());
            }
        }

        Map<String, Object> fresh = access.lead(leadId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("lead_id", leadId);
        data.put("previous_stage", stage);
        data.put("stage", fresh.get("stage"));
        data.put("qualification_status", fresh.get("qualification_status"));
        return ActionResult.builder().ok(true).message("Lead updated").recordId(leadId).data(data).build();
    }
}
