package com.aria.workflow.projection;

import com.aria.security.TenantContext;
import com.aria.workflow.engine.WorkflowTransitionListener;
import com.aria.workflow.model.WorkflowInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Persists {@code aria:pmt-camera-verification}'s "SKU-wise Loading Issue
 * Details" RepeatingGroup ({@code loading_issue_items}) into {@code
 * pmt_loading_issue_detail} (V281) — one row per SKU line, not the
 * single-row-per-instance shape {@link WorkflowProjectionListener}'s generic
 * config handles (that mechanism only ever JSON-serialises a repeater into
 * one column, e.g. dispatch_vehicle_detail.oil_rm_item; a genuine per-SKU
 * table needs real rows, same reasoning as {@code M10StartBayActionHandler}'s
 * {@code order_sku_line_items} handling).
 *
 * <p>Gated on the {@code PMT_CAMERA_VERIFICATION -> PMT_CHECK_PENDING_MANAGER}
 * transition (the one and only transition this page drives, in both
 * M10.FGLoading and M10.PackLoading today) rather than a hardcoded
 * workflow-code list, so any future workflow reusing this same page is
 * covered automatically with no code change here.
 *
 * <p>Replace-all semantics: every submission of this page deletes this
 * instance's existing rows and reinserts the current grid contents, so a row
 * the operator removed in the UI is actually removed here too (not left
 * stranded from an earlier submission) — same idea as {@code
 * M10StartBayActionHandler} clearing an open bay before starting another,
 * just simpler since there is no separate "close" step for this grid.
 *
 * <p>Exceptions never propagate — the engine catches listener failures, so a
 * malformed row can't fail the transition; that row is just skipped.
 */
@Component
public class PmtLoadingIssueItemsListener implements WorkflowTransitionListener {

    private static final Logger log = LoggerFactory.getLogger(PmtLoadingIssueItemsListener.class);
    private static final String PAGE_CODE = "aria:pmt-camera-verification";
    private static final String FROM_STATE = "PMT_CAMERA_VERIFICATION";

    private final JdbcTemplate jdbc;

    public PmtLoadingIssueItemsListener(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onTransition(WorkflowInstance instance, String previousState, Map<String, Object> bag) {
        if (!FROM_STATE.equals(previousState) || bag == null) return;
        Object pageObj = bag.get(PAGE_CODE);
        if (!(pageObj instanceof Map<?, ?> pageMap)) return;
        Object itemsObj = ((Map<String, Object>) pageMap).get("loading_issue_items");
        if (!(itemsObj instanceof List<?> items)) return;

        try {
            long tenantId = TenantContext.get();
            Long instanceId = instance.getId();
            // Replace-all: clear this instance's rows before reinserting the
            // current grid contents (see class javadoc).
            jdbc.update("DELETE FROM pmt_loading_issue_detail WHERE tenant_id=? AND workflow_instance_id=?",
                    tenantId, instanceId);

            for (Object rowObj : items) {
                if (!(rowObj instanceof Map<?, ?> row)) continue;
                Map<String, Object> r = (Map<String, Object>) row;
                String vehicleNo = str(r.get("vehicle_no"));
                String orderNo = str(r.get("order_no"));
                String rdName = str(r.get("rd_name"));
                String rdNo = str(r.get("rd_no"));
                String skuCode = str(r.get("sku"));
                String loadingIssue = str(r.get("loading_issue"));
                BigDecimal boxQty = num(r.get("box_qty"));
                BigDecimal pcsQty = num(r.get("pcs_qty"));
                String remarks = str(r.get("checking_officer_remarks"));

                // Skip a fully-empty placeholder row (e.g. the grid's default
                // blank first row when nothing was actually filled in).
                if (skuCode == null && orderNo == null && loadingIssue == null) continue;

                jdbc.update(
                        "INSERT INTO pmt_loading_issue_detail "
                                + "(tenant_id, workflow_instance_id, flow_variant_code, vehicle_no, order_no, rd_name, rd_no, sku_code, loading_issue, box_qty, pcs_qty, checking_officer_remarks) "
                                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                        tenantId, instanceId, instance.getWorkflowCode(), vehicleNo, orderNo, rdName, rdNo,
                        skuCode, loadingIssue, boxQty, pcsQty, remarks);
            }
        } catch (Exception ex) {
            log.warn("pmt loading-issue-items save failed for instance={}: {}", instance.getId(), ex.getMessage());
        }
    }

    private static String str(Object v) {
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static BigDecimal num(Object v) {
        if (v == null) return null;
        try {
            return new BigDecimal(v.toString().trim());
        } catch (RuntimeException e) {
            return null;
        }
    }
}
