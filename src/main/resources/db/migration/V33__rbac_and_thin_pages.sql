-- V33: (a) Add RBAC columns + sensible defaults to entity_registry so the
-- GenericEntityController can refuse reads/writes by role.  (b) Stop the
-- 5 truly-plain LIST pages from being empty by injecting proper columns
-- + filters into their page_version.body_json.
--
-- Roles in scope (from `role` table):
--   1001 FrontGate            — gate entry / vehicle log only
--   1002 ParkingSecurity      — parking bay scan
--   1003 Dispatch             — dispatch officer
--   1004 QC                   — quality
--   1005 WeighmentOfficer     — weighbridge ops
--   1006 UnloadingSupervisor  — tank line + unloading
--   1007 Manager              — approvals / variance
--   1008 Plantowner           — full read; mutations on owner-level entities
--   1009 TenantAdmin          — superuser

-- ---------------------------------------------------------------------------
-- (a) Entity-registry RBAC scaffolding
-- ---------------------------------------------------------------------------
ALTER TABLE entity_registry
  ADD COLUMN IF NOT EXISTS read_role  VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS write_role VARCHAR(64) NULL,
  ADD COLUMN IF NOT EXISTS approver_role VARCHAR(64) NULL;

-- Defaults: everyone authenticated reads; only TenantAdmin writes unless
-- a specific entity grants wider write. Concrete grants below override.
UPDATE entity_registry SET read_role = '*', write_role = 'TenantAdmin'
  WHERE read_role IS NULL OR write_role IS NULL;

-- Operational entities — wider write access (matches PICO 09 Jun 2026 sheet)
UPDATE entity_registry SET write_role = 'FrontGate,Dispatch,Manager,TenantAdmin', approver_role='Manager'
  WHERE code IN ('GatePass','Vehicle','Driver');
UPDATE entity_registry SET write_role = 'ParkingSecurity,Dispatch,Manager,TenantAdmin'
  WHERE code IN ('ParkingBay');
UPDATE entity_registry SET write_role = 'WeighmentOfficer,Manager,TenantAdmin', approver_role='Manager'
  WHERE code IN ('Weighbridge');
UPDATE entity_registry SET write_role = 'Dispatch,UnloadingSupervisor,Manager,TenantAdmin', approver_role='Manager'
  WHERE code IN ('MaterialRequisition','GoodsReceiptNote','FGDispatch','ReturnNote');
UPDATE entity_registry SET write_role = 'QC,Manager,TenantAdmin', approver_role='Manager'
  WHERE code IN ('QualityTest','QcTestResult','InwardAudit');
UPDATE entity_registry SET write_role = 'Dispatch,Manager,TenantAdmin', approver_role='Manager'
  WHERE code IN ('PurchaseOrder','SalesOrder','PackingRun','ProductionOrder','JobCard');
UPDATE entity_registry SET write_role = 'Manager,TenantAdmin', approver_role='Plantowner'
  WHERE code IN ('Payment','SalesInvoice','PurchaseInvoice','PayrollRun');

-- ---------------------------------------------------------------------------
-- (b) Fix the 5 plain LIST pages
-- ---------------------------------------------------------------------------
-- pico:driver-master  — Drivers list with proper columns
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_OBJECT(
  'kind', 'list',
  'dataSource', JSON_OBJECT('entity','Driver','select',JSON_ARRAY('id','name','phone','licence_no','vehicle_id','status')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','id','label','#','sortable',true),
    JSON_OBJECT('field','name','label','Driver Name','sortable',true,'render','link','linkTo','page:pico:driver-detail?id={id}'),
    JSON_OBJECT('field','phone','label','Phone','sortable',true),
    JSON_OBJECT('field','licence_no','label','Licence #','sortable',true),
    JSON_OBJECT('field','vehicle_id','label','Default Vehicle','sortable',true),
    JSON_OBJECT('field','status','label','Status','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('ACTIVE','BLOCKED','INACTIVE')),
    JSON_OBJECT('field','_q','label','Search','type','text-contains','applyTo',JSON_ARRAY('name','phone','licence_no'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code = 'pico:driver-master';

-- pico:plant-transfer-line-list — line items list
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','PlantTransferLine','select',JSON_ARRAY('id','plant_transfer_id','item_code','qty','uom','batch_no')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','id','label','#','sortable',true),
    JSON_OBJECT('field','plant_transfer_id','label','Transfer #','sortable',true,'render','link','linkTo','page:pico:plant-transfer-list?id={plant_transfer_id}'),
    JSON_OBJECT('field','item_code','label','Item','sortable',true),
    JSON_OBJECT('field','qty','label','Qty','sortable',true,'render','number'),
    JSON_OBJECT('field','uom','label','UOM'),
    JSON_OBJECT('field','batch_no','label','Batch')
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','_q','label','Search','type','text-contains','applyTo',JSON_ARRAY('item_code','batch_no'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code = 'pico:plant-transfer-line-list';

-- pico:posh-committee
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','PoshCommittee','select',JSON_ARRAY('id','committee_name','formed_on','term_ends','members_count','status')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','id','label','#','sortable',true),
    JSON_OBJECT('field','committee_name','label','Committee','sortable',true),
    JSON_OBJECT('field','formed_on','label','Formed On','render','date','sortable',true),
    JSON_OBJECT('field','term_ends','label','Term Ends','render','date','sortable',true),
    JSON_OBJECT('field','members_count','label','Members','render','number'),
    JSON_OBJECT('field','status','label','Status','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('ACTIVE','DISSOLVED','PENDING_REFORM'))
  ),
  'pagination', JSON_OBJECT('pageSize',25)
)
WHERE pd.code = 'pico:posh-committee';

-- pico:role-master — system roles
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','Role','select',JSON_ARRAY('id','name')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','id','label','Role #','sortable',true),
    JSON_OBJECT('field','name','label','Role Name','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','_q','label','Search Role','type','text-contains','applyTo',JSON_ARRAY('name'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
WHERE pd.code = 'pico:role-master';

-- pico:department-master
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_OBJECT(
  'kind','list',
  'dataSource', JSON_OBJECT('entity','Department','select',JSON_ARRAY('id','departmentname','status','description','headEmployeeId')),
  'columns', JSON_ARRAY(
    JSON_OBJECT('field','id','label','#','sortable',true),
    JSON_OBJECT('field','departmentname','label','Department','sortable',true),
    JSON_OBJECT('field','headEmployeeId','label','Head','sortable',true),
    JSON_OBJECT('field','description','label','Description'),
    JSON_OBJECT('field','status','label','Status','sortable',true)
  ),
  'filters', JSON_ARRAY(
    JSON_OBJECT('field','status','label','Status','type','multi-select','options',JSON_ARRAY('ACTIVE','INACTIVE'))
  ),
  'pagination', JSON_OBJECT('pageSize',50)
)
WHERE pd.code = 'pico:department-master';
