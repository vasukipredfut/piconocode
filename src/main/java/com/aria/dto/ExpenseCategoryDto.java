package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.ExpenseCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseCategoryDto {

	private Long id;
	private String name;
	private String description;
	private Boolean status = true;
	private Long createdBy;
	private Long updatedBy;
	private LocalDateTime createdDate;
	private LocalDateTime updatedDate;

	public ExpenseCategoryDto(ExpenseCategory entity) {
		this.id = entity.getId();
		this.name = entity.getName();
		this.description = entity.getDescription();
		this.status = entity.getStatus();
		this.createdBy = entity.getCreatedBy();
		this.updatedBy = entity.getUpdatedBy();
		this.createdDate = entity.getCreatedDate();
		this.updatedDate = entity.getUpdatedDate();
	}
}
