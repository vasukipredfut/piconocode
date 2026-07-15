-- V111 — Aria v2 substrate: drive registration to FULL compliance (0 phantom fields, 0 phantom tables)
--
-- V110 repointed the 165 unambiguous phantom field_codes. This finishes the job
-- for the 18 that needed a SEMANTIC rename the underscore-matcher couldn't see,
-- plus the handful that are genuine garbage (duplicate of an already-correct
-- field, or a column that simply does not exist), plus one entity whose table
-- was never created.
--
-- Surfaced + verified by the substrate-parity harness P1 check
-- (com.aria.integration.parity.SubstrateParityHarness#assertSchemaFaithful).
-- Pure substrate data change: no @Entity touched, no business table altered.
-- Every statement is idempotent.

-- ---------------------------------------------------------------------------
-- 1. Semantic renames — the registered field_code is a real column under a
--    different name. Guarded against the uq_field unique key.
-- ---------------------------------------------------------------------------
UPDATE entity_field_registry SET field_code='name'                WHERE entity_code='Branch' AND field_code='branch_name'  AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='Branch') e WHERE e.field_code='name');
UPDATE entity_field_registry SET field_code='org_id'              WHERE entity_code='Branch' AND field_code='organisation' AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='Branch') e WHERE e.field_code='org_id');
UPDATE entity_field_registry SET field_code='order_billdate'      WHERE entity_code='Order' AND field_code='bill_date'      AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='Order') e WHERE e.field_code='order_billdate');
UPDATE entity_field_registry SET field_code='order_billno'        WHERE entity_code='Order' AND field_code='bill_no'        AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='Order') e WHERE e.field_code='order_billno');
UPDATE entity_field_registry SET field_code='branch'             WHERE entity_code='PAttributesMapping' AND field_code='branch_id' AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='PAttributesMapping') e WHERE e.field_code='branch');
UPDATE entity_field_registry SET field_code='dept'               WHERE entity_code='PAttributesMapping' AND field_code='dept_id'   AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='PAttributesMapping') e WHERE e.field_code='dept');
UPDATE entity_field_registry SET field_code='refno'              WHERE entity_code='SalesReturn' AND field_code='reference_no' AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='SalesReturn') e WHERE e.field_code='refno');
UPDATE entity_field_registry SET field_code='createddate'        WHERE entity_code='WindmillTrn' AND field_code='created_dt' AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='WindmillTrn') e WHERE e.field_code='createddate');
UPDATE entity_field_registry SET field_code='vehicle_req_allot_id' WHERE entity_code='VehicleRequestAllotmentShuffleEntity' AND field_code='allotment_id' AND NOT EXISTS (SELECT 1 FROM (SELECT field_code FROM entity_field_registry WHERE entity_code='VehicleRequestAllotmentShuffleEntity') e WHERE e.field_code='vehicle_req_allot_id');

-- ---------------------------------------------------------------------------
-- 2. The AI-agent 'Mode' entity points at m11_ai_agent_mode, which was never
--    created (the ai_agent* tables exist; no mode table). It is 100%
--    non-functional. Drop the registration; re-add properly if the feature
--    is built (create the table first, then register).
-- ---------------------------------------------------------------------------
DELETE FROM entity_field_registry WHERE entity_code='Mode';
DELETE FROM entity_registry WHERE code='Mode'
  AND NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='m11_ai_agent_mode');

-- ---------------------------------------------------------------------------
-- 3. Final sweep — any field still pointing at a non-existent column, for an
--    entity whose physical table EXISTS, is a genuine orphan or a duplicate of
--    an already-correct field. It can never read or write, so remove it to make
--    the registration faithful. (Renames above already preserved every field
--    that maps to a real column.)
-- ---------------------------------------------------------------------------
DELETE efr FROM entity_field_registry efr
JOIN entity_registry er ON er.code = efr.entity_code
WHERE EXISTS (SELECT 1 FROM information_schema.columns c
              WHERE c.table_schema = DATABASE() AND c.table_name = er.table_name)
  AND NOT EXISTS (SELECT 1 FROM information_schema.columns c
                  WHERE c.table_schema = DATABASE() AND c.table_name = er.table_name
                    AND c.column_name = efr.field_code);
