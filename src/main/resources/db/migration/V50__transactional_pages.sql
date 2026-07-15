-- V50: GRN / PO / MR / SO transactional list pages — ERP-grade columns.
-- Cross-ref: Tally Prime Pending Vouchers + SAP MM ME23N + Marg Inward
-- ===========================================================================

-- 1. Register additional fields on these entities (idempotent)
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json) VALUES
  -- GRN
  ('GoodsReceiptNote','grn_number','GRN #','STRING',1,1,1,1,'text',NULL),
  ('GoodsReceiptNote','po_number','PO #','STRING',1,1,1,0,'text',NULL),
  ('GoodsReceiptNote','vendor_name','Vendor','STRING',1,1,1,0,'text',NULL),
  ('GoodsReceiptNote','invoice_number','Invoice #','STRING',1,1,1,0,'text',NULL),
  ('GoodsReceiptNote','invoice_date','Invoice Date','DATE',1,1,0,0,'date',NULL),
  ('GoodsReceiptNote','invoice_value','Invoice Value (Rs)','DECIMAL',1,1,0,0,'currency',NULL),
  ('GoodsReceiptNote','vehicle_no','Vehicle','STRING',1,1,1,0,'text',NULL),
  ('GoodsReceiptNote','transporter_name','Transporter','STRING',1,1,1,0,'text',NULL),
  ('GoodsReceiptNote','source_type','Source','STRING',1,1,0,0,'select','["PO","DIRECT","RETURN","INTER_UNIT","SAP_PUSH"]'),
  ('GoodsReceiptNote','received_at','Received At','DATETIME',1,1,0,0,'datetime',NULL),
  ('GoodsReceiptNote','gate_in_time','Gate In','DATETIME',1,1,0,0,'datetime',NULL),
  ('GoodsReceiptNote','gate_out_time','Gate Out','DATETIME',1,1,0,0,'datetime',NULL),
  ('GoodsReceiptNote','ewb_no','E-Way Bill #','STRING',0,1,1,0,'text',NULL),
  -- PO
  ('PurchaseOrder','po_number','PO #','STRING',1,1,1,1,'text',NULL),
  ('PurchaseOrder','vendor_name','Vendor','STRING',1,1,1,0,'text',NULL),
  ('PurchaseOrder','total_value','Total Value (Rs)','DECIMAL',1,1,0,0,'currency',NULL),
  ('PurchaseOrder','required_by_date','Required By','DATE',1,1,0,0,'date',NULL),
  ('PurchaseOrder','currency','Currency','STRING',1,1,0,0,'select','["INR","USD","EUR"]'),
  -- MR
  ('MaterialRequisition','mr_number','MR #','STRING',1,1,1,1,'text',NULL),
  ('MaterialRequisition','requested_by','Requested By','LONG',1,1,0,0,'entity-ref',NULL),
  ('MaterialRequisition','requested_for_dept_id','For Department','LONG',1,1,0,0,'entity-ref',NULL),
  ('MaterialRequisition','required_by_date','Required By','DATE',1,1,0,0,'date',NULL),
  -- SO
  ('SalesOrder','so_number','SO #','STRING',1,1,1,1,'text',NULL),
  ('SalesOrder','customer_name','Customer','STRING',1,1,1,0,'text',NULL),
  ('SalesOrder','total_value','Total Value (Rs)','DECIMAL',1,1,0,0,'currency',NULL),
  ('SalesOrder','required_dispatch_date','Required Dispatch','DATE',1,1,0,0,'date',NULL);

