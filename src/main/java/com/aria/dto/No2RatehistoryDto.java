package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.No2Ratehistory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class No2RatehistoryDto {

	private Long id;

	private Long partyid;

	private Long productid;

	private Long increasedecrease;

	private Double percentage;

	private LocalDateTime created_date;

	private Long created_by;

	private Double amount;

	private Double originalamount;

	private Long lot;

	public No2RatehistoryDto(No2Ratehistory model) {
		this.id = model.getId();
		this.partyid = model.getPartyId();
		this.productid = model.getProductId();
		this.amount = model.getAmount();
		this.originalamount = model.getOriginalAmount();
		this.lot = model.getLot();
		this.increasedecrease = model.getIncreaseDecrease();
		this.created_by = model.getCreatedBy();
		this.created_date = model.getCreatedDate();
		this.percentage = model.getPercentage();

	}
}
