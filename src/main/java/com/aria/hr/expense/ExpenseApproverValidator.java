package com.aria.hr.expense;

import com.aria.hr.leave.PicoLeaveRules;
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

/**
 * Who may move an expense claim (M7.ExpenseClaim); the engine itself checks no permission.
 * ERPNext's approve_reject_expense_claim was allow_guest=True: anyone could approve any claim.
 * <ul>
 *   <li>-> APPROVED / REJECTED: the claim's approver (approver_emp_id -> m7_employee.user_id); legacy
 *       claims without one: Manager. Never the claimant themself.</li>
 *   <li>-> WITHDRAWN: the claimant.</li>
 *   <li>-> PAID: TenantAdmin (finance), with a pay_reference.</li>
 * </ul>
 * TenantAdmin / SuperAdmin may approve / reject any claim except their own.
 */
@Component
public class ExpenseApproverValidator implements WorkflowTransitionValidator {

    public static final String WF_EXPENSE = "M7.ExpenseClaim";

    private final JdbcTemplate jdbc;
    private final PicoLeaveRules people;

    public ExpenseApproverValidator(JdbcTemplate jdbc, PicoLeaveRules people) {
        this.jdbc = jdbc;
        this.people = people;
    }

    @Override
    public void validate(WorkflowInstance instance, String toState, Map<String, Object> formData) {
        if (!WF_EXPENSE.equals(instance.getWorkflowCode())) return;
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT employee_id, approver_emp_id FROM m7_expense_claim WHERE tenant_id=? AND workflow_instance_id=?",
                TenantContext.get(), instance.getId());
        if (rows.isEmpty()) return;
        Map<String, Object> claim = rows.get(0);
        Long actor = CurrentUser.id();
        if (actor == null) throw new WorkflowValidationException("Authentication required");
        boolean isClaimant = actor.equals(people.userIdOf(claim.get("employee_id")));

        switch (toState) {
            case "WITHDRAWN" -> {
                if (!isClaimant && !CurrentRole.isAny(jdbc, CurrentRole.TENANT_ADMIN)) {
                    throw new WorkflowValidationException("Only the employee can withdraw this claim");
                }
            }
            case "PAID" -> {
                if (!CurrentRole.isAny(jdbc, CurrentRole.TENANT_ADMIN)) throw new WorkflowValidationException("Only TenantAdmin can record a reimbursement");
                Object ref = formData == null ? null : formData.get("pay_reference");
                if (ref == null || ref.toString().isBlank()) throw new WorkflowValidationException("pay_reference is required");
                if (ref.toString().length() > 64) throw new WorkflowValidationException("pay_reference must be at most 64 characters");
            }
            case "APPROVED", "REJECTED" -> {
                if (isClaimant) throw new WorkflowValidationException("You cannot decide your own expense claim");
                if (CurrentRole.isAny(jdbc, CurrentRole.TENANT_ADMIN)) return;
                Object approver = claim.get("approver_emp_id");
                if (approver == null) {
                    if (!CurrentRole.isAny(jdbc, CurrentRole.MANAGER)) throw new WorkflowValidationException("Only a Manager can decide this claim");
                } else if (!actor.equals(people.userIdOf(approver))) {
                    throw new WorkflowValidationException("Only the expense approver (" + people.employeeName(approver) + ") can decide this claim");
                }
            }
            default -> { }
        }
    }
}
