package com.aria.dto;

import java.util.Date;

public interface JournalOrMrnNotClosingList {

    Long getBranchId();

    String getBranchName();

    Long getDeptId();

    String getDeptName();

    Long getJournalId();

    String getJournalName();

    String getBatchNo();

    Long getShiftId();

    String getShiftName();

    Date getShiftDate();

    String getTrnNo();
    String getDcType();



    Long getRequestId();

    Date getRequestDate();
    Date getIssueDate();

    Long getRequestBranchId();

    String getRequestBranchName();

    Long getRequestDeptId();

    String getRequestDeptName();

    Long getIssueBranchId();
    String getIssueBranchName();

    Long getIssueDeptId();
    String getIssueDeptName();

    String getIssueStatus();

    Date getWithInBranchReceiveDate();

    String getWithInBranchStatus();

    String getOtherBranchStatus();

    Date getOtherBranchReceiveDate();


    Long getInwardId();

    String getBillNo();
    Date getInwardDate();
    String getAsrsMapped(); 


}
