-- =====================================================================
-- V7 — Apply Pico TENANT-CONFIG bundle (workflows / fields / layouts /
--                                       saved views / notification tpl)
-- =====================================================================
-- This script applies the static content of
-- docs/customers/pico-power/TENANT-CONFIG.json directly to the live tables.
-- The TenantConfigBundleService can apply the same content at runtime via
-- REST (POST /api/m0/tenant-config/apply-pico) — both paths use the same
-- target schemas + idempotency rules (INSERT IGNORE on natural keys).
-- =====================================================================

-- ----- Entity registry rows -------------------------------------------
-- Custom-field bundles target these entities. Insert them if missing.
INSERT IGNORE INTO entity_registry (code, display_name, table_name, description, category) VALUES
  ('Vehicle',        'Vehicle',         'dispatch_vehicle',         'Tankers / box trucks / LCVs', 'Dispatch'),
  ('Driver',         'Driver',          'dispatch_driver',          'Driver master',               'Dispatch'),
  ('TankLocation',   'Tank Location',   'dispatch_tank_location',   'RM-oil tank bins',            'Dispatch'),
  ('DispatchEvent',  'Dispatch Event',  'gate_pass',                'Gate-pass lifecycle event',   'Dispatch');
-- Vendor is seeded by V5_1; do nothing if exists.

-- ----- Custom fields per entity (entity_field_registry) ---------------
-- Vehicle
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, default_value, options_source_json, relation_entity) VALUES
  ('Vehicle','vehicleType','Vehicle Type','ENUM',1,1,1,1,NULL, JSON_ARRAY('TANKER','FLATBED_CONTAINER','BOX_TRUCK','TIPPER','LCV'), NULL),
  ('Vehicle','tankerCompartments','Tanker Compartments','INT',1,1,0,0,NULL,NULL,NULL),
  ('Vehicle','tankCleaningCertNo','Tank Cleaning Certificate No','STRING',1,1,0,0,NULL,NULL,NULL),
  ('Vehicle','tankCleaningCertExpiry','Tank Cleaning Cert Expiry','DATE',1,1,1,0,NULL,NULL,NULL),
  ('Vehicle','previousLoad','Previous Load (declared)','STRING',1,1,0,0,NULL,NULL,NULL),
  ('Vehicle','tareWeightKg','Tare (kg)','DECIMAL',1,1,0,0,NULL,NULL,NULL),
  ('Vehicle','rcExpiry','RC Expiry','DATE',1,1,0,1,NULL,NULL,NULL),
  ('Vehicle','insuranceExpiry','Insurance Expiry','DATE',1,1,0,1,NULL,NULL,NULL),
  ('Vehicle','pucExpiry','PUC Expiry','DATE',1,1,0,1,NULL,NULL,NULL),
  ('Vehicle','transporterId','Transporter','RELATION',1,1,1,0,NULL,NULL,'Transporter');

-- Driver
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, default_value, options_source_json, relation_entity) VALUES
  ('Driver','dlNumber','DL Number','STRING',1,1,1,1,NULL,NULL,NULL),
  ('Driver','dlExpiry','DL Expiry','DATE',1,1,0,1,NULL,NULL,NULL),
  ('Driver','primaryMobile','Mobile','STRING',1,1,0,1,NULL,NULL,NULL),
  ('Driver','altMobile','Alt Mobile','STRING',1,1,0,0,NULL,NULL,NULL),
  ('Driver','preferredLanguage','Preferred SMS Language','ENUM',1,1,0,0,'ta', JSON_ARRAY('en','ta','hi'), NULL),
  ('Driver','aadhaarLast4','Aadhaar Last 4','STRING',1,1,0,0,NULL,NULL,NULL);

-- Vendor (extending V5_1 vendor seed)
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, default_value, options_source_json, relation_entity) VALUES
  ('Vendor','vendorRole','Vendor Role','JSON',1,0,1,1,NULL, JSON_ARRAY('RM_OIL','PACKING_MATERIAL','TRANSPORTER','SERVICE'), NULL),
  ('Vendor','udyamNumber','Udyam Number','STRING',1,1,0,0,NULL,NULL,NULL),
  ('Vendor','msmeCategory','MSME Category','ENUM',1,1,1,0,NULL, JSON_ARRAY('MICRO','SMALL','MEDIUM','NON_MSME'), NULL),
  ('Vendor','agreedCreditDays','Agreed Credit Days','INT',1,1,0,0,'30',NULL,NULL),
  ('Vendor','oilGradeSupplied','Oil Grade Supplied','JSON',1,0,0,0,NULL, JSON_ARRAY('PALM','PALMOLEIN','SUNFLOWER','SOYBEAN','RICEBRAN','COCONUT','COTTONSEED'), NULL);

