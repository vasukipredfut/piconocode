package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.SkillGradeLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillGradeLevelDto {

	private Long id;

	private Integer levelNumber;

	private String description;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public SkillGradeLevelDto(SkillGradeLevel skillGradeLevel) {
		this.id = skillGradeLevel.getId();
		this.levelNumber = skillGradeLevel.getLevelNumber();
		this.description = skillGradeLevel.getDescription();
		this.createdBy = skillGradeLevel.getCreatedBy();
		this.updatedBy = skillGradeLevel.getUpdatedBy();
		this.created_date = skillGradeLevel.getCreated_date();
		this.updated_date = skillGradeLevel.getUpdated_date();
	}
}
