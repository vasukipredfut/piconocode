package com.aria.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The single dispatcher every agent invocation flows through.
 *
 * <p>Reads the agent row from {@code ai_agent}, hydrates the context, calls the
 * handler, records the run in {@code ai_agent_run}, and applies the decision
 * based on the agent's current {@link AiAgent.Mode}.
 *
 * <p>Mode semantics (from {@code docs/architecture/AI-PDLC.md} §3):
 * <ul>
 *   <li>SHADOW      — log the decision, do NOT act</li>
 *   <li>SUPERVISED  — queue the decision for a human to confirm</li>
 *   <li>GUARDED     — act if confidence &gt;= threshold, else escalate</li>
 *   <li>AUTONOMOUS  — always act, escalate only on explicit ESCALATE decision</li>
 *   <li>RETIRED     — never invoke</li>
 * </ul>
 *
 * <p>Every decision, whatever the mode, lands in {@code ai_agent_run} — the
 * single audit surface for the whole product.
 */
@Service
public class AiAgentDispatcher {

    private static final Logger log = LoggerFactory.getLogger(AiAgentDispatcher.class);

    private final Map<String, AiAgentHandler> handlers = new HashMap<>();
    private final JdbcTemplate jdbc;
    private final ObjectMapper json = new ObjectMapper();

    @Autowired
    public AiAgentDispatcher(JdbcTemplate jdbc, List<AiAgentHandler> discovered) {
        this.jdbc = jdbc;
        for (AiAgentHandler h : discovered) {
            handlers.put(h.code(), h);
            log.info("Registered AI agent handler: {}", h.code());
        }
    }

    /**
     * Invoke an agent by code. This is what callers use — sensor listeners,
     * workflow transition hooks, GitHub webhook receivers, etc.
     */
    public Long invoke(String agentCode, Map<String, Object> inputs,
                       Long workflowInstanceId, Long operatorId,
                       String triggeredBy) {

        Map<String, Object> agent = loadAgent(agentCode);
        if (agent == null) {
            log.warn("AI agent not registered: {}", agentCode);
            return null;
        }
        AiAgent.Mode mode = AiAgent.Mode.valueOf((String) agent.get("mode"));
        if (mode == AiAgent.Mode.RETIRED) {
            log.info("Skipping RETIRED agent {}", agentCode);
            return null;
        }

        AiAgentHandler handler = handlers.get(agentCode);
        if (handler == null) {
            log.warn("No Java handler registered for agent code: {}", agentCode);
            return null;
        }

        Long tenantId = ((Number) agent.get("tenant_id")).longValue();
        Long agentId  = ((Number) agent.get("id")).longValue();

        // Pre-flight: check kill switch on tenant credentials if this is an LLM agent
        // (skip for RULE_CEP or ML_MODEL agents)
        if ("LLM".equals(agent.get("kind")) && isTenantAiKillSwitchOn(tenantId)) {
            log.warn("Tenant {} AI kill-switch is ON; skipping agent {}", tenantId, agentCode);
            return null;
        }

        AgentContext ctx = new AgentContext(
                tenantId, agentCode, workflowInstanceId, operatorId,
                inputs, List.of() /* TODO hydrate from ai_agent_tool */, mode);

        Instant startedAt = Instant.now();
        AgentDecision decision;
        String error = null;
        try {
            decision = handler.run(ctx);
        } catch (Exception ex) {
            log.error("Agent {} threw: {}", agentCode, ex.getMessage(), ex);
            decision = AgentDecision.fail("handler threw: " + ex.getMessage());
            error = ex.getClass().getSimpleName() + ": " + ex.getMessage();
        }

        String actionTaken = decideActionTaken(agent, decision);

        Long runId = insertRun(tenantId, agentId, workflowInstanceId, triggeredBy,
                               startedAt, ctx, decision, mode, actionTaken, operatorId, error);

        // Apply side-effect ONLY when action_taken is AUTO_APPLIED
        if ("AUTO_APPLIED".equals(actionTaken) && decision.action() != AgentDecision.Action.NOOP) {
            applyAction(decision, workflowInstanceId, tenantId);
        }

        return runId;
    }

    private String decideActionTaken(Map<String, Object> agent, AgentDecision decision) {
        AiAgent.Mode mode = AiAgent.Mode.valueOf((String) agent.get("mode"));
        BigDecimal threshold = (BigDecimal) agent.getOrDefault("confidence_threshold",
                new BigDecimal("0.85"));

        switch (decision.action()) {
            case FAIL:      return "FAILED";
            case NOOP:      return "PROPOSED";
            case ESCALATE:  return "ESCALATED";
            default:        break;
        }

        return switch (mode) {
            case SHADOW      -> "PROPOSED";
            case SUPERVISED  -> "PROPOSED";
            case GUARDED     -> decision.confidence().compareTo(threshold) >= 0
                                    ? "AUTO_APPLIED" : "ESCALATED";
            case AUTONOMOUS  -> "AUTO_APPLIED";
            case RETIRED     -> "PROPOSED";
        };
    }

    private void applyAction(AgentDecision d, Long workflowInstanceId, Long tenantId) {
        // Stub — real implementations dispatch to WorkflowEngine.transition,
        // NotificationService.send, GenericEntityController.save, etc. This
        // wiring lands in Sprint 2 of the AI-PDLC roadmap.
        log.info("[stub] would apply {} on workflow instance {}", d.action(), workflowInstanceId);
    }

    private Long insertRun(Long tenantId, Long agentId, Long wfInstanceId, String triggeredBy,
                           Instant startedAt, AgentContext ctx, AgentDecision d, AiAgent.Mode mode,
                           String actionTaken, Long operatorId, String error) {
        String inputsJson  = safeJson(ctx.inputs());
        String outputsJson = safeJson(Map.of(
                "action", d.action().name(),
                "actionArgs", d.actionArgs(),
                "reason", d.reason(),
                "confidence", d.confidence(),
                "telemetry", d.telemetry()));

        return jdbc.queryForObject(
                "INSERT INTO ai_agent_run " +
                "(tenant_id, agent_id, workflow_instance_id, triggered_by, started_at, finished_at, " +
                " inputs_json, outputs_json, mode_at_run, action_taken, operator_id, error) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id",
                Long.class,
                tenantId, agentId, wfInstanceId, triggeredBy,
                startedAt, Instant.now(),
                inputsJson, outputsJson, mode.name(), actionTaken, operatorId, error);
    }

    private Map<String, Object> loadAgent(String code) {
        try {
            return jdbc.queryForMap(
                    "SELECT id, tenant_id, code, mode, kind, model, confidence_threshold " +
                    "FROM ai_agent WHERE code=? AND status='PUBLISHED'",
                    code);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTenantAiKillSwitchOn(Long tenantId) {
        try {
            Integer flagged = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM tenant_ai_credential " +
                    "WHERE tenant_id=? AND kill_switch=TRUE",
                    Integer.class, tenantId);
            return flagged != null && flagged > 0;
        } catch (Exception e) {
            return false; // table may not exist yet on old DBs — fail-open
        }
    }

    private String safeJson(Object o) {
        try { return json.writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }
}
