package com.aria.dto;

import java.util.Date;

 public interface PhysicalStockList {


     Long getId();

     Long getRefid();

     String getName();

     String getProductName();

     String getDispunit();

     String getGoDownName();


     String getBinid();

     Long getBinname();

     Long getGid();

     Long getBid();

     Long getDid();

     Double getQty() ;

     Double getLot();

     Double getActualStkQty();
     Double getPhysicalStkQty();

     Date getAdate();

     boolean isCheck();

}
