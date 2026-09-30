package com.aria.integration.conveyor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.security.TenantContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;

/**
 * Endpoints invoked by the LAN-side edge agent ({@code edge/conveyor-agent/}).
 *
 * <p>The agent owns the chatty PLC traffic; this controller owns the business
 * decisions:
 *   * Is the scanned barcode a known SKU?
 *   * Has the dispatch order quantity been reached?
 *   * Is the line halted by manager command?
 * Each box event gets one of {ACCEPT, REJECT_DIVERT, HALT_LINE} back so the
 * agent can pulse the right coil immediately.
 *
 * <p>Endpoints:
 * <pre>
 *   POST /api/m11/conveyor/box-event               agent → us (per box)
 *   GET  /api/m11/conveyor/commands?lineId=X       agent ← us (poll for halt/resume/speed)
 *   POST /api/m11/conveyor/commands/{id}/ack       agent → us (command done)
 *   POST /api/m11/agents/heartbeat                 agent → us (every 30 s)
 * </pre>
 */
@RestController
@RequestMapping
public class ConveyorAgentController {

    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public ConveyorAgentController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    // ------------------------------------------------------------------
    @PostMapping("/api/m11/conveyor/box-event")
    public ResponseEntity<Map<String, Object>> boxEvent(@RequestBody Map<String, Object> body) {
        Long tenantId = TenantContext.get();
        String lineId = str(body.get("lineId"));
        String barcode = str(body.get("barcode"));
        Double weight = body.get("weightKg") == null ? null
                : Double.valueOf(String.valueOf(body.get("weightKg")));
        String bay = str(body.get("bayCode"));
        Long packingRunId = body.get("packingRunId") == null ? null
                : Long.valueOf(String.valueOf(body.get("packingRunId")));

        if (barcode == null || barcode.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("action", "REJECT_DIVERT",
                    "reason", "no barcode"));
        }

        // ----- 1. Lookup SKU by barcode (may match multiple offer-variants) -
        // Match on EITHER barcode OR sku_code so SEPARATE-mode lines (whose
        // QR scanners emit the SKU itself, not the barcode) work alongside
        // COMBO-mode lines (whose checkweigher emits the raw 13-digit barcode).
        List<Map<String, Object>> matches;
        try {
            matches = jdbc.queryForList(
                "SELECT sku_code, offer_variant, carton_weight_kg, status " +
                "FROM m5_sku_master WHERE tenant_id=? AND (barcode=? OR sku_code=?)",
                tenantId, barcode, barcode);
        } catch (Exception e) { matches = List.of(); }

        String action; String reason; String matchedSku = null; String matchedOffer = null;
        if (matches.isEmpty()) {
            action = "REJECT_DIVERT"; reason = "UNAUTHORIZED_BARCODE";
        } else {
            Map<String, Object> chosen;
            if (matches.size() == 1) {
                chosen = matches.get(0);
            } else {
                // Same barcode, several offer-variant rows (e.g. a festive
                // combo pack reusing a standard pack's barcode) -- unlike the
                // manual/handheld-scanner path (M5PackScanActionHandler),
                // there is no operator here to ask mid-stream, so this MUST
                // be resolved from the line's own pre-set expectation
                // (m11_agent_acceptance_scope.expected_offer, set once when
                // the run starts) rather than silently guessing. Previously
                // this just took matches.get(0) regardless -- risked packing
                // the wrong offer variant onto every box for that barcode
                // whenever more than one was on file.
                String expectedOffer = null;
                try {
                    expectedOffer = jdbc.queryForObject(
                        "SELECT expected_offer FROM m11_agent_acceptance_scope " +
                        "WHERE tenant_id=? AND active=1 AND expected_offer IS NOT NULL " +
                        "  AND agent_id IN (SELECT agent_id FROM m11_agent WHERE tenant_id=? AND line_id=?) " +
                        "LIMIT 1",
                        String.class, tenantId, tenantId, lineId);
                } catch (Exception ignored) { /* no scope row / no line match */ }

                final String expected = expectedOffer;
                chosen = expected == null ? null : matches.stream()
                        .filter(m -> expected.equals(m.get("offer_variant")))
                        .findFirst().orElse(null);
            }
            if (chosen == null) {
                action = "REJECT_DIVERT"; reason = "AMBIGUOUS_OFFER_NOT_CONFIGURED";
            } else if (!"ACTIVE".equalsIgnoreCase(String.valueOf(chosen.get("status")))) {
                action = "REJECT_DIVERT"; reason = "SKU_BLOCKED_OR_DISCONTINUED";
            } else {
                matchedSku = String.valueOf(chosen.get("sku_code"));
                matchedOffer = String.valueOf(chosen.get("offer_variant"));
                // ----- 2. Weight tolerance check -----------------------
                Double expectedCarton = chosen.get("carton_weight_kg") == null ? null
                        : ((Number) chosen.get("carton_weight_kg")).doubleValue();
                if (weight != null && expectedCarton != null) {
                    double diffPct = Math.abs(weight - expectedCarton) / expectedCarton;
                    if (diffPct > 0.10) {
                        action = "REJECT_DIVERT"; reason = "WEIGHT_OUT_OF_SPEC_" +
                                String.format("%.1f%%", diffPct * 100);
                    } else {
                        action = "ACCEPT"; reason = "ok";
                    }
                } else {
                    action = "ACCEPT"; reason = "ok";
                }
            }
        }

