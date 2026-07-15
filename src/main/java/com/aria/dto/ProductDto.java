package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.Product;

@Getter
@Setter
@NoArgsConstructor
public class ProductDto {

	private Long id;

	private String productId;

	private String productName;

	private String lot;

	private String totalQuantity;

	private int active;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<SalesProductDto> salesProduct;

	public ProductDto(Product product) {
		this.id = product.getId();
		this.productId = "P-" + product.getId();

		this.productName = product.getProductName();
		this.lot = product.getLot();
		this.totalQuantity = product.getTotalQuantity();
		this.active = product.getActive();
		this.createdBy = product.getCreatedBy();
		this.updatedBy = product.getUpdatedBy();
		this.created_date = product.getCreated_date();
		if (product.getUpdated_date() != null) {
			this.updated_date = product.getUpdated_date();
		} else {
			this.updated_date = product.getCreated_date();
		}
		this.salesProduct = product.getSalesProduct() != null
				? product.getSalesProduct().stream().map(obj -> new SalesProductDto(obj)).collect(Collectors.toList())
				: null;
	}

}
