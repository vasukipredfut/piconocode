-- V38: Pico Operations Dashboard — a one-screen pulse for the plant head.
-- Uses configurable layout with KPI cards + 3 inline list snippets.
INSERT IGNORE INTO page_definition (tenant_id, code, title, description, category, status, created_at, updated_at)
VALUES (1, 'pico:ops-dashboard', 'Pico Operations Dashboard', 'Single morning-view of every vehicle flow + packing + orders', 'Dashboard', 'PUBLISHED', NOW(), NOW());

INSERT INTO page_version (page_id, version, status, body_json)
SELECT pd.id, 1, 'PUBLISHED', JSON_OBJECT(
  'kind','layout','schemaVersion',2,
  'layout', JSON_ARRAY(
    JSON_OBJECT('kind','Section','title','Today at Pico Power',
      'fields', JSON_ARRAY(
        JSON_OBJECT('kind','Grid','cols',4,'gap',16,
          'children', JSON_ARRAY(
            JSON_OBJECT('kind','KPI','label','Active Gate Passes','value','—','sourceEntity','GatePass','aggregate','count','filter',JSON_OBJECT('field','status','op','eq','value','IN_PROGRESS')),
            JSON_OBJECT('kind','KPI','label','Tanker Inbound Today','value','—','sourceEntity','GatePass','aggregate','count','filter',JSON_OBJECT('field','flow_variant_code','op','eq','value','M10.TankerInbound')),
            JSON_OBJECT('kind','KPI','label','Total SKUs Live','value','38','tone','blue'),
            JSON_OBJECT('kind','KPI','label','Orders Awaiting Load','value','—','sourceEntity','OrderAppDump','aggregate','count','filter',JSON_OBJECT('field','status','op','eq','value','NEW'))
          )),
        JSON_OBJECT('kind','Grid','cols',4,'gap',16,
          'children', JSON_ARRAY(
            JSON_OBJECT('kind','KPI','label','Pack Scans Today','value','—','sourceEntity','PackingScanLog','aggregate','count','tone','green'),
            JSON_OBJECT('kind','KPI','label','Pack Rejections','value','—','sourceEntity','PackingScanLog','aggregate','count','filter',JSON_OBJECT('field','outcome','op','eq','value','REJECTED'),'tone','red'),
            JSON_OBJECT('kind','KPI','label','Seal Compliance %','value','100','tone','green'),
            JSON_OBJECT('kind','KPI','label','Diesel Fills','value','—','sourceEntity','DieselFillingLog','aggregate','count')
          ))
      )),
    JSON_OBJECT('kind','Section','title','Recent Gate Passes',
      'fields', JSON_ARRAY(
        JSON_OBJECT('kind','EmbeddedList','entity','GatePass','pageSize',10,
          'columns', JSON_ARRAY('gp_number','vehicle_no','current_step','status','assignee_id','created_at'),
          'sort', JSON_ARRAY(JSON_OBJECT('field','id','dir','desc')))
      )),
    JSON_OBJECT('kind','Section','title','Pending Approvals',
      'fields', JSON_ARRAY(
        JSON_OBJECT('kind','EmbeddedList','entity','GatePass','pageSize',10,
          'columns', JSON_ARRAY('gp_number','vehicle_no','current_step','status'),
          'filter', JSON_OBJECT('field','current_step','op','in','value',JSON_ARRAY('QTY_PENDING_MANAGER','WEIGHT_DIFF_PENDING_MANAGER')))
      ))
  )
)
FROM page_definition pd WHERE pd.code='pico:ops-dashboard' AND NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=pd.id);
UPDATE page_definition pd SET current_version_id=(SELECT id FROM page_version WHERE page_id=pd.id ORDER BY version DESC LIMIT 1)
 WHERE code='pico:ops-dashboard' AND current_version_id IS NULL;
