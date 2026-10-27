package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.hr.payroll.PayrollCalcService;
import com.aria.hr.payroll.PayslipController;
import com.aria.hr.payroll.PicoAttendanceSummaryBuilder;
import com.aria.security.CurrentRole;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowEngineService;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * pico.payroll-run -- runs payroll for a month (ERPNext Payroll Entry + Salary Slip creation, with
 * overrides/salary.py's OT). Manager / TenantAdmin only.
 *
 * <p>args: {@code period} YYYY-MM (required), {@code employee_ids} (optional subset),
 * {@code allow_open_period} (default false: the month must be over, else not-yet-worked days
 * would be LOP).
 *
 * <p>Per employee with a salary card: rebuild m7_attendance_summary, PayrollCalcService.compute,
 * upsert m7_payroll_run (+ replace its m7_payroll_line rows) and m7_payslip (HTML snapshot).
 * A new row gets an M7.PayrollRun instance which is submitted on aria:pr-compute (DRAFT ->
 * COMPUTED); a DRAFT row is advanced the same way; a COMPUTED row is recomputed in place.
 * APPROVED / PAID rows are never touched. One employee's failure does not stop the others.
 *
 * <p>Not @Transactional: the engine runs on its own transaction manager (as in GenericEntityController).
 */
@Component
public class PicoPayrollRunActionHandler implements ActionHandler {

    public static final String CODE = "pico.payroll-run";
    static final String WF_PAYROLL = "M7.PayrollRun";

    @Autowired private JdbcTemplate jdbc;
    @Autowired private PayrollCalcService calc;
    @Autowired private PayslipController payslips;
    @Autowired private PicoAttendanceSummaryBuilder summaries;
    @Autowired private WorkflowEngineService workflowEngine;

    @Override
    public String code() { return CODE; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        if (!CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN)) {
            return ActionResult.error("Payroll requires Manager or TenantAdmin");
        }
        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        YearMonth period = period(args.get("period"));
        if (!period.atEndOfMonth().isBefore(LocalDate.now()) && !Boolean.TRUE.equals(args.get("allow_open_period"))) {
            return ActionResult.error(period + " is not over yet; pass allow_open_period=true to compute it anyway");
        }
        List<Long> onlyIds = ids(args.get("employee_ids"));

        int computed = 0, skipped = 0;
        BigDecimal totalNet = BigDecimal.ZERO;
        List<Map<String, Object>> errors = new ArrayList<>();
        for (Map<String, Object> emp : summaries.payrollEmployees(period, onlyIds)) {
            long empId = ((Number) emp.get("id")).longValue();
            try {
                String outcome = runOne(emp, empId, period);
                if (outcome == null) {
                    computed++;
                } else if (outcome.startsWith("SKIP")) {
                    skipped++;
                } else {
                    errors.add(Map.of("employee_id", empId, "emp_code", String.valueOf(emp.get("emp_code")), "error", outcome));
                }
            } catch (RuntimeException e) {
                errors.add(Map.of("employee_id", empId, "emp_code", String.valueOf(emp.get("emp_code")),
                        "error", String.valueOf(e.getMessage())));
            }
        }
        BigDecimal net = jdbc.queryForObject(
                "SELECT COALESCE(SUM(net_pay),0) FROM m7_payroll_run WHERE tenant_id=? AND payroll_period=?",
                BigDecimal.class, TenantContext.get(), period.toString());
        if (net != null) totalNet = net;

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", period.toString());
        data.put("computed", computed);
        data.put("skipped_locked", skipped);
        data.put("failed", errors.size());
        data.put("errors", errors);
        data.put("total_net_pay", totalNet);
        return ActionResult.builder().ok(errors.isEmpty())
                .message(computed + " computed, " + skipped + " already approved / paid, " + errors.size() + " failed")
                .data(data)
                .build();
    }

    /** null = computed; "SKIP..." = locked; anything else = error message. */
    private String runOne(Map<String, Object> emp, long empId, YearMonth period) {
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id, status, workflow_instance_id FROM m7_payroll_run WHERE tenant_id=? AND payroll_period=? AND employee_id=?",
                tenantId, period.toString(), empId);
        String status = existing.isEmpty() ? null : (String) existing.get(0).get("status");
        if ("APPROVED".equals(status) || "PAID".equals(status)) return "SKIP " + status;

        Map<String, Object> summary = summaries.build(emp, period);
        PayrollCalcService.PayrollResult r = calc.compute(empId, period.toString());
        if (r.error != null && !r.error.isEmpty()) return r.error;

        Map<String, BigDecimal> byCode = new LinkedHashMap<>();
        for (Map<String, Object> l : r.lines) {
            byCode.merge((String) l.get("component_code"), (BigDecimal) l.get("amount"), BigDecimal::add);
        }
        BigDecimal other = r.totalDeduct.subtract(nz(byCode.get("PF_EMP"))).subtract(nz(byCode.get("ESI_EMP"))).subtract(nz(byCode.get("TDS")));
        Object[] values = {
                summary.get("present_days") == null ? null : ((BigDecimal) summary.get("present_days")).add((BigDecimal) summary.get("paid_leave_days")),
                nz(byCode.get("BASIC")), nz(byCode.get("HRA")), nz(byCode.get("CONVEYANCE")), r.otAmount, r.gross,
                nz(byCode.get("PF_EMP")), nz(byCode.get("ESI_EMP")), nz(byCode.get("TDS")), other, r.netPay,
                r.payableDays, r.lopDays, r.totalDeduct, CurrentUser.id()
        };
        Long runId;
        if (existing.isEmpty()) {
            jdbc.update("INSERT INTO m7_payroll_run (tenant_id, payroll_period, employee_id, worked_days, basic, hra, conveyance, ot_amount, " +
                            "gross, pf_employee, esi_employee, tds, other_deduction, net_pay, payable_days, lop_days, total_deduct, " +
                            "computed_by, computed_at, status, current_step) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),'DRAFT','DRAFT')",
                    prepend(new Object[]{tenantId, period.toString(), empId}, values));
            runId = jdbc.queryForObject("SELECT id FROM m7_payroll_run WHERE tenant_id=? AND payroll_period=? AND employee_id=?",
                    Long.class, tenantId, period.toString(), empId);
        } else {
            runId = ((Number) existing.get(0).get("id")).longValue();
            jdbc.update("UPDATE m7_payroll_run SET worked_days=?, basic=?, hra=?, conveyance=?, ot_amount=?, gross=?, pf_employee=?, " +
                            "esi_employee=?, tds=?, other_deduction=?, net_pay=?, payable_days=?, lop_days=?, total_deduct=?, computed_by=?, " +
                            "computed_at=NOW() WHERE tenant_id=? AND id=?",
                    append(values, new Object[]{tenantId, runId}));
        }

        jdbc.update("DELETE FROM m7_payroll_line WHERE tenant_id=? AND run_id=?", tenantId, runId);
        for (Map<String, Object> l : r.lines) {
            jdbc.update("INSERT INTO m7_payroll_line (tenant_id, run_id, emp_id, component_code, component_type, amount) VALUES (?,?,?,?,?,?)",
                    tenantId, runId, empId, l.get("component_code"), l.get("component_type"), l.get("amount"));
        }
        payslips.upsertPayslip(empId, period.toString(), r, payslips.snapshotHtml(empId, period.toString()));

        Object instanceId = existing.isEmpty() ? null : existing.get(0).get("workflow_instance_id");
        if (instanceId == null) {
            WorkflowInstance inst = workflowEngine.startInstance(WF_PAYROLL, Map.of("businessRef", "PayrollRun#" + runId));
            jdbc.update("UPDATE m7_payroll_run SET workflow_instance_id=? WHERE tenant_id=? AND id=?", inst.getId(), tenantId, runId);
            instanceId = inst.getId();
            status = inst.getCurrentState();
        }
        if (status == null || "DRAFT".equals(status)) {
            workflowEngine.handlePageSubmission(((Number) instanceId).longValue(), "aria:pr-compute",
                    Map.of("period", period.toString(), "net_pay", r.netPay.toPlainString()), CurrentUser.id());
        }
        return null;
    }

    static YearMonth period(Object raw) {
        String s = raw == null ? null : raw.toString().trim();
        if (s == null || !s.matches("\\d{4}-\\d{2}")) throw new IllegalArgumentException("period must be YYYY-MM");
        try {
            return YearMonth.parse(s);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("period must be YYYY-MM");
        }
    }

    static List<Long> ids(Object raw) {
        if (raw == null) return List.of();
        if (!(raw instanceof List<?> list)) throw new IllegalArgumentException("employee_ids must be an array of numbers");
        List<Long> out = new ArrayList<>();
        for (Object o : list) {
            try {
                out.add(Long.valueOf(o.toString().trim()));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("employee_ids must be an array of numbers");
            }
        }
        return out;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static Object[] prepend(Object[] head, Object[] tail) {
        Object[] out = new Object[head.length + tail.length];
        System.arraycopy(head, 0, out, 0, head.length);
        System.arraycopy(tail, 0, out, head.length, tail.length);
        return out;
    }

    private static Object[] append(Object[] head, Object[] tail) {
        return prepend(head, tail);
    }
}
