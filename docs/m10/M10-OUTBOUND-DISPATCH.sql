-- ============================================================================
-- M10 OUTBOUND DISPATCH — one runnable, idempotent substrate script
-- ============================================================================
-- Supports the whole outbound loading process-flow (Tanker-Outbound / Packing /
-- FG variants): Front Gate -> Parking In -> Dispatch Entry -> QC Verify ->
-- Parking Out -> First Weight -> Loading -> Bay Dashboard -> Loading Progress.
--
-- Nothing here is code. It is entity FIELDS, entity_field_registry rows,
-- page_version bodies, and a workflow projection — all DATA. The three
-- workflows (M10.TankerOutbound / M10.PackLoading / M10.FGLoading), the
-- gate_pass entity, and ~30 pages already exist; this fills the gaps.
--
-- Safe to re-run: every ALTER uses IF NOT EXISTS, every INSERT is guarded by
-- NOT EXISTS, every UPDATE is deterministic.
--
-- >>> AFTER RUNNING against a live instance, evict the page cache so the new
-- >>> page bodies are served (the API caches published bodies in Redis 1h):
-- >>>     redis-cli --scan --pattern 'm0:page:*' | xargs redis-cli DEL
-- >>> A fresh DB has no cache entry, so this is only for running instances.
--
-- This is the consolidated view of migrations V127, V129, V130, V131. In the
-- product these ship as those Flyway files; this single script is for the team
-- to read/run/learn from.
-- ============================================================================


-- ----------------------------------------------------------------------------
-- 1) DISPATCH-ENTRY / QC / WEIGHT / LOADING FIELDS on gate_pass  (was V129)
--    Variant-specific fields use visible_when so one form serves all 3 variants.
-- ----------------------------------------------------------------------------
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loading_type          VARCHAR(32)   NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS broker_name           VARCHAR(128)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS invoice_no            VARCHAR(64)   NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS invoice_qty           DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS oil_rm_item           VARCHAR(128)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS tanker_no             VARCHAR(32)   NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS vehicle_category      VARCHAR(32)   NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS transport_name        VARCHAR(128)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loading_supervisor    VARCHAR(128)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loading_tank_location VARCHAR(64)   NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS supervisor_name       VARCHAR(128)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS checking_officer      VARCHAR(128)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS qc_status             VARCHAR(16)   NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS qc_reject_reason      VARCHAR(255)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS weight_officer        VARCHAR(128)  NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS weight_entry_at       TIMESTAMP     NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS route_id              VARCHAR(64)   NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS loading_bay           VARCHAR(16)   NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS processed_qty         DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS remaining_qty         DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS dispatch_qty          DECIMAL(12,3) NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS parking_in_at         TIMESTAMP     NULL;
ALTER TABLE gate_pass ADD COLUMN IF NOT EXISTS parking_out_at        TIMESTAMP     NULL;

-- Register them so Page Designer, generic CRUD, filters, and column
-- auto-derive all see them. visible_when (SpEL over the form bag) is what makes
-- one dispatch-entry / QC form serve all three variants — DATA, not an if().
--   #form.flow_variant_code == 'M10.TankerOutbound'  -> Tanker-only fields
--   #form.flow_variant_code == 'M10.PackLoading'      -> Packing-only fields
--   #form.qc_status == 'REJECTED'                     -> reject-reason only on reject
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable, visible_when)
  SELECT * FROM (SELECT
    'GatePass' e, 'broker_name' f, 'Broker' l, 'STRING' d, 'text' w, 0 r, 1 fi, 1 so, 1 se,
    '#form.flow_variant_code == ''M10.TankerOutbound''' vw) t
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='GatePass' AND field_code='broker_name');
-- ... (repeat per field; broker/invoice/invoice_qty/oil_rm/tanker_no => Tanker,
--      loading_supervisor => Packing, qc_reject_reason => qc_status REJECTED,
--      the rest => all variants, visible_when NULL). See V129 for the full set.


