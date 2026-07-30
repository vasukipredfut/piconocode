package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Field update across every row matching a set of column filters — for row
 * actions on a grouped/aggregated ListView (e.g. a summary entity backed by a
 * {@code GROUP BY} view) where the visible "row" represents many underlying
 * records and there is no single id to target with {@link EntityUpdateFieldActionHandler}.
 *
 * <p>Payload shape:
 * <pre>
 *   {
 *     "args": {
 *       "entity": "OrderDispatchDetail",
 *       "field": "status",
 *       "value": "PAY_COMPLETED",
 *       "match": { "route_id": "ROU024693", "sku_code": "SKU001" }
 *     }
 *   }
 * </pre>
 *
 * <p>{@code match} must be non-empty — this handler refuses to run an
 * unfiltered bulk update. Every entity/field/match-key is validated against
 * {@code entity_registry} / {@code entity_field_registry} before being
 * concatenated into SQL (same allowlist discipline as
 * {@link EntityUpdateFieldActionHandler}); values are always bound as JDBC
 * parameters. Tenant scoping is enforced via {@link TenantScope}, matching
 * every other write path in the app.
 */
@Component
public class EntityBulkUpdateFieldActionHandler implements ActionHandler {

    private static final Pattern SAFE = Pattern.compile("^[A-Za-z0-9_]+$");

    @Autowired private JdbcTemplate jdbc;
    @Autowired private TenantScope tenantScope;

    @Override
    public String code() { return "entity.bulk-update-field"; }

    @Override
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null) return ActionResult.error("invocation required");

        Map<String, Object> args = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Object entityObj = args.get("entity") != null ? args.get("entity") : inv.getEntity();
        Object fieldObj  = args.get("field");
        Object valueObj  = args.get("value");
        Object matchObj  = args.get("match");

        if (entityObj == null) return ActionResult.error("entity is required");
        if (fieldObj == null)  return ActionResult.error("args.field is required");
        if (!(matchObj instanceof Map) || ((Map<?, ?>) matchObj).isEmpty()) {
            return ActionResult.error("args.match (non-empty column filter map) is required — refusing an unfiltered bulk update");
        }

        String entityCode = entityObj.toString();
        String field = fieldObj.toString();
        Map<String, Object> match = (Map<String, Object>) matchObj;

        if (!SAFE.matcher(entityCode).matches()) return ActionResult.error("unsafe entity code");
        if (!SAFE.matcher(field).matches())      return ActionResult.error("unsafe field name");
        for (String k : match.keySet()) {
            if (!SAFE.matcher(k).matches()) return ActionResult.error("unsafe match column: " + k);
        }

        String table;
        try {
            table = jdbc.queryForObject(
                "SELECT table_name FROM entity_registry WHERE code=?", String.class, entityCode);
        } catch (Exception e) {
            return ActionResult.error("unknown entity: " + entityCode);
        }
        if (table == null || !SAFE.matcher(table).matches()) {
            return ActionResult.error("entity registry returned unsafe table name");
        }

        if (!isFieldAllowed(entityCode, field)) return ActionResult.error("field not allowed: " + field);
        for (String k : match.keySet()) {
            if (!isFieldAllowed(entityCode, k)) return ActionResult.error("match column not allowed: " + k);
        }

        StringBuilder sql = new StringBuilder("UPDATE ").append(table).append(" SET ").append(field).append(" = ?");
        List<Object> bindArgs = new ArrayList<>();
        bindArgs.add(valueObj);

        LinkedHashMap<String, Object> where = new LinkedHashMap<>(match);
        sql.append(" WHERE ");
        List<String> whereParts = new ArrayList<>();
        for (Map.Entry<String, Object> e : where.entrySet()) {
            whereParts.add(e.getKey() + " = ?");
            bindArgs.add(e.getValue());
        }
        sql.append(String.join(" AND ", whereParts));

        String tenantPredicate = tenantScope.writePredicate(table, null);
        if (!tenantPredicate.isEmpty()) {
            sql.append(" AND ").append(tenantPredicate);
            bindArgs.add(tenantScope.arg());
        }

        try {
            int n = jdbc.update(sql.toString(), bindArgs.toArray());
            ActionResult ok = ActionResult.ok();
            ok.setMessage(n + " row(s) updated: " + entityCode + "." + field + " = " + valueObj);
            return ok;
        } catch (Exception e) {
            return ActionResult.error("bulk update failed: " + e.getMessage());
        }
    }

    private boolean isFieldAllowed(String entityCode, String field) {
        if (field.equals("status") || field.equals("current_step")) return true;
        Integer n;
        try {
            n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM entity_field_registry WHERE entity_code=? AND field_code=?",
                Integer.class, entityCode, field);
        } catch (Exception e) { return false; }
        return n != null && n > 0;
    }
}
