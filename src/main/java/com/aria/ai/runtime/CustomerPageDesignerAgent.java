package com.aria.ai.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.ai.AgentContext;
import com.aria.ai.AgentDecision;
import com.aria.ai.AiAgent;
import com.aria.ai.AiAgentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ai-element
 *   kind:      LLM
 *   agent:     customer.page-designer
 *   mode-max:  SUPERVISED  (customer confirms every page before it applies)
 *   inputs:    { prompt: "natural language description of the page needed",
 *                entity: "optional — which registered entity this page reads/writes" }
 *   outputs:   AgentDecision.SAVE with actionArgs = { pageCode, category, kind, bodyJson }
 *              Operator confirms via AiExplainerPanel; on confirm, the OnboardingController
 *              (or PageDefinitionController) writes the page_definition + page_version rows.
 *
 * Customer-facing AI agent that turns a natural-language page request into a
 * concrete page_definition + page_version.body_json proposal.
 *
 * Example customer prompts this handles:
 *   - "I need a list page for my suppliers, columns: name, gstin, primary contact"
 *   - "Add a form to capture daily boiler pressure reading with pressure, temp, operator"
 *   - "Dashboard showing overdue payments grouped by vendor with KPI tiles"
 *
 * This scaffold implements the deterministic pieces:
 *   1. Parses the prompt for entity hints + hint verbs (list/form/dashboard/kanban)
 *   2. Loads entity_field_registry for the resolved entity to suggest columns
 *   3. Builds a body_json skeleton in the correct schema
 *
 * The actual LLM call is stubbed with a TODO — the LlmProxy service (see
 * docs/architecture/AI-TECH-DECISIONS.md) will land in a follow-up sprint
 * and this class flips from "rule-based skeleton" to "LLM-augmented" without
 * changing its @AiAgent contract or run() signature.
 */
@AiAgent(
        code = "customer.page-designer",
        displayName = "Page designer (customer)",
        model = "TBD — see docs/architecture/AI-TECH-DECISIONS.md",
        kind = AiAgent.Kind.LLM,
        population = AiAgent.Population.RUNTIME,
        maxMode = AiAgent.Mode.SUPERVISED,
        tools = { "datasource.query", "save" },
        description = "Turns a natural-language page request into a page_definition + body_json proposal"
)
@Component
public class CustomerPageDesignerAgent implements AiAgentHandler {

    private static final String CODE = "customer.page-designer";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final JdbcTemplate jdbc;

