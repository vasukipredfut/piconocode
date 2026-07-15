package com.aria.dto;

import java.util.Date;

public interface ProductionEntryGrouping {


    Long getJournalId();
    String getJournalName();
    Long getTsId();
    String getTsName();
    Long getBatchId();
    String getBatchNo();
    String getTrnNo();
    Long getMachineId();
    Long getAuxMachineId();

    Long getShiftId();

    Date getShiftDate();

}
