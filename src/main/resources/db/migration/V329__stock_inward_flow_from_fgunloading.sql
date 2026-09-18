-- M10.StockInward: new flow_variant registered by Dhivya (2026-09-18) as an
-- exact clone of M10.FGUnloading's state machine -- same states, same
-- transitions, same guards, only the workflow code changed. Pairs with
-- M10.StockOutward (V328, cloned from M10.FGLoading) the same way
-- M10.FGLoading/M10.FGUnloading themselves pair up. Rides entirely on the
-- SAME existing pages M10.FGUnloading already uses (aria:dispatch-vehicle-
-- details, aria:qc-approval, aria:loading-planning-order-sheet-upload,
-- aria:weighment-capture, aria:unloading-in-progress-entry,
-- aria:manager-qty-approval, aria:manager-weight-approval,
-- aria:gate-pass-closure-form, aria:loading-allocation) -- zero new pages,
-- zero new Java.
--
-- Deliberately scoped to ONLY the FSM + its data-projection wiring
-- (workflow_definition + m0_workflow_projection), matching V328/the
-- M10.OtherVehicles precedent (V259): those existing pages' own JSON still
-- branches on the LITERAL string 'M10.FGUnloading' in a number of
-- visibleWhen/computedFrom conditions, so fields tuned for FG Unloading
-- will NOT automatically show/compute the same way for M10.StockInward
-- instances yet -- confirmed intentional for now, same as V328.
--
-- m0_workflow_projection rows cloned: ALL of M10.FGUnloading's own rows --
-- unlike V328 (M10.FGLoading had 2 rows this FSM's own transitions never
-- use), every one of M10.FGUnloading's 6 rows has a page_code that DOES
-- appear in this FSM's transitions (including qc_unloading_approval, since
-- aria:qc-approval is a real step here).
--
-- Idempotent throughout -- safe to (re)run on any database.

INSERT IGNORE INTO `m10_flow_variant` (`code`, `display_name`) VALUES
('M10.StockInward', 'Stock Inward');

