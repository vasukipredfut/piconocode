-- V128 — let 37 thin workflow-step list pages auto-derive their columns.
--
-- These pages were hardcoded to columns [id, status] (2-3 cols), which reads as
-- an unusably narrow list. Clearing the explicit columns makes ListView fall
-- back to its generic smart-default: up to 8 columns ranked from
-- entity_field_registry (id, required, searchable, dates, status-last). Every
-- page instantly shows the real record shape (vehicle, subject, dates, actor…)
-- with zero per-page authoring — the whole point of the substrate. Entities
-- with fewer than 5 fields (e.g. Role) legitimately stay narrow.
--
-- JSON_SET on $.columns is idempotent; only PUBLISHED bodies that already have
-- a columns array are touched.

UPDATE page_version pv
SET pv.body_json = JSON_SET(pv.body_json, '$.columns', JSON_ARRAY())
WHERE pv.status = 'PUBLISHED'
  AND JSON_CONTAINS_PATH(pv.body_json, 'one', '$.columns')
  AND pv.page_id IN (SELECT id FROM page_definition WHERE code IN ('pico:role-master','pico:kyc-docs','pico:eway-create','pico:eway-cancel','pico:eway-conditional','pico:increment-mgr-approve','pico:increment-hr-approve','pico:increment-ceo-approve','pico:increment-release','pico:increment-accept','pico:increment-reject','pico:appraisal-self','pico:appraisal-mgr','pico:appraisal-hr','pico:hiring-call','pico:hiring-reject','pico:hiring-tech','pico:hiring-hm','pico:hiring-hr','pico:hiring-offer','pico:hiring-accept','pico:hiring-decline','pico:hiring-onboard','pico:plant-transfer-create','pico:plant-transfer-approve','pico:plant-transfer-cancel','pico:plant-transfer-load','pico:plant-transfer-weigh-out','pico:plant-transfer-gate-out','pico:plant-transfer-arrive','pico:plant-transfer-weigh-in','pico:plant-transfer-gate-in','pico:plant-transfer-unload','pico:plant-transfer-stock','custom:return-material-workflow-request-return','custom:inspect','custom:approve-or-reject'));
