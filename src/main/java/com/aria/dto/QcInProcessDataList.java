package com.aria.dto;

import java.util.Date;

public interface QcInProcessDataList {
	Long getId();
     Long getKpiId();

     String getKpiName();

     Long getShiftId();

     String getShiftName();

     Date getShiftDate();

     Long getBatchId();

     String getBatchNo();

     Long getDeptId();

     String getDeptName();

     String getMachineNme();

     Long getMachineId();
     String getStatus();

     Long getBinName();
     String getBinId();
     
     Double getQuantity();   
     Long getBatchBinNo();
     String getBatchProduct();
     String getPackingBatchNo();
     Long getBatchProductNo();
     
}
