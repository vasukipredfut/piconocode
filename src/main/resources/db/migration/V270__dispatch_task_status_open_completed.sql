-- Conveyor Dispatch Task: rename the actively-used status values from
-- IN_PROGRESS/COMPLETED to Open/Completed (business-facing terminology).
-- PENDING and CANCELLED are left as-is -- PENDING is only ever the DB
-- default before DispatchTaskService.start() moves a task on, and CANCELLED
-- is a reserved value nothing in application code currently sets or checks;
-- neither needed renaming for this change.
--
-- dispatch_task uses COLLATE=utf8mb4_uca1400_ai_ci (accent+CASE-insensitive).
-- A single ENUM definition can't declare both 'COMPLETED' and 'Completed' at
-- once -- MariaDB rejects it with "Column 'status' has duplicated value
-- 'COMPLETED' in ENUM", since the two are the same value under this
-- collation. The widen/narrow approach that works for a case-sensitive
-- collation (see V210/V269 for other columns) does NOT work here for the
-- COMPLETED/Completed pair specifically. Route the rename through a plain
-- VARCHAR shadow column instead -- reading/writing string values never hits
-- the enum-definition-uniqueness check, only DEFINING an enum does.
ALTER TABLE dispatch_task ADD COLUMN status_new VARCHAR(16) DEFAULT NULL AFTER status;

UPDATE dispatch_task SET status_new = CASE status
  WHEN 'IN_PROGRESS' THEN 'Open'
  WHEN 'COMPLETED' THEN 'Completed'
  ELSE status
END;

ALTER TABLE dispatch_task DROP COLUMN status;
ALTER TABLE dispatch_task CHANGE COLUMN status_new status
  ENUM('PENDING','Open','Completed','CANCELLED') NOT NULL DEFAULT 'PENDING';
