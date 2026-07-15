package com.aria.dto;


import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ViewShiftsDto {

    private Long empId;

    private String empName;

    private List<ShiftPeriod> shifts;


    public ViewShiftsDto(ShiftAssignUserList userList) {
        this.empId = userList.getEmpId();
        this.empName = userList.getEmpName();
    }
}
