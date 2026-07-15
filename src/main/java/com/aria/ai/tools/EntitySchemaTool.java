package com.aria.ai.tools;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Tool: {@code entity.schema} — describe an entity's fields.
 *
 * <p>The LLM calls this when it knows which entity but needs to know
 * which field to filter on (e.g., is it {@code status}, {@code state},
 * {@code order_status}?). Returns every field with data type, widget,
 * and search / filter / sort capabilities.
 */
@Component
public class EntitySchemaTool implements AgentTool {

    private final EntityRegistryHelper reg;

    public EntitySchemaTool(EntityRegistryHelper reg) {
        this.reg = reg;
    }

    @Override public String name() { return "entity.schema"; }

    @Override
    public String description() {
        return "Describe an entity's fields. Returns {entity, displayName, fields:[{fieldCode, "
             + "dataType, widgetKind, filterable, sortable}]}. Call this before entity.query if "
             + "you're unsure what fields exist.";
    }

    @Override
    public Map<String, Object> argsSchema() {
        return Map.of(
                "entity", Map.of("type", "string", "required", true,
                                 "desc",  "Entity code from entity.list, e.g. 'PurchaseOrder'."));
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> args, Long tenantId) {
        String entity = strArg(args, "entity");
        if (entity == null || !reg.safeIdent(entity)) {
            return fail("missing or invalid arg 'entity'");
        }
        Map<String, Object> row = reg.lookup(entity);
        if (row == null) {
            return fail("unknown entity: " + entity);
        }
        List<Map<String, Object>> fields = reg.fields(entity);

        Map<String, Object> out = ok("entity", entity);
        out.put("displayName", row.get("display_name"));
        out.put("table", row.get("table_name"));
        out.put("fields", fields);
        out.put("fieldCount", fields.size());
        return out;
    }

    private String strArg(Map<String, Object> args, String key) {
        Object v = args == null ? null : args.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }
}
