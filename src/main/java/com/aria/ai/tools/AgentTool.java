package com.aria.ai.tools;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contract every AI agent tool implements — MCP-style.
 *
 * <p>The LLM sees a compact catalog of registered tools in its system prompt.
 * It then emits JSON of the form:
 * <pre>
 *   { "action": "call_tool", "tool": "entity.query",
 *     "args": { "entity": "PurchaseOrder", "filter": [...], "limit": 20 } }
 * </pre>
 * The agent runtime dispatches to the matching tool via
 * {@link AgentToolRegistry}, hands the result back to the LLM, and asks it
 * to synthesize the final answer.
 *
 * <p>Every tool MUST:
 * <ul>
 *   <li>Enforce tenant scoping via {@code TenantContext.get()}.</li>
 *   <li>Validate every string that touches SQL against {@code SAFE_IDENT}.</li>
 *   <li>Bound result size (a runaway {@code entity.query} can flood the LLM
 *       and blow the context window).</li>
 *   <li>Return failure as {@code {ok:false, error:"..."}} rather than throw
 *       — the LLM handles recovery.</li>
 * </ul>
 */
public interface AgentTool {

    /** Machine name — dotted lowercase, e.g. "entity.query". */
    String name();

    /** One-sentence human description for the LLM catalog. */
    String description();

    /**
     * JSON-Schema-shaped argument descriptor for the LLM.
     * Example:
     * <pre>
     *   { "entity":  { "type":"string", "required":true, "desc":"..." },
     *     "filter":  { "type":"array",  "required":false, "desc":"..." },
     *     "limit":   { "type":"integer","required":false, "default":20 } }
     * </pre>
     */
    Map<String, Object> argsSchema();

    /**
     * Execute the tool. Result is JSON-serializable; must include {@code ok}.
     * Success:  {@code {ok:true, rows:[...], count:N, ...}}
     * Failure:  {@code {ok:false, error:"..."}}
     */
    Map<String, Object> execute(Map<String, Object> args, Long tenantId);

    /** Convenience: shape a success payload consistently. */
    default Map<String, Object> ok(String key, Object value) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put(key, value);
        return out;
    }

    /** Convenience: shape an error payload consistently. */
    default Map<String, Object> fail(String message) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", false);
        out.put("error", message);
        return out;
    }

    /** Convenience: coerce a value to a plain list of maps for LLM display. */
    @SuppressWarnings("unchecked")
    default List<Map<String, Object>> asRows(Object rows) {
        return rows instanceof List ? (List<Map<String, Object>>) rows : List.of();
    }
}
