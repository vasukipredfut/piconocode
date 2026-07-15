package com.aria.ai.runtime;

import com.aria.ai.AgentContext;
import com.aria.ai.AgentDecision;
import com.aria.ai.AiAgent;
import com.aria.ai.AiAgentHandler;
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
 *   agent:     customer.field-designer
 *   mode-max:  SUPERVISED  (customer confirms every schema change)
 *   inputs:    { entityCode: "Supplier", fieldsDescription: "name, gstin, primary contact..." }
 *   outputs:   AgentDecision.SAVE with actionArgs.fields = [{ field_code, display_label,
 *              data_type, widget_kind, required, options_json }]
 *
 * Customer AI that turns a natural-language description of columns into
 * entity_field_registry-ready rows. The AiExplainerPanel shows the proposed
 * fields; customer confirms; DesignerController POST adds them.
 *
 * Example customer prompts:
 *   - "add fields: name, gstin, contact number, is_active checkbox"
 *   - "supplier needs a category dropdown with options Manufacturer/Trader/Distributor"
 *   - "add a mandatory pincode field and an optional gps_lat / gps_lng"
 *
 * Deterministic heuristics — inflates common noun → data_type mapping. Real
 * LLM ordering will replace guessType/guessWidget once LlmProxy lands.
 */
@AiAgent(
        code = "customer.field-designer",
        displayName = "Field designer (customer)",
        model = "TBD — see docs/architecture/AI-TECH-DECISIONS.md",
        kind = AiAgent.Kind.LLM,
        population = AiAgent.Population.RUNTIME,
        maxMode = AiAgent.Mode.SUPERVISED,
        tools = { "datasource.query", "save" },
        description = "Turns natural-language column descriptions into entity_field_registry-ready rows"
)
@Component
public class CustomerFieldDesignerAgent implements AiAgentHandler {

    private static final String CODE = "customer.field-designer";
    private final JdbcTemplate jdbc;

    public CustomerFieldDesignerAgent(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public String code() { return CODE; }

    @Override
    public AgentDecision run(AgentContext ctx) {
        String entity = String.valueOf(ctx.inputs().getOrDefault("entityCode", "")).trim();
        String desc   = String.valueOf(ctx.inputs().getOrDefault("fieldsDescription", "")).trim();
        if (entity.isEmpty() || desc.isEmpty()) {
            return AgentDecision.fail("need both entityCode and fieldsDescription");
        }

        // Existing fields — skip anything already registered
        List<String> existing = loadExistingFields(entity);

        // Split "name, gstin, is_active checkbox" into candidate phrases
        List<Map<String, Object>> proposed = new ArrayList<>();
        for (String rawPart : desc.split("[,;\\n]")) {
            String part = rawPart.trim();
            if (part.isEmpty()) continue;
            Map<String, Object> f = inferField(part);
            String code = (String) f.get("field_code");
            if (code != null && !existing.contains(code)) {
                proposed.add(f);
            }
        }

        if (proposed.isEmpty()) {
            return AgentDecision.noop("no new fields to add — all proposed columns already exist");
        }

        Map<String, Object> args = Map.of(
                "entityCode", entity,
                "fields", proposed,
                "skippedExisting", existing.size());

        String reason = String.format(
                "Proposed %d new field(s) for entity '%s'. Skipped %d existing.",
                proposed.size(), entity, existing.size());

        return new AgentDecision(
                AgentDecision.Action.SAVE,
                args,
                reason,
                new BigDecimal("0.75"),
                Map.of("proposed_count", proposed.size(), "llm_used", false));
    }

    // ---- private helpers ---------------------------------------------------

    private List<String> loadExistingFields(String entityCode) {
        try {
            return jdbc.queryForList(
                    "SELECT field_code FROM entity_field_registry WHERE entity_code=?",
                    String.class, entityCode);
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Object> inferField(String phrase) {
        String p = phrase.toLowerCase().trim();
        boolean required = p.contains("mandatory") || p.contains("required");

        String widget = guessWidget(p);
        String dataType = mapDataType(widget);

        // Strip modifiers to get the base field name
        String base = p.replaceAll("\\b(mandatory|required|optional|is|has|the|a|checkbox|dropdown|select|number|date|datetime|email|password|text|boolean)\\b", "").trim();
        String code = base.replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        if (code.isEmpty()) code = "field_" + Math.abs(phrase.hashCode() % 1000);
        String label = capitalizeWords(base.isEmpty() ? phrase : base);

        Map<String, Object> field = new LinkedHashMap<>();
        field.put("field_code", code);
        field.put("display_label", label);
        field.put("data_type", dataType);
        field.put("widget_kind", widget);
        field.put("required", required);
        return field;
    }

    private String guessWidget(String p) {
        if (p.contains("checkbox") || p.contains("boolean") || p.contains("is ") || p.contains("has ")) return "checkbox";
        if (p.contains("dropdown") || p.contains("select"))       return "select";
        if (p.contains("email"))                                  return "email";
        if (p.contains("password"))                               return "password";
        if (p.contains("datetime") || p.contains("date time"))    return "datetime";
        if (p.contains("date") || p.contains("dob"))              return "date";
        if (p.contains("phone") || p.contains("mobile") || p.contains("tel")) return "tel";
        if (p.contains("number") || p.contains("qty") || p.contains("amount") || p.contains("count")) return "number";
        if (p.contains("textarea") || p.contains("description") || p.contains("notes")) return "textarea";
        if (p.contains("file") || p.contains("upload") || p.contains("photo") || p.contains("attachment")) return "file";
        return "text";
    }

    private String mapDataType(String widget) {
        return switch (widget) {
            case "checkbox" -> "BOOLEAN";
            case "number"   -> "DECIMAL";
            case "date"     -> "DATE";
            case "datetime" -> "DATETIME";
            default         -> "STRING";
        };
    }

    private String capitalizeWords(String s) {
        String[] parts = s.trim().split("\\s+");
        StringBuilder out = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) out.append(p.substring(1));
        }
        return out.toString();
    }
}
