package com.aria.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class InspectionEntryPrintResponse {
	String kpiName;
	String ISO;
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
	String inwardDate;
	List<Attribute> attributesList = new ArrayList<>();
	String roqCommentList;
}
