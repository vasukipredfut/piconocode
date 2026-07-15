-- V78 — field-level dependency rules on entity_field_registry.
--
-- Adds three optional rule columns so a page-def can say things like:
--   { field_code: "reject_reason",
--     visible_when: "#form.decision == 'REJECTED'",
--     required_when: "#form.decision == 'REJECTED'" }
--   { field_code: "net_kg",
--     computed_from: "#form.gross_kg - #form.tare_kg" }
--
-- Expression syntax is a lightweight JS-safe subset evaluated client-side
-- by FormField (backend just stores the string). We deliberately keep the
-- backend contract simple — server-side re-validation happens in a later
-- pass if / when a UI bypass proves necessary.

ALTER TABLE entity_field_registry
    ADD COLUMN visible_when   TEXT NULL COMMENT 'JS-lite expr, e.g. #form.decision == ''REJECTED''',
    ADD COLUMN required_when  TEXT NULL COMMENT 'JS-lite expr; when true, field is required',
    ADD COLUMN computed_from  TEXT NULL COMMENT 'JS-lite expr, e.g. #form.gross_kg - #form.tare_kg';
