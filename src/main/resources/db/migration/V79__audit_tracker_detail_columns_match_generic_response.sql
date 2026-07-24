-- aria:audit-tracker-detail's declared `columns` still used the camelCase
-- field names from the old bespoke /api/m0/audit-tracker/detail response
-- (actionKind, fromState, toState, actorId). Since V77 switched the detail
-- drill-down to the generic /api/m0/datasource/query on the AuditEvent
-- entity, the actual rows come back with raw column names (action_kind,
-- from_state, to_state, actor_id, resolved_entity_code, resolved_entity_id)
-- — DataSourceQueryService does not camelCase anything. Fixes the contract
-- so a frontend reading `columns` looks up fields that actually exist on
-- the response rows.

UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = JSON_SET(pv.body_json, '$.columns', JSON_ARRAY(
    JSON_OBJECT('field', 'ts', 'label', 'When'),
    JSON_OBJECT('field', 'resolved_entity_code', 'label', 'Entity'),
    JSON_OBJECT('field', 'resolved_entity_id', 'label', 'Entity ID'),
    JSON_OBJECT('field', 'action_kind', 'label', 'Action'),
    JSON_OBJECT('field', 'from_state', 'label', 'From'),
    JSON_OBJECT('field', 'to_state', 'label', 'To'),
    JSON_OBJECT('field', 'actor_id', 'label', 'Actor'),
    JSON_OBJECT('field', 'message', 'label', 'Message'),
    JSON_OBJECT('field', 'payload_json', 'label', 'Details')
))
WHERE pd.code = 'aria:audit-tracker-detail';
