package com.aria.dto;

import java.util.Date;

public interface ASRSProductionEntryList {

    String getTrnNo();

    String getJournalName();

    Long getJournalId();

    Long getProductId();

    Long getAsrsId();

    String getProductName();

    String getStatus();

    String getBatchNo();

    Long getBatchId();

    Long getShiftId();

    Date getShiftDate();

    String getShiftName();


    Long getDominoCount();
    Long getPalletQuantity();

//    29-08-2025 created by dhivya (for amendment restriction), discussed by alaguraja sir
    String getAsrsStatus();
    
    String  getAsrsProductType();
    
    String getPartCategory();
   

}
