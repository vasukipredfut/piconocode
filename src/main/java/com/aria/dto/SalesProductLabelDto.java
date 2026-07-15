package com.aria.dto;

import com.aria.model.SalesProductLabel;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesProductLabelDto {

	private Long id;

	private String name;

	private Long productId;

	private Long productGroupId;

	private String productGroupName;

	private Long capacity;

	private Long branchId;

	private String branchName;

	private Long finalProductId;

	private String finalProductName;

	private Double labelweightinkg;

	private Double labelWeightinKg1;

	private Long updatedBy;

	private String ip;

	private Long locationId;

	private String dispLocation;

	private String price;

	private Long dispLocationOrder;

	private String disLabelName;

	private String createdDate;

	public SalesProductLabelDto(SalesProductLabel model) {
		DateUtil dateUtil = new DateUtil();
		this.id = model.getId();
		this.name = model.getName();
		this.productId = model.getProductId();
		this.productGroupId = model.getProductGroupId();
		this.capacity = model.getCapacity();
		this.branchId = model.getBranchId();
		this.finalProductId = model.getFinalProductId();
		this.price = model.getPrice();
		if (model.getUpdatedBy() != null) {
			this.updatedBy = model.getUpdatedBy().getId();
		}
		this.labelweightinkg = model.getLabelweightinkg();
		this.labelWeightinKg1 = model.getLabelWeightinKg1();
		this.locationId = model.getLocationId();
		this.createdDate = dateUtil.getDateTOStringWk(model.getCreatedDate());

	}

}
