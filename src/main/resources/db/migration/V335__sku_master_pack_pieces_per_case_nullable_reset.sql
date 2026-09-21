-- pack_pieces_per_case ("Pcs/Case" / "PER BOX INSIDE PC COUNT") was
-- NOT NULL DEFAULT 12, so every SKU silently carried 12 whether or not it
-- was ever actually confirmed. Business only has real counts for 14 SKUs
-- today -- make the column nullable, reset everyone to NULL, then set the
-- 14 confirmed values.
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE m5_sku_master MODIFY COLUMN pack_pieces_per_case INT(11) DEFAULT NULL;

UPDATE m5_sku_master SET pack_pieces_per_case = NULL WHERE tenant_id = 1;

UPDATE m5_sku_master SET pack_pieces_per_case = 55  WHERE tenant_id = 1 AND sku_code = 'SKU1591';
UPDATE m5_sku_master SET pack_pieces_per_case = 250 WHERE tenant_id = 1 AND sku_code = 'SKU1599';
UPDATE m5_sku_master SET pack_pieces_per_case = 60  WHERE tenant_id = 1 AND sku_code = 'SKU1622';
UPDATE m5_sku_master SET pack_pieces_per_case = 20  WHERE tenant_id = 1 AND sku_code = 'SKU9121';
UPDATE m5_sku_master SET pack_pieces_per_case = 20  WHERE tenant_id = 1 AND sku_code = 'SKU9126';
UPDATE m5_sku_master SET pack_pieces_per_case = 50  WHERE tenant_id = 1 AND sku_code = 'SKU9137';
UPDATE m5_sku_master SET pack_pieces_per_case = 20  WHERE tenant_id = 1 AND sku_code = 'SKU9253';
UPDATE m5_sku_master SET pack_pieces_per_case = 40  WHERE tenant_id = 1 AND sku_code = 'SKU9360';
UPDATE m5_sku_master SET pack_pieces_per_case = 20  WHERE tenant_id = 1 AND sku_code = 'SKU1440';
UPDATE m5_sku_master SET pack_pieces_per_case = 20  WHERE tenant_id = 1 AND sku_code = 'SKU1445';
UPDATE m5_sku_master SET pack_pieces_per_case = 1   WHERE tenant_id = 1 AND sku_code = 'SKU1450';
UPDATE m5_sku_master SET pack_pieces_per_case = 10  WHERE tenant_id = 1 AND sku_code = 'SKU1463';
UPDATE m5_sku_master SET pack_pieces_per_case = 10  WHERE tenant_id = 1 AND sku_code = 'SKU1471';
UPDATE m5_sku_master SET pack_pieces_per_case = 4   WHERE tenant_id = 1 AND sku_code = 'SKU1489';
