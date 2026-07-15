package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;


public interface SupplierEvaluationReportVendorScore {


    Integer getDeliveryScore();

    Long getLedgerId();
    
    String getVendorName();
    
    Integer getLoyaltyScore();

    Integer getQualityScore();

    Integer getYear();

}
