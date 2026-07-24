-- Dynamic Conveyor Workflow & Integration — Step 3: Dispatch Task. Each task
-- tracks target vs processed quantity for one order/SKU and auto-completes
-- when processed reaches target. Multiple concurrent tasks per bay/workflow
-- are just multiple rows — no special-casing needed.

CREATE TABLE IF NOT EXISTS dispatch_task (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  workflow_code VARCHAR(128) NOT NULL,
  workflow_instance_id BIGINT DEFAULT NULL,
  bay_code VARCHAR(64) DEFAULT NULL,
  task_code VARCHAR(64) NOT NULL,
  order_ref VARCHAR(128) DEFAULT NULL,
  sku_code VARCHAR(64) DEFAULT NULL,
  target_qty DECIMAL(14,3) NOT NULL,
  processed_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
  rejected_qty DECIMAL(14,3) NOT NULL DEFAULT 0,
  status ENUM('PENDING','IN_PROGRESS','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  rule_set_id BIGINT DEFAULT NULL,
  started_at TIMESTAMP NULL DEFAULT NULL,
  completed_at TIMESTAMP NULL DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_dt_task_code (tenant_id, task_code),
  KEY idx_dt_status (tenant_id, status),
  KEY idx_dt_bay_active (tenant_id, bay_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS dispatch_task_scan_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  dispatch_task_id BIGINT DEFAULT NULL,
  barcode VARCHAR(128) DEFAULT NULL,
  outcome ENUM('ACCEPTED','REJECTED') NOT NULL,
  reject_reason VARCHAR(256) DEFAULT NULL,
  matched_sku VARCHAR(64) DEFAULT NULL,
  matched_order_ref VARCHAR(128) DEFAULT NULL,
  integration_id BIGINT DEFAULT NULL,
  scanned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_dtsl_task (tenant_id, dispatch_task_id, scanned_at),
  CONSTRAINT fk_dtsl_task FOREIGN KEY (dispatch_task_id) REFERENCES dispatch_task (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, created_at, value_column, display_column)
SELECT * FROM (
  SELECT -1 tid, 'DispatchTask' code, 'Dispatch Task' dn, 'dispatch_task' tn, 'Target vs processed quantity for one order/SKU on a conveyor line; auto-completes at target.' descr, 'M12' cat, NOW() ca, 'id' vc, 'task_code' dc
  UNION ALL SELECT -1, 'DispatchTaskScanLog', 'Dispatch Task Scan Log', 'dispatch_task_scan_log', 'Every accepted/rejected barcode scan against a Dispatch Task.', 'M12', NOW(), 'id', 'outcome'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = x.code);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DispatchTask', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, c.req, c.wk
FROM (
  SELECT 'workflow_code' fc, 'Workflow Code' lbl, 'STRING' dt, 1 filt, 1 sort, 1 srch, 1 req, 'text' wk
  UNION ALL SELECT 'bay_code', 'Bay Code', 'STRING', 1, 1, 1, 0, 'text'
  UNION ALL SELECT 'task_code', 'Task Code', 'STRING', 1, 1, 1, 1, 'text'
  UNION ALL SELECT 'order_ref', 'Order Ref', 'STRING', 1, 1, 1, 0, 'text'
  UNION ALL SELECT 'sku_code', 'SKU', 'STRING', 1, 1, 1, 0, 'text'
  UNION ALL SELECT 'target_qty', 'Target Qty', 'DECIMAL', 1, 1, 0, 1, 'number'
  UNION ALL SELECT 'processed_qty', 'Processed Qty', 'DECIMAL', 1, 1, 0, 0, 'number'
  UNION ALL SELECT 'rejected_qty', 'Rejected Qty', 'DECIMAL', 1, 1, 0, 0, 'number'
  UNION ALL SELECT 'status', 'Status', 'STRING', 1, 1, 0, 0, 'select'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'DispatchTask' AND field_code = c.fc);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'DispatchTaskScanLog', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, 0, c.wk
FROM (
  SELECT 'dispatch_task_id' fc, 'Dispatch Task' lbl, 'LONG' dt, 1 filt, 1 sort, 0 srch, 'number' wk
  UNION ALL SELECT 'barcode', 'Barcode', 'STRING', 1, 1, 1, 'text'
  UNION ALL SELECT 'outcome', 'Outcome', 'STRING', 1, 1, 0, 'text'
  UNION ALL SELECT 'reject_reason', 'Reject Reason', 'STRING', 0, 0, 1, 'text'
  UNION ALL SELECT 'matched_sku', 'Matched SKU', 'STRING', 1, 1, 1, 'text'
  UNION ALL SELECT 'scanned_at', 'Scanned At', 'DATETIME', 1, 1, 0, 'datetime'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'DispatchTaskScanLog' AND field_code = c.fc);
