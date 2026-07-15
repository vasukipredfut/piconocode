-- =====================================================================
-- V11 — Stock as a first-class entity.
-- m5_stock_balance: live on-hand quantities by (item, lot, bin).
-- m5_stock_movement: append-only ledger of every receipt/issue/transfer/adjust.
-- A Java listener (StockMovementListener) writes ledger rows + updates
-- balances on key workflow transitions:
--   M5.InwardReceipt   STORED       -> RECEIPT
--   M5.MaterialRequisition ISSUED   -> ISSUE
--   (Future: M5.JobCard CONSUMED, M5.Packing SPOIL, M10.* DISPATCH)
-- =====================================================================

CREATE TABLE IF NOT EXISTS m5_stock_balance (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NULL,
  lot_no VARCHAR(64) NULL,
  bin_code VARCHAR(64) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  qty_on_hand DECIMAL(14,3) NOT NULL DEFAULT 0,
  qty_reserved DECIMAL(14,3) NOT NULL DEFAULT 0,
  last_movement_at TIMESTAMP NULL,
  UNIQUE KEY uq_balance (tenant_id, item_code, lot_no, bin_code),
  KEY idx_balance_item (item_code),
  KEY idx_balance_bin (bin_code)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS m5_stock_movement (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  ts TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  kind VARCHAR(16) NOT NULL,
  item_code VARCHAR(64) NOT NULL,
  item_name VARCHAR(256) NULL,
  lot_no VARCHAR(64) NULL,
  from_bin VARCHAR(64) NULL,
  to_bin VARCHAR(64) NULL,
  qty DECIMAL(14,3) NOT NULL,
  uom VARCHAR(16) NOT NULL,
  ref_type VARCHAR(32) NULL,
  ref_id BIGINT NULL,
  ref_number VARCHAR(64) NULL,
  user_id BIGINT NULL,
  notes TEXT NULL,
  KEY idx_mvt_item (item_code, ts),
  KEY idx_mvt_kind (kind, ts),
  KEY idx_mvt_ref (ref_type, ref_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Seed: opening balances for the items we already have GRN data for
INSERT IGNORE INTO m5_stock_balance (tenant_id, item_code, item_name, lot_no, bin_code, uom, qty_on_hand) VALUES
  (1,'OIL-SUN-RAW','Raw Sunflower Oil','LOT-SUN-260607-A','T-OIL-03','KG', 21500.000),
  (1,'OIL-GND-RAW','Raw Groundnut Oil','LOT-GND-260605-A','T-OIL-17','KG', 18750.000),
  (1,'OIL-SUN-RFD','Refined Sunflower Oil','LOT-SUN-260605-B','T-OIL-08','KG', 5000.000),
  (1,'PET-5L-SUN','5L Sunflower PET','LOT-PET-260606','FG-BIN-A','PCS', 2500.000),
  (1,'PET-1L-GND','1L Groundnut PET','LOT-PET-260607','FG-BIN-B','PCS', 5000.000),
  (1,'CAP-25MM-WH','25mm White Caps','LOT-CAP-2606','PACK-BIN-1','PCS', 50000.000);

-- Seed: opening receipt movements matching the balances
INSERT IGNORE INTO m5_stock_movement (tenant_id, kind, item_code, item_name, lot_no, to_bin, qty, uom, ref_type, ref_number, notes) VALUES
  (1,'RECEIPT','OIL-SUN-RAW','Raw Sunflower Oil','LOT-SUN-260607-A','T-OIL-03',21500.000,'KG','GRN','GRN-2026-001','Opening balance — V11 seed'),
  (1,'RECEIPT','OIL-GND-RAW','Raw Groundnut Oil','LOT-GND-260605-A','T-OIL-17',18750.000,'KG','GRN','GRN-2026-002','Opening balance — V11 seed'),
  (1,'RECEIPT','OIL-SUN-RFD','Refined Sunflower Oil','LOT-SUN-260605-B','T-OIL-08',5000.000,'KG','OPENING',NULL,'Opening balance'),
  (1,'RECEIPT','PET-5L-SUN','5L Sunflower PET','LOT-PET-260606','FG-BIN-A',2500.000,'PCS','OPENING',NULL,'Opening balance'),
  (1,'RECEIPT','PET-1L-GND','1L Groundnut PET','LOT-PET-260607','FG-BIN-B',5000.000,'PCS','OPENING',NULL,'Opening balance'),
  (1,'RECEIPT','CAP-25MM-WH','25mm White Caps','LOT-CAP-2606','PACK-BIN-1',50000.000,'PCS','OPENING',NULL,'Opening balance');

-- Entity registry
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'StockBalance','Stock Balance','m5_stock_balance','Live on-hand quantities.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='StockBalance');
INSERT INTO entity_registry (code, display_name, table_name, description, category) SELECT 'StockMovement','Stock Movement','m5_stock_movement','Append-only stock ledger.','M5' WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='StockMovement');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockBalance','item_code','Item','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockBalance' AND field_code='item_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockBalance','item_name','Name','STRING',FALSE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockBalance' AND field_code='item_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockBalance','lot_no','Lot','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockBalance' AND field_code='lot_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockBalance','bin_code','Bin/Tank','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockBalance' AND field_code='bin_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockBalance','qty_on_hand','On Hand','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockBalance' AND field_code='qty_on_hand');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockBalance','uom','UoM','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockBalance' AND field_code='uom');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','ts','When','DATETIME',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='ts');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','kind','Kind','STRING',TRUE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='kind');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','item_code','Item','STRING',TRUE,TRUE,TRUE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='item_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','lot_no','Lot','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='lot_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','from_bin','From','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='from_bin');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','to_bin','To','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='to_bin');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','qty','Qty','DECIMAL',FALSE,TRUE,FALSE,TRUE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='qty');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','ref_type','Ref Type','STRING',TRUE,TRUE,FALSE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='ref_type');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required) SELECT 'StockMovement','ref_number','Ref #','STRING',TRUE,TRUE,TRUE,FALSE WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='StockMovement' AND field_code='ref_number');

