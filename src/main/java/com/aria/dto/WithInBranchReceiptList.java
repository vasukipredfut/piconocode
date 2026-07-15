package com.aria.dto;

import java.util.Date;

public interface WithInBranchReceiptList {

    Long getId();

    Long getRequestId();

    String getItemNames();

    String getTrnNo();

    String getStatus();

    Date getRequestDate();

    String getRequestFromBranch();

    String getIssueFromBranch();

    Long getRequestFromBranchId();

    Long getRequestFromDepartmentId();

    String getRequestFromDepartment();

    String getIssueFromDepartment();
}
