-- M10.ReturnStockIn's new steps (V127) submit onto aria:dispatch-vehicle-details
-- and aria:weighment-capture, but neither had an m0_workflow_projection row
-- for this workflow_code — everything submitted there would silently drop
-- (same class of gap V125 fixed for TankerInbound). Also adds the 2 fields
-- genuinely new to ReturnStockIn ("No of invoice stock return count" and
-- "box weight" from the spec) as their own dispatch_vehicle_detail columns —
-- vendor/oil-RM/driver/invoice-return-reason are all covered by columns that
-- already exist (vendor_name, oil_rm_item, driver_name, driver_phone), just
-- gated off from ReturnStockIn on the page today (fixed in V129).

-- 1. New columns.
ALTER TABLE dispatch_vehicle_detail
  ADD COLUMN IF NOT EXISTS invoice_return_count INT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS box_weight_kg DECIMAL(10,3) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.fc),
       -1, 'DispatchVehicleDetail', c.fc, c.lbl, c.dt, 1, 1, 0, 0, c.wk
FROM (SELECT 'invoice_return_count' fc,'Invoice Stock Return Count' lbl,'LONG' dt,'number' wk
      UNION ALL SELECT 'box_weight_kg','Box Weight (kg)','DECIMAL','number') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code=c.fc);

-- 2. dispatch_vehicle_detail projection row for M10.ReturnStockIn — same
--    field_map_json shape as the 4 sibling workflows (V125's own comment
--    documents this same gap+fix pattern), plus the 2 new columns.
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.ReturnStockIn', 'dispatch_vehicle_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"ownership_type": ["ownershipType", "ownership_type"], "vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverContact"], "vendor_name": ["vendorName", "vendor_name"], "oil_rm_item": ["oilRmItem", "oil_rm_item"], "dispatch_officer_name": ["dispatchOfficerName", "dispatch_officer_name"], "invoice_return_count": ["invoiceReturnCount", "invoice_return_count"], "box_weight_kg": ["boxWeightKg", "box_weight_kg"], "gp_number": ["gp_number", "gpNumber"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.ReturnStockIn' AND target_table = 'dispatch_vehicle_detail'
);

-- 3. weighment_capture_detail projection row for M10.ReturnStockIn — same
--    field_map_json as FGLoading/PackLoading/TankerOutbound (no
--    seal_lock_status; that's TankerInbound-only).
INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.ReturnStockIn', 'weighment_capture_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  '{"w1_weight_kg": ["w1_weight_kg", "w1WeightKg", "grossKg"], "w2_weight_kg": ["w2_weight_kg", "w2WeightKg"], "w3_weight_kg": ["w3_weight_kg", "w3WeightKg"], "weight_officer": ["weightOfficer", "weight_officer"], "weight_entry_at": ["weightEntryAt", "weight_entry_at"]}'
WHERE NOT EXISTS (
  SELECT 1 FROM m0_workflow_projection
  WHERE tenant_id = -1 AND workflow_code = 'M10.ReturnStockIn' AND target_table = 'weighment_capture_detail'
);
