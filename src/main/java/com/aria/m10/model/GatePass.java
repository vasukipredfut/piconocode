package com.aria.m10.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Customer-facing gate-pass record. The M0 workflow engine writes through to
 * this row on every transition so the dispatch UI can render the live state
 * without traversing the workflow_event audit log.
 *
 * <p>gp_number is generated on insert as {@code GP-{branch}-{instance}} —
 * collision-free because workflow_instance.id is monotonic per tenant.
 */
@Entity
@Table(name = "gate_pass")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GatePass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "gp_number", nullable = false, length = 32)
    private String gpNumber;

    @Column(name = "flow_variant_code", nullable = false, length = 64)
    private String flowVariantCode;

    @Column(name = "vehicle_no", nullable = false, length = 32)
    private String vehicleNo;

    @Column(name = "driver_name", length = 128)
    private String driverName;

    @Column(name = "driver_phone", length = 16)
    private String driverPhone;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(nullable = false, length = 32)
    @lombok.Builder.Default
    private String status = "DRAFT";

    @Column(name = "current_step", length = 64)
    private String currentStep;

    @Column(name = "workflow_instance_id")
    private Long workflowInstanceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "total_unloading_qty", precision = 12, scale = 3)
    private BigDecimal totalUnloadingQty;

    @Column(name = "manager_overrides_count", nullable = false)
    @lombok.Builder.Default
    private Integer managerOverridesCount = 0;

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
