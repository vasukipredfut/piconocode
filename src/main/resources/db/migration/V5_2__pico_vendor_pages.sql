-- =====================================================================
-- M0 — Pico vendor-list + vendor-detail (Schema V2 demo) (Round 1G)
-- =====================================================================
-- Two new page_definition rows in Schema V2 format. ListView + DetailView
-- primitives on the frontend dispatch on body.kind == 'list' / 'detail'.
--
-- Joined entities: m0_vendor (canonical Vendor), city, state.
-- Cascading filters: state -> city (the city filter's optionsSource
-- depends on the state filter's value via the new `page:` binding prefix).
--
-- Idempotent on (tenant_id, code).
-- =====================================================================

SET @tenant := 1;

-- ----- pico:vendor-list ---------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT @tenant, 'pico:vendor-list', 'Vendors', 'Vendor master list (Schema V2 demo)', 'M3', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=@tenant AND code='pico:vendor-list');
SET @pid := (SELECT id FROM page_definition WHERE tenant_id=@tenant AND code='pico:vendor-list');

INSERT INTO page_version (page_id, version, status, body_json, published_at)
SELECT @pid, 1, 'PUBLISHED',
  JSON_OBJECT(
    'kind','list',
    'schemaVersion',2,
    'slug','pico:vendor-list',
    'title','Vendors',
    'dataSource', JSON_OBJECT(
      'type','entity',
      'entity','Vendor',
      'joins', JSON_ARRAY(
        JSON_OBJECT('entity','City','as','city','on','m0_vendor.city_id = city.id',
                    'fields', JSON_ARRAY('city.name as cityName','city.stateid as cityStateId')),
        JSON_OBJECT('entity','State','as','state','on','m0_vendor.state_id = state.id',
                    'fields', JSON_ARRAY('state.name as stateName'))
      ),
      'filter', JSON_OBJECT('and', JSON_ARRAY(
        JSON_OBJECT('field','tenant_id','op','eq','value','ctx:tenantId')
      )),
      'select', JSON_ARRAY('id','name','gstin','cityName','stateName','is_msme','status')
    ),
    'columns', JSON_ARRAY(
      JSON_OBJECT('field','name','label','Vendor','render','link','linkTo','page:pico:vendor-detail?id={id}','sticky','left','sortable',TRUE),
      JSON_OBJECT('field','gstin','label','GSTIN','render','text'),
      JSON_OBJECT('field','cityName','label','City','render','text','sortable',TRUE),
      JSON_OBJECT('field','stateName','label','State','render','text'),
      JSON_OBJECT('field','is_msme','label','MSME','render','badge'),
      JSON_OBJECT('field','status','label','Status','render','badge')
    ),
    'filters', JSON_ARRAY(
      JSON_OBJECT('field','stateName','label','State','type','multi-select',
                  'optionsSource', JSON_OBJECT('type','entity','entity','State',
                                               'select', JSON_ARRAY('name as label','name as value'))),
      JSON_OBJECT('field','cityName','label','City','type','multi-select',
                  'optionsSource', JSON_OBJECT('type','entity','entity','City',
                                               'filter', JSON_OBJECT('field','state','op','in','value','page:stateName'),
                                               'select', JSON_ARRAY('name as label','name as value')),
                  'dependsOn', JSON_ARRAY('stateName')),
      JSON_OBJECT('field','_q','label','Search','type','text-contains','applyTo', JSON_ARRAY('name','gstin')),
      JSON_OBJECT('field','is_msme','label','MSME only','type','bool-toggle')
    ),
    'sort', JSON_OBJECT('default', JSON_ARRAY(JSON_OBJECT('field','name','dir','asc')),
                        'allow', JSON_ARRAY('name','cityName','status')),
    'pagination', JSON_OBJECT('kind','server','pageSize',25,'options', JSON_ARRAY(10,25,50,100)),
    'savedViews', JSON_OBJECT('enabled',TRUE,'scopes', JSON_ARRAY('TENANT','USER')),
    'rowActions', JSON_ARRAY(
      JSON_OBJECT('action','page.open','label','Open','icon','view','page','pico:vendor-detail','params', JSON_OBJECT('id','row.id'))
    ),
    'bulkActions', JSON_ARRAY(
      JSON_OBJECT('action','export.csv','label','Export CSV')
    ),
    'header', JSON_OBJECT('title','Vendors',
                          'primaryAction', JSON_OBJECT('label','Add Vendor','action','page.open','page','pico:vendor-detail')),
    'responsive', JSON_OBJECT('mobile', JSON_OBJECT('renderAs','cards','cardFields', JSON_ARRAY('name','cityName','status')))
  ),
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=@pid AND version=1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id=@pid AND version=1)
WHERE id=@pid AND (current_version_id IS NULL OR current_version_id != (SELECT id FROM page_version WHERE page_id=@pid AND version=1));

