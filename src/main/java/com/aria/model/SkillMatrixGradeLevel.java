package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.SkillMatrixGradeLevelDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "skill_matrix_grade_details")
public class SkillMatrixGradeLevel {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "skill_Matrix_Id")
	private Long skillMatrixId;

	@OneToOne
	@JoinColumn(name = "skill_id", referencedColumnName = "id", nullable = false)
	private Skills skills;

	@OneToOne
	@JoinColumn(name = "skill_grade_level_id", referencedColumnName = "id", nullable = false)
	private SkillGradeLevel skillGradeLevel;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public SkillMatrixGradeLevel(SkillMatrixGradeLevelDto skillMatrixGradeLevelDto) {

		this.id = skillMatrixGradeLevelDto.getId();
		this.skillMatrixId = skillMatrixGradeLevelDto.getSkillMatrixId();

		if (skillMatrixGradeLevelDto.getSkillId() != null) {
			Skills skillDetails = new Skills();
			skillDetails.setId(skillMatrixGradeLevelDto.getSkillId());
			this.skills = skillDetails;
		}

		if (skillMatrixGradeLevelDto.getSkillGradeLevelId() != null) {
			SkillGradeLevel skillGradeLevel = new SkillGradeLevel();
			skillGradeLevel.setId(skillMatrixGradeLevelDto.getSkillGradeLevelId());
			this.skillGradeLevel = skillGradeLevel;
		}
	}
}
