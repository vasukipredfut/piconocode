-- =====================================================================
-- M0 — register gate_pass as a Schema V2 entity + seed the
-- pico:recent-gate-passes list page (Round 3 follow-up).
--
-- After Round 3 the workflow engine writes gate_pass rows on every
-- transition via WorkflowTransitionListener. This migration makes that
-- table queryable through the Schema V2 list view so a Plantowner /
-- Manager can see live dispatch operations without any new React code.
-- All idempotent via NOT EXISTS guards.
-- =====================================================================

-- 1. Register GatePass entity --------------------------------------------------
INSERT INTO entity_registry (code, display_name, table_name, description, category)
SELECT 'GatePass', 'Gate Pass', 'gate_pass',
       'Dispatch gate-pass record written by the M10 workflow engine on every transition.',
       'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'GatePass');

-- 2. Register fields for the list view ----------------------------------------
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT * FROM (SELECT 'GatePass' c, 'id' f, 'ID' l, 'LONG' t, FALSE fil, TRUE so, FALSE se, TRUE re) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='id');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT 'GatePass', 'gp_number', 'GP #', 'STRING', TRUE, TRUE, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='gp_number');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT 'GatePass', 'flow_variant_code', 'Flow', 'STRING', TRUE, TRUE, FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='flow_variant_code');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT 'GatePass', 'vehicle_no', 'Vehicle', 'STRING', TRUE, TRUE, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='vehicle_no');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT 'GatePass', 'driver_name', 'Driver', 'STRING', TRUE, TRUE, TRUE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='driver_name');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT 'GatePass', 'status', 'Status', 'STRING', TRUE, TRUE, FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='status');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT 'GatePass', 'current_step', 'Current Step', 'STRING', TRUE, TRUE, FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='current_step');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT 'GatePass', 'created_at', 'Created', 'DATETIME', TRUE, TRUE, FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='created_at');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
SELECT 'GatePass', 'updated_at', 'Updated', 'DATETIME', FALSE, TRUE, FALSE, FALSE
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='updated_at');

-- 3. Seed pico:recent-gate-passes page (Schema V2 list view) -------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT 1, 'pico:recent-gate-passes',
       'Recent Gate Passes',
       'Live list of dispatch gate-passes — written by the M10 workflow engine on every transition.',
       'M10.Dispatch', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:recent-gate-passes');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
SELECT p.id, 1, 'PUBLISHED',
       JSON_OBJECT(
         'kind', 'list',
         'schemaVersion', 2,
         'dataSource', JSON_OBJECT(
            'type', 'entity',
            'entity', 'GatePass',
            'filter', JSON_OBJECT('and', JSON_ARRAY(
                JSON_OBJECT('field', 'tenant_id', 'op', 'eq', 'value', 'ctx:tenantId'))),
            'select', JSON_ARRAY('id','gp_number','flow_variant_code','vehicle_no','driver_name','status','current_step','created_at')
         ),
         'columns', JSON_ARRAY(
            JSON_OBJECT('field','gp_number','label','GP #','sticky','left','sortable',TRUE),
            JSON_OBJECT('field','flow_variant_code','label','Flow','render','badge'),
            JSON_OBJECT('field','vehicle_no','label','Vehicle'),
            JSON_OBJECT('field','driver_name','label','Driver'),
            JSON_OBJECT('field','status','label','Status','render','badge',
                'colorMap', JSON_OBJECT(
                    'DRAFT','grey','GATE_PASS_CREATED','blue',
                    'PARKING_IN','blue','DISPATCH_QUEUED','amber',
                    'QC_APPROVED','green','QC_REJECTED','red','CLOSED','green')),
            JSON_OBJECT('field','current_step','label','Step'),
            JSON_OBJECT('field','created_at','label','Created','render','datetime','format','DD-MMM HH:mm')
         ),
         'filters', JSON_ARRAY(
            JSON_OBJECT('field','status','label','Status','type','multi-select',
                'optionsSource', JSON_OBJECT('type','static','options', JSON_ARRAY(
                    'DRAFT','GATE_PASS_CREATED','PARKING_IN','DISPATCH_QUEUED','QC_APPROVED','QC_REJECTED','CLOSED'))),
            JSON_OBJECT('field','_q','label','Search','type','text-contains','applyTo', JSON_ARRAY('gp_number','vehicle_no','driver_name')),
            JSON_OBJECT('field','flow_variant_code','label','Flow','type','multi-select',
                'optionsSource', JSON_OBJECT('type','static','options', JSON_ARRAY(
                    'TANKER_IN','PACKING_IN','INTERNAL_TRANSFER_IN','THIRD_PARTY_IN','RETURN_STOCK_IN',
                    'TANKER_OUT','FG_OUT','PACKING_OUT','INTERNAL_TRANSFER_OUT','THIRD_PARTY_OUT')))
         ),
         'sort', JSON_OBJECT('default', JSON_ARRAY(JSON_OBJECT('field','created_at','dir','desc')),
                            'allow', JSON_ARRAY('created_at','status','gp_number')),
         'pagination', JSON_OBJECT('kind','server','pageSize',25,'options',JSON_ARRAY(10,25,50,100)),
         'header', JSON_OBJECT('title','Recent Gate Passes'),
         'responsive', JSON_OBJECT('mobile', JSON_OBJECT('renderAs','cards',
            'cardFields', JSON_ARRAY('gp_number','vehicle_no','status','created_at')))
       ),
       'V7_4 — Round 3 follow-up: register GatePass entity + seed list view bound to it.',
       CURRENT_TIMESTAMP
FROM page_definition p
WHERE p.tenant_id=1 AND p.code='pico:recent-gate-passes'
  AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (
    SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1
)
WHERE tenant_id=1 AND code='pico:recent-gate-passes' AND current_version_id IS NULL;
