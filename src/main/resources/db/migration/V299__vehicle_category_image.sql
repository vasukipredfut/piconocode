-- Client ask: each Vehicle Category (m10_vehicle_category, V39) needs an
-- image. Stored as a plain URL/path string, same convention as
-- Employee.photo / EmployeeMaster.photo_url (V2) -- widget_kind='text', no
-- new upload machinery: the existing generic file upload endpoint
-- (com.aria.document.DocumentUploadController, /api/m0/document-vault/upload)
-- uploads the file and this column just holds the resulting URL.
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE `m10_vehicle_category`
  ADD COLUMN IF NOT EXISTS `image_url` varchar(500) DEFAULT NULL AFTER `display_name`;

INSERT INTO `entity_field_registry`
  (`tenant_id`, `entity_code`, `field_code`, `display_label`, `data_type`,
   `filterable`, `sortable`, `searchable`, `required`, `widget_kind`)
SELECT -1, 'VehicleCategory', 'image_url', 'Image URL', 'STRING', 0, 0, 0, 0, 'text'
WHERE NOT EXISTS (
  SELECT 1 FROM `entity_field_registry` WHERE `entity_code` = 'VehicleCategory' AND `field_code` = 'image_url'
);
