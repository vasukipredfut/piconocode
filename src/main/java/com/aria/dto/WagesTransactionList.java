package com.aria.dto;

import java.util.Date;

public interface WagesTransactionList {


    Date getShiftDate();

    Long getShiftId();

    Long getPshift();

    String getShiftType();

    String getPshiftName();

    String getDeptName();

    String getBranchName();

    Long getDeptId();

    Long getDaCatId();

    Long getBranchId();
}
