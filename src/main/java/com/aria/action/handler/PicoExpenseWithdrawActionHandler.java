package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.expense-withdraw -- the employee withdraws their own SUBMITTED claim
 * (M7.ExpenseClaim SUBMITTED -> WITHDRAWN via pico:expense-withdraw). ERPNext had no API for this.
 * args: {@code claim_id} (required), {@code reason}.
 */
@Component
public class PicoExpenseWithdrawActionHandler implements ActionHandler {

    public static final String CODE = "pico.expense-withdraw";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        long claimId;
        try {
            claimId = Long.parseLong(String.valueOf(args.get("claim_id")).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("claim_id is required");
        }
        String reason = PicoSelf.str(args.get("reason"));
        if (reason != null && reason.length() > 512) throw new IllegalArgumentException("reason must be at most 512 characters");

        Map<String, Object> emp = PicoSelf.employee(jdbc);
        if (emp == null) return ActionResult.error("Employee not linked to this user");
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT status, workflow_instance_id FROM m7_expense_claim WHERE tenant_id=? AND id=? AND employee_id=?",
                TenantContext.get(), claimId, emp.get("id"));
        if (rows.isEmpty()) return ActionResult.error("Expense claim not found");
        if (!"SUBMITTED".equals(rows.get(0).get("status"))) return ActionResult.error("Claim is already " + rows.get(0).get("status"));
        Object instanceId = rows.get(0).get("workflow_instance_id");
        if (instanceId == null) return ActionResult.error("Claim has no workflow instance; ask HR to withdraw it");

        Map<String, Object> form = new HashMap<>();
        form.put("reason", reason);
        try {
            workflowEngine.handlePageSubmission(((Number) instanceId).longValue(), "pico:expense-withdraw", form, CurrentUser.id());
        } catch (RuntimeException e) {
            return ActionResult.error("Could not withdraw claim: " + e.getMessage());
        }
        return ActionResult.builder().ok(true).message("Expense claim withdrawn").recordId(claimId)
                .data(Map.of("status", "WITHDRAWN")).build();
    }
}
