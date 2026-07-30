-- New action code entity.bulk-update-field (backend: EntityBulkUpdateFieldActionHandler).
--
-- Motivation: "Pay Complete" / "Bay Complete" on aria:loading-in-progress-entry's
-- SKU list needs to update EVERY underlying m5_order_dispatch_detail row for a
-- (route_id, sku_code) group, not a single row by id -- the list's dataSource
-- was changed (via the Builder, concurrently with this backend work) to
-- RouteSkuSummary (v_route_sku_summary), a GROUP BY-aggregating view where one
-- visible row represents many real OrderDispatchDetail records. Neither
-- entity.update-field (single row by id) nor an UPDATE against the aggregating
-- view itself (non-updatable in MySQL/MariaDB) can do this.
--
-- entity.bulk-update-field takes an explicit column-filter map (args.match)
-- instead of a record id, and refuses to run with an empty filter. Registering
-- it here so POST /api/m0/actions/entity.bulk-update-field/invoke resolves
-- instead of 422 unknown_action.
--
-- The page_version change to actually wire a row action to this new code is
-- deliberately NOT part of this migration -- page 428 is under active
-- concurrent editing via the Builder (it moved twice more, mid-session, while
-- this fix was being built), so publishing a page_version here would keep
-- reverting live edits. That wiring should happen once through the Builder UI
-- directly against whatever the page's current content is.
INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'entity.bulk-update-field', 'Bulk Update Field (by filter)', 'entity',
       '{"type":"object","required":["entity","field","value","match"],"properties":{"entity":{"type":"string"},"field":{"type":"string"},"value":{},"match":{"type":"object","minProperties":1}}}',
       1, NULL,
       'Updates a single field on every row of the given entity matching a non-empty column filter map (args.match). Used where a ListView row represents a grouped/aggregated set of underlying records rather than one row by id.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'entity.bulk-update-field');
