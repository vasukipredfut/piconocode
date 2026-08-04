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
 * codes still have {@code processed_quantity < quantity} (summed across
 * every {@code loading_progress_detail_bay} row for that SKU — a vehicle
 * can load the same SKU across more than one bay session; see
 * {@code M10StartBayActionHandler}/{@code M10BayCompleteActionHandler}).
 * Response is SKU codes only — the totals drive the comparison but aren't
 * returned.
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
        // total_order_quantity/processed_quantity drive the HAVING comparison
        // but aren't projected — the response returns sku_code only.
        List<Map<String, Object>> content = jdbc.queryForList(
                "SELECT b.sku_code AS sku_code "
                        + "FROM loading_progress_detail_bay b "
                        + "JOIN loading_progress_detail d ON d.workflow_instance_id = b.workflow_instance_id "
                        + "AND d.tenant_id = b.tenant_id "
                        + "JOIN workflow_instance wi ON wi.id = d.workflow_instance_id AND wi.tenant_id = d.tenant_id "
                        + "WHERE b.tenant_id = ? AND d.vehicle_no = ? AND wi.status = 'OPEN' "
                        + "GROUP BY b.sku_code "
                        + "HAVING SUM(COALESCE(b.processed_quantity, 0)) < SUM(b.quantity) "
                        + "ORDER BY b.sku_code",
                tenantId, vehicleNo)
                .stream()
                .map(r -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("sku_code", r.get("sku_code"));
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
}
