package com.aria.savedview.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * One row in {@code tenant_list_view} (Schema V2 §18 #2). A saved-view is
 * a tuple of (columns, filters, sort, optional pageSize) stored as JSON
 * and looked up per (tenant, page_slug, scope).
 */
@Entity
@Table(name = "tenant_list_view")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "page_slug", nullable = false, length = 128)
    private String pageSlug;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 64)
    private String icon;

    /** TENANT | ROLE | USER. ROLE is reserved for Round 1G.2; Round 1G only
     *  honours TENANT (visible to all in tenant) and USER (creator only). */
    @Column(nullable = false, length = 16)
    private String scope;

    @Column(name = "scope_value", length = 128)
    private String scopeValue;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "body_json", nullable = false, columnDefinition = "JSON")
    private String bodyJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by")
    private Long createdBy;
}
