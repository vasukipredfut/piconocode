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

import com.aria.dto.ActionConfigurationDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "action_configurations")
public class ActionConfiguration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column
	private String taskName;

	@Column
	private String taskType;

	@Column
	private String successor;

	@Column
	private String successorType;

	@Column
	private String levelOfAccess;

	@OneToOne
	@JoinColumn(name = "department_id", referencedColumnName = "id", nullable = false)
	private DepartmentDetails departmentDetails;

	@OneToOne
	@JoinColumn(name = "branch_id", referencedColumnName = "id", nullable = false)
	private Branch branch;

	@Column(updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public ActionConfiguration(ActionConfigurationDto model) {

		this.id = model.getId();

		if (model.getDeptID() != null) {
			DepartmentDetails departmentDetails = new DepartmentDetails();
			departmentDetails.setId(model.getDeptID());
			this.departmentDetails = departmentDetails;
		}
		if (model.getBranchId() != null) {
			Branch branch = new Branch();
			branch.setId(model.getBranchId());
			this.branch = branch;
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
