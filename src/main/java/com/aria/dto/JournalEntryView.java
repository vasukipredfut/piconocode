package com.aria.dto;

import java.util.Date;

public interface JournalEntryView {

     Date getTrnDate();
     Date getADate();
     String getTrnNo();
     Long getWcid();
     String getWcTable();
     Long getGodownId();
     Long getAby();
     Long getEby();
     String getProducedProductName();
     Long getProductId();
     Long getJournalId();
     String getJournalName();
     String getShiftName();
     String getDepartmentName();
     String getBranchName();
     String getStatus();
}
