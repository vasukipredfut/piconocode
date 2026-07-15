package com.aria.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.No2ProductPartyMappingDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "no2productpartymapping")
public class No2ProductsPartyMapping {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "partyid")
	private Long partyId;

	@Column(name = "productid")
	private Long productId;

	@Column(name = "brokerid")
	private Long brokerId;

	@Column(name = "brokername")
	private String brokerName;

	@Column(name = "updated_date")
	private LocalDate updatedDate;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(name = "amount")
	private Double amount;

	@Column(name = "originalamount")
	private Double originalAmount;

	@Column(name = "active")
	private Long active;

	@Column(name = "lot")
	private Long lot;

	@Column(name = "product_type")
	private String productType;

	public No2ProductsPartyMapping(No2ProductPartyMappingDto model) {
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
