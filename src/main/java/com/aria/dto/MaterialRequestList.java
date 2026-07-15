package com.aria.dto;

import java.util.Date;

public interface MaterialRequestList {

    Long getId();

    String getRequestDescription();

    String getRequestFromBranch();

    Long getRequestFromBranchId();

    Long getRequestFromDepartmentId();

    String getRequestFromDepartment();

    String getStatus();

    Date getRequestDate();
}
