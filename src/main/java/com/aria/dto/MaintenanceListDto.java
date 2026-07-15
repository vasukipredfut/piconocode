package com.aria.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MaintenanceListDto {


    private Long taskId;

    private String taskName;

    private Long taskGroupId;

    private String taskGroupName;

    private Long taskCategoryId;

    private String taskCategoryName;

    private Long taskGroupMapId;

    private String modeOfEntry = "m";


    public MaintenanceListDto( MaintenanceList maintenance) {
        this.taskId = maintenance.getTaskId();
        this.taskName =  maintenance.getTaskName();
        this.taskGroupId =  maintenance.getTaskGroupId();
        this.taskGroupName =  maintenance.getTaskGroupName();
        this.taskCategoryId =  maintenance.getTaskCategoryId();
        this.taskCategoryName = maintenance.getTaskCategoryName();
        this.taskGroupMapId =  maintenance.getTaskGroupMapId();
        this.modeOfEntry =  maintenance.getTaskCategoryName();
    }
}
