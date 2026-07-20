-- List page for SkuMaster, curated to surface the SKU <-> carton-box naming
-- fields added in V39 alongside the core identity/status columns.
-- Idempotent throughout — safe to (re)run on any database.

INSERT INTO `page_definition` (`tenant_id`, `code`, `title`, `category`, `status`)
VALUES (1, 'aria:skumaster-list', 'SKU Master', 'M5', 'PUBLISHED')
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `category` = VALUES(`category`);

INSERT IGNORE INTO `page_version` (`page_id`, `version`, `body_json`, `status`, `published_at`)
SELECT `id`, 1,
  '{"kind":"list","dataSource":{"type":"entity","entity":"SkuMaster"},"columns":[{"field":"sku_code","label":"SKU","sortable":true},{"field":"sku_name","label":"SKU Name","sortable":true},{"field":"barcode","label":"Barcode","sortable":true},{"field":"carton_box_name","label":"Carton Box Name","sortable":true},{"field":"brand","label":"Brand","sortable":true},{"field":"product_family","label":"Family","sortable":true},{"field":"status","label":"Status","sortable":true}],"pageSize":25}',
  'PUBLISHED', NOW()
FROM `page_definition` WHERE `code` = 'aria:skumaster-list';

UPDATE `page_definition` pd
JOIN `page_version` pv ON pv.`page_id` = pd.`id` AND pv.`version` = 1
SET pd.`current_version_id` = pv.`id`
WHERE pd.`code` = 'aria:skumaster-list' AND pd.`current_version_id` IS NULL;
