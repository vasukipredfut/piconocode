package com.aria.dto;

import java.time.LocalDateTime;

import com.aria.model.TaskGroupMaster;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskGroupMasterDto {

	private Long id;

	private String taskGroupName;
	
	
	private Long createdBy;

	
	private Long updatedBy;

	
	private LocalDateTime created_date;

	
	private LocalDateTime updated_date;

	public TaskGroupMasterDto(TaskGroupMaster model) {

		this.id = model.getId();
		this.taskGroupName = model.getTaskGroupName();
	
		this.createdBy = model.getCreatedBy();
		this.updatedBy = model.getUpdatedBy();
		this.created_date = model.getCreated_date();
		this.updated_date = model.getUpdated_date();
		
	}

}
