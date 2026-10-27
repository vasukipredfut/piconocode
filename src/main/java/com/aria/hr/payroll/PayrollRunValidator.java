package com.aria.hr.payroll;

import com.aria.security.CurrentRole;
import com.aria.workflow.engine.WorkflowTransitionValidator;
import com.aria.workflow.engine.WorkflowValidationException;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Who may move payroll (M7.PayrollRun): the engine checks no permission, so without this any
 * logged-in user could approve or mark salaries paid through POST /api/workflow/transition.
 * <ul>
 *   <li>-> COMPUTED, -> APPROVED: Manager or TenantAdmin</li>
 *   <li>-> PAID: TenantAdmin (money has left the bank)</li>
 * </ul>
 * SuperAdmin always passes (CurrentRole.isAny).
 */
@Component
public class PayrollRunValidator implements WorkflowTransitionValidator {

    private final JdbcTemplate jdbc;

    public PayrollRunValidator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void validate(WorkflowInstance instance, String toState, Map<String, Object> formData) {
        if (!"M7.PayrollRun".equals(instance.getWorkflowCode())) return;
        boolean allowed = "PAID".equals(toState)
                ? CurrentRole.isAny(jdbc, CurrentRole.TENANT_ADMIN)
                : CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN);
        if (!allowed) {
            throw new WorkflowValidationException("PAID".equals(toState)
                    ? "Only TenantAdmin can mark payroll paid"
                    : "Only Manager or TenantAdmin can move payroll to " + toState);
        }
    }
}
