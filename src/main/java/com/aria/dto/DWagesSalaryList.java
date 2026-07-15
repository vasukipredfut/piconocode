package com.aria.dto;

import java.util.Date;

public interface DWagesSalaryList {

    Date getShiftDate();

    String getShiftType();

    Long getShiftId();

    Long getPshiftId();

    String getpShiftName();

    String getTrnNo();

    String getTrnName();

    String getRefTrnNo();

    Long getRefTrnId();
    String getRefTrnTable();

    Long getItemId();
    Long getBranchId();
    Long getDeptId();
}
