package com.aria.m11.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Opaque path-token for inbound webhook routing. {@code tokenHash} is
 * SHA-256(token); the plaintext token is shown to the operator on creation
 * and never persisted.
 */
@Entity
@Table(name = "integration_inbound_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationInboundToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(name = "token_hash", nullable = false, length = 128)
    private String tokenHash;

    @Column(name = "scope_json", columnDefinition = "JSON")
    private String scopeJson;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
