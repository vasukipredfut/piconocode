-- aria:manager-qty-approval (M10.TankerInbound): the page's "Difference (Invoice - Unloading)" had no column, so it was never saved.
-- ManagerQtyApprovalDecisionListener now writes it (and the expected / loaded weight, variance % and within-tolerance verdict the
-- page also shows -- those columns already exist, V263) -- from WEIGHMENT_2 as well as QTY_PENDING_MANAGER.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS manager_qty_approval_detail ADD COLUMN IF NOT EXISTS qty_difference DECIMAL(12,3) NULL AFTER unloading_qty;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'ManagerQtyApprovalDetail', 'qty_difference', 'Difference (Invoice - Unloading)', 'DECIMAL', 0, 1, 0, 0, 'number'
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'ManagerQtyApprovalDetail')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = 'ManagerQtyApprovalDetail' AND field_code = 'qty_difference');
