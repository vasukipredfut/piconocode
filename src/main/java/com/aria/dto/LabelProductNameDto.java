package com.aria.dto;

import com.aria.model.LabelProductName;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LabelProductNameDto {

	private Long id;

	private Long productGroupId;

	private String productName;

	private String type;

	private String capacity;

	private Long displayOrder;

	private String ip;

	private Long createdBy;

	private Long flag;

	public LabelProductNameDto(LabelProductName model) {

		this.id = model.getId();
		this.productGroupId = model.getProductGroupId();
		this.productName = model.getProductName();
		this.type = model.getType();
		this.capacity = model.getCapacity();
		this.displayOrder = model.getDisplayOrder();
		this.ip = model.getIp();
		if(model.getCreatedBy() != null) {
			this.createdBy = model.getCreatedBy().getId();
		}
		this.flag = model.getFlag();

	}

}
