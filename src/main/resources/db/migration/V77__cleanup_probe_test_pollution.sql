-- V77 — clean up probe-* test pollution
--
-- Earlier persona-walk + field-level agents inadvertently POSTed to
-- EntityRegistry and PageDefinition (before I added the SKIP filter),
-- creating "probe-xxxxxx" entities + pages with random codes. They
-- pollute every list, scan, and test report. Wipe them.
--
-- Hard-coded LIKE 'probe-%' — that prefix is reserved for the test
-- agents per their Math.random() string generator. No production data
-- uses it.

DELETE FROM page_version
 WHERE id IN (
     SELECT current_version_id FROM page_definition
      WHERE code LIKE 'probe-%' AND tenant_id = 1);

DELETE FROM page_definition WHERE code LIKE 'probe-%' AND tenant_id = 1;

DELETE FROM entity_field_registry
 WHERE entity_code IN (
     SELECT code FROM entity_registry WHERE code LIKE 'probe-%');

DELETE FROM entity_registry WHERE code LIKE 'probe-%';
