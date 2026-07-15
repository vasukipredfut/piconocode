package com.aria.m11.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * One row in {@code integration_connector_catalog} — the platform-published
 * registry of every connector EARMS supports. Composite PK (code, version)
 * lets tenants pin instances to a known version while platform engineers
 * publish v2+ schemas without invalidating older instances.
 */
@Entity
@Table(name = "integration_connector_catalog")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(IntegrationConnectorCatalog.Key.class)
public class IntegrationConnectorCatalog {

    @Id
    @Column(length = 64)
    private String code;

    @Id
    private Integer version;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(nullable = false, length = 64)
    private String vendor;

    @Column(name = "display_name", nullable = false, length = 160)
    private String displayName;

    @Column(length = 512)
    private String description;

    @Column(name = "config_schema_json", nullable = false, columnDefinition = "JSON")
    private String configSchemaJson;

    @Column(name = "default_props_json", nullable = false, columnDefinition = "JSON")
    private String defaultPropsJson;

    @Column(name = "supported_operations", nullable = false, columnDefinition = "JSON")
    private String supportedOperationsJson;

    @Column(name = "auth_methods", nullable = false, columnDefinition = "JSON")
    private String authMethodsJson;

    @Column(name = "dependencies", nullable = false, columnDefinition = "JSON")
    private String dependenciesJson;

    @Column(name = "pii_paths_json", columnDefinition = "JSON")
    private String piiPathsJson;

    @Column(name = "residency_constraints", nullable = false, length = 32)
    private String residencyConstraints;

    @Column(name = "docs_url", length = 255)
    private String docsUrl;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    /** Composite primary key for {@link IntegrationConnectorCatalog}. */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Key implements Serializable {
        private String code;
        private Integer version;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key k)) return false;
            return Objects.equals(code, k.code) && Objects.equals(version, k.version);
        }
        @Override public int hashCode() { return Objects.hash(code, version); }
    }
}
