package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code m5.pack-scan} — manual barcode scan validation for the "Packing
 * Process" requirement's operator-facing (non-PLC) path: an operator on the
 * {@code aria:pack-start}/running-packing page scans a box's barcode with a
 * handheld scanner into a web form, and this action decides ACCEPTED /
 * REJECTED, same job {@link com.aria.integration.conveyor.ConveyorAgentController#boxEvent}
 * already does for the automated PLC/conveyor line — this is the manual
 * equivalent, sharing the same {@code m5_packing_scan_log} ledger and the
 * same {@code m5_sku_master} lookup, but without any PLC coil/print concerns.
 *
 * <p>Outcomes:
 * <ul>
 *   <li><b>No SKU Master row matches the barcode</b> (or matches are all
 *       inactive) — rejected, {@code reject_reason='UNAUTHORIZED_BARCODE'}.
 *   <li><b>Exactly one active match</b> — accepted straight away.
 *   <li><b>Multiple active matches</b> (same barcode, different
 *       {@code offer_variant} — e.g. a festive combo pack reusing a
 *       standard pack's barcode) and no {@code offer_variant} was given in
 *       the request — NOT an error: returns {@code needsOfferSelection=true}
 *       plus the candidate offers, so the page can show a picker and the
 *       operator re-submits with the chosen {@code offer_variant}.
 *   <li><b>Planned quantity already reached</b> for this packing run (if
 *       {@code planned_total_pieces} is set) — rejected,
 *       {@code reject_reason='QTY_EXCEEDED'}, packing run left {@code RUNNING}
 *       so the operator can still hit Finish.
 * </ul>
 * On ACCEPTED, {@code m5_packing_run.actual_packs} is incremented by 1 (or by
 * {@code bundle_count} if the payload gives one) — same accounting the
 * PLC-driven path keeps via its own {@code m5_packing_scan_log} count query.
 *
 * <pre>
 *   { "instanceId": 1, "formData": {
 *       "packingRunId": 4, "barcode": "8901234567890", "bay_code": "BAY-A",
 *       "offer_variant": "FESTIVE_PACK"   // optional, only needed after a
 *                                          // needsOfferSelection response
 *   } }
 * </pre>
 */
@Component
public class M5PackScanActionHandler implements ActionHandler {

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m5.pack-scan"; }

    @Override
    @Transactional("m0PageTransactionManager")
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> formData = inv == null || inv.getFormData() == null ? Map.of() : inv.getFormData();
        Long packingRunId = asLong(formData.get("packingRunId"));
        if (packingRunId == null) return ActionResult.error("formData.packingRunId required");
        String barcode = strOpt(formData, "barcode");
        if (barcode == null) return ActionResult.error("formData.barcode required");
        String bayCode = strOpt(formData, "bay_code", "bayCode");
        String requestedOffer = strOpt(formData, "offer_variant", "offerVariant");

        long tenantId = TenantContext.get();

        Map<String, Object> run;
        try {
            run = jdbc.queryForMap(
                    "SELECT status, planned_total_pieces FROM m5_packing_run WHERE tenant_id = ? AND id = ?",
                    tenantId, packingRunId);
        } catch (EmptyResultDataAccessException e) {
            return ActionResult.error("packing run " + packingRunId + " not found");
        }
        if (!"RUNNING".equals(run.get("status"))) {
            return ActionResult.error("packing run " + packingRunId + " is not RUNNING (status=" + run.get("status") + ") — Start Bay first");
        }

        List<Map<String, Object>> matches = jdbc.queryForList(
                "SELECT sku_code, sku_name, offer_variant, carton_weight_kg FROM m5_sku_master "
                        + "WHERE tenant_id = ? AND barcode = ? AND status = 'ACTIVE'",
                tenantId, barcode);

        if (matches.isEmpty()) {
            logScan(tenantId, packingRunId, bayCode, barcode, null, null, null, inv, "REJECTED", "UNAUTHORIZED_BARCODE");
            return ActionResult.error("barcode not recognised — no active SKU in the approved SKU Master list for " + barcode);
        }

        Map<String, Object> chosen;
        if (matches.size() == 1) {
            chosen = matches.get(0);
        } else if (requestedOffer == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("needsOfferSelection", true);
            data.put("offers", matches.stream().map(m -> Map.of(
                    "offerVariant", m.get("offer_variant"),
                    "skuCode", m.get("sku_code"),
                    "skuName", m.get("sku_name"))).toList());
            return ActionResult.builder()
                    .ok(true)
                    .message("multiple offers found for this barcode — select the applicable offer")
                    .data(data)
                    .build();
        } else {
            chosen = matches.stream()
                    .filter(m -> requestedOffer.equals(m.get("offer_variant")))
                    .findFirst()
                    .orElse(null);
            if (chosen == null) {
                logScan(tenantId, packingRunId, bayCode, barcode, null, requestedOffer, null, inv, "REJECTED", "WRONG_SKU");
                return ActionResult.error("offer_variant '" + requestedOffer + "' is not valid for barcode " + barcode);
            }
        }

        String skuCode = (String) chosen.get("sku_code");
        String skuName = (String) chosen.get("sku_name");
        String offerVariant = (String) chosen.get("offer_variant");
        BigDecimal boxWeightKg = chosen.get("carton_weight_kg") == null ? null : new BigDecimal(chosen.get("carton_weight_kg").toString());

        Integer plannedTotal = (Integer) run.get("planned_total_pieces");
        if (plannedTotal != null) {
            Integer acceptedSoFar = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM m5_packing_scan_log WHERE tenant_id = ? AND packing_run_id = ? AND outcome = 'ACCEPTED'",
                    Integer.class, tenantId, packingRunId);
            if (acceptedSoFar != null && acceptedSoFar >= plannedTotal) {
                logScan(tenantId, packingRunId, bayCode, barcode, skuCode, offerVariant, null, inv, "REJECTED", "QTY_EXCEEDED");
                return ActionResult.error("planned quantity (" + plannedTotal + ") already reached for this packing run");
            }
        }

        BigDecimal bundleCount = formData.get("bundle_count") == null ? BigDecimal.ONE : new BigDecimal(formData.get("bundle_count").toString());
        Long scanLogId = logScan(tenantId, packingRunId, bayCode, barcode, skuCode, offerVariant, bundleCount, inv, "ACCEPTED", null);
        jdbc.update(
                "UPDATE m5_packing_run SET actual_packs = COALESCE(actual_packs, 0) + ? WHERE tenant_id = ? AND id = ?",
                bundleCount, tenantId, packingRunId);

        Map<String, Object> data = new HashMap<>();
        data.put("outcome", "ACCEPTED");
        data.put("scanLogId", scanLogId);
        data.put("skuCode", skuCode);
        data.put("skuName", skuName);
        data.put("offerVariant", offerVariant);
        data.put("boxWeightKg", boxWeightKg);
        // The barcode step never weighs the box itself -- pass boxWeightKg
        // (the SKU's expected carton weight) to the page so it can prompt
        // the operator/checkweigher for the actual weight next, then call
        // m5.pack-weigh with this scanLogId (see M5PackWeighActionHandler).
        data.put("needsWeighIn", boxWeightKg != null);

        return ActionResult.builder()
                .ok(true)
                .message("scan accepted: " + skuCode + (offerVariant != null ? " (" + offerVariant + ")" : ""))
                .data(data)
                .build();
    }

    private Long logScan(long tenantId, Long packingRunId, String bayCode, String barcode, String skuCode,
                          String offerVariant, BigDecimal bundleCount, ActionInvocation inv, String outcome, String rejectReason) {
        Long operatorId = inv != null && inv.getContext() != null && inv.getContext().get("actorUserId") instanceof Number
                ? ((Number) inv.getContext().get("actorUserId")).longValue()
                : null;
        jdbc.update(
                "INSERT INTO m5_packing_scan_log (tenant_id, packing_run_id, bay_code, scanned_barcode, sku_code, "
                        + "offer_variant, bundle_count, outcome, reject_reason, operator_id, scanned_at) "
                        + "VALUES (?,?,?,?,?,?,?,?,?,?, CURRENT_TIMESTAMP)",
                tenantId, packingRunId, bayCode, barcode, skuCode, offerVariant, bundleCount, outcome, rejectReason, operatorId);
        // LAST_INSERT_ID() is connection-scoped -- safe under a pooled
        // connection as long as this runs on the same transaction/connection
        // as the INSERT immediately above (same convention as
        // ParkingBayScanService).
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    private static Long asLong(Object v) {
        if (v == null) return null;
        try { return Long.valueOf(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static String strOpt(Map<String, Object> bag, String... keys) {
        for (String key : keys) {
            Object v = bag.get(key);
            if (v != null && !v.toString().isBlank()) return v.toString();
        }
        return null;
    }
}
