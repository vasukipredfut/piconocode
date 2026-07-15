package com.aria.dto;

import java.util.Date;

public interface OtherBranchIssueList {

    Long getId();

    Long getMaterialWithInBranchId();

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

    Long getMaterialIssueId();
}
