package com.aria.dto;

import java.util.Date;

public interface CreditNoteBillList {

     Long getVendorId();

     Date getInwardDate();
     Date getBillDate();

     String getBillNo();
}
