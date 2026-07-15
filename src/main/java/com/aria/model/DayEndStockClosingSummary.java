
package com.aria.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.aria.dto.DayEndStockClosingSummaryDto;
import com.aria.dto.DayEndStockList;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name = "day_end_stock_closing_summary")
@Getter
@Setter
public class DayEndStockClosingSummary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "item_name")
	private String itemName;

	@Column(name = "item_id")
	private Long itemId;

	@Column(name = "physical_stock")
	private Double physicalStock;

	@Column
	private Long branch;

	@Column(name = "bin_stock")
	private Double binStock;

	@Column(name = "yield_summary")
	private Double yieldSummary;

	@Column(name = "today_production")
	private Double todayProduction;

	@Column(name = "today_closing")
	private Double todayClosing;

	@Column(updatable = false)
	@Temporal(TemporalType.DATE)
	private Date adate;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column(name = "yield_summary_value")
	private String yieldSummaryValue;

	@Column(name = "status")
	private String status;

	@Column
	private LocalDateTime updated_date;

	public DayEndStockClosingSummary(DayEndStockClosingSummaryDto dayEndStock) {
		this.id = dayEndStock.getId();
		this.itemId = dayEndStock.getItemId();
		this.itemName = dayEndStock.getItemName();
		this.physicalStock = dayEndStock.getPhysicalStock();
		this.todayClosing = dayEndStock.getTodayClosing();
		this.branch = dayEndStock.getBranchId();
		this.binStock = dayEndStock.getBinStock();
		this.createdBy = dayEndStock.getCreatedBy();
		this.created_date = dayEndStock.getCreated_date();
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			this.yieldSummaryValue = objectMapper.writeValueAsString(dayEndStock.getYieldSummaryValue());
		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public DayEndStockClosingSummary(DayEndStockList dayEndStock) {
		this.itemId = dayEndStock.getItemId();
		this.itemName = dayEndStock.getItemName();
		this.physicalStock = dayEndStock.getQty();
		this.todayClosing = dayEndStock.getQty();
		this.branch = dayEndStock.getBranchId();
		this.binStock = dayEndStock.getQty();
	}
}
