package com.aria.dto;

import java.util.Date;

public interface MrnPendingList {


    Long getIssueitemId();
    Long getItemId();
    Long getFirmId();
    String getFirmName();
    String getTrnNo();
    Date getTrnDate();
    Long getRequestBranchId();
    String getRequestBranchName();
    Long getIssueBranchId();
    String getIssueBranchName();
    Long getRequestDeptId();
    String getRequestDeptName();
    Long getIssueDeptId();
    String getIssueDeptName();
    String getProductName();
    String getQty();
    String getLot();
    String getBatchNo();
    String getLockstatus();
    String getIssueStatus();
    String getOtherBranchIssueStatus();
    String getIntraBranchStatus();
    String getWithinBranchStatus();
    Long getIssueId();
    Long getWithinBranchDisbutionId();
    Long getOtherbranchIssueId();
    Long getIntraBranchRecId();
}
