package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

public interface WagesSalary {

    Long getEmpId();

    String getEmployeeName();

    String getExperience();

    Long getDesignationId();

    String getDesignationName();

    String getGender();

    String getJoiningDate();

    double getBasic();

    double getDa();

    double getAllowance();

    double getPerDayTotal();

    Date getShiftDate();

    Long getDept();

    String getDeptName();

    Long getBranchId();

    String getBranchName();

    Long getCreatedBy();

    String getCreatedByName();

    Date getCreatedOn();

    Long getShiftId();

    Long getpShiftId();

    String getShiftName();

    String getpShiftName();
    
    LocalDateTime getCheckIn();
    LocalDateTime getCheckOut();

    String getTotalworkinghrs();
}
