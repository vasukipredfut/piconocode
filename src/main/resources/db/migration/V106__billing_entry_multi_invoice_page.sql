-- V106 — aria:billing-entry: replace the single "Invoice Number" text field
-- with a repeating "Invoice Number(s)" input plus a derived, read-only
-- "Invoice Number Count".
--
-- Backend side (this migration + V121/V124 + WorkflowProjectionListener.java):
-- the form now submits an array under "invoice_numbers" instead of a scalar
-- under "invoice_no". WorkflowProjectionListener already accepts either shape
-- (see extractInvoiceNumbers()), so this page JSON change and the projection
-- change can ship independently/in either order without breaking the other.
--
-- Frontend side: "repeater" is a NEW widget_kind/field type — the Renderer's
-- Form component does not implement it yet (today's vocabulary is text|
-- number|date|datetime|textarea|select|multiselect|entity-ref|checkbox|
-- currency|chip; see entity_field_registry.widget_kind's comment in
-- V1__baseline.sql). Until the Renderer adds a case for "repeater" (render N
-- text inputs bound to an array field, "+" appends an empty slot, "-" removes
-- one, unlimited rows) and for a computed/read-only numeric field driven by
-- "computedFrom" (array length), this page will not render those two fields
-- correctly — REQUIRES FRONTEND RENDERER CHANGES, tracked separately in
-- earmsfrontend. This migration only stages the config so the backend and
-- page-JSON side are ready the moment the Renderer supports it.

UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = '{"kind": "layout", "schemaVersion": 2, "entity": "GatePass", "dataSources": [{"id": "q", "kind": "rest", "method": "GET", "url": "/api/workflow/queue/Dispatch"}], "layout": [{"kind": "Grid", "columns": 12, "gap": 16, "children": [{"kind": "Section", "title": "Billing — Queue", "span": 12, "subtitle": "Vehicles cleared by PMT, awaiting billing. Record the invoice.", "children": [{"kind": "WorkflowQueueList", "persona": "Dispatch", "dataSource": "q", "pageSize": 25, "cardFields": ["gpNumber", "vehicleNo", "flowVariant", "currentState"]}]}, {"kind": "Section", "title": "Capture & Submit", "span": 12, "children": [{"kind": "Form", "bindings": {"fields": [{"name": "gpNumber", "label": "Gate Pass # (scan / pick / type)", "type": "select", "required": true, "freeSolo": true, "scannable": true, "optionsSource": {"type": "datasource", "spec": {"type": "entity", "entity": "GatePass", "joins": [{"entity": "WorkflowInstance", "as": "wi", "on": "gate_pass.workflow_instance_id = wi.id"}], "filter": {"and": [{"field": "wi.assigned_role", "op": "eq", "value": "BillingOfficer"}, {"field": "wi.status", "op": "eq", "value": "OPEN"}]}, "select": ["id", "gp_number"], "sort": [{"field": "gp_number", "dir": "desc"}]}, "valueField": "gp_number", "labelField": "gp_number"}, "onChangeFill": {"vehicle_no": "vehicle_no", "flow_variant_code": "flow_variant_code", "instanceId": "workflow_instance_id", "reference_no": "reference_no", "dispatch_qty": "dispatch_qty"}}, {"name": "vehicle_no", "label": "Vehicle", "type": "text", "readOnly": true}, {"name": "reference_no", "label": "Order ID", "type": "text", "readOnly": true}, {"name": "dispatch_qty", "label": "Loading Qty", "type": "text", "readOnly": true}, {"name": "invoice_numbers", "label": "Invoice Number(s)", "type": "repeater", "itemType": "text", "itemLabel": "Invoice Number", "required": true, "minItems": 1, "addLabel": "+ Add Invoice Number", "removeLabel": "−", "placeholder": "e.g. INV001, INV002, INV003 (comma-separated until the + row picker ships)"}, {"name": "invoice_number_count", "label": "Invoice Number Count (calculated on save)", "type": "number", "readOnly": true, "computedFrom": "#form.invoice_numbers.length"}, {"name": "checking_officer", "label": "Billing Officer", "type": "text"}]}, "children": [{"kind": "WorkflowActionButton", "label": "Billing Done", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "targetState": "BILLING_DONE", "color": "primary"}]}]}]}]}'
WHERE pd.code = 'aria:billing-entry' AND pv.status = 'PUBLISHED';
