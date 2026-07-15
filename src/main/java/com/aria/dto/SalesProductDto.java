package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import com.aria.model.SalesProduct;

@Getter
@Setter
@NoArgsConstructor
public class SalesProductDto {

	private Long id;

	private Long productId;
	
	private Long vendorId;

	private String vendorName;

	private Long oldPrice;
	
	private Long newPrice;
	
	private Long assignedQuantity;

	private String bin;

	private String batch;

	private String comparePercentage;
	
	

	private int active;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public SalesProductDto(SalesProduct salesProduct) {
		this.id = salesProduct.getId();
		this.productId = salesProduct.getProductId();
		if(salesProduct.getVendor() != null) {
			this.vendorId = salesProduct.getVendor().getId();
			this.vendorName = salesProduct.getVendor().getName();
		}
		this.oldPrice = salesProduct.getOldPrice();
		this.newPrice = salesProduct.getNewPrice();
		
		this.assignedQuantity = salesProduct.getAssignedQuantity();
		this.bin = salesProduct.getBin();
		this.batch = salesProduct.getBatch();
		this.comparePercentage = salesProduct.getComparePercentage();
		
		this.active = salesProduct.getActive();
		this.createdBy = salesProduct.getCreatedBy();
		this.updatedBy = salesProduct.getUpdatedBy();
		this.created_date = salesProduct.getCreated_date();
		if (salesProduct.getUpdated_date() != null) {
			this.updated_date = salesProduct.getUpdated_date();
		} else {
			this.updated_date = salesProduct.getCreated_date();
		}
	}
}