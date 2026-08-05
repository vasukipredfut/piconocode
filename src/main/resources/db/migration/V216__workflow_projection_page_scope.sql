-- WorkflowProjectionListener currently projects into EVERY target_table
-- configured for a workflow_code on EVERY transition of that workflow,
-- regardless of which page actually triggered the transition -- so a gate
-- pass barely created (DRAFT->GATE_PASS_CREATED) already gets placeholder
-- rows inserted into qc_preload_detail, loading_allocation_detail,
-- weighment_capture_detail, sealing_process_detail, billing_entry,
-- gate_pass_closure etc, long before those steps are ever reached.
--
-- page_code scopes a projection row to the one page (or comma-separated
-- few, for tables spanning a short multi-page sub-phase) that legitimately
-- feeds it. NULL (the default, left as-is for every workflow except this
-- pilot) means "always applies" -- the existing behavior, unchanged for
-- every workflow_code other than M10.PackLoading below. gate_pass itself
-- stays NULL even for M10.PackLoading: it's the one genuinely cross-page
-- state tracker every transition should still sync.
--
-- Scoped to M10.PackLoading only, as an initial pilot before this convention
-- is considered for the other 9 M10 workflows sharing this table.

ALTER TABLE m0_workflow_projection
  ADD COLUMN IF NOT EXISTS page_code VARCHAR(512) DEFAULT NULL
  COMMENT 'CSV of page_code(s) this projection row applies to; NULL = every transition (old behavior)';

UPDATE m0_workflow_projection SET page_code = 'aria:dispatch-vehicle-details'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'dispatch_vehicle_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:qc-preload-approval'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'qc_preload_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:loading-allocation'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'loading_allocation_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:weighment-capture'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'weighment_capture_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:sealing-process'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'sealing_process_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:pmt-camera-verification'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'pmt_camera_verification_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:billing-entry'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'billing_entry';

UPDATE m0_workflow_projection SET page_code = 'aria:gate-pass-closure-form'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'gate_pass_closure';

-- Loading-phase tracker (V94/V177): created at WEIGHMENT_1->LOADING_IN_PROGRESS
-- (aria:loading-in-progress), then advanced by Start Bay/Bay Complete
-- (aria:loading-in-progress-entry). The WEIGHMENT_2 re-weigh
-- (aria:weighment-capture) has its own satellite table
-- (weighment_capture_detail) -- loading_progress_detail should not also be
-- touched by that page.
UPDATE m0_workflow_projection SET page_code = 'aria:loading-in-progress,aria:loading-in-progress-entry'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'loading_progress_detail';

-- Field shape (item_category/sample_jar_id/density_kgl/... ) matches
-- aria:qc-approval's capture fields exactly (see GenericEntityController's
-- GatePass satellite join comments on qc_unloading_approval).
UPDATE m0_workflow_projection SET page_code = 'aria:qc-approval'
  WHERE workflow_code = 'M10.PackLoading' AND target_table = 'qc_unloading_approval';

-- gate_pass deliberately left NULL (page_code stays unset) -- every
-- transition, from any page, should keep syncing its status/current_step.

-- M10.TankerInbound -- fifth workflow off the pilot list. Its own closure
-- page is "aria:gate-pass-closure" (no "-form" suffix -- unlike
-- M10.PackLoading/PackUnload, TankerInbound has no separate closure-form
-- step; the single closure page both captures the checklist fields AND
-- decides withinTolerance in one submission).
UPDATE m0_workflow_projection SET page_code = 'aria:dispatch-vehicle-details'
  WHERE workflow_code = 'M10.TankerInbound' AND target_table = 'dispatch_vehicle_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:weighment-capture'
  WHERE workflow_code = 'M10.TankerInbound' AND target_table = 'weighment_capture_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:supervisor-unloading'
  WHERE workflow_code = 'M10.TankerInbound' AND target_table = 'tanker_unloading_detail';

-- aria:qc-approval is TankerInbound's REAL QC step (unlike PackLoading/
-- TankerOutbound, where it never fires) -- see QcApprovalDecisionListener.
UPDATE m0_workflow_projection SET page_code = 'aria:qc-approval'
  WHERE workflow_code = 'M10.TankerInbound' AND target_table = 'qc_unloading_approval';

UPDATE m0_workflow_projection SET page_code = 'aria:gate-pass-closure'
  WHERE workflow_code = 'M10.TankerInbound' AND target_table = 'gate_pass_closure';

-- M10.ReturnStockIn -- sixth workflow off the pilot list. Same page-naming
-- convention as TankerInbound (aria:gate-pass-closure, no "-form" suffix).
UPDATE m0_workflow_projection SET page_code = 'aria:dispatch-vehicle-details'
  WHERE workflow_code = 'M10.ReturnStockIn' AND target_table = 'dispatch_vehicle_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:loading-allocation'
  WHERE workflow_code = 'M10.ReturnStockIn' AND target_table = 'loading_allocation_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:weighment-capture'
  WHERE workflow_code = 'M10.ReturnStockIn' AND target_table = 'weighment_capture_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:supervisor-unloading'
  WHERE workflow_code = 'M10.ReturnStockIn' AND target_table = 'tanker_unloading_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:qc-approval'
  WHERE workflow_code = 'M10.ReturnStockIn' AND target_table = 'qc_unloading_approval';

UPDATE m0_workflow_projection SET page_code = 'aria:gate-pass-closure'
  WHERE workflow_code = 'M10.ReturnStockIn' AND target_table = 'gate_pass_closure';

