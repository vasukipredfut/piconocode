-- Extend SkuMaster (m5_sku_master) with sku_name / carton_box_name so the
-- SKU <-> carton-box naming sheet can be loaded directly, and relax
-- brand / carton_weight_kg to nullable since that sheet doesn't carry them.
-- Idempotent throughout — safe to (re)run on any database.

ALTER TABLE `m5_sku_master` MODIFY `brand` varchar(64) DEFAULT NULL;
ALTER TABLE `m5_sku_master` MODIFY `carton_weight_kg` decimal(8,3) DEFAULT NULL;
ALTER TABLE `m5_sku_master` ADD COLUMN IF NOT EXISTS `sku_name` varchar(255) NULL AFTER `sku_code`;
ALTER TABLE `m5_sku_master` ADD COLUMN IF NOT EXISTS `carton_box_name` varchar(255) NULL AFTER `barcode`;

INSERT INTO `entity_field_registry`
  (`tenant_id`, `entity_code`, `field_code`, `display_label`, `data_type`,
   `filterable`, `sortable`, `searchable`, `required`, `widget_kind`)
SELECT -1, 'SkuMaster', 'sku_name', 'SKU Name', 'STRING', 1, 1, 1, 0, 'text'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `entity_field_registry` WHERE `entity_code` = 'SkuMaster' AND `field_code` = 'sku_name');

INSERT INTO `entity_field_registry`
  (`tenant_id`, `entity_code`, `field_code`, `display_label`, `data_type`,
   `filterable`, `sortable`, `searchable`, `required`, `widget_kind`)
SELECT -1, 'SkuMaster', 'carton_box_name', 'Carton Box Name', 'STRING', 1, 1, 1, 0, 'text'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `entity_field_registry` WHERE `entity_code` = 'SkuMaster' AND `field_code` = 'carton_box_name');

-- unique key (tenant_id, sku_code) -> INSERT IGNORE skips rows already present
INSERT IGNORE INTO `m5_sku_master`
  (`tenant_id`, `sku_code`, `sku_name`, `product_family`, `barcode`, `carton_box_name`)
VALUES
(1, 'SKU1005', 'SKU1005 (GINGELLY OIL RS.15 PACK BOX (08.01.25))', 'Gingelly', '8900006141016', 'SPR0050-MG-GIN- RS.15 CARTON BOX'),
(1, 'SKU1007', 'SKU1007 (GINGELLY OIL RS.40 PACK BOX (08.01.25))', 'Gingelly', '8900006151015', 'SPR0052-MG-GIN- RS.40 CARTON BOX'),
(1, 'SKU1012', 'SKU1012 (GROUNDNUT RAW OIL 1 LTR BOX (GULAS OFFER))', 'Groundnut', '8900002011023', 'SPR0019-MG-FGN- 1 LTR POUCH CARTON BOX -OFFER'),
(1, 'SKU1013', 'SKU1013 (GROUNDNUT RAW OIL 1/2 LTR BOX (GULAS OFFER))', 'Groundnut', '8900002021022', 'SPR0021-MG-FGN- 500 ML POUCH CARTON BOX -OFFER'),
(1, 'SKU1014', 'SKU1014 (GROUNDNUT RAW OIL 1/2 LTR PET BOX (GULAS OFFER))', 'Groundnut', '8900002022029', 'SPR0623-MG-FGN- 500 ML PET JAR CARTON BOX OFFER'),
(1, 'SKU1015', 'SKU1015 (GROUNDNUT RAW OIL 1 LTR PET BOX (GULAS OFFER))', 'Groundnut', '8900002012020', 'SPR0622-MG-FGN- 1 LTR PET JAR CARTON BOX OFFER'),
(1, 'SKU1024', 'SKU1024 (NAKSHATRA AGARBATTI Rs 10 VALUE PACK)', 'Agarbatti', '8900021105048', 'SPR0627 - AGARBATTI RS.10 BIG OUTER CARTON BOX'),
(1, 'SKU1027', 'SKU1027 (NAKSHATRA AGARBATTI Rs 60 MEGA COMBO BOX (COMBO-1))', 'Agarbatti', '8900021205045', 'SPR0624-AGARBATTI RS.55 PACK BOX BIG - COMBO1');
