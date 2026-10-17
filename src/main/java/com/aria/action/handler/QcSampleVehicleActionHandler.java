package com.aria.action.handler;

import com.aria.action.ActionHandler;
import com.aria.action.ActionInvocation;
import com.aria.action.ActionResult;
import com.aria.security.CurrentUser;
import com.aria.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@code m10.qc-sample-vehicle} -- the Sample Oil Test of aria:qc-approval (M10.TankerInbound) where the sample leaves in
 * its own vehicle. The QC officer enters the sample vehicle's number and the location the oil is unloaded to; the vehicle
 * is weighed (first weight), unloaded, and weighed again (second weight). Everything is kept in {@code qc_sample_vehicle}
 * (V373), one row per instance + vehicle + invoice + SKU + location.
 *
 * <p>A line carries what is known so far and the row is updated in place, so the same call serves all three steps:
 * <ol>
 *   <li>vehicle_no + sample_location (+ sample_qty) -> status OPEN;</li>
 *   <li>+ {@code w1_weight_kg} -> W1_DONE (first weight taken);</li>
 *   <li>+ {@code w2_weight_kg} -> DONE: the oil that came off, {@code |W1 - W2|}, is received into stock at the sample
 *       location ({@code m5_stock_balance} +, {@code m5_stock_movement} kind RECEIPT, ref_type QC_SAMPLE) and kept as
 *       {@code unloaded_qty}.</li>
 * </ol>
 * The second weight needs the first. A DONE row is closed: stock is posted once, so it cannot be weighed again. The
 * workflow state does not move. The logged-in user and {@code performed_by_name} are recorded per weight.
 *
 * <p>Payload ({@code sampleVehicles}; {@code samples} is accepted too, and one vehicle may be sent flat in {@code formData}):
 * <pre>
 *   { "instanceId": 1559,
 *     "formData": { "performed_by_name": "Ravi", "sampleVehicles": [
 *       { "vehicle_no": "TN23J1144", "invoice_no": "INV1", "sku_code": "SKU1", "sample_location": "SAMPLE-1",
 *         "sample_qty": 5, "w1_weight_kg": 8200 }
 *     ] } }
 * </pre>
 */
@Component
public class QcSampleVehicleActionHandler implements ActionHandler {

    private static final Logger log = LoggerFactory.getLogger(QcSampleVehicleActionHandler.class);

    @Autowired private JdbcTemplate jdbc;

    @Override
    public String code() { return "m10.qc-sample-vehicle"; }