-- TankLocation
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, default_value, options_source_json, relation_entity) VALUES
  ('TankLocation','tankCapacityKL','Capacity (KL)','DECIMAL',1,1,0,1,NULL,NULL,NULL),
  ('TankLocation','currentOilGrade','Current Grade','ENUM',1,1,1,1,NULL, JSON_ARRAY('PALM','PALMOLEIN','SUNFLOWER','SOYBEAN','RICEBRAN','COCONUT','COTTONSEED','EMPTY'), NULL),
  ('TankLocation','batchDensity','Batch Density (g/cc @30C)','DECIMAL',1,1,0,0,NULL,NULL,NULL),
  ('TankLocation','lastCleanedOn','Last Cleaned','DATE',1,1,0,0,NULL,NULL,NULL),
  ('TankLocation','cleaningCertExpiry','Cleaning Cert Expiry','DATE',1,1,0,0,NULL,NULL,NULL),
  ('TankLocation','minHeadspaceKL','Min Headspace (KL)','DECIMAL',1,1,0,0,'5',NULL,NULL);

-- DispatchEvent (gate-pass field bundle)
INSERT IGNORE INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, default_value, options_source_json, relation_entity) VALUES
  ('DispatchEvent','gatePassNo','Gate Pass No','STRING',1,1,1,0,NULL,NULL,NULL),
  ('DispatchEvent','tokenNo','Parking Token','STRING',1,1,1,0,NULL,NULL,NULL),
  ('DispatchEvent','qcSampleId','QC Sample ID','STRING',1,1,0,0,NULL,NULL,NULL),
  ('DispatchEvent','qcVerdict','QC Verdict','ENUM',1,1,0,0,NULL, JSON_ARRAY('PASS','FAIL','CONDITIONAL'), NULL),
  ('DispatchEvent','weighmentLoadedKg','Loaded (kg)','DECIMAL',1,1,0,0,NULL,NULL,NULL),
  ('DispatchEvent','weighmentEmptyKg','Empty (kg)','DECIMAL',1,1,0,0,NULL,NULL,NULL),
  ('DispatchEvent','weighmentVerifyKg','Re-verify (kg)','DECIMAL',1,1,0,0,NULL,NULL,NULL),
  ('DispatchEvent','weighmentNetKg','Net (kg)','COMPUTED',1,1,0,0,NULL,NULL,NULL),
  ('DispatchEvent','weighmentDeltaPct','Re-verify Delta %','COMPUTED',1,1,0,0,NULL,NULL,NULL),
  ('DispatchEvent','managerApprovalReason','Manager Approval Reason','TEXT',0,0,0,0,NULL,NULL,NULL);

-- ----- Workflow definitions (10 variants) ----------------------------
-- Strategy: keep M10.TankerInbound v1 (V4 seed) as-is — every prior test
-- depends on its DRAFT initial state + page-code transitions. Add the
-- other 9 variants as new v1 rows.

