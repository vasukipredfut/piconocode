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
 * put-away location ({@code qc_processed_location.box_qty}, the "order"
 * quantity for that batch/bin) compare against what
 * {@code ReturnStockUnloadingStockPostingListener} actually posted for it at
 * {@code UNLOADING_COMPLETED} ({@code m5_stock_movement}, {@code ref_type='DSP'},
 * {@code ref_id=<instanceId>}, summed per {@code (item_code, lot_no, to_bin)},
 * BOX lines only — a piece-count shortfall isn't tracked at this stage, same
 * scope as the page's own concrete example: "box quantity is 5 but there are
 * only 4 boxes").
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
 * query for two views.
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

    private List<Map<String, Object>> fetchBatchLines(Long instanceId) {
        long tenantId = TenantContext.get();
        return jdbc.queryForList(
                        "SELECT rsoi.batch_no AS batch_no, rsoi.sku_code AS sku_code, rsoi.sku_name AS sku_name, "
                                + "qpl.processed_area AS processed_area, "
                                + "qpl.box_qty AS order_box_qty, qpl.pc_qty AS order_pc_qty, "
                                + "COALESCE(sm.unloaded_qty, 0) AS unloaded_qty "
                                + "FROM qc_processed_location qpl "
                                + "JOIN return_stock_officer_item rsoi "
                                + "  ON rsoi.id = qpl.return_stock_officer_item_id AND rsoi.tenant_id = qpl.tenant_id "
                                + "LEFT JOIN ( "
                                + "  SELECT item_code, lot_no, to_bin, SUM(qty) AS unloaded_qty "
                                + "  FROM m5_stock_movement "
                                + "  WHERE tenant_id = ? AND ref_type = 'DSP' AND ref_id = ? AND uom = 'BOX' "
                                + "  GROUP BY item_code, lot_no, to_bin "
                                + ") sm ON sm.item_code = rsoi.sku_code AND sm.lot_no = rsoi.batch_no AND sm.to_bin = qpl.processed_area "
                                + "WHERE qpl.tenant_id = ? AND qpl.workflow_instance_id = ? "
                                + "ORDER BY rsoi.batch_no, qpl.processed_area",
                        tenantId, instanceId, tenantId, instanceId)
                .stream()
                .map(r -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("batch_no", r.get("batch_no"));
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
