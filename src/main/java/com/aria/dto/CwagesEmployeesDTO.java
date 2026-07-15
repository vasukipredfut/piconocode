package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CwagesEmployeesDTO {


    public Long empId;
    public String employeeName;
    public double amount;

    public CwagesEmployeesDTO(CwagesEmployees cwagesEmployees) {
        this.empId = cwagesEmployees.getEmpId();
        this.employeeName = cwagesEmployees.getEmployeeName();
        this.amount = cwagesEmployees.getAmount();
    }
}
