package com.aria.dto;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.SkillMatrix;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillMatrixDto {

	private Long id;

	private Long userId;

	private String userName;

	private String date;

	private Long departmentId;

	private String departmentName;

	private Long designationId;

	private String designationName;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private Date updated_date;

	private List<SkillMatrixGradeLevelDto> skillMatrixGradeLevelDtos;

	public SkillMatrixDto(SkillMatrix skillMatrix) {
		this.id = skillMatrix.getId();

		if (skillMatrix.getUser() != null) {
			this.userId = skillMatrix.getUser().getId();
			this.userName = skillMatrix.getUser().getEmployeename();
		}

		if (skillMatrix.getDepartment() != null) {
			this.departmentId = skillMatrix.getDepartment().getId();
			this.departmentName = skillMatrix.getDepartment().getName();
		}

		if (skillMatrix.getDesignation() != null) {
			this.designationId = skillMatrix.getDesignation().getId();
			this.designationName = skillMatrix.getDesignation().getName();
		}

		this.createdBy = skillMatrix.getCreatedBy();
		this.updatedBy = skillMatrix.getUpdatedBy();
		this.created_date = skillMatrix.getCreated_date();
		this.updated_date = skillMatrix.getUpdated_date();

		this.skillMatrixGradeLevelDtos = skillMatrix.getSkillMatrixGradeLevels() != null
				? skillMatrix.getSkillMatrixGradeLevels().stream().map(data -> new SkillMatrixGradeLevelDto(data))
						.collect(Collectors.toList())
				: null;
	}
}
