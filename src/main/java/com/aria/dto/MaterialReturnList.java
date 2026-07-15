package com.aria.dto;

import java.util.Date;

public interface MaterialReturnList {

    Long getId();

    String getItemName();

    Date getIssueDate();

    Date getDispatchDate();

    Date getRequestDate();

    Long getRequestDeptId();

    String getRequestDeptName();

    Long getRequestBranchId();

    String getRequestBranchName();
    String getRequestFrom();
    Long getRequestFromId();
    String getRequestFromName();

    Long getIssueDeptId();

    String getIssueDeptName();

    Long getIssueBranchId();
    Long getReturnId();

    String getIssueBranchName();
    String getQuantity();
    String getToReach();
    Long getToReachId();
    String getToReachName();
}
