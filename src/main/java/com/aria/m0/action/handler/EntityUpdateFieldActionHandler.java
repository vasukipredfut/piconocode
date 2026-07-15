package com.aria.m0.action.handler;

import com.aria.m0.action.ActionHandler;
import com.aria.m0.action.ActionInvocation;
import com.aria.m0.action.ActionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Direct field update on an entity row — used by Inbox-style row actions
 * (e.g. clicking "Approve" on a Material-Requisition row sets status='APPROVED').
 *
 * <p>The richer {@code workflow.transition} action requires the row to have a
 * live {@code workflow_instance_id}; seed rows + retrofitted lists don't have
 * one. This handler short-circuits straight to {@code GenericEntityController}'s
 * update path so the visible behaviour ("approve a row, watch it disappear from
 * the SUBMITTED inbox") works even before the workflow engine is bound.
 *
 * <p>Payload shape:
 * <pre>
 *   {
 *     "args": {
 *       "field": "status",
 *       "value": "APPROVED"
 *     },
 *     "recordId": 42,
 *     "entity":   "MaterialRequisition"
 *   }
 * </pre>
 *
 * <p>SQL-safe: every identifier matched against {@code [A-Za-z0-9_]+} before
 * concat; values bound as JDBC parameters.
 */
@Component
public class EntityUpdateFieldActionHandler implements ActionHandler {

    private static final Pattern SAFE = Pattern.compile("^[A-Za-z0-9_]+$");

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "entity.update-field"; }

    @Override
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null) return ActionResult.error("invocation required");

        // The Action dispatcher unmarshals into ActionInvocation but we don't
        // know its exact field layout — read defensively from getArgs().
        Map<String, Object> args  = inv.getArgs() == null ? Map.of() : inv.getArgs();
        Object entityObj  = firstOf(args.get("entity"),  inv.getEntity());
        Object recordObj  = firstOf(args.get("recordId"), inv.getRecordId());
        Object fieldObj   = args.get("field");
        Object valueObj   = args.get("value");

        if (entityObj == null) return ActionResult.error("entity is required");
        if (recordObj == null) return ActionResult.error("recordId is required");
        if (fieldObj  == null) return ActionResult.error("args.field is required");

        String entityCode = entityObj.toString();
        String field      = fieldObj.toString();
        if (!SAFE.matcher(entityCode).matches()) return ActionResult.error("unsafe entity code");
        if (!SAFE.matcher(field).matches())      return ActionResult.error("unsafe field name");

        long recordId;
        try { recordId = Long.parseLong(recordObj.toString()); }
        catch (NumberFormatException e) { return ActionResult.error("recordId must be numeric"); }

        // Look up the table
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

        // Optional safety: confirm the field is registered for this entity OR
        // is one of a small whitelist (status / current_step) so a misbehaving
        // page can't update arbitrary columns.
        boolean allowed;
        if (field.equals("status") || field.equals("current_step")) {
            allowed = true;
        } else {
            Integer n;
            try {
                n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM entity_field_registry WHERE entity_code=? AND field_code=?",
                    Integer.class, entityCode, field);
            } catch (Exception e) { return ActionResult.error("field check failed: " + e.getMessage()); }
            allowed = n != null && n > 0;
        }
        if (!allowed) return ActionResult.error("field not allowed: " + field);

        try {
            int n = jdbc.update("UPDATE " + table + " SET " + field + " = ? WHERE id = ?",
                                valueObj, recordId);
            if (n == 0) return ActionResult.error("no row updated (record " + recordId + " not found)");
            ActionResult ok = ActionResult.ok();
            ok.setMessage(field + " set to " + valueObj + " on " + entityCode + " #" + recordId);
            return ok;
        } catch (Exception e) {
            return ActionResult.error("update failed: " + e.getMessage());
        }
    }

    private Object firstOf(Object a, Object b) { return a != null ? a : b; }
}
