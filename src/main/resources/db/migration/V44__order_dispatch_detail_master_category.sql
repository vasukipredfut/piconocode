-- V41 — pico:order-dispatch-detail and pico:order-dispatch-detail-upload
-- move from category 'M5' to 'Master', matching the existing page_category
-- row (-1, 'Master', 'Master Data', ...) from V12__page_category_metadata.sql.

UPDATE page_definition
SET category = 'Master'
WHERE code IN ('pico:order-dispatch-detail', 'pico:order-dispatch-detail-upload');
