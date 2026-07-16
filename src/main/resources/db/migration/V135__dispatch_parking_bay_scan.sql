-- =====================================================================
-- V135 — Dispatch Parking Bay In screen (pico:dispatch-parking-scan).
--
-- Officer picks the Gate Pass # from a dropdown backed by the generic
-- entity API (GatePass) — same field also accepts a scanned/typed value,
-- per the existing pico:parking-scan precedent (V42) whose gp_number
-- field is plain text with a "scan or type" placeholder. On selecting a
-- row, vehicle_no / flow_variant_code / loading_type auto-fill as
-- disabled context fields (per user: onChange -> pass row values into
-- the form, shown disabled). Submitting creates the dispatch_parking_
-- bay_entry row (plain entity create — no workflow on this table).
--
-- NOTE — two page-body keys below are this migration's OWN convention,
-- not something already demonstrated elsewhere in this repo's migrations:
--   1) FormField "optionsSource":{"type":"entity",...} for a live
--      entity-backed dropdown (existing precedent is "static" options
--      only, e.g. V42's bay_code select).
--   2) FormField "onChangeFill" mapping selected-row columns onto sibling
--      field names, and the "entity.create" WorkflowActionButton action
--      (existing confirmed action family is "entity.update-field").
-- Confirm both against the frontend Renderer; adjust key names here if
-- it expects different ones — the DATA below (columns, registry rows)
-- does not depend on getting these two exactly right on the first try.
-- =====================================================================

-- 1) dispatch_parking_bay_entry — extra display context columns --------
ALTER TABLE dispatch_parking_bay_entry ADD COLUMN IF NOT EXISTS flow_variant_code VARCHAR(64) NULL;
ALTER TABLE dispatch_parking_bay_entry ADD COLUMN IF NOT EXISTS loading_type VARCHAR(32) NULL;

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'ParkingBayEntry','flow_variant_code','Flow Variant','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ParkingBayEntry' AND field_code='flow_variant_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'ParkingBayEntry','loading_type','Loading Type','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ParkingBayEntry' AND field_code='loading_type');

-- gate_pass_no becomes a live entity-backed dropdown (extends the
-- already-existing options_source_json column — same mechanism used for
-- static selects like ownership_type/po_verified, "entity" type is new).
UPDATE entity_field_registry
   SET widget_kind = 'select',
       options_source_json = '{"type":"entity","entity":"GatePass","valueField":"gp_number","labelField":"gp_number"}'
 WHERE entity_code='ParkingBayEntry' AND field_code='gate_pass_no';

-- 2) pico:dispatch-parking-scan page ------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
  SELECT 1,'pico:dispatch-parking-scan','Dispatch — Parking Bay In','Pick (or scan) the gate pass, confirm vehicle/flow/loading context, mark parking in.','M10.Dispatch','PUBLISHED'
  WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dispatch-parking-scan');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
  SELECT p.id, 1, 'PUBLISHED',
  '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Parking Bay In","fields":[{"kind":"FormField","type":"select","name":"gateNo","label":"Gate Pass #","placeholder":"Scan QR or select gate pass","optionsSource":{"type":"entity","entity":"GatePass","lookupType": "gpNumbers","valueField":"gp_number","labelField":"gp_number"},"onChangeFill":{"vehicleNo":"vehicle_no","flowVariantCode":"flow_variant_code","loadingType":"loading_type"},"required":true},{"kind":"FormField","type":"text","name":"vehicleNo","label":"Vehicle Number","disabled":true},{"kind":"FormField","type":"text","name":"flowVariantCode","label":"Flow Variant","disabled":true},{"kind":"FormField","type":"text","name":"loadingType","label":"Loading Type","disabled":true},{"kind":"WorkflowActionButton","label":"Mark Parking In","action":"entity.create","entity":"ParkingBayEntry","variant":"primary"}]}]}',
  'V135', CURRENT_TIMESTAMP
  FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dispatch-parking-scan'
    AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1)
  WHERE tenant_id=1 AND code='pico:dispatch-parking-scan' AND current_version_id IS NULL;
