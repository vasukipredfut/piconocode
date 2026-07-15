package com.aria.dto;

import java.util.Date;

public interface DutyTransferAssignDutyList {

    Long getId();

    Long getTransferId();

    Long getStaffId();

    String getEmployeeName();

    Long getCurrentDeptId();

    String getCurrentDeptName();

    Long getCurrentBranchId();

    String getCurrentBranchName();

    Long getAssignDeptId();

    String getAssignDeptName();

    Long getAssignBranchId();

    String getAssignBranchName();

    Date getAssignedDate();
}
