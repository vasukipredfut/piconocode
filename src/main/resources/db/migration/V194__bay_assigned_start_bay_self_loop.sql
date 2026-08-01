-- M10StartBayActionHandler deliberately supports pressing "Start Bay" more
-- than once per instance -- the FIRST call drives LOADING_IN_PROGRESS ->
-- BAY_ASSIGNED via the workflow engine, but every call AFTER that (Bay2,
-- Bay3, ...) skips the engine entirely (see the handler's own "else:
-- already BAY_ASSIGNED -- no FSM call needed" branch) and just opens more
-- loading_progress_detail_bay rows under the same instance.
--
-- Found live: GET /api/workflow/instances/{id}/available?pageCode=... is
-- documented as "UI gates buttons by this" (WorkflowController.java). Once
-- an instance reaches BAY_ASSIGNED, that endpoint returns exactly ONE
-- transition -- BAY_ASSIGNED -> WEIGHMENT_2 (Completed) -- because there is
-- no FSM edge at all representing "you can still press Start Bay from
-- here" (the real mechanic bypasses the FSM, so it was never modelled as
-- an edge). If the frontend hides/disables buttons that have no matching
-- available transition, Start Bay disappears the moment Bay1 is recorded,
-- leaving only Completed -- which is exactly the reported symptom: load
-- Bay1, then whatever's clickable next silently advances the workflow
-- instead of letting Bay2/Bay3 be recorded.
--
-- Fix: add a same-state loop-back edge, BAY_ASSIGNED -> BAY_ASSIGNED, same
-- guard as the real one. This is never actually traversed at runtime (the
-- Java handler still skips the engine call for repeat bays, per above) --
-- it exists purely so /available reports Start Bay as still legal once
-- BAY_ASSIGNED, matching what the handler already allows.
--
-- Idempotent -- this migration deliberately adds a SECOND transition that
-- also targets BAY_ASSIGNED, so it can't reuse V193's single-match "is
-- there already a transition to BAY_ASSIGNED" guard. Instead: exactly one
-- transition currently targets BAY_ASSIGNED (the real guarded edge) before
-- this runs; after it runs there are two (the self-loop too). Guarding on
-- "exactly one so far" keeps this safe to (re)run on any database.

UPDATE workflow_definition
   SET body_json = JSON_ARRAY_APPEND(body_json, '$.transitions',
       JSON_OBJECT('from', 'BAY_ASSIGNED', 'to', 'BAY_ASSIGNED', 'trigger', 'page.submitted',
                    'pageCode', 'aria:loading-in-progress-entry', 'guard', '#submitted[''bayAction''] == ''start'''))
 WHERE code IN ('M10.PackLoading', 'M10.TankerOutbound', 'M10.FGLoading')
   AND status = 'PUBLISHED'
   AND JSON_SEARCH(body_json, 'one', 'BAY_ASSIGNED', NULL, '$.states[*].code') IS NOT NULL
   AND JSON_LENGTH(JSON_SEARCH(body_json, 'all', 'BAY_ASSIGNED', NULL, '$.transitions[*].to')) = 1;
