package com.aria.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * A row in a persona's work-queue. The {@link #submittedData} bag is the
 * accumulated form data from all prior page-submissions on this instance;
 * the front-end can pluck out vehicleNo / driverName etc to render on the
 * queue card.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowQueueItem {
    private Long id;
    private String workflowCode;
    private String currentState;
    private String previousState;
    private String status;
    private String businessRef;
    private String assignedRole;
    private Instant updatedAt;
    private Map<String, Object> submittedData;

    /** Looked up from gate_pass by workflow_instance_id (see WorkflowProjectionListener,
     *  which keeps gate_pass in sync on this same key) — not every workflow has a
     *  gate_pass row, so null when it doesn't. */
    private String gpNumber;

    /**
     * Parking bay is not a workflow state for every flow (vehicles can move
     * in/out at any point, not on a fixed sequential gate) — this overlay is
     * resolved from m0_audit_log (latest to_state LIKE 'PARKING%' row for the
     * gate pass), separate from {@link #currentState}/{@link #previousState}
     * which stay FSM-only. Null/false when the flow has no parking states at
     * all, or the gate pass has never had one logged.
     */
    private boolean inParkingBay;
    private String parkingState;
    private Instant parkingSince;
    private Long parkingLoggedBy;

    /**
     * True when {@code m5_order_dispatch_detail} has an "active" row for this
     * vehicle's vehicle_no — i.e. the Planning Dispatch Order Sheet has
     * already been uploaded for it (see OrderDispatchDetailImportController,
     * whose gate-out javadoc notes vehicle_no as the one field both sides
     * share). False when the vehicle number is unknown or no such row exists.
     */
    private boolean orderSheetUploaded;

    /**
     * Why the operator picked this instance out of order in its queue (see
     * V232/V233) — looked up from {@code gate_pass.queue_reason} by
     * workflow_instance_id, same source as {@link #gpNumber}. Null when
     * there's no gate_pass row yet, or no reason was ever recorded.
     */
    private String queueReason;

    /** From {@code gate_pass.vehicle_category} / {@code gate_pass.loading_type} (same lookup as {@link #queueReason}); null when no gate_pass row or never set. */
    private String vehicleCategory;
    private String loadingType;
}
