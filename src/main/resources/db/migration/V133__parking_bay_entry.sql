-- =====================================================================
-- V133 — Parking Bay Entry (per-vehicle parking in/out log).
-- Distinct from dispatch_parking_bay (V7_3), which is bay MASTER data
-- (code/gate/capacity). This is the transactional log of a vehicle's
-- gate pass occupying a bay between in_time and out_time.
-- =====================================================================

CREATE TABLE IF NOT EXISTS dispatch_parking_bay_entry (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  gate_pass_no VARCHAR(32) NOT NULL,
  in_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  out_time TIMESTAMP NULL,
  created_by BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_pbe_tenant_gp (tenant_id, gate_pass_no),
  KEY idx_pbe_in_time (in_time)
) ENGINE=InnoDB ROW_FORMAT=DYNAMIC DEFAULT CHARSET=utf8mb4;

-- Entity registry ------------------------------------------------------
INSERT INTO entity_registry (code, display_name, table_name, description, category)
  SELECT 'ParkingBayEntry','Parking Bay Entry','dispatch_parking_bay_entry','Vehicle parking bay in/out log linked to a gate pass.','M10'
  WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code='ParkingBayEntry');

-- Field registry ---------------------------------------------------------
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'ParkingBayEntry','gate_pass_no','Gate Pass No','STRING',TRUE,TRUE,TRUE,TRUE
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ParkingBayEntry' AND field_code='gate_pass_no');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'ParkingBayEntry','in_time','In Time','DATETIME',TRUE,TRUE,FALSE,TRUE
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ParkingBayEntry' AND field_code='in_time');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'ParkingBayEntry','out_time','Out Time','DATETIME',TRUE,TRUE,FALSE,FALSE
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ParkingBayEntry' AND field_code='out_time');
INSERT INTO entity_field_registry (entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required)
  SELECT 'ParkingBayEntry','created_by','Created By','LONG',TRUE,TRUE,FALSE,FALSE
  WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE entity_code='ParkingBayEntry' AND field_code='created_by');
