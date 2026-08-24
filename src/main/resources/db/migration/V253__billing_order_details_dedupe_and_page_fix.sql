-- billing_order_details (V192) was never actually written to reliably:
-- BillingOrderDetailTransitionListener hard-coded a lookup at
-- bag.get("aria:skipped-item-allocation") -- but confirmed live (a real
-- workflow.transition payload, pageCode=aria:billing-entry, instance 1776),
-- the submitted "loaded_sku_details" array rides under whatever page is
-- CURRENTLY submitting (this app's Renderer carries one big accumulated
-- form-state object forward across pages), not under a literal
-- "aria:skipped-item-allocation" bag key. Net effect: the listener almost
-- never found data to write.
--
-- Fixed in code (this same change) to resolve the actual triggering page
-- per-transition (matching WorkflowProjectionListener's own
-- resolveTriggeringPageCode pattern against workflow_definition.transitions[])
-- and read loaded_sku_details from THAT page's bag entry instead of a fixed
-- key -- which also means it can now find real data on more transitions
-- than before, so a write-once instance-level guard is no longer safe
-- (legitimate repeat submissions, e.g. correcting a line's qty, must still
-- land). Switching to a per-line UNIQUE key + INSERT .. ON DUPLICATE KEY
-- UPDATE instead: idempotent re-submission of the same (instance, order,
-- sku) line updates it in place rather than creating a duplicate row.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE billing_order_details
  ADD UNIQUE KEY IF NOT EXISTS uq_bod_instance_order_sku (tenant_id, workflow_instance_id, order_id, sku_code);
