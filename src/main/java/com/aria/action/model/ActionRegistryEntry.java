package com.aria.action.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One row in {@code action_registry} (Schema V2 §18 #6) — the catalogue of
 * actions the Renderer can dispatch. {@code implemented=false} entries
 * still appear in the Builder palette but invoke returns 422
 * implementation_pending.
 */
@Entity
@Table(name = "action_registry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActionRegistryEntry {

    @Id
    @Column(length = 64)
    private String code;

    @Column(name = "display_name", nullable = false, length = 256)
    private String displayName;

    @Column(nullable = false, length = 32)
    private String category;

    @Column(name = "params_schema_json", nullable = false, columnDefinition = "JSON")
    private String paramsSchemaJson;

    @Column(nullable = false)
    private boolean implemented;

    @Column(name = "requires_role", length = 64)
    private String requiresRole;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
