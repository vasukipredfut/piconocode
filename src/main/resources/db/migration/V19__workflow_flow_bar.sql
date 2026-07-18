-- V19 — flow progress bar: "you are here" + "next step ->" on gate-pass pages.
--
-- After creating a gate pass, an operator had no idea which of the ~10 M10 pages
-- came next (the same page is reused several times and the flow branches). The
-- WorkflowFlowBar primitive reads the record's flow variant + current state and
-- renders the happy-path steps with the current one highlighted plus a
-- "Next: <page>" button (data from GET /api/workflow/flow/{code}). Generic — any
-- workflow-backed entity can turn it on.
--
-- This registers the primitive, switches it on for the gate-pass DETAIL (via a
-- flowTracker block DetailView reads) and drops it onto the gate-pass SLIP (where
-- the front gate lands right after Create), so the next step is always one click.

INSERT INTO page_element_catalog (code, category, display_name, icon, description, status, config_schema_json)
VALUES ('WorkflowFlowBar', 'DISPLAY', 'Flow Progress', 'Timeline',
  'Shows where a workflow-backed record sits in its flow (a stepper) plus a "Next step" button. Reads flow variant + current state.',
  'AVAILABLE',
  JSON_OBJECT('props', JSON_ARRAY(
    JSON_OBJECT('name','entity','type','entityCode','help','Entity mode: fetch the record by ?id= and read code+state off it.'),
    JSON_OBJECT('name','idParam','type','string','help','URL query param holding the record id (default "id").'),
    JSON_OBJECT('name','codeField','type','fieldCode','help','Field holding the workflow code (default flow_variant_code).'),
    JSON_OBJECT('name','stateField','type','fieldCode','help','Field holding the current state (default current_step).'),
    JSON_OBJECT('name','code','type','string','help','Direct mode: the workflow code (used when a parent passes the row).'),
    JSON_OBJECT('name','state','type','string','help','Direct mode: the current state.')
  )))
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name), description=VALUES(description),
  config_schema_json=VALUES(config_schema_json), status='AVAILABLE';

-- Gate-pass DETAIL: turn on the flow tracker (DetailView renders it under the header).
UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_SET(
  pv.body_json, '$.flowTracker',
  JSON_OBJECT('codeField','flow_variant_code','stateField','current_step')
)
WHERE pd.code = 'aria:gate-pass-detail'
  AND JSON_EXTRACT(pv.body_json, '$.kind') = 'detail';

-- Gate-pass SLIP: append the flow bar under the QR (idempotent via JSON_SEARCH guard).
UPDATE page_version pv
JOIN page_definition pd ON pd.id = pv.page_id
SET pv.body_json = JSON_ARRAY_APPEND(
  pv.body_json, '$.layout',
  JSON_OBJECT('kind','WorkflowFlowBar','entity','GatePass','idParam','id',
              'codeField','flow_variant_code','stateField','current_step')
)
WHERE pd.code = 'aria:gate-pass-slip'
  AND JSON_EXTRACT(pv.body_json, '$.layout[0].kind') = 'QrCode'
  AND JSON_SEARCH(pv.body_json, 'one', 'WorkflowFlowBar') IS NULL;
