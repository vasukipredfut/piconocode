package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class No2ProductsPriceDto {

	private Long productId;

	private String productType;

	private Long lot;

	private Long partyId;

	private Double price;

	private Double percentage;

	private String percentageType;

}
