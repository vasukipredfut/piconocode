package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.No2RatehistoryDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "no2ratehistory")
public class No2Ratehistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "partyid")
	private Long partyId;

	@Column(name = "productid")
	private Long productId;

	@Column(name = "increasedecrease")
	private Long increaseDecrease;

	@Column(name = "percentage")
	private Double percentage;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "created_by")
	private Long createdBy;

	@Column(name = "amount")
	private Double amount;

	@Column(name = "originalamount")
	private Double originalAmount;

	@Column(name = "lot")
	private Long lot;

	public No2Ratehistory(No2RatehistoryDto model) {
		this.id = model.getId();
		this.partyId = model.getPartyid();
		this.productId = model.getProductid();
		this.amount = model.getAmount();
		this.originalAmount = model.getOriginalamount();
		this.lot = model.getLot();
		this.increaseDecrease = model.getIncreasedecrease();
		this.createdBy = model.getCreated_by();
		this.createdDate = model.getCreated_date();
		this.percentage = model.getPercentage();

	}

}
