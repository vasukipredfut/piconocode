-- aria:weighment-capture-other-vehicle's two RepeatingGroups
-- (material_details for LOADING, invoice_details_items for UNLOADING) have
-- never had anywhere real to land:
--   - invoice_details_items has zero plumbing anywhere -- it only ever
--     survived in workflow_instance.submitted_data_json.
--   - material_details got a dispatch_vehicle_detail.material_details TEXT
--     column + a field_map_json entry (V267), on the assumption
--     WorkflowProjectionListener would special-case it like it does
--     po_attachment/qc_sample_test. It never got that special case, so a
--     RepeatingGroup submission (a List<Map<...>>) falls through to the
--     plain-scalar path (scalarOf()) and silently keeps only the FIRST
--     row's Map.toString() -- not even valid JSON -- discarding every other
--     row.
--
-- Both are line-item grids captured once at the first weighment, mutually
-- exclusive per submission (movement_direction is either LOADING or
-- UNLOADING, never both), so they share ONE child table here rather than
-- two -- a line_type discriminator ('MATERIAL'/'INVOICE') tells them apart;
-- columns only one side uses (unit for material, invoice_no/invoice_date/
-- amount for invoice) simply stay NULL on the other's rows. A bespoke
-- listener (OtherVehicleLineItemTransitionListener) fans the submitted
-- array out into rows here, same as TankerUnloadInvoiceTransitionListener/
-- DispatchInvoiceItemTransitionListener do for their own repeaters.

CREATE TABLE IF NOT EXISTS weighment_other_vehicle_line_item (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  line_type VARCHAR(16) NOT NULL COMMENT 'MATERIAL (loading) or INVOICE (unloading)',
  row_no INT NOT NULL DEFAULT 0,
  material_name VARCHAR(255) DEFAULT NULL,
  unit VARCHAR(32) DEFAULT NULL,
  invoice_no VARCHAR(64) DEFAULT NULL,
  invoice_date DATE DEFAULT NULL,
  item_desc VARCHAR(255) DEFAULT NULL,
  qty DECIMAL(12,3) DEFAULT NULL,
  amount DECIMAL(14,2) DEFAULT NULL,
  remarks VARCHAR(512) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_wovli_instance (tenant_id, workflow_instance_id, line_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'WeighmentOtherVehicleLineItem', 'Weighment Other-Vehicle Line Item', 'weighment_other_vehicle_line_item',
       'One row per material line (LOADING) or invoice line (UNLOADING) captured on aria:weighment-capture-other-vehicle -- line_type tells them apart.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'WeighmentOtherVehicleLineItem');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'WeighmentOtherVehicleLineItem' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','line_type','Line Type','STRING',1,1,0,1,'text' UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','material_name','Material Name','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','unit','Unit','STRING',1,1,0,0,'text' UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','invoice_no','Invoice No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','invoice_date','Invoice Date','DATE',1,1,0,0,'date' UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','item_desc','Item / Material','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','qty','Quantity','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','amount','Amount','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'WeighmentOtherVehicleLineItem','remarks','Remarks','STRING',0,0,1,0,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = x.b AND field_code = x.c);

-- material_details was mapped here (V267) expecting the generic listener to
-- serialize it -- it never got that special case (see comment above), so
-- every submission silently corrupted this column. The new bespoke listener
-- is now the single source of truth for line items; stop double-writing
-- garbage into this one.
UPDATE m0_workflow_projection
SET field_map_json = JSON_REMOVE(field_map_json, '$.material_details')
WHERE tenant_id = -1 AND workflow_code = 'M10.OtherVehicles' AND target_table = 'dispatch_vehicle_detail'
  AND field_map_json IS NOT NULL AND JSON_VALID(field_map_json)
  AND JSON_CONTAINS_PATH(field_map_json, 'one', '$.material_details');
