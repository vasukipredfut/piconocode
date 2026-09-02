-- M10.PackUnload's gate_pass projection row had no tolerance_pct set, so
-- DispatchToleranceListener never activated for it -- expected_weight_kg,
-- loaded_weight_kg, pmt_variance_pct and pmt_within_tolerance on
-- manager_qty_approval_detail were never computed, even though
-- aria:manager-qty-approval's own page shows client-computed versions of all
-- four. Same fix already applied to M10.FGUnloading (V278) -- 0.400 (0.4%)
-- matches the convention already used by FGLoading/PackLoading/TankerOutbound.
-- PackUnload already has both source rows DispatchToleranceListener needs
-- (weighment_capture_detail for w1/w2_weight_kg, loading_allocation_detail
-- for ordered_qty), so this alone is enough to switch it on.
--
-- Guarded on "tolerance_pct IS NULL" so this never overwrites a value someone
-- deliberately changed after this migration first ran -- idempotent, safe to
-- (re)run on any database.

UPDATE m0_workflow_projection
SET tolerance_pct = 0.400
WHERE tenant_id = -1 AND workflow_code = 'M10.PackUnload' AND target_table = 'gate_pass' AND tolerance_pct IS NULL;
