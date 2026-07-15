-- V131 — author pico:loading-order-sheet (M10 loading step) as a real page.
--
-- The loading step composes from existing primitives, no new code: a ListView
-- on SalesOrderLine with a route_id filter ("Enter Route ID -> Fetch Order"),
-- offer inline-editable per row (click a row / cell -> Offer Selection ->
-- Update), and a Start-Loading WorkflowActionButton (-> LOADING_IN_PROGRESS).
-- Reached mid-flow with the vehicle's workflow instanceId.
--
-- Redis note: a running instance caches m0:page:<tenant>:<code> — evict it
-- (publish flow / restart / redis DEL) to serve the new body; fresh DBs apply
-- cleanly.

UPDATE page_version pv
SET pv.body_json = '{"schemaVersion": "1.0.0", "slug": "pico:loading-order-sheet", "title": "Loading \\u2014 Order Sheet", "status": "published", "version": 1, "roles": ["Dispatch", "UnloadingSupervisor", "TenantAdmin"], "dataSources": [], "streams": [], "layout": {"type": "Grid", "id": "root", "props": {"columns": 12, "gap": 16}, "children": [{"type": "Section", "id": "sec-guide", "props": {"title": "Loading Order Sheet", "span": 12, "subtitle": "Filter the order lines by Route ID, set the Offer on each line (click a row to edit), then Start Loading for the vehicle."}, "children": []}, {"type": "Section", "id": "sec-orders", "props": {"title": "Order Details", "span": 12}, "children": [{"type": "ListView", "id": "order-lines", "props": {"dataSource": {"type": "entity", "entity": "SalesOrderLine"}, "filters": [{"field": "route_id", "label": "Route ID", "type": "text-contains", "applyTo": ["route_id"]}], "columns": [{"field": "so_id", "label": "Order ID", "sortable": true}, {"field": "item_name", "label": "SKU Name"}, {"field": "item_code", "label": "SKU Code"}, {"field": "sku_barcode", "label": "SKU Barcode"}, {"field": "qty_ordered", "label": "Qty", "render": "number"}, {"field": "offer", "label": "Offer"}], "pageSize": 25}}]}, {"type": "Section", "id": "sec-start", "props": {"title": "Start Loading", "span": 12, "subtitle": "Once offers are set on every line, begin the loading process for this vehicle."}, "children": [{"type": "WorkflowActionButton", "id": "btn-start", "props": {"label": "Start Loading", "action": "workflow.transition", "targetState": "LOADING_IN_PROGRESS", "trigger": "page.submitted", "color": "primary"}}]}]}}'
WHERE pv.page_id = (SELECT id FROM page_definition WHERE code='pico:loading-order-sheet')
  AND pv.status = 'PUBLISHED';
