package com.aria.dto;



import java.util.Date;


public interface ProductionEntryList {

     String getStatus();
     Long getJournalId() ;
     String getJournalName();
     Date getShiftDate();
     String getShiftName();
     Long getShiftId();
     Long getMachineId();
     String getMmctable();
     String getDcType();
     String getTsTable();
     String getTsName();
     String getBatchNo();
     Long getBatchId();
     String getHumidity();
     String getTemperature();
     String getGodownName();
     Long getGodownId();
     String getTrnNo();
     Long getTsId();


}
