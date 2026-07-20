-- V39__packloading_copy_fixes.sql
-- Page-by-page QA (2026-07-20): operator-facing copy fixes on the M10.PackLoading
-- flow pages. Removes leftover tanker-inbound wording and raw workflow state
-- codes from text the operator reads. Targeted REPLACE()s (idempotent: a REPLACE
-- of an absent substring is a no-op) on the current published version of each
-- page, plus friendlier page descriptions. Generic — applies to every tenant.

-- Front Gate (shared entry page) — flow-neutral wording, not "Tanker Intake".
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(REPLACE(pv.body_json,
         'Tanker Intake', 'Vehicle Intake'),
         'tanker-inbound flow', 'selected flow')
 WHERE pd.code = 'aria:front-gate-create';
UPDATE page_definition SET description = 'Gate-pass creation for any inbound or outbound vehicle'
 WHERE code = 'aria:front-gate-create';

-- Weighment Capture — drop "tanker" from the seal-lock label; fix the
-- contradictory page description (it read tanker-inbound "loaded on entry,
-- empty on exit … not oil W1/W2/W3", which contradicts the packing sequence).
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json, '2nd tanker weight', 'at 2nd weight')
 WHERE pd.code = 'aria:weighment-capture';
UPDATE page_definition SET description = 'Weighbridge reading per stage. Packing loading: W1 = empty/tare, W2 = loaded, W3 = exit.'
 WHERE code = 'aria:weighment-capture';

-- Manager Weight Approval — remove raw state code WEIGHMENT_3 from operator copy.
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json, 'showing WEIGHMENT_3', 'at its final (exit) weighment')
 WHERE pd.code = 'aria:manager-weight-approval';

-- Manager Qty Approval — remove raw state code PMT_CHECK_PENDING_MANAGER.
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json, 'gate pass showing PMT_CHECK_PENDING_MANAGER', 'gate pass the system flagged')
 WHERE pd.code = 'aria:manager-qty-approval';

-- Gate Pass Closure — packing example instead of a tanker-unloading example.
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(REPLACE(pv.body_json,
         'Tanker Unloading', 'Packing loading'),
         '9,120 kg into TANK-1', '2,315 kg loaded')
 WHERE pd.code = 'aria:gate-pass-closure-form';

-- Unload & Recheck — page description was cloned from the Camera page; make it
-- match what the page actually does (record shortage/excess, then reload).
UPDATE page_definition SET description = 'Unload the FG stock, record the shortage/excess, then send for re-weigh + reload.'
 WHERE code = 'aria:pmt-unload-recheck';
