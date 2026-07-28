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
}
