-- V265 added time_taken as DECIMAL(10,2), documented as "elapsed minutes"
-- computed client-side. In practice the UI submits an HH:MM:SS duration
-- string (e.g. "00:01:12"), not a decimal minute count -- WorkflowProjectionListener
-- (V265's own code change) parsed that string with new BigDecimal(v), which
-- throws on "00:01:12" and silently skipped the column (never saved).
--
-- Per product decision: save time_taken exactly as the UI sends it, not
-- reinterpreted into decimal minutes. TIME is the natural MariaDB column
-- type for an "HH:MM:SS" duration string -- it stores/round-trips the exact
-- value handed in (up to 838:59:59, well beyond any real elapsed-step
-- duration) and sorts/compares correctly, unlike a free-text VARCHAR.
--
-- Same 15 tables V265 touched.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE billing_entry                  MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE dispatch_vehicle_detail        MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE gate_pass                      MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE gate_pass_closure              MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE loading_allocation_detail      MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE loading_progress_detail        MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE m10_eway_bill                  MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE pmt_camera_verification_detail MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE qc_final_outbound_detail       MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE qc_preload_detail              MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE qc_unloading_approval          MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE sealing_process_detail         MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE tanker_unloading_detail        MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE unload_reconciliation_detail   MODIFY COLUMN time_taken TIME DEFAULT NULL;
ALTER TABLE weighment_capture_detail       MODIFY COLUMN time_taken TIME DEFAULT NULL;

UPDATE entity_field_registry
SET data_type = 'STRING', widget_kind = 'text', display_label = 'Time Taken (hh:mm:ss)'
WHERE field_code = 'time_taken'
  AND entity_code IN (
    'BillingEntry','DispatchVehicleDetail','GatePass','DispatchEvent','GatePassClosure',
    'LoadingAllocationDetail','LoadingProgressDetail','EwayBill','PmtCameraVerificationDetail',
    'QcFinalOutboundDetail','QcPreloadDetail','QcUnloadingApproval','SealingProcessDetail',
    'TankerUnloadingDetail','UnloadReconciliationDetail','WeighmentCaptureDetail'
  );
