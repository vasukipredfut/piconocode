-- =====================================================================
-- V21 — Fix entity_field_registry.field_code values that don't match
-- the actual column name on the underlying table. These mismatches made
-- EntityFormModal's create / view / edit silently broken for 7 entities:
--   - View mode showed all fields empty (row data uses snake_case keys,
--     modal looked up camelCase)
--   - Create mode tried to INSERT camelCase column names; controller's
--     allowlist dropped them so nothing got persisted
-- =====================================================================

-- Driver: camelCase -> snake_case
UPDATE entity_field_registry SET field_code='dl_number'         WHERE entity_code='Driver' AND field_code='dlNumber';
UPDATE entity_field_registry SET field_code='dl_expiry'         WHERE entity_code='Driver' AND field_code='dlExpiry';
UPDATE entity_field_registry SET field_code='primary_mobile'    WHERE entity_code='Driver' AND field_code='primaryMobile';
UPDATE entity_field_registry SET field_code='alt_mobile'        WHERE entity_code='Driver' AND field_code='altMobile';
UPDATE entity_field_registry SET field_code='preferred_language' WHERE entity_code='Driver' AND field_code='preferredLanguage';
UPDATE entity_field_registry SET field_code='aadhaar_last4'     WHERE entity_code='Driver' AND field_code='aadhaarLast4';

-- Vehicle: camelCase -> snake_case + drop fields that don't exist
UPDATE entity_field_registry SET field_code='vehicle_type'           WHERE entity_code='Vehicle' AND field_code='vehicleType';
UPDATE entity_field_registry SET field_code='tanker_compartments'    WHERE entity_code='Vehicle' AND field_code='tankerCompartments';
UPDATE entity_field_registry SET field_code='tank_cleaning_cert_no'  WHERE entity_code='Vehicle' AND field_code='tankCleaningCertNo';
UPDATE entity_field_registry SET field_code='tank_cleaning_cert_expiry' WHERE entity_code='Vehicle' AND field_code='tankCleaningCertExpiry';
UPDATE entity_field_registry SET field_code='tare_weight_kg'         WHERE entity_code='Vehicle' AND field_code='tareWeightKg';
UPDATE entity_field_registry SET field_code='rc_expiry'              WHERE entity_code='Vehicle' AND field_code='rcExpiry';
UPDATE entity_field_registry SET field_code='insurance_expiry'       WHERE entity_code='Vehicle' AND field_code='insuranceExpiry';
UPDATE entity_field_registry SET field_code='puc_expiry'             WHERE entity_code='Vehicle' AND field_code='pucExpiry';
-- These don't exist on dispatch_vehicle; remove the registry rows
DELETE FROM entity_field_registry WHERE entity_code='Vehicle' AND field_code IN ('previousLoad','transporterId');

-- TankLocation: camelCase -> snake_case + drop nonexistent
UPDATE entity_field_registry SET field_code='tank_capacity_kl'   WHERE entity_code='TankLocation' AND field_code='tankCapacityKL';
UPDATE entity_field_registry SET field_code='current_oil_grade'  WHERE entity_code='TankLocation' AND field_code='currentOilGrade';
UPDATE entity_field_registry SET field_code='batch_density'      WHERE entity_code='TankLocation' AND field_code='batchDensity';
UPDATE entity_field_registry SET field_code='last_cleaned_on'    WHERE entity_code='TankLocation' AND field_code='lastCleanedOn';
UPDATE entity_field_registry SET field_code='min_headspace_kl'   WHERE entity_code='TankLocation' AND field_code='minHeadspaceKL';
DELETE FROM entity_field_registry WHERE entity_code='TankLocation' AND field_code='cleaningCertExpiry';

-- Vendor: m0_vendor only has id/tenant_id/name/gstin/city_id/state_id/is_msme/status
-- Drop fields that don't exist; the columns 'legalname' / 'tradename' etc don't exist either
DELETE FROM entity_field_registry WHERE entity_code='Vendor' AND field_code IN
  ('vendorRole','udyamNumber','msmeCategory','agreedCreditDays','oilGradeSupplied',
   'legalname','tradename','pan','contactPerson','contactPhone','contactEmail','active');
