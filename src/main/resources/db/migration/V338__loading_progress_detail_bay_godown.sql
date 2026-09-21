-- loading_progress_detail_bay: add godown_code so a loading bay session can
-- be tied to a specific Godown (PlantGodown) -- the UI is adding a Godown
-- picker on the loading page (Start Bay); this is the backend side: the
-- column to receive it, plus scoping the Bay Complete FEFO stock deduction
-- to that Godown (see M10StartBayActionHandler / M10BayCompleteActionHandler).
--
-- Nullable -- older/in-flight bay rows opened before this existed have no
-- Godown, and Bay Complete falls back to its previous (any-Godown) behaviour
-- for those.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE loading_progress_detail_bay ADD COLUMN IF NOT EXISTS godown_code VARCHAR(20) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT -1, 'LoadingProgressDetailBay', 'godown_code', 'Godown', 'STRING', 1, 1, 0, 0, 'entity-ref', 'PlantGodown', '{"entity":"PlantGodown","valueField":"godown_code","labelField":"godown_name"}'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'LoadingProgressDetailBay' AND field_code = 'godown_code');

-- Same for the unloading side (M10UnloadingBayCompleteActionHandler / RECEIPT
-- into stock via M10StartUnloadingBayActionHandler) -- a separate table, same
-- Godown key, so the received quantity posts into the operator-selected
-- Godown instead of an unqualified bin.
ALTER TABLE unloading_progress_detail_bay ADD COLUMN IF NOT EXISTS godown_code VARCHAR(20) DEFAULT NULL;

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT -1, 'UnloadingProgressDetailBay', 'godown_code', 'Godown', 'STRING', 1, 1, 0, 0, 'entity-ref', 'PlantGodown', '{"entity":"PlantGodown","valueField":"godown_code","labelField":"godown_name"}'
WHERE EXISTS (SELECT 1 FROM entity_registry WHERE code = 'UnloadingProgressDetailBay')
  AND NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'UnloadingProgressDetailBay' AND field_code = 'godown_code');