    public CustomerPageDesignerAgent(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override public String code() { return CODE; }

    @Override
    public AgentDecision run(AgentContext ctx) {
        String prompt = String.valueOf(ctx.inputs().getOrDefault("prompt", "")).trim();
        String entity = String.valueOf(ctx.inputs().getOrDefault("entity", "")).trim();
        if (prompt.isEmpty()) {
            return AgentDecision.fail("empty prompt — need a page description");
        }

        // 1. Detect the page kind from verbs in the prompt
        String kind = detectKind(prompt);

        // 2. Resolve entity — either explicit, or lookup by keyword from prompt
        String resolvedEntity = entity.isEmpty() ? guessEntity(prompt) : entity;

        // 3. Load field list from entity_field_registry — this is what the LLM
        //    will use as its column-candidate palette
        List<Map<String, Object>> fields = loadFields(resolvedEntity);

        // 4. Build a bodyJson skeleton in the correct schema
        Map<String, Object> bodyJson = buildBodySkeleton(kind, resolvedEntity, fields);

        // 5. Propose a page code + category
        String customerPrefix = detectCustomerPrefix(ctx.tenantId());
        String pageCode = customerPrefix + ":" + slugify(prompt) + "-" + kind;
        String category = categoryFor(resolvedEntity);

        String bodyJsonStr = serialize(bodyJson);

        Map<String, Object> args = new LinkedHashMap<>();
        args.put("pageCode", pageCode);
        args.put("category", category);
        args.put("kind", kind);
        args.put("entity", resolvedEntity);
        args.put("bodyJson", bodyJsonStr);
        args.put("proposedTitle", proposeTitle(prompt));

        String reason = String.format(
            "Proposed a %s page for entity '%s' with %d columns. Prompt: %s",
            kind, resolvedEntity, fields.size(), truncate(prompt, 80));

        return new AgentDecision(
                AgentDecision.Action.SAVE,
                args,
                reason,
                new BigDecimal("0.70"),  // deterministic-only until LLM lands
                Map.of("fields_available", fields.size(),
                       "kind_detected", kind,
                       "llm_used", false));
    }

    // ---- private helpers ---------------------------------------------------

    private String detectKind(String prompt) {
        String p = prompt.toLowerCase();
        if (p.contains("list") || p.contains("table") || p.contains("all my")) return "list";
        if (p.contains("dashboard") || p.contains("overview") || p.contains("kpi")) return "layout";
        if (p.contains("kanban") || p.contains("board") || p.contains("swimlane")) return "kanban";
        if (p.contains("detail") || p.contains("view single")) return "detail";
        return "form"; // capture / entry / add / new all fall through to form
    }

    private String guessEntity(String prompt) {
        String p = prompt.toLowerCase();
        try {
            List<String> known = jdbc.queryForList(
                    "SELECT code FROM entity_registry ORDER BY code", String.class);
            for (String code : known) {
                if (p.contains(code.toLowerCase()) || p.contains(code.toLowerCase() + "s")) {
                    return code;
                }
            }
        } catch (Exception ignored) { /* empty DB — return empty */ }
        return "";
    }

    private List<Map<String, Object>> loadFields(String entityCode) {
        if (entityCode == null || entityCode.isEmpty()) return List.of();
        try {
            return jdbc.queryForList(
                    "SELECT field_code, display_label, data_type, widget_kind, required " +
                    "FROM entity_field_registry WHERE entity_code=? ORDER BY id",
                    entityCode);
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Object> buildBodySkeleton(String kind, String entity, List<Map<String, Object>> fields) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("schemaVersion", 2);
        body.put("kind", kind);
        if ("list".equals(kind) || "kanban".equals(kind)) {
            body.put("dataSource", Map.of("type", "entity", "entity", entity == null ? "" : entity));
            List<Map<String, Object>> cols = new ArrayList<>();
            int max = Math.min(fields.size(), 18);
            for (int i = 0; i < max; i++) {
                Map<String, Object> f = fields.get(i);
                cols.add(Map.of("field", f.get("field_code"),
                                "label", f.get("display_label")));
            }
            body.put("columns", cols);
        } else if ("form".equals(kind) || "layout".equals(kind)) {
            List<Map<String, Object>> layout = new ArrayList<>();
            List<Map<String, Object>> formFields = new ArrayList<>();
            for (Map<String, Object> f : fields) {
                Map<String, Object> ff = new LinkedHashMap<>();
                ff.put("kind", "FormField");
                ff.put("name", f.get("field_code"));
                ff.put("label", f.get("display_label"));
                ff.put("type", String.valueOf(f.getOrDefault("widget_kind", "text")).toLowerCase());
                if (Boolean.TRUE.equals(f.get("required"))) ff.put("required", true);
                formFields.add(ff);
            }
            layout.add(Map.of("kind", "Section", "props", Map.of("title", "Details"), "fields", formFields));
            body.put("layout", layout);
        }
        return body;
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

    private String categoryFor(String entity) {
        if (entity == null || entity.isEmpty()) return "Custom";
        try {
            String cat = jdbc.queryForObject(
                    "SELECT category FROM entity_registry WHERE code=?", String.class, entity);
            return cat == null ? "Custom" : cat;
        } catch (Exception e) {
            return "Custom";
        }
    }

    private String slugify(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private String proposeTitle(String prompt) {
        String s = prompt.trim();
        if (s.length() > 60) s = s.substring(0, 60);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String truncate(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n) + "...";
    }

    private String serialize(Object o) {
        try { return JSON.writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }
}
