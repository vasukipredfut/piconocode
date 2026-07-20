-- V45__packloading_reconcile_with_team.sql
-- Reconciles our M10.PackLoading operator fixes with the team's V37-V44
-- (loading-point master, front-gate + QC rewrites, sku master, etc.).
-- Runs AFTER the team's V44, so it patches THEIR page bodies. Idempotent
-- REPLACE()s (no-op if the target substring is absent). Generic.

-- 1) QC page (team V43) — re-add the guard values the Approve/Reject buttons
--    need. The QC transition guard is #submitted['status']=='APPROVED' /
--    'REJECTED'; without these submitValues the button submits no status and
--    the guard rejects with 409 (QC approval breaks).
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json,
        '"targetState": "QC_PRELOAD_APPROVED", "color": "primary"}',
        '"targetState": "QC_PRELOAD_APPROVED", "color": "primary", "submitValues": {"status": "APPROVED", "qc_status": "APPROVED"}}')
 WHERE pd.code = 'aria:qc-preload-approval';
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json,
        '"targetState": "QC_PRELOAD_REJECTED", "color": "error"}',
        '"targetState": "QC_PRELOAD_REJECTED", "color": "error", "submitValues": {"status": "REJECTED", "qc_status": "REJECTED"}}')
 WHERE pd.code = 'aria:qc-preload-approval';

-- 2) QC page — show the Loading Supervisor field for packing/FG loading too
--    (team gated it to M10.TankerOutbound only).
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json,
        '#form.flow_variant_code == ''M10.TankerOutbound''',
        '#form.flow_variant_code == ''M10.TankerOutbound'' || #form.flow_variant_code == ''M10.PackLoading'' || #form.flow_variant_code == ''M10.FGLoading''')
 WHERE pd.code = 'aria:qc-preload-approval';

-- 3) Front Gate (team V40) — flow-neutral wording, not "Tanker Intake".
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(REPLACE(pv.body_json,
        'Tanker Intake', 'Vehicle Intake'),
        'tanker-inbound flow', 'selected flow')
 WHERE pd.code = 'aria:front-gate-create';
UPDATE page_definition SET description = 'Gate-pass creation for any inbound or outbound vehicle'
 WHERE code = 'aria:front-gate-create';

-- 4) Drop our redundant LoadingBay master — the team's LoadingPoint master
--    (Pouch sections, tin godowns, jar/dispatch) is the canonical one, and the
--    QC location dropdown already points at LoadingPoint.
DELETE FROM entity_field_registry WHERE entity_code = 'LoadingBay';
DELETE FROM entity_registry WHERE code = 'LoadingBay';
DROP TABLE IF EXISTS dispatch_loading_bay;
