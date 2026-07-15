package com.aria.dto;

import com.aria.model.ISkillMatrixFilter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SkillMatrixFilterDto {

	private Long id;

	private String date;

	private Long branchId;

	private String branchName;

	private Long departmentId;

	private String departmentName;

	private Long designationId;

	private String designationName;

	private Integer skillsCount;

	private String updatedDate;

	public SkillMatrixFilterDto(ISkillMatrixFilter iSkillMatrixFilter) {

		this.id = iSkillMatrixFilter.getId();
//		this.date = iSkillMatrixListResponse.getDate();

//		DateUtil dateUtil = new DateUtil();
//		if(iSkillMatrixListResponse.getDate() != null)
//		{
//			date=dateUtil.getDateToStringDDMMYYYY(iSkillMatrixListResponse.getDate());
//
//		}

		this.branchId = iSkillMatrixFilter.getBranchId();
		this.branchName = iSkillMatrixFilter.getBranchName();
		this.departmentId = iSkillMatrixFilter.getDepartmentId();
		this.departmentName = iSkillMatrixFilter.getDepartmentName();
		this.designationId = iSkillMatrixFilter.getDesignationId();
		this.designationName = iSkillMatrixFilter.getDesignationName();
		this.skillsCount = iSkillMatrixFilter.getSkillsCount();
	}
}
