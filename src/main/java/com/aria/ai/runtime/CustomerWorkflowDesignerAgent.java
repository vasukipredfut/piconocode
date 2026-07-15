package com.aria.ai.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.ai.AgentContext;
import com.aria.ai.AgentDecision;
import com.aria.ai.AiAgent;
import com.aria.ai.AiAgentHandler;
import com.aria.ai.llm.LlmProvider;
import com.aria.ai.llm.LlmProxy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ai-element
 *   kind:      LLM
 *   agent:     customer.workflow-designer
 *   mode-max:  SUPERVISED  (customer confirms every workflow before it applies)
 *   inputs:    { description: "natural-language description of the process" }
 *   outputs:   AgentDecision.SAVE with actionArgs = { workflowCode, states[], transitions[] }
 *              matching the workflow_definition body_json schema exactly.
 *
 * Customer AI that turns a natural-language process description into a
 * workflow_definition body. The AiExplainerPanel visualises the proposed
 * state machine; customer confirms; the workflow is applied.
 *
 * Example customer prompts:
 *   - "when supplier submits kyc, compliance reviews, then either approved or rejected"
 *   - "return stock: parking, qc, if pass then unload+close, if fail then reject+return"
 *   - "invoice approval: submit → manager review → if under 100k auto-approve else escalate"
 *
 * Deterministic version parses the arrow-based description language, extracts
 * state names, and infers guard conditions from "if/else" phrases. Real LLM
 * ordering will replace the parser once LlmProxy lands.
 */
@AiAgent(
        code = "customer.workflow-designer",
        displayName = "Workflow designer (customer)",
        model = "TBD — see docs/architecture/AI-TECH-DECISIONS.md",
        kind = AiAgent.Kind.LLM,
        population = AiAgent.Population.RUNTIME,
        maxMode = AiAgent.Mode.SUPERVISED,
        tools = { "datasource.query", "save" },
        description = "Turns a natural-language process description into a workflow_definition body_json proposal"
)
@Component
public class CustomerWorkflowDesignerAgent implements AiAgentHandler {

    private static final String CODE = "customer.workflow-designer";
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Pattern ARROW = Pattern.compile("([^→\\-]+?)\\s*(?:→|->|then)\\s*", Pattern.CASE_INSENSITIVE);

    private final JdbcTemplate jdbc;
    private final LlmProxy llm;

    public CustomerWorkflowDesignerAgent(JdbcTemplate jdbc, LlmProxy llm) {
        this.jdbc = jdbc;
        this.llm = llm;
    }

    @Override public String code() { return CODE; }

    @Override
    public AgentDecision run(AgentContext ctx) {
        String desc = String.valueOf(ctx.inputs().getOrDefault("description", "")).trim();
        if (desc.isEmpty()) {
            return AgentDecision.fail("empty description — need a process description");
        }

        // TRY LLM FIRST — small CPU-bound model handles this well.
        // Falls back to deterministic if LLM unavailable / errors.
        Optional<AgentDecision> llmResult = tryLlmGenerate(desc);
        if (llmResult.isPresent()) return llmResult.get();

        // Parse state names from arrow sequences ("X → Y → Z" or "X then Y then Z")
        List<String> stateNames = extractStates(desc);
        if (stateNames.size() < 2) {
            return AgentDecision.fail("could not extract at least 2 states from description");
        }

        // Detect approval-branch keywords → adds APPROVED/REJECTED with guards
        boolean hasApproval = desc.toLowerCase().matches(".*\\b(approve|approval|reject|pass|fail)\\b.*");

        // Build workflow body
        List<Map<String, Object>> states = new ArrayList<>();
        List<Map<String, Object>> transitions = new ArrayList<>();
        // Default role for the deterministic path. Without this, the
        // workflow runs but never routes to a user's inbox — the customer
        // is left wondering where their work went. Manager is the safe
        // default; the AI could pick better roles later.
        states.add(Map.of("code", "DRAFT", "assignedRole", "Manager"));

        String prev = "DRAFT";
        for (int i = 0; i < stateNames.size(); i++) {
            String code = normaliseState(stateNames.get(i));
            boolean isLast = (i == stateNames.size() - 1);
            Map<String, Object> stateRow = new LinkedHashMap<>();
            stateRow.put("code", code);
            stateRow.put("assignedRole", "Manager");
            if (isLast) stateRow.put("terminal", true);
            states.add(stateRow);

            transitions.add(Map.of(
                    "from", prev, "to", code,
                    "trigger", "page.submitted",
                    "pageCode", "custom:" + code.toLowerCase().replace('_', '-')));
            prev = code;
        }

        // If approval branch — add REJECTED terminal + guard on the last transition
        if (hasApproval && !stateNames.isEmpty()) {
            String lastState = normaliseState(stateNames.get(stateNames.size() - 1));
            states.add(Map.of("code", "REJECTED", "terminal", true));
            // Add reject alternative
            transitions.add(Map.of(
                    "from", transitions.get(transitions.size() - 1).get("from"),
                    "to", "REJECTED",
                    "trigger", "page.submitted",
                    "pageCode", "custom:" + lastState.toLowerCase().replace('_', '-'),
                    "guard", "#submitted['decision'] == 'REJECTED'"));
        }

        // Detect customer prefix for the workflow code
        String prefix = detectCustomerPrefix(ctx.tenantId());
        String workflowCode = prefix.toUpperCase() + "." + camelName(desc);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", workflowCode);
        body.put("version", 1);
        body.put("initialState", "DRAFT");
        body.put("states", states);
        body.put("transitions", transitions);

        Map<String, Object> args = Map.of(
                "workflowCode", workflowCode,
                "stateCount", states.size(),
                "transitionCount", transitions.size(),
                "hasApprovalBranch", hasApproval,
                "bodyJson", serialize(body));

        String reason = String.format(
                "Proposed workflow '%s' with %d states, %d transitions%s. Prompt: %s",
                workflowCode, states.size(), transitions.size(),
                hasApproval ? " + approval branch" : "",
                truncate(desc, 80));

        return new AgentDecision(
                AgentDecision.Action.SAVE,
                args,
                reason,
                new BigDecimal("0.70"),
                Map.of("state_count", states.size(),
                       "transition_count", transitions.size(),
                       "llm_used", false));
    }

