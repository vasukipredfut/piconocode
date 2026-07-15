-- V86 — Add value_column + display_column to entity_registry so the
-- generic dropdown-options endpoint (/api/m0/entities/{code}/options)
-- can render any entity as a dropdown source.
--
-- Cherry-picked from earmsbackend/develope V7_21 (originally authored by
-- Dhivya) — this part of develope aligns with the substrate strategy and
-- is landed as-is with rewritten headers and idempotency guards.
-- Idempotent — safe to reapply.

ALTER TABLE entity_registry
    ADD COLUMN IF NOT EXISTS value_column VARCHAR(100) NULL COMMENT 'Column used as the VALUE for dropdown options (e.g. id or code)',
    ADD COLUMN IF NOT EXISTS display_column VARCHAR(100) NULL COMMENT 'Column used as the LABEL for dropdown options (e.g. name, title)';

-- Populate common lookup entities. UPDATEs are inherently idempotent.
UPDATE entity_registry SET value_column='id',   display_column='name'          WHERE code='Role';
UPDATE entity_registry SET value_column='id',   display_column='branch_name'   WHERE code='Branch';
UPDATE entity_registry SET value_column='id',   display_column='employeename'  WHERE code='Employee';
UPDATE entity_registry SET value_column='code', display_column='title'         WHERE code='PageDefinition';
UPDATE entity_registry SET value_column='id',   display_column='name'          WHERE code='Driver';
UPDATE entity_registry SET value_column='id',   display_column='vehicle_no'    WHERE code='Vehicle';
UPDATE entity_registry SET value_column='id',   display_column='name'          WHERE code='Vendor';
UPDATE entity_registry SET value_column='code', display_column='display_name'  WHERE code='WorkflowDefinition';
