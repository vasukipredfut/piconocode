-- V233 — follow-up to V232: that migration only covered gate_pass and
-- dispatch_vehicle_detail. Checked live against m0_workflow_projection,
-- most of the 10 queue-enabled pages actually feed a dedicated per-page
-- satellite table (same convention as dispatch_vehicle_detail — one row
-- per workflow_instance_id) IN ADDITION to gate_pass, e.g. qc-preload-
-- approval's own loading_supervisor/loading_tank_location/qc_reject_reason
-- fields were deliberately moved OUT of gate_pass into qc_preload_detail
-- (see V98) — so queue_reason needs to land there too, not just gate_pass.
--
-- aria:front-gate-create / aria:weight-diff-entry / aria:pmt-weight-diff-check
-- have no page_code-scoped satellite row at all (checked live) — they only
-- ever project into gate_pass, already covered by V232, so no table to add
-- here for them.
--
-- aria:weighment-capture -> weighment_capture_detail included too (6
-- workflow_code rows live) even though this particular page's Queue widget
-- is separately missing on at least one environment (V220's REPLACE()
-- silently no-op'd against pretty-printed JSON) — that's a display-wiring
-- bug tracked separately; the reason column/mapping is still correct to
-- have ready for whenever that queue is visible again.
--
-- pmt_weight_diff_detail / manager_qty_approval_detail /
-- manager_weight_approval_detail (aria:pmt-weight-diff-check,
-- aria:manager-qty-approval, aria:manager-weight-approval): these three
-- satellite tables are NOT driven by m0_workflow_projection at all — they're
-- populated by bespoke Java in PmtSharedReasonListener (routing the
-- shared-field-name 'variance_reason'/'status' precisely by previousState,
-- to avoid exactly the cross-page ambiguity a generic field_map would hit).
-- Column only for now — wiring queue_reason through that listener is a
-- separate follow-up, deliberately not done here.

ALTER TABLE qc_preload_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE qc_unloading_approval ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE loading_allocation_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE sealing_process_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE pmt_camera_verification_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE billing_entry ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE weighment_capture_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE pmt_weight_diff_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE manager_qty_approval_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE manager_weight_approval_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;

UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(
  field_map_json,
  JSON_OBJECT('queue_reason', JSON_ARRAY('queue_reason', 'queueReason', 'queueSkipReason', 'queue_skip_reason'))
)
WHERE target_table IN (
    'qc_preload_detail', 'qc_unloading_approval', 'loading_allocation_detail',
    'sealing_process_detail', 'pmt_camera_verification_detail', 'billing_entry',
    'weighment_capture_detail'
  )
  AND page_code IN (
    'aria:qc-preload-approval', 'aria:qc-approval', 'aria:loading-allocation',
    'aria:sealing-process', 'aria:pmt-camera-verification', 'aria:billing-entry',
    'aria:weighment-capture'
  )
  AND field_map_json IS NOT NULL
  AND JSON_VALID(field_map_json)
  AND JSON_EXTRACT(field_map_json, '$.queue_reason') IS NULL;
