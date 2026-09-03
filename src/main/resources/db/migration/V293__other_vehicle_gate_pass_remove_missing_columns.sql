-- M10.OtherVehicles' gate_pass field_map_json (V259) mapped 7 fields that
-- were never given a matching column on gate_pass at all:
-- w1_weight_kg/w2_weight_kg (already correctly captured on
-- weighment_capture_detail, V267/V286/V289 -- duplicated here by mistake),
-- and invoice_no/invoice_qty/seal_verified/weighment_verified/
-- invoice_count_verified (these look copy-pasted from
-- gate_pass_closure's own field_map, V120/V259 -- aria:weighment-capture-
-- other-vehicle never even has form fields with these names).
--
-- Confirmed live: MariaDB's UPDATE...SET fails ATOMICALLY on the first
-- unknown column ("Unknown column 'w1_weight_kg' in 'SET'") -- since
-- current_step is bundled in the SAME UPDATE statement (state_columns
-- mechanism, WorkflowProjectionListener), this one broken key silently
-- blocked gate_pass.current_step from EVER updating for M10.OtherVehicles,
-- on every single transition, since V259 first shipped.

UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json,
  '$.w1_weight_kg', '$.w2_weight_kg', '$.invoice_no', '$.invoice_qty',
  '$.seal_verified', '$.weighment_verified', '$.invoice_count_verified')
WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'gate_pass'
  AND field_map_json IS NOT NULL AND JSON_VALID(field_map_json);
