package com.aria.dto;

import java.util.Date;

public interface QualityInspectionScoreList {

    Long getCompany();
    Long getVendorId();
    String getBillRefId();
    String getBillNo();
    String getVendorName();
    Long getBranchId();
    Long getPoId();
    Date getInwardDate();
    String getQcScore();
    String getDeliveryScore();
    Date getDeliveryDueDate();


}
