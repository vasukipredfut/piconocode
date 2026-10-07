-- aria:qc-preload-approval's Tank Allocation grid has per-SKU "Diff" and "Diff
-- Reason" inputs that were only ever kept inside workflow_instance.submitted_data_json.
-- QcPreloadTankAllocationListener now persists them onto every loading_progress_detail_bay
-- row it seeds for that SKU (one SKU may span several tank rows; each carries the same diff).
--
-- Idempotent -- safe to (re)run on any database.

ALTER TABLE loading_progress_detail_bay
  ADD COLUMN IF NOT EXISTS diff_qty DECIMAL(12,3) NULL,
  ADD COLUMN IF NOT EXISTS diff_reason VARCHAR(255) NULL;
