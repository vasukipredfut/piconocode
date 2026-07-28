-- aria:dispatch-vehicle-details (used by ReturnStockIn's new PARKING_IN_1 ->
-- DISPATCH_QUEUED step, V127): Vendor Details / Oil RM Details / Dispatch
-- Front Officer Name already exist on this shared page but are gated
-- TankerOutbound/TankerInbound-only — extended to also show for
-- M10.ReturnStockIn (covers the spec's "Vendor Details" / "Oil RM Details"
-- requirements directly; "Oil Type" is this same oilRmItem field — no
-- separate column needed). invoiceNo/invoiceQty/tankerNo/poVerified/
-- brokerName stay TankerOutbound/TankerInbound-only on purpose: return-stock
-- deliveries have no invoice ("NO Invoice Details Entry" in the spec) and no
-- tanker/PO to verify.
--
-- Plus the 2 fields genuinely new to this workflow (V128's new columns):
-- Invoice Stock Return Count, Box Weight — both ReturnStockIn-only.
--
-- REPLACE() targets each field's exact ("name"+"label"+"visibleWhen") block
-- so this can't accidentally touch the other TankerInbound/TankerOutbound-
-- gated fields that share the same visibleWhen string. NOT LIKE guard makes
-- this idempotent against a re-run.
UPDATE page_version pv JOIN page_definition pd ON pd.current_version_id = pv.id
   SET pv.body_json = REPLACE(REPLACE(REPLACE(REPLACE(pv.body_json,
     '"name": "vendorName", "label": "Vendor Details", "visibleWhen": "#form.flowVariantCode == \'M10.TankerOutbound\' || #form.flowVariantCode == \'M10.TankerInbound\'"',
     '"name": "vendorName", "label": "Vendor Details", "visibleWhen": "#form.flowVariantCode == \'M10.TankerOutbound\' || #form.flowVariantCode == \'M10.TankerInbound\' || #form.flowVariantCode == \'M10.ReturnStockIn\'"'),
     '"name": "oilRmItem", "label": "Oil RM Details", "visibleWhen": "#form.flowVariantCode == \'M10.TankerOutbound\' || #form.flowVariantCode == \'M10.TankerInbound\'"',
     '"name": "oilRmItem", "label": "Oil RM Details", "visibleWhen": "#form.flowVariantCode == \'M10.TankerOutbound\' || #form.flowVariantCode == \'M10.TankerInbound\' || #form.flowVariantCode == \'M10.ReturnStockIn\'"'),
     '"name": "dispatchOfficerName", "label": "Dispatch Front Officer Name", "visibleWhen": "#form.flowVariantCode == \'M10.TankerOutbound\' || #form.flowVariantCode == \'M10.TankerInbound\'"',
     '"name": "dispatchOfficerName", "label": "Dispatch Front Officer Name", "visibleWhen": "#form.flowVariantCode == \'M10.TankerOutbound\' || #form.flowVariantCode == \'M10.TankerInbound\' || #form.flowVariantCode == \'M10.ReturnStockIn\'"'),
     '{"kind": "WorkflowActionButton", "label": "Save Vehicle Details", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "variant": "primary"}',
     '{"kind": "FormField", "type": "number", "name": "invoiceReturnCount", "label": "Invoice Stock Return Count", "visibleWhen": "#form.flowVariantCode == \'M10.ReturnStockIn\'"}, {"kind": "FormField", "type": "number", "name": "boxWeightKg", "label": "Box Weight (kg)", "visibleWhen": "#form.flowVariantCode == \'M10.ReturnStockIn\'"}, {"kind": "WorkflowActionButton", "label": "Save Vehicle Details", "action": "workflow.transition", "instanceId": "{{form.instanceId}}", "variant": "primary"}')
 WHERE pd.code = 'aria:dispatch-vehicle-details' AND pv.status = 'PUBLISHED'
   AND pv.body_json NOT LIKE '%"invoiceReturnCount"%';
