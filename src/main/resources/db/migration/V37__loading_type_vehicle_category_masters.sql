-- LoadingType / VehicleCategory masters: lookups for the GatePass /
-- DispatchEvent loading_type and vehicle_category strings, which were
-- free-text (widget_kind='text') today. Mirrors the m10_flow_variant /
-- flow_variant_code pattern from V35/V16.
-- Idempotent throughout — safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS `m10_loading_type` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) NOT NULL,
  `display_name` varchar(64) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_loading_type_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

INSERT IGNORE INTO `m10_loading_type` (`code`, `display_name`) VALUES
('LOADING', 'Loading'),
('UN_LOADING', 'Un-Loading'),
('RETURN_STOCK', 'Return Stock'),
('EMPTY', 'Empty'),
('OTHERS', 'Others');

CREATE TABLE IF NOT EXISTS `m10_vehicle_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) NOT NULL,
  `display_name` varchar(64) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_vehicle_category_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

INSERT IGNORE INTO `m10_vehicle_category` (`code`, `display_name`) VALUES
('BODY_VEHICLE', 'Body Vehicle'),
('TANKER', 'Tanker'),
('OPEN_BODY', 'Open Body'),
('CRANE', 'Crane'),
('JCB', 'JCB'),
('TRACTOR', 'Tractor'),
('STAFF_BUS', 'Staff Bus'),
('CAR', 'Car'),
('TWO_WHEELER', 'Two-Wheeler'),
('AUTO', 'Auto');

-- entity_registry rows so these masters are manageable as their own list/edit pages

INSERT INTO `entity_registry`
  (`tenant_id`, `code`, `display_name`, `table_name`, `description`, `category`,
   `read_role`, `write_role`, `value_column`, `display_column`, `lifecycle_kind`)
SELECT -1, 'LoadingType', 'Loading Type', 'm10_loading_type',
   'Lookup for GatePass/DispatchEvent loading_type values.', 'M10',
   '*', 'TenantAdmin', 'code', 'display_name', 'DEFINED'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `entity_registry` WHERE `code` = 'LoadingType');

INSERT INTO `entity_registry`
  (`tenant_id`, `code`, `display_name`, `table_name`, `description`, `category`,
   `read_role`, `write_role`, `value_column`, `display_column`, `lifecycle_kind`)
SELECT -1, 'VehicleCategory', 'Vehicle Category', 'm10_vehicle_category',
   'Lookup for GatePass/DispatchEvent vehicle_category values.', 'M10',
   '*', 'TenantAdmin', 'code', 'display_name', 'DEFINED'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `entity_registry` WHERE `code` = 'VehicleCategory');

INSERT INTO `entity_field_registry`
  (`tenant_id`, `entity_code`, `field_code`, `display_label`, `data_type`,
   `filterable`, `sortable`, `searchable`, `required`, `widget_kind`)
SELECT a, b, c, d, e, f, g, h, i, j FROM (
  SELECT -1 a, 'LoadingType' b, 'id' c, 'ID' d, 'LONG' e, 0 f, 1 g, 0 h, 1 i, 'number' j UNION ALL
  SELECT -1, 'LoadingType', 'code', 'Code', 'STRING', 1, 1, 1, 1, 'text' UNION ALL
  SELECT -1, 'LoadingType', 'display_name', 'Display Name', 'STRING', 1, 1, 1, 1, 'text' UNION ALL
  SELECT -1, 'LoadingType', 'active', 'Active', 'BOOLEAN', 1, 1, 0, 0, 'checkbox'
) x
WHERE NOT EXISTS (
  SELECT 1 FROM `entity_field_registry` WHERE `entity_code` = 'LoadingType'
);

INSERT INTO `entity_field_registry`
  (`tenant_id`, `entity_code`, `field_code`, `display_label`, `data_type`,
   `filterable`, `sortable`, `searchable`, `required`, `widget_kind`)
SELECT a, b, c, d, e, f, g, h, i, j FROM (
  SELECT -1 a, 'VehicleCategory' b, 'id' c, 'ID' d, 'LONG' e, 0 f, 1 g, 0 h, 1 i, 'number' j UNION ALL
  SELECT -1, 'VehicleCategory', 'code', 'Code', 'STRING', 1, 1, 1, 1, 'text' UNION ALL
  SELECT -1, 'VehicleCategory', 'display_name', 'Display Name', 'STRING', 1, 1, 1, 1, 'text' UNION ALL
  SELECT -1, 'VehicleCategory', 'active', 'Active', 'BOOLEAN', 1, 1, 0, 0, 'checkbox'
) x
WHERE NOT EXISTS (
  SELECT 1 FROM `entity_field_registry` WHERE `entity_code` = 'VehicleCategory'
);

-- Turn the free-text GatePass/DispatchEvent fields into dropdowns backed by the masters above.

UPDATE entity_field_registry
SET widget_kind = 'select',
    options_json = '[{"label": "Loading", "value": "LOADING"}, {"label": "Un-Loading", "value": "UN_LOADING"}, {"label": "Return Stock", "value": "RETURN_STOCK"}, {"label": "Empty", "value": "EMPTY"}, {"label": "Others", "value": "OTHERS"}]'
WHERE entity_code IN ('GatePass', 'DispatchEvent') AND field_code = 'loading_type'
  AND tenant_id = -1;

UPDATE entity_field_registry
SET widget_kind = 'select',
    options_json = '[{"label": "Body Vehicle", "value": "BODY_VEHICLE"}, {"label": "Tanker", "value": "TANKER"}, {"label": "Open Body", "value": "OPEN_BODY"}, {"label": "Crane", "value": "CRANE"}, {"label": "JCB", "value": "JCB"}, {"label": "Tractor", "value": "TRACTOR"}, {"label": "Staff Bus", "value": "STAFF_BUS"}, {"label": "Car", "value": "CAR"}, {"label": "Two-Wheeler", "value": "TWO_WHEELER"}, {"label": "Auto", "value": "AUTO"}]'
WHERE entity_code IN ('GatePass', 'DispatchEvent') AND field_code = 'vehicle_category'
  AND tenant_id = -1;
