-- V232 — "queue_reason": when an operator picks a queue card that isn't the
-- first (oldest) item in a WorkflowQueueList — see /api/workflow/queue-for-page,
-- sorted oldest-first — they're expected to record why they skipped ahead.
-- The frontend is responsible for detecting the out-of-order pick and adding
-- the actual form field; this migration only prepares the backend side so
-- that field, once submitted, lands in the right place with zero Java code —
-- WorkflowProjectionListener (see its class javadoc) is already a generic,
-- config-driven field_map_json -> column mapper keyed off m0_workflow_projection.
--
-- Every queue-enabled page currently resolves to one of two entities
-- (checked live against page_version.body_json's top-level "entity" —
-- aria:qc-approval has none, so it's out of scope: no backing table to
-- write a reason into):
--   GatePass             -> gate_pass table
--   DispatchVehicleDetail -> dispatch_vehicle_detail table
-- so both tables get the column, and every existing projection row that
-- targets either one picks up the new field mapping via JSON_MERGE_PATCH
-- (additive — doesn't disturb any field already mapped).

ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;
ALTER TABLE dispatch_vehicle_detail ADD COLUMN IF NOT EXISTS queue_reason VARCHAR(255) NULL;

UPDATE m0_workflow_projection
SET field_map_json = JSON_MERGE_PATCH(
  field_map_json,
  JSON_OBJECT('queue_reason', JSON_ARRAY('queue_reason', 'queueReason', 'queueSkipReason', 'queue_skip_reason'))
)
WHERE target_table IN ('gate_pass', 'dispatch_vehicle_detail')
  AND field_map_json IS NOT NULL
  AND JSON_VALID(field_map_json)
  AND JSON_EXTRACT(field_map_json, '$.queue_reason') IS NULL;
