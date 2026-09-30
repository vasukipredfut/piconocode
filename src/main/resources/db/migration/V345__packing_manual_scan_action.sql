-- Packing Process — manual (non-PLC) barcode scan validation.
--
-- M5.PackingRun (workflow), m5_packing_run, m5_packing_scan_log and
-- SkuMaster.barcode/offer_variant already existed (built for the automated
-- conveyor/PLC line, com.aria.integration.conveyor.ConveyorAgentController).
-- This registers the operator-facing equivalent for a manual handheld-scanner
-- web-page flow (M5PackScanActionHandler): validates the scanned barcode
-- against the approved SKU Master list, rejects unknown/inactive barcodes,
-- and offers an offer_variant picker when one barcode matches more than one
-- active SKU Master row.
--
-- Idempotent -- safe to (re)run on any database.

INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm5.pack-scan', 'Scan Box', 'workflow',
       '{"type":"object","properties":{"packingRunId":{"type":"integer"},"barcode":{"type":"string"},"bay_code":{"type":"string"},"offer_variant":{"type":"string"},"bundle_count":{"type":"number"}},"required":["packingRunId","barcode"]}',
       1, NULL,
       'Manual barcode scan validation for a Packing Run (operator handheld scanner, web page). Rejects barcodes not in the active SKU Master list; if one barcode matches multiple offer_variant rows, returns needsOfferSelection so the page can prompt the operator, then re-invoke with offer_variant set. Accepted scans are logged to m5_packing_scan_log and roll up onto m5_packing_run.actual_packs.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm5.pack-scan');
