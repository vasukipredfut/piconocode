-- Audit Tracker — Group By
--
-- 1) A normalizing view over m0_audit_log that reconciles the two writer
--    conventions (AuditTrailListener writes workflow_code/workflow_instance_id
--    into entity_code/entity_id; GenericEntityController writes the real
--    business entity_code/row id) into one resolved_entity_code/resolved_entity_id
--    pair, using workflow_instance.business_ref to resolve workflow-transition
--    rows. business_ref has two conventions, both handled here (confirmed
--    against live data): GenericEntityController.startWorkflowFor sets
--    "EntityCode#id"; the M10 dispatch flow instead sets business_ref
--    directly to the vehicle registration number (no "#" at all) — guarded
--    with `business_ref LIKE '%#%'` so the latter isn't misparsed.
-- 2) vehicle_no/gp_number denormalized directly onto m0_audit_log at write
--    time (AuditTrailListener / GenericEntityController.writeAudit), so
--    Group By is a plain GROUP BY with no joins to business-entity tables —
--    and the "latest status" for a group is just its most recent row's own
--    action_kind/from_state/to_state, already tracked there for free.
-- 3) m0_audit_group_dimension: the selectable Group-By fields
--    (VEHICLE_NO/GATE_PASS_NO/TO_STATE/EMPLOYEE/DATE), each a plain column
--    on v_m0_audit_event. Registered in entity_registry so dimensions are
--    addable/editable via the generic entity admin CRUD without a code
--    deploy.
-- 4) Contract pages (category M0, not Workflow/Integration) documenting the
--    /api/m0/audit-tracker endpoints for the frontend to build against,
--    including a layout hint: dimension/filter selector on top, grouped
--    summary list below it.

CREATE OR REPLACE VIEW v_m0_audit_event AS
SELECT
  al.id,
  al.tenant_id,
  al.ts,
  DATE(al.ts) AS ts_date,
  al.actor_id,
  al.action_kind,
  al.vehicle_no,
  al.gp_number,
  CASE WHEN al.action_kind = 'workflow.transition' AND wi.business_ref LIKE '%#%'
       THEN SUBSTRING_INDEX(wi.business_ref, '#', 1)
       ELSE al.entity_code END AS resolved_entity_code,
  CASE WHEN al.action_kind = 'workflow.transition' AND wi.business_ref LIKE '%#%'
       THEN CAST(SUBSTRING_INDEX(wi.business_ref, '#', -1) AS UNSIGNED)
       ELSE al.entity_id END AS resolved_entity_id,
  al.workflow_code,
  al.workflow_instance_id,
  al.from_state,
  al.to_state,
  al.message,
  al.payload_json
FROM m0_audit_log al
LEFT JOIN workflow_instance wi ON wi.id = al.workflow_instance_id;

ALTER TABLE m0_audit_log
  ADD COLUMN IF NOT EXISTS vehicle_no VARCHAR(32) DEFAULT NULL AFTER entity_id,
  ADD COLUMN IF NOT EXISTS gp_number VARCHAR(32) DEFAULT NULL AFTER vehicle_no,
  ADD KEY IF NOT EXISTS idx_audit_vehicle (tenant_id, vehicle_no, ts),
  ADD KEY IF NOT EXISTS idx_audit_gpnumber (tenant_id, gp_number, ts);

-- Backfill historical rows from all three sources of vehicle/gate-pass
-- identity (see the view comment above for the two business_ref conventions).
UPDATE m0_audit_log al
JOIN workflow_instance wi ON wi.id = al.workflow_instance_id
JOIN gate_pass gp ON al.action_kind = 'workflow.transition' AND wi.business_ref = CONCAT('GatePass#', gp.id)
SET al.vehicle_no = gp.vehicle_no,
    al.gp_number = gp.gp_number;

UPDATE m0_audit_log al
JOIN workflow_instance wi ON wi.id = al.workflow_instance_id AND wi.business_ref NOT LIKE '%#%'
SET al.vehicle_no = wi.business_ref
WHERE al.action_kind = 'workflow.transition' AND al.vehicle_no IS NULL;

UPDATE m0_audit_log al
JOIN gate_pass gp ON al.action_kind <> 'workflow.transition' AND al.entity_code = 'GatePass' AND al.entity_id = gp.id
SET al.vehicle_no = gp.vehicle_no,
    al.gp_number = gp.gp_number;

