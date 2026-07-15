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
 * Versioned page body (the JSON schema authored in the Builder).
 *
 * <p>Every save creates a new row. {@code status} transitions DRAFT -> PUBLISHED
 * and prior PUBLISHED rows demote to ARCHIVED in the same transaction.
 * Rollback re-publishes an older body forward as a new version (audit-friendly).
 *
 * <p>JSON column: Hibernate 6 maps {@code body_json} via
 * {@link SqlTypes#JSON}, same pattern used by the OrderRepository migration
 * after the SB3 cutover (see commit d26db262).
 */
@Entity
@Table(name = "page_version")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id", nullable = false)
    private Long pageId;

    @Column(nullable = false)
    private Integer version;

    @Column(nullable = false, length = 16)
    private String status; // DRAFT | PUBLISHED | ARCHIVED

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "body_json", nullable = false, columnDefinition = "json")
    private String bodyJson;

    @Column(columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "published_by")
    private Long publishedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
