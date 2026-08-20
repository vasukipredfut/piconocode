package com.aria.entity;

/**
 * Shared LEFT JOIN + SELECT for reading gate_pass together with the satellite
 * detail tables its columns were split out into over time (V79-V102, V96).
 * Established so a generic gate_pass read never carries duplicate/stale
 * columns once the real data lives on a satellite table — extend this rather
 * than adding a column back onto gate_pass for a new consumer (see V214,
 * which reverted V162's gate_pass duplicate columns in favor of extending
 * this join with unload_reconciliation_detail + a computed qty_difference_kg).
 *
 * <p>Used by {@link GenericEntityController} (getRow/getRowByField) and by
 * {@code com.aria.print.PrintController} (doRender/send) for any print
 * template whose target_table is gate_pass.
 */
public final class GatePassSatelliteJoin {

    private GatePassSatelliteJoin() { }

    public static final String JOIN =
            " LEFT JOIN qc_preload_detail qpd ON qpd.workflow_instance_id = gp.workflow_instance_id AND qpd.tenant_id = gp.tenant_id" +
            // Same id-vs-name mismatch as unloading_supervisor_id below:
            // aria:qc-preload-approval's "Loading Supervisor" field is a
            // select bound to Employee with valueField:"id" (V43), so
            // qc_preload_detail.loading_supervisor holds the numeric id.
            " LEFT JOIN employee_details qpdemp ON qpdemp.id = qpd.loading_supervisor" +
            " LEFT JOIN loading_allocation_detail lad ON lad.workflow_instance_id = gp.workflow_instance_id AND lad.tenant_id = gp.tenant_id" +
            // Same mismatch again: aria:loading-allocation's "Supervisor Name"
            // field is also a select with valueField:"id" (V45), so
            // loading_allocation_detail.supervisor_name — despite its name —
            // holds the numeric id, not the employee's name.
            " LEFT JOIN employee_details ladeemp ON ladeemp.id = lad.supervisor_name" +
            " LEFT JOIN weighment_capture_detail gpw ON gpw.workflow_instance_id = gp.workflow_instance_id AND gpw.tenant_id = gp.tenant_id" +
            " LEFT JOIN sealing_process_detail spd ON spd.workflow_instance_id = gp.workflow_instance_id AND spd.tenant_id = gp.tenant_id" +
            " LEFT JOIN pmt_weight_diff_detail pwd ON pwd.workflow_instance_id = gp.workflow_instance_id AND pwd.tenant_id = gp.tenant_id" +
            " LEFT JOIN pmt_camera_verification_detail pcvd ON pcvd.workflow_instance_id = gp.workflow_instance_id AND pcvd.tenant_id = gp.tenant_id" +
            " LEFT JOIN dispatch_vehicle_detail dvd ON dvd.workflow_instance_id = gp.workflow_instance_id AND dvd.tenant_id = gp.tenant_id" +
            " LEFT JOIN loading_progress_detail lpd ON lpd.workflow_instance_id = gp.workflow_instance_id AND lpd.tenant_id = gp.tenant_id" +
            " LEFT JOIN gate_pass_closure gpc ON gpc.workflow_instance_id = gp.workflow_instance_id AND gpc.tenant_id = gp.tenant_id" +
            " LEFT JOIN billing_entry be ON be.workflow_instance_id = gp.workflow_instance_id AND be.tenant_id = gp.tenant_id" +
            " LEFT JOIN qc_unloading_approval qua ON qua.workflow_instance_id = gp.workflow_instance_id AND qua.tenant_id = gp.tenant_id" +
            // qc_unloading_approval.unloading_supervisor_id is misleadingly
            // named: the aria:qc-approval field labeled "Unloading Supervisor
            // Name" is a select bound to Employee with valueField:"id", so it
            // actually stores the employee's numeric id, not their name (same
            // resolve-by-join already done for aria:dispatch-vehicle-details,
            // see V206). Joined here so plain GatePass reads get the name too.
            " LEFT JOIN employee_details uemp ON uemp.id = qua.unloading_supervisor_id" +
            " LEFT JOIN unload_reconciliation_detail urd ON urd.workflow_instance_id = gp.workflow_instance_id AND urd.tenant_id = gp.tenant_id" +
            // manager_qty_approval_detail / manager_weight_approval_detail are
            // populated by PmtSharedReasonListener (bespoke Java, not this
            // generic projection) — joined here purely so a plain GatePass
            // read also surfaces their queue_reason (see SELECT below).
            " LEFT JOIN manager_qty_approval_detail mqad ON mqad.workflow_instance_id = gp.workflow_instance_id AND mqad.tenant_id = gp.tenant_id" +
            " LEFT JOIN manager_weight_approval_detail mwad ON mwad.workflow_instance_id = gp.workflow_instance_id AND mwad.tenant_id = gp.tenant_id";

