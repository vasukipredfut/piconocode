package com.aria.dto;

import java.time.LocalDateTime;
import com.aria.model.Action;
import com.aria.utils.CustomConfiguration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ActionDto {

	private Long id;

	private Long deptID;

	private Long branchId;

	private String branchName;

	private String deptName;

	private Long actionConfigId;

	private String taskDescription;

	private String uniqueRef;

	private Long uniqueRefId;

	private String uniqueRefDescription;

	private String refType;

	private Long quantity;

	private Long ownerId;

	private String ownerName;

	private String status;
	
	private CustomConfiguration customConfiguration;

	private Long createdBy;

	private Long updatedBy;

	private LocalDateTime created_date;

	private LocalDateTime updated_date;

	public ActionDto(Action model) {
		this.id = model.getId();

		if (model.getDepartmentDetails() != null) {
			this.deptID = model.getDepartmentDetails().getId();
			this.deptName = model.getDepartmentDetails().getName();
		}
		if (model.getBranch() != null) {
			this.branchId = model.getBranch().getId();
			this.branchName = model.getBranch().getBranchName();
		}

		if (model.getOwner() != null) {
			this.ownerId = model.getOwner().getId();
			this.ownerName = model.getOwner().getEmployeename();
		}
		
		
		this.customConfiguration = model.getCustomConfiguration();
//		this.actionConfigId = model.getTaskId();
//		this.customConfiguration = model.getCustomConfiguration() != null
//                ? model.getCustomConfiguration().stream().map(field -> new Acti(field)).collect(Collectors.toList()) : null;

		this.taskDescription = model.getTaskDescription();
		this.uniqueRef = model.getUniqueRef();
		this.uniqueRefId = model.getUniqueRefId();
		this.uniqueRefDescription = model.getUniqueRefDescription();
		this.quantity = model.getQuantity();
		this.status = model.getStatus();
		this.refType = model.getRefType();
		this.createdBy = model.getCreatedBy();
		this.updatedBy = model.getUpdatedBy();
		this.created_date = model.getCreated_date();
		this.updated_date = model.getUpdated_date();
	}
}
