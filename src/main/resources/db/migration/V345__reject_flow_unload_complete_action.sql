-- Standalone "unloading completed" signal for Reject Flow Unloading (vehicle
-- breakdown), for the frontend to call directly regardless of what action
-- type its own "Unloading Completed" button ends up wired to
-- (M10RejectFlowUnloadCompleteActionHandler). Marks every
-- loading_progress_detail_bay row (and the loading_progress_detail parent
-- rollup) for the given instanceId REVERSED unconditionally -- same
-- "operator says unloading is done" authority as
-- RejectFlowUnloadReversalListener (which does the same thing when the
-- workflow engine itself drives a transition into UNLOADING_COMPLETED).
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.reject-flow-unload-complete', 'Reject Flow Unload — Mark Reversed', 'workflow',
       '{"type":"object","properties":{"instanceId":{"type":"integer"}}}',
       1, NULL,
       'Marks every loading_progress_detail_bay row (and the loading_progress_detail parent) for the given instanceId REVERSED. Call this from the Reject Flow Unloading "Unloading Completed" button (vehicle breakdown) so the reversal happens regardless of whether that button drives a workflow.transition.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.reject-flow-unload-complete');
