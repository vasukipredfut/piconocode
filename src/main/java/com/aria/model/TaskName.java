package com.aria.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.aria.dto.TaskNameDto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "task_name")
public class TaskName {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "task_name")
	private String taskName;

	@Column(name = "task_id")
	private Long taskId;
	
	@OneToOne
	@JoinColumn(name = "task_category_id", referencedColumnName = "id")
	private TaskCategory taskCategroyDet;

	@Column(name = "taskcategory")
	private String taskCategoryName;

	/*@Column(name = "purpose")
	private String purpose;

	@Column(name = "tools")
	private String tools;

	@Column(name = "consumables")
	private String consumables;

	@Column(name = "workinst")
	private String workinst;

	@Column(name = "safetyinst")
	private String safetyinst;

	@Column(name = "benifit")
	private String benifit;*/

	@Column(name = "author")
	private String author;

	@Column(name = "createdon")
	private Date createdon;


	@Column(name = "final")
	private String finals;

	public TaskName(TaskNameDto taskNameDto) {

		this.id = taskNameDto.getId();

		this.taskName = taskNameDto.getTaskName();

		this.taskId = taskNameDto.getTaskId();

		if(taskNameDto.getTaskCategoryId()!=null) {
			TaskCategory tc= new TaskCategory();
			tc.setId(taskNameDto.getTaskCategoryId());
			this.taskCategroyDet = tc;
		}
		
		
	}
}
