package com.aria.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.No2ProductsPartyDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "no2productsparty")
public class No2ProductsParty {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "partyid")
	private Long partyId;

	@Column(name = "party_name")
	private String partyName;

	@Column(name = "active")
	private Long active;

	@Column(name = "updated_date")
	private LocalDate updatedDate;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(name = "productType")
	private String productType;

	@Column(name = "broker_id")
	private Long brokerId;

	@Column(name = "broker_name")
	private String brokerName;

	@Column(name = "percentage")
	private Double percentage;

	@OneToOne
	@JoinColumn(name = "ref_party", referencedColumnName = "id")
	private Vendor refParty;

	public No2ProductsParty(No2ProductsPartyDto model) {

		this.id = model.getId();
		this.partyId = model.getPartyId();
		this.partyName = model.getPartyName();
		this.active = model.getActive();
		this.updatedBy = model.getUpdatedBy();
		this.updatedDate = model.getUpdatedDate();
		this.brokerId = model.getBrokerId();
		this.brokerName = model.getBrokerName();
		this.percentage = model.getPercentage();
		if (model.getRefParty() != null) {
			Vendor party = new Vendor();
			party.setId(model.getRefParty());
			this.refParty = party;
		}
		this.productType = model.getProductType();

	}

}
