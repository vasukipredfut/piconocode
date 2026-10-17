-- aria:qc-approval (M10.TankerInbound) Sample Oil Test: the oil sample leaves in its own vehicle. The QC officer enters that
-- sample vehicle's number and the location it is unloaded to, the vehicle is weighed (first weight), unloaded, and weighed
-- again (second weight); the oil actually unloaded (|W1 - W2|) goes into stock at that location.
-- One row per sample vehicle (instance + vehicle + invoice + SKU + location), written by QcSampleVehicleActionHandler
-- (m10.qc-sample-vehicle). Status: OPEN (entered) -> W1_DONE (first weight taken) -> DONE (second weight taken, stock posted).
--
-- Idempotent -- safe to (re)run on any database.

CREATE TABLE IF NOT EXISTS qc_sample_vehicle (
  id BIGINT NOT NULL AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  workflow_instance_id BIGINT NOT NULL,
  gp_number VARCHAR(32) DEFAULT NULL,
  invoice_no VARCHAR(64) DEFAULT NULL,
  order_id VARCHAR(64) DEFAULT NULL,
  sku_code VARCHAR(64) DEFAULT NULL,
  sku_desc VARCHAR(255) DEFAULT NULL,
  vendor_name VARCHAR(255) DEFAULT NULL,
  rd_code VARCHAR(64) DEFAULT NULL,
  vehicle_no VARCHAR(32) NOT NULL COMMENT 'the sample vehicle',
  sample_location VARCHAR(64) NOT NULL COMMENT 'location the sample oil is unloaded to',
  sample_qty DECIMAL(15,3) DEFAULT NULL COMMENT 'planned / entered qty',
  w1_weight_kg DECIMAL(15,3) DEFAULT NULL,
  w2_weight_kg DECIMAL(15,3) DEFAULT NULL,
  unloaded_qty DECIMAL(15,3) DEFAULT NULL COMMENT '|W1 - W2|, what went into stock',
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  remarks VARCHAR(255) DEFAULT NULL,
  w1_by_id BIGINT DEFAULT NULL,
  w1_by_name VARCHAR(128) DEFAULT NULL,
  w2_by_id BIGINT DEFAULT NULL,
  w2_by_name VARCHAR(128) DEFAULT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_qsv_instance (tenant_id, workflow_instance_id),
  KEY idx_qsv_vehicle (tenant_id, vehicle_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci ROW_FORMAT=DYNAMIC;

INSERT INTO entity_registry (tenant_id, code, display_name, table_name, description, category)
SELECT -1, 'QcSampleVehicle', 'QC Sample Vehicle', 'qc_sample_vehicle',
       'Vehicle that carries the QC oil sample (aria:qc-approval, Tanker Inbound): vehicle no, location, first / second weight.', 'M10'
WHERE NOT EXISTS (SELECT 1 FROM entity_registry WHERE code = 'QcSampleVehicle');

INSERT INTO entity_field_registry (tenant_id, entity_code, field_code, display_label, data_type, filterable, sortable, searchable, required, widget_kind)
SELECT * FROM (
  SELECT -1 a,'QcSampleVehicle' b,'workflow_instance_id' c,'Workflow Instance' d,'LONG' e,1 f,1 g,0 h,1 i,'number' j UNION ALL
  SELECT -1,'QcSampleVehicle','gp_number','Gate Pass #','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','invoice_no','Invoice No','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','order_id','Order ID','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','sku_code','SKU Code','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','sku_desc','SKU Description','STRING',0,1,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','vendor_name','Vendor','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','rd_code','RD Code','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','vehicle_no','Sample Vehicle No','STRING',1,1,1,1,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','sample_location','Sample Location','STRING',1,1,1,1,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','sample_qty','Sample Qty','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'QcSampleVehicle','w1_weight_kg','First Weight (kg)','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'QcSampleVehicle','w2_weight_kg','Second Weight (kg)','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'QcSampleVehicle','unloaded_qty','Unloaded Qty (kg)','DECIMAL',0,1,0,0,'number' UNION ALL
  SELECT -1,'QcSampleVehicle','status','Status','STRING',1,1,0,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','remarks','Remarks','STRING',0,0,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','w1_by_id','First Weight By (user id)','LONG',1,1,0,0,'number' UNION ALL
  SELECT -1,'QcSampleVehicle','w1_by_name','First Weight By','STRING',1,1,1,0,'text' UNION ALL
  SELECT -1,'QcSampleVehicle','w2_by_id','Second Weight By (user id)','LONG',1,1,0,0,'number' UNION ALL
  SELECT -1,'QcSampleVehicle','w2_by_name','Second Weight By','STRING',1,1,1,0,'text'
) x
WHERE NOT EXISTS (SELECT 1 FROM entity_field_registry WHERE tenant_id = -1 AND entity_code = x.b AND field_code = x.c);

-- ActionDispatcher needs an implemented=1 action_registry row before it will invoke the handler (same as V251 / V360).
INSERT INTO action_registry (code, display_name, category, params_schema_json, implemented, requires_role, description)
SELECT 'm10.qc-sample-vehicle', 'Save Sample Vehicle', 'workflow',
       '{"type":"object","properties":{},"note":"formData.sampleVehicles (array of {vehicle_no,sku_code,sample_location,invoice_no?,order_id?,sample_qty?,w1_weight_kg?,w2_weight_kg?,remarks?}) is read from the page form, not args. Send w1_weight_kg to take the first weight, w2_weight_kg for the second."}',
       1, NULL,
       'Sample vehicle of the QC oil sample (aria:qc-approval, Tanker Inbound): saves the vehicle no + location, its first weight and its second weight into qc_sample_vehicle. When both weights are in, |W1-W2| is received into stock at the sample location (m5_stock_movement RECEIPT, ref_type QC_SAMPLE) and the row is DONE. Does not change workflow state.'
WHERE NOT EXISTS (SELECT 1 FROM action_registry WHERE code = 'm10.qc-sample-vehicle');
