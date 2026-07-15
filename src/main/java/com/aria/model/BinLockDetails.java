package com.aria.model;

import java.util.Date;

public interface BinLockDetails {

    Long getUniqueId();

    String getBinId();

    String getBinName();

    Long getProductId();

    String getProductName();

    Double getQty();

    String getBatchNo();

    String getBatchId();

    String getLorrySerialNo();

    Date getInWardDate();

    Long getCampId();

    String getCampName();

    String getStateName();

    Long getStateId();


}
