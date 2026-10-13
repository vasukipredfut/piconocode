package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionValidator;
import com.aria.workflow.engine.WorkflowValidationException;
import com.aria.workflow.model.WorkflowInstance;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Gate closure seal check for M10.TankerOutbound (aria:gate-pass-closure-form,
 * {@code READY_FOR_EXIT -> GATE_PASS_OUT_CLOSED}). The gate officer types the seal number they see on the
 * vehicle ({@code entered_seal_no}) and picks a {@code seal_verified} outcome:
 * <ul>
 *   <li>{@code NA} (no seal, e.g. auto lock) -- nothing to check;</li>
 *   <li>{@code MISMATCH...} -- the officer says the seal differs; allowed, but the replacement seal
 *       ({@code new_seal_no}) must be entered;</li>
 *   <li>anything else (MATCHED) -- the entered seal must equal the seal recorded for THIS gate pass, else the
 *       transition is refused with a 422 and the state does not change.</li>
 * </ul>
 * The recorded seal is {@code weighment_capture_detail.sealing_no} (aria:weighment-capture), falling back to
 * {@code sealing_process_detail.sealing_no} (aria:sealing-process). Comparison ignores case and surrounding
 * spaces.
 */
@Component
public class GateClosureSealValidator implements WorkflowTransitionValidator {

    private static final String WORKFLOW_CODE = "M10.TankerOutbound";
    private static final String TO_STATE = "GATE_PASS_OUT_CLOSED";

    private final JdbcTemplate jdbc;

    public GateClosureSealValidator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void validate(WorkflowInstance instance, String toState, Map<String, Object> formData) {
        if (!WORKFLOW_CODE.equals(instance.getWorkflowCode()) || !TO_STATE.equals(toState)) return;
        if (formData == null) return;

        String verified = firstNonBlank(str(formData, "seal_verified"), str(formData, "sealVerified"));
        if (verified == null) return;                       // page did not carry the seal check
        verified = verified.trim().toUpperCase();
        if ("NA".equals(verified)) return;

        if (verified.startsWith("MISMATCH")) {
            if (firstNonBlank(str(formData, "new_seal_no"), str(formData, "newSealNo")) == null) {
                throw new WorkflowValidationException("Seal mismatch reported - enter the new seal number");
            }
            return;
        }

        String entered = firstNonBlank(str(formData, "entered_seal_no"), str(formData, "enteredSealNo"));
        if (entered == null) {
            throw new WorkflowValidationException("Enter the seal number to verify it against the recorded seal");
        }

        String recorded = recordedSeal(TenantContext.get(), instance.getId());
        if (recorded == null) {
            throw new WorkflowValidationException(
                    "No seal number is recorded for this gate pass to verify against - choose 'NA' or report a mismatch");
        }
        if (!recorded.trim().equalsIgnoreCase(entered.trim())) {
            throw new WorkflowValidationException(
                    "Seal number mismatch: entered '" + entered.trim() + "' but recorded '" + recorded.trim()
                            + "'. Report a mismatch and enter the new seal number if the seal was changed");
        }
    }

    private String recordedSeal(Long tenantId, Long instanceId) {
        for (String table : List.of("weighment_capture_detail", "sealing_process_detail")) {
            List<String> rows = jdbc.queryForList(
                    "SELECT sealing_no FROM " + table + " WHERE tenant_id = ? AND workflow_instance_id = ? "
                            + "AND sealing_no IS NOT NULL AND sealing_no <> '' ORDER BY id DESC LIMIT 1",
                    String.class, tenantId, instanceId);
            if (!rows.isEmpty() && rows.get(0) != null && !rows.get(0).isBlank()) return rows.get(0);
        }
        return null;
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
