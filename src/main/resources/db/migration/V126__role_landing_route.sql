-- V126 — DB-driven post-login landing route per role.
--
-- The frontend PersonaRouter used to hardcode a role-id → route map in JS
-- (an anti-pattern: per-deployment routing baked into shipped code). Make it
-- DATA: each role carries its landing route; AuthController returns it in the
-- signin response as `landingRoute`; PersonaRouter just reads that. Any tenant
-- can repoint a role's landing page by editing this column — no code change.
--
-- Roles with a NULL landing_route fall back to /panel/home (the EARMS home
-- dashboard) on the frontend, which is the right default for admin/owner/
-- manager personas that navigate the full sidebar rather than one station.

ALTER TABLE role ADD COLUMN IF NOT EXISTS landing_route VARCHAR(128) NULL;

-- Operator personas land on their station console; everyone else falls back.
UPDATE role SET landing_route = '/m10/front-gate'   WHERE id = 1001; -- FrontGate
UPDATE role SET landing_route = '/m10/parking'       WHERE id = 1002; -- ParkingSecurity
UPDATE role SET landing_route = '/m10/dispatch'      WHERE id = 1003; -- Dispatch
UPDATE role SET landing_route = '/m10/qc'            WHERE id = 1004; -- QC
UPDATE role SET landing_route = '/m10/manager-inbox' WHERE id = 1007; -- Manager
