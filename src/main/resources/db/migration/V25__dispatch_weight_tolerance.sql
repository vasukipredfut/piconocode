-- V25 — Dispatch weight tolerance (the two "future" items, now built):
--   (1) Weight routing — the weighment form stores each stage's reading in its
--       own column (w1/w2/w3_weight_kg) instead of a single blended field, and
--       the outbound projections map them through.
--   (2) PMT 0.4% hard auto-block — the SYSTEM computes loaded (W1-W2) vs
--       expected (Σ order_qty × SKU unit_weight_kg over the route) and stamps a
--       within-tolerance verdict onto the gate pass. The PMT step's guard blocks
--       on that machine verdict; over-tolerance routes to camera + manager.
--       Arithmetic is in DispatchToleranceListener (Java) because the guard
--       evaluator's SimpleEvaluationContext sandbox forbids it in SpEL.
--
-- Fully generic + data-driven: any tenant enables the check on any dispatch
-- projection by setting m0_workflow_projection.tolerance_pct. No customer code.

-- ===== schema + seed + tolerance config =====
-- 1. SKU unit weight (kg per ordered piece) — basis for expected dispatch weight.
ALTER TABLE m5_sku_master ADD COLUMN IF NOT EXISTS unit_weight_kg DECIMAL(10,3) NULL COMMENT 'weight of one sellable/ordered unit incl packaging (kg)';

-- 2. Seed the 5 edible-oil SKUs the seeded dispatch orders reference (idempotent).
INSERT INTO m5_sku_master (tenant_id, sku_code, brand, product_family, pack_size_ml, pack_pieces_per_case, carton_weight_kg, unit_weight_kg, hsn_code, gst_rate_pct, uom_primary)
SELECT * FROM (
  SELECT 1 AS a,'SFO-1L-PCH' AS b,'Sunraja' AS c,'Sunflower' AS d,1000 AS e,12 AS f,11.100 AS g,0.925 AS h,'15121910' AS i,5.00 AS j,'L' AS k UNION ALL
  SELECT 1,'SFO-5L-CAN','Sunraja','Sunflower',5000,4,19.200,4.800,'15121910',5.00,'L' UNION ALL
  SELECT 1,'SFO-15L-TIN','Sunraja','Sunflower',15000,1,14.650,14.650,'15121910',5.00,'L' UNION ALL
  SELECT 1,'GNO-1L-PCH','Sunraja','Groundnut',1000,12,11.100,0.925,'15081000',5.00,'L' UNION ALL
  SELECT 1,'GNO-5L-CAN','Sunraja','Groundnut',5000,4,19.200,4.800,'15081000',5.00,'L'
) s
WHERE NOT EXISTS (SELECT 1 FROM m5_sku_master m WHERE m.sku_code=s.b AND m.tenant_id=s.a);

-- 2b. In case the SKUs already existed (207-SKU seed) but lacked a unit weight, backfill just these.
UPDATE m5_sku_master SET unit_weight_kg=0.925 WHERE sku_code IN ('SFO-1L-PCH','GNO-1L-PCH') AND unit_weight_kg IS NULL;
UPDATE m5_sku_master SET unit_weight_kg=4.800 WHERE sku_code IN ('SFO-5L-CAN','GNO-5L-CAN') AND unit_weight_kg IS NULL;
UPDATE m5_sku_master SET unit_weight_kg=14.650 WHERE sku_code='SFO-15L-TIN' AND unit_weight_kg IS NULL;

-- 3. Register the SKU field so it shows in the SKU master page + designer.
INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+1,-1,'SkuMaster','unit_weight_kg','Unit Weight (kg)','DECIMAL',0,1,0,0,'number'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SkuMaster' AND field_code='unit_weight_kg');

-- 4. Gate-pass columns the system stamps: expected vs loaded, variance, verdict.
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS expected_weight_kg  DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loaded_weight_kg    DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS pmt_variance_pct    DECIMAL(8,3)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS pmt_within_tolerance VARCHAR(8)   NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1,'GatePass',c.fc,c.lbl,c.dt,0,1,0,0,c.wk
FROM (SELECT 'expected_weight_kg' fc,'Expected Weight (kg)' lbl,'DECIMAL' dt,'number' wk
      UNION ALL SELECT 'loaded_weight_kg','Loaded Weight W1-W2 (kg)','DECIMAL','number'
      UNION ALL SELECT 'pmt_variance_pct','PMT Variance %','DECIMAL','number'
      UNION ALL SELECT 'pmt_within_tolerance','PMT Within Tolerance','STRING','text') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code=c.fc);

