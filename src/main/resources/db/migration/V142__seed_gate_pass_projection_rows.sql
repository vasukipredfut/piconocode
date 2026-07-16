-- =====================================================================
-- V142 — seed the base m0_workflow_projection rows targeting gate_pass.
--
-- No migration anywhere actually INSERTs these — V129/V134 only UPDATE
-- field_map_json on rows they assume already exist (grep for "INSERT
-- INTO m0_workflow_projection" across every migration finds none before
-- V134's dispatch_vehicle_detail rows, and those target a different
-- table). On any DB where m0_workflow_projection was created empty by
-- V123 (this one included), NO workflow transition for ANY of the 6
-- M10 gate-pass workflows ever wrote a gate_pass row — the transition
-- itself succeeds (workflow_instance advances), but WorkflowProjection-
-- Listener has nothing to project with.
-- =====================================================================

INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, field_map_json)
  SELECT 1,'M10.TankerInbound','gate_pass','workflow_instance_id','status,current_step','flow_variant_code','gp_number','GP-{tenant}-{instance}',
    '{"vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone"], "loading_type": ["loadingType", "loading_type"], "vehicle_category": ["vehicleCategory", "vehicle_category"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.TankerInbound' AND target_table='gate_pass');
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, field_map_json)
  SELECT 1,'M10.PackUnload','gate_pass','workflow_instance_id','status,current_step','flow_variant_code','gp_number','GP-{tenant}-{instance}',
    '{"vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone"], "loading_type": ["loadingType", "loading_type"], "vehicle_category": ["vehicleCategory", "vehicle_category"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.PackUnload' AND target_table='gate_pass');
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, field_map_json)
  SELECT 1,'M10.ReturnStockIn','gate_pass','workflow_instance_id','status,current_step','flow_variant_code','gp_number','GP-{tenant}-{instance}',
    '{"vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone"], "loading_type": ["loadingType", "loading_type"], "vehicle_category": ["vehicleCategory", "vehicle_category"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.ReturnStockIn' AND target_table='gate_pass');
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, field_map_json)
  SELECT 1,'M10.TankerOutbound','gate_pass','workflow_instance_id','status,current_step','flow_variant_code','gp_number','GP-{tenant}-{instance}',
    '{"vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone"], "loading_type": ["loadingType", "loading_type"], "vehicle_category": ["vehicleCategory", "vehicle_category"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.TankerOutbound' AND target_table='gate_pass');
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, field_map_json)
  SELECT 1,'M10.PackLoading','gate_pass','workflow_instance_id','status,current_step','flow_variant_code','gp_number','GP-{tenant}-{instance}',
    '{"vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone"], "loading_type": ["loadingType", "loading_type"], "vehicle_category": ["vehicleCategory", "vehicle_category"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.PackLoading' AND target_table='gate_pass');
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, field_map_json)
  SELECT 1,'M10.FGLoading','gate_pass','workflow_instance_id','status,current_step','flow_variant_code','gp_number','GP-{tenant}-{instance}',
    '{"vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone"], "loading_type": ["loadingType", "loading_type"], "vehicle_category": ["vehicleCategory", "vehicle_category"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.FGLoading' AND target_table='gate_pass');
