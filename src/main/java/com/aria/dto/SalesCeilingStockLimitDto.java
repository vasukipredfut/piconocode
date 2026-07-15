package com.aria.dto;

import com.aria.model.SalesCeilingStockLimit;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesCeilingStockLimitDto {

	private Long id;
	private Long plantId;
	private String plantName;
	private Long productGroupId;
	private String productGroupName;
	private String capacity;
	private Long ceilingLimit;
	private String createdDate;
	private Long updatedBy;

	public SalesCeilingStockLimitDto(SalesCeilingStockLimit model) {
		this.id = model.getId();
		this.plantId = model.getBranchId();
		this.productGroupId = model.getProductGroup();
		this.capacity = model.getCapacity();
		this.ceilingLimit = model.getCeilingLimit();
		this.createdDate = DateUtil.localDateToString(model.getCreatedDate());
		if (model.getUpdatedBy() != null) {
			this.updatedBy = model.getUpdatedBy().getId();
		}
	}

}
