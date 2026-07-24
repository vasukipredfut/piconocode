-- V73 — aria:loading-allocation: scope the "OrderApp Order #" (orderRef)
-- dropdown to the vehicle_no already picked in the form, instead of listing
-- every OrderAppOrder row. Uses the existing "pageData:" binding prefix
-- (DataSourceQueryService.resolveBinding — no new backend code needed).
--
-- CAVEAT: this only filters live if the frontend Renderer actually sends
-- the current form's vehicle_no under bindings key "pageData:vehicle_no"
-- (or plain "vehicle_no", which the resolver falls back to) when it queries
-- this field's optionsSource. No other page in this codebase currently uses
-- the pageData: prefix, so this is unverified end-to-end — if the dropdown
-- still shows unfiltered orders in the browser, the fix needs to happen on
-- the frontend side (earmsfrontend), not here.

UPDATE page_version pv
  JOIN page_definition pd ON pd.id = pv.page_id
   SET pv.body_json = REPLACE(pv.body_json,
     '"optionsSource": {"type": "entity", "entity": "OrderAppOrder", "valueField": "order_ref", "labelField": "order_ref"}',
     '"optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "OrderAppOrder", "filter": {"field": "vehicle_no", "op": "eq", "value": "pageData:vehicle_no"}, "select": ["order_ref"], "sort": [{"field": "order_ref", "dir": "asc"}]}, "valueField": "order_ref", "labelField": "order_ref"}')
 WHERE pd.code = 'aria:loading-allocation' AND pv.status = 'PUBLISHED';
