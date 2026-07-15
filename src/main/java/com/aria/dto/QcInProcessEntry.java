package com.aria.dto;

import java.util.Date;

public interface QcInProcessEntry {

    String getKpiName();
    String getTestedBy();
    String getIsoNo();
    String getProductName();
    String getPlant();
    Date getShiftDate();
    String getShiftName();
    Long getKpiNameId();
    Long getMachineId();
    String getMachineName();
    String getPackingBatchNo();
    Long getBinName();
}
