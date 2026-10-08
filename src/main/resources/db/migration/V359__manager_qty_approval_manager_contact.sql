-- aria:manager-qty-approval also sends the approving manager's contact no (UI field "manager_contact"),
-- saved next to manager_name (V358) on manager_qty_approval_detail and exposed on a plain GatePass read.
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE manager_qty_approval_detail
  ADD COLUMN IF NOT EXISTS manager_contact VARCHAR(32) DEFAULT NULL;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+1,
       -1, 'ManagerQtyApprovalDetail', 'manager_contact', 'Manager Contact No', 'STRING', 1, 1, 1, 0, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'ManagerQtyApprovalDetail' AND field_code = 'manager_contact');
