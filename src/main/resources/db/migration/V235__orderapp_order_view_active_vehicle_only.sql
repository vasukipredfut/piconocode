-- V235 — v_orderapp_order (backing the OrderAppOrder entity, see V52) had no
-- vehicle_status filter at all: it aggregated EVERY m5_order_dispatch_detail
-- row for an order_id, active or not. aria:loading-allocation's "OrderApp
-- Order #" picker filters this view by vehicle_no (V73) — so if the same
-- physical vehicle registration returns on a later, unrelated trip, its
-- picker would ALSO surface the earlier trip's now-stale order (whether that
-- earlier import already got flipped to 'inactive' on gate-out, per
-- OrderDispatchDetailImportController#gateOut, or was simply never re-scoped
-- to the new trip). Restricting the inner per-(order_id, sku_code) aggregate
-- to vehicle_status='active' rows before grouping means only the CURRENT
-- trip's import ever contributes to this view — a vehicle's earlier,
-- already-superseded order rows drop out of consideration entirely, the same
-- way they already drop out of m5_order_dispatch_detail's own active-row
-- semantics.

CREATE OR REPLACE VIEW v_orderapp_order AS
SELECT MIN(line.min_id) AS id, line.tenant_id, line.order_id AS order_ref,
       MAX(line.rd_desc) AS customer_name, MAX(line.route_id) AS customer_route,
       COUNT(*) AS sku_lines, SUM(line.sku_cases) AS ordered_cases,
       ROUND(SUM(line.sku_cases * COALESCE(s.carton_weight_kg,0)),3) AS ordered_weight_kg,
       MAX(line.dispatch_wgt) AS sheet_dispatch_wt,
       MAX(line.status) AS status, MAX(line.vehicle_no) AS vehicle_no
FROM (
  SELECT MIN(id) AS min_id, tenant_id, order_id, sku_code,
         MAX(rd_desc) AS rd_desc, MAX(route_id) AS route_id,
         SUM(line_order_qty) AS sku_cases,
         MAX(status) AS status, MAX(vehicle_no) AS vehicle_no, MAX(dispatch_wgt) AS dispatch_wgt
  FROM m5_order_dispatch_detail
  WHERE vehicle_status = 'active'
  GROUP BY tenant_id, order_id, sku_code
) line
LEFT JOIN m5_sku_master s
       ON s.sku_code = line.sku_code COLLATE utf8mb4_unicode_ci
      AND s.tenant_id = line.tenant_id
GROUP BY line.tenant_id, line.order_id;
