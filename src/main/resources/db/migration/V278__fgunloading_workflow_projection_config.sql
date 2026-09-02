-- M10.FGUnloading's workflow_definition already exists (authored via the Workflow Designer
-- UI on the server). What's missing is the m0_workflow_projection config that makes
-- WorkflowProjectionListener actually persist each submitted page into its business table
-- (and stamp gate_pass.current_step on every transition) — without a row here, keyed on
-- the exact workflow_code, submitting a page only advances the FSM state; nothing is saved.
-- Rows below are cloned verbatim (field_map_json byte-for-byte) from M10.PackUnload's
-- existing config for the page codes FGUnloading shares with it.

-- gate_pass: workflow-agnostic master record, page_code NULL => applies on every transition.
-- This is what stamps gate_pass.status / gate_pass.current_step per the requirement.
-- tolerance_pct=0.400 (matches M10.FGLoading/PackLoading/TankerOutbound's own convention)
-- switches on DispatchToleranceListener, which server-computes expected_weight_kg /
-- loaded_weight_kg / pmt_variance_pct / pmt_within_tolerance into manager_qty_approval_detail
-- from weighment_capture_detail's w1/w2_weight_kg and loading_allocation_detail's ordered_qty
-- (both already wired below) -- without this column set, none of those 4 fields compute at all.
INSERT INTO m0_workflow_projection
    (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, variant_column, number_column, number_pattern, terminal_states, terminal_ts_column, field_map_json, tolerance_pct)
SELECT -1, 'M10.FGUnloading', 'gate_pass', NULL, 'workflow_instance_id', 'status,current_step', 'flow_variant_code', 'gp_number', 'GP-{tenant}-{instance}',
    'QC_REJECTED,QC_PRELOAD_REJECTED,QC_SAMPLE_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED,REJECTED_BY_MANAGER', 'closed_at',
    '{"vehicle_no": ["vehicleNo", "vehicle_no", "registrationNo", "registration_no", "plate", "vehicleRegNo"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverMobile", "driver_mobile"], "loading_type": ["loadingType", "loading_type"], "vehicle_category": ["vehicleCategory", "vehicle_category"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "time_taken": ["time_taken", "timeTaken"]}',
    0.400
WHERE NOT EXISTS (
    SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.FGUnloading' AND target_table = 'gate_pass'
);

-- dispatch_vehicle_detail: aria:dispatch-vehicle-details
INSERT INTO m0_workflow_projection
    (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.FGUnloading', 'dispatch_vehicle_detail', 'aria:dispatch-vehicle-details', 'workflow_instance_id', 'current_step', 'flow_variant_code',
    '{"ownership_type": ["ownershipType", "ownership_type"], "vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverContact"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "vendor_name": ["vendorName", "vendor_name"], "po_verified": ["poVerified", "po_verified"], "po_override_reason": ["poOverrideReason", "po_override_reason"], "dispatch_officer_name": ["dispatchOfficerName", "dispatch_officer_name"], "packing_fg_stock_details": ["packingFgStockDetails", "packing_fg_stock_details"], "gp_number": ["gp_number", "gpNumber"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "po_numbers": ["po_numbers", "poNumbers"], "po_attachment": ["po_attachment", "poAttachment", "attachmentDocs"], "broker_number": ["broker_number", "brokerNumber", "brokerContactNumber"], "party_name": ["party_name", "partyName", "partyNames"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
    SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.FGUnloading' AND target_table = 'dispatch_vehicle_detail'
);

-- qc_preload_detail: aria:qc-approval
INSERT INTO m0_workflow_projection
    (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.FGUnloading', 'qc_preload_detail', 'aria:qc-approval', 'workflow_instance_id', 'current_step', 'flow_variant_code',
    '{"loading_supervisor": ["loadingSupervisor", "loading_supervisor"], "loading_tank_location": ["loadingTankLocation", "loading_tank_location"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "unloading_location": ["unloadingLocation", "unloading_location"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
    SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.FGUnloading' AND target_table = 'qc_preload_detail'
);

-- qc_unloading_approval: aria:qc-approval (supplementary columns; status/qc_reject_reason are
-- written separately by QcApprovalDecisionListener once M10.FGUnloading is added to its allowlist)
INSERT INTO m0_workflow_projection
    (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, terminal_states, field_map_json)
SELECT -1, 'M10.FGUnloading', 'qc_unloading_approval', 'aria:qc-approval', 'workflow_instance_id', 'current_step',
    'QC_REJECTED,QC_PRELOAD_REJECTED,QC_SAMPLE_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED,REJECTED_BY_MANAGER',
    '{"item_category": ["item_category", "itemCategory"], "sample_jar_id": ["sample_jar_id", "sampleJarId"], "qc_officer_id": ["qc_officer_id", "qcOfficerId"], "density_kgl": ["density_kgl", "densityKgl"], "ffa_pct": ["ffa_pct", "ffaPct"], "colour_rating": ["colour_rating", "colourRating"], "temperature_c": ["temperature_c", "temperatureC"], "print_quality": ["print_quality", "printQuality"], "dimensional_check": ["dimensional_check", "dimensionalCheck"], "bottle_integrity": ["bottle_integrity", "bottleIntegrity"], "carton_strength": ["carton_strength", "cartonStrength"], "seal_intact": ["seal_intact", "sealIntact"], "label_match": ["label_match", "labelMatch"], "batch_expiry_check": ["batch_expiry_check", "batchExpiryCheck"], "sample_photo": ["sample_photo", "samplePhoto"], "seal_unlock_no": ["seal_unlock_no", "sealUnlockNo"], "unloading_supervisor_id": ["unloadingSupervisorName", "unloading_supervisor_name"], "unloading_location": ["unloadingLocation", "unloading_location"], "oil_sample_recovered": ["oilSampleRecovered", "oil_sample_recovered"], "qc_notes": ["qc_notes", "qcNotes"], "qc_reject_reason": ["qcRejectReason", "qc_reject_reason"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
    SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.FGUnloading' AND target_table = 'qc_unloading_approval'
);

