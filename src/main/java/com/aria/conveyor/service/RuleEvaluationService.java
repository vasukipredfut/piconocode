package com.aria.conveyor.service;

import com.aria.conveyor.dto.RuleDefinition;
import com.aria.conveyor.dto.RuleEvalResult;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.SpelGuardEvaluator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Rule Builder evaluation — Order/SKU/Quantity/Packing/Dispatch validation
 * rules stored as data ({@code rule_definition.expression}, a SpEL boolean
 * expression) and evaluated via the existing
 * {@link com.aria.workflow.engine.SpelGuardEvaluator} (already used for
 * workflow transition guards; its {@code SimpleEvaluationContext} blocks
 * {@code T(...)}/reflection/constructors, safe for tenant-authored rules).
 *
 * <p>Split into a pure {@link #evaluate} (no DB — unit-testable) and a
 * DB-backed {@link #loadAndEvaluate} wrapper that loads the rule set and
 * writes the audit log row.
 */
@Service
public class RuleEvaluationService {

    private static final Logger log = LoggerFactory.getLogger(RuleEvaluationService.class);

    private final SpelGuardEvaluator guardEvaluator;
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public RuleEvaluationService(SpelGuardEvaluator guardEvaluator, JdbcTemplate jdbc) {
        this.guardEvaluator = guardEvaluator;
        this.jdbc = jdbc;
    }

    /**
     * Pure: evaluate an already-loaded rule list (expected pre-ordered by
     * sequence_no) against a scan context. First failing BLOCKING rule wins;
     * WARNING rules never block. A rule that errors (bad expression, missing
     * context key) is treated as a failure — an unevaluable rule must never
     * silently let a carton through.
     */
    public RuleEvalResult evaluate(List<RuleDefinition> rules, Map<String, Object> context) {
        SpelGuardEvaluator.InstanceView stubView = new SpelGuardEvaluator.InstanceView() {
            @Override
            public String getCurrentState() {
                return "SCANNING";
            }

            @Override
            public Map<String, Object> getSubmittedData() {
                return context;
            }
        };
        for (RuleDefinition rule : rules) {
            SpelGuardEvaluator.Outcome outcome =
                    guardEvaluator.evaluate(rule.expression(), context, stubView, null, TenantContext.get());
            if (outcome.allowed()) continue;
            if (!"BLOCKING".equals(rule.severity())) {
                log.info("Rule {} (WARNING) did not pass: {}", rule.ruleCode(), outcome.reason);
                continue;
            }
            String reason = rule.rejectReasonCode() != null
                    ? rule.rejectReasonCode()
                    : (outcome.errored() ? "RULE_ERROR: " + outcome.reason : outcome.reason);
            return RuleEvalResult.reject(rule.ruleCode(), reason);
        }
        return RuleEvalResult.pass();
    }

    /** DB-backed: load the rule set's active rules, evaluate, and log the outcome. Null ruleSetId = always pass. */
    public RuleEvalResult loadAndEvaluate(Long ruleSetId, Map<String, Object> context,
                                          Long dispatchTaskId, Long workflowInstanceId, String scanBarcode) {
        if (ruleSetId == null) return RuleEvalResult.pass();
        long tenantId = TenantContext.get();
        List<RuleDefinition> rules = jdbc.query(
                "SELECT rule_code, category, sequence_no, expression, severity, reject_reason_code FROM rule_definition "
                        + "WHERE tenant_id = ? AND rule_set_id = ? AND active = 1 ORDER BY sequence_no, rule_code",
                (rs, i) -> new RuleDefinition(rs.getString("rule_code"), rs.getString("category"), rs.getInt("sequence_no"),
                        rs.getString("expression"), rs.getString("severity"), rs.getString("reject_reason_code")),
                tenantId, ruleSetId);
        RuleEvalResult result = evaluate(rules, context);
        logEvaluation(tenantId, ruleSetId, dispatchTaskId, workflowInstanceId, scanBarcode, context, result);
        return result;
    }

    private void logEvaluation(long tenantId, Long ruleSetId, Long dispatchTaskId, Long workflowInstanceId,
                                String scanBarcode, Map<String, Object> context, RuleEvalResult result) {
        try {
            String contextJson = mapper.writeValueAsString(context);
            jdbc.update(
                    "INSERT INTO rule_evaluation_log (tenant_id, rule_set_id, dispatch_task_id, workflow_instance_id, "
                            + "scan_barcode, input_context_json, outcome, failed_rule_code, failed_reason) VALUES (?,?,?,?,?,?,?,?,?)",
                    tenantId, ruleSetId, dispatchTaskId, workflowInstanceId, scanBarcode, contextJson,
                    result.passed() ? "PASS" : "REJECT", result.failedRuleCode(), result.failedReason());
        } catch (Exception e) {
            log.warn("rule_evaluation_log write failed: {}", e.toString());
        }
    }
}
