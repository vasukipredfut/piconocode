-- aria:skipped-item-allocation had no Queue widget at all (no dataSources,
-- no WorkflowQueueList) -- only a free-typed gpNumber picker with no role
-- filter. Adding the same page-scoped queue pattern as
-- aria:weighment-capture (V220) / aria:loading-allocation.
--
-- FSM wiring (binding SKIPPED_ITEM_ALLOCATION's transition pageCode to this
-- page) is being done separately via the Workflow Designer UI -- not part
-- of this migration. Persona 'Dispatch' matches SKIPPED_ITEM_ALLOCATION's
-- assignedRole in M10.FGLoading, confirmed live in the Designer.
--
-- REPLACE() on a single anchor substring -- no-op on rerun once it's gone --
-- scoped by pd.code + pv.status so it always targets the currently
-- published version without needing its numeric page_version.id.

UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = REPLACE(
    pv.body_json,
    '"entity":"GatePass","layout":[{"kind":"Grid","columns":12,"gap":16,"children":[{"kind":"Section","title":"Gate Pass — Vehicle & Flow Details"',
    '"entity":"GatePass","dataSources":[{"id":"q","kind":"rest","method":"GET","url":"/api/workflow/queue-for-page?pageCode=aria:skipped-item-allocation"}],"layout":[{"kind":"Grid","columns":12,"gap":16,"children":[{"kind":"Section","title":"Skipped Item Allocation — Queue","span":12,"subtitle":"Vehicles awaiting skipped-item allocation at this step.","children":[{"kind":"WorkflowQueueList","persona":"Dispatch","dataSource":"q","pageSize":25,"cardFields":["gpNumber","vehicleNo","flowVariantCode","currentState"],"fillFormOnSelect":true,"priorityShading":true}]},{"kind":"Section","title":"Gate Pass — Vehicle & Flow Details"'
)
WHERE pd.code = 'aria:skipped-item-allocation' AND pv.status = 'PUBLISHED';
