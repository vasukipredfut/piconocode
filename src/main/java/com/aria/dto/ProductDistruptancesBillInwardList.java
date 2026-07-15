package com.aria.dto;

import java.util.Date;

public interface ProductDistruptancesBillInwardList {


    Date getReceivedDate();

    Date getBillDate();

    Long getId();
    Long getVendorId();

    String getVendorName();

    String getBillNo();

    String getLorrySerialNo();
}
