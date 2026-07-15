package com.aria.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import lombok.Data;

@Data
public class InspectionEntryPrintRotResponse {
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
	String roqCommentList;
	LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
	List<InspectionEntryPrintRotComparisonResponse> comparisonResponseList = new ArrayList<>();
}
