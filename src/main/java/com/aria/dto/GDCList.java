package com.aria.dto;

import java.util.Date;

public interface GDCList {

      String getGdcNo();

      Long getIssueBranchId();

      String getIssueBranchName();

      Long getIssueDeptId();

      String getIssueDeptName();

      Long getReqDeptId();

      String getReqDeptName();

      Long getRequestBranchId();

      String getRequestBranchName();

      Date getGdcDate();
}
