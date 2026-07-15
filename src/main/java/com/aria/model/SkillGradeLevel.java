package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.SkillGradeLevelDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "skill_grade_level")
public class SkillGradeLevel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "level_Number")
	private Integer levelNumber;

	@Column
	private String description;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public SkillGradeLevel(SkillGradeLevelDto skillGradeLevelDto) {
		this.id = skillGradeLevelDto.getId();
		this.levelNumber = skillGradeLevelDto.getLevelNumber();
		this.description = skillGradeLevelDto.getDescription();
		this.createdBy = skillGradeLevelDto.getCreatedBy();
		this.updatedBy = skillGradeLevelDto.getUpdatedBy();
		this.created_date = skillGradeLevelDto.getCreated_date();
		this.updated_date = skillGradeLevelDto.getUpdated_date();
	}
}
