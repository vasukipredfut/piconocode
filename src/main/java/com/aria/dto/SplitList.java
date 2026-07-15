package com.aria.dto;

import java.util.Date;

public interface SplitList {

     Long getId();
     String getTrnNo();

     Date getTrnDate();

     String getStatus();

     Double getQty();

     String getProductName() ;

     Long getProductId();

     Long getBinNo();

     String getBatchNo();

     Long getBatchId();

}
