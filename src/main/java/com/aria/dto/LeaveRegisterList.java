package com.aria.dto;

import java.util.Date;

public interface LeaveRegisterList {

    Long getId();

    Long getUserId();

    String getEmployeeName();

    String getLeaveType();

    String getLeaveReason();

    Date getShiftDate();

    String getVerifyByName();

    Date getVerifiedOn();

    String getApprovedByName();

    Date getApprovedOn();

    String getShiftName();
}
