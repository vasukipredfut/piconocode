package com.aria.ai.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.ai.AgentContext;
import com.aria.ai.AgentDecision;
import com.aria.ai.AiAgent;
import com.aria.ai.AiAgentHandler;
import com.aria.ai.llm.LlmProvider;
import com.aria.ai.llm.LlmProxy;
import com.aria.ai.tools.AgentToolRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * @ai-element
 *   kind:      HYBRID  (LLM tool-use + deterministic fallback)
 *   agent:     customer.decision-analyzer
 *   mode-max:  SUPERVISED
 *   inputs:    { question: "get me all overdue purchase orders" }
 *   outputs:   AgentDecision.NOTIFY with
 *              actionArgs = { question, answer, toolCalls, evidence, llm_used }
 *
 * <p>Runs an MCP-style tool-use loop against the {@link AgentToolRegistry}:
 * <ol>
 *   <li>Turn 1: send the catalog + user question → model picks a tool + args.</li>
 *   <li>Execute the tool via the registry (parameterised SQL, cap 200 rows).</li>
 *   <li>Turn 2: send the result → model writes the customer-facing answer.</li>
 * </ol>
 * If the LLM is unreachable or the response can't be parsed, falls through
 * to the regex intent classifier for a small set of canned questions.
 */
@AiAgent(
        code = "customer.decision-analyzer",
        displayName = "Decision analyzer (customer)",
        model = "qwen2.5:3b-instruct via LlmProxy",
        kind = AiAgent.Kind.HYBRID,
        population = AiAgent.Population.RUNTIME,
        maxMode = AiAgent.Mode.SUPERVISED,
        tools = { "entity.list", "entity.schema", "entity.query", "entity.count" },
        description = "Answers ad-hoc analytical questions by picking a substrate tool "
                    + "and citing the rows it read."
)
@Component
public class CustomerDecisionAnalyzerAgent implements AiAgentHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomerDecisionAnalyzerAgent.class);
    private static final String CODE = "customer.decision-analyzer";
    private static final int MAX_TURN1_TOKENS = 512;
    private static final int MAX_TURN2_TOKENS = 640;
    private static final ObjectMapper JSON = new ObjectMapper();

    private final JdbcTemplate jdbc;
    private final LlmProxy llm;
    private final AgentToolRegistry tools;

    public CustomerDecisionAnalyzerAgent(JdbcTemplate jdbc, LlmProxy llm, AgentToolRegistry tools) {
        this.jdbc = jdbc;
        this.llm = llm;
        this.tools = tools;
    }

    @Override public String code() { return CODE; }

    @Override
    public AgentDecision run(AgentContext ctx) {
        String question = String.valueOf(ctx.inputs().getOrDefault("question", "")).trim();
        if (question.isEmpty()) {
            return AgentDecision.fail("empty question — need something to analyse");
        }

        // ---- Attempt LLM-driven tool-use ---------------------------------
        LlmRun llmRun = tryLlmToolUse(question, ctx.tenantId());
        if (llmRun != null) return llmRun.decision;

        // ---- Fallback: deterministic aggregation --------------------------
        return runDeterministicFallback(question, ctx.tenantId());
    }

    // ---------------------------------------------------------------------
    //  LLM tool-use loop
    // ---------------------------------------------------------------------

    private LlmRun tryLlmToolUse(String question, Long tenantId) {
        String systemPrompt = buildSystemPrompt();
        String userPromptT1 = buildTurn1UserPrompt(question);
        String schemaHintT1 = TURN1_SCHEMA_HINT;

        Optional<LlmProvider.LlmResult> t1 = llm.tryGenerate(
                systemPrompt, userPromptT1, schemaHintT1, MAX_TURN1_TOKENS);
        if (t1.isEmpty()) {
            log.info("decision-analyzer: LLM turn-1 unavailable — falling back");
            return null;
        }

        Turn1 parsed = parseTurn1(t1.get().json());
        if (parsed == null) {
            log.info("decision-analyzer: turn-1 unparseable, falling back. raw={}", truncate(t1.get().json(), 200));
            return null;
        }

        // Path A: model answered directly, no tool needed.
        if ("answer".equalsIgnoreCase(parsed.action)) {
            return new LlmRun(buildDecision(question, parsed.text, List.of(), List.of(), true, t1.get()));
        }

        // Path B: model requested a tool call.
        if (!"call_tool".equalsIgnoreCase(parsed.action) || parsed.tool == null) {
            log.info("decision-analyzer: turn-1 gave neither answer nor tool call — falling back");
            return null;
        }
        // Weak local models often name the ENTITY (e.g. "Vendor") as the tool
        // instead of calling entity.query with an entity arg. Remap that.
        String tool = parsed.tool;
        Map<String, Object> args = parsed.args;
        if (!tools.has(tool)) {
            if (isEntityCode(tool)) {
                args = args == null ? new java.util.LinkedHashMap<>() : new java.util.LinkedHashMap<>(args);
                args.putIfAbsent("entity", tool);
                tool = "entity.query";
                log.info("decision-analyzer: remapped entity '{}' -> entity.query", parsed.tool);
            } else {
                log.info("decision-analyzer: LLM asked for unknown tool '{}' — falling back", parsed.tool);
                return null;
            }
        }

        AgentToolRegistry.ToolCallResult call = tools.call(tool, args, tenantId);

        // Turn 2 is intentionally skipped for the CPU-bound Ollama path — a second
        // pass with the tool result stuffed into the prompt roughly doubles latency
        // (30s → 60s+) without meaningfully improving the answer. The templated
        // narrative + the evidence table on the frontend already tell the customer
        // what they need. When a cloud LLM lands (fast enough to make turn 2 cheap)
        // we'll re-enable it behind a `provider != ollama-local` guard.
        String narrative = summariseToolResult(parsed.tool, call.result());
        return new LlmRun(buildDecision(question, narrative, List.of(call),
                                         evidenceFromResult(call.result()), true, t1.get()));
    }

    // ---------------------------------------------------------------------
    //  Prompt assembly
    // ---------------------------------------------------------------------

    /**
     * Compact system prompt — every extra token costs 20-40 ms on CPU inference.
     * We hand the LLM ONE-line tool signatures + comma-joined entity codes,
     * not JSON-encoded schemas. That keeps the prompt under 400 tokens and
     * the round-trip under 10s on a mid-range CPU with qwen2.5:3b-instruct.
     */
    private String buildSystemPrompt() {
        String toolSignatures = tools.catalog().stream()
                .map(t -> "- " + t.get("name") + ": " + t.get("description"))
                .reduce((a, b) -> a + "\n" + b).orElse("(no tools registered)");

        String entityCodes;
        try {
            entityCodes = jdbc.queryForList(
                    "SELECT code FROM entity_registry ORDER BY code",
                    String.class).stream()
                    .reduce((a, b) -> a + ", " + b).orElse("(none)");
        } catch (Exception e) {
            entityCodes = "(unavailable)";
        }
        return "You are EARMS's decision analyzer. Pick ONE tool to answer the user's question. "
             + "Reply with ONLY valid JSON — no prose.\n\n"
             + "Rules:\n"
             + "  * \"how many …\" → entity.count\n"
             + "  * \"get me …\" / \"list …\" / \"show me …\" → entity.query with limit 20\n"
             + "  * If unsure which fields exist, call entity.schema first\n"
             + "  * Never invent tools or entity codes\n\n"
             + "TOOLS:\n" + toolSignatures + "\n\n"
             + "ENTITIES: " + entityCodes;
    }

    private String buildTurn1UserPrompt(String question) {
        return "Customer question: \"" + question + "\"\n\n"
             + "Reply with JSON of one of these shapes:\n"
             + "  { \"action\": \"call_tool\", \"tool\": \"<tool_name>\", \"args\": { ... } }\n"
             + "  { \"action\": \"answer\",    \"text\": \"<short answer for questions not needing data>\" }\n"
             + "Prefer call_tool. Only use 'answer' when no tool is needed (e.g. the user asked what you can do).";
    }

    private String buildTurn2UserPrompt(String question, String toolName, Map<String, Object> args,
                                         Map<String, Object> result) {
        String argsJson;
        String resultJson;
        try {
            argsJson   = JSON.writeValueAsString(args);
            resultJson = JSON.writeValueAsString(compactResultForLlm(result));
        } catch (Exception e) {
            argsJson = String.valueOf(args);
            resultJson = String.valueOf(result);
        }
        return "Customer question: \"" + question + "\"\n\n"
             + "You called tool '" + toolName + "' with args: " + argsJson + "\n"
             + "Tool returned: " + resultJson + "\n\n"
             + "Reply with JSON: { \"action\": \"answer\", \"text\": \"<one-paragraph customer-facing "
             + "answer citing the counts / row values from the tool result>\" }.\n"
             + "Be specific: cite numbers. If the result is empty, say so plainly.";
    }

    /**
     * Compress the tool result for the LLM prompt — full rows blow the context window.
     * Keeps count + first 5 rows.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> compactResultForLlm(Map<String, Object> result) {
        Map<String, Object> compact = new LinkedHashMap<>(result);
        Object rows = compact.get("rows");
        if (rows instanceof List) {
            List<Map<String, Object>> full = (List<Map<String, Object>>) rows;
            if (full.size() > 5) {
                compact.put("rows", full.subList(0, 5));
                compact.put("rowsSample", "first 5 of " + full.size());
            }
        }
        return compact;
    }

    // ---------------------------------------------------------------------
    //  Response parsing (LLM emits JSON; be forgiving on shape)
    // ---------------------------------------------------------------------

    private static final String TURN1_SCHEMA_HINT =
            "{ \"action\":\"call_tool|answer\", \"tool\":\"string?\", \"args\":\"object?\", \"text\":\"string?\" }";

    private static final String TURN2_SCHEMA_HINT =
            "{ \"action\":\"answer\", \"text\":\"string\" }";

    private record Turn1(String action, String tool, Map<String, Object> args, String text) {}
    private record Turn2(String action, String text) {}

    // Lenient reader — small local models emit single quotes / unquoted keys.
    private static final com.fasterxml.jackson.databind.ObjectMapper LENIENT_JSON =
            com.fasterxml.jackson.databind.json.JsonMapper.builder()
                    .enable(com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_SINGLE_QUOTES)
                    .enable(com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
                    .enable(com.fasterxml.jackson.core.json.JsonReadFeature.ALLOW_TRAILING_COMMA)
                    .build();

    @SuppressWarnings("unchecked")
    private Turn1 parseTurn1(String raw) {
        if (raw == null) return null;
        String body = extractJsonObject(raw);
        String s = body != null ? body : raw;
        // 1. strict, then 2. lenient (single quotes / unquoted keys) on the
        //    balanced object (trailing model garbage already stripped).
        for (com.fasterxml.jackson.databind.ObjectMapper m :
                new com.fasterxml.jackson.databind.ObjectMapper[]{JSON, LENIENT_JSON}) {
            try {
                Map<String, Object> j = m.readValue(s, Map.class);
                String action = strOrNull(j.get("action"));
                if (action != null) {
                    Map<String, Object> args = j.get("args") instanceof Map ? (Map<String, Object>) j.get("args") : null;
                    return new Turn1(action, strOrNull(j.get("tool")), args, strOrNull(j.get("text")));
                }
            } catch (Exception ignored) { }
        }
        // 3. regex salvage — recover action/tool/filter from mangled output.
        String action = firstMatch(s, "[\"']?action[\"']?\\s*:\\s*[\"']?([a-zA-Z_]+)");
        if (action == null) return null;
        String tool = firstMatch(s, "[\"']?tool[\"']?\\s*:\\s*[\"']?([A-Za-z0-9_.]+)");
        Map<String, Object> args = null;
        String field = firstMatch(s, "[\"']?field[\"']?\\s*:\\s*[\"']?([A-Za-z0-9_]+)");
        if (field != null) {
            Map<String, Object> f = new java.util.LinkedHashMap<>();
            f.put("field", field);
            String op = firstMatch(s, "[\"']?op[\"']?\\s*:\\s*[\"']?([A-Za-z_]+)");
            if (op != null) f.put("op", op);
            String value = firstMatch(s, "[\"']?value[\"']?\\s*:\\s*[\"']?([^\"'\\],}]+)");
            if (value != null) f.put("value", value.trim());
            args = new java.util.LinkedHashMap<>();
            args.put("filter", java.util.List.of(f));
            String limit = firstMatch(s, "[\"']?limit[\"']?\\s*:\\s*(\\d+)");
            if (limit != null) args.put("limit", Integer.parseInt(limit));
        }
        return new Turn1(action, tool, args, firstMatch(s, "[\"']?text[\"']?\\s*:\\s*\"([^\"]+)\""));
    }

    /** First balanced {...} object in the text (strips markdown fences + trailing model junk). */
    private static String extractJsonObject(String raw) {
        int start = raw.indexOf('{');
        if (start < 0) return null;
        int depth = 0; boolean inStr = false; char q = 0;
        for (int i = start; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (inStr) { if (c == q && raw.charAt(i - 1) != '\\') inStr = false; }
            else if (c == '"' || c == '\'') { inStr = true; q = c; }
            else if (c == '{') depth++;
            else if (c == '}') { if (--depth == 0) return raw.substring(start, i + 1); }
        }
        return null;
    }

    private static String firstMatch(String s, String regex) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(regex).matcher(s);
        return m.find() ? m.group(1) : null;
    }

    private boolean isEntityCode(String code) {
        if (code == null || code.isBlank()) return false;
        try {
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM entity_registry WHERE code=?", Integer.class, code);
            return n != null && n > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Turn2 parseTurn2(String raw) {
        if (raw == null) return null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> j = JSON.readValue(raw, Map.class);
            return new Turn2(strOrNull(j.get("action")), strOrNull(j.get("text")));
        } catch (Exception e) {
            return null;
        }
    }

    private String strOrNull(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    // ---------------------------------------------------------------------
    //  Decision + evidence packaging
    // ---------------------------------------------------------------------

    private record LlmRun(AgentDecision decision) {}

    private AgentDecision buildDecision(String question, String answer,
                                         List<AgentToolRegistry.ToolCallResult> calls,
                                         List<Map<String, Object>> evidence,
                                         boolean llmUsed,
                                         LlmProvider.LlmResult llmResult) {

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("question", question);
        args.put("answer", answer);
        args.put("evidence", evidence);
        args.put("evidenceRowCount", evidence.size());
        args.put("toolCalls", calls.stream().map(this::traceOne).toList());

        BigDecimal confidence;
        if (!calls.isEmpty() && calls.get(0).ok() && !evidence.isEmpty()) confidence = new BigDecimal("0.90");
        else if (!calls.isEmpty() && calls.get(0).ok())                    confidence = new BigDecimal("0.75");
        else if (llmUsed)                                                  confidence = new BigDecimal("0.55");
        else                                                                confidence = new BigDecimal("0.40");

        Map<String, Object> telemetry = new LinkedHashMap<>();
        telemetry.put("llm_used", llmUsed);
        telemetry.put("tool_calls", calls.size());
        telemetry.put("evidence_rows", evidence.size());
        if (llmResult != null) {
            telemetry.put("model", llmResult.modelUsed());
            telemetry.put("latency_ms", llmResult.latencyMs());
            telemetry.put("tokens_in", llmResult.tokensIn());
            telemetry.put("tokens_out", llmResult.tokensOut());
        }

        String reason = String.format(Locale.ROOT,
                "Answered via %s. Tools: %d. Evidence rows: %d.",
                llmUsed ? "LLM tool-use" : "deterministic fallback",
                calls.size(), evidence.size());

        return new AgentDecision(AgentDecision.Action.NOTIFY, args, reason, confidence, telemetry);
    }

    private Map<String, Object> traceOne(AgentToolRegistry.ToolCallResult c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tool", c.tool());
        m.put("args", c.args());
        m.put("ok", c.ok());
        m.put("latencyMs", c.latencyMs());
        Object cnt = c.result() == null ? null : c.result().get("count");
        if (cnt != null) m.put("count", cnt);
        Object sql = c.result() == null ? null : c.result().get("sql");
        if (sql != null) m.put("sql", sql);
        return m;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> evidenceFromResult(Map<String, Object> result) {
        if (result == null) return List.of();
        Object rows = result.get("rows");
        if (rows instanceof List) return (List<Map<String, Object>>) rows;
        // For count-style results, package the count as a single evidence row.
        Object count = result.get("count");
        if (count != null) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("count", count);
            Object entity = result.get("entity");
            if (entity != null) row.put("entity", entity);
            return List.of(row);
        }
        return List.of();
    }

    /**
     * Compose a customer-facing sentence directly from the tool result — no
     * second LLM turn needed. Shape depends on the tool:
     * <ul>
     *   <li>{@code entity.list}   → "You have N entities: A, B, C…"</li>
     *   <li>{@code entity.count}  → "There are N X. (query: …)"</li>
     *   <li>{@code entity.query}  → "Found N X. Showing the first M — see the
     *                                evidence table below."</li>
     *   <li>{@code entity.schema} → "X has N fields: a, b, c…"</li>
     * </ul>
     */
    @SuppressWarnings("unchecked")
    private String summariseToolResult(String toolName, Map<String, Object> result) {
        if (result == null || Boolean.FALSE.equals(result.get("ok"))) {
            String err = result == null ? "" : String.valueOf(result.get("error"));
            return "The lookup didn't succeed." + (err.isBlank() ? "" : " " + err);
        }
        String entity = result.get("entity") == null ? "" : String.valueOf(result.get("entity"));
        Object count  = result.get("count");
        long n        = count instanceof Number ? ((Number) count).longValue() : -1;

        switch (toolName) {
            case "entity.list": {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("entities");
                int total = rows == null ? 0 : rows.size();
                String sample = rows == null ? "" : rows.stream().limit(6)
                        .map(r -> String.valueOf(r.get("display_name")))
                        .filter(s -> !"null".equals(s))
                        .reduce((a, b) -> a + ", " + b).orElse("");
                return "You have " + total + " entities registered"
                     + (sample.isBlank() ? "." : ". First few: " + sample + (total > 6 ? "…" : "."));
            }
            case "entity.count":
                return n == 0
                        ? "There are no " + entity + " rows matching that filter."
                        : "There " + (n == 1 ? "is 1 " : "are " + n + " ") + entity + " row"
                          + (n == 1 ? "" : "s") + " matching that filter.";
            case "entity.query": {
                List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("rows");
                int shown = rows == null ? 0 : rows.size();
                if (shown == 0) return "No " + entity + " rows matched that filter.";
                return "Found " + shown + " " + entity + " row" + (shown == 1 ? "" : "s")
                     + " — see the evidence table below.";
            }
            case "entity.schema": {
                List<Map<String, Object>> fields = (List<Map<String, Object>>) result.get("fields");
                int total = fields == null ? 0 : fields.size();
                return entity + " has " + total + " field" + (total == 1 ? "" : "s") + " registered.";
            }
            default:
                if (n >= 0) return "Found " + n + " matching record(s) via " + toolName + ".";
                return "Tool " + toolName + " returned a result — see below.";
        }
    }

    private String truncate(String s, int n) {
        return s == null ? "" : (s.length() <= n ? s : s.substring(0, n) + "…");
    }

    // ---------------------------------------------------------------------
    //  Deterministic fallback — kept for when the LLM is offline
    // ---------------------------------------------------------------------

    private AgentDecision runDeterministicFallback(String question, Long tenantId) {
        Intent intent = classifyIntent(question);
        List<Map<String, Object>> evidence = gatherEvidence(intent, tenantId);
        String narrative = composeNarrative(intent, evidence);

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("question", question);
        args.put("answer", narrative);
        args.put("intent", intent.name());
        args.put("evidence", evidence);
        args.put("evidenceRowCount", evidence.size());
        args.put("toolCalls", List.of());

        BigDecimal confidence = evidence.isEmpty() ? new BigDecimal("0.30") : new BigDecimal("0.65");
        String reason = String.format(Locale.ROOT,
                "Deterministic path (LLM unavailable). Intent=%s. Evidence=%d rows.",
                intent.name(), evidence.size());

        Map<String, Object> telemetry = Map.of(
                "intent", intent.name(),
                "evidence_rows", evidence.size(),
                "llm_used", false,
                "tool_calls", 0);

        return new AgentDecision(AgentDecision.Action.NOTIFY, args, reason, confidence, telemetry);
    }

    private enum Intent { OVERDUE, CASHFLOW, QC_FAILURE, DELAY, INVENTORY_LOW, GENERIC }

    private Intent classifyIntent(String q) {
        String lq = q.toLowerCase(Locale.ROOT);
        if (lq.contains("overdue") || lq.contains("past due"))                      return Intent.OVERDUE;
        if (lq.contains("cash") || lq.contains("payment risk"))                     return Intent.CASHFLOW;
        if (lq.contains("qc fail") || lq.contains("quality fail"))                  return Intent.QC_FAILURE;
        if (lq.contains("delay") || lq.contains("late"))                            return Intent.DELAY;
        if (lq.contains("stock low") || lq.contains("inventory low") || lq.contains("out of stock")) return Intent.INVENTORY_LOW;
        return Intent.GENERIC;
    }

    private List<Map<String, Object>> gatherEvidence(Intent intent, Long tenantId) {
        try {
            switch (intent) {
                case OVERDUE:
                    return jdbc.queryForList(
                            "SELECT * FROM m6_ar_open_item WHERE tenant_id=? AND due_date < CURDATE() " +
                            "ORDER BY due_date ASC LIMIT 20", tenantId);
                case CASHFLOW:
                    return jdbc.queryForList(
                            "SELECT SUM(amount) AS pending_receivables FROM m6_ar_open_item WHERE tenant_id=?",
                            tenantId);
                case QC_FAILURE:
                    return jdbc.queryForList(
                            "SELECT * FROM m5_quality_test WHERE tenant_id=? AND status='FAILED' " +
                            "ORDER BY id DESC LIMIT 20", tenantId);
                case DELAY:
                    return jdbc.queryForList(
                            "SELECT id, workflow_code, current_state, updated_at FROM workflow_instance " +
                            "WHERE tenant_id=? AND status='OPEN' AND updated_at < DATE_SUB(NOW(), INTERVAL 3 DAY) " +
                            "LIMIT 20", tenantId);
                case INVENTORY_LOW:
                    return jdbc.queryForList(
                            "SELECT * FROM m5_stock_balance WHERE tenant_id=? AND qty_on_hand < reorder_point LIMIT 20",
                            tenantId);
                default:
                    return new ArrayList<>();
            }
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String composeNarrative(Intent intent, List<Map<String, Object>> evidence) {
        if (evidence.isEmpty()) {
            return "No matching records found for this question. Either everything is clean, or the "
                 + "entity for this intent isn't yet registered for this tenant.";
        }
        return String.format(Locale.ROOT,
                "Found %d record(s) matching intent %s. See evidence below.",
                evidence.size(), intent.name());
    }
}
