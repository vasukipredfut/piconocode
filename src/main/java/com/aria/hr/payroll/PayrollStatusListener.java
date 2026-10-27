package com.aria.hr.payroll;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Keeps m7_payslip and m7_overtime_entry in step with M7.PayrollRun (m7_payroll_run itself is
 * written by the projection):
 * <ul>
 *   <li>APPROVED -> m7_payslip.status APPROVED: the slip becomes visible to the employee
 *       (ERPNext only showed submitted slips)</li>
 *   <li>PAID     -> m7_payslip.status PAID; the month's OPEN m7_overtime_entry rows -> PAID with
 *       payslip_id, so OT is traceable to the slip that paid it</li>
 * </ul>
 */
@Component
public class PayrollStatusListener implements WorkflowTransitionListener {

    private final JdbcTemplate jdbc;

    public PayrollStatusListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!"M7.PayrollRun".equals(instance.getWorkflowCode())) return;
        String state = instance.getCurrentState();
        if (!"APPROVED".equals(state) && !"PAID".equals(state)) return;
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> runs = jdbc.queryForList(
                "SELECT employee_id, payroll_period FROM m7_payroll_run WHERE tenant_id=? AND workflow_instance_id=?",
                tenantId, instance.getId());
        if (runs.isEmpty()) return;
        Object empId = runs.get(0).get("employee_id");
        String period = (String) runs.get(0).get("payroll_period");
        jdbc.update("UPDATE m7_payslip SET status=? WHERE tenant_id=? AND emp_id=? AND payroll_period=?",
                state, tenantId, empId, period);
        if ("PAID".equals(state)) {
            jdbc.update("UPDATE m7_overtime_entry o JOIN m7_payslip p ON p.tenant_id=o.tenant_id AND p.emp_id=o.employee_id AND p.payroll_period=? " +
                            "SET o.status='PAID', o.payslip_id=p.id WHERE o.tenant_id=? AND o.employee_id=? AND o.status='OPEN' " +
                            "AND o.attendance_date BETWEEN ? AND LAST_DAY(?)",
                    period, tenantId, empId, period + "-01", period + "-01");
        }
    }
}
