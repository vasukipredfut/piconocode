-- =====================================================================
-- V7_1 — Pico master data seed
-- =====================================================================
-- Real-looking master rows for Pico Power, plant=Tuticorin (PP-TUT).
-- All INSERTs use INSERT IGNORE so re-running is a no-op.
-- Sizes: 4 gates, 8 lanes, 24 parking bays, 30 tanks, 50 vehicles,
-- 20 drivers, 2 weighbridges.
-- =====================================================================

-- ----- Gates ----------------------------------------------------------
INSERT IGNORE INTO dispatch_gate (tenant_id, code, name, gate_type, scanner_workerless, active) VALUES
  (1, 'G-MAIN',   'Main Front Gate',     'FRONT',   1, 1),
  (1, 'G-RM',     'RM Oil Inbound Gate', 'FRONT',   1, 1),
  (1, 'G-FG',     'FG Dispatch Gate',    'EXIT',    1, 1),
  (1, 'G-WORKER', 'Worker / Visitor',    'FRONT',   1, 1);

-- ----- Lanes (2 per gate) --------------------------------------------
INSERT IGNORE INTO dispatch_lane (tenant_id, gate_code, code, direction, active) VALUES
  (1, 'G-MAIN',   'L-MAIN-IN',   'IN',  1),
  (1, 'G-MAIN',   'L-MAIN-OUT',  'OUT', 1),
  (1, 'G-RM',     'L-RM-IN',     'IN',  1),
  (1, 'G-RM',     'L-RM-OUT',    'OUT', 1),
  (1, 'G-FG',     'L-FG-IN',     'IN',  1),
  (1, 'G-FG',     'L-FG-OUT',    'OUT', 1),
  (1, 'G-WORKER', 'L-WK-IN',     'IN',  1),
  (1, 'G-WORKER', 'L-WK-OUT',    'OUT', 1);

-- ----- Parking bays (6 per gate = 24 total) --------------------------
INSERT IGNORE INTO dispatch_parking_bay (tenant_id, gate_code, code, capacity, active) VALUES
  (1, 'G-MAIN',   'PB-MAIN-01', 4, 1), (1, 'G-MAIN', 'PB-MAIN-02', 4, 1), (1, 'G-MAIN', 'PB-MAIN-03', 4, 1),
  (1, 'G-MAIN',   'PB-MAIN-04', 4, 1), (1, 'G-MAIN', 'PB-MAIN-05', 4, 1), (1, 'G-MAIN', 'PB-MAIN-06', 4, 1),
  (1, 'G-RM',     'PB-RM-01',   4, 1), (1, 'G-RM',   'PB-RM-02',   4, 1), (1, 'G-RM',   'PB-RM-03',   4, 1),
  (1, 'G-RM',     'PB-RM-04',   4, 1), (1, 'G-RM',   'PB-RM-05',   4, 1), (1, 'G-RM',   'PB-RM-06',   4, 1),
  (1, 'G-FG',     'PB-FG-01',   4, 1), (1, 'G-FG',   'PB-FG-02',   4, 1), (1, 'G-FG',   'PB-FG-03',   4, 1),
  (1, 'G-FG',     'PB-FG-04',   4, 1), (1, 'G-FG',   'PB-FG-05',   4, 1), (1, 'G-FG',   'PB-FG-06',   4, 1),
  (1, 'G-WORKER', 'PB-WK-01',   2, 1), (1, 'G-WORKER','PB-WK-02',  2, 1), (1, 'G-WORKER','PB-WK-03',  2, 1),
  (1, 'G-WORKER', 'PB-WK-04',   2, 1), (1, 'G-WORKER','PB-WK-05',  2, 1), (1, 'G-WORKER','PB-WK-06',  2, 1);

