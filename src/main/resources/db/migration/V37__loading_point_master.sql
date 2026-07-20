-- LoadingPoint master: body-vehicle loading points used during dispatch
-- (pouch sections, godowns, dispatch section, jar section).
-- Idempotent throughout — safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS `m10_loading_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(64) NOT NULL,
  `display_name` varchar(128) NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_loading_point_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;

-- unique key on `code` → INSERT IGNORE skips rows already present
INSERT IGNORE INTO `m10_loading_point` (`code`, `display_name`) VALUES
('POUCH_SECTION_1', 'Pouch Section-1'),
('POUCH_SECTION_2', 'Pouch Section-2'),
('POUCH_SECTION_3', 'Pouch Section-3'),
('POUCH_SECTION_4', 'Pouch Section-4'),
('POUCH_SECTION_5', 'Pouch Section-5'),
('NEW_TIN_GODOWN', 'New Tin Godown'),
('OLD_TIN_GODOWN', 'Old Tin Godown'),
('DISPATCH_SECTION', 'Dispatch section'),
('JAR_SECTION', 'Jar section');

INSERT INTO `entity_registry`
  (`tenant_id`, `code`, `display_name`, `table_name`, `description`, `category`,
   `read_role`, `write_role`, `value_column`, `display_column`, `lifecycle_kind`)
SELECT -1, 'LoadingPoint', 'Loading Point', 'm10_loading_point',
   'Body-vehicle loading points (pouch sections, godowns, dispatch, jar section).', 'M10',
   '*', 'TenantAdmin', 'code', 'display_name', 'DEFINED'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `entity_registry` WHERE `code` = 'LoadingPoint');

INSERT INTO `entity_field_registry`
  (`tenant_id`, `entity_code`, `field_code`, `display_label`, `data_type`,
   `filterable`, `sortable`, `searchable`, `required`, `widget_kind`)
SELECT a, b, c, d, e, f, g, h, i, j FROM (
  SELECT -1 a, 'LoadingPoint' b, 'id' c, 'ID' d, 'LONG' e, 0 f, 1 g, 0 h, 1 i, 'number' j UNION ALL
  SELECT -1, 'LoadingPoint', 'code', 'Code', 'STRING', 1, 1, 1, 1, 'text' UNION ALL
  SELECT -1, 'LoadingPoint', 'display_name', 'Display Name', 'STRING', 1, 1, 1, 1, 'text' UNION ALL
  SELECT -1, 'LoadingPoint', 'active', 'Active', 'BOOLEAN', 1, 1, 0, 0, 'checkbox'
) x
WHERE NOT EXISTS (
  SELECT 1 FROM `entity_field_registry` WHERE `entity_code` = 'LoadingPoint'
);
