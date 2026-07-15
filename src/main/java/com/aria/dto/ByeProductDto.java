package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ByeProductDto {
	private	Long itemId;
	private	String itemName;	
	private String conversion;
	private Double stockQty;
	public ByeProductDto(ByeProductDtoList list) {
	
		this.itemId = list.getItemId();
		this.itemName = list.getName();
		this.conversion = list.getConversion();
		this.stockQty = list.getStockQty();
	}

}
