-- aria:weighment-capture-other-vehicle writes its material / invoice lines to weighment_other_vehicle_line_item
-- (one row per line, replaced on every submit by OtherVehicleLineItemTransitionListener). Wire it into the
-- page-submitter stamping (V363 / V364): the logged-in user id + the "performed by" name go on every line of that
-- submission. PageSubmitterStamper runs after the listeners, so the freshly re-inserted lines are the ones stamped.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS weighment_other_vehicle_line_item
  ADD COLUMN IF NOT EXISTS submitted_by_id    BIGINT       NULL,
  ADD COLUMN IF NOT EXISTS performed_by_name  VARCHAR(128) NULL;

INSERT IGNORE INTO page_submitter_map (page_code, table_name, key_column, user_column, name_column)
VALUES ('aria:weighment-capture-other-vehicle', 'weighment_other_vehicle_line_item', 'workflow_instance_id', 'submitted_by_id', 'performed_by_name');

-- The same page also saves the vehicle's weights into weighment_capture_detail (w1_weight_kg / w2_weight_kg, V267 /
-- V286 / V288): it is submitted twice for M10.OtherVehicles (first weight DRAFT -> WEIGHMENT_1, second weight
-- LOADING_UNLOADING_IN_PROGRESS -> WEIGHMENT_2), carrying its own weight_seq. Give each weight its officer, like
-- aria:weighment-capture: w1/w2_by_id, w1/w2_by_name (performed_by_name, else the page's weight_officer) and
-- w1/w2_contact (weight_officer_contact when the page sends one). Columns already exist (V363 / V364).
INSERT IGNORE INTO page_submitter_map (page_code, table_name, key_column, user_column, name_column, contact_column)
VALUES ('aria:weighment-capture-other-vehicle', 'weighment_capture_detail', 'workflow_instance_id', 'w#_by_id', 'w#_by_name', 'w#_contact');
