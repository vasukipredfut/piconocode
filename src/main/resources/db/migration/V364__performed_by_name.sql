-- The login user id (V363) says who submitted; this adds WHO ACTUALLY DID THE WORK, because every page now shows an
-- editable "performed by" name (pre-filled with the logged-in user, but someone else may have done the job):
--   * <page table>.performed_by_name (VARCHAR 128) on every table V363 stamped -- the form field "performed_by_name";
--   * aria:weighment-capture (submitted for the 1st / 2nd / 3rd weight, often by different officers) keeps each
--     weight's officer separately: w1/w2/w3_by_name (the page's performed_by_name, else its "Weighment Officer
--     Name") and w1/w2/w3_contact (its "Weighment Officer Contact Number");
--   * bay rows: started_by_name / weighed_by_name (loading) and started_by_name / unloaded_by_name (unloading), set by
--     the bay actions from the "performed_by_name" in the payload (line, else the form).
-- page_submitter_map gets name_column / contact_column so wiring stays data-driven.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE page_submitter_map
  ADD COLUMN IF NOT EXISTS name_column    VARCHAR(64) NULL DEFAULT 'performed_by_name',
  ADD COLUMN IF NOT EXISTS contact_column VARCHAR(64) NULL DEFAULT NULL;

ALTER TABLE IF EXISTS gate_pass                            ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS dispatch_vehicle_detail              ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS qc_preload_detail                    ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS qc_unloading_approval                ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS loading_allocation_detail            ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS sealing_process_detail               ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS pmt_weight_diff_detail               ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS pmt_camera_verification_detail       ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS manager_qty_approval_detail          ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS manager_weight_approval_detail       ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS unload_reconciliation_detail         ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS gate_pass_closure                    ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS billing_entry                        ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS qc_final_outbound_detail             ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS qc_officer_final_check_detail        ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS dispatch_officer_verification_detail ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS loading_progress_detail              ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS unloading_progress_detail            ADD COLUMN IF NOT EXISTS performed_by_name VARCHAR(128) NULL;

ALTER TABLE IF EXISTS weighment_capture_detail
  ADD COLUMN IF NOT EXISTS w1_by_name VARCHAR(128) NULL,
  ADD COLUMN IF NOT EXISTS w2_by_name VARCHAR(128) NULL,
  ADD COLUMN IF NOT EXISTS w3_by_name VARCHAR(128) NULL,
  ADD COLUMN IF NOT EXISTS w1_contact VARCHAR(32)  NULL,
  ADD COLUMN IF NOT EXISTS w2_contact VARCHAR(32)  NULL,
  ADD COLUMN IF NOT EXISTS w3_contact VARCHAR(32)  NULL;

ALTER TABLE IF EXISTS loading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS started_by_name VARCHAR(128) NULL,
  ADD COLUMN IF NOT EXISTS weighed_by_name VARCHAR(128) NULL;
ALTER TABLE IF EXISTS unloading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS started_by_name  VARCHAR(128) NULL,
  ADD COLUMN IF NOT EXISTS unloaded_by_name VARCHAR(128) NULL;

-- the weighment page: name + contact per weight
UPDATE page_submitter_map
   SET name_column = 'w#_by_name', contact_column = 'w#_contact'
 WHERE page_code = 'aria:weighment-capture' AND table_name = 'weighment_capture_detail';
