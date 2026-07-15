package com.aria.dto;

import java.time.LocalDate;

import com.aria.model.No2ProductsParty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class No2ProductsPartyDto {

	private Long id;

	private Long partyId;

	private String partyName;

	private Long active;

	private String productType;

	private Long updatedBy;

	private Long brokerId;

	private String brokerName;

	private Double percentage;

	private Long refParty;

	private String refPartyName;

	private LocalDate updatedDate;

	public No2ProductsPartyDto(No2ProductsParty model) {
		this.id = model.getId();
		this.partyId = model.getPartyId();
		this.partyName = model.getPartyName();
		this.active = model.getActive();
		this.productType = model.getProductType();
		this.updatedBy = model.getUpdatedBy();
		this.updatedDate = model.getUpdatedDate();
		this.brokerId = model.getBrokerId();
		this.brokerName = model.getBrokerName();
		this.percentage = model.getPercentage();
		if (model.getRefParty() != null) {
			this.refParty = model.getRefParty().getId();
			this.refPartyName = model.getRefParty().getName();
		}

	}
}
