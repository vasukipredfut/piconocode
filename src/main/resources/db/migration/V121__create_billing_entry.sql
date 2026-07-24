-- V103 — Billing Entry gets its own table, following the gate_pass_closure
-- precedent (V100/V101/V102).
--
-- Today the "Billing Entry" screen (aria:billing-entry, transition
-- PMT_CHECK_OK/WEIGHT_DIFF_OK -> BILLING_DONE on M10.FGLoading/PackLoading/
-- TankerOutbound) submits into WorkflowProjectionListener, which writes
-- invoice_no/checking_officer straight onto `gate_pass` (added by V1 baseline,
-- routed by V36). That mixes one-time billing-capture data into the
-- vehicle/lifecycle master row — and gate_pass.invoice_no is a single scalar
-- column, which can't hold more than one invoice number per gate pass.
--
-- billing_entry holds exactly that capture, one row per gate pass, linked by
-- workflow_instance_id — the same key WorkflowProjectionListener already uses
-- for dispatch_vehicle_detail (V36) and gate_pass_closure (V100). gate_pass
-- itself keeps only status/current_step/closed_at (see V104 for the
-- projection-config side of this split, V105 for the column drop).
--
-- invoice_numbers is JSON (a plain string array, e.g. ["INV-001","INV-002"])
-- rather than a second child table, because the field itself is small and
-- bounded to one gate pass — a repeating UI field backed by one JSON column,
-- same shape as other JSON-holding fields in this schema (SavedView.body_json,
-- AiAgent.input_schema_json, etc.), all CHECK(json_valid(...)) rather than a
-- MariaDB JSON pseudo-type (MariaDB's JSON is LONGTEXT + CHECK).
--
-- invoice_number_count is a persisted, derived column (COUNT of
-- invoice_numbers) rather than a computed/virtual one: WorkflowProjectionListener
-- writes it directly whenever it writes invoice_numbers (see V104 / the
-- accompanying WorkflowProjectionListener.java change), so it's always in
-- sync with no extra read-time computation, and list/filter/sort views over
-- entity_field_registry can treat it like any other plain INT column.

CREATE TABLE IF NOT EXISTS `billing_entry` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint(20) NOT NULL DEFAULT 1,
  `workflow_instance_id` bigint(20) NOT NULL,
  `invoice_numbers` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`invoice_numbers`)),
  `invoice_number_count` int(11) NOT NULL DEFAULT 0,
  `checking_officer` varchar(128) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_be_tenant_instance` (`tenant_id`,`workflow_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- Register as a queryable entity (read-only via GenericEntityController — this
-- table is written exclusively by WorkflowProjectionListener via raw
-- JdbcTemplate, same as gate_pass_closure/dispatch_vehicle_detail; no
-- write_role needed since there's no direct insert/update endpoint usage for
-- it, only GET .../schema, GET .../{id}, and DataSourceController joins).
INSERT IGNORE INTO `entity_registry`
  (`tenant_id`,`code`,`display_name`,`table_name`,`description`,`category`,`created_at`,
   `read_role`,`write_role`,`approver_role`,`value_column`,`display_column`,`lifecycle_kind`,`workflow_code`)
VALUES
  (-1,'BillingEntry','Billing Entry','billing_entry',
   'Sales-invoice capture for a gate pass — one or more invoice numbers plus the billing officer, written once by the M10 workflow engine at Billing Entry.',
   'M10',NOW(),NULL,NULL,NULL,NULL,NULL,NULL,NULL);

-- Field registry rows (schema/list/Designer visibility). Idempotent — same
-- MAX(id)+ROW_NUMBER() pattern used in V100 for GatePassClosure.
--
-- invoice_numbers' widget_kind is 'repeater' — NOT in the Renderer's current
-- vocabulary (today: text|number|date|datetime|textarea|select|multiselect|
-- entity-ref|checkbox|currency|chip; see earmsfrontend). This row documents
-- the intended widget; the frontend Renderer needs a matching case added
-- before this field can render as add/remove-row inputs (see accompanying
-- proposal — the field falls back to unstyled JSON-array-as-textarea in the
-- Designer/list views until that lands).
INSERT INTO entity_field_registry
  (id, tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT (SELECT COALESCE(MAX(id),0) FROM entity_field_registry x) + ROW_NUMBER() OVER (ORDER BY c.fc),
       -1, 'BillingEntry', c.fc, c.lbl, c.dt, c.filt, c.sort, 0, c.req, c.wk
FROM (
  SELECT 'workflow_instance_id' fc,'Workflow Instance' lbl,'LONG' dt,0 filt,0 sort,0 req,'number' wk
  UNION ALL SELECT 'invoice_numbers','Invoice Numbers','JSON',0,0,1,'repeater'
  UNION ALL SELECT 'invoice_number_count','Invoice Number Count','INT',1,1,0,'number'
  UNION ALL SELECT 'checking_officer','Billing Officer','STRING',1,1,0,'text'
) c
WHERE NOT EXISTS (
  SELECT 1 FROM entity_field_registry WHERE entity_code='BillingEntry' AND field_code=c.fc
);
