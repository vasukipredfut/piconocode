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
            // Config resolves like the rest of the substrate: a product-default row
            // at tenant_id=-1 applies to every tenant; a tenant-specific row (same
            // target_table) overrides it. Without the -1 fallback, any tenant that
            // wasn't hand-seeded projected nothing → its dispatch list came back empty.
            List<Map<String, Object>> raw = jdbc.queryForList(
                    "SELECT * FROM m0_workflow_projection WHERE workflow_code=? AND tenant_id IN (?, -1) "
                            + "ORDER BY tenant_id DESC", code, tenantId);
            LinkedHashMap<String, Map<String, Object>> byTable = new LinkedHashMap<>();
            for (Map<String, Object> c : raw) byTable.putIfAbsent(String.valueOf(c.get("target_table")), c);
            configs = new ArrayList<>(byTable.values());
        } catch (Exception e) {
            return; // table not applied yet — nothing to project
        }
        for (Map<String, Object> cfg : configs) {
            try {
                project(cfg, instance, tenantId, previousState, submittedDataBag);
            } catch (Exception ex) {
                log.warn("projection failed for workflow={} instance={}: {}",
                        code, instance.getId(), ex.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void project(Map<String, Object> cfg, WorkflowInstance instance,
                         Long tenantId, String previousState, Map<String, Object> bag) throws Exception {
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
            boolean isGatePass = "gate_pass".equals(table);
            for (Map.Entry<String, Object> e : map.entrySet()) {
                // variance_reason is a name shared by six PMT/manager-approval pages;
                // PmtSharedReasonListener already routes four of them into their own
                // satellite table, so gate_pass no longer needs (or should keep) a
                // copy for those four — it would just be last-write-wins noise.
                // weight-diff-entry / reconciliation aren't covered yet, so this only
                // skips the four transitions PmtSharedReasonListener actually owns.
                if (isGatePass && "variance_reason".equals(e.getKey())
                        && PmtSharedReasonListener.ownsVarianceReason(instance.getWorkflowCode(), previousState)) {
                    continue;
                }
                // invoice_no is shared by aria:dispatch-vehicle-details (vendor
                // invoice, already captured in dispatch_vehicle_detail) and
                // aria:billing-entry (sales invoice, no dedicated table yet —
                // gate_pass is still its only save location).
                if (isGatePass && "invoice_no".equals(e.getKey())) {
                    // dispatch-vehicle-details' own transition: don't write gate_pass
                    // at all (its value already lives in dispatch_vehicle_detail).
                    if ("PARKING_IN_1".equals(previousState)) continue;
                    // billing-entry's own transition (the only pageCode firing from
                    // PMT_CHECK_OK): look up its bag entry directly by pageCode
                    // instead of pluck()'s generic cross-page fallback, which
                    // searches EVERY page's submitted data for a matching key and
                    // returns whichever nested map it hits first — a plain HashMap,
                    // so NOT insertion-order, and confirmed (2026-07-23 smoke test)
                    // to return dispatch-vehicle-details' older vendor invoice_no
                    // instead of billing-entry's own fresh value. Same cross-page
                    // ambiguity PmtSharedReasonListener's javadoc warns about for
                    // variance_reason/status — fixed here the same way: look up the
                    // one page we know just fired, not every page that ever fired.
                    if ("PMT_CHECK_OK".equals(previousState)) {
                        Object page = bag.get("aria:billing-entry");
                        if (page instanceof Map<?, ?> m) {
                            Object v = m.get("invoice_no");
                            if (v != null && !v.toString().isBlank()) cols.put(ident("invoice_no"), v.toString());
                        }
                        continue;
                    }
                }
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
