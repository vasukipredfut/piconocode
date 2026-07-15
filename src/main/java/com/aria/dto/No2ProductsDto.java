package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Objects;

import com.aria.model.No2Products;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class No2ProductsDto {

	private Long id;

	private Long productId;

	private String name;

	private Long active;

	private Long lot;

	private String units;

	private Long percentage;

	private String productType;

	private String productTypeName;

	private Long createdBy;

	private LocalDateTime createdDate;

	public No2ProductsDto(No2Products model) {
		this.id = model.getId();
		this.productId = model.getProductId();
		this.active = model.getActive();
		this.createdDate = model.getCreatedDate();
		this.createdBy = model.getCreatedBy();
		this.lot = model.getLot();
		this.units = model.getUnits();
		this.productType = model.getProductType();
		this.name = model.getName();
		this.percentage = model.getPercentage();
		if (Objects.nonNull(this.productType)) {
			if (this.productType.equals("1")) {
				this.productTypeName = "GUNNY";
			}
			if (this.productType.equals("2")) {
				this.productType = "GroundNut";
			}
			if (this.productType.equals("3")) {
				this.productType = "Seasame";
			}
		}

	}
}
