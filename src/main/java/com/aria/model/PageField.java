package com.aria.model;

import com.aria.dto.PageFieldDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "page_field")
public class PageField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String componentName;

    @Column
    private String componentField;

    @Column
    private String fieldOrder;

    @Column
    private int active;

    @Column
    private Long pageId;

    @Column(updatable = false)
    private Long createdBy;

    @Column
    private Long updatedBy;

    @Column(updatable = false)
    private LocalDateTime created_date;

    @Column
    private LocalDateTime updated_date;


    public PageField(PageFieldDto pageField) {
        this.id = pageField.getId();
        this.componentName = pageField.getComponentName();
        this.componentField = pageField.getComponentField();
        this.fieldOrder = pageField.getFieldOrder();
        this.active = pageField.getActive();
        this.createdBy = pageField.getCreatedBy();
        this.updatedBy = pageField.getUpdatedBy();
        this.created_date = pageField.getCreated_date();
        this.updated_date = pageField.getUpdated_date();
    }
}
