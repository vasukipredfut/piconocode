package com.aria.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.aria.model.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class FingerPrintDto {

    private String employeeName;

    private Long employeeId;

    private String secureKey;

    private String employeeCategory;

    private String joinDate;

    private String designation;

    private String status;

    public FingerPrintDto(User user) {

        this.employeeName = user.getEmployeename();
        this.employeeId = user.getId();
        this.employeeCategory = user.getEmployeetype();
        if(user.getDesignation() != null)
        {
            this.designation = user.getDesignation().getName();

        }
    }
}
