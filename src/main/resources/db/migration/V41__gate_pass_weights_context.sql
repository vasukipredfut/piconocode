-- V41: Manager-decision context — per SAP TM / Tally Prime, a manager
-- pending the weight-diff or quantity decision needs to see, in the inbox
-- row itself:
--   * W1 (loaded weight), W2 (empty), W3 (re-check empty) for tanker
--   * W2-W3 diff in kg + %
--   * Source party (vendor for inbound, customer for outbound)
--   * PO/SO reference
--   * Variance reason (free text from the page that flagged it)
-- Without these the Inbox is just a list of GP numbers — the manager has
-- to click 5x into the detail to decide.

-- ---------------------------------------------------------------------------
-- 1) Columns
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_gp_context_col;
DELIMITER $$
CREATE PROCEDURE add_gp_context_col(IN col VARCHAR(64), IN ddl VARCHAR(255))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                 WHERE table_schema=DATABASE() AND table_name='gate_pass' AND column_name=col) THEN
    SET @s = CONCAT('ALTER TABLE gate_pass ADD COLUMN ', ddl);
    PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
  END IF;
END$$
DELIMITER ;
CALL add_gp_context_col('w1_weight_kg',     'w1_weight_kg DECIMAL(10,3) NULL');
CALL add_gp_context_col('w2_weight_kg',     'w2_weight_kg DECIMAL(10,3) NULL');
CALL add_gp_context_col('w3_weight_kg',     'w3_weight_kg DECIMAL(10,3) NULL');
CALL add_gp_context_col('source_party',     'source_party VARCHAR(255) NULL');
CALL add_gp_context_col('reference_no',     'reference_no VARCHAR(64) NULL');
CALL add_gp_context_col('variance_reason',  'variance_reason VARCHAR(512) NULL');
CALL add_gp_context_col('ordered_qty',      'ordered_qty DECIMAL(12,3) NULL');
CALL add_gp_context_col('vehicle_type',     'vehicle_type VARCHAR(32) NULL');
CALL add_gp_context_col('source_party_type','source_party_type VARCHAR(16) NULL');
DROP PROCEDURE add_gp_context_col;

-- Computed-style values: we keep them as real columns updated by the workflow
-- write-through later. For now backfill from existing W1/W2/W3 deltas.

-- 2) Register fields so they show up in detail-view + are RBAC-editable
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) VALUES
  ('GatePass','w1_weight_kg','W1 — Loaded kg','DECIMAL',0,1,0,0,'number'),
  ('GatePass','w2_weight_kg','W2 — Empty kg','DECIMAL',0,1,0,0,'number'),
  ('GatePass','w3_weight_kg','W3 — Re-check kg','DECIMAL',0,1,0,0,'number'),
  ('GatePass','source_party','From (Vendor / Customer)','STRING',1,1,1,0,'text'),
  ('GatePass','source_party_type','Party Type','STRING',1,1,0,0,'select'),
  ('GatePass','reference_no','Reference (PO / SO / RN)','STRING',1,1,1,0,'text'),
  ('GatePass','variance_reason','Variance Reason','TEXT',0,0,1,0,'textarea'),
  ('GatePass','ordered_qty','Ordered Qty','DECIMAL',0,1,0,0,'number'),
  ('GatePass','vehicle_type','Vehicle Type','STRING',1,1,0,0,'select');

-- 3) Backfill the two manager-pending demo rows with realistic context
UPDATE gate_pass SET
  w1_weight_kg     = 18450.000,
  w2_weight_kg     = 9320.000,
  w3_weight_kg     = NULL,
  ordered_qty      = 9150.000,
  total_unloading_qty = 9120.000,
  source_party     = 'ABC Edible Oils Ltd, Chennai',
  source_party_type= 'VENDOR',
  reference_no     = 'PO-2026-0421',
  vehicle_no       = 'TN-43-AB-9421',
  driver_name      = 'M. Subramani',
  driver_phone     = '9842512345',
  vehicle_type     = 'TANKER',
  variance_reason  = 'Unloaded qty 9,120 kg is short by 30 kg vs ordered 9,150 kg (variance -0.33%). Awaiting manager approval per Pico SOP > 0.25% tolerance.'
WHERE id=15 AND current_step='QTY_PENDING_MANAGER';

