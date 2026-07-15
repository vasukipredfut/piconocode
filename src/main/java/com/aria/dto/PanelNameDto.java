package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.PanelName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PanelNameDto {

    private Long id;

    private String name;
    
    private Long createdBy;

    private LocalDateTime createdDate;

    private Long updatedBy;

    private LocalDateTime updatedDate;

    public PanelNameDto(PanelName field) {
        this.id = field.getId();
        this.name = field.getName();
        this.createdBy = field.getCreatedBy();
        this.updatedBy = field.getUpdatedBy();
        this.createdDate = field.getCreated_date();
        this.updatedDate = field.getUpdated_date();
    }
}
