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
import java.util.Map;

/**
 * {@code m5.pack-weigh} — the checkweigher step of the Packing Process,
 * always SECOND: an operator first scans the box's barcode
 * ({@link M5PackScanActionHandler}, {@code m5.pack-scan}), then the box is
 * weighed and this action validates the reading against the SKU's expected
 * {@code carton_weight_kg} (same ±10% tolerance convention already used by
 * {@link com.aria.integration.conveyor.ConveyorAgentController#boxEvent}
 * and {@code #skuSpec} for the automated PLC/conveyor line — this is the
 * manual/handheld-scanner-page equivalent).
 *
 * <p>Targets the scan log row either directly by {@code scanLogId} (the
 * barcode step's response — for API/edge-agent callers that already have
 * it), or by {@code packingRunId} + {@code barcode} (for the page UI, which
 * has no way to carry a prior action response's field into a later action's
 * formData — the page schema here has no "fill this field from that
 * button's response" primitive, only {@code onChangeFill} for a field-level
 * pick), resolving the most recent {@code ACCEPTED}, not-yet-weighed row for
 * that (packing run, barcode). A scan log row must already exist and be
 * {@code ACCEPTED} before it can be weighed; a barcode-rejected scan has
 * nothing to weigh.
 *
 * <p>Outcomes:
 * <ul>
 *   <li><b>No expected weight on file</b> for the SKU — weight is recorded
 *       for traceability but nothing is validated (can't check against a
 *       spec that doesn't exist); outcome stays {@code ACCEPTED}.
 *   <li><b>Within ±10%</b> of {@code carton_weight_kg} — stays
 *       {@code ACCEPTED}, {@code weight_kg} recorded on the row.
 *   <li><b>Out of tolerance</b> — the row flips to {@code REJECTED} /
 *       {@code reject_reason='WEIGHT_OUT_OF_SPEC'}, and the earlier
 *       {@code m5_packing_run.actual_packs} increment from the barcode step
 *       is reversed (by the row's own {@code bundle_count}) — a
 *       weight-failed box isn't a completed pack.
 * </ul>
 *
 * <pre>
 *   { "formData": { "scanLogId": 231, "weightKg": 0.94 } }
 *   // or, from the page UI:
 *   { "formData": { "packingRunId": 4, "barcode": "8901234567890", "weightKg": 0.94 } }
 * </pre>
 */
@Component
public class M5PackWeighActionHandler implements ActionHandler {

    private static final BigDecimal TOLERANCE_PCT = new BigDecimal("0.10");

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m5.pack-weigh"; }

