-- loading_progress_detail_bay: child table of loading_progress_detail
-- (V94/V177) -- one row per bay/SKU entry within a single loading
-- transaction. A transaction can span several of these (Bay 1 = 100 units
-- of SKU A, Bay 2 = 200 units of SKU B, Bay 3 = 500 units of SKU C), which
-- is why this can't live on the 1-row-per-instance parent.
--
-- route_id/sku_code/sku_desc identify which SKU line item (from this
-- page's Order/SKU grid, backed by RouteSkuSummary/OrderDispatchDetail --
-- see V75/V76/V169) this bay entry is for. bay_name is a plain label
-- (whatever the operator enters/picks for that bay) -- not a foreign key
-- to m10_loading_point/LoadingPoint; that master turned out to be a
-- different, unrelated M10 concept from what "Bay" means on this page.
--
-- quantity is set on Start Bay (the amount planned for that bay entry).
-- processed_quantity is set on Bay Complete (the amount actually done) and
-- stays NULL while status is IN_PROGRESS.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS loading_progress_detail_bay (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  loading_progress_detail_id BIGINT NOT NULL,
  workflow_instance_id BIGINT NOT NULL,
  route_id VARCHAR(64) DEFAULT NULL,
  sku_code VARCHAR(32) DEFAULT NULL,
  sku_desc VARCHAR(255) DEFAULT NULL,
  bay_name VARCHAR(128) DEFAULT NULL,
  quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
  processed_quantity DECIMAL(12,3) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_lpdb_parent (loading_progress_detail_id),
  KEY idx_lpdb_instance (tenant_id, workflow_instance_id),
  KEY idx_lpdb_status (tenant_id, workflow_instance_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'LoadingProgressDetailBay', 'Loading Progress Bay Detail', 'loading_progress_detail_bay',
       'One row per bay/SKU entry within a loading transaction, child of LoadingProgressDetail. Created by Start Bay, closed by Bay Complete on aria:loading-in-progress-entry.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'LoadingProgressDetailBay');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'LoadingProgressDetailBay' b,'loading_progress_detail_id' c,'Loading Progress Detail' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'LoadingProgressDetailBay','workflow_instance_id','Workflow Instance','LONG',1,1,0,1,'number' UNION ALL
  SELECT -1,'LoadingProgressDetailBay','route_id','Route','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'LoadingProgressDetailBay','sku_code','SKU','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'LoadingProgressDetailBay','sku_desc','SKU Desc','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'LoadingProgressDetailBay','bay_name','Bay','STRING',1,1,1,1,'text' UNION ALL
  SELECT -1,'LoadingProgressDetailBay','quantity','Quantity','DECIMAL',0,1,0,1,'number' UNION ALL
  SELECT -1,'LoadingProgressDetailBay','processed_quantity','Processed Quantity','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'LoadingProgressDetailBay','status','Status','STRING',1,1,0,1,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'LoadingProgressDetailBay' AND field_code = x.c);
