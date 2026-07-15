package com.aria.dto;

import java.time.LocalDateTime;

public interface CreditNoteBillItemList {


     String getBillNumber();

     Long getVendorId();

     Long getProductId() ;

     String getProductName() ;

     Double getQuantity();

     Double getCreditQuantity();

     Double getUnitRate();

     Double getCgstPercentage();

     Double getCgstAmount();

     Double getSgstPercentage();

     Double getSgstAmount() ;

     Double getIgstPercentage() ;

     Double getIgstAmount();

     Double getLineTotal();

     Long getStockInwardBy();
     LocalDateTime getStockInwardDate();

     String getLot();
}
