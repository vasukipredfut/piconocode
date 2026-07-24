package com.aria.conveyor.dto;

/** Outcome of evaluating a Rule Builder rule set against a scan context. */
public record RuleEvalResult(boolean passed, String failedRuleCode, String failedReason) {

    public static RuleEvalResult pass() {
        return new RuleEvalResult(true, null, null);
    }

    public static RuleEvalResult reject(String ruleCode, String reason) {
        return new RuleEvalResult(false, ruleCode, reason);
    }
}
