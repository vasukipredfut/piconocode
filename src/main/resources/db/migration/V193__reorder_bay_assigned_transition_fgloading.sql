-- V184 already fixed M10.PackLoading and M10.FGLoading's missing/misordered
-- BAY_ASSIGNED wiring once (applied 2026-07-31, confirmed live: PackLoading
-- id=142 now has the guarded LOADING_IN_PROGRESS -> BAY_ASSIGNED transition
-- correctly at index 0). But M10.FGLoading was republished again through
-- the Designer AFTER V184 ran (v24 -> v29, adding the new
-- SKIPPED_ITEM_ALLOCATION step along the way) -- and that republish put the
-- guarded transition back at the END of the array, reintroducing the exact
-- bug V184 fixed. Confirmed live: FGLoading's current PUBLISHED row (id=160,
-- v29) has the guarded transition at index 22 of 23, after the unguarded
-- LOADING_IN_PROGRESS -> WEIGHMENT_2 edge that wins the match every time.
--
-- Flyway never re-runs V184 (already recorded as applied), so this repeats
-- just its step-4 reorder logic under a new version -- same reasoning as
-- there: JSON_REMOVE and JSON_EXTRACT both read the pre-update body_json,
-- so removing the old position and re-inserting at [0] is safe in one
-- statement. Scoped generically (code + status='PUBLISHED') rather than a
-- hardcoded id, since this is exactly the kind of republish-driven regression
-- that keeps recurring -- expect to need this again after a future republish
-- unless the Designer itself learns to preserve guard-precedence ordering.
--
-- Idempotent -- a no-op on PackLoading/TankerOutbound, which are already
-- correctly ordered.

UPDATE workflow_definition
   SET body_json = JSON_ARRAY_INSERT(
         JSON_REMOVE(body_json, REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_json, 'one', 'BAY_ASSIGNED', NULL, '$.transitions[*].to')), '.to', '')),
         '$.transitions[0]',
         JSON_EXTRACT(body_json, REPLACE(JSON_UNQUOTE(JSON_SEARCH(body_json, 'one', 'BAY_ASSIGNED', NULL, '$.transitions[*].to')), '.to', ''))
       )
 WHERE code IN ('M10.PackLoading', 'M10.TankerOutbound', 'M10.FGLoading')
   AND status = 'PUBLISHED'
   AND JSON_SEARCH(body_json, 'one', 'BAY_ASSIGNED', NULL, '$.transitions[*].to') IS NOT NULL
   AND JSON_UNQUOTE(JSON_SEARCH(body_json, 'one', 'BAY_ASSIGNED', NULL, '$.transitions[*].to')) <> '$.transitions[0].to';
