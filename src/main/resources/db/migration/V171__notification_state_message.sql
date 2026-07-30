-- m0_notification (V141) only ever carried the raw submitted-data bag
-- (payload_json) plus assigned_role — no current_state, so a notification
-- consumer had no way to show what step the instance is waiting at without
-- re-deriving it from payload_json. Per direct request: add current_state
-- only (assigned_role already existed).
ALTER TABLE m0_notification
  ADD COLUMN IF NOT EXISTS current_state VARCHAR(64) DEFAULT NULL AFTER assigned_role;