-- ----- Tanks: 15 sunflower (T-OIL-01..15) + 15 groundnut (16..30) -----
INSERT IGNORE INTO dispatch_tank_location (tenant_id, code, tank_capacity_kl, current_oil_grade, min_headspace_kl, batch_density) VALUES
  (1, 'T-OIL-01', 80, 'SUNFLOWER', 8, 0.9160),
  (1, 'T-OIL-02', 80, 'SUNFLOWER', 8, 0.9160),
  (1, 'T-OIL-03', 80, 'SUNFLOWER', 8, 0.9160),
  (1, 'T-OIL-04', 80, 'SUNFLOWER', 8, 0.9160),
  (1, 'T-OIL-05', 80, 'SUNFLOWER', 8, 0.9160),
  (1, 'T-OIL-06', 60, 'SUNFLOWER', 6, 0.9160),
  (1, 'T-OIL-07', 60, 'SUNFLOWER', 6, 0.9160),
  (1, 'T-OIL-08', 60, 'SUNFLOWER', 6, 0.9160),
  (1, 'T-OIL-09', 60, 'EMPTY',     6, NULL),
  (1, 'T-OIL-10', 60, 'EMPTY',     6, NULL),
  (1, 'T-OIL-11', 50, 'SUNFLOWER', 5, 0.9160),
  (1, 'T-OIL-12', 50, 'SUNFLOWER', 5, 0.9160),
  (1, 'T-OIL-13', 50, 'SUNFLOWER', 5, 0.9160),
  (1, 'T-OIL-14', 50, 'SUNFLOWER', 5, 0.9160),
  (1, 'T-OIL-15', 50, 'EMPTY',     5, NULL),
  (1, 'T-OIL-16', 80, 'GROUNDNUT', 8, 0.9170),
  (1, 'T-OIL-17', 80, 'GROUNDNUT', 8, 0.9170),
  (1, 'T-OIL-18', 80, 'GROUNDNUT', 8, 0.9170),
  (1, 'T-OIL-19', 80, 'GROUNDNUT', 8, 0.9170),
  (1, 'T-OIL-20', 80, 'GROUNDNUT', 8, 0.9170),
  (1, 'T-OIL-21', 60, 'GROUNDNUT', 6, 0.9170),
  (1, 'T-OIL-22', 60, 'GROUNDNUT', 6, 0.9170),
  (1, 'T-OIL-23', 60, 'GROUNDNUT', 6, 0.9170),
  (1, 'T-OIL-24', 60, 'GROUNDNUT', 6, 0.9170),
  (1, 'T-OIL-25', 60, 'EMPTY',     6, NULL),
  (1, 'T-OIL-26', 50, 'GROUNDNUT', 5, 0.9170),
  (1, 'T-OIL-27', 50, 'GROUNDNUT', 5, 0.9170),
  (1, 'T-OIL-28', 50, 'GROUNDNUT', 5, 0.9170),
  (1, 'T-OIL-29', 50, 'GROUNDNUT', 5, 0.9170),
  (1, 'T-OIL-30', 50, 'EMPTY',     5, NULL);

-- ----- Weighbridges --------------------------------------------------
INSERT IGNORE INTO dispatch_weighbridge (tenant_id, code, capacity_kg, protocol, frame, device, tolerance_kg) VALUES
  (1, 'WB-1', 60000, 'RS232', 'MT_IND', 'M9.CHECKWEIGHER_RS232', 20),
  (1, 'WB-2', 80000, 'TCP',   'MT_IND', 'M9.CHECKWEIGHER_TCP',   20);

