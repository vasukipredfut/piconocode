package com.aria.dto;

import com.aria.model.StockbyProduct;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StockbyProductDTO {

	private Long productId;

	private Double stkQty;

	private String productName;

	private Long lot;

	public StockbyProductDTO(StockbyProduct stock) {

		this.productId = stock.getProductId();
		this.stkQty = stock.getStkQty();
		this.productName = stock.getProductName();
		this.lot = stock.getLot().longValue();

	}

}