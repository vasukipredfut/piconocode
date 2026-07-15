package com.aria.dto;

import com.aria.model.LabelPrices;
import com.aria.utils.DateUtil;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LabelPricesDto {

	private Long id;

	private Long productId;

	private Long locationId;

	private double price;

	private double permlprice;

	private Long productGroupId;

	private String status;

	private String fromDate;

	private String enteredDate;

	private String ip;

	private Long updatedBy;

	public LabelPricesDto(LabelPrices model) {
		DateUtil dateUtil = new DateUtil();
		this.id = model.getId();
		this.productId = model.getProductId();
		this.locationId = model.getLocationId();
		this.price = model.getPrice();
		if(model.getPer_ml_price() != null) {
		this.permlprice = model.getPer_ml_price();
		}
		this.productGroupId = model.getProductGroupId();
		this.status = model.getStatus();
		this.fromDate = dateUtil.getDateTOStringWk(model.getFromDate());
		this.enteredDate = dateUtil.getDateTOStringWk(model.getEnteredDate());
		this.ip = model.getIp();
		this.updatedBy = model.getUpdatedBy().getId();

	}

}
