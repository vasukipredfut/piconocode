package com.aria.dto;

import lombok.Data;

@Data
public class QcInProcessEntryRequest {

    String entryType;
    String batchNo;
    String kpiName;
    String shiftDate;
    Long kpiId;
    Long shiftId;
}