-- 2. GRN list page
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','GoodsReceiptNote','select',JSON_ARRAY('id','grn_number','po_number','vendor_name','invoice_number','invoice_value','vehicle_no','source_type','status','current_step','received_at')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','grn_number','label','GRN #','sortable',true),
    JSON_OBJECT('field','po_number','label','PO #','sortable',true),
    JSON_OBJECT('field','vendor_name','label','Vendor','sortable',true),
    JSON_OBJECT('field','invoice_number','label','Invoice #','sortable',true),
    JSON_OBJECT('field','invoice_value','label','Invoice Value Rs','render','number','sortable',true),
    JSON_OBJECT('field','vehicle_no','label','Vehicle','sortable',true),
    JSON_OBJECT('field','source_type','label','Source','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('PO','blue','DIRECT','amber','RETURN','purple','INTER_UNIT','green','SAP_PUSH','grey')),
    JSON_OBJECT('field','status','label','Status','sortable',true),
    JSON_OBJECT('field','current_step','label','Step','sortable',true),
    JSON_OBJECT('field','received_at','label','Received','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','source_type','label','Source','type','multi-select','options',JSON_ARRAY('PO','DIRECT','RETURN','INTER_UNIT','SAP_PUSH')),
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('DRAFT','RECEIVED','QC_PASSED','QC_REJECTED','STORED','POSTED','CLOSED')),
    JSON_OBJECT('field','_q','label','Search GRN/PO/Vendor','type','text-contains','applyTo',JSON_ARRAY('grn_number','po_number','vendor_name','vehicle_no'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','Mark QC Pass','action','entity.update-field','args',JSON_OBJECT('field','current_step','value','QC_PASSED')),
    JSON_OBJECT('label','Mark Stored','action','entity.update-field','args',JSON_OBJECT('field','current_step','value','STORED'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code IN ('pico:grn-list','pico:grn-store','pico:grn-from-po');
UPDATE page_definition SET description='Goods Receipt Notes — auto-created from gate pass / direct entry / PO match / return / inter-unit transfer / SAP push. Posts to Tally as Purchase Voucher on store.' WHERE code IN ('pico:grn-list','pico:grn-store','pico:grn-from-po');

-- 3. PO list page
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:po-list', 'Purchase Orders', 'Open POs awaiting receipt + closed POs for reference.', 'M5', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b_po = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','PurchaseOrder','select',JSON_ARRAY('id','po_number','vendor_name','total_value','required_by_date','status','current_step','assignee_id','updated_at')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','po_number','label','PO #','sortable',true),
    JSON_OBJECT('field','vendor_name','label','Vendor','sortable',true),
    JSON_OBJECT('field','total_value','label','Total (Rs)','render','number','sortable',true),
    JSON_OBJECT('field','required_by_date','label','Required By','render','date','sortable',true),
    JSON_OBJECT('field','status','label','Status','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('DRAFT','grey','SUBMITTED','blue','APPROVED','green','SENT','blue','PARTIALLY_RECEIVED','amber','CLOSED','green','CANCELLED','red')),
    JSON_OBJECT('field','current_step','label','Step','sortable',true),
    JSON_OBJECT('field','updated_at','label','Last Activity','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('DRAFT','SUBMITTED','APPROVED','SENT','PARTIALLY_RECEIVED','CLOSED','CANCELLED')),
    JSON_OBJECT('field','_q','label','Search PO/Vendor','type','text-contains','applyTo',JSON_ARRAY('po_number','vendor_name'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','Approve','action','entity.update-field','args',JSON_OBJECT('field','status','value','APPROVED')),
    JSON_OBJECT('label','Cancel','action','entity.update-field','args',JSON_OBJECT('field','status','value','CANCELLED'))
  ),
  'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:po-list';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b_po FROM page_definition WHERE code='pico:po-list';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:po-list';

-- 4. MR list (already exists at pico:mr-approve; add a generic mr-list)
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
  VALUES (1, 'pico:mr-list', 'Material Requisitions', 'All MRs raised by departments. Click row for detail; manager approves on Inbox.', 'M5', 'PUBLISHED', NOW(), NOW())
  ON DUPLICATE KEY UPDATE title=VALUES(title), description=VALUES(description), updated_at=NOW();
SET @b_mr = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','MaterialRequisition','select',JSON_ARRAY('id','mr_number','requested_by','requested_for_dept_id','required_by_date','status','current_step','assignee_id','updated_at')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','mr_number','label','MR #','sortable',true),
    JSON_OBJECT('field','requested_by','label','Requested By','sortable',true),
    JSON_OBJECT('field','requested_for_dept_id','label','Department','sortable',true),
    JSON_OBJECT('field','required_by_date','label','Required By','render','date','sortable',true),
    JSON_OBJECT('field','status','label','Status','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('DRAFT','grey','SUBMITTED','blue','APPROVED','green','ISSUED','green','REJECTED','red')),
    JSON_OBJECT('field','current_step','label','Step','sortable',true),
    JSON_OBJECT('field','updated_at','label','Last Activity','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('DRAFT','SUBMITTED','APPROVED','ISSUED','REJECTED')),
    JSON_OBJECT('field','_q','label','Search MR #','type','text-contains','applyTo',JSON_ARRAY('mr_number'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','Approve','action','entity.update-field','args',JSON_OBJECT('field','status','value','APPROVED')),
    JSON_OBJECT('label','Reject','action','entity.update-field','args',JSON_OBJECT('field','status','value','REJECTED'))
  ),
  'pagination', JSON_OBJECT('pageSize',25));
DELETE pv FROM page_version pv JOIN page_definition pd ON pd.id=pv.page_id WHERE pd.code='pico:mr-list';
INSERT INTO page_version (page_id, version, status, body_json) SELECT id, 1, 'PUBLISHED', @b_mr FROM page_definition WHERE code='pico:mr-list';
UPDATE page_definition SET current_version_id=(SELECT id FROM page_version WHERE page_id=page_definition.id ORDER BY version DESC LIMIT 1) WHERE code='pico:mr-list';

-- 5. SO list (update existing pico:sales-order-list)
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','SalesOrder','select',JSON_ARRAY('id','so_number','customer_name','total_value','required_dispatch_date','status','current_step','assignee_id','updated_at')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','so_number','label','SO #','sortable',true),
    JSON_OBJECT('field','customer_name','label','Customer','sortable',true),
    JSON_OBJECT('field','total_value','label','Total (Rs)','render','number','sortable',true),
    JSON_OBJECT('field','required_dispatch_date','label','Dispatch By','render','date','sortable',true),
    JSON_OBJECT('field','status','label','Status','sortable',true,'render','badge',
      'colorMap', JSON_OBJECT('DRAFT','grey','SUBMITTED','blue','APPROVED','green','PICKING','blue','PACKED','blue','DISPATCHED','green','INVOICED','green','CLOSED','green','CANCELLED','red')),
    JSON_OBJECT('field','current_step','label','Step','sortable',true),
    JSON_OBJECT('field','updated_at','label','Last Activity','render','datetime','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('DRAFT','SUBMITTED','APPROVED','PICKING','PACKED','DISPATCHED','INVOICED','CLOSED','CANCELLED')),
    JSON_OBJECT('field','_q','label','Search SO #/Customer','type','text-contains','applyTo',JSON_ARRAY('so_number','customer_name'))
  ),
  'rowActions', JSON_ARRAY(
    JSON_OBJECT('label','Approve','action','entity.update-field','args',JSON_OBJECT('field','status','value','APPROVED')),
    JSON_OBJECT('label','Cancel','action','entity.update-field','args',JSON_OBJECT('field','status','value','CANCELLED'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code='pico:sales-order-list';
UPDATE page_definition SET description='Open sales orders awaiting dispatch + recently closed. Credit-limit check + discount approval gated by SO total.' WHERE code='pico:sales-order-list';
