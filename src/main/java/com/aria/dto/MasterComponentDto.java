package com.aria.dto;

import com.aria.model.MasterComponent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class MasterComponentDto {


    private Long id;

    private String componentName;

    private String deptName;

    private String userLevelAccess;

    private int active;

    private Long createdBy;

    private Long updatedBy;

    private String createdByName;

    private String updatedByName;

    private LocalDateTime created_date;

    private LocalDateTime updated_date;

    List<MasterFieldDto> fieldList;


    public MasterComponentDto(MasterComponent component) {
        this.id = component.getId();
        this.componentName = component.getComponentName();
        if (component.getDepartmentDetails() != null) {
            this.deptName = component.getDepartmentDetails().getName();
        }
        this.userLevelAccess = component.getUserLevelAccess();
        this.active = component.getActive();
        this.createdBy = component.getCreatedBy();
        this.updatedBy = component.getUpdatedBy();
        this.created_date = component.getCreated_date();
        if (component.getUpdated_date() != null) {
            this.updated_date = component.getUpdated_date();
        }

    }


}
