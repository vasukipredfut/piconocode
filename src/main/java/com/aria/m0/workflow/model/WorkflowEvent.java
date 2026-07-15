package com.aria.m0.workflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Append-only audit log for the workflow runtime. One row per state
 * transition, page submission, action firing, or guard-rejection.
 *
 * <p>{@link #payloadJson} holds the form data for PAGE_SUBMITTED rows,
 * the action result for ACTION_FIRED rows, the SpEL expression + cause
 * for ERROR rows.
 */
@Entity
@Table(name = "workflow_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowEvent {

    public static final String KIND_STATE_TRANSITION = "STATE_TRANSITION";
    public static final String KIND_PAGE_SUBMITTED = "PAGE_SUBMITTED";
    public static final String KIND_ACTION_FIRED = "ACTION_FIRED";
    public static final String KIND_ERROR = "ERROR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(name = "ts", nullable = false, updatable = false)
    private Instant ts;

    @Column(nullable = false, length = 32)
    private String kind;

    @Column(name = "from_state", length = 64)
    private String fromState;

    @Column(name = "to_state", length = 64)
    private String toState;

    @Column(name = "page_code", length = 128)
    private String pageCode;

    @Column(name = "actor_user_id")
    private Long actorUserId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", columnDefinition = "json")
    private String payloadJson;

    @PrePersist
    void onCreate() {
        if (ts == null) ts = Instant.now();
    }
}
