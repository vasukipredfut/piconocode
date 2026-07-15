package com.aria.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.aria.model.AwardMaintainance;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AwardMaintainanceDto {

	private Long id;

	private Long branchId;

	private String branchName;

	private Long empId;

	private String employeeName;

	private String awardCategory;

	private String awardDate;

	private String employeeCategory;

	private int active;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	private List<MaintainanceDetailsDto> maintainanceDetails;

	public AwardMaintainanceDto(AwardMaintainance awardMaintainance) {
		this.id = awardMaintainance.getId();
		if (awardMaintainance.getBranch() != null) {
			this.branchId = awardMaintainance.getBranch().getId();
			this.branchName = awardMaintainance.getBranch().getBranchName();
		}
		if (awardMaintainance.getUser() != null) {
			this.empId = awardMaintainance.getUser().getId();
			this.employeeName = awardMaintainance.getUser().getEmployeename();
		}
		this.awardCategory = awardMaintainance.getAwardCategory();

		this.employeeCategory = awardMaintainance.getEmployeeCategory();

		this.active = awardMaintainance.getActive();
		this.createdBy = awardMaintainance.getCreatedBy();
		this.updatedBy = awardMaintainance.getUpdatedBy();
		this.created_date = awardMaintainance.getCreated_date();
		if (awardMaintainance.getUpdated_date() != null) {
			this.updated_date = awardMaintainance.getUpdated_date();
		} else {
			this.updated_date = awardMaintainance.getCreated_date();
		}

		this.maintainanceDetails = awardMaintainance.getMaintainanceDetails() != null
				? awardMaintainance.getMaintainanceDetails().stream().map(obj -> new MaintainanceDetailsDto(obj))
						.collect(Collectors.toList())
				: null;
	}

}
