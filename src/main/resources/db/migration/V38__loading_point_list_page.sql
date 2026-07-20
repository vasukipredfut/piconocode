-- List page for the LoadingPoint master added in V37.
-- Mirrors what POST /api/m0/designer/pages (kind=list) generates.
-- Idempotent throughout — safe to (re)run on any database.

INSERT INTO `page_definition` (`tenant_id`, `code`, `title`, `category`, `status`)
VALUES (1, 'aria:loading-point-list', 'Loading Points', 'M10', 'PUBLISHED')
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `category` = VALUES(`category`);

INSERT IGNORE INTO `page_version` (`page_id`, `version`, `body_json`, `status`, `published_at`)
SELECT `id`, 1,
  '{"kind":"list","dataSource":{"type":"entity","entity":"LoadingPoint"},"columns":[{"field":"code","label":"Code","sortable":true},{"field":"display_name","label":"Display Name","sortable":true},{"field":"active","label":"Active","sortable":true}],"pageSize":25}',
  'PUBLISHED', NOW()
FROM `page_definition` WHERE `code` = 'aria:loading-point-list';

UPDATE `page_definition` pd
JOIN `page_version` pv ON pv.`page_id` = pd.`id` AND pv.`version` = 1
SET pd.`current_version_id` = pv.`id`
WHERE pd.`code` = 'aria:loading-point-list' AND pd.`current_version_id` IS NULL;
