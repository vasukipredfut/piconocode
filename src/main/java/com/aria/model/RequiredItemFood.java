package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.RequiredItemFoodDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "required_item_food")
public class RequiredItemFood {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "food_Item_Id")
	private Long foodItemId;

	@OneToOne
	@JoinColumn(name = "product_master_id", referencedColumnName = "id")
	public ProductMaster product;

	@Column(name = "measuring_type")
	private String measuringType;

	public RequiredItemFood(RequiredItemFoodDto requiredItemFoodDto) {
		this.id = requiredItemFoodDto.getId();
		this.foodItemId = requiredItemFoodDto.getFoodItemId();
		if (requiredItemFoodDto.getProductMasterId() != null) {
			ProductMaster productMaster = new ProductMaster();
			productMaster.setId(requiredItemFoodDto.getProductMasterId());
			this.product = productMaster;
		}
		this.measuringType = requiredItemFoodDto.getMeasuringType();

	}
}