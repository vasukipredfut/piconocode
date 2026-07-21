-- V65__weight_diff_entry_restore_submitvalues.sql
-- Same bug class as V63 (aria:qc-preload-approval), same root cause
-- (V58__m10_remaining_gpnumber_datasource_filters.sql's full body_json
-- rewrite of aria:weight-diff-entry), found by auditing every workflow-guarded
-- WorkflowActionButton against the guard it must satisfy, and confirmed live:
--
--   POST /api/workflow/transition on aria:weight-diff-entry (M10.TankerOutbound,
--   state PARKING_IN_2) returned:
--     "All guards rejected the transition. state=PARKING_IN_2
--      page=aria:weight-diff-entry
--      tried-guards=[#submitted['withinTolerance'] == 'YES' ||
--                    #submitted['withinTolerance'] == 'NO']
--      submitted-fields=[vehicle_no,flow_variant_code,instanceId,w2_weight_kg,
--                        gpNumber,w3_weight_kg,variance_reason]"
--
-- No "withinTolerance" field exists anywhere on this page (unlike its PMT
-- sibling aria:pmt-weight-diff-check, which auto-computes one) -- the two
-- WorkflowActionButtons ("Weight OK" / "Needs Manager") never had a way to
-- supply it. Add submitValues so each button asserts its own verdict.
UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json,
        '"targetState": "WEIGHT_DIFF_OK", "color": "primary"}',
        '"targetState": "WEIGHT_DIFF_OK", "color": "primary", "submitValues": {"withinTolerance": "YES"}}')
 WHERE pd.code = 'aria:weight-diff-entry';

UPDATE page_version pv JOIN page_definition pd ON pv.id = pd.current_version_id
   SET pv.body_json = REPLACE(pv.body_json,
        '"targetState": "WEIGHT_DIFF_PENDING_MANAGER", "color": "warning"}',
        '"targetState": "WEIGHT_DIFF_PENDING_MANAGER", "color": "warning", "submitValues": {"withinTolerance": "NO"}}')
 WHERE pd.code = 'aria:weight-diff-entry';
