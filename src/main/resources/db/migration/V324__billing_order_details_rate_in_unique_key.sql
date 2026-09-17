-- Business ask (Dhivya, 2026-09-17): the same Order + same SKU can
-- legitimately appear as two separate billing lines when they carry
-- DIFFERENT order_rate (e.g. two separately-priced batches of the same
-- product under one order) -- those must save as two rows, each keeping its
-- own order_qty. When the rate is the SAME, a repeat submission should
-- still collapse into one row (correction/resubmission), same as today.
--
-- billing_order_details' current UNIQUE key (V253) is
-- (tenant_id, workflow_instance_id, order_id, sku_code) -- no order_rate,
-- so two lines for the same order+SKU always collided into one row
-- regardless of rate, silently losing whichever entry lost the race.
--
-- Fix: add order_rate to the unique key. This requires the column to never
-- be NULL first -- MySQL/MariaDB unique keys treat NULL <> NULL (two NULLs
-- never collide), so leaving it nullable would make every
-- BillingOrderDetailTransitionListener#onTransition loaded_sku_details write
-- (which never carries a real rate) start creating a NEW row on every
-- resubmission instead of updating the existing one. Normalizing to
-- NOT NULL DEFAULT 0 (backfilling any existing NULLs first) keeps that path's
-- existing "same order+SKU resubmission collapses to one row" behavior
-- intact, while the Billing Table path (which does carry a real, sometimes-
-- differing rate) now gets a genuinely separate row per distinct rate.

UPDATE billing_order_details SET order_rate = 0 WHERE order_rate IS NULL;

ALTER TABLE billing_order_details
  MODIFY COLUMN order_rate DECIMAL(12,3) NOT NULL DEFAULT 0;

ALTER TABLE billing_order_details
  DROP INDEX uq_bod_instance_order_sku,
  ADD UNIQUE KEY uq_bod_instance_order_sku_rate (tenant_id, workflow_instance_id, order_id, sku_code, order_rate);
