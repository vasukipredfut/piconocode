package com.aria.dto;

import com.aria.model.LabelCeilingStockLimit;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LabelCeilingStockLimitDto {

	private Long id;

	private Long branchId;

	private String branchName;

	private Long ceilingLimit;

	private String status;

	private String date;

	private String capacity;

	private Long productGroupId;

	private String productGroupName;

	private Long updatedBy;

	private String ip;

	public LabelCeilingStockLimitDto(LabelCeilingStockLimit model) {
		this.id = model.getId();
		if (model.getBranchId() != null) {
			this.branchId = model.getBranchId().getBranchId();
			this.branchName = model.getBranchId().getName();
		}
		this.ceilingLimit = model.getCeilingLimit();
		this.status = model.getStatus();
		this.date = DateUtil.localDateToString(model.getDate());
		this.capacity = model.getCapacity();
		this.productGroupId = model.getProductGroupId();
		if (model.getUpdatedBy() != null) {
			this.updatedBy = model.getUpdatedBy().getId();
		}
		this.ip = model.getIp();
	}

}
