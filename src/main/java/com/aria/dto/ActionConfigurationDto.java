package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.ActionConfiguration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActionConfigurationDto {

	private Long id;

	private Long deptID;

	private Long branchId;

	private String branchName;

	private String deptName;

	private String taskName;

	private String taskType;

	private String successor;

	private String successorType;

	private String levelOfAccess;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public ActionConfigurationDto(ActionConfiguration model) {
		this.id = model.getId();

		if (model.getDepartmentDetails() != null) {
			this.deptID = model.getDepartmentDetails().getId();
			this.deptName = model.getDepartmentDetails().getName();
		}
		if (model.getBranch() != null) {
			this.branchId = model.getBranch().getId();
			this.branchName = model.getBranch().getBranchName();
		}
		this.taskName = model.getTaskName();
		this.taskType = model.getTaskType();
		this.successor = model.getSuccessor();

		this.successorType = model.getSuccessorType();
		this.createdBy = model.getCreatedBy();
		this.updatedBy = model.getUpdatedBy();
		this.created_date = model.getCreated_date();
		this.updated_date = model.getUpdated_date();
	}
}
