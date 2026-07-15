package com.aria.m11.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Per-publish snapshot of an instance's config + mapping. Status:
 *   DRAFT — author-side checkpoint, not yet published
 *   PUBLISHED — the version the live runtime should use
 *   ROLLED_BACK — a previously-published version that has been superseded.
 */
@Entity
@Table(name = "integration_instance_version")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationInstanceVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(nullable = false)
    private Integer version;

    @Column(name = "config_json", nullable = false, columnDefinition = "JSON")
    private String configJson;

    @Column(name = "mapping_json", columnDefinition = "JSON")
    private String mappingJson;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by")
    private Long publishedBy;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
