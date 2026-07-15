package com.aria.model;

import java.util.Date;

public interface InwardInspectionBatchList {

    Date getInwardDate();

    String getBillNo();

    String getBatchNo();
    String getLot();
    String getNoOfBag();

    Long getReceivedId();
    String getEntryType();

}
