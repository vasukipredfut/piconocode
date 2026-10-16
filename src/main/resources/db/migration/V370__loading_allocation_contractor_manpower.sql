-- FG Loading - Supervisor Allocation Sheet (aria:loading-allocation): two more fields the sheet captures --
--   1. Loading Contractor Name
--   2. No of Loading Manpower Count
-- They live in loading_allocation_detail (the page's own one-row-per-instance table, V99), filled by the generic
-- WorkflowProjectionListener through the page-scoped m0_workflow_projection row of each workflow that uses this page
-- (M10.FGLoading, M10.PackLoading). The page must send them as loading_contractor_name / loading_manpower_count
-- (camelCase loadingContractorName / loadingManpowerCount are accepted too).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE IF EXISTS loading_allocation_detail ADD COLUMN IF NOT EXISTS loading_contractor_name VARCHAR(128) NULL AFTER loading_bay;
ALTER TABLE IF EXISTS loading_allocation_detail ADD COLUMN IF NOT EXISTS loading_manpower_count INT NULL AFTER loading_contractor_name;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'LoadingAllocationDetail' b,'loading_contractor_name' c,'Loading Contractor Name' d,'STRING' e,1 f,1 g,1 h,0 i,'text' j UNION ALL
  SELECT -1,'LoadingAllocationDetail','loading_manpower_count','No of Loading Manpower Count','LONG',0,1,0,0,'number'
) x
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'LoadingAllocationDetail')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- project the two fields into the table for every workflow that has this page
UPDATE m0_workflow_projection
SET field_map_json = JSON_SET(field_map_json,
      '$.loading_contractor_name', JSON_ARRAY('loading_contractor_name', 'loadingContractorName'),
      '$.loading_manpower_count',  JSON_ARRAY('loading_manpower_count', 'loadingManpowerCount'))
WHERE target_table = 'loading_allocation_detail'
  AND workflow_code IN ('M10.FGLoading', 'M10.PackLoading');