-- M10.PackUnload -- seventh workflow off the pilot list. gate_pass stays
-- NULL here too (see above).
UPDATE m0_workflow_projection SET page_code = 'aria:dispatch-vehicle-details'
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'dispatch_vehicle_detail';

-- PackUnload's QC decision (V196) uses aria:qc-approval but projects into
-- qc_preload_detail (not qc_unloading_approval -- see the DELETE below),
-- reusing the table name/shape M10.PackLoading's own pre-load QC step uses.
UPDATE m0_workflow_projection SET page_code = 'aria:qc-approval'
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'qc_preload_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:loading-allocation'
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'loading_allocation_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:weighment-capture'
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'weighment_capture_detail';

-- aria:supervisor-unloading (V205's minimal Start/Finish/Supervisor form for
-- PackUnload) -- field_map_json (supervisor_id/start_ts/finish_ts/
-- unload_seal_unlock_no) confirms this is the owning page.
UPDATE m0_workflow_projection SET page_code = 'aria:supervisor-unloading'
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'tanker_unloading_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:reconciliation'
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'unload_reconciliation_detail';

-- Unlike TankerInbound/ReturnStockIn, PackUnload DOES have a separate
-- closure-form step (aria:gate-pass-closure is just the withinTolerance
-- toggle at WEIGHMENT_3; the checklist fields this row's field_map_json
-- expects are submitted on aria:gate-pass-closure-form at READY_FOR_EXIT).
UPDATE m0_workflow_projection SET page_code = 'aria:gate-pass-closure-form'
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'gate_pass_closure';

-- qc_unloading_approval's field_map (sample_jar_id/density_kgl/ffa_pct/
-- bottle_integrity/...) is M10.TankerInbound/M10.ReturnStockIn's own
-- chemistry-lab QC shape -- PackUnload's aria:qc-approval submission never
-- carries those fields (V196 moved PackUnload's own QC data to
-- qc_preload_detail instead). Scoping this row to a page_code that never
-- fires in M10.PackUnload would be equivalent but dishonest about the model;
-- PackUnload genuinely never owns this table, so the row is removed outright.
DELETE FROM m0_workflow_projection
  WHERE workflow_code = 'M10.PackUnload' AND target_table = 'qc_unloading_approval';

-- M10.FGLoading -- same convention, fourth workflow off the pilot list.
-- Structurally identical to M10.PackLoading (same 11 target_tables, same
-- field_map_json), except it has no QC step at all: DISPATCH_VEHICLE_ENTRY
-- goes straight to LOADING_ALLOCATED via aria:loading-allocation, skipping
-- aria:qc-preload-approval entirely -- so qc_preload_detail is dead/never-
-- firing here too (scoped to the page it would own, same inert-but-
-- documented treatment as qc_unloading_approval below).
-- gate_pass stays NULL here too (see above).
UPDATE m0_workflow_projection SET page_code = 'aria:dispatch-vehicle-details'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'dispatch_vehicle_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:qc-preload-approval'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'qc_preload_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:loading-allocation'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'loading_allocation_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:weighment-capture'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'weighment_capture_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:sealing-process'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'sealing_process_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:pmt-camera-verification'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'pmt_camera_verification_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:loading-in-progress,aria:loading-in-progress-entry'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'loading_progress_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:gate-pass-closure-form'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'gate_pass_closure';

UPDATE m0_workflow_projection SET page_code = 'aria:billing-entry'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'billing_entry';

UPDATE m0_workflow_projection SET page_code = 'aria:qc-approval'
  WHERE workflow_code = 'M10.FGLoading' AND target_table = 'qc_unloading_approval';

-- M10.TankerOutbound -- same convention, third workflow off the pilot list.
-- gate_pass stays NULL here too (see above).
UPDATE m0_workflow_projection SET page_code = 'aria:dispatch-vehicle-details'
  WHERE workflow_code = 'M10.TankerOutbound' AND target_table = 'dispatch_vehicle_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:qc-preload-approval'
  WHERE workflow_code = 'M10.TankerOutbound' AND target_table = 'qc_preload_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:weighment-capture'
  WHERE workflow_code = 'M10.TankerOutbound' AND target_table = 'weighment_capture_detail';

-- field_map (route_id/supervisor_name) confirms this is the same
-- loading-phase tracker as M10.PackLoading -- aria:weighment-capture has its
-- own satellite table and should not also touch this one.
UPDATE m0_workflow_projection SET page_code = 'aria:loading-in-progress,aria:loading-in-progress-entry'
  WHERE workflow_code = 'M10.TankerOutbound' AND target_table = 'loading_progress_detail';

UPDATE m0_workflow_projection SET page_code = 'aria:gate-pass-closure-form'
  WHERE workflow_code = 'M10.TankerOutbound' AND target_table = 'gate_pass_closure';

UPDATE m0_workflow_projection SET page_code = 'aria:billing-entry'
  WHERE workflow_code = 'M10.TankerOutbound' AND target_table = 'billing_entry';

-- M10.TankerOutbound's own QC steps are aria:qc-preload-approval (pre-load)
-- and aria:qc-final-out (pre-weighment-2 sample) -- aria:qc-approval (this
-- row's field shape) never fires in this workflow at all, same as
-- M10.PackLoading above.
UPDATE m0_workflow_projection SET page_code = 'aria:qc-approval'
  WHERE workflow_code = 'M10.TankerOutbound' AND target_table = 'qc_unloading_approval';
