-- V143 — "Record First Weight" fields: Date, Entry Time, Weighment Officer
-- Name, Actual Weight. gate_pass_weighment already has weight_kg (one row
-- per reading_no: 1=loaded/first, 2=empty, 3=recheck) and captured_at/
-- captured_by, but captured_at is a single DATETIME (no separate Date +
-- Entry Time inputs) and captured_by is a user-id FK (no free-text officer
-- name). This adds the 3 missing columns, registers them so the generic
-- entity CRUD / Page Designer pick them up, and relabels weight_kg to
-- "Actual Weight" to match the requested field name. Idempotent.

-- 1) gate_pass_weighment columns are added by the Java caller
--    (M0PageSchemaInitialiser) before this script runs — MariaDB's
--    "ADD COLUMN IF NOT EXISTS" isn't portable to a plain-MySQL dev
--    instance, and ScriptUtils (which runs this file) can't parse a
--    DELIMITER $$ stored-procedure guard. See the V143 probe block.

-- 2) entity_registry row for GatePassWeighment — normally seeded by
--    V120 (aria_v2_bulk_register_legacy_entities), but that 3600-line
--    migration has no boot hook in M0PageSchemaInitialiser either, so a
--    dev DB that never had it hand-applied is missing this row entirely.
--    entity_field_registry.entity_code has an FK to entity_registry.code,
--    so the inserts below 42P01/1452 without this. Idempotent.
INSERT INTO entity_registry (code, display_name, table_name, category, lifecycle_kind)
  SELECT 'GatePassWeighment','Gate Pass Weighment','gate_pass_weighment','Legacy','DEFINED'
  WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='GatePassWeighment' OR table_name='gate_pass_weighment');

-- 3) entity_field_registry (GatePassWeighment) — core fields (same as
--    V120, re-asserted here defensively) plus the 3 new ones -----------
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'GatePassWeighment','gate_pass_id','Gate Pass Id','LONG',1,1,0,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='gate_pass_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'GatePassWeighment','weighbridge_code','Weighbridge Code','STRING',1,1,0,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='weighbridge_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'GatePassWeighment','reading_no','Reading No','INT',1,1,0,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='reading_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'GatePassWeighment','weight_kg','Actual Weight','DECIMAL',1,1,0,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='weight_kg');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'GatePassWeighment','captured_at','Captured At','DATETIME',1,1,0,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='captured_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'GatePassWeighment','captured_by','Captured By','LONG',1,1,0,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='captured_by');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'GatePassWeighment','source','Source','STRING',1,1,0,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='source');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'GatePassWeighment','weighment_date','Date','DATE','date',0,1,1,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='weighment_date');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'GatePassWeighment','entry_time','Entry Time','TIME','time',0,1,1,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='entry_time');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'GatePassWeighment','weight_officer','Weighment Officer Name','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePassWeighment' AND field_code='weight_officer');

-- Relabel weight_kg to "Actual Weight" for a DB where V120 already ran
-- with the old "Weight Kg" label (the INSERT above only fires when the
-- row is absent, so this UPDATE covers the already-present case).
UPDATE entity_field_registry SET display_label='Actual Weight'
  WHERE entity_code='GatePassWeighment' AND field_code='weight_kg';

-- 3) pico:weighment-capture page — add Date / Entry Time / Weighment
--    Officer Name to the vehicle & weighbridge context section, right
--    after weighbridge_code. Leaves gross_kg/tare_kg/net_kg untouched —
--    those carry deliberate Gross/Tare/Net labels for bulk-weighing
--    (see V47), separate from the GatePassWeighment "Actual Weight" above.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id=pv.id
SET pv.body_json = JSON_ARRAY_APPEND(
  pv.body_json,
  '$.layout.children[0].children[0].bindings.fields', JSON_OBJECT('name','weighment_date','label','Date','type','date','required',true),
  '$.layout.children[0].children[0].bindings.fields', JSON_OBJECT('name','entry_time','label','Entry Time','type','time','required',true),
  '$.layout.children[0].children[0].bindings.fields', JSON_OBJECT('name','weight_officer','label','Weighment Officer Name','type','text','required',true)
)
WHERE pd.code='pico:weighment-capture'
  AND JSON_SEARCH(pv.body_json, 'one', 'weighment_date') IS NULL;