-- 5. Tolerance config on the projection: 0.4% for the outbound dispatch flows.
ALTER TABLE m0_workflow_projection ADD COLUMN IF NOT EXISTS tolerance_pct DECIMAL(6,3) NULL COMMENT 'PMT weight tolerance %; NULL = check disabled';
UPDATE m0_workflow_projection SET tolerance_pct=0.400
WHERE target_table='gate_pass'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerOutbound')
  AND tolerance_pct IS NULL;

-- ===== weight routing: w1/w2/w3 into the outbound projections' field_map =====
UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(
  COALESCE(field_map_json, JSON_OBJECT()),
  JSON_OBJECT(
    'w1_weight_kg', JSON_ARRAY('w1_weight_kg','w1WeightKg','grossKg'),
    'w2_weight_kg', JSON_ARRAY('w2_weight_kg','w2WeightKg'),
    'w3_weight_kg', JSON_ARRAY('w3_weight_kg','w3WeightKg')))
WHERE target_table='gate_pass'
  AND workflow_code IN ('M10.FGLoading','M10.PackLoading','M10.TankerOutbound','M10.TankerInbound')
  AND (field_map_json IS NULL OR JSON_EXTRACT(field_map_json,'$.w1_weight_kg') IS NULL);

-- ===== weighment-capture form: per-stage weight fields (gated by weight_seq) =====
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json='{"kind": "layout", "schemaVersion": 2, "slug": "aria:weighment-capture", "title": "Weighment Capture", "status": "published", "version": 1, "roles": ["WeighmentOfficer", "Manager", "TenantAdmin"], "dataSources": [], "streams": [], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Vehicle & Weighbridge", "span": 12, "subtitle": "One weighment per direction. Loaded weight on entry, empty on exit; difference = quantity received/dispatched.", "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "gp_number", "label": "Gate Pass # (from queue)", "type": "text", "required": true, "readOnly": true}, {"name": "vehicle_no", "label": "Vehicle (auto-fill)", "type": "text", "readOnly": true}, {"name": "weighbridge_code", "label": "Weighbridge", "type": "select", "required": true, "options": [{"label": "WB-1 (Main Gate, 60T)", "value": "WB-1"}, {"label": "WB-2 (Dispatch, 40T)", "value": "WB-2"}]}, {"name": "weighment_date", "label": "Date", "type": "date", "required": true}, {"name": "entry_time", "label": "Entry Time", "type": "time", "required": true}, {"name": "weight_officer", "label": "Weighment Officer Name", "type": "text", "required": true}]}}]}, {"kind": "Section", "title": "Reading", "span": 12, "subtitle": "Live feed from the weighbridge controller. Press Capture to lock the reading.", "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "weight_seq", "label": "Weighment", "type": "select", "required": true, "options": [{"label": "First (loaded / gross)", "value": "FIRST"}, {"label": "Second (empty / tare)", "value": "SECOND"}, {"label": "Third (re-check)", "value": "THIRD"}]}, {"kind": "FormField", "name": "w1_weight_kg", "label": "First Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == \'FIRST\'"}, {"kind": "FormField", "name": "w2_weight_kg", "label": "Second Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == \'SECOND\'"}, {"kind": "FormField", "name": "w3_weight_kg", "label": "Third Weight (kg)", "type": "number", "visibleWhen": "#form.weight_seq == \'THIRD\'"}, {"kind": "FormField", "name": "seal_lock_status", "label": "Seal Lock Status (2nd tanker weight)", "type": "select", "options": [{"label": "Auto Lock", "value": "AUTO_LOCK"}, {"label": "Seal No Update", "value": "SEAL_NO_UPDATE"}], "visibleWhen": "#form.weight_seq == \'SECOND\' && #form.flow_variant_code_hidden == \'M10.TankerOutbound\'"}, {"name": "live_reading_display", "label": "Live Reading", "type": "text", "readOnly": true, "helperText": "Locked while stabilising — wait for the green tick on the controller"}, {"name": "gross_kg", "label": "Gross Weight (kg)", "type": "number", "required": true}, {"name": "tare_kg", "label": "Tare (kg) — from vehicle master or this empty reading", "type": "number"}, {"name": "net_kg", "label": "Net (kg) — auto Gross − Tare", "type": "number", "readOnly": true}, {"name": "expected_kg", "label": "Expected (kg) — from PO / SO", "type": "number", "readOnly": true}, {"name": "variance_kg", "label": "Variance (kg) — auto", "type": "number", "readOnly": true}, {"name": "within_tolerance", "label": "Within Tolerance?", "type": "select", "required": true, "options": [{"label": "YES — proceed", "value": "YES"}, {"label": "NO — route to manager", "value": "NO"}]}, {"kind": "FormField", "name": "flow_variant_code_hidden", "label": "", "type": "text", "readOnly": true}]}}]}, {"kind": "Section", "title": "Photo Evidence", "span": 12, "children": [{"kind": "Form", "submitLabel": "", "bindings": {"fields": [{"name": "wb_display_photo", "label": "Weighbridge Display Photo", "type": "file", "accept": "image/*"}, {"name": "vehicle_photo", "label": "Vehicle on Bridge Photo", "type": "file", "accept": "image/*"}, {"name": "notes", "label": "Notes (any anomaly)", "type": "textarea", "rows": 2}]}}]}, {"kind": "Section", "span": 12, "children": [{"kind": "WorkflowActionButton", "action": "workflow.transition", "label": "Capture & Save Weighment", "variant": "primary"}]}]}]}'
WHERE pd.code='aria:weighment-capture'
  AND JSON_SEARCH(pv.body_json,'one','w1_weight_kg') IS NULL;

