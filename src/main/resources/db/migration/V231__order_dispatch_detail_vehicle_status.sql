-- Separate from the existing `status` column, which already carries real
-- business meaning (Dispatched / Assigned All / Pending, see V7's list-page
-- filter) — overloading it with active/inactive would break whatever already
-- reads those values. vehicle_status is new, dedicated to the
-- import-sets-active / gate-out-sets-inactive lifecycle
-- (OrderDispatchDetailImportController.insertRow / gateOut).
ALTER TABLE m5_order_dispatch_detail
  ADD COLUMN IF NOT EXISTS vehicle_status VARCHAR(32) NULL AFTER status;
