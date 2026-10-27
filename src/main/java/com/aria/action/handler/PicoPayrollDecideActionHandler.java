package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.CurrentRole;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.payroll-decide -- bulk M7.PayrollRun transitions for a month, so HR does not click through
 * every employee (ERPNext: submit Payroll Entry / Salary Slips).
 *
 * <p>args: {@code period} YYYY-MM, {@code decision} APPROVE | PAID, optional {@code employee_ids},
 * {@code pay_reference} (bank UTR / batch ref, required for PAID), {@code remarks}.
 * <ul>
 *   <li>APPROVE: COMPUTED rows -> APPROVED via aria:pr-approve (Manager / TenantAdmin)</li>
 *   <li>PAID:    APPROVED rows -> PAID via aria:pr-disburse (TenantAdmin)</li>
 * </ul>
 * Each row goes through the engine, so PayrollRunValidator, the projection and
 * PayrollStatusListener run exactly as for a single click.
 */
@Component
public class PicoPayrollDecideActionHandler implements ActionHandler {

    public static final String CODE = "pico.payroll-decide";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        YearMonth period = PicoPayrollRunActionHandler.period(args.get("period"));
        String decision = PicoSelf.str(args.get("decision"));
        String fromState, pageCode;
        if ("APPROVE".equals(decision)) {
            if (!CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN)) {
                return ActionResult.error("Approving payroll requires Manager or TenantAdmin");
            }
            fromState = "COMPUTED";
            pageCode = "aria:pr-approve";
        } else if ("PAID".equals(decision)) {
            if (!CurrentRole.isAny(jdbc, CurrentRole.TENANT_ADMIN)) return ActionResult.error("Marking payroll paid requires TenantAdmin");
            fromState = "APPROVED";
            pageCode = "aria:pr-disburse";
        } else {
            throw new IllegalArgumentException("decision must be APPROVE or PAID");
        }
        String payRef = PicoSelf.str(args.get("pay_reference"));
        if ("PAID".equals(decision) && (payRef == null || payRef.isEmpty())) {
            throw new IllegalArgumentException("pay_reference is required to mark payroll PAID");
        }
        if (payRef != null && payRef.length() > 64) throw new IllegalArgumentException("pay_reference must be at most 64 characters");

        List<Long> onlyIds = PicoPayrollRunActionHandler.ids(args.get("employee_ids"));
        String filter = onlyIds.isEmpty() ? ""
                : " AND employee_id IN (" + String.join(",", onlyIds.stream().map(String::valueOf).toList()) + ")";
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, employee_id, workflow_instance_id FROM m7_payroll_run WHERE tenant_id=? AND payroll_period=? " +
                        "AND status=? AND workflow_instance_id IS NOT NULL" + filter + " ORDER BY id",
                TenantContext.get(), period.toString(), fromState);

        Map<String, Object> form = new HashMap<>();
        form.put("decision", decision);
        form.put("remarks", PicoSelf.str(args.get("remarks")));
        if (payRef != null) form.put("pay_reference", payRef);
        int done = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        for (Map<String, Object> r : rows) {
            try {
                workflowEngine.handlePageSubmission(((Number) r.get("workflow_instance_id")).longValue(), pageCode, form, CurrentUser.id());
                done++;
            } catch (RuntimeException e) {
                errors.add(Map.of("employee_id", r.get("employee_id"), "error", String.valueOf(e.getMessage())));
            }
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", period.toString());
        data.put("decision", decision);
        data.put("moved", done);
        data.put("failed", errors.size());
        data.put("errors", errors);
        return ActionResult.builder().ok(errors.isEmpty())
                .message(done + " of " + rows.size() + " " + fromState + " row(s) moved")
                .data(data)
                .build();
    }
}
