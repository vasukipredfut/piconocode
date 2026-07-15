package com.aria.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Full instance + the most recent N events, returned by GET /api/workflow/instances/{id}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstanceDto {
    private Long id;
    private String workflowCode;
    private Integer workflowVersion;
    private String currentState;
    private String status;
    private String assignedRole;
    private String businessRef;
    private Instant createdAt;
    private Instant updatedAt;
    private Map<String, Object> submittedData;
    private List<EventDto> recentEvents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EventDto {
        private Long id;
        private Instant ts;
        private String kind;
        private String fromState;
        private String toState;
        private String pageCode;
        private Long actorUserId;
    }
}
