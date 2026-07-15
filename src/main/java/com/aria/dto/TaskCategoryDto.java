package com.aria.dto;


import com.aria.model.TaskCategory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskCategoryDto {

	private Long id;

	private String taskCategoryName;
	
	
	private String inw_ins_flag;
	
	
	private String inp_ins_flag;

	public TaskCategoryDto(TaskCategory taskCategory) {

		this.id = taskCategory.getId();
		this.taskCategoryName = taskCategory.getTaskCategoryName();
		this.inp_ins_flag = taskCategory.getInp_ins_flag();
		this.inw_ins_flag = taskCategory.getInw_ins_flag();
	}

}
