package com.aria.dto;

import java.time.LocalDate;

import com.aria.model.No2ProductsPartyMapping;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class No2ProductPartyMappingDto {

	private Long id;

	private Long partyId;

	private Long productId;

	private Long brokerId;

	private String brokerName;

	private LocalDate updatedDate;

	private Long updatedBy;

	private Double amount;

	private Double originalAmount;

	private Long active;

	private Long lot;

	private String productType;

	public No2ProductPartyMappingDto(No2ProductsPartyMapping model) {

		this.id = model.getId();
		this.partyId = model.getPartyId();
		this.active = model.getActive();
		this.productId = model.getProductId();
		this.brokerId = model.getBrokerId();
		this.amount = model.getAmount();
		this.brokerName = model.getBrokerName();
		this.originalAmount = model.getOriginalAmount();
		this.updatedBy = model.getUpdatedBy();
		this.updatedDate = model.getUpdatedDate();
		this.lot = model.getLot();
		this.productType = model.getProductType();

	}
}