    @Override
    @Transactional("m0PageTransactionManager")
    @SuppressWarnings("unchecked")
    public ActionResult handle(ActionInvocation inv) {
        if (inv == null || inv.getInstanceId() == null) return ActionResult.error("instanceId required");

        Map<String, Object> formData = inv.getFormData() == null ? Map.of() : inv.getFormData();
        List<Object> lines = null;
        for (String key : new String[]{"sampleVehicles", "samples"}) {
            if (formData.get(key) instanceof List<?> l && !l.isEmpty()) { lines = (List<Object>) l; break; }
        }
        if (lines == null) {
            if (str(formData, "vehicle_no", "vehicleNo") == null) {
                return ActionResult.error("formData.sampleVehicles must be a non-empty array");
            }
            lines = List.of(formData);
        }

        long tenantId = TenantContext.get();
        Long instanceId = inv.getInstanceId();
        String gpNumber = str(formData, "gpNumber", "gp_number");
        if (gpNumber == null) {
            List<Map<String, Object>> gp = jdbc.queryForList(
                    "SELECT gp_number FROM gate_pass WHERE workflow_instance_id = ? AND tenant_id = ?", instanceId, tenantId);
            if (!gp.isEmpty() && gp.get(0).get("gp_number") != null) gpNumber = String.valueOf(gp.get(0).get("gp_number"));
        }
        Long actor = CurrentUser.id();

        List<Map<String, Object>> saved = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            if (!(lines.get(i) instanceof Map)) {
                markRollbackOnly();
                return ActionResult.error("sampleVehicles[" + i + "] must be an object");
            }
            Map<String, Object> line = (Map<String, Object>) lines.get(i);
            String vehicleNo = str(line, "vehicle_no", "vehicleNo");
            String skuCode = str(line, "sku_code", "skuCode");
            String location = str(line, "sample_location", "to_location", "location", "tank_location", "tankLocation");
            String invoiceNo = str(line, "invoice_no", "invoiceNo");
            if (vehicleNo == null) return fail(i, "needs the sample vehicle_no");
            if (location == null) return fail(i, "(" + vehicleNo + ") needs a sample_location");
            if (skuCode == null) return fail(i, "(" + vehicleNo + ") needs a sku_code (the stock is per SKU)");

            BigDecimal qty, w1In, w2In;
            try {
                qty = num(line, "sample_qty", "qty");
                w1In = num(line, "w1_weight_kg", "first_weight_kg");
                w2In = num(line, "w2_weight_kg", "second_weight_kg");
            } catch (NumberFormatException e) {
                return fail(i, "(" + vehicleNo + ") quantities and weights must be numbers");
            }
            if (qty != null && qty.signum() <= 0) return fail(i, "(" + vehicleNo + ") sample_qty must be greater than 0");
            if (w1In != null && w1In.signum() <= 0) return fail(i, "(" + vehicleNo + ") w1_weight_kg must be greater than 0");
            if (w2In != null && w2In.signum() <= 0) return fail(i, "(" + vehicleNo + ") w2_weight_kg must be greater than 0");

            List<Map<String, Object>> existing = jdbc.queryForList(
                    "SELECT id, status, sample_qty, w1_weight_kg, w2_weight_kg FROM qc_sample_vehicle WHERE tenant_id = ? "
                            + "AND workflow_instance_id = ? AND vehicle_no = ? AND invoice_no <=> ? AND sku_code <=> ? AND sample_location = ?",
                    tenantId, instanceId, vehicleNo, invoiceNo, skuCode, location);
            Map<String, Object> row = existing.isEmpty() ? null : existing.get(0);
            if (row != null && "DONE".equals(String.valueOf(row.get("status")))) {
                return fail(i, "(" + vehicleNo + ") is already weighed and unloaded -- nothing more can be saved for it");
            }

            BigDecimal w1 = w1In != null ? w1In : (row == null ? null : dec(row.get("w1_weight_kg")));
            BigDecimal w2 = w2In != null ? w2In : (row == null ? null : dec(row.get("w2_weight_kg")));
            BigDecimal samplePlanned = qty != null ? qty : (row == null ? null : dec(row.get("sample_qty")));
            if (w2 != null && w1 == null) return fail(i, "(" + vehicleNo + ") take the first weight before the second");

            String by = RowActorStamp.performedBy(line, formData);
            String status = w1 == null ? "OPEN" : (w2 == null ? "W1_DONE" : "DONE");
            BigDecimal unloaded = null;
            if ("DONE".equals(status)) {
                unloaded = w1.subtract(w2).abs();
                if (unloaded.signum() == 0) return fail(i, "(" + vehicleNo + ") first and second weight are the same -- nothing was unloaded");
                receive(tenantId, instanceId, skuCode, location, vehicleNo, unloaded);
            }

            String skuDesc = str(line, "sku_desc", "sku_name", "skuName");
            String vendorName = str(line, "vendor_name", "vendorName");
            String rdCode = str(line, "rd_code", "rdCode");
            String orderId = str(line, "order_id", "orderId");
            String remarks = str(line, "remarks");
            if (row == null) {
                jdbc.update("INSERT INTO qc_sample_vehicle (tenant_id, workflow_instance_id, gp_number, invoice_no, order_id, sku_code, sku_desc, "
                                + "vendor_name, rd_code, vehicle_no, sample_location, sample_qty, w1_weight_kg, w2_weight_kg, unloaded_qty, status, remarks, "
                                + "w1_by_id, w1_by_name, w2_by_id, w2_by_name) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                        tenantId, instanceId, gpNumber, invoiceNo, orderId, skuCode, skuDesc, vendorName, rdCode, vehicleNo, location,
                        samplePlanned, w1, w2, unloaded, status, remarks,
                        w1In != null ? actor : null, w1In != null ? by : null, w2In != null ? actor : null, w2In != null ? by : null);
            } else {
                jdbc.update("UPDATE qc_sample_vehicle SET sample_qty = ?, w1_weight_kg = ?, w2_weight_kg = ?, unloaded_qty = ?, status = ?, "
                                + "remarks = COALESCE(?, remarks), order_id = COALESCE(?, order_id), sku_desc = COALESCE(?, sku_desc), "
                                + "vendor_name = COALESCE(?, vendor_name), rd_code = COALESCE(?, rd_code), "
                                + "w1_by_id = COALESCE(?, w1_by_id), w1_by_name = COALESCE(?, w1_by_name), "
                                + "w2_by_id = COALESCE(?, w2_by_id), w2_by_name = COALESCE(?, w2_by_name) "
                                + "WHERE tenant_id = ? AND id = ?",
                        samplePlanned, w1, w2, unloaded, status, remarks, orderId, skuDesc, vendorName, rdCode,
                        w1In != null ? actor : null, w1In != null ? by : null, w2In != null ? actor : null, w2In != null ? by : null,
                        tenantId, row.get("id"));
            }
            Map<String, Object> out = new java.util.LinkedHashMap<>();
            out.put("vehicleNo", vehicleNo);
            out.put("skuCode", skuCode);
            out.put("sampleLocation", location);
            out.put("status", status);
            if (unloaded != null) out.put("unloadedQty", unloaded);
            saved.add(out);
            log.info("action.qc-sample-vehicle tenantId={} instanceId={} vehicle={} sku={} location={} status={} unloaded={}",
                    tenantId, instanceId, vehicleNo, skuCode, location, status, unloaded);
        }

