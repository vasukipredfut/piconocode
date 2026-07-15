package com.aria.dto;

import java.util.Date;

public interface SupplierEvalValidUsers {


    String getTrnNo();

    String getStatus();

    Date getCreatedDate();

    String getCreatedStaff();

    String getCreatedStaffName();

    Date getApprovedDate();

    String getApprovalStaff();

    String getApprovalStaffName();

    Date getVerifiedDate();

    String getVerifiedStaff();

    String getVerifiedStaffName();

    Date getAuthorisedDate();

    String getAuthorisedStaff();

    String getAuthorisedStaffName();

}
