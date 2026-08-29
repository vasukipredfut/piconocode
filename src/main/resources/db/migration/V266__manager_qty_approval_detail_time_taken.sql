-- "Time taken" (V265) extended onto manager_qty_approval_detail -- this
-- table is written by BESPOKE listeners (ManagerQtyApprovalDecisionListener
-- for M10.TankerInbound, PmtSharedReasonListener for the other 3 workflows'
-- manager-qty-approval states), not the generic WorkflowProjectionListener
-- mechanism V265 covered, so it needs its own column + its own write-path
-- wiring (see both listener classes).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE manager_qty_approval_detail
  ADD COLUMN IF NOT EXISTS time_taken DECIMAL(10,2) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+1,
       -1, 'ManagerQtyApprovalDetail', 'time_taken', 'Time Taken (min)', 'DECIMAL', 0, 1, 0, 0, 'number'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ManagerQtyApprovalDetail' AND field_code = 'time_taken');
