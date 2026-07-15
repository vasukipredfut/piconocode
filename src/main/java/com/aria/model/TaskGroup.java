package com.aria.model;

import jakarta.persistence.*;

import com.aria.dto.TaskGroupDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "task_group")
public class TaskGroup {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "task_Group_Name")
	private String taskGroupName;

	@Column
	private Long taskgroup;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "taskcategory", referencedColumnName = "id")
	private TaskCategory taskCategory;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "task", referencedColumnName = "id")
	private TaskName taskName;

	@Column
	private Long cluster;

	@Column
	private Long dept;

	@Column
	private Long plant;

	@Column(name = "final")
	private String finals;

	public TaskGroup(TaskGroupDto taskGroupDto) {

		this.id = taskGroupDto.getId();
		this.taskgroup=taskGroupDto.getTaskgroup();

		this.taskGroupName = taskGroupDto.getTaskGroupName();

		if (taskGroupDto.getTaskId() != null) {
			TaskName t = new TaskName();
			t.setId(taskGroupDto.getTaskId());
			this.taskName = t;
		}

		if (taskGroupDto.getTaskCategoryId() != null) {
			TaskCategory tc = new TaskCategory();
			tc.setId(taskGroupDto.getTaskCategoryId());
			this.taskCategory = tc;
		}
		if(taskGroupDto.getDepartmentId()!=null){
			this.dept=taskGroupDto.getDepartmentId();
		}

	}
}
