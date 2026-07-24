package com.aria.conveyor;

import com.aria.conveyor.dto.RuleDefinition;
import com.aria.conveyor.dto.RuleEvalResult;
import com.aria.conveyor.service.RuleEvaluationService;
import com.aria.workflow.engine.SpelGuardEvaluator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure tests for the Rule Builder's evaluation logic — no DB (the pure
 * {@code evaluate(List, Map)} overload is exercised directly; the
 * DB-backed {@code loadAndEvaluate} wrapper is covered by the app-context
 * curl smoke test instead, mirroring this repo's "pure logic in a *Test,
 * DB-touching behaviour verified live" split).
 */
class RuleEvaluationServiceTest {

    // jdbc is never touched by evaluate(), so null is safe here.
    private final RuleEvaluationService service = new RuleEvaluationService(new SpelGuardEvaluator(), null);

    private RuleDefinition rule(String code, String category, int seq, String expr, String severity) {
        return new RuleDefinition(code, category, seq, expr, severity, null);
    }

    @Test
    void noRules_alwaysPasses() {
        RuleEvalResult r = service.evaluate(List.of(), Map.of("sku", "ABC"));
        assertThat(r.passed()).isTrue();
    }

    @Test
    void allRulesPass_overallPasses() {
        List<RuleDefinition> rules = List.of(
                rule("SKU_MATCH", "SKU", 1, "#submitted['scannedSku'] == #submitted['taskSkuCode']", "BLOCKING"),
                rule("QTY_OK", "QUANTITY", 2, "#submitted['taskProcessedQty'] < #submitted['taskTargetQty']", "BLOCKING"));
        Map<String, Object> ctx = Map.of("scannedSku", "SKU-1", "taskSkuCode", "SKU-1",
                "taskProcessedQty", 5, "taskTargetQty", 10);
        RuleEvalResult r = service.evaluate(rules, ctx);
        assertThat(r.passed()).isTrue();
        assertThat(r.failedRuleCode()).isNull();
    }

    @Test
    void firstFailingBlockingRule_rejectsWithItsReasonCode() {
        List<RuleDefinition> rules = List.of(
                new RuleDefinition("SKU_MATCH", "SKU", 1,
                        "#submitted['scannedSku'] == #submitted['taskSkuCode']", "BLOCKING", "SKU_MISMATCH"),
                new RuleDefinition("QTY_OK", "QUANTITY", 2,
                        "#submitted['taskProcessedQty'] < #submitted['taskTargetQty']", "BLOCKING", "OVER_TARGET"));
        Map<String, Object> ctx = Map.of("scannedSku", "WRONG-SKU", "taskSkuCode", "SKU-1",
                "taskProcessedQty", 5, "taskTargetQty", 10);
        RuleEvalResult r = service.evaluate(rules, ctx);
        assertThat(r.passed()).isFalse();
        assertThat(r.failedRuleCode()).isEqualTo("SKU_MATCH");
        assertThat(r.failedReason()).isEqualTo("SKU_MISMATCH");
    }

    @Test
    void sequenceOrder_determinesWhichRuleFailsFirst() {
        // Both rules would fail; QTY_OK is sequence 1 (evaluated first), so it should win, not SKU_MATCH (sequence 2).
        List<RuleDefinition> rules = List.of(
                new RuleDefinition("SKU_MATCH", "SKU", 2,
                        "#submitted['scannedSku'] == #submitted['taskSkuCode']", "BLOCKING", "SKU_MISMATCH"),
                new RuleDefinition("QTY_OK", "QUANTITY", 1,
                        "#submitted['taskProcessedQty'] < #submitted['taskTargetQty']", "BLOCKING", "OVER_TARGET"));
        // Caller is responsible for pre-ordering by sequence_no (loadAndEvaluate does this via SQL ORDER BY);
        // evaluate() itself just walks the list in the order given.
        List<RuleDefinition> ordered = List.of(rules.get(1), rules.get(0));
        Map<String, Object> ctx = Map.of("scannedSku", "WRONG-SKU", "taskSkuCode", "SKU-1",
                "taskProcessedQty", 10, "taskTargetQty", 10);
        RuleEvalResult r = service.evaluate(ordered, ctx);
        assertThat(r.failedRuleCode()).isEqualTo("QTY_OK");
    }

    @Test
    void warningSeverity_doesNotBlock() {
        List<RuleDefinition> rules = List.of(
                rule("PACKING_HINT", "PACKING", 1, "false", "WARNING"),
                rule("SKU_MATCH", "SKU", 2, "true", "BLOCKING"));
        RuleEvalResult r = service.evaluate(rules, Map.of());
        assertThat(r.passed()).isTrue();
    }

    @Test
    void erroringExpression_isTreatedAsBlockingFailure() {
        List<RuleDefinition> rules = List.of(
                rule("BAD_EXPR", "CUSTOM", 1, "not a real spel !!! (((", "BLOCKING"));
        RuleEvalResult r = service.evaluate(rules, Map.of());
        assertThat(r.passed()).isFalse();
        assertThat(r.failedRuleCode()).isEqualTo("BAD_EXPR");
        assertThat(r.failedReason()).startsWith("RULE_ERROR");
    }

    @Test
    void rejectReasonCode_usedWhenPresent_elseGuardReason() {
        List<RuleDefinition> withCode = List.of(new RuleDefinition("R1", "SKU", 1, "false", "BLOCKING", "MY_CODE"));
        RuleEvalResult r = service.evaluate(withCode, Map.of());
        assertThat(r.failedReason()).isEqualTo("MY_CODE");
    }

    @Test
    void noRejectReasonCode_fallsBackToGuardDenyReason() {
        List<RuleDefinition> noCode = List.of(rule("R1", "SKU", 1, "false", "BLOCKING"));
        RuleEvalResult r = service.evaluate(noCode, Map.of());
        assertThat(r.failedReason()).isEqualTo("guard false");
    }
}
