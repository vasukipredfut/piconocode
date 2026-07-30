-- aria:loading-in-progress-entry (page id 428): "✓ Completed" targeted
-- LOADING_COMPLETED, which is not a real state in any of the 3 workflows this
-- page serves (confirmed: zero hits searching every workflow_definition for
-- LOADING_COMPLETED as a `to`). The only real next state already modeled from
-- LOADING_IN_PROGRESS for FGLoading/PackLoading is WEIGHMENT_2 (today only
-- reachable via the separate aria:weighment-capture page's own pageCode) --
-- add a transition for THIS page's own code so the button on this page
-- actually fires it.
--
-- TankerOutbound (id 95) is deliberately NOT touched here: its LOADING_IN_PROGRESS
-- step gates through a mandatory QC_SAMPLE_APPROVED guard before WEIGHMENT_2
-- (transitions -> QC_SAMPLE_APPROVED/REJECTED via aria:qc-final-out; then
-- QC_SAMPLE_APPROVED -> WEIGHMENT_2). Adding a direct LOADING_IN_PROGRESS ->
-- WEIGHMENT_2 transition there would let this page skip that QC gate entirely
-- -- needs its own decision, not a copy-paste of the FGLoading/PackLoading fix.
--
-- NOTE: the page_version content itself (the "Pay Complete"/row-action fix,
-- and the RouteSkuSummary aggregate-view question it raised) is intentionally
-- NOT part of this migration -- that page is under active concurrent editing
-- via the Builder and needs its own resolution once the RouteSkuSummary
-- update-target question is settled. This file only carries the
-- workflow_definition transition addition, which is independent of that.

UPDATE workflow_definition
   SET body_json = JSON_ARRAY_APPEND(body_json, '$.transitions',
       JSON_OBJECT('from','LOADING_IN_PROGRESS','to','WEIGHMENT_2','trigger','page.submitted','pageCode','aria:loading-in-progress-entry'))
 WHERE id = 125
   AND JSON_LENGTH(JSON_SEARCH(body_json, 'all', 'aria:loading-in-progress-entry', NULL, '$.transitions[*].pageCode')) = 1;

UPDATE workflow_definition
   SET body_json = JSON_ARRAY_APPEND(body_json, '$.transitions',
       JSON_OBJECT('from','LOADING_IN_PROGRESS','to','WEIGHMENT_2','trigger','page.submitted','pageCode','aria:loading-in-progress-entry'))
 WHERE id = 93
   AND JSON_LENGTH(JSON_SEARCH(body_json, 'all', 'aria:loading-in-progress-entry', NULL, '$.transitions[*].pageCode')) = 1;