    // ---- LLM path ----------------------------------------------------------

    private Optional<AgentDecision> tryLlmGenerate(String desc) {
        String systemPrompt = """
                You are a workflow designer for EARMS, a factory ERP for Indian SMEs. You produce
                JSON describing a state machine from a natural-language process description.

                Rules:
                - Every state code is SCREAMING_SNAKE_CASE.
                - initialState is always "DRAFT".
                - Every terminal state must have "terminal": true.
                - Every transition has "from", "to", "trigger" ("page.submitted"), and optionally
                  "guard" (SpEL expression on submitted form data).
                - For approval branches use guards like "#submitted['decision'] == 'APPROVED'".
                - Output ONLY the JSON, no prose.
                """;

        String schema = """
                {
                  "code": "<UPPER_CASE_WORKFLOW_CODE>",
                  "version": 1,
                  "initialState": "DRAFT",
                  "states": [ { "code": "STATE_CODE", "assignedRole": "RoleName", "terminal": false } ],
                  "transitions": [ { "from": "FROM", "to": "TO", "trigger": "page.submitted",
                                     "pageCode": "custom:some-page", "guard": "optional SpEL" } ]
                }
                """;

        Optional<LlmProvider.LlmResult> res = llm.tryGenerate(systemPrompt, desc, schema, 1500);
        if (res.isEmpty()) return Optional.empty();

        try {
            JsonNode wf = JSON.readTree(res.get().json());
            // Sanitize the LLM output so downstream consumers get clean data:
            //  - strip the workflow-name prefix out of state codes
            //    (LLM tends to emit RETURN_MATERIAL_WORKFLOW_REQUEST_RETURN when
            //     it should just say REQUEST_RETURN)
            //  - fill in assignedRole where the LLM forgot (defaults to Manager)
            //    so the workflow actually routes to someone's inbox
            wf = sanitiseLlmWorkflow(wf);
            int stateCount = wf.path("states").size();
            int transitionCount = wf.path("transitions").size();
            String workflowCode = wf.path("code").asText("CUSTOM.Unnamed");

            String bodyJsonStr = JSON.writeValueAsString(wf);

            java.util.Map<String, Object> args = java.util.Map.of(
                    "workflowCode", workflowCode,
                    "stateCount", stateCount,
                    "transitionCount", transitionCount,
                    "hasApprovalBranch", bodyJsonStr.toLowerCase().contains("approv") ||
                                          bodyJsonStr.toLowerCase().contains("reject"),
                    "bodyJson", bodyJsonStr,
                    "llm_used", true,
                    "llm_model", res.get().modelUsed());

            String reason = String.format(
                    "LLM (%s) proposed workflow '%s' with %d states, %d transitions. Latency %dms.",
                    res.get().modelUsed(), workflowCode, stateCount, transitionCount, res.get().latencyMs());

            return Optional.of(new AgentDecision(
                    AgentDecision.Action.SAVE, args, reason,
                    new java.math.BigDecimal("0.85"),
                    java.util.Map.of(
                            "tokens_in", res.get().tokensIn(),
                            "tokens_out", res.get().tokensOut(),
                            "latency_ms", res.get().latencyMs(),
                            "llm_used", true)));

        } catch (Exception e) {
            // LLM returned malformed JSON — fall through to deterministic
            return Optional.empty();
        }
    }

    // ---- Deterministic path ------------------------------------------------

