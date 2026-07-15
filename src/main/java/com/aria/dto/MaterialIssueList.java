package com.aria.dto;

import java.util.Date;

public interface MaterialIssueList  {

    Long getId();

    Long getRequestId();

    String getRequestName();
    String getTrnNo();
    String getItemNames();

    String getStatus();

    Date getRequestDate();

    String getRequestFromBranch();

    Long getRequestFromBranchId();

    Long getRequestFromDepartmentId();

    String getRequestFromDepartment();
    
    String getRequestedPerson();


}
