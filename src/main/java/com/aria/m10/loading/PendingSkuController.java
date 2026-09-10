package com.aria.m10.loading;

import com.aria.datasource.DataSourceSpec;
import com.aria.security.TenantContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code GET /api/m10/loading/{vehicleNo}/pending-skus} — read-only lookup:
 * for the given vehicle's currently OPEN loading transaction, which SKU
 * codes still have processed short of their target. Each content item
 * carries {@code sku_code}, {@code sku_desc}, {@code order_qty},
 * {@code processed_qty}, and {@code pending_routes} — a comma-separated
 * list of the {@code route_id}s (a SKU can span more than one route/order
 * line for the same vehicle) whose OWN target is not yet fully processed.
 * {@code order_qty}/{@code processed_qty} are scoped to those SAME pending
 * route(s) only, NOT the SKU's grand total across every route — a route
 * where this SKU is already done contributes to neither the quantities nor
 * {@code pending_routes}, so the two stay consistent with each other.
 *
 * <p><b>Aggregation grain is {@code (route_id, sku_code)}, not raw
 * {@code loading_progress_detail_bay} rows.</b> One route/SKU pair is one
 * order line with ONE fixed target quantity — but {@code M10StartBayActionHandler}
 * inserts a brand new {@code loading_progress_detail_bay} row every time
 * "Start Bay" is called for that line (e.g. a retried/duplicate scan), each
 * carrying that same target again. Naively {@code SUM(b.quantity)} across
 * every such row for a SKU double/triple-counts the target once per repeat
 * row (observed live: 3 duplicate rows of quantity=200 summed to a reported
 * order_qty of 600 for a real target of 200). This query instead takes
 * {@code MAX(b.quantity)} per {@code (route_id, sku_code)} — collapsing any
 * number of repeat rows for the same line back down to its one true target —
 * then sums across distinct lines per SKU code (a vehicle can genuinely have
 * the same SKU on more than one route/order line; those really should add
 * up). {@code processed_quantity} is still summed across every row
 * regardless of grain, since physical progress accumulates across repeated
 * bay attempts and must not be deduped the same way.
 *
 * <p>{@code sku_desc} is taken from whichever row happens to win
 * {@code MAX()} — bay entries for the same SKU are expected to carry the
 * same description.
 *
 * <p>Wrapped in {@link DataSourceSpec.Response} — the existing
 * "frontend-friendly shape mirroring Spring's Page" envelope this repo
 * already uses for {@code POST /api/m0/datasource/query} results — rather
 * than inventing a one-off shape. No real pagination: a vehicle's pending
 * SKUs is always a small set, so this is always a single page (size 500,
 * page 0) covering every result.
 *
 * <p>Bespoke SQL because the generic {@code POST /api/m0/datasource/query}
 * filter DSL cannot express either requirement this needs: a column-vs-
 * column comparison (only column-vs-literal/context bindings are supported,
 * see {@code DataSourceQueryService}'s filter compiler) or a SUM aggregate
 * (its groupBy path is top-1-per-group by MAX(id), not SUM).
 *
 * <p>Scoped to the vehicle's OPEN {@code workflow_instance} — {@code
 * vehicle_no} is not unique on {@code loading_progress_detail}; the same
 * physical vehicle gets a new row on every future visit. An unscoped query
 * would mix unrelated historical loading transactions together.
 *
 * <p>{@link #skuStatus} is the sibling endpoint: same scope and grouping,
 * collapsed to a single boolean for the whole vehicle rather than a per-SKU
 * list.
 */
@RestController
@RequestMapping("/api/m10/loading")
public class PendingSkuController {

    private static final int PAGE_SIZE = 500;

    private final JdbcTemplate jdbc;

    public PendingSkuController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/{vehicleNo}/pending-skus")
    public DataSourceSpec.Response pendingSkus(@PathVariable String vehicleNo) {
        long tenantId = TenantContext.get();
        List<Map<String, Object>> content = jdbc.queryForList(
                "WITH line_targets AS ( "
                        + "SELECT b.route_id, b.sku_code, MAX(b.sku_desc) AS sku_desc, "
                        + "MAX(b.quantity) AS target_qty, SUM(COALESCE(b.processed_quantity, 0)) AS processed_qty "
                        + "FROM loading_progress_detail_bay b "
                        + "JOIN loading_progress_detail d ON d.workflow_instance_id = b.workflow_instance_id "
                        + "AND d.tenant_id = b.tenant_id "
                        + "JOIN workflow_instance wi ON wi.id = d.workflow_instance_id AND wi.tenant_id = d.tenant_id "
                        + "WHERE b.tenant_id = ? AND d.vehicle_no = ? AND wi.status = 'OPEN' "
                        + "GROUP BY b.route_id, b.sku_code) "
                        + "SELECT sku_code, MAX(sku_desc) AS sku_desc, "
                        + "SUM(CASE WHEN processed_qty < target_qty THEN target_qty ELSE 0 END) AS order_qty, "
                        + "SUM(CASE WHEN processed_qty < target_qty THEN processed_qty ELSE 0 END) AS processed_qty, "
                        + "GROUP_CONCAT(CASE WHEN processed_qty < target_qty THEN route_id END "
                        + "ORDER BY route_id SEPARATOR ', ') AS pending_routes "
                        + "FROM line_targets "
                        + "GROUP BY sku_code "
                        + "HAVING SUM(CASE WHEN processed_qty < target_qty THEN 1 ELSE 0 END) > 0 "
                        + "ORDER BY sku_code",
                tenantId, vehicleNo)
                .stream()
                .map(r -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sku_code", r.get("sku_code"));
                    row.put("sku_desc", r.get("sku_desc"));
                    row.put("order_qty", r.get("order_qty"));
                    row.put("processed_qty", r.get("processed_qty"));
                    row.put("pending_routes", r.get("pending_routes"));
                    return row;
                })
                .toList();

        return DataSourceSpec.Response.builder()
                .content(content)
                .totalElements(content.size())
                .totalPages(1)
                .number(0)
                .size(PAGE_SIZE)
                .build();
    }

    /**
     * {@code GET /api/m10/loading/{vehicleNo}/sku-status} — collapses
     * {@link #pendingSkus}'s per-SKU comparison into one boolean for the
     * whole vehicle: {@code true} only if every SKU's summed-per-line target
     * (same {@code (route_id, sku_code)} dedup as {@link #pendingSkus} —
     * see its Javadoc) for the vehicle's OPEN loading transaction has
     * {@code processed == target} (including the vacuous case of no bay
     * rows at all — nothing pending). {@code false} if at least one SKU
     * still has processed short of (or over) its order total.
     */
    @GetMapping("/{vehicleNo}/sku-status")
    public boolean skuStatus(@PathVariable String vehicleNo) {
        long tenantId = TenantContext.get();
        Integer incompleteSkuCount = jdbc.queryForObject(
                "WITH line_targets AS ( "
                        + "SELECT b.route_id, b.sku_code, "
                        + "MAX(b.quantity) AS target_qty, SUM(COALESCE(b.processed_quantity, 0)) AS processed_qty "
                        + "FROM loading_progress_detail_bay b "
                        + "JOIN loading_progress_detail d ON d.workflow_instance_id = b.workflow_instance_id "
                        + "AND d.tenant_id = b.tenant_id "
                        + "JOIN workflow_instance wi ON wi.id = d.workflow_instance_id AND wi.tenant_id = d.tenant_id "
                        + "WHERE b.tenant_id = ? AND d.vehicle_no = ? AND wi.status = 'OPEN' "
                        + "GROUP BY b.route_id, b.sku_code) "
                        + "SELECT COUNT(*) FROM ( "
                        + "SELECT sku_code FROM line_targets "
                        + "GROUP BY sku_code "
                        + "HAVING SUM(processed_qty) <> SUM(target_qty) "
                        + ") incomplete",
                Integer.class, tenantId, vehicleNo);
        return incompleteSkuCount == null || incompleteSkuCount == 0;
    }
}
