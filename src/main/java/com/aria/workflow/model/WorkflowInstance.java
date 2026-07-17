package com.aria.workflow.model;

import com.aria.workflow.engine.WorkflowDriven;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * A running workflow. Driven through its states by page-submissions; each
 * submission either advances or is rejected by a guard (in which case a
 * PAGE_SUBMITTED event is recorded without a state change).
 *
 * <p>{@code submitted_data_json} is a {@code Map<pageCode, formData>} so
 * the engine can reference data from a prior step in a guard expression
 * (e.g. {@code #instance.submittedData['qc-approval'].status}).
 *
 * <p>{@code assigned_role} drives the persona queue index — for a given
 * persona the engine returns instances WHERE assigned_role = :persona
 * AND status = 'OPEN'.
 */
@Entity
@Table(name = "workflow_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "workflow_code", nullable = false, length = 128)
    private String workflowCode;

    @Column(name = "workflow_version", nullable = false)
    private Integer workflowVersion;

    /**
     * Tenant that OWNS the definition this instance runs: -1 for a product flow,
     * N for a tenant's own fork. Not the same as {@link #tenantId}, which is the
     * tenant the instance belongs to.
     *
     * <p>Why it exists: the engine pins (code, version) at start and re-reads
     * that exact definition on every transition. Resolving that pin by the
     * INSTANCE's tenant breaks twice — a global (-1) flow is never found at all,
     * and once a tenant forks the flow their fork restarts version numbering, so
     * "v3" would silently resolve to a completely different state machine
     * mid-flight. Pinning the owner makes the reference unambiguous.
     */
    @Column(name = "workflow_owner_tenant_id")
    private Long workflowOwnerTenantId;

    @WorkflowDriven(reason = "engine-only mutation; ArchUnit Rule G")
    @Column(name = "current_state", nullable = false, length = 64)
    private String currentState;

    @WorkflowDriven(reason = "engine-only mutation; ArchUnit Rule G")
    @Column(nullable = false, length = 16)
    @lombok.Builder.Default
    private String status = "OPEN";

    @Column(name = "business_ref", length = 128)
    private String businessRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "submitted_data_json", columnDefinition = "json")
    private String submittedDataJson;

    @Column(name = "assigned_role", length = 64)
    private String assignedRole;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
