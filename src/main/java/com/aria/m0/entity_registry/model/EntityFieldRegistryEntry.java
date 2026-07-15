package com.aria.m0.entity_registry.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row in {@code entity_field_registry} — describes a bindable field of
 * an entity (Schema V2 §18 #4). The Builder UI uses this to know which
 * fields can appear in columns/filters/select clauses, and what UI widget
 * to render for each.
 */
@Entity
@Table(name = "entity_field_registry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntityFieldRegistryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_code", nullable = false, length = 128)
    private String entityCode;

    @Column(name = "field_code", nullable = false, length = 128)
    private String fieldCode;

    @Column(name = "display_label", nullable = false, length = 256)
    private String displayLabel;

    /** STRING / INT / LONG / DECIMAL / BOOLEAN / DATE / DATETIME / TIME /
     *  TEXT / JSON / ENUM / RELATION / COMPUTED. ENUM stored as String to
     *  decouple JPA from MariaDB's ENUM column. */
    @Column(name = "data_type", nullable = false, length = 32)
    private String dataType;

    @Column(nullable = false)
    private boolean filterable;

    @Column(nullable = false)
    private boolean sortable;

    @Column(nullable = false)
    private boolean searchable;

    @Column(nullable = false)
    private boolean required;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Column(name = "options_source_json", columnDefinition = "JSON")
    private String optionsSourceJson;

    @Column(name = "relation_entity", length = 128)
    private String relationEntity;
}
