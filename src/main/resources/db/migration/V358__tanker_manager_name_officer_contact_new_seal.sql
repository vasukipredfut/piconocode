-- Three small tanker-flow additions:
--   1. aria:manager-qty-approval  -> manager_qty_approval_detail.manager_name   (who approved / rejected;
--      the UI sends the logged-in manager's name as "manager_name").
--   2. aria:weighment-capture     -> weighment_capture_detail.weight_officer_contact  (weighment officer's
--      contact no; UI field name "weight_officer_contact").
--   3. aria:gate-pass-closure-form -> gate_pass_closure.new_seal_no  (when the seal check says MISMATCH the
--      officer types the replacement seal number; UI field name "new_seal_no").
-- manager_name is written by PmtSharedReasonListener / ManagerQtyApprovalDecisionListener (page-scoped, see their
-- javadoc for why not the generic field map); the other two ride the generic WorkflowProjectionListener field_map.
-- All three are exposed on a plain GatePass read via GatePassSatelliteJoin.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE manager_qty_approval_detail
  ADD COLUMN IF NOT EXISTS manager_name VARCHAR(128) DEFAULT NULL;
ALTER TABLE weighment_capture_detail
  ADD COLUMN IF NOT EXISTS weight_officer_contact VARCHAR(32) DEFAULT NULL;
ALTER TABLE gate_pass_closure
  ADD COLUMN IF NOT EXISTS new_seal_no VARCHAR(64) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ec, c.fc),
       -1, c.ec, c.fc, c.lbl, 'STRING', 1, 1, 1, 0, 'text'
FROM (SELECT 'ManagerQtyApprovalDetail' ec, 'manager_name' fc, 'Approved By (Manager)' lbl
      UNION ALL SELECT 'WeighmentCaptureDetail', 'weight_officer_contact', 'Weighment Officer Contact No'
      UNION ALL SELECT 'GatePassClosure', 'new_seal_no', 'New Seal No (on seal mismatch)') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = c.ec AND field_code = c.fc);

-- weighment contact + new seal number go through the generic projection (same tables/rows the page already uses).
UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json,
      '$.weight_officer_contact', JSON_ARRAY('weight_officer_contact', 'weightOfficerContact'))
WHERE target_table = 'weighment_capture_detail'
  AND workflow_code IN ('M10.TankerOutbound', 'M10.TankerInbound');

UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json,
      '$.new_seal_no', JSON_ARRAY('new_seal_no', 'newSealNo'))
WHERE target_table = 'gate_pass_closure'
  AND workflow_code IN ('M10.TankerOutbound', 'M10.TankerInbound');
