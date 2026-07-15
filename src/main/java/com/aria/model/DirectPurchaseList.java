package com.aria.model;

import java.time.LocalDateTime;
import java.util.Date;

public interface DirectPurchaseList {

    Long getId();

    Long getBranchId();
    
    Long getBillInwardId();


    String getBranchName();

    String getVendorName();

    Date getInwardDate();

    Date getLorryDate();
    Date getBillDate();

    String getBillNumber();
    
    String getAddress();
    
    String getCity();
    
    String getState();
    
    String getGstNumber();
    
    Double getSubTotalAmount();
    
    Double getSgstAmount();

    Double getCgstAmount();
    
    Double getIgstAmount();
    
    Double getSgst();
    Double getSgstper();

    Double getIgst();
    Double getIgstper();

    Double getCgst();
    Double getCgstper();

    Double getBillAmount();

    String getStatus();

    Long getVendor();

    String getProductCategory();

    String getLorryWithoutWeight();

   /* Long getCampId();

    String getCampName();

    Long getColorId();

    String getColorName();*/



     Double getBillFrightAmount();

     String getBillInwardperson();

     String getStockInwardperson();

     String getBillApprovePerson();

     String getBillAuthorizedPerson();

     String getEwayBill();

     String getLorryWithWeight();

     String getLorrySerialNo();

     String getLorryno();

     Double getInsurance();

     String getCessTax();

     String getRoundOfType();

     String getRoundOfValue();

     Boolean getLorryFreight();

     Double getLorryfreightValue();

     Double getDedLorryfreight();

     String getTcs();

     Double getTcsper();

     LocalDateTime getUpdated_date();

     LocalDateTime getCreated_date();

     String getUpdated_byname();

     String getCreated_byname();

}
