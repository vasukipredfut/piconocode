---
description: Add a new tool that AI agents can call — MCP-style. Every tool is a Spring @Component implementing AgentTool. The registry autowires it, the LLM discovers it from the catalog, and calls it by name with typed args. Use this when an agent needs a capability that isn't in the four built-ins (entity.list / entity.schema / entity.query / entity.count).
---

# New agent tool — the substrate way

## The one rule

An agent tool is a **Spring bean implementing `AgentTool`** — nothing else.

Never wire a tool into an agent by direct field injection. Never hard-code
its name in the agent. Never bypass `AgentToolRegistry`. If the LLM can't
discover the tool by walking `AgentToolRegistry.catalog()`, it's wrong.

## Contract

```java
public interface AgentTool {
    String name();                                           // dotted, lowercase — e.g. "workflow.instances"
    String description();                                    // one sentence for the LLM catalog
    Map<String, Object> argsSchema();                        // JSON-schema-ish for the LLM
    Map<String, Object> execute(Map<String, Object> args,
                                Long tenantId);              // runs it — return {ok:true, ...} or {ok:false, error:"..."}
}
```

Every tool MUST:

- Enforce tenant scoping — `TenantContext.get()` is untrusted at this layer;
  the `tenantId` argument is the source of truth.
- Validate every string that touches SQL with `SAFE_IDENT` (via
  `EntityRegistryHelper.safeIdent`).
- Bound result size — no unbounded rows to the LLM. 200 is a safe upper bound.
- Return failures as `{ok:false, error:"…"}`, not throw. The LLM recovers.

## The three files you touch

### 1. The tool class

```java
package com.idayam.ai.tools;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class WorkflowInstancesTool implements AgentTool {

    private final JdbcTemplate jdbc;
    private final EntityRegistryHelper reg;

    public WorkflowInstancesTool(JdbcTemplate jdbc, EntityRegistryHelper reg) {
        this.jdbc = jdbc; this.reg = reg;
    }

    @Override public String name() { return "workflow.instances"; }

    @Override
    public String description() {
        return "List workflow instances by state and/or workflow code. "
             + "Returns {ok, rows, count, sql}. Use this for questions like "
             + "'which purchase-approval requests are still awaiting me?'.";
    }

    @Override
    public Map<String, Object> argsSchema() {
        return Map.of(
            "workflowCode", Map.of("type", "string",  "required", false,
                                   "desc", "e.g. 'purchase-approval'. Optional."),
            "state",        Map.of("type", "string",  "required", false,
                                   "desc", "e.g. 'PENDING_MGR'. Optional."),
            "limit",        Map.of("type", "integer", "required", false, "default", 20));
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args, Long tenantId) {
        String code  = strArg(args, "workflowCode");
        String state = strArg(args, "state");

        // Both strings will land in a WHERE clause — validate before use.
        if (code  != null && !reg.safeIdent(code))  return fail("bad workflowCode");
        if (state != null && !reg.safeIdent(state)) return fail("bad state");

        StringBuilder where = new StringBuilder(" WHERE tenant_id=?");
        List<Object> params = new java.util.ArrayList<>();
        params.add(tenantId);
        if (code  != null) { where.append(" AND workflow_code=?");  params.add(code);  }
        if (state != null) { where.append(" AND current_state=?");  params.add(state); }

        int limit = intArg(args, "limit", 20);
        if (limit < 1 || limit > 200) limit = 20;

        String sql = "SELECT id, workflow_code, current_state, updated_at "
                   + "FROM workflow_instance" + where + " ORDER BY updated_at DESC LIMIT " + limit;
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, params.toArray());
            return Map.of("ok", true, "rows", rows, "count", rows.size(), "sql", sql);
        } catch (Exception e) {
            return fail("query failed: " + e.getMessage());
        }
    }

    // ... helper strArg / intArg / fail as in EntityQueryTool ...
}
```

### 2. Unit test — parameterised, no live DB

Every tool needs a test that proves it rejects bad identifiers, ANDs the
tenant clause in, and honours the row cap. Follow the shape of the existing
`EntityQueryToolTest` (Sonar wants 100% element coverage on new files).

### 3. `AGENTS-CATALOG.md` — one paragraph per tool

Every tool goes into `docs/architecture/AGENTS-CATALOG.md` under its module
heading. Include: name, description, arg schema summary, one example call
+ result. This is the primary source when engineers explain "what can the
AI actually do?" to leadership.

## What you do NOT do

- ❌ Direct `@Autowired` of a tool into a specific agent — every agent gets
  its tools through `AgentToolRegistry.get(name)`.
- ❌ Add tool-selection logic inside the agent — the LLM picks. If your
  question is "how do I make agent X always call tool Y first?", the answer
  is: put that in the agent's system prompt, not in Java.
- ❌ Rely on `TenantContext.get()` inside the tool — the LLM runs on the
  operator's request but the LlmProxy has already resolved the tenant; use
  the `tenantId` parameter you were handed.
- ❌ Return more than 200 rows — the LLM context window can't hold a full
  ListView page and the answer degrades to a shrug.

## When you're done

1. Spring picks the tool up on next boot — check the log line
   `AgentToolRegistry: N tools registered — [...]` includes your name.
2. Open the AI chat, ask a question that should route to the new tool.
3. Confirm the "HOW I ANSWERED" trace in `AnswerPreview` shows your tool
   name + args + row count.

## Testing checklist

- [ ] Unit test: bad identifier → `{ok:false}` (never throws)
- [ ] Unit test: tenant clause always present
- [ ] Unit test: row cap enforced
- [ ] Integration test (recommended): stand up an in-mem DB, register the
      real tool, call via the registry, assert the result
- [ ] `AGENTS-CATALOG.md` entry with example
- [ ] Sonar coverage ≥ 90% on the new class
