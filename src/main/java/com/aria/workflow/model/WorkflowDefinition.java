package com.aria.workflow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * A workflow's static definition: states + transitions + guards + actions,
 * stored verbatim as JSON in {@code body_json}. The engine parses it on
 * demand via Jackson.
 *
 * <p>Keyed by {@code (tenant_id, code, version)} — multiple versions of the
 * same workflow can coexist (the engine always picks the latest PUBLISHED
 * for new instances; running instances stay pinned to their version).
 */
@Entity
@Table(name = "workflow_definition")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, length = 16)
    @lombok.Builder.Default
    private String status = "PUBLISHED";

    @Column(name = "body_json", nullable = false, columnDefinition = "JSON")
    @JdbcTypeCode(SqlTypes.JSON)
    private String bodyJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
