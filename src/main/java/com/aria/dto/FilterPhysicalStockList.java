package com.aria.dto;

import java.util.Date;

public interface FilterPhysicalStockList {

    Long getId();
    Long getRefid();
    Long getAby();
    Date getAon();
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

}
