package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.utils.DateUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class WagesSalaryDto {

    public Long empId;
    public String employeeName;
    public String experience;
    public Long designationId;
    public String designationName;
    public String gender;
    public String joiningDate;
    public double basic;
    public double da;
    public double allowance;
    public double perDayTotal;
    public String shiftDate;
    public Long dept;
    public String deptName;
    public Long branchId;
    public String branchName;
    public Long createdBy;
    public String createdByName;
    public String createdOn;
    public Long shiftId;
    public Long pShiftId;
    public String shiftName;
    public String pShiftName;
    public LocalDateTime checkIn;
    public LocalDateTime checkOut;
    public String totalworkinghrs;



    public WagesSalaryDto(WagesSalary wagesSalary ) {
        DateUtil dateUtil = new DateUtil();
        this.empId = wagesSalary.getEmpId();
        this.employeeName = wagesSalary.getEmployeeName();
        this.experience = wagesSalary.getExperience();
        this.designationId = wagesSalary.getDesignationId();
        this.designationName = wagesSalary.getDesignationName();
        this.gender = wagesSalary.getGender();
        this.joiningDate = wagesSalary.getJoiningDate();
        this.basic = wagesSalary.getBasic();
        this.da = wagesSalary.getDa();
        this.allowance = wagesSalary.getAllowance();
        this.perDayTotal = wagesSalary.getPerDayTotal();
        this.shiftDate = dateUtil.getDateTOStringWk(wagesSalary.getShiftDate());
        this.dept = wagesSalary.getDept();
        this.deptName = wagesSalary.getDeptName();
        this.branchId = wagesSalary.getBranchId();
        this.branchName = wagesSalary.getBranchName();
        this.createdBy = wagesSalary.getCreatedBy();
        this.createdByName = wagesSalary.getCreatedByName();
        this.createdOn = dateUtil.getDateTime(wagesSalary.getCreatedOn());
        this.shiftId = wagesSalary.getShiftId();
        this.pShiftId = wagesSalary.getpShiftId();
        this.shiftName = wagesSalary.getShiftName();
        this.pShiftName = wagesSalary.getpShiftName();
        this.checkIn = wagesSalary.getCheckIn();
        this.checkOut = wagesSalary.getCheckOut();
        this.totalworkinghrs = wagesSalary.getTotalworkinghrs();
        
    }
}
