package com.aria.dto;

import java.util.Date;

public interface SendExternalEntityList {


    Long getMaterialFlowSendExternalId();

    String getToReach();

    Long getCreatedBy();

    String getCreatedByName();

    Date getReferenceDate();

    String getItemNames();
    String getFromBranchName();
    Long getStatus();
}
