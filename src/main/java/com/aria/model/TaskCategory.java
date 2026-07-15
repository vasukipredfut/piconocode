package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.aria.dto.TaskCategoryDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "task_category")
public class TaskCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "task_Category_Name")
	private String taskCategoryName;
	
	@Column
	private String inw_ins_flag;
	
	@Column
	private String inp_ins_flag;

	public TaskCategory(TaskCategoryDto taskCategoryDto) {

		this.id = taskCategoryDto.getId();

		this.taskCategoryName = taskCategoryDto.getTaskCategoryName();
		
		this.inp_ins_flag = taskCategoryDto.getInp_ins_flag();
		
		this.inw_ins_flag = taskCategoryDto.getInw_ins_flag();
	}
}