UPDATE gate_pass SET
  w1_weight_kg     = 18610.000,
  w2_weight_kg     = 9330.000,
  w3_weight_kg     = 9398.000,
  ordered_qty      = 9280.000,
  total_unloading_qty = 9280.000,
  source_party     = 'Sri Vinayaga Stores, Coimbatore',
  source_party_type= 'CUSTOMER',
  reference_no     = 'SO-2026-1108',
  vehicle_no       = 'TN-46-CJ-6701',
  driver_name      = 'K. Arumugam',
  driver_phone     = '9994578812',
  vehicle_type     = 'TANKER',
  variance_reason  = 'W3 re-check 9,398 kg is 68 kg heavier than W2 empty 9,330 kg (residual oil suspected in tank baffle). Threshold is 50 kg per Pico SOP — manager decision required before gate close.'
WHERE id=20 AND current_step='WEIGHT_DIFF_PENDING_MANAGER';

-- 4) Rebuild Manager Inbox page with the new context columns
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','GatePass',
    'select', JSON_ARRAY('id','gp_number','vehicle_no','driver_name','driver_phone','flow_variant_code','current_step','status','source_party','reference_no','ordered_qty','total_unloading_qty','w1_weight_kg','w2_weight_kg','w3_weight_kg','variance_reason','updated_at'),
    'filter', JSON_OBJECT('field','current_step','op','in','value',JSON_ARRAY('QTY_PENDING_MANAGER','WEIGHT_DIFF_PENDING_MANAGER','PO_PENDING_MANAGER','RETURN_PENDING_MANAGER')),
    'sort', JSON_ARRAY(JSON_OBJECT('field','updated_at','dir','desc'))),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','gp_number','label','GP #','sortable',true),
    JSON_OBJECT('field','vehicle_no','label','Vehicle','sortable',true),
    JSON_OBJECT('field','driver_name','label','Driver','sortable',true),
    JSON_OBJECT('field','flow_variant_code','label','Flow','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('M10.TankerInbound','blue','M10.PackUnload','blue','M10.ReturnStockIn','amber','M10.TankerOutbound','green','M10.PackLoading','green','M10.FGLoading','green')),
    JSON_OBJECT('field','current_step','label','Awaiting','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('QTY_PENDING_MANAGER','amber','WEIGHT_DIFF_PENDING_MANAGER','amber','PO_PENDING_MANAGER','amber','RETURN_PENDING_MANAGER','amber')),
    JSON_OBJECT('field','source_party','label','From','sortable',true),
    JSON_OBJECT('field','reference_no','label','Ref #','sortable',true),
    JSON_OBJECT('field','w1_weight_kg','label','W1 (kg)','render','number'),
    JSON_OBJECT('field','w2_weight_kg','label','W2 (kg)','render','number'),
    JSON_OBJECT('field','w3_weight_kg','label','W3 (kg)','render','number'),
    JSON_OBJECT('field','total_unloading_qty','label','Unloaded','render','number'),
    JSON_OBJECT('field','variance_reason','label','Reason'),
    JSON_OBJECT('field','updated_at','label','Last Activity','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','flow_variant_code','label','Flow','type','multi-select','options',JSON_ARRAY('M10.TankerInbound','M10.PackUnload','M10.ReturnStockIn','M10.TankerOutbound','M10.PackLoading','M10.FGLoading')),
    JSON_OBJECT('field','current_step','label','Awaiting Step','type','multi-select','options',JSON_ARRAY('QTY_PENDING_MANAGER','WEIGHT_DIFF_PENDING_MANAGER','PO_PENDING_MANAGER','RETURN_PENDING_MANAGER')),
    JSON_OBJECT('field','_q','label','Search Vehicle / Driver / GP#','type','text-contains','applyTo',JSON_ARRAY('vehicle_no','driver_name','gp_number'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','✓ Approve','action','entity.update-field','args',JSON_OBJECT('field','current_step','value','QTY_RECONCILED')),
    JSON_OBJECT('label','✗ Reject','action','entity.update-field','args',JSON_OBJECT('field','current_step','value','REJECTED_BY_MANAGER')),
    JSON_OBJECT('label','Trail','action','page.open','page','pico:audit-log')
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:manager-inbox';
