-- Follow-up to V360/V362/V363 after a field-by-field audit against the live
-- ERPNext doctype definitions. Fixes genuine oversights (not already-agreed
-- "core fields only" trims):
--   1. ExpenseClaim.company was left nullable; ERPNext marks it reqd=1.
--   2. LeaveAllocation was missing `company` (reqd=1 in ERPNext) entirely.
--   3. LeaveAllocation was missing `carry_forward` -- a common field, not an
--      advanced policy-engine one, so it belongs in "core".
--   4. Lead Report has both "Mobile" and "Phone" as distinct columns; aria's
--      Lead only had one contact_phone field (mapped to Mobile). Adding the
--      missing `phone` column.
-- All tables involved have 0 rows (created in this same feature branch), so
-- these are safe, lossless schema tweaks.

-- ===================== ExpenseClaim.company: make required =====================
ALTER TABLE m7_expense_claim MODIFY COLUMN company VARCHAR(64) NOT NULL;
UPDATE entity_field_registry SET required = 1 WHERE entity_code = 'ExpenseClaim' AND field_code = 'company';

-- ===================== LeaveAllocation: add company + carry_forward =====================
ALTER TABLE m7_leave_allocation
  ADD COLUMN IF NOT EXISTS company VARCHAR(64) NOT NULL AFTER leave_type_id,
  ADD COLUMN IF NOT EXISTS carry_forward TINYINT(1) NOT NULL DEFAULT 0 AFTER new_leaves_allocated;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x)+ROW_NUMBER() OVER (ORDER BY c.ord),
       -1,'LeaveAllocation',c.fc,c.lbl,c.dt,c.filt,c.sort,c.srch,c.req,c.wk
FROM (SELECT 1 ord,'company' fc,'Company' lbl,'STRING' dt,1 filt,1 sort,0 srch,1 req,'text' wk
      UNION ALL SELECT 2,'carry_forward','Add Unused Leaves From Previous Allocation','BOOLEAN',1,1,0,0,'checkbox') c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='LeaveAllocation' AND field_code=c.fc);

-- ===================== Lead: add the missing "Phone" (distinct from Mobile) =====================
ALTER TABLE m8_lead ADD COLUMN IF NOT EXISTS phone VARCHAR(32) DEFAULT NULL AFTER contact_phone;

INSERT INTO entity_field_registry (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0)+1 FROM entity_field_registry x),
       -1,'Lead','phone','Phone','STRING',0,0,1,0,'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Lead' AND field_code='phone');

-- ===================== Update the two affected pages' select lists =====================
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind": "list", "dataSource": {"entity": "LeaveAllocation", "select": ["employee_id", "leave_type_id", "company", "from_date", "to_date", "new_leaves_allocated", "carry_forward", "total_leaves_allocated"]}, "columns": [], "filters": [{"field": "employee_id", "label": "Employee", "type": "equals", "applyTo": ["employee_id"]}], "pagination": {"pageSize": 50}}'
WHERE pd.tenant_id = -1 AND pd.code = 'aria:hrm-leave-allocation-list';

UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind": "list", "dataSource": {"entity": "Lead", "select": ["lead_name", "status", "owner_id", "contact_email", "contact_phone", "phone", "company_name", "city", "state", "country"]}, "columns": [], "filters": [{"field": "_q", "label": "Search Lead", "type": "text-contains", "applyTo": ["lead_name", "contact_email", "contact_phone"]}, {"field": "status", "label": "Status", "type": "equals", "applyTo": ["status"]}], "pagination": {"pageSize": 50}}'
WHERE pd.tenant_id = -1 AND pd.code = 'aria:slm-lead-list';
