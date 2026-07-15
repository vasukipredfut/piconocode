package com.aria.dto;

import java.util.Date;

public interface BinQtyWiseStockList {

    Long getBinNo();

    String getBinId();

    Double getQty();

    Double getCost();

    Double getAvgCost();

    Double getLot();

    Long getProductId();

    String getProductName();

    String getProductCategory();

    Long getBranchId();

    String getBranchName();

    Date getLorryDate();

    String getCamp();

    String getLorrySerialNo();

    String getPlace();

    String getState();

    Long getBatchId();

    String getBatchNo();

    String getLockType();

    String getLockStatus();
    String getMrnStatus();
}
