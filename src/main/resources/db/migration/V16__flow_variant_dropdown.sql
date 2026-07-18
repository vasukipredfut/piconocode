-- V16 — flow_variant_code is a dropdown, not a free-text box.
--
-- GatePass.flow_variant_code drives the whole M10 flow (which workflow runs,
-- which slip prints, which fields show). It was registered as widget_kind='text',
-- so every edit form and inline-edit rendered a bare text box where a user could
-- type anything. Make it a select with the 11 real variants + friendly labels.
-- Substrate data on the product row (tenant_id=-1) — every tenant inherits it.

UPDATE entity_field_registry
SET widget_kind = 'select',
    options_json = '[{"label": "Tanker Inbound (RM oil receipt)", "value": "M10.TankerInbound"}, {"label": "Tanker Outbound (bulk oil dispatch)", "value": "M10.TankerOutbound"}, {"label": "Packing — Loading (packed goods out)", "value": "M10.PackLoading"}, {"label": "Packing — Unload (packed goods in)", "value": "M10.PackUnload"}, {"label": "FG Loading (finished goods out)", "value": "M10.FGLoading"}, {"label": "Return Stock In", "value": "M10.ReturnStockIn"}, {"label": "Internal Transfer In", "value": "M10.IntTransferIn"}, {"label": "Internal Transfer Out", "value": "M10.IntTransferOut"}, {"label": "Third Party In", "value": "M10.ThirdPartyIn"}, {"label": "Third Party Out", "value": "M10.ThirdPartyOut"}, {"label": "E-Way Bill", "value": "M10.EwayBill"}]'
WHERE entity_code = 'GatePass' AND field_code = 'flow_variant_code'
  AND tenant_id = -1;
