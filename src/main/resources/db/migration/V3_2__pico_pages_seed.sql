-- =====================================================================
-- M0 — Pico-Power 7 persona pages (Round 1F)
-- =====================================================================
-- Seeds the 7 persona screens for the M10 TankerInbound flow into
-- page_definition + page_version + page_workflow_binding. Each page is
-- DRAFT-then-PUBLISHED in one transaction; the body JSON conforms to the
-- v1 DESIGN.md schema (dataSources / streams / layout / primitives).
--
-- Field lists derive from the M10 personas at US-M10-06 (gate-pass),
-- US-M10-07 (parking-scan), US-M10-08 (dispatch entry), US-M10-09 (QC),
-- US-M10-12 (weighment), US-M10-14 (unloading), US-M10-15 (manager
-- approval). The /api/m10/* endpoints these reference are stubs until
-- Round 1H — that's intentional. The Renderer must degrade gracefully.
--
-- tenant_id is hardcoded to 1; the M0-tenant-context task (Round 1H) will
-- look this up from the JWT subject.
-- =====================================================================

-- Pico tenant marker. Reused as the FK target for every binding row.
SET @tenant := 1;

-- ---------------------------------------------------------------------
-- 1. pico:front-gate-create  (US-M10-06)
-- ---------------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (@tenant, 'pico:front-gate-create', 'Front-Gate Intake', 'Gate-pass creation flow for inbound tankers', 'M10', 'PUBLISHED');
SET @pid := LAST_INSERT_ID();
INSERT INTO page_version (page_id, version, status, body_json, published_at)
VALUES (@pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion','1.0.0','slug','pico:front-gate-create','title','Front-Gate Intake',
    'status','published','version',1,
    'roles', JSON_ARRAY('FrontGateSecurity'),
    'dataSources', JSON_ARRAY(
      JSON_OBJECT('id','vehicleMaster','kind','rest','method','GET','url','/api/m10/vehicles'),
      JSON_OBJECT('id','flowVariants','kind','rest','method','GET','url','/api/m10/flow-variants')
    ),
    'streams', JSON_ARRAY(),
    'layout', JSON_OBJECT('type','Grid','id','root','props', JSON_OBJECT('columns',12,'gap',16),
      'children', JSON_ARRAY(
        JSON_OBJECT('type','Section','id','sec-intake','props', JSON_OBJECT('title','Vehicle & Driver','span',12),
          'children', JSON_ARRAY(
            JSON_OBJECT('type','Form','id','form-gp','props', JSON_OBJECT('submitLabel','Create Gate Pass'),
              'bindings', JSON_OBJECT(
                'fields', JSON_ARRAY(
                  JSON_OBJECT('name','vehicleNo','label','Vehicle No.','type','text','required',true),
                  JSON_OBJECT('name','flowVariant','label','Flow Variant','type','select','optionsFrom','data:flowVariants'),
                  JSON_OBJECT('name','driverPhone','label','Driver Phone','type','tel','required',true)
                )
              )
            ),
            JSON_OBJECT('type','OtpVerify','id','otp','props', JSON_OBJECT('phoneField','driverPhone','purpose','DRIVER_VERIFY')),
            JSON_OBJECT('type','WorkflowActionButton','id','btn-create','props', JSON_OBJECT('action','m10.gate-pass.create','label','Create Gate Pass','variant','primary'))
          )
        )
      )
    )
  ),
  CURRENT_TIMESTAMP);
UPDATE page_definition SET current_version_id = LAST_INSERT_ID() WHERE id = @pid;
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:front-gate-create', 'M10.TankerInbound', 'GATE_PASS_CREATE', 'INPUT');

-- ---------------------------------------------------------------------
-- 2. pico:parking-scan  (US-M10-07)
-- ---------------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (@tenant, 'pico:parking-scan', 'Parking-Bay Scan', 'Scan GP QR + assign bay', 'M10', 'PUBLISHED');
SET @pid := LAST_INSERT_ID();
INSERT INTO page_version (page_id, version, status, body_json, published_at)
VALUES (@pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion','1.0.0','slug','pico:parking-scan','title','Parking-Bay Scan',
    'status','published','version',1,
    'roles', JSON_ARRAY('ParkingSecurity'),
    'dataSources', JSON_ARRAY(
      JSON_OBJECT('id','availableBays','kind','rest','method','GET','url','/api/m10/parking-bays?status=FREE')
    ),
    'streams', JSON_ARRAY(),
    'layout', JSON_OBJECT('type','Grid','id','root','props', JSON_OBJECT('columns',12,'gap',16),
      'children', JSON_ARRAY(
        JSON_OBJECT('type','QrScan','id','scan','props', JSON_OBJECT('targetField','gpQr','manualFallback',true)),
        JSON_OBJECT('type','FormField','id','bay','props', JSON_OBJECT('name','bayCode','label','Bay','type','select','optionsFrom','data:availableBays')),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-assign','props', JSON_OBJECT('action','m10.parking.assign','label','Assign Bay'))
      )
    )
  ),
  CURRENT_TIMESTAMP);
UPDATE page_definition SET current_version_id = LAST_INSERT_ID() WHERE id = @pid;
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:parking-scan', 'M10.TankerInbound', 'PARKING_ASSIGNED', 'INPUT');

-- ---------------------------------------------------------------------
-- 3. pico:dispatch-vehicle-entry  (US-M10-08)
-- ---------------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (@tenant, 'pico:dispatch-vehicle-entry', 'Dispatch Vehicle Entry', 'Dispatch-office GP intake', 'M10', 'PUBLISHED');
SET @pid := LAST_INSERT_ID();
INSERT INTO page_version (page_id, version, status, body_json, published_at)
VALUES (@pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion','1.0.0','slug','pico:dispatch-vehicle-entry','title','Dispatch Vehicle Entry',
    'status','published','version',1,
    'roles', JSON_ARRAY('Dispatch'),
    'dataSources', JSON_ARRAY(
      JSON_OBJECT('id','queue','kind','rest','method','GET','url','/api/m10/queues/dispatch')
    ),
    'streams', JSON_ARRAY(),
    'layout', JSON_OBJECT('type','Grid','id','root','props', JSON_OBJECT('columns',12,'gap',16),
      'children', JSON_ARRAY(
        JSON_OBJECT('type','WorkflowQueueList','id','q','props',
          JSON_OBJECT('persona','dispatch','pageSize',10,
            'cardFields', JSON_ARRAY('vehicleNo','flowVariant','arrivedAt'))),
        JSON_OBJECT('type','Form','id','form-invoice','props', JSON_OBJECT('submitLabel','Capture Invoice'),
          'bindings', JSON_OBJECT(
            'fields', JSON_ARRAY(
              JSON_OBJECT('name','invoiceNo','label','Invoice No.','type','text','required',true),
              JSON_OBJECT('name','invoiceQty','label','Invoice Qty','type','number','min',0)
            )
          )
        ),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-queue','props', JSON_OBJECT('action','m10.dispatch.queue','label','Queue for QC'))
      )
    )
  ),
  CURRENT_TIMESTAMP);
UPDATE page_definition SET current_version_id = LAST_INSERT_ID() WHERE id = @pid;
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:dispatch-vehicle-entry', 'M10.TankerInbound', 'DISPATCH_QUEUED', 'INPUT');

-- ---------------------------------------------------------------------
-- 4. pico:qc-approval  (US-M10-09)
-- ---------------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (@tenant, 'pico:qc-approval', 'QC Approval', 'QC pass/fail on inbound tanker', 'M10', 'PUBLISHED');
SET @pid := LAST_INSERT_ID();
INSERT INTO page_version (page_id, version, status, body_json, published_at)
VALUES (@pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion','1.0.0','slug','pico:qc-approval','title','QC Approval',
    'status','published','version',1,
    'roles', JSON_ARRAY('QCOfficer'),
    'dataSources', JSON_ARRAY(
      JSON_OBJECT('id','queue','kind','rest','method','GET','url','/api/m10/queues/qc')
    ),
    'streams', JSON_ARRAY(),
    'layout', JSON_OBJECT('type','Grid','id','root','props', JSON_OBJECT('columns',12,'gap',16),
      'children', JSON_ARRAY(
        JSON_OBJECT('type','WorkflowQueueList','id','q','props',
          JSON_OBJECT('persona','qc','pageSize',10,
            'cardFields', JSON_ARRAY('vehicleNo','flowVariant','arrivedAt'))),
        JSON_OBJECT('type','Form','id','form-qc','props', JSON_OBJECT('submitLabel','Save'),
          'bindings', JSON_OBJECT(
            'fields', JSON_ARRAY(
              JSON_OBJECT('name','qcResult','label','Result','type','select',
                'options', JSON_ARRAY('PASS','REJECT')),
              JSON_OBJECT('name','rejectReason','label','Reject reason','type','text','minLen',10)
            )
          )
        ),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-pass','props', JSON_OBJECT('action','m10.qc.pass','label','QC Pass','variant','primary')),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-fail','props', JSON_OBJECT('action','m10.qc.reject','label','QC Reject','variant','danger','confirmRequired',true))
      )
    )
  ),
  CURRENT_TIMESTAMP);
UPDATE page_definition SET current_version_id = LAST_INSERT_ID() WHERE id = @pid;
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:qc-approval', 'M10.TankerInbound', 'QC_APPROVAL', 'INPUT');

-- ---------------------------------------------------------------------
-- 5. pico:weighment-capture  (US-M10-12)
-- ---------------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (@tenant, 'pico:weighment-capture', 'Weighment Capture', 'Live weighbridge capture with motion-settled gate', 'M10', 'PUBLISHED');
SET @pid := LAST_INSERT_ID();
INSERT INTO page_version (page_id, version, status, body_json, published_at)
VALUES (@pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion','1.0.0','slug','pico:weighment-capture','title','Weighment Capture',
    'status','published','version',1,
    'roles', JSON_ARRAY('WeighmentOfficer'),
    'dataSources', JSON_ARRAY(),
    'streams', JSON_ARRAY(
      JSON_OBJECT('id','wb1','kind','m9','workerId','wb-1','windowSeconds',60)
    ),
    'layout', JSON_OBJECT('type','Grid','id','root','props', JSON_OBJECT('columns',12,'gap',16),
      'children', JSON_ARRAY(
        JSON_OBJECT('type','Section','id','sec-live','props', JSON_OBJECT('title','Live Weight','span',12),
          'children', JSON_ARRAY(
            JSON_OBJECT('type','WeighbridgeLive','id','wb','props',
              JSON_OBJECT('workerId','wb-1','targetField','grossKg','motionSettledWindowMs',1500,'unit','kg'))
          )
        ),
        JSON_OBJECT('type','Form','id','form-w','props', JSON_OBJECT('submitLabel','Save Weighment'),
          'bindings', JSON_OBJECT(
            'fields', JSON_ARRAY(
              JSON_OBJECT('name','grossKg','label','Gross (kg)','type','number','readonly',true),
              JSON_OBJECT('name','notes','label','Notes','type','text')
            )
          )
        ),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-save','props', JSON_OBJECT('action','m10.weighment.capture','label','Save Weighment'))
      )
    )
  ),
  CURRENT_TIMESTAMP);
UPDATE page_definition SET current_version_id = LAST_INSERT_ID() WHERE id = @pid;
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:weighment-capture', 'M10.TankerInbound', 'WB_LOADED_WEIGHED', 'INPUT');
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:weighment-capture', 'M10.TankerInbound', 'WB_EMPTY_WEIGHED', 'INPUT');

-- ---------------------------------------------------------------------
-- 6. pico:supervisor-unloading  (US-M10-14)
-- ---------------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (@tenant, 'pico:supervisor-unloading', 'Unloading Supervisor', 'Tank assignment, unload start/close, GRN capture', 'M10', 'PUBLISHED');
SET @pid := LAST_INSERT_ID();
INSERT INTO page_version (page_id, version, status, body_json, published_at)
VALUES (@pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion','1.0.0','slug','pico:supervisor-unloading','title','Unloading Supervisor',
    'status','published','version',1,
    'roles', JSON_ARRAY('UnloadingSupervisor'),
    'dataSources', JSON_ARRAY(
      JSON_OBJECT('id','tanks','kind','rest','method','GET','url','/api/m10/tanks?status=AVAILABLE')
    ),
    'streams', JSON_ARRAY(),
    'layout', JSON_OBJECT('type','Grid','id','root','props', JSON_OBJECT('columns',12,'gap',16),
      'children', JSON_ARRAY(
        JSON_OBJECT('type','FormField','id','tank','props', JSON_OBJECT('name','tankCode','label','Tank','type','select','optionsFrom','data:tanks')),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-start','props', JSON_OBJECT('action','m10.unloading.start','label','Start Unload')),
        JSON_OBJECT('type','Form','id','form-grn','props', JSON_OBJECT('submitLabel','Capture GRN'),
          'bindings', JSON_OBJECT(
            'fields', JSON_ARRAY(
              JSON_OBJECT('name','unloadingQty','label','Unloading Qty','type','number','min',0),
              JSON_OBJECT('name','sealOk','label','Seal intact?','type','checkbox')
            )
          )
        ),
        JSON_OBJECT('type','SignaturePad','id','sig','props', JSON_OBJECT('targetField','supervisorSig','width',400,'height',150)),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-close','props', JSON_OBJECT('action','m10.unloading.close','label','Close Unload'))
      )
    )
  ),
  CURRENT_TIMESTAMP);
UPDATE page_definition SET current_version_id = LAST_INSERT_ID() WHERE id = @pid;
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:supervisor-unloading', 'M10.TankerInbound', 'UNLOADING_IN_PROGRESS', 'INPUT');
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:supervisor-unloading', 'M10.TankerInbound', 'UNLOADING_CLOSED', 'INPUT');

-- ---------------------------------------------------------------------
-- 7. pico:manager-approval-inbox  (US-M10-15)
-- ---------------------------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
VALUES (@tenant, 'pico:manager-approval-inbox', 'Manager Approval Inbox', 'Out-of-tolerance delta approvals', 'M10', 'PUBLISHED');
SET @pid := LAST_INSERT_ID();
INSERT INTO page_version (page_id, version, status, body_json, published_at)
VALUES (@pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'schemaVersion','1.0.0','slug','pico:manager-approval-inbox','title','Manager Approval Inbox',
    'status','published','version',1,
    'roles', JSON_ARRAY('Manager','Plantowner'),
    'dataSources', JSON_ARRAY(
      JSON_OBJECT('id','queue','kind','rest','method','GET','url','/api/m10/queues/manager-approvals')
    ),
    'streams', JSON_ARRAY(),
    'layout', JSON_OBJECT('type','Grid','id','root','props', JSON_OBJECT('columns',12,'gap',16),
      'children', JSON_ARRAY(
        JSON_OBJECT('type','WorkflowQueueList','id','q','props',
          JSON_OBJECT('persona','manager-approvals','pageSize',10,
            'cardFields', JSON_ARRAY('vehicleNo','deltaPct','vendor','flagged'))),
        JSON_OBJECT('type','Form','id','form-mgr','props', JSON_OBJECT('submitLabel','Save Decision'),
          'bindings', JSON_OBJECT(
            'fields', JSON_ARRAY(
              JSON_OBJECT('name','decision','label','Decision','type','select',
                'options', JSON_ARRAY('APPROVE','REJECT')),
              JSON_OBJECT('name','remarks','label','Remarks','type','text','minLen',5)
            )
          )
        ),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-approve','props', JSON_OBJECT('action','m10.manager.approve','label','Approve','variant','primary')),
        JSON_OBJECT('type','WorkflowActionButton','id','btn-reject','props', JSON_OBJECT('action','m10.manager.reject','label','Reject','variant','danger','confirmRequired',true))
      )
    )
  ),
  CURRENT_TIMESTAMP);
UPDATE page_definition SET current_version_id = LAST_INSERT_ID() WHERE id = @pid;
INSERT INTO page_workflow_binding (tenant_id, page_code, workflow_code, step_code, role)
VALUES (@tenant, 'pico:manager-approval-inbox', 'M10.TankerInbound', 'MANAGER_APPROVAL', 'INPUT');
