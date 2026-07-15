package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class productPieceQtyDTO {

	private Long productId;
	private Long boxQty;
	private Long pieceQty;

	public productPieceQtyDTO(productPieceList pieceList) {
		this.productId = pieceList.getProductId();
		this.boxQty = pieceList.getBoxQty();
		this.pieceQty = pieceList.getPieceQty();
	}

}
