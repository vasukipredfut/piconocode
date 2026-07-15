package com.aria.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.aria.dto.TaskGroupMasterDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "task_group_master")
public class TaskGroupMaster {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "task_group_name")
	private String taskGroupName;

	@Column(name = "created_by", updatable = false)
	private Long createdBy;

	@Column(name = "updated_by")
	private Long updatedBy;

	@Column(updatable = false)
	private LocalDateTime created_date;

	@Column
	private LocalDateTime updated_date;

	public TaskGroupMaster(String taskGroupName,Long userId) {
		this.taskGroupName = taskGroupName;
		this.createdBy = userId;
		this.created_date = LocalDateTime.now();

	}

	public TaskGroupMaster(TaskGroupMasterDto model) {

		this.id = model.getId();
		this.taskGroupName = model.getTaskGroupName();
	
		this.createdBy = model.getCreatedBy();
		this.updatedBy = model.getUpdatedBy();
		this.created_date = model.getCreated_date();
		this.updated_date = model.getUpdated_date();
		
	}
}
