-- V72 — Gate Pass Closure gets its own table.
--
-- Today the "Gate Pass Closure — Final Checks" screen (aria:gate-pass-closure-form)
-- and the quick picker (aria:gate-pass-closure) submit into WorkflowProjectionListener,
-- which writes seal_verified/weighment_verified/qc_clearance/order_recon_done/
-- within_tolerance/invoice_count_verified straight onto `gate_pass` (added by V20/V47).
-- That mixes one-time closure-checklist data into the vehicle/lifecycle master row.
--
-- gate_pass_closure holds exactly that checklist, one row per gate pass, linked by
-- workflow_instance_id — the same key WorkflowProjectionListener already uses for
-- dispatch_vehicle_detail (see V36__projection_config_product_default.sql). gate_pass
-- itself keeps only status/current_step/closed_at (see V73 for the projection-config
-- side of this split).

CREATE TABLE IF NOT EXISTS `gate_pass_closure` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `workflow_instance_id` bigint(20) NOT NULL,
  `seal_verified` varchar(24) DEFAULT NULL,
  `invoice_count_verified` varchar(8) DEFAULT NULL,
  `qc_clearance` varchar(24) DEFAULT NULL,
  `weighment_verified` varchar(24) DEFAULT NULL,
  `order_recon_done` varchar(24) DEFAULT NULL,
  `within_tolerance` varchar(24) DEFAULT NULL,
  `closure_note` varchar(512) DEFAULT NULL,
  `closed_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_gpc_tenant_instance` (`tenant_id`,`workflow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- Register as a queryable entity (read-only via GenericEntityController — this table
-- is written exclusively by WorkflowProjectionListener via raw JdbcTemplate, same as
-- dispatch_vehicle_detail; no write_role needed since there's no direct insert/update
-- endpoint usage for it, only GET .../schema, GET .../{id}, and DataSourceController joins).
INSERT IGNORE INTO `entity_registry`
  (`tenant_id`,`code`,`display_name`,`table_name`,`description`,`category`,`created_at`,
   `read_role`,`write_role`,`approver_role`,`value_column`,`display_column`,`lifecycle_kind`,`workflow_code`)
VALUES
  (-1,'GatePassClosure','Gate Pass Closure','gate_pass_closure',
   'Final-checks closure record for a gate pass — seal/QC/weighment/reconciliation verdicts, written once by the M10 workflow engine at closure.',
   'M10',NOW(),NULL,NULL,NULL,NULL,NULL,NULL,NULL);

-- Field registry rows (schema/list/Designer visibility). Idempotent — same
-- MAX(id)+ROW_NUMBER() pattern used in V25 for SkuMaster/GatePass field additions.
INSERT INTO entity_field_registry
  (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x) + ROW_NUMBER() OVER (ORDER BY c.fc),
       -1, 'GatePassClosure', c.fc, c.lbl, c.dt, c.filt, c.sort, 0, 0, c.wk
FROM (
  SELECT 'workflow_instance_id' fc,'Workflow Instance' lbl,'LONG' dt,0 filt,0 sort,'number' wk
  UNION ALL SELECT 'seal_verified','Seal Verified','STRING',1,1,'select'
  UNION ALL SELECT 'invoice_count_verified','Invoice Count Verified','STRING',1,1,'select'
  UNION ALL SELECT 'qc_clearance','QC Clearance','STRING',1,1,'select'
  UNION ALL SELECT 'weighment_verified','Weighment Verified','STRING',1,1,'select'
  UNION ALL SELECT 'order_recon_done','Order vs Unloaded Reconciled','STRING',1,1,'select'
  UNION ALL SELECT 'within_tolerance','All Checks Clear','STRING',1,1,'select'
  UNION ALL SELECT 'closure_note','Closure Note','STRING',0,0,'textarea'
  UNION ALL SELECT 'closed_at','Closed At','DATETIME',1,1,'datetime'
) c
WHERE NOT EXISTS (
  SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassClosure' AND field_code=c.fc
);