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
}
