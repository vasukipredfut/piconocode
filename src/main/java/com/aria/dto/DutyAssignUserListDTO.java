package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DutyAssignUserListDTO {

    public Long empId;
    public String empName;



    public DutyAssignUserListDTO(DutyAssignUserList dutyAssignUserList) {
        this.empId = dutyAssignUserList.getEmpId();
        this.empName = dutyAssignUserList.getEmpName();
    }
}
