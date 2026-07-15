-- =====================================================================
-- V7_2 — Pico role permissions
-- =====================================================================
-- Seeds the 9 Pico personas into the new `tenant_role` table with their
-- permission bundle (JSON array). The legacy `role` table is kept in sync
-- (insert id=1001-1009 with name=role.code) because employee_details.
-- roles_id FK targets it.
--
-- Idempotent: INSERT IGNORE on tenant_role (uq), ON DUPLICATE KEY on role.
-- =====================================================================

-- ----- tenant_role (the M0 source-of-truth) ---------------------------
INSERT IGNORE INTO tenant_role (tenant_id, code, label, permissions_json) VALUES
  (1, 'FrontGate',           'Front Gate Security',
      JSON_ARRAY('DispatchEvent:create','DispatchEvent:read.queue','DispatchEvent:transition:frontgate_verify','DispatchEvent:transition:frontgate_reject','DispatchEvent:transition:exit','Vehicle:read','Driver:read')),
  (1, 'ParkingSecurity',     'Parking Security',
      JSON_ARRAY('DispatchEvent:read.queue','DispatchEvent:transition:assign_parking','DispatchEvent:update.parking')),
  (1, 'Dispatch',            'Dispatch Front Office',
      JSON_ARRAY('DispatchEvent:read.all','DispatchEvent:transition:queue_dispatch','DispatchEvent:transition:draft_stn','DispatchEvent:transition:draft_dc','DispatchEvent:transition:draft_cn','DispatchEvent:transition:reconcile','DispatchEvent:transition:verify_return')),
  (1, 'QC',                  'Quality Control',
      JSON_ARRAY('DispatchEvent:read.queue','DispatchEvent:transition:qc_draw_sample','DispatchEvent:transition:qc_pending','DispatchEvent:transition:qc_pass','DispatchEvent:transition:qc_fail','DispatchEvent:transition:pre_qc')),
  (1, 'WeighmentOfficer',    'Weighment Officer',
      JSON_ARRAY('DispatchEvent:read.queue','DispatchEvent:transition:weigh_loaded','DispatchEvent:transition:weigh_empty','DispatchEvent:transition:weigh_reverify')),
  (1, 'UnloadingSupervisor', 'Unloading/Loading Supervisor',
      JSON_ARRAY('DispatchEvent:read.queue','DispatchEvent:transition:assign_tank','DispatchEvent:transition:assign_bay','DispatchEvent:transition:start_unload','DispatchEvent:transition:start_load','DispatchEvent:transition:close_unload','DispatchEvent:transition:close_pick','DispatchEvent:transition:capture_grn','TankLocation:read','TankLocation:update.headspace')),
  (1, 'Manager',             'Plant Manager',
      JSON_ARRAY('*:read','DispatchEvent:transition:mgr_approve','DispatchEvent:transition:mgr_reject','Vendor:approve','Item:approve')),
  (1, 'Plantowner',          'Plant Owner / Promoter',
      JSON_ARRAY('*:read','Dashboard:owner','ConfigurableViews:owner')),
  (1, 'TenantAdmin',         'Tenant IT champ',
      JSON_ARRAY('M0:*','M9:device.*','M0:formLayouts.*','M0:savedViews.*'));

-- ----- legacy `role` table sync (only when the table exists) ---------
-- The schema initialiser guards on tableExists('role') before running this
-- migration; safe to assume it's present here. ON DUPLICATE keeps V4_1
-- rows aligned (V4_1 already wrote 1001-1004; we extend with 1005-1009).
INSERT INTO role (id, name) VALUES
  (1001, 'FrontGate'),
  (1002, 'ParkingSecurity'),
  (1003, 'Dispatch'),
  (1004, 'QC'),
  (1005, 'WeighmentOfficer'),
  (1006, 'UnloadingSupervisor'),
  (1007, 'Manager'),
  (1008, 'Plantowner'),
  (1009, 'TenantAdmin')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ----- 5 more persona users to back the additional roles -------------
-- Same bcrypt password hash as V4_1 (Test@1234).
INSERT INTO employee_details (id, username, password, active, roles_id, tenant_id, employeename, status, branch_id) VALUES
  (2005, '2005', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1005, 1, 'Pico Weighment Officer',  'A', 8),
  (2006, '2006', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1006, 1, 'Pico Unloading Supervisor','A', 8),
  (2007, '2007', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1007, 1, 'Pico Plant Manager',      'A', 8),
  (2008, '2008', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1008, 1, 'Pico Plant Owner',        'A', 8),
  (2009, '2009', '$2a$10$pivDYxR5RH2IOTau7oWBB.LQC7nf6BrsWcWxfFAXTrBoNnmWqhy9G', 1, 1009, 1, 'Pico Tenant Admin',       'A', 8)
ON DUPLICATE KEY UPDATE
    username = VALUES(username), password = VALUES(password), roles_id = VALUES(roles_id),
    status = VALUES(status), branch_id = VALUES(branch_id), active = VALUES(active);
