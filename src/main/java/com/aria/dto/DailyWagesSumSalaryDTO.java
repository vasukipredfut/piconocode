package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DailyWagesSumSalaryDTO {

    private Long empId;
    private String empName;
    private double basic;
    private double da;
    private double allowance;


    public DailyWagesSumSalaryDTO(DailyWagesSumSalary sumSalary) {
        this.empId = sumSalary.getEmpId();
        this.empName = sumSalary.getEmpName();
        this.basic = sumSalary.getBasic();
        this.da = sumSalary.getDa();
        this.allowance = sumSalary.getAllowance();
    }
}
