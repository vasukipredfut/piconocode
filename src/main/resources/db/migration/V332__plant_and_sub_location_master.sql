-- Plant + Godown master: two-level physical-location hierarchy (Plant e.g.
-- "SUNRAJA - HEAD OFFICE", Godown e.g. "POUCH-GODOWN" inside that plant),
-- requested to eventually back a live-stock-by-section view. Godowns are
-- scoped per-plant (each row belongs to exactly one plant, per business
-- decision -- codes are NOT a shared/reusable type list).
--
-- Structure only -- no sample data seeded, since the two draft lists supplied
-- (SR0001/SRG000x vs SR01/PR01) didn't agree on final plant codes/names.
-- Add real rows via the generic entity list screens once deployed.
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS m5_plant (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  plant_code VARCHAR(20) NOT NULL,
  plant_name VARCHAR(150) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_plant_code (tenant_id, plant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS m5_plant_godown (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL,
  plant_id BIGINT NOT NULL,
  godown_code VARCHAR(20) NOT NULL,
  godown_name VARCHAR(150) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_plant_godown_code (tenant_id, plant_id, godown_code),
  CONSTRAINT fk_pg_plant FOREIGN KEY (plant_id) REFERENCES m5_plant (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, category, read_role, write_role)
SELECT -1, 'Plant', 'Plant', 'm5_plant', 'M5', '*', 'TenantAdmin'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'Plant');

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, category, read_role, write_role)
SELECT -1, 'PlantGodown', 'Godown Master', 'm5_plant_godown', 'M5', '*', 'TenantAdmin'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'PlantGodown');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'Plant', 'plant_code', 'Plant Code', 'STRING', 1, 1, 1, 1, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'Plant' AND field_code = 'plant_code');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'Plant', 'plant_name', 'Plant Name', 'STRING', 1, 1, 1, 1, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'Plant' AND field_code = 'plant_name');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind, relation_entity, options_json)
SELECT -1, 'PlantGodown', 'plant_id', 'Plant', 'RELATION', 1, 1, 0, 1, 'entity-ref', 'Plant', '{"entity":"Plant","valueField":"id","labelField":"plant_name"}'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'PlantGodown' AND field_code = 'plant_id');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'PlantGodown', 'godown_code', 'Godown Code', 'STRING', 1, 1, 1, 1, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'PlantGodown' AND field_code = 'godown_code');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT -1, 'PlantGodown', 'godown_name', 'Godown Name', 'STRING', 1, 1, 1, 1, 'text'
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code = 'PlantGodown' AND field_code = 'godown_name');

-- List pages
INSERT INTO `page_definition` (`tenant_id`, `code`, `title`, `category`, `status`)
VALUES (1, 'aria:plant-list', 'Plant Master', 'Master', 'PUBLISHED')
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `category` = VALUES(`category`);

INSERT IGNORE INTO `page_version` (`page_id`, `version`, `body_json`, `status`, `published_at`)
SELECT `id`, 1,
  '{"kind":"list","dataSource":{"type":"entity","entity":"Plant"},"columns":[{"field":"plant_code","label":"Plant Code","sortable":true},{"field":"plant_name","label":"Plant Name","sortable":true}],"pageSize":25}',
  'PUBLISHED', NOW()
FROM `page_definition` WHERE `code` = 'aria:plant-list';

UPDATE `page_definition` pd
JOIN `page_version` pv ON pv.`page_id` = pd.`id` AND pv.`version` = 1
SET pd.`current_version_id` = pv.`id`
WHERE pd.`code` = 'aria:plant-list' AND pd.`current_version_id` IS NULL;

INSERT INTO `page_definition` (`tenant_id`, `code`, `title`, `category`, `status`)
VALUES (1, 'aria:godown-master-list', 'Godown Master', 'Master', 'PUBLISHED')
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`), `category` = VALUES(`category`);

INSERT IGNORE INTO `page_version` (`page_id`, `version`, `body_json`, `status`, `published_at`)
SELECT `id`, 1,
  '{"kind":"list","dataSource":{"type":"entity","entity":"PlantGodown"},"columns":[{"field":"plant_id","label":"Plant","sortable":true},{"field":"godown_code","label":"Godown Code","sortable":true},{"field":"godown_name","label":"Godown Name","sortable":true}],"pageSize":25}',
  'PUBLISHED', NOW()
FROM `page_definition` WHERE `code` = 'aria:godown-master-list';

UPDATE `page_definition` pd
JOIN `page_version` pv ON pv.`page_id` = pd.`id` AND pv.`version` = 1
SET pd.`current_version_id` = pv.`id`
WHERE pd.`code` = 'aria:godown-master-list' AND pd.`current_version_id` IS NULL;
