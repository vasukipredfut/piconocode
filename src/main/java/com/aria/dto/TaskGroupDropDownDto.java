package com.aria.dto;

import com.aria.model.TaskGroup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskGroupDropDownDto {

	private Long id;
	private String taskGroupName;
}