INSERT INTO workflow_definition (tenant_id, code, version, status, body_json)
SELECT -1, 'M10.StockInward', 1, 'PUBLISHED', '{"code":"M10.StockInward","initialState":"DRAFT","states":[{"code":"DRAFT"},{"code":"DISPATCH_VEHICLE_ENTRY","assignedRole":"QC"},{"code":"QC_APPROVED","assignedRole":"Dispatch"},{"code":"QC_REJECTED","terminal":true},{"code":"UNLOADING_ALLOCATED","assignedRole":"WeighmentOfficer"},{"code":"WEIGHMENT_1","assignedRole":"UnloadingSupervisor"},{"code":"UNLOADING_COMPLETED","assignedRole":"WeighmentOfficer"},{"code":"WEIGHMENT_2","assignedRole":"Manager"},{"code":"QUANTITY_RECONCILIATION","assignedRole":"WeighmentOfficer"},{"code":"QUANTITY_APPROVAL_PENDING","assignedRole":"Manager"},{"code":"WEIGHMENT_3","assignedRole":"Manager"},{"code":"WEIGHT_APPROVAL_PENDING","assignedRole":"Manager"},{"code":"READY_FOR_EXIT","assignedRole":"FrontGate"},{"code":"GATE_PASS_OUT_CLOSED","terminal":true},{"code":"REJECTED_BY_MANAGER","terminal":true},{"code":"BAY_ASSIGNED","assignedRole":"UnloadingSupervisor"},{"code":"UNLOADING_IN_PROGRESS","assignedRole":"UnloadingSupervisor"},{"code":"PLANNING_SHEET_UPDATED","assignedRole":"Dispatch"}],"transitions":[{"from":"DRAFT","to":"DISPATCH_VEHICLE_ENTRY","trigger":"page.submitted","pageCode":"aria:dispatch-vehicle-details"},{"from":"DISPATCH_VEHICLE_ENTRY","to":"QC_APPROVED","trigger":"page.submitted","pageCode":"aria:qc-approval","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"DISPATCH_VEHICLE_ENTRY","to":"QC_REJECTED","trigger":"page.submitted","pageCode":"aria:qc-approval","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"QC_APPROVED","to":"PLANNING_SHEET_UPDATED","trigger":"page.submitted","pageCode":"aria:loading-planning-order-sheet-upload"},{"from":"UNLOADING_ALLOCATED","to":"WEIGHMENT_1","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_1","to":"UNLOADING_IN_PROGRESS","trigger":"page.submitted","pageCode":"aria:unloading-in-progress-entry"},{"from":"UNLOADING_COMPLETED","to":"WEIGHMENT_2","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"WEIGHMENT_2","to":"QUANTITY_RECONCILIATION","trigger":"page.submitted","pageCode":"aria:manager-qty-approval","guard":"#submitted[''withinTolerance''] == ''YES''"},{"from":"WEIGHMENT_2","to":"QUANTITY_APPROVAL_PENDING","trigger":"page.submitted","pageCode":"aria:manager-qty-approval","guard":"#submitted[''withinTolerance''] == ''NO''"},{"from":"QUANTITY_APPROVAL_PENDING","to":"QUANTITY_RECONCILIATION","trigger":"page.submitted","pageCode":"aria:manager-qty-approval","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"QUANTITY_APPROVAL_PENDING","to":"REJECTED_BY_MANAGER","trigger":"page.submitted","pageCode":"aria:manager-qty-approval","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"WEIGHMENT_3","to":"READY_FOR_EXIT","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"WEIGHMENT_3","to":"WEIGHT_APPROVAL_PENDING","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"WEIGHT_APPROVAL_PENDING","to":"READY_FOR_EXIT","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[''status''] == ''APPROVED''"},{"from":"WEIGHT_APPROVAL_PENDING","to":"REJECTED_BY_MANAGER","trigger":"page.submitted","pageCode":"aria:manager-weight-approval","guard":"#submitted[''status''] == ''REJECTED''"},{"from":"READY_FOR_EXIT","to":"GATE_PASS_OUT_CLOSED","trigger":"page.submitted","pageCode":"aria:gate-pass-closure-form","guard":"(#submitted[''weighment_verified''] == ''OK'' || #submitted[''weighment_verified''] == ''MGR_APPROVED'') && (#submitted[''seal_verified''] == ''MATCHED'' || #submitted[''seal_verified''] == ''NA'') && #submitted[''invoice_count_verified''] == ''YES''"},{"from":"QUANTITY_RECONCILIATION","to":"WEIGHMENT_3","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"READY_FOR_EXIT","to":"WEIGHT_APPROVAL_PENDING","trigger":"page.submitted","pageCode":"aria:gate-pass-closure-form","guard":"#submitted[''report_weight_diff''] == ''true''"},{"from":"BAY_ASSIGNED","to":"BAY_ASSIGNED","trigger":"page.submitted","pageCode":"aria:unloading-in-progress-entry","guard":"#submitted[''bayAction''] == ''start''"},{"from":"BAY_ASSIGNED","to":"UNLOADING_COMPLETED","trigger":"page.submitted","pageCode":"aria:unloading-in-progress-entry","guard":""},{"from":"UNLOADING_IN_PROGRESS","to":"BAY_ASSIGNED","trigger":"page.submitted","pageCode":"aria:unloading-in-progress-entry"},{"from":"UNLOADING_ALLOCATED","to":"WEIGHMENT_1","trigger":"page.submitted","pageCode":"aria:weighment-capture"},{"from":"PLANNING_SHEET_UPDATED","to":"UNLOADING_ALLOCATED","trigger":"page.submitted","pageCode":"aria:loading-allocation"}]}'
WHERE NOT EXISTS (SELECT 1 FROM workflow_definition WHERE tenant_id = -1 AND code = 'M10.StockInward' AND version = 1);

-- m0_workflow_projection rows -- cloned 1:1 from M10.FGUnloading's own rows (checked live).

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json)
SELECT -1, 'M10.StockInward', 'gate_pass', 'workflow_instance_id', 'status,current_step', 'flow_variant_code', 'gp_number', 'GP-{tenant}-{instance}',
  'QC_REJECTED,QC_PRELOAD_REJECTED,QC_SAMPLE_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED,REJECTED_BY_MANAGER', 'closed_at',
  '{"vehicle_no": ["vehicleNo", "vehicle_no", "registrationNo", "registration_no", "plate", "vehicleRegNo"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverMobile", "driver_mobile"], "loading_type": ["loadingType", "loading_type"], "vehicle_category": ["vehicleCategory", "vehicle_category"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.StockInward' AND target_table = 'gate_pass');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.StockInward', 'dispatch_vehicle_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', 'aria:dispatch-vehicle-details',
  '{"ownership_type": ["ownershipType", "ownership_type"], "vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverContact"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "vendor_name": ["vendorName", "vendor_name"], "po_verified": ["poVerified", "po_verified"], "po_override_reason": ["poOverrideReason", "po_override_reason"], "dispatch_officer_name": ["dispatchOfficerName", "dispatch_officer_name"], "packing_fg_stock_details": ["packingFgStockDetails", "packing_fg_stock_details"], "gp_number": ["gp_number", "gpNumber"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "po_numbers": ["po_numbers", "poNumbers"], "po_attachment": ["po_attachment", "poAttachment", "attachmentDocs"], "broker_number": ["broker_number", "brokerNumber", "brokerContactNumber"], "party_name": ["party_name", "partyName", "partyNames"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.StockInward' AND target_table = 'dispatch_vehicle_detail');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, terminal_states, page_code, field_map_json)
