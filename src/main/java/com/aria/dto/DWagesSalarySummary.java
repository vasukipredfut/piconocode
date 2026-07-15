package com.aria.dto;

import java.util.Date;

public interface DWagesSalarySummary {

    Long getAllotmentId();

    Date getShiftDate();

    String getShiftType();

    Long getShiftId();

    Long getWorkedDept();
    String getWorkedDeptName();

    Long getWorkedBranch();
    String getWorkedBranchName();

    Long getEmpId();

    int getEmp_type();
    String getContract();

    Long getLuzone();
    Long getPShiftId();

     Long getFromDept();
     String getFromDeptName();
     Long getFromBranch();
     String getFromBranchName();
}
