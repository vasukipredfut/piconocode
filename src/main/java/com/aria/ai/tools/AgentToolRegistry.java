package com.aria.ai.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Autoconfigured registry of all {@link AgentTool} beans in the context.
 *
 * <p>Agents consume this in two ways:
 * <ol>
 *   <li>{@link #catalog()} — a compact JSON list handed to the LLM in the
 *       system prompt so it can pick a tool by name.</li>
 *   <li>{@link #call(String, Map, Long)} — dispatches the LLM's tool call
 *       to the matching implementation, timing it and catching errors.</li>
 * </ol>
 */
@Service
public class AgentToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(AgentToolRegistry.class);

    private final Map<String, AgentTool> tools = new ConcurrentHashMap<>();

    public AgentToolRegistry(List<AgentTool> discovered) {
        for (AgentTool t : discovered) tools.put(t.name(), t);
        log.info("AgentToolRegistry: {} tools registered — {}", tools.size(), tools.keySet());
    }

    /** Ordered catalog for the LLM system prompt. */
    public List<Map<String, Object>> catalog() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (AgentTool t : tools.values()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", t.name());
            entry.put("description", t.description());
            entry.put("args", t.argsSchema());
            out.add(entry);
        }
        return out;
    }

    /** Get a tool by name. */
    public AgentTool get(String name) {
        return tools.get(name);
    }

    /** Whether the given name is a registered tool. */
    public boolean has(String name) {
        return tools.containsKey(name);
    }

    /** Dispatch a call from the LLM. Never throws — surfaces errors as {@code {ok:false}}. */
    public ToolCallResult call(String name, Map<String, Object> args, Long tenantId) {
        long t0 = System.currentTimeMillis();
        AgentTool tool = tools.get(name);
        if (tool == null) {
            return new ToolCallResult(name, args, Map.of("ok", false, "error", "unknown tool: " + name),
                                       System.currentTimeMillis() - t0);
        }
        try {
            Map<String, Object> result = tool.execute(args == null ? Map.of() : args, tenantId);
            return new ToolCallResult(name, args, result, System.currentTimeMillis() - t0);
        } catch (Exception e) {
            log.warn("tool {} failed: {}", name, e.getMessage(), e);
            return new ToolCallResult(name, args,
                    Map.of("ok", false, "error", e.getClass().getSimpleName() + ": " + e.getMessage()),
                    System.currentTimeMillis() - t0);
        }
    }

    /** Immutable result of one tool call — for tracing back to the LLM + UI. */
    public record ToolCallResult(String tool, Map<String, Object> args,
                                 Map<String, Object> result, long latencyMs) {
        public boolean ok() {
            Object v = result == null ? null : result.get("ok");
            return Boolean.TRUE.equals(v);
        }
    }
}
