package com.aria.dto;

import java.text.DecimalFormat;

import com.aria.model.ProductPartyPrice;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PartyPriceDto {

	private Long partyid;

	private String partyName;

	private String price;

	private Long primaryParty;

	private static final DecimalFormat decfor = new DecimalFormat("0.00");

	public PartyPriceDto(ProductPartyPrice product) {

		this.partyid = product.getPartyid();
		this.partyName = product.getPartyName();
		this.primaryParty = product.getPrimaryParty();
		this.price = decfor.format(product.getPrice());
	}

}
