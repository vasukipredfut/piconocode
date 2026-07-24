package com.aria.conveyor.dto;

/** One Rule Builder rule (a row of {@code rule_definition}), as loaded for evaluation. */
public record RuleDefinition(
        String ruleCode,
        String category,
        int sequenceNo,
        String expression,
        String severity,
        String rejectReasonCode) {
}
