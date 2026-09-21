package com.aria.m10.returnstock;

import com.aria.datasource.DataSourceSpec;
import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only lookups for M10.ReturnStockIn's {@code aria:stock-checking-officer}
 * page: for a given workflow instance, how does what QC allocated to each
 * put-away location ({@code qc_processed_location.box_qty}/{@code pc_qty},
 * the "order" quantity for that batch/bin) compare against what was actually
 * confirmed on {@code aria:unloading-progress}, read from {@code
 * return_stock_leakage_section} (summed per {@code (sku_code, batch_no,
 * processed_area)}, split BOX vs PC by {@code line_type} — each {@code
 * qc_processed_location} row is itself one or the other, never both).
 *
 * <p>{@code return_stock_leakage_section} is written by {@link
 * com.aria.workflow.projection.ReturnStockLeakageSectionTransitionListener}
 * once the instance reaches {@code UNLOADING_COMPLETED} — see that class
 * and {@code V331__return_stock_leakage_section_table.sql} for why it
 * replaced the earlier {@code m5_stock_movement}-based approach (that
 * listener was removed; this controller used to read the resulting
 * always-empty ledger, so every {@code unloaded_qty} read as 0 regardless
 * of what was actually confirmed — fixed by switching the read side over to
 * the same table the write side now actually populates).
 *
 * <p>{@code qc_processed_location} itself already holds one row for a
 * location's box line and a SEPARATE row for its pieces line (same
 * batch_no+processed_area, one with {@code box_qty} set and {@code pc_qty}
 * NULL, the other the reverse) — the page's own {@code dataSource.splitRows}
 * mirrors this at display time rather than creating it. Both rows share the
 * same (sku_code, batch_no, processed_area) key the leakage subquery joins
 * on, so without {@code qpl.box_qty IS NOT NULL} on that join the one BOX
 * leakage total would double-attach to both rows — inflating the invoice
 * rollup and showing a phantom qty on the untracked pieces row.
 *
 * <p>Same bespoke-SQL rationale as {@link com.aria.m10.loading.PendingSkuController}:
 * comparing one column against an aggregate of a DIFFERENT table's rows,
 * joined on business keys (sku/batch/bin) rather than a foreign key, isn't
 * expressible through the generic {@code POST /api/m0/datasource/query}
 * filter DSL. Same {@link DataSourceSpec.Response} envelope for the same
 * reason: any {@code dataSource.type:"url"} ListView can consume it exactly
 * like a generic entity query.
 *
 * <p>{@link #unloadedBatches} is the base result (every QC-allocated
 * location for the instance); {@link #pendingBatches} is the same query
 * filtered down to rows still short (order box qty > unloaded box qty) — the
 * two endpoints share {@link #fetchBatchLines} rather than duplicating the
 * SQL, mirroring {@code PendingSkuController}/{@code skuStatus} reusing one
 * query for two views. Both stay at LOCATION grain (one row per {@code
 * qc_processed_location}) since that's the grain the officer physically
 * checks stock against, bin by bin.
 *
 * <p>{@link #unloadingDetailsByInvoice} is a DIFFERENT grain for a DIFFERENT
 * purpose: the page's final "Unloading Details" export table, which needs to
 * read the way the vehicle's paperwork does — by invoice, not by physical
 * bin. It re-aggregates the same box_qty/pc_qty/unloaded_qty comparison up
 * to {@code (invoice_no, sku_code)}, pulling {@code invoice_no} off {@code
 * return_stock_officer_item} (captured on {@code aria:return-stock-officer},
 * copied there from {@code dispatch_invoice_item} where the invoice was
 * first declared on {@code aria:dispatch-vehicle-details}) and {@code
 * vendor_name}/{@code rd_code} straight from {@code dispatch_invoice_item}
 * itself. A batch/SKU with no invoice_no captured (older instances, or a
 * line the officer left blank) rolls up under a NULL invoice_no group rather
 * than being dropped.
 */
@RestController
@RequestMapping("/api/m10/return-stock")
public class ReturnStockBatchController {

    private static final int PAGE_SIZE = 500;

    private final JdbcTemplate jdbc;

    public ReturnStockBatchController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/{instanceId}/pending-batches")
    public DataSourceSpec.Response pendingBatches(@PathVariable Long instanceId) {
        List<Map<String, Object>> content = fetchBatchLines(instanceId).stream()
                .filter(r -> asDecimal(r.get("order_box_qty")).compareTo(asDecimal(r.get("unloaded_qty"))) > 0)
                .toList();
        return wrap(content);
    }

    @GetMapping("/{instanceId}/unloaded-batches")
    public DataSourceSpec.Response unloadedBatches(@PathVariable Long instanceId) {
        return wrap(fetchBatchLines(instanceId));
    }

    @GetMapping("/{instanceId}/unloading-details-by-invoice")
    public DataSourceSpec.Response unloadingDetailsByInvoice(@PathVariable Long instanceId) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> content = jdbc.queryForList(
                        "SELECT rsoi.invoice_no AS invoice_no, dii.vendor_name AS vendor_name, dii.rd_code AS rd_code, "
                                + "rsoi.sku_code AS sku_code, rsoi.sku_name AS sku_name, "
                                + "SUM(qpl.box_qty) AS order_box_qty, SUM(qpl.pc_qty) AS order_pc_qty, "
                                + "SUM(COALESCE(smb.unloaded_qty, 0)) AS unloaded_box_qty, "
                                + "SUM(COALESCE(smp.unloaded_qty, 0)) AS unloaded_pc_qty "
                                + "FROM qc_processed_location qpl "
                                + "JOIN return_stock_officer_item rsoi "
                                + "  ON rsoi.id = qpl.return_stock_officer_item_id AND rsoi.tenant_id = qpl.tenant_id "
                                + "LEFT JOIN dispatch_invoice_item dii "
                                + "  ON dii.tenant_id = rsoi.tenant_id AND dii.workflow_instance_id = rsoi.workflow_instance_id "
                                + "  AND dii.invoice_no = rsoi.invoice_no AND dii.sku_code = rsoi.sku_code "
                                + "LEFT JOIN ( "
                                + "  SELECT sku_code, batch_no, processed_area, SUM(unloading_qty) AS unloaded_qty "
                                + "  FROM return_stock_leakage_section "
                                + "  WHERE tenant_id = ? AND workflow_instance_id = ? AND line_type = 'BOX' "
                                + "  GROUP BY sku_code, batch_no, processed_area "
                                + ") smb ON smb.sku_code = rsoi.sku_code AND smb.batch_no = rsoi.batch_no AND smb.processed_area = qpl.processed_area AND qpl.box_qty IS NOT NULL "
                                + "LEFT JOIN ( "
                                + "  SELECT sku_code, batch_no, processed_area, SUM(unloading_qty) AS unloaded_qty "
                                + "  FROM return_stock_leakage_section "
                                + "  WHERE tenant_id = ? AND workflow_instance_id = ? AND line_type = 'PC' "
                                + "  GROUP BY sku_code, batch_no, processed_area "
                                + ") smp ON smp.sku_code = rsoi.sku_code AND smp.batch_no = rsoi.batch_no AND smp.processed_area = qpl.processed_area AND qpl.pc_qty IS NOT NULL "
                                + "WHERE qpl.tenant_id = ? AND qpl.workflow_instance_id = ? "
                                + "GROUP BY rsoi.invoice_no, dii.vendor_name, dii.rd_code, rsoi.sku_code, rsoi.sku_name "
                                + "ORDER BY rsoi.invoice_no, rsoi.sku_code",
                        tenantId, instanceId, tenantId, instanceId, tenantId, instanceId)
                .stream()
                .map(r -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("invoice_no", r.get("invoice_no"));
                    row.put("vendor_name", r.get("vendor_name"));
                    row.put("rd_code", r.get("rd_code"));
                    row.put("sku_code", r.get("sku_code"));
                    row.put("sku_name", r.get("sku_name"));
                    row.put("order_box_qty", r.get("order_box_qty"));
                    row.put("order_pc_qty", r.get("order_pc_qty"));
                    row.put("unloaded_qty", r.get("unloaded_box_qty"));
                    row.put("unloaded_pc_qty", r.get("unloaded_pc_qty"));
                    return row;
                })
                .toList();
        return wrap(content);
    }

    private List<Map<String, Object>> fetchBatchLines(Long instanceId) {
        long tenantId = TenantContext.get();
        return jdbc.queryForList(
                        "SELECT rsoi.batch_no AS batch_no, rsoi.invoice_no AS invoice_no, "
                                + "rsoi.sku_code AS sku_code, rsoi.sku_name AS sku_name, "
                                + "qpl.processed_area AS processed_area, "
                                + "qpl.box_qty AS order_box_qty, qpl.pc_qty AS order_pc_qty, "
                                // qpl is one row per BOX-or-PC line (never both) — the two LEFT
                                // JOINs are each guarded to their own row type, so at most one of
                                // smb/smp ever matches a given qpl row; COALESCE picks whichever did.
                                + "COALESCE(smb.unloaded_qty, smp.unloaded_qty, 0) AS unloaded_qty "
                                + "FROM qc_processed_location qpl "
                                + "JOIN return_stock_officer_item rsoi "
                                + "  ON rsoi.id = qpl.return_stock_officer_item_id AND rsoi.tenant_id = qpl.tenant_id "
                                + "LEFT JOIN ( "
                                + "  SELECT sku_code, batch_no, processed_area, SUM(unloading_qty) AS unloaded_qty "
                                + "  FROM return_stock_leakage_section "
                                + "  WHERE tenant_id = ? AND workflow_instance_id = ? AND line_type = 'BOX' "
                                + "  GROUP BY sku_code, batch_no, processed_area "
                                + ") smb ON smb.sku_code = rsoi.sku_code AND smb.batch_no = rsoi.batch_no AND smb.processed_area = qpl.processed_area AND qpl.box_qty IS NOT NULL "
                                + "LEFT JOIN ( "
                                + "  SELECT sku_code, batch_no, processed_area, SUM(unloading_qty) AS unloaded_qty "
                                + "  FROM return_stock_leakage_section "
                                + "  WHERE tenant_id = ? AND workflow_instance_id = ? AND line_type = 'PC' "
                                + "  GROUP BY sku_code, batch_no, processed_area "
                                + ") smp ON smp.sku_code = rsoi.sku_code AND smp.batch_no = rsoi.batch_no AND smp.processed_area = qpl.processed_area AND qpl.pc_qty IS NOT NULL "
                                + "WHERE qpl.tenant_id = ? AND qpl.workflow_instance_id = ? "
                                + "ORDER BY rsoi.batch_no, qpl.processed_area",
                        tenantId, instanceId, tenantId, instanceId, tenantId, instanceId)
                .stream()
                .map(r -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("batch_no", r.get("batch_no"));
                    row.put("invoice_no", r.get("invoice_no"));
                    row.put("sku_code", r.get("sku_code"));
                    row.put("sku_name", r.get("sku_name"));
                    row.put("processed_area", r.get("processed_area"));
                    row.put("order_box_qty", r.get("order_box_qty"));
                    row.put("order_pc_qty", r.get("order_pc_qty"));
                    row.put("unloaded_qty", r.get("unloaded_qty"));
                    return row;
                })
                .toList();
    }

    private static BigDecimal asDecimal(Object v) {
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    private DataSourceSpec.Response wrap(List<Map<String, Object>> content) {
        return DataSourceSpec.Response.builder()
                .content(content)
                .totalElements(content.size())
                .totalPages(1)
                .number(0)
                .size(PAGE_SIZE)
                .build();
    }
}
