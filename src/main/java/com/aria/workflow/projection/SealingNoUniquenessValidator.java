package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionValidator;
import com.aria.workflow.engine.WorkflowValidationException;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * A physical seal is used once — the same sealing_no must never be recorded
 * against two different gate passes. Runs pre-commit (V86) so a duplicate
 * gets a clear 422 instead of either a raw DB constraint error surfacing
 * from WorkflowProjectionListener's swallowed-exception path (V85's
 * sealing_process_detail UNIQUE(tenant_id, sealing_no) would otherwise fail
 * silently there) or, worse, no rejection at all.
 *
 * <p>Not scoped to a specific workflow_code or state — it only activates
 * when the submitted page actually carries a non-blank sealing_no, which
 * today is only aria:sealing-process.
 */
@Component
public class SealingNoUniquenessValidator implements WorkflowTransitionValidator {

    private final JdbcTemplate jdbc;

    public SealingNoUniquenessValidator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void validate(WorkflowInstance instance, String toState, Map<String, Object> formData) {
        if (formData == null) return;
        String sealingNo = firstNonBlank(str(formData, "sealing_no"), str(formData, "sealingNo"));
        if (sealingNo == null) return;

        Long tenantId = TenantContext.get();
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sealing_process_detail " +
                        "WHERE tenant_id=? AND sealing_no=? AND workflow_instance_id<>?",
                Integer.class, tenantId, sealingNo, instance.getId());
        if (count != null && count > 0) {
            throw new WorkflowValidationException(
                    "Seal number '" + sealingNo + "' is already recorded against another gate pass");
        }
    }

    private static String str(Map<String, Object> bag, String key) {
        Object v = bag.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
