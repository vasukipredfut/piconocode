package com.aria.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Result of a single page-submission against a workflow instance.
 *
 * <p>{@code transitioned == false} means a matching transition was found
 * but its guard returned false — the engine recorded the PAGE_SUBMITTED
 * event but did not change state. Front-end shows this as "saved but not
 * advanced".
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowTransitionResult {
    private Long instanceId;
    private String previousState;
    private String newState;
    private String assignedRole;
    private boolean transitioned;
    private Instant transitionedAt;
    @lombok.Builder.Default
    private List<String> actionsFired = List.of();
}
