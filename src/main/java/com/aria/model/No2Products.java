package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.No2ProductsDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "no2products")
public class No2Products {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "refid")
	private Long productId;

	@Column(name = "name")
	private String name;

	@Column(name = "created_date")
	private LocalDateTime createdDate;

	@Column(name = "created_By")
	private Long createdBy;

	@Column(name = "active")
	private Long active;

	@Column(name = "lot")
	private Long lot;

	@Column(name = "units")
	private String units;

	@Column(name = "percentage")
	private Long percentage;

	@Column(name = "producttype")
	private String productType;

	public No2Products(No2ProductsDto model) {
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
	}

}
