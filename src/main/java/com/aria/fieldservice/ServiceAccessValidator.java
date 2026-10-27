package com.aria.fieldservice;

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
 * Who may move service records; the engine checks no permission.
 * <ul>
 *   <li>M9.InstallationReport -> VERIFIED: only after pico.installation-otp stamped
 *       otp_verified_at (the OTP check cannot be a guard -- guards have no DB access -- so a direct
 *       POST /api/workflow/transition would otherwise skip it).</li>
 *   <li>M9.InstallationReport -> CANCELLED: the report's engineer or Manager / TenantAdmin.</li>
 *   <li>M9.ServiceTicket (any move): Manager / TenantAdmin, the assigned technician, or role Service.</li>
 * </ul>
 */
@Component
public class ServiceAccessValidator implements WorkflowTransitionValidator {

    public static final String SERVICE_ROLE = "Service";

    private final JdbcTemplate jdbc;

    public ServiceAccessValidator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void validate(WorkflowInstance instance, String toState, Map<String, Object> formData) {
        String code = instance.getWorkflowCode();
        if ("M9.InstallationReport".equals(code)) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT engineer_emp_id, otp_verified_at FROM m9_installation_report WHERE tenant_id=? AND workflow_instance_id=?",
                    TenantContext.get(), instance.getId());
            if (rows.isEmpty()) return;
            if ("VERIFIED".equals(toState)) {
                if (rows.get(0).get("otp_verified_at") == null) {
                    throw new WorkflowValidationException("The customer's one-time code has not been verified (use pico.installation-otp)");
                }
                return;
            }
            if (CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN)) return;
            if (!isEmployee(rows.get(0).get("engineer_emp_id"))) {
                throw new WorkflowValidationException("Only the report's engineer or a Manager can cancel it");
            }
            return;
        }
        if ("M9.ServiceTicket".equals(code)) {
            if (CurrentRole.isAny(jdbc, CurrentRole.MANAGER, CurrentRole.TENANT_ADMIN, SERVICE_ROLE)) return;
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT technician_id FROM m9_service_ticket WHERE tenant_id=? AND workflow_instance_id=?",
                    TenantContext.get(), instance.getId());
            if (rows.isEmpty()) return;
            if (!isEmployee(rows.get(0).get("technician_id"))) {
                throw new WorkflowValidationException("Only the assigned technician, the Service team or a Manager can update this ticket");
            }
        }
    }

    private boolean isEmployee(Object empId) {
        Long actor = CurrentUser.id();
        if (actor == null || empId == null) return false;
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM m7_employee WHERE tenant_id=? AND id=? AND user_id=?",
                Integer.class, TenantContext.get(), empId, actor);
        return n != null && n > 0;
    }
}
