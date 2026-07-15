package com.aria.ai.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Tool: {@code entity.count} — quick count with an optional filter.
 *
 * <p>The LLM calls this when the user's question is really "how many …?"
 * — for headline numbers ("47 orders pending", "3 gate passes with
 * exceptions today"). Same safety envelope as {@link EntityQueryTool} but
 * returns a single number instead of rows.
 */
@Component
public class EntityCountTool implements AgentTool {

    private static final Logger log = LoggerFactory.getLogger(EntityCountTool.class);
    private static final Set<String> ALLOWED_OPS = Set.of(
            "eq", "ne", "gt", "gte", "lt", "lte",
            "contains", "startsWith", "in", "isNull", "isNotNull");

    private final JdbcTemplate jdbc;
    private final EntityRegistryHelper reg;

    public EntityCountTool(JdbcTemplate jdbc, EntityRegistryHelper reg) {
        this.jdbc = jdbc;
        this.reg = reg;
    }

    @Override public String name() { return "entity.count"; }

    @Override
    public String description() {
        return "Count rows in an entity, optionally filtered. Same filter grammar as entity.query. "
             + "Returns {ok, entity, count, sql}.";
    }

    @Override
    public Map<String, Object> argsSchema() {
        return Map.of(
                "entity", Map.of("type", "string", "required", true,
                                 "desc", "Entity code from entity.list."),
                "filter", Map.of("type", "array",  "required", false,
                                 "desc", "[{field, op, value}]. AND-ed. Optional."));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(Map<String, Object> args, Long tenantId) {
        String entity = strArg(args, "entity");
        if (entity == null || !reg.safeIdent(entity)) return fail("missing or invalid arg 'entity'");

        Map<String, Object> row = reg.lookup(entity);
        if (row == null) return fail("unknown entity: " + entity);
        String table = (String) row.get("table_name");
        if (!reg.safeIdent(table)) return fail("registry table_name failed safety check");
        Set<String> allowedCols = reg.allowedColumns(entity);
        boolean tenantCol = reg.hasTenantId(table);

        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder();
        int dropped = 0;

        if (tenantCol && tenantId != null) {
            where.append(" WHERE tenant_id=?");
            params.add(tenantId);
        }

        Object rawFilter = args == null ? null : args.get("filter");
        if (rawFilter instanceof List) {
            for (Object o : (List<Object>) rawFilter) {
                if (!(o instanceof Map)) { dropped++; continue; }
                Map<String, Object> cond = (Map<String, Object>) o;
                String field = strVal(cond.get("field"));
                String op    = strVal(cond.get("op"));
                Object value = cond.get("value");

                if (field == null || !reg.safeIdent(field) || !allowedCols.contains(field)) { dropped++; continue; }
                if (op == null) { dropped++; continue; }
                op = op.toLowerCase(Locale.ROOT);
                if (!ALLOWED_OPS.contains(op)) { dropped++; continue; }

                String clause = renderClause(field, op, value, params);
                if (clause == null) { dropped++; continue; }
                where.append(where.length() == 0 ? " WHERE " : " AND ").append(clause);
            }
        }

        String sql = "SELECT COUNT(*) FROM " + table + where;
        try {
            Long count = jdbc.queryForObject(sql, Long.class, params.toArray());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", true);
            out.put("entity", entity);
            out.put("table", table);
            out.put("count", count == null ? 0 : count);
            out.put("filterDropped", dropped);
            out.put("sql", sql);
            return out;
        } catch (Exception e) {
            log.warn("entity.count failed: {}", e.getMessage());
            return fail("count failed: " + e.getMessage());
        }
    }

    private String renderClause(String field, String op, Object value, List<Object> params) {
        switch (op) {
            case "eq":  params.add(value); return field + " = ?";
            case "ne":  params.add(value); return field + " <> ?";
            case "gt":  params.add(value); return field + " > ?";
            case "gte": params.add(value); return field + " >= ?";
            case "lt":  params.add(value); return field + " < ?";
            case "lte": params.add(value); return field + " <= ?";
            case "contains":
                params.add("%" + value + "%"); return field + " LIKE ?";
            case "startsWith":
                params.add(value + "%"); return field + " LIKE ?";
            case "in": {
                if (!(value instanceof List)) return null;
                List<?> list = (List<?>) value;
                if (list.isEmpty()) return null;
                StringBuilder marks = new StringBuilder("(");
                for (int i = 0; i < list.size(); i++) {
                    if (i > 0) marks.append(',');
                    marks.append('?');
                    params.add(list.get(i));
                }
                marks.append(')');
                return field + " IN " + marks;
            }
            case "isNull":    return field + " IS NULL";
            case "isNotNull": return field + " IS NOT NULL";
            default: return null;
        }
    }

    private String strArg(Map<String, Object> args, String key) {
        Object v = args == null ? null : args.get(key);
        return v == null ? null : String.valueOf(v).trim();
    }

    private String strVal(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }
}
