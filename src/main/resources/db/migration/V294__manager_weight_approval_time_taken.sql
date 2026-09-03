-- aria:manager-weight-approval submits time_taken (Time Spent, liveTimerFrom
-- gpNumber) same as every other queue page, but manager_weight_approval_detail
-- never had a column for it and PmtSharedReasonListener's WEIGHMENT_3/
-- WEIGHT_APPROVAL_PENDING case never captured it -- silently lost to
-- workflow_instance.submitted_data_json only, unlike manager_qty_approval_detail's
-- own time_taken (already captured, same listener, different case).

ALTER TABLE manager_weight_approval_detail
  ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'ManagerWeightApprovalDetail', 'time_taken', 'Time Taken', 'DECIMAL', 0, 1, 0, 0, 'number'
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'ManagerWeightApprovalDetail')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ManagerWeightApprovalDetail' AND field_code = 'time_taken');
