---
description: Add a new AI agent that runs an LLM tool-use loop with graceful deterministic fallback. Use this when you need behaviour like the customer.decision-analyzer — take a natural question, pick a tool, cite the answer with evidence. Not for pure text-to-JSON scaffold agents (page-designer / field-designer / workflow-designer) — those don't need tools.
---

# New AI agent — LLM tool-use with fallback

## When this shape fits

Pick this pattern when the agent's job is: **customer asks a question → we
need to look at real substrate data → we answer with a paragraph + citations.**

Wrong shape for this skill:

- Agents that transform text into a JSON scaffold (page / field / workflow
  designers). Those emit structured JSON directly — no tool call needed.
- Agents that transition a workflow. Those are pure `AgentDecision.TRANSITION`
  handlers — see `new-workflow` skill.

## Contract

An agent is a `@Component` implementing `AiAgentHandler` and annotated
with `@AiAgent(...)`. The dispatcher (`AiAgentDispatcher`) finds it via
the annotation and calls `run(AgentContext)`.

## The scaffold

```java
@AiAgent(
        code = "customer.compliance-checker",           // dotted lowercase, matches ai_agent.code
        displayName = "Compliance checker (customer)",
        model = "qwen2.5:3b-instruct via LlmProxy",
        kind = AiAgent.Kind.HYBRID,                     // HYBRID = tool-use + deterministic fallback
        population = AiAgent.Population.RUNTIME,        // customer-facing, NOT DEV
        maxMode = AiAgent.Mode.SUPERVISED,              // never AUTONOMOUS for RUNTIME + external actions
        tools = { "entity.list", "entity.query" },      // names the LLM may see in this agent's catalog
        description = "Answers compliance questions and cites the licence rows it checked."
)
@Component
public class ComplianceCheckerAgent implements AiAgentHandler {

    private final JdbcTemplate  jdbc;                   // fallback path uses this
    private final LlmProxy      llm;                    // the tenant-aware LLM router
    private final AgentToolRegistry tools;              // MCP-style tool catalog

    public ComplianceCheckerAgent(JdbcTemplate jdbc, LlmProxy llm, AgentToolRegistry tools) {
        this.jdbc = jdbc; this.llm = llm; this.tools = tools;
    }

    @Override public String code() { return "customer.compliance-checker"; }

    @Override
    public AgentDecision run(AgentContext ctx) {
        String question = String.valueOf(ctx.inputs().getOrDefault("question", "")).trim();
        if (question.isEmpty()) return AgentDecision.fail("empty question");

        // 1. Try LLM tool-use.
        AgentDecision d = tryLlmToolUse(question, ctx.tenantId());
        if (d != null) return d;

        // 2. Deterministic fallback — never leave the customer with no answer.
        return runDeterministicFallback(question, ctx.tenantId());
    }

    // See CustomerDecisionAnalyzerAgent for the reference implementation of the two-turn loop.
    ...
}
```

## The two-turn loop (copy this shape, don't reinvent)

```java
private AgentDecision tryLlmToolUse(String question, Long tenantId) {
    String system = buildSystemPrompt();                     // catalog + entity list + persona
    Optional<LlmResult> t1 = llm.tryGenerate(system, turn1Prompt(question), TURN1_SCHEMA, 512);
    if (t1.isEmpty()) return null;                           // Ollama down → fall back

    Turn1 parsed = parseTurn1(t1.get().json());
    if (parsed == null) return null;                         // malformed → fall back

    if ("answer".equals(parsed.action)) {                    // model answered without a tool
        return buildDecision(question, parsed.text, List.of(), List.of(), true, t1.get());
    }
    if (!"call_tool".equals(parsed.action) || !tools.has(parsed.tool)) return null;

    ToolCallResult call = tools.call(parsed.tool, parsed.args, tenantId);   // execute

    Optional<LlmResult> t2 = llm.tryGenerate(system, turn2Prompt(question, call), TURN2_SCHEMA, 640);
    if (t2.isEmpty()) {                                      // second turn failed → summarise ourselves
        return buildDecision(question, summariseResult(call), List.of(call),
                              evidenceFromResult(call.result()), false, t1.get());
    }
    Turn2 answer = parseTurn2(t2.get().json());
    return buildDecision(question, answer.text, List.of(call),
                          evidenceFromResult(call.result()), true, t2.get());
}
```

The deterministic fallback is REQUIRED — Ollama's daemon can be down, the
model can be missing, the LLM can emit garbage. The agent's job is to
answer, not "answer if the LLM is up". Ship both paths.

## AgentDecision shape for tool-use answers

```java
AgentDecision(
    action     = Action.NOTIFY,
    actionArgs = Map.of(
        "question",         question,
        "answer",           narrativeString,       // LLM's paragraph
        "evidence",         List<Map<String,Object>>,   // rows the LLM cited
        "evidenceRowCount", n,
        "toolCalls",        List<Map<String,Object>>),  // {tool, args, ok, latencyMs, count, sql}
    reason     = "one-line what-happened",
    confidence = tunedByPath,                       // see reference
    telemetry  = Map.of("llm_used", bool, ...))
```

The frontend `AnswerPreview` renders these fields into the "HOW I ANSWERED"
trace + evidence table. Match the shape or the UI shows a shrug.

## Confidence tuning

- Tool call ran + evidence returned →  `0.90`
- Tool call ran + no evidence rows →  `0.75`
- LLM turn 1 answered without a tool →  `0.55`
- Deterministic fallback with rows →  `0.65`
- Deterministic fallback with nothing →  `0.30`

`ai_agent.confidence_threshold` decides whether the decision is presented
or asks for confirmation.

## What you do NOT do

- ❌ Skip the deterministic fallback because "Ollama is always up in prod".
  It isn't. Ship it.
- ❌ Cap the LLM at more than two turns without a good reason — every
  extra turn adds 3-6 seconds of user-visible latency.
- ❌ Emit anything but valid JSON in either LLM turn — Ollama's JSON mode
  covers this; use it via LlmProxy which already sets `format=json`.
- ❌ Trust LLM-supplied identifiers directly. Tools validate against
  `entity_field_registry`; the agent shouldn't second-guess this.

## Testing checklist

- [ ] Unit test: empty question → `Action.FAIL`
- [ ] Unit test: LlmProxy returns empty → falls through to deterministic
- [ ] Unit test: LLM returns malformed JSON → falls through
- [ ] Unit test: LLM asks for unknown tool → falls through
- [ ] Unit test: happy path — mock LlmProxy + AgentToolRegistry, assert
      final `AgentDecision.actionArgs` has {answer, toolCalls, evidence}
- [ ] Skill integration test in the assistant UI — screenshot the tool
      trace + evidence render
- [ ] Sonar coverage ≥ 90% on the new class

## Where to look for the reference

`com.idayam.ai.runtime.CustomerDecisionAnalyzerAgent` is the canonical
implementation. Copy its two-turn loop + prompt builders + parser +
decision-builder verbatim; change only the agent code, description,
and (optionally) the tools you narrow the LLM to.
