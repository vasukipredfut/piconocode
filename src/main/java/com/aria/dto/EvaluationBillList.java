package com.aria.dto;

import java.util.Date;

public interface EvaluationBillList {


    Long getBranchId();

    Long getFirmId();

    Long getVendorId();

    String getVendorName();

    String getBillRefId();

    String getBillNo();
    String getPoNumber();

    String getTimePeriod();
    Integer getYear();

    Date getInwardDate();

    Date getDeliveryDueDate();

    String getQcScore();

    String getDeliveryScore();
}
