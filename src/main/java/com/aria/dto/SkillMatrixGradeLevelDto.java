package com.aria.dto;

import com.aria.model.SkillMatrixGradeLevel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillMatrixGradeLevelDto {

	private Long id;

	private Long skillMatrixId;

	private Long skillId;

	private String skills;

	private Long skillGradeLevelId;

	private Integer skillGradeLevel;

	public SkillMatrixGradeLevelDto(SkillMatrixGradeLevel skillMatrixGradeLevel) {

		this.id = skillMatrixGradeLevel.getId();
		this.skillMatrixId = skillMatrixGradeLevel.getSkillMatrixId();

		if (skillMatrixGradeLevel.getSkills() != null) {
			this.skillId = skillMatrixGradeLevel.getSkills().getId();
			this.skills = skillMatrixGradeLevel.getSkills().getName();
		}

		if (skillMatrixGradeLevel.getSkillGradeLevel() != null) {
			this.skillGradeLevelId = skillMatrixGradeLevel.getSkillGradeLevel().getId();
			this.skillGradeLevel = skillMatrixGradeLevel.getSkillGradeLevel().getLevelNumber();
		}
	}
}
