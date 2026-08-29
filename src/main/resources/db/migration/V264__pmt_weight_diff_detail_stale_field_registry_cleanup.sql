-- V263 redirected the 4 computed tolerance columns (expected_weight_kg,
-- loaded_weight_kg, pmt_variance_pct, pmt_within_tolerance) off
-- pmt_weight_diff_detail onto manager_qty_approval_detail -- nothing writes
-- fresh values into those 4 columns on pmt_weight_diff_detail anymore.
--
-- pmt_weight_diff_detail ITSELF is NOT dropped by this migration (see
-- PmtSharedReasonListener.java for the follow-up: both consuming pages,
-- aria:pmt-weight-diff-check AND aria:weight-diff-entry, are being retired
-- from the UI, so nothing writes pmt_variance_reason/queue_reason there
-- either anymore -- the table is left as a frozen historical record rather
-- than dropped).
--
-- This migration just removes the now-stale entity_field_registry rows the
-- V92 migration added for those 4 columns on the PmtWeightDiffDetail entity,
-- so a PmtWeightDiffDetail read no longer advertises fields nothing keeps
-- current anymore. The underlying columns are left in place (frozen,
-- historical) rather than dropped -- pmt_weight_diff_detail is still a live
-- table for a different purpose, and dropping columns off a live table is
-- unnecessary risk for a purely cosmetic cleanup.
--
-- Idempotent -- safe to (re)run on any database.

DELETE FROM entity_field_registry
WHERE entity_code = 'PmtWeightDiffDetail'
  AND field_code IN ('expected_weight_kg', 'loaded_weight_kg', 'pmt_variance_pct', 'pmt_within_tolerance');
