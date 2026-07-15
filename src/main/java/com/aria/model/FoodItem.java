package com.aria.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.aria.dto.FoodItemDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "food_item")
public class FoodItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "food_name")
	private String foodName;

	@Column(name = "measuring_type")
	private String measuringType;
	
	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	@OneToMany(mappedBy = "foodItemId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private List<RequiredItemFood> requiredItemFoods;

	public FoodItem(FoodItemDto foodItem) {
		this.id = foodItem.getId();
		this.foodName = foodItem.getFoodName();
		this.measuringType = foodItem.getMeasuringType();
		
		this.createdBy = foodItem.getCreatedBy();
		this.updatedBy = foodItem.getUpdatedBy();
		this.created_date = foodItem.getCreated_date();
		this.updated_date = foodItem.getUpdated_date();
	}
}
