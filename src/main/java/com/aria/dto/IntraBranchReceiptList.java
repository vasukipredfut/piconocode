package com.aria.dto;

import java.util.Date;

public interface IntraBranchReceiptList {

    Long getId();

    Long getRequestId();

    String getTrnNo();

    String getHupId();

    String getDocketNo();

    String getStatus();

    Date getIssueDate();

    String getRequestFromBranch();

    String getIssueFromBranch();

    Long getRequestFromBranchId();

    Long getRequestFromDepartmentId();

    String getRequestFromDepartment();

    String getIssueFromDepartment();

    String getIssuePerson();
    Long getIssueId();
}
