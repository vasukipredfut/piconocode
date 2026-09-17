package com.aria.workflow.projection;

import com.aria.auth.AuthUserDetails;
import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * billing_order_details child rows driven by a page's "Loaded SKU Details"
 * grid (originally aria:skipped-item-allocation's) — one row per order/SKU
 * line, capturing what was actually loaded vs skipped for that loading
 * transaction.
 *
 * <p>Kept as its own listener (like {@link ParkingBayEntryTransitionListener})
 * rather than a generic {@code m0_workflow_projection} config row, because
 * the submitted {@code loaded_sku_details} field is an ARRAY — the generic
 * projection mechanism only ever writes one row per instance per table, it
 * has no fan-out from a list to N child rows.
 *
 * <p>V253 fix: this used to look for {@code loaded_sku_details} under a
 * hard-coded {@code bag.get("aria:skipped-item-allocation")} key — but
 * confirmed off a live {@code workflow.transition} payload (pageCode
 * {@code aria:billing-entry}, instance 1776), this Renderer carries one
 * accumulated form-state object forward across pages, so the field actually
 * rides under whatever page is CURRENTLY submitting, not a fixed literal
 * key. The fixed key meant this almost never found data to write. Now
 * resolves the actual triggering page per-transition — same
 * {@code resolveTriggeringPageCode} approach {@link WorkflowProjectionListener}
 * uses (matching previousState -&gt; instance.getCurrentState() against
 * {@code workflow_definition.transitions[]}, each of which already declares
 * its own pageCode) — and reads {@code loaded_sku_details} from THAT page's
 * bag entry.
 *
 * <p>Not scoped to a single workflow code — this grid is reused across M10
 * flow variants (V192's table migration references M10.PackUnload; seen
 * live in production use on M10.FGLoading too), so this only gates on a
 * submitted {@code loaded_sku_details} list being present under the
 * resolved triggering page's key in the bag.
 *
 * <p>Because the fix above now finds real data on more transitions than
 * before, a write-once instance-level guard would be unsafe (a genuine
 * repeat submission — e.g. correcting one line's qty — must still land).
 * {@code billing_order_details} has a UNIQUE key on (tenant_id,
 * workflow_instance_id, order_id, sku_code) (V253) and this listener uses
 * {@code INSERT .. ON DUPLICATE KEY UPDATE} instead of a plain INSERT, so a
 * repeat submission for the same line updates it in place rather than
 * creating a duplicate row.
 *
 * <p>V192 created {@code billing_order_details} + its entity registry rows
 * but explicitly deferred this write-side wiring ("pending confirmation of
 * the intended FSM placement") — this listener is that follow-up.
 */
@Component
public class BillingOrderDetailTransitionListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(BillingOrderDetailTransitionListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JdbcTemplate jdbc;

    public BillingOrderDetailTransitionListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (bag == null) return;

        String triggeringPageCode = resolveTriggeringPageCode(instance, previousState);
        if (triggeringPageCode == null) return;

        Object pageDataRaw = bag.get(triggeringPageCode);
        if (!(pageDataRaw instanceof Map)) return;
        Map<String, Object> pageData = (Map<String, Object>) pageDataRaw;

        long tenantId = TenantContext.get();
        String vehicleNo = str(pageData, "vehicle_no");
        String gatePassNo = str(pageData, "gpNumber", "gp_number");

        Object linesRaw = pageData.get("loaded_sku_details");
        if (linesRaw instanceof List<?> lines && !lines.isEmpty()) {
            int written = 0;
            for (Object lineRaw : lines) {
                if (!(lineRaw instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> line = (Map<String, Object>) lineRaw;
                try {
                    jdbc.update(
                            "INSERT INTO billing_order_details (tenant_id, workflow_instance_id, vehicle_no, "
                                    + "gate_pass_no, order_id, rd_code, rd_desc, sku_code, sku_desc, order_qty, "
                                    + "order_rate, loading_qty, skipped_quantity, reason, invoice_no) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                                    + "ON DUPLICATE KEY UPDATE vehicle_no = VALUES(vehicle_no), "
                                    + "gate_pass_no = VALUES(gate_pass_no), rd_code = VALUES(rd_code), "
                                    + "rd_desc = VALUES(rd_desc), sku_desc = VALUES(sku_desc), "
                                    + "order_qty = VALUES(order_qty), loading_qty = VALUES(loading_qty), "
                                    + "skipped_quantity = VALUES(skipped_quantity), reason = VALUES(reason), "
                                    + "invoice_no = COALESCE(VALUES(invoice_no), invoice_no), "
                                    + "updated_at = CURRENT_TIMESTAMP",
                            tenantId, instance.getId(), vehicleNo, gatePassNo,
                            str(line, "order_id"), str(line, "rd_code"), str(line, "rd_desc"),
                            str(line, "sku_code"), str(line, "sku_desc"),
                            // Confirmed off a live transition payload: this grid's line items carry the order
                            // quantity under "assign_qty" (the underlying OrderDispatchDetail column name),
                            // never a literal "order_qty" key -- that fallback always missed, writing 0.
                            num(line, "assign_qty", "order_qty"),
                            // This grid never carries a rate -- 0, not null, so two lines for the
                            // same order+SKU here always compare equal on the (..., order_rate) unique
                            // key (V324) and correctly collapse into one row, same as before that
                            // migration. A real rate only ever comes from the Billing Table path below.
                            num(line, "order_rate"), num(line, "loading_qty"), num(line, "skipped_quantity"),
                            str(line, "reason", "skip_reason"), str(line, "invoice_no"));
                    written++;
                } catch (Exception e) {
                    // One bad line must not sink the rest — insert what we can, log what we can't.
                    log.warn("billing_order_details insert failed for instance {} line order_id={}: {}",
                            instance.getId(), line.get("order_id"), e.getMessage());
                }
            }

            writeAudit(tenantId, instance, vehicleNo, gatePassNo, written, lines.size(), triggeringPageCode);
            log.info("billing_order_details: wrote {}/{} row(s) for instance {} (vehicle={}) via {}",
                    written, lines.size(), instance.getId(), vehicleNo, triggeringPageCode);
        }

        writeBillingTableRateRows(tenantId, instance, vehicleNo, gatePassNo, pageData);
    }

    /**
     * The "Billing Table" grid's rate/tax/net-rate/billed-qty/invoice_no
     * figures, taken directly from the submitted {@code billing_table} rows
     * — no join back to {@code m5_order_dispatch_detail} /
     * {@code m5_sku_master} / {@code loading_progress_detail_bay}. The grid
     * already carries {@code rd_code}, {@code rd_desc}, {@code sku_desc},
     * {@code order_rate}, {@code gst_rate_pct} and {@code invoice_no} per
     * row; loaded quantity is read from {@code loaded_qty} (falling back to
     * the grid's existing {@code line_dispatch_qty}/{@code loading_qty}
     * aliases for clients that haven't added the new key yet).
     *
     * <p>{@code tax_amount}/{@code net_rate} are still derived arithmetically
     * from the row's own {@code order_rate}/{@code gst_rate_pct} rather than
     * trusting whatever the client computed for those two, so rounding stays
     * consistent with the rest of the GST math in this codebase — this is
     * pure arithmetic on the row, not a lookup, so it doesn't reintroduce a
     * join.
     *
     * <p>{@code order_qty}: confirmed live (2026-09-17) the {@code billing_table}
     * grid's own rows already carry this under the key {@code assign_qty}
     * (same underlying meaning as {@code loaded_sku_details}' own
     * {@code assign_qty} -&gt; order_qty mapping above) — some products reach
     * billing WITHOUT ever having a {@code loaded_sku_details} line for
     * them, so their row is created fresh right here, and previously had
     * nothing to fill {@code order_qty} with, silently leaving it at the
     * column's {@code DEFAULT 0}. Now read straight off the row, same as
     * every other field in this method — no join, no separate lookup.
     */
    @SuppressWarnings("unchecked")
    private void writeBillingTableRateRows(long tenantId, WorkflowInstance instance, String vehicleNo,
                                            String gatePassNo, Map<String, Object> pageData) {
        if (vehicleNo == null) return;

        Object raw = pageData.get("billing_table");
        if (!(raw instanceof List<?> rows) || rows.isEmpty()) return;

        int written = 0;
        for (Object rowRaw : rows) {
            if (!(rowRaw instanceof Map)) continue;
            Map<String, Object> row = (Map<String, Object>) rowRaw;
            try {
                BigDecimal orderRate = num(row, "order_rate");
                BigDecimal gstRatePct = num(row, "gst_rate_pct");
                BigDecimal taxAmount = orderRate.multiply(gstRatePct)
                        .divide(BigDecimal.valueOf(100), 3, RoundingMode.HALF_UP);
                BigDecimal netRate = orderRate.subtract(taxAmount);
                BigDecimal billedQty = num(row, "loaded_qty", "line_dispatch_qty", "loading_qty");
                String orderId = str(row, "order_id");
                String skuCode = str(row, "sku_code");
                String invoiceNo = str(row, "invoice_no");
                BigDecimal orderQty = num(row, "assign_qty", "order_qty");

                jdbc.update(
                        "INSERT INTO billing_order_details (tenant_id, workflow_instance_id, vehicle_no, "
                                + "gate_pass_no, order_id, rd_code, rd_desc, sku_code, sku_desc, order_qty, loading_qty, order_rate, "
                                + "gst_rate_pct, tax_amount, net_rate, billed_qty, invoice_no) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "
                                + "ON DUPLICATE KEY UPDATE rd_code = VALUES(rd_code), rd_desc = VALUES(rd_desc), "
                                + "sku_desc = VALUES(sku_desc), "
                                // Takes the larger of the stored value and this submission rather than only
                                // filling in when still at the table default of 0 (V192/V253's original
                                // guard) -- that froze loading_qty at whatever the skip-allocation write set
                                // on the FIRST loading pass, so a LOAD_AGAIN reload (a second, cumulative
                                // loading session on the same instance/SKU) never grew it past that
                                // first-pass figure. GREATEST keeps that same accumulation for loading_qty
                                // instead of leaving it stale, while still never overwriting it with
                                // something smaller. order_qty gets the same GREATEST treatment: the
                                // loaded_sku_details insert (above) may have already set it, and this
                                // row's own assign_qty should agree -- GREATEST just means a row missing
                                // assign_qty (0 here) never overwrites an already-correct value.
                                + "loading_qty = GREATEST(loading_qty, VALUES(loading_qty)), "
                                + "order_qty = GREATEST(order_qty, VALUES(order_qty)), "
                                + "order_rate = VALUES(order_rate), "
                                + "gst_rate_pct = VALUES(gst_rate_pct), tax_amount = VALUES(tax_amount), "
                                + "net_rate = VALUES(net_rate), billed_qty = VALUES(billed_qty), "
                                + "invoice_no = COALESCE(VALUES(invoice_no), invoice_no), "
                                + "updated_at = CURRENT_TIMESTAMP",
                        tenantId, instance.getId(), vehicleNo, gatePassNo,
                        orderId, str(row, "rd_code"), str(row, "rd_desc"),
                        skuCode, str(row, "sku_desc"), orderQty, billedQty,
                        orderRate, gstRatePct, taxAmount, netRate, billedQty, invoiceNo);
                written++;
            } catch (Exception e) {
                log.warn("billing_order_details rate/tax upsert failed for instance {} order_id={}: {}",
                        instance.getId(), row.get("order_id"), e.getMessage());
            }
        }
        log.info("billing_order_details: wrote {}/{} billing-table rate/tax row(s) for instance {} (vehicle={})",
                written, rows.size(), instance.getId(), vehicleNo);
    }

    /**
     * Which page's submission actually drove this transition — see class
     * javadoc. Self-contained rather than sharing
     * {@link WorkflowProjectionListener}'s private method, matching that
     * class's own note on why it isn't widened onto the shared
     * {@link WorkflowTransitionListener} interface for one caller's sake.
     */
    @SuppressWarnings("unchecked")
    private String resolveTriggeringPageCode(WorkflowInstance instance, String previousState) {
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

    private void writeAudit(long tenantId, WorkflowInstance instance, String vehicleNo, String gatePassNo,
                             int written, int total, String triggeringPageCode) {
        try {
            jdbc.update(
                    "INSERT INTO m0_audit_log (tenant_id, ts, actor_id, action_kind, entity_code, "
                            + "workflow_code, workflow_instance_id, vehicle_no, gp_number, message) "
                            + "VALUES (?, CURRENT_TIMESTAMP, ?, 'BILLING_ORDER_DETAIL_CAPTURED', 'BillingOrderDetail', "
                            + "?, ?, ?, ?, ?)",
                    tenantId, currentUserId(), instance.getWorkflowCode(), instance.getId(), vehicleNo, gatePassNo,
                    "Captured " + written + "/" + total + " loaded/skipped SKU line(s) via " + triggeringPageCode);
        } catch (Exception e) {
            // Audit-log writes must never break the mutation that triggered them.
            log.warn("billing_order_details audit-log write failed for instance {}: {}",
                    instance.getId(), e.getMessage());
        }
    }

    private static Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return null;
        Object p = a.getPrincipal();
        return p instanceof AuthUserDetails uid ? uid.getId() : null;
    }

    private static String str(Map<String, Object> m, String... keys) {
        for (String k : keys) {
            Object v = m.get(k);
            if (v != null && !String.valueOf(v).isBlank()) return String.valueOf(v);
        }
        return null;
    }

    private static BigDecimal num(Map<String, Object> m, String... keys) {
        Object v = null;
        for (String k : keys) {
            v = m.get(k);
            if (v != null) break;
        }
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
