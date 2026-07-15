package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;

public interface GDCDetailsList {

    String getGdcNo();
    Long getIssueBranchId();
    Long getRequestBranchId() ;
    Long getRequestId();
    Long getUpdatedBy();
    Long getIssueId();
    Long getSenderId();
    Long getDeliveryUserId();
    String getTransportMode() ;
    String getIssueMode();
    String getTruckNo();
    String getTruckType();
    Date getDispatchOn();
    LocalDateTime getGdcDate();

}
