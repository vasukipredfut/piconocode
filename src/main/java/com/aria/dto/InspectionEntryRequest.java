package com.aria.dto;

import lombok.Data;

@Data
public class InspectionEntryRequest {
    String entryType;
    String batchNo;
    String kpiName;
}
