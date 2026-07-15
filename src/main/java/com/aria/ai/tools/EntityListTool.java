package com.aria.ai.tools;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tool: {@code entity.list} — enumerate every entity registered on the
 * tenant's substrate.
 *
 * <p>Called by the LLM when it doesn't yet know which entity to query.
 * Typical trajectory: user asks "get me all suppliers" → LLM calls
 * {@code entity.list} → picks the row whose display name matches → then
 * calls {@code entity.query} with that code.
 */
@Component
public class EntityListTool implements AgentTool {

    private final EntityRegistryHelper reg;

    public EntityListTool(EntityRegistryHelper reg) {
        this.reg = reg;
    }

    @Override public String name() { return "entity.list"; }

    @Override
    public String description() {
        return "List every entity registered on the substrate. Returns [{code, displayName, table}]. "
             + "Call this first if you're not sure which entity holds the data the user is asking about.";
    }

    @Override
    public Map<String, Object> argsSchema() {
        return Map.of();
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args, Long tenantId) {
        List<Map<String, Object>> rows = reg.listEntities();
        Map<String, Object> out = ok("entities", rows);
        out.put("count", rows.size());
        return out;
    }
}
