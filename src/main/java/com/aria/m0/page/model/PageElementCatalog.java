package com.aria.m0.page.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Catalog row for one Renderer primitive (KPI, FormField, WeighbridgeLive, ...).
 *
 * <p>The Builder UI reads this to populate its palette; the runtime Renderer
 * does NOT consult it (the JS registry is authoritative at runtime).
 * Seeded by V3 + V3_2 for the 14 first-party primitives. Tenant-authored
 * custom primitives in Round 1G will land additional rows.
 */
@Entity
@Table(name = "page_element_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageElementCatalog {

    @Id
    @Column(length = 64)
    private String code;

    @Column(nullable = false, length = 32)
    private String category;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_schema_json", columnDefinition = "json")
    private String configSchemaJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_props_json", columnDefinition = "json")
    private String defaultPropsJson;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