    private List<String> extractStates(String desc) {
        List<String> out = new ArrayList<>();
        Matcher m = ARROW.matcher(desc);
        int lastEnd = 0;
        while (m.find()) {
            String seg = m.group(1).trim();
            if (!seg.isEmpty()) out.add(seg);
            lastEnd = m.end();
        }
        String tail = desc.substring(lastEnd).trim();
        if (!tail.isEmpty()) out.add(tail);
        // Fallback — split on commas if no arrows found
        if (out.size() < 2) {
            out.clear();
            for (String s : desc.split("[,;]")) {
                String t = s.trim();
                if (!t.isEmpty()) out.add(t);
            }
        }
        return out;
    }

    private String normaliseState(String raw) {
        String s = raw.trim().toUpperCase();
        s = s.replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
        if (s.length() > 40) s = s.substring(0, 40);
        return s;
    }

    /**
     * Clean up common quirks in the LLM's workflow output:
     *
     * <ul>
     *   <li>The LLM often prefixes state codes with the workflow name — e.g.
     *       for "Return material workflow: request return then inspect" it
     *       emits {@code RETURN_MATERIAL_WORKFLOW_REQUEST_RETURN} instead of
     *       just {@code REQUEST_RETURN}. Strip the code prefix so state
     *       names stay legible.</li>
     *   <li>When {@code assignedRole} is missing on a state, default it to
     *       {@code Manager} so the workflow instance actually routes to a
     *       user's inbox. Without a role, transitions succeed but nobody
     *       ever sees the work.</li>
     * </ul>
     *
     * Returns a NEW ObjectNode so the caller doesn't mutate the LLM's raw output.
     */
    private JsonNode sanitiseLlmWorkflow(JsonNode wf) {
        com.fasterxml.jackson.databind.node.ObjectNode out = JSON.createObjectNode();
        String code = wf.path("code").asText("CUSTOM.Unnamed");
        out.put("code", code);
        out.put("version", 1);
        String init = wf.path("initialState").asText("DRAFT");

        // Compute the "prefix to strip" from the workflow code: the code minus
        // any leading {custom.,CUSTOM.,m10.,…} prefix, and upper-snake'd.
        String stripPrefix = code.replaceFirst("(?i)^[A-Z]+\\.", "").toUpperCase()
                                 .replaceAll("[^A-Z0-9]+", "_") + "_";

        java.util.function.Function<String, String> clean = raw -> {
            if (raw == null) return null;
            String s = raw.toUpperCase().replaceAll("[^A-Z0-9]+", "_").replaceAll("^_|_$", "");
            if (s.startsWith(stripPrefix) && s.length() > stripPrefix.length()) {
                s = s.substring(stripPrefix.length());
            }
            if (s.length() > 40) s = s.substring(0, 40);
            return s;
        };
        out.put("initialState", clean.apply(init));

        com.fasterxml.jackson.databind.node.ArrayNode states = out.putArray("states");
        for (JsonNode s : wf.path("states")) {
            com.fasterxml.jackson.databind.node.ObjectNode st = JSON.createObjectNode();
            st.put("code", clean.apply(s.path("code").asText("")));
            String role = s.path("assignedRole").asText("");
            st.put("assignedRole", role.isEmpty() ? "Manager" : role);
            if (s.path("terminal").asBoolean(false)) st.put("terminal", true);
            states.add(st);
        }

        com.fasterxml.jackson.databind.node.ArrayNode transitions = out.putArray("transitions");
        for (JsonNode t : wf.path("transitions")) {
            com.fasterxml.jackson.databind.node.ObjectNode tr = JSON.createObjectNode();
            tr.put("from", clean.apply(t.path("from").asText("")));
            tr.put("to", clean.apply(t.path("to").asText("")));
            String trig = t.path("trigger").asText("page.submitted");
            tr.put("trigger", trig);
            if (!t.path("pageCode").isMissingNode()) tr.put("pageCode", t.path("pageCode").asText());
            if (!t.path("guard").isMissingNode())    tr.put("guard",    t.path("guard").asText());
            transitions.add(tr);
        }
        return out;
    }

    private String camelName(String desc) {
        String[] parts = desc.trim().split("\\s+");
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            String p = parts[i].replaceAll("[^A-Za-z0-9]", "");
            if (p.isEmpty()) continue;
            out.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) out.append(p.substring(1).toLowerCase());
        }
        return out.length() == 0 ? "Custom" : out.toString();
    }

    private String detectCustomerPrefix(Long tenantId) {
        try {
            String code = jdbc.queryForObject(
                    "SELECT code FROM tenant WHERE id=?", String.class, tenantId);
            return code == null ? "custom" : code.toLowerCase();
        } catch (Exception e) {
            return "custom";
        }
    }

    private String truncate(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }

    private String serialize(Object o) {
        try { return JSON.writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }
}
