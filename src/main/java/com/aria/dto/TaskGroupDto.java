package com.aria.dto;

import com.aria.model.TaskGroup;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskGroupDto {

    private Long id;
    private Long taskgroup;

    private String taskGroupName;

    private Long taskId;
    private String taskName;

    private Long taskCategoryId;
    private String taskCategoryName;
    private Long departmentId;
    private String modeOfEntry;


    public TaskGroupDto(Long id, String groupName, Long taskGroup) {
        this.id = id;
        this.taskGroupName = groupName;
        this.taskgroup = taskGroup;
    }

    public TaskGroupDto(TaskGroup taskGroup) {

        this.id = taskGroup.getId();
        this.taskgroup = taskGroup.getTaskgroup();
        this.taskGroupName = taskGroup.getTaskGroupName();

        if (taskGroup.getTaskName() != null) {
            this.taskId = taskGroup.getTaskName().getId();
            this.taskName = taskGroup.getTaskName().getTaskName();
        }
        if (taskGroup.getTaskCategory() != null) {
            this.taskCategoryId = taskGroup.getTaskCategory().getId();
            this.taskCategoryName = taskGroup.getTaskCategory().getTaskCategoryName();
            if (taskGroup.getTaskCategory().getInw_ins_flag() != null && taskGroup.getTaskCategory().getInp_ins_flag() != null) {
                if (!taskGroup.getTaskCategory().getInw_ins_flag().equalsIgnoreCase("1") && !taskGroup.getTaskCategory().getInp_ins_flag().equalsIgnoreCase("1")) {
                    this.modeOfEntry = "M";
                } else {
                    this.modeOfEntry = "I";
                }
            }
        }

        if (taskGroup.getDept() != null) {
            this.departmentId = taskGroup.getDept();
        }

    }

}
