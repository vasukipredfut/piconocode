package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.productPieceQtyDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "product_pieces_count")
public class ProductPieceQty {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "product_ref_id")
	private Long productId;

	@Column(name = "no_of_pieces")
	private Long boxQty;

	@Column(name = "lot_pieces")
	private Long pieceQty;

	public ProductPieceQty(productPieceQtyDTO dto) {
		this.productId = dto.getProductId();
		this.boxQty = dto.getBoxQty();
		this.pieceQty = dto.getPieceQty();
	}

}