-- ----------------------------------------------------------------------------
-- 2) ORDER-LINE FIELDS for the loading order sheet  (was V130)
-- ----------------------------------------------------------------------------
ALTER TABLE m5_sales_order_line ADD COLUMN IF NOT EXISTS route_id    VARCHAR(64) NULL;
ALTER TABLE m5_sales_order_line ADD COLUMN IF NOT EXISTS sku_barcode VARCHAR(64) NULL;
ALTER TABLE m5_sales_order_line ADD COLUMN IF NOT EXISTS offer       VARCHAR(64) NULL;

INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'SalesOrderLine','route_id','Route ID','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrderLine' AND field_code='route_id');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, widget_kind, required, filterable, sortable, searchable)
  SELECT 'SalesOrderLine','offer','Offer','STRING','text',0,1,1,1
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='SalesOrderLine' AND field_code='offer');
-- (sku_barcode + item_code/item_name registered the same way — see V130.)


-- ----------------------------------------------------------------------------
-- 3) AUTHOR A PAGE BODY — the loading order sheet  (was V131)
--    A layout with a ListView (route_id filter + editable Offer column) and a
--    Start-Loading WorkflowActionButton. Update ALL published versions, because
--    the GET serves status='PUBLISHED' and a page may have >1 published row.
-- ----------------------------------------------------------------------------
UPDATE page_version pv
SET pv.body_json = JSON_OBJECT(
  'schemaVersion','1.0.0','slug','pico:loading-order-sheet','title','Loading — Order Sheet',
  'status','published','version',1,
  'roles', JSON_ARRAY('Dispatch','UnloadingSupervisor','TenantAdmin'),
  'dataSources', JSON_ARRAY(), 'streams', JSON_ARRAY(),
  'layout', JSON_OBJECT('type','Grid','id','root','props',JSON_OBJECT('columns',12,'gap',16),
    'children', JSON_ARRAY(
      JSON_OBJECT('type','Section','id','sec-orders','props',JSON_OBJECT('title','Order Details','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','ListView','id','order-lines','props',JSON_OBJECT(
            'dataSource', JSON_OBJECT('type','entity','entity','SalesOrderLine'),
            'filters', JSON_ARRAY(JSON_OBJECT('field','route_id','label','Route ID','type','text-contains','applyTo',JSON_ARRAY('route_id'))),
            'columns', JSON_ARRAY(
              JSON_OBJECT('field','so_id','label','Order ID','sortable',true),
              JSON_OBJECT('field','item_name','label','SKU Name'),
              JSON_OBJECT('field','item_code','label','SKU Code'),
              JSON_OBJECT('field','sku_barcode','label','SKU Barcode'),
              JSON_OBJECT('field','qty_ordered','label','Qty','render','number'),
              JSON_OBJECT('field','offer','label','Offer')),
            'pageSize', 25)))),
      JSON_OBJECT('type','Section','id','sec-start','props',JSON_OBJECT('title','Start Loading','span',12),
        'children', JSON_ARRAY(
          JSON_OBJECT('type','WorkflowActionButton','id','btn-start','props',JSON_OBJECT(
            'label','Start Loading','action','workflow.transition',
            'targetState','LOADING_IN_PROGRESS','trigger','page.submitted','color','primary'))))
    )))
WHERE pv.page_id = (SELECT id FROM page_definition WHERE code='pico:loading-order-sheet')
  AND pv.status = 'PUBLISHED';


-- ----------------------------------------------------------------------------
-- 4) WORKFLOW PROJECTION — land form data on gate_pass  (part of V129)
--    On every transition the WorkflowProjectionListener maps submitted-form
--    keys to gate_pass columns. Extend the field-map so the new dispatch/QC/
--    loading fields persist. (Full JSON in V129; shape shown here.)
-- ----------------------------------------------------------------------------
-- UPDATE m0_workflow_projection
--   SET field_map_json = '{"vehicle_no":["vehicleNo"],"broker_name":["brokerName","broker_name"], ...}'
--   WHERE tenant_id=1 AND workflow_code IN ('M10.TankerOutbound','M10.PackLoading','M10.FGLoading');

-- ============================================================================
-- END. See docs/m10/PLAYBOOK.md for the copy-paste recipes + the API equivalents.
-- ============================================================================
