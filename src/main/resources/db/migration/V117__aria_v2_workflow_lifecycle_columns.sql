-- V117 — Aria v2: add lifecycle-tracking columns to bound WORKFLOW tables that lack them
--
-- Instance-on-create (GenericEntityController.maybeStartWorkflow) only engages
-- when the entity's table has workflow_instance_id. Five WORKFLOW-class entities
-- bound 1:1 to a workflow were missing it (they tracked state in a plain `status`
-- column). Add the substrate lifecycle columns so their transactional lifecycle
-- is engine-driven too. Nullable + additive; existing rows are backfilled
-- separately. Pure schema add on WORKFLOW-class tables only.
ALTER TABLE m5_amc_contract   ADD COLUMN IF NOT EXISTS workflow_instance_id BIGINT NULL,
                              ADD COLUMN IF NOT EXISTS current_step VARCHAR(64) NULL;
ALTER TABLE m7_appraisal_form ADD COLUMN IF NOT EXISTS workflow_instance_id BIGINT NULL,
                              ADD COLUMN IF NOT EXISTS current_step VARCHAR(64) NULL;
ALTER TABLE material_return   ADD COLUMN IF NOT EXISTS workflow_instance_id BIGINT NULL,
                              ADD COLUMN IF NOT EXISTS current_step VARCHAR(64) NULL;
ALTER TABLE m7_payroll_run    ADD COLUMN IF NOT EXISTS workflow_instance_id BIGINT NULL,
                              ADD COLUMN IF NOT EXISTS current_step VARCHAR(64) NULL;
ALTER TABLE m5_subscription   ADD COLUMN IF NOT EXISTS workflow_instance_id BIGINT NULL,
                              ADD COLUMN IF NOT EXISTS current_step VARCHAR(64) NULL;
