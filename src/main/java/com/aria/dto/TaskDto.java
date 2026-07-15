package com.aria.dto;

import com.aria.model.Task;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Column;

@Getter
@Setter
@NoArgsConstructor
public class TaskDto {

    private Long id;

    private String taskName;

    public TaskDto(Task task) {
        this.id = task.getId();
        this.taskName = task.getTaskName();
    }
}
