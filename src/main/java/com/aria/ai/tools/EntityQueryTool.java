package com.aria.ai.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Tool: {@code entity.query} — parameterized SELECT with a JSON filter grammar.
 *
 * <p>The LLM produces a filter of shape:
 * <pre>
 *   [
 *     { "field": "status",   "op": "eq",       "value": "PENDING" },
 *     { "field": "due_date", "op": "lt",       "value": "2026-07-11" }
 *   ]
 * </pre>
 * Conditions are AND-ed (there's no OR in the grammar — kept intentionally
 * simple; if we need OR later we'll add a nested {@code any:[...]} node).
 *
 * <p>Safety:
 * <ul>
 *   <li>Table name comes from {@code entity_registry} — never from the LLM.</li>
 *   <li>Every column referenced must appear in {@code entity_field_registry}
 *       for that entity — anything else is silently dropped.</li>
 *   <li>Every value goes through {@code PreparedStatement} bindings.</li>
 *   <li>Tenant scoping is added if the table has a {@code tenant_id} column.</li>
 *   <li>Hard row cap of {@value #MAX_LIMIT} so a runaway LLM can't flood
 *       the context window.</li>
 * </ul>
 */
@Component
public class EntityQueryTool implements AgentTool {

    private static final Logger log = LoggerFactory.getLogger(EntityQueryTool.class);
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 200;
    private static final Set<String> ALLOWED_OPS = Set.of(
            "eq", "ne", "gt", "gte", "lt", "lte",
            "contains", "startsWith", "in", "isNull", "isNotNull");
    private static final Set<String> ALLOWED_DIRS = Set.of("ASC", "DESC");

    private final JdbcTemplate jdbc;
    private final EntityRegistryHelper reg;

    public EntityQueryTool(JdbcTemplate jdbc, EntityRegistryHelper reg) {
        this.jdbc = jdbc;
        this.reg = reg;
    }

    @Override public String name() { return "entity.query"; }

    @Override
    public String description() {
        return "Query rows from an entity with an optional filter, order, and row limit. "
             + "Filter is [{field, op, value}]. Ops: eq, ne, gt, gte, lt, lte, contains, "
             + "startsWith, in, isNull, isNotNull. Conditions are AND-ed. Returns "
             + "{ok, entity, rows, count, sql}. Cap 200 rows.";
    }

    @Override
    public Map<String, Object> argsSchema() {
        return Map.of(
                "entity",  Map.of("type", "string",  "required", true,
                                  "desc", "Entity code from entity.list."),
                "filter",  Map.of("type", "array",   "required", false,
                                  "desc", "[{field, op, value}]. AND-ed. Optional."),
                "orderBy", Map.of("type", "object",  "required", false,
                                  "desc", "{field, dir:'ASC'|'DESC'}. Optional."),
                "limit",   Map.of("type", "integer", "required", false, "default", DEFAULT_LIMIT,
                                  "desc", "Row cap. Max 200."));
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

        // Tenant scope first — mandatory when the column exists.
        if (tenantCol && tenantId != null) {
            where.append(" WHERE tenant_id=?");
            params.add(tenantId);
        }

        // Parse filter clauses.
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

        // Ordering.
        StringBuilder order = new StringBuilder();
        Object rawOrder = args == null ? null : args.get("orderBy");
        if (rawOrder instanceof Map) {
            Map<String, Object> ord = (Map<String, Object>) rawOrder;
            String field = strVal(ord.get("field"));
            String dir   = strVal(ord.get("dir"));
            dir = dir == null ? "ASC" : dir.toUpperCase(Locale.ROOT);
            if (field != null && reg.safeIdent(field) && allowedCols.contains(field)
                && ALLOWED_DIRS.contains(dir)) {
                order.append(" ORDER BY ").append(field).append(' ').append(dir);
            }
        }

        // Row cap.
        int limit = DEFAULT_LIMIT;
        Object rawLimit = args == null ? null : args.get("limit");
        if (rawLimit instanceof Number) limit = ((Number) rawLimit).intValue();
        else if (rawLimit instanceof String) try { limit = Integer.parseInt((String) rawLimit); } catch (Exception ignore) {}
        if (limit < 1) limit = 1;
        if (limit > MAX_LIMIT) limit = MAX_LIMIT;

        String sql = "SELECT * FROM " + table + where + order + " LIMIT " + limit;
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(sql, params.toArray());
            Map<String, Object> out = new LinkedHashMap<>();
            out.put("ok", true);
            out.put("entity", entity);
            out.put("table", table);
            out.put("rows", rows);
            out.put("count", rows.size());
            out.put("limit", limit);
            out.put("filterDropped", dropped);
            // Echo the SQL (without params) so the UI can display it in the tool-trace panel.
            out.put("sql", sql);
            return out;
        } catch (Exception e) {
            log.warn("entity.query failed: {}", e.getMessage());
            return fail("query failed: " + e.getMessage());
        }
    }

    /** Emit one WHERE-clause fragment and push its params. Null on invalid input. */
    private String renderClause(String field, String op, Object value, List<Object> params) {
        switch (op) {
            case "eq":  params.add(value); return field + " = ?";
            case "ne":  params.add(value); return field + " <> ?";
            case "gt":  params.add(value); return field + " > ?";
            case "gte": params.add(value); return field + " >= ?";
            case "lt":  params.add(value); return field + " < ?";
            case "lte": params.add(value); return field + " <= ?";
            case "contains":
                params.add("%" + String.valueOf(value) + "%");
                return field + " LIKE ?";
            case "startsWith":
                params.add(String.valueOf(value) + "%");
                return field + " LIKE ?";
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
            case "isNull":     return field + " IS NULL";
            case "isNotNull":  return field + " IS NOT NULL";
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

    // Reserved for future OR-node support — silences the unused import if we
    // wire it later. Not used today; kept as a marker for the intended grammar.
    @SuppressWarnings("unused")
    private static final List<String> RESERVED_OPS = Arrays.asList("any", "all");
}
