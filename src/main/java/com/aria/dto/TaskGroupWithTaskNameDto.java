package com.aria.dto;

import com.aria.model.TaskGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskGroupWithTaskNameDto {
	private String taskGroupName;
	private Long taskId;
	private Long taskCategoryId;
	private Long taskGroup;
}
