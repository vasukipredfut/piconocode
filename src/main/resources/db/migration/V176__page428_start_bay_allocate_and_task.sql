-- aria:loading-in-progress-entry (page 428) "Start Bay" FSM wiring: today
-- it just saves data with no FSM movement (per the report this migration
-- answers: pressing Start Bay "saves the data and transaction" but does not
-- advance the workflow state, unlike "Completed" which already does via
-- V168's LOADING_IN_PROGRESS -> WEIGHMENT_2 edge).
--
-- NOTE: an earlier version of this migration also created an m10_loading_task
-- table keyed by a LoadingPoint bay master (V37). That was replaced --
-- "Bay" here turned out to mean a SKU line item on this page's
-- Order/SKU grid (route_id + sku_code), not a physical loading dock, and
-- the actual data now lives in loading_progress_detail (V94/V177, one row
-- per gate pass) + loading_progress_detail_bay (V179, one child row per
-- bay/SKU entry). See those files for the real schema.
--
-- This migration only adds the FSM plumbing:
--
--   1. A new BAY_ASSIGNED state + a guarded LOADING_IN_PROGRESS ->
--      BAY_ASSIGNED transition for the same 3 workflows page 428 already
--      serves (FGLoading=125, PackLoading=93, TankerOutbound=95 -- per
--      V167's audit). Guarded on #submitted['bayAction']=='start' so it can
--      never intercept a plain "Completed" page-submission -- the new
--      M10StartBayActionHandler (com.aria.action.handler) is the only
--      caller that ever sets that flag, and it's inserted at
--      transitions[0] so WorkflowEngineService's matching loop evaluates it
--      BEFORE the pre-existing unguarded WEIGHMENT_2 edge (an unguarded
--      candidate wins immediately regardless of position after it, per
--      handlePageSubmission's for-loop -- ordering here is load-bearing,
--      not cosmetic).
--
--   2. A BAY_ASSIGNED -> WEIGHMENT_2 edge so "Completed" still works for a
--      vehicle that went through Start Bay first. FGLoading/PackLoading
--      only -- TankerOutbound(95) excluded, mirroring V168's own reasoning:
--      its LOADING_IN_PROGRESS step gates through a mandatory
--      QC_SAMPLE_APPROVED guard before WEIGHMENT_2 and this needs its own
--      decision, not a copy-paste.
--
-- The page_version JSON (wiring the actual Start Bay/Bay Complete buttons
-- to the new action codes, and however this page binds "the selected SKU
-- row") is intentionally NOT patched here -- unlike the FSM shape, the
-- binding syntax for "currently selected ListView row" on this Renderer is
-- not confirmed from this session (no DB/API access to read page 428's
-- live body_json), so guessing at it risked shipping a wrong binding
-- silently. Wire it via the Builder using the API contract in
-- M10StartBayActionHandler / M10BayCompleteActionHandler's class javadoc.
--
-- Idempotent -- safe to (re)run on any database.

-- BAY_ASSIGNED state, copying assignedRole from LOADING_IN_PROGRESS so the
-- vehicle stays visible in the same persona's workflow queue.
UPDATE workflow_definition
   SET body_json = JSON_ARRAY_APPEND(body_json, '$.states',
       JSON_OBJECT('code', 'BAY_ASSIGNED', 'assignedRole',
         JSON_EXTRACT(body_json, REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_json, 'one', 'LOADING_IN_PROGRESS', NULL, '$.states[*].code')), '.code', '.assignedRole'))))
 WHERE id IN (93, 95, 125)
   AND JSON_SEARCH(body_json, 'one', 'BAY_ASSIGNED', NULL, '$.states[*].code') IS NULL
   AND JSON_SEARCH(body_json, 'one', 'LOADING_IN_PROGRESS', NULL, '$.states[*].code') IS NOT NULL;

-- LOADING_IN_PROGRESS -> BAY_ASSIGNED, guarded, inserted FIRST in the array.
UPDATE workflow_definition
   SET body_json = JSON_ARRAY_INSERT(body_json, '$.transitions[0]',
       JSON_OBJECT('from', 'LOADING_IN_PROGRESS', 'to', 'BAY_ASSIGNED', 'trigger', 'page.submitted',
                    'pageCode', 'aria:loading-in-progress-entry', 'guard', '#submitted[''bayAction''] == ''start'''))
 WHERE id IN (93, 95, 125)
   AND JSON_LENGTH(JSON_SEARCH(body_json, 'all', 'BAY_ASSIGNED', NULL, '$.transitions[*].to')) IS NULL;

-- BAY_ASSIGNED -> WEIGHMENT_2, so "Completed" still works after Start Bay.
UPDATE workflow_definition
   SET body_json = JSON_ARRAY_APPEND(body_json, '$.transitions',
       JSON_OBJECT('from', 'BAY_ASSIGNED', 'to', 'WEIGHMENT_2', 'trigger', 'page.submitted', 'pageCode', 'aria:loading-in-progress-entry'))
 WHERE id IN (93, 125)
   AND JSON_LENGTH(JSON_SEARCH(body_json, 'all', 'BAY_ASSIGNED', NULL, '$.transitions[*].from')) IS NULL;
