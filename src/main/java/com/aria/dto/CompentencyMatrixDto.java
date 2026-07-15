
package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.CompentencyMatrix;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CompentencyMatrixDto {
	private Long id;

	private Long designationId;

	private String designation;

	private Long deptId;

	private String departmentName;

	private String date;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<SkillDetailsDto> skillDetails;
	private int skillCount;

	public CompentencyMatrixDto(CompentencyMatrix compentencyMatrix) {
		this.id = compentencyMatrix.getId();
		if (compentencyMatrix.getDepartment() != null) {
			this.deptId = compentencyMatrix.getDepartment().getId();
			this.departmentName = compentencyMatrix.getDepartment().getName();
		}
		if (compentencyMatrix.getDesignation() != null) {
			this.designationId = compentencyMatrix.getDesignation().getId();
			this.designation = compentencyMatrix.getDesignation().getName();
		}

		this.createdBy = compentencyMatrix.getCreatedBy();
		this.updatedBy = compentencyMatrix.getUpdatedBy();
		this.created_date = compentencyMatrix.getCreated_date();
		this.updated_date = compentencyMatrix.getUpdated_date();

		this.skillDetails = compentencyMatrix.getSkillDetails() != null ? compentencyMatrix.getSkillDetails().stream()
				.map(field -> new SkillDetailsDto(field)).collect(Collectors.toList()) : null;

		this.skillCount = compentencyMatrix.getSkillDetails() != null ? compentencyMatrix.getSkillDetails().stream()
				.map(field -> new SkillDetailsDto(field)).collect(Collectors.toList()).size() : 0;

	}
}