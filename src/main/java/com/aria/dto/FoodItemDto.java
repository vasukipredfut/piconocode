package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.FoodItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FoodItemDto {

	private Long id;

	private String foodName;

	private String measuringType;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<RequiredItemFoodDto> requiredItemFoodDtos;

	public FoodItemDto(FoodItem foodItem) {
		this.id = foodItem.getId();
		this.foodName = foodItem.getFoodName();
		this.measuringType = foodItem.getMeasuringType();
		
		this.requiredItemFoodDtos = foodItem.getRequiredItemFoods() != null ? foodItem.getRequiredItemFoods().stream()
				.map(obj -> new RequiredItemFoodDto(obj)).collect(Collectors.toList()) : null;

		this.createdBy = foodItem.getCreatedBy();
		this.created_date = foodItem.getCreated_date();
		this.updatedBy = foodItem.getUpdatedBy();
		this.updated_date = foodItem.getUpdated_date();	
	}
}
