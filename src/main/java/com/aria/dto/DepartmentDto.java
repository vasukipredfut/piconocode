package com.aria.dto;


import com.aria.model.DepartmentDetails;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentDto {

    private Long id;

    private String name;

    public DepartmentDto(DepartmentDetails departmentDetails) {
        this.id = departmentDetails.getId();
        this.name = departmentDetails.getName();
    }
}
