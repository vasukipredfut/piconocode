package com.aria.dto;

import java.util.Date;

public interface QcInProcessList {

    String getBatchno();

    Long getQcId();

    Long getKpiNameId();

    String getKpiName();

    Date getShiftDate();

    Long getShiftId();
    String getStatus();
}
