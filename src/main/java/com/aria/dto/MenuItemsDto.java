package com.aria.dto;

import com.aria.model.MenuItems;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MenuItemsDto {
	private Long id;

	private Long foodId;

	private Long foodItemId;

	private String foodName;

	private String measuringType;

	private String menutype;

	/*
	 * private String beverageItems; private String dinnerItems; private String
	 * breakfastItems; private String lunchItems; private String snacksItems;
	 */

	public MenuItemsDto(MenuItems menuItems) {
		this.id = menuItems.getId();
		this.foodId = menuItems.getFoodId();
		if (menuItems.getFoodItem() != null) {
			this.foodItemId = menuItems.getFoodItem().getId();
			this.foodName = menuItems.getFoodItem().getFoodName();
			this.measuringType = menuItems.getFoodItem().getMeasuringType();
		}

		this.menutype = menuItems.getMenutype();

	}
}
