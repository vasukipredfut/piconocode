-- V129 — M10 outbound dispatch: variant prefill + QC + weight + loading fields.
--
-- The M10 outbound loading flow (Tanker-Outbound / Packing / FG variants) is
-- already substrate: workflows M10.TankerOutbound/PackLoading/FGLoading, the
-- gate_pass entity (+ qc/weighment/unloading children), ParkingBay/Order/SKU,
-- and ~30 pages exist. This migration only ADDS the extra fields the process
-- flow names, registers them (so Page Designer + generic CRUD + auto-derive
-- pick them up), and wires variant-conditional visibility via visible_when
-- (#form.flow_variant_code == '...') — DATA, no code. Idempotent.
--
-- Grandfathered gate_pass table (pre-CUSTOMIZATION-POLICY). Variant differences
-- are visible_when rows, NOT if(tenant) branches.

-- 1) gate_pass columns -------------------------------------------------
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loading_type VARCHAR(32) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS broker_name VARCHAR(128) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS invoice_no VARCHAR(64) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS invoice_qty DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS oil_rm_item VARCHAR(128) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS tanker_no VARCHAR(32) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS vehicle_category VARCHAR(32) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS transport_name VARCHAR(128) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loading_supervisor VARCHAR(128) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loading_tank_location VARCHAR(64) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS supervisor_name VARCHAR(128) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS checking_officer VARCHAR(128) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS qc_status VARCHAR(16) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS qc_reject_reason VARCHAR(255) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS weight_officer VARCHAR(128) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS weight_entry_at TIMESTAMP NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS route_id VARCHAR(64) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loading_bay VARCHAR(16) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS processed_qty DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS remaining_qty DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS dispatch_qty DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS parking_in_at TIMESTAMP NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS parking_out_at TIMESTAMP NULL;

-- 2) entity_field_registry (GatePass) — idempotent inserts -----------
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','loading_type','Loading Type','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='loading_type');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','broker_name','Broker','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='broker_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','invoice_no','Invoice No','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='invoice_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','invoice_qty','Invoice Qty','DECIMAL','number',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='invoice_qty');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','oil_rm_item','Oil / RM Item','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='oil_rm_item');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','tanker_no','Tanker No','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='tanker_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','vehicle_category','Vehicle Category','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='vehicle_category');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','transport_name','Transport Name','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='transport_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','loading_supervisor','Loading Supervisor','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.PackLoading'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='loading_supervisor');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','loading_tank_location','Loading Tank Location','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='loading_tank_location');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','supervisor_name','Supervisor Name','STRING','text',0,1,1,1,'#form.flow_variant_code == ''M10.TankerOutbound'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='supervisor_name');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','checking_officer','Checking Officer','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='checking_officer');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','qc_status','QC Status','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='qc_status');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','qc_reject_reason','QC Reject Reason','STRING','textarea',0,1,1,1,'#form.qc_status == ''REJECTED'''
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='qc_reject_reason');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','weight_officer','Weight Officer','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='weight_officer');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','weight_entry_at','Weight Entry Time','DATETIME','datetime',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='weight_entry_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','route_id','Route ID','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='route_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','loading_bay','Loading Bay','STRING','text',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='loading_bay');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','processed_qty','Processed Qty','DECIMAL','number',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='processed_qty');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','remaining_qty','Remaining Qty','DECIMAL','number',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='remaining_qty');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','dispatch_qty','Dispatch Qty','DECIMAL','number',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='dispatch_qty');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','parking_in_at','Parking In Time','DATETIME','datetime',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='parking_in_at');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT 'GatePass','parking_out_at','Parking Out Time','DATETIME','datetime',0,1,1,1,NULL
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='parking_out_at');

