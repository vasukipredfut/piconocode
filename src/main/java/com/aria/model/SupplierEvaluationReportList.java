package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;


public interface SupplierEvaluationReportList {

    Long getId();

    Long getBranch();

    String getBillId();

    String getBillNo();

    Long getCompany();

    Date getDeliveryDueDate();

    Integer getDeliveryScore();

    Date getInwardDate();

    Long getLedgerId();
    
    String getVendorName();

    Integer getLoyaltyScore();

    Long getPoid();
    
    String getPoNumber();

    String getPeriod();

    Integer getQualityScore();

    String getTrnNo();

    Integer getYear();

}
