package com.aria.dto;

import java.util.Date;

public interface JournalEntryList {

    String getStatus();
    Long getJournalId() ;
    String getJournalName();
    Date getShiftDate();
    String getShiftName();
    Long getShiftId();
    String getBatchNo();
    String getTrnNo();
    Long getBatchId();
}
