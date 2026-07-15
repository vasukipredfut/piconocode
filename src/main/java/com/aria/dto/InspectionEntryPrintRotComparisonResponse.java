package com.aria.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class InspectionEntryPrintRotComparisonResponse {
    String kpiName;
    String productName;
    String reportType;
    String lorrySNo;
    String lorryDate;
    String camp;
    List<String> storedBins;
    String kgs;
    String plant;
    Double stock;
    String vendorPlace;
    String billNo;
    String billDate;
    HashMap<String,String> attributes=new HashMap<>();
}
