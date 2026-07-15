package com.aria.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BinStockFilterListDto {

	private String binNo;

	private String batchNo;

	private Double stkQty;

	public BinStockFilterListDto(BinStockListDto binStock) {
		this.binNo = binStock.getBinNo();
		this.batchNo = binStock.getBatchNo();
		this.stkQty = binStock.getStockQty();

	}
}
