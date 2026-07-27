-- Admin pages for the conveyor scanner/PLC setup (com.aria.conveyor): lets an
-- operator create WorkflowIntegrationBinding + DispatchTask rows from the
-- browser instead of curl/Postman. Mirrors exactly what
-- POST /api/m0/designer/pages produces for a plain "kind":"list" page
-- (verified live against a dev boot before being migrated here).
-- category='M11' — grouped with the Integration Hub these entities belong to.
--
-- Idempotent (NOT EXISTS guards) so this is a no-op on a DB that already has
-- these rows from the live Designer-API call used during development.

INSERT INTO page_definition (tenant_id, code, title, category, status)
SELECT * FROM (
  SELECT 1 tid, 'aria:conveyor-scanner-plc-bindings' code, 'Conveyor Scanner/PLC Bindings' title, 'M11' cat, 'PUBLISHED' st
  UNION ALL SELECT 1, 'm12:conveyor-dispatch-tasks', 'Conveyor Dispatch Tasks', 'M11', 'PUBLISHED'
) x
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = x.tid AND code = x.code);

INSERT INTO page_version (page_id, version, body_json, status, published_at)
SELECT pd.id, 1,
  '{"kind":"list","dataSource":{"type":"entity","entity":"WorkflowIntegrationBinding"},"columns":[{"field":"workflow_code","label":"Workflow Code","sortable":true},{"field":"step_code","label":"Step Code","sortable":true},{"field":"bay_code","label":"Bay Code","sortable":true},{"field":"integration_role","label":"Integration Role","sortable":true},{"field":"integration_id","label":"Integration","sortable":true}],"pageSize":25}',
  'PUBLISHED', NOW()
FROM page_definition pd
WHERE pd.tenant_id = 1 AND pd.code = 'aria:conveyor-scanner-plc-bindings'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

INSERT INTO page_version (page_id, version, body_json, status, published_at)
SELECT pd.id, 1,
  '{"kind":"list","dataSource":{"type":"entity","entity":"DispatchTask"},"columns":[{"field":"workflow_code","label":"Workflow Code","sortable":true},{"field":"bay_code","label":"Bay Code","sortable":true},{"field":"task_code","label":"Task Code","sortable":true},{"field":"order_ref","label":"Order Ref","sortable":true},{"field":"sku_code","label":"SKU","sortable":true},{"field":"target_qty","label":"Target Qty","sortable":true},{"field":"processed_qty","label":"Processed Qty","sortable":true},{"field":"rejected_qty","label":"Rejected Qty","sortable":true},{"field":"status","label":"Status","sortable":true}],"pageSize":25}',
  'PUBLISHED', NOW()
FROM page_definition pd
WHERE pd.tenant_id = 1 AND pd.code = 'aria:conveyor-dispatch-tasks'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.code IN ('aria:conveyor-scanner-plc-bindings', 'aria:conveyor-dispatch-tasks')
  AND pd.tenant_id = 1
  AND pd.current_version_id IS NULL;
