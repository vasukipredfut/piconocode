-- Dynamic Conveyor Workflow & Integration — Step 2: Rule Builder. Order/SKU/
-- Quantity/Packing/Dispatch validation rules stored as data (SpEL boolean
-- expressions), not hardcoded Java — evaluated via the EXISTING
-- com.aria.workflow.engine.SpelGuardEvaluator (already used for workflow
-- transition guards; its SimpleEvaluationContext blocks T(...)/reflection/
-- constructors, safe for tenant-authored rule expressions). No new
-- expression engine needed.

CREATE TABLE IF NOT EXISTS rule_set (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  code VARCHAR(128) NOT NULL,
  version INT NOT NULL DEFAULT 1,
  workflow_code VARCHAR(128) DEFAULT NULL,
  name VARCHAR(256) NOT NULL,
  description VARCHAR(512) DEFAULT NULL,
  status ENUM('DRAFT','PUBLISHED','DISABLED') NOT NULL DEFAULT 'DRAFT',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_rule_set (tenant_id, code, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS rule_definition (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  rule_set_id BIGINT NOT NULL,
  rule_code VARCHAR(128) NOT NULL,
  display_name VARCHAR(256) NOT NULL,
  category ENUM('ORDER','SKU','QUANTITY','PACKING','DISPATCH','CUSTOM') NOT NULL,
  sequence_no INT NOT NULL DEFAULT 0,
  expression TEXT NOT NULL,
  severity ENUM('BLOCKING','WARNING') NOT NULL DEFAULT 'BLOCKING',
  reject_reason_code VARCHAR(64) DEFAULT NULL,
  active TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_rule_def (tenant_id, rule_set_id, rule_code),
  KEY idx_rd_seq (tenant_id, rule_set_id, sequence_no),
  CONSTRAINT fk_rd_rule_set FOREIGN KEY (rule_set_id) REFERENCES rule_set (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS rule_evaluation_log (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  rule_set_id BIGINT DEFAULT NULL,
  dispatch_task_id BIGINT DEFAULT NULL,
  workflow_instance_id BIGINT DEFAULT NULL,
  scan_barcode VARCHAR(128) DEFAULT NULL,
  input_context_json TEXT DEFAULT NULL CHECK (json_valid(input_context_json)),
  outcome ENUM('PASS','REJECT','ERROR') NOT NULL,
  failed_rule_code VARCHAR(128) DEFAULT NULL,
  failed_reason VARCHAR(512) DEFAULT NULL,
  evaluated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_rel_task (tenant_id, dispatch_task_id, evaluated_at),
  KEY idx_rel_instance (tenant_id, workflow_instance_id, evaluated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, created_at, value_column, display_column)
SELECT * FROM (
  SELECT -1 tid, 'RuleSet' code, 'Rule Set' dn, 'rule_set' tn, 'A named, versioned group of validation rules for a conveyor workflow.' descr, 'M12' cat, NOW() ca, 'id' vc, 'name' dc
  UNION ALL SELECT -1, 'RuleDefinition', 'Rule Definition', 'rule_definition', 'One Order/SKU/Quantity/Packing/Dispatch validation rule (a SpEL boolean expression) within a Rule Set.', 'M12', NOW(), 'id', 'display_name'
  UNION ALL SELECT -1, 'RuleEvaluationLog', 'Rule Evaluation Log', 'rule_evaluation_log', 'Audit trail of every Rule Builder evaluation (pass/reject/error) for a barcode scan.', 'M12', NOW(), 'id', 'outcome'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = x.code);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'RuleSet', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, c.req, c.wk
FROM (
  SELECT 'code' fc, 'Code' lbl, 'STRING' dt, 1 filt, 1 sort, 1 srch, 1 req, 'text' wk
  UNION ALL SELECT 'version', 'Version', 'INT', 1, 1, 0, 0, 'number'
  UNION ALL SELECT 'workflow_code', 'Workflow Code', 'STRING', 1, 1, 1, 0, 'text'
  UNION ALL SELECT 'name', 'Name', 'STRING', 1, 1, 1, 1, 'text'
  UNION ALL SELECT 'description', 'Description', 'STRING', 0, 0, 1, 0, 'textarea'
  UNION ALL SELECT 'status', 'Status', 'STRING', 1, 1, 0, 0, 'select'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'RuleSet' AND field_code = c.fc);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'RuleDefinition', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, c.req, c.wk
FROM (
  SELECT 'rule_set_id' fc, 'Rule Set' lbl, 'LONG' dt, 1 filt, 1 sort, 0 srch, 1 req, 'number' wk
  UNION ALL SELECT 'rule_code', 'Rule Code', 'STRING', 1, 1, 1, 1, 'text'
  UNION ALL SELECT 'display_name', 'Display Name', 'STRING', 1, 1, 1, 1, 'text'
  UNION ALL SELECT 'category', 'Category', 'STRING', 1, 1, 0, 1, 'select'
  UNION ALL SELECT 'sequence_no', 'Sequence', 'INT', 1, 1, 0, 0, 'number'
  UNION ALL SELECT 'expression', 'Expression (SpEL)', 'STRING', 0, 0, 1, 1, 'textarea'
  UNION ALL SELECT 'severity', 'Severity', 'STRING', 1, 1, 0, 1, 'select'
  UNION ALL SELECT 'reject_reason_code', 'Reject Reason Code', 'STRING', 1, 1, 1, 0, 'text'
  UNION ALL SELECT 'active', 'Active', 'BOOLEAN', 1, 1, 0, 0, 'checkbox'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'RuleDefinition' AND field_code = c.fc);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'RuleEvaluationLog', c.fc, c.lbl, c.dt, c.filt, c.sort, c.srch, 0, c.wk
FROM (
  SELECT 'dispatch_task_id' fc, 'Dispatch Task' lbl, 'LONG' dt, 1 filt, 1 sort, 0 srch, 'number' wk
  UNION ALL SELECT 'scan_barcode', 'Barcode', 'STRING', 1, 1, 1, 'text'
  UNION ALL SELECT 'outcome', 'Outcome', 'STRING', 1, 1, 0, 'text'
  UNION ALL SELECT 'failed_rule_code', 'Failed Rule', 'STRING', 1, 1, 1, 'text'
  UNION ALL SELECT 'failed_reason', 'Reason', 'STRING', 0, 0, 1, 'text'
  UNION ALL SELECT 'evaluated_at', 'Evaluated At', 'DATETIME', 1, 1, 0, 'datetime'
) c
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'RuleEvaluationLog' AND field_code = c.fc);
