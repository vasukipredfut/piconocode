package com.aria.dto;

import java.time.LocalDate;

import com.aria.model.SalesProductFilm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SalesProductFilmDto {

	private Long id;
	private String name;
	private Long productId;
	private Long productGroupID;
	private Long capacity;
	private Long branchID;
	private Long finalProductNameID;
	private LocalDate createdDate;
	private Long updatedBy;

	public SalesProductFilmDto(SalesProductFilm model) {
		this.id = model.getId();
		this.name = model.getName();
		this.productId = model.getProductId();
		this.productGroupID = model.getProductGroupID();
		this.capacity = model.getCapacity();
		if (model.getBranchID() != null) {
			this.branchID = model.getBranchID().getBranchId();
		}
		this.finalProductNameID = model.getFinalProductID();
		if (model.getUpdatedBy() != null) {
			this.updatedBy = model.getUpdatedBy().getId();
		}
		this.createdDate = model.getCreatedDate();

	}
}
