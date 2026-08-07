-- aria:weighment-capture never got the page-scoped Queue widget the other
-- M10 pages picked up in V170 (it wasn't in that migration's list of 8 —
-- this page only ever had the scan/select gp_number field, no queue view).
-- Same pattern as aria:loading-allocation: a Queue section on top using the
-- existing /api/workflow/queue-for-page endpoint, the scan/select field
-- below it untouched — an officer can either click a queue card or keep
-- scanning the gate pass QR directly.
--
-- REPLACE() on two anchor substrings — no-op on rerun once each is gone —
-- scoped by pd.code + pv.status like the other full-page edits in this
-- series (V74/V125), so it always targets the currently published version
-- without needing to know its numeric page_version.id.

UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = REPLACE(
    pv.body_json,
    '"dataSources":[]',
    '"dataSources":[{"id":"q","kind":"rest","method":"GET","url":"/api/workflow/queue-for-page?pageCode=aria:weighment-capture"}]'
)
WHERE pd.code = 'aria:weighment-capture' AND pv.status = 'PUBLISHED';

UPDATE page_version pv JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = REPLACE(
    pv.body_json,
    '"gap":16,"children":[{"kind":"Section","title":"Vehicle & Weighbridge"',
    '"gap":16,"children":[{"kind":"Section","title":"Weighment — Queue","span":12,"subtitle":"Vehicles awaiting a weighment capture at this step.","children":[{"kind":"WorkflowQueueList","persona":"WeighmentOfficer","dataSource":"q","pageSize":25,"cardFields":["gpNumber","vehicleNo","flowVariantCode","currentState"],"fillFormOnSelect":true,"priorityShading":true}]},{"kind":"Section","title":"Vehicle & Weighbridge"'
)
WHERE pd.code = 'aria:weighment-capture' AND pv.status = 'PUBLISHED';
