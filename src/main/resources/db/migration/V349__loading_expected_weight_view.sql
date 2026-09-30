-- Expected weight per SKU on a loading vehicle -- same "view + registered
-- entity" pattern v_orderapp_order/OrderAppOrder already uses (V52/V235),
-- so this becomes queryable via the generic /api/m0/datasource/query
-- endpoint (filtered by workflow_instance_id) with no custom Java action
-- needed: DataSourceSpec has no SUM/multiplication/subquery support, so
-- that computation is done once here, in the view, and datasource query
-- just reads the already-computed columns like any other entity.
--
-- Line grain is (workflow_instance_id, sku_code): processed_quantity summed
-- across every DONE loading_progress_detail_bay row for that SKU on that
-- vehicle (a SKU can be split across multiple bay sessions), times the
-- SKU's own carton_weight_kg from m5_sku_master. COLLATE on the SKU-code
-- join for the same reason v_orderapp_order needs it -- the two tables were
-- created with different default collations.
--
-- vehicle_no isn't a column on loading_progress_detail_bay itself -- it
-- lives on gate_pass, joined here by workflow_instance_id (same linkage
-- gate_pass.workflow_instance_id already uses everywhere else), so a
-- consumer of this view/entity doesn't need a second lookup just to show
-- which vehicle a row belongs to.
--
-- Idempotent -- safe to (re)run on any database.

CREATE OR REPLACE VIEW v_loading_expected_weight AS
SELECT MIN(bay.min_id) AS id, bay.tenant_id, bay.workflow_instance_id, gp.vehicle_no, bay.sku_code,
       bay.total_processed_qty,
       s.carton_weight_kg,
       ROUND(bay.total_processed_qty * COALESCE(s.carton_weight_kg, 0), 3) AS expected_weight_kg
FROM (
  SELECT MIN(id) AS min_id, tenant_id, workflow_instance_id, sku_code,
         SUM(processed_quantity) AS total_processed_qty
  FROM loading_progress_detail_bay
  WHERE status = 'DONE'
  GROUP BY tenant_id, workflow_instance_id, sku_code
) bay
LEFT JOIN m5_sku_master s
       ON s.sku_code = bay.sku_code COLLATE utf8mb4_unicode_ci
      AND s.tenant_id = bay.tenant_id
LEFT JOIN gate_pass gp
       ON gp.workflow_instance_id = bay.workflow_instance_id
      AND gp.tenant_id = bay.tenant_id;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name)
SELECT -1, 'LoadingExpectedWeight', 'Loading Expected Weight', 'v_loading_expected_weight'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'LoadingExpectedWeight');

DELETE FROM entity_field_registry WHERE entity_code = 'LoadingExpectedWeight';
INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, widget_kind, required, sortable, filterable, searchable) VALUES
 (-1, 'LoadingExpectedWeight', 'workflow_instance_id', 'Vehicle Instance', 'LONG', 'number', 0, 1, 1, 0),
 (-1, 'LoadingExpectedWeight', 'vehicle_no', 'Vehicle No', 'STRING', 'text', 0, 1, 1, 1),
 (-1, 'LoadingExpectedWeight', 'sku_code', 'SKU', 'STRING', 'text', 0, 1, 1, 1),
 (-1, 'LoadingExpectedWeight', 'total_processed_qty', 'Loaded Qty', 'DECIMAL', 'number', 0, 1, 0, 0),
 (-1, 'LoadingExpectedWeight', 'carton_weight_kg', 'Unit Weight (kg)', 'DECIMAL', 'number', 0, 1, 0, 0),
 (-1, 'LoadingExpectedWeight', 'expected_weight_kg', 'Expected Weight (kg)', 'DECIMAL', 'number', 0, 1, 0, 0);
