package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * aria:weighment-capture-other-vehicle's two RepeatingGroups —
 * material_details (LOADING) and invoice_details_items (UNLOADING) — fanned
 * out one row per line into a single shared child table
 * (weighment_other_vehicle_line_item), told apart by {@code line_type}
 * ('MATERIAL'/'INVOICE'). Kept as its own listener rather than a generic
 * m0_workflow_projection field_map_json entry for the same reason as
 * {@link DispatchInvoiceItemTransitionListener}: the generic projection
 * mechanism has no fan-out from a submitted array to N child rows — a plain
 * scalar column mapping (as material_details briefly had, see V285) just
 * keeps the first row's Map.toString() and drops the rest.
 */
@Component
public class OtherVehicleLineItemTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(OtherVehicleLineItemTransitionListener.class);
    private static final String PAGE_CODE = "aria:weighment-capture-other-vehicle";

    private final JdbcTemplate jdbc;

    public OtherVehicleLineItemTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null) return;
        Object pageDataRaw = bag.get(PAGE_CODE);
        if (!(pageDataRaw instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;

        long tenantId = TenantContext.get();
        writeLines(tenantId, instance, pageData, "material_details", "MATERIAL");
        writeLines(tenantId, instance, pageData, "invoice_details_items", "INVOICE");
    }

    /**
     * Only touches {@code line_type} rows when the submission actually
     * carries that key — the section (and both repeaters) is only
     * visible/submitted at the first weighment (weight_seq=='FIRST'); a
     * later transition (second weighment, etc.) carries neither key at all
     * and must leave whatever was captured at the first weighment alone,
     * not wipe it. Delete-then-insert (scoped to this instance's own
     * line_type) makes re-submission idempotent without touching the other
     * line_type's rows.
     */
    private void writeLines(long tenantId, WorkflowInstance instance, Map<String, Object> pageData,
                             String formKey, String lineType) {
        if (!pageData.containsKey(formKey)) return;
        Object linesRaw = pageData.get(formKey);
        if (!(linesRaw instanceof List<?> lines)) return;

        jdbc.update("DELETE FROM weighment_other_vehicle_line_item WHERE tenant_id=? AND workflow_instance_id=? AND line_type=?",
                tenantId, instance.getId(), lineType);

        int rowNo = 0;
        int written = 0;
        for (Object lineRaw : lines) {
            rowNo++;
            if (!(lineRaw instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> line = (Map<String, Object>) lineRaw;
            try {
                if ("MATERIAL".equals(lineType)) {
                    jdbc.update(
                            "INSERT INTO weighment_other_vehicle_line_item "
                                    + "(tenant_id, workflow_instance_id, line_type, row_no, material_name, qty, unit, remarks) "
                                    + "VALUES (?,?,?,?,?,?,?,?)",
                            tenantId, instance.getId(), lineType, rowNo,
                            str(line, "material_name"), num(line, "qty"), str(line, "unit"), str(line, "remarks"));
                } else {
                    jdbc.update(
                            "INSERT INTO weighment_other_vehicle_line_item "
                                    + "(tenant_id, workflow_instance_id, line_type, row_no, invoice_no, invoice_date, item_desc, qty, amount, remarks) "
                                    + "VALUES (?,?,?,?,?,?,?,?,?,?)",
                            tenantId, instance.getId(), lineType, rowNo,
                            str(line, "invoice_no"), date(line, "invoice_date"), str(line, "item_desc"),
                            num(line, "qty"), num(line, "amount"), str(line, "remarks"));
                }
                written++;
            } catch (Exception e) {
                log.warn("weighment_other_vehicle_line_item ({}) insert failed for instance {} row {}: {}",
                        lineType, instance.getId(), rowNo, e.getMessage());
            }
        }
        log.info("weighment_other_vehicle_line_item ({}): wrote {}/{} row(s) for instance {}",
                lineType, written, lines.size(), instance.getId());
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return (v != null && !String.valueOf(v).isBlank()) ? String.valueOf(v) : null;
    }

    private static BigDecimal num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Date date(Map<String, Object> m, String key) {
        String v = str(m, key);
        if (v == null) return null;
        try {
            return Date.valueOf(LocalDate.parse(v.length() > 10 ? v.substring(0, 10) : v));
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
