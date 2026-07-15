package com.aria.ai;

import java.util.List;
import java.util.Map;

/**
 * The full context passed into an {@link AiAgentHandler#run(AgentContext)}.
 *
 * <p>Immutable, no side effects. Agents receive their inputs, allowed tools,
 * and the mode they're currently operating in — everything they need to
 * produce a decision without touching global state.
 */
public record AgentContext(
        Long tenantId,
        String agentCode,
        Long workflowInstanceId,          // nullable — set when triggered from a workflow
        Long operatorId,                  // nullable — set when triggered from a UI action
        Map<String, Object> inputs,       // hydrated from binding trigger + hydration
        List<ToolSpec> allowedTools,      // what this agent may call
        AiAgent.Mode mode                 // captured at invocation time
) {
    /**
     * Tool schema exposed to LLM agents in the standard function-call format.
     * Read-only view of {@code ai_agent_tool}.
     */
    public record ToolSpec(
            String code,           // 'datasource.query' | 'workflow.transition' | ...
            String handlerBean,    // Spring bean name that executes it
            String schemaJson,     // OpenAI-style function schema (parameters etc.)
            String safety          // 'READ' | 'WRITE' | 'NOTIFY' | 'CRITICAL'
    ) {}
}
