-- Page: Planning Dispatch Order Sheet upload (M5 loading/dispatch planning)
-- Default body_json is a placeholder file-upload shell; wire a real
-- FileImportButton endpoint once the backend importer exists (see
-- pico:order-dispatch-detail-upload / V54 for the pattern to upgrade to).

INSERT INTO `page_definition` (`tenant_id`, `code`, `title`, `description`, `category`, `status`)
VALUES (1, 'aria:loading-planning-order-sheet-upload', 'Planning Dispatch Order Sheet',
        'Upload the planning dispatch order sheet for loading.', 'M5', 'PUBLISHED')
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `description` = VALUES(`description`),
  `category` = VALUES(`category`),
  `status` = VALUES(`status`);

INSERT IGNORE INTO `page_version` (`page_id`, `version`, `status`, `body_json`, `rationale`, `published_at`)
SELECT `id`, 1, 'PUBLISHED',
  '{"kind":"layout","schemaVersion":2,"layout":[{"kind":"Section","title":"Planning Dispatch Order Sheet","description":"Upload the planning dispatch order sheet.","children":[{"kind":"FormField","type":"file","name":"orderSheetFile","label":"Dispatch Order Sheet","accept":".csv,.xlsx,.xls"}]}]}',
  'V229 initial', NOW()
FROM `page_definition` WHERE `code` = 'aria:loading-planning-order-sheet-upload';

UPDATE `page_definition` pd
JOIN `page_version` pv ON pv.`page_id` = pd.`id` AND pv.`version` = 1
SET pd.`current_version_id` = pv.`id`
WHERE pd.`code` = 'aria:loading-planning-order-sheet-upload' AND pd.`current_version_id` IS NULL;
