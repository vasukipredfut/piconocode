-- M10.OtherVehicles: new flow_variant for incidental/non-SKU vehicle traffic
-- (store vehicle, water tanker, scrap vehicle, empty vehicle, JCB) that still
-- needs gate control but doesn't carry SKU line items through bays.
--
-- Rides entirely on existing generic pages (aria:weighment-capture,
-- aria:loading-in-progress-entry, aria:gate-pass-closure-form) -- same shape
-- as M10.ReturnStockIn (V128/V129): zero new pages, zero new Java action
-- handlers, just a new workflow_definition + m0_workflow_projection rows (all
-- existing tables/columns -- vehicle_category and material capture reuse
-- what gate_pass already has, no ALTERs here).
--
-- Starts straight at first weight -- no aria:dispatch-vehicle-details step:
-- DRAFT's own outgoing transition is aria:weighment-capture, so the very
-- first page a workflow instance shows is the weighment-capture screen.
--
-- Deliberately has NO parking-bay state (no PARKING_IN_*/PARKING_OUT_*) --
-- parking QR scans for these vehicles, if used operationally, go through the
-- existing standalone POST /api/parking/scan endpoint independent of this
-- workflow (per ParkingBayScanController), not an FSM hop.
--
-- Weighment 2 is skippable per-instance (e.g. JCB): the weighment officer
-- submits "skip_weighment" = YES on the first-weight page instead of a
-- category-based guard, matching the OPEN_BODY-skip precedent in
-- M10.PackLoading (V241) but keyed on this field instead of vehicle_category.
-- Not projected to a table column -- the FSM guard reads it straight off
-- workflow_instance.submitted_data_json.
--
-- Idempotent throughout -- safe to (re)run on any database.

-- 1. FlowVariant master row -- with a fixed loading_type_code pinned to
--    'OTHERS' (existing m10_loading_type code from V39), since these vehicles
--    don't fit the LOADING/UN_LOADING distinction the other flow variants use.
ALTER TABLE m10_flow_variant
  ADD COLUMN IF NOT EXISTS loading_type_code VARCHAR(32) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+1,
       -1, 'FlowVariant', 'loading_type_code', 'Loading Type Code', 'STRING', 1, 1, 0, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='FlowVariant' AND field_code='loading_type_code');

INSERT IGNORE INTO `m10_flow_variant` (`code`, `display_name`, `loading_type_code`) VALUES
('M10.OtherVehicles', 'Other Vehicles (Store/Tanker/Scrap/Empty/JCB)', 'OTHERS');

UPDATE m10_flow_variant SET loading_type_code = 'OTHERS' WHERE code = 'M10.OtherVehicles';

-- 2. vehicle_category: already exists on gate_pass with its own master/options
--    (m10_vehicle_category, V39) -- nothing to add here; M10.OtherVehicles
--    reuses the existing codes (JCB, OPEN_BODY, etc.) as-is.

-- 3. weighment_capture_detail / gate_pass: no ALTERs -- skip_weighment stays
--    a plain submitted form field (the FSM guard reads it straight off
--    workflow_instance.submitted_data_json, no destination column needed),
--    and material details/auto-tracked duration are left out of this pass.

-- 5. New workflow_definition row.
INSERT INTO workflow_definition (tenant_id, code, version, status, body_json)
SELECT -1, 'M10.OtherVehicles', 1, 'PUBLISHED', '{"code":"M10.OtherVehicles","version":1,"initialState":"DRAFT","states":[{"code":"DRAFT"},{"code":"WEIGHMENT_1","assignedRole":"WeighmentOfficer"},{"code":"LOADING_UNLOADING_IN_PROGRESS","assignedRole":"LoadingSupervisor"},{"code":"WEIGHMENT_2","assignedRole":"WeighmentOfficer"},{"code":"GATE_PASS_CLOSURE","assignedRole":"FrontGate"},{"code":"GATE_PASS_OUT_CLOSED","terminal":true}],"transitions":[{"from":"DRAFT","to":"WEIGHMENT_1","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_1","to":"LOADING_UNLOADING_IN_PROGRESS","trigger":"page.submitted","pageCode":"aria:loading-in-progress-entry"},{"from":"LOADING_UNLOADING_IN_PROGRESS","to":"WEIGHMENT_2","trigger":"page.submitted","pageCode":"aria:weighment-capture","guard":"#submitted[''skip_weighment''] != ''YES''"},{"from":"LOADING_UNLOADING_IN_PROGRESS","to":"GATE_PASS_CLOSURE","trigger":"page.submitted","pageCode":"aria:weighment-capture","guard":"#submitted[''skip_weighment''] == ''YES''"},{"from":"WEIGHMENT_2","to":"GATE_PASS_CLOSURE","trigger":"page.submitted","pageCode":"aria:gate-pass-closure-form"},{"from":"GATE_PASS_CLOSURE","to":"GATE_PASS_OUT_CLOSED","trigger":"page.submitted","pageCode":"aria:gate-pass-closure-form","guard":"(#submitted[''weighment_verified''] == ''OK'' || #submitted[''weighment_verified''] == ''MGR_APPROVED'' || #submitted[''weighment_verified''] == ''NA'') && (#submitted[''seal_verified''] == ''MATCHED'' || #submitted[''seal_verified''] == ''NA'') && #submitted[''invoice_count_verified''] == ''YES''"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M10.OtherVehicles' AND version = 1);

-- 6. m0_workflow_projection rows -- one per target_table. No
--    dispatch_vehicle_detail row: this flow has no aria:dispatch-vehicle-
--    details step (it starts straight at first weight) -- vehicle/driver
--    fields ride on gate_pass instead (captured at gate-pass creation and via
--    aria:weighment-capture's own vehicle_no/driver fields).
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, terminal_states, field_map_json)
SELECT -1, 'M10.OtherVehicles', 'weighment_capture_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', 'GATE_PASS_OUT_CLOSED',
  '{"w1_weight_kg": ["w1_weight_kg", "w1WeightKg", "grossKg"], "w2_weight_kg": ["w2_weight_kg", "w2WeightKg"], "weight_officer": ["weightOfficer", "weight_officer"], "weight_entry_at": ["weightEntryAt", "weight_entry_at"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'weighment_capture_detail'
);

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, terminal_states, field_map_json)
SELECT -1, 'M10.OtherVehicles', 'gate_pass', 'workflow_instance_id', 'current_step', 'flow_variant_code', 'GATE_PASS_OUT_CLOSED',
  '{"vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone"], "vehicle_category": ["vehicleCategory", "vehicle_category"], "transport_name": ["transportName", "transport_name"], "w1_weight_kg": ["w1_weight_kg", "w1WeightKg"], "w2_weight_kg": ["w2_weight_kg", "w2WeightKg"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "seal_verified": ["seal_verified", "sealVerified"], "weighment_verified": ["weighment_verified", "weighmentVerified"], "invoice_count_verified": ["invoice_count_verified", "invoiceCountVerified"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'gate_pass'
);
