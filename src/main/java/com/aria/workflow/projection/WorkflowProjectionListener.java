package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Generic, config-driven replacement for the old bespoke M10 GatePassWorkflowListener.
 *
 * <p>On every workflow transition it looks up {@code m0_workflow_projection} rows
 * for the transitioning workflow and upserts the configured target row: syncing
 * the state column(s), stamping a generated number on first sight, mapping
 * submitted-data-bag fields to columns, and freezing a timestamp on terminal
 * states. All of that — which workflow projects into which table, and how — is
 * DATA, so ANY workflow can materialise a denormalised record with no Java.
 *
 * <p>Exceptions never propagate (the engine catches listener failures); a bad
 * projection config can't fail a transition.
 */
@Component
public class WorkflowProjectionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(WorkflowProjectionListener.class);
    private static final Pattern SAFE_IDENT = Pattern.compile("^[A-Za-z0-9_]+$");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JdbcTemplate jdbc;

    public WorkflowProjectionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState,
                             Map<String, Object> submittedDataBag) {
        String code = instance.getWorkflowCode();
        if (code == null) return;
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> configs;
        try {
            configs = jdbc.queryForList(
                    "SELECT * FROM m0_workflow_projection WHERE workflow_code=? AND tenant_id=?",
                    code, tenantId);
        } catch (Exception e) {
            return; // table not applied yet — nothing to project
        }
        for (Map<String, Object> cfg : configs) {
            try {
                project(cfg, instance, tenantId, submittedDataBag);
            } catch (Exception ex) {
                log.warn("projection failed for workflow={} instance={}: {}",
                        code, instance.getId(), ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void project(Map<String, Object> cfg, WorkflowInstance instance,
                         Long tenantId, Map<String, Object> bag) throws Exception {
        String table = ident((String) cfg.get("target_table"));
        String keyCol = ident(orDefault((String) cfg.get("key_column"), "workflow_instance_id"));
        Long instanceId = instance.getId();
        String state = instance.getCurrentState();

        // Column -> value to write.
        LinkedHashMap<String, Object> cols = new LinkedHashMap<>();
        for (String sc : splitCsv((String) cfg.get("state_columns"))) cols.put(ident(sc), state);

        // Mapped fields from the data bag (only when present/non-blank).
        String fieldMapJson = (String) cfg.get("field_map_json");
        if (fieldMapJson != null && !fieldMapJson.isBlank()) {
            Map<String, Object> map = MAPPER.readValue(fieldMapJson, Map.class);
            for (Map.Entry<String, Object> e : map.entrySet()) {
                List<String> keys = (List<String>) e.getValue();
                String v = pluck(bag, keys);
                if (v != null) cols.put(ident(e.getKey()), v);
            }
        }

        // Terminal timestamp.
        String terminalTsCol = (String) cfg.get("terminal_ts_column");
        if (terminalTsCol != null && splitCsv((String) cfg.get("terminal_states")).contains(state)) {
            cols.put(ident(terminalTsCol), Timestamp.from(Instant.now()));
        }

        boolean exists;
        try {
            Integer n = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM " + table + " WHERE " + keyCol + "=? AND tenant_id=?",
                    Integer.class, instanceId, tenantId);
            exists = n != null && n > 0;
        } catch (Exception e) {
            exists = false;
        }

        if (!exists) {
            // Insert-only columns: key, tenant, variant, generated number.
            cols.put("tenant_id", tenantId);
            cols.put(keyCol, instanceId);
            String variantCol = (String) cfg.get("variant_column");
            if (variantCol != null) cols.put(ident(variantCol), instance.getWorkflowCode());
            String numberCol = (String) cfg.get("number_column");
            String pattern = (String) cfg.get("number_pattern");
            if (numberCol != null && pattern != null) {
                cols.put(ident(numberCol), pattern
                        .replace("{tenant}", String.valueOf(tenantId))
                        .replace("{instance}", String.valueOf(instanceId)));
            }
            String columns = String.join(", ", cols.keySet());
            String qs = String.join(", ", Collections.nCopies(cols.size(), "?"));
            jdbc.update("INSERT INTO " + table + " (" + columns + ") VALUES (" + qs + ")",
                    cols.values().toArray());
        } else {
            String sets = String.join(", ", cols.keySet().stream().map(c -> c + "=?").toList());
            List<Object> args = new ArrayList<>(cols.values());
            args.add(instanceId);
            args.add(tenantId);
            jdbc.update("UPDATE " + table + " SET " + sets + " WHERE " + keyCol + "=? AND tenant_id=?",
                    args.toArray());
        }
    }

    /** First non-blank value for any of {@code keys}, top-level or one level into nested maps. */
    @SuppressWarnings("unchecked")
    private String pluck(Map<String, Object> bag, List<String> keys) {
        if (bag == null) return null;
        for (String k : keys) {
            Object v = bag.get(k);
            if (v != null && !v.toString().isBlank()) return v.toString();
        }
        for (Object v : bag.values()) {
            if (v instanceof Map<?, ?> m) {
                for (String k : keys) {
                    Object x = ((Map<String, Object>) m).get(k);
                    if (x != null && !x.toString().isBlank()) return x.toString();
                }
            }
        }
        return null;
    }

    private static String ident(String s) {
        if (s == null || !SAFE_IDENT.matcher(s).matches())
            throw new IllegalArgumentException("unsafe identifier in projection config: " + s);
        return s;
    }

    private static String orDefault(String s, String def) { return (s == null || s.isBlank()) ? def : s; }

    private static List<String> splitCsv(String s) {
        if (s == null || s.isBlank()) return List.of();
        List<String> out = new ArrayList<>();
        for (String p : s.split(",")) if (!p.trim().isEmpty()) out.add(p.trim());
        return out;
    }
}
