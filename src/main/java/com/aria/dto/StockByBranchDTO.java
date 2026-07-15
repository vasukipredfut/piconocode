package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockByBranchDTO {
	
	private Long branchId;
	
	private Long departmentId;
	
	private Long productId;
	
	private Double stock;
	
	private String branchName;
	
	private String departmentName;
	
	private String productName;
	
	private Double lot;
	
	public StockByBranchDTO(StockByBranch stock) {
		
		this.branchId = stock.getBranch();
		this.departmentId=stock.getDepartmentId();
		this.productId=stock.getProductId();
		this.stock=stock.getStock();
		this.branchName=stock.getBranchName();
		this.departmentName=stock.getDepartmentName();
		this.lot=stock.getLot();
		this.productName=stock.getProductName();
		
	}

}