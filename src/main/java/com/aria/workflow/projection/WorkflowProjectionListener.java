package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
 *
 * <p>{@code @Order(HIGHEST_PRECEDENCE + 1)} (right after {@link
 * GatePassDispatchLinkListener}'s HIGHEST_PRECEDENCE) so this runs BEFORE
 * {@link DispatchToleranceListener} in WorkflowEngineService's
 * {@code List<WorkflowTransitionListener>} autowiring. DispatchToleranceListener
 * reads w1_weight_kg/w2_weight_kg back off the very row this listener writes
 * (weighment_capture_detail) on the SAME transition — with no explicit order
 * between the two, Spring's default (unordered, effectively component-scan
 * order) let the tolerance calc run first and compute expected/loaded weight
 * off the PREVIOUS transition's stale reading.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
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
        // Resolved lazily (only if some config actually has a page_code to
        // check against, OR maps queue_reason — see below) since it's an
        // extra query per transition.
        String triggeringPageCode = null;
        boolean triggeringPageCodeResolved = false;
        for (Map<String, Object> cfg : configs) {
            String pageCodeScope = (String) cfg.get("page_code");
            String fieldMapJsonForCfg = (String) cfg.get("field_map_json");
            boolean needsTriggeringPage = (pageCodeScope != null && !pageCodeScope.isBlank())
                    || mapsQueueReason(fieldMapJsonForCfg)
                    || mapsTimeTaken(fieldMapJsonForCfg);
            if (needsTriggeringPage && !triggeringPageCodeResolved) {
                triggeringPageCode = resolveTriggeringPageCode(instance, tenantId, previousState);
                triggeringPageCodeResolved = true;
            }
            if (pageCodeScope != null && !pageCodeScope.isBlank()) {
                // NULL/blank page_code means "always applies" (every workflow's
                // existing rows, unchanged); a non-blank one restricts this
                // target_table to the page(s) that legitimately feed it — see
                // V214. If the triggering page couldn't be resolved, fail open
                // (project anyway) rather than silently stop writing a table
                // that used to always get synced.
                if (triggeringPageCode != null && !splitCsv(pageCodeScope).contains(triggeringPageCode)) {
                    continue;
                }
            }
            try {
                project(cfg, instance, tenantId, previousState, submittedDataBag, triggeringPageCode);
            } catch (Exception ex) {
                log.warn("projection failed for workflow={} instance={}: {}",
                        code, instance.getId(), ex.getMessage());
            }
        }
    }

    /** Cheap pre-check so resolveTriggeringPageCode's extra query only runs when actually needed. */
    private static boolean mapsQueueReason(String fieldMapJson) {
        return fieldMapJson != null && fieldMapJson.contains("queue_reason");
    }

    /** Same purpose as {@link #mapsQueueReason} for the time_taken (V265) page-scoped field. */
    private static boolean mapsTimeTaken(String fieldMapJson) {
        return fieldMapJson != null && fieldMapJson.contains("time_taken");
    }

    /**
     * Which page's submission actually drove this transition, resolved by
     * matching (previousState -&gt; instance.getCurrentState()) against the
     * workflow_definition's own transitions[] (each transition already
     * declares the pageCode that triggers it — see WorkflowEngineService).
     * Self-contained rather than widening {@link WorkflowTransitionListener}
     * to carry pageCode itself, which would touch every implementing
     * listener for a value only this one currently needs.
     */
    @SuppressWarnings("unchecked")
    private String resolveTriggeringPageCode(WorkflowInstance instance, Long tenantId, String previousState) {
        try {
            Long ownerTenantId = instance.getWorkflowOwnerTenantId() != null
                    ? instance.getWorkflowOwnerTenantId() : instance.getTenantId();
            String bodyJson = jdbc.queryForObject(
                    "SELECT body_json FROM workflow_definition WHERE tenant_id=? AND code=? AND version=?",
                    String.class, ownerTenantId, instance.getWorkflowCode(), instance.getWorkflowVersion());
            Map<String, Object> body = MAPPER.readValue(bodyJson, Map.class);
            List<Map<String, Object>> transitions = (List<Map<String, Object>>) body.get("transitions");
            if (transitions == null) return null;
            String currentState = instance.getCurrentState();
            for (Map<String, Object> t : transitions) {
                if (Objects.equals(previousState, t.get("from")) && Objects.equals(currentState, t.get("to"))) {
                    Object pc = t.get("pageCode");
                    if (pc != null) return pc.toString();
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void project(Map<String, Object> cfg, WorkflowInstance instance,
                         Long tenantId, String previousState, Map<String, Object> bag,
                         String triggeringPageCode) throws Exception {
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
            boolean isBillingEntry = "billing_entry".equals(table);
            boolean isDispatchVehicleDetail = "dispatch_vehicle_detail".equals(table);
            boolean isWeighmentCaptureDetail = "weighment_capture_detail".equals(table);
            boolean isQcFinalOutboundDetail = "qc_final_outbound_detail".equals(table);
            for (Map.Entry<String, Object> e : map.entrySet()) {
                // variance_reason is a name shared by six PMT/manager-approval pages;
                // PmtSharedReasonListener already routes five of them (including
                // weight-diff-entry, M10.TankerOutbound's own WEIGHMENT_2 exit page)
                // into their own satellite table, so gate_pass no longer needs (or
                // should keep) a copy for those — it would just be last-write-wins
                // noise. reconciliation isn't covered yet, so this only skips the
                // transitions PmtSharedReasonListener actually owns.
                if (isGatePass && "variance_reason".equals(e.getKey())
                        && PmtSharedReasonListener.ownsVarianceReason(instance.getWorkflowCode(), previousState)) {
                    continue;
                }
                // invoice_numbers/invoice_no on billing_entry: Billing Entry now
                // has its own dedicated satellite table (V103/V104), so the
                // cross-page ambiguity that used to require special-casing
                // gate_pass.invoice_no (dispatch-vehicle-details' vendor invoice
                // vs. billing-entry's sales invoice sharing one gate_pass column)
                // is gone — gate_pass's field_map_json no longer maps invoice_no
                // at all (V104), and dispatch_vehicle_detail's own invoice_no
                // mapping was never part of this branch. What's left to special-
                // case is just serialisation: aria:billing-entry submits a LIST of
                // invoice numbers (a repeater field), not a scalar, so it can't go
                // through pluck()'s generic toString() path (that would produce
                // Java's `[a, b]` list syntax, not valid JSON) — and
                // invoice_number_count is a derived column, not itself present in
                // the submitted bag, so it's computed here rather than mapped.
                if (isBillingEntry && "invoice_numbers".equals(e.getKey())) {
                    List<String> keys = (List<String>) e.getValue();
                    List<String> numbers = extractInvoiceNumbers(bag, keys);
                    if (!numbers.isEmpty()) {
                        cols.put(ident("invoice_numbers"), MAPPER.writeValueAsString(numbers));
                        cols.put(ident("invoice_number_count"), numbers.size());
                    }
                    continue;
                }
                // queue_reason (V232/V233) is deliberately reused as the same
                // literal field name across every queue-enabled page, so
                // pluck()'s generic cross-page fallback is exactly wrong here:
                // it'd happily return an OLDER submission's reason from a
                // DIFFERENT page still sitting in the bag instead of the one
                // that's actually submitting right now (confirmed live —
                // gate_pass.queue_reason picked up a stale value from another
                // page's earlier entry). Scoped strictly to the triggering
                // page's own bag entry instead; no triggeringPageCode
                // resolved (e.g. an unrecognised transition) means skip
                // rather than risk writing the wrong page's value.
                if ("queue_reason".equals(e.getKey())) {
                    List<String> keys = (List<String>) e.getValue();
                    String v = pluckScoped(bag, keys, triggeringPageCode);
                    if (v != null) cols.put(ident("queue_reason"), v);
                    continue;
                }
                // qc_decision (V249): "status"/"qc_status" is submitted by
                // aria:qc-preload-approval's Approve/Reject buttons, but that
                // same "status" key is reused by unrelated pages too -- same
                // cross-page-collision risk as queue_reason above, so this
                // also needs the page-scoped lookup rather than the generic
                // cross-page fallback.
                if ("qc_decision".equals(e.getKey())) {
                    List<String> keys = (List<String>) e.getValue();
                    String v = pluckScoped(bag, keys, triggeringPageCode);
                    if (v != null) cols.put(ident("qc_decision"), v);
                    continue;
                }
                // time_taken (V265, column type fixed in V270): the UI submits an
                // HH:MM:SS elapsed-duration string (e.g. "00:01:12") under this
                // exact literal name on EVERY page that projects through this
                // listener -- same cross-page-collision risk as queue_reason/
                // qc_decision above, so this needs the page-scoped lookup too,
                // not the generic cross-page fallback. Saved verbatim (column is
                // TIME, which stores/round-trips "HH:MM:SS" as given) rather than
                // reinterpreted into a decimal minute count -- V265's original
                // new BigDecimal(v) parse threw on this exact string and silently
                // dropped the column.
                if ("time_taken".equals(e.getKey())) {
                    List<String> keys = (List<String>) e.getValue();
                    String v = pluckScoped(bag, keys, triggeringPageCode);
                    if (v != null) cols.put(ident("time_taken"), v);
                    continue;
                }
                // oil_rm_item on dispatch_vehicle_detail (V237): aria:dispatch-
                // vehicle-details submits oilRmItem as a repeater/array field,
                // same shape as billing_entry's invoice_numbers above — pluck()'s
                // scalarOf() would silently drop every item but the first, so
                // this keeps the whole list and JSON-serialises it instead.
                if (isDispatchVehicleDetail && "oil_rm_item".equals(e.getKey())) {
                    List<String> keys = (List<String>) e.getValue();
                    List<String> items = pluckList(bag, keys);
                    if (!items.isEmpty()) cols.put(ident("oil_rm_item"), MAPPER.writeValueAsString(items));
                    continue;
                }
                // po_numbers/po_attachment/party_name on dispatch_vehicle_detail
                // (V246): same repeater/array shape as oil_rm_item above — a
                // vehicle can carry multiple PO numbers, multiple PO scan
                // attachments, and multiple party names.
                if (isDispatchVehicleDetail
                        && ("po_numbers".equals(e.getKey()) || "po_attachment".equals(e.getKey())
                            || "party_name".equals(e.getKey()))) {
                    List<String> keys = (List<String>) e.getValue();
                    List<String> items = pluckList(bag, keys);
                    if (!items.isEmpty()) cols.put(ident(e.getKey()), MAPPER.writeValueAsString(items));
                    continue;
                }
                // w1_weight_kg/w2_weight_kg/w3_weight_kg on weighment_capture_detail
                // (V81): same cross-page-collision risk as queue_reason/qc_decision
                // above -- aria:pmt-weight-diff-check, aria:pmt-camera-verification,
                // aria:manager-weight-approval and aria:gate-pass-closure-form all
                // echo these same field names back in their OWN submitted formData
                // (onChangeFill display/reference bindings, see V81's class doc), so
                // pluck()'s generic cross-page fallback can resolve to one of THOSE
                // pages' stale reading instead of this transition's actual
                // aria:weighment-capture submission -- confirmed live: a reload's
                // fresh w2 got silently dropped, leaving the PMT step's older w2 in
                // place. Scoped strictly to the triggering page's own bag entry.
                if (isWeighmentCaptureDetail
                        && ("w1_weight_kg".equals(e.getKey()) || "w2_weight_kg".equals(e.getKey())
                            || "w3_weight_kg".equals(e.getKey()))) {
                    List<String> keys = (List<String>) e.getValue();
                    String v = pluckScoped(bag, keys, triggeringPageCode);
                    if (v != null) cols.put(ident(e.getKey()), v);
                    continue;
                }
                // wb_display_photo/vehicle_photo on weighment_capture_detail are
                // submitted under the same field name for all 3 weighments --
                // aria:weighment-capture's Photo Evidence section is shared
                // across weight_seq FIRST/SECOND/THIRD (only w1_/w2_/w3_weight_kg
                // are split per-reading). The generic fallback below would target
                // a literal "wb_display_photo"/"vehicle_photo" column, which
                // doesn't exist -- each reading has its own w1_/w2_/w3_ column
                // (V244) -- so route using this same submission's weight_seq.
                if (isWeighmentCaptureDetail
                        && ("wb_display_photo".equals(e.getKey()) || "vehicle_photo".equals(e.getKey()))) {
                    String seq = pluck(bag, List.of("weight_seq"));
                    String prefix = "FIRST".equals(seq) ? "w1_"
                            : "SECOND".equals(seq) ? "w2_"
                            : "THIRD".equals(seq) ? "w3_" : null;
                    if (prefix != null) {
                        List<String> keys = (List<String>) e.getValue();
                        String v = pluck(bag, keys);
                        if (v != null) cols.put(ident(prefix + e.getKey()), v);
                    }
                    continue;
                }
                // weight_entry_at on weighment_capture_detail (V289): same
                // weight_seq-prefixed routing as wb_display_photo/vehicle_photo
                // above, but server-stamped rather than plucked -- none of the
                // pages that submit through this key (aria:weighment-capture,
                // aria:weighment-capture-other-vehicle) actually carry a
                // capture-timestamp value worth trusting (aria:weighment-capture
                // has no such field at all; the other-vehicle page's own
                // w1_captured_at/w2_captured_at were just a client "now()"
                // default, subject to clock skew). w1_/w2_/w3_captured_at record
                // when THIS SERVER actually persisted each reading.
                if (isWeighmentCaptureDetail && "weight_entry_at".equals(e.getKey())) {
                    String seq = pluck(bag, List.of("weight_seq"));
                    String prefix = "FIRST".equals(seq) ? "w1_"
                            : "SECOND".equals(seq) ? "w2_"
                            : "THIRD".equals(seq) ? "w3_" : null;
                    if (prefix != null) {
                        cols.put(ident(prefix + "captured_at"), nowIst());
                    }
                    continue;
                }
                // billing_documents on billing_entry (V356): several uploaded files per
                // billing capture. Unlike the attachment columns above (which flatten each
                // file object to its Map.toString()), the file objects are kept as real
                // JSON objects here -- [{"url":..,"doc_no":..,"sha256":..,"size_kb":..}, ...].
                // qc_final_files on qc_officer_final_check_detail (V357) takes the same path.
                if (("billing_entry".equals(table) && "billing_documents".equals(e.getKey()))
                        || ("qc_officer_final_check_detail".equals(table) && "qc_final_files".equals(e.getKey()))) {
                    Object raw = pluckRaw(bag, (List<String>) e.getValue());
                    if (raw instanceof List<?> l && !l.isEmpty()) {
                        cols.put(ident(e.getKey()), MAPPER.writeValueAsString(l));
                    } else if (raw != null && !raw.toString().isBlank()) {
                        cols.put(ident(e.getKey()), MAPPER.writeValueAsString(List.of(raw)));
                    }
                    continue;
                }
                // qc_sample_test on qc_final_outbound_detail (V250): same
                // repeater/array shape as po_attachment above -- a sample
                // sign-off can carry more than one attached file.
                if (isQcFinalOutboundDetail && "qc_sample_test".equals(e.getKey())) {
                    List<String> keys = (List<String>) e.getValue();
                    List<String> items = pluckList(bag, keys);
                    if (!items.isEmpty()) cols.put(ident(e.getKey()), MAPPER.writeValueAsString(items));
                    continue;
                }
                List<String> keys = (List<String>) e.getValue();
                // A page-scoped table (page_code set) is fed by that page: read its OWN submission first. The
                // generic pluck() walks every page's data in HashMap order and takes the first hit, so a field name
                // shared by several pages ("status", "qc_notes"...) could come back from ANOTHER page -- e.g.
                // qc_final_outbound_detail.status saved as APPROVED (aria:qc-preload-approval's value) although the
                // QC had just submitted REJECTED on aria:qc-final-out. Falls back to the cross-page lookup only when
                // the triggering page itself did not send the field.
                String scope = (String) cfg.get("page_code");
                boolean pageScoped = scope != null && !scope.isBlank() && triggeringPageCode != null;
                String v = pageScoped ? pluckScoped(bag, keys, triggeringPageCode) : null;
                if (v == null) v = pluck(bag, keys);
                if (v != null) cols.put(ident(e.getKey()), v);
            }
        }

        // Terminal timestamp.
        String terminalTsCol = (String) cfg.get("terminal_ts_column");
        if (terminalTsCol != null && splitCsv((String) cfg.get("terminal_states")).contains(state)) {
            cols.put(ident(terminalTsCol), nowIst());
        }

        // Per-state timestamps: unlike terminal_ts_column (one column, one
        // condition), this maps several distinct states to their own distinct
        // columns -- e.g. {"LOADING_IN_PROGRESS":"loading_start_time",
        // "LOADING_COMPLETED":"loading_finish_time"} stamps a different
        // column depending on which state was just entered.
        String stateTsColumnsJson = (String) cfg.get("state_ts_columns_json");
        if (stateTsColumnsJson != null && !stateTsColumnsJson.isBlank()) {
            Map<String, String> stateTsColumns = MAPPER.readValue(stateTsColumnsJson, Map.class);
            String col = stateTsColumns.get(state);
            if (col != null) cols.put(ident(col), nowIst());
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
            if (cols.isEmpty()) return; // nothing this transition's bag maps to this table yet
            String sets = String.join(", ", cols.keySet().stream().map(c -> c + "=?").toList());
            List<Object> args = new ArrayList<>(cols.values());
            args.add(instanceId);
            args.add(tenantId);
            jdbc.update("UPDATE " + table + " SET " + sets + " WHERE " + keyCol + "=? AND tenant_id=?",
                    args.toArray());
        }
    }

    /**
     * All non-blank invoice numbers billing-entry submitted, tried in order
     * of {@code keys}. Looked up directly off the aria:billing-entry page's
     * own bag entry (not pluck()'s generic cross-page fallback) — same
     * reasoning as PmtSharedReasonListener's variance_reason javadoc: a plain
     * HashMap of every page's submitted data isn't insertion-ordered, so a
     * cross-page search can return a different page's stale value for a
     * same-named key. Accepts the new repeater shape (a List of strings); a
     * scalar submission (the Renderer doesn't have a repeater widget yet, so
     * today's page falls back to one plain text box) is split on comma/
     * semicolon so a user typing "INV001,INV002,INV003" into that single box
     * still produces 3 invoice numbers, not one string containing commas.
     */
    @SuppressWarnings("unchecked")
    private List<String> extractInvoiceNumbers(Map<String, Object> bag, List<String> keys) {
        if (bag == null) return List.of();
        Object page = bag.get("aria:billing-entry");
        if (!(page instanceof Map<?, ?> m)) return List.of();
        for (String k : keys) {
            Object v = ((Map<String, Object>) m).get(k);
            if (v instanceof List<?> list) {
                List<String> out = new ArrayList<>();
                for (Object item : list) {
                    if (item != null && !item.toString().isBlank()) out.add(item.toString().trim());
                }
                if (!out.isEmpty()) return out;
            } else if (v != null && !v.toString().isBlank()) {
                List<String> out = new ArrayList<>();
                for (String part : v.toString().split("[,;]")) {
                    String t = part.trim();
                    if (!t.isEmpty()) out.add(t);
                }
                if (!out.isEmpty()) return out;
            }
        }
        return List.of();
    }

    /** First non-blank value for any of {@code keys}, top-level or one level into nested maps. */
    @SuppressWarnings("unchecked")
    private String pluck(Map<String, Object> bag, List<String> keys) {
        if (bag == null) return null;
        for (String k : keys) {
            String v = scalarOf(bag.get(k));
            if (v != null) return v;
        }
        for (Object v : bag.values()) {
            if (v instanceof Map<?, ?> m) {
                for (String k : keys) {
                    String x = scalarOf(((Map<String, Object>) m).get(k));
                    if (x != null) return x;
                }
            }
        }
        return null;
    }

    /**
     * Same lookup order as {@link #pluck}, but for a repeater/array field:
     * preserves every item instead of collapsing to the first one the way
     * {@link #scalarOf} does. Falls back to comma/semicolon-splitting a plain
     * string, since the Renderer's "repeater" widget kind isn't implemented
     * yet (V237) and today's submission may still be free-typed text.
     */
    @SuppressWarnings("unchecked")
    private List<String> pluckList(Map<String, Object> bag, List<String> keys) {
        if (bag == null) return List.of();
        for (String k : keys) {
            List<String> v = listOf(bag.get(k));
            if (v != null) return v;
        }
        for (Object v : bag.values()) {
            if (v instanceof Map<?, ?> m) {
                for (String k : keys) {
                    List<String> x = listOf(((Map<String, Object>) m).get(k));
                    if (x != null) return x;
                }
            }
        }
        return List.of();
    }

    /** First non-null value for any of {@code keys}, top level of the bag first, then inside each page's own map -- values returned as-is (no stringifying). */
    @SuppressWarnings("unchecked")
    private Object pluckRaw(Map<String, Object> bag, List<String> keys) {
        if (bag == null) return null;
        for (String k : keys) if (bag.get(k) != null) return bag.get(k);
        for (Object v : bag.values()) {
            if (v instanceof Map<?, ?> m) {
                for (String k : keys) if (((Map<String, Object>) m).get(k) != null) return ((Map<String, Object>) m).get(k);
            }
        }
        return null;
    }


    private List<String> listOf(Object v) {
        if (v == null) return null;
        List<String> out = new ArrayList<>();
        if (v instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !item.toString().isBlank()) out.add(item.toString().trim());
            }
        } else {
            String s = v.toString();
            if (s.isBlank()) return null;
            for (String part : s.split("[,;]")) {
                String t = part.trim();
                if (!t.isEmpty()) out.add(t);
            }
        }
        return out.isEmpty() ? null : out;
    }

    /**
     * Same as {@link #pluck}, but looked up ONLY from {@code pageCode}'s own
     * bag entry — no cross-page fallback. Use for field names deliberately
     * reused across multiple pages (e.g. queue_reason) where the generic
     * fallback would risk returning a different page's value.
     */
    @SuppressWarnings("unchecked")
    /**
     * Server-stamped "now", rendered as IST wall-clock digits regardless of
     * the JVM host's own default timezone. {@code Timestamp.from(Instant.now())}
     * (this class's previous approach, matching {@code DateUtil}'s absence of
     * IST-awareness in a few spots) round-trips through JDBC's
     * setTimestamp(no-Calendar), which renders the instant using the JVM's
     * default zone before sending it to MariaDB -- confirmed live: on a host
     * whose JVM default zone is UTC while the DB session zone is IST, this
     * silently stored "09:53:18" for a reading actually captured at 15:23:18
     * IST (row's own created_at/updated_at, both DB-computed CURRENT_TIMESTAMP,
     * showed the correct 15:23:18 side by side). Deliberately mirrors this
     * codebase's established IST-explicit convention ({@link
     * com.aria.utils.DateUtil#getNowLocalDateTime()}) rather than inventing a
     * new one: compute the IST wall-clock fields explicitly, then hand JDBC a
     * Timestamp whose own default-zone rendering reproduces those exact digits.
     */
    private static Timestamp nowIst() {
        return Timestamp.valueOf(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));
    }

    private String pluckScoped(Map<String, Object> bag, List<String> keys, String pageCode) {
        if (bag == null || pageCode == null) return null;
        Object v = bag.get(pageCode);
        if (!(v instanceof Map<?, ?> m)) return null;
        for (String k : keys) {
            String x = scalarOf(((Map<String, Object>) m).get(k));
            if (x != null) return x;
        }
        return null;
    }

    /**
     * Coerces a submitted field value to the single scalar this projection
     * writes into a plain VARCHAR column. Multiselect widgets (e.g. the
     * route_id picker on aria:loading-in-progress-entry, V74) submit a JSON
     * array even though the target column only ever holds one value --
     * without this, {@code List.toString()} was landing literally as
     * "[ROU027834]", brackets included, in the projected column.
     */
    private String scalarOf(Object v) {
        if (v == null) return null;
        if (v instanceof List<?> list) {
            for (Object item : list) {
                if (item != null && !item.toString().isBlank()) return item.toString();
            }
            return null;
        }
        String s = v.toString();
        return s.isBlank() ? null : s;
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