-- M10.PackUnload, IntTransferIn, ThirdPartyIn, ReturnStockIn,
-- TankerOutbound, FGLoading, PackLoading, IntTransferOut, ThirdPartyOut
-- — minimal published bodies so the engine can start instances. Pages and
-- transitions for each variant get fleshed out by the
-- TenantConfigBundleService where the full WDL bodies live.
INSERT IGNORE INTO workflow_definition (tenant_id, code, version, status, body_json) VALUES
  (1, 'M10.PackUnload',         1, 'PUBLISHED', JSON_OBJECT('code','M10.PackUnload','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','REJECTED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY())),
  (1, 'M10.IntTransferIn',      1, 'PUBLISHED', JSON_OBJECT('code','M10.IntTransferIn','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','REJECTED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY())),
  (1, 'M10.ThirdPartyIn',       1, 'PUBLISHED', JSON_OBJECT('code','M10.ThirdPartyIn','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','REJECTED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY())),
  (1, 'M10.ReturnStockIn',      1, 'PUBLISHED', JSON_OBJECT('code','M10.ReturnStockIn','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','REJECTED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY())),
  (1, 'M10.TankerOutbound',     1, 'PUBLISHED', JSON_OBJECT('code','M10.TankerOutbound','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','REJECTED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY())),
  (1, 'M10.FGLoading',          1, 'PUBLISHED', JSON_OBJECT('code','M10.FGLoading','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','REJECTED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL','assignedRole','FrontGate'),JSON_OBJECT('code','PARKING_ASSIGNED','assignedRole','ParkingSecurity'),JSON_OBJECT('code','DISPATCH_QUEUED','assignedRole','Dispatch'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY(JSON_OBJECT('from','FRONT_GATE_ARRIVAL','to','PARKING_ASSIGNED','trigger','page.submitted','pageCode','pico:fg-front-gate'),JSON_OBJECT('from','PARKING_ASSIGNED','to','DISPATCH_QUEUED','trigger','page.submitted','pageCode','pico:fg-parking'),JSON_OBJECT('from','DISPATCH_QUEUED','to','EXIT_STAMPED','trigger','page.submitted','pageCode','pico:fg-dispatch')))),
  (1, 'M10.PackLoading',        1, 'PUBLISHED', JSON_OBJECT('code','M10.PackLoading','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY())),
  (1, 'M10.IntTransferOut',     1, 'PUBLISHED', JSON_OBJECT('code','M10.IntTransferOut','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY())),
  (1, 'M10.ThirdPartyOut',      1, 'PUBLISHED', JSON_OBJECT('code','M10.ThirdPartyOut','version',1,'entity','DispatchEvent','initialState','FRONT_GATE_ARRIVAL','terminalStates', JSON_ARRAY('EXIT_STAMPED','ABORTED'),'states', JSON_ARRAY(JSON_OBJECT('code','FRONT_GATE_ARRIVAL'),JSON_OBJECT('code','EXIT_STAMPED','terminal',true)),'transitions', JSON_ARRAY()));

-- ----- Notification templates ----------------------------------------
INSERT IGNORE INTO notification_template (tenant_id, code, channel, language, subject, body) VALUES
  (1, 'parking_assigned',     'sms',   'ta-IN', NULL, 'PICO POWER: உங்கள் டோக்கன் {{tokenNo}} – பார்க்கிங் வளாகம் {{bay}}. மொபைல் ON வைக்கவும்.'),
  (1, 'parking_assigned',     'sms',   'en-IN', NULL, 'PICO POWER: Token {{tokenNo}} – Park at bay {{bay}}. Keep phone on.'),
  (1, 'parking_overdue',      'push',  'en-IN', 'Parking SLA breach', 'Vehicle {{vehicleNo}} idle in parking > 15 min. Token {{tokenNo}}.'),
  (1, 'qc_overdue',           'push',  'en-IN', 'QC SLA breach',      'QC sample {{qcSampleId}} pending > 60 min for vehicle {{vehicleNo}}.'),
  (1, 'qc_failed',            'push',  'en-IN', 'QC fail',            'QC verdict FAIL for {{vehicleNo}} / {{itemCode}}. Vehicle held at parking bay {{bay}}.'),
  (1, 'weight_delta_approve', 'push',  'en-IN', 'Weight delta needs approval', '3rd weighment delta {{weighmentDeltaPct}}% > 0.2% on {{vehicleNo}}. Approve in mobile inbox.'),
  (1, 'mgr_overdue',          'push',  'en-IN', 'Manager approval overdue', 'Vehicle {{vehicleNo}} waiting > 15 min on manager approval. Owner notified.'),
  (1, 'unload_overdue',       'push',  'en-IN', 'Unloading SLA breach',     'Unloading at bay {{bay}} > 2h. Vehicle {{vehicleNo}}.'),
  (1, 'exit_cleared',         'sms',   'ta-IN', NULL, 'PICO POWER: கேட் பாஸ் {{gatePassNo}} – வெளியேற அனுமதி. நன்றி.'),
  (1, 'vehicle_rejected',     'push',  'en-IN', 'Vehicle rejected at front gate', '{{vehicleNo}} rejected: {{rejectReason}}. Driver advised to leave.');

-- ----- Form layouts (8 sections from TENANT-CONFIG.formLayouts) -------
INSERT IGNORE INTO tenant_form_layout (tenant_id, entity_code, code, label, device, body_json) VALUES
  (1, 'DispatchEvent', 'frontgate.scan',  'Front Gate – Scan & Verify', 'mobile-pwa',           JSON_OBJECT('sections', JSON_ARRAY(JSON_OBJECT('code','identity','fields', JSON_ARRAY('vehicleNo','driver.dlNumber','driver.primaryMobile','dispatch.flowVariant')),JSON_OBJECT('code','statutory','fields', JSON_ARRAY('vehicle.rcExpiry','vehicle.insuranceExpiry','vehicle.pucExpiry','vehicle.tankCleaningCertExpiry')),JSON_OBJECT('code','action','fields', JSON_ARRAY('actions.verify','actions.reject'))))),
  (1, 'DispatchEvent', 'parking.allocate','Parking – Bay Allocation',   'mobile-pwa',           JSON_OBJECT('sections', JSON_ARRAY(JSON_OBJECT('code','queue','fields', JSON_ARRAY('dispatch.tokenNo','dispatch.flowVariant','dispatch.priority')),JSON_OBJECT('code','assign','fields', JSON_ARRAY('parking.bayCode','actions.allocate'))))),
  (1, 'DispatchEvent', 'dispatch.queue',  'Dispatch – Live Queue',      'tablet+desktop',       JSON_OBJECT('sections', JSON_ARRAY(JSON_OBJECT('code','queue','fields', JSON_ARRAY('dispatch.tokenNo','dispatch.flowVariant','dispatch.state','dispatch.slaCountdown'))))),
  (1, 'DispatchEvent', 'qc.sample',       'QC – Sampling',              'mobile-pwa',           JSON_OBJECT('sections', JSON_ARRAY(JSON_OBJECT('code','sample','fields', JSON_ARRAY('dispatch.qcSampleId','dispatch.itemCode','dispatch.batchNo')),JSON_OBJECT('code','verdict','fields', JSON_ARRAY('dispatch.qcVerdict','dispatch.qcRemarks','actions.submit'))))),
  (1, 'DispatchEvent', 'wb.read',         'Weighbridge – 3 Reads',      'tablet+wb-console',    JSON_OBJECT('sections', JSON_ARRAY(JSON_OBJECT('code','reading','fields', JSON_ARRAY('dispatch.weighmentLoadedKg','dispatch.weighmentEmptyKg','dispatch.weighmentVerifyKg','dispatch.weighmentNetKg','dispatch.weighmentDeltaPct'))))),
  (1, 'DispatchEvent', 'unloading.bay',   'Unloading – Bay Control',    'mobile-pwa+tablet',    JSON_OBJECT('sections', JSON_ARRAY(JSON_OBJECT('code','tank','fields', JSON_ARRAY('tank.code','tank.currentOilGrade','tank.minHeadspaceKL')),JSON_OBJECT('code','progress','fields', JSON_ARRAY('dispatch.startedAt','dispatch.closedAt','actions.close'))))),
  (1, 'DispatchEvent', 'mgr.approval',    'Manager Approval Inbox',     'mobile',               JSON_OBJECT('sections', JSON_ARRAY(JSON_OBJECT('code','context','fields', JSON_ARRAY('dispatch.weighmentDeltaPct','dispatch.weighmentLoadedKg','dispatch.weighmentEmptyKg','dispatch.weighmentVerifyKg')),JSON_OBJECT('code','action','fields', JSON_ARRAY('dispatch.managerApprovalReason','actions.approve','actions.reject'))))),
  (1, 'DispatchEvent', 'owner.dashboard', 'Owner Day View',             'mobile',               JSON_OBJECT('sections', JSON_ARRAY(JSON_OBJECT('code','kpis','fields', JSON_ARRAY('kpi.vehiclesIn','kpi.vehiclesOut','kpi.avgTurnaroundMin','kpi.qcFailRate','kpi.mgrOverridesToday')))));

-- ----- Saved views (5 entries from TENANT-CONFIG.savedViews) ----------
INSERT IGNORE INTO tenant_list_view (tenant_id, page_slug, name, description, scope, is_default, body_json) VALUES
  (1, 'm10.dispatch.queue',           'Live gate queue',             'Vehicles in front-gate / parking / dispatch states', 'TENANT', 1, JSON_OBJECT('code','queue.live','entity','DispatchEvent','filter',JSON_OBJECT('expr','dispatch.state in [''FRONT_GATE_ARRIVAL'',''PARKING_ASSIGNED'',''DISPATCH_QUEUED'']'),'sort','-createdAt')),
  (1, 'm10.dispatch.mgr-inbox',       'Manager approval inbox',      'Pending manager decisions',                          'ROLE',   0, JSON_OBJECT('code','queue.mgrInbox','entity','DispatchEvent','filter',JSON_OBJECT('expr','dispatch.state == ''MANAGER_APPROVAL'''),'sort','-createdAt')),
  (1, 'm10.dispatch.sla-breached',    'SLA-breached',                'Instances past their SLA window',                    'TENANT', 0, JSON_OBJECT('code','queue.slaBreached','entity','DispatchEvent','filter',JSON_OBJECT('expr','dispatch.slaBreached == true'),'sort','-slaBreachedAt')),
  (1, 'm0.vehicles.expiring',         'Docs expiring < 30d',         'Vehicles with RC/Insurance expiring soon',           'TENANT', 0, JSON_OBJECT('code','expiringDocs','entity','Vehicle','filter',JSON_OBJECT('expr','days_between(today(), vehicle.rcExpiry) < 30'),'sort','vehicle.rcExpiry')),
  (1, 'm0.tanks.low-headspace',       'Tanks: low headspace',        'Tanks with min headspace < 10 KL',                   'TENANT', 0, JSON_OBJECT('code','tanksLowHeadspace','entity','TankLocation','filter',JSON_OBJECT('expr','tank.minHeadspaceKL < 10'),'sort','tank.code'));
