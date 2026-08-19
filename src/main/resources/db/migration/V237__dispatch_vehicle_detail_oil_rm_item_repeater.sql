-- Oil RM Details on aria:dispatch-vehicle-details needs to hold MULTIPLE
-- items per vehicle (a tanker can carry more than one oil/RM item), not just
-- one. Same staged pattern as invoice_numbers on aria:billing-entry
-- (V106/V121/V124): the Renderer's Form component does not implement the
-- "repeater" widget kind yet, so today it degrades to a plain text input
-- where officers type comma-separated items — WorkflowProjectionListener's
-- pluckList()/listOf() (added alongside this migration) accept both that
-- shape and a genuine array the moment the Renderer's "+ Add" UI ships.

-- 1. Existing scalar values -> JSON array, then widen the column to hold one.
UPDATE dispatch_vehicle_detail
SET oil_rm_item = JSON_ARRAY(oil_rm_item)
WHERE oil_rm_item IS NOT NULL AND JSON_VALID(oil_rm_item) = 0;

ALTER TABLE dispatch_vehicle_detail
  MODIFY COLUMN oil_rm_item longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL;

-- Idempotent -- MariaDB has no "ADD CONSTRAINT IF NOT EXISTS" for CHECK, so
-- this guards via information_schema (same pattern as V180).
SET @constraint_exists = (
  SELECT COUNT(*) FROM information_schema.CHECK_CONSTRAINTS
  WHERE CONSTRAINT_SCHEMA = DATABASE()
    AND TABLE_NAME = 'dispatch_vehicle_detail'
    AND CONSTRAINT_NAME = 'chk_dvd_oil_rm_item_json'
);

SET @sql = IF(@constraint_exists = 0,
  'ALTER TABLE dispatch_vehicle_detail ADD CONSTRAINT chk_dvd_oil_rm_item_json CHECK (json_valid(oil_rm_item))',
  'SELECT 1');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Field registry: JSON/repeater instead of STRING/text.
UPDATE entity_field_registry
SET data_type = 'JSON', widget_kind = 'repeater'
WHERE entity_code = 'DispatchVehicleDetail' AND field_code = 'oil_rm_item';

-- 3. Page JSON: text -> repeater. Both the tenant -1 default and any
-- tenant-specific override carry the identical oilRmItem fragment today, so
-- one textual REPLACE (naturally idempotent -- once swapped, the old
-- fragment no longer matches) covers every tenant's copy of this page.
UPDATE page_version pv
JOIN page_definition pd ON pd.current_version_id = pv.id
SET pv.body_json = REPLACE(
  pv.body_json,
  '{"kind":"FormField","type":"text","name":"oilRmItem","label":"Oil RM Details","visibleWhen":"#form.flowVariantCode == \'M10.TankerOutbound\' || #form.flowVariantCode == \'M10.TankerInbound\' || #form.flowVariantCode == \'M10.ReturnStockIn\'"}',
  '{"kind":"FormField","type":"repeater","name":"oilRmItem","label":"Oil RM Details","itemType":"text","itemLabel":"Oil RM Item","minItems":1,"addLabel":"+ Add Oil RM Item","removeLabel":"−","placeholder":"e.g. Groundnut Oil, Palm Oil (comma-separated until the + row picker ships)","visibleWhen":"#form.flowVariantCode == \'M10.TankerOutbound\' || #form.flowVariantCode == \'M10.TankerInbound\' || #form.flowVariantCode == \'M10.ReturnStockIn\'"}'
)
WHERE pd.code = 'aria:dispatch-vehicle-details';
