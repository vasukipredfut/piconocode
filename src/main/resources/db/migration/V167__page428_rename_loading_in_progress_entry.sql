-- Page id=428 (tenant 1's override of the shared "aria:loading-in-progress"
-- code, the global default being page id=29/tenant -1) is renamed to
-- aria:loading-in-progress-entry.
--
-- Scope of this rename, decided after auditing every live pageCode reference
-- (workflow_definition.body_json '$**.pageCode', m0_saved_view.base_page_code,
-- page_workflow_binding.page_code, and other pages' body_json for page.open
-- links -- only workflow_definition hits came back):
--   - Tenant-1 workflows PackLoading (id 93 v3), TankerOutbound (id 95 v3),
--     FGLoading (id 125 v22) each have exactly one LOADING_IN_PROGRESS
--     transition keyed to this pageCode -- updated here so they keep matching
--     page submissions after the rename.
--   - The GLOBAL IntTransferOut workflow (id 10, tenant -1) and the global
--     catalog page (id 29) are deliberately left untouched. Tenant 1 will
--     fall back to the shared global page 29 for that one workflow's step --
--     accepted tradeoff, not a bug.
--
-- Guarded so re-running this file (or Flyway replaying it on a DB that
-- already has the rename) is a no-op.

UPDATE workflow_definition
   SET body_json = JSON_REPLACE(body_json, '$.transitions[8].pageCode', 'aria:loading-in-progress-entry')
 WHERE id = 93
   AND JSON_EXTRACT(body_json, '$.transitions[8].pageCode') = 'aria:loading-in-progress';

UPDATE workflow_definition
   SET body_json = JSON_REPLACE(body_json, '$.transitions[7].pageCode', 'aria:loading-in-progress-entry')
 WHERE id = 95
   AND JSON_EXTRACT(body_json, '$.transitions[7].pageCode') = 'aria:loading-in-progress';

UPDATE workflow_definition
   SET body_json = JSON_REPLACE(body_json, '$.transitions[4].pageCode', 'aria:loading-in-progress-entry')
 WHERE id = 125
   AND JSON_EXTRACT(body_json, '$.transitions[4].pageCode') = 'aria:loading-in-progress';

UPDATE page_definition
   SET code = 'aria:loading-in-progress-entry'
 WHERE id = 428
   AND code = 'aria:loading-in-progress';
