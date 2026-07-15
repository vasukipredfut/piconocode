package com.aria.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.model.DayEndStockClosingSummary;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class DayEndStockClosingSummaryDto {

	private Long id;
	private Long itemId;
	private String itemName;
	private Double physicalStock;
	private Double todayClosing;
	private Long branchId;
	private String branchName;
	private Double binStock;
	private Double differance;
	private Long deviation;
	private Long createdBy;
	private String accountingDate;
	private LocalDateTime created_date;
	private double yieldSummary;
	private Map<String, Object> yieldSummaryValue;



	public DayEndStockClosingSummaryDto(DayEndStockClosingSummary dayEndStock) {
		this.id = dayEndStock.getId();
		this.itemId = dayEndStock.getItemId();
		this.itemName = dayEndStock.getItemName();
		this.physicalStock = dayEndStock.getPhysicalStock();
		this.todayClosing = dayEndStock.getTodayClosing();
		this.branchId = dayEndStock.getBranch();
		this.binStock = dayEndStock.getBinStock();
		this.createdBy = dayEndStock.getCreatedBy();
		this.created_date = dayEndStock.getCreated_date();
		ObjectMapper mapper = new ObjectMapper();
	}

	public DayEndStockClosingSummaryDto(DayEndStockList dayEndStock) {
		this.itemId = dayEndStock.getItemId();
		this.itemName = dayEndStock.getItemName();
		this.branchId = dayEndStock.getBranchId();
		this.binStock = 0.0;
		this.physicalStock = 0.0;
	}


}