-- loading_allocation_detail: aria:loading-allocation
INSERT INTO m0_workflow_projection
    (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.FGUnloading', 'loading_allocation_detail', 'aria:loading-allocation', 'workflow_instance_id', 'current_step', 'flow_variant_code',
    '{"supervisor_name": ["supervisor_name", "supervisorName"], "loading_bay": ["loadingBay", "loading_bay"], "reference_no": ["orderRef", "order_ref", "reference_no"], "customer_name": ["customerName", "customer_name"], "customer_route": ["orderRoute", "customer_route"], "ordered_cases": ["orderedCases", "ordered_cases"], "ordered_qty": ["orderedQty", "ordered_qty", "orderedWeightKg"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
    SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.FGUnloading' AND target_table = 'loading_allocation_detail'
);

-- weighment_capture_detail: aria:weighment-capture (one row covers all 3 W1/W2/W3 occurrences)
INSERT INTO m0_workflow_projection
    (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, variant_column, field_map_json)
SELECT -1, 'M10.FGUnloading', 'weighment_capture_detail', 'aria:weighment-capture', 'workflow_instance_id', 'current_step', 'flow_variant_code',
    '{"w1_weight_kg": ["w1_weight_kg", "w1WeightKg", "grossKg"], "w2_weight_kg": ["w2_weight_kg", "w2WeightKg"], "w3_weight_kg": ["w3_weight_kg", "w3WeightKg"], "weight_officer": ["weightOfficer", "weight_officer"], "weight_entry_at": ["weightEntryAt", "weight_entry_at"], "queue_reason": ["queue_reason", "queueReason", "queueSkipReason", "queue_skip_reason"], "notes": ["notes"], "unwanted_material_removed": ["unwantedMaterialRemoved", "unwanted_material_removed"], "wb_display_photo": ["wbDisplayPhoto", "wb_display_photo"], "vehicle_photo": ["vehiclePhoto", "vehicle_photo"], "sealing_no": ["sealingNo", "sealing_no"], "sealing_supervisor": ["sealingSupervisor", "sealing_supervisor"], "sealing_status": ["seal_lock_status", "sealLockStatus"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
    SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.FGUnloading' AND target_table = 'weighment_capture_detail'
);

-- gate_pass_closure: aria:gate-pass-closure-form
-- state_columns explicitly '' (this table has no `status` column, unlike the schema default)
INSERT INTO m0_workflow_projection
    (tenant_id, workflow_code, target_table, page_code, key_column, state_columns, terminal_states, terminal_ts_column, field_map_json)
SELECT -1, 'M10.FGUnloading', 'gate_pass_closure', 'aria:gate-pass-closure-form', 'workflow_instance_id', '',
    'QC_REJECTED,QC_PRELOAD_REJECTED,QC_SAMPLE_REJECTED,GATE_PASS_OUT_CLOSED,CLOSED,REJECTED_BY_MANAGER', 'closed_at',
    '{"seal_verified": ["seal_verified", "sealVerified"], "invoice_count_verified": ["invoice_count_verified", "invoiceCountVerified"], "qc_clearance": ["qc_clearance", "qcClearance"], "weighment_verified": ["weighment_verified", "weighmentVerified"], "order_recon_done": ["order_recon_done", "orderReconDone"], "within_tolerance": ["within_tolerance", "withinTolerance"], "closure_note": ["closure_note", "closureNote"], "time_taken": ["time_taken", "timeTaken"]}'
WHERE NOT EXISTS (
    SELECT 1 FROM m0_workflow_projection WHERE tenant_id = -1 AND workflow_code = 'M10.FGUnloading' AND target_table = 'gate_pass_closure'
);
