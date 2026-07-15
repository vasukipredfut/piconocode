package com.aria.entity_registry.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One row in {@code entity_registry} (Schema V2 §18 #3). Catalogs every
 * entity the DataSource grammar (§3) can reference. The Builder UI reads
 * this to populate its palette; {@link com.aria.datasource.DataSourceQueryService}
 * looks up table names from it at query-compile time.
 */
@Entity
@Table(name = "entity_registry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityRegistryEntry {

    @Id
    @Column(length = 128)
    private String code;

    @Column(name = "display_name", nullable = false, length = 256)
    private String displayName;

    @Column(name = "table_name", nullable = false, length = 128)
    private String tableName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 64)
    private String category;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
