-- aria:manager-qty-approval shows 5 GRN detail fields (grn_number/grn_date/
-- grn_not_required_reason/grn_exception_reason/grn_updated_by) for
-- M10.PackUnload/M10.FGUnloading, alongside grn_status which already saves
-- (PmtSharedReasonListener, QUANTITY_APPROVAL_PENDING case). None of these 5
-- had a column here or anywhere else -- only grn_status ever made it out of
-- workflow_instance.submitted_data_json.

ALTER TABLE manager_qty_approval_detail
  ADD COLUMN IF NOT EXISTS grn_number VARCHAR(64) DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS grn_date DATE DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS grn_not_required_reason TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS grn_exception_reason TEXT DEFAULT NULL,
  ADD COLUMN IF NOT EXISTS grn_updated_by VARCHAR(128) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'ManagerQtyApprovalDetail' b,'grn_number' c,'GRN Number' d,'STRING' e,1 f,1 g,1 h,0 i,'text' j UNION ALL
  SELECT -1,'ManagerQtyApprovalDetail','grn_date','GRN Date','DATE',1,1,0,0,'date' UNION ALL
  SELECT -1,'ManagerQtyApprovalDetail','grn_not_required_reason','GRN Not Required Reason','STRING',0,0,1,0,'text' UNION ALL
  SELECT -1,'ManagerQtyApprovalDetail','grn_exception_reason','GRN Exception Reason','STRING',0,0,1,0,'text' UNION ALL
  SELECT -1,'ManagerQtyApprovalDetail','grn_updated_by','GRN Updated By','STRING',1,1,1,0,'text'
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'ManagerQtyApprovalDetail')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = x.b AND field_code = x.c);
