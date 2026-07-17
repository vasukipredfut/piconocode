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
 * Wires a {@link PageDefinition} (by code) to a workflow step.
 *
 * <p>Examples:
 * <ul>
 *   <li>{@code aria:weighment-capture} -> {@code M10.TankerInbound /
 *       WB_LOADED_WEIGHED} as INPUT — i.e. when the workflow advances to
 *       that step, the workflow engine looks up this page and serves it.</li>
 *   <li>OUTPUT bindings let a downstream step read submitted data via
 *       {@code com.aria.workflow.PageDataReferenceResolver}.</li>
 *   <li>BOTH = same page used to capture *and* surface data.</li>
 * </ul>
 *
 * <p>{@code field_map_json} optionally renames fields between the page form
 * and the workflow context — round 1H wires this through.
 */
@Entity
@Table(name = "page_workflow_binding")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageWorkflowBinding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "page_code", nullable = false, length = 128)
    private String pageCode;

    @Column(name = "workflow_code", nullable = false, length = 128)
    private String workflowCode;

    @Column(name = "step_code", nullable = false, length = 128)
    private String stepCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @lombok.Builder.Default
    private BindingRole role = BindingRole.INPUT;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "field_map_json", columnDefinition = "json")
    private String fieldMapJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    /** Direction of data flow between page and workflow step. */
    public enum BindingRole { INPUT, OUTPUT, BOTH }
}