        // ----- 3. Dispatch-qty guard — if the SO this run is fulfilling
        //         has been hit, halt the line entirely.
        if ("ACCEPT".equals(action) && packingRunId != null) {
            try {
                Integer scansSoFar = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM m5_packing_scan_log " +
                    "WHERE packing_run_id=? AND outcome='ACCEPTED'",
                    Integer.class, packingRunId);
                Integer plannedTotal = jdbc.queryForObject(
                    "SELECT planned_total_pieces FROM m5_packing_run WHERE id=?",
                    Integer.class, packingRunId);
                if (plannedTotal != null && scansSoFar != null && scansSoFar + 1 >= plannedTotal) {
                    action = "HALT_LINE"; reason = "DISPATCH_QTY_REACHED";
                }
            } catch (Exception ignored) {}
        }

        // ----- 4. Update acceptance scope counter so the next config-poll
        //         from the agent reflects the new running count.
        if ("ACCEPT".equals(action)) {
            try {
                jdbc.update(
                    "UPDATE m11_agent_acceptance_scope " +
                    "SET current_pieces = current_pieces + 1 " +
                    "WHERE tenant_id=? AND active=1 " +
                    "  AND (expected_sku IS NULL OR expected_sku=?) " +
                    "  AND (expected_barcode IS NULL OR expected_barcode=?) " +
                    "  AND agent_id IN (SELECT agent_id FROM m11_agent WHERE tenant_id=? AND line_id=?)",
                    tenantId, matchedSku, barcode, tenantId, lineId);
            } catch (Exception ignored) {}
        }

        // ----- 5. Persist the scan log row ------------------------------
        try {
            jdbc.update(
                "INSERT INTO m5_packing_scan_log " +
                "(tenant_id, packing_run_id, bay_code, scanned_barcode, sku_code, offer_variant, " +
                " bundle_count, pieces_total, outcome, reject_reason, scanned_at) " +
                "VALUES (?,?,?,?,?,?, NULL, NULL, ?, ?, CURRENT_TIMESTAMP)",
                tenantId, packingRunId, bay, barcode, matchedSku, matchedOffer,
                action.equals("ACCEPT") ? "ACCEPTED" : "REJECTED",
                action.equals("ACCEPT") ? null : reason);
        } catch (Exception ignored) { /* log table missing on fresh DB; non-fatal */ }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("action", action);
        out.put("reason", reason);
        if (matchedSku != null) out.put("matchedSku", matchedSku);
        if (matchedOffer != null) out.put("matchedOffer", matchedOffer);

        // ----- 4. Print payload on ACCEPT (vvscwprint-shape) -------------
        // Build BUFFERDATA fields the agent forwards to the line printer.
        // Customers who already designed a printer template can override
        // with {raw: "<frame>"} by extending this block.
        if ("ACCEPT".equals(action) && matchedSku != null) {
            try {
                Map<String, Object> sku = jdbc.queryForMap(
                    "SELECT sku_code, brand, product_family, pack_size_ml, mrp_inr, hsn_code " +
                    "FROM m5_sku_master WHERE tenant_id=? AND sku_code=?",
                    tenantId, matchedSku);
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate bestBefore = today.plusMonths(12);   // FSSAI default
                String batchNo = "B" + today.toString().replace("-","") + "-" + bay;
                String shiftNo = computeShiftNo(today);
                // vvscwprint fields: [unitMrp, totalMrp, BatchNo., Use By,
                //   Date of Packaging, Gross Weight, QR-text]
                String unitMrp = String.format("%.2f", num(sku.get("mrp_inr")));
                String grossKgLabel = String.format("%.3fKg", weight == null ? 0.0 : weight);
                String qrText = "(Batch No:) " + batchNo +
                                " (Shift No:) " + shiftNo +
                                " (Date of Packing:) " + today +
                                " (Use By) " + bestBefore +
                                " (Gross Weight:) " + grossKgLabel +
                                " (Unit MRP) " + unitMrp +
                                " (SKU) " + matchedSku +
                                " (Brand) " + String.valueOf(sku.get("brand")) +
                                " (Product) " + String.valueOf(sku.get("product_family")) +
                                " (HSN) " + String.valueOf(sku.get("hsn_code")) +
                                " (website:) www.aria.com";
                List<String> fields = List.of(
                    unitMrp,
                    String.format("%.2f", 0.0),     // totalMrp — caller can re-compute
                    "Batchno." + batchNo,
                    "USE BY." + bestBefore,
                    "Date of Packaging : " + today,
                    "Gross Weight : " + grossKgLabel);
                out.put("printPayload", Map.of(
                    "fields", fields,
                    "qrCode", qrText));
            } catch (Exception ignored) {
                // SKU lookup failed (concurrent delete?) — skip the print payload;
                // ACCEPT still stands so the conveyor keeps moving.
            }
        }
        return ResponseEntity.ok(out);
    }

    private static double num(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(o.toString()); }
        catch (Exception e) { return 0.0; }
    }

    private static String computeShiftNo(java.time.LocalDate d) {
        // vvscwprint: 3 shifts/day, (3*(day-1))+factor where factor depends
        // on hour-of-day. Same formula the plant's existing label printers use.
        int hour = java.time.LocalTime.now().getHour();
        int factor = (hour >= 5 && hour < 14) ? 1
                    : (hour >= 14 && hour < 22) ? 2 : 3;
        return String.valueOf(3 * (d.getDayOfMonth() - 1) + factor);
    }

    // ------------------------------------------------------------------
    @GetMapping("/api/m11/conveyor/commands")
    public ResponseEntity<Map<String, Object>> pendingCommands(@RequestParam String lineId) {
        Long tenantId = TenantContext.get();
        List<Map<String, Object>> cmds;
        try {
            cmds = jdbc.queryForList(
                "SELECT id, kind, rpm FROM m11_conveyor_command " +
                "WHERE tenant_id=? AND line_id=? AND ack_at IS NULL " +
                "ORDER BY id ASC LIMIT 10", tenantId, lineId);
        } catch (Exception e) { cmds = List.of(); }
        return ResponseEntity.ok(Map.of("commands", cmds));
    }

    @PostMapping("/api/m11/conveyor/commands/{id}/ack")
    public ResponseEntity<Map<String, Object>> ackCommand(@PathVariable long id,
                                                            @RequestBody(required=false) Map<String, Object> body) {
        String result = body == null ? "ok" : String.valueOf(body.getOrDefault("result", "ok"));
        try {
            jdbc.update("UPDATE m11_conveyor_command SET ack_at=CURRENT_TIMESTAMP, ack_result=? WHERE id=?",
                        result, id);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ------------------------------------------------------------------
    // CONFIG ENDPOINT — agent polls this every 10 s. Returns current PLC
    // params + active acceptance scopes + config_version. Agent compares
    // version locally; if newer, reapplies (reconnects PLC if host/port
    // changed, recaches acceptance scopes).
    // ------------------------------------------------------------------
    @GetMapping("/api/m11/agents/{agentId}/config")
    public ResponseEntity<Map<String, Object>> agentConfig(@PathVariable String agentId) {
        Long tenantId = TenantContext.get();
        Map<String, Object> agent;
        try {
            agent = jdbc.queryForMap(
                "SELECT agent_id, agent_kind, line_id, plc_host, plc_port, " +
                "plc_unit_id, plc_timeout_ms, devices_json, line_workflow_json, " +
                "config_version, enabled " +
                "FROM m11_agent WHERE tenant_id=? AND agent_id=?",
                tenantId, agentId);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(404).body(Map.of(
                "error","agent_not_registered",
                "message","Register agent at /configurable/aria:agents first"));
        }
        // Active acceptance scopes for this agent — include flow_steps_json
        List<Map<String, Object>> scopes;
        try {
            scopes = jdbc.queryForList(
                "SELECT id, scope_kind, scope_ref_id, expected_sku, expected_offer, " +
                "expected_barcode, max_pieces, current_pieces, weight_min_kg, " +
                "weight_max_kg, flow_steps_json, ttl_s " +
                "FROM m11_agent_acceptance_scope " +
                "WHERE tenant_id=? AND agent_id=? AND active=1 " +
                "ORDER BY id ASC", tenantId, agentId);
        } catch (Exception e) { scopes = List.of(); }
        // Parse flow_steps_json from string → list so the agent doesn't
        // have to re-parse.
        for (Map<String, Object> s : scopes) {
            Object raw = s.get("flow_steps_json");
            if (raw instanceof String str && !str.isBlank()) {
                try { s.put("flowSteps", mapper.readValue(str, List.class)); }
                catch (Exception e) { s.put("flowSteps", List.of()); }
            } else { s.put("flowSteps", List.of()); }
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("agentId", agent.get("agent_id"));
        out.put("lineId", agent.get("line_id"));
        out.put("enabled", agent.get("enabled"));
        out.put("configVersion", agent.get("config_version"));

        // ----- Device topology --------------------------------------------
        // Prefer devices_json (rich, multi-device). Fall back to flat
        // plc_host/port (legacy single-PLC agents).
        Map<String, Object> devices = null;
        Object dj = agent.get("devices_json");
        if (dj instanceof String djs && !djs.isBlank()) {
            try { devices = mapper.readValue(djs, Map.class); }
            catch (Exception ignored) {}
        }
        if (devices == null) {
            devices = Map.of("plc", Map.of(
                "mode","MODBUS_TCP",
                "host", agent.get("plc_host") == null ? "" : agent.get("plc_host"),
                "port", agent.get("plc_port"),
                "unit_id", agent.get("plc_unit_id"),
                "timeout_ms", agent.get("plc_timeout_ms")));
        }
        out.put("devices", devices);

        // ----- Per-line workflow binding (v6) -------------------------
        // Tells the agent "PACK-LINE-1 is currently executing packing_run #1
        // for SKU GIN-1L-IDH, target 1200 pcs". Agent uses this to: attribute
        // box events to the right run, enforce SKU/qty cap locally, and
        // auto-halt when target is hit.
        Object lwj = agent.get("line_workflow_json");
        if (lwj instanceof String s2 && !s2.isBlank()) {
            try { out.put("lineWorkflow", mapper.readValue(s2, Map.class)); }
            catch (Exception ignored) { out.put("lineWorkflow", Map.of("lines", List.of())); }
        } else {
            out.put("lineWorkflow", Map.of("lines", List.of()));
        }
        // Keep legacy plc block too so v1/v2 agents still work
        out.put("plc", Map.of(
            "host", agent.get("plc_host") == null ? "" : agent.get("plc_host"),
            "port", agent.get("plc_port"),
            "unitId", agent.get("plc_unit_id"),
            "timeoutMs", agent.get("plc_timeout_ms")));
        out.put("acceptanceScopes", scopes);
        out.put("serverTs", Instant.now().toString());
        return ResponseEntity.ok(out);
    }

    @GetMapping("/api/m11/conveyor/sku-spec")
    public ResponseEntity<Map<String, Object>> skuSpec(@RequestParam String sku) {
        Long tenantId = TenantContext.get();
        try {
            Map<String, Object> row = jdbc.queryForMap(
                "SELECT sku_code, carton_weight_kg, status FROM m5_sku_master " +
                "WHERE tenant_id=? AND sku_code=?", tenantId, sku);
            double cartonKg = num(row.get("carton_weight_kg"));
            int targetG = (int) Math.round(cartonKg * 1000);
            // ±10% default tolerance — caller can override via per-SKU column in future
            int minG = (int) Math.round(targetG * 0.90);
            int maxG = (int) Math.round(targetG * 1.10);
            return ResponseEntity.ok(Map.of(
                "sku", row.get("sku_code"),
                "targetG", targetG,
                "minG", minG,
                "maxG", maxG,
                "tolerancePct", 10,
                "active", "ACTIVE".equalsIgnoreCase(String.valueOf(row.get("status")))));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", "sku not found: " + sku));
        }
    }

    @PostMapping("/api/m11/agents/heartbeat")
    public ResponseEntity<Map<String, Object>> heartbeat(@RequestBody Map<String, Object> body) {
        Long tenantId = TenantContext.get();
        String lineId = str(body.get("lineId"));
        Integer queueDepth = body.get("queueDepth") == null ? 0
                : ((Number) body.get("queueDepth")).intValue();
        try {
            jdbc.update(
                "INSERT INTO m11_agent_heartbeat (tenant_id, agent_kind, line_id, queue_depth, last_ts) " +
                "VALUES (?, 'conveyor', ?, ?, CURRENT_TIMESTAMP) " +
                "ON DUPLICATE KEY UPDATE queue_depth=VALUES(queue_depth), last_ts=CURRENT_TIMESTAMP",
                tenantId, lineId, queueDepth);
        } catch (Exception ignored) {}
        return ResponseEntity.ok(Map.of("ok", true, "serverTs", Instant.now().toString()));
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
}
