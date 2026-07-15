-- V121 — Aria v2: add reject_reason to workflow_instance
-- The registration seed lists WorkflowInstance.reject_reason, but the table
-- lacked the column, so it read as a phantom field. Instances DO get rejected
-- (REJECTED_BY_MANAGER etc.), so the column is legitimate — add it (rather than
-- keep deleting the field, which the idempotent seed re-adds on every reboot).
ALTER TABLE workflow_instance ADD COLUMN IF NOT EXISTS reject_reason VARCHAR(512) NULL;
