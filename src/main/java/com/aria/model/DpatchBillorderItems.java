package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.DpatchBillorderItemsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@NoArgsConstructor
@Table(name = "dpatch_billorder_items")
@Getter
@Setter
@ToString
public class DpatchBillorderItems {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "dpatch_bill_order_ref_id")
	private Long orderId;

	@OneToOne
	@JoinColumn(name = "product_ref_id", referencedColumnName = "id")
	private ProductMaster product;

	@Column(name = "lot_ref_id")
	private Long lot;

	@Column(name = "boxquantity")
	private Double boxQuantity;

	@Column(name = "piecequantity")
	private Double pieceQuantity;

	public DpatchBillorderItems(DpatchBillorderItemsDto dto) {
		if (dto.getProductId() != null) {
			ProductMaster prod = new ProductMaster();
			prod.setId(dto.getProductId());
			this.product = prod;
		}
		this.lot = dto.getLot();
		this.pieceQuantity = dto.getPieceQuantity();
		this.boxQuantity = dto.getBoxQuantity();
	}

}
