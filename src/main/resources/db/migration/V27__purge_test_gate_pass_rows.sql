-- V27 — Purge stale test-artifact gate passes from the table itself.
-- Earlier boots/probes/e2e-runs left junk gate_pass rows (gp_number
-- 'probe-…', 'GP-DEBUG-…', 'GP-E2E-…') that polluted every Gate-Pass picker.
-- Nothing in the shipped code recreates them, so the durable fix is to remove
-- the DATA rather than filter it out at query time on every lookup.
-- Idempotent: DELETE simply matches nothing on a clean DB.
--
-- Scoped strictly to unambiguous test prefixes so no real gate pass is touched.
DELETE FROM gate_pass
WHERE gp_number LIKE 'probe-%'
   OR gp_number LIKE 'GP-DEBUG-%'
   OR gp_number LIKE 'GP-E2E-%';
