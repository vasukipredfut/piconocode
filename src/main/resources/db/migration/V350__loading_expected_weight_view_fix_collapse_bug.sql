-- V349's v_loading_expected_weight wrapped MIN(bay.min_id) AS id in the
-- OUTER select with no GROUP BY -- an aggregate function with no GROUP BY
-- collapses the ENTIRE result set to a single row (standard SQL behavior),
-- so the view returned exactly one (arbitrary) row total instead of one row
-- per (workflow_instance_id, sku_code). Confirmed live: SELECT * FROM
-- v_loading_expected_weight returned 1 row, while the identical GROUP BY
-- against loading_progress_detail_bay directly returned the correct 3 rows
-- for a test instance.
--
-- Fix: the inner "bay" subquery already groups by (tenant_id,
-- workflow_instance_id, sku_code), so every bay.* column -- including
-- min_id -- is already unique per outer row. No aggregate function is
-- needed in the outer SELECT at all; drop the MIN() wrapper and just
-- reference bay.min_id directly.
--
-- Idempotent -- safe to (re)run on any database.

CREATE OR REPLACE VIEW v_loading_expected_weight AS
SELECT bay.min_id AS id, bay.tenant_id, bay.workflow_instance_id, gp.vehicle_no, bay.sku_code,
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
