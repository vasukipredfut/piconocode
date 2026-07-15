package com.aria.dto;

import java.util.Date;

public interface ProductExcessStorageList {

    Long getId();

    Long getVendorId();

    String getVendorName();

    String getProductName();

    Date getBillInwardDate();

    String getType();

    String getBillNo();
    
    String getStatus();
}
