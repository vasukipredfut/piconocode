package com.aria.m11.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Tenant-scoped registration of an external system. One row per
 * SAP/MSG91/weighbridge/etc. Lifecycle:
 * DRAFT → ENROLLED → ACTIVE → DEGRADED → DOWN → DISABLED → RETIRED.
 *
 * <p>The optimistic-lock column is named {@code optimistic_lock} in DDL so
 * it doesn't collide with the {@code current_version} business column.
 */
@Entity
@Table(name = "integration_instance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 48)
    private String code;

    @Column(name = "connector_code", nullable = false, length = 64)
    private String connectorCode;

    @Column(name = "connector_version", nullable = false)
    private Integer connectorVersion;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 512)
    private String description;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "status_reason", length = 255)
    private String statusReason;

    @Column(name = "current_version", nullable = false)
    private Integer currentVersion;

    @Column(name = "config_json", nullable = false, columnDefinition = "JSON")
    private String configJson;

    @Column(name = "mapping_json", columnDefinition = "JSON")
    private String mappingJson;

    @Column(name = "mapped_workflow_codes", columnDefinition = "JSON")
    private String mappedWorkflowCodesJson;

    @Column(name = "data_residency_flag", nullable = false, length = 32)
    private String dataResidencyFlag;

    @Column(name = "ip_allowlist_cidr", columnDefinition = "JSON")
    private String ipAllowlistCidrJson;

    @Column(name = "rate_limit_rps")
    private Integer rateLimitRps;

    @Column(name = "last_health_at")
    private Instant lastHealthAt;

    @Column(name = "last_event_at")
    private Instant lastEventAt;

    @Column(name = "error_count_24h", nullable = false)
    private Integer errorCount24h;

    @Column(name = "custom_fields", columnDefinition = "JSON")
    private String customFieldsJson;

    @Version
    @Column(name = "optimistic_lock", nullable = false)
    private Integer optimisticLock;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    /** Canonical lifecycle codes for the {@code status} column. */
    public static final String STATUS_DRAFT     = "DRAFT";
    public static final String STATUS_ENROLLED  = "ENROLLED";
    public static final String STATUS_ACTIVE    = "ACTIVE";
    public static final String STATUS_DEGRADED  = "DEGRADED";
    public static final String STATUS_DOWN      = "DOWN";
    public static final String STATUS_DISABLED  = "DISABLED";
    public static final String STATUS_RETIRED   = "RETIRED";
}
