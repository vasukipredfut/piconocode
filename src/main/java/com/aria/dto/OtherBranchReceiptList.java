package com.aria.dto;

import java.util.Date;

public interface OtherBranchReceiptList {

    Long getId();

    String getHupId();

    Long getRequestId();

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
}
