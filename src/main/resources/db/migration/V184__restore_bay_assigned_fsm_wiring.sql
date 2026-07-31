-- V176/V178 wired a BAY_ASSIGNED state + guarded LOADING_IN_PROGRESS ->
-- BAY_ASSIGNED transition (pageCode aria:loading-in-progress-entry, guard
-- #submitted['bayAction']=='start') onto M10.PackLoading/TankerOutbound/
-- FGLoading (ids 93/95/125 at the time) directly via JSON_ARRAY_APPEND/
-- JSON_ARRAY_INSERT on that specific row's body_json.
--
-- Root cause found live: that SQL patch only touched the one
-- already-published row. Whenever a workflow is next re-published through
-- the Designer/Builder, the new version is built from the Designer's own
-- stored source-of-truth, which never learned about the raw-SQL patch --
-- so the republish silently drops the BAY_ASSIGNED wiring. Confirmed this
-- actually happened: M10.FGLoading's live PUBLISHED row (v24) and
-- M10.PackLoading's live PUBLISHED row (v18) both have ZERO BAY_ASSIGNED
-- references, while the older SUPERSEDED versions still have it.
-- M10.TankerOutbound's live PUBLISHED row still has it intact, so it's
-- left alone here except for the same defensive idempotent check.
--
-- Effect of the gap: pressing "Start Bay" (m10.start-bay) while a vehicle
-- is LOADING_IN_PROGRESS finds no candidate transition guarded on
-- bayAction=='start', falls through to the old unguarded
-- LOADING_IN_PROGRESS -> WEIGHMENT_2 edge on the same pageCode, and the
-- vehicle silently jumps straight to WEIGHMENT_2 as if "Completed" had
-- been pressed -- while still reporting ok:true and inserting the bay
-- rows, so nothing in the response signals the skip.
--
-- Fixed here by matching on (code, status='PUBLISHED') instead of
-- hardcoded ids, since republishing changes the id every time -- pinning
-- to ids the way V176 did is exactly what let this regress unnoticed.
-- Idempotent -- safe to (re)run on any database/tenant.

-- 1. BAY_ASSIGNED state, copying assignedRole from LOADING_IN_PROGRESS.
UPDATE workflow_definition
   SET body_json = JSON_ARRAY_APPEND(body_json, '$.states',
       JSON_OBJECT('code', 'BAY_ASSIGNED', 'assignedRole',
         JSON_EXTRACT(body_json, REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_json, 'one', 'LOADING_IN_PROGRESS', NULL, '$.states[*].code')), '.code', '.assignedRole'))))
 WHERE code IN ('M10.PackLoading', 'M10.TankerOutbound', 'M10.FGLoading')
   AND status = 'PUBLISHED'
   AND JSON_SEARCH(body_json, 'one', 'BAY_ASSIGNED', NULL, '$.states[*].code') IS NULL
   AND JSON_SEARCH(body_json, 'one', 'LOADING_IN_PROGRESS', NULL, '$.states[*].code') IS NOT NULL;

-- 2. LOADING_IN_PROGRESS -> BAY_ASSIGNED, guarded, inserted FIRST so it's
--    evaluated before the pre-existing unguarded WEIGHMENT_2 edge on the
--    same pageCode (WorkflowEngineService matches candidates in array
--    order -- an unguarded candidate later in the array still loses to a
--    guarded one earlier, but only if the guarded one is actually there).
UPDATE workflow_definition
   SET body_json = JSON_ARRAY_INSERT(body_json, '$.transitions[0]',
       JSON_OBJECT('from', 'LOADING_IN_PROGRESS', 'to', 'BAY_ASSIGNED', 'trigger', 'page.submitted',
                    'pageCode', 'aria:loading-in-progress-entry', 'guard', '#submitted[''bayAction''] == ''start'''))
 WHERE code IN ('M10.PackLoading', 'M10.TankerOutbound', 'M10.FGLoading')
   AND status = 'PUBLISHED'
   AND JSON_LENGTH(JSON_SEARCH(body_json, 'all', 'BAY_ASSIGNED', NULL, '$.transitions[*].to')) IS NULL;

-- 3. BAY_ASSIGNED -> WEIGHMENT_2, so "Completed" still works after Start
--    Bay. PackLoading/FGLoading only -- TankerOutbound excluded, matching
--    V176's original reasoning (its LOADING_IN_PROGRESS step gates through
--    a mandatory QC_SAMPLE_APPROVED guard before WEIGHMENT_2).
UPDATE workflow_definition
   SET body_json = JSON_ARRAY_APPEND(body_json, '$.transitions',
       JSON_OBJECT('from', 'BAY_ASSIGNED', 'to', 'WEIGHMENT_2', 'trigger', 'page.submitted', 'pageCode', 'aria:loading-in-progress-entry'))
 WHERE code IN ('M10.PackLoading', 'M10.FGLoading')
   AND status = 'PUBLISHED'
   AND JSON_LENGTH(JSON_SEARCH(body_json, 'all', 'BAY_ASSIGNED', NULL, '$.transitions[*].from')) IS NULL;

-- NOTE: a 4th step reordering an already-present-but-misplaced guarded
-- transition was added here, then reverted -- this migration had already
-- been applied (see V193's comment for the full story), and editing an
-- applied migration's content breaks its recorded Flyway checksum, which
-- fails validation on every subsequent boot. That reorder logic now lives
-- in V193__reorder_bay_assigned_transition_fgloading.sql instead, under a
-- version Flyway hasn't seen yet. Never edit this file again.
