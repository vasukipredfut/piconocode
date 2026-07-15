package com.aria.dto;

import com.aria.model.BinLock;

import jakarta.persistence.Column;
import java.util.Date;

public interface BinLockStock {

    Long getId();

    Long getRefId();

    String getBatchNo();

    Long getBatchId();

    String getBinId();

    Long getBinName();

    Double getLot();

    Double getStkQty();

    Double getsStkQty();

    Double getAvgCost();

    Double getCost();

    Long getDept();

    Long getBranch();

    String getLockStatus();

    String getlTrnNo();

    Long getGid();

    String getDelStatus();

    String getBinType();

    Long getCid();

    Long getOrderId();

    Long getPurchaseBillId();

    Long getMaterialTransactionId();

    String getCategory();

    String getLockType();

    Double getOilFFA();



    String getLocation();

    String getSeedcolor();

    String getSeedMoisture();

    String getCamp();

    String getPlace();

    String getState();

    String getLorrySerialNo();
    Date getInwarDate();


}
