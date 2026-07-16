-- =====================================================================
-- V134 — Dispatch Vehicle Details as its OWN entity (not gate_pass
-- columns), with Tanker / Packing / FG variant fields, + a Parking Queue
-- page feeding it.
--
-- Business spec (as given):
--   Tanker:   Ownership, Broker, Invoice No/Qty, Vendor, Oil RM Item,
--             Tanker No, Driver Name/Contact, PO Verified (+ override
--             reason when not verified), Dispatch Officer Name.
--   Packing:  Ownership, Transport Name, Packing/FG Stock Details,
--             Driver Name/Contact.
--   FG:       same as Packing.
--
-- All of the above live on a NEW dispatch_vehicle_detail table/entity,
-- separate from gate_pass. The officer's single "Save Vehicle Details"
-- WorkflowActionButton must still both (a) persist this record AND
-- (b) advance the gate_pass workflow step — same button, two tables.
--
-- WorkflowProjectionListener (com.aria.workflow.projection) already loops
-- over EVERY m0_workflow_projection row matching the transitioning
-- workflow_code — it is not limited to one row per workflow. The only
-- blocker is m0_workflow_projection's UNIQUE KEY (tenant_id, workflow_code),
-- which allows just one target_table per workflow. Widening it to include
-- target_table lets a SECOND projection row target dispatch_vehicle_detail
-- for the same M10.TankerOutbound/PackLoading/FGLoading codes that already
-- project into gate_pass — no new Java, same listener, same button.
-- =====================================================================

-- 1) dispatch_vehicle_detail table --------------------------------------
CREATE TABLE IF NOT EXISTS dispatch_vehicle_detail (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  flow_variant_code VARCHAR(64) NULL,
  current_step VARCHAR(64) NULL,
  ownership_type VARCHAR(16) NULL,
  vehicle_no VARCHAR(32) NULL,
  driver_name VARCHAR(128) NULL,
  driver_phone VARCHAR(16) NULL,
  broker_name VARCHAR(128) NULL,
  invoice_no VARCHAR(64) NULL,
  invoice_qty DECIMAL(12,3) NULL,
  vendor_name VARCHAR(128) NULL,
  oil_rm_item VARCHAR(128) NULL,
  tanker_no VARCHAR(32) NULL,
  po_verified VARCHAR(32) NULL,
  po_override_reason VARCHAR(255) NULL,
  dispatch_officer_name VARCHAR(128) NULL,
  transport_name VARCHAR(128) NULL,
  packing_fg_stock_details VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_dvd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- 2) entity_registry + entity_field_registry ----------------------------
INSERT INTO entity_registry (code, display_name, table_name, description, category)
  SELECT 'DispatchVehicleDetail','Dispatch Vehicle Detail','dispatch_vehicle_detail','Front-office vehicle/driver/broker/PO capture per gate pass — Tanker/Packing/FG variants.','M10'
  WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='DispatchVehicleDetail');

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'DispatchVehicleDetail','flow_variant_code','Flow Variant','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='flow_variant_code');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'DispatchVehicleDetail','current_step','Current Step','STRING','text',0,1,1,0
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='current_step');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, options_source_json)
  SELECT 'DispatchVehicleDetail','ownership_type','Ownership','STRING','select',0,1,1,0,'{"type":"static","options":["OWN VEHICLE","HIRED VEHICLE"]}'
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='ownership_type');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'DispatchVehicleDetail','vehicle_no','Tanker / Vehicle Number','STRING','text',1,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='vehicle_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'DispatchVehicleDetail','driver_name','Driver Name','STRING','text',1,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='driver_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'DispatchVehicleDetail','driver_phone','Driver Contact','STRING','text',1,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='driver_phone');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','broker_name','Broker Details','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='broker_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','invoice_no','Invoice No','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='invoice_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','invoice_qty','Invoice Qty','DECIMAL','number',0,1,1,0,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='invoice_qty');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','vendor_name','Vendor Details','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='vendor_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','oil_rm_item','Oil RM Details','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='oil_rm_item');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','tanker_no','Tanker Number','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='tanker_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, options_source_json, visible_when)
  SELECT 'DispatchVehicleDetail','po_verified','PO Verified','STRING','select',0,1,1,0,'{"type":"static","options":["YES","NO - MANAGER APPROVAL"]}','#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='po_verified');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when, required_when)
  SELECT 'DispatchVehicleDetail','po_override_reason','Reason (PO not verified)','STRING','textarea',0,0,0,1,
    '#form.flow_variant_code == ''M10.TankerOutbound'' && #form.po_verified != ''YES''',
    '#form.flow_variant_code == ''M10.TankerOutbound'' && #form.po_verified != ''YES'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='po_override_reason');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','dispatch_officer_name','Dispatch Front Officer Name','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='dispatch_officer_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','transport_name','Transport Name','STRING','text',0,1,1,1,
    '#form.flow_variant_code == ''M10.PackLoading'' || #form.flow_variant_code == ''M10.FGLoading'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='transport_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'DispatchVehicleDetail','packing_fg_stock_details','Packing / FG Stock Details','STRING','textarea',0,0,0,1,
    '#form.flow_variant_code == ''M10.PackLoading'' || #form.flow_variant_code == ''M10.FGLoading'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='DispatchVehicleDetail' AND field_code='packing_fg_stock_details');

