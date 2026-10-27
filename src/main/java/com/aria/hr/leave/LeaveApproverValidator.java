package com.aria.hr.leave;

import com.aria.security.CurrentRole;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionValidator;
import com.aria.workflow.engine.WorkflowValidationException;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Who may move a leave. The engine only checks the guard, and assignedRole "Manager" is a queue
 * owner, not a permission -- so without this any logged-in user could approve any leave through
 * POST /api/workflow/transition. ERPNext only let the employee's leave_approver (or HR) decide.
 *
 * <ul>
 *   <li>M7.LeaveRequest -> APPROVED / REJECTED, M7.LeaveCancellation -> CANCELLED / DECLINED:
 *       the leave's approver (m7_leave_request.approver_emp_id -> m7_employee.user_id). Legacy rows
 *       without an approver: the Manager role, as v1 allowed.</li>
 *   <li>M7.LeaveRequest -> WITHDRAWN: the leave's own employee.</li>
 *   <li>TenantAdmin / SuperAdmin may always act (ERPNext HR Manager / System Manager).</li>
 * </ul>
 * Approving also re-checks overlap, in case another leave was approved meanwhile.
 */
@Component
public class LeaveApproverValidator implements WorkflowTransitionValidator {

    private static final Set<String> APPROVER_STATES = Set.of("APPROVED", "REJECTED", "CANCELLED", "DECLINED");

    private final JdbcTemplate jdbc;
    private final PicoLeaveRules rules;

    public LeaveApproverValidator(JdbcTemplate jdbc, PicoLeaveRules rules) {
        this.jdbc = jdbc;
        this.rules = rules;
    }

    @Override
    public void validate(WorkflowInstance instance, String toState, Map<String, Object> formData) {
        String code = instance.getWorkflowCode();
        boolean leaveFlow = PicoLeaveRules.WF_LEAVE.equals(code);
        if (!leaveFlow && !PicoLeaveRules.WF_CANCEL.equals(code)) return;

        Map<String, Object> leave = leaveFlow ? rules.leaveByInstance(instance.getId()) : rules.leaveByCancelInstance(instance.getId());
        if (leave == null) return; // instance started outside the PICO actions (e.g. aria:lr-apply) -- v1 behaviour
        Long actor = CurrentUser.id();
        if (actor == null) throw new WorkflowValidationException("Authentication required");
        if (CurrentRole.isAny(jdbc, CurrentRole.TENANT_ADMIN)) return;

        if ("WITHDRAWN".equals(toState)) {
            if (!actor.equals(rules.userIdOf(leave.get("employee_id")))) {
                throw new WorkflowValidationException("Only the employee can withdraw this leave");
            }
            return;
        }
        if (!APPROVER_STATES.contains(toState)) return;

        Object approver = leave.get("approver_emp_id");
        if (approver == null) {
            if (!CurrentRole.isAny(jdbc, CurrentRole.MANAGER)) throw new WorkflowValidationException("Only a Manager can decide this leave");
        } else if (!actor.equals(rules.userIdOf(approver))) {
            throw new WorkflowValidationException("Only the leave approver (" + rules.employeeName(approver) + ") can decide this leave");
        }

        if (leaveFlow && "APPROVED".equals(toState)) {
            Map<String, Object> clash = jdbc.queryForList(
                    "SELECT lr_number FROM m7_leave_request WHERE tenant_id=? AND employee_id=? AND id<>? AND status='APPROVED' " +
                            "AND COALESCE(cancel_status,'') <> 'CANCELLED' AND start_date <= ? AND end_date >= ? " +
                            "AND NOT (is_half_day=1 AND ?=1 AND which_half <> ?) LIMIT 1",
                    TenantContext.get(), leave.get("employee_id"), leave.get("id"), leave.get("end_date"), leave.get("start_date"),
                    leave.get("is_half_day"), leave.get("which_half")).stream().findFirst().orElse(null);
            if (clash != null) {
                throw new WorkflowValidationException("Approved leave " + clash.get("lr_number") + " already covers these dates");
            }
        }
    }
}
