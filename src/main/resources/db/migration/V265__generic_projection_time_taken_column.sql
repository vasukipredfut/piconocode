-- "Time taken" per page submission (Phase 1): the UI computes elapsed
-- minutes client-side for a step and submits it as `time_taken` on that
-- page's own formData; the backend just needs to persist it onto the same
-- satellite row WorkflowProjectionListener (com.aria.workflow.projection)
-- already projects for that transition.
--
-- Phase 1 scope: only the 15 tables driven by the GENERIC, config-driven
-- mechanism (every distinct target_table in m0_workflow_projection as of
-- this writing). Tables written by bespoke listeners/action handlers
-- (manager_qty_approval_detail, manager_weight_approval_detail,
-- billing_order_details, dispatch_invoice_item, unloading_progress_detail(_bay),
-- loading_progress_detail_bay, dispatch_parking_bay_entry, etc.) are a
-- separate follow-up -- NOT touched here. loading_progress_detail itself IS
-- included (it has its own generic config row too), but only the generic
-- mechanism's own field-mapped columns are affected; its several bespoke
-- writers (M10StartBayActionHandler etc.) are untouched.
--
-- See WorkflowProjectionListener.java for the matching code change: `time_taken`
-- is deliberately reused as the same literal field name across every one of
-- these pages, so it needs the same page-scoped pluck (pluckScoped(), not the
-- generic cross-page pluck()) already used for queue_reason/qc_decision/
-- w1_weight_kg -- otherwise a stale value from a DIFFERENT page still sitting
-- in the same instance's submitted-data bag could get picked up instead of
-- the one just submitted.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE billing_entry                    ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE dispatch_vehicle_detail          ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE gate_pass                        ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE gate_pass_closure                 ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE loading_allocation_detail        ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE loading_progress_detail          ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE m10_eway_bill                    ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE pmt_camera_verification_detail   ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE qc_final_outbound_detail         ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE qc_preload_detail                 ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE qc_unloading_approval            ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE sealing_process_detail           ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE tanker_unloading_detail          ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE unload_reconciliation_detail     ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;
ALTER TABLE weighment_capture_detail         ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;

-- Register the field on every entity code backed by one of these tables
-- (gate_pass has two: GatePass and DispatchEvent).
INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.entity_code),
       -1, c.entity_code, 'time_taken', 'Time Taken (min)', 'DECIMAL', 0, 1, 0, 0, 'number'
FROM (
  SELECT 'BillingEntry' entity_code UNION ALL
  SELECT 'DispatchVehicleDetail' UNION ALL
  SELECT 'GatePass' UNION ALL
  SELECT 'DispatchEvent' UNION ALL
  SELECT 'GatePassClosure' UNION ALL
  SELECT 'LoadingAllocationDetail' UNION ALL
  SELECT 'LoadingProgressDetail' UNION ALL
  SELECT 'EwayBill' UNION ALL
  SELECT 'PmtCameraVerificationDetail' UNION ALL
  SELECT 'QcFinalOutboundDetail' UNION ALL
  SELECT 'QcPreloadDetail' UNION ALL
  SELECT 'QcUnloadingApproval' UNION ALL
  SELECT 'SealingProcessDetail' UNION ALL
  SELECT 'TankerUnloadingDetail' UNION ALL
  SELECT 'UnloadReconciliationDetail' UNION ALL
  SELECT 'WeighmentCaptureDetail'
) c
WHERE NOT EXISTS (
  SELECT 1 FROM entity_field_registry WHERE entity_code = c.entity_code AND field_code = 'time_taken'
);

-- Extend every existing m0_workflow_projection config row targeting one of
-- these 15 tables so the generic listener starts mapping `time_taken` for
-- it -- accepts either snake_case or camelCase from the submitting page.
UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(COALESCE(field_map_json, '{}'), '$.time_taken', JSON_ARRAY('time_taken', 'timeTaken'))
WHERE target_table IN (
  'billing_entry','dispatch_vehicle_detail','gate_pass','gate_pass_closure',
  'loading_allocation_detail','loading_progress_detail','m10_eway_bill',
  'pmt_camera_verification_detail','qc_final_outbound_detail','qc_preload_detail',
  'qc_unloading_approval','sealing_process_detail','tanker_unloading_detail',
  'unload_reconciliation_detail','weighment_capture_detail'
);
