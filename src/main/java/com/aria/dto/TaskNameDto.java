package com.aria.dto;

import com.aria.model.TaskName;
import com.aria.model.TrainingType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskNameDto {

	private Long id;

	private String taskName;

	private Long taskId;
	
	private Long taskCategoryId;

	public TaskNameDto(TaskName taskNameObj) {

		this.id = taskNameObj.getId();
		this.taskName = taskNameObj.getTaskName();
		this.taskId = taskNameObj.getTaskId();
		if(taskNameObj.getTaskCategroyDet()!=null) {
			this.taskCategoryId = taskNameObj.getTaskCategroyDet().getId();
		}
		
	}

}
