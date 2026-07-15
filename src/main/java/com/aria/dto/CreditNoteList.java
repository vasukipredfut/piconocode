package com.aria.dto;

import java.util.Date;

public interface CreditNoteList {

    Long getId();

    String getCreditNoteNumber();

    String getCreditNoteBillNumber();

    String getType();

    Double getCreditAmount();

    Double getCreditQuantity();

    Date getCreditNoteDate();
    Long getBillType();
    Long getVendorId();
    String getVendorName();
    String getBillInwardDate();
    String getStatus();

}