-- 3) projection field-map: land dispatch-entry / QC / loading form data on gate_pass
UPDATE m0_workflow_projection SET field_map_json='{"vehicle_no": ["vehicleNo", "vehicle_no", "registrationNo", "plate"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverMobile"], "loading_type": ["loadingType", "loading_type"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "oil_rm_item": ["oilRmItem", "oil_rm_item"], "tanker_no": ["tankerNo", "tanker_no"], "vehicle_category": ["vehicleCategory", "vehicle_category"], "transport_name": ["transportName", "transport_name"], "loading_supervisor": ["loadingSupervisor", "loading_supervisor"], "loading_tank_location": ["loadingTankLocation", "loading_tank_location"], "supervisor_name": ["supervisorName", "supervisor_name"], "checking_officer": ["checkingOfficer", "checking_officer"], "qc_status": ["qcStatus", "qc_status"], "qc_reject_reason": ["qcRejectReason", "qc_reject_reason"], "weight_officer": ["weightOfficer", "weight_officer"], "weight_entry_at": ["weightEntryAt", "weight_entry_at"], "route_id": ["routeId", "route_id"], "loading_bay": ["loadingBay", "loading_bay"], "processed_qty": ["processedQty", "processed_qty"], "remaining_qty": ["remainingQty", "remaining_qty"], "dispatch_qty": ["dispatchQty", "dispatch_qty"], "parking_in_at": ["parkingInAt", "parking_in_at"], "parking_out_at": ["parkingOutAt", "parking_out_at"]}' WHERE tenant_id=1 AND workflow_code='M10.TankerOutbound';
UPDATE m0_workflow_projection SET field_map_json='{"vehicle_no": ["vehicleNo", "vehicle_no", "registrationNo", "plate"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverMobile"], "loading_type": ["loadingType", "loading_type"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "oil_rm_item": ["oilRmItem", "oil_rm_item"], "tanker_no": ["tankerNo", "tanker_no"], "vehicle_category": ["vehicleCategory", "vehicle_category"], "transport_name": ["transportName", "transport_name"], "loading_supervisor": ["loadingSupervisor", "loading_supervisor"], "loading_tank_location": ["loadingTankLocation", "loading_tank_location"], "supervisor_name": ["supervisorName", "supervisor_name"], "checking_officer": ["checkingOfficer", "checking_officer"], "qc_status": ["qcStatus", "qc_status"], "qc_reject_reason": ["qcRejectReason", "qc_reject_reason"], "weight_officer": ["weightOfficer", "weight_officer"], "weight_entry_at": ["weightEntryAt", "weight_entry_at"], "route_id": ["routeId", "route_id"], "loading_bay": ["loadingBay", "loading_bay"], "processed_qty": ["processedQty", "processed_qty"], "remaining_qty": ["remainingQty", "remaining_qty"], "dispatch_qty": ["dispatchQty", "dispatch_qty"], "parking_in_at": ["parkingInAt", "parking_in_at"], "parking_out_at": ["parkingOutAt", "parking_out_at"]}' WHERE tenant_id=1 AND workflow_code='M10.PackLoading';
UPDATE m0_workflow_projection SET field_map_json='{"vehicle_no": ["vehicleNo", "vehicle_no", "registrationNo", "plate"], "driver_name": ["driverName", "driver_name"], "driver_phone": ["driverPhone", "driver_phone", "driverMobile"], "loading_type": ["loadingType", "loading_type"], "broker_name": ["brokerName", "broker_name"], "invoice_no": ["invoiceNo", "invoice_no"], "invoice_qty": ["invoiceQty", "invoice_qty"], "oil_rm_item": ["oilRmItem", "oil_rm_item"], "tanker_no": ["tankerNo", "tanker_no"], "vehicle_category": ["vehicleCategory", "vehicle_category"], "transport_name": ["transportName", "transport_name"], "loading_supervisor": ["loadingSupervisor", "loading_supervisor"], "loading_tank_location": ["loadingTankLocation", "loading_tank_location"], "supervisor_name": ["supervisorName", "supervisor_name"], "checking_officer": ["checkingOfficer", "checking_officer"], "qc_status": ["qcStatus", "qc_status"], "qc_reject_reason": ["qcRejectReason", "qc_reject_reason"], "weight_officer": ["weightOfficer", "weight_officer"], "weight_entry_at": ["weightEntryAt", "weight_entry_at"], "route_id": ["routeId", "route_id"], "loading_bay": ["loadingBay", "loading_bay"], "processed_qty": ["processedQty", "processed_qty"], "remaining_qty": ["remainingQty", "remaining_qty"], "dispatch_qty": ["dispatchQty", "dispatch_qty"], "parking_in_at": ["parkingInAt", "parking_in_at"], "parking_out_at": ["parkingOutAt", "parking_out_at"]}' WHERE tenant_id=1 AND workflow_code='M10.FGLoading';
