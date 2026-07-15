package com.aria.ai;

import java.math.BigDecimal;
import java.util.Map;

/**
 * The decision an {@link AiAgentHandler} produces.
 *
 * <p>{@code action} is always one of a small enumerated set — no free-form action strings.
 * That constrains what agents can do at the type level, so the dispatcher can validate.
 */
public record AgentDecision(
        Action action,
        Map<String, Object> actionArgs,   // args to the action handler
        String reason,                    // NL reason shown to operator via AiExplainerPanel
        BigDecimal confidence,            // 0..1 — dispatcher compares to agent.confidence_threshold
        Map<String, Object> telemetry     // tokens, latency, cost — for AiAgentRun audit
) {
    public enum Action {
        NOOP,        // agent decided nothing to do
        TRANSITION,  // workflow.transition — actionArgs: {instanceId, toState}
        NOTIFY,      // notify.toast + optional SMS — actionArgs: {kind, message, role}
        ESCALATE,    // route to human via ai_agent_binding.escalation_role
        SAVE,        // write to an entity — actionArgs: {entity, fields}
        FAIL         // agent tried, could not decide — reason has the why
    }

    public static AgentDecision noop(String reason) {
        return new AgentDecision(Action.NOOP, Map.of(), reason,
                BigDecimal.ONE, Map.of());
    }

    public static AgentDecision fail(String reason) {
        return new AgentDecision(Action.FAIL, Map.of(), reason,
                BigDecimal.ZERO, Map.of());
    }
}
