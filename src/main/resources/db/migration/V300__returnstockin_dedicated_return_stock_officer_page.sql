-- V300: Correct M10.ReturnStockIn's Return Stock Officer step -- it was
-- mistakenly mapped to reuse aria:dispatch-vehicle-details a 2nd time
-- (per V296), which meant no distinct queue for the Return Stock Officer
-- role and the return_stock_items grid appearing on the wrong page/stage.
--
-- This migration:
--   1. Removes return_stock_items from aria:dispatch-vehicle-details (it no
--      longer belongs there -- that page is still used, unmodified
--      otherwise, for DRAFT -> DISPATCH_QUEUED across every M10 flow
--      variant).
--   2. Publishes a real body for aria:return-stock-officer (an
--      orphaned/unused page per V296's comment -- created here if it
--      doesn't already exist) with its own dedicated queue (automatically
--      scoped to M10.ReturnStockIn / WEIGHMENT_1 by virtue of being a page
--      code used by exactly one transition -- see
--      WorkflowEngineService#statesForPage, the same pattern V296 used for
--      aria:unloading-progress / aria:stock-checking-officer), read-only
--      vehicle summary fields (vehicleNo, priority_override, loadingType),
--      and the return_stock_items grid (Batch No / SKU Name / Qty / Good
--      Stock Qty / Expiry Stock Qty).
--   3. Repoints the WEIGHMENT_1 -> RETURN_STOCK_OFFICER transition's
--      pageCode from aria:dispatch-vehicle-details to
--      aria:return-stock-officer.
--
-- Paired Java change (same intent, this migration):
--   ReturnStockOfficerItemTransitionListener.PAGE_CODE is now
--   aria:return-stock-officer (was aria:dispatch-vehicle-details).
--
-- No other flow variant (TankerInbound/TankerOutbound/PackLoading/
-- PackUnload/ThirdPartyIn/ThirdPartyOut) or workflow is touched -- the
-- return_stock_items field only ever existed on M10.ReturnStockIn's
-- conditional branch of the shared page.

-- 1. Remove return_stock_items from aria:dispatch-vehicle-details.
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_REMOVE(
  pv.body_json,
  REPLACE(
    JSON_UNQUOTE(JSON_SEARCH(pv.body_json, 'one', 'return_stock_items', NULL, '$.layout[*].fields[*].name')),
    '.name', ''
  )
)
WHERE pd.code = 'aria:dispatch-vehicle-details' AND pv.status = 'PUBLISHED'
  AND JSON_SEARCH(pv.body_json, 'one', 'return_stock_items', NULL, '$.layout[*].fields[*].name') IS NOT NULL;

-- 2. Ensure aria:return-stock-officer's page_definition row exists.
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:return-stock-officer', 'Return Stock Officer',
  'Record Batch/SKU/Qty and Good vs Expiry stock for a Return Stock In vehicle, after First Weighment and before QC Approval.',
  'M10', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:return-stock-officer');

-- 3. Publish the real body as the next version for that page (covers the
--    orphaned-with-no-version case and the overwrite-stale-body case alike).
INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id,
  COALESCE((SELECT MAX(version) FROM page_version WHERE page_id = pd.id), 0) + 1,
  'PUBLISHED',
  '{"kind":"layout","schemaVersion":2,"entity":"GatePass","dataSources":[{"id":"returnStockQueue","kind":"rest","method":"GET","url":"/api/workflow/queue-for-page?pageCode=aria:return-stock-officer&size=200"}],"layout":[{"kind":"Section","title":"Dispatch Queue (Open)","fields":[{"kind":"WorkflowQueueList","persona":"ReturnStockOfficer","dataSource":"returnStockQueue","pageSize":25,"cardFields":[],"fillFormOnSelect":true,"priorityShading":true,"secondLineFields":[{"field":"gpNumber","label":""},{"field":"vehicleNo"}],"fillLookupEntity":"GatePass","fillLookupField":"gp_number","filterFields":[{"field":"vehicleNo","label":"Vehicle No"},{"field":"gpNumber","label":"GP Number"}],"priorityOverrideField":"priority_override"}]},{"kind":"Section","title":"Vehicle Details","fields":[{"kind":"FormField","type":"text","name":"currentUserId","defaultValue":"{{user.username}}","hidden":true},{"kind":"FormField","type":"select","name":"gpNumber","label":"GP Number","required":true,"placeholder":"Select Gate Pass","freeSolo":true,"scannable":true,"optionsSource":{"type":"datasource","valueField":"gp_number","labelField":"gp_number","spec":{"type":"entity","entity":"GatePass","select":["id","gp_number","vehicle_no","flow_variant_code","loading_type","workflow_instance_id"],"filter":{"field":"flow_variant_code","op":"eq","value":"M10.ReturnStockIn"},"sort":[{"field":"gp_number","dir":"desc"}]}},"onChangeFill":{"vehicleNo":"vehicle_no","loadingType":"loading_type","instanceId":"workflow_instance_id"}},{"kind":"FormField","type":"text","name":"vehicleNo","label":"Vehicle Number","disabled":true},{"kind":"FormField","name":"priority_override","type":"text","label":"Priority Override","readOnly":true},{"kind":"FormField","type":"text","name":"loadingType","label":"Vehicle Movement Type","disabled":true}]},{"kind":"Section","title":"Return Stock Details","subtitle":"Batch / SKU / Qty / Good & Expiry Stock -- captured after First Weighment, before QC Approval.","fields":[{"kind":"RepeatingGroup","name":"return_stock_items","label":"Batch / SKU / Qty / Good & Expiry Stock","minRows":1,"defaultRows":1,"columns":[{"name":"batch_no","label":"Batch No","type":"text","required":true,"width":"1fr","plainBackground":true},{"name":"sku_name","label":"SKU Name","type":"text","required":true,"width":"1.4fr","plainBackground":true},{"name":"qty","label":"Qty","type":"number","required":true,"width":"0.8fr","plainBackground":true},{"name":"good_stock_qty","label":"Good Stock Qty","type":"number","width":"1fr","plainBackground":true},{"name":"expiry_stock_qty","label":"Expiry Stock Qty","type":"number","width":"1fr","plainBackground":true}],"rowActions":[{"kind":"Button","label":"🗑️ Delete","action":"repeatingGroup.deleteRow","color":"error","confirmBeforeAction":true}],"footerActions":[{"kind":"Button","label":"+ Add More","action":"repeatingGroup.addRow","color":"secondary"}]},{"kind":"WorkflowActionButton","label":"Submit Return Stock Details","action":"workflow.transition","instanceId":"{{form.instanceId}}","color":"primary","visibleWhen":"#form.gpNumber != null && #form.gpNumber != ''''"}]}]}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'aria:return-stock-officer';

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'aria:return-stock-officer'
  AND pv.version = (SELECT MAX(v2.version) FROM page_version v2 WHERE v2.page_id = pd.id);

-- 4. Repoint the WEIGHMENT_1 -> RETURN_STOCK_OFFICER transition's pageCode.
UPDATE workflow_definition
SET body_json = JSON_REPLACE(
  body_json,
  REPLACE(
    JSON_UNQUOTE(JSON_SEARCH(body_json, 'one', 'RETURN_STOCK_OFFICER', NULL, '$.transitions[*].to')),
    '.to', '.pageCode'
  ),
  'aria:return-stock-officer'
)
WHERE code = 'M10.ReturnStockIn' AND status = 'PUBLISHED' AND tenant_id = 1
  AND JSON_SEARCH(body_json, 'one', 'RETURN_STOCK_OFFICER', NULL, '$.transitions[*].to') IS NOT NULL;
