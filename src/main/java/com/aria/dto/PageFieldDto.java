package com.aria.dto;

import com.aria.model.PageField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class PageFieldDto {


    private Long id;

    private String componentName;

    private String componentField;

    private String fieldOrder;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;


    public PageFieldDto(PageField pageField) {
        this.id = pageField.getId();
        this.componentName = pageField.getComponentName();
        this.componentField = pageField.getComponentField();
        this.fieldOrder = pageField.getFieldOrder();
        this.active = pageField.getActive();
        this.createdBy = pageField.getCreatedBy();
        this.updatedBy = pageField.getUpdatedBy();
        this.created_date = pageField.getCreated_date();
        if(pageField.getUpdated_date() != null) {
            this.updated_date = pageField.getUpdated_date();
        }else {
            this.updated_date = pageField.getCreated_date();
        }

    }
}
