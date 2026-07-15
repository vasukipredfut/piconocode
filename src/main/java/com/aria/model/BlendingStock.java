package com.aria.model;

import java.util.Date;

public interface BlendingStock {

    Double getQty();

    Long getProductId();
    Long getBranch();

    String getBranchName();

    String getLockType();

    String getCategory();

    String getBatchNo();

    Long getBatchId();

    String getBinNo();

    Long getBinName();

    String getLocation();

    String getCampName();

    String getSeedcolor();

    Date getInwardDate();

    Double getFFA();

    Double getLot();

    String getSeedMoisture();

    Boolean getBlendingLock();
    Boolean getPreprocess();
    String getGodownType();
    Long getSilo();
}
