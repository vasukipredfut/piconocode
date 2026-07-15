package com.aria.dto;

import java.util.Date;

public interface BlendingAdvicePrintList {


    String getJournalName();

    String getId();

    String getAdviceId();

    Date getFromDate();

    Date getToDate();

    String getProductId();

    String getProductName();
    String getBranchName();

    String getStatus();
    
    String getCasTrn();
}