-- Pages: balance list + movement ledger + stock-take form
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:stock-balance','Stock Balance','Live on-hand by item + lot + bin.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:stock-balance');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:stock-movement','Stock Movement','Append-only ledger of receipts/issues/transfers.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:stock-movement');
INSERT INTO page_definition (tenant_id, code, title, description, category, status) SELECT 1,'pico:stock-take','Stock Take / Adjust','Manual stock-take + adjustment with reason.','M5','PUBLISHED' WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:stock-take');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"StockBalance","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","item_code","item_name","lot_no","bin_code","uom","qty_on_hand","qty_reserved"]},"columns":[{"field":"item_code","label":"Item","sticky":"left","sortable":true},{"field":"item_name","label":"Name"},{"field":"lot_no","label":"Lot"},{"field":"bin_code","label":"Bin/Tank","render":"badge"},{"field":"qty_on_hand","label":"On Hand","render":"number","align":"right"},{"field":"qty_reserved","label":"Reserved","render":"number","align":"right"},{"field":"uom","label":"UoM"}],"filters":[{"field":"item_code","label":"Item","type":"text-contains","applyTo":["item_code","item_name"]},{"field":"bin_code","label":"Bin","type":"text-contains","applyTo":["bin_code"]}],"sort":{"default":[{"field":"item_code","dir":"asc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Stock Balance"},"responsive":{"mobile":{"renderAs":"cards","cardFields":["item_code","lot_no","bin_code","qty_on_hand"]}}}', 'V11', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:stock-balance' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"StockMovement","filter":{"and":[{"field":"tenant_id","op":"eq","value":1}]},"select":["id","ts","kind","item_code","lot_no","from_bin","to_bin","qty","uom","ref_type","ref_number"]},"columns":[{"field":"ts","label":"When","render":"datetime"},{"field":"kind","label":"Kind","render":"badge","colorMap":{"RECEIPT":"green","ISSUE":"amber","TRANSFER":"blue","ADJUST":"purple","CONSUME":"red","SPOIL":"red"}},{"field":"item_code","label":"Item","sticky":"left"},{"field":"lot_no","label":"Lot"},{"field":"from_bin","label":"From"},{"field":"to_bin","label":"To"},{"field":"qty","label":"Qty","render":"number","align":"right"},{"field":"uom","label":"UoM"},{"field":"ref_type","label":"Ref"},{"field":"ref_number","label":"Ref #"}],"filters":[{"field":"kind","label":"Kind","type":"multi-select","optionsSource":{"type":"static","options":["RECEIPT","ISSUE","TRANSFER","ADJUST","CONSUME","SPOIL"]}},{"field":"item_code","label":"Item","type":"text-contains","applyTo":["item_code"]}],"sort":{"default":[{"field":"ts","dir":"desc"}]},"pagination":{"kind":"server","pageSize":50},"header":{"title":"Stock Movement Ledger"},"responsive":{"mobile":{"renderAs":"cards","cardFields":["ts","kind","item_code","qty"]}}}', 'V11', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:stock-movement' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at) SELECT p.id, 1, 'PUBLISHED', '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Stock Take","fields":[{"kind":"FormField","type":"text","name":"itemCode","label":"Item Code"},{"kind":"FormField","type":"text","name":"lotNo","label":"Lot #"},{"kind":"FormField","type":"text","name":"binCode","label":"Bin / Tank"},{"kind":"FormField","type":"number","name":"countedQty","label":"Counted Qty"},{"kind":"FormField","type":"text","name":"uom","label":"UoM"},{"kind":"FormField","type":"text","name":"reason","label":"Adjustment Reason"},{"kind":"WorkflowActionButton","label":"Post Adjustment","action":"workflow.transition","args":{"workflowCode":"M5.StockAdjust","kind":"ADJUST"},"variant":"primary"}]}]}', 'V11', CURRENT_TIMESTAMP FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:stock-take' AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1) WHERE tenant_id=1 AND code IN ('pico:stock-balance','pico:stock-movement','pico:stock-take') AND current_version_id IS NULL;
