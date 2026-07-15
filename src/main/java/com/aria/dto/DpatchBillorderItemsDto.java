package com.aria.dto;

import java.util.List;


import com.aria.model.DpatchBillorderItems;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class DpatchBillorderItemsDto {
	private Long productId;
	private String productName;
	private Double boxQuantity;
	private Long lot;
	private Double pieceQuantity;
	private List<BinStockDto> binStockDto;

	public DpatchBillorderItemsDto(DpatchBillorderItems dispatch) {
		if (dispatch.getProduct() != null) {
			this.productId = dispatch.getProduct().getId();
			this.productName = dispatch.getProduct().getProductName();
		}
		this.boxQuantity = dispatch.getBoxQuantity();
		this.pieceQuantity = dispatch.getPieceQuantity();
		this.lot = dispatch.getLot();
	}

}