-- 3) widen m0_workflow_projection's unique key so a workflow_code can
--    project into MORE THAN ONE target_table (needed for the same
--    transition to also write dispatch_vehicle_detail).
DROP PROCEDURE IF EXISTS v134_widen_wf_projection_key;
DELIMITER $$
CREATE PROCEDURE v134_widen_wf_projection_key()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.statistics
    WHERE table_schema=DATABASE() AND table_name='m0_workflow_projection'
      AND index_name='uq_wf_projection' AND column_name='target_table'
  ) THEN
    ALTER TABLE m0_workflow_projection DROP INDEX uq_wf_projection;
    ALTER TABLE m0_workflow_projection ADD UNIQUE KEY uq_wf_projection (tenant_id, workflow_code, target_table);
  END IF;
END$$
DELIMITER ;
CALL v134_widen_wf_projection_key();
DROP PROCEDURE v134_widen_wf_projection_key;

-- 4) second projection row per outbound workflow, targeting
--    dispatch_vehicle_detail (in addition to whatever already targets
--    gate_pass) — same WorkflowActionButton, same transition, two rows.
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
  SELECT 1,'M10.TankerOutbound','dispatch_vehicle_detail','workflow_instance_id','current_step','flow_variant_code',
    '{"ownership_type": ["ownershipType", "ownership_type"], "vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverContact"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "vendor_name": ["vendorName", "vendor_name"], "oil_rm_item": ["oilRmItem", "oil_rm_item"], "tanker_no": ["tankerNo", "tanker_no"], "po_verified": ["poVerified", "po_verified"], "po_override_reason": ["poOverrideReason", "po_override_reason"], "dispatch_officer_name": ["dispatchOfficerName", "dispatch_officer_name"], "transport_name": ["transportName", "transport_name"], "packing_fg_stock_details": ["packingFgStockDetails", "packing_fg_stock_details"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.TankerOutbound' AND target_table='dispatch_vehicle_detail');
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
  SELECT 1,'M10.PackLoading','dispatch_vehicle_detail','workflow_instance_id','current_step','flow_variant_code',
    '{"ownership_type": ["ownershipType", "ownership_type"], "vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverContact"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "vendor_name": ["vendorName", "vendor_name"], "oil_rm_item": ["oilRmItem", "oil_rm_item"], "tanker_no": ["tankerNo", "tanker_no"], "po_verified": ["poVerified", "po_verified"], "po_override_reason": ["poOverrideReason", "po_override_reason"], "dispatch_officer_name": ["dispatchOfficerName", "dispatch_officer_name"], "transport_name": ["transportName", "transport_name"], "packing_fg_stock_details": ["packingFgStockDetails", "packing_fg_stock_details"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.PackLoading' AND target_table='dispatch_vehicle_detail');
INSERT INTO m0_workflow_projection (tenant_id, workflow_code, target_table, key_column, state_columns, variant_column, field_map_json)
  SELECT 1,'M10.FGLoading','dispatch_vehicle_detail','workflow_instance_id','current_step','flow_variant_code',
    '{"ownership_type": ["ownershipType", "ownership_type"], "vehicle_no": ["vehicleNo", "vehicle_no"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverContact"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "vendor_name": ["vendorName", "vendor_name"], "oil_rm_item": ["oilRmItem", "oil_rm_item"], "tanker_no": ["tankerNo", "tanker_no"], "po_verified": ["poVerified", "po_verified"], "po_override_reason": ["poOverrideReason", "po_override_reason"], "dispatch_officer_name": ["dispatchOfficerName", "dispatch_officer_name"], "transport_name": ["transportName", "transport_name"], "packing_fg_stock_details": ["packingFgStockDetails", "packing_fg_stock_details"]}'
  WHERE NOT EXISTS (SELECT 1 FROM m0_workflow_projection WHERE tenant_id=1 AND workflow_code='M10.FGLoading' AND target_table='dispatch_vehicle_detail');

