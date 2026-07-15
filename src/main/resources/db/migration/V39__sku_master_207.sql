-- V39: expand SKU master from 38 → 207 via programmatic generator. The
-- Pico proposal calls for 207 SKUs across all 9 product families × pack
-- sizes × offer variants. We generate them deterministically so re-runs
-- are idempotent via INSERT IGNORE on uq_sku_code.
DROP PROCEDURE IF EXISTS gen_skus;
DELIMITER $$
CREATE PROCEDURE gen_skus()
BEGIN
  DECLARE i_brand INT DEFAULT 0;
  DECLARE i_fam INT DEFAULT 0;
  DECLARE i_pack INT DEFAULT 0;
  DECLARE i_offer INT DEFAULT 0;
  DECLARE brand_code VARCHAR(8);
  DECLARE brand_name VARCHAR(32);
  DECLARE fam_code VARCHAR(8);
  DECLARE fam_name VARCHAR(32);
  DECLARE hsn VARCHAR(16);
  DECLARE gst DECIMAL(4,2);
  DECLARE pack_ml INT;
  DECLARE pack_str VARCHAR(8);
  DECLARE pcs INT;
  DECLARE carton DECIMAL(8,3);
  DECLARE bar VARCHAR(64);
  DECLARE offer VARCHAR(32);
  DECLARE offer_suffix VARCHAR(12);
  DECLARE mrp DECIMAL(10,2);
  DECLARE sku VARCHAR(64);

  WHILE i_brand < 4 DO
    SET brand_code = ELT(i_brand+1, 'IDH', 'PCP', 'PCG', 'PCS');
    SET brand_name = ELT(i_brand+1, 'Aria', 'Pico Pure', 'Pico Gold', 'Pico Saver');
    SET i_fam = 0;
    WHILE i_fam < 9 DO
      SET fam_code = ELT(i_fam+1, 'GIN','GND','CNT','SNF','RCB','MST','JAG','POJ','AGB');
      SET fam_name = ELT(i_fam+1, 'Gingelly','Groundnut','Coconut','Sunflower','Rice Bran','Mustard','Jaggery','Pooja Oil','Agarbatti');
      SET hsn = ELT(i_fam+1, '15155029','15081000','15131900','15121110','15159040','15141900','17031000','15159040','33074100');
      SET gst = IF(i_fam=8, 12.00, 5.00);
      SET i_pack = 0;
      WHILE i_pack < 6 DO
        SET pack_ml = ELT(i_pack+1, 100, 200, 500, 1000, 2000, 5000);
        SET pack_str = ELT(i_pack+1, '100','200','500','1L','2L','5L');
        SET pcs = ELT(i_pack+1, 72, 48, 24, 12, 6, 2);
        SET carton = ELT(i_pack+1, 7.50, 10.80, 12.50, 12.40, 12.40, 10.40);
        SET bar = CONCAT('89', LPAD(i_brand, 2, '0'), LPAD(i_fam, 2, '0'), LPAD(i_pack+10, 6, '0'));
        SET i_offer = 0;
        WHILE i_offer < 4 DO
          SET offer = ELT(i_offer+1, 'STANDARD', 'BUY1GET1', '10PCT_OFF', 'FESTIVE_PACK');
          SET offer_suffix = ELT(i_offer+1, '', '-B1G1', '-10OFF', '-FES');
          SET mrp = (pack_ml/100.0) * (40 + i_fam*5 + i_brand*8);
          IF i_offer = 2 THEN SET mrp = mrp * 0.90; END IF;
          SET sku = CONCAT(fam_code,'-',pack_str,'-',brand_code, offer_suffix);
          -- Agarbatti/Jaggery don't have pack_ml — use null for pack_size_ml
          INSERT IGNORE INTO m5_sku_master
            (tenant_id, sku_code, brand, product_family, pack_size_ml, pack_pieces_per_case, carton_weight_kg, barcode, offer_variant, bundle_pieces, mrp_inr, hsn_code, gst_rate_pct)
          VALUES
            (1, sku, brand_name, fam_name,
             IF(i_fam IN (6,8), NULL, pack_ml),
             pcs, carton, bar, offer, 15, mrp, hsn, gst);
          SET i_offer = i_offer + 1;
        END WHILE;
        SET i_pack = i_pack + 1;
      END WHILE;
      SET i_fam = i_fam + 1;
    END WHILE;
    SET i_brand = i_brand + 1;
  END WHILE;
END$$
DELIMITER ;

CALL gen_skus();
DROP PROCEDURE gen_skus;
