-- Seeds conveyor_loading_bay (V103, M12 Pi-conveyor scanning -- NOT the
-- M10 truck-dock LoadingPoint master, and NOT loading_progress_detail_bay
-- from this same session's M10 Start Bay/Bay Complete work; see V103's own
-- "do not conflate the two" comment) with the 12 physical bays, Bay1..Bay12.
--
-- workflow_code is left as '' (not tied to a specific conveyor workflow --
-- no workflow_code was designated for this). This mirrors the existing
-- "default applies to every workflow" convention already used for bay_code
-- in workflow_integration_binding (see WorkflowIntegrationBindingService:
-- "a bay-specific binding wins over the workflow-wide default, bay_code=''")
-- -- here the whole row is workflow-unscoped instead. Update workflow_code
-- to a real value (or add per-workflow rows) once one exists; the
-- UNIQUE KEY (tenant_id, workflow_code, bay_code) allows both to coexist.
--
-- Idempotent -- unique key on (tenant_id, workflow_code, bay_code) ->
-- INSERT IGNORE skips rows already present, safe to (re)run.

INSERT IGNORE INTO conveyor_loading_bay (tenant_id, workflow_code, bay_code, display_name, active)
SELECT 1, '', CONCAT('Bay', n), CONCAT('Bay ', n), 1
FROM (
  SELECT 1 n UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
  SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
  SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
) bays;
