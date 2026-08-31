-- Conveyor Dispatch Task: rename the actively-used status values from
-- IN_PROGRESS/COMPLETED to Open/Completed (business-facing terminology).
-- PENDING and CANCELLED are left as-is -- PENDING is only ever the DB
-- default before DispatchTaskService.start() moves a task on, and CANCELLED
-- is a reserved value nothing in application code currently sets or checks;
-- neither needed renaming for this change.
--
-- Two-step ALTER (widen, migrate data, narrow) because MariaDB rejects
-- existing rows whose ENUM value isn't in the new allowed set -- a
-- single-step swap from IN_PROGRESS/COMPLETED straight to Open/Completed
-- would either error or silently blank out every existing row's status.
ALTER TABLE dispatch_task
  MODIFY COLUMN status ENUM('PENDING','IN_PROGRESS','COMPLETED','CANCELLED','Open','Completed')
  NOT NULL DEFAULT 'PENDING';

UPDATE dispatch_task SET status = 'Open' WHERE status = 'IN_PROGRESS';
UPDATE dispatch_task SET status = 'Completed' WHERE status = 'COMPLETED';

ALTER TABLE dispatch_task
  MODIFY COLUMN status ENUM('PENDING','Open','Completed','CANCELLED')
  NOT NULL DEFAULT 'PENDING';
