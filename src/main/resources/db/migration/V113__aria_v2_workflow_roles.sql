-- V113 — Aria v2: register the 30 workflow assignedRole values missing from the role table
-- The workflow-compliance audit found 30 assignedRole names referenced by workflow
-- states that had no matching row in `role` (Planning, StoreKeeper, HR, EHS, CFO, ...).
-- These are legitimate factory/HR/finance roles; register them so every workflow state's
-- assignedRole resolves. Idempotent (guarded by name). id is AUTO_INCREMENT.

INSERT INTO role (name) SELECT 'Accountant' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Accountant');
INSERT INTO role (name) SELECT 'BillingOfficer' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='BillingOfficer');
INSERT INTO role (name) SELECT 'Buyer' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Buyer');
INSERT INTO role (name) SELECT 'CEO' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='CEO');
INSERT INTO role (name) SELECT 'CFO' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='CFO');
INSERT INTO role (name) SELECT 'Candidate' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Candidate');
INSERT INTO role (name) SELECT 'Driver' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Driver');
INSERT INTO role (name) SELECT 'EHS' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='EHS');
INSERT INTO role (name) SELECT 'Employee' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Employee');
INSERT INTO role (name) SELECT 'GateKeeper' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='GateKeeper');
INSERT INTO role (name) SELECT 'HR' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='HR');
INSERT INTO role (name) SELECT 'HiringManager' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='HiringManager');
INSERT INTO role (name) SELECT 'InterviewPanel' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='InterviewPanel');
INSERT INTO role (name) SELECT 'LoadSupervisor' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='LoadSupervisor');
INSERT INTO role (name) SELECT 'LoadingSupervisor' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='LoadingSupervisor');
INSERT INTO role (name) SELECT 'MaintenanceLead' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='MaintenanceLead');
INSERT INTO role (name) SELECT 'MaintenanceTech' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='MaintenanceTech');
INSERT INTO role (name) SELECT 'PMTTeam' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='PMTTeam');
INSERT INTO role (name) SELECT 'PackingOp' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='PackingOp');
INSERT INTO role (name) SELECT 'Planning' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Planning');
INSERT INTO role (name) SELECT 'PlantManager' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='PlantManager');
INSERT INTO role (name) SELECT 'Purchase' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Purchase');
INSERT INTO role (name) SELECT 'Sales' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Sales');
INSERT INTO role (name) SELECT 'StockOwner' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='StockOwner');
INSERT INTO role (name) SELECT 'StoreKeeper' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='StoreKeeper');
INSERT INTO role (name) SELECT 'Supervisor' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Supervisor');
INSERT INTO role (name) SELECT 'Trainer' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Trainer');
INSERT INTO role (name) SELECT 'UnloadSupervisor' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='UnloadSupervisor');
INSERT INTO role (name) SELECT 'Vendor' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='Vendor');
INSERT INTO role (name) SELECT 'WeighbridgeOp' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM role WHERE name='WeighbridgeOp');