SELECT -1, 'M10.StockInward', 'qc_unloading_approval', 'workflow_instance_id', 'current_step', 'flow_variant_code',
  'QC_REJECTED,QC_PRELOAD_REJECTED,QC_SAMPLE_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED,REJECTED_BY_MANAGER', 'aria:qc-approval',
  '{"item_category": ["item_category", "itemCategory"], "sample_jar_id": ["sample_jar_id", "sampleJarId"], "qc_officer_id": ["qc_officer_id", "qcOfficerId"], "density_kgl": ["density_kgl", "densityKgl"], "ffa_pct": ["ffa_pct", "ffaPct"], "colour_rating": ["colour_rating", "colourRating"], "temperature_c": ["temperature_c", "temperatureC"], "print_quality": ["print_quality", "printQuality"], "dimensional_check": ["dimensional_check", "dimensionalCheck"], "bottle_integrity": ["bottle_integrity", "bottleIntegrity"], "carton_strength": ["carton_strength", "cartonStrength"], "seal_intact": ["seal_intact", "sealIntact"], "label_match": ["label_match", "labelMatch"], "batch_expiry_check": ["batch_expiry_check", "batchExpiryCheck"], "sample_photo": ["sample_photo", "samplePhoto"], "seal_unlock_no": ["seal_unlock_no", "sealUnlockNo"], "unloading_supervisor_id": ["unloadingSupervisorName", "unloading_supervisor_name"], "unloading_location": ["unloadingLocation", "unloading_location"], "oil_sample_recovered": ["oilSampleRecovered", "oil_sample_recovered"], "qc_notes": ["qc_notes", "qcNotes"], "qc_reject_reason": ["qcRejectReason", "qc_reject_reason"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.StockInward' AND target_table = 'qc_unloading_approval');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.StockInward', 'loading_allocation_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', 'aria:loading-allocation',
  '{"supervisor_name": ["supervisor_name", "supervisorName"], "loading_bay": ["loadingBay", "loading_bay"], "reference_no": ["orderRef", "order_ref", "reference_no"], "customer_name": ["customerName", "customer_name"], "customer_route": ["orderRoute", "customer_route"], "ordered_cases": ["orderedCases", "ordered_cases"], "ordered_qty": ["orderedQty", "ordered_qty", "orderedWeightKg"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.StockInward' AND target_table = 'loading_allocation_detail');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, page_code, field_map_json)
SELECT -1, 'M10.StockInward', 'weighment_capture_detail', 'workflow_instance_id', 'current_step', 'flow_variant_code', 'aria:weighment-capture',
  '{"w1_weight_kg": ["w1_weight_kg", "w1WeightKg", "grossKg"], "w2_weight_kg": ["w2_weight_kg", "w2WeightKg"], "w3_weight_kg": ["w3_weight_kg", "w3WeightKg"], "weight_officer": ["weightOfficer", "weight_officer"], "weight_entry_at": ["weightEntryAt", "weight_entry_at"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "notes": ["notes"], "unwanted_material_removed": ["unwantedMaterialRemoved", "unwanted_material_removed"], "wb_display_photo": ["wbDisplayPhoto", "wb_display_photo"], "vehicle_photo": ["vehiclePhoto", "vehicle_photo"], "sealing_no": ["sealingNo", "sealing_no"], "sealing_supervisor": ["sealingSupervisor", "sealing_supervisor"], "sealing_status": ["seal_lock_status", "sealLockStatus"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.StockInward' AND target_table = 'weighment_capture_detail');

INSERT INTO m0_workflow_projection
  (tenant_id, workflow_code, target_table, key_column, terminal_states, terminal_ts_column, page_code, field_map_json)
SELECT -1, 'M10.StockInward', 'gate_pass_closure', 'workflow_instance_id',
  'QC_REJECTED,QC_PRELOAD_REJECTED,QC_SAMPLE_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED,REJECTED_BY_MANAGER', 'closed_at', 'aria:gate-pass-closure-form',
  '{"seal_verified": ["seal_verified", "sealVerified"], "invoice_count_verified": ["invoice_count_verified", "invoiceCountVerified"], "qc_clearance": ["qc_clearance", "qcClearance"], "weighment_verified": ["weighment_verified", "weighmentVerified"], "order_recon_done": ["order_recon_done", "orderReconDone"], "within_tolerance": ["within_tolerance", "withinTolerance"], "closure_note": ["closure_note", "closureNote"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.StockInward' AND target_table = 'gate_pass_closure');
