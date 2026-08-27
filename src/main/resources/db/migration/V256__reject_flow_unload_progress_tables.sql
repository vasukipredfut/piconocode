-- Reject Flow Unloading (aria "Reject Flow Unloading — Mark In Progress" page,
-- GatePass entity): a vehicle that was already loaded (loading_progress_detail
-- / loading_progress_detail_bay, V94/V179) gets some or all of its cargo
-- taken back off and returned to stock. Mirrors the loading tables/flow
-- structure 1:1 (parent = one row per instance, child = one row per bay/SKU
-- line) rather than overloading the loading tables themselves, so a vehicle's
-- original loaded-quantity history stays intact and queryable on its own —
-- unloading_progress_detail(_bay) is the new, independent record of what
-- came back off. See M10StartUnloadBayActionHandler / M10UnloadBayCompleteActionHandler.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS unloading_progress_detail (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  vehicle_no VARCHAR(32) DEFAULT NULL,
  gate_pass_no VARCHAR(64) DEFAULT NULL,
  total_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
  processed_quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_upd_tenant_instance (tenant_id, workflow_instance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- quantity is set on Start (Unload) Bay (planned amount for that bay entry).
-- processed_quantity is set on Unload Bay Complete (amount actually taken
-- off) and stays NULL while status is IN_PROGRESS -- exact mirror of
-- loading_progress_detail_bay (V179).
CREATE TABLE IF NOT EXISTS unloading_progress_detail_bay (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  unloading_progress_detail_id BIGINT NOT NULL,
  workflow_instance_id BIGINT NOT NULL,
  route_id VARCHAR(64) DEFAULT NULL,
  order_id VARCHAR(64) DEFAULT NULL,
  sku_code VARCHAR(32) DEFAULT NULL,
  sku_desc VARCHAR(255) DEFAULT NULL,
  bay_name VARCHAR(128) DEFAULT NULL,
  quantity DECIMAL(12,3) NOT NULL DEFAULT 0,
  processed_quantity DECIMAL(12,3) DEFAULT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'IN_PROGRESS',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_updb_parent (unloading_progress_detail_id),
  KEY idx_updb_instance (tenant_id, workflow_instance_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

-- Soft-reverse marker on the ORIGINAL loading tables -- per business call,
-- unloaded rows are never deleted (audit trail), just flagged. loading_progress_detail_bay
-- already has a free-text status column (no CHECK constraint on it, unlike
-- bay_name -- see V180/V236) so it takes the new 'UNLOAD_IN_PROGRESS' /
-- 'REVERSED' values with no DDL change; the one-row-per-instance parent
-- needs a new column for the same purpose.
ALTER TABLE loading_progress_detail
  ADD COLUMN IF NOT EXISTS status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' AFTER processed_quantity;

-- Register both as generic entities (GenericEntityController CRUD + list/detail
-- pages), same pattern as V94/V179's LoadingProgressDetail(Bay) registration.
INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'UnloadingProgressDetail', 'Unloading Progress Detail', 'unloading_progress_detail',
       'One row per workflow instance -- rollup of a reject-flow unload (Reject Flow Unloading page).', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'UnloadingProgressDetail');

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'UnloadingProgressDetailBay', 'Unloading Progress Detail Bay', 'unloading_progress_detail_bay',
       'One row per bay/SKU line of a reject-flow unload -- child of UnloadingProgressDetail.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'UnloadingProgressDetailBay');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'UnloadingProgressDetail' b,'vehicle_no' c,'Vehicle' d,'STRING' e,1 f,1 g,1 h,0 i,'text' j UNION ALL
  SELECT -1,'UnloadingProgressDetail','gate_pass_no','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'UnloadingProgressDetail','total_quantity','Total Qty','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'UnloadingProgressDetail','processed_quantity','Processed Qty','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','route_id','Route','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','order_id','Order','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','sku_code','SKU','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','sku_desc','Description','STRING',0,1,1,0,'text' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','bay_name','Bay','STRING',1,1,0,0,'text' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','quantity','Quantity','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','processed_quantity','Unloaded Qty','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'UnloadingProgressDetailBay','status','Status','STRING',1,1,0,0,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = x.b AND field_code = x.c);

-- Action codes for the Reject Flow Unloading page's Start/Stop Bay buttons --
-- separate codes from m10.start-bay/m10.bay-complete (which stay untouched,
-- CONSUME-only, for the forward loading flow) so this reject-unload path
-- can never accidentally double-consume stock through the loading handlers.
INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.start-unload-bay', 'Start Unload Bay', 'workflow',
       '{"type":"object","properties":{},"note":"formData.bay_name (string) and formData.order_sku_line_items (array of {route_id,sku_code,sku_desc,unloading_qty}) are required and read from the page form, not args."}',
       1, NULL,
       'Opens one unloading_progress_detail_bay row per line in formData.order_sku_line_items, requires a matching loaded (DONE) loading_progress_detail_bay row to unload against and flags it UNLOAD_IN_PROGRESS. Used by the Reject Flow Unloading page''s Start Bay button.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.start-unload-bay');

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.unload-bay-complete', 'Unload Bay Complete', 'workflow',
       '{"type":"object","properties":{},"note":"formData.order_sku_line_items (array of {route_id,sku_code,unloading_qty}) is required and read from the page form, not args."}',
       1, NULL,
       'Closes every open unloading_progress_detail_bay row matching (route_id, sku_code), adds the unloaded quantity back to stock (m5_stock_movement kind RETURN, reversing the original CONSUME lots/bins), and soft-reverses the matching loading_progress_detail_bay row (status REVERSED). Used by the Reject Flow Unloading page''s Stop Bay button.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.unload-bay-complete');
