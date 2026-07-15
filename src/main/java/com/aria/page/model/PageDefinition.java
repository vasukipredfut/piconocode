package com.aria.page.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * A configurable page row. One row per (tenant_id, code).
 *
 * <p>The body of the page (layout, dataSources, etc.) lives on
 * {@link PageVersion}. This entity is the immutable handle the rest of the
 * platform refers to; bindings, audit, and "what page is published right
 * now?" all key on the page record's id.
 *
 * <p>Tenant scoping is per-row (denormalised) — the M0-tenant-context task in
 * Round 1H lifts this to a JWT-derived value at the service layer.
 */
@Entity
@Table(name = "page_definition")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 128)
    private String code;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 64)
    private String category;

    /**
     * FK into {@link PageVersion#id} of the currently-published version,
     * or null when only DRAFTs exist. Updated atomically by
     * {@code PageDefinitionService#publish}.
     */
    @Column(name = "current_version_id")
    private Long currentVersionId;

    @Column(nullable = false, length = 16)
    @lombok.Builder.Default
    private String status = "DRAFT";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;

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