-- ===== PMT form: show the system verdict (read-only) + expected/loaded/variance =====
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json='{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "dataSources": [{"id": "q", "kind": "rest", "method": "GET", "url": "/api/workflow/queue/PMT"}], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "PMT — Pending Verification", "span": 12, "subtitle": "OrderApp qty vs loading qty. Tolerance 4 kg / ton (0.4%). Over-tolerance → camera + manager reason.", "children": [{"kind": "WorkflowQueueList", "persona": "PMT", "dataSource": "q", "pageSize": 25, "cardFields": ["gpNumber", "vehicleNo", "flowVariant", "currentState"]}]}, {"kind": "Section", "title": "Verify", "span": 12, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (pick one)", "type": "select", "required": true, "optionsSource": {"type": "entity", "entity": "GatePass", "lookupType": "gpNumbers", "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "instanceId": "workflow_instance_id", "w1_weight_kg": "w1_weight_kg", "w2_weight_kg": "w2_weight_kg", "ordered_qty": "ordered_qty", "reference_no": "reference_no", "withinTolerance": "pmt_within_tolerance", "expected_weight_kg": "expected_weight_kg", "loaded_weight_kg": "loaded_weight_kg", "pmt_variance_pct": "pmt_variance_pct"}}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "reference_no", "label": "Order ID", "type": "text", "readOnly": true}, {"name": "w1_weight_kg", "label": "1st Weight (kg)", "type": "text", "readOnly": true}, {"name": "w2_weight_kg", "label": "2nd Weight (kg)", "type": "text", "readOnly": true}, {"name": "ordered_qty", "label": "OrderApp Qty", "type": "text", "readOnly": true}, {"name": "expected_weight_kg", "label": "Expected Weight (kg) — Σ order×SKU wt", "type": "text", "readOnly": true}, {"name": "loaded_weight_kg", "label": "Loaded (W1 − W2) kg", "type": "text", "readOnly": true}, {"name": "pmt_variance_pct", "label": "Variance %", "type": "text", "readOnly": true}, {"name": "withinTolerance", "label": "System Verdict (auto — ≤0.4% = YES)", "type": "text", "readOnly": true}, {"name": "variance_reason", "label": "Variance Reason (required if over tolerance / to manager)", "type": "textarea"}]}, "children": [{"kind": "WorkflowActionButton", "label": "Check OK", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CHECK_OK", "color": "primary"}, {"kind": "WorkflowActionButton", "label": "Camera Verification", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "PMT_CAMERA_VERIFICATION", "color": "warning"}]}]}]}]}'
WHERE pd.code='aria:pmt-weight-diff-check'
  AND JSON_EXTRACT(pv.body_json,'$.layout[0].children[1].children[0].bindings.fields[0].onChangeFill.withinTolerance') IS NULL;

