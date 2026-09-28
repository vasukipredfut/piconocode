-- Vehicle Breakdown: a workflow-agnostic escape hatch (M10ReportBreakdownActionHandler)
-- letting an operator report a vehicle breakdown from ANY state of ANY M10
-- workflow instance that drives a gate_pass. Two outcomes: nothing loaded
-- yet -> gate pass closed directly (workflow_instance.status='CANCELLED'
-- via the existing WorkflowEngineService.cancel() admin escape hatch, plus
-- gate_pass.status/current_step='ABORTED', closed_at=NOW()); already loaded
-- -> refused, operator must reverse via the EXISTING Reject Flow Unload
-- actions (m10.start-unload-bay / m10.unload-bay-complete) first.
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.report-breakdown', 'Report Vehicle Breakdown', 'workflow',
       '{"type":"object","properties":{"reason":{"type":"string"}}}',
       1, NULL,
       'Workflow-agnostic vehicle-breakdown escape hatch -- works from any M10 workflow state. If nothing was loaded yet, closes the gate pass directly. If stock was already loaded, refuses and directs the operator to Reject Flow Unload (Start Bay/Stop Bay) first.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.report-breakdown');
