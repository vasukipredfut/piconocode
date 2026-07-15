package com.aria.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;

import com.aria.dto.MenuItemsDto;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "menu_items")
public class MenuItems {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "food_id")
	private Long foodId;

	@OneToOne
	@JoinColumn(name = "food_Item_id", referencedColumnName = "id")
	private FoodItem foodItem;

	@Column
	private String menutype;
	
	
	public MenuItems(MenuItemsDto menuItems) {
		this.id = menuItems.getId();
		this.foodId = menuItems.getFoodId();
		if (menuItems.getFoodItemId() != null) {
			FoodItem foodItem = new FoodItem();
			foodItem.setId(menuItems.getFoodItemId());
			this.foodItem = foodItem;
		}
		this.menutype = menuItems.getMenutype();
	}
}
