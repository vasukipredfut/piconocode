package com.aria.page.model;

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
 * does NOT consult it (the JS registry is authoritative at runtime). That split
 * is why the two drifted: the catalog listed four cell-render modes and omitted
 * ListView and DetailView -- the two most-used primitives in the product --
 * without anything breaking, because nothing at runtime reads this table.
 *
 * <p>This table has no tenant_id on purpose: it describes the PRODUCT's
 * elements. A tenant does not get their own primitives; they get their own
 * pages built from these. A new primitive is a row here plus a registry entry,
 * never a per-customer fork.
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

    /**
     * AVAILABLE = a real implementation stands behind this code.
     * STUB = the registry entry renders a placeholder (DataTable draws the text
     * "Table (0 rows)", not a table). The palette must not offer a STUB as a
     * usable element; grey it out. See V3 for why these rows survive at all.
     */
    @Column(length = 16, nullable = false)
    private String status;

    /** Palette label; falls back to {@link #code} when null. */
    @Column(name = "display_name", length = 96)
    private String displayName;

    /** MUI icon name for the palette tile. */
    @Column(length = 32)
    private String icon;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