    // Each satellite's own out-of-order-selection reason (see V232/V233,
    // PmtSharedReasonListener), aliased per table — gate_pass's own bare
    // "queue_reason" (from gp.*) stays the plain top-level field; every
    // satellite gets its own distinct alias so a vehicle's full trail of
    // reasons across every step it passed through is visible in one read,
    // not just the last one to overwrite a shared column name.
    private static final String QUEUE_REASON_SELECT =
            "qpd.queue_reason AS qc_preload_queue_reason, " +
            "lad.queue_reason AS loading_allocation_queue_reason, " +
            "gpw.queue_reason AS weighment_capture_queue_reason, " +
            "spd.queue_reason AS sealing_process_queue_reason, " +
            "pwd.queue_reason AS pmt_weight_diff_queue_reason, " +
            "pcvd.queue_reason AS pmt_camera_verification_queue_reason, " +
            "dvd.queue_reason AS dispatch_vehicle_queue_reason, " +
            "be.queue_reason AS billing_entry_queue_reason, " +
            "qua.queue_reason AS qc_unloading_approval_queue_reason, " +
            "mqad.queue_reason AS manager_qty_approval_queue_reason, " +
            "mwad.queue_reason AS manager_weight_approval_queue_reason";

    public static final String SELECT =
            "gp.*, " + QUEUE_REASON_SELECT + ", " +
            "qpd.loading_supervisor, qpd.loading_tank_location, qpd.qc_reject_reason, " +
            "lad.supervisor_name, lad.loading_bay, lad.reference_no, lad.customer_name, lad.customer_route, lad.ordered_cases, lad.ordered_qty, " +
            // Single unified key for "who's the loading supervisor" — QC_PRELOAD_APPROVED
            // (qpd.loading_supervisor) and the next step LOADING_ALLOCATED (lad.supervisor_name)
            // are the same conceptual field captured twice in sequence (see M10.PackLoading
            // workflow_definition transitions); the later step is the more final assignment,
            // so it wins when both are set.
            "COALESCE(ladeemp.employeename, qpdemp.employeename) AS loading_supervisor_name, " +
            "gpw.w1_weight_kg, gpw.w2_weight_kg, gpw.w3_weight_kg, gpw.weight_officer, gpw.weight_entry_at, " +
            "spd.sealing_no, spd.sealing_supervisor, spd.seal_lock_status, spd.sealing_status, spd.sealing_started_at, spd.sealing_finished_at, " +
            "pwd.pmt_variance_reason, pwd.expected_weight_kg, pwd.loaded_weight_kg, pwd.pmt_variance_pct, pwd.pmt_within_tolerance, " +
            "pcvd.camera_photo, pcvd.issue_identified, pcvd.camera_variance_reason, " +
            "dvd.broker_name, dvd.broker_number, dvd.invoice_qty, dvd.oil_rm_item, dvd.transport_name, " +
            "dvd.po_numbers, dvd.po_attachment, dvd.party_name, " +
            "dvd.invoice_return_count, dvd.box_weight_kg, " +
            "lpd.route_id, " +
            "gpc.seal_verified, gpc.invoice_count_verified, gpc.qc_clearance, gpc.weighment_verified, " +
            "gpc.order_recon_done, gpc.within_tolerance, gpc.closure_note, gpc.closed_at AS closure_closed_at, " +
            "be.invoice_numbers, be.invoice_number_count, be.checking_officer, " +
            "CASE WHEN be.invoice_numbers IS NULL OR be.invoice_numbers = '[]' THEN NULL " +
            "ELSE REPLACE(REPLACE(REPLACE(be.invoice_numbers, '\",\"', ', '), '[\"', ''), '\"]', '') END AS invoice_no, " +
            "qua.item_category, qua.sample_jar_id, qua.qc_officer_id, " +
            "qua.density_kgl, qua.ffa_pct, qua.colour_rating, qua.temperature_c, " +
            "qua.print_quality, qua.dimensional_check, qua.bottle_integrity, qua.carton_strength, " +
            "qua.seal_intact, qua.label_match, qua.batch_expiry_check, " +
            "qua.sample_photo, qua.seal_unlock_no, qua.qc_notes, " +
            "qua.unloading_supervisor_id, uemp.employeename AS unloading_supervisor_name, " +
            "qua.oil_sample_recovered, " +
            // M10.PackUnload's aria:qc-approval routes unloading_location into
            // qc_preload_detail instead of qc_unloading_approval (see V234) —
            // COALESCE so a plain GatePass read surfaces it either way.
            "COALESCE(qua.unloading_location, qpd.unloading_location) AS unloading_location, " +
            "qua.status AS qc_decision_status, qua.qc_reject_reason AS qc_approval_reject_reason, " +
            "urd.grn_status, urd.grn_number, " +
            "(dvd.invoice_qty - (gpw.w1_weight_kg - gpw.w2_weight_kg)) AS qty_difference_kg";
}
