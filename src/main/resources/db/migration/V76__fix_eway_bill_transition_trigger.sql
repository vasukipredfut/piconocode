-- V76 — M10.EwayBill SUBMITTED→GENERATED transition trigger
--
-- The body_json's second transition (SUBMITTED→GENERATED) is configured
-- with `trigger: "workflow.transition"` which has no engine handler.
-- The flow-walker test agent surfaced this — both transitions reuse
-- the same page (pico:eway-create), confirming the intent is
-- page.submitted. Replace the bad trigger string in the latest
-- PUBLISHED row's body_json.
--
-- Done as a string-level REPLACE to keep migration idempotent across
-- DBs that may have already been manually patched.

UPDATE workflow_definition
SET    body_json = REPLACE(
           body_json,
           '"trigger":"workflow.transition","pageCode":"pico:eway-create"',
           '"trigger":"page.submitted","pageCode":"pico:eway-create"')
WHERE  code = 'M10.EwayBill'
  AND  status = 'PUBLISHED'
  AND  body_json LIKE '%"trigger":"workflow.transition","pageCode":"pico:eway-create"%';