CREATE TABLE IF NOT EXISTS m0_audit_group_dimension (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT -1,
  code VARCHAR(64) NOT NULL,
  display_label VARCHAR(128) NOT NULL,
  value_type VARCHAR(16) NOT NULL DEFAULT 'STRING',
  sort_order INT NOT NULL DEFAULT 0,
  active TINYINT(1) NOT NULL DEFAULT 1,
  source_column VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_agd_tenant_code (tenant_id, code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO m0_audit_group_dimension (tenant_id, code, display_label, value_type, sort_order, source_column) VALUES
  (-1, 'VEHICLE_NO',   'Vehicle Number',   'STRING', 10, 'vehicle_no'),
  (-1, 'GATE_PASS_NO', 'Gate Pass Number', 'STRING', 20, 'gp_number'),
  (-1, 'TO_STATE',     'Status',           'STRING', 25, 'to_state'),
  (-1, 'EMPLOYEE',     'Employee',         'STRING', 40, 'actor_id'),
  (-1, 'DATE',         'Date',             'DATE',   60, 'ts_date')
ON DUPLICATE KEY UPDATE display_label = VALUES(display_label), value_type = VALUES(value_type),
                        sort_order = VALUES(sort_order), source_column = VALUES(source_column);

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, created_at, value_column, display_column)
VALUES (-1, 'AuditGroupDimension', 'Audit Group Dimension', 'm0_audit_group_dimension',
        'Selectable Group-By field for the Audit Tracker Group By view.', 'M0', NOW(), 'id', 'display_label')
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), table_name = VALUES(table_name),
                        description = VALUES(description), category = VALUES(category);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) VALUES
  (-1, 'AuditGroupDimension', 'code',          'Code',          'STRING',  1, 1, 1, 1, 'text'),
  (-1, 'AuditGroupDimension', 'display_label', 'Display Label', 'STRING',  1, 1, 1, 1, 'text'),
  (-1, 'AuditGroupDimension', 'value_type',    'Value Type',    'STRING',  1, 1, 0, 1, 'text'),
  (-1, 'AuditGroupDimension', 'sort_order',    'Sort Order',    'INT',     1, 1, 0, 0, 'number'),
  (-1, 'AuditGroupDimension', 'active',        'Active',        'BOOLEAN', 1, 1, 0, 0, 'checkbox'),
  (-1, 'AuditGroupDimension', 'source_column', 'Source Column', 'STRING',  1, 1, 1, 1, 'text')
ON DUPLICATE KEY UPDATE display_label = VALUES(display_label), data_type = VALUES(data_type), widget_kind = VALUES(widget_kind);

-- Contract pages ---------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:audit-tracker-groupby', 'Audit Tracker — Group By',
  'Group audit history by one or more fields (Vehicle Number, Gate Pass Number, Status, Employee, Date) to see the latest status per group; drill into any row for the full history.',
  'M0', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:audit-tracker-groupby');

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED',
  '{"kind":"auditTrackerGroupBy","endpoints":{"dimensions":"/api/m0/audit-tracker/dimensions","summary":"/api/m0/audit-tracker/summary","detail":"/api/m0/audit-tracker/detail"},"multiSelectGroupBy":true,"groupByOptions":["VEHICLE_NO","GATE_PASS_NO","TO_STATE","EMPLOYEE","DATE"],"summaryColumns":[{"field":"memberCount","label":"Records"},{"field":"latestActivityAt","label":"Latest Activity"},{"field":"latestActionKind","label":"Latest Action"},{"field":"latestFromState","label":"From"},{"field":"latestToState","label":"To"}],"rowActions":[{"label":"View History","action":"page.open","page":"aria:audit-tracker-detail"}],"pageSize":25,"layout":{"dimensionSelector":{"position":"top"},"summaryList":{"position":"below"}}}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'aria:audit-tracker-groupby'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'aria:audit-tracker-groupby' AND pd.current_version_id IS NULL;

INSERT INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
SELECT -1, 'aria:audit-tracker-detail', 'Audit Tracker — Group Detail',
  'Complete chronological audit trail for the selected grouped record: status changes and activity history, oldest first.',
  'M0', 'PUBLISHED', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id = -1 AND code = 'aria:audit-tracker-detail');

INSERT INTO page_version (page_id, version, status, body_json, created_at)
SELECT pd.id, 1, 'PUBLISHED',
  '{"kind":"auditTrackerDetail","endpoints":{"detail":"/api/m0/audit-tracker/detail"},"columns":[{"field":"ts","label":"When"},{"field":"resolvedEntityCode","label":"Entity"},{"field":"resolvedEntityId","label":"Entity ID"},{"field":"actionKind","label":"Action"},{"field":"fromState","label":"From"},{"field":"toState","label":"To"},{"field":"actorId","label":"Actor"},{"field":"message","label":"Message"},{"field":"payloadJson","label":"Details"}],"pageSize":50}',
  NOW()
FROM page_definition pd
WHERE pd.tenant_id = -1 AND pd.code = 'aria:audit-tracker-detail'
  AND NOT EXISTS (SELECT 1 FROM page_version pv WHERE pv.page_id = pd.id);

UPDATE page_definition pd
JOIN page_version pv ON pv.page_id = pd.id AND pv.version = 1
SET pd.current_version_id = pv.id
WHERE pd.tenant_id = -1 AND pd.code = 'aria:audit-tracker-detail' AND pd.current_version_id IS NULL;

-- Entry point from the existing flat Audit Log page ---------------------
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_SET(pv.body_json, '$.rowActions', JSON_ARRAY(JSON_OBJECT('label', 'Group By View', 'action', 'page.open', 'page', 'aria:audit-tracker-groupby')))
WHERE pd.code = 'aria:audit-log' AND JSON_EXTRACT(pv.body_json, '$.rowActions') IS NULL;
