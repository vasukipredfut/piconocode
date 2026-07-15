package com.aria.dto;

import java.util.Date;

public interface ExcessOrShortageOrReturn {


     Long getId();
     Long getVendorId();

     Long getItemId();

     String getBillNo();

     String getType();
     String getLot();

     Double getQty();
     Date getBillDate();
}