    @Override
    @Transactional("m0PageTransactionManager")
    public ActionResult handle(ActionInvocation inv) {
        Map<String, Object> formData = inv == null || inv.getFormData() == null ? Map.of() : inv.getFormData();
        Long scanLogId = asLong(formData.get("scanLogId"));
        BigDecimal weightKg = asBigDecimal(formData.get("weightKg"));
        if (weightKg == null || weightKg.signum() <= 0) return ActionResult.error("formData.weightKg must be a positive number");

        long tenantId = TenantContext.get();

        Map<String, Object> row;
        if (scanLogId != null) {
            try {
                row = jdbc.queryForMap(
                        "SELECT id, packing_run_id, sku_code, bundle_count, outcome, weight_kg FROM m5_packing_scan_log "
                                + "WHERE tenant_id = ? AND id = ?",
                        tenantId, scanLogId);
            } catch (EmptyResultDataAccessException e) {
                return ActionResult.error("scan log " + scanLogId + " not found");
            }
        } else {
            Long packingRunId = asLong(formData.get("packingRunId"));
            String barcode = formData.get("barcode") == null ? null : formData.get("barcode").toString();
            if (packingRunId == null || barcode == null || barcode.isBlank()) {
                return ActionResult.error("formData.scanLogId, or formData.packingRunId + barcode, required");
            }
            try {
                row = jdbc.queryForMap(
                        "SELECT id, packing_run_id, sku_code, bundle_count, outcome, weight_kg FROM m5_packing_scan_log "
                                + "WHERE tenant_id = ? AND packing_run_id = ? AND scanned_barcode = ? AND outcome = 'ACCEPTED' AND weight_kg IS NULL "
                                + "ORDER BY id DESC LIMIT 1",
                        tenantId, packingRunId, barcode);
            } catch (EmptyResultDataAccessException e) {
                return ActionResult.error("no unweighed ACCEPTED scan found for packing run " + packingRunId + " barcode " + barcode);
            }
            scanLogId = ((Number) row.get("id")).longValue();
        }
        if (!"ACCEPTED".equals(row.get("outcome"))) {
            return ActionResult.error("scan log " + scanLogId + " was not ACCEPTED at barcode scan — nothing to weigh");
        }
        if (row.get("weight_kg") != null) {
            return ActionResult.error("scan log " + scanLogId + " has already been weighed");
        }

        Long packingRunId = ((Number) row.get("packing_run_id")).longValue();
        String skuCode = (String) row.get("sku_code");
        BigDecimal bundleCount = row.get("bundle_count") == null ? BigDecimal.ONE : new BigDecimal(row.get("bundle_count").toString());

        BigDecimal expectedKg = null;
        if (skuCode != null) {
            try {
                Object v = jdbc.queryForObject(
                        "SELECT carton_weight_kg FROM m5_sku_master WHERE tenant_id = ? AND sku_code = ?",
                        Object.class, tenantId, skuCode);
                expectedKg = v == null ? null : new BigDecimal(v.toString());
            } catch (EmptyResultDataAccessException ignored) { /* SKU gone since scan — skip validation */ }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("scanLogId", scanLogId);
        data.put("weightKg", weightKg);
        data.put("expectedWeightKg", expectedKg);

        if (expectedKg == null || expectedKg.signum() <= 0) {
            jdbc.update("UPDATE m5_packing_scan_log SET weight_kg = ? WHERE tenant_id = ? AND id = ?",
                    weightKg, tenantId, scanLogId);
            data.put("outcome", "ACCEPTED");
            return ActionResult.builder().ok(true)
                    .message("weight recorded (no expected weight on file for " + skuCode + " — not validated)")
                    .data(data).build();
        }

        BigDecimal diffPct = weightKg.subtract(expectedKg).abs().divide(expectedKg, 4, java.math.RoundingMode.HALF_UP);
        data.put("diffPct", diffPct);

        if (diffPct.compareTo(TOLERANCE_PCT) > 0) {
            jdbc.update(
                    "UPDATE m5_packing_scan_log SET weight_kg = ?, outcome = 'REJECTED', reject_reason = 'WEIGHT_OUT_OF_SPEC' "
                            + "WHERE tenant_id = ? AND id = ?",
                    weightKg, tenantId, scanLogId);
            jdbc.update(
                    "UPDATE m5_packing_run SET actual_packs = GREATEST(COALESCE(actual_packs, 0) - ?, 0) WHERE tenant_id = ? AND id = ?",
                    bundleCount, tenantId, packingRunId);
            data.put("outcome", "REJECTED");
            data.put("rejectReason", "WEIGHT_OUT_OF_SPEC");
            return ActionResult.builder().ok(false)
                    .message("weight out of spec: " + weightKg + "kg vs expected " + expectedKg + "kg (tolerance ±10%) — pack rejected")
                    .data(data).build();
        }

        jdbc.update("UPDATE m5_packing_scan_log SET weight_kg = ? WHERE tenant_id = ? AND id = ?",
                weightKg, tenantId, scanLogId);
        data.put("outcome", "ACCEPTED");
        return ActionResult.builder().ok(true)
                .message("weight OK: " + weightKg + "kg (expected " + expectedKg + "kg)")
                .data(data).build();
    }

    private static Long asLong(Object v) {
        if (v == null) return null;
        try { return Long.valueOf(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static BigDecimal asBigDecimal(Object v) {
        if (v == null) return null;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return null; }
    }
}