-- 5) pico:dispatch-vehicle-details — variant-conditional field list -----
-- Same layout/WorkflowActionButton mechanism as today (unchanged,
-- proven) — the button still fires ONE workflow.transition; the listener
-- now projects it into BOTH gate_pass (unchanged fields) AND
-- dispatch_vehicle_detail (this page's fields) via the two config rows
-- above. Each conditional field carries visibleWhen/requiredWhen (per
-- .claude/skills/new-page.md), evaluated against #form, seeded from the
-- current workflow instance (flow_variant_code available from the very
-- first page load, before any save).
UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Vehicle & Dispatch Details","fields":[{"kind":"FormField","type":"select","name":"ownershipType","label":"Ownership","options":["OWN VEHICLE","HIRED VEHICLE"]},{"kind":"FormField","type":"text","name":"vehicleNo","label":"Tanker / Vehicle Number"},{"kind":"FormField","type":"text","name":"driverName","label":"Driver Name"},{"kind":"FormField","type":"text","name":"driverPhone","label":"Driver Contact"},{"kind":"FormField","type":"text","name":"brokerName","label":"Broker Details","visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound''"},{"kind":"FormField","type":"text","name":"invoiceNo","label":"Invoice No","visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound''"},{"kind":"FormField","type":"number","name":"invoiceQty","label":"Invoice Qty","visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound''"},{"kind":"FormField","type":"text","name":"vendorName","label":"Vendor Details","visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound''"},{"kind":"FormField","type":"text","name":"oilRmItem","label":"Oil RM Details","visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound''"},{"kind":"FormField","type":"text","name":"tankerNo","label":"Tanker Number","visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound''"},{"kind":"FormField","type":"select","name":"poVerified","label":"PO Verified","options":["YES","NO - MANAGER APPROVAL"],"visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound''"},{"kind":"FormField","type":"text","name":"poOverrideReason","label":"Reason (if PO not verified)","visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound'' && #form.poVerified != ''YES''","requiredWhen":"#form.flow_variant_code == ''M10.TankerOutbound'' && #form.poVerified != ''YES''"},{"kind":"FormField","type":"text","name":"dispatchOfficerName","label":"Dispatch Front Officer Name","visibleWhen":"#form.flow_variant_code == ''M10.TankerOutbound''"},{"kind":"FormField","type":"text","name":"transportName","label":"Transport Name","visibleWhen":"#form.flow_variant_code == ''M10.PackLoading'' || #form.flow_variant_code == ''M10.FGLoading''"},{"kind":"FormField","type":"textarea","name":"packingFgStockDetails","label":"Packing / FG Stock Details","visibleWhen":"#form.flow_variant_code == ''M10.PackLoading'' || #form.flow_variant_code == ''M10.FGLoading''"},{"kind":"WorkflowActionButton","label":"Save Vehicle Details","action":"workflow.transition","variant":"primary"}]}]}'
WHERE pd.code = 'pico:dispatch-vehicle-details' AND pv.status = 'PUBLISHED';

-- 6) dispatch_parking_bay_entry — show which vehicle is in the queue ----
ALTER TABLE dispatch_parking_bay_entry ADD COLUMN IF NOT EXISTS vehicle_no VARCHAR(32) NULL;

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'ParkingBayEntry','vehicle_no','Vehicle No','STRING',TRUE,TRUE,TRUE,TRUE
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ParkingBayEntry' AND field_code='vehicle_no');

-- 7) pico:dispatch-parking-queue — left-side queue feeding the form -----
-- Lists vehicles currently sitting in a parking bay (out_time not yet
-- stamped). Opening a row navigates to pico:dispatch-vehicle-details with
-- the gate pass number as a route param (param name to be confirmed
-- against the frontend's step-instance router before go-live).
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
  SELECT 1,'pico:dispatch-parking-queue','Dispatch — Parking Queue','Vehicles currently parked, awaiting dispatch vehicle-details entry.','M10.Dispatch','PUBLISHED'
  WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=1 AND code='pico:dispatch-parking-queue');

INSERT INTO page_version (page_id, version, status, body_json, rationale, published_at)
  SELECT p.id, 1, 'PUBLISHED',
  '{"kind":"list","schemaVersion":2,"dataSource":{"type":"entity","entity":"ParkingBayEntry","filter":{"and":[{"field":"tenant_id","op":"eq","value":"ctx:tenantId"},{"field":"out_time","op":"isNull"}]},"select":["id","gate_pass_no","vehicle_no","in_time"]},"columns":[{"field":"gate_pass_no","label":"Gate Pass #","sticky":"left","sortable":true},{"field":"vehicle_no","label":"Vehicle No","sortable":true},{"field":"in_time","label":"In Time","render":"datetime","sortable":true}],"rowActions":[{"label":"Open Dispatch Form","action":"page.open","page":"pico:dispatch-vehicle-details","params":{"gpNumber":"row.gate_pass_no"}}],"sort":{"default":[{"field":"in_time","dir":"asc"}]},"pagination":{"kind":"server","pageSize":25},"header":{"title":"Parking Queue"},"responsive":{"mobile":{"renderAs":"cards","cardFields":["gate_pass_no","vehicle_no","in_time"]}}}',
  'V134', CURRENT_TIMESTAMP
  FROM page_definition p WHERE p.tenant_id=1 AND p.code='pico:dispatch-parking-queue'
    AND NOT EXISTS (SELECT 1 FROM page_version v WHERE v.page_id = p.id AND v.version = 1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id = page_definition.id AND version = 1 LIMIT 1)
  WHERE tenant_id=1 AND code='pico:dispatch-parking-queue' AND current_version_id IS NULL;