-- ----- pico:vendor-detail -------------------------------------------------
INSERT INTO page_definition (tenant_id, code, title, description, category, status)
SELECT @tenant, 'pico:vendor-detail', 'Vendor Detail', 'Vendor detail with tabs (Schema V2 demo)', 'M3', 'PUBLISHED'
WHERE NOT EXISTS (SELECT 1 FROM page_definition WHERE tenant_id=@tenant AND code='pico:vendor-detail');
SET @pid2 := (SELECT id FROM page_definition WHERE tenant_id=@tenant AND code='pico:vendor-detail');

INSERT INTO page_version (page_id, version, status, body_json, published_at)
SELECT @pid2, 1, 'PUBLISHED',
  JSON_OBJECT(
    'kind','detail',
    'schemaVersion',2,
    'slug','pico:vendor-detail',
    'title','Vendor Detail',
    'dataSource', JSON_OBJECT(
      'type','entity','entity','Vendor',
      'joins', JSON_ARRAY(
        JSON_OBJECT('entity','City','as','city','on','m0_vendor.city_id = city.id','fields', JSON_ARRAY('city.name as cityName')),
        JSON_OBJECT('entity','State','as','state','on','m0_vendor.state_id = state.id','fields', JSON_ARRAY('state.name as stateName'))
      ),
      'filter', JSON_OBJECT('and', JSON_ARRAY(
        JSON_OBJECT('field','id','op','eq','value','route:id'),
        JSON_OBJECT('field','tenant_id','op','eq','value','ctx:tenantId')
      )),
      'select', JSON_ARRAY('id','name','gstin','cityName','stateName','is_msme','status')
    ),
    'header', JSON_OBJECT('title','Vendor: {{row.name}}','subtitle','{{row.cityName}}, {{row.stateName}}'),
    'tabs', JSON_ARRAY(
      JSON_OBJECT('id','summary','label','Summary',
        'sections', JSON_ARRAY(
          JSON_OBJECT('id','sec-basic','title','Basic Info',
            'fields', JSON_ARRAY(
              JSON_OBJECT('field','name','label','Vendor Name','type','text','required',TRUE),
              JSON_OBJECT('field','gstin','label','GSTIN','type','text'),
              JSON_OBJECT('field','status','label','Status','type','select','options', JSON_ARRAY('ACTIVE','BLOCKED','PENDING'))
            )
          )
        )
      ),
      JSON_OBJECT('id','classification','label','Classification',
        'sections', JSON_ARRAY(
          JSON_OBJECT('id','sec-msme','title','MSME / Compliance',
            'fields', JSON_ARRAY(
              JSON_OBJECT('field','is_msme','label','MSME registered?','type','bool-toggle')
            )
          )
        )
      )
    ),
    'actionToolbar', JSON_ARRAY(
      JSON_OBJECT('action','save','label','Save','variant','primary'),
      JSON_OBJECT('action','save.and.close','label','Save & Close','variant','secondary'),
      JSON_OBJECT('action','delete','label','Delete','variant','danger','requiresConfirm',TRUE),
      JSON_OBJECT('action','page.open','label','Back to list','page','pico:vendor-list','variant','tertiary')
    )
  ),
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM page_version WHERE page_id=@pid2 AND version=1);

UPDATE page_definition SET current_version_id = (SELECT id FROM page_version WHERE page_id=@pid2 AND version=1)
WHERE id=@pid2 AND (current_version_id IS NULL OR current_version_id != (SELECT id FROM page_version WHERE page_id=@pid2 AND version=1));
