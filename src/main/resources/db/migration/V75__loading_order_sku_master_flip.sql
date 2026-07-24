-- V75 — aria:loading-order: flip Master-Detail grouping.
-- Was: Master = OrderHeader (per order), Lines = OrderDispatchDetail by order_id (SKUs in that order).
-- Now: Master = SKU (per route, summed across orders), Lines = OrderDispatchDetail by sku_code (orders needing that SKU).

-- 1. Aggregate view: one row per (route, SKU), summed across all orders on that route.
CREATE OR REPLACE VIEW v_route_sku_summary AS
SELECT MIN(id) AS id, tenant_id, route_id, sku_code,
       MAX(sku_desc) AS sku_desc,
       COUNT(DISTINCT order_id) AS order_count,
       SUM(line_order_qty) AS order_qty,
       SUM(line_dispatch_qty) AS dispatch_qty,
       SUM(line_remaining_qty) AS remaining_qty
FROM m5_order_dispatch_detail
GROUP BY tenant_id, route_id, sku_code;

-- 2. Register entity for the picker.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'RouteSkuSummary', 'Route SKU Summary', 'v_route_sku_summary',
       'One row per SKU per route, summed across all orders on that route.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='RouteSkuSummary');

INSERT INTO entity_field_registry
  (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'RouteSkuSummary' b,'route_id' c,'Route' d,'STRING' e,1 f,1 g,1 h,0 i,'text' j UNION ALL
  SELECT -1,'RouteSkuSummary','sku_code','SKU','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'RouteSkuSummary','sku_desc','SKU Name','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'RouteSkuSummary','order_count','Orders','INT',0,1,0,0,'number' UNION ALL
  SELECT -1,'RouteSkuSummary','order_qty','Order Qty','INT',0,1,0,0,'number' UNION ALL
  SELECT -1,'RouteSkuSummary','dispatch_qty','Dispatched','INT',0,1,0,0,'number' UNION ALL
  SELECT -1,'RouteSkuSummary','remaining_qty','Remaining','INT',0,1,0,0,'number'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='RouteSkuSummary');

-- 3. Flip aria:loading-order: master = SKU (per route), lines = orders needing that SKU.
UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
   SET pv.body_json = '{"kind": "layout", "schemaVersion": 2, "layout": [{"kind": "MasterDetailList", "title": "Loading Order", "subtitle": "Fetch by route — each SKU expands to the orders needing it. Pick an offer per line, then Start Loading.", "dataSource": {"type": "entity", "entity": "RouteSkuSummary", "filter": {"field": "route_id", "op": "eq", "value": "route:route"}}, "card": {"titleField": "sku_code", "titlePrefix": "SKU ", "statusField": "sku_desc", "selectable": true, "summary": [{"field": "sku_desc", "label": "SKU Name"}, {"field": "order_count", "label": "Orders"}, {"field": "order_qty", "label": "Order Qty"}, {"field": "dispatch_qty", "label": "Dispatched"}]}, "lines": {"entity": "OrderDispatchDetail", "fkField": "sku_code", "masterKey": "sku_code", "columns": [{"field": "order_id", "label": "Order ID"}, {"field": "rd_desc", "label": "Customer"}, {"field": "line_order_qty", "label": "Order Qty"}, {"field": "line_dispatch_qty", "label": "Dispatch Qty"}, {"field": "line_remaining_qty", "label": "Remaining"}]}}]}'
 WHERE pd.code = 'aria:loading-order' AND pv.status = 'PUBLISHED';