-- Ensure the actual columns are registered
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
  SELECT 'Vendor','name','Name','STRING',TRUE,TRUE,TRUE,TRUE,'text'
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Vendor' AND field_code='name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
  SELECT 'Vendor','gstin','GSTIN','STRING',TRUE,FALSE,TRUE,FALSE,'text'
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Vendor' AND field_code='gstin');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json)
  SELECT 'Vendor','status','Status','STRING',TRUE,TRUE,FALSE,TRUE,'select','["ACTIVE","INACTIVE","HOLD","BLACKLISTED"]'
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Vendor' AND field_code='status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
  SELECT 'Vendor','is_msme','MSME','BOOLEAN',TRUE,TRUE,FALSE,FALSE,'checkbox'
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Vendor' AND field_code='is_msme');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json)
  SELECT 'Vendor','city_id','City','LONG',TRUE,TRUE,FALSE,FALSE,'entity-ref','{"entity":"City","valueField":"id","labelField":"name"}'
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Vendor' AND field_code='city_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, options_json)
  SELECT 'Vendor','state_id','State','LONG',TRUE,TRUE,FALSE,FALSE,'entity-ref','{"entity":"State","valueField":"id","labelField":"name"}'
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='Vendor' AND field_code='state_id');

-- WorkflowInstance: drop camelCase rows; the entity is read-only so don't add
DELETE FROM entity_field_registry WHERE entity_code='WorkflowInstance' AND field_code IN ('currentState','assignedRole');

-- SavedView: the registry has view_code/view_name/base_page_code/shared but
-- the table actually has page_slug/name/description/scope/is_default — completely
-- different schema. Replace.
DELETE FROM entity_field_registry WHERE entity_code='SavedView';
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) VALUES
  ('SavedView','name','Name','STRING',TRUE,TRUE,TRUE,TRUE,'text'),
  ('SavedView','page_slug','Base Page','STRING',TRUE,TRUE,TRUE,TRUE,'text'),
  ('SavedView','description','Description','STRING',FALSE,FALSE,TRUE,FALSE,'textarea'),
  ('SavedView','scope','Scope','STRING',TRUE,TRUE,FALSE,FALSE,'select'),
  ('SavedView','is_default','Default','BOOLEAN',TRUE,TRUE,FALSE,FALSE,'checkbox');
UPDATE entity_field_registry SET options_json='["USER","ROLE","TENANT","PUBLIC"]'
  WHERE entity_code='SavedView' AND field_code='scope';

-- DispatchEvent: registry fields are completely synthetic; the table 'gate_pass'
-- doesn't have any of them. DispatchEvent is logically an alias for GatePass —
-- replace the registry with the gate_pass columns + point table_name.
UPDATE entity_registry SET table_name='gate_pass' WHERE code='DispatchEvent';
DELETE FROM entity_field_registry WHERE entity_code='DispatchEvent';
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind) VALUES
  ('DispatchEvent','gp_number','GP #','STRING',TRUE,TRUE,TRUE,TRUE,'text'),
  ('DispatchEvent','flow_variant_code','Variant','STRING',TRUE,TRUE,FALSE,TRUE,'text'),
  ('DispatchEvent','vehicle_no','Vehicle','STRING',TRUE,TRUE,TRUE,FALSE,'text'),
  ('DispatchEvent','driver_name','Driver','STRING',TRUE,TRUE,TRUE,FALSE,'text'),
  ('DispatchEvent','driver_phone','Phone','STRING',TRUE,FALSE,TRUE,FALSE,'text'),
  ('DispatchEvent','status','Status','STRING',TRUE,TRUE,FALSE,TRUE,'select'),
  ('DispatchEvent','current_step','Step','STRING',TRUE,TRUE,FALSE,FALSE,'text'),
  ('DispatchEvent','total_unloading_qty','Qty','DECIMAL',FALSE,TRUE,FALSE,FALSE,'number'),
  ('DispatchEvent','created_at','Created','DATETIME',TRUE,TRUE,FALSE,FALSE,'datetime');
