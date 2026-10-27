package com.aria.crm;

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
 * Who may move a lead (M8.Lead) or quote (M8.Quote); the engine checks no permission, and ERPNext's
 * update / convert / quote APIs were all allow_guest=True. Allowed: CRM admins (Manager /
 * TenantAdmin) and the people on the lead -- its sales person, creator or owner; for a quote also
 * its creator and the customer's sales person (CrmAccess.canSeeQuote).
 */
@Component
public class CrmAccessValidator implements WorkflowTransitionValidator {

    private final JdbcTemplate jdbc;
    private final CrmAccess access;

    public CrmAccessValidator(JdbcTemplate jdbc, CrmAccess access) {
        this.jdbc = jdbc;
        this.access = access;
    }

    @Override
    public void validate(WorkflowInstance instance, String toState, Map<String, Object> formData) {
        String code = instance.getWorkflowCode();
        boolean leadFlow = "M8.Lead".equals(code);
        if (!leadFlow && !"M8.Quote".equals(code)) return;
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT * FROM " + (leadFlow ? "m8_lead" : "m8_quote") + " WHERE tenant_id=? AND workflow_instance_id=?",
                TenantContext.get(), instance.getId());
        if (rows.isEmpty()) return; // started outside the PICO actions -- v1 behaviour
        if (access.isAdmin()) return;
        Long actor = CurrentUser.id();
        if (actor == null) throw new WorkflowValidationException("Authentication required");
        List<Long> self = jdbc.queryForList("SELECT id FROM m7_employee WHERE tenant_id=? AND user_id=?",
                Long.class, TenantContext.get(), actor);
        if (self.isEmpty()) throw new WorkflowValidationException("Your login is not linked to an employee");
        long employeeId = self.get(0);
        boolean allowed = leadFlow ? access.isOnLead(rows.get(0), employeeId) : access.canSeeQuote(rows.get(0), employeeId);
        if (!allowed) {
            throw new WorkflowValidationException("Only the " + (leadFlow ? "lead" : "quotation")
                    + "'s sales person, creator or a Manager can change it");
        }
    }
}
