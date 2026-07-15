package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserListDto {

    private Long id;

    private String employeeName;

    private Long designationId;

    private String designationName;

    private Long branchId;

    private String branchName;

    private Long departmentId;

    private String departmentName;

    private String currentWeekOff;

    private String employeeType;

    private String currentDuty;
    private String gender;

    private String lastShiftRotateDate;
    private String lastRotateShiftName;

    public UserListDto(UserList user) {
        this.employeeName = user.getEmployeeName();
        this.id = user.getId();
        this.designationId = user.getDesignationId();
        this.designationName = user.getDesignationName();
        this.branchId = user.getBranchId();
        this.branchName = user.getBranchName();
        this.departmentId = user.getDepartmentId();
        this.departmentName = user.getDepartmentName();
        this.employeeType = user.getEmployeeType();
        this.gender = user.getGender();
    }
}
