package com.aria.dto;

 public interface WagesJournalWork {

     Long getProductId();

     String getProductName();

     Long getBatchId();

     String getBatchNo();

     String getType();

     double getQty();
     double getLot();

     String getTrnNo();
     String getTotalInputQty();

     String getTotalOutputQty();
}