        return ActionResult.builder()
                .ok(true)
                .message(saved.size() + " sample vehicle(s) saved")
                .data(Map.of("saved", saved))
                .build();
    }

    /** The oil that came off the sample vehicle goes into stock at the sample location (lot-less, same as the unload receipt). */
    private void receive(long tenantId, Long instanceId, String sku, String location, String vehicleNo, BigDecimal qty) {
        List<String> uoms = jdbc.queryForList("SELECT uom_primary FROM m5_sku_master WHERE tenant_id = ? AND sku_code = ?",
                String.class, tenantId, sku);
        String uom = uoms.isEmpty() || uoms.get(0) == null ? "EA" : uoms.get(0);
        int updated = jdbc.update(
                "UPDATE m5_stock_balance SET qty_on_hand = qty_on_hand + ?, last_movement_at = CURRENT_TIMESTAMP "
                        + "WHERE tenant_id = ? AND item_code = ? AND lot_no IS NULL AND bin_code = ?",
                qty, tenantId, sku, location);
        if (updated == 0) {
            jdbc.update("INSERT INTO m5_stock_balance (tenant_id, item_code, lot_no, bin_code, uom, qty_on_hand, last_movement_at) "
                    + "VALUES (?,?,NULL,?,?,?,CURRENT_TIMESTAMP)", tenantId, sku, location, uom, qty);
        }
        jdbc.update("INSERT INTO m5_stock_movement (tenant_id, kind, item_code, to_bin, qty, uom, ref_type, ref_id, ref_number) "
                + "VALUES (?,?,?,?,?,?,?,?,?)", tenantId, "RECEIPT", sku, location, qty, uom, "QC_SAMPLE", instanceId, vehicleNo);
    }

    private ActionResult fail(int i, String why) {
        markRollbackOnly();
        return ActionResult.error("sampleVehicles[" + i + "] " + why);
    }

    private static BigDecimal num(Map<String, Object> m, String... keys) {
        String s = str(m, keys);
        return s == null ? null : new BigDecimal(s);
    }

    private static BigDecimal dec(Object v) {
        if (v == null) return null;
        try { return new BigDecimal(v.toString()); } catch (NumberFormatException e) { return null; }
    }

    private static void markRollbackOnly() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }

    private static String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !v.toString().isBlank()) return v.toString().trim();
        }
        return null;
    }
}
