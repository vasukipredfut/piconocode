package com.aria.dto;

import java.util.Date;

public interface WithInBranchReceiveList {

    Long getId();

    Long getRequestId();

    String getTrnNo();
    String getItemNames();

    String getStatus();

    Date getRequestDate();

    String getRequestFromBranch();

    String getIssueFromBranch();

    Long getRequestFromBranchId();

    Long getRequestFromDepartmentId();

    String getRequestFromDepartment();

    String getIssueFromDepartment();
}
