package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

public interface DirectPurchaseDetails {


    Long getId();

    String getReferenceNo();

    Long getBranchId();

    String getBranchName();

    Date getStorageDate();

    Long getCreatedBy();

    Long getUpdatedBy();

    String getStockInwardBy();
    String getStockInwardBy1();

    LocalDateTime getStockInwardByDate();

    LocalDateTime getStockInwardByDate1();

    Long getVendorId();

    Date getInwardDate();

    Date getLorryDate();

    Date getBillDate();

    Double getBillAmount();

    Double getLorryFreightAmount();

    String getStockStatus();

    String getProductCategory();

    Long getCampId();

    String getCampName();

    Long getBillRefId();

    Boolean getExcessOrShortage();

    Long getFirmOrgId();

    String getCreditOrDebit();

    String geteTransfer();

    Long getVerifiedBy();

    String getVerifiedByName();

    LocalDateTime getVerifiedDate();

    Long getApprovedBy();

    String getApprovedByName();

    LocalDateTime getApprovedDate();

    Long getAuthorisedBy();

    String getAuthorisedByName();

    LocalDateTime getAuthorisedDate();

    Long getRejectedBy();

    String getRejectedByName();

    LocalDateTime getRejectedDate();

    Long getHandOverToDepartment();

    String getHandOverToDepartmentName();

}
