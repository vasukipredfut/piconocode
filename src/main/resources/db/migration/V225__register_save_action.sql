-- Registers the "save" action in the catalogue. SaveActionHandler
-- (com.aria.action.handler.SaveActionHandler, code() = "save") has existed as
-- an ActionHandler bean all along, but ActionDispatcher.dispatch() only
-- reaches a handler bean after finding a matching, implemented=true row in
-- action_registry (see ActionDispatcher.java) — with no row, every call
-- 422s as "unknown_action: save" before the handler is ever consulted.
-- No page in this app has used "action":"save" until
-- V224__un_loading_flows_tenant1_pages_dispatch_entry_start.sql's plain Gate
-- Pass save button, which is what surfaced this gap. action_registry has no
-- tenant scoping (it's a global catalogue), so this applies everywhere; safe
-- since nothing else references "save" yet.
INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'save', 'Save', 'entity',
       '{"type":"object","required":["entity","formData"],"properties":{"entity":{"type":"string"},"recordId":{"type":["integer","null"]},"formData":{"type":"object"}}}',
       1, NULL, 'Generic INSERT (recordId absent) or UPDATE (recordId present) over the entity registry — SaveActionHandler/EntityWriter.upsert. Fields are filtered to those registered in entity_field_registry for the given entity.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'save');
