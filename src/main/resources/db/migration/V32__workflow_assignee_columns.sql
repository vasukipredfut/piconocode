-- V32: add assignee_id + assigned_at to workflow-tracked tables, and
-- register the new fields in entity_field_registry so the generic
-- entity.update-field action accepts writes.
--
-- Why: until now there was no first-class "who owns this row" — managers
-- approved by status alone. The user wants per-row employee assignment
-- visible in every Inbox so work is routable and accountable.

-- ---------------------------------------------------------------------------
-- 1) Columns — idempotent ALTER pattern via dynamic SQL
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_assignee_cols;
DELIMITER $$
CREATE PROCEDURE add_assignee_cols(IN tbl VARCHAR(128))
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables
             WHERE table_schema = DATABASE() AND table_name = tbl)
  THEN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = tbl
                   AND column_name = 'assignee_id') THEN
      SET @s = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN assignee_id BIGINT NULL');
      PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = tbl
                   AND column_name = 'assigned_at') THEN
      SET @s = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN assigned_at TIMESTAMP NULL');
      PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = DATABASE() AND table_name = tbl
                   AND column_name = 'assigned_by') THEN
      SET @s = CONCAT('ALTER TABLE `', tbl, '` ADD COLUMN assigned_by BIGINT NULL');
      PREPARE st FROM @s; EXECUTE st; DEALLOCATE PREPARE st;
    END IF;
  END IF;
END$$
DELIMITER ;

CALL add_assignee_cols('material_requisition');
CALL add_assignee_cols('gate_pass');
CALL add_assignee_cols('goods_receipt_note');
CALL add_assignee_cols('m5_return_note');
CALL add_assignee_cols('m5_fg_dispatch');
CALL add_assignee_cols('m5_purchase_order');
CALL add_assignee_cols('m5_sales_order');
CALL add_assignee_cols('m5_production_order');
CALL add_assignee_cols('m5_job_card');
CALL add_assignee_cols('m5_packing_run');
CALL add_assignee_cols('m5_quality_test');
CALL add_assignee_cols('m5_maintenance_order');
CALL add_assignee_cols('m6_payment');
CALL add_assignee_cols('m6_sales_invoice');
CALL add_assignee_cols('m6_purchase_invoice');
CALL add_assignee_cols('m7_leave_request');

DROP PROCEDURE add_assignee_cols;

-- ---------------------------------------------------------------------------
-- 2) Register assignee_id + assigned_at in entity_field_registry for every
--    workflow-tracked entity. entity-ref widget renders an Employee picker
--    on the ListView; datetime renders a read-only timestamp.
-- ---------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS register_assignee_fields;
DELIMITER $$
CREATE PROCEDURE register_assignee_fields(IN entity VARCHAR(128))
BEGIN
  INSERT IGNORE INTO entity_field_registry
    (entity_code, field_code, display_label, data_type, filterable, sortable, searchable,
     required, widget_kind, relation_entity)
  VALUES
    (entity, 'assignee_id', 'Assignee', 'RELATION', 1, 1, 0, 0, 'entity-ref', 'Employee'),
    (entity, 'assigned_at', 'Assigned At', 'DATETIME', 1, 1, 0, 0, 'datetime', NULL),
    (entity, 'assigned_by', 'Assigned By', 'RELATION', 0, 0, 0, 0, 'entity-ref', 'Employee');
END$$
DELIMITER ;

CALL register_assignee_fields('MaterialRequisition');
CALL register_assignee_fields('GatePass');
CALL register_assignee_fields('GoodsReceiptNote');
CALL register_assignee_fields('ReturnNote');
CALL register_assignee_fields('FGDispatch');
CALL register_assignee_fields('PurchaseOrder');
CALL register_assignee_fields('SalesOrder');
CALL register_assignee_fields('ProductionOrder');
CALL register_assignee_fields('JobCard');
CALL register_assignee_fields('PackingRun');
CALL register_assignee_fields('QualityTest');
CALL register_assignee_fields('MaintenanceOrder');
CALL register_assignee_fields('Payment');
CALL register_assignee_fields('SalesInvoice');
CALL register_assignee_fields('PurchaseInvoice');
CALL register_assignee_fields('LeaveRequest');

DROP PROCEDURE register_assignee_fields;
