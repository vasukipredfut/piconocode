-- Replaces the bespoke /api/m0/audit-tracker/detail endpoint with the
-- already-existing generic datasource/query mechanism (DataSourceQueryService):
-- the chronological detail drill-down is just "filter v_m0_audit_event by one
-- field, sort by ts" — exactly what that service already does for any
-- registered entity, with no custom SQL needed.
--
-- v_m0_audit_event is registered as a NEW entity (AuditEvent) rather than
-- repointing the existing AuditLog entity (which stays on the raw
-- m0_audit_log table — the flat aria:audit-log page renders legacy columns
-- the view doesn't expose, e.g. event_at/event_type/entity_type/details_json).
-- TenantScope derives scoping from information_schema.COLUMNS, which covers
-- views too, and the view already selects tenant_id — so this is
-- automatically tenant-scoped the same as any table-backed entity.

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category, created_at, value_column, display_column)
VALUES (-1, 'AuditEvent', 'Audit Event', 'v_m0_audit_event',
        'Read-only normalized audit event stream: one row per audit_log entry, with entity_code/entity_id reconciled across both writer conventions and vehicle_no/gp_number denormalized. Used by the Audit Tracker Group-By detail drill-down via the generic datasource query endpoint.',
        'M0', NOW(), 'id', 'message')
ON DUPLICATE KEY UPDATE display_name = VALUES(display_name), table_name = VALUES(table_name),
                        description = VALUES(description), category = VALUES(category);

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) VALUES
  (-1, 'AuditEvent', 'ts',                  'When',            'DATETIME', 1, 1, 0, 0, 'datetime'),
  (-1, 'AuditEvent', 'ts_date',             'Date',             'DATE',    1, 1, 0, 0, 'date'),
  (-1, 'AuditEvent', 'actor_id',            'Actor',            'LONG',    1, 1, 0, 0, 'text'),
  (-1, 'AuditEvent', 'action_kind',         'Action',           'STRING',  1, 1, 1, 0, 'text'),
  (-1, 'AuditEvent', 'vehicle_no',          'Vehicle Number',   'STRING',  1, 1, 1, 0, 'text'),
  (-1, 'AuditEvent', 'gp_number',           'Gate Pass Number', 'STRING',  1, 1, 1, 0, 'text'),
  (-1, 'AuditEvent', 'resolved_entity_code','Entity',           'STRING',  1, 1, 1, 0, 'text'),
  (-1, 'AuditEvent', 'resolved_entity_id',  'Entity ID',        'LONG',    1, 1, 0, 0, 'text'),
  (-1, 'AuditEvent', 'from_state',          'From',             'STRING',  1, 1, 1, 0, 'text'),
  (-1, 'AuditEvent', 'to_state',            'To',               'STRING',  1, 1, 1, 0, 'text'),
  (-1, 'AuditEvent', 'message',             'Message',          'STRING',  1, 1, 1, 0, 'text'),
  (-1, 'AuditEvent', 'payload_json',        'Details',          'JSON',    0, 0, 0, 0, 'textarea')
ON DUPLICATE KEY UPDATE display_label = VALUES(display_label), data_type = VALUES(data_type), widget_kind = VALUES(widget_kind);

-- Detail contract page now documents a generic datasource/query call instead
-- of a bespoke endpoint. filter.and[0].field is a placeholder for whichever
-- dimension column the user grouped by (vehicle_no / gp_number / to_state /
-- actor_id) — the frontend fills in field+value from the summary row's
-- groupKey before calling.
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_SET(pv.body_json,
    '$.endpoints', JSON_OBJECT('detail', '/api/m0/datasource/query'),
    '$.detailQueryTemplate', JSON_OBJECT(
      'spec', JSON_OBJECT(
        'type', 'entity',
        'entity', 'AuditEvent',
        'filter', JSON_OBJECT('and', JSON_ARRAY(JSON_OBJECT('field', '<dimensionColumn>', 'op', 'eq', 'value', '<groupKeyValue>'))),
        'sort', JSON_ARRAY(JSON_OBJECT('field', 'ts', 'dir', 'asc'))
      ),
      'page', 0,
      'size', 200
    ))
WHERE pd.code = 'aria:audit-tracker-detail';

UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_SET(pv.body_json, '$.endpoints.detail', '/api/m0/datasource/query')
WHERE pd.code = 'aria:audit-tracker-groupby';
