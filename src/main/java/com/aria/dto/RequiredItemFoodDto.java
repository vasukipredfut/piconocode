package com.aria.dto;

import com.aria.model.RequiredItemFood;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequiredItemFoodDto {

	private Long id;

	private Long foodItemId;

	private Long productMasterId;

	private String productName;

	private String measuringType;

	public RequiredItemFoodDto(RequiredItemFood requiredItemFood) {
		this.id = requiredItemFood.getId();
		if (requiredItemFood.getProduct() != null) {
			this.productMasterId = requiredItemFood.getProduct().getId();
			this.productName = requiredItemFood.getProduct().getProductName();
		}
		this.measuringType = requiredItemFood.getMeasuringType();

		this.foodItemId = requiredItemFood.getFoodItemId();

	}
}
