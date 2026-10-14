-- Who submitted each page: the logged-in user's id, stored on that page's own table.
--
--  * page_submitter_map  : page_code -> table / key column / user column. PageSubmitterStamper (called by the
--                          workflow engine after every transition) reads it, so wiring another page is just a
--                          new row here + its column -- no code change.
--  * <table>.submitted_by_id (BIGINT, the JWT user id) on every page table below.
--  * weighment_capture_detail.w1_by_id / w2_by_id / w3_by_id : first / second / third weight, separately
--    (the page is submitted up to three times, often by different officers).
--  * bay rows, every flow (FG / Pack / Stock inward+outward / Tanker / reject-unload), stamped by the bay action
--    handlers (RowActorStamp), not by the engine:
--      loading_progress_detail_bay   : started_by_id (Start Bay)  / weighed_by_id  (Bay Complete, tanker weight Save)
--      unloading_progress_detail_bay : started_by_id (Start Bay)  / unloaded_by_id (unload / receipt)
--
-- Pages whose table is not known / not keyed by workflow_instance_id (aria:loading-planning-order-sheet-upload,
-- aria:supervisor-unloading, aria:parking-scan, aria:dispatch-vehicle-entry, aria:stock-checking-officer,
-- aria:skipped-item-allocation; aria:weighment-capture-other-vehicle is mapped in V365) are not mapped yet: add a row to
-- page_submitter_map and the column to its table.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS page_submitter_map (
  page_code   VARCHAR(128) NOT NULL,
  table_name  VARCHAR(64)  NOT NULL,
  key_column  VARCHAR(64)  NOT NULL DEFAULT 'workflow_instance_id',
  user_column VARCHAR(64)  NOT NULL DEFAULT 'submitted_by_id',
  PRIMARY KEY (page_code, table_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- 1. the user column on every page table
ALTER TABLE IF EXISTS gate_pass                          ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS dispatch_vehicle_detail            ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS qc_preload_detail                  ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS qc_unloading_approval              ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS loading_allocation_detail          ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS sealing_process_detail             ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS pmt_weight_diff_detail             ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS pmt_camera_verification_detail     ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS manager_qty_approval_detail        ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS manager_weight_approval_detail     ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS unload_reconciliation_detail       ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS gate_pass_closure                  ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS billing_entry                      ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS qc_final_outbound_detail           ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS qc_officer_final_check_detail      ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS dispatch_officer_verification_detail ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS loading_progress_detail            ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;
ALTER TABLE IF EXISTS unloading_progress_detail          ADD COLUMN IF NOT EXISTS submitted_by_id BIGINT NULL;

ALTER TABLE IF EXISTS weighment_capture_detail
  ADD COLUMN IF NOT EXISTS w1_by_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS w2_by_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS w3_by_id BIGINT NULL;

-- bay rows (every flow): who started the bay, who finished it
ALTER TABLE IF EXISTS loading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS started_by_id BIGINT NULL,
  ADD COLUMN IF NOT EXISTS weighed_by_id BIGINT NULL;
ALTER TABLE IF EXISTS unloading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS started_by_id  BIGINT NULL,
  ADD COLUMN IF NOT EXISTS unloaded_by_id BIGINT NULL;

-- 2. page -> table
INSERT IGNORE INTO page_submitter_map (page_code, table_name, key_column, user_column) VALUES
  ('aria:front-gate-create',                 'gate_pass',                          'workflow_instance_id', 'submitted_by_id'),
  ('aria:dispatch-vehicle-details',          'dispatch_vehicle_detail',            'workflow_instance_id', 'submitted_by_id'),
  ('aria:qc-preload-approval',               'qc_preload_detail',                  'workflow_instance_id', 'submitted_by_id'),
  ('aria:qc-approval',                       'qc_unloading_approval',              'workflow_instance_id', 'submitted_by_id'),
  ('aria:loading-allocation',                'loading_allocation_detail',          'workflow_instance_id', 'submitted_by_id'),
  ('aria:weighment-capture',                 'weighment_capture_detail',           'workflow_instance_id', 'w#_by_id'),
  ('aria:sealing-process',                   'sealing_process_detail',             'workflow_instance_id', 'submitted_by_id'),
  ('aria:pmt-weight-diff-check',             'pmt_weight_diff_detail',             'workflow_instance_id', 'submitted_by_id'),
  ('aria:weight-diff-entry',                 'pmt_weight_diff_detail',             'workflow_instance_id', 'submitted_by_id'),
  ('aria:pmt-camera-verification',           'pmt_camera_verification_detail',     'workflow_instance_id', 'submitted_by_id'),
  ('aria:manager-qty-approval',              'manager_qty_approval_detail',        'workflow_instance_id', 'submitted_by_id'),
  ('aria:manager-weight-approval',           'manager_weight_approval_detail',     'workflow_instance_id', 'submitted_by_id'),
  ('aria:reconciliation',                    'unload_reconciliation_detail',       'workflow_instance_id', 'submitted_by_id'),
  ('aria:gate-pass-closure',                 'gate_pass_closure',                  'workflow_instance_id', 'submitted_by_id'),
  ('aria:gate-pass-closure-form',            'gate_pass_closure',                  'workflow_instance_id', 'submitted_by_id'),
  ('aria:billing-entry',                     'billing_entry',                      'workflow_instance_id', 'submitted_by_id'),
  ('aria:qc-final-out',                      'qc_final_outbound_detail',           'workflow_instance_id', 'submitted_by_id'),
  ('aria:qc-officer-final-check',            'qc_officer_final_check_detail',      'workflow_instance_id', 'submitted_by_id'),
  ('aria:dispatch-officer-verification',     'dispatch_officer_verification_detail','workflow_instance_id', 'submitted_by_id'),
  ('aria:loading-in-progress',               'loading_progress_detail',            'workflow_instance_id', 'submitted_by_id'),
  ('aria:loading-in-progress-entry',         'loading_progress_detail',            'workflow_instance_id', 'submitted_by_id'),
  ('aria:unloading-in-progress',             'unloading_progress_detail',          'workflow_instance_id', 'submitted_by_id'),
  ('aria:unloading-in-progress-entry',       'unloading_progress_detail',          'workflow_instance_id', 'submitted_by_id'),
  ('aria:unloading-progress',                'unloading_progress_detail',          'workflow_instance_id', 'submitted_by_id'),
  ('aria:reject-flow-unloading',             'unloading_progress_detail',          'workflow_instance_id', 'submitted_by_id');
