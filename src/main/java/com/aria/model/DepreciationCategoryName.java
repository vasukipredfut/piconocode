package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.DepreciationCategoryNameDto;
import com.aria.dto.PanelNameDto;
import com.aria.dto.ScheduleNameDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "depreciation_category_name_master")
public class DepreciationCategoryName {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name="depreciation_name")
	private String name;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public DepreciationCategoryName(DepreciationCategoryNameDto field) {
		this.id = field.getId();

		this.name = field.getName();

		this.createdBy = field.getCreatedBy();
		this.updatedBy = field.getUpdatedBy();
		this.created_date = field.getCreatedDate();
		this.updated_date = field.getUpdatedDate();
	}
}