-- ----- Vehicles (50: 25 owned + 25 hired, mix of TN-46 and TN-30) -----
INSERT IGNORE INTO dispatch_vehicle (tenant_id, registration_no, vehicle_type, tanker_compartments, tare_weight_kg, capacity_kg, ownership, transporter_name, rc_expiry, insurance_expiry, puc_expiry, tank_cleaning_cert_no, tank_cleaning_cert_expiry) VALUES
  (1, 'TN-46-CJ-0001', 'TANKER',            2, 8500.0,  16000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-001', '2026-12-31'),
  (1, 'TN-46-CJ-0002', 'TANKER',            2, 8600.0,  16000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-002', '2026-12-31'),
  (1, 'TN-46-CJ-0003', 'TANKER',            3, 9100.0,  20000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-003', '2026-12-31'),
  (1, 'TN-46-CJ-0004', 'TANKER',            3, 9200.0,  20000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-004', '2026-12-31'),
  (1, 'TN-46-CJ-0005', 'TANKER',            4, 9800.0,  25000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-005', '2026-12-31'),
  (1, 'TN-46-CJ-0006', 'TANKER',            4, 9900.0,  25000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-006', '2026-12-31'),
  (1, 'TN-46-CJ-0007', 'TANKER',            2, 8500.0,  15000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-007', '2026-12-31'),
  (1, 'TN-46-CJ-0008', 'TANKER',            2, 8600.0,  15000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-008', '2026-12-31'),
  (1, 'TN-46-CJ-0009', 'FLATBED_CONTAINER', NULL, 7800.0, 12000.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0010', 'FLATBED_CONTAINER', NULL, 7900.0, 12000.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0011', 'BOX_TRUCK',         NULL, 6500.0,  8000.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0012', 'BOX_TRUCK',         NULL, 6500.0,  8000.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0013', 'TIPPER',            NULL, 9000.0, 18000.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0014', 'TIPPER',            NULL, 9100.0, 18000.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0015', 'LCV',               NULL, 3200.0,  5000.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0016', 'TANKER',            2, 8500.0,  16000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-016', '2026-12-31'),
  (1, 'TN-46-CJ-0017', 'TANKER',            2, 8600.0,  16000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-017', '2026-12-31'),
  (1, 'TN-46-CJ-0018', 'TANKER',            3, 9100.0,  20000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-018', '2026-12-31'),
  (1, 'TN-46-CJ-0019', 'TANKER',            3, 9200.0,  20000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-019', '2026-12-31'),
  (1, 'TN-46-CJ-0020', 'TANKER',            4, 9800.0,  25000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-020', '2026-12-31'),
  (1, 'TN-46-CJ-0021', 'TANKER',            2, 8550.0,  16000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-021', '2026-12-31'),
  (1, 'TN-46-CJ-0022', 'TANKER',            2, 8650.0,  16000.0, 'OWNED', 'Pico Logistics',     '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-022', '2026-12-31'),
  (1, 'TN-46-CJ-0023', 'BOX_TRUCK',         NULL, 6400.0,  7500.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0024', 'BOX_TRUCK',         NULL, 6450.0,  7500.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-46-CJ-0025', 'LCV',               NULL, 3300.0,  5000.0, 'OWNED', 'Pico Logistics',   '2027-03-31', '2026-09-30', '2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1001', 'TANKER',            2, 8400.0,  16000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-101', '2026-12-31'),
  (1, 'TN-30-AB-1002', 'TANKER',            2, 8500.0,  16000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-102', '2026-12-31'),
  (1, 'TN-30-AB-1003', 'TANKER',            3, 9000.0,  20000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-103', '2026-12-31'),
  (1, 'TN-30-AB-1004', 'TANKER',            3, 9100.0,  20000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-104', '2026-12-31'),
  (1, 'TN-30-AB-1005', 'TANKER',            4, 9700.0,  25000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-105', '2026-12-31'),
  (1, 'TN-30-AB-1006', 'TANKER',            4, 9750.0,  25000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-106', '2026-12-31'),
  (1, 'TN-30-AB-1007', 'FLATBED_CONTAINER', NULL, 7700.0, 12000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1008', 'FLATBED_CONTAINER', NULL, 7800.0, 12000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1009', 'BOX_TRUCK',         NULL, 6300.0,  8000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1010', 'BOX_TRUCK',         NULL, 6350.0,  8000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1011', 'LCV',               NULL, 3100.0,  5000.0, 'HIRED', 'Local Carriers',     '2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1012', 'LCV',               NULL, 3150.0,  5000.0, 'HIRED', 'Local Carriers',     '2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1013', 'TIPPER',            NULL, 9000.0, 18000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1014', 'TIPPER',            NULL, 9050.0, 18000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1015', 'TANKER',            2, 8400.0,  16000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-115', '2026-12-31'),
  (1, 'TN-30-AB-1016', 'TANKER',            2, 8500.0,  16000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-116', '2026-12-31'),
  (1, 'TN-30-AB-1017', 'TANKER',            3, 9000.0,  20000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-117', '2026-12-31'),
  (1, 'TN-30-AB-1018', 'TANKER',            3, 9100.0,  20000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-118', '2026-12-31'),
  (1, 'TN-30-AB-1019', 'TANKER',            4, 9700.0,  25000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-119', '2026-12-31'),
  (1, 'TN-30-AB-1020', 'TANKER',            4, 9750.0,  25000.0, 'HIRED', 'Shree Transport',    '2027-03-31', '2026-09-30', '2026-12-31', 'TCC-2025-120', '2026-12-31'),
  (1, 'TN-30-AB-1021', 'BOX_TRUCK',         NULL, 6300.0,  8000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1022', 'BOX_TRUCK',         NULL, 6350.0,  8000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1023', 'LCV',               NULL, 3100.0,  5000.0, 'HIRED', 'Local Carriers',     '2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1024', 'LCV',               NULL, 3150.0,  5000.0, 'HIRED', 'Local Carriers',     '2027-03-31','2026-09-30','2026-12-31', NULL, NULL),
  (1, 'TN-30-AB-1025', 'TIPPER',            NULL, 9000.0, 18000.0, 'HIRED', 'Velmurugan Roadways','2027-03-31','2026-09-30','2026-12-31', NULL, NULL);

-- ----- Drivers (20, each unique phone) --------------------------------
INSERT IGNORE INTO dispatch_driver (tenant_id, name, dl_number, dl_expiry, primary_mobile, alt_mobile, preferred_language, aadhaar_last4) VALUES
  (1, 'Suresh Kumar',       'TN3020201234567', '2028-04-30', '+919876543201', '+919876540001', 'ta', '1234'),
  (1, 'Murugan Selvam',     'TN3020201234568', '2028-04-30', '+919876543202', NULL, 'ta', '1235'),
  (1, 'Karthik Raja',       'TN3020201234569', '2028-04-30', '+919876543203', NULL, 'ta', '1236'),
  (1, 'Anbu Selvan',        'TN3020201234570', '2028-04-30', '+919876543204', NULL, 'ta', '1237'),
  (1, 'Velmurugan R',       'TN3020201234571', '2028-04-30', '+919876543205', NULL, 'ta', '1238'),
  (1, 'Ramesh Babu',        'TN3020201234572', '2028-04-30', '+919876543206', NULL, 'ta', '1239'),
  (1, 'Senthil Kumar',      'TN3020201234573', '2028-04-30', '+919876543207', NULL, 'ta', '1240'),
  (1, 'Prakash V',          'TN3020201234574', '2028-04-30', '+919876543208', NULL, 'ta', '1241'),
  (1, 'Mani Kannan',        'TN3020201234575', '2028-04-30', '+919876543209', NULL, 'ta', '1242'),
  (1, 'Saravanan M',        'TN3020201234576', '2028-04-30', '+919876543210', NULL, 'ta', '1243'),
  (1, 'Dinesh Kumar',       'TN4620201234001', '2028-04-30', '+919876543211', NULL, 'ta', '1244'),
  (1, 'Vijay Anand',        'TN4620201234002', '2028-04-30', '+919876543212', NULL, 'ta', '1245'),
  (1, 'Manikandan G',       'TN4620201234003', '2028-04-30', '+919876543213', NULL, 'ta', '1246'),
  (1, 'Arun Prakash',       'TN4620201234004', '2028-04-30', '+919876543214', NULL, 'ta', '1247'),
  (1, 'Gopinath S',         'TN4620201234005', '2028-04-30', '+919876543215', NULL, 'ta', '1248'),
  (1, 'Balaji R',           'TN4620201234006', '2028-04-30', '+919876543216', NULL, 'ta', '1249'),
  (1, 'Hari Krishnan',      'TN4620201234007', '2028-04-30', '+919876543217', NULL, 'ta', '1250'),
  (1, 'Jayakumar P',        'TN4620201234008', '2028-04-30', '+919876543218', NULL, 'ta', '1251'),
  (1, 'Kannan M',           'TN4620201234009', '2028-04-30', '+919876543219', NULL, 'ta', '1252'),
  (1, 'Loganathan T',       'TN4620201234010', '2028-04-30', '+919876543220', NULL, 'ta', '1253');
