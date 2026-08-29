-- aria:weighment-capture-other-vehicle (M10.OtherVehicles' own dedicated
-- weighment page, distinct from the generic aria:weighment-capture V259
-- originally pointed this workflow at) splits its submitted fields across
-- THREE existing tables -- no new table:
--
--   gate_pass               -- vehicle_no, vehicle_category, driver_name,
--                              driver_phone (already mapped by V259's own
--                              gate_pass projection row -- untouched here).
--   weighment_capture_detail -- weight-related fields: weight_officer,
--                              w1_weight_kg, w2_weight_kg, w1_captured_at/
--                              w2_captured_at (aliased onto the existing
--                              single weight_entry_at column -- same
--                              one-stamp-per-submission convention the
--                              generic aria:weighment-capture page already
--                              uses), wb_display_photo/vehicle_photo (the
--                              existing isWeighmentCaptureDetail branch in
--                              WorkflowProjectionListener auto-prefixes
--                              these w1_/w2_/w3_ from weight_seq -- no code
--                              change needed), notes, skip_weighment (new
--                              column), time_taken (existing, V265).
--   dispatch_vehicle_detail -- everything else: gp_number, ownership_type
--                              (both existing columns already), plus
--                              material_details (new column). Reusing this
--                              existing satellite (shared with
--                              aria:dispatch-vehicle-details on the OTHER
--                              5 M10 workflows) rather than a new table --
--                              M10.OtherVehicles has no dispatch-vehicle-
--                              details step of its own (V259), so this
--                              table was otherwise never written for it.
--
-- weight_seq itself (FIRST/SECOND) is not persisted anywhere -- it's a
-- purely computed UI helper, redundant with current_step (already stamped
-- via state_columns on every projected table).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS skip_weighment VARCHAR(8) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+1,
       -1, 'WeighmentCaptureDetail', 'skip_weighment', 'Skip Weighment', 'STRING', 1, 0, 0, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='WeighmentCaptureDetail' AND field_code='skip_weighment');

ALTER TABLE dispatch_vehicle_detail
  ADD COLUMN IF NOT EXISTS material_details TEXT DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+1,
       -1, 'DispatchVehicleDetail', 'material_details', 'Material Details', 'STRING', 0, 0, 1, 0, 'textarea'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='material_details');

-- Projection row 1: weight-related fields -> weighment_capture_detail.
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.OtherVehicles', 'weighment_capture_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  'aria:weighment-capture-other-vehicle',
  '{"weight_officer": ["weight_officer", "weightOfficer"], "w1_weight_kg": ["w1_weight_kg", "w1WeightKg"], "w2_weight_kg": ["w2_weight_kg", "w2WeightKg"], "weight_entry_at": ["w1_captured_at", "w2_captured_at", "weight_entry_at", "weightEntryAt"], "wb_display_photo": ["wb_display_photo", "wbDisplayPhoto"], "vehicle_photo": ["vehicle_photo", "vehiclePhoto"], "notes": ["notes"], "skip_weighment": ["skip_weighment", "skipWeighment"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'weighment_capture_detail'
);

-- Projection row 2: everything else -> dispatch_vehicle_detail (existing
-- shared table, otherwise unused by this workflow).
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.OtherVehicles', 'dispatch_vehicle_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  'aria:weighment-capture-other-vehicle',
  '{"gp_number": ["gp_number", "gpNumber"], "ownership_type": ["ownership_type", "ownershipType"], "material_details": ["material_details", "materialDetails"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'dispatch_vehicle_detail'
);
