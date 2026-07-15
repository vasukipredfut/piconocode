package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProductPriceListDto {


	private Long productId;

	private String productName;
	
	private Long lot;

	private List<PartyPriceDto> partyList;

	
	public ProductPriceListDto(Long productId, Long lot, String productName, List<PartyPriceDto> partyList)
	{
		this.productId=productId;
		this.productName=productName;
		this.partyList=partyList;
		this.lot=lot;
	}
	
}
