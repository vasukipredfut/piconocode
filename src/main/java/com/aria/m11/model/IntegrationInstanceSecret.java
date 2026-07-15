package com.aria.m11.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Envelope-encrypted secret for an integration instance. {@code ciphertext}
 * is base64(nonce || ciphertext || tag) produced by
 * {@link com.aria.m11.secrets.EnvelopeCipher}.
 */
@Entity
@Table(name = "integration_instance_secret")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegrationInstanceSecret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "instance_id", nullable = false)
    private Long instanceId;

    @Column(nullable = false, length = 64)
    private String kid;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String ciphertext;

    @Column(name = "encoded_key_ref", length = 128)
    private String encodedKeyRef;

    @Column(name = "rotates_at")
    private Instant rotatesAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;
}
