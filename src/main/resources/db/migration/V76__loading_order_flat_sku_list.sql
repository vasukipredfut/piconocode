-- V76 — aria:loading-order: replace the V75 MasterDetailList (SKU card ->
-- expand to see orders) with a plain flat ListView. No expand interaction:
-- one row per SKU (already deduped/summed by route via v_route_sku_summary,
-- from V75), columns SKU / SKU Name / Order Qty / Dispatch Qty / Balance.
-- The V75 aggregate view + RouteSkuSummary entity are kept — still exactly
-- what this flat list needs.

UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
   SET pv.body_json = '{"kind": "list", "title": "Loading Order", "subtitle": "SKU-wise totals for this route — one row per SKU, summed across all its orders.", "dataSource": {"type": "entity", "entity": "RouteSkuSummary", "filter": {"field": "route_id", "op": "eq", "value": "route:route"}}, "columns": [{"field": "sku_code", "label": "SKU", "sortable": true}, {"field": "sku_desc", "label": "SKU Name"}, {"field": "order_qty", "label": "Order Qty", "render": "number"}, {"field": "dispatch_qty", "label": "Dispatch Qty", "render": "number"}, {"field": "remaining_qty", "label": "Balance", "render": "number"}], "pagination": {"pageSize": 50}}'
 WHERE pd.code = 'aria:loading-order' AND pv.status = 'PUBLISHED';
